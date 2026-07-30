package ru.sprbut.m17.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;

@DisplayName("Расширенный пример: каталог аннотаций раскрывает композиции сам")
final class AnnotationCatalogTest {

    @Test
    @DisplayName("@Service сводится к @Component")
    void reducesServiceToComponent() {
        assertThat(
            "@Service cannot reduce to @Component",
            new Expanded(Service.class).parts(),
            hasItem("@Component")
        );
    }

    @Test
    @DisplayName("@Repository — тоже стереотип, несмотря на своё поведение")
    void treatsRepositoryAsStereotype() {
        assertThat(
            "@Repository cannot be a stereotype",
            new Expanded(Repository.class).stereotype(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("@RestController раскрывается до @Controller и @ResponseBody")
    void expandsRestController() {
        assertThat(
            "@RestController cannot expand to its two parts",
            new Expanded(RestController.class).parts(),
            hasItems("@Controller", "@ResponseBody")
        );
    }

    @Test
    @DisplayName("@SpringBootApplication включает автоконфигурацию и сканирование")
    void expandsSpringBootApplication() {
        assertThat(
            "@SpringBootApplication cannot expand to its three parts",
            new Expanded(SpringBootApplication.class).parts(),
            hasItems("@EnableAutoConfiguration", "@ComponentScan", "@Configuration")
        );
    }

    @Test
    @DisplayName("@Component сама сводится только к служебной @Indexed")
    void keepsComponentBasic() {
        assertThat(
            "@Component cannot reduce to the indexing marker alone",
            new Expanded(Component.class).parts(),
            contains("@Indexed")
        );
    }

    @Test
    @DisplayName("@Configuration стереотипом тоже является")
    void treatsConfigurationAsStereotype() {
        assertThat(
            "@Configuration cannot be a stereotype",
            new Expanded(Configuration.class).stereotype(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("объяснение читается как формула")
    void explainsAsFormula() {
        assertThat(
            "composition cannot be explained as a formula",
            new Expanded(Service.class).explain(),
            containsString("@Service = @Component")
        );
    }

    @Test
    @DisplayName("аннотация без мета-аннотаций объясняется как базовая")
    void explainsBaseAnnotation() {
        assertThat(
            "annotation without meta annotations cannot be called basic",
            new Expanded(org.springframework.core.annotation.Order.class).explain(),
            containsString("базовая аннотация")
        );
    }

    @Test
    @DisplayName("справочник содержит все аннотации модуля")
    void listsEveryAnnotation() {
        assertThat(
            "catalog cannot list every annotation of the module",
            new AnnotationCatalog().all(),
            hasKey("@SpringBootApplication")
        );
    }

    @Test
    @DisplayName("стереотипы отбираются по сводимости к @Component")
    void collectsStereotypes() {
        assertThat(
            "stereotypes cannot be collected by their reduction",
            new AnnotationCatalog().stereotypes(),
            hasItems("@Service", "@Repository", "@Controller")
        );
    }
}
