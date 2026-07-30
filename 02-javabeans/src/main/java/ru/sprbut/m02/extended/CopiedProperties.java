package ru.sprbut.m02.extended;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import ru.sprbut.m02.classic.Introspected;
import ru.sprbut.m02.classic.Invoked;

/**
 * Одноимённые свойства, скопированные из одного бина в другой.
 * <p>
 * Упрощённый аналог {@code BeanUtils.copyProperties}. Так собирают DTO из
 * сущностей, пока не подключат MapStruct (модуль 10) — и здесь же видна
 * причина, по которой его в итоге подключают: опечатка в имени свойства
 * не ломает ничего, поле просто молча не копируется.
 */
public final class CopiedProperties {

    private final Object source;

    private final Object target;

    public CopiedProperties(Object source, Object target) {
        this.source = source;
        this.target = target;
    }

    /**
     * Имена скопированных свойств.
     */
    public List<String> list() {
        List<String> copied = new ArrayList<>();
        Introspected from = new Introspected(this.source.getClass());
        for (PropertyDescriptor into : new Introspected(this.target.getClass()).descriptors()) {
            if (into.getWriteMethod() == null || "class".equals(into.getName())) {
                continue;
            }
            PropertyDescriptor read = from.descriptor(into.getName()).orElse(null);
            if (read == null || read.getReadMethod() == null) {
                continue;
            }
            if (!into.getPropertyType().isAssignableFrom(read.getPropertyType())) {
                continue;
            }
            new Invoked(into.getWriteMethod(), this.target).value(
                new Invoked(read.getReadMethod(), this.source).value()
            );
            copied.add(into.getName());
        }
        copied.sort(String::compareTo);
        return List.copyOf(copied);
    }
}
