package nhom05.daonguyenduykhoa_2280601493.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import lombok.RequiredArgsConstructor;
import nhom05.daonguyenduykhoa_2280601493.service.UserService;
import nhom05.daonguyenduykhoa_2280601493.service.OauthService;

@Configuration
@EnableWebSecurity
// @EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final OauthService oAuthService;
    private final UserService userService;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .authorizeHttpRequests(auth -> auth
            // Public resources - cho phép anonymous xem sách
            .requestMatchers("/css/**", "/js/**", "/images/**", "/", "/index", 
                           "/books", "/books/search", "/oauth/**", "/register", "/error", "/login")
            .permitAll()
            
            // API public - không cần xác thực
            .requestMatchers("/api/**")
            .permitAll()
            
            // ADMIN routes
            .requestMatchers("/admin/**", "/books/edit/**", "/books/add", "/books/delete/**", 
                           "/books/import-excel", "/books/download-template", "/categories/**")
            .hasAuthority("ADMIN")
            
            // Chỉ USER và ADMIN mới thêm giỏ hàng được
            .requestMatchers("/books/add-to-cart", "/cart", "/cart/**")
            .hasAnyAuthority("ADMIN", "USER")
            
            .anyRequest()
            .authenticated()
        )
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login")
            .deleteCookies("JSESSIONID")
            .invalidateHttpSession(true)
            .clearAuthentication(true)
            .permitAll()
        )
        .formLogin(formLogin -> formLogin
            .loginPage("/login")
            .loginProcessingUrl("/login")
            .defaultSuccessUrl("/")
            .failureUrl("/login?error")
            .permitAll()
        )
        .oauth2Login(oauth2Login -> oauth2Login
            .loginPage("/login")
            .failureUrl("/login?error")
            .userInfoEndpoint(userInfoEndpoint -> 
                userInfoEndpoint.userService(oAuthService))
            .successHandler((request, response, authentication) -> {
                var oidcUser = (DefaultOidcUser) authentication.getPrincipal();
                String fullName = oidcUser.getFullName() != null ? oidcUser.getFullName() : oidcUser.getEmail();
                userService.saveOauthUser(oidcUser.getEmail(), fullName);
                response.sendRedirect("/");
            })
            .permitAll()
        )
        .rememberMe(rememberMe -> rememberMe
            .key("hutech")
            .rememberMeCookieName("hutech")
            .tokenValiditySeconds(24 * 60 * 60)
            .userDetailsService(userService)
        )
        .exceptionHandling(exceptionHandling -> exceptionHandling
            .accessDeniedPage("/403")
        )
        .sessionManagement(sessionManagement -> sessionManagement
            .maximumSessions(1)
            .expiredUrl("/login")
        )
        .httpBasic(httpBasic -> httpBasic
            .realmName("hutech")
        )
        .build();
}
}