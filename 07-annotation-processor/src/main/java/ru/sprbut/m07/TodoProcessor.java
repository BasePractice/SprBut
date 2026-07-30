package ru.sprbut.m07;

import ru.sprbut.m07.api.Todo;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

/**
 * Слайд 60: «Анализ исходного кода».
 * <p>
 * Процессор, который <b>ничего не генерирует</b>. Он только проверяет код
 * и печатает диагностику через {@code Messager} — ровно так работают Error Prone,
 * NullAway и проверки Checker Framework.
 * <p>
 * Возвращает {@code false} из {@code process}: «я аннотацию не поглотил,
 * пусть её увидят и другие процессоры». Это важное отличие от
 * {@link BuilderProcessor}, который возвращает {@code true}.
 */
@SupportedAnnotationTypes("ru.sprbut.m07.api.Todo")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class TodoProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(Todo.class)) {
            Todo todo = element.getAnnotation(Todo.class);
            Diagnostic.Kind kind = todo.blocking() ? Diagnostic.Kind.ERROR : Diagnostic.Kind.WARNING;
            processingEnv.getMessager().printMessage(kind,
                    "TODO: " + todo.value() + " (" + element.getSimpleName() + ")", element);
        }
        // false — аннотацию не поглощаем: она может быть нужна и другим процессорам
        return false;
    }
}
