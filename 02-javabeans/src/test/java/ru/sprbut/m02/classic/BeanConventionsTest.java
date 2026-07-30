package ru.sprbut.m02.classic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m02.modern.CustomerRecord;
import ru.sprbut.m02.modern.ImmutableCustomer;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Слайды 12–16: проверка соглашения JavaBeans")
class BeanConventionsTest {

    @SuppressWarnings("unused")
    static class NoDefaultCtor {
        private String name;

        NoDefaultCtor(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @SuppressWarnings("unused")
    static class SetterWithoutGetter {
        private String secret;

        public SetterWithoutGetter() {
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }

    /** Полноценный бин по «правилам Spring», но без {@code Serializable}. */
    @SuppressWarnings("unused")
    public static class NotSerializable {
        private String name;

        public NotSerializable() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    @DisplayName("CustomerBean выполняет все четыре пункта соглашения")
    void classicBeanIsValid() {
        BeanConventions.Verdict verdict = BeanConventions.validateStrict(CustomerBean.class);

        assertThat(verdict.valid()).isTrue();
        assertThat(verdict.violations()).isEmpty();
    }

    @Test
    @DisplayName("Без публичного конструктора без параметров класс — не бин")
    void detectsMissingNoArgConstructor() {
        assertThat(BeanConventions.hasPublicNoArgConstructor(NoDefaultCtor.class)).isFalse();
        assertThat(BeanConventions.validateSpringStyle(NoDefaultCtor.class).violations())
                .containsExactly("нет публичного конструктора без параметров");
    }

    @Test
    @DisplayName("Setter без парного getter нарушает соглашение")
    void detectsSetterWithoutGetter() {
        assertThat(BeanConventions.validateSpringStyle(SetterWithoutGetter.class).violations())
                .containsExactly("у свойства 'secret' есть setter, но нет getter");
    }

    @Test
    @DisplayName("Serializable требует строгое соглашение, но не требует Spring")
    void serializableIsOptionalForSpring() {
        assertThat(BeanConventions.validateStrict(NotSerializable.class).violations())
                .containsExactly("класс не реализует Serializable");
        assertThat(BeanConventions.validateSpringStyle(NotSerializable.class).valid()).isTrue();
    }

    @Test
    @DisplayName("Getter и is-getter одинаково дают свойство; getFullName() — свойство без поля")
    void collectsProperties() {
        assertThat(BeanConventions.readableProperties(CustomerBean.class))
                .contains("id", "firstName", "lastName", "age", "vip", "fullName");
        assertThat(BeanConventions.writableProperties(CustomerBean.class))
                .containsExactly("age", "firstName", "id", "lastName", "vip")
                .doesNotContain("fullName");
    }

    @Test
    @DisplayName("record — не JavaBean: нет ни конструктора без параметров, ни getXxx")
    void recordIsNotABean() {
        BeanConventions.Verdict verdict = BeanConventions.validateSpringStyle(CustomerRecord.class);

        assertThat(verdict.valid()).isFalse();
        assertThat(verdict.violations()).contains("нет публичного конструктора без параметров");
        assertThat(BeanConventions.readableProperties(CustomerRecord.class)).isEmpty();
    }

    @Test
    @DisplayName("Immutable + Builder тоже не бин: геттеры есть, конструктора без параметров нет")
    void immutableIsNotABean() {
        assertThat(BeanConventions.hasPublicNoArgConstructor(ImmutableCustomer.class)).isFalse();
        assertThat(BeanConventions.readableProperties(ImmutableCustomer.class))
                .contains("id", "firstName", "age", "vip", "tags");
    }

    @Test
    @DisplayName("Слайд 18: бин создаётся пустым — то есть заведомо невалидным")
    void mutabilityMeansInvalidIntermediateState() {
        CustomerBean bean = (CustomerBean) BeanConventions.instantiateEmpty(CustomerBean.class);

        assertThat(bean.getId()).isNull();
        assertThat(bean.getFirstName()).isNull();
        assertThat(bean.getFullName()).isEqualTo("null null");
    }
}
