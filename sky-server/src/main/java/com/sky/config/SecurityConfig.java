package com.sky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // 明确启用Web安全功能
public class SecurityConfig {

    // 您项目中已有的 PasswordEncoder Bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    // 配置HTTP安全规则
    http
            // 禁用CSRF保护（适用于无状态API场景）
            .csrf(csrf -> csrf.disable())

            // 配置请求授权规则
            .authorizeHttpRequests(authorize -> authorize
                    // 定义白名单路径（无需认证）
                    .antMatchers(
                            "/doc.html",                // Knife4j文档页面
                            "/webjars/**",              // Webjars静态资源
                            "/swagger-resources/**",    // Swagger资源路径
                            "/v2/api-docs",             // Swagger v2 API文档
                            "/v3/api-docs/**",          // Swagger v3 API文档
                            "/favicon.ico",             // 网站图标
                            "/admin/employee/login"     // 管理员登录接口
                    ).permitAll()  // 允许匿名访问

                    // 管理员路径需要认证（/admin/开头的所有路径）
                    .antMatchers("/admin/**").authenticated()

                    // 其他所有请求都需要认证（保护所有未明确配置的路径）
                    .anyRequest().authenticated()
            )

            // 配置会话管理为无状态（不创建和使用HTTP Session）
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 启用HTTP基本认证（作为默认认证方式，实际项目可能需要替换为JWT等方案）
            .httpBasic(Customizer.withDefaults());

    // 构建安全过滤链
    return http.build();
}
}