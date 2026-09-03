/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m22.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Слайды 217–218: контекст безопасности и правило рядом с методом.
 *
 * <p>{@code @PreAuthorize} переносит правило доступа туда, где живёт сам
 * метод: защищён не адрес, а операция. Разница видна, когда до одного и того
 * же метода ведут два пути — из контроллера и из планировщика: правило
 * на адресе защитит только первый.</p>
 *
 * <p>Работает это ровно так же, как {@code @Transactional} из модуля 15 —
 * через прокси. Отсюда и знакомое ограничение: вызов такого метода изнутри
 * того же объекта проходит мимо прокси, а значит и мимо проверки.</p>
 *
 * @since 1.0
 */
@Service
public class Vault {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public Vault() {
        // нечего инициализировать
    }

    /**
     * Слайд 217: текущий пользователь берётся из контекста, а не из аргументов.
     *
     * <p>{@code SecurityContextHolder} хранит его в {@code ThreadLocal},
     * поэтому метод узнаёт, кто его вызвал, ничего не принимая на вход.
     * Та же привязка к потоку объясняет, почему в реактивном стеке
     * (модуль 21) контекст устроен иначе.</p>
     *
     * @return Имя текущего пользователя
     */
    public String owner() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * Слайд 218: правило доступа записано рядом с методом.
     * @return Содержимое, доступное только администратору
     */
    @PreAuthorize("hasRole('ADMIN')")
    public String secret() {
        return "код от сейфа";
    }
}
