package br.com.assinanet.security.config;

import br.com.assinanet.security.jwt.JwtAuthenticationEntryPoint;
import br.com.assinanet.security.jwt.JwtAuthenticationTokenFilter;
import br.com.assinanet.security.service.JwtUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 * Data: 20/08/2018 - 22:44
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtUserDetailsService userDetailsService;

    @Autowired
    public WebSecurityConfig(JwtAuthenticationEntryPoint unauthorizedHandler, JwtUserDetailsService userDetailsService) {
        this.unauthorizedHandler = unauthorizedHandler;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(this.userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public static  PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationTokenFilter authenticationTokenFilterBean() {
        return new JwtAuthenticationTokenFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(HttpMethod.POST, "/api/v1/integracao/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/integracao/status/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/validar/**").permitAll()

                // .antMatchers(HttpMethod.POST, "/api/v1/assinador/**").permitAll()
                .requestMatchers(
                        HttpMethod.GET,
                        "/",
                        "/resources/**",
                        "/static/**",
                        "/public/**",
                        "/webui/**",
                        "/*.html",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js",
                        "/**/*.png",
                        "/**/*.jpg",
                        "/**/*.gif",
                        "/**/*.svg",
                        "/**/*.ico",
                        "/**/*.ttf",
                        "/**/*.woff",
                        "/**/*.woff2",
                        "/**/*.otf",
                        "/**/*.properties"
                ).permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST,"/api/v1/lost/**").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/v1/recuperarsenha/**").permitAll()
                .requestMatchers(HttpMethod.POST,"/api/v1/validarTokenAlterSenha/**").permitAll()
                .requestMatchers("/api/v1/assinar/validarChaveAcesso/**").permitAll()
                .requestMatchers("/api/v1/gerarTokenIntegracao/**").permitAll()
                .requestMatchers(HttpMethod.POST,"/api/v1/registro/**").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/v1/registro/cliente/**").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/v1/registro/usuario/**").permitAll()

                //.antMatchers("/api/v1/usuario/findById/**").permitAll()
                .anyRequest().authenticated());
        httpSecurity.authenticationProvider(authenticationProvider());
        httpSecurity.addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);
        httpSecurity.headers(headers -> headers.cacheControl(cacheControl -> {}));
        return httpSecurity.build();
    }
}

