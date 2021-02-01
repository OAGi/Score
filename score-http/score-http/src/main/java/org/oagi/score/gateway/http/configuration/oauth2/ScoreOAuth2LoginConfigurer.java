package org.oagi.score.gateway.http.configuration.oauth2;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationProvider;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.oidc.authentication.OidcAuthorizationCodeAuthenticationProvider;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.*;
import org.springframework.security.oauth2.client.web.*;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.util.matcher.*;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.*;

/**
 * An {@link AbstractHttpConfigurer} for OAuth 2.0 Login,
 * which leverages the OAuth 2.0 Authorization Code Grant Flow.
 *
 * <p>
 * OAuth 2.0 Login provides an application with the capability to have users log in
 * by using their existing account at an OAuth 2.0 or OpenID Connect 1.0 Provider.
 *
 * <p>
 * Defaults are provided for all configuration options with the only required configuration
 * being {@link #clientRegistrationRepository(ClientRegistrationRepository)}.
 * Alternatively, a {@link ClientRegistrationRepository} {@code @Bean} may be registered instead.
 *
 * <h2>Security Filters</h2>
 * <p>
 * The following {@code Filter}'s are populated:
 *
 * <ul>
 * <li>{@link OAuth2AuthorizationRequestRedirectFilter}</li>
 * <li>{@link OAuth2LoginAuthenticationFilter}</li>
 * </ul>
 *
 * <h2>Shared Objects Created</h2>
 * <p>
 * The following shared objects are populated:
 *
 * <ul>
 * <li>{@link ClientRegistrationRepository} (required)</li>
 * <li>{@link OAuth2AuthorizedClientRepository} (optional)</li>
 * <li>{@link GrantedAuthoritiesMapper} (optional)</li>
 * </ul>
 *
 * <h2>Shared Objects Used</h2>
 * <p>
 * The following shared objects are used:
 *
 * <ul>
 * <li>{@link ClientRegistrationRepository}</li>
 * <li>{@link OAuth2AuthorizedClientRepository}</li>
 * <li>{@link GrantedAuthoritiesMapper}</li>
 * <li>{@link DefaultLoginPageGeneratingFilter} - if {@link #loginPage(String)} is not configured
 * and {@code DefaultLoginPageGeneratingFilter} is available, then a default login page will be made available</li>
 * </ul>
 *
 * @author Joe Grandja
 * @author Kazuki Shimizu
 * @see HttpSecurity#oauth2Login()
 * @see OAuth2AuthorizationRequestRedirectFilter
 * @see OAuth2LoginAuthenticationFilter
 * @see ClientRegistrationRepository
 * @see OAuth2AuthorizedClientRepository
 * @see AbstractAuthenticationFilterConfigurer
 * @since 5.0
 */
