/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m20;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.sprbut.m20.domain.CatalogService;
import ru.sprbut.m20.domain.Product;
import ru.sprbut.m20.domain.ProductRepository;
import ru.sprbut.m20.web.ProductController;
import tools.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@DisplayName("Слайды 180–185 (СХЕМА 13): срезы поднимают разный объём контекста")
class SliceTests {

    @Nested
    @SpringBootTest
    @DisplayName("@SpringBootTest — полный контекст")
    class FullContext {

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
        @DisplayName("Поднимаются все слои: web, сервис, репозиторий, JPA")
        void everyLayerIsPresent() {
            MatcherAssert.assertThat(
                "cannot verify that every layer is present",
                this.context.containsBean("productController"),
                Matchers.equalTo(true)
            );
            MatcherAssert.assertThat(
                "cannot verify that every layer is present",
                this.context.getBean(CatalogService.class),
                Matchers.notNullValue()
            );
            MatcherAssert.assertThat(
                "cannot verify that every layer is present",
                this.context.getBean(ProductRepository.class),
                Matchers.notNullValue()
            );
            MatcherAssert.assertThat(
                "cannot verify that every layer is present",
                this.context.containsBean("entityManagerFactory"),
                Matchers.equalTo(true)
            );
        }

        @Test
        @DisplayName("Сквозной сценарий работает на настоящих бинах")
        void endToEndScenarioWorks() {
            this.catalog.add("SKU-1", "Кофемолка", new BigDecimal("4990.00"));
            MatcherAssert.assertThat(
                "cannot verify that end to end scenario works",
                this.catalog.bySku("SKU-1").getName(),
                Matchers.equalTo("Кофемолка")
            );
            MatcherAssert.assertThat(
                "cannot verify that end to end scenario works",
                this.catalog.available().stream().map(Product::getSku).toList(),
                Matchers.hasItems("SKU-1")
            );
            MatcherAssert.assertThat(
                "cannot verify that end to end scenario works",
                this.catalog.priceTag(this.catalog.bySku("SKU-1")),
                Matchers.equalTo("4990.00 RUB")
            );
        }

        @Test
        @DisplayName("Дубликат артикула отклоняется")
        void duplicateSkuIsRejected() {
            this.catalog.add("SKU-DUP", "Первый", BigDecimal.TEN);
            MatcherAssert.assertThat(
                "cannot verify that duplicate sku is rejected",
                Assertions.assertThrows(IllegalArgumentException.class, () -> this.catalog.add("SKU-DUP", "Второй", BigDecimal.ONE)).getMessage(),
                Matchers.containsString("уже есть")
            );
        }
    }

    @Nested
    @DataJpaTest
    @DisplayName("@DataJpaTest — только слой данных")
    class DataSlice {

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
        @DisplayName("Репозиторий работает, web-слой не поднимается вовсе")
        void onlyPersistenceLayerIsLoaded() {
            MatcherAssert.assertThat(
                "cannot verify that only persistence layer is loaded",
                this.repository,
                Matchers.notNullValue()
            );
            MatcherAssert.assertThat(
                "data slice cannot leave the controllers out",
                this.context.containsBean("productController"),
                Matchers.equalTo(false)
            );
        }

        @Test
        @DisplayName("Производные запросы работают на настоящей БД в памяти")
        void derivedQueriesWork() {
            this.repository.save(new Product("SKU-A", "Дешёвый", new BigDecimal("100")));
            this.repository.save(new Product("SKU-B", "Дорогой", new BigDecimal("100000")));
            MatcherAssert.assertThat(
                "derived query cannot find the saved product",
                this.repository.findBySku("SKU-A").isPresent(),
                Matchers.equalTo(true)
            );
            MatcherAssert.assertThat(
                "cannot verify that derived queries work",
                this.repository.findCheaperThan(new BigDecimal("1000")).stream().map(Product::getSku).toList(),
                Matchers.contains("SKU-A")
            );
        }

        @Test
        @DisplayName("Каждый тест среза откатывается — данные не протекают между тестами")
        void eachTestIsRolledBack() {
            MatcherAssert.assertThat(
                "slice test cannot roll back its data",
                this.repository.count(),
                Matchers.equalTo(0L)
            );
        }
    }

    @Nested
    @WebMvcTest(ProductController.class)
    @DisplayName("@WebMvcTest — только web-слой")
    class WebSlice {

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
        @DisplayName("Поднят только web-слой: репозитория и JPA в контексте нет")
        void onlyWebLayerIsLoaded() {
            MatcherAssert.assertThat(
                "cannot verify that only web layer is loaded",
                this.context.containsBean("productController"),
                Matchers.equalTo(true)
            );
            MatcherAssert.assertThat(
                "web slice cannot leave the repositories out",
                this.context.getBeanNamesForType(ProductRepository.class).length,
                Matchers.equalTo(0)
            );
            MatcherAssert.assertThat(
                "cannot verify that only web layer is loaded",
                this.context.containsBean("entityManagerFactory"),
                Matchers.equalTo(false)
            );
        }

