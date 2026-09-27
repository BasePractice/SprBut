/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m28.dto;

/**
 * Анкета из формы: то, что прислал клиент.
 *
 * <p>Форма общая для анкеты и для страницы администратора, поэтому в ней
 * есть роль. Клиент прислать её может, но не должен: в своей анкете
 * роль назначает сервер.</p>
 *
 * <p>Привычная защита от подмены полей, {@code setAllowedFields}, для
 * записи не работает. Запись связывается через конструктор, а список
 * разрешённых полей действует только при связывании через сеттеры и
 * поля: конструктор получит {@code role=ADMIN}, как бы связыватель ни
 * был настроен. Роль отвергает {@code RoleGuard}.</p>
 *
 * @param name Имя
 * @param city Город
 * @param role Роль, которую клиент пытался назначить себе сам
 * @since 1.0
 */
public record ProfileForm(String name, String city, String role) {
}
