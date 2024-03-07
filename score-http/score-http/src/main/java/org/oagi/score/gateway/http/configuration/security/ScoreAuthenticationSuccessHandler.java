package org.oagi.score.gateway.http.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jooq.DSLContext;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AppOauth2UserRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.Oauth2AppRecord;
import org.oagi.score.repo.api.message.model.SendMessageRequest;
import org.oagi.score.service.message.MessageService;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.OAUTH2_APP;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.AppOauth2User.APP_OAUTH2_USER;

@Component
public class ScoreAuthenticationSuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler
        implements InitializingBean {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private MessageService messageService;

    @Override
    public void afterPropertiesSet() throws Exception {
        setUseReferer(true);
        setDefaultTargetUrl("/");
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            clearAuthenticationAttributes(request);

            Map<String, Object> resp = new HashMap();
            UserDetails userDetails = (UserDetails) principal;
            resp.put("username", userDetails.getUsername());
            resp.put("authentication", "basic");
            resp.put("roles", userDetails.getAuthorities().stream().map(e -> e.toString()).collect(Collectors.toList()));

            ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
            objectMapper.writeValue(response.getOutputStream(), resp);
        } else if (principal instanceof OAuth2User) {
            OAuth2User oAuth2User = (OAuth2User) principal;
            AppOauth2UserRecord appOauth2UserRecord = storeOAuth2UserInfoIfAbsent(authentication, oAuth2User);
            if (appOauth2UserRecord != null) {
                StringBuilder messageBody = new StringBuilder();
                messageBody = messageBody.append("[**A new account**](")
                        .append("/account/pending/").append(appOauth2UserRecord.getAppOauth2UserId())
                        .append(") (Sub: ")
                        .append(appOauth2UserRecord.getSub())
                        .append(") is awaiting for the approval.");

                for (org.oagi.score.repo.api.user.model.ScoreUser adminUser : sessionService.getScoreAdminUsers()) {
                    SendMessageRequest sendMessageRequest = new SendMessageRequest(
                            sessionService.getScoreSystemUser())
                            .withRecipient(adminUser)
                            .withSubject("Awaiting account (Sub: " + appOauth2UserRecord.getSub() + ") approval")
                            .withBody(messageBody.toString())
                            .withBodyContentType(SendMessageRequest.MARKDOWN_CONTENT_TYPE);

                    messageService.asyncSendMessage(sendMessageRequest);
                }
            }

            handle(request, response, authentication);
            clearAuthenticationAttributes(request);
        }
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        String targetUrl = super.determineTargetUrl(request, response);
        if (targetUrl == null) {
            targetUrl = getDefaultTargetUrl();
        }
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

    private AppOauth2UserRecord storeOAuth2UserInfoIfAbsent(Authentication authentication, OAuth2User oAuth2User) {
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

            appOauth2UserRecord.setAppOauth2UserId(
                    dslContext.insertInto(APP_OAUTH2_USER)
                            .set(appOauth2UserRecord)
                            .returning(APP_OAUTH2_USER.APP_OAUTH2_USER_ID)
                            .fetchOne().getAppOauth2UserId()
            );
            return appOauth2UserRecord;
        }
        return null;
    }
}