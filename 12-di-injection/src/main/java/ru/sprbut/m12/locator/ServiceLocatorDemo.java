/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m12.locator;

import java.math.BigDecimal;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import ru.sprbut.m12.domain.TaxService;

/**
 * Слайд 95: «Service Locator — антипаттерн».
 *
 * <p>Внешне похоже на DI: зависимость тоже приходит извне. Разница принципиальная —
 * объект <b>сам ходит за зависимостью</b>, вместо того чтобы её получить.
 * Инверсии управления не произошло, она только замаскировалась.</p>
 *
 * <p>Что с этим не так:
 * <ul>
 * <li>зависимости невидимы: ни конструктор, ни поля о них не говорят,
 * они спрятаны в теле методов;</li>
 * <li>класс намертво привязан к Spring — вне контейнера он не работает вовсе;</li>
 * <li>ошибка «нет такого бина» вылезает в момент вызова метода, а не при
 * старте приложения;</li>
 * <li>чтобы протестировать, придётся поднимать контекст или подсовывать
 * мок самого контекста.</li>
 * </ul></p>
 *
 * @since 1.0
 */
@Component
public class ServiceLocatorDemo implements ApplicationContextAware {

    /**
     * Контекст.
     */
    private ApplicationContext context;

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public ServiceLocatorDemo() {
        // нечего инициализировать
    }

    // имя параметра задано контрактом ApplicationContextAware
    // @checkstyle HiddenFieldCheck (5 lines)
    @Override
    public final void setApplicationContext(final ApplicationContext context)
        throws BeansException {
        this.context = context;
    }

    /**
     * Зависимость достаётся вручную, в момент вызова.
     * @param net Сумма без налога
     * @return Зависимость достаётся вручную, в момент вызова
     */
    public BigDecimal total(final BigDecimal net) {
        return this.context.getBean(TaxService.class).applyVat(net);
    }

    /**
     * Ошибка вылезет только здесь, а не при старте контекста.
     * @param bean Имя бина
     * @return Бин, найденный по имени уже во время работы
     */
    public Object lookup(final String bean) {
        return this.context.getBean(bean);
    }

    /**
     * Работа без контейнера.
     * @return Работа без контейнера
     */
    public boolean worksWithoutContainer() {
        return this.context != null;
    }
}
