package com.crio.rentvideo.config;

import com.crio.rentvideo.security.CustomAccessDeniedHandler;
import com.crio.rentvideo.security.CustomAuthenticationEntryPoint;
import com.crio.rentvideo.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Autowired
    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          CustomAuthenticationEntryPoint authenticationEntryPoint,
                          CustomAccessDeniedHandler accessDeniedHandler) {
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
                .and()
                .authorizeRequests()
                // Public endpoints
                .antMatchers("/api/auth/**", "/api/register", "/api/login", "/h2-console/**").permitAll()
                // Video read endpoints - accessible by authenticated users (CUSTOMER and ADMIN)
                .antMatchers(HttpMethod.GET, "/api/videos", "/api/videos/**").hasAnyRole("CUSTOMER", "ADMIN")
                // Video write endpoints - restricted to ADMIN only
                .antMatchers(HttpMethod.POST, "/api/videos/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/api/videos/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/videos/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PATCH, "/api/videos/**").hasRole("ADMIN")
                // Any other request must be authenticated
                .anyRequest().authenticated()
                .and()
                .httpBasic();

        // Allow H2 console frame options
        http.headers().frameOptions().disable();

        http.authenticationProvider(authenticationProvider());

        return http.build();
    }
}
