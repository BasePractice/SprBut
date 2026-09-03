/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m23.registry;

import java.net.URI;
import java.util.List;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

/**
 * Слайд 223: сосед находится по имени, а не по адресу.
 *
 * <p>Приложение спрашивает «где склад», а не «что на 10.0.0.7:8091». Адреса
 * знает реестр, и это его единственная обязанность: он превращает имя
 * в список живых экземпляров. Переехал сервис, добавили ещё три копии,
 * упала одна — список меняется, а код вызывающего остаётся прежним.</p>
 *
 * <p>Здесь реестр простой — {@code SimpleDiscoveryClient}, читающий адреса
 * из настроек. Eureka или Consul отличаются тем, откуда берётся список
 * и как быстро он обновляется, но не тем, как им пользуются.</p>
 *
 * @since 1.0
 */
@Component
public final class Warehouses {

    /**
     * Реестр сервисов.
     */
    private final DiscoveryClient registry;

    /**
     * Основной конструктор.
     * @param registry Реестр сервисов
     */
    public Warehouses(final DiscoveryClient registry) {
        this.registry = registry;
    }

    /**
     * Адреса всех экземпляров склада, известных реестру.
     * @return Адреса всех экземпляров склада, известных реестру
     */
    public List<URI> uris() {
        return this.registry.getInstances("warehouse").stream()
            .map(ServiceInstance::getUri)
            .toList();
    }

    /**
     * Имена сервисов, о которых реестр вообще знает.
     * @return Имена сервисов, о которых реестр вообще знает
     */
    public List<String> names() {
        return List.copyOf(this.registry.getServices());
    }
}
