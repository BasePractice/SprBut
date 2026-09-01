/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// поля срезов внедряет контейнер — приватными qulice их видеть не даёт,
// а имена mockMvc и objectMapper совпадают с именами бинов Spring
// @checkstyle VisibilityModifierCheck disable
// @checkstyle MemberNameCheck disable
package ru.sprbut.m20;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.sprbut.m20.domain.CatalogService;
import ru.sprbut.m20.domain.Product;
import ru.sprbut.m20.domain.ProductRepository;
import ru.sprbut.m20.web.ProductController;
import tools.jackson.databind.ObjectMapper;

/**
 * Слайды 180–185 (СХЕМА 13): срезы поднимают разный объём контекста.
 * @since 1.0
 */
@DisplayName("Слайды 180–185 (СХЕМА 13): срезы поднимают разный объём контекста")
final class SliceTests {

    /**
     * Тело запроса для среза сериализации.
     */
    private static final String PAYLOAD =
        "{\"sku\":\"SKU-1\",\"name\":\"Кофемолка\",\"price\":4990.00,\"available\":true}";

    private static ProductController.CreateRequest request() {
        return new ProductController.CreateRequest(
            "SKU-2", "Чайник", new BigDecimal("2990.00")
        );
    }

    /**
     * Полный контекст через {@code @SpringBootTest}.
     * @since 1.0
     */
    @Nested
    @SpringBootTest
    @DisplayName("@SpringBootTest — полный контекст")
    final class FullContext {

        /**
         * Контекст.
         */
        @Autowired
        ApplicationContext context;

        /**
         * Каталог.
         */
        @Autowired
        CatalogService catalog;

        @Test
        @DisplayName("Web-слой поднят целиком")
        void webLayerIsPresent() {
            MatcherAssert.assertThat(
                "full context cannot leave the controller out",
                this.context.containsBean("productController"),
                Matchers.equalTo(true)
            );
        }

        @Test
        @DisplayName("Сервисный слой поднят целиком")
        void serviceLayerIsPresent() {
            MatcherAssert.assertThat(
                "full context cannot leave the service out",
                this.context.getBean(CatalogService.class),
                Matchers.notNullValue()
            );
        }

        @Test
        @DisplayName("Слой данных поднят целиком")
        void persistenceLayerIsPresent() {
            MatcherAssert.assertThat(
                "full context cannot leave the repository out",
                this.context.getBean(ProductRepository.class),
                Matchers.notNullValue()
            );
        }

        @Test
        @DisplayName("JPA поднята вместе с остальным")
        void jpaIsPresent() {
            MatcherAssert.assertThat(
                "full context cannot leave JPA out",
                this.context.containsBean("entityManagerFactory"),
                Matchers.equalTo(true)
            );
        }

        @Test
        @DisplayName("Сквозной сценарий сохраняет товар")
        void savesProduct() {
            this.catalog.add("SKU-1", "Кофемолка", new BigDecimal("4990.00"));
            MatcherAssert.assertThat(
                "end to end scenario cannot save the product",
                this.catalog.bySku("SKU-1").getName(),
                Matchers.equalTo("Кофемолка")
            );
        }

        @Test
        @DisplayName("Сохранённый товар попадает в список доступных")
        void listsSavedProduct() {
            this.catalog.add("SKU-2", "Чайник", new BigDecimal("2990.00"));
            MatcherAssert.assertThat(
                "saved product cannot reach the available list",
                this.catalog.available().stream().map(Product::getSku).toList(),
                Matchers.hasItems("SKU-2")
            );
        }

        @Test
        @DisplayName("Ценник собирается с валютой из настроек")
        void buildsPriceTag() {
            this.catalog.add("SKU-3", "Тостер", new BigDecimal("3990.00"));
            MatcherAssert.assertThat(
                "price tag cannot use the configured currency",
                this.catalog.priceTag(this.catalog.bySku("SKU-3")),
                Matchers.equalTo("3990.00 RUB")
            );
        }

