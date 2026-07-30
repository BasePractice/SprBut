package ru.sprbut.m12.locator;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import ru.sprbut.m12.domain.TaxService;

import java.math.BigDecimal;

/**
 * Слайд 95: «Service Locator — антипаттерн».
 * <p>
 * Внешне похоже на DI: зависимость тоже приходит извне. Разница принципиальная —
 * объект <b>сам ходит за зависимостью</b>, вместо того чтобы её получить.
 * Инверсии управления не произошло, она только замаскировалась.
 * <p>
 * Что с этим не так:
 * <ul>
 *   <li>зависимости невидимы: ни конструктор, ни поля о них не говорят,
 *       они спрятаны в теле методов;</li>
 *   <li>класс намертво привязан к Spring — вне контейнера он не работает вовсе;</li>
 *   <li>ошибка «нет такого бина» вылезает в момент вызова метода, а не при
 *       старте приложения;</li>
 *   <li>чтобы протестировать, придётся поднимать контекст или подсовывать
 *       мок самого контекста.</li>
 * </ul>
 */
@Component
public class ServiceLocatorDemo implements ApplicationContextAware {

    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    /** Зависимость достаётся вручную, в момент вызова. */
    public BigDecimal total(BigDecimal net) {
        TaxService taxService = context.getBean(TaxService.class);
        return taxService.applyVat(net);
    }

    /** Ошибка вылезет только здесь, а не при старте контекста. */
    public Object lookup(String beanName) {
        return context.getBean(beanName);
    }

    public boolean worksWithoutContainer() {
        return context != null;
    }
}
