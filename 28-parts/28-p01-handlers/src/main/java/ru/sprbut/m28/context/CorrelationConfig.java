/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m28.context;

import java.util.Objects;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Бин, который живёт столько же, сколько запрос.
 *
 * <p>Участники обработки передают друг другу данные через атрибуты
 * запроса: {@code CorrelationFilter} кладёт идентификатор, контроллер
 * забирает его через {@code @RequestAttribute}. Но атрибут доступен
 * тому, у кого в руках запрос. Сервису в глубине вызовов запрос не
 * передают, и для него есть два других способа.</p>
 *
 * <p>{@code RequestContextHolder} — это {@code ThreadLocal}, куда Spring
 * кладёт текущий запрос; отсюда его может достать кто угодно в том же
 * потоке. {@code @RequestScope} идёт дальше: создаёт бин на каждый
 * запрос и внедряет в одиночки прокси, который при каждом вызове
 * находит экземпляр текущего запроса.</p>
 *
 * <p>Атрибута может не оказаться: запрос, не прошедший через
 * {@code CorrelationFilter}, его не несёт. {@code String.valueOf}
 * превратил бы такое в идентификатор «null» и спрятал ошибку; здесь
 * она всплывает сразу.</p>
 *
 * <p>По умолчанию прокси строится наследованием объявленного типа бина.
 * Здесь это интерфейс, и прокси обошёлся бы без наследования и так;
 * {@code proxyMode = INTERFACES} делает это явным. Стоит объявить метод
 * возвращающим саму запись {@code RequestCorrelation} — и контекст не
 * поднимется: запись {@code final}, унаследовать её нельзя.</p>
 *
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
public final class CorrelationConfig {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public CorrelationConfig() {
        // нечего инициализировать
    }

    /**
     * Идентификатор текущего запроса, взятый из атрибута.
     * @return Идентификатор
     */
    @Bean
    @RequestScope(proxyMode = ScopedProxyMode.INTERFACES)
    public Correlation correlation() {
        return new RequestCorrelation(
            Objects.requireNonNull(
                RequestContextHolder.currentRequestAttributes().getAttribute(
                    "sprbut.correlation", RequestAttributes.SCOPE_REQUEST
                ),
                "в запросе нет атрибута sprbut.correlation, CorrelationFilter его не выставил"
            ).toString()
        );
    }
}