        @Test
        @DisplayName("Дубликат артикула отклоняется")
        void rejectsDuplicateSku() {
            this.catalog.add("SKU-DUP", "Первый", BigDecimal.TEN);
            MatcherAssert.assertThat(
                "duplicate sku cannot be rejected with an explanation",
                Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> this.catalog.add("SKU-DUP", "Второй", BigDecimal.ONE)
                ).getMessage(),
                Matchers.containsString("уже есть")
            );
        }
    }

    /**
     * Срез слоя данных через {@code @DataJpaTest}.
     * @since 1.0
     */
    @Nested
    @DataJpaTest
    @DisplayName("@DataJpaTest — только слой данных")
    final class DataSlice {

        /**
         * Репозиторий.
         */
        @Autowired
        ProductRepository repository;

        /**
         * Контекст.
         */
        @Autowired
        ApplicationContext context;

        @Test
        @DisplayName("Репозиторий в срезе есть")
        void repositoryIsLoaded() {
            MatcherAssert.assertThat(
                "data slice cannot provide the repository",
                this.repository,
                Matchers.notNullValue()
            );
        }

        @Test
        @DisplayName("Web-слой в срез не входит вовсе")
        void dontLoadWebLayer() {
            MatcherAssert.assertThat(
                "data slice cannot leave the controllers out",
                this.context.containsBean("productController"),
                Matchers.equalTo(false)
            );
        }

        @Test
        @DisplayName("Производный запрос находит сохранённый товар")
        void findsSavedProduct() {
            this.repository.save(new Product("SKU-A", "Дешёвый", new BigDecimal("100")));
            MatcherAssert.assertThat(
                "derived query cannot find the saved product",
                this.repository.findBySku("SKU-A").isPresent(),
                Matchers.equalTo(true)
            );
        }

        @Test
        @DisplayName("Производный запрос отбирает по цене")
        void filtersByPrice() {
            this.repository.save(new Product("SKU-A", "Дешёвый", new BigDecimal("100")));
            this.repository.save(new Product("SKU-B", "Дорогой", new BigDecimal("100000")));
            MatcherAssert.assertThat(
                "derived query cannot filter by price",
                this.repository.findCheaperThan(new BigDecimal("1000"))
                    .stream()
                    .map(Product::getSku)
                    .toList(),
                Matchers.contains("SKU-A")
            );
        }

        @Test
        @DisplayName("Каждый тест среза откатывается — данные не протекают между тестами")
        void rollsBackEachTest() {
            MatcherAssert.assertThat(
                "slice test cannot roll back its data",
                this.repository.count(),
                Matchers.equalTo(0L)
            );
        }
    }

    /**
     * Срез веб-слоя через {@code @WebMvcTest}.
     * @since 1.0
     */
    @Nested
    @WebMvcTest(ProductController.class)
    @DisplayName("@WebMvcTest — только web-слой")
    @SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
    final class WebSlice {

        /**
         * MockMvc.
         */
        @Autowired
        MockMvc mockMvc;

        /**
         * Маппер.
         */
        @Autowired
        ObjectMapper objectMapper;

        /**
         * Слайд 184: сервис подменяется моком, настоящий в срез не входит.
         */
        @MockitoBean
        CatalogService catalog;

        /**
         * Контекст.
         */
        @Autowired
        ApplicationContext context;

        @Test
        @DisplayName("Контроллер в срезе есть")
        void controllerIsLoaded() {
            MatcherAssert.assertThat(
                "web slice cannot provide the controller",
                this.context.containsBean("productController"),
                Matchers.equalTo(true)
            );
        }

        @Test
        @DisplayName("Репозитория в срезе нет")
        void dontLoadRepositories() {
            MatcherAssert.assertThat(
                "web slice cannot leave the repositories out",
                this.context.getBeanNamesForType(ProductRepository.class).length,
                Matchers.equalTo(0)
            );
        }

        @Test
        @DisplayName("JPA в срез не входит")
        void dontLoadJpa() {
            MatcherAssert.assertThat(
                "web slice cannot leave JPA out",
                this.context.containsBean("entityManagerFactory"),
                Matchers.equalTo(false)
            );
        }

        @Test
        @DisplayName("GET возвращает список из мока")
        void listsProducts() throws Exception {
            BDDMockito.given(this.catalog.available()).willReturn(
                List.of(new Product("SKU-1", "Кофемолка", new BigDecimal("4990.00")))
            );
            this.mockMvc.perform(MockMvcRequestBuilders.get("/api/products"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sku").value("SKU-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("Кофемолка"));
        }

        @Test
        @DisplayName("Отсутствующий товар превращается в 404 обработчиком исключений")
        void missingProductBecomesNotFound() throws Exception {
            BDDMockito
                .willThrow(new CatalogService.ProductNotFoundException("SKU-X"))
                .given(this.catalog)
                .bySku("SKU-X");
            this.mockMvc.perform(MockMvcRequestBuilders.get("/api/products/SKU-X"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string("Товар не найден: SKU-X"));
        }

        @Test
        @DisplayName("POST отдаёт 201 и тело созданного объекта")
        void postReturnsCreated() throws Exception {
            BDDMockito.given(
                this.catalog.add(
                    ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()
                )
            ).willReturn(new Product("SKU-2", "Чайник", new BigDecimal("2990.00")));
            this.mockMvc.perform(
                MockMvcRequestBuilders.post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.objectMapper.writeValueAsString(SliceTests.request()))
            ).andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.sku").value("SKU-2"));
        }

        @Test
        @DisplayName("Дубликат превращается в 400")
        void duplicateBecomesBadRequest() throws Exception {
            BDDMockito.given(
                this.catalog.add(
                    ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()
                )
            ).willThrow(new IllegalArgumentException("Товар с артикулом SKU-2 уже есть"));
            this.mockMvc.perform(
                MockMvcRequestBuilders.post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"sku\":\"SKU-2\",\"name\":\"Чайник\",\"price\":1}")
            ).andExpect(MockMvcResultMatchers.status().isBadRequest());
        }
    }

    /**
     * Срез сериализации через {@code @JsonTest}.
     * @since 1.0
     */
    @Nested
    @JsonTest
    @DisplayName("@JsonTest — только сериализация")
    final class JsonSlice {

        /**
         * JSON.
         */
        @Autowired
        JacksonTester<ProductController.ProductView> json;

        /**
         * Контекст.
         */
        @Autowired
        ApplicationContext context;

        @Test
        @DisplayName("Контроллера в срезе нет")
        void dontLoadController() {
            MatcherAssert.assertThat(
                "json slice cannot leave the controller out",
                this.context.containsBean("productController"),
                Matchers.equalTo(false)
            );
        }

        @Test
        @DisplayName("Репозитория в срезе нет")
        void dontLoadRepositories() {
            MatcherAssert.assertThat(
                "json slice cannot leave the repositories out",
                this.context.getBeanNamesForType(ProductRepository.class).length,
                Matchers.equalTo(0)
            );
        }

        @Test
        @DisplayName("Jackson в срезе есть")
        void jacksonIsLoaded() {
            MatcherAssert.assertThat(
                "json slice cannot provide the object mapper",
                this.context.getBean(ObjectMapper.class),
                Matchers.notNullValue()
            );
        }

        @Test
        @DisplayName("Сериализация даёт ожидаемый JSON")
        void serialises() throws Exception {
            MatcherAssert.assertThat(
                "serialisation cannot produce the expected JSON",
                this.json.write(
                    new ProductController.ProductView(
                        "SKU-1", "Кофемолка", new BigDecimal("4990.00"), true
                    )
                ).getJson(),
                Matchers.containsString("Кофемолка")
            );
        }

        @Test
        @DisplayName("Десериализация восстанавливает артикул")
        void deserialisesSku() throws Exception {
            MatcherAssert.assertThat(
                "deserialisation cannot restore the sku",
                this.json.parseObject(SliceTests.PAYLOAD).sku(),
                Matchers.equalTo("SKU-1")
            );
        }

        @Test
        @DisplayName("Десериализация восстанавливает цену")
        void deserialisesPrice() throws Exception {
            MatcherAssert.assertThat(
                "deserialisation cannot restore the price",
                this.json.parseObject(SliceTests.PAYLOAD).price(),
                Matchers.comparesEqualTo(new BigDecimal("4990.00"))
            );
        }
    }

    /**
     * Самый быстрый вариант — вообще без Spring.
     * @since 1.0
     */
    @Nested
    @DisplayName("Без Spring вовсе — самый быстрый тест")
    // @checkstyle NonStaticMethodCheck disable
    final class PlainUnitTest {

        @Test
        @DisplayName("Сервис тестируется обычным new, если зависимости внедрены конструктором")
        void serviceIsTestableWithoutSpring() {
            final ProductRepository fake = Mockito.mock(ProductRepository.class);
            BDDMockito.given(fake.findBySku("SKU-1")).willReturn(
                Optional.of(new Product("SKU-1", "Кофемолка", new BigDecimal("4990.00")))
            );
            final CatalogService service = new CatalogService(fake, "EUR");
            MatcherAssert.assertThat(
                "service cannot be tested without spring",
                service.priceTag(service.bySku("SKU-1")),
                Matchers.equalTo("4990.00 EUR")
            );
        }

        @Test
        @DisplayName("Контроллер тоже: MockMvc умеет работать standalone")
        @SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
        void controllerIsTestableStandalone() throws Exception {
            final CatalogService catalog = Mockito.mock(CatalogService.class);
            BDDMockito.given(catalog.available()).willReturn(List.of());
            MockMvcBuilders.standaloneSetup(new ProductController(catalog))
                .build()
                .perform(MockMvcRequestBuilders.get("/api/products"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("[]"));
        }
    }
}
