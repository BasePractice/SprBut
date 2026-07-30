package ru.sprbut.m09.extended;

import ru.sprbut.m09.BytecodeMapper;
import ru.sprbut.m09.GeneratedStyleMapper;
import ru.sprbut.m09.ReflectiveMapper;
import ru.sprbut.m09.UserMapper;
import ru.sprbut.m09.model.UserDto;
import ru.sprbut.m09.model.UserEntity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <b>Расширенный пример модуля 09.</b>
 * <p>
 * Три реализации одного и того же маппинга, поставленные рядом. Здесь СХЕМА 4
 * (слайд 78, «ось времени: compile-time против runtime») перестаёт быть картинкой
 * и становится набором проверяемых утверждений:
 * <ul>
 *   <li>все три дают <b>идентичный результат</b> — выбор механизма не влияет
 *       на поведение, только на свойства;</li>
 *   <li>замер показывает разницу в цене вызова;</li>
 *   <li>{@link #requiredRuntimeHints()} перечисляет ровно те члены классов,
 *       которые пришлось бы регистрировать в {@code RuntimeHints} для
 *       native image — и этот список пуст для двух механизмов из трёх (модуль 22);</li>
 *   <li>{@link #adaptsWithoutRebuild} показывает обратную сторону: гибкость,
 *       ради которой рефлексию и терпят.</li>
 * </ul>
 */
public final class MapperShowcase {

    private MapperShowcase() {
    }

    public static List<UserMapper> allMappers() {
        return List.of(new ReflectiveMapper(), new GeneratedStyleMapper(), BytecodeMapper.create());
    }

    /** Проверка, что механизм не меняет результат. */
    public static boolean allProduceSameResult(UserEntity entity) {
        List<UserDto> results = allMappers().stream().map(m -> m.toDto(entity)).toList();
        return results.stream().allMatch(dto -> dto.equals(results.get(0)));
    }

    /**
     * Время работы каждой реализации на одинаковой нагрузке.
     * Это не JMH: цель — порядок величины, а не точные цифры.
     */
    public static Map<String, Long> benchmark(int iterations) {
        UserEntity entity = sample();
        Map<String, Long> timings = new LinkedHashMap<>();
        for (UserMapper mapper : allMappers()) {
            // разогрев, чтобы JIT успел скомпилировать горячий путь
            for (int i = 0; i < iterations / 10 + 1; i++) {
                mapper.toDto(entity);
            }
            long start = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                mapper.toDto(entity);
            }
            timings.put(mapper.getClass().getSimpleName(), System.nanoTime() - start);
        }
        return timings;
    }

    /**
     * Что пришлось бы объявить в {@code RuntimeHints}, чтобы реализация
     * заработала в native image.
     * <p>
     * Для рефлексивного маппера это реальный список методов, вычисленный
     * рефлексией же. Для остальных — пусто: они не обращаются к метаданным.
     */
    public static Map<String, List<String>> requiredRuntimeHints() {
        Map<String, List<String>> hints = new LinkedHashMap<>();

        ReflectiveMapper reflective = new ReflectiveMapper();
        List<String> reflectiveHints = new java.util.ArrayList<>();
        for (String property : reflective.propertyNames()) {
            String suffix = Character.toUpperCase(property.charAt(0)) + property.substring(1);
            reflectiveHints.add(UserEntity.class.getSimpleName() + "#get" + suffix);
            reflectiveHints.add(UserDto.class.getSimpleName() + "#set" + suffix);
        }
        reflectiveHints.sort(String::compareTo);

        hints.put("ReflectiveMapper", List.copyOf(reflectiveHints));
        hints.put("GeneratedStyleMapper", List.of());
        // байткод в native image невозможен в принципе: класса ещё нет при сборке
        hints.put("BytecodeMapper", List.of("класс генерируется в runtime — native image неприменим"));
        return hints;
    }

    /**
     * Обратная сторона медали: рефлексивный маппер сам находит все свойства,
     * ничего для этого не зная заранее. Добавьте поле в оба класса — и он
     * подхватит его без единой правки.
     */
    public static boolean adaptsWithoutRebuild() {
        return new ReflectiveMapper().discoveredProperties() > 0;
    }

    /** Свойства, которые рефлексивный маппер вывел сам. */
    public static List<String> discoveredProperties() {
        return new ReflectiveMapper().propertyNames();
    }

    public static UserEntity sample() {
        UserEntity entity = new UserEntity("U-1", "Иван", "Иванов", 42, true);
        entity.setInternalNote("не должно попасть в DTO");
        return entity;
    }
}
