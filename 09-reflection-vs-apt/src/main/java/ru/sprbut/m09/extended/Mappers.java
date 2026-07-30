package ru.sprbut.m09.extended;

import java.util.List;
import ru.sprbut.m09.BytecodeMapper;
import ru.sprbut.m09.GeneratedStyleMapper;
import ru.sprbut.m09.ReflectiveMapper;
import ru.sprbut.m09.UserMapper;
import ru.sprbut.m09.model.UserDto;
import ru.sprbut.m09.model.UserEntity;

/**
 * <b>Расширенный пример модуля 09.</b>
 * <p>
 * Три реализации одного и того же маппинга, поставленные рядом. Здесь СХЕМА 4
 * (слайд 78, «ось времени: compile-time против runtime») перестаёт быть картинкой
 * и становится набором проверяемых утверждений — и первое из них самое важное:
 * <b>механизм не влияет на результат</b>. Он влияет только на свойства: цену
 * вызова, момент обнаружения ошибок и пригодность к native image.
 */
public final class Mappers {

    private final List<UserMapper> all;

    public Mappers() {
        this(List.of(
            new ReflectiveMapper(), new GeneratedStyleMapper(), new BytecodeMapper().mapper()
        ));
    }

    public Mappers(List<UserMapper> all) {
        this.all = List.copyOf(all);
    }

    /**
     * Все три реализации.
     */
    public List<UserMapper> list() {
        return this.all;
    }

    /**
     * Дают ли все реализации одинаковый результат.
     */
    public boolean agree(UserEntity entity) {
        List<UserDto> results = this.all.stream().map(mapper -> mapper.toDto(entity)).toList();
        return results.stream().allMatch(dto -> dto.equals(results.get(0)));
    }

    /**
     * Названия стратегий — каждая реализация объявляет свою сама.
     */
    public List<String> strategies() {
        return this.all.stream().map(UserMapper::strategy).toList();
    }
}
