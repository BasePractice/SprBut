/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m27.security;

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
 * Защита трекера: кто это и что ему можно.
 *
 * <p>Цепочка фильтров стоит перед {@code DispatcherServlet}, поэтому решение
 * пустить запрос принимается задолго до контроллера — в контроллерах трекера
 * нет ни одной проверки прав, и это не упущение, а следствие.</p>
 *
 * <p>Карта контейнера закрыта ролью администратора намеренно: она рассказывает
 * про внутреннее устройство приложения — имена бинов, классы за прокси,
 * цепочку фильтров, — и наружу такому знанию ходу нет.</p>
 *
 * <p>Правило на адресе защищает путь, правило на методе — операцию. Оба вида
 * встречаются в модуле: {@code hasRole} здесь и {@code @PreAuthorize}
 * на закрытии задачи, до которого ведёт не только HTTP.</p>
 *
 * @since 1.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class TrackerSecurity {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public TrackerSecurity() {
        // нечего инициализировать
    }

    /**
     * Цепочка фильтров, через которую проходит каждый запрос.
     * @param http Настройки защиты
     * @return Цепочка фильтров, через которую проходит каждый запрос
     * @throws Exception Если цепочку не удалось собрать
     */
    @Bean
    public SecurityFilterChain chain(final HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(
                requests -> requests
                    .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers("/api/introspection/**").hasRole("ADMIN")
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
     * Пользователи трекера.
     *
     * <p>Хранилище в памяти — тот же контракт, что у базы или LDAP: по имени
     * вернуть пароль и роли либо сказать, что такого нет.</p>
     *
     * @param encoder Шифратор паролей
     * @return Пользователи трекера
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
     * Шифратор паролей: в хранилище лежит хеш, а не пароль.
     * @return Шифратор паролей
     */
    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
}
