package ru.sprbut.m09;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m09.model.UserDto;
import ru.sprbut.m09.model.UserEntity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

@DisplayName("Слайды 73–75: три механизма, один результат")
final class MappersTest {

    private static UserEntity entity() {
        return new UserEntity("U-1", "Иван", "Иванов", 42, true);
    }

    @Test
    @DisplayName("рефлексия выводит правила маппинга из метаданных, а не из кода")
    void discoversRulesItself() {
        assertThat(
            "reflective mapper cannot discover the properties itself",
            new ReflectiveMapper().discoveredProperties(),
            greaterThan(0)
        );
    }

    @Test
    @DisplayName("рефлексивный маппинг даёт правильный результат")
    void mapsReflectively() {
        assertThat(
            "reflective mapping cannot copy the first name",
            new ReflectiveMapper().toDto(entity()).getFirstName(),
            equalTo("Иван")
        );
    }

    @Test
    @DisplayName("null на входе — null на выходе")
    void handlesNull() {
        assertThat(
            "null entity cannot yield a null dto",
            new ReflectiveMapper().toDto(null),
            nullValue()
        );
    }

    @Test
    @DisplayName("сгенерированный код даёт тот же результат, что и рефлексия")
    void agreesWithGeneratedStyle() {
        assertThat(
            "generated mapper cannot agree with the reflective one",
            new GeneratedStyleMapper().toDto(entity()),
            equalTo(new ReflectiveMapper().toDto(entity()))
        );
    }

    @Test
    @DisplayName("класс байткодного маппера собирается в runtime — в исходниках его нет")
    void generatesClassAtRuntime() {
        assertThat(
            "bytecode mapper class cannot be generated at runtime",
            new BytecodeMapper().mapper().getClass().getName(),
            containsString("bytebuddy")
        );
    }

    @Test
    @DisplayName("сгенерированный класс — полноценная реализация интерфейса")
    void generatedClassWorks() {
        assertThat(
            "generated class cannot implement the contract",
            new BytecodeMapper().mapper().toDto(entity()),
            equalTo(new GeneratedStyleMapper().toDto(entity()))
        );
    }

    @Test
    @DisplayName("каждый вызов даёт новый загруженный класс")
    void loadsNewClassEachTime() {
        assertThat(
            "each generation cannot produce its own loaded class",
            new BytecodeMapper().mapper().getClass(),
            not(equalTo(new BytecodeMapper().mapper().getClass()))
        );
    }

    @Test
    @DisplayName("байткод проксирует класс без интерфейса — то, чего не умеет JDK-прокси")
    void proxiesClassWithoutInterface() {
        assertThat(
            "class without an interface cannot be proxied by a subclass",
            new BytecodeMapper().proxied().getClass().getSuperclass(),
            equalTo(AuditService.class)
        );
    }

    @Test
    @DisplayName("цель интерфейсов не реализует вовсе")
    void keepsTargetInterfaceFree() {
        assertThat(
            "target cannot stay free of interfaces",
            AuditService.class.getInterfaces().length,
            equalTo(0)
        );
    }

    @Test
    @DisplayName("перехват срабатывает, а оригинальный метод всё равно вызывается")
    void interceptsAndDelegates() {
        AuditService proxied = new BytecodeMapper().proxied();
        proxied.record("вход");
        assertThat(
            "interceptor cannot record the call",
            new Intercepted().entries(),
            hasItem(containsString("Enhanced"))
        );
    }

    @Test
    @DisplayName("оригинальный метод возвращает своё значение, несмотря на перехват")
    void keepsOriginalResult() {
        assertThat(
            "original method cannot keep its own result",
            new BytecodeMapper().proxied().record("вход"),
            equalTo("записано: вход")
        );
    }

    @Test
    @DisplayName("каждая реализация называет свою стратегию сама")
    void namesItsOwnStrategy() {
        assertThat(
            "reflective mapper cannot name its own strategy",
            new ReflectiveMapper().strategy(),
            containsString("reflection")
        );
    }

    @Test
    @DisplayName("копия совпадает с оригиналом по всем полям")
    void copiesEveryField() {
        UserDto dto = new GeneratedStyleMapper().toDto(entity());
        assertThat(
            "generated mapping cannot copy the boolean field",
            dto.isActive(),
            equalTo(true)
        );
    }
}
