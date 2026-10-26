package lfpimenta.microservicos.p2.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(basic -> basic.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .formLogin(config -> config.disable())
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/trocarsenha/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/").hasRole("MANUTENCAO")
                        .requestMatchers(HttpMethod.GET, "/").hasRole("MANUTENCAO")
                        .requestMatchers(HttpMethod.PUT, "/unlock/*").hasRole("ADMIN")
                        .requestMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                                "/h2-console/**", "/error")
                        .permitAll()
                        .anyRequest().denyAll());

        return http.build();
    }

    @Bean
    public JdbcUserDetailsManager userDetailsService(DataSource datasource) {
        return new JdbcUserDetailsManager(datasource);
    }

}
