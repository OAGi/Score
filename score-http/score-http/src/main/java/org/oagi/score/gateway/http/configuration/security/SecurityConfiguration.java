package org.oagi.score.gateway.http.configuration.security;

import org.oagi.score.gateway.http.configuration.oauth2.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.session.security.web.authentication.SpringSessionRememberMeServices;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private AppUserDetailsService userDetailsService;

    @Autowired
    private ScoreOAuth2AuthorizationRequestResolver authorizationRequestResolver;

    @Autowired
    private ScoreOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    @Autowired
    private ScoreOAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    @Autowired
    private ScoreAuthenticationSuccessHandler authenticationSuccessHandler;

    @Autowired
    private ScoreClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder;
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        return new HttpSessionCsrfTokenRepository();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.applyPermitDefaultValues();
        corsConfiguration.setAllowedMethods(
                Arrays.asList(
                        HttpMethod.GET.name(), HttpMethod.HEAD.name(),
                        HttpMethod.POST.name(), HttpMethod.PUT.name(),
                        HttpMethod.DELETE.name())
        );
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedOriginPatterns(Arrays.asList("*"));
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    RememberMeServices rememberMeServices() {
        SpringSessionRememberMeServices rememberMeServices = new SpringSessionRememberMeServices();
        // optionally customize
        rememberMeServices.setAlwaysRemember(true);
        return rememberMeServices;
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/favicon.ico", "/resources/**", "/error", "/messages/**");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
    }

    @Bean
    public OidcIdTokenDecoderFactory oidcIdTokenDecoderFactory() {
        return new OidcIdTokenDecoderFactory();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .headers()
                .defaultsDisabled()
                .contentTypeOptions()
                .and()
                .frameOptions()
                .deny()
                .and()
                .authorizeRequests()
                .antMatchers("/info/**").permitAll()
                .antMatchers("/messages/**").permitAll()
                .antMatchers("/oauth2/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint((request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                .and()
                .formLogin()
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(authenticationSuccessHandler)
                .failureHandler(new SimpleUrlAuthenticationFailureHandler() {
                    @Override
                    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
                        super.onAuthenticationFailure(request, response, exception);
                    }
                })
                .and()
                .logout()
                .logoutUrl("/logout")
                /**
                 * RP-Initiated Logout is implemented in
                 * {@code org.oagi.score.gateway.http.api.account_management.controller.AccountController#oauth2Logout}
                 * using {@code OidcClientInitiatedLogoutSuccessHandler}
                 */
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .and()
                .httpBasic().disable()
                .cors()
                .configurationSource(corsConfigurationSource())
                .and()
                //.csrf().csrfTokenRepository(csrfTokenRepository())
                //.and()
                .csrf().disable()
                .rememberMe().rememberMeServices(rememberMeServices())
                .and()
                .apply(new ScoreOAuth2LoginConfigurer<>())
                .successHandler(authenticationSuccessHandler)
                .loginProcessingUrl("/oauth2/code/*")
                .authorizedClientService(oAuth2AuthorizedClientService)
                .authorizationEndpoint()
                .authorizationRequestRepository(authorizationRequestRepository)
                .authorizationRequestResolver(authorizationRequestResolver)
                .and().loginPage("/login");
    }
}
