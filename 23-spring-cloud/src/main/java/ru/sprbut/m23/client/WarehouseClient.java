/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m23.client;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Слайды 224–225: балансировка на стороне клиента и сборка клиента из интерфейса.
 *
 * <p>В адресе стоит имя сервиса, а не хост: {@code http://warehouse/...}.
 * Подставить настоящий адрес — работа {@code @LoadBalanced}: он берёт список
 * экземпляров из реестра и выбирает один. Балансировщик живёт в вызывающем,
 * а не отдельной машиной посередине — отсюда и название.</p>
 *
 * <p>Реализацию {@link WarehouseApi} собирает {@code HttpServiceProxyFactory}
 * динамическим прокси. Курс замыкается: то же средство, что в модуле 04
 * показывалось на игрушечном примере, здесь делает настоящую работу.</p>
 *
 * @since 1.0
 */
@Configuration
public class WarehouseClient {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public WarehouseClient() {
        // нечего инициализировать
    }

    /**
     * Слайд 224: сборщик клиентов, умеющий превращать имя сервиса в адрес.
     * @return Сборщик клиентов, умеющий превращать имя сервиса в адрес
     */
    @Bean
    @LoadBalanced
    public RestClient.Builder builder() {
        return RestClient.builder();
    }

    /**
     * Слайд 225: реализация интерфейса, которой никто не писал.
     * @param builder Сборщик клиентов
     * @return Реализация интерфейса, которой никто не писал
     */
    @Bean
    public WarehouseApi warehouse(final RestClient.Builder builder) {
        return HttpServiceProxyFactory
            .builderFor(
                RestClientAdapter.create(builder.baseUrl("http://warehouse").build())
            )
            .build()
            .createClient(WarehouseApi.class);
    }
}
