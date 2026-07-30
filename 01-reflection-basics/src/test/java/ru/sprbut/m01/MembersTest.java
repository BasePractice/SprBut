package ru.sprbut.m01;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m01.model.Account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;

@DisplayName("Слайд 6: getDeclaredXxx против getXxx")
final class MembersTest {

    @Test
    @DisplayName("getDeclaredFields() видит и приватные поля тоже")
    void listsDeclaredFields() {
        assertThat(
            "declared fields cannot include the private ones",
            new Members(Account.class).declaredFields(),
            containsInAnyOrder("TYPE", "id", "owner", "balance", "blocked", "cachedLabel")
        );
    }

    @Test
    @DisplayName("getFields() показывает только публичные поля")
    void listsPublicFieldsOnly() {
        assertThat(
            "public fields cannot be limited to the public ones",
            new Members(Account.class).publicFields(),
            contains("TYPE")
        );
    }

    @Test
    @DisplayName("приватные поля отбираются по флагу модификатора")
    void filtersPrivateFields() {
        assertThat(
            "private fields cannot be filtered by their modifier",
            new Members(Account.class).privateFields(),
            containsInAnyOrder("id", "owner", "balance")
        );
    }

    @Test
    @DisplayName("статические поля отбираются по флагу модификатора")
    void filtersStaticFields() {
        assertThat(
            "static fields cannot be filtered by their modifier",
            new Members(Account.class).staticFields(),
            contains("TYPE")
        );
    }

    @Test
    @DisplayName("getDeclaredMethods() перечисляет и приватные методы")
    void listsPrivateMethods() {
        assertThat(
            "declared methods cannot include the private ones",
            new Members(Account.class).declaredMethods(),
            hasItems("applyFee", "block", "describeType")
        );
    }

    @Test
    @DisplayName("унаследованные методы Object в объявленные не попадают")
    void dontListInheritedMethods() {
        assertThat(
            "declared methods cannot leave the inherited ones out",
            new Members(Account.class).declaredMethods(),
            not(hasItems("equals", "hashCode"))
        );
    }
}