public class ScoreOAuth2LoginConfigurer<B extends HttpSecurityBuilder<B>> extends
        AbstractAuthenticationFilterConfigurer<B, ScoreOAuth2LoginConfigurer<B>, OAuth2LoginAuthenticationFilter> {

    private final AuthorizationEndpointConfig authorizationEndpointConfig = new AuthorizationEndpointConfig();
    private final TokenEndpointConfig tokenEndpointConfig = new TokenEndpointConfig();
    private final RedirectionEndpointConfig redirectionEndpointConfig = new RedirectionEndpointConfig();
    private final UserInfoEndpointConfig userInfoEndpointConfig = new UserInfoEndpointConfig();
    private String loginPage;
    private String loginProcessingUrl = OAuth2LoginAuthenticationFilter.DEFAULT_FILTER_PROCESSES_URI;

    /**
     * Sets the repository of client registrations.
     *
     * @param clientRegistrationRepository the repository of client registrations
     * @return the {@link ScoreOAuth2LoginConfigurer} for further configuration
     */
    public ScoreOAuth2LoginConfigurer<B> clientRegistrationRepository(ClientRegistrationRepository clientRegistrationRepository) {
        Assert.notNull(clientRegistrationRepository, "clientRegistrationRepository cannot be null");
        this.getBuilder().setSharedObject(ClientRegistrationRepository.class, clientRegistrationRepository);
        return this;
    }

    /**
     * Sets the repository for authorized client(s).
     *
     * @param authorizedClientRepository the authorized client repository
     * @return the {@link ScoreOAuth2LoginConfigurer} for further configuration
     * @since 5.1
     */
    public ScoreOAuth2LoginConfigurer<B> authorizedClientRepository(OAuth2AuthorizedClientRepository authorizedClientRepository) {
        Assert.notNull(authorizedClientRepository, "authorizedClientRepository cannot be null");
        this.getBuilder().setSharedObject(OAuth2AuthorizedClientRepository.class, authorizedClientRepository);
        return this;
    }

    /**
     * Sets the service for authorized client(s).
     *
     * @param authorizedClientService the authorized client service
     * @return the {@link ScoreOAuth2LoginConfigurer} for further configuration
     */
    public ScoreOAuth2LoginConfigurer<B> authorizedClientService(OAuth2AuthorizedClientService authorizedClientService) {
        Assert.notNull(authorizedClientService, "authorizedClientService cannot be null");
        this.authorizedClientRepository(new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService));
        return this;
    }

    @Override
    public ScoreOAuth2LoginConfigurer<B> loginPage(String loginPage) {
        Assert.hasText(loginPage, "loginPage cannot be empty");
        this.loginPage = loginPage;
        return this;
    }

    @Override
    public ScoreOAuth2LoginConfigurer<B> loginProcessingUrl(String loginProcessingUrl) {
        Assert.hasText(loginProcessingUrl, "loginProcessingUrl cannot be empty");
        this.loginProcessingUrl = loginProcessingUrl;
        return this;
    }

    /**
     * Returns the {@link ScoreOAuth2LoginConfigurer.AuthorizationEndpointConfig} for configuring the Authorization Server's Authorization Endpoint.
     *
     * @return the {@link ScoreOAuth2LoginConfigurer.AuthorizationEndpointConfig}
     */
    public AuthorizationEndpointConfig authorizationEndpoint() {
        return this.authorizationEndpointConfig;
    }

    /**
     * Configures the Authorization Server's Authorization Endpoint.
     *
     * @param authorizationEndpointCustomizer the {@link org.springframework.security.config.Customizer} to provide more options for
     *                                        the {@link ScoreOAuth2LoginConfigurer.AuthorizationEndpointConfig}
     * @return the {@link ScoreOAuth2LoginConfigurer} for further customizations
     */
    public ScoreOAuth2LoginConfigurer<B> authorizationEndpoint(Customizer<AuthorizationEndpointConfig> authorizationEndpointCustomizer) {
        authorizationEndpointCustomizer.customize(this.authorizationEndpointConfig);
        return this;
    }

    /**
     * Configuration options for the Authorization Server's Authorization Endpoint.
     */
    public class AuthorizationEndpointConfig {
        private String authorizationRequestBaseUri;
        private OAuth2AuthorizationRequestResolver authorizationRequestResolver;
        private AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository;

        private AuthorizationEndpointConfig() {
        }

        /**
         * Sets the base {@code URI} used for authorization requests.
         *
         * @param authorizationRequestBaseUri the base {@code URI} used for authorization requests
         * @return the {@link ScoreOAuth2LoginConfigurer.AuthorizationEndpointConfig} for further configuration
         */
        public ScoreOAuth2LoginConfigurer.AuthorizationEndpointConfig baseUri(String authorizationRequestBaseUri) {
            Assert.hasText(authorizationRequestBaseUri, "authorizationRequestBaseUri cannot be empty");
            this.authorizationRequestBaseUri = authorizationRequestBaseUri;
            return this;
        }

        /**
         * Sets the resolver used for resolving {@link OAuth2AuthorizationRequest}'s.
         *
         * @param authorizationRequestResolver the resolver used for resolving {@link OAuth2AuthorizationRequest}'s
         * @return the {@link ScoreOAuth2LoginConfigurer.AuthorizationEndpointConfig} for further configuration
         * @since 5.1
         */
        public ScoreOAuth2LoginConfigurer.AuthorizationEndpointConfig authorizationRequestResolver(OAuth2AuthorizationRequestResolver authorizationRequestResolver) {
            Assert.notNull(authorizationRequestResolver, "authorizationRequestResolver cannot be null");
            this.authorizationRequestResolver = authorizationRequestResolver;
            return this;
        }

        /**
         * Sets the repository used for storing {@link OAuth2AuthorizationRequest}'s.
         *
         * @param authorizationRequestRepository the repository used for storing {@link OAuth2AuthorizationRequest}'s
         * @return the {@link ScoreOAuth2LoginConfigurer.AuthorizationEndpointConfig} for further configuration
         */
        public ScoreOAuth2LoginConfigurer.AuthorizationEndpointConfig authorizationRequestRepository(AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository) {
            Assert.notNull(authorizationRequestRepository, "authorizationRequestRepository cannot be null");
            this.authorizationRequestRepository = authorizationRequestRepository;
            return this;
        }

        /**
         * Returns the {@link ScoreOAuth2LoginConfigurer} for further configuration.
         *
         * @return the {@link ScoreOAuth2LoginConfigurer}
         */
        public ScoreOAuth2LoginConfigurer<B> and() {
            return ScoreOAuth2LoginConfigurer.this;
        }
    }

    /**
     * Returns the {@link ScoreOAuth2LoginConfigurer.TokenEndpointConfig} for configuring the Authorization Server's Token Endpoint.
     *
     * @return the {@link ScoreOAuth2LoginConfigurer.TokenEndpointConfig}
     */
    public ScoreOAuth2LoginConfigurer.TokenEndpointConfig tokenEndpoint() {
        return this.tokenEndpointConfig;
    }

    /**
     * Configures the Authorization Server's Token Endpoint.
     *
     * @param tokenEndpointCustomizer the {@link Customizer} to provide more options for
     *                                the {@link ScoreOAuth2LoginConfigurer.TokenEndpointConfig}
     * @return the {@link ScoreOAuth2LoginConfigurer} for further customizations
     * @throws Exception
     */
    public ScoreOAuth2LoginConfigurer<B> tokenEndpoint(Customizer<ScoreOAuth2LoginConfigurer.TokenEndpointConfig> tokenEndpointCustomizer) {
        tokenEndpointCustomizer.customize(this.tokenEndpointConfig);
        return this;
    }

    /**
     * Configuration options for the Authorization Server's Token Endpoint.
     */
    public class TokenEndpointConfig {
        private OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient;

        private TokenEndpointConfig() {
        }

        /**
         * Sets the client used for requesting the access token credential from the Token Endpoint.
         *
         * @param accessTokenResponseClient the client used for requesting the access token credential from the Token Endpoint
         * @return the {@link ScoreOAuth2LoginConfigurer.TokenEndpointConfig} for further configuration
         */
        public ScoreOAuth2LoginConfigurer.TokenEndpointConfig accessTokenResponseClient(
                OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient) {

            Assert.notNull(accessTokenResponseClient, "accessTokenResponseClient cannot be null");
            this.accessTokenResponseClient = accessTokenResponseClient;
            return this;
        }

        /**
         * Returns the {@link ScoreOAuth2LoginConfigurer} for further configuration.
         *
         * @return the {@link ScoreOAuth2LoginConfigurer}
         */
        public ScoreOAuth2LoginConfigurer<B> and() {
            return ScoreOAuth2LoginConfigurer.this;
        }
    }

    /**
     * Returns the {@link ScoreOAuth2LoginConfigurer.RedirectionEndpointConfig} for configuring the Client's Redirection Endpoint.
     *
     * @return the {@link ScoreOAuth2LoginConfigurer.RedirectionEndpointConfig}
     */
    public ScoreOAuth2LoginConfigurer.RedirectionEndpointConfig redirectionEndpoint() {
        return this.redirectionEndpointConfig;
    }

    /**
     * Configures the Client's Redirection Endpoint.
     *
     * @param redirectionEndpointCustomizer the {@link Customizer} to provide more options for
     *                                      the {@link ScoreOAuth2LoginConfigurer.RedirectionEndpointConfig}
     * @return the {@link ScoreOAuth2LoginConfigurer} for further customizations
     */
    public ScoreOAuth2LoginConfigurer<B> redirectionEndpoint(Customizer<ScoreOAuth2LoginConfigurer.RedirectionEndpointConfig> redirectionEndpointCustomizer) {
        redirectionEndpointCustomizer.customize(this.redirectionEndpointConfig);
        return this;
    }

    /**
     * Configuration options for the Client's Redirection Endpoint.
     */
    public class RedirectionEndpointConfig {
        private String authorizationResponseBaseUri;

        private RedirectionEndpointConfig() {
        }

        /**
         * Sets the {@code URI} where the authorization response will be processed.
         *
         * @param authorizationResponseBaseUri the {@code URI} where the authorization response will be processed
         * @return the {@link ScoreOAuth2LoginConfigurer.RedirectionEndpointConfig} for further configuration
         */
        public ScoreOAuth2LoginConfigurer.RedirectionEndpointConfig baseUri(String authorizationResponseBaseUri) {
            Assert.hasText(authorizationResponseBaseUri, "authorizationResponseBaseUri cannot be empty");
            this.authorizationResponseBaseUri = authorizationResponseBaseUri;
            return this;
        }

        /**
         * Returns the {@link ScoreOAuth2LoginConfigurer} for further configuration.
         *
         * @return the {@link ScoreOAuth2LoginConfigurer}
         */
        public ScoreOAuth2LoginConfigurer<B> and() {
            return ScoreOAuth2LoginConfigurer.this;
        }
    }

    /**
     * Returns the {@link ScoreOAuth2LoginConfigurer.UserInfoEndpointConfig} for configuring the Authorization Server's UserInfo Endpoint.
     *
     * @return the {@link ScoreOAuth2LoginConfigurer.UserInfoEndpointConfig}
     */
    public ScoreOAuth2LoginConfigurer.UserInfoEndpointConfig userInfoEndpoint() {
        return this.userInfoEndpointConfig;
    }

    /**
     * Configures the Authorization Server's UserInfo Endpoint.
     *
     * @param userInfoEndpointCustomizer the {@link Customizer} to provide more options for
     *                                   the {@link ScoreOAuth2LoginConfigurer.UserInfoEndpointConfig}
     * @return the {@link ScoreOAuth2LoginConfigurer} for further customizations
     */
    public ScoreOAuth2LoginConfigurer<B> userInfoEndpoint(Customizer<ScoreOAuth2LoginConfigurer.UserInfoEndpointConfig> userInfoEndpointCustomizer) {
        userInfoEndpointCustomizer.customize(this.userInfoEndpointConfig);
        return this;
    }

    /**
     * Configuration options for the Authorization Server's UserInfo Endpoint.
     */
    public class UserInfoEndpointConfig {
        private OAuth2UserService<OAuth2UserRequest, OAuth2User> userService;
        private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService;
        private final Map<String, Class<? extends OAuth2User>> customUserTypes = new HashMap<>();

        private UserInfoEndpointConfig() {
        }

        /**
         * Sets the OAuth 2.0 service used for obtaining the user attributes of the End-User from the UserInfo Endpoint.
         *
         * @param userService the OAuth 2.0 service used for obtaining the user attributes of the End-User from the UserInfo Endpoint
         * @return the {@link ScoreOAuth2LoginConfigurer.UserInfoEndpointConfig} for further configuration
         */
        public ScoreOAuth2LoginConfigurer.UserInfoEndpointConfig userService(OAuth2UserService<OAuth2UserRequest, OAuth2User> userService) {
            Assert.notNull(userService, "userService cannot be null");
            this.userService = userService;
            return this;
        }

        /**
         * Sets the OpenID Connect 1.0 service used for obtaining the user attributes of the End-User from the UserInfo Endpoint.
         *
         * @param oidcUserService the OpenID Connect 1.0 service used for obtaining the user attributes of the End-User from the UserInfo Endpoint
         * @return the {@link ScoreOAuth2LoginConfigurer.UserInfoEndpointConfig} for further configuration
         */
        public ScoreOAuth2LoginConfigurer.UserInfoEndpointConfig oidcUserService(OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService) {
            Assert.notNull(oidcUserService, "oidcUserService cannot be null");
            this.oidcUserService = oidcUserService;
            return this;
        }

        /**
         * Sets a custom {@link OAuth2User} type and associates it to the provided
         * client {@link ClientRegistration#getRegistrationId() registration identifier}.
         *
         * @param customUserType       a custom {@link OAuth2User} type
         * @param clientRegistrationId the client registration identifier
         * @return the {@link ScoreOAuth2LoginConfigurer.UserInfoEndpointConfig} for further configuration
         */
        public ScoreOAuth2LoginConfigurer.UserInfoEndpointConfig customUserType(Class<? extends OAuth2User> customUserType, String clientRegistrationId) {
            Assert.notNull(customUserType, "customUserType cannot be null");
            Assert.hasText(clientRegistrationId, "clientRegistrationId cannot be empty");
            this.customUserTypes.put(clientRegistrationId, customUserType);
            return this;
        }

        /**
         * Sets the {@link GrantedAuthoritiesMapper} used for mapping {@link OAuth2User#getAuthorities()}.
         *
         * @param userAuthoritiesMapper the {@link GrantedAuthoritiesMapper} used for mapping the user's authorities
         * @return the {@link ScoreOAuth2LoginConfigurer.UserInfoEndpointConfig} for further configuration
         */
        public ScoreOAuth2LoginConfigurer.UserInfoEndpointConfig userAuthoritiesMapper(GrantedAuthoritiesMapper userAuthoritiesMapper) {
            Assert.notNull(userAuthoritiesMapper, "userAuthoritiesMapper cannot be null");
            ScoreOAuth2LoginConfigurer.this.getBuilder().setSharedObject(GrantedAuthoritiesMapper.class, userAuthoritiesMapper);
            return this;
        }

        /**
         * Returns the {@link ScoreOAuth2LoginConfigurer} for further configuration.
         *
         * @return the {@link ScoreOAuth2LoginConfigurer}
         */
        public ScoreOAuth2LoginConfigurer<B> and() {
            return ScoreOAuth2LoginConfigurer.this;
        }
    }

    @Override
    public void init(B http) throws Exception {
        OAuth2LoginAuthenticationFilter authenticationFilter =
                new OAuth2LoginAuthenticationFilter(
                        ScoreOAuth2ClientConfigurerUtils.getClientRegistrationRepository(this.getBuilder()),
                        ScoreOAuth2ClientConfigurerUtils.getAuthorizedClientRepository(this.getBuilder()),
                        this.loginProcessingUrl);
        if (authorizationEndpointConfig.authorizationRequestRepository != null) {
            authenticationFilter.setAuthorizationRequestRepository(authorizationEndpointConfig.authorizationRequestRepository);
        }
        this.setAuthenticationFilter(authenticationFilter);
        super.loginProcessingUrl(this.loginProcessingUrl);

        if (this.loginPage != null) {
            // Set custom login page
            super.loginPage(this.loginPage);
            super.init(http);
        } else {
            Map<String, String> loginUrlToClientName = this.getLoginLinks();
            if (loginUrlToClientName.size() == 1) {
                // Setup auto-redirect to provider login page
                // when only 1 client is configured
                this.updateAuthenticationDefaults();
                this.updateAccessDefaults(http);
                String providerLoginPage = loginUrlToClientName.keySet().iterator().next();
                this.registerAuthenticationEntryPoint(http, this.getLoginEntryPoint(http, providerLoginPage));
            } else {
                super.init(http);
            }
        }

        OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient =
                this.tokenEndpointConfig.accessTokenResponseClient;
        if (accessTokenResponseClient == null) {
            accessTokenResponseClient = new DefaultAuthorizationCodeTokenResponseClient();
        }

        OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService = getOAuth2UserService();
        OAuth2LoginAuthenticationProvider oauth2LoginAuthenticationProvider =
                new OAuth2LoginAuthenticationProvider(accessTokenResponseClient, oauth2UserService);
        GrantedAuthoritiesMapper userAuthoritiesMapper = this.getGrantedAuthoritiesMapper();
        if (userAuthoritiesMapper != null) {
            oauth2LoginAuthenticationProvider.setAuthoritiesMapper(userAuthoritiesMapper);
        }
        http.authenticationProvider(this.postProcess(oauth2LoginAuthenticationProvider));

        boolean oidcAuthenticationProviderEnabled = ClassUtils.isPresent(
                "org.springframework.security.oauth2.jwt.JwtDecoder", this.getClass().getClassLoader());

        if (oidcAuthenticationProviderEnabled) {
            OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService = getOidcUserService();
            OidcAuthorizationCodeAuthenticationProvider oidcAuthorizationCodeAuthenticationProvider =
                    new OidcAuthorizationCodeAuthenticationProvider(accessTokenResponseClient, oidcUserService);
            JwtDecoderFactory<ClientRegistration> jwtDecoderFactory = this.getJwtDecoderFactoryBean();
            if (jwtDecoderFactory != null) {
                oidcAuthorizationCodeAuthenticationProvider.setJwtDecoderFactory(jwtDecoderFactory);
            }
            if (userAuthoritiesMapper != null) {
                oidcAuthorizationCodeAuthenticationProvider.setAuthoritiesMapper(userAuthoritiesMapper);
            }
            http.authenticationProvider(this.postProcess(oidcAuthorizationCodeAuthenticationProvider));
        } else {
            http.authenticationProvider(new ScoreOAuth2LoginConfigurer.OidcAuthenticationRequestChecker());
        }

        this.initDefaultLoginFilter(http);
    }

    @Override
    public void configure(B http) throws Exception {
        OAuth2AuthorizationRequestRedirectFilter authorizationRequestFilter;

        if (this.authorizationEndpointConfig.authorizationRequestResolver != null) {
            authorizationRequestFilter = new OAuth2AuthorizationRequestRedirectFilter(
                    this.authorizationEndpointConfig.authorizationRequestResolver);
        } else {
            String authorizationRequestBaseUri = this.authorizationEndpointConfig.authorizationRequestBaseUri;
            if (authorizationRequestBaseUri == null) {
                authorizationRequestBaseUri = OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI;
            }
            authorizationRequestFilter = new OAuth2AuthorizationRequestRedirectFilter(
                    ScoreOAuth2ClientConfigurerUtils.getClientRegistrationRepository(this.getBuilder()), authorizationRequestBaseUri);
        }

        if (this.authorizationEndpointConfig.authorizationRequestRepository != null) {
            authorizationRequestFilter.setAuthorizationRequestRepository(
                    this.authorizationEndpointConfig.authorizationRequestRepository);
        }
        RequestCache requestCache = http.getSharedObject(RequestCache.class);
        if (requestCache != null) {
            authorizationRequestFilter.setRequestCache(requestCache);
        }
        http.addFilter(this.postProcess(authorizationRequestFilter));

        OAuth2LoginAuthenticationFilter authenticationFilter = this.getAuthenticationFilter();
        if (this.redirectionEndpointConfig.authorizationResponseBaseUri != null) {
            authenticationFilter.setFilterProcessesUrl(this.redirectionEndpointConfig.authorizationResponseBaseUri);
        }
        if (this.authorizationEndpointConfig.authorizationRequestRepository != null) {
            authenticationFilter.setAuthorizationRequestRepository(
                    this.authorizationEndpointConfig.authorizationRequestRepository);
        }
        super.configure(http);
    }

    @Override
    protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
        return new AntPathRequestMatcher(loginProcessingUrl);
    }

    @SuppressWarnings("unchecked")
    private JwtDecoderFactory<ClientRegistration> getJwtDecoderFactoryBean() {
        ResolvableType type = ResolvableType.forClassWithGenerics(JwtDecoderFactory.class, ClientRegistration.class);
        String[] names = this.getBuilder().getSharedObject(ApplicationContext.class).getBeanNamesForType(type);
        if (names.length > 1) {
            throw new NoUniqueBeanDefinitionException(type, names);
        }
        if (names.length == 1) {
            return (JwtDecoderFactory<ClientRegistration>) this.getBuilder().getSharedObject(ApplicationContext.class).getBean(names[0]);
        }
        return null;
    }

    private GrantedAuthoritiesMapper getGrantedAuthoritiesMapper() {
        GrantedAuthoritiesMapper grantedAuthoritiesMapper =
                this.getBuilder().getSharedObject(GrantedAuthoritiesMapper.class);
        if (grantedAuthoritiesMapper == null) {
            grantedAuthoritiesMapper = this.getGrantedAuthoritiesMapperBean();
            if (grantedAuthoritiesMapper != null) {
                this.getBuilder().setSharedObject(GrantedAuthoritiesMapper.class, grantedAuthoritiesMapper);
            }
        }
        return grantedAuthoritiesMapper;
    }

    private GrantedAuthoritiesMapper getGrantedAuthoritiesMapperBean() {
        Map<String, GrantedAuthoritiesMapper> grantedAuthoritiesMapperMap =
                BeanFactoryUtils.beansOfTypeIncludingAncestors(
                        this.getBuilder().getSharedObject(ApplicationContext.class),
                        GrantedAuthoritiesMapper.class);
        return (!grantedAuthoritiesMapperMap.isEmpty() ? grantedAuthoritiesMapperMap.values().iterator().next() : null);
    }

    private OAuth2UserService<OidcUserRequest, OidcUser> getOidcUserService() {
        if (this.userInfoEndpointConfig.oidcUserService != null) {
            return this.userInfoEndpointConfig.oidcUserService;
        }
        ResolvableType type = ResolvableType.forClassWithGenerics(OAuth2UserService.class, OidcUserRequest.class, OidcUser.class);
        OAuth2UserService<OidcUserRequest, OidcUser> bean = getBeanOrNull(type);
        if (bean == null) {
            return new OidcUserService();
        }

        return bean;
    }

    private OAuth2UserService<OAuth2UserRequest, OAuth2User> getOAuth2UserService() {
        if (this.userInfoEndpointConfig.userService != null) {
            return this.userInfoEndpointConfig.userService;
        }
        ResolvableType type = ResolvableType.forClassWithGenerics(OAuth2UserService.class, OAuth2UserRequest.class, OAuth2User.class);
        OAuth2UserService<OAuth2UserRequest, OAuth2User> bean = getBeanOrNull(type);
        if (bean == null) {
            if (!this.userInfoEndpointConfig.customUserTypes.isEmpty()) {
                List<OAuth2UserService<OAuth2UserRequest, OAuth2User>> userServices = new ArrayList<>();
                userServices.add(new CustomUserTypesOAuth2UserService(this.userInfoEndpointConfig.customUserTypes));
                userServices.add(new DefaultOAuth2UserService());
                return new DelegatingOAuth2UserService<>(userServices);
            } else {
                return new DefaultOAuth2UserService();
            }
        }

        return bean;
    }

    private <T> T getBeanOrNull(ResolvableType type) {
        ApplicationContext context = getBuilder().getSharedObject(ApplicationContext.class);
        if (context == null) {
            return null;
        }
        String[] names = context.getBeanNamesForType(type);
        if (names.length == 1) {
            return (T) context.getBean(names[0]);
        }
        return null;
    }

    private void initDefaultLoginFilter(B http) {
        DefaultLoginPageGeneratingFilter loginPageGeneratingFilter = http.getSharedObject(DefaultLoginPageGeneratingFilter.class);
        if (loginPageGeneratingFilter == null || this.isCustomLoginPage()) {
            return;
        }

        loginPageGeneratingFilter.setOauth2LoginEnabled(true);
        loginPageGeneratingFilter.setOauth2AuthenticationUrlToClientName(this.getLoginLinks());
        loginPageGeneratingFilter.setLoginPageUrl(this.getLoginPage());
        loginPageGeneratingFilter.setFailureUrl(this.getFailureUrl());
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getLoginLinks() {
        Iterable<ClientRegistration> clientRegistrations = null;
        ClientRegistrationRepository clientRegistrationRepository =
                ScoreOAuth2ClientConfigurerUtils.getClientRegistrationRepository(this.getBuilder());
        ResolvableType type = ResolvableType.forInstance(clientRegistrationRepository).as(Iterable.class);
        if (type != ResolvableType.NONE && ClientRegistration.class.isAssignableFrom(type.resolveGenerics()[0])) {
            clientRegistrations = (Iterable<ClientRegistration>) clientRegistrationRepository;
        }
        if (clientRegistrations == null) {
            return Collections.emptyMap();
        }

        String authorizationRequestBaseUri = this.authorizationEndpointConfig.authorizationRequestBaseUri != null ?
                this.authorizationEndpointConfig.authorizationRequestBaseUri :
                OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI;
        Map<String, String> loginUrlToClientName = new HashMap<>();
        clientRegistrations.forEach(registration -> loginUrlToClientName.put(
                authorizationRequestBaseUri + "/" + registration.getRegistrationId(),
                registration.getClientName()));

        return loginUrlToClientName;
    }

    private AuthenticationEntryPoint getLoginEntryPoint(B http, String providerLoginPage) {
        RequestMatcher loginPageMatcher = new AntPathRequestMatcher(this.getLoginPage());
        RequestMatcher faviconMatcher = new AntPathRequestMatcher("/favicon.ico");
        RequestMatcher defaultEntryPointMatcher = this.getAuthenticationEntryPointMatcher(http);
        RequestMatcher defaultLoginPageMatcher = new AndRequestMatcher(
                new OrRequestMatcher(loginPageMatcher, faviconMatcher), defaultEntryPointMatcher);

        RequestMatcher notXRequestedWith = new NegatedRequestMatcher(
                new RequestHeaderRequestMatcher("X-Requested-With", "XMLHttpRequest"));

        LinkedHashMap<RequestMatcher, AuthenticationEntryPoint> entryPoints = new LinkedHashMap<>();
        entryPoints.put(new AndRequestMatcher(notXRequestedWith, new NegatedRequestMatcher(defaultLoginPageMatcher)),
                new LoginUrlAuthenticationEntryPoint(providerLoginPage));

        DelegatingAuthenticationEntryPoint loginEntryPoint = new DelegatingAuthenticationEntryPoint(entryPoints);
        loginEntryPoint.setDefaultEntryPoint(this.getAuthenticationEntryPoint());

        return loginEntryPoint;
    }

    private static class OidcAuthenticationRequestChecker implements AuthenticationProvider {

        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            OAuth2LoginAuthenticationToken authorizationCodeAuthentication =
                    (OAuth2LoginAuthenticationToken) authentication;

            // Section 3.1.2.1 Authentication Request - https://openid.net/specs/openid-connect-core-1_0.html#AuthRequest
            // scope
            // 		REQUIRED. OpenID Connect requests MUST contain the "openid" scope value.
            if (authorizationCodeAuthentication.getAuthorizationExchange()
                    .getAuthorizationRequest().getScopes().contains(OidcScopes.OPENID)) {

                OAuth2Error oauth2Error = new OAuth2Error(
                        "oidc_provider_not_configured",
                        "An OpenID Connect Authentication Provider has not been configured. " +
                                "Check to ensure you include the dependency 'spring-security-oauth2-jose'.",
                        null);
                throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
            }

            return null;
        }

        @Override
        public boolean supports(Class<?> authentication) {
            return OAuth2LoginAuthenticationToken.class.isAssignableFrom(authentication);
        }
    }
}