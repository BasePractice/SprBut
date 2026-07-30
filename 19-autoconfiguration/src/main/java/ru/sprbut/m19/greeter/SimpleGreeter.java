package ru.sprbut.m19.greeter;

/** Реализация по умолчанию, которую поставляет автоконфигурация. */
public class SimpleGreeter implements Greeter {

    private final String template;
    private final boolean shout;

    public SimpleGreeter(String template, boolean shout) {
        this.template = template;
        this.shout = shout;
    }

    @Override
    public String greet(String name) {
        String message = template.replace("{name}", name);
        return shout ? message.toUpperCase() : message;
    }

    @Override
    public String flavour() {
        return "simple";
    }
}
