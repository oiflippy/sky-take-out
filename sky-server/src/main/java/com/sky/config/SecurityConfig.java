package com.sky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity // 明确启用Web安全功能
public class SecurityConfig {

    // 您项目中已有的 PasswordEncoder Bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//    // 配置HttpSecurity对象，用于定义安全策略
//        http
//        // 禁用CSRF保护，因为使用Token认证时不需要CSRF保护
//                .csrf(csrf -> csrf.disable())
//        // 配置请求授权规则
//                .authorizeHttpRequests(authorize -> authorize
//                        // 使用 antMatchers 替换 requestMatchers
//                        .antMatchers(
//                                "/doc.html",
//                                "/webjars/**",
//                                "/swagger-resources/**",
//                                "/v2/api-docs",
//                                "/v3/api-docs/**",
//                                "/favicon.ico",
//                                "/admin/employee/login"
//                        ).permitAll()
//                        // 使用 antMatchers 替换 requestMatchers
//                        .antMatchers("/admin/**").authenticated()
//                        .anyRequest().authenticated()
//                )
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .httpBasic(Customizer.withDefaults());
//
//        return http.build();
//    }
}