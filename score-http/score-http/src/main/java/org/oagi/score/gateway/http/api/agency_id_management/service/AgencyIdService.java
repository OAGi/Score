package org.oagi.score.gateway.http.api.agency_id_management.service;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.agency_id_management.data.*;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.agency.model.AgencyIdList;
import org.oagi.score.repo.api.agency.model.GetAgencyIdListListRequest;
import org.oagi.score.repo.api.agency.model.GetAgencyIdListListResponse;

import org.oagi.score.repo.api.corecomponent.model.CcState;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AgencyIdListValueRecord;
import org.oagi.score.repo.api.user.model.ScoreRole;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.service.common.data.AccessPrivilege;
import org.oagi.score.service.log.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.or;
import static org.oagi.score.repo.api.corecomponent.model.CcState.Production;
import static org.oagi.score.repo.api.corecomponent.model.CcState.Published;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.AgencyIdListManifest.AGENCY_ID_LIST_MANIFEST;

@Service
@Transactional(readOnly = true)
public class AgencyIdService {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private ScoreRepositoryFactory scoreRepositoryFactory;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private LogRepository logRepository;

    public GetSimpleAgencyIdListValuesResponse getSimpleAgencyIdListValues(ScoreUser user, BigInteger releaseId) {
        List<SimpleAgencyIdList> simpleAgencyIdLists;
        if (user.hasAnyRole(ScoreRole.END_USER)) {
            simpleAgencyIdLists = dslContext.select(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                            AGENCY_ID_LIST.AGENCY_ID_LIST_ID,
                            AGENCY_ID_LIST.NAME,
                            AGENCY_ID_LIST.STATE)
                    .from(AGENCY_ID_LIST_MANIFEST)
                    .join(AGENCY_ID_LIST)
                    .on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                    .join(RELEASE)
                    .on(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .where(and(
                            RELEASE.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                            RELEASE.STATE.in(Published.name(), Production.name()),
                            or(
                                    AGENCY_ID_LIST.STATE.in(Published.name(), Production.name()),
                                    AGENCY_ID_LIST.PREV_AGENCY_ID_LIST_ID.isNotNull()
                            )
                    ))
                    .fetchInto(SimpleAgencyIdList.class);
        } else {
            simpleAgencyIdLists = dslContext.select(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                            AGENCY_ID_LIST.AGENCY_ID_LIST_ID,
                            AGENCY_ID_LIST.NAME,
                            AGENCY_ID_LIST.STATE)
                    .from(AGENCY_ID_LIST_MANIFEST)
                    .join(AGENCY_ID_LIST)
                    .on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                    .join(RELEASE)
                    .on(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(APP_USER)
                    .on(AGENCY_ID_LIST.LAST_UPDATED_BY.eq(APP_USER.APP_USER_ID))
                    .where(and(
                            RELEASE.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                            RELEASE.STATE.in(Published.name(), Production.name()),
                            or(
                                    AGENCY_ID_LIST.STATE.in(Published.name(), Production.name()),
                                    AGENCY_ID_LIST.PREV_AGENCY_ID_LIST_ID.isNotNull()
                            ),
                            APP_USER.IS_DEVELOPER.eq((byte) 1)
                    ))
                    .fetchInto(SimpleAgencyIdList.class);
        }

        List<SimpleAgencyIdListValue> simpleAgencyIdListValues = dslContext.select(
                        AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                        AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                        AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID,
                        AGENCY_ID_LIST_VALUE.VALUE,
                        AGENCY_ID_LIST_VALUE.NAME)
                .from(AGENCY_ID_LIST_VALUE)
                .join(AGENCY_ID_LIST_VALUE_MANIFEST)
                .on(and(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID),
                        AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))))
                .where(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.in(
                        simpleAgencyIdLists.stream().map(e -> ULong.valueOf(e.getAgencyIdListManifestId())).collect(Collectors.toList())
                ))
                .fetchStreamInto(SimpleAgencyIdListValue.class)
                .sorted(Comparator.comparing(SimpleAgencyIdListValue::getName))
                .collect(Collectors.toList());

