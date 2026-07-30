package ru.sprbut.m11.domain;

import java.util.ArrayList;
import java.util.List;

/** Альтернативная реализация — нужна, чтобы было что подменять. */
public class SmsSender implements NotificationSender {

    private final List<String> sent = new ArrayList<>();

    @Override
    public void send(String recipient, String message) {
        sent.add("SMS " + recipient + ": " + message);
    }

    @Override
    public String channel() {
        return "sms";
    }

    public List<String> sent() {
        return List.copyOf(sent);
    }
}
