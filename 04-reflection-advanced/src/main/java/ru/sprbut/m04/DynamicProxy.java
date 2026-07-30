package ru.sprbut.m04;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * Слайд 35 и СХЕМА 2 (слайд 36): «Proxy и InvocationHandler — основа Spring AOP».
 * <p>
 * Схема вызова: <b>вызов → Proxy → InvocationHandler → цель</b>.
 * <p>
 * {@link Proxy#newProxyInstance} генерирует класс в runtime, который реализует
 * указанные интерфейсы. Каждый вызов любого метода этого класса попадает
 * в один-единственный {@link InvocationHandler#invoke}. Оттуда мы решаем:
 * вызвать цель, подменить результат, залогировать, начать транзакцию.
 * <p>
 * Ограничение, которое напрямую объясняет поведение Spring AOP:
 * JDK-прокси реализует <b>только интерфейсы</b>. Нет интерфейса — Spring
 * переключается на CGLIB-подкласс (слайд 122, модуль 15).
 */
public final class DynamicProxy {

    private DynamicProxy() {
    }

    public interface Greeter {
        String greet(String name);

        int length(String text);

        default String greetTwice(String name) {
            return greet(name) + " " + greet(name);
        }
    }

    /** Реальная цель, вокруг которой строится прокси. */
    public static class SimpleGreeter implements Greeter {
        @Override
        public String greet(String name) {
            return "Привет, " + name;
        }

        @Override
        public int length(String text) {
            return text.length();
        }
    }

    /**
     * Логирующий прокси: цель не меняется, поведение добавляется снаружи.
     * Это и есть суть AOP — сквозная функциональность живёт отдельно от кода.
     */
    @SuppressWarnings("unchecked")
    public static <T> T logging(Class<T> iface, T target, List<String> log) {
        return (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class<?>[]{iface},
                (proxy, method, args) -> {
                    log.add("→ " + method.getName());
                    try {
                        Object result = method.invoke(target, args);
                        log.add("← " + method.getName() + " = " + result);
                        return result;
                    } catch (InvocationTargetException e) {
                        log.add("✗ " + method.getName() + " : " + e.getCause());
                        throw e.getCause();
                    }
                });
    }

    /**
     * Прокси вообще без цели: поведение целиком синтезируется из метаданных метода.
     * Так работают репозитории Spring Data — интерфейс есть, реализации нет.
     */
    @SuppressWarnings("unchecked")
    public static <T> T stub(Class<T> iface) {
        return (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class<?>[]{iface},
                (proxy, method, args) -> defaultValueFor(method.getReturnType()));
    }

    /**
     * Прокси, реализующий несколько интерфейсов сразу — то, чего подкласс
     * (CGLIB) сделать не может.
     */
    public static Object multiInterface(Class<?>[] interfaces, InvocationHandler handler) {
        return Proxy.newProxyInstance(DynamicProxy.class.getClassLoader(), interfaces, handler);
    }

    /**
     * Проверка, что объект — сгенерированный прокси. Полезно при отладке:
     * в стектрейсе Spring такие классы называются {@code $Proxy17}.
     */
    public static boolean isProxy(Object candidate) {
        return Proxy.isProxyClass(candidate.getClass());
    }

    public static InvocationHandler handlerOf(Object proxy) {
        return Proxy.getInvocationHandler(proxy);
    }

    /**
     * Ключевое ограничение: self-invocation. Если цель внутри себя вызывает
     * свой же метод (как {@code greetTwice} вызывает {@code greet}), этот вызов
     * идёт напрямую и <b>минует прокси</b>. Ровно поэтому в Spring не работает
     * {@code @Transactional} на методе, вызванном из соседнего метода того же бина
     * (слайд 124, модуль 15).
     */
    public static List<String> demonstrateSelfInvocation() {
        List<String> log = new ArrayList<>();
        Greeter proxy = logging(Greeter.class, new SimpleGreeter(), log);
        proxy.greetTwice("Мир");
        return log;
    }

    private static Object defaultValueFor(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == void.class) {
            return null;
        }
        if (type == char.class) {
            return '\0';
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == double.class) {
            return 0.0d;
        }
        if (type == float.class) {
            return 0.0f;
        }
        return 0;
    }
}
