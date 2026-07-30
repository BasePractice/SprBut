package ru.sprbut.m10.lombok;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m10.lombok.samples.Account;
import ru.sprbut.m10.lombok.samples.Fluent;
import ru.sprbut.m10.lombok.samples.Order;
import ru.sprbut.m10.lombok.samples.Partial;
import ru.sprbut.m10.lombok.samples.Service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;

@DisplayName("Слайд 79–82: Lombok дописывает класс на этапе компиляции")
final class GeneratedTest {

    @Test
    @DisplayName("@Data порождает геттеры и сеттеры, которых нет в исходниках")
    void generatesAccessors() {
        assertThat(
            "@Data cannot generate the accessors",
            new Generated(CustomerEntity.class).methods(),
            hasItems("getId", "setId")
        );
    }

    @Test
    @DisplayName("@Data порождает equals, hashCode и toString")
    void generatesObjectMethods() {
        assertThat(
            "@Data cannot generate the Object methods",
            new Generated(CustomerEntity.class).methods(),
            hasItems("equals", "hashCode", "toString")
        );
    }

    @Test
    @DisplayName("@Value делает все поля финальными")
    void makesFieldsFinal() {
        assertThat(
            "@Value cannot make every field final",
            new Generated(CustomerDto.class).immutable(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("у неизменяемого класса сеттеров нет вовсе")
    void dontGenerateSettersForValue() {
        assertThat(
            "@Value cannot avoid generating setters",
            new Generated(CustomerDto.class).mutable(),
            equalTo(false)
        );
    }

    @Test
    @DisplayName("класс с @Data подчиняется соглашению JavaBeans")
    void staysJavaBean() {
        assertThat(
            "@Data class cannot satisfy the JavaBeans convention",
            new Generated(CustomerEntity.class).javaBean(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("уровень доступа сеттера задаётся аннотацией")
    void appliesAccessLevel() {
        assertThat(
            "access level cannot reach the generated setter",
            new Generated(Partial.class).access("setVisible"),
            equalTo("protected")
        );
    }

    @Test
    @DisplayName("@Getter(NONE) отключает геттер для одного поля")
    void skipsDisabledGetter() {
        assertThat(
            "disabled getter cannot stay ungenerated",
            new Generated(Partial.class).methods(),
            not(hasItem("getHidden"))
        );
    }

    @Test
    @DisplayName("@RequiredArgsConstructor берёт только final-поля")
    void buildsConstructorFromFinalFields() {
        assertThat(
            "required args constructor cannot take only the final fields",
            new Generated(Service.class).constructors(),
            contains(2)
        );
    }

    @Test
    @DisplayName("@EqualsAndHashCode(of = id) сравнивает только по идентификатору")
    void comparesByIdOnly() {
        assertThat(
            "equality cannot be limited to the identifier",
            new Account("A-1", "ivanov", "секрет"),
            equalTo(new Account("A-1", "другой-логин", "другой-пароль"))
        );
    }

    @Test
    @DisplayName("@ToString(exclude) не пускает пароль в логи")
    void hidesExcludedField() {
        assertThat(
            "excluded field cannot stay out of toString",
            new Account("A-1", "ivanov", "секрет").toString(),
            not(containsString("секрет"))
        );
    }

    @Test
    @DisplayName("@Singular наполняет коллекцию по одному элементу")
    void fillsCollectionOneByOne() {
        assertThat(
            "singular builder cannot add items one by one",
            Order.builder().number("O-1").item("хлеб").item("молоко").build().getItems(),
            contains("хлеб", "молоко")
        );
    }

    @Test
    @DisplayName("@Accessors(fluent) лишает класс статуса JavaBean")
    void breaksJavaBeanConvention() {
        assertThat(
            "fluent accessors cannot break the JavaBeans convention",
            new Generated(Fluent.class).javaBean(),
            equalTo(false)
        );
    }

    @Test
    @DisplayName("fluent-аксессоры работают, просто называются иначе")
    void keepsFluentAccessorsWorking() {
        assertThat(
            "fluent accessor cannot work under its own name",
            new Fluent().name("тест").name(),
            equalTo("тест")
        );
    }
}
