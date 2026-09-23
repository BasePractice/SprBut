package ru.sprbut.m16;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

public abstract class Russian {
    static void main() throws IOException {
        Properties properties = new Properties();
        try (Reader reader = new InputStreamReader(
                Objects.requireNonNull(Russian.class.getResourceAsStream(
                        "/russian.properties")), StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        System.out.println(properties);
    }
}
