# Модуль 10 — Готовые генераторы: Lombok и MapStruct

[Слайды 79–82](https://docs.google.com/presentation/d/1bsebBSBpseNGlDBeEnZbYGq1Kt5A6qHR/edit#slide=id.p79) ·
[← к списку модулей](../README.md)

## Что в презентации

> Готовые Framework: **MapStruct**, **Lombok**.
>
> (Слайд 19: «Lombok, record, Immutability (Builder)» — здесь та же тема,
> но уже с рабочим инструментом.)

## Примеры

| Класс | Что показывает |
|---|---|
| [`CustomerEntity`](src/main/java/ru/sprbut/m10/lombok/CustomerEntity.java) | `@Data` + `@NoArgsConstructor` + `@AllArgsConstructor` — три аннотации вместо 60 строк модуля 02 |
| [`CustomerDto`](src/main/java/ru/sprbut/m10/lombok/CustomerDto.java) | `@Value` + `@Builder(toBuilder = true)` — неизменяемость и сборка по частям |
| [`Partial`](src/main/java/ru/sprbut/m10/lombok/samples/Partial.java) | `@Getter`/`@Setter` по отдельности и `AccessLevel`, в том числе `NONE` на одном поле |
| [`Service`](src/main/java/ru/sprbut/m10/lombok/samples/Service.java) | `@RequiredArgsConstructor` — основной способ внедрения зависимостей в Spring-коде |
| [`Account`](src/main/java/ru/sprbut/m10/lombok/samples/Account.java) | `@ToString(exclude)` и `@EqualsAndHashCode(of)`: пароль мимо логов, равенство по идентификатору |
| [`Order`](src/main/java/ru/sprbut/m10/lombok/samples/Order.java) | `@Builder` с `@Singular` — коллекция по элементу, результат неизменяем |
| [`Fluent`](src/main/java/ru/sprbut/m10/lombok/samples/Fluent.java) | `@Accessors(fluent)` лишает класс статуса JavaBean — и всё, что работает по соглашению, его не увидит |
| [`Generated`](src/main/java/ru/sprbut/m10/lombok/Generated.java) | Доказательство «хака AST» рефлексией: методов нет в исходнике, но они есть в байткоде |

## Расширенный пример

[`CustomerMapper`](src/main/java/ru/sprbut/m10/extended/CustomerMapper.java) — интерфейс
без реализации. `CustomerMapperImpl` появляется при компиляции:

```java
@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CustomerMapper {
    @Mapping(target = "fullName", expression = "java(entity.getFirstName() + \" \" + entity.getLastName())")
    @Mapping(target = "age",    source = "birthDate", qualifiedByName = "toAge")
    @Mapping(target = "status", source = "vip",       qualifiedByName = "toStatus")
    CustomerDto toDto(CustomerEntity entity);

    List<CustomerDto> toDtos(List<CustomerEntity> entities);   // сгенерируется сам
}
```

Здесь Lombok и MapStruct работают **в паре**, и это главная практическая деталь модуля:
MapStruct должен увидеть геттеры, которых нет в исходнике. Порядок процессоров
и `lombok-mapstruct-binding` в [`pom.xml`](pom.xml) — обязательны.

## Ключевые выводы

* Lombok — не обычный annotation processor. Штатное API умеет только **создавать
  новые файлы**; Lombok **меняет существующий класс** через внутренний AST javac.
  Отсюда и плагин для IDE, и хрупкость при смене мажорной версии JDK.
* MapStruct — наоборот, честный APT: генерирует отдельный класс с прямыми вызовами.
  В нём нет ни рефлексии, ни прокси, поэтому он работает в native image.
* `-Amapstruct.unmappedTargetPolicy=ERROR` превращает забытое поле в **ошибку сборки**.
  Рефлексивный `BeanUtils.copyProperties` в той же ситуации молча оставит `null`.
* `@Accessors(fluent = true)` удобен, но выводит класс из соглашения JavaBeans —
  `Introspector`, биндинг Spring и Jackson перестают его видеть.
* `Mappers.getMapper()` создаёт **новый экземпляр на каждый вызов**. В Spring-проектах
  используют `componentModel = "spring"`, чтобы маппер стал бином.

## Запуск

```bash
mvn -pl 10-lombok-mapstruct test
```

Сгенерированный `CustomerMapperImpl` смотреть здесь:
`target/generated-sources/annotations/ru/sprbut/m10/extended/`.
