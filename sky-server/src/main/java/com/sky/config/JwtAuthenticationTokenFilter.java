package com.sky.config; // 或者 com.sky.security.filter

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component // 声明为Spring组件，以便能够注入到SecurityConfig中
@Slf4j
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtProperties jwtProperties;

    // 如果你需要根据empId加载更详细的用户信息或权限，可以注入UserDetailsService
    // @Autowired
    // private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. 从请求头获取令牌
        // 注意：这里我们假设管理端和用户端使用不同的token name和secret key
        // 这个过滤器可能需要区分是处理管理端请求还是用户端请求，
        // 或者创建两个独立的过滤器。为简化，我们先假设这是管理端的。
        String token = request.getHeader(jwtProperties.getAdminTokenName());
        log.debug("Attempting to process token from header '{}': '{}'", jwtProperties.getAdminTokenName(), token);

        if (StringUtils.hasText(token)) {
            try {
                // 2. 校验令牌
                Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
                Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
                log.debug("JWT validated successfully for employee ID: {}", empId);

                // 3. 设置BaseContext (如果需要，这是从旧拦截器迁移过来的逻辑)
                BaseContext.setCurrentId(empId);

                // 4. 构建Authentication对象
                // TODO: 实际项目中，权限列表应该从数据库或claims中动态获取
                // 例如，如果JWT的claims中包含了角色信息，可以从那里解析
                List<GrantedAuthority> authorities = new ArrayList<>();
                // 假设所有管理员都有 "ROLE_ADMIN" 权限，或者更细粒度的权限
                // authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                // authorities.add(new SimpleGrantedAuthority("permission:employee:read"));

                // 如果你的Employee实体实现了UserDetails，或者你有UserDetailsService来加载UserDetails
                // UserDetails userDetails = this.userDetailsService.loadUserByUsername(empId.toString());
                // UsernamePasswordAuthenticationToken authentication =
                //         new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // 简化版：直接使用empId作为principal，不加载UserDetails
                // 注意：这里的principal可以是empId，也可以是Employee对象（如果加载了）
                // credentials通常为null，因为JWT本身就是凭证
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(empId, null, authorities);

                // 5. 将Authentication对象设置到SecurityContextHolder
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Set Authentication to SecurityContextHolder for employee ID: {}", empId);

            } catch (Exception e) {
                log.error("JWT token validation error: {}", e.getMessage());
                // 如果令牌无效，确保清除上下文
                SecurityContextHolder.clearContext();
            }
        } else {
            log.debug("No JWT token found in request headers.");
            // 即使没有token，也可能需要清除上下文，以防之前的请求留下了状态（虽然不太可能在STATELESS模式下）
            SecurityContextHolder.clearContext();
        }

        // 继续执行过滤器链中的下一个过滤器
        filterChain.doFilter(request, response);
    }
}