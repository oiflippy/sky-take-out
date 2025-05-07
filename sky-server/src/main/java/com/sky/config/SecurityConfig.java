package com.sky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder构造函数可以接受一个可选的“强度”参数 (log rounds)
        // 范围是 4 到 31，默认是 10。数值越大，哈希计算越慢，密码越难破解。
        // 对于大多数应用，默认值10或选择12通常是合适的起点。
        return new BCryptPasswordEncoder(); // 使用默认强度
        // return new BCryptPasswordEncoder(12); // 或者指定强度为12
    }
}
