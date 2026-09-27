/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Приложение на настоящем Tomcat.
 *
 * <p>{@code MockMvc} не отправляет ответ по сети и принимает заголовки
 * в любой момент, поэтому часть ошибок видна только здесь. Интерсептор
 * {@link Late} пишет заголовок в {@code postHandle}: для обычного
 * ответа контейнер его выбрасывает, а под {@code ShallowEtagHeaderFilter},
 * который придерживает тело до конца цепочки, заголовок успевает.</p>
 *
 * @since 1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(HandlersAppTest.Late.class)
@DisplayName("Приложение на настоящем Tomcat")
final class HandlersAppTest {

    @Test
    @DisplayName("заголовок из совета над ответом доходит до клиента")
    void deliversAdviceHeader(@LocalServerPort final int port) throws Exception {
        MatcherAssert.assertThat(
            "заголовок длительности потерян по дороге к клиенту",
            HandlersAppTest.send(port, HttpRequest.newBuilder().GET(), "/api/users/me")
                .headers().firstValue("X-Elapsed-Nanos").orElse(""),
            Matchers.matchesRegex("\\d+")
        );
    }

    @Test
    @DisplayName("заголовок из postHandle до клиента не доходит: ответ уже отправлен")
    void losesPostHandleHeader(@LocalServerPort final int port) throws Exception {
        MatcherAssert.assertThat(
            "заголовок, выставленный после отправки ответа, всё же дошёл",
            HandlersAppTest.send(port, HttpRequest.newBuilder().GET(), "/api/users/me")
                .headers().firstValue("X-Late").isPresent(),
            Matchers.is(false)
        );
    }

    @Test
    @DisplayName("под буферизующим фильтром заголовок из postHandle успевает к клиенту")
    void keepsPostHandleHeaderUnderEtag(@LocalServerPort final int port) throws Exception {
        MatcherAssert.assertThat(
            "ответ, придержанный фильтром ETag, всё равно потерял заголовок",
            HandlersAppTest.send(port, HttpRequest.newBuilder().GET(), "/api/greeting")
                .headers().firstValue("X-Late").orElse(""),
            Matchers.is("postHandle")
        );
    }

    @Test
    @DisplayName("слишком длинное тело отвергается кодом 413 ещё в фильтре")
    void rejectsLongBody(@LocalServerPort final int port) throws Exception {
        MatcherAssert.assertThat(
            "длинное тело дошло до контроллера",
            HandlersAppTest.send(
                port,
                HttpRequest.newBuilder()
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("x".repeat(2048))),
                "/api/users"
            ).statusCode(),
            Matchers.is(413)
        );
    }

    @Test
    @DisplayName("отказ фильтра приходит в формате RFC 9457, как и прочие ошибки")
    void answersLongBodyWithProblem(@LocalServerPort final int port) throws Exception {
        MatcherAssert.assertThat(
            "отказ фильтра пришёл не в формате ProblemDetail",
            HandlersAppTest.send(
                port,
                HttpRequest.newBuilder()
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("x".repeat(2048))),
                "/api/users"
            ).headers().firstValue("Content-Type").orElse(""),
            Matchers.startsWith("application/problem+json")
        );
    }

    private static HttpResponse<String> send(
        final int port, final HttpRequest.Builder builder, final String path
    ) throws Exception {
        try (
            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build()
        ) {
            return client.send(
                builder.uri(URI.create(String.format("http://localhost:%d%s", port, path)))
                    .timeout(Duration.ofSeconds(5))
                    .build(),
                HttpResponse.BodyHandlers.ofString()
            );
        }
    }

    /**
     * Интерсептор, который пишет заголовок слишком поздно.
     * @since 1.0
     */
    @TestConfiguration(proxyBeanMethods = false)
    static final class Late implements WebMvcConfigurer {

        @Override
        public void addInterceptors(final InterceptorRegistry registry) {
            registry.addInterceptor(
                new HandlerInterceptor() {
                    @Override
                    public void postHandle(
                        final HttpServletRequest request, final HttpServletResponse response,
                        final Object handler, final ModelAndView model
                    ) {
                        response.setHeader("X-Late", "postHandle");
                    }
                }
            );
        }
    }
}
