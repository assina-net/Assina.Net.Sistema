package br.com.assinanet.security.config;

import br.com.assinanet.security.jwt.JwtAuthenticationEntryPoint;
import br.com.assinanet.security.jwt.JwtAuthenticationTokenFilter;
import br.com.assinanet.security.service.JwtUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 * Data: 20/08/2018 - 22:44
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtUserDetailsService userDetailsService;

    @Autowired
    public WebSecurityConfig(JwtAuthenticationEntryPoint unauthorizedHandler, JwtUserDetailsService userDetailsService) {
        this.unauthorizedHandler = unauthorizedHandler;
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    public void configureAuthentication(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.userDetailsService(this.userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public static  PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationTokenFilter authenticationTokenFilterBean() {
        return new JwtAuthenticationTokenFilter();
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf().disable()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/v1/integracao/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/v1/integracao/status/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/v1/validar/**").permitAll()

                // .antMatchers(HttpMethod.POST, "/api/v1/assinador/**").permitAll()
                .antMatchers(
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
                .antMatchers("/api/v1/auth/**").permitAll()
                .antMatchers(HttpMethod.POST,"/api/v1/lost/**").permitAll()
                .antMatchers(HttpMethod.GET,"/api/v1/recuperarsenha/**").permitAll()
                .antMatchers(HttpMethod.POST,"/api/v1/validarTokenAlterSenha/**").permitAll()
                .antMatchers("/api/v1/assinar/validarChaveAcesso/**").permitAll()
                .antMatchers("/api/v1/gerarTokenIntegracao/**").permitAll()
                .antMatchers(HttpMethod.POST,"/api/v1/registro/**").permitAll()
                .antMatchers(HttpMethod.GET,"/api/v1/registro/cliente/**").permitAll()
                .antMatchers(HttpMethod.GET,"/api/v1/registro/usuario/**").permitAll()

                //.antMatchers("/api/v1/usuario/findById/**").permitAll()
                .anyRequest().authenticated();
        httpSecurity.addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);
        httpSecurity.headers().cacheControl();
    }
}

