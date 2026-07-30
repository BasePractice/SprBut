package ru.sprbut.m02.classic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Чтение и запись свойств по строковому имени")
final class BeanValueTest {

    private static CustomerBean customer() {
        CustomerBean bean = new CustomerBean();
        bean.setId("C-1");
        bean.setFirstName("Иван");
        bean.setLastName("Петров");
        bean.setAge(33);
        bean.setVip(true);
        return bean;
    }

    @Test
    @DisplayName("свойство читается по имени, без знания класса при компиляции")
    void readsByName() {
        assertThat(
            "property cannot be read by its name",
            new BeanValue(customer(), "firstName").value(),
            equalTo("Иван")
        );
    }

    @Test
    @DisplayName("boolean-свойство читается через is-getter")
    void readsBooleanProperty() {
        assertThat(
            "boolean property cannot be read through its is-getter",
            new BeanValue(customer(), "vip").value(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("свойство пишется по имени — так контейнер заполняет бин из yaml")
    void writesByName() {
        CustomerBean bean = customer();
        new BeanValue(bean, "firstName").assign("Пётр");
        assertThat(
            "property cannot be written by its name",
            bean.getFirstName(),
            equalTo("Пётр")
        );
    }

    @Test
    @DisplayName("запись в свойство только для чтения отклоняется с внятной ошибкой")
    void dontWriteReadOnlyProperty() {
        assertThat(
            "read only property cannot reject the write",
            assertThrows(
                IllegalArgumentException.class,
                () -> new BeanValue(customer(), "fullName").assign("X")
            ).getMessage(),
            containsString("только на чтение")
        );
    }

    @Test
    @DisplayName("неизвестное свойство отклоняется")
    void dontReadUnknownProperty() {
        assertThat(
            "unknown property cannot be reported by its name",
            assertThrows(
                IllegalArgumentException.class,
                () -> new BeanValue(customer(), "salary").value()
            ).getMessage(),
            containsString("salary")
        );
    }

    @Test
    @DisplayName("бин целиком превращается в карту свойств")
    void convertsBeanToMap() {
        assertThat(
            "bean cannot be turned into a property map",
            new BeanMap(customer()).values(),
            hasEntry("lastName", "Петров")
        );
    }

    @Test
    @DisplayName("слайд 18: пустой бин заведомо невалиден — все свойства пусты")
    void createsInvalidEmptyBean() {
        assertThat(
            "empty bean cannot start with null properties",
            ((CustomerBean) new EmptyBean(CustomerBean.class).instance()).getFirstName(),
            nullValue()
        );
    }
}
