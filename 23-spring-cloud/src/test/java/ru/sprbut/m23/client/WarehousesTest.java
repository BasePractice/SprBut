/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m23.client;

import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.sprbut.m23.registry.Warehouses;

/**
 * Слайды 223–225: адрес соседа берётся из реестра, клиент собирается из интерфейса.
 * @since 1.0
 */
@SpringBootTest
@DisplayName("Слайды 223–225: адрес соседа берётся из реестра, клиент — из интерфейса")
final class WarehousesTest {

    /**
     * Склады.
     */
    @Autowired
    private Warehouses warehouses;

    /**
     * Склад по ту сторону сети.
     */
    @Autowired
    private WarehouseApi warehouse;

    @Test
    @DisplayName("реестр знает сервис по имени, а не по адресу")
    void knowsServiceByName() {
        MatcherAssert.assertThat(
            "реестр не знает сервиса, объявленного в настройках",
            this.warehouses.names(),
            Matchers.hasItem("warehouse")
        );
    }

    @Test
    @DisplayName("одно имя разворачивается в несколько живых экземпляров")
    void resolvesNameToInstances() {
        MatcherAssert.assertThat(
            "имя сервиса не развернулось в список экземпляров",
            this.warehouses.uris(),
            Matchers.hasSize(2)
        );
    }

    @Test
    @DisplayName("адрес экземпляра приходит из настроек реестра, а не из кода")
    void takesAddressFromRegistry() {
        MatcherAssert.assertThat(
            "адрес экземпляра не совпал с объявленным в реестре",
            this.warehouses.uris(),
            Matchers.hasItem(URI.create("http://localhost:8091"))
        );
    }

    @Test
    @DisplayName("реализация интерфейса появляется без единой написанной строки")
    void buildsClientFromInterface() {
        MatcherAssert.assertThat(
            "интерфейс клиента не превратился в рабочий объект",
            this.warehouse,
            Matchers.instanceOf(WarehouseApi.class)
        );
    }
}
