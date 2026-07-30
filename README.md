# SprBut — практикум по курсу Spring Boot

Практический проект по курсу **SprBut**: один модуль на каждый раздел презентации
плюс итоговый модуль с расширенным заданием по всему курсу.

**Презентация:**
[SprBut — план курса и слайды](https://docs.google.com/presentation/d/1bsebBSBpseNGlDBeEnZbYGq1Kt5A6qHR/edit?usp=sharing&ouid=103425146937158568285&rtpof=true&sd=true)

## О чём этот проект

Курс отвечает на один вопрос: **почему код в исходниках и поведение в рантайме —
это не одно и то же**. Первая половина показывает, чем эту разницу создают
(рефлексия, аннотации, генерация кода), вторая — как ею управляет Spring
(контейнер, прокси, конфигурация, автоконфигурация).

Каждый модуль устроен одинаково:

* **примеры** — по классу на пункт слайда, с docblock'ом, объясняющим назначение;
* **тесты** — покрывают каждый пример, проверяя ровно одно поведение;
* **расширенный пример** — итог модуля: рабочий инструмент, собирающий тему целиком;
* **README** — цитата из презентации, таблица примеров, ключевые выводы.

## Модули

| № | Модуль | Тема | Расширенный пример |
|---|---|---|---|
| 01 | [reflection-basics](01-reflection-basics/) | Reflection и метаданные в runtime | JSON-сериализатор целиком на рефлексии |
| 02 | [javabeans](02-javabeans/) | JavaBeans, record, Builder | Сравнение четырёх стилей одного класса |
| 03 | [reflection-api](03-reflection-api/) | `java.lang.reflect`: Field, Method, Constructor | Обходчик графа объектов |
| 04 | [reflection-advanced](04-reflection-advanced/) | JPMS, дженерики, MethodHandles, Proxy | Динамический прокси со своим `InvocationHandler` |
| 05 | [annotations-basics](05-annotations-basics/) | `@Target`, `@Retention`, `@Inherited` | Проверка объектов по аннотациям |
| 06 | [annotations-advanced](06-annotations-advanced/) | `@Repeatable`, `TYPE_USE`, композиции | Раскрытие вложенных мета-аннотаций |
| 07 | [annotation-processor](07-annotation-processor/) | `AbstractProcessor`, генерация кода | Свой процессор с JavaPoet |
| 08 | [apt-usage](08-apt-usage/) | Регистрация, раунды, `Filer` | Сборка, использующая сгенерированный код |
| 09 | [reflection-vs-apt](09-reflection-vs-apt/) | Runtime, compile-time, байткод | Замер цены каждого подхода |
| 10 | [lombok-mapstruct](10-lombok-mapstruct/) | Готовые генераторы | Слой отображения на MapStruct |
| 11 | [ioc-di](11-ioc-di/) | IoC, DI, фабрики | **Работающий IoC-контейнер на 150 строк** |
| 12 | [di-injection](12-di-injection/) | Конструктор, сеттер, поле, циклы | Аудитор точек внедрения |
| 13 | [bean-annotations](13-bean-annotations/) | `@Bean`, `@Scope`, `@Qualifier`, `@Conditional` | Отчёт о содержимом контейнера |
| 14 | [bean-lifecycle](14-bean-lifecycle/) | Восемь шагов жизненного цикла | Шкала с проверкой инвариантов порядка |
| 15 | [spring-modules-aop](15-spring-modules-aop/) | Модули Spring, AOP, прокси | Своя `@Retryable` и четыре вида self-invocation |
| 16 | [configuration](16-configuration/) | Приоритеты источников, профили | «Откуда взялось это значение» |
| 17 | [spring-annotations](17-spring-annotations/) | Стереотипы, `@Configuration`, условия | Каталог с раскрытием композиций |
| 18 | [boot-startup](18-boot-startup/) | Последовательность запуска Boot | Восстановление таймлайна старта |
| 19 | [autoconfiguration](19-autoconfiguration/) | Стартеры, `imports`, условия | Программный отчёт об условиях |
| 20 | [testing](20-testing/) | `@SpringBootTest`, срезы, `@MockBean` | Один функционал четырьмя способами |
| 21 | [common-mistakes](21-common-mistakes/) | Пять типичных ошибок контейнера | Диагност падений в духе `FailureAnalyzer` |
| 22 | [aot-native](22-aot-native/) | AOT, native image, `javax` → `jakarta` | Аудит готовности к native image |
| 23 | [capstone](23-capstone/) | **Итоговое задание: SprBut Tracker** | **Карта контейнера: приложение о самом себе** |

Разделы презентации, у которых материала на несколько занятий, разбиты
на отдельные модули: Reflection — на 01–04, аннотации — на 05–06,
annotation processor — на 07–09, IoC и DI — на 11–14, аннотации Spring — на 16–17.

## Итоговое задание

[Модуль 23 — SprBut Tracker](23-capstone/) — трекер задач, в котором встречается
каждая тема курса, от собственной аннотации с аспектом до подсказок для native image.

Итоговый расширенный пример —
[`ContextMap`](23-capstone/src/main/java/ru/sprbut/m23/extended/ContextMap.java):
приложение, которое рефлексией по живому контейнеру рассказывает о себе само —
чем стали его бины, во что они обёрнуты и какие методы перехвачены.

## Сборка

Требуется **JDK 17** и Maven 3.9+.

```bash
mvn test                      # все модули
mvn -pl 23-capstone test      # один модуль
mvn -pl 23-capstone spring-boot:run
```

Модули 01–09 активно используют рефлексию, поэтому корневой `pom.xml` открывает
базовые пакеты JDK через `--add-opens` — без этого `setAccessible` падал бы
на `InaccessibleObjectException` (см. [модуль 04](04-reflection-advanced/)).

## Стиль кода

Код следует правилам из [RULE.md](RULE.md): Elegant Objects и SOLID, неизменяемые
объекты, `final`-классы с одним-четырьмя атрибутами, docblock'и вместо
инлайн-комментариев, «злые тесты» с одним утверждением на тест.

Там, где правило спорит с темой курса, побеждает тема — и расхождение объясняется
прямо в docblock'е. Рефлексия, JavaBeans-геттеры и статические точки входа
существуют в модулях именно потому, что они и есть предмет разговора:
модуль про JavaBeans без сеттеров не показал бы ничего.
