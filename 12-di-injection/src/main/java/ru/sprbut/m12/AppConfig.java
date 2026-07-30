package ru.sprbut.m12;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.sprbut.m12.jakarta.JakartaInjected;

/**
 * Конфигурация модуля: сканирует пакеты с примерами внедрения.
 * <p>
 * Классы с циклическими зависимостями лежат отдельно и <b>не</b> попадают
 * в сканирование — иначе контекст бы просто не поднялся.
 */
@Configuration
@ComponentScan(basePackageClasses = {
        ru.sprbut.m12.domain.TaxService.class,
        ru.sprbut.m12.injection.ConstructorInjected.class,
        ru.sprbut.m12.locator.ServiceLocatorDemo.class,
        JakartaInjected.class
})
public class AppConfig {
}
