package org.oagi.score.gateway.http.api.account_management.repository;

import org.oagi.score.gateway.http.api.account_management.model.*;
import org.oagi.score.gateway.http.api.account_management.repository.criteria.AccountListFilterCriteria;
import org.oagi.score.gateway.http.api.account_management.repository.criteria.PendingListFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;

import java.util.List;

public interface AccountQueryRepository {

    ResultAndCount<AccountListEntryRecord> getAccountList(
            AccountListFilterCriteria filterCriteria, PageRequest pageRequest);

    AccountDetailsRecord getAccountDetails(UserId appUserId);

    AccountDetailsRecord getAccountDetailsByLoginId(String username);

    String getEncodedPassword(UserId userId);

    List<String> getLoginIdList();

    OAuth2UserRecord getOAuth2User(OAuth2UserId oAuth2UserId, String sub);

    OAuth2UserRecord getOAuth2User(String providerName, String sub);

    OAuth2UserRecord getOAuth2User(UserId userId);

    ResultAndCount<AppOauth2User> getPendingList(PendingListFilterCriteria filterCriteria, PageRequest pageRequest);

    AppOauth2User getPending(OAuth2UserId appOauth2UserId);

    AppOauth2User getPendingBySub(String providerName, String sub);

}
