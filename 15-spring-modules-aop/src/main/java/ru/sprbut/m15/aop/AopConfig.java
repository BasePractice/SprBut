package ru.sprbut.m15.aop;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Включает автоматическое создание прокси вокруг бинов, попадающих под pointcut.
 * <p>
 * {@code @EnableAspectJAutoProxy} регистрирует
 * {@code AnnotationAwareAspectJAutoProxyCreator} — обычный
 * {@link org.springframework.beans.factory.config.BeanPostProcessor}, который
 * на шаге 6 жизненного цикла (модуль 14) возвращает вместо бина его прокси.
 */
@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackageClasses = AopConfig.class)
public class AopConfig {
}
