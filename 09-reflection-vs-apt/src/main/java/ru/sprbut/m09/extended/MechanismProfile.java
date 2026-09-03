/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// каталог механизмов: три константы и статические выборки по ним —
// это и есть таблица сравнения со слайдов 73–77
// @checkstyle DeclarationOrderCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m09.extended;

import java.util.List;

/**
 * Характеристики механизма — то, что на слайдах 73–77 перечислено словами,
 * здесь оформлено как данные, которые можно проверить тестом.
 * @param name Название механизма
 * @param phase Фаза, в которой он работает
 * @param flexibleAtRuntime Гибкость в рантайме
 * @param fastCalls Скорость вызовов
 * @param typeSafeAtCompileTime Проверка типов на компиляции
 * @param nativeImageFriendly Пригодность для native image
 * @param springUsesItFor Для чего механизм использует Spring
 * @since 1.0
 */
@SuppressWarnings({
    "PMD.LongVariable",
    "PMD.ProhibitPublicStaticMethods",
    "PMD.FieldDeclarationsShouldBeAtStartOfClass"
})
public record MechanismProfile(
    String name,
    Phase phase,
    boolean flexibleAtRuntime,
    boolean fastCalls,
    boolean typeSafeAtCompileTime,
    boolean nativeImageFriendly,
    List<String> springUsesItFor) {

    /**
     * Фаза, в которой работает механизм.
     * @since 1.0
     */
    public enum Phase {

        /**
         * Работает при компиляции.
         */
        COMPILE_TIME,

        /**
         * Работает в рантайме.
         */
        RUNTIME,

        /**
         * Работает и там, и там.
         */
        BOTH
    }

    /**
     * Компактный конструктор: список неизменяем.
     */
    public MechanismProfile {
        springUsesItFor = List.copyOf(springUsesItFor);
    }

    /**
     * Слайд 73: «Reflection: runtime, гибко, медленно».
     */
    public static final MechanismProfile REFLECTION = new MechanismProfile(
        "reflection",
        Phase.RUNTIME,
        true,
        false,
        false,
        false,
        List.of(
            "@Autowired: поиск и внедрение зависимостей",
            "@Value: чтение конфигурации в поля",
            "@EventListener: вызов методов-слушателей",
            "разбор аннотаций при старте контекста"
        )
    );

    /**
     * Слайд 74: «APT: compile-time, только генерация, быстро».
     */
    public static final MechanismProfile APT = new MechanismProfile(
        "apt",
        Phase.COMPILE_TIME,
        false,
        true,
        true,
        true,
        List.of(
            "spring-boot-configuration-processor: метаданные @ConfigurationProperties",
            "Spring AOT: генерация кода контекста при сборке",
            "MapStruct и Lombok в прикладном коде"
        )
    );

    /**
     * Слайд 75: «Байткод (CGLIB, ByteBuddy): и то, и другое».
     */
    public static final MechanismProfile BYTECODE = new MechanismProfile(
        "bytecode",
        Phase.BOTH,
        true,
        true,
        false,
        false,
        List.of(
            "CGLIB-прокси для бинов без интерфейса",
            "@Transactional и @Cacheable через прокси",
            "@Configuration: перехват вызовов @Bean-методов"
        )
    );

    /**
     * Все элементы.
     * @return Все элементы
     */
    public static List<MechanismProfile> all() {
        return List.of(
            MechanismProfile.REFLECTION,
            MechanismProfile.APT,
            MechanismProfile.BYTECODE
        );
    }

    /**
     * Слайд 76: «Spring использует все три механизма».
     * @return Признак того, что все три механизма задействованы
     */
    public static boolean springUsesAllThree() {
        return MechanismProfile.all().stream()
            .allMatch(profile -> !profile.springUsesItFor().isEmpty());
    }

    /**
     * Слайд 77: «В native image рефлексия почти недоступна».
     *
     * <p>Выживают только механизмы, у которых всё известно на этапе сборки.</p>
     *
     * @return Механизмы, переживающие сборку образа
     */
    public static List<String> survivingInNativeImage() {
        return MechanismProfile.all().stream()
            .filter(MechanismProfile::nativeImageFriendly)
            .map(MechanismProfile::name)
            .toList();
    }
}
