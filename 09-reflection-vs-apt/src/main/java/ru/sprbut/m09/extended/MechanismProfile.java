package ru.sprbut.m09.extended;

import java.util.List;

/**
 * Характеристики механизма — то, что на слайдах 73–77 перечислено словами,
 * здесь оформлено как данные, которые можно проверить тестом.
 */
public record MechanismProfile(String name,
                               Phase phase,
                               boolean flexibleAtRuntime,
                               boolean fastCalls,
                               boolean typeSafeAtCompileTime,
                               boolean nativeImageFriendly,
                               List<String> springUsesItFor) {

    public enum Phase { COMPILE_TIME, RUNTIME, BOTH }

    public MechanismProfile {
        springUsesItFor = List.copyOf(springUsesItFor);
    }

    /** Слайд 73: «Reflection: runtime, гибко, медленно». */
    public static final MechanismProfile REFLECTION = new MechanismProfile(
            "reflection",
            Phase.RUNTIME,
            true,
            false,
            false,
            false,
            List.of("@Autowired: поиск и внедрение зависимостей",
                    "@Value: чтение конфигурации в поля",
                    "@EventListener: вызов методов-слушателей",
                    "разбор аннотаций при старте контекста"));

    /** Слайд 74: «APT: compile-time, только генерация, быстро». */
    public static final MechanismProfile APT = new MechanismProfile(
            "apt",
            Phase.COMPILE_TIME,
            false,
            true,
            true,
            true,
            List.of("spring-boot-configuration-processor: метаданные @ConfigurationProperties",
                    "Spring AOT: генерация кода контекста при сборке",
                    "MapStruct и Lombok в прикладном коде"));

    /** Слайд 75: «Байткод (CGLIB, ByteBuddy): и то, и другое». */
    public static final MechanismProfile BYTECODE = new MechanismProfile(
            "bytecode",
            Phase.BOTH,
            true,
            true,
            false,
            false,
            List.of("CGLIB-прокси для бинов без интерфейса",
                    "@Transactional и @Cacheable через прокси",
                    "@Configuration: перехват вызовов @Bean-методов"));

    public static List<MechanismProfile> all() {
        return List.of(REFLECTION, APT, BYTECODE);
    }

    /** Слайд 76: «Spring использует все три механизма». */
    public static boolean springUsesAllThree() {
        return all().stream().allMatch(p -> !p.springUsesItFor().isEmpty());
    }

    /**
     * Слайд 77: «В native image рефлексия почти недоступна».
     * Выживают только механизмы, у которых всё известно на этапе сборки.
     */
    public static List<String> survivingInNativeImage() {
        return all().stream()
                .filter(MechanismProfile::nativeImageFriendly)
                .map(MechanismProfile::name)
                .toList();
    }
}
