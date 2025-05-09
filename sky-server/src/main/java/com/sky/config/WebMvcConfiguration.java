package com.sky.config;

import com.sky.interceptor.JwtTokenAdminInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    /**
     * 注册自定义拦截器
     *
     * @param registry 拦截器注册表，用于添加拦截器
     */
    protected void addInterceptors(InterceptorRegistry registry) {
    // 记录日志，表示开始注册自定义拦截器
        log.info("开始注册自定义拦截器...");
    // 向拦截器注册表中添加jwtTokenAdminInterceptor拦截器
    // 由于JWT认证已由Spring Security的JwtAuthenticationTokenFilter处理，
    // 此处不再需要注册JwtTokenAdminInterceptor进行认证。
    // 如果JwtTokenAdminInterceptor还承担其他非认证职责（例如设置BaseContext，但这已移至新过滤器），
    // 则需要评估是否仍需以其他方式注册或重构。
    /*
        registry.addInterceptor(jwtTokenAdminInterceptor)
            // 设置拦截器拦截的路径模式，这里表示拦截所有以/admin/开头的请求
                .addPathPatterns("/admin/**")
            // 设置拦截器排除的路径模式，这里表示不拦截/admin/employee/login路径的请求
                .excludePathPatterns("/admin/employee/login");
    */
    }

    /**
     * 通过knife4j生成接口文档
     * @return
     */
    @Bean
    public Docket docket() {
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("苍穹外卖项目接口文档")
                .version("2.0")
                .description("苍穹外卖项目接口文档")
                .build();
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.sky.controller"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }

    /**
     * 设置静态资源映射
     * @param registry
     */
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
