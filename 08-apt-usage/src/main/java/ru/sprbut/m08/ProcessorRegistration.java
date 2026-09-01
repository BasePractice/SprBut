/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m08;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.ServiceLoader;
import javax.annotation.processing.Processor;

/**
 * Слайд 66: «Регистрация: META-INF/services или {@code @AutoService}».
 *
 * <p>Компилятор находит процессоры не по волшебству, а через стандартный
 * {@link ServiceLoader}: он читает файл
 * {@code META-INF/services/javax.annotation.processing.Processor} на processor-path,
 * где в каждой строке — полное имя класса процессора.</p>
 *
 * <p>{@code @AutoService(Processor.class)} от Google — просто ещё один annotation
 * processor, который генерирует этот файл за вас. Механизм остаётся тем же,
 * и модуль 07 регистрирует процессоры вручную именно поэтому: чтобы он был виден
 * целиком, без промежуточной библиотеки.</p>
 *
 * @since 1.0
 */
public final class ProcessorRegistration {

    /**
     * Загрузчик классов.
     */
    private final ClassLoader loader;

    /**
     * Основной конструктор.
     */
    public ProcessorRegistration() {
        this(ProcessorRegistration.class.getClassLoader());
    }

    /**
     * Основной конструктор.
     * @param loader Загрузчик классов
     */
    public ProcessorRegistration(final ClassLoader loader) {
        this.loader = loader;
    }

    /**
     * Путь, по которому {@link ServiceLoader} ищет процессоры.
     * @return Путь, по которому {@link ServiceLoader} ищет процессоры
     */
    // @checkstyle NonStaticMethodCheck (3 lines)
    public String servicePath() {
        return "META-INF/services/javax.annotation.processing.Processor";
    }

    /**
     * Что реально написано в файлах регистрации на текущем classpath —
     * ровно это и прочитает javac при сборке.
     * @return Что реально написано в файлах регистрации на текущем classpath — ровно это и прочитает javac при сборке
     */
    public List<String> declared() {
        final List<String> names = new ArrayList<>();
        try {
            final Enumeration<URL> resources = this.loader.getResources(this.servicePath());
            while (resources.hasMoreElements()) {
                names.addAll(this.lines(resources.nextElement()));
            }
        } catch (final IOException broken) {
            throw new UncheckedIOException(broken);
        }
        names.sort(Comparator.naturalOrder());
        return List.copyOf(names);
    }

    /**
     * Процессоры, реально загруженные {@link ServiceLoader}, — то же самое,
     * что делает javac, только вручную.
     * @return Процессоры, реально загруженные {@link ServiceLoader}, — то же самое, что делает javac, только вручную
     */
    public List<String> loaded() {
        final List<String> names = new ArrayList<>();
        for (final Processor each : ServiceLoader.load(Processor.class, this.loader)) {

            names.add(each.getClass().getName());
        }
        names.sort(Comparator.naturalOrder());
        return List.copyOf(names);
    }

    /**
     * Какие аннотации объявляет процессор — по этому javac и решает, звать ли его.
     * @param processor Процессор
     * @return Какие аннотации объявляет процессор — по этому javac и решает, звать ли его
     */
    public List<String> supported(final String processor) {
        for (final Processor each : ServiceLoader.load(Processor.class, this.loader)) {

            if (each.getClass().getName().equals(processor)) {
                return each.getSupportedAnnotationTypes().stream().sorted().toList();
            }
        }
        throw new IllegalArgumentException("Процессор не зарегистрирован: " + processor);
    }

    private static List<String> lines(final URL resource) throws IOException {
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8
)
        )) {
            return reader.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .toList();
        }
    }
}
