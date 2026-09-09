/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m07.bytecode;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;
import ru.sprbut.m07.api.Instrumented;
import ru.sprbut.m07.api.Meta;

/**
 * Слайд 63: «Генерация кода» — но не исходного, а сразу байткода.
 *
 * <p>Штатный путь процессора — {@code Filer.createSourceFile}: он пишет текст,
 * который javac потом компилирует сам. У {@code Filer} есть и второй выход —
 * {@code createClassFile}, и тогда классы не компилируются, а <b>кладутся
 * готовыми</b>. Байткод для них собирает ByteBuddy, никакого javac в этой
 * ветке нет вовсе.</p>
 *
 * <p>Отсюда видно границу подхода, о которую спотыкаются все, кто пробует его
 * впервые. ByteBuddy строит типы вокруг <b>загруженных</b> классов, а классов
 * текущей компиляции ещё не существует — загружать нечего. Поэтому паспорт
 * реализует {@link Meta} из готового модуля, а сведения о помеченном классе
 * берутся не рефлексией, а из модели javac и запекаются в константы. Ровно
 * по этой же причине Lombok не генерирует байткод, а правит AST компилятора:
 * иначе к сгенерированному не подступиться из исходников.</p>
 *
 * <p>А вот чего ожидаешь и не получаешь — так это невидимости результата.
 * Готовый класс javac всё равно находит: типы он ищет и среди файлов,
 * созданных {@code Filer}, поэтому обычный код спокойно пишет
 * {@code new UserServiceMeta()}. Условие то же, что и у сгенерированных
 * исходников: файл должен появиться в рабочем раунде, а не в последнем.</p>
 *
 * @since 1.0
 */
@SupportedAnnotationTypes("ru.sprbut.m07.api.Instrumented")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class BytecodeProcessor extends AbstractProcessor {

    /**
     * Открытый конструктор: экземпляр создаёт компилятор.
     */
    public BytecodeProcessor() {
        // нечего инициализировать
    }

    // паспорт пишется в рабочем раунде: файл последнего раунда javac уже
    // не увидит, а сам процессор аннотацию поглощает — она больше никому
    // не нужна
    @Override
    @SuppressWarnings("DoNotClaimAnnotations")
    public final boolean process(
        final Set<? extends TypeElement> annotations, final RoundEnvironment env
    ) {
        if (!env.processingOver()) {
            for (final Element element : env.getElementsAnnotatedWith(Instrumented.class)) {
                this.instrument(element);
            }
        }
        return true;
    }

    // один помеченный элемент: паспорт есть о чём выдавать только классу
    private void instrument(final Element element) {
        if (element.getKind() == ElementKind.CLASS) {
            this.write((TypeElement) element);
        } else {
            this.processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR, "@Instrumented применим только к классам", element
            );
        }
    }

    // createClassFile вместо createSourceFile: содержимое дальше по конвейеру
    // не пойдёт, поэтому оно обязано быть готовым классом
    private void write(final TypeElement type) {
        final String name = String.format("%sMeta", type.getQualifiedName());
        try (
            OutputStream stream = this.processingEnv.getFiler()
                .createClassFile(name, type)
                .openOutputStream()
        ) {
            stream.write(BytecodeProcessor.bytecode(name, type));
        } catch (final IOException failure) {
            this.processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                String.format("Не удалось записать паспорт %s: %s", name, failure.getMessage())
            );
        }
    }

    // весь класс — это две константы, и обе известны компилятору: имя типа
    // и число его методов. Версия формата задана явно: по умолчанию ByteBuddy
    // берёт версию текущей JVM, а собираем мы под --release 17
    private static byte[] bytecode(final String name, final TypeElement type) {
        return new ByteBuddy(ClassFileVersion.JAVA_V17)
            .subclass(Object.class)
            .implement(Meta.class)
            .name(name)
            .method(ElementMatchers.named("origin"))
            .intercept(FixedValue.value(type.getQualifiedName().toString()))
            .method(ElementMatchers.named("methods"))
            .intercept(
                FixedValue.value(ElementFilter.methodsIn(type.getEnclosedElements()).size())
            )
            .make()
            .getBytes();
    }
}
