/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m09;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ru.sprbut.m09.model.UserDto;
import ru.sprbut.m09.model.UserEntity;

/**
 * Слайд 73: «Reflection: runtime, гибко, медленно».
 *
 * <p>Правила маппинга выводятся <b>в момент выполнения</b> — из метаданных классов.
 * Плюсы и минусы здесь две стороны одной монеты:
 * <ul>
 * <li><b>гибко</b>: добавьте поле в оба класса — маппер подхватит его сам,
 * никакой перекомпиляции и никаких правок;</li>
 * <li><b>небезопасно</b>: опечатка в имени или несовпадение типов
 * обнаружатся только в runtime, и то лишь на конкретном объекте;</li>
 * <li><b>медленно</b>: каждый вызов — это работа с {@code Method}
 * и упаковка аргументов.</li>
 * </ul></p>
 *
 * @since 1.0
 */
public final class ReflectiveMapper implements UserMapper {

    /**
     * Пары «геттер источника — сеттер цели», найденные один раз при создании.
     */
    private final Map<Method, Method> plan;

    /**
     * Основной конструктор.
     *
     * <p>План маппинга строится один раз при создании — в этом и смысл
     * сравнения с генерацией кода: работа выполняется в runtime.</p>
     *
     * @checkstyle ConstructorsCodeFreeCheck (4 lines)
     */
    public ReflectiveMapper() {
        this.plan = ReflectiveMapper.buildPlan(UserEntity.class, UserDto.class);
    }

    @Override
    public UserDto toDto(final UserEntity entity) {
        final UserDto dto;
        if (entity == null) {
            dto = null;
        } else {
            dto = new UserDto();
            for (final Map.Entry<Method, Method> step : this.plan.entrySet()) {
                ReflectiveMapper.invoke(
                    step.getValue(), dto, ReflectiveMapper.invoke(step.getKey(), entity)
                );
            }
        }
        return dto;
    }

    @Override
    public String strategy() {
        return "reflection: правила выводятся в runtime из метаданных";
    }

    /**
     * Сколько свойств маппер нашёл сам, без единой строчки правил.
     * @return Число найденных свойств
     */
    public int discoveredProperties() {
        return this.plan.size();
    }

    /**
     * Имя свойства.
     * @return Имя свойства
     */
    public List<String> propertyNames() {
        return this.plan.keySet().stream()
            .map(ReflectiveMapper::property)
            .sorted()
            .toList();
    }

    private static Map<Method, Method> buildPlan(final Class<?> source, final Class<?> target) {
        final Map<Method, Method> plan = new LinkedHashMap<>(0);
        for (final Method getter : source.getMethods()) {
            if (ReflectiveMapper.reader(getter)) {
                ReflectiveMapper.pair(getter, target, plan);
            }
        }
        return plan;
    }

    private static String property(final Method getter) {
        return ReflectiveMapper.decapitalize(
            ReflectiveMapper.stripPrefix(getter.getName())
        );
    }

    private static boolean reader(final Method getter) {
        return getter.getParameterCount() == 0
            && getter.getDeclaringClass() != Object.class
            && ReflectiveMapper.named(getter);
    }

    private static boolean named(final Method getter) {
        final String name = getter.getName();
        final boolean found;
        if (name.startsWith("get")) {
            found = name.length() > 3 && getter.getReturnType() != void.class;
        } else if (name.startsWith("is")) {
            found = name.length() > 2 && ReflectiveMapper.bool(getter.getReturnType());
        } else {
            found = false;
        }
        return found;
    }

    private static boolean bool(final Class<?> type) {
        return type == boolean.class || type == Boolean.class;
    }

    private static void pair(final Method getter, final Class<?> target,
        final Map<Method, Method> plan) {
        try {
            plan.put(
                getter,
                target.getMethod(
                    String.format("set%s", ReflectiveMapper.stripPrefix(getter.getName())),
                    getter.getReturnType()
                )
            );
        } catch (final NoSuchMethodException absent) {
            plan.remove(getter);
        }
    }

    private static String stripPrefix(final String method) {
        final String property;
        if (method.startsWith("is")) {
            property = method.substring(2);
        } else {
            property = method.substring(3);
        }
        return property;
    }

    private static String decapitalize(final String name) {
        return String.format(
            "%s%s", Character.toLowerCase(name.charAt(0)), name.substring(1)
        );
    }

    private static Object invoke(final Method method, final Object target, final Object... args) {
        try {
            return method.invoke(target, args);
        } catch (final IllegalAccessException denied) {
            throw new IllegalStateException(
                String.format("Нет доступа к %s", method.getName()), denied
            );
        } catch (final InvocationTargetException wrapped) {
            throw new IllegalStateException(wrapped.getCause().getMessage(), wrapped);
        }
    }
}
