package org.oagi.score.gateway.http.api.account_management.service;

import org.oagi.score.gateway.http.api.account_management.model.AccountDetailsRecord;
import org.oagi.score.gateway.http.api.account_management.model.AccountListEntryRecord;
import org.oagi.score.gateway.http.api.account_management.model.OAuth2UserRecord;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.repository.criteria.AccountListFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AccountQueryService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    public ResultAndCount<AccountListEntryRecord> getAccountList(
            ScoreUser requester, AccountListFilterCriteria filterCriteria, PageRequest pageRequest) {

        var query = repositoryFactory.accountQueryRepository(requester);
        return query.getAccountList(filterCriteria, pageRequest);
    }

    public AccountDetailsRecord getAccountDetails(ScoreUser requester, UserId userId) {
        var query = repositoryFactory.accountQueryRepository(requester);
        return query.getAccountDetails(userId);
    }

    public AccountDetailsRecord getAccountDetails(ScoreUser requester, String str) {
        AccountDetailsRecord accountDetails;
        try {
            accountDetails = getAccountDetails(requester, UserId.from(str));
        } catch (NumberFormatException e) {
            var query = repositoryFactory.accountQueryRepository(requester);
            accountDetails = query.getAccountDetailsByLoginId(str);
        }

        if (accountDetails == null) {
            throw new AuthenticationCredentialsNotFoundException("An authentication information was not found.");
        }

        return accountDetails;
    }

    public List<String> getAccountLoginIds(ScoreUser requester) {

        var query = repositoryFactory.accountQueryRepository(requester);
        return query.getLoginIdList();
    }

    public OAuth2UserRecord getOAuth2User(ScoreUser requester, String sub) {

        var query = repositoryFactory.accountQueryRepository(requester);
        return query.getOAuth2User(sub);
    }
}
