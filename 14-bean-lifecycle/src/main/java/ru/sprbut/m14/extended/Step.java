package ru.sprbut.m14.extended;

/**
 * Шаг жизненного цикла: номер, название фазы и бин.
 *
 * @param number порядковый номер шага со слайда 118
 * @param phase  название фазы
 * @param bean   имя бина
 */
public record Step(int number, String phase, String bean) {

    @Override
    public String toString() {
        return this.number + ". " + this.phase + " → " + this.bean;
    }
}
