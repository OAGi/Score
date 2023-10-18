package org.oagi.score.gateway.http.api.cc_management.service;

import org.apache.commons.lang3.tuple.Pair;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;
import org.oagi.score.data.ACC;
import org.oagi.score.data.Release;
import org.oagi.score.gateway.http.api.cc_management.data.*;
import org.oagi.score.gateway.http.api.cc_management.repository.CcListRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.ManifestRepository;
import org.oagi.score.gateway.http.api.info.data.SummaryCcExt;
import org.oagi.score.gateway.http.api.tag_management.data.ShortTag;
import org.oagi.score.gateway.http.api.tag_management.service.TagService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.api.bie.model.BieState;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccpManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BccpManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtManifestRecord;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.repo.component.release.ReleaseRepository;
import org.oagi.score.repository.UserRepository;
import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.service.common.data.OagisComponentType;
import org.oagi.score.service.common.data.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Service
@Transactional(readOnly = true)
public class CcListService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CcListRepository repository;

    @Autowired
    private CcNodeService ccNodeService;

    @Autowired
    private ManifestRepository manifestRepository;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReleaseRepository releaseRepository;

    @Autowired
    private TagService tagService;

    @Autowired
    private DSLContext dslContext;

    public PageResponse<CcList> getCcList(CcListRequest request) {
        request.setUsernameMap(userRepository.getUsernameMap());
        PageResponse<CcList> response = repository.getCcList(request);
        Map<Pair<CcType, BigInteger>, List<ShortTag>> tags =
                tagService.getShortTagsByPairsOfTypeAndManifestId(response.getList().stream()
                        .map(e -> Pair.of(e.getType(), e.getManifestId())).collect(Collectors.toList()));
        response.getList().forEach(ccList -> {
            ccList.setTagList(
                    tags.getOrDefault(Pair.of(ccList.getType(), ccList.getManifestId()), Collections.emptyList())
            );
        });
        return response;
    }

    public CcChangesResponse getCcChanges(ScoreUser requester, BigInteger releaseId) {
        Collection<CcChangesResponse.CcChange> changeList = repository.getCcChanges(requester, ULong.valueOf(releaseId));
        CcChangesResponse response = new CcChangesResponse();
        response.addCcChangeList(changeList);
        return response;
    }

    public ACC getAcc(BigInteger manifestId) {
        List<Field> fields = new ArrayList();
        fields.add(ACC_MANIFEST.ACC_MANIFEST_ID);
        fields.addAll(Arrays.asList(ACC.fields()));

        return dslContext.select(fields)
                .from(ACC)
                .join(ACC_MANIFEST).on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(manifestId)))
                .fetchOneInto(ACC.class);
    }

    @Transactional
    public void transferOwnership(AuthenticatedPrincipal user, String type, BigInteger manifestId, String targetLoginId) {
        AppUser targetUser = sessionService.getAppUserByUsername(targetLoginId);
        if (targetUser == null) {
            throw new IllegalArgumentException("Not found a target user.");
        }

        switch (CcType.valueOf(type.toUpperCase())) {
            case ACC:
                AccManifestRecord accManifest = manifestRepository.getAccManifestById(ULong.valueOf(manifestId));
                if (accManifest == null) {
                    throw new IllegalArgumentException("Not found a target ACC.");
                }

                ccNodeService.updateAccOwnerUserId(user, manifestId, targetUser.getAppUserId());
                break;

            case ASCCP:
                AsccpManifestRecord asccpManifest = manifestRepository.getAsccpManifestById(ULong.valueOf(manifestId));
                if (asccpManifest == null) {
                    throw new IllegalArgumentException("Not found a target ASCCP.");
                }

                ccNodeService.updateAsccpOwnerUserId(user, manifestId, targetUser.getAppUserId());
                break;

            case BCCP:
                BccpManifestRecord bccpManifest = manifestRepository.getBccpManifestById(ULong.valueOf(manifestId));
                if (bccpManifest == null) {
                    throw new IllegalArgumentException("Not found a target BCCP.");
                }

                ccNodeService.updateBccpOwnerUserId(user, manifestId, targetUser.getAppUserId());
                break;
            case DT:
                DtManifestRecord dtManifest = manifestRepository.getDtManifestById(ULong.valueOf(manifestId));
                if (dtManifest == null) {
                    throw new IllegalArgumentException("Not found a target DT.");
                }

                ccNodeService.updateDtOwnerUserId(user, manifestId, targetUser.getAppUserId());
                break;

            default:
                throw new UnsupportedOperationException("Unsupported type: " + type);
        }
    }

    public List<SummaryCcExt> getMyExtensionsUnusedInBIEs(AuthenticatedPrincipal user) {
        BigInteger requesterId = sessionService.userId(user);

        Release workingRelease = releaseRepository.getWorkingRelease();

        List<ULong> uegAccManifestIds = dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID.as("id"))
                .from(ACC)
                .join(ACC_MANIFEST).on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                .where(and(
                        ACC.OAGIS_COMPONENT_TYPE.eq(OagisComponentType.UserExtensionGroup.getValue()),
                        ACC_MANIFEST.RELEASE_ID.notEqual(ULong.valueOf(workingRelease.getReleaseId())),
                        ACC.OWNER_USER_ID.eq(ULong.valueOf(requesterId))
                ))
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
                .join(ASBIE).on(and(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(ASBIE.BASED_ASCC_MANIFEST_ID), ASBIE.IS_USED.eq(isUsed)))
                .join(ASCCP_MANIFEST).on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(ASCCP).on(ASCC.TO_ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(APP_USER).on(ACC.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(APP_USER.as("updater")).on(ACC.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID))
                .join(TOP_LEVEL_ASBIEP).on(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                .join(ASBIEP).on(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                .join(ASCCP_MANIFEST.as("bie_manifest")).on(ASCCP_MANIFEST.as("bie_manifest").ASCCP_ID.eq(ASBIEP.BASED_ASCCP_MANIFEST_ID))
                .join(ASCCP.as("bie")).on(ASCCP_MANIFEST.as("bie_manifest").ASCCP_ID.eq(ASCCP.as("bie").ASCCP_ID))
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.in(uegAccManifestIds))
                .fetchStream().map(e -> {
                    SummaryCcExt item = new SummaryCcExt();
                    item.setAccId(e.get(Tables.ACC.ACC_ID).toBigInteger());
                    item.setAccManifestId(e.get(ACC_MANIFEST.ACC_MANIFEST_ID).toBigInteger());
                    item.setReleaseId(e.get(RELEASE.RELEASE_ID).toBigInteger());
                    item.setReleaseNum(e.get(RELEASE.RELEASE_NUM));
                    item.setGuid(e.get(Tables.ACC.GUID));
                    item.setObjectClassTerm(e.get(Tables.ACC.OBJECT_CLASS_TERM));
                    item.setState(CcState.valueOf(e.get(Tables.ACC.STATE)));
                    item.setLastUpdateTimestamp(Date.from(e.get(Tables.ACC.LAST_UPDATE_TIMESTAMP, LocalDateTime.class)
                            .atZone(ZoneId.systemDefault()).toInstant()));
                    item.setLastUpdateUser(e.get(APP_USER.as("updater").LOGIN_ID));
                    item.setOwnerUsername(e.get(APP_USER.LOGIN_ID));
                    item.setOwnerUserId(e.get(APP_USER.APP_USER_ID).toBigInteger());
                    item.setTopLevelAsbiepId(e.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger());
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
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.in(uegAccManifestIds))
                .fetchStream().map(e -> {
                    SummaryCcExt item = new SummaryCcExt();
                    item.setAccId(e.get(Tables.ACC.ACC_ID).toBigInteger());
                    item.setAccManifestId(e.get(ACC_MANIFEST.ACC_MANIFEST_ID).toBigInteger());
                    item.setReleaseId(e.get(RELEASE.RELEASE_ID).toBigInteger());
                    item.setReleaseNum(e.get(RELEASE.RELEASE_NUM));
                    item.setGuid(e.get(Tables.ACC.GUID));
                    item.setObjectClassTerm(e.get(Tables.ACC.OBJECT_CLASS_TERM));
                    item.setState(CcState.valueOf(e.get(Tables.ACC.STATE)));
                    item.setLastUpdateTimestamp(Date.from(e.get(Tables.ACC.LAST_UPDATE_TIMESTAMP, LocalDateTime.class)
                            .atZone(ZoneId.systemDefault()).toInstant()));
                    item.setLastUpdateUser(e.get(APP_USER.as("updater").LOGIN_ID));
                    item.setOwnerUsername(e.get(APP_USER.LOGIN_ID));
                    item.setOwnerUserId(e.get(APP_USER.APP_USER_ID).toBigInteger());
                    item.setTopLevelAsbiepId(e.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger());
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
            int compFirst = o1.getAccId().compareTo(o2.getAccId());
            if (compFirst == 0) {
                return Integer.compare(o1.getSeqKey(), o2.getSeqKey());
            }
            return compFirst;
        });
        return result;
    }

    @Transactional
    public void deleteCcs(AuthenticatedPrincipal user, CcUpdateStateListRequest request) {
        request.getAccManifestIds().forEach(e -> {
            ccNodeService.deleteAcc(user, e);
        });
        request.getAsccpManifestIds().forEach(e -> {
            ccNodeService.deleteAsccp(user, e);
        });
        request.getBccpManifestIds().forEach(e -> {
            ccNodeService.deleteBccp(user, e);
        });
        request.getDtManifestIds().forEach(e -> {
            ccNodeService.deleteDt(user, e);
        });
    }

    @Transactional
    public void purgeCcs(AuthenticatedPrincipal user, CcUpdateStateListRequest request) {
        boolean isRequestForSingle = request.getAccManifestIds().size() +
                request.getAsccpManifestIds().size() +
                request.getBccpManifestIds().size() +
                request.getDtManifestIds().size() == 1;

        request.getAccManifestIds().forEach(e -> {
            ccNodeService.purgeAcc(user, e, (isRequestForSingle) ? false : true);
        });
        request.getAsccpManifestIds().forEach(e -> {
            ccNodeService.purgeAsccp(user, e, (isRequestForSingle) ? false : true, false);
        });
        request.getBccpManifestIds().forEach(e -> {
            ccNodeService.purgeBccp(user, e, (isRequestForSingle) ? false : true);
        });
        request.getDtManifestIds().forEach(e -> {
            ccNodeService.purgeDt(user, e, (isRequestForSingle) ? false : true);
        });
    }

    @Transactional
    public void restoreCcs(AuthenticatedPrincipal user, CcUpdateStateListRequest request) {
        request.getAccManifestIds().forEach(e -> {
            ccNodeService.updateAccState(user, e, CcState.WIP);
        });
        request.getAsccpManifestIds().forEach(e -> {
            ccNodeService.updateAsccpState(user, e, CcState.WIP);
        });
        request.getBccpManifestIds().forEach(e -> {
            ccNodeService.updateBccpState(user, e, CcState.WIP);
        });
        request.getDtManifestIds().forEach(e -> {
            ccNodeService.updateDtState(user, e, CcState.WIP);
        });
    }

    @Transactional
    public void updateStateCcs(AuthenticatedPrincipal user, CcUpdateStateListRequest request) {
        request.getAccManifestIds().forEach(e -> {
            ccNodeService.updateAccState(user, e, CcState.valueOf(request.getToState()));
        });
        request.getAsccpManifestIds().forEach(e -> {
            ccNodeService.updateAsccpState(user, e, CcState.valueOf(request.getToState()));
        });
        request.getBccpManifestIds().forEach(e -> {
            ccNodeService.updateBccpState(user, e, CcState.valueOf(request.getToState()));
        });
        request.getDtManifestIds().forEach(e -> {
            ccNodeService.updateDtState(user, e, CcState.valueOf(request.getToState()));
        });
    }

    @Transactional
    public void transferOwnershipList(AuthenticatedPrincipal user, CcTransferOwnershipListRequest request) {
        request.getAccManifestIds().forEach(e -> {
            transferOwnership(user, "ACC", e, request.getTargetLoginId());
        });
        request.getAsccpManifestIds().forEach(e -> {
            transferOwnership(user, "ASCCP", e, request.getTargetLoginId());
        });
        request.getBccpManifestIds().forEach(e -> {
            transferOwnership(user, "BCCP", e, request.getTargetLoginId());
        });
        request.getDtManifestIds().forEach(e -> {
            transferOwnership(user, "DT", e, request.getTargetLoginId());
        });
    }
}

