package ru.sprbut.m02.classic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m02.modern.CustomerRecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Слайд 16: java.beans.Introspector — штатная работа с соглашением")
class IntrospectionExampleTest {

    private CustomerBean bean() {
        CustomerBean bean = new CustomerBean();
        bean.setId("C-1");
        bean.setFirstName("Иван");
        bean.setLastName("Иванов");
        bean.setAge(42);
        bean.setVip(true);
        return bean;
    }

    @Test
    @DisplayName("Introspector сам находит свойства по парам методов")
    void discoversProperties() {
        assertThat(IntrospectionExample.propertyNames(CustomerBean.class))
                .containsExactly("age", "firstName", "fullName", "id", "lastName", "vip");
    }

    @Test
    @DisplayName("Свойство read-write требует и getter, и setter")
    void splitsReadWriteAndReadOnly() {
        assertThat(IntrospectionExample.readWriteProperties(CustomerBean.class))
                .containsExactly("age", "firstName", "id", "lastName", "vip");
        assertThat(IntrospectionExample.readOnlyProperties(CustomerBean.class))
                .containsExactly("fullName");
    }

    @Test
    @DisplayName("Чтение и запись свойства по строковому имени — без знания класса при компиляции")
    void readsAndWritesByName() {
        CustomerBean bean = bean();

        assertThat(IntrospectionExample.read(bean, "firstName")).isEqualTo("Иван");
        assertThat(IntrospectionExample.read(bean, "vip")).isEqualTo(true);

        IntrospectionExample.write(bean, "firstName", "Пётр");
        assertThat(bean.getFirstName()).isEqualTo("Пётр");
    }

    @Test
    @DisplayName("Запись в read-only свойство отклоняется с внятной ошибкой")
    void rejectsWriteToReadOnly() {
        assertThatThrownBy(() -> IntrospectionExample.write(bean(), "fullName", "X"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("только на чтение");
    }

    @Test
    @DisplayName("Неизвестное свойство отклоняется")
    void rejectsUnknownProperty() {
        assertThatThrownBy(() -> IntrospectionExample.read(bean(), "salary"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("salary");
    }

    @Test
    @DisplayName("Бин целиком превращается в Map свойств")
    void convertsBeanToMap() {
        assertThat(IntrospectionExample.toMap(bean()))
                .containsEntry("id", "C-1")
                .containsEntry("firstName", "Иван")
                .containsEntry("age", 42)
                .containsEntry("vip", true)
                .containsEntry("fullName", "Иван Иванов");
    }

    @Test
    @DisplayName("У record Introspector не видит ни одного свойства — аксессоры названы иначе")
    void introspectorIgnoresRecords() {
        assertThat(IntrospectionExample.propertyNames(CustomerRecord.class)).isEmpty();
    }

    @Test
    @DisplayName("Тип свойства известен заранее — на этом строится конвертация значений")
    void exposesPropertyType() {
        assertThat(IntrospectionExample.descriptor(CustomerBean.class, "age"))
                .get()
                .extracting(java.beans.PropertyDescriptor::getPropertyType)
                .isEqualTo(int.class);
    }
}
