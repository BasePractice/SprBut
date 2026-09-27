/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m28.web;

import jakarta.validation.Valid;
import java.util.Locale;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sprbut.m28.dto.Profile;
import ru.sprbut.m28.dto.ProfileForm;
import ru.sprbut.m28.validation.RoleGuard;

/**
 * Контроллер, который сам настраивает связывание своих аргументов.
 *
 * <p>Метод {@code @InitBinder} вызывается перед связыванием каждого
 * аргумента-формы этого контроллера и получает {@code WebDataBinder} —
 * объект, который раскладывает параметры запроса по полям. Настройка
 * действует только здесь: в чужих контроллерах связыватель прежний,
 * а общий для всех {@code @InitBinder} живёт в {@code @ControllerAdvice}.</p>
 *
 * <p>Две настройки. {@code StringTrimmerEditor(true)} обрезает строки и
 * превращает пустые в {@code null}: город из одних пробелов не станет
 * городом. {@code addValidators} подключает {@code RoleGuard} — правило,
 * которое нужно только здесь: без него клиент, дописавший в форму
 * {@code role=ADMIN}, назначил бы роль себе сам. Учебник предложил бы
 * {@code setAllowedFields}, но анкета — запись, и связывание через
 * конструктор этот список не читает.</p>
 *
 * <p>Метод {@code @ModelAttribute} тоже вызывается перед каждым методом
 * контроллера, нужен ему результат или нет. Результат попадает в модель
 * под своим именем, и метод-обработчик получает его как аргумент.</p>
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/profiles")
public final class ProfileController {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public ProfileController() {
        // нечего инициализировать
    }

    /**
     * Настройка связывания анкеты.
     * @param binder Связыватель аргумента {@code profile}
     */
    @InitBinder("profile")
    public void bind(final WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
        binder.addValidators(new RoleGuard());
    }

    /**
     * Регион из заголовка, собранный до вызова обработчика.
     * @param header Значение заголовка {@code X-Region}
     * @return Регион
     */
    @ModelAttribute("region")
    public String region(
        @RequestHeader(name = "X-Region", defaultValue = "ru") final String header
    ) {
        return header.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Анкета из формы.
     * @param form Анкета, связанная по правилам {@code bind}
     * @param region Регион из модели
     * @return Анкета с регионом
     */
    @PostMapping
    public Profile create(
        @Valid @ModelAttribute("profile") final ProfileForm form,
        @ModelAttribute("region") final String region
    ) {
        return new Profile(form.name(), form.city(), region);
    }
}
