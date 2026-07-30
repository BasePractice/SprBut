package ru.sprbut.m09;

import ru.sprbut.m09.model.UserDto;
import ru.sprbut.m09.model.UserEntity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Слайд 73: «Reflection: runtime, гибко, медленно».
 * <p>
 * Правила маппинга выводятся <b>в момент выполнения</b> — из метаданных классов.
 * Плюсы и минусы здесь две стороны одной монеты:
 * <ul>
 *   <li><b>гибко</b>: добавьте поле в оба класса — маппер подхватит его сам,
 *       никакой перекомпиляции и никаких правок;</li>
 *   <li><b>небезопасно</b>: опечатка в имени или несовпадение типов
 *       обнаружатся только в runtime, и то лишь на конкретном объекте;</li>
 *   <li><b>медленно</b>: каждый вызов — это работа с {@code Method}
 *       и упаковка аргументов.</li>
 * </ul>
 */
public class ReflectiveMapper implements UserMapper {

    /** Пары «геттер источника → сеттер цели», найденные один раз при создании. */
    private final Map<Method, Method> plan;

    public ReflectiveMapper() {
        this.plan = buildPlan(UserEntity.class, UserDto.class);
    }

    @Override
    public UserDto toDto(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        UserDto dto = new UserDto();
        for (Map.Entry<Method, Method> step : plan.entrySet()) {
            Object value = invoke(step.getKey(), entity);
            invoke(step.getValue(), dto, value);
        }
        return dto;
    }

    @Override
    public String strategy() {
        return "reflection: правила выводятся в runtime из метаданных";
    }

    /** Сколько свойств маппер нашёл сам, без единой строчки правил. */
    public int discoveredProperties() {
        return plan.size();
    }

    public java.util.List<String> propertyNames() {
        return plan.keySet().stream()
                .map(m -> decapitalize(stripPrefix(m.getName())))
                .sorted()
                .toList();
    }

    /**
     * Правило вывода: для каждого геттера источника ищем одноимённый сеттер цели
     * с совместимым типом. Именно так работает {@code BeanUtils.copyProperties}.
     */
    private static Map<Method, Method> buildPlan(Class<?> source, Class<?> target) {
        Map<Method, Method> plan = new LinkedHashMap<>();
        for (Method getter : source.getMethods()) {
            if (getter.getParameterCount() != 0 || getter.getDeclaringClass() == Object.class) {
                continue;
            }
            String name = getter.getName();
            boolean isGetter = name.startsWith("get") && name.length() > 3
                    && getter.getReturnType() != void.class;
            boolean isBooleanGetter = name.startsWith("is") && name.length() > 2
                    && (getter.getReturnType() == boolean.class || getter.getReturnType() == Boolean.class);
            if (!isGetter && !isBooleanGetter) {
                continue;
            }
            String property = stripPrefix(name);
            try {
                Method setter = target.getMethod("set" + property, getter.getReturnType());
                plan.put(getter, setter);
            } catch (NoSuchMethodException ignored) {
                // у цели нет такого свойства — например, internalNote; просто пропускаем
            }
        }
        return plan;
    }

    private static String stripPrefix(String methodName) {
        return methodName.startsWith("is") ? methodName.substring(2) : methodName.substring(3);
    }

    private static String decapitalize(String name) {
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    private static Object invoke(Method method, Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Нет доступа к " + method.getName(), e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            throw cause instanceof RuntimeException re ? re : new IllegalStateException(cause);
        }
    }
}
