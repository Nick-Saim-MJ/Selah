package com.selahfinance.config;

import com.selahfinance.identidad.application.port.in.ConsultarUsuarioUseCase;
import com.selahfinance.identidad.infrastructure.security.SesionVigenteFilter;
import com.selahfinance.integraciones.application.port.in.AutenticarClienteApiUseCase;
import com.selahfinance.integraciones.infrastructure.security.ApiKeyAuthenticationFilter;
import com.selahfinance.shared.infrastructure.security.SeguridadProperties;
import com.selahfinance.shared.infrastructure.web.UsuarioActual;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Dos cadenas de seguridad:
 * <ol>
 *   <li>{@code /api/v1/integraciones/**}: sistemas de otros equipos, header {@code X-API-Key}.</li>
 *   <li>Resto de {@code /api/v1/**}: app móvil, JWT Bearer + rol.</li>
 * </ol>
 *
 * <p>Roles (claim {@code rol} del JWT → authority {@code ROLE_<rol>}):
 * <ul>
 *   <li>ADMIN: {@code /api/v1/admin/**}. No accede a finanzas ni a reportes de nadie.</li>
 *   <li>PASTOR: {@code /api/v1/pastor/**} (totales anónimos + reportes compartidos) y sus propias finanzas.</li>
 *   <li>HERMANO: sus propias finanzas, hábitos y reportes.</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

    private static final String[] RUTAS_PUBLICAS = {
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
            "/actuator/health/**", "/actuator/info"
    };

    @Bean
    @Order(1)
    SecurityFilterChain integracionesFilterChain(HttpSecurity http, AutenticarClienteApiUseCase autenticarClienteApi)
            throws Exception {
        http.securityMatcher("/api/v1/integraciones/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new ApiKeyAuthenticationFilter(autenticarClienteApi), AuthorizationFilter.class)
                .authorizeHttpRequests(a -> a.anyRequest().authenticated());
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain appFilterChain(HttpSecurity http, ConsultarUsuarioUseCase usuarios) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        // Orden importa: gana la primera regla que coincide.
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/cambiar-password").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/registro", "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/iglesias").permitAll()
                        .requestMatchers(RUTAS_PUBLICAS).permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/pastor/**").hasRole("PASTOR")
                        .requestMatchers("/api/v1/perfil/**").authenticated()
                        .anyRequest().hasAnyRole("HERMANO", "PASTOR"))
                .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .addFilterAfter(new SesionVigenteFilter(usuarios), BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    /** El rol viaja en el claim {@code rol} y se convierte en ROLE_&lt;rol&gt;. */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String rol = jwt.getClaimAsString(UsuarioActual.CLAIM_ROL);
            return rol == null ? List.of() : List.of(new SimpleGrantedAuthority("ROLE_" + rol));
        });
        return converter;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(SeguridadProperties props) {
        var config = new CorsConfiguration();
        config.setAllowedOriginPatterns(props.corsOrigenes());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-API-Key", "Idempotency-Key"));
        config.setExposedHeaders(List.of("Location"));
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
