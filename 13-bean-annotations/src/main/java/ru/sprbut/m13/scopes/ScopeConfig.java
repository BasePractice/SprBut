package ru.sprbut.m13.scopes;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Слайд 101: {@code @Scope (singleton, prototype, request, session)}.
 * <p>
 * Скоуп отвечает на один вопрос: <b>сколько экземпляров бина существует
 * и как долго они живут</b>.
 * <ul>
 *   <li><b>singleton</b> (по умолчанию) — один на контейнер, создаётся при старте;</li>
 *   <li><b>prototype</b> — новый на каждый запрос из контейнера. Внимание:
 *       контейнер <b>не управляет</b> его уничтожением, {@code @PreDestroy}
 *       у prototype не вызывается никогда;</li>
 *   <li><b>request</b>, <b>session</b> — веб-скоупы, требуют веб-контекста.</li>
 * </ul>
 * Отдельная тема — {@code proxyMode} (слайд 110): prototype внутри singleton
 * без прокси создаётся один раз и дальше ведёт себя как singleton.
 */
@Configuration
public class ScopeConfig {

    /** Считает, сколько раз вообще создавался объект каждого типа. */
    public static final AtomicInteger SINGLETON_INSTANCES = new AtomicInteger();
    public static final AtomicInteger PROTOTYPE_INSTANCES = new AtomicInteger();

    public static void resetCounters() {
        SINGLETON_INSTANCES.set(0);
        PROTOTYPE_INSTANCES.set(0);
    }

    public static class SingletonBean {
        private final int serial;

        public SingletonBean() {
            this.serial = SINGLETON_INSTANCES.incrementAndGet();
        }

        public int serial() {
            return serial;
        }
    }

    public static class PrototypeBean {
        private final int serial;

        public PrototypeBean() {
            this.serial = PROTOTYPE_INSTANCES.incrementAndGet();
        }

        public int serial() {
            return serial;
        }
    }

    /**
     * Singleton, внутрь которого внедрён prototype <b>без прокси</b>.
     * Классическая ловушка: зависимость создалась один раз вместе с владельцем
     * и больше не меняется — «prototype» превратился в singleton (слайд 110).
     */
    public static class HolderWithoutProxy {
        private final PrototypeBean prototype;

        public HolderWithoutProxy(PrototypeBean prototype) {
            this.prototype = prototype;
        }

        public int prototypeSerial() {
            return prototype.serial();
        }
    }

    /**
     * Тот же случай, но prototype объявлен с {@code proxyMode}.
     * Внутрь попадает прокси, и каждый вызов метода получает свежий экземпляр.
     */
    public static class HolderWithProxy {
        private final ProxiedPrototypeBean prototype;

        public HolderWithProxy(ProxiedPrototypeBean prototype) {
            this.prototype = prototype;
        }

        public int prototypeSerial() {
            return prototype.serial();
        }
    }

    public static class ProxiedPrototypeBean {
        private final int serial;

        public ProxiedPrototypeBean() {
            this.serial = PROTOTYPE_INSTANCES.incrementAndGet();
        }

        public int serial() {
            return serial;
        }
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public SingletonBean singletonBean() {
        return new SingletonBean();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public PrototypeBean prototypeBean() {
        return new PrototypeBean();
    }

    /**
     * {@code proxyMode = TARGET_CLASS} — в место внедрения подставляется
     * CGLIB-прокси, который на каждый вызов метода достаёт новый бин из контейнера.
     */
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public ProxiedPrototypeBean proxiedPrototypeBean() {
        return new ProxiedPrototypeBean();
    }

    @Bean
    public HolderWithoutProxy holderWithoutProxy(PrototypeBean prototype) {
        return new HolderWithoutProxy(prototype);
    }

    @Bean
    public HolderWithProxy holderWithProxy(ProxiedPrototypeBean prototype) {
        return new HolderWithProxy(prototype);
    }
}
