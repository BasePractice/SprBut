package ru.sprbut.m17.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Расширенный пример: каталог аннотаций Spring с раскрытием композиций")
class AnnotationCatalogTest {

    @Test
    @DisplayName("Слайд 145: @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan")
    void springBootApplicationIsAComposition() {
        assertThat(AnnotationCatalog.expand(SpringBootApplication.class))
                .contains("@SpringBootApplication",
                        "@SpringBootConfiguration",
                        "@Configuration",
                        "@EnableAutoConfiguration",
                        "@ComponentScan",
                        "@Component");
    }

    @Test
    @DisplayName("Слайд 155: @RestController = @Controller + @ResponseBody")
    void restControllerIsAComposition() {
        assertThat(AnnotationCatalog.expand(RestController.class))
                .contains("@RestController", "@Controller", "@ResponseBody", "@Component");
    }

    @Test
    @DisplayName("Все стереотипы сводятся к @Component")
    void stereotypesReduceToComponent() {
        assertThat(AnnotationCatalog.isStereotype(Service.class)).isTrue();
        assertThat(AnnotationCatalog.isStereotype(Repository.class)).isTrue();
        assertThat(AnnotationCatalog.isStereotype(RestController.class)).isTrue();
        assertThat(AnnotationCatalog.isStereotype(Configuration.class)).isTrue();

        assertThat(AnnotationCatalog.isStereotype(Bean.class))
                .as("@Bean — не стереотип: он помечает метод, а не класс")
                .isFalse();
    }

    @Test
    @DisplayName("@Component — базовая аннотация, дальше раскрывать нечего")
    void componentIsTheBase() {
        assertThat(AnnotationCatalog.reducesTo(Component.class))
                .containsExactly("@Indexed");
        assertThat(AnnotationCatalog.explain(Component.class))
                .contains("@Component = @Indexed");
    }

    @Test
    @DisplayName("Объяснение состава читается человеком")
    void explanationIsReadable() {
        assertThat(AnnotationCatalog.explain(RestController.class))
                .startsWith("@RestController = ")
                .contains("@Controller")
                .contains("@ResponseBody");
    }

    @Test
    @DisplayName("Каталог покрывает весь список аннотаций со слайдов 140–149")
    void catalogCoversTheSlides() {
        assertThat(AnnotationCatalog.catalog())
                .containsKeys("@Component", "@Service", "@Repository", "@Controller",
                        "@RestController", "@Configuration", "@Bean", "@ComponentScan",
                        "@SpringBootApplication");
    }

    @Test
    @DisplayName("Список стереотипов вычисляется, а не задаётся вручную")
    void stereotypeListIsDerived() {
        assertThat(AnnotationCatalog.stereotypes())
                .contains("@Component", "@Service", "@Repository", "@Controller",
                        "@RestController", "@Configuration")
                .doesNotContain("@Bean");
    }

    @Test
    @DisplayName("Обход не зацикливается на служебных аннотациях языка")
    void javaBuiltinsAreExcluded() {
        assertThat(AnnotationCatalog.expand(Service.class))
                .doesNotContain("@Retention", "@Target", "@Documented");
    }
}
