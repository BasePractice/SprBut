/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m11.step4;

import dagger.Component;
import jakarta.inject.Singleton;
import ru.sprbut.m11.step2.ManualOrderService;

/**
 * Компонент Dagger — граница графа и единственная точка входа в него.
 *
 * <p>Интерфейс перечисляет то, что нужно снаружи; всё остальное — приватная
 * внутренность сборки. По этому объявлению процессор аннотаций пишет класс
 * {@code DaggerOrderComponent}: обычный Java-код, который можно открыть в
 * {@code target/generated-sources} и прочитать глазами.</p>
 *
 * <p>{@code @Singleton} здесь означает не «один на приложение», а «один на
 * компонент»: сколько компонентов собрали, столько и графов, и каждый со своими
 * экземплярами. Время жизни объекта равно времени жизни компонента, который его
 * создал.</p>
 *
 * @since 1.0
 */
// у компонента один метод, но функциональным интерфейсом он не является:
// его реализацию пишет процессор, а не лямбда
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
@Singleton
@Component(modules = WiringModule.class)
public interface OrderComponent {

    /**
     * Сервис заказов со всеми зависимостями.
     * @return Сервис заказов со всеми зависимостями
     */
    ManualOrderService orderService();
}
