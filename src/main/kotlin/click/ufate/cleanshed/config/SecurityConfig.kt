package click.ufate.cleanshed.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/**").permitAll()
                    .requestMatchers("/").permitAll()
                    .requestMatchers("/*.html").permitAll()
                    .requestMatchers("/*.js").permitAll()
                    .requestMatchers("/*.css").permitAll()
                    .requestMatchers("/*.json").permitAll()
                    .requestMatchers("/assets/**").permitAll()
                    .requestMatchers("/icons/**").permitAll()
                    .anyRequest().permitAll()
            }
            .headers { headers ->
                headers.frameOptions { it.sameOrigin() }
            }

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
