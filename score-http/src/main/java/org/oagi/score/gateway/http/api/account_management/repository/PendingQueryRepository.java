package org.oagi.score.gateway.http.api.account_management.repository;

import org.oagi.score.gateway.http.api.account_management.model.AppOauth2User;
import org.oagi.score.gateway.http.api.account_management.repository.criteria.PendingListFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;

public interface PendingQueryRepository {

    ResultAndCount<AppOauth2User> getPendingList(PendingListFilterCriteria filterCriteria, PageRequest pageRequest);

}
