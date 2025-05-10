package com.sky.config;

import com.sky.json.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.List;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
// 注意：如果你的项目使用 Spring Boot 2.x 及以上，并且没有特殊需求（如完全自定义MVC配置），
// 通常更推荐实现 WebMvcConfigurer 接口而不是继承 WebMvcConfigurationSupport。
// 继承 WebMvcConfigurationSupport 会禁用 Spring Boot 的默认 MVC 自动配置。
// 但根据你提供的代码，是继承了 WebMvcConfigurationSupport，所以我们基于这个来修改。
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

//    @Autowired
//    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    /**
     * 注册自定义拦截器
     *
     * @param registry 拦截器注册表，用于添加拦截器
     */
//    @Override // 添加 @Override 注解是良好的实践
//    protected void addInterceptors(InterceptorRegistry registry) {
//        // 记录日志，表示开始注册自定义拦截器
//        log.info("开始注册自定义拦截器...");
//        // 向拦截器注册表中添加jwtTokenAdminInterceptor拦截器
//        // 由于JWT认证已由Spring Security的JwtAuthenticationTokenFilter处理，
//        // 此处不再需要注册JwtTokenAdminInterceptor进行认证。
//        // 如果JwtTokenAdminInterceptor还承担其他非认证职责（例如设置BaseContext，但这已移至新过滤器），
//        // 则需要评估是否仍需以其他方式注册或重构。
//    /*
//        registry.addInterceptor(jwtTokenAdminInterceptor)
//            // 设置拦截器拦截的路径模式，这里表示拦截所有以/admin/开头的请求
//                .addPathPatterns("/admin/**")
//            // 设置拦截器排除的路径模式，这里表示不拦截/admin/employee/login路径的请求
//                .excludePathPatterns("/admin/employee/login");
//    */
//    }

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
    @Override // 添加 @Override 注解是良好的实践
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * 扩展Spring MVC框架的消息转换器
     * @param converters 消息转换器列表
     */
    @Override // 添加 @Override 注解是良好的实践
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");
        // 创建一个消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        // 为消息转换器设置一个对象转换器，对象转换器可以将Java对象序列化为json数据
        messageConverter.setObjectMapper(new JacksonObjectMapper()); // 使用你自定义的ObjectMapper
        // 将自己的消息转换器添加到容器中
        // 0表示将自定义的转换器放到最前面，优先使用
        converters.add(0, messageConverter);
    }
}