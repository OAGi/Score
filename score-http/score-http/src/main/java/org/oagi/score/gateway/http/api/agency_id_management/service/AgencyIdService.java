package org.oagi.score.gateway.http.api.agency_id_management.service;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.export.model.AgencyId;
import org.oagi.score.gateway.http.api.agency_id_management.data.CreateAgencyIdListRequest;
import org.oagi.score.gateway.http.api.agency_id_management.data.SameAgencyIdListParams;
import org.oagi.score.gateway.http.api.agency_id_management.data.SameNameAgencyIdListParams;
import org.oagi.score.gateway.http.api.agency_id_management.data.SimpleAgencyIdListValue;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.agency.model.AgencyIdList;
import org.oagi.score.repo.api.agency.model.GetAgencyIdListListRequest;
import org.oagi.score.repo.api.agency.model.GetAgencyIdListListResponse;

import org.oagi.score.repo.api.corecomponent.model.CcState;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.service.common.data.AccessPrivilege;
import org.oagi.score.service.log.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.AGENCY_ID_LIST;
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

    public List<SimpleAgencyIdListValue> getSimpleAgencyIdListValues(BigInteger releaseId) {
        return dslContext.select(Tables.AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID,
                Tables.AGENCY_ID_LIST_VALUE.NAME)
                .from(Tables.AGENCY_ID_LIST_VALUE)
                .join(Tables.AGENCY_ID_LIST_VALUE_MANIFEST).on(and(Tables.AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.eq(Tables.AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID),
                        Tables.AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))))
                .join(Tables.APP_USER).on(and(Tables.AGENCY_ID_LIST_VALUE.OWNER_USER_ID.eq(Tables.APP_USER.APP_USER_ID), Tables.APP_USER.IS_DEVELOPER.eq((byte) 1)))
                .fetchStreamInto(SimpleAgencyIdListValue.class)
                .sorted(Comparator.comparing(SimpleAgencyIdListValue::getName))
                .collect(Collectors.toList());
    }

    public AgencyIdList getAgencyIdListDetail(ScoreUser user, BigInteger manifestId) {
        AgencyIdList agencyIdList = scoreRepositoryFactory.createAgencyIdListReadRepository().getAgencyIdList(manifestId);
        boolean isWorkingRelease = agencyIdList.getReleaseNum().equals("Working");
        agencyIdList.setAccess(
                AccessPrivilege.toAccessPrivilege(sessionService.getAppUser(user.getUserId()),
                        sessionService.getAppUser(agencyIdList.getOwner().getUserId()),
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
        scoreRepositoryFactory.createAgencyIdListWriteRepository().transferOwnerShipAgencyIdList(user, agencyIdListManifestId, targetLoginId);
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
        conditions.add(and(
                AGENCY_ID_LIST.LIST_ID.eq(params.getListId()),
                params.getAgencyId() == null ? AGENCY_ID_LIST.AGENCY_ID_LIST_VALUE_ID.isNull() :
                AGENCY_ID_LIST.AGENCY_ID_LIST_VALUE_ID.eq(ULong.valueOf(params.getAgencyId())),
                AGENCY_ID_LIST.VERSION_ID.eq(params.getVersionId())
        ));

        return dslContext.selectCount()
                .from(AGENCY_ID_LIST_MANIFEST)
                .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                .where(conditions).fetchOneInto(Integer.class) > 0;
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
