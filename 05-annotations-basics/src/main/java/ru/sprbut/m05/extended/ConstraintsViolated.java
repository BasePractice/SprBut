package ru.sprbut.m05.extended;

/**
 * Отказ режима «падать сразу».
 * <p>
 * Несёт весь вердикт целиком, а не только первое нарушение: сообщение об одной
 * ошибке из пяти заставляет чинить их по очереди, по одной за запуск.
 */
public final class ConstraintsViolated extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final transient Verdict verdict;

    public ConstraintsViolated(Verdict verdict) {
        super(
            "Нарушений: " + verdict.violations().size()
                + " — " + String.join("; ", verdict.messages())
        );
        this.verdict = verdict;
    }

    /**
     * Полный вердикт со всеми нарушениями.
     */
    public Verdict verdict() {
        return this.verdict;
    }
}