        GetSimpleAgencyIdListValuesResponse response = new GetSimpleAgencyIdListValuesResponse();
        response.setAgencyIdLists(simpleAgencyIdLists);
        response.setAgencyIdListValues(simpleAgencyIdListValues);
        return response;
    }

    public AgencyIdList getAgencyIdListDetail(ScoreUser user, BigInteger manifestId) {
        AgencyIdList agencyIdList = scoreRepositoryFactory.createAgencyIdListReadRepository().getAgencyIdList(manifestId);
        boolean isWorkingRelease = agencyIdList.getReleaseNum().equals("Working");
        agencyIdList.setAccess(
                AccessPrivilege.toAccessPrivilege(sessionService.getAppUserByUsername(user.getUserId()),
                        sessionService.getAppUserByUsername(agencyIdList.getOwner().getUserId()),
                        agencyIdList.getState().name(), isWorkingRelease).name()
        );
        if (agencyIdList.getPrevAgencyIdListId() != null) {
            agencyIdList.setPrev(scoreRepositoryFactory.createAgencyIdListReadRepository().getAgencyIdListById(agencyIdList.getPrevAgencyIdListId()));
        }
        return agencyIdList;
    }

    public GetAgencyIdListListResponse getAgencyIdListList(GetAgencyIdListListRequest request) {
        return scoreRepositoryFactory.createAgencyIdListReadRepository().getAgencyIdListList(request);
    }

    @Transactional
    public BigInteger createAgencyIdList(ScoreUser user, CreateAgencyIdListRequest request) {
        return scoreRepositoryFactory.createAgencyIdListWriteRepository().createAgencyIdList(user, request.getReleaseId(), request.getBasedAgencyIdListManifestId());
    }

    @Transactional
    public AgencyIdList updateAgencyIdListProperty(ScoreUser user, AgencyIdList agencyIdList) {
        return scoreRepositoryFactory.createAgencyIdListWriteRepository().updateAgencyIdListProperty(user, agencyIdList);
    }

    @Transactional
    public void updateAgencyIdListState(ScoreUser user, BigInteger agencyIdListManifestId, CcState toState) {
        scoreRepositoryFactory.createAgencyIdListWriteRepository().updateAgencyIdListState(user, agencyIdListManifestId, toState);
    }

    @Transactional
    public void updateAgencyIdListState(AuthenticatedPrincipal user, LocalDateTime timestamp, BigInteger agencyIdListManifestId, String state) {
        scoreRepositoryFactory.createAgencyIdListWriteRepository().updateAgencyIdListState(sessionService.asScoreUser(user),
                agencyIdListManifestId, CcState.valueOf(state));
    }

    @Transactional
    public void transferOwnership(ScoreUser user, BigInteger agencyIdListManifestId, String targetLoginId) {
        scoreRepositoryFactory.createAgencyIdListWriteRepository().transferOwnershipAgencyIdList(user, agencyIdListManifestId, targetLoginId);
    }

    @Transactional
    public void reviseAgencyIdList(ScoreUser user, BigInteger agencyIdListManifestId) {
        scoreRepositoryFactory.createAgencyIdListWriteRepository().reviseAgencyIdList(user, agencyIdListManifestId);
    }

    @Transactional
    public void cancelAgencyIdList(ScoreUser user, BigInteger agencyIdListManifestId) {
        scoreRepositoryFactory.createAgencyIdListWriteRepository().cancelAgencyIdList(user, agencyIdListManifestId);
    }

    public boolean hasSameAgencyIdList(SameAgencyIdListParams params) {
        List<Condition> conditions = new ArrayList();
        conditions.add(and(
                AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(params.getReleaseId())),
                AGENCY_ID_LIST.STATE.notEqual(CcState.Deleted.name())
        ));
        if (params.getAgencyIdListManifestId() != null) {
            conditions.add(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.ne(ULong.valueOf(params.getAgencyIdListManifestId())));
        }
        if (params.getAgencyIdListValueManifestId() == null) {
            conditions.add(and(
                    AGENCY_ID_LIST.LIST_ID.eq(params.getListId()),
                    AGENCY_ID_LIST.AGENCY_ID_LIST_VALUE_ID.isNull(),
                    AGENCY_ID_LIST.VERSION_ID.eq(params.getVersionId())
            ));
            return dslContext.selectCount()
                    .from(AGENCY_ID_LIST_MANIFEST)
                    .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                    .where(conditions).fetchOneInto(Integer.class) > 0;
        } else {
            AgencyIdListValueRecord valueRecord = dslContext.select(AGENCY_ID_LIST_VALUE.fields())
                    .from(AGENCY_ID_LIST_VALUE_MANIFEST)
                    .join(AGENCY_ID_LIST_VALUE).on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID))
                    .where(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(ULong.valueOf(params.getAgencyIdListValueManifestId())))
                    .fetchOneInto(AgencyIdListValueRecord.class);

            conditions.add(and(
                    AGENCY_ID_LIST.LIST_ID.eq(params.getListId()),
                    AGENCY_ID_LIST.VERSION_ID.eq(params.getVersionId()),
                    AGENCY_ID_LIST_VALUE.NAME.eq(valueRecord.getName()),
                    AGENCY_ID_LIST_VALUE.VALUE.eq(valueRecord.getValue())
            ));

            return dslContext.selectCount()
                    .from(AGENCY_ID_LIST_MANIFEST)
                    .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                    .join(AGENCY_ID_LIST_VALUE_MANIFEST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID))
                    .join(AGENCY_ID_LIST_VALUE).on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID))
                    .where(conditions).fetchOneInto(Integer.class) > 0;
        }
    }

    public boolean hasSameNameAgencyIdList(SameNameAgencyIdListParams params) {
        List<Condition> conditions = new ArrayList();
        conditions.add(and(
                AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(params.getReleaseId())),
                AGENCY_ID_LIST.STATE.notEqual(CcState.Deleted.name())
        ));

        if (params.getAgencyIdListManifestId() != null) {
            conditions.add(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.ne(ULong.valueOf(params.getAgencyIdListManifestId())));
        }
        conditions.add(AGENCY_ID_LIST.NAME.eq(params.getAgencyIdListName()));

        return dslContext.selectCount()
                .from(AGENCY_ID_LIST_MANIFEST)
                .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                .where(conditions).fetchOneInto(Integer.class) > 0;
    }
}
