package ru.sprbut.m14;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Proxy;

/**
 * Шаги 4 и 6: {@link BeanPostProcessor} — главная точка расширения контейнера.
 * <p>
 * Через неё Spring реализует почти всё «магическое»: {@code @Autowired}
 * (AutowiredAnnotationBeanPostProcessor), {@code @PostConstruct}
 * (CommonAnnotationBeanPostProcessor), AOP-прокси
 * (AnnotationAwareAspectJAutoProxyCreator).
 * <p>
 * Ключевая деталь: {@code postProcessAfterInitialization} может вернуть
 * <b>другой объект</b>. Именно так на месте бина оказывается прокси —
 * и именно поэтому в контексте лежит не тот экземпляр, который создал
 * ваш конструктор (модуль 15).
 */
public class AuditBeanPostProcessor implements BeanPostProcessor {

    /** Интерфейс, реализации которого будут подменены прокси. */
    public interface Auditable {
        String describe();
    }

    /** Бин, который на выходе из контейнера окажется прокси, а не собой. */
    public static class AuditableBean implements Auditable {

        @Override
        public String describe() {
            return "оригинал";
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ManagedBean || bean instanceof AuditableBean) {
            LifecycleLog.record("4-bpp-before:" + beanName);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ManagedBean) {
            LifecycleLog.record("6-bpp-after:" + beanName);
            return bean;
        }
        if (bean instanceof Auditable auditable) {
            LifecycleLog.record("6-bpp-after:" + beanName);
            // Подмена объекта: в контейнер попадёт прокси, а не оригинал
            return Proxy.newProxyInstance(
                    bean.getClass().getClassLoader(),
                    new Class<?>[]{Auditable.class},
                    (proxy, method, args) -> {
                        Object result = method.invoke(auditable, args);
                        return "describe".equals(method.getName()) ? result + " (через прокси)" : result;
                    });
        }
        return bean;
    }
}
