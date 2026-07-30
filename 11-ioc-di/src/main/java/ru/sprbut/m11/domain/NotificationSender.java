package ru.sprbut.m11.domain;

/**
 * Абстракция, ради которой всё и затевается: пока код зависит от интерфейса,
 * его реализацию можно подменить — контейнером, фабрикой или руками в тесте.
 */
public interface NotificationSender {

    void send(String recipient, String message);

    String channel();
}
