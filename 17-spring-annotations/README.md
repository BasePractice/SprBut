# Модуль 17 — Аннотации Spring

[Слайды 155–172, СХЕМА 22 (слайд 173)](https://docs.google.com/presentation/d/1zJBrQvw25ehkCVgjoY5U0-Gap6JR3ogs/edit#slide=id.p150) ·
[← к списку модулей](../README.md)

## Что в презентации

> `@ComponentScan` (`ClassPathBeanDefinitionScanner`), `@Component`, `@Repository`,
> `@Controller`, `@Configuration`, `@Bean`, `@SpringBootApplication`,
> `@EnableAutoConfiguration`, `@RestController`, `@Value`.
>
> `@Service` — сервисный слой. `@Transactional` — через AOP-прокси.
> `@ConditionalOnProperty`. `@ConditionalOnMissingBean`.
> `@Scope` с `proxyMode` для prototype в singleton.

## Примеры

| Класс | Слайд | Что показывает |
|---|---|---|
| [`Stereotypes`](src/main/java/ru/sprbut/m17/stereotypes/Stereotypes.java) | 140–144 | Все четыре стереотипа технически одно и то же — но не для инструментов |
| [`ProxyBeanMethods`](src/main/java/ru/sprbut/m17/configuration/ProxyBeanMethods.java) | 143 | `@Configuration` сам оборачивается CGLIB — почти всегда неочевидно |
| [`TransactionalDemo`](src/main/java/ru/sprbut/m17/transactional/TransactionalDemo.java) | 147 | Настоящий `@Transactional` со своим менеджером: видна механика, а не база |
| [`ConditionalOnDemo`](src/main/java/ru/sprbut/m17/conditionals/ConditionalOnDemo.java) | 148–149 | Не «ещё две аннотации», а основа всей автоконфигурации |

## Расширенный пример

[`AnnotationCatalog`](src/main/java/ru/sprbut/m17/extended/AnnotationCatalog.java) —
каталог аннотаций Spring, который **сам раскрывает композиции**.

`@RestController` разворачивается в `@Controller` + `@ResponseBody`,
`@SpringBootApplication` — в три аннотации сразу. Пока это не видно списком,
поведение аннотаций приходится запоминать; после — выводится.

## Ключевые выводы

* Стереотипы различаются не поведением контейнера, а смыслом для человека
  и для инструментов: `@Repository`, например, включает перевод исключений
  доступа к данным.
* `@Configuration` с `proxyBeanMethods = true` перехватывает вызовы `@Bean`-методов
  друг к другу — поэтому один и тот же метод отдаёт один и тот же бин.
* Композиция аннотаций — обычный механизм Java плюс обход метаданных Spring.
  Своя `@MyApp` пишется точно так же.
* `@ConditionalOnProperty` и `@ConditionalOnMissingBean` работают **до** создания
  бинов, на этапе разбора определений.

## Запуск

```bash
mvn -pl 17-spring-annotations test
```
