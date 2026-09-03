/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m22.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sprbut.m22.security.Vault;

/**
 * Слайды 213–214: три зоны доступа на одном контроллере.
 *
 * <p>Контроллер ничего не знает о защите: в нём нет ни проверок, ни ролей,
 * ни ссылок на текущего пользователя. Решение, пускать ли сюда запрос,
 * принято раньше — в цепочке фильтров, до того как {@code DispatcherServlet}
 * вообще выбрал метод. Это и есть разница между аутентификацией
 * («кто это») и авторизацией («что можно»): обе случились до контроллера.</p>
 *
 * @since 1.0
 */
@RestController
public final class AreaController {

    /**
     * Хранилище.
     */
    private final Vault vault;

    /**
     * Основной конструктор.
     * @param vault Хранилище
     */
    public AreaController(final Vault vault) {
        this.vault = vault;
    }

    /**
     * Открытая зона: правило permitAll пропускает без пароля.
     * @return Ответ открытой зоны
     */
    @GetMapping("/api/public/hello")
    public String open() {
        return "сюда пускают всех";
    }

    /**
     * Закрытая зона: нужен любой опознанный пользователь.
     * @return Имя текущего пользователя
     */
    @GetMapping("/api/me")
    public String owner() {
        return this.vault.owner();
    }

    /**
     * Зона администратора: правило на адресе требует роль.
     * @return Ответ зоны администратора
     */
    @GetMapping("/api/admin/report")
    public String report() {
        return "отчёт для администратора";
    }

    /**
     * Правило на методе: адрес открыт для всех опознанных, а метод — нет.
     * @return Содержимое сейфа
     */
    @GetMapping("/api/secret")
    public String secret() {
        return this.vault.secret();
    }
}
