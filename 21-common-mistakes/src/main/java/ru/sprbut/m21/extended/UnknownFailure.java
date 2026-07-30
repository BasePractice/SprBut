package ru.sprbut.m21.extended;

import ru.sprbut.m21.Diagnosis;

/**
 * Диагноз по умолчанию: тип ошибки не распознан.
 * <p>
 * Молчать в таком случае нельзя — незнакомая поломка должна называть себя
 * классом исключения, иначе диагност начнёт врать уверенным тоном.
 */
public final class UnknownFailure implements Diagnosis {

    private final Throwable cause;

    public UnknownFailure(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public String summary() {
        return "контекст не поднялся: " + this.cause.getClass().getSimpleName();
    }

    @Override
    public String remedy() {
        return "запустить приложение с --debug и прочитать отчёт об условиях";
    }
}
