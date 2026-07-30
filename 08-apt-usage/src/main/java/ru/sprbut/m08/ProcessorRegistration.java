package ru.sprbut.m08;

import javax.annotation.processing.Processor;
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

/**
 * Слайд 66: «Регистрация: META-INF/services или {@code @AutoService}».
 * <p>
 * Компилятор находит процессоры не по волшебству, а через стандартный
 * {@link ServiceLoader}: он читает файл
 * {@code META-INF/services/javax.annotation.processing.Processor} на processor-path.
 * В каждой строке — полное имя класса процессора.
 * <p>
 * {@code @AutoService(Processor.class)} от Google — просто ещё один annotation
 * processor, который генерирует этот файл за вас. Механизм остаётся тем же.
 * <p>
 * Модуль 07 регистрирует процессоры файлом вручную, чтобы механизм был виден
 * целиком, без промежуточной библиотеки.
 */
public final class ProcessorRegistration {

    /** Путь, по которому ServiceLoader ищет процессоры. */
    public static final String SERVICE_FILE = "META-INF/services/javax.annotation.processing.Processor";

    private ProcessorRegistration() {
    }

    /**
     * Что реально написано в файле регистрации на текущем classpath.
     * Ровно это и прочитает javac при сборке.
     */
    public static List<String> declaredProcessorNames() {
        List<String> names = new ArrayList<>();
        try {
            Enumeration<URL> resources = ProcessorRegistration.class.getClassLoader()
                    .getResources(SERVICE_FILE);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                    reader.lines()
                            .map(String::trim)
                            .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                            .forEach(names::add);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        names.sort(Comparator.naturalOrder());
        return names;
    }

    /**
     * Процессоры, реально загруженные {@link ServiceLoader} — то же самое,
     * что делает javac, только вручную.
     */
    public static List<String> loadedProcessorNames() {
        List<String> names = new ArrayList<>();
        for (Processor processor : ServiceLoader.load(Processor.class,
                ProcessorRegistration.class.getClassLoader())) {
            names.add(processor.getClass().getName());
        }
        names.sort(Comparator.naturalOrder());
        return names;
    }

    /** Какие аннотации объявляет процессор — по этому javac решает, звать ли его. */
    public static List<String> supportedAnnotationsOf(String processorClassName) {
        for (Processor processor : ServiceLoader.load(Processor.class,
                ProcessorRegistration.class.getClassLoader())) {
            if (processor.getClass().getName().equals(processorClassName)) {
                return processor.getSupportedAnnotationTypes().stream().sorted().toList();
            }
        }
        throw new IllegalArgumentException("Процессор не зарегистрирован: " + processorClassName);
    }
}
