package org.oagi.score.gateway.http.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.DSLContext;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AppOauth2UserRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.Oauth2AppRecord;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.OAUTH2_APP;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.AppOauth2User.APP_OAUTH2_USER;

@Component
public class ScoreAuthenticationSuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler
        implements InitializingBean {

    @Autowired
    private DSLContext dslContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        setUseReferer(true);
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            clearAuthenticationAttributes(request);

            Map<String, String> resp = new HashMap();
            UserDetails userDetails = (UserDetails) principal;
            resp.put("username", userDetails.getUsername());
            resp.put("authentication", "basic");
            resp.put("role", userDetails.getAuthorities().stream().findFirst().get().toString());

            ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
            objectMapper.writeValue(response.getOutputStream(), resp);
        } else if (principal instanceof OAuth2User) {
            OAuth2User oAuth2User = (OAuth2User) principal;
            storeOAuth2UserInfoIfAbsent(authentication, oAuth2User);

            handle(request, response, authentication);
            clearAuthenticationAttributes(request);
        }
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        String targetUrl = super.determineTargetUrl(request, response);
        if ("/".equals(targetUrl)) {
            if (authentication instanceof OAuth2AuthenticationToken) {
                OAuth2AuthenticationToken authenticationToken = (OAuth2AuthenticationToken) authentication;
                String redirectUri = dslContext.select(OAUTH2_APP.REDIRECT_URI)
                        .from(OAUTH2_APP)
                        .where(OAUTH2_APP.PROVIDER_NAME.eq(authenticationToken.getAuthorizedClientRegistrationId()))
                        .fetchOneInto(String.class);

                Assert.isTrue(UrlUtils.isAbsoluteUrl(redirectUri),
                        "redirectUri must start with 'http(s)'");

                int idx = redirectUri.indexOf('/', redirectUri.indexOf("://") + 3);
                targetUrl = redirectUri.substring(0, (idx == -1) ? redirectUri.length() : idx + 1);
                logger.debug("Using redirect Url: " + targetUrl);
            }
        }
        return targetUrl.replaceAll("/login", "/");
    }

    private void storeOAuth2UserInfoIfAbsent(Authentication authentication, OAuth2User oAuth2User) {
        String sub = oAuth2User.getAttribute("sub");
        AppOauth2UserRecord appOauth2UserRecord = dslContext.selectFrom(APP_OAUTH2_USER)
                .where(APP_OAUTH2_USER.SUB.eq(sub))
                .fetchOptional().orElse(null);

        if (appOauth2UserRecord == null) {
            OAuth2AuthenticationToken authenticationToken = (OAuth2AuthenticationToken) authentication;
            Oauth2AppRecord oauth2AppRecord = dslContext.selectFrom(OAUTH2_APP)
                    .where(OAUTH2_APP.PROVIDER_NAME.eq(authenticationToken.getAuthorizedClientRegistrationId()))
                    .fetchOne();

            String name = oAuth2User.getAttribute("name");
            String email = oAuth2User.getAttribute("email");
            String nickname = oAuth2User.getAttribute("nickname");
            String preferredUsername = oAuth2User.getAttribute("preferred_username");
            String phoneNumber = oAuth2User.getAttribute("phone_number");
            LocalDateTime timestamp = LocalDateTime.now();

            appOauth2UserRecord = new AppOauth2UserRecord();
            appOauth2UserRecord.setOauth2AppId(oauth2AppRecord.getOauth2AppId());
            appOauth2UserRecord.setName(name);
            appOauth2UserRecord.setEmail(email);
            appOauth2UserRecord.setNickname(nickname);
            appOauth2UserRecord.setPreferredUsername(preferredUsername);
            appOauth2UserRecord.setPhoneNumber(phoneNumber);
            appOauth2UserRecord.setCreationTimestamp(timestamp);
            appOauth2UserRecord.setSub(sub);
            appOauth2UserRecord.setEmail(email);

            dslContext.insertInto(APP_OAUTH2_USER)
                    .set(appOauth2UserRecord)
                    .execute();
        }
    }
}