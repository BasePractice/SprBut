package ru.sprbut.m02.classic;

import java.io.Serializable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m02.modern.CustomerRecord;
import ru.sprbut.m02.modern.ImmutableCustomer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

@DisplayName("Слайды 12–16: проверка соглашения JavaBeans")
final class BeanVerdictTest {

    @SuppressWarnings("unused")
    private static final class NoDefaultCtor implements Serializable {

        private String name;

        NoDefaultCtor(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @SuppressWarnings("unused")
    public static final class SetterWithoutGetter implements Serializable {

        private String secret;

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }

    @SuppressWarnings("unused")
    public static final class NotSerializable {

        private String name;

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    @DisplayName("классический бин выполняет все четыре пункта соглашения")
    void acceptsClassicBean() {
        assertThat(
            "classic bean cannot satisfy the strict convention",
            new BeanVerdict(CustomerBean.class, true).violations(),
            emptyIterable()
        );
    }

    @Test
    @DisplayName("без публичного конструктора без параметров класс — не бин")
    void dontAcceptMissingNoArgConstructor() {
        assertThat(
            "class without a no-arg constructor cannot be rejected",
            new BeanVerdict(NoDefaultCtor.class).violations(),
            hasItem("нет публичного конструктора без параметров")
        );
    }

    @Test
    @DisplayName("setter без парного getter нарушает соглашение")
    void dontAcceptSetterWithoutGetter() {
        assertThat(
            "setter without a getter cannot be reported",
            new BeanVerdict(SetterWithoutGetter.class).violations(),
            hasItem("у свойства 'secret' есть setter, но нет getter")
        );
    }

    @Test
    @DisplayName("строгое соглашение требует Serializable")
    void demandsSerializableWhenStrict() {
        assertThat(
            "strict convention cannot demand Serializable",
            new BeanVerdict(NotSerializable.class, true).violations(),
            hasItem("класс не реализует Serializable")
        );
    }

    @Test
    @DisplayName("Spring того же Serializable не требует — слайд это прямо оговаривает")
    void dontDemandSerializableForSpring() {
        assertThat(
            "Spring style convention cannot ignore Serializable",
            new BeanVerdict(NotSerializable.class).valid(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("record — не JavaBean: нет ни конструктора без параметров, ни getXxx")
    void dontAcceptRecord() {
        assertThat(
            "record cannot fail the JavaBeans convention",
            new BeanVerdict(CustomerRecord.class).violations(),
            hasItem("нет публичного конструктора без параметров")
        );
    }

    @Test
    @DisplayName("неизменяемый класс с билдером тоже не бин")
    void dontAcceptImmutable() {
        assertThat(
            "immutable class cannot fail the constructor requirement",
            new BeanVerdict(ImmutableCustomer.class).constructible(),
            equalTo(false)
        );
    }
}
