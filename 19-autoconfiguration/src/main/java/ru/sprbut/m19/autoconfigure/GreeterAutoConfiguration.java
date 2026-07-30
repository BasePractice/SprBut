package ru.sprbut.m19.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import ru.sprbut.m19.greeter.Greeter;
import ru.sprbut.m19.greeter.GreeterProperties;
import ru.sprbut.m19.greeter.SimpleGreeter;

/**
 * Слайды 174–177 (СХЕМА 12): «starter → imports → условия → бин».
 * <p>
 * Настоящая автоконфигурация, целиком. В ней нет ничего, кроме трёх идей:
 * <ol>
 *   <li><b>Регистрация.</b> Класс перечислен в
 *       {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}
 *       (слайд 175). Boot читает этот файл со всех jar в classpath — именно так
 *       подключение зависимости «само» добавляет бины. В Boot 2 файл назывался
 *       {@code spring.factories};</li>
 *   <li><b>Условия.</b> {@code @ConditionalOnClass} — «работай, только если
 *       нужная библиотека в classpath». {@code @ConditionalOnProperty} — «только
 *       если не выключили». {@code @ConditionalOnMissingBean} — «только если
 *       пользователь не объявил своё» (слайд 176);</li>
 *   <li><b>Уступчивость.</b> {@code @ConditionalOnMissingBean} и есть механизм
 *       правила «свой бин переопределяет автоконфигурацию» (слайд 177).
 *       Автоконфигурации обрабатываются <b>после</b> пользовательских — поэтому
 *       к моменту проверки пользовательский бин уже зарегистрирован.</li>
 * </ol>
 * {@code @AutoConfiguration} — это {@code @Configuration(proxyBeanMethods = false)}
 * плюс порядок относительно других автоконфигураций.
 */
@AutoConfiguration
@ConditionalOnClass(Greeter.class)
@ConditionalOnProperty(prefix = "sprbut.greeter", name = "enabled",
        havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(GreeterProperties.class)
public class GreeterAutoConfiguration {

    /**
     * Бин появится, только если пользователь не объявил свой {@link Greeter}.
     * Это одна строка, но именно она делает автоконфигурацию не навязчивой.
     */
    @Bean
    @ConditionalOnMissingBean
    public Greeter greeter(GreeterProperties properties) {
        return new SimpleGreeter(properties.getTemplate(), properties.isShout());
    }
}
