package ru.sprbut.m02.classic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m02.modern.CustomerRecord;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

@DisplayName("Слайд 14: свойство определяется методами, а не полями")
final class BeanPropertiesTest {

    @Test
    @DisplayName("getXxx и isXxx одинаково дают свойство на чтение")
    void collectsReadableProperties() {
        assertThat(
            "getter and is-getter cannot both yield a property",
            new BeanProperties(CustomerBean.class).readable(),
            hasItems("id", "firstName", "vip")
        );
    }

    @Test
    @DisplayName("getFullName() даёт свойство, которому не соответствует ни одно поле")
    void collectsPropertyWithoutField() {
        assertThat(
            "computed getter cannot yield a property of its own",
            new BeanProperties(CustomerBean.class).readable(),
            hasItem("fullName")
        );
    }

    @Test
    @DisplayName("свойство без сеттера на запись не попадает")
    void dontListComputedPropertyAsWritable() {
        assertThat(
            "computed property cannot stay out of the writable list",
            new BeanProperties(CustomerBean.class).writable(),
            not(hasItem("fullName"))
        );
    }

    @Test
    @DisplayName("метод чтения находится по имени свойства")
    void findsReader() {
        assertThat(
            "reader cannot be found by the property name",
            new BeanProperties(CustomerBean.class).reader("firstName").getName(),
            org.hamcrest.Matchers.equalTo("getFirstName")
        );
    }

    @Test
    @DisplayName("для несуществующего свойства метода чтения нет")
    void dontFindReaderForUnknownProperty() {
        assertThat(
            "unknown property cannot yield a missing reader",
            new BeanProperties(CustomerBean.class).reader("salary"),
            nullValue()
        );
    }

    @Test
    @DisplayName("у record свойств по соглашению нет — аксессоры названы иначе")
    void dontSeeRecordAccessors() {
        assertThat(
            "record accessors cannot stay invisible to the convention",
            new BeanProperties(CustomerRecord.class).readable(),
            emptyIterable()
        );
    }
}
