package org.oagi.score.gateway.http.api.account_management.service;

import org.oagi.score.gateway.http.api.account_management.repository.criteria.PendingListFilterCriteria;
import org.oagi.score.gateway.http.api.account_management.model.AppOauth2User;
import org.oagi.score.gateway.http.api.account_management.model.OAuth2UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
public class PendingListService {

    private final MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ResultAndCount<AppOauth2User> getPendingList(ScoreUser requester,
                                                        PendingListFilterCriteria filterCriteria, PageRequest pageRequest) {
        requireAdministrator(requester);

        return repositoryFactory.accountQueryRepository(requester).getPendingList(filterCriteria, pageRequest);
    }

    private void requireAdministrator(ScoreUser requester) {
        if (requester == null || !requester.isAdministrator()) {
            throw new InsufficientAuthenticationException(
                    messages.getMessage(
                            "ExceptionTranslationFilter.insufficientAuthentication",
                            "Full authentication is required to access this resource"));
        }
    }

    public AppOauth2User getPending(ScoreUser requester, OAuth2UserId appOauth2UserId) {
        requireAdministrator(requester);

        return repositoryFactory.accountQueryRepository(requester).getPending(appOauth2UserId);
    }

    public AppOauth2User getPendingBySub(ScoreUser requester, String sub) {
        return repositoryFactory.accountQueryRepository(requester).getPendingBySub(sub);
    }

    @Transactional
    public void deletePending(ScoreUser requester, OAuth2UserId appOauth2UserId) {
        requireAdministrator(requester);

        if (appOauth2UserId != null) {
            repositoryFactory.accountCommandRepository(requester, passwordEncoder).deletePending(appOauth2UserId);
        }
    }

    @Transactional
    public void linkPendingToAppUser(ScoreUser requester, OAuth2UserId appOauth2UserId, UserId appUserId) {
        requireAdministrator(requester);

        var query = repositoryFactory.accountQueryRepository(requester);
        if (query.getAccountDetails(appUserId) == null) {
            throw new IllegalArgumentException("Cannot find target Account");
        }

        if (query.getPending(appOauth2UserId) == null) {
            throw new IllegalArgumentException("Cannot find target Pending Account");
        }

        var command = repositoryFactory.accountCommandRepository(requester, passwordEncoder);
        command.delinkOAuth2User(appUserId);
        command.linkOAuth2User(appOauth2UserId, appUserId);
    }
}
