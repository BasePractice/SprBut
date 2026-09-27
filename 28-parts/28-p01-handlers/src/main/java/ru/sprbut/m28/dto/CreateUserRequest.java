/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m28.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ru.sprbut.m28.validation.NotReserved;

/**
 * Тело запроса на создание пользователя.
 *
 * <p>Тело читает конвертер сообщений, а не контроллер: к моменту вызова
 * метода JSON уже разобран в этот объект. Между разбором и вызовом
 * успевает вмешаться совет над телом — {@code MaskingRequestBodyAdvice}.</p>
 *
 * <p>Объект неизменяемый, поэтому совет не правит поля, а собирает новый
 * экземпляр: в этом и состоит смысл возвращаемого значения
 * {@code afterBodyRead}.</p>
 *
 * <p>Аннотации проверки — метаданные, и сами по себе они ничего не
 * запрещают: работать их заставляет {@code @Valid} в контроллере, а
 * нарушение превращает в ответ {@code Failures}. Порядок участников виден
 * и здесь: совет успевает замаскировать почту до того, как её увидит
 * проверка.</p>
 *
 * <p>{@code @NotReserved} — правило, написанное в модуле: готовые правила
 * проверяют форму имени, а это — его смысл.</p>
 *
 * @param username Имя пользователя
 * @param email Почта пользователя
 * @since 1.0
 */
public record CreateUserRequest(

    @NotBlank(message = "имя пользователя обязательно")
    @Size(min = 3, max = 20, message = "имя короче трёх или длиннее двадцати символов")
    @Pattern(
        regexp = "[\\p{Alnum}_.-]*",
        message = "имя содержит что-то кроме букв, цифр, точки, дефиса и подчёркивания"
    )
    @NotReserved
    String username,

    @NotBlank(message = "почта обязательна")
    @Size(max = 100, message = "почта длиннее ста символов")
    @Email(message = "почта не похожа на адрес")
    String email
) {
}
