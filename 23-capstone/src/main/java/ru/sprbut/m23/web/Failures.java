package ru.sprbut.m23.web;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Превращает исключения предметной области в ответы HTTP.
 * <p>
 * {@code @RestControllerAdvice} — тот же {@code @Component}, только найденный
 * не по типу, а по назначению: Spring MVC сам находит такие бины и подключает
 * их ко всем контроллерам сразу.
 */
@RestControllerAdvice
public final class Failures {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> missing(IllegalArgumentException failure) {
        return Map.of("error", failure.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> conflict(IllegalStateException failure) {
        return Map.of("error", failure.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalid(MethodArgumentNotValidException failure) {
        return Map.of(
            "error",
            failure.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(field -> field.getField() + ": " + field.getDefaultMessage())
                .orElse("некорректный запрос")
        );
    }
}
