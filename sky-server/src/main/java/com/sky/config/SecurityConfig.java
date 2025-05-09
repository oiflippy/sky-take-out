package com.sky.config;

import com.sky.interceptor.JwtAuthenticationTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // 明确启用Web安全功能
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter; // 注入自定义的JWT过滤器

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

                // 配置会话管理为无状态（不创建和使用HTTP Session）
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

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
                                "/admin/employee/login",    // 管理员登录接口
                                "/user/user/login"          // 假设的用户登录接口 (如果存在)
                                // 添加其他需要白名单的公共API端点
                        ).permitAll()  // 允许匿名访问

                        // 对于 /admin/** 路径 (除了已 permitAll 的登录路径)
                        // 以及其他需要保护的路径，要求用户已认证
                        // 这个 .authenticated() 现在会依赖 JwtAuthenticationTokenFilter 是否成功设置了 SecurityContext
                        .antMatchers("/admin/**", "/user/**").authenticated() // 根据实际情况调整路径

                        // 其他所有未明确配置的请求，也要求认证 (这是一个兜底规则)
                        .anyRequest().authenticated()
                )
                // 移除HTTP基本认证，因为我们将依赖自定义的JWT过滤器
                // .httpBasic(Customizer.withDefaults()); // 注释或删除此行

                // 将自定义的JWT认证过滤器添加到Spring Security的过滤器链中
                // 通常放在 UsernamePasswordAuthenticationFilter 之前，因为它处理的是基于令牌的认证
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        // 构建安全过滤链
        return http.build();
}
}