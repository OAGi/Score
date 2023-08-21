package org.oagi.score.gateway.http.configuration.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.oagi.score.gateway.http.configuration.oauth2.ScoreClientRegistrationRepository;
import org.oagi.score.gateway.http.configuration.oauth2.ScoreOAuth2AuthorizedClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.session.security.web.authentication.SpringSessionRememberMeServices;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;

@Configuration
public class SecurityConfiguration {

    @Autowired
    private AppUserDetailsService userDetailsService;

    @Autowired
    private ScoreOAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    @Autowired
    private ScoreAuthenticationSuccessHandler authenticationSuccessHandler;

    @Autowired
    private ScoreClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public OAuth2AuthorizationRequestResolver oAuth2AuthorizationRequestResolver() {
        OAuth2AuthorizationRequestResolver oAuth2AuthorizationRequestResolver =
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
        return oAuth2AuthorizationRequestResolver;
    }

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository =
                new HttpSessionOAuth2AuthorizationRequestRepository();
        return authorizationRequestRepository;
    }

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

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/favicon.ico", "/resources/**", "/error", "/messages/**");
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public OidcIdTokenDecoderFactory oidcIdTokenDecoderFactory() {
        return new OidcIdTokenDecoderFactory();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .headers(headersConfigurer -> {
                    headersConfigurer.defaultsDisabled()
                            .contentTypeOptions(contentTypeOptionsCustomizer -> {
                            })
                            .frameOptions(frameOptionsConfig -> {
                                frameOptionsConfig.deny();
                            });
                })
                .authorizeHttpRequests(requestMatcherRegistry -> {
                    requestMatcherRegistry
                            .requestMatchers("/info/**", "/messages/**", "/oauth2/**").permitAll()
                            .anyRequest().authenticated();
                })
                .exceptionHandling(exceptionHandlingConfigurer -> {
                    exceptionHandlingConfigurer.authenticationEntryPoint((request, response, authException) ->
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED));
                })
                .formLogin(formLoginConfigurer -> {
                    formLoginConfigurer
                            .loginProcessingUrl("/login")
                            .usernameParameter("username")
                            .passwordParameter("password")
                            .successHandler(authenticationSuccessHandler)
                            .failureHandler(new SimpleUrlAuthenticationFailureHandler() {
                                @Override
                                public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
                                    super.onAuthenticationFailure(request, response, exception);
                                }
                            });
                })
                .logout(logoutConfigurer -> {
                    logoutConfigurer
                            .logoutUrl("/logout")
                            .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
                            .invalidateHttpSession(true)
                            .clearAuthentication(true);
                })
                .httpBasic(httpBasicConfigurer -> {
                    httpBasicConfigurer.disable();
                })
                .cors(corsConfigurer -> {
                    corsConfigurer.configurationSource(corsConfigurationSource());
                })
                .csrf(csrfConfigurer -> {
                    csrfConfigurer.disable();
                })
                .rememberMe(rememberMeConfigurer -> {
                    rememberMeConfigurer.rememberMeServices(rememberMeServices());
                })
                .oauth2Login(oAuth2LoginConfigurer -> {
                    oAuth2LoginConfigurer
                            .loginPage("/login")
                            .loginProcessingUrl("/oauth2/code/*")
                            .successHandler(authenticationSuccessHandler)
                            .authorizedClientService(oAuth2AuthorizedClientService)
                            .authorizationEndpoint(authorizationEndpointConfig -> {
                                authorizationEndpointConfig
                                        .authorizationRequestRepository(authorizationRequestRepository())
                                        .authorizationRequestResolver(oAuth2AuthorizationRequestResolver());
                            });
                });
        return http.build();
    }
}
