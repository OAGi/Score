package org.oagi.score.repo.component.dt;

import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.cc_management.data.node.CcBdtPriRestri;
import org.oagi.score.gateway.http.api.cc_management.data.node.PrimitiveRestriType;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.user.model.ScoreRole;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.repo.component.bcc.UpdateBccPropertiesRepositoryRequest;
import org.oagi.score.repo.component.bccp.UpdateBccpPropertiesRepositoryRequest;
import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.service.log.LogRepository;
import org.oagi.score.service.log.model.LogAction;
import org.oagi.score.service.log.model.LogSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.compare;
import static org.jooq.impl.DSL.*;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Repository
public class DtWriteRepository {

    @Autowired
    private BdtReadRepository bdtReadRepository;

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private LogSerializer serializer;

    public CreateBdtRepositoryResponse createBdt(CreateBdtRepositoryRequest request) {
        ScoreUser requestor = sessionService.asScoreUser(request.getUser());
        ULong userId = ULong.valueOf(requestor.getUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        DtManifestRecord basedBdtManifest = dslContext.selectFrom(DT_MANIFEST)
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(ULong.valueOf(request.getBasedDdtManifestId())))
                .fetchOne();

        DtRecord basedBdt = dslContext.selectFrom(DT)
                .where(DT.DT_ID.eq(basedBdtManifest.getDtId()))
                .fetchOne();

        NamespaceRecord namespaceRecord = null;
        if (basedBdt.getNamespaceId() != null) {
            namespaceRecord = dslContext.selectFrom(NAMESPACE)
                    .where(NAMESPACE.NAMESPACE_ID.eq(basedBdt.getNamespaceId()))
                    .fetchOne();
        }

        List<DtScManifestRecord> basedDtScManifestList = dslContext.selectFrom(DT_SC_MANIFEST)
                .where(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.eq(basedBdtManifest.getDtManifestId()))
                .fetch();

        DtRecord bdt = new DtRecord();
        bdt.setGuid(ScoreGuid.randomGuid());
        bdt.setDataTypeTerm(basedBdt.getDataTypeTerm());
        if (basedBdt.getQualifier_() != null) {
            bdt.setQualifier_(basedBdt.getQualifier_());
        }
        bdt.setDen(((bdt.getQualifier_() != null) ? (bdt.getQualifier_() + "_ ") : "") + bdt.getDataTypeTerm() + ". Type");

        bdt.setRepresentationTerm(basedBdt.getDataTypeTerm());
        bdt.setBasedDtId(basedBdt.getDtId());
        bdt.setContentComponentDefinition(basedBdt.getContentComponentDefinition());
        bdt.setDefinition(basedBdt.getDefinition());
        bdt.setDefinitionSource(basedBdt.getDefinitionSource());
        bdt.setState(CcState.WIP.name());
        bdt.setIsDeprecated((byte) 0);
        bdt.setCommonlyUsed((byte) 0);
        if (namespaceRecord != null) {
            if (requestor.hasRole(ScoreRole.DEVELOPER) && (namespaceRecord.getIsStdNmsp() == (byte) 1)) {
                bdt.setNamespaceId(namespaceRecord.getNamespaceId());
            } else if (requestor.hasRole(ScoreRole.END_USER) && (namespaceRecord.getIsStdNmsp() == (byte) 0)) {
                bdt.setNamespaceId(namespaceRecord.getNamespaceId());
            }
        }
        bdt.setCreatedBy(userId);
        bdt.setLastUpdatedBy(userId);
        bdt.setOwnerUserId(userId);
        bdt.setCreationTimestamp(timestamp);
        bdt.setLastUpdateTimestamp(timestamp);
        bdt.setDtId(
                dslContext.insertInto(DT)
                        .set(bdt)
                        .returning(DT.DT_ID).fetchOne().getDtId()
        );

        DtManifestRecord bdtManifest = new DtManifestRecord();
        bdtManifest.setDtId(bdt.getDtId());
        bdtManifest.setBasedDtManifestId(basedBdtManifest.getDtManifestId());
        bdtManifest.setReleaseId(ULong.valueOf(request.getReleaseId()));
        bdtManifest = dslContext.insertInto(DT_MANIFEST)
                .set(bdtManifest)
                .returning(DT_MANIFEST.DT_MANIFEST_ID).fetchOne();

        createBdtPriRestri(bdtManifest.getDtManifestId(), basedBdtManifest.getDtManifestId(), basedBdtManifest.getBasedDtManifestId() != null);

        Map<ULong, ULong> basedScMap = new HashMap<>();

        dslContext.select(DT_SC.DT_SC_ID, DT_SC.BASED_DT_SC_ID)
                .from(DT_SC)
                .join(DT_SC_MANIFEST).on(DT_SC.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID))
                .where(and(DT_SC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())), DT_SC.BASED_DT_SC_ID.isNotNull()))
                .fetchStream().forEach(record -> {
                    basedScMap.put(record.get(DT_SC.DT_SC_ID), record.get(DT_SC.BASED_DT_SC_ID));
                });

        for (DtScManifestRecord basedDtScManifest : basedDtScManifestList) {
            DtScRecord basedDtSc = dslContext.selectFrom(DT_SC)
                    .where(DT_SC.DT_SC_ID.eq(basedDtScManifest.getDtScId()))
                    .fetchOne();

            DtScRecord dtScRecord = new DtScRecord();
            DtScManifestRecord dtScManifestRecord = new DtScManifestRecord();

            dtScRecord.setGuid(ScoreGuid.randomGuid());
            dtScRecord.setObjectClassTerm(basedDtSc.getObjectClassTerm());
            dtScRecord.setPropertyTerm(basedDtSc.getPropertyTerm());
            dtScRecord.setRepresentationTerm(basedDtSc.getRepresentationTerm());
            dtScRecord.setDefinition(basedDtSc.getDefinition());
            dtScRecord.setDefinitionSource(basedDtSc.getDefinitionSource());
            dtScRecord.setOwnerDtId(bdt.getDtId());
            if (basedBdt.getBasedDtId() == null && request.getSpecId().longValue() > 0) {
                ULong cdtScId = findCdtSc(basedDtSc.getDtScId(), basedScMap);
                CdtScRefSpecRecord specRecord = dslContext.selectFrom(CDT_SC_REF_SPEC)
                        .where(and(CDT_SC_REF_SPEC.CDT_SC_ID.eq(cdtScId), CDT_SC_REF_SPEC.REF_SPEC_ID.eq(ULong.valueOf(request.getSpecId()))))
                        .fetchOne();
                if (specRecord != null) {
                    dtScRecord.setCardinalityMin(0);
                    dtScRecord.setCardinalityMax(1);
                } else {
                    dtScRecord.setCardinalityMax(0);
                    dtScRecord.setCardinalityMin(0);
                }
            } else {
                dtScRecord.setCardinalityMin(basedDtSc.getCardinalityMin());
                dtScRecord.setCardinalityMax(basedDtSc.getCardinalityMax());
            }

            dtScRecord.setBasedDtScId(basedDtSc.getDtScId());
            dtScRecord.setDefaultValue(basedDtSc.getDefaultValue());
            dtScRecord.setFixedValue(basedDtSc.getFixedValue());
            dtScRecord.setCreatedBy(userId);
            dtScRecord.setLastUpdatedBy(userId);
            dtScRecord.setOwnerUserId(userId);
            dtScRecord.setCreationTimestamp(timestamp);
            dtScRecord.setLastUpdateTimestamp(timestamp);
            dtScRecord.setDtScId(
                    dslContext.insertInto(DT_SC)
                            .set(dtScRecord)
                            .returning(DT_SC.DT_SC_ID).fetchOne().getDtScId());

            dtScManifestRecord.setReleaseId(basedBdtManifest.getReleaseId());
            dtScManifestRecord.setDtScId(dtScRecord.getDtScId());
            dtScManifestRecord.setBasedDtScManifestId(basedDtScManifest.getDtScManifestId());
            dtScManifestRecord.setOwnerDtManifestId(bdtManifest.getDtManifestId());

            dtScManifestRecord.setDtScManifestId(
                    dslContext.insertInto(DT_SC_MANIFEST)
                            .set(dtScManifestRecord)
                            .returning(DT_SC_MANIFEST.DT_SC_MANIFEST_ID).fetchOne().getDtScManifestId());

            createBdtScPriRestri(dtScManifestRecord.getDtScManifestId(), basedDtScManifest.getDtScManifestId(), basedBdtManifest.getBasedDtManifestId() != null);
        }

        LogRecord logRecord =
                logRepository.insertBdtLog(
                        bdtManifest,
                        bdt,
                        LogAction.Added,
                        userId, timestamp);
        bdtManifest.setLogId(logRecord.getLogId());
        bdtManifest.update(DT_MANIFEST.LOG_ID);

        if (StringUtils.hasLength(request.getTag())) {
            ULong dtManifestId = bdtManifest.getDtManifestId();
            dslContext.selectFrom(TAG)
                    .where(TAG.NAME.eq(request.getTag()))
                    .fetchOptionalInto(TagRecord.class)
                    .ifPresent(tagRecord -> {
                        DtManifestTagRecord dtManifestTagRecord = new DtManifestTagRecord();
                        dtManifestTagRecord.setDtManifestId(dtManifestId);
                        dtManifestTagRecord.setTagId(tagRecord.getTagId());
                        dtManifestTagRecord.setCreatedBy(userId);
                        dtManifestTagRecord.setCreationTimestamp(timestamp);
                        dslContext.insertInto(DT_MANIFEST_TAG)
                                .set(dtManifestTagRecord)
                                .execute();
                    });
        }

        return new CreateBdtRepositoryResponse(bdtManifest.getDtManifestId().toBigInteger());
    }

    private ULong findCdtSc(ULong dtScId, Map<ULong, ULong> map) {
        if (map.get(dtScId) == null) {
            return dtScId;
        }
        return findCdtSc(map.get(dtScId), map);
    }

    private void createBdtPriRestri(ULong dtManifestId, ULong basedDtManifestId, boolean isBdt) {
        if (isBdt) {
            dslContext.insertInto(BDT_PRI_RESTRI,
                    BDT_PRI_RESTRI.BDT_MANIFEST_ID,
                    BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID,
                    BDT_PRI_RESTRI.CODE_LIST_MANIFEST_ID,
                    BDT_PRI_RESTRI.AGENCY_ID_LIST_MANIFEST_ID,
                    BDT_PRI_RESTRI.IS_DEFAULT)
                    .select(dslContext.select(inline(dtManifestId),
                            BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID,
                            BDT_PRI_RESTRI.CODE_LIST_MANIFEST_ID,
                            BDT_PRI_RESTRI.AGENCY_ID_LIST_MANIFEST_ID,
                            BDT_PRI_RESTRI.IS_DEFAULT)
                            .from(BDT_PRI_RESTRI)
                            .where(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(basedDtManifestId))).execute();
        } else {
            dslContext.insertInto(BDT_PRI_RESTRI,
                    BDT_PRI_RESTRI.BDT_MANIFEST_ID,
                    BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID,
                    BDT_PRI_RESTRI.IS_DEFAULT)
                    .select(dslContext.select(inline(dtManifestId),
                            CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_XPS_TYPE_MAP_ID,
                            CDT_AWD_PRI.IS_DEFAULT)
                            .from(CDT_AWD_PRI)
                            .join(CDT_AWD_PRI_XPS_TYPE_MAP).on(CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_ID.eq(CDT_AWD_PRI.CDT_AWD_PRI_ID))
                            .join(DT_MANIFEST).on(CDT_AWD_PRI.CDT_ID.eq(DT_MANIFEST.DT_ID))
                            .where(DT_MANIFEST.DT_MANIFEST_ID.eq(basedDtManifestId))).execute();
        }
    }

    private void createBdtScPriRestri(ULong dtScManifestId, ULong basedDtScManifestId, boolean isBdt) {
        // insert BDT_SC_PRI_RESTRI
        if (isBdt) {
            dslContext.insertInto(BDT_SC_PRI_RESTRI,
                    BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID,
                    BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID,
                    BDT_SC_PRI_RESTRI.CODE_LIST_MANIFEST_ID,
                    BDT_SC_PRI_RESTRI.AGENCY_ID_LIST_MANIFEST_ID,
                    BDT_SC_PRI_RESTRI.IS_DEFAULT)
                    .select(dslContext.select(inline(dtScManifestId),
                            BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID,
                            BDT_SC_PRI_RESTRI.CODE_LIST_MANIFEST_ID,
                            BDT_SC_PRI_RESTRI.AGENCY_ID_LIST_MANIFEST_ID,
                            BDT_SC_PRI_RESTRI.IS_DEFAULT)
                            .from(BDT_SC_PRI_RESTRI)
                            .where(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID.eq(basedDtScManifestId))).execute();
        } else {
            dslContext.insertInto(BDT_SC_PRI_RESTRI,
                    BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID,
                    BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID,
                    BDT_SC_PRI_RESTRI.IS_DEFAULT)
                    .select(dslContext.select(inline(dtScManifestId),
                            CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID,
                            CDT_SC_AWD_PRI.IS_DEFAULT)
                            .from(CDT_SC_AWD_PRI)
                            .join(CDT_SC_AWD_PRI_XPS_TYPE_MAP).on(CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_ID.eq(CDT_SC_AWD_PRI.CDT_SC_AWD_PRI_ID))
                            .join(DT_SC_MANIFEST).on(CDT_SC_AWD_PRI.CDT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID))
                            .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(basedDtScManifestId))).execute();
        }
    }

    public ReviseDtRepositoryResponse reviseDt(ReviseDtRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        DtManifestRecord dtManifestRecord = dslContext.selectFrom(DT_MANIFEST)
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(
                        ULong.valueOf(request.getDtManifestId())
                ))
                .fetchOne();

        DtRecord prevDtRecord = dslContext.selectFrom(DT)
                .where(DT.DT_ID.eq(dtManifestRecord.getDtId()))
                .fetchOne();

        if (user.isDeveloper()) {
            if (!CcState.Published.equals(CcState.valueOf(prevDtRecord.getState()))) {
                throw new IllegalArgumentException("Only the core component in 'Published' state can be revised.");
            }
        } else {
            if (!CcState.Production.equals(CcState.valueOf(prevDtRecord.getState()))) {
                throw new IllegalArgumentException("Only the core component in 'Production' state can be revised.");
            }
        }

        if (dtManifestRecord.getBasedDtManifestId() == null) {
            throw new IllegalArgumentException("CDT can not be revised.");
        }

        ULong workingReleaseId = dslContext.select(RELEASE.RELEASE_ID)
                .from(RELEASE)
                .where(RELEASE.RELEASE_NUM.eq("Working"))
                .fetchOneInto(ULong.class);

        ULong targetReleaseId = dtManifestRecord.getReleaseId();
        if (user.isDeveloper()) {
            if (!targetReleaseId.equals(workingReleaseId)) {
                throw new IllegalArgumentException("It only allows to revise the component in 'Working' branch for developers.");
            }
        } else {
            if (targetReleaseId.equals(workingReleaseId)) {
                throw new IllegalArgumentException("It only allows to revise the component in non-'Working' branch for end-users.");
            }
        }

        boolean ownerIsDeveloper = dslContext.select(APP_USER.IS_DEVELOPER)
                .from(APP_USER)
                .where(APP_USER.APP_USER_ID.eq(prevDtRecord.getOwnerUserId()))
                .fetchOneInto(Boolean.class);

        if (user.isDeveloper() != ownerIsDeveloper) {
            throw new IllegalArgumentException("It only allows to revise the component for users in the same roles.");
        }

        // creates new bdt for revised record.
        DtRecord nextDtRecord = prevDtRecord.copy();
        nextDtRecord.setState(CcState.WIP.name());
        nextDtRecord.setCreatedBy(userId);
        nextDtRecord.setLastUpdatedBy(userId);
        nextDtRecord.setOwnerUserId(userId);
        nextDtRecord.setCreationTimestamp(timestamp);
        nextDtRecord.setLastUpdateTimestamp(timestamp);
        nextDtRecord.setPrevDtId(prevDtRecord.getDtId());
        nextDtRecord.setDtId(
                dslContext.insertInto(DT)
                        .set(nextDtRecord)
                        .returning(DT.DT_ID).fetchOne().getDtId()
        );

        prevDtRecord.setNextDtId(nextDtRecord.getDtId());
        prevDtRecord.update(DT.NEXT_DT_ID);

        // creates new log for revised record.
        LogRecord logRecord =
                logRepository.insertBdtLog(
                        dtManifestRecord,
                        nextDtRecord, dtManifestRecord.getLogId(),
                        LogAction.Revised,
                        userId, timestamp);

        ULong responseDtManifestId;
        dtManifestRecord.setDtId(nextDtRecord.getDtId());
        dtManifestRecord.setLogId(logRecord.getLogId());
        dtManifestRecord.update(DT_MANIFEST.DT_ID, DT_MANIFEST.LOG_ID);

        responseDtManifestId = dtManifestRecord.getDtManifestId();

        List<DtScManifestRecord> dtScManifestRecords = dslContext.selectFrom(DT_SC_MANIFEST)
                .where(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.eq(dtManifestRecord.getDtManifestId())).fetch();

        // revise DT_SCs
        for(DtScManifestRecord dtScManifestRecord : dtScManifestRecords) {
            DtScRecord prevDtSc = dslContext.selectFrom(DT_SC)
                    .where(DT_SC.DT_SC_ID.eq(dtScManifestRecord.getDtScId())).fetchOne();

            DtScRecord nextDtSc = prevDtSc.copy();

            nextDtSc.setOwnerDtId(nextDtRecord.getDtId());
            nextDtSc.setPrevDtScId(prevDtSc.getDtScId());
            nextDtSc.setDtScId(
                    dslContext.insertInto(DT_SC)
                            .set(nextDtSc)
                            .returning(DT_SC.DT_SC_ID).fetchOne().getDtScId()
            );

            prevDtSc.setNextDtScId(nextDtSc.getDtScId());
            prevDtSc.update(DT_SC.NEXT_DT_SC_ID);

            dtScManifestRecord.setDtScId(nextDtSc.getDtScId());
            dtScManifestRecord.update(DT_SC_MANIFEST.DT_SC_ID);
        }

        return new ReviseDtRepositoryResponse(responseDtManifestId.toBigInteger());
    }

    public UpdateDtPropertiesRepositoryResponse updateDtProperties(UpdateDtPropertiesRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        DtManifestRecord bdtManifestRecord = dslContext.selectFrom(DT_MANIFEST)
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(
                        ULong.valueOf(request.getDtManifestId())
                ))
                .fetchOne();

        DtRecord originalDtRecord = dslContext.selectFrom(DT)
                .where(DT.DT_ID.eq(bdtManifestRecord.getDtId()))
                .fetchOne();

        if (!CcState.WIP.equals(CcState.valueOf(originalDtRecord.getState()))) {
            throw new IllegalArgumentException("Only the core component in 'WIP' state can be modified.");
        }

        if (!originalDtRecord.getOwnerUserId().equals(userId)) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        // update bdt record.
        UpdateSetFirstStep<DtRecord> firstStep = dslContext.update(DT);
        UpdateSetMoreStep<DtRecord> moreStep = null;
        boolean denChanged = false;
        String newDen = null;
        if (compare(originalDtRecord.getQualifier_(), request.getQualifier()) != 0) {
            if (StringUtils.hasLength(request.getQualifier())) {
                newDen = request.getQualifier() + "_ " + originalDtRecord.getRepresentationTerm() + ". Type";
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .set(DT.QUALIFIER, request.getQualifier())
                        .set(DT.DEN, newDen);
            } else {
                newDen = originalDtRecord.getRepresentationTerm() + ". Type";
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .setNull(DT.QUALIFIER)
                        .set(DT.DEN, newDen);
            }
            denChanged = true;
        }
        if (compare(originalDtRecord.getSixDigitId(), request.getSixDigitId()) != 0) {
            DtRecord exist = dslContext.selectFrom(DT)
                    .where(and(DT.GUID.notEqual(originalDtRecord.getGuid()),
                            DT.SIX_DIGIT_ID.eq(request.getSixDigitId()))).fetchOne();
            if (exist != null) {
                throw new IllegalArgumentException("Six Digit Id '" + request.getSixDigitId() + "' already exist.");
            }
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(DT.SIX_DIGIT_ID, request.getSixDigitId());
        }
        if (compare(originalDtRecord.getContentComponentDefinition(), request.getContentComponentDefinition()) != 0) {
            if (StringUtils.hasLength(request.getContentComponentDefinition())) {
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .set(DT.CONTENT_COMPONENT_DEFINITION, request.getContentComponentDefinition());
            } else {
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .setNull(DT.CONTENT_COMPONENT_DEFINITION);
            }
        }
        if (compare(originalDtRecord.getDefinition(), request.getDefinition()) != 0) {
            if (StringUtils.hasLength(request.getDefinition())) {
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .set(DT.DEFINITION, request.getDefinition());
            } else {
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .setNull(DT.DEFINITION);
            }
        }
        if (compare(originalDtRecord.getDefinitionSource(), request.getDefinitionSource()) != 0) {
            if (StringUtils.hasLength(request.getDefinitionSource())) {
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .set(DT.DEFINITION_SOURCE, request.getDefinitionSource());
            } else {
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .setNull(DT.DEFINITION_SOURCE);
            }
        }
        if ((originalDtRecord.getIsDeprecated() == 1) != request.isDeprecated()) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(DT.IS_DEPRECATED, (byte) ((request.isDeprecated()) ? 1 : 0));
        }
        if (request.getNamespaceId() == null || request.getNamespaceId().longValue() <= 0L) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .setNull(DT.NAMESPACE_ID);
        } else {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(DT.NAMESPACE_ID, ULong.valueOf(request.getNamespaceId()));
        }

        if (moreStep != null) {
            moreStep.set(DT.LAST_UPDATED_BY, userId)
                    .set(DT.LAST_UPDATE_TIMESTAMP, timestamp)
                    .where(DT.DT_ID.eq(bdtManifestRecord.getDtId()))
                    .execute();

            DtRecord dtRecord = dslContext.selectFrom(DT)
                    .where(DT.DT_ID.eq(bdtManifestRecord.getDtId()))
                    .fetchOne();

            updateBdtPriList(bdtManifestRecord.getDtManifestId(), request.getBdtPriRestriList());

            // creates new log for updated record.
            LogRecord logRecord =
                    logRepository.insertBdtLog(
                            bdtManifestRecord,
                            dtRecord, bdtManifestRecord.getLogId(),
                            LogAction.Modified,
                            userId, timestamp);

            bdtManifestRecord.setLogId(logRecord.getLogId());
            bdtManifestRecord.update(DT_MANIFEST.LOG_ID);

            for (DtManifestRecord derivedDtManifestRecord : dslContext.selectFrom(DT_MANIFEST)
                    .where(DT_MANIFEST.BASED_DT_MANIFEST_ID.eq(bdtManifestRecord.getDtManifestId()))
                    .fetch()) {
                propagateUpdateDtRecord(originalDtRecord, derivedDtManifestRecord, request, user);
            }
        }

        if (denChanged) {
            for (Record3<ULong, ULong, String> bccp : dslContext.select(BCCP_MANIFEST.BCCP_MANIFEST_ID, BCCP.BCCP_ID, BCCP.PROPERTY_TERM)
                    .from(BCCP_MANIFEST)
                    .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                    .where(BCCP_MANIFEST.BDT_MANIFEST_ID.eq(bdtManifestRecord.getDtManifestId()))
                    .fetch()) {
                String newBccpDen = bccp.get(BCCP.PROPERTY_TERM) + ". " + newDen.replaceAll(". Type", "");
                dslContext.update(BCCP)
                        .set(BCCP.DEN, newBccpDen)
                        .where(BCCP.BCCP_ID.eq(bccp.get(BCCP.BCCP_ID)))
                        .execute();

                for (Record2<ULong, String> bcc : dslContext.select(BCC.BCC_ID, ACC.OBJECT_CLASS_TERM)
                        .from(BCC_MANIFEST)
                        .join(BCC).on(BCC_MANIFEST.BCC_ID.eq(BCC.BCC_ID))
                        .join(ACC_MANIFEST).on(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                        .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                        .where(BCC_MANIFEST.TO_BCCP_MANIFEST_ID.eq(bccp.get(BCCP_MANIFEST.BCCP_MANIFEST_ID)))
                        .fetch()) {
                    String newBccDen = bcc.get(ACC.OBJECT_CLASS_TERM) + ". " + newBccpDen;
                    dslContext.update(BCC)
                            .set(BCC.DEN, newBccDen)
                            .where(BCC.BCC_ID.eq(bcc.get(BCC.BCC_ID)))
                            .execute();
                }
            }
        }

        return new UpdateDtPropertiesRepositoryResponse(bdtManifestRecord.getDtManifestId().toBigInteger());
    }

    private void propagateUpdateDtRecord(DtRecord originalDtRecord,
                                         DtManifestRecord dtManifestRecord,
                                         UpdateDtPropertiesRepositoryRequest request,
                                         AppUser user) {
        DtRecord dtRecord = dslContext.selectFrom(DT)
                .where(DT.DT_ID.eq(dtManifestRecord.getDtId()))
                .fetchOne();

        UpdateSetFirstStep<DtRecord> firstStep = dslContext.update(DT);
        UpdateSetMoreStep<DtRecord> moreStep = null;
        if (compare(dtRecord.getContentComponentDefinition(), originalDtRecord.getContentComponentDefinition()) == 0) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(DT.CONTENT_COMPONENT_DEFINITION, request.getContentComponentDefinition());
        }
        if (compare(dtRecord.getDefinition(), originalDtRecord.getDefinition()) == 0) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(DT.DEFINITION, request.getDefinition());
        }
        if (compare(dtRecord.getDefinitionSource(), originalDtRecord.getDefinitionSource()) == 0) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(DT.DEFINITION_SOURCE, request.getDefinitionSource());
        }

        if (moreStep != null) {
            moreStep.where(DT.DT_ID.eq(dtRecord.getDtId()))
                    .execute();
        }

        for (DtManifestRecord derivedDtManifestRecord : dslContext.selectFrom(DT_MANIFEST)
                .where(DT_MANIFEST.BASED_DT_MANIFEST_ID.eq(dtManifestRecord.getDtManifestId()))
                .fetch()) {
            propagateUpdateDtRecord(originalDtRecord, derivedDtManifestRecord, request, user);
        }
    }

    private void deleteDerivedValueDomain(ULong basedDtManifestId, List<BdtPriRestriRecord> deleteList) {
        List<DtManifestRecord> derivedDtManifestList = dslContext.selectFrom(DT_MANIFEST)
                .where(DT_MANIFEST.BASED_DT_MANIFEST_ID.eq(basedDtManifestId)).fetch();

        List<ULong> cdtAwdPriXpsTypeMapIdList = deleteList.stream().filter(e -> e.getCdtAwdPriXpsTypeMapId() != null)
                .map(BdtPriRestriRecord::getCdtAwdPriXpsTypeMapId).collect(Collectors.toList());

        List<ULong> codeListManifestIdList = deleteList.stream().filter(e -> e.getCodeListManifestId() != null)
                .map(BdtPriRestriRecord::getCodeListManifestId).collect(Collectors.toList());

        List<ULong> agencyIdListManifestIdList = deleteList.stream().filter(e -> e.getAgencyIdListManifestId() != null)
                .map(BdtPriRestriRecord::getAgencyIdListManifestId).collect(Collectors.toList());

        for (DtManifestRecord dtManifest : derivedDtManifestList) {
            deleteDerivedValueDomain(dtManifest.getDtManifestId(), deleteList);

            dslContext.deleteFrom(BDT_PRI_RESTRI).where(
                            and(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(dtManifest.getDtManifestId())),
                            BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID.in(cdtAwdPriXpsTypeMapIdList))
                    .execute();
            dslContext.deleteFrom(BDT_PRI_RESTRI).where(
                            and(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(dtManifest.getDtManifestId())),
                            BDT_PRI_RESTRI.CODE_LIST_MANIFEST_ID.in(codeListManifestIdList))
                    .execute();
            dslContext.deleteFrom(BDT_PRI_RESTRI).where(
                            and(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(dtManifest.getDtManifestId())),
                            BDT_PRI_RESTRI.AGENCY_ID_LIST_MANIFEST_ID.in(agencyIdListManifestIdList))
                    .execute();

            BdtPriRestriRecord defaultRecord = dslContext.selectFrom(BDT_PRI_RESTRI).where(
                    and(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(dtManifest.getDtManifestId())),
                    BDT_PRI_RESTRI.IS_DEFAULT.eq((byte) 1)).fetchOne();

            if (defaultRecord == null) {
                BdtPriRestriRecord baseDefaultRecord = dslContext.selectFrom(BDT_PRI_RESTRI).where(and(
                        BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(basedDtManifestId),
                        BDT_PRI_RESTRI.IS_DEFAULT.eq((byte) 1))).fetchOne();

                if (baseDefaultRecord.getCdtAwdPriXpsTypeMapId() != null) {
                    dslContext.update(BDT_PRI_RESTRI).set(BDT_PRI_RESTRI.IS_DEFAULT, (byte) 1)
                            .where(and(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(dtManifest.getDtManifestId()),
                                    BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID.eq(baseDefaultRecord.getCdtAwdPriXpsTypeMapId())))
                            .execute();
                } else if (baseDefaultRecord.getCodeListManifestId() != null) {
                    dslContext.update(BDT_PRI_RESTRI).set(BDT_PRI_RESTRI.IS_DEFAULT, (byte) 1)
                            .where(and(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(dtManifest.getDtManifestId()),
                                    BDT_PRI_RESTRI.CODE_LIST_MANIFEST_ID.eq(baseDefaultRecord.getCodeListManifestId())))
                            .execute();
                } else {
                    dslContext.update(BDT_PRI_RESTRI).set(BDT_PRI_RESTRI.IS_DEFAULT, (byte) 1)
                            .where(and(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(dtManifest.getDtManifestId()),
                                    BDT_PRI_RESTRI.AGENCY_ID_LIST_MANIFEST_ID.eq(baseDefaultRecord.getAgencyIdListManifestId())))
                            .execute();
                }
            }
        }
    }

    private void updateDefaultValueDomainForDerivedDt(
            ULong dtManifestId, CcBdtPriRestri defaultValueDomain) {
        List<DtManifestRecord> derivedDtManifestList = dslContext.selectFrom(DT_MANIFEST).where(DT_MANIFEST.BASED_DT_MANIFEST_ID.eq(dtManifestId)).fetch();
        for (DtManifestRecord derivedDtManifest : derivedDtManifestList) {
            for (BdtPriRestriRecord bdtPriRestriRecord : dslContext.selectFrom(BDT_PRI_RESTRI)
                    .where(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(derivedDtManifest.getDtManifestId()))
                    .fetch()) {
                if (defaultValueDomain.getCdtAwdPriXpsTypeMapId() != null) {
                    if (bdtPriRestriRecord.getCdtAwdPriXpsTypeMapId() != null &&
                        bdtPriRestriRecord.getCdtAwdPriXpsTypeMapId().toBigInteger().equals(defaultValueDomain.getCdtAwdPriXpsTypeMapId())) {
                        bdtPriRestriRecord.setIsDefault((byte) 1);
                    } else {
                        bdtPriRestriRecord.setIsDefault((byte) 0);
                    }
                } else if (defaultValueDomain.getCodeListManifestId() != null) {
                    if (bdtPriRestriRecord.getCodeListManifestId() != null &&
                        bdtPriRestriRecord.getCodeListManifestId().toBigInteger().equals(defaultValueDomain.getCodeListManifestId())) {
                        bdtPriRestriRecord.setIsDefault((byte) 1);
                    } else {
                        bdtPriRestriRecord.setIsDefault((byte) 0);
                    }
                } else if (defaultValueDomain.getAgencyIdListManifestId() != null) {
                    if (bdtPriRestriRecord.getAgencyIdListManifestId() != null &&
                        bdtPriRestriRecord.getAgencyIdListManifestId().toBigInteger().equals(defaultValueDomain.getAgencyIdListManifestId())) {
                        bdtPriRestriRecord.setIsDefault((byte) 1);
                    } else {
                        bdtPriRestriRecord.setIsDefault((byte) 0);
                    }
                }
                bdtPriRestriRecord.update(BDT_PRI_RESTRI.IS_DEFAULT);
            }

            updateDefaultValueDomainForDerivedDt(derivedDtManifest.getDtManifestId(), defaultValueDomain);
        }
    }

    private void insertDerivedValueDomain(ULong dtManifestId, List<BdtPriRestriRecord> insertList) {
        List<DtManifestRecord> derivedDtManifestList = dslContext.selectFrom(DT_MANIFEST).where(DT_MANIFEST.BASED_DT_MANIFEST_ID.eq(dtManifestId)).fetch();

        List<ULong> cdtAwdPriXpsTypeMapIdList = insertList.stream().filter(e -> e.getCdtAwdPriXpsTypeMapId() != null)
                .map(BdtPriRestriRecord::getCdtAwdPriXpsTypeMapId).collect(Collectors.toList());

        List<ULong> codeListManifestIdList = insertList.stream().filter(e -> e.getCodeListManifestId() != null)
                .map(BdtPriRestriRecord::getCodeListManifestId).collect(Collectors.toList());

        List<ULong> agencyIdListManifestIdList = insertList.stream().filter(e -> e.getAgencyIdListManifestId() != null)
                .map(BdtPriRestriRecord::getAgencyIdListManifestId).collect(Collectors.toList());

        for (DtManifestRecord dtManifest : derivedDtManifestList) {
            for (ULong cdtAwdPriXpsTypeMapId: cdtAwdPriXpsTypeMapIdList) {
                BdtPriRestriRecord existRecord = dslContext.selectFrom(BDT_PRI_RESTRI).where(
                        and(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(dtManifest.getDtManifestId())),
                        BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID.eq(cdtAwdPriXpsTypeMapId)).fetchOne();
                if (existRecord == null) {
                    dslContext.insertInto(BDT_PRI_RESTRI)
                            .set(BDT_PRI_RESTRI.BDT_MANIFEST_ID, dtManifest.getDtManifestId())
                            .set(BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID, cdtAwdPriXpsTypeMapId)
                            .set(BDT_PRI_RESTRI.IS_DEFAULT, (byte) 0).execute();
                }
            }

            for (ULong codeListManifestId : codeListManifestIdList) {
                BdtPriRestriRecord existRecord = dslContext.selectFrom(BDT_PRI_RESTRI).where(
                        and(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(dtManifest.getDtManifestId())),
                        BDT_PRI_RESTRI.CODE_LIST_MANIFEST_ID.eq(codeListManifestId)).fetchOne();
                if (existRecord == null) {
                    dslContext.insertInto(BDT_PRI_RESTRI)
                            .set(BDT_PRI_RESTRI.BDT_MANIFEST_ID, dtManifest.getDtManifestId())
                            .set(BDT_PRI_RESTRI.CODE_LIST_MANIFEST_ID, codeListManifestId)
                            .set(BDT_PRI_RESTRI.IS_DEFAULT, (byte) 0).execute();
                }
            }

            for (ULong agencyIdListManifestId : agencyIdListManifestIdList) {
                BdtPriRestriRecord existRecord = dslContext.selectFrom(BDT_PRI_RESTRI).where(
                        and(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(dtManifest.getDtManifestId())),
                        BDT_PRI_RESTRI.AGENCY_ID_LIST_MANIFEST_ID.eq(agencyIdListManifestId)).fetchOne();
                if (existRecord == null) {
                    dslContext.insertInto(BDT_PRI_RESTRI)
                            .set(BDT_PRI_RESTRI.BDT_MANIFEST_ID, dtManifest.getDtManifestId())
                            .set(BDT_PRI_RESTRI.AGENCY_ID_LIST_MANIFEST_ID, agencyIdListManifestId)
                            .set(BDT_PRI_RESTRI.IS_DEFAULT, (byte) 0).execute();
                }
            }

            insertDerivedValueDomain(dtManifest.getDtManifestId(), insertList);
        }
    }

    private void updateBdtPriList(ULong dtManifestId, List<CcBdtPriRestri> list) {
        List<BdtPriRestriRecord> records = dslContext
                .selectFrom(BDT_PRI_RESTRI)
                .where(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(dtManifestId)).fetch();

        List<BdtPriRestriRecord> deleteList = new ArrayList<>();

        records.forEach(r -> {
            if (!list.stream().map(CcBdtPriRestri::getBdtPriRestriId).collect(Collectors.toList())
                    .contains(r.getBdtPriRestriId().toBigInteger())) {
                deleteList.add(r);
            }
        });

        deleteDerivedValueDomain(dtManifestId, deleteList);

        dslContext.deleteFrom(BDT_PRI_RESTRI).where(BDT_PRI_RESTRI.BDT_PRI_RESTRI_ID.in(
                deleteList.stream().map(BdtPriRestriRecord::getBdtPriRestriId).collect(Collectors.toList())))
                .execute();

        CcBdtPriRestri defaultValueDomain = null;

        List<BdtPriRestriRecord> insertedList = new ArrayList<>();

        for (CcBdtPriRestri restri : list) {
            if (restri.getBdtPriRestriId() == null) {
                // insert
                BdtPriRestriRecord newBdtPriRestri = new BdtPriRestriRecord();
                newBdtPriRestri.setIsDefault((byte) 0);
                newBdtPriRestri.setBdtManifestId(dtManifestId);
                if (restri.getType().equals(PrimitiveRestriType.CodeList)) {
                    newBdtPriRestri.setCodeListManifestId(ULong.valueOf(restri.getCodeListManifestId()));
                } else if (restri.getType().equals(PrimitiveRestriType.AgencyIdList)) {
                    newBdtPriRestri.setAgencyIdListManifestId(ULong.valueOf(restri.getAgencyIdListManifestId()));
                }
                restri.setBdtPriRestriId(dslContext.insertInto(BDT_PRI_RESTRI)
                        .set(newBdtPriRestri)
                        .returning(BDT_PRI_RESTRI.BDT_PRI_RESTRI_ID).fetchOne().getBdtPriRestriId().toBigInteger());

                insertedList.add(newBdtPriRestri);
            } else {
                // update
                BdtPriRestriRecord bdtPriRestriRecord = dslContext.selectFrom(BDT_PRI_RESTRI)
                        .where(BDT_PRI_RESTRI.BDT_PRI_RESTRI_ID.eq(ULong.valueOf(restri.getBdtPriRestriId())))
                        .fetchOne();

                if (restri.getCdtAwdPriXpsTypeMapId() != null) {
                    bdtPriRestriRecord.setCdtAwdPriXpsTypeMapId(ULong.valueOf(restri.getCdtAwdPriXpsTypeMapId()));
                    bdtPriRestriRecord.setCodeListManifestId(null);
                    bdtPriRestriRecord.setAgencyIdListManifestId(null);
                } else if (restri.getCodeListManifestId() != null) {
                    bdtPriRestriRecord.setCdtAwdPriXpsTypeMapId(null);
                    bdtPriRestriRecord.setCodeListManifestId(ULong.valueOf(restri.getCodeListManifestId()));
                    bdtPriRestriRecord.setAgencyIdListManifestId(null);
                } else if (restri.getAgencyIdListManifestId() != null) {
                    bdtPriRestriRecord.setCdtAwdPriXpsTypeMapId(null);
                    bdtPriRestriRecord.setCodeListManifestId(null);
                    bdtPriRestriRecord.setAgencyIdListManifestId(ULong.valueOf(restri.getAgencyIdListManifestId()));
                }

                bdtPriRestriRecord.setIsDefault((byte) (restri.isDefault() ? 1 : 0));
                bdtPriRestriRecord.update();

                if (bdtPriRestriRecord.getCdtAwdPriXpsTypeMapId() != null) {
                    restri.setCdtAwdPriXpsTypeMapId(bdtPriRestriRecord.getCdtAwdPriXpsTypeMapId().toBigInteger());
                    restri.setCodeListManifestId(null);
                    restri.setAgencyIdListManifestId(null);
                } else if (bdtPriRestriRecord.getCodeListManifestId() != null) {
                    restri.setCdtAwdPriXpsTypeMapId(null);
                    restri.setCodeListManifestId(bdtPriRestriRecord.getCodeListManifestId().toBigInteger());
                    restri.setAgencyIdListManifestId(null);
                } else if (bdtPriRestriRecord.getAgencyIdListManifestId() != null) {
                    restri.setCdtAwdPriXpsTypeMapId(null);
                    restri.setCodeListManifestId(null);
                    restri.setAgencyIdListManifestId(bdtPriRestriRecord.getAgencyIdListManifestId().toBigInteger());
                }
            }

            if (restri.isDefault()) {
                defaultValueDomain = restri;
            }
        }

        if (defaultValueDomain == null) {
            throw new IllegalArgumentException("Default Value Domain required.");
        }

        BigInteger defaultValueDomainId = defaultValueDomain.getBdtPriRestriId();
        dslContext.update(BDT_PRI_RESTRI).set(BDT_PRI_RESTRI.IS_DEFAULT, (byte) 0)
                .where(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(dtManifestId)).execute();

        dslContext.update(BDT_PRI_RESTRI).set(BDT_PRI_RESTRI.IS_DEFAULT, (byte) 1)
                .where(BDT_PRI_RESTRI.BDT_PRI_RESTRI_ID.eq(ULong.valueOf(defaultValueDomainId))).execute();

        insertDerivedValueDomain(dtManifestId, insertedList);
        updateDefaultValueDomainForDerivedDt(dtManifestId, defaultValueDomain);
    }

    public UpdateDtStateRepositoryResponse updateDtState(UpdateDtStateRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        DtManifestRecord dtManifestRecord = dslContext.selectFrom(DT_MANIFEST)
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(
                        ULong.valueOf(request.getDtManifestId())
                ))
                .fetchOne();

        DtRecord dtRecord = dslContext.selectFrom(DT)
                .where(DT.DT_ID.eq(dtManifestRecord.getDtId()))
                .fetchOne();

        CcState prevState = CcState.valueOf(dtRecord.getState());
        CcState nextState = request.getToState();

        if (prevState != request.getFromState()) {
            throw new IllegalArgumentException("Target core component is not in '" + request.getFromState() + "' state.");
        }

        if (!prevState.canMove(nextState)) {
            throw new IllegalArgumentException("The core component in '" + prevState + "' state cannot move to '" + nextState + "' state.");
        }

        // Change owner of CC when it restored.
        if (prevState == CcState.Deleted && nextState == CcState.WIP) {
            dtRecord.setOwnerUserId(userId);
        } else if (prevState != CcState.Deleted && !dtRecord.getOwnerUserId().equals(userId)
                && !prevState.canForceMove(request.getToState())) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        } else if (dtRecord.getNamespaceId() == null) {
            throw new IllegalArgumentException("'" + dtRecord.getDen() + "' dose not have NamespaceId.");
        }

        // update dt state.
        dtRecord.setState(nextState.name());
        if (!prevState.canForceMove(request.getToState())) {
            dtRecord.setLastUpdatedBy(userId);
            dtRecord.setLastUpdateTimestamp(timestamp);
        }
        dtRecord.update(DT.STATE,
                DT.LAST_UPDATED_BY, DT.LAST_UPDATE_TIMESTAMP, DT.OWNER_USER_ID);

        // creates new log for updated record.
        LogAction logAction = (CcState.Deleted == prevState && CcState.WIP == nextState)
                ? LogAction.Restored : LogAction.Modified;
        LogRecord logRecord =
                logRepository.insertBdtLog(
                        dtManifestRecord,
                        dtRecord, dtManifestRecord.getLogId(),
                        logAction,
                        userId, timestamp);

        dtManifestRecord.setLogId(logRecord.getLogId());
        dtManifestRecord.update(DT_MANIFEST.LOG_ID);

        return new UpdateDtStateRepositoryResponse(dtManifestRecord.getDtManifestId().toBigInteger());
    }

    public DeleteDtRepositoryResponse deleteDt(DeleteDtRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        DtManifestRecord bdtManifestRecord = dslContext.selectFrom(DT_MANIFEST)
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(
                        ULong.valueOf(request.getDtManifestId())
                ))
                .fetchOne();

        DtRecord bdtRecord = dslContext.selectFrom(DT)
                .where(DT.DT_ID.eq(bdtManifestRecord.getDtId()))
                .fetchOne();

        if (!CcState.WIP.equals(CcState.valueOf(bdtRecord.getState()))) {
            throw new IllegalArgumentException("Only the core component in 'WIP' state can be deleted.");
        }

        if (!bdtRecord.getOwnerUserId().equals(userId)) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        // update bdt state.
        bdtRecord.setState(CcState.Deleted.name());
        bdtRecord.setLastUpdatedBy(userId);
        bdtRecord.setLastUpdateTimestamp(timestamp);
        bdtRecord.update(DT.STATE,
                DT.LAST_UPDATED_BY, DT.LAST_UPDATE_TIMESTAMP);

        // creates new log for deleted record.
        LogRecord logRecord =
                logRepository.insertBdtLog(
                        bdtManifestRecord,
                        bdtRecord, bdtManifestRecord.getLogId(),
                        LogAction.Deleted,
                        userId, timestamp);

        bdtManifestRecord.setLogId(logRecord.getLogId());
        bdtManifestRecord.update(DT_MANIFEST.LOG_ID);

        return new DeleteDtRepositoryResponse(bdtManifestRecord.getDtManifestId().toBigInteger());
    }

    public PurgeDtRepositoryResponse purgeDt(PurgeDtRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        DtManifestRecord dtManifestRecord = dslContext.selectFrom(DT_MANIFEST)
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(
                        ULong.valueOf(request.getDtManifestId())
                ))
                .fetchOne();

        DtRecord dtRecord = dslContext.selectFrom(DT)
                .where(DT.DT_ID.eq(dtManifestRecord.getDtId()))
                .fetchOne();

        if (!CcState.Deleted.equals(CcState.valueOf(dtRecord.getState()))) {
            IllegalArgumentException e = new IllegalArgumentException("Only the core component in 'Deleted' state can be purged.");
            if (request.isIgnoreOnError()) {
                return new PurgeDtRepositoryResponse(dtManifestRecord.getDtManifestId().toBigInteger(), e);
            } else {
                throw e;
            }
        }

        List<DtManifestRecord> derivationDtManifestRecords = dslContext.selectFrom(DT_MANIFEST)
                .where(DT_MANIFEST.BASED_DT_MANIFEST_ID.eq(dtManifestRecord.getDtManifestId()))
                .fetch();
        if (!derivationDtManifestRecords.isEmpty()) {
            IllegalArgumentException e = new IllegalArgumentException("Please purge derivations first before purging the DT '" + dtRecord.getDen() + "'.");
            if (request.isIgnoreOnError()) {
                return new PurgeDtRepositoryResponse(dtManifestRecord.getDtManifestId().toBigInteger(), e);
            } else {
                throw e;
            }
        }

        List<BccpManifestRecord> bccpManifestRecords = dslContext.selectFrom(BCCP_MANIFEST)
                .where(BCCP_MANIFEST.BDT_MANIFEST_ID.eq(dtManifestRecord.getDtManifestId()))
                .fetch();
        if (!bccpManifestRecords.isEmpty()) {
            IllegalArgumentException e = new IllegalArgumentException("Please purge related-BCCPs first before purging the DT '" + dtRecord.getDen() + "'.");
            if (request.isIgnoreOnError()) {
                return new PurgeDtRepositoryResponse(dtManifestRecord.getDtManifestId().toBigInteger(), e);
            } else {
                throw e;
            }
        }

        // discard Log
        ULong logId = dtManifestRecord.getLogId();
        dslContext.update(DT_MANIFEST)
                .setNull(DT_MANIFEST.LOG_ID)
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(dtManifestRecord.getDtManifestId()))
                .execute();

        dslContext.update(LOG)
                .setNull(LOG.PREV_LOG_ID)
                .setNull(LOG.NEXT_LOG_ID)
                .where(LOG.REFERENCE.eq(dtRecord.getGuid()))
                .execute();

        dslContext.deleteFrom(LOG)
                .where(LOG.REFERENCE.eq(dtRecord.getGuid()))
                .execute();

        // discard DT_SCs
        List<DtScManifestRecord> dtScManifestRecords = dslContext.selectFrom(DT_SC_MANIFEST)
                .where(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.eq(dtManifestRecord.getDtManifestId()))
                .fetch();

        if (!dtScManifestRecords.isEmpty()) {
            // discard BDT_SC_PRI_RESTRIs
            List<BdtScPriRestriRecord> bdtScPriRestriRecords = dslContext.selectFrom(BDT_SC_PRI_RESTRI)
                    .where(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID.in(dtScManifestRecords.stream().map(e -> e.getDtScManifestId()).collect(Collectors.toList())))
                    .fetch();

            if (!bdtScPriRestriRecords.isEmpty()) {
                List<ULong> bdtScManifestIdList = bdtScPriRestriRecords.stream().map(e -> e.getBdtScManifestId()).collect(Collectors.toList());
                List<ULong> bdtScIdList = dslContext.select(DT_SC_MANIFEST.DT_SC_ID).from(DT_SC_MANIFEST)
                        .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.in(bdtScManifestIdList)).fetchInto(ULong.class);

                dslContext.deleteFrom(BDT_SC_PRI_RESTRI)
                        .where(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID.in(bdtScManifestIdList))
                        .execute();

                List<CdtScAwdPriRecord> cdtScAwdPriRecords = dslContext.selectFrom(CDT_SC_AWD_PRI)
                        .where(CDT_SC_AWD_PRI.CDT_SC_ID.in(bdtScIdList))
                        .fetch();
                if (!cdtScAwdPriRecords.isEmpty()) {
                    dslContext.deleteFrom(CDT_SC_AWD_PRI_XPS_TYPE_MAP)
                            .where(CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_ID.in(
                                    cdtScAwdPriRecords.stream().map(e -> e.getCdtScAwdPriId()).collect(Collectors.toList())))
                            .execute();

                    dslContext.deleteFrom(CDT_SC_AWD_PRI)
                            .where(CDT_SC_AWD_PRI.CDT_SC_ID.in(bdtScIdList))
                            .execute();
                }
            }

            dslContext.deleteFrom(DT_SC_MANIFEST)
                    .where(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.eq(dtManifestRecord.getDtManifestId()))
                    .execute();

            dslContext.deleteFrom(DT_SC)
                    .where(DT_SC.DT_SC_ID.in(dtScManifestRecords.stream().map(e -> e.getDtScId()).collect(Collectors.toList())))
                    .execute();
        }

        // discard assigned DT in modules
        dslContext.deleteFrom(MODULE_DT_MANIFEST)
                .where(MODULE_DT_MANIFEST.DT_MANIFEST_ID.eq(dtManifestRecord.getDtManifestId()))
                .execute();

        List<BdtPriRestriRecord> bdtPriRestriRecords = dslContext.selectFrom(BDT_PRI_RESTRI)
                .where(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(dtManifestRecord.getDtManifestId()))
                .fetch();
        if (!bdtPriRestriRecords.isEmpty()) {
            dslContext.deleteFrom(BDT_PRI_RESTRI)
                    .where(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(dtManifestRecord.getDtManifestId()))
                    .execute();

            List<CdtAwdPriRecord> cdtAwdPriRecords = dslContext.selectFrom(CDT_AWD_PRI)
                    .where(CDT_AWD_PRI.CDT_ID.eq(dtRecord.getDtId()))
                    .fetch();
            if (!cdtAwdPriRecords.isEmpty()) {
                dslContext.deleteFrom(CDT_AWD_PRI_XPS_TYPE_MAP)
                        .where(CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_ID.in(cdtAwdPriRecords.stream().map(e -> e.getCdtAwdPriId()).collect(Collectors.toList())))
                        .execute();

                dslContext.deleteFrom(CDT_AWD_PRI)
                        .where(CDT_AWD_PRI.CDT_ID.eq(dtRecord.getDtId()))
                        .execute();
            }
        }

        // discard corresponding tags
        dslContext.deleteFrom(DT_MANIFEST_TAG)
                .where(DT_MANIFEST_TAG.DT_MANIFEST_ID.eq(dtManifestRecord.getDtManifestId()))
                .execute();

        // discard DT
        dslContext.deleteFrom(DT_MANIFEST)
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(dtManifestRecord.getDtManifestId()))
                .execute();

        dslContext.deleteFrom(DT)
                .where(DT.DT_ID.eq(dtRecord.getDtId()))
                .execute();

        return new PurgeDtRepositoryResponse(dtManifestRecord.getDtManifestId().toBigInteger());
    }

    public UpdateDtOwnerRepositoryResponse updateDtOwner(UpdateDtOwnerRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        DtManifestRecord bdtManifestRecord = dslContext.selectFrom(DT_MANIFEST)
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(
                        ULong.valueOf(request.getDtManifestId())
                ))
                .fetchOne();

        DtRecord bdtRecord = dslContext.selectFrom(DT)
                .where(DT.DT_ID.eq(bdtManifestRecord.getDtId()))
                .fetchOne();

        if (!CcState.WIP.equals(CcState.valueOf(bdtRecord.getState()))) {
            throw new IllegalArgumentException("Only the core component in 'WIP' state can be modified.");
        }

        if (!bdtRecord.getOwnerUserId().equals(userId)) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        bdtRecord.setOwnerUserId(ULong.valueOf(request.getOwnerId()));
        bdtRecord.setLastUpdatedBy(userId);
        bdtRecord.setLastUpdateTimestamp(timestamp);
        bdtRecord.update(DT.OWNER_USER_ID, DT.LAST_UPDATED_BY, DT.LAST_UPDATE_TIMESTAMP);

        LogRecord logRecord =
                logRepository.insertBdtLog(
                        bdtManifestRecord,
                        bdtRecord, bdtManifestRecord.getLogId(),
                        LogAction.Modified,
                        userId, timestamp);

        bdtManifestRecord.setLogId(logRecord.getLogId());
        bdtManifestRecord.update(DT_MANIFEST.LOG_ID);

        return new UpdateDtOwnerRepositoryResponse(bdtManifestRecord.getDtManifestId().toBigInteger());
    }

    public CancelRevisionDtRepositoryResponse cancelRevisionDt(CancelRevisionDtRepositoryRequest request) {
        ULong userId = ULong.valueOf(sessionService.userId(request.getUser()));
        LocalDateTime timestamp = request.getLocalDateTime();

        DtManifestRecord dtManifestRecord = dslContext.selectFrom(DT_MANIFEST)
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(ULong.valueOf(request.getDtManifestId()))).fetchOne();

        List<DtScManifestRecord> dtScManifestRecords = dslContext.selectFrom(DT_SC_MANIFEST)
                .where(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.eq(dtManifestRecord.getDtManifestId())).fetch();

        if (dtManifestRecord == null) {
            throw new IllegalArgumentException("Not found a target DT");
        }

        DtRecord bdtRecord = dslContext.selectFrom(DT)
                .where(DT.DT_ID.eq(dtManifestRecord.getDtId())).fetchOne();

        if (bdtRecord.getPrevDtId() == null) {
            throw new IllegalArgumentException("Not found previous log");
        }

        DtRecord prevDtRecord = dslContext.selectFrom(DT)
                .where(DT.DT_ID.eq(bdtRecord.getPrevDtId())).fetchOne();

        dslContext.deleteFrom(BDT_PRI_RESTRI)
                .where(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(dtManifestRecord.getDtManifestId())).execute();

        // unlink prev DT
        prevDtRecord.setNextDtId(null);
        prevDtRecord.update(DT.NEXT_DT_ID);

        // remove revised DT_SCs
        for(DtScManifestRecord dtScManifestRecord : dtScManifestRecords) {
            dslContext.deleteFrom(BDT_SC_PRI_RESTRI)
                    .where(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID.eq(dtScManifestRecord.getDtScManifestId())).execute();

            DtScRecord currentDtSc = dslContext.selectFrom(DT_SC)
                    .where(DT_SC.DT_SC_ID.eq(dtScManifestRecord.getDtScId())).fetchOne();

            DtScRecord prevDtSc = dslContext.selectFrom(DT_SC)
                    .where(DT_SC.DT_SC_ID.eq(currentDtSc.getPrevDtScId())).fetchOne();

            prevDtSc.setNextDtScId(null);
            prevDtSc.update(DT_SC.NEXT_DT_SC_ID);

            dtScManifestRecord.setDtScId(prevDtSc.getDtScId());
            dtScManifestRecord.update(DT_SC_MANIFEST.DT_SC_ID);

            currentDtSc.delete();
        }

        // clean logs up
        logRepository.revertToStableState(dtManifestRecord);

        dtManifestRecord.setDtId(prevDtRecord.getDtId());
        dtManifestRecord.update(DT_MANIFEST.DT_ID);

        // delete current DT
        bdtRecord.delete();

        return new CancelRevisionDtRepositoryResponse(request.getDtManifestId());
    }

    private void createDtScForDerived(ULong dtManifestId, DtScManifestRecord dtScManifestRecord, DtScRecord dtScRecord) {

        DtManifestRecord ownerDtManifest = dslContext.selectFrom(DT_MANIFEST)
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(dtManifestId)).fetchOne();

        DtRecord targetDtRecord = dslContext.selectFrom(DT)
                .where(DT.DT_ID.eq(ownerDtManifest.getDtId())).fetchOne();

        List<BdtScPriRestriRecord> bdtScPriRestriRecords = dslContext.selectFrom(BDT_SC_PRI_RESTRI)
                .where(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID.eq(dtScManifestRecord.getDtScManifestId()))
                .fetch();

        dtScRecord.setBasedDtScId(dtScRecord.getDtScId());
        dtScRecord.setDtScId(null);
        dtScRecord.setGuid(ScoreGuid.randomGuid());
        dtScRecord.setOwnerDtId(targetDtRecord.getDtId());

        dtScRecord.setDtScId(
                dslContext.insertInto(DT_SC)
                        .set(dtScRecord)
                        .returning(DT_SC.DT_SC_ID).fetchOne().getDtScId()
        );

        dtScManifestRecord.setDtScId(dtScRecord.getDtScId());
        dtScManifestRecord.setBasedDtScManifestId(dtScManifestRecord.getDtScManifestId());
        dtScManifestRecord.setOwnerDtManifestId(ownerDtManifest.getDtManifestId());
        dtScManifestRecord.setDtScManifestId(null);

        dtScManifestRecord.setDtScManifestId(
                dslContext.insertInto(DT_SC_MANIFEST)
                        .set(dtScManifestRecord)
                        .returning(DT_SC_MANIFEST.DT_SC_MANIFEST_ID).fetchOne().getDtScManifestId()
        );

        for (BdtScPriRestriRecord bdtScPriRestriRecord : bdtScPriRestriRecords) {
            bdtScPriRestriRecord = bdtScPriRestriRecord.copy();
            bdtScPriRestriRecord.setBdtScPriRestriId(null);
            bdtScPriRestriRecord.setBdtScManifestId(dtScManifestRecord.getDtScManifestId());
            dslContext.insertInto(BDT_SC_PRI_RESTRI)
                    .set(bdtScPriRestriRecord)
                    .execute();
        }

        dslContext.selectFrom(DT_MANIFEST).where(DT_MANIFEST.BASED_DT_MANIFEST_ID.eq(dtManifestId))
                .fetchStream().forEach(record -> createDtScForDerived(record.getDtManifestId(), dtScManifestRecord, dtScRecord));
    }

    private void insertBdtScPriRestriByRepresentationTerm(ULong bdtScManifestId, String representationTerm) {
        ULong bdtScId = dslContext.select(DT_SC_MANIFEST.DT_SC_ID).from(DT_SC_MANIFEST)
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(bdtScManifestId)).fetchOneInto(ULong.class);

        DtRecord cdtRecord = dslContext.selectFrom(DT)
                .where(and(
                        DT.BASED_DT_ID.isNull(),
                        DT.DATA_TYPE_TERM.eq(representationTerm)))
                .fetchOne();

        List<CdtAwdPriRecord> cdtAwdPriRecords = dslContext.selectFrom(CDT_AWD_PRI)
                .where(CDT_AWD_PRI.CDT_ID.eq(cdtRecord.getDtId()))
                .fetch();

        Map<ULong, List<CdtScAwdPriRecord>> cdtScAwdPriRecordMapByCdtAwdPriId = new HashMap();
        for (CdtAwdPriRecord cdtAwdPriRecord : cdtAwdPriRecords) {
            CdtScAwdPriRecord cdtScAwdPriRecord = new CdtScAwdPriRecord();
            cdtScAwdPriRecord.setCdtPriId(cdtAwdPriRecord.getCdtPriId());
            cdtScAwdPriRecord.setCdtScId(bdtScId);
            cdtScAwdPriRecord.setIsDefault(cdtAwdPriRecord.getIsDefault());
            cdtScAwdPriRecord.setCdtScAwdPriId(
                    dslContext.insertInto(CDT_SC_AWD_PRI)
                            .set(cdtScAwdPriRecord)
                            .returning(CDT_SC_AWD_PRI.CDT_SC_AWD_PRI_ID)
                            .fetchOne().getCdtScAwdPriId()
            );

            if (!cdtScAwdPriRecordMapByCdtAwdPriId.containsKey(cdtAwdPriRecord.getCdtAwdPriId())) {
                cdtScAwdPriRecordMapByCdtAwdPriId.put(cdtAwdPriRecord.getCdtAwdPriId(), new ArrayList());
            }

            cdtScAwdPriRecordMapByCdtAwdPriId.get(cdtAwdPriRecord.getCdtAwdPriId()).add(cdtScAwdPriRecord);
        }

        List<CdtAwdPriXpsTypeMapRecord> cdtAwdPriXpsTypeMapRecords = dslContext.selectFrom(CDT_AWD_PRI_XPS_TYPE_MAP)
                .where(CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_ID.in(
                        cdtAwdPriRecords.stream().map(e -> e.getCdtAwdPriId()).collect(Collectors.toList())
                ))
                .fetch();

        List<CdtScAwdPriXpsTypeMapRecord> cdtScAwdPriXpsTypeMapRecords = new ArrayList();
        for (CdtAwdPriXpsTypeMapRecord cdtAwdPriXpsTypeMapRecord : cdtAwdPriXpsTypeMapRecords) {
            for (CdtScAwdPriRecord cdtScAwdPriRecord : cdtScAwdPriRecordMapByCdtAwdPriId.get(cdtAwdPriXpsTypeMapRecord.getCdtAwdPriId())) {
                CdtScAwdPriXpsTypeMapRecord cdtScAwdPriXpsTypeMapRecord = new CdtScAwdPriXpsTypeMapRecord();
                cdtScAwdPriXpsTypeMapRecord.setCdtScAwdPriId(cdtScAwdPriRecord.getCdtScAwdPriId());
                cdtScAwdPriXpsTypeMapRecord.setXbtId(cdtAwdPriXpsTypeMapRecord.getXbtId());
                cdtScAwdPriXpsTypeMapRecord.setCdtScAwdPriXpsTypeMapId(
                        dslContext.insertInto(CDT_SC_AWD_PRI_XPS_TYPE_MAP)
                                .set(cdtScAwdPriXpsTypeMapRecord)
                                .returning(CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID)
                                .fetchOne().getCdtScAwdPriXpsTypeMapId()
                );
                cdtScAwdPriXpsTypeMapRecords.add(cdtScAwdPriXpsTypeMapRecord);

                BdtScPriRestriRecord bdtScPriRestriRecord = new BdtScPriRestriRecord();
                bdtScPriRestriRecord.setBdtScManifestId(bdtScManifestId);
                bdtScPriRestriRecord.setCdtScAwdPriXpsTypeMapId(cdtScAwdPriXpsTypeMapRecord.getCdtScAwdPriXpsTypeMapId());
                bdtScPriRestriRecord.setIsDefault(cdtAwdPriXpsTypeMapRecord.getIsDefault());
                bdtScPriRestriRecord.setBdtScPriRestriId(
                        dslContext.insertInto(BDT_SC_PRI_RESTRI)
                                .set(bdtScPriRestriRecord)
                                .returning(BDT_SC_PRI_RESTRI.BDT_SC_PRI_RESTRI_ID).fetchOne().getBdtScPriRestriId()
                );
            }
        }
    }

    private String getUniquePropertyTerm() {
        List<String> propertyTerms = dslContext.select(DT_SC.PROPERTY_TERM)
                .from(DT_SC)
                .where(DT_SC.PROPERTY_TERM.like("Property Term%"))
                .fetchInto(String.class);

        List<Integer> existingNumbers = propertyTerms.stream().filter(e -> e.startsWith("Property Term "))
                .map(e -> e.substring("Property Term ".length()))
                .map(e -> {
                    try {
                        return Integer.parseInt(e.trim());
                    } catch (NumberFormatException ex) {
                        return 0;
                    }
                }).collect(Collectors.toList());

        int propertyTermNum = 1;
        while (true) {
            if (!existingNumbers.contains(propertyTermNum)) {
                break;
            }
            propertyTermNum++;
        }

        return "Property Term " + propertyTermNum;
    }

    public CreateDtScRepositoryResponse createDtSc(CreateDtScRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        DtManifestRecord ownerDtManifest = dslContext.selectFrom(DT_MANIFEST)
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(ULong.valueOf(request.getOwnerDdtManifestId()))).fetchOne();

        DtRecord targetDtRecord = dslContext.selectFrom(DT)
                .where(DT.DT_ID.eq(ownerDtManifest.getDtId())).fetchOne();

        DtScRecord dtScRecord = new DtScRecord();
        dtScRecord.setGuid(ScoreGuid.randomGuid());
        dtScRecord.setObjectClassTerm(targetDtRecord.getDataTypeTerm());
        dtScRecord.setPropertyTerm(getUniquePropertyTerm());

        String defaultRepresentationTerm = "Amount";
        dtScRecord.setRepresentationTerm(defaultRepresentationTerm);
        dtScRecord.setOwnerDtId(ownerDtManifest.getDtId());
        dtScRecord.setCardinalityMin(0);
        dtScRecord.setCardinalityMax(1);
        dtScRecord.setCreatedBy(ULong.valueOf(user.getAppUserId()));
        dtScRecord.setLastUpdatedBy(ULong.valueOf(user.getAppUserId()));
        dtScRecord.setOwnerUserId(ULong.valueOf(user.getAppUserId()));
        dtScRecord.setCreationTimestamp(request.getLocalDateTime());
        dtScRecord.setLastUpdateTimestamp(request.getLocalDateTime());

        dtScRecord.setDtScId(
                dslContext.insertInto(DT_SC)
                        .set(dtScRecord)
                        .returning(DT_SC.DT_SC_ID).fetchOne().getDtScId()
        );

        DtScManifestRecord dtScManifestRecord = new DtScManifestRecord();
        dtScManifestRecord.setDtScId(dtScRecord.getDtScId());
        dtScManifestRecord.setReleaseId(ownerDtManifest.getReleaseId());
        dtScManifestRecord.setOwnerDtManifestId(ownerDtManifest.getDtManifestId());

        dtScManifestRecord.setDtScManifestId(
                dslContext.insertInto(DT_SC_MANIFEST)
                        .set(dtScManifestRecord)
                        .returning(DT_SC_MANIFEST.DT_SC_MANIFEST_ID).fetchOne().getDtScManifestId()
        );
        insertBdtScPriRestriByRepresentationTerm(dtScManifestRecord.getDtScManifestId(), defaultRepresentationTerm);

        // creates new log for updated record.
        LogRecord logRecord =
                logRepository.insertBdtLog(
                        ownerDtManifest,
                        targetDtRecord, ownerDtManifest.getLogId(),
                        LogAction.Modified,
                        userId, timestamp);

        ownerDtManifest.setLogId(logRecord.getLogId());
        ownerDtManifest.update(DT_MANIFEST.LOG_ID);

        dslContext.selectFrom(DT_MANIFEST).where(DT_MANIFEST.BASED_DT_MANIFEST_ID.eq(ownerDtManifest.getDtManifestId()))
                .fetchStream().forEach(record -> createDtScForDerived(record.getDtManifestId(), dtScManifestRecord, dtScRecord));

        return new CreateDtScRepositoryResponse(dtScManifestRecord.getDtScManifestId().toBigInteger());
    }

    public void addDtPrimitiveRestriction(CreatePrimitiveRestrictionRepositoryRequest request) {

        DtManifestRecord dtManifestRecord = dslContext.selectFrom(DT_MANIFEST)
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(ULong.valueOf(request.getDtManifestId())))
                .fetchOne();

        List<BdtPriRestriRecord> bdtPriRestriRecords = dslContext.selectFrom(BDT_PRI_RESTRI)
                .where(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(dtManifestRecord.getDtManifestId()))
                .fetch();

        CdtPriRecord cdtPriRecord = dslContext.selectFrom(CDT_PRI)
                .where(CDT_PRI.NAME.eq(request.getPrimitive()))
                .fetchOne();

        CdtAwdPriRecord cdtAwdPriRecord = new CdtAwdPriRecord();
        cdtAwdPriRecord.setCdtId(dtManifestRecord.getDtId());
        cdtAwdPriRecord.setCdtPriId(cdtPriRecord.getCdtPriId());
        cdtAwdPriRecord.setIsDefault((byte) 0);

        cdtAwdPriRecord.setCdtAwdPriId(dslContext.insertInto(CDT_AWD_PRI)
                .set(cdtAwdPriRecord).returning(CDT_AWD_PRI.CDT_AWD_PRI_ID)
                .fetchOne().getCdtAwdPriId());

        boolean isDefault = bdtPriRestriRecords.size() == 0;

        for (BigInteger xbtManifestId: request.getXbtManifestIdList()) {
            XbtManifestRecord xbtManifestRecord = dslContext.selectFrom(XBT_MANIFEST)
                    .where(XBT_MANIFEST.XBT_MANIFEST_ID.eq(ULong.valueOf(xbtManifestId)))
                    .fetchOne();

            String duplicated = dslContext.select(CDT_PRI.NAME).from(BDT_PRI_RESTRI)
                    .join(CDT_AWD_PRI_XPS_TYPE_MAP).on(BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID.eq(CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_XPS_TYPE_MAP_ID))
                    .join(XBT).on(CDT_AWD_PRI_XPS_TYPE_MAP.XBT_ID.eq(XBT.XBT_ID))
                    .join(CDT_AWD_PRI).on(CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_ID.eq(CDT_AWD_PRI.CDT_AWD_PRI_ID))
                    .join(CDT_PRI).on(CDT_AWD_PRI.CDT_PRI_ID.eq(CDT_PRI.CDT_PRI_ID))
                    .where(and(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(dtManifestRecord.getDtManifestId()),
                            CDT_PRI.NAME.eq(request.getPrimitive()),
                            XBT.XBT_ID.eq(xbtManifestRecord.getXbtId())))
                    .fetchOneInto(String.class);

            if (duplicated != null) {
                throw new IllegalArgumentException("Duplicated Primitive already exist.");
            }

            CdtAwdPriXpsTypeMapRecord cdtAwdPriXpsTypeMapRecord = new CdtAwdPriXpsTypeMapRecord();
            cdtAwdPriXpsTypeMapRecord.setCdtAwdPriId(cdtAwdPriRecord.getCdtAwdPriId());
            cdtAwdPriXpsTypeMapRecord.setXbtId(xbtManifestRecord.getXbtId());

            cdtAwdPriXpsTypeMapRecord.setCdtAwdPriXpsTypeMapId(dslContext.insertInto(CDT_AWD_PRI_XPS_TYPE_MAP)
                    .set(cdtAwdPriXpsTypeMapRecord).returning(CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_XPS_TYPE_MAP_ID)
                    .fetchOne().getCdtAwdPriXpsTypeMapId());

            BdtPriRestriRecord bdtPriRestriRecord = new BdtPriRestriRecord();
            bdtPriRestriRecord.setCdtAwdPriXpsTypeMapId(cdtAwdPriXpsTypeMapRecord.getCdtAwdPriXpsTypeMapId());
            bdtPriRestriRecord.setBdtManifestId(dtManifestRecord.getDtManifestId());

            if(isDefault) {
                bdtPriRestriRecord.setIsDefault((byte) 1);
                isDefault = false;
            } else {
                bdtPriRestriRecord.setIsDefault((byte) 0);
            }

            dslContext.insertInto(BDT_PRI_RESTRI).set(bdtPriRestriRecord).execute();
        }
    }

    public void addDtCodeListRestriction(CreateCodeListRestrictionRepositoryRequest request) {
        CodeListManifestRecord codeListManifestRecord = dslContext.selectFrom(CODE_LIST_MANIFEST)
                .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(ULong.valueOf(request.getCodeListManifestId())))
                .fetchOne();

        CodeListRecord codeListRecord = dslContext.selectFrom(CODE_LIST)
                .where(CODE_LIST.CODE_LIST_ID.eq(codeListManifestRecord.getCodeListId()))
                .fetchOne();

        DtManifestRecord dtManifestRecord = dslContext.selectFrom(DT_MANIFEST)
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(ULong.valueOf(request.getDtManifestId())))
                .fetchOne();

        List<BdtPriRestriRecord> bdtPriRestriRecords = dslContext.selectFrom(BDT_PRI_RESTRI)
                .where(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(dtManifestRecord.getDtManifestId()))
                .fetch();

        BdtPriRestriRecord bdtPriRestriRecord = new BdtPriRestriRecord();

        if (bdtPriRestriRecords.size() == 0) {
            bdtPriRestriRecord.setIsDefault((byte) 1);
        } else {
            bdtPriRestriRecord.setIsDefault((byte) 0);
        }

        if (bdtPriRestriRecords.stream().anyMatch(e -> codeListManifestRecord.getCodeListManifestId().equals(e.getCodeListManifestId()))) {
            throw new IllegalArgumentException("Duplicated Code List already exist.");
        }

        bdtPriRestriRecord.setCodeListManifestId(codeListManifestRecord.getCodeListManifestId());
        bdtPriRestriRecord.setBdtManifestId(dtManifestRecord.getDtManifestId());
        
        dslContext.insertInto(BDT_PRI_RESTRI).set(bdtPriRestriRecord).execute();
    }

    public void addDtAgencyIdListRestriction(CreateAgencyIdListRestrictionRepositoryRequest request) {
        AgencyIdListManifestRecord agencyIdListManifestRecord = dslContext.selectFrom(AGENCY_ID_LIST_MANIFEST)
                .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(ULong.valueOf(request.getAgencyIdListManifestId())))
                .fetchOne();

        AgencyIdListRecord agencyIdListRecord = dslContext.selectFrom(AGENCY_ID_LIST)
                .where(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(agencyIdListManifestRecord.getAgencyIdListId()))
                .fetchOne();

        DtManifestRecord dtManifestRecord = dslContext.selectFrom(DT_MANIFEST)
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(ULong.valueOf(request.getDtManifestId())))
                .fetchOne();

        List<BdtPriRestriRecord> bdtPriRestriRecords = dslContext.selectFrom(BDT_PRI_RESTRI)
                .where(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(dtManifestRecord.getDtManifestId()))
                .fetch();

        BdtPriRestriRecord bdtPriRestriRecord = new BdtPriRestriRecord();

        if (bdtPriRestriRecords.size() == 0) {
            bdtPriRestriRecord.setIsDefault((byte) 1);
        } else {
            bdtPriRestriRecord.setIsDefault((byte) 0);
        }

        if (bdtPriRestriRecords.stream().anyMatch(e -> agencyIdListManifestRecord.getAgencyIdListManifestId().equals(e.getAgencyIdListManifestId()))) {
            throw new IllegalArgumentException("Duplicated Agency Id List already exist.");
        }

        bdtPriRestriRecord.setAgencyIdListManifestId(agencyIdListManifestRecord.getAgencyIdListManifestId());
        bdtPriRestriRecord.setBdtManifestId(dtManifestRecord.getDtManifestId());

        dslContext.insertInto(BDT_PRI_RESTRI).set(bdtPriRestriRecord).execute();
    }
}