package ru.sprbut.m20;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.sprbut.m20.domain.CatalogService;
import ru.sprbut.m20.domain.Product;
import ru.sprbut.m20.domain.ProductRepository;
import ru.sprbut.m20.web.ProductController;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Слайды 180–185 (СХЕМА 13): срезы поднимают разный объём контекста")
class SliceTests {

    @Nested
    @SpringBootTest
    @DisplayName("@SpringBootTest — полный контекст")
    class FullContext {

        @Autowired
        ApplicationContext context;

        @Autowired
        CatalogService catalog;

        @Test
        @DisplayName("Поднимаются все слои: web, сервис, репозиторий, JPA")
        void everyLayerIsPresent() {
            assertThat(
                "cannot verify that every layer is present",
                context.containsBean("productController"),
                equalTo(true)
            );
            assertThat(
                "cannot verify that every layer is present",
                context.getBean(CatalogService.class),
                notNullValue()
            );
            assertThat(
                "cannot verify that every layer is present",
                context.getBean(ProductRepository.class),
                notNullValue()
            );
            assertThat(
                "cannot verify that every layer is present",
                context.containsBean("entityManagerFactory"),
                equalTo(true)
            );
        }

        @Test
        @DisplayName("Сквозной сценарий работает на настоящих бинах")
        void endToEndScenarioWorks() {
            catalog.add("SKU-1", "Кофемолка", new BigDecimal("4990.00"));

            assertThat(
                "cannot verify that end to end scenario works",
                catalog.bySku("SKU-1").getName(),
                equalTo("Кофемолка")
            );
            assertThat(
                "cannot verify that end to end scenario works",
                catalog.available().stream().map(Product::getSku).toList(),
                hasItems("SKU-1")
            );
            assertThat(
                "cannot verify that end to end scenario works",
                catalog.priceTag(catalog.bySku("SKU-1")),
                equalTo("4990.00 RUB")
            );
        }

        @Test
        @DisplayName("Дубликат артикула отклоняется")
        void duplicateSkuIsRejected() {
            catalog.add("SKU-DUP", "Первый", BigDecimal.TEN);

            assertThat(
                "cannot verify that duplicate sku is rejected",
                assertThrows(IllegalArgumentException.class, () -> catalog.add("SKU-DUP", "Второй", BigDecimal.ONE)).getMessage(),
                containsString("уже есть")
            );
        }
    }

    @Nested
    @DataJpaTest
    @DisplayName("@DataJpaTest — только слой данных")
    class DataSlice {

        @Autowired
        ProductRepository repository;

        @Autowired
        ApplicationContext context;

        @Test
        @DisplayName("Репозиторий работает, web-слой не поднимается вовсе")
        void onlyPersistenceLayerIsLoaded() {
            assertThat(
                "cannot verify that only persistence layer is loaded",
                repository,
                notNullValue()
            );
            assertThat(
                "data slice cannot leave the controllers out",
                context.containsBean("productController"),
                equalTo(false)
            );
        }

        @Test
        @DisplayName("Производные запросы работают на настоящей БД в памяти")
        void derivedQueriesWork() {
            repository.save(new Product("SKU-A", "Дешёвый", new BigDecimal("100")));
            repository.save(new Product("SKU-B", "Дорогой", new BigDecimal("100000")));

            assertThat(
                "derived query cannot find the saved product",
                repository.findBySku("SKU-A").isPresent(),
                equalTo(true)
            );
            assertThat(
                "cannot verify that derived queries work",
                repository.findCheaperThan(new BigDecimal("1000")).stream().map(Product::getSku).toList(),
                contains("SKU-A")
            );
        }

        @Test
        @DisplayName("Каждый тест среза откатывается — данные не протекают между тестами")
        void eachTestIsRolledBack() {
            assertThat(
                "slice test cannot roll back its data",
                repository.count(),
                equalTo(0L)
            );
        }
    }

    @Nested
    @WebMvcTest(ProductController.class)
    @DisplayName("@WebMvcTest — только web-слой")
    class WebSlice {

        @Autowired
        MockMvc mockMvc;

        @Autowired
        ObjectMapper objectMapper;

        /** Слайд 184: сервис подменяется моком, настоящий в срез не входит. */
        @MockitoBean
        CatalogService catalog;

        @Autowired
        ApplicationContext context;

        @Test
        @DisplayName("Поднят только web-слой: репозитория и JPA в контексте нет")
        void onlyWebLayerIsLoaded() {
            assertThat(
                "cannot verify that only web layer is loaded",
                context.containsBean("productController"),
                equalTo(true)
            );
            assertThat(
                "web slice cannot leave the repositories out",
                context.getBeanNamesForType(ProductRepository.class).length,
                equalTo(0)
            );
            assertThat(
                "cannot verify that only web layer is loaded",
                context.containsBean("entityManagerFactory"),
                equalTo(false)
            );
        }

