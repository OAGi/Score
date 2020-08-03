package org.oagi.score.gateway.http.api.release_management.service;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.entity.jooq.Tables;
import org.oagi.score.gateway.http.api.release_management.data.ReleaseList;
import org.oagi.score.gateway.http.api.release_management.data.ReleaseState;
import org.oagi.score.gateway.http.api.release_management.data.SimpleRelease;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReleaseService {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    public List<SimpleRelease> getSimpleReleases() {
        return dslContext.select(Tables.RELEASE.RELEASE_ID, Tables.RELEASE.RELEASE_NUM)
                .from(Tables.RELEASE)
                .where(Tables.RELEASE.STATE.eq(ReleaseState.Final.getValue()))
                .fetchInto(SimpleRelease.class);
    }

    public SimpleRelease getSimpleReleaseByReleaseId(long releaseId) {
        return dslContext.select(Tables.RELEASE.RELEASE_ID, Tables.RELEASE.RELEASE_NUM)
                .from(Tables.RELEASE)
                .where(Tables.RELEASE.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .fetchOneInto(SimpleRelease.class);
    }

    public List<ReleaseList> getReleaseList(User user) {
        List<ReleaseList> releaseLists = dslContext.select(Tables.RELEASE.RELEASE_ID,
                Tables.RELEASE.RELEASE_NUM,
                Tables.RELEASE.LAST_UPDATE_TIMESTAMP,
                Tables.RELEASE.STATE.as("raw_state"),
                Tables.NAMESPACE.URI.as("namespace"),
                Tables.APP_USER.LOGIN_ID.as("last_updated_by"))
                .from(Tables.RELEASE)
                .join(Tables.NAMESPACE)
                .on(Tables.RELEASE.NAMESPACE_ID.eq(Tables.NAMESPACE.NAMESPACE_ID))
                .join(Tables.APP_USER)
                .on(Tables.RELEASE.LAST_UPDATED_BY.eq(Tables.APP_USER.APP_USER_ID))
                .fetchInto(ReleaseList.class);

        releaseLists.forEach(releaseList -> {
            releaseList.setState(ReleaseState.valueOf(releaseList.getRawState()).name());
        });
        return releaseLists;
    }
}