        @Test
        @DisplayName("GET возвращает список из мока")
        void getReturnsList() throws Exception {
            BDDMockito.given(this.catalog.available()).willReturn(
                    List.of(new Product("SKU-1", "Кофемолка", new BigDecimal("4990.00"))));
            this.mockMvc.perform(MockMvcRequestBuilders.get("/api/products"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].sku").value("SKU-1"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("Кофемолка"));
        }

        @Test
        @DisplayName("Отсутствующий товар превращается в 404 обработчиком исключений")
        void missingProductBecomes404() throws Exception {
            BDDMockito.willThrow(new CatalogService.ProductNotFoundException("SKU-X"))
                    .given(this.catalog).bySku("SKU-X");
            this.mockMvc.perform(MockMvcRequestBuilders.get("/api/products/SKU-X"))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(MockMvcResultMatchers.content().string("Товар не найден: SKU-X"));
        }

        @Test
        @DisplayName("POST отдаёт 201 и тело созданного объекта")
        void postReturns201() throws Exception {
            BDDMockito.given(this.catalog.add(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
                    .willReturn(new Product("SKU-2", "Чайник", new BigDecimal("2990.00")));
            final var request = new ProductController.CreateRequest(
                    "SKU-2", "Чайник", new BigDecimal("2990.00"));
            this.mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(this.objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.sku").value("SKU-2"));
        }

        @Test
        @DisplayName("Дубликат превращается в 400")
        void duplicateBecomes400() throws Exception {
            BDDMockito.given(this.catalog.add(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
                    .willThrow(new IllegalArgumentException("Товар с артикулом SKU-2 уже есть"));
            this.mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"sku\":\"SKU-2\",\"name\":\"Чайник\",\"price\":1}"))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }
    }

    @Nested
    @JsonTest
    @DisplayName("@JsonTest — только сериализация")
    class JsonSlice {

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
        @DisplayName("Поднят только Jackson: ни web, ни данных")
        void onlyJacksonIsLoaded() {
            MatcherAssert.assertThat(
                "cannot verify that only jackson is loaded",
                this.context.containsBean("productController"),
                Matchers.equalTo(false)
            );
            MatcherAssert.assertThat(
                "json slice cannot leave the repositories out",
                this.context.getBeanNamesForType(ProductRepository.class).length,
                Matchers.equalTo(0)
            );
            MatcherAssert.assertThat(
                "cannot verify that only jackson is loaded",
                this.context.getBean(ObjectMapper.class),
                Matchers.notNullValue()
            );
        }

        @Test
        @DisplayName("Сериализация даёт ожидаемый JSON")
        void serialises() throws Exception {
            final var view = new ProductController.ProductView(
                    "SKU-1", "Кофемолка", new BigDecimal("4990.00"), true);
            MatcherAssert.assertThat(
                "serialisation cannot produce the expected JSON",
                this.json.write(view).getJson(),
                Matchers.containsString("Кофемолка")
            );
        }

        @Test
        @DisplayName("Десериализация восстанавливает объект")
        void deserialises() throws Exception {
            final var parsed = this.json.parseObject(
                    "{\"sku\":\"SKU-1\",\"name\":\"Кофемолка\",\"price\":4990.00,\"available\":true}");
            MatcherAssert.assertThat(
                "cannot verify that deserialises",
                parsed.sku(),
                Matchers.equalTo("SKU-1")
            );
            MatcherAssert.assertThat(
                "cannot verify that deserialises",
                parsed.price(),
                Matchers.comparesEqualTo(new java.math.BigDecimal("4990.00"))
            );
        }
    }

    @Nested
    @DisplayName("Без Spring вовсе — самый быстрый тест")
    class PlainUnitTest {

        @Test
        @DisplayName("Сервис тестируется обычным new, если зависимости внедрены конструктором")
        void serviceIsTestableWithoutSpring() {
            final ProductRepository fakeRepository = org.mockito.Mockito.mock(ProductRepository.class);
            BDDMockito.given(fakeRepository.findBySku("SKU-1"))
                    .willReturn(java.util.Optional.of(
                            new Product("SKU-1", "Кофемолка", new BigDecimal("4990.00"))));
            final CatalogService service = new CatalogService(fakeRepository, "EUR");
            MatcherAssert.assertThat(
                "cannot verify that service is testable without spring",
                service.priceTag(service.bySku("SKU-1")),
                Matchers.equalTo("4990.00 EUR")
            );
        }

        @Test
        @DisplayName("Контроллер тоже: MockMvc умеет работать standalone")
        void controllerIsTestableStandalone() throws Exception {
            final CatalogService catalog = org.mockito.Mockito.mock(CatalogService.class);
            BDDMockito.given(catalog.available()).willReturn(List.of());
            final MockMvc standalone = MockMvcBuilders
                    .standaloneSetup(new ProductController(catalog))
                    .build();
            standalone.perform(MockMvcRequestBuilders.get("/api/products"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().json("[]"));
        }
    }

    /**
     * Общий предок, чтобы не тянуть WebApplicationContext в каждый тест.
     */
    interface WebContextAware {
        WebApplicationContext context();
    }
}
