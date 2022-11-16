package org.oagi.score.gateway.http.configuration.security;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.user.ScoreUserReadRepository;
import org.oagi.score.repo.api.user.model.GetScoreUserRequest;
import org.oagi.score.service.common.data.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.session.Session;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Map;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.APP_OAUTH2_USER;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.APP_USER;
import static org.oagi.score.repo.api.user.model.ScoreUser.SYSTEM_USER_ID;

@Service
@Transactional(readOnly = true)
public class SessionService {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private RedisIndexedSessionRepository sessionRepository;

    @Autowired
    private ScoreRepositoryFactory scoreRepositoryFactory;

    public boolean isDeveloper(BigInteger userId) {
        Record record = dslContext.select(APP_USER.IS_DEVELOPER)
                .from(APP_USER)
                .where(APP_USER.APP_USER_ID.eq(ULong.valueOf(userId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return false;
        }
        return (byte) 1 == record.get(APP_USER.IS_DEVELOPER);
    }

    public BigInteger userId(User user) {
        if (user == null) {
            return BigInteger.ZERO;
        }
        return dslContext.select(APP_USER.APP_USER_ID)
                .from(APP_USER)
                .where(APP_USER.LOGIN_ID.equalIgnoreCase(user.getUsername()))
                .fetchOptional(APP_USER.APP_USER_ID).orElse(ULong.valueOf(0L)).toBigInteger();
    }

    public BigInteger userId(OAuth2User user) {
        if (user == null) {
            return BigInteger.valueOf(0);
        }
        return dslContext.select(APP_USER.APP_USER_ID)
                .from(APP_USER)
                .join(APP_OAUTH2_USER).on(APP_USER.APP_USER_ID.eq(APP_OAUTH2_USER.APP_USER_ID))
                .where(APP_OAUTH2_USER.SUB.equalIgnoreCase((String) user.getAttribute("sub")))
                .fetchOptional(APP_USER.APP_USER_ID).orElse(ULong.valueOf(0L)).toBigInteger();
    }

    public BigInteger userId(AuthenticatedPrincipal principal) {
        if (principal == null) {
            throw new ScoreDataAccessException("User does not exist.");
        }
        if (principal instanceof User) {
            return userId((User) principal);
        } else if (principal instanceof OAuth2User) {
            return userId((OAuth2User) principal);
        } else {
            return BigInteger.valueOf(0);
        }
    }

    public AppUser getAppUserByUsername(String username) {
        return dslContext.select(
                APP_USER.APP_USER_ID,
                APP_USER.LOGIN_ID,
                APP_USER.NAME,
                APP_USER.ORGANIZATION,
                APP_USER.IS_DEVELOPER.as("developer"),
                APP_USER.IS_ADMIN.as("admin"),
                APP_USER.IS_ENABLED.as("enabled")
            ).from(APP_USER)
                .where(APP_USER.LOGIN_ID.equalIgnoreCase(username))
                .fetchOneInto(AppUser.class);
    }

    public AppUser getAppUserByUsername(BigInteger appUserId) {
        return dslContext.select(
                APP_USER.APP_USER_ID,
                APP_USER.LOGIN_ID,
                APP_USER.NAME,
                APP_USER.ORGANIZATION,
                APP_USER.IS_DEVELOPER.as("developer"),
                APP_USER.IS_ADMIN.as("admin"),
                APP_USER.IS_ENABLED.as("enabled")
        ).from(APP_USER)
                .where(APP_USER.APP_USER_ID.eq(ULong.valueOf(appUserId)))
                .fetchOneInto(AppUser.class);
    }

    public AppUser getAppUserByUsername(User user) {
        return getAppUserByUsername(user.getUsername());
    }

    public AppUser getAppUserByUsername(OAuth2User user) {
        String sub = user.getAttribute("sub");
        return dslContext.select(
                APP_USER.APP_USER_ID,
                APP_USER.LOGIN_ID,
                APP_USER.NAME,
                APP_USER.ORGANIZATION,
                APP_USER.IS_DEVELOPER.as("developer"),
                APP_USER.IS_ADMIN.as("admin"),
                APP_USER.IS_ENABLED.as("enabled"))
                .from(APP_USER)
                .join(APP_OAUTH2_USER).on(APP_USER.APP_USER_ID.eq(APP_OAUTH2_USER.APP_USER_ID))
                .where(APP_OAUTH2_USER.SUB.equalIgnoreCase(sub))
                .fetchOneInto(AppUser.class);
    }

    public AppUser getAppUserByUsername(AuthenticatedPrincipal user) {
        if (user instanceof User) {
            return getAppUserByUsername((User) user);
        } else if (user instanceof OAuth2User) {
            return getAppUserByUsername((OAuth2User) user);
        }
        return getAppUserByUsername(user.getName());
    }

    public void invalidateByUsername(String username) {
        Map<String, ? extends Session> sessions = sessionRepository.findByPrincipalName(username);
        sessions.values().forEach(session -> {
            sessionRepository.deleteById(session.getId());
        });
    }

    public org.oagi.score.repo.api.user.model.ScoreUser getScoreUserByUserId(BigInteger userId) {
        ScoreUserReadRepository repo = scoreRepositoryFactory.createScoreUserReadRepository();
        GetScoreUserRequest request = new GetScoreUserRequest().withUserId(userId);
        return repo.getScoreUser(request).getUser();
    }

    public org.oagi.score.repo.api.user.model.ScoreUser getScoreSystemUser() {
        return getScoreUserByUserId(SYSTEM_USER_ID);
    }

    public org.oagi.score.repo.api.user.model.ScoreUser asScoreUser(AuthenticatedPrincipal user) {
        ScoreUserReadRepository repo = scoreRepositoryFactory.createScoreUserReadRepository();
        GetScoreUserRequest request;
        if (user instanceof OAuth2User) {
            request = new GetScoreUserRequest().withOidcSub(user.getName());
        } else {
            request = new GetScoreUserRequest().withUserName(user.getName());
        }
        return repo.getScoreUser(request).getUser();
    }
}
