package ru.sprbut.m23.extended;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import ru.sprbut.m23.audit.Audited;

/**
 * <b>Расширенный итог курса.</b>
 * <p>
 * Приложение, которое рассказывает о себе само: карта живого контейнера,
 * построенная рефлексией по нему же.
 * <p>
 * В одном классе сходится всё, чему был посвящён курс. Рефлексия читает
 * настоящие классы бинов и ищет в них аннотации (модули 01–06). Аннотация
 * {@link Audited} оказывается всего лишь меткой, которую кто-то должен прочесть
 * (модуль 05). Контейнер отдаёт определения бинов и их области видимости
 * (модули 11–14). {@code AopProxyUtils} снимает обёртку и показывает, что бин
 * в контексте — не тот объект, который написан в исходниках (модуль 15).
 * <p>
 * Практический смысл тот же, что у {@code /actuator/beans}: когда поведение
 * приложения расходится с кодом, разница почти всегда объясняется одной
 * из строчек этой карты.
 */
@Component
public final class ContextMap {

    private final ConfigurableApplicationContext context;

    public ContextMap(ConfigurableApplicationContext context) {
        this.context = context;
    }

    /**
     * Карточки прикладных бинов — только своих, без инфраструктуры Spring.
     */
    public List<BeanCard> cards() {
        ConfigurableListableBeanFactory beans = this.context.getBeanFactory();
        return Arrays.stream(beans.getBeanDefinitionNames())
            .filter(name -> mine(beans.getBeanDefinition(name).getBeanClassName()))
            .map(name -> card(name, beans))
            .toList();
    }

    /**
     * Проксирован ли бин — то есть перехватываются ли вызовы его методов.
     */
    public boolean proxied(String name) {
        return AopUtils.isAopProxy(this.context.getBean(name));
    }

    /**
     * Вид прокси: JDK-прокси вокруг интерфейса или CGLIB-подкласс.
     */
    public String proxy(String name) {
        Object bean = this.context.getBean(name);
        if (AopUtils.isJdkDynamicProxy(bean)) {
            return "jdk";
        }
        if (AopUtils.isCglibProxy(bean)) {
            return "cglib";
        }
        return "none";
    }

    private BeanCard card(String name, ConfigurableListableBeanFactory beans) {
        Class<?> type = AopProxyUtils.ultimateTargetClass(this.context.getBean(name));
        return new BeanCard(
            name,
            type.getName(),
            beans.getBeanDefinition(name).getScope().isEmpty()
                ? "singleton"
                : beans.getBeanDefinition(name).getScope(),
            audited(type)
        );
    }

    private List<String> audited(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
            .filter(method -> method.isAnnotationPresent(Audited.class))
            .map(this::operation)
            .sorted()
            .toList();
    }

    private String operation(Method method) {
        Audited audited = method.getAnnotation(Audited.class);
        return audited.value().isBlank() ? method.getName() : audited.value();
    }

    private boolean mine(String type) {
        return type != null && type.startsWith("ru.sprbut.m23");
    }
}
