package ru.sprbut.m21.extended;

import ru.sprbut.m21.Diagnosis;

/**
 * Диагноз здоровой конфигурации: контекст поднялся, лечить нечего.
 */
public final class Healthy implements Diagnosis {

    @Override
    public String summary() {
        return "контекст поднялся без ошибок";
    }

    @Override
    public String remedy() {
        return "вмешательство не требуется";
    }
}
