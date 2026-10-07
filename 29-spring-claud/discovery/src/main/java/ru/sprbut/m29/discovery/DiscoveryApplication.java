/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Реестр, в котором сервисы объявляют свои адреса и находят друг друга.
 * @since 1.0
 */
@SpringBootApplication(proxyBeanMethods = false)
@EnableEurekaServer
public final class DiscoveryApplication {

    /**
     * Конструктор для Spring.
     */
    private DiscoveryApplication() {
    }

    /**
     * Запуск реестра.
     * @param args Аргументы командной строки
     */
    public static void main(final String... args) {
        SpringApplication.run(DiscoveryApplication.class, args);
    }
}
