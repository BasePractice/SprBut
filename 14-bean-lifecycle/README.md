# Модуль 14 — Жизненный цикл бина

[Слайды 110–118, СХЕМА 7 (слайд 118)](https://docs.google.com/presentation/d/1bsebBSBpseNGlDBeEnZbYGq1Kt5A6qHR/edit#slide=id.p118) ·
[← к списку модулей](../README.md)

## Что в презентации

> 1. Создание экземпляра (конструктор). 2. Внедрение зависимостей. 3. `*Aware`-интерфейсы.
> 4. `BeanPostProcessor`: before. 5. `@PostConstruct`, `afterPropertiesSet`.
> 6. `BeanPostProcessor`: after. 7. Бин готов, `SmartLifecycle.start`.
> 8. `@PreDestroy`, `DisposableBean.destroy`.

Восемь шагов, каждый из которых в модуле пишет строчку в общий журнал.

## Примеры

| Класс | Шаг | Что показывает |
|---|---|---|
| [`ManagedBean`](src/main/java/ru/sprbut/m14/ManagedBean.java) | 1–5, 8 | Один бин, отмечающийся на каждом этапе своей жизни |
| [`AuditBeanPostProcessor`](src/main/java/ru/sprbut/m14/AuditBeanPostProcessor.java) | 4, 6 | Главная точка расширения контейнера: через неё сделано почти всё «магическое» |
| [`BackgroundWorker`](src/main/java/ru/sprbut/m14/BackgroundWorker.java) | 7 | `SmartLifecycle` — это не инициализация: он ждёт готовности всего контекста |
| [`LifecycleLog`](src/main/java/ru/sprbut/m14/LifecycleLog.java) | — | Общий журнал, по которому виден точный порядок |

## Расширенный пример

[`LifecycleTimeline`](src/main/java/ru/sprbut/m14/extended/LifecycleTimeline.java) —
временная шкала, которая не просто печатает журнал, а **проверяет инварианты**
восьми шагов: зависимости внедрены до `@PostConstruct`, `before` строго раньше
`after`, `start` позже готовности бина, уничтожение в обратном порядке.

Порядок шагов — не описательный факт, а контракт, на который можно опереться
в собственном коде. Здесь он превращён в набор утверждений.

## Ключевые выводы

* `BeanPostProcessor` — то место, где Spring перестаёт быть фреймворком
  и становится набором собственных расширений: `@Autowired`, `@PostConstruct`
  и AOP-прокси сделаны именно через него.
* `@PostConstruct` вызывается, когда готов **бин**; `SmartLifecycle.start` —
  когда готов **контекст**. Путаница между ними даёт самые труднообъяснимые ошибки.
* Прокси появляется на шаге 6: до него бин — это ещё «настоящий» объект,
  после — обёртка.
* Уничтожение идёт в порядке, обратном созданию, и `prototype`-бины в нём
  не участвуют вовсе.

## Запуск

```bash
mvn -pl 14-bean-lifecycle test
```
