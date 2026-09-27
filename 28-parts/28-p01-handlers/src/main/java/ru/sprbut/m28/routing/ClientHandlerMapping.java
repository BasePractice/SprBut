/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle ProtectedMethodInFinalClassCheck disable
package ru.sprbut.m28.routing;

import java.lang.reflect.Method;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Таблица маршрутов, которая знает про {@link Client}.
 *
 * <p>{@code RequestMappingHandlerMapping} при старте обходит контроллеры
 * и собирает из аннотаций таблицу «условия запроса — метод». Для
 * собственных условий в нём оставлены две точки расширения:
 * {@code getCustomTypeCondition} для класса и
 * {@code getCustomMethodCondition} для метода. Здесь используется
 * вторая.</p>
 *
 * <p>Наследование здесь вынужденное: таблицу маршрутов создаёт
 * автоконфигурация, и подменить её можно только своим наследником,
 * переданным через {@code WebMvcRegistrations}.</p>
 *
 * @since 1.0
 */
public final class ClientHandlerMapping extends RequestMappingHandlerMapping {

    /**
     * Открытый конструктор: экземпляр создаёт {@code ClientRegistrations}.
     */
    public ClientHandlerMapping() {
        super();
    }

    @Override
    protected RequestCondition<?> getCustomMethodCondition(final Method method) {
        final Client client = AnnotatedElementUtils.findMergedAnnotation(method, Client.class);
        final RequestCondition<?> condition;
        if (client == null) {
            condition = null;
        } else {
            condition = new ClientCondition(client.value());
        }
        return condition;
    }
}
