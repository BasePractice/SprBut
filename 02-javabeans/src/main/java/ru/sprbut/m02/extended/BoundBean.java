package ru.sprbut.m02.extended;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ru.sprbut.m02.classic.BeanVerdict;
import ru.sprbut.m02.classic.EmptyBean;
import ru.sprbut.m02.classic.Introspected;
import ru.sprbut.m02.classic.Invoked;
import ru.sprbut.m02.classic.PropertyKey;

/**
 * <b>Расширенный пример модуля 02.</b>
 * <p>
 * Мини-биндер конфигурации: заполняет JavaBean из плоской карты «ключ → строка»,
 * ровно как {@code @ConfigurationProperties} заполняет объект из
 * {@code application.yaml}. Именно ради этого сценария соглашение JavaBeans
 * и существует — контейнеру нужен конструктор без параметров, чтобы создать
 * объект, и сеттеры, чтобы его наполнить.
 * <p>
 * Заодно виден предел применимости соглашения: на {@code record} этот биндер
 * не работает вовсе — нет ни конструктора без параметров, ни сеттеров. Поэтому
 * Spring Boot для неизменяемых конфигураций пришлось учить отдельному режиму
 * constructor binding — тот самый, что используется в модуле 16.
 */
public final class BoundBean<T> {

    private final Class<T> type;

    private final Map<String, String> values;

    public BoundBean(Class<T> type, Map<String, String> values) {
        this.type = type;
        this.values = Map.copyOf(values);
    }

    /**
     * Заполненный объект вместе с отчётом о привязке.
     *
     * @throws IllegalArgumentException если класс не подчиняется соглашению JavaBeans
     */
    @SuppressWarnings("unchecked")
    public Binding<T> result() {
        BeanVerdict verdict = new BeanVerdict(this.type);
        if (!verdict.valid()) {
            throw new IllegalArgumentException(
                this.type.getSimpleName() + " не является JavaBean: "
                    + String.join("; ", verdict.violations())
            );
        }
        T bean = (T) new EmptyBean(this.type).instance();
        Introspected introspected = new Introspected(this.type);
        List<String> bound = new ArrayList<>();
        List<String> ignored = new ArrayList<>();
        for (Map.Entry<String, String> entry : this.values.entrySet()) {
            String property = new PropertyKey(entry.getKey()).camelCase();
            PropertyDescriptor described = introspected.descriptor(property).orElse(null);
            if (described == null || described.getWriteMethod() == null) {
                ignored.add(entry.getKey());
                continue;
            }
            new Invoked(described.getWriteMethod(), bean).value(
                new Converted(entry.getValue(), described.getPropertyType(), property).value()
            );
            bound.add(property);
        }
        bound.sort(String::compareTo);
        ignored.sort(String::compareTo);
        return new Binding<>(bean, bound, ignored);
    }
}