        @Test
        @DisplayName("GET возвращает список из мока")
        void getReturnsList() throws Exception {
            given(catalog.available()).willReturn(
                    List.of(new Product("SKU-1", "Кофемолка", new BigDecimal("4990.00"))));

            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].sku").value("SKU-1"))
                    .andExpect(jsonPath("$[0].name").value("Кофемолка"));
        }

        @Test
        @DisplayName("Отсутствующий товар превращается в 404 обработчиком исключений")
        void missingProductBecomes404() throws Exception {
            willThrow(new CatalogService.ProductNotFoundException("SKU-X"))
                    .given(catalog).bySku("SKU-X");

            mockMvc.perform(get("/api/products/SKU-X"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string("Товар не найден: SKU-X"));
        }

        @Test
        @DisplayName("POST отдаёт 201 и тело созданного объекта")
        void postReturns201() throws Exception {
            given(catalog.add(any(), any(), any()))
                    .willReturn(new Product("SKU-2", "Чайник", new BigDecimal("2990.00")));

            var request = new ProductController.CreateRequest(
                    "SKU-2", "Чайник", new BigDecimal("2990.00"));

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.sku").value("SKU-2"));
        }

        @Test
        @DisplayName("Дубликат превращается в 400")
        void duplicateBecomes400() throws Exception {
            given(catalog.add(any(), any(), any()))
                    .willThrow(new IllegalArgumentException("Товар с артикулом SKU-2 уже есть"));

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"sku\":\"SKU-2\",\"name\":\"Чайник\",\"price\":1}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @JsonTest
    @DisplayName("@JsonTest — только сериализация")
    class JsonSlice {

        @Autowired
        JacksonTester<ProductController.ProductView> json;

        @Autowired
        ApplicationContext context;

        @Test
        @DisplayName("Поднят только Jackson: ни web, ни данных")
        void onlyJacksonIsLoaded() {
            assertThat(
                "cannot verify that only jackson is loaded",
                context.containsBean("productController"),
                equalTo(false)
            );
            assertThat(
                "json slice cannot leave the repositories out",
                context.getBeanNamesForType(ProductRepository.class).length,
                equalTo(0)
            );
            assertThat(
                "cannot verify that only jackson is loaded",
                context.getBean(ObjectMapper.class),
                notNullValue()
            );
        }

        @Test
        @DisplayName("Сериализация даёт ожидаемый JSON")
        void serialises() throws Exception {
            var view = new ProductController.ProductView(
                    "SKU-1", "Кофемолка", new BigDecimal("4990.00"), true);

            assertThat(
                "serialisation cannot produce the expected JSON",
                json.write(view).getJson(),
                containsString("Кофемолка")
            );
        }

        @Test
        @DisplayName("Десериализация восстанавливает объект")
        void deserialises() throws Exception {
            var parsed = json.parseObject(
                    "{\"sku\":\"SKU-1\",\"name\":\"Кофемолка\",\"price\":4990.00,\"available\":true}");

            assertThat(
                "cannot verify that deserialises",
                parsed.sku(),
                equalTo("SKU-1")
            );
            assertThat(
                "cannot verify that deserialises",
                parsed.price(),
                comparesEqualTo(new java.math.BigDecimal("4990.00"))
            );
        }
    }

    @Nested
    @DisplayName("Без Spring вовсе — самый быстрый тест")
    class PlainUnitTest {

        @Test
        @DisplayName("Сервис тестируется обычным new, если зависимости внедрены конструктором")
        void serviceIsTestableWithoutSpring() {
            ProductRepository fakeRepository = org.mockito.Mockito.mock(ProductRepository.class);
            given(fakeRepository.findBySku("SKU-1"))
                    .willReturn(java.util.Optional.of(
                            new Product("SKU-1", "Кофемолка", new BigDecimal("4990.00"))));

            CatalogService service = new CatalogService(fakeRepository, "EUR");

            assertThat(
                "cannot verify that service is testable without spring",
                service.priceTag(service.bySku("SKU-1")),
                equalTo("4990.00 EUR")
            );
        }

        @Test
        @DisplayName("Контроллер тоже: MockMvc умеет работать standalone")
        void controllerIsTestableStandalone() throws Exception {
            CatalogService catalog = org.mockito.Mockito.mock(CatalogService.class);
            given(catalog.available()).willReturn(List.of());

            MockMvc standalone = MockMvcBuilders
                    .standaloneSetup(new ProductController(catalog))
                    .build();

            standalone.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("[]"));
        }
    }

    /** Общий предок, чтобы не тянуть WebApplicationContext в каждый тест. */
    interface WebContextAware {
        WebApplicationContext context();
    }
}
