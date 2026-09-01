/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m04;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

/**
 * Слайд 33: «Рефлексия медленнее прямого вызова».
 *
 * <p>Здесь эта фраза измеряется, а не принимается на веру. Замер намеренно
 * <b>не</b> микробенчмарк уровня JMH: цель — не точные наносекунды, а порядок
 * величины и, главное, разница между «искать каждый раз» и «искать один раз,
 * вызывать много».</p>
 *
 * <p>Тесты на этих числах ничего не утверждают о скорости — только о том, что все
 * четыре способа дают одинаковый результат. Замеры на CI флаки по природе,
 * и тест, зависящий от них, ломался бы через раз.</p>
 *
 * @since 1.0
 */
public final class InvocationCost {

    /**
     * Целевой объект.
     */
    private final Target target;

    /**
     * Число итераций.
     */
    private final int iterations;

    /**
     * Основной конструктор.
     * @param target Целевой объект
     * @param iterations Число итераций
     */
    public InvocationCost(final Target target, final int iterations) {
        this.target = target;
        this.iterations = iterations;
    }

    /**
     * Прямой вызов — эталон.
     * @return Прямой вызов — эталон
     */
    public int direct() {
        int sum = 0;
        for (int step = 0; step < this.iterations; step++) {
            sum = this.target.add(sum, 1);
        }
        return sum;
    }

    /**
     * Худший вариант: поиск метода на каждой итерации. Так писать нельзя,
     * но именно так выглядит наивный код «на рефлексии».
     * @return Худший вариант: поиск метода на каждой итерации. Так писать нельзя, но именно так выглядит наивный код «на рефлексии»
     */
    public int searching() throws ReflectiveOperationException {
        int sum = 0;
        for (int step = 0; step < this.iterations; step++) {
            final Method found = Target.class.getDeclaredMethod("add", int.class, int.class);
            sum = (int) found.invoke(this.target, sum, 1);
        }
        return sum;
    }

    /**
     * Метод найден один раз и переиспользуется — минимально приемлемый вариант.
     * @return Метод найден один раз и переиспользуется — минимально приемлемый вариант
     */
    public int cached() throws ReflectiveOperationException {
        final Method found = Target.class.getDeclaredMethod("add", int.class, int.class);
        int sum = 0;
        for (int step = 0; step < this.iterations; step++) {
            sum = (int) found.invoke(this.target, sum, 1);
        }
        return sum;
    }

    /**
     * Через {@link MethodHandle}: доступ проверен при создании, и JIT способен
     * встроить такой вызов почти как прямой.
     * @return Через {@link MethodHandle}: доступ проверен при создании, и JIT способен встроить такой вызов почти как прямой
     */
    public int handle() throws Throwable {
        final MethodHandle found = new Handles(Target.class).virtual(
            "add", int.class, int.class, int.class
        );
        int sum = 0;
        for (int step = 0; step < this.iterations; step++) {
            sum = (int) found.invokeExact(this.target, sum, 1);
        }
        return sum;
    }
}
