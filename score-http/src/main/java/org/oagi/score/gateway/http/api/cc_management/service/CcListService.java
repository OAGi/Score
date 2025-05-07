package org.oagi.score.gateway.http.api.cc_management.service;

import org.apache.commons.lang3.tuple.Pair;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.*;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.cc_management.repository.CcListRepository;
import org.oagi.score.gateway.http.api.info_management.model.SummaryCcExt;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.tag_management.model.TagSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.PageResponse;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.Tables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

@Deprecated
@Service
@Transactional(readOnly = true)
public class CcListService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private CcListRepository repository;

    @Autowired
    private DSLContext dslContext;

    public PageResponse<CcList> getCcList(ScoreUser requester, CcListRequest request) {
        request.setUsernameMap(repositoryFactory.scoreUserQueryRepository().getScoreUsers().stream()
                .collect(Collectors.toMap(e -> e.userId(), e -> e.username())));
        PageResponse<CcList> response = repository.getCcList(requester, request);
        Map<Pair<CcType, ManifestId>, List<TagSummaryRecord>> tags =
                repositoryFactory.tagQueryRepository(requester)
                        .getTagSummariesByPairsOfTypeAndManifestId(response.getList().stream()
                                .map(e -> Pair.of(e.getType(), e.getManifestId())).collect(Collectors.toList()));
        response.getList().forEach(ccList -> {
            ccList.setTagList(
                    tags.getOrDefault(Pair.of(ccList.getType(), ccList.getManifestId()), Collections.emptyList()));
        });
        return response;
    }

    public PageResponse<CcList> getCcListWithLastUpdatedAndSince(ScoreUser requester, CcListRequest request) {
        PageResponse<CcList> response = getCcList(requester, request);
        Map<String, String> lastUpdatedReleaseMap = repository.getLastUpdatedRelease();
        Map<String, String> sinceReleaseMap = repository.getSinceRelease();
        response.getList().forEach(ccList -> {
            if (ccList.getType().equals(CcType.ASCCP)) {
                String lastChangedReleaseNum = lastUpdatedReleaseMap.get(ccList.getDen());
                ccList.setLastChangedReleaseNum(lastChangedReleaseNum);
                String sinceReleaseNum = sinceReleaseMap.get(ccList.getDen());
                ccList.setSinceReleaseNum(sinceReleaseNum);
            }

        });
        return response;
    }

    public List<SummaryCcExt> getMyExtensionsUnusedInBIEs(ScoreUser requester, LibraryId libraryId) {
        UserId requesterId = requester.userId();

        ReleaseSummaryRecord workingRelease = repositoryFactory.releaseQueryRepository(requester)
                .getReleaseSummary(libraryId, "Working");

        List<ULong> uegAccManifestIds = dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID.as("id"))
                .from(ACC)
                .join(ACC_MANIFEST).on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                .join(RELEASE).on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                .where(and(
                        ACC.OAGIS_COMPONENT_TYPE.eq(OagisComponentType.UserExtensionGroup.getValue()),
                        ACC_MANIFEST.RELEASE_ID.notEqual(ULong.valueOf(workingRelease.releaseId().value())),
                        LIBRARY.LIBRARY_ID.eq(ULong.valueOf(libraryId.value())),
                        ACC.OWNER_USER_ID.eq(ULong.valueOf(requesterId.value()))))
                .fetchInto(ULong.class);

        byte isUsed = (byte) 0;

        List<SummaryCcExt> summaryCcExtListForAscc = dslContext.select(
                        Tables.ACC.ACC_ID,
                        Tables.ACC.GUID,
                        Tables.ACC.OBJECT_CLASS_TERM,
                        Tables.ACC.STATE,
                        Tables.ACC.LAST_UPDATE_TIMESTAMP,
                        ACC_MANIFEST.ACC_MANIFEST_ID,
                        RELEASE.RELEASE_ID,
                        RELEASE.RELEASE_NUM,
                        APP_USER.LOGIN_ID,
                        APP_USER.APP_USER_ID,
                        APP_USER.as("updater").LOGIN_ID,
                        TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                        TOP_LEVEL_ASBIEP.STATE,
                        ASCCP_MANIFEST.as("bie_manifest").DEN,
                        ASCCP_MANIFEST.DEN,
                        ASBIE.SEQ_KEY)
                .from(ASCC)
                .join(ASCC_MANIFEST).on(ASCC_MANIFEST.ASCC_ID.eq(ASCC.ASCC_ID))
                .join(ACC).on(ASCC.FROM_ACC_ID.eq(ACC.ACC_ID))
                .join(ACC_MANIFEST).on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                .join(RELEASE).on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                .join(ASBIE).on(and(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(ASBIE.BASED_ASCC_MANIFEST_ID), ASBIE.IS_USED.eq(isUsed)))
                .join(ASCCP_MANIFEST).on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(ASCCP).on(ASCC.TO_ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(APP_USER).on(ACC.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(APP_USER.as("updater")).on(ACC.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID))
                .join(TOP_LEVEL_ASBIEP).on(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                .join(ASBIEP).on(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                .join(ASCCP_MANIFEST.as("bie_manifest")).on(ASCCP_MANIFEST.as("bie_manifest").ASCCP_ID.eq(ASBIEP.BASED_ASCCP_MANIFEST_ID))
                .join(ASCCP.as("bie")).on(ASCCP_MANIFEST.as("bie_manifest").ASCCP_ID.eq(ASCCP.as("bie").ASCCP_ID))
                .where(and(
                        LIBRARY.LIBRARY_ID.eq(ULong.valueOf(libraryId.value())),
                        ACC_MANIFEST.ACC_MANIFEST_ID.in(uegAccManifestIds)
                ))
                .fetchStream().map(e -> {
                    SummaryCcExt item = new SummaryCcExt();
                    item.setAccId(new AccId(e.get(Tables.ACC.ACC_ID).toBigInteger()));
                    item.setAccManifestId(new AccManifestId(e.get(ACC_MANIFEST.ACC_MANIFEST_ID).toBigInteger()));
                    item.setReleaseId(new ReleaseId(e.get(RELEASE.RELEASE_ID).toBigInteger()));
                    item.setReleaseNum(e.get(RELEASE.RELEASE_NUM));
                    item.setGuid(new Guid(e.get(Tables.ACC.GUID)));
                    item.setObjectClassTerm(e.get(Tables.ACC.OBJECT_CLASS_TERM));
                    item.setState(CcState.valueOf(e.get(Tables.ACC.STATE)));
                    item.setLastUpdateTimestamp(Date.from(e.get(Tables.ACC.LAST_UPDATE_TIMESTAMP, LocalDateTime.class)
                            .atZone(ZoneId.systemDefault()).toInstant()));
                    item.setLastUpdateUser(e.get(APP_USER.as("updater").LOGIN_ID));
                    item.setOwnerUsername(e.get(APP_USER.LOGIN_ID));
                    item.setOwnerUserId(new UserId(e.get(APP_USER.APP_USER_ID).toBigInteger()));
                    item.setTopLevelAsbiepId(new TopLevelAsbiepId(e.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger()));
                    item.setBieState(BieState.valueOf(e.get(TOP_LEVEL_ASBIEP.STATE)));
                    item.setDen(e.get(ASCCP_MANIFEST.as("bie_manifest").DEN));
                    item.setAssociationDen(e.get(ASCCP_MANIFEST.DEN));
                    item.setSeqKey(e.get(ASBIE.SEQ_KEY).intValue());
                    return item;
                }).collect(Collectors.toList());

        List<SummaryCcExt> summaryCcExtListForBcc = dslContext.select(
                        Tables.ACC.ACC_ID,
                        Tables.ACC.GUID,
                        Tables.ACC.OBJECT_CLASS_TERM,
                        Tables.ACC.STATE,
                        Tables.ACC.LAST_UPDATE_TIMESTAMP,
                        ACC_MANIFEST.ACC_MANIFEST_ID,
                        RELEASE.RELEASE_ID,
                        RELEASE.RELEASE_NUM,
                        APP_USER.LOGIN_ID,
                        APP_USER.APP_USER_ID,
                        APP_USER.as("updater").LOGIN_ID,
                        TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                        TOP_LEVEL_ASBIEP.STATE,
                        ASCCP_MANIFEST.as("bie_manifest").DEN,
                        BCCP_MANIFEST.DEN,
                        BBIE.SEQ_KEY)
                .from(BCC)
                .join(ACC).on(BCC.FROM_ACC_ID.eq(ACC.ACC_ID))
                .join(ACC_MANIFEST).on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                .join(RELEASE).on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                .join(BCC_MANIFEST).on(BCC.BCC_ID.eq(BCC_MANIFEST.BCC_ID))
                .join(BBIE).on(and(BCC_MANIFEST.BCC_MANIFEST_ID.eq(BBIE.BASED_BCC_MANIFEST_ID), BBIE.IS_USED.eq(isUsed)))
                .join(BCCP_MANIFEST).on(BCC_MANIFEST.TO_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.BCCP_MANIFEST_ID))
                .join(BCCP).on(BCC.TO_BCCP_ID.eq(BCCP.BCCP_ID))
                .join(APP_USER).on(ACC.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(APP_USER.as("updater")).on(ACC.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID))
                .join(TOP_LEVEL_ASBIEP).on(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                .join(ASBIEP).on(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                .join(ASCCP_MANIFEST.as("bie_manifest")).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.as("bie_manifest").ASCCP_MANIFEST_ID))
                .join(ASCCP.as("bie")).on(ASCCP.as("bie").ASCCP_ID.eq(ASCCP_MANIFEST.as("bie_manifest").ASCCP_ID))
                .where(and(
                        LIBRARY.LIBRARY_ID.eq(ULong.valueOf(libraryId.value())),
                        ACC_MANIFEST.ACC_MANIFEST_ID.in(uegAccManifestIds)
                ))
                .fetchStream().map(e -> {
                    SummaryCcExt item = new SummaryCcExt();
                    item.setAccId(new AccId(e.get(Tables.ACC.ACC_ID).toBigInteger()));
                    item.setAccManifestId(new AccManifestId(e.get(ACC_MANIFEST.ACC_MANIFEST_ID).toBigInteger()));
                    item.setReleaseId(new ReleaseId(e.get(RELEASE.RELEASE_ID).toBigInteger()));
                    item.setReleaseNum(e.get(RELEASE.RELEASE_NUM));
                    item.setGuid(new Guid(e.get(Tables.ACC.GUID)));
                    item.setObjectClassTerm(e.get(Tables.ACC.OBJECT_CLASS_TERM));
                    item.setState(CcState.valueOf(e.get(Tables.ACC.STATE)));
                    item.setLastUpdateTimestamp(Date.from(e.get(Tables.ACC.LAST_UPDATE_TIMESTAMP, LocalDateTime.class)
                            .atZone(ZoneId.systemDefault()).toInstant()));
                    item.setLastUpdateUser(e.get(APP_USER.as("updater").LOGIN_ID));
                    item.setOwnerUsername(e.get(APP_USER.LOGIN_ID));
                    item.setOwnerUserId(new UserId(e.get(APP_USER.APP_USER_ID).toBigInteger()));
                    item.setTopLevelAsbiepId(new TopLevelAsbiepId(e.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger()));
                    item.setBieState(BieState.valueOf(e.get(TOP_LEVEL_ASBIEP.STATE)));
                    item.setDen(e.get(ASCCP_MANIFEST.as("bie_manifest").DEN));
                    item.setAssociationDen(e.get(BCCP_MANIFEST.DEN));
                    item.setSeqKey(e.get(BBIE.SEQ_KEY).intValue());
                    return item;
                }).collect(Collectors.toList());

        Set<SummaryCcExt> set = new HashSet();
        set.addAll(summaryCcExtListForAscc);
        set.addAll(summaryCcExtListForBcc);

        List<SummaryCcExt> result = new ArrayList(set);
        result.sort((o1, o2) -> {
            int compFirst = o1.getAccId().value().compareTo(o2.getAccId().value());
            if (compFirst == 0) {
                return Integer.compare(o1.getSeqKey(), o2.getSeqKey());
            }
            return compFirst;
        });
        return result;
    }
}
