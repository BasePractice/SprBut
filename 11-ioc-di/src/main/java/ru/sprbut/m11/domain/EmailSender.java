package ru.sprbut.m11.domain;

import java.util.ArrayList;
import java.util.List;

/** Реализация «по умолчанию». Отправленное копится в списке — так его видно в тестах. */
public class EmailSender implements NotificationSender {

    private final List<String> sent = new ArrayList<>();

    @Override
    public void send(String recipient, String message) {
        sent.add(recipient + " <- " + message);
    }

    @Override
    public String channel() {
        return "email";
    }

    public List<String> sent() {
        return List.copyOf(sent);
    }
}
