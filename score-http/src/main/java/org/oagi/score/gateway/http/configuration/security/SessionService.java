package org.oagi.score.gateway.http.configuration.security;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.repository.ScoreUserQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.session.Session;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.oagi.score.gateway.http.common.model.ScoreUser.SYSTEM_USER_ID;

@Service
@Transactional(readOnly = true)
public class SessionService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private RedisIndexedSessionRepository sessionRepository;

    private ScoreUserQueryRepository query() {
        return repositoryFactory.scoreUserQueryRepository();
    }

    public UserId userId(AuthenticatedPrincipal principal) {
        ScoreUser user = asScoreUser(principal);
        if (user == null) {
            throw new ScoreDataAccessException("User does not exist.");
        }
        return user.userId();
    }

    public void invalidateByUsername(String username) {
        Map<String, ? extends Session> sessions = sessionRepository.findByPrincipalName(username);
        sessions.values().forEach(session -> {
            sessionRepository.deleteById(session.getId());
        });
    }

    public ScoreUser getScoreUserByUserId(UserId userId) {
        return query().getScoreUser(userId);
    }

    public ScoreUser getScoreUserByUsername(String username) {
        return query().getScoreUserByUsername(username);
    }

    public ScoreUser getScoreSystemUser() {
        return query().getScoreUser(new UserId(SYSTEM_USER_ID));
    }

    public List<ScoreUser> getScoreAdminUsers() {
        return query().getScoreUsersByRole(ScoreRole.ADMINISTRATOR);
    }

    public ScoreUser asScoreUser(AuthenticatedPrincipal user) {
        if (user == null) {
            throw new IllegalArgumentException("AuthenticatedPrincipal cannot be null");
        }
        if (user instanceof OAuth2User) {
            String sub = ((OAuth2User) user).getAttribute("sub");
            return query().getScoreUserByOidcSub(sub);
        } else {
            return query().getScoreUserByUsername(user.getName());
        }
    }
}
