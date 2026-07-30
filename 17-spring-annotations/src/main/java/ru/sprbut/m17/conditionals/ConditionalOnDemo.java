package ru.sprbut.m17.conditionals;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Слайды 148–149: {@code @ConditionalOnProperty} и {@code @ConditionalOnMissingBean}.
 * <p>
 * Это не «ещё две аннотации», а <b>основа всей автоконфигурации</b> Spring Boot
 * (модуль 19):
 * <ul>
 *   <li>{@code @ConditionalOnProperty} — включить кусок конфигурации настройкой;</li>
 *   <li>{@code @ConditionalOnMissingBean} — «дай значение по умолчанию, но
 *       отступи, если пользователь объявил своё». Именно эта аннотация делает
 *       возможным правило «свой бин переопределяет автоконфигурацию» (слайд 177).</li>
 * </ul>
 * Порядок важен: условие проверяется в момент разбора конфигурации, поэтому
 * пользовательская конфигурация должна быть обработана <b>раньше</b>
 * автоконфигурации — Boot это гарантирует.
 */
public final class ConditionalOnDemo {

    private ConditionalOnDemo() {
    }

    public interface Notifier {
        String send(String message);
    }

    public record ConsoleNotifier(String prefix) implements Notifier {
        @Override
        public String send(String message) {
            return prefix + ": " + message;
        }
    }

    /** Роль «автоконфигурации»: даёт бин по умолчанию, но уступает пользовательскому. */
    @Configuration
    public static class DefaultsConfig {

        @Bean
        @ConditionalOnMissingBean(Notifier.class)
        public Notifier notifier() {
            return new ConsoleNotifier("по умолчанию");
        }

        @Bean
        @ConditionalOnProperty(name = "sprbut.metrics.enabled", havingValue = "true")
        public String metricsCollector() {
            return "метрики включены";
        }

        /** {@code matchIfMissing} — включено, пока явно не выключили. */
        @Bean
        @ConditionalOnProperty(name = "sprbut.audit.enabled", havingValue = "true",
                matchIfMissing = true)
        public String auditCollector() {
            return "аудит включён";
        }
    }

    /** Роль пользователя: свой бин, который должен победить. */
    @Configuration
    public static class UserConfig {

        @Bean
        public Notifier notifier() {
            return new ConsoleNotifier("пользовательский");
        }
    }
}
