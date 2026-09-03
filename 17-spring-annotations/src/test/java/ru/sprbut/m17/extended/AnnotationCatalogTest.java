/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m17.extended;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

/**
 * Расширенный пример: каталог аннотаций раскрывает композиции сам.
 * @since 1.0
 */
@DisplayName("Расширенный пример: каталог аннотаций раскрывает композиции сам")
final class AnnotationCatalogTest {

    @Test
    @DisplayName("@Service сводится к @Component")
    void reducesServiceToComponent() {
        MatcherAssert.assertThat(
            "@Service cannot reduce to @Component",
            new Expanded(Service.class).parts(),
            Matchers.hasItem("@Component")
        );
    }

    @Test
    @DisplayName("@Repository — тоже стереотип, несмотря на своё поведение")
    void treatsRepositoryAsStereotype() {
        MatcherAssert.assertThat(
            "@Repository cannot be a stereotype",
            new Expanded(Repository.class).stereotype(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("@RestController раскрывается до @Controller и @ResponseBody")
    void expandsRestController() {
        MatcherAssert.assertThat(
            "@RestController cannot expand to its two parts",
            new Expanded(RestController.class).parts(),
            Matchers.hasItems("@Controller", "@ResponseBody")
        );
    }

    @Test
    @DisplayName("@SpringBootApplication включает автоконфигурацию и сканирование")
    void expandsSpringBootApplication() {
        MatcherAssert.assertThat(
            "@SpringBootApplication cannot expand to its three parts",
            new Expanded(SpringBootApplication.class).parts(),
            Matchers.hasItems("@EnableAutoConfiguration", "@ComponentScan", "@Configuration")
        );
    }

    @Test
    @DisplayName("@Component сама сводится только к служебной @Indexed")
    void keepsComponentBasic() {
        MatcherAssert.assertThat(
            "@Component cannot reduce to the indexing marker alone",
            new Expanded(Component.class).parts(),
            Matchers.contains("@Indexed")
        );
    }

    @Test
    @DisplayName("@Configuration — тоже стереотип")
    void treatsConfigurationAsStereotype() {
        MatcherAssert.assertThat(
            "@Configuration cannot be a stereotype",
            new Expanded(Configuration.class).stereotype(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("объяснение читается как формула")
    void explainsAsFormula() {
        MatcherAssert.assertThat(
            "composition cannot be explained as a formula",
            new Expanded(Service.class).explain(),
            Matchers.containsString("@Service = @Component")
        );
    }

    @Test
    @DisplayName("аннотация без мета-аннотаций объясняется как базовая")
    void explainsBaseAnnotation() {
        MatcherAssert.assertThat(
            "annotation without meta annotations cannot be called basic",
            new Expanded(Order.class).explain(),
            Matchers.containsString("базовая аннотация")
        );
    }

    @Test
    @DisplayName("справочник содержит все аннотации модуля")
    void listsEveryAnnotation() {
        MatcherAssert.assertThat(
            "catalog cannot list every annotation of the module",
            new AnnotationCatalog().all(),
            Matchers.hasKey("@SpringBootApplication")
        );
    }

    @Test
    @DisplayName("стереотипы отбираются по сводимости к @Component")
    void collectsStereotypes() {
        MatcherAssert.assertThat(
            "stereotypes cannot be collected by their reduction",
            new AnnotationCatalog().stereotypes(),
            Matchers.hasItems("@Service", "@Repository", "@Controller")
        );
    }
}
