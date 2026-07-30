package ru.sprbut.m03.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Расширенный пример: команда-строка → реальный вызов метода")
class ReflectiveCommandRunnerTest {

    private static final String ORDER = "ru.sprbut.m03.model.Order";

    @Nested
    @DisplayName("Разбор команды")
    class Parsing {

        @Test
        @DisplayName("Имя и аргументы отделяются от скобок")
        void parsesNameAndArgs() {
            ReflectiveCommandRunner.Spec spec = ReflectiveCommandRunner.Spec.parse("addLines(10, 20)");

            assertThat(spec.name()).isEqualTo("addLines");
            assertThat(spec.args()).containsExactly("10", "20");
        }

        @Test
        @DisplayName("Пустые скобки и их отсутствие дают один и тот же результат")
        void handlesEmptyArgs() {
            assertThat(ReflectiveCommandRunner.Spec.parse("cancel()").args()).isEmpty();
            assertThat(ReflectiveCommandRunner.Spec.parse("cancel").args()).isEmpty();
        }

        @Test
        @DisplayName("Незакрытая скобка и отсутствие '#' — ошибки разбора")
        void rejectsMalformedInput() {
            assertThatThrownBy(() -> ReflectiveCommandRunner.Spec.parse("addLines(10"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Не закрыта скобка");

            assertThatThrownBy(() -> ReflectiveCommandRunner.run("Order.addLines(10)"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Ожидался формат");
        }
    }

    @Nested
    @DisplayName("Class + Constructor: создание цели")
    class TargetCreation {

        @Test
        @DisplayName("Без скобок используется конструктор без параметров")
        void usesNoArgConstructor() {
            var invocation = ReflectiveCommandRunner.run(ORDER + "#getId()");

            assertThat(invocation.type()).isEqualTo("Order");
            assertThat(invocation.constructorUsed()).isEqualTo("Order()");
            assertThat(invocation.result()).isEqualTo("UNKNOWN");
        }

        @Test
        @DisplayName("Конструктор выбирается по количеству аргументов, строки конвертируются по типам")
        void picksConstructorByArity() {
            var invocation = ReflectiveCommandRunner.run(ORDER + "(A-1,99.90)#getTotal()");

            assertThat(invocation.constructorUsed()).isEqualTo("Order(String, BigDecimal)");
            assertThat(invocation.result()).isEqualTo(new BigDecimal("99.90"));
        }

        @Test
        @DisplayName("Protected-конструктор доступен через setAccessible")
        void usesProtectedConstructor() {
            var invocation = ReflectiveCommandRunner.run(ORDER + "(A-2,Иванов,10)#getCustomer()");

            assertThat(invocation.constructorUsed()).isEqualTo("Order(String, String, BigDecimal)");
            assertThat(invocation.result()).isEqualTo("Иванов");
        }

        @Test
        @DisplayName("Неизвестный класс и абстрактный тип отклоняются с внятным сообщением")
        void rejectsBadTargets() {
            assertThatThrownBy(() -> ReflectiveCommandRunner.run("ru.sprbut.Nope#toString()"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Класс не найден");

            assertThatThrownBy(() -> ReflectiveCommandRunner.run("java.util.List#size()"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("абстрактный тип");
        }
    }

    @Nested
    @DisplayName("Method + Parameter + Array: вызов")
    class Invocation {

        @Test
        @DisplayName("Метод с одним аргументом: строка конвертируется в BigDecimal")
        void convertsArgumentByParameterType() {
            var invocation = ReflectiveCommandRunner.run(ORDER + "(A-1,10)#addLines(5.5)");

            assertThat(invocation.methodSignature()).isEqualTo("BigDecimal addLines(BigDecimal[])");
            assertThat(invocation.result()).isEqualTo(new BigDecimal("15.5"));
        }

        @Test
        @DisplayName("varargs: хвост аргументов упаковывается в массив через Array.newInstance")
        void packsVarargs() {
            var invocation = ReflectiveCommandRunner.run(ORDER + "(A-1,0)#addLines(1,2,3,4)");

            assertThat(invocation.result()).isEqualTo(new BigDecimal("10"));
        }

        @Test
        @DisplayName("varargs без аргументов — пустой массив, а не null")
        void supportsEmptyVarargs() {
            var invocation = ReflectiveCommandRunner.run(ORDER + "(A-1,7)#addLines()");

            assertThat(invocation.result()).isEqualTo(new BigDecimal("7"));
        }

        @Test
        @DisplayName("Приватный метод вызывается наравне с публичным")
        void invokesPrivateMethod() {
            var invocation = ReflectiveCommandRunner.run(ORDER + "(A-9)#internalTag()");

            assertThat(invocation.result()).isEqualTo("order-A-9");
        }

        @Test
        @DisplayName("Void-метод возвращает null, но состояние объекта меняет")
        void invokesVoidMethod() {
            var invocation = ReflectiveCommandRunner.run(ORDER + "(A-1,10)#setCustomer(Петров)");

            assertThat(invocation.methodSignature()).isEqualTo("void setCustomer(String)");
            assertThat(invocation.result()).isNull();
        }

        @Test
        @DisplayName("Checked-исключение метода приходит завёрнутым в CommandFailedException")
        void wrapsCheckedException() {
            assertThatThrownBy(() -> ReflectiveCommandRunner.run(ORDER + "(A-1,100)#pay(1)"))
                    .isInstanceOf(ReflectiveCommandRunner.CommandFailedException.class)
                    .hasMessageContaining("Недостаточная сумма")
                    .hasCauseInstanceOf(ru.sprbut.m03.model.Order.PaymentException.class);
        }

        @Test
        @DisplayName("Успешный вызов метода с checked-исключением проходит без обёртки")
        void succeedsWhenNoExceptionThrown() {
            var invocation = ReflectiveCommandRunner.run(ORDER + "(A-1,100)#pay(100)");

            assertThat(invocation.methodSignature()).isEqualTo("void pay(BigDecimal)");
        }

        @Test
        @DisplayName("Несуществующий метод и негодный аргумент отклоняются до вызова")
        void rejectsBadInvocations() {
            assertThatThrownBy(() -> ReflectiveCommandRunner.run(ORDER + "#nope()"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("нет метода 'nope'");

            assertThatThrownBy(() -> ReflectiveCommandRunner.run(ORDER + "#setCustomer(a,b,c)"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("с 3 аргументами");
        }
    }

    @Nested
    @DisplayName("Конвертация аргументов по метаданным")
    class Conversion {

        @Test
        @DisplayName("Поддерживаются примитивы, обёртки, BigDecimal, LocalDate и enum")
        void convertsCommonTypes() {
            assertThat(ArgumentConverter.convert("42", int.class)).isEqualTo(42);
            assertThat(ArgumentConverter.convert("42", Long.class)).isEqualTo(42L);
            assertThat(ArgumentConverter.convert("true", boolean.class)).isEqualTo(true);
            assertThat(ArgumentConverter.convert("1.25", BigDecimal.class))
                    .isEqualTo(new BigDecimal("1.25"));
            assertThat(ArgumentConverter.convert("2030-01-31", java.time.LocalDate.class))
                    .isEqualTo(java.time.LocalDate.of(2030, 1, 31));
            assertThat(ArgumentConverter.convert("monday", java.time.DayOfWeek.class))
                    .isEqualTo(java.time.DayOfWeek.MONDAY);
        }

        @Test
        @DisplayName("Неизвестная константа enum перечисляет допустимые варианты")
        void unknownEnumConstantListsOptions() {
            assertThatThrownBy(() -> ArgumentConverter.convert("SUNDEY", java.time.DayOfWeek.class))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("MONDAY");
        }

        @Test
        @DisplayName("Литерал null допустим только для ссылочных типов")
        void handlesNullLiteral() {
            assertThat(ArgumentConverter.convert("null", String.class)).isNull();
            assertThatThrownBy(() -> ArgumentConverter.convert("null", int.class))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("примитивный");
        }

        @Test
        @DisplayName("Неподдерживаемый тип и негодное значение дают разные ошибки")
        void reportsConversionProblems() {
            assertThatThrownBy(() -> ArgumentConverter.convert("x", java.util.List.class))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Нет конвертера");

            assertThatThrownBy(() -> ArgumentConverter.convert("много", int.class))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("не приводится");
        }
    }
}
