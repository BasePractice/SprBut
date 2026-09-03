/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m27.remote;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Сборка клиента к соседнему сервису.
 *
 * <p>В адресе стоит имя сервиса, а не хост: {@code http://tracker-board}.
 * Настоящий адрес подставляет {@code @LoadBalanced}, беря список экземпляров
 * из реестра. Балансировщик живёт в вызывающем — отсюда и название
 * «клиентская балансировка».</p>
 *
 * <p>Соседа в этом репозитории нет и не будет: модуль показывает не удачный
 * вызов, а неудачный. Отказ соседа — нормальный режим работы, и трекер должен
 * его пережить.</p>
 *
 * @since 1.0
 */
@Configuration
public class BoardClient {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public BoardClient() {
        // нечего инициализировать
    }

    /**
     * Сборщик клиентов, умеющий превращать имя сервиса в адрес.
     * @return Сборщик клиентов, умеющий превращать имя сервиса в адрес
     */
    @Bean
    @LoadBalanced
    public RestClient.Builder builder() {
        return RestClient.builder();
    }

    /**
     * Реализация интерфейса, которой никто не писал.
     * @param builder Сборщик клиентов
     * @return Реализация интерфейса, которой никто не писал
     */
    @Bean
    public BoardApi notices(final RestClient.Builder builder) {
        return HttpServiceProxyFactory
            .builderFor(
                RestClientAdapter.create(builder.baseUrl("http://tracker-board").build())
            )
            .build()
            .createClient(BoardApi.class);
    }
}
