/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m22.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Слайды 212–219: цепочка фильтров, пользователи и правила доступа.
 *
 * <p>{@code SecurityFilterChain} как бин — весь способ настройки, начиная
 * со Spring Security 6: {@code WebSecurityConfigurerAdapter}, который
 * наследовали раньше, удалён. Разница не косметическая — вместо переопределения
 * методов родителя конфигурация стала обычным объектом, который контейнер
 * собирает по правилам модулей 11–14.</p>
 *
 * <p>Правила читаются сверху вниз, и <b>первое совпавшее</b> решает исход:
 * порядок здесь значим ровно так же, как в файле маршрутов. Замыкающее
 * {@code anyRequest().authenticated()} — не перестраховка, а условие того,
 * что забытый эндпоинт окажется закрытым, а не открытым.</p>
 *
 * @since 1.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public SecurityConfig() {
        // нечего инициализировать
    }

    /**
     * Слайды 212–213: цепочка фильтров, стоящая перед DispatcherServlet.
     * @param http Настройки защиты
     * @return Цепочка фильтров, стоящая перед DispatcherServlet
     * @throws Exception Если цепочку не удалось собрать
     */
    @Bean
    public SecurityFilterChain chain(final HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(
                requests -> requests
                    .requestMatchers("/api/public/**").permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            )
            .httpBasic(
                basic -> {
                }
            )
            .csrf(csrf -> csrf.disable())
            .build();
    }

    /**
     * Слайд 215: откуда берётся пользователь.
     *
     * <p>Хранилище в памяти — это тот же контракт, что у хранилища в базе
     * или в LDAP: по имени вернуть пароль и роли либо сказать, что такого нет.
     * Всё остальное Spring Security делает одинаково, независимо от источника.</p>
     *
     * @param encoder Шифратор паролей
     * @return Пользователи приложения
     */
    @Bean
    public UserDetailsService users(final PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
            User.withUsername("anna").password(encoder.encode("anna-pass"))
                .roles("USER").build(),
            User.withUsername("boris").password(encoder.encode("boris-pass"))
                .roles("USER", "ADMIN").build()
        );
    }

    /**
     * Слайд 216: пароль не хранится открытым.
     *
     * <p>BCrypt — не шифрование, а односторонняя функция с солью внутри:
     * восстановить пароль из хеша нельзя, а проверить — можно. Поэтому утечка
     * хранилища не равна утечке паролей, и поэтому же два одинаковых пароля
     * дают разные хеши.</p>
     *
     * @return Шифратор паролей
     */
    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
}
