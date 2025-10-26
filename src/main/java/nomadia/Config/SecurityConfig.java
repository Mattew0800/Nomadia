package nomadia.Config;

import org.springframework.beans.factory.annotation.Value;

import nomadia.Service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final String jwtSecret;

    public SecurityConfig(@Value("${jwt.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(UserService userService) {
        return new JwtAuthFilter(userService, jwtSecret);
    }
    //Jerarquia
    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
        return hierarchy;
    }

    // Mantener este filtrado como está
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Endpoints auth
                        .requestMatchers("/nomadia/auth/logout").hasRole("USER")
                        .requestMatchers("/nomadia/auth/register").permitAll()
                        .requestMatchers("/nomadia/auth/login").permitAll()

                        // Endpoints usuario
                        .requestMatchers("/nomadia/user/me").hasRole("USER")
                        .requestMatchers("/nomadia/user/me/update").hasRole("USER")

                        // Endpoints admin
                        .requestMatchers("/nomadia/user/create").hasRole("ADMIN")
                        .requestMatchers("/nomadia/user/get-all").hasRole("ADMIN")
                        .requestMatchers("/nomadia/user/get-user/{id}").hasRole("ADMIN")
                        .requestMatchers("/nomadia/user/delete-user/{id}").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

