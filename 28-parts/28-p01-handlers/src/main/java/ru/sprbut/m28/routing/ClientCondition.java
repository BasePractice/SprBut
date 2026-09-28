/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m28.routing;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.springframework.web.servlet.mvc.condition.RequestCondition;

/**
 * Условие выбора обработчика по заголовку {@code X-Client}.
 *
 * <p>У условия три обязанности, и каждая отвечает на свой вопрос
 * диспетчера. {@code getMatchingCondition} — подходит ли запрос; ответ
 * «нет» выражается через {@code null}, так устроен контракт.
 * {@code combine} — как соединить условие класса с условием метода.
 * {@code compareTo} — какое из двух подходящих условий точнее.</p>
 *
 * <p>Метод с условием побеждает метод без условия на том же пути: так
 * диспетчер сравнивает свои условия, когда одно из них отсутствует.
 * Поэтому общий метод и метод для мобильного клиента живут на одном
 * адресе, и ни один не мешает другому.</p>
 *
 * @since 1.0
 */
public final class ClientCondition implements RequestCondition<ClientCondition> {

    /**
     * Вид клиента, который обслуживает метод.
     */
    private final String kind;

    /**
     * Основной конструктор.
     * @param kind Вид клиента, который обслуживает метод
     * @checkstyle ConstructorsCodeFreeCheck (8 lines)
     */
    public ClientCondition(final @NonNull String kind) {
        this.kind = Objects.requireNonNull(kind, "вид клиента не задан");
    }

    @Override
    public ClientCondition combine(final @NonNull ClientCondition other) {
        return other;
    }

    @Override
    public ClientCondition getMatchingCondition(final HttpServletRequest request) {
        final ClientCondition matching;
        if (this.kind.equalsIgnoreCase(request.getHeader("X-Client"))) {
            matching = this;
        } else {
            matching = null;
        }
        return matching;
    }

    @Override
    public int compareTo(final @NonNull ClientCondition other, final @NonNull HttpServletRequest request) {
        return 0;
    }
}
