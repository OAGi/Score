package org.oagi.score.repo.component.dt;

import com.google.gson.JsonObject;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repo.component.bcc.UpdateBccPropertiesRepositoryRequest;
import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.service.common.data.DTType;
import org.oagi.score.service.log.LogRepository;
import org.oagi.score.service.log.model.LogAction;
import org.oagi.score.service.log.model.LogSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.compare;
import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.inline;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.service.common.data.DTType.Core;

@Repository
public class BdtWriteRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private LogSerializer serializer;

    public CreateBdtRepositoryResponse createBdt(CreateBdtRepositoryRequest request) {
        ULong userId = ULong.valueOf(sessionService.userId(request.getUser()));
        LocalDateTime timestamp = request.getLocalDateTime();

        DtManifestRecord basedBdtManifest = dslContext.selectFrom(DT_MANIFEST)
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(ULong.valueOf(request.getBasedDdtManifestId())))
                .fetchOne();

        List<DtScManifestRecord> basedBdtScManifestList = dslContext.selectFrom(DT_SC_MANIFEST)
                .where(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.eq(basedBdtManifest.getDtManifestId()))
                .fetch();

        DtRecord basedBdt = dslContext.selectFrom(DT)
                .where(DT.DT_ID.eq(basedBdtManifest.getDtId()))
                .fetchOne();

        List<DtScRecord> basedBdtScList = dslContext.selectFrom(DT_SC)
                .where(DT_SC.OWNER_DT_ID.eq(basedBdt.getDtId()))
                .fetch();

        DtRecord bdt = new DtRecord();
        bdt.setGuid(ScoreGuid.randomGuid());
        bdt.setDataTypeTerm(basedBdt.getDataTypeTerm());

        switch (DTType.valueOf(basedBdt.getType())) {
            case Core:
                bdt.setType(DTType.Default.name());
                break;
            case Default:
            case Unqualified:
                bdt.setType(DTType.Unqualified.name());
                break;
            case Qualified:
                bdt.setType(DTType.Qualified.name());
                break;
            default:
                break;
        }

        if (basedBdt.getQualifier() != null) {
            bdt.setDen(basedBdt.getQualifier() + "_ " + basedBdt.getDen());
            bdt.setContentComponentDen(basedBdt.getQualifier() + "_ " + basedBdt.getContentComponentDen());
        } else {
            bdt.setDen(basedBdt.getDen());
            bdt.setContentComponentDen(basedBdt.getContentComponentDen());
        }

        bdt.setVersionNum("1.0");
        bdt.setBasedDtId(basedBdt.getDtId());
        bdt.setState(CcState.WIP.name());
        bdt.setIsDeprecated((byte) 0);
        bdt.setCommonlyUsed((byte) 1);
        bdt.setNamespaceId(null);
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

        // insert BDT_PRI_RESTRI
        dslContext.insertInto(BDT_PRI_RESTRI,
                BDT_PRI_RESTRI.BDT_ID,
                BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID,
                BDT_PRI_RESTRI.CODE_LIST_ID,
                BDT_PRI_RESTRI.AGENCY_ID_LIST_ID,
                BDT_PRI_RESTRI.IS_DEFAULT)
                .select(dslContext.select(inline(bdt.getDtId()),
                        BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID,
                        BDT_PRI_RESTRI.CODE_LIST_ID,
                        BDT_PRI_RESTRI.AGENCY_ID_LIST_ID,
                        BDT_PRI_RESTRI.IS_DEFAULT)
                        .from(BDT_PRI_RESTRI)
                        .where(BDT_PRI_RESTRI.BDT_ID.eq(basedBdt.getDtId()))).execute();

        for(DtScRecord basedDtSc: basedBdtScList) {
            DtScRecord dtScRecord = new DtScRecord();
            DtScManifestRecord dtScManifestRecord = new DtScManifestRecord();

            dtScRecord.setGuid(ScoreGuid.randomGuid());
            dtScRecord.setPropertyTerm(basedDtSc.getPropertyTerm());
            dtScRecord.setRepresentationTerm(basedDtSc.getRepresentationTerm());
            dtScRecord.setOwnerDtId(bdt.getDtId());
            dtScRecord.setCardinalityMax(basedDtSc.getCardinalityMax());
            dtScRecord.setCardinalityMin(basedDtSc.getCardinalityMin());
            dtScRecord.setBasedDtScId(basedDtSc.getDtScId());
            dtScRecord.setDefaultValue(basedDtSc.getDefaultValue());
            dtScRecord.setFixedValue(basedDtSc.getFixedValue());
            dtScRecord.setDtScId(
                    dslContext.insertInto(DT_SC)
                            .set(dtScRecord)
                            .returning(DT_SC.DT_SC_ID).fetchOne().getDtScId());

            dtScManifestRecord.setReleaseId(basedBdtManifest.getReleaseId());
            dtScManifestRecord.setDtScId(dtScRecord.getDtScId());
            dtScManifestRecord.setOwnerDtManifestId(bdtManifest.getDtManifestId());

            dtScManifestRecord.setDtScManifestId(
                    dslContext.insertInto(DT_SC_MANIFEST)
                    .set(dtScManifestRecord)
                    .returning(DT_SC_MANIFEST.DT_SC_MANIFEST_ID).fetchOne().getDtScManifestId());

            // insert BDT_SC_PRI_RESTRI
            dslContext.insertInto(BDT_SC_PRI_RESTRI,
                    BDT_SC_PRI_RESTRI.BDT_SC_ID,
                    BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID,
                    BDT_SC_PRI_RESTRI.CODE_LIST_ID,
                    BDT_SC_PRI_RESTRI.AGENCY_ID_LIST_ID,
                    BDT_SC_PRI_RESTRI.IS_DEFAULT)
                    .select(dslContext.select(inline(dtScRecord.getDtScId()),
                            BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID,
                            BDT_SC_PRI_RESTRI.CODE_LIST_ID,
                            BDT_SC_PRI_RESTRI.AGENCY_ID_LIST_ID,
                            BDT_SC_PRI_RESTRI.IS_DEFAULT)
                            .from(BDT_SC_PRI_RESTRI)
                            .where(BDT_SC_PRI_RESTRI.BDT_SC_ID.eq(basedDtSc.getDtScId()))).execute();
        }

        LogRecord logRecord =
                logRepository.insertBdtLog(
                        bdtManifest,
                        bdt,
                        LogAction.Added,
                        userId, timestamp);
        bdtManifest.setLogId(logRecord.getLogId());
        bdtManifest.update(DT_MANIFEST.LOG_ID);

        return new CreateBdtRepositoryResponse(bdtManifest.getDtManifestId().toBigInteger());
    }

//    public ReviseBdtRepositoryResponse reviseBdt(ReviseBdtRepositoryRequest request) {
//        AppUser user = sessionService.getAppUser(request.getUser());
//        ULong userId = ULong.valueOf(user.getAppUserId());
//        LocalDateTime timestamp = request.getLocalDateTime();
//
//        DtManifestRecord bdtManifestRecord = dslContext.selectFrom(DT_MANIFEST)
//                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(
//                        ULong.valueOf(request.getBdtManifestId())
//                ))
//                .fetchOne();
//
//        DtRecord prevDtRecord = dslContext.selectFrom(DT)
//                .where(DT.DT_ID.eq(bdtManifestRecord.getBdtId()))
//                .fetchOne();
//
//        if (user.isDeveloper()) {
//            if (!CcState.Published.equals(CcState.valueOf(prevDtRecord.getState()))) {
//                throw new IllegalArgumentException("Only the core component in 'Published' state can be revised.");
//            }
//        } else {
//            if (!CcState.Production.equals(CcState.valueOf(prevDtRecord.getState()))) {
//                throw new IllegalArgumentException("Only the core component in 'Production' state can be revised.");
//            }
//        }
//
//        ULong workingReleaseId = dslContext.select(RELEASE.RELEASE_ID)
//                .from(RELEASE)
//                .where(RELEASE.RELEASE_NUM.eq("Working"))
//                .fetchOneInto(ULong.class);
//
//        ULong targetReleaseId = bdtManifestRecord.getReleaseId();
//        if (user.isDeveloper()) {
//            if (!targetReleaseId.equals(workingReleaseId)) {
//                throw new IllegalArgumentException("It only allows to revise the component in 'Working' branch for developers.");
//            }
//        } else {
//            if (targetReleaseId.equals(workingReleaseId)) {
//                throw new IllegalArgumentException("It only allows to revise the component in non-'Working' branch for end-users.");
//            }
//        }
//
//        boolean ownerIsDeveloper = dslContext.select(APP_USER.IS_DEVELOPER)
//                .from(APP_USER)
//                .where(APP_USER.APP_USER_ID.eq(prevDtRecord.getOwnerUserId()))
//                .fetchOneInto(Boolean.class);
//
//        if (user.isDeveloper() != ownerIsDeveloper) {
//            throw new IllegalArgumentException("It only allows to revise the component for users in the same roles.");
//        }
//
//        // creates new bdt for revised record.
//        DtRecord nextDtRecord = prevDtRecord.copy();
//        nextDtRecord.setState(CcState.WIP.name());
//        nextDtRecord.setCreatedBy(userId);
//        nextDtRecord.setLastUpdatedBy(userId);
//        nextDtRecord.setOwnerUserId(userId);
//        nextDtRecord.setCreationTimestamp(timestamp);
//        nextDtRecord.setLastUpdateTimestamp(timestamp);
//        nextDtRecord.setPrevBdtId(prevDtRecord.getBdtId());
//        nextDtRecord.setBdtId(
//                dslContext.insertInto(DT)
//                        .set(nextDtRecord)
//                        .returning(DT.DT_ID).fetchOne().getBdtId()
//        );
//
//        prevDtRecord.setNextBdtId(nextDtRecord.getBdtId());
//        prevDtRecord.update(DT.NEXT_DT_ID);
//
//        // creates new log for revised record.
//        LogRecord logRecord =
//                logRepository.insertBdtLog(
//                        bdtManifestRecord,
//                        nextDtRecord, bdtManifestRecord.getLogId(),
//                        LogAction.Revised,
//                        userId, timestamp);
//
//        ULong responseBdtManifestId;
//        bdtManifestRecord.setBdtId(nextDtRecord.getBdtId());
//        bdtManifestRecord.setLogId(logRecord.getLogId());
//        bdtManifestRecord.update(DT_MANIFEST.DT_ID, DT_MANIFEST.LOG_ID);
//
//        responseBdtManifestId = bdtManifestRecord.getBdtManifestId();
//
//        // update `conflict` for bcc_manifests' to_bdt_manifest_id which indicates given bdt manifest.
//        dslContext.update(BCC_MANIFEST)
//                .set(BCC_MANIFEST.TO_DT_MANIFEST_ID, responseBdtManifestId)
//                .set(BCC_MANIFEST.CONFLICT, (byte) 1)
//                .where(and(
//                        BCC_MANIFEST.RELEASE_ID.eq(targetReleaseId),
//                        BCC_MANIFEST.TO_DT_MANIFEST_ID.in(Arrays.asList(
//                                bdtManifestRecord.getBdtManifestId(),
//                                bdtManifestRecord.getPrevBdtManifestId()))
//                ))
//                .execute();
//
//        return new ReviseBdtRepositoryResponse(responseBdtManifestId.toBigInteger());
//    }
//
//    public UpdateBdtPropertiesRepositoryResponse updateBdtProperties(UpdateBdtPropertiesRepositoryRequest request) {
//        AppUser user = sessionService.getAppUser(request.getUser());
//        ULong userId = ULong.valueOf(user.getAppUserId());
//        LocalDateTime timestamp = request.getLocalDateTime();
//
//        DtManifestRecord bdtManifestRecord = dslContext.selectFrom(DT_MANIFEST)
//                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(
//                        ULong.valueOf(request.getBdtManifestId())
//                ))
//                .fetchOne();
//
//        DtRecord bdtRecord = dslContext.selectFrom(DT)
//                .where(DT.DT_ID.eq(bdtManifestRecord.getBdtId()))
//                .fetchOne();
//
//        if (!CcState.WIP.equals(CcState.valueOf(bdtRecord.getState()))) {
//            throw new IllegalArgumentException("Only the core component in 'WIP' state can be modified.");
//        }
//
//        if (!bdtRecord.getOwnerUserId().equals(userId)) {
//            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
//        }
//
//        // update bdt record.
//        UpdateSetFirstStep<DtRecord> firstStep = dslContext.update(DT);
//        UpdateSetMoreStep<DtRecord> moreStep = null;
//        boolean propertyTermChanged = false;
//        if (compare(bdtRecord.getPropertyTerm(), request.getPropertyTerm()) != 0) {
//            propertyTermChanged = true;
//            moreStep = ((moreStep != null) ? moreStep : firstStep)
//                    .set(DT.PROPERTY_TERM, request.getPropertyTerm())
//                    .set(DT.DEN, request.getPropertyTerm() + ". " + bdtRecord.getRepresentationTerm());
//        }
//        if (!StringUtils.hasLength(request.getDefaultValue()) && !StringUtils.hasLength(request.getFixedValue())) {
//            moreStep = ((moreStep != null) ? moreStep : firstStep)
//                    .setNull(DT.DEFAULT_VALUE)
//                    .setNull(DT.FIXED_VALUE);
//        } else {
//            if (compare(bdtRecord.getDefaultValue(), request.getDefaultValue()) != 0) {
//                moreStep = ((moreStep != null) ? moreStep : firstStep)
//                        .set(DT.DEFAULT_VALUE, request.getDefaultValue())
//                        .setNull(DT.FIXED_VALUE);
//            } else if (compare(bdtRecord.getFixedValue(), request.getFixedValue()) != 0) {
//                moreStep = ((moreStep != null) ? moreStep : firstStep)
//                        .setNull(DT.DEFAULT_VALUE)
//                        .set(DT.FIXED_VALUE, request.getFixedValue());
//            }
//        }
//        if (compare(bdtRecord.getDefinition(), request.getDefinition()) != 0) {
//            moreStep = ((moreStep != null) ? moreStep : firstStep)
//                    .set(DT.DEFINITION, request.getDefinition());
//        }
//        if (compare(bdtRecord.getDefinitionSource(), request.getDefinitionSource()) != 0) {
//            moreStep = ((moreStep != null) ? moreStep : firstStep)
//                    .set(DT.DEFINITION_SOURCE, request.getDefinitionSource());
//        }
//        if ((bdtRecord.getIsDeprecated() == 1) != request.isDeprecated()) {
//            moreStep = ((moreStep != null) ? moreStep : firstStep)
//                    .set(DT.IS_DEPRECATED, (byte) ((request.isDeprecated()) ? 1 : 0));
//        }
//        if ((bdtRecord.getIsNillable() == 1) != request.isNillable()) {
//            moreStep = ((moreStep != null) ? moreStep : firstStep)
//                    .set(DT.IS_NILLABLE, (byte) ((request.isNillable()) ? 1 : 0));
//        }
//        if (request.getNamespaceId() == null || request.getNamespaceId().longValue() <= 0L) {
//            moreStep = ((moreStep != null) ? moreStep : firstStep)
//                    .setNull(DT.NAMESPACE_ID);
//        } else {
//            moreStep = ((moreStep != null) ? moreStep : firstStep)
//                    .set(DT.NAMESPACE_ID, ULong.valueOf(request.getNamespaceId()));
//        }
//
//        if (moreStep != null) {
//            moreStep.set(DT.LAST_UPDATED_BY, userId)
//                    .set(DT.LAST_UPDATE_TIMESTAMP, timestamp)
//                    .where(DT.DT_ID.eq(bdtRecord.getBdtId()))
//                    .execute();
//
//            bdtRecord = dslContext.selectFrom(DT)
//                    .where(DT.DT_ID.eq(bdtManifestRecord.getBdtId()))
//                    .fetchOne();
//        }
//
//        // creates new log for updated record.
//        LogRecord logRecord =
//                logRepository.insertBdtLog(
//                        bdtManifestRecord,
//                        bdtRecord, bdtManifestRecord.getLogId(),
//                        LogAction.Modified,
//                        userId, timestamp);
//
//        bdtManifestRecord.setLogId(logRecord.getLogId());
//        bdtManifestRecord.update(DT_MANIFEST.LOG_ID);
//
//        if (propertyTermChanged) {
//            for (ULong bccManifestId : dslContext.select(BCC_MANIFEST.BCC_MANIFEST_ID)
//                    .from(BCC_MANIFEST)
//                    .where(BCC_MANIFEST.TO_DT_MANIFEST_ID.eq(bdtManifestRecord.getBdtManifestId()))
//                    .fetchInto(ULong.class)) {
//
//                UpdateBccPropertiesRepositoryRequest updateBccPropertiesRepositoryRequest =
//                        new UpdateBccPropertiesRepositoryRequest(request.getUser(), request.getLocalDateTime(),
//                                bccManifestId.toBigInteger());
//                updateBccPropertiesRepositoryRequest.setPropagation(true);
//                bccWriteRepository.updateBccProperties(updateBccPropertiesRepositoryRequest);
//            }
//        }
//
//        return new UpdateBdtPropertiesRepositoryResponse(bdtManifestRecord.getBdtManifestId().toBigInteger());
//    }
//
//    public UpdateBdtBdtRepositoryResponse updateBdtBdt(UpdateBdtBdtRepositoryRequest request) {
//        AppUser user = sessionService.getAppUser(request.getUser());
//        ULong userId = ULong.valueOf(user.getAppUserId());
//        LocalDateTime timestamp = request.getLocalDateTime();
//
//        DtManifestRecord bdtManifestRecord = dslContext.selectFrom(DT_MANIFEST)
//                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(
//                        ULong.valueOf(request.getBdtManifestId())
//                ))
//                .fetchOne();
//
//        DtRecord bdtRecord = dslContext.selectFrom(DT)
//                .where(DT.DT_ID.eq(bdtManifestRecord.getBdtId()))
//                .fetchOne();
//
//        if (!CcState.WIP.equals(CcState.valueOf(bdtRecord.getState()))) {
//            throw new IllegalArgumentException("Only the core component in 'WIP' state can be modified.");
//        }
//
//        if (!bdtRecord.getOwnerUserId().equals(userId)) {
//            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
//        }
//
//        // update bdt record.
//        ULong bdtManifestId = ULong.valueOf(request.getBdtManifestId());
//        Record2<ULong, String> result = dslContext.select(DT.DT_ID, DT.DATA_TYPE_TERM)
//                .from(DT)
//                .join(DT_MANIFEST).on(DT.DT_ID.eq(DT_MANIFEST.DT_ID))
//                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(bdtManifestId))
//                .fetchOne();
//
//        bdtRecord.setBdtId(result.get(DT.DT_ID));
//        bdtRecord.setRepresentationTerm(result.get(DT.DATA_TYPE_TERM));
//        bdtRecord.setDen(bdtRecord.getPropertyTerm() + ". " + bdtRecord.getRepresentationTerm());
//        bdtRecord.setLastUpdatedBy(userId);
//        bdtRecord.setLastUpdateTimestamp(timestamp);
//        bdtRecord.update(DT.BDT_ID,
//                DT.REPRESENTATION_TERM, DT.DEN,
//                DT.LAST_UPDATED_BY, DT.LAST_UPDATE_TIMESTAMP);
//
//        // creates new log for updated record.
//        LogRecord logRecord =
//                logRepository.insertBdtLog(
//                        bdtManifestRecord,
//                        bdtRecord, bdtManifestRecord.getLogId(),
//                        LogAction.Modified,
//                        userId, timestamp);
//
//        bdtManifestRecord.setBdtManifestId(bdtManifestId);
//        bdtManifestRecord.setLogId(logRecord.getLogId());
//        bdtManifestRecord.update(DT_MANIFEST.BDT_MANIFEST_ID, DT_MANIFEST.LOG_ID);
//
//        return new UpdateBdtBdtRepositoryResponse(bdtManifestRecord.getBdtManifestId().toBigInteger(), bdtRecord.getDen());
//    }
//
//    public UpdateBdtStateRepositoryResponse updateBdtState(UpdateBdtStateRepositoryRequest request) {
//        AppUser user = sessionService.getAppUser(request.getUser());
//        ULong userId = ULong.valueOf(user.getAppUserId());
//        LocalDateTime timestamp = request.getLocalDateTime();
//
//        DtManifestRecord bdtManifestRecord = dslContext.selectFrom(DT_MANIFEST)
//                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(
//                        ULong.valueOf(request.getBdtManifestId())
//                ))
//                .fetchOne();
//
//        DtRecord bdtRecord = dslContext.selectFrom(DT)
//                .where(DT.DT_ID.eq(bdtManifestRecord.getBdtId()))
//                .fetchOne();
//
//        CcState prevState = CcState.valueOf(bdtRecord.getState());
//        CcState nextState = request.getToState();
//
//        if (prevState != request.getFromState()) {
//            throw new IllegalArgumentException("Target core component is not in '" + request.getFromState() + "' state.");
//        }
//
//        if (!prevState.canMove(nextState)) {
//            throw new IllegalArgumentException("The core component in '" + prevState + "' state cannot move to '" + nextState + "' state.");
//        }
//
//        // Change owner of CC when it restored.
//        if (prevState == CcState.Deleted && nextState == CcState.WIP) {
//            bdtRecord.setOwnerUserId(userId);
//        } else if (prevState != CcState.Deleted && !bdtRecord.getOwnerUserId().equals(userId)
//                && !prevState.canForceMove(request.getToState())) {
//            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
//        } else if (bdtRecord.getNamespaceId() == null) {
//            throw new IllegalArgumentException("'" + bdtRecord.getDen() + "' dose not have NamespaceId.");
//        }
//
//        // update bdt state.
//        bdtRecord.setState(nextState.name());
//        if (!prevState.canForceMove(request.getToState())) {
//            bdtRecord.setLastUpdatedBy(userId);
//            bdtRecord.setLastUpdateTimestamp(timestamp);
//        }
//        bdtRecord.update(DT.STATE,
//                DT.LAST_UPDATED_BY, DT.LAST_UPDATE_TIMESTAMP, DT.OWNER_USER_ID);
//
//        // creates new log for updated record.
//        LogAction logAction = (CcState.Deleted == prevState && CcState.WIP == nextState)
//                ? LogAction.Restored : LogAction.Modified;
//        LogRecord logRecord =
//                logRepository.insertBdtLog(
//                        bdtManifestRecord,
//                        bdtRecord, bdtManifestRecord.getLogId(),
//                        logAction,
//                        userId, timestamp);
//
//        bdtManifestRecord.setLogId(logRecord.getLogId());
//        bdtManifestRecord.update(DT_MANIFEST.LOG_ID);
//
//        return new UpdateBdtStateRepositoryResponse(bdtManifestRecord.getBdtManifestId().toBigInteger());
//    }
//
//    public DeleteBdtRepositoryResponse deleteBdt(DeleteBdtRepositoryRequest request) {
//        AppUser user = sessionService.getAppUser(request.getUser());
//        ULong userId = ULong.valueOf(user.getAppUserId());
//        LocalDateTime timestamp = request.getLocalDateTime();
//
//        DtManifestRecord bdtManifestRecord = dslContext.selectFrom(DT_MANIFEST)
//                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(
//                        ULong.valueOf(request.getBdtManifestId())
//                ))
//                .fetchOne();
//
//        DtRecord bdtRecord = dslContext.selectFrom(DT)
//                .where(DT.DT_ID.eq(bdtManifestRecord.getBdtId()))
//                .fetchOne();
//
//        if (!CcState.WIP.equals(CcState.valueOf(bdtRecord.getState()))) {
//            throw new IllegalArgumentException("Only the core component in 'WIP' state can be deleted.");
//        }
//
//        if (!bdtRecord.getOwnerUserId().equals(userId)) {
//            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
//        }
//
//        // update bdt state.
//        bdtRecord.setState(CcState.Deleted.name());
//        bdtRecord.setLastUpdatedBy(userId);
//        bdtRecord.setLastUpdateTimestamp(timestamp);
//        bdtRecord.update(DT.STATE,
//                DT.LAST_UPDATED_BY, DT.LAST_UPDATE_TIMESTAMP);
//
//        // creates new log for deleted record.
//        LogRecord logRecord =
//                logRepository.insertBdtLog(
//                        bdtManifestRecord,
//                        bdtRecord, bdtManifestRecord.getLogId(),
//                        LogAction.Deleted,
//                        userId, timestamp);
//
//        bdtManifestRecord.setLogId(logRecord.getLogId());
//        bdtManifestRecord.update(DT_MANIFEST.LOG_ID);
//
//        return new DeleteBdtRepositoryResponse(bdtManifestRecord.getBdtManifestId().toBigInteger());
//    }
//
//    public UpdateBdtOwnerRepositoryResponse updateBdtOwner(UpdateBdtOwnerRepositoryRequest request) {
//        AppUser user = sessionService.getAppUser(request.getUser());
//        ULong userId = ULong.valueOf(user.getAppUserId());
//        LocalDateTime timestamp = request.getLocalDateTime();
//
//        DtManifestRecord bdtManifestRecord = dslContext.selectFrom(DT_MANIFEST)
//                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(
//                        ULong.valueOf(request.getBdtManifestId())
//                ))
//                .fetchOne();
//
//        DtRecord bdtRecord = dslContext.selectFrom(DT)
//                .where(DT.DT_ID.eq(bdtManifestRecord.getBdtId()))
//                .fetchOne();
//
//        if (!CcState.WIP.equals(CcState.valueOf(bdtRecord.getState()))) {
//            throw new IllegalArgumentException("Only the core component in 'WIP' state can be modified.");
//        }
//
//        if (!bdtRecord.getOwnerUserId().equals(userId)) {
//            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
//        }
//
//        bdtRecord.setOwnerUserId(ULong.valueOf(request.getOwnerId()));
//        bdtRecord.setLastUpdatedBy(userId);
//        bdtRecord.setLastUpdateTimestamp(timestamp);
//        bdtRecord.update(DT.OWNER_USER_ID, DT.LAST_UPDATED_BY, DT.LAST_UPDATE_TIMESTAMP);
//
//        LogRecord logRecord =
//                logRepository.insertBdtLog(
//                        bdtManifestRecord,
//                        bdtRecord, bdtManifestRecord.getLogId(),
//                        LogAction.Modified,
//                        userId, timestamp);
//
//        bdtManifestRecord.setLogId(logRecord.getLogId());
//        bdtManifestRecord.update(DT_MANIFEST.LOG_ID);
//
//        return new UpdateBdtOwnerRepositoryResponse(bdtManifestRecord.getBdtManifestId().toBigInteger());
//    }
//
//    public CancelRevisionBdtRepositoryResponse cancelRevisionBdt(CancelRevisionBdtRepositoryRequest request) {
//        ULong userId = ULong.valueOf(sessionService.userId(request.getUser()));
//        LocalDateTime timestamp = request.getLocalDateTime();
//
//        DtManifestRecord bdtManifestRecord = dslContext.selectFrom(DT_MANIFEST)
//                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(ULong.valueOf(request.getBdtManifestId()))).fetchOne();
//
//        if (bdtManifestRecord == null) {
//            throw new IllegalArgumentException("Not found a target DT");
//        }
//
//        DtRecord bdtRecord = dslContext.selectFrom(DT)
//                .where(DT.DT_ID.eq(bdtManifestRecord.getBdtId())).fetchOne();
//
//        if (bdtRecord.getPrevBdtId() == null) {
//            throw new IllegalArgumentException("Not found previous log");
//        }
//
//        DtRecord prevDtRecord = dslContext.selectFrom(DT)
//                .where(DT.DT_ID.eq(bdtRecord.getPrevBdtId())).fetchOne();
//
//        // update DT MANIFEST's bdt_id
//        if (prevDtRecord.getBdtId() != null) {
//            String prevBdtGuid = dslContext.select(DT.GUID)
//                    .from(DT).where(DT.DT_ID.eq(prevDtRecord.getBdtId())).fetchOneInto(String.class);
//            DtManifestRecord bdtManifest = dslContext.select(DT_MANIFEST.fields()).from(DT)
//                    .join(DT_MANIFEST).on(DT.DT_ID.eq(DT_MANIFEST.DT_ID))
//                    .where(and(DT_MANIFEST.RELEASE_ID.eq(bdtManifestRecord.getReleaseId()),
//                            DT.GUID.eq(prevBdtGuid))).fetchOneInto(DtManifestRecord.class);
//            bdtManifestRecord.setBdtManifestId(bdtManifest.getDtManifestId());
//        }
//        bdtManifestRecord.setBdtId(bdtRecord.getPrevBdtId());
//        bdtManifestRecord.update(DT_MANIFEST.DT_ID, DT_MANIFEST.BDT_MANIFEST_ID);
//
//        // update BCCs which using current DT
//        dslContext.update(BCC)
//                .set(BCC.TO_DT_ID, bdtRecord.getPrevBdtId())
//                .where(BCC.TO_DT_ID.eq(bdtRecord.getBdtId()))
//                .execute();
//
//        // unlink prev DT
//        prevDtRecord.setNextBdtId(null);
//        prevDtRecord.update(DT.NEXT_DT_ID);
//
//        // clean logs up
//        logRepository.revertToStableState(bdtManifestRecord);
//
//        // delete current DT
//        bdtRecord.delete();
//
//        return new CancelRevisionBdtRepositoryResponse(request.getBdtManifestId());
//    }
//
//    public CancelRevisionBdtRepositoryResponse resetLogBdt(CancelRevisionBdtRepositoryRequest request) {
//        DtManifestRecord bdtManifestRecord = dslContext.selectFrom(DT_MANIFEST)
//                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(ULong.valueOf(request.getBdtManifestId()))).fetchOne();
//
//        if (bdtManifestRecord == null) {
//            throw new IllegalArgumentException("Not found a target DT");
//        }
//
//        DtRecord bdtRecord = dslContext.selectFrom(DT)
//                .where(DT.DT_ID.eq(bdtManifestRecord.getBdtId())).fetchOne();
//
//        LogRecord cursorLog = dslContext.selectFrom(LOG)
//                .where(LOG.LOG_ID.eq(bdtManifestRecord.getLogId())).fetchOne();
//
//        UInteger logNum = cursorLog.getRevisionNum();
//
//        if (cursorLog.getPrevLogId() == null) {
//            throw new IllegalArgumentException("There is no change to be reset.");
//        }
//
//        List<ULong> deleteLogTargets = new ArrayList<>();
//
//        while (cursorLog.getPrevLogId() != null) {
//            if (!cursorLog.getRevisionNum().equals(logNum)) {
//                throw new IllegalArgumentException("Cannot find reset point");
//            }
//            if (cursorLog.getRevisionTrackingNum().equals(UInteger.valueOf(1))) {
//                break;
//            }
//            deleteLogTargets.add(cursorLog.getLogId());
//            cursorLog = dslContext.selectFrom(LOG)
//                    .where(LOG.LOG_ID.eq(cursorLog.getPrevLogId())).fetchOne();
//        }
//
//        JsonObject snapshot = serializer.deserialize(cursorLog.getSnapshot().toString());
//
//        ULong bdtId = serializer.getSnapshotId(snapshot.get("bdtId"));
//        DtManifestRecord bdtManifestRecord = dslContext.selectFrom(DT_MANIFEST).where(and(
//                DT_MANIFEST.DT_ID.eq(bdtId),
//                DT_MANIFEST.RELEASE_ID.eq(bdtManifestRecord.getReleaseId())
//        )).fetchOne();
//
//        if (bdtManifestRecord == null) {
//            throw new IllegalArgumentException("Not found based BDT.");
//        }
//
//        bdtManifestRecord.setBdtManifestId(bdtManifestRecord.getDtManifestId());
//        bdtManifestRecord.setLogId(cursorLog.getLogId());
//        bdtManifestRecord.update(DT_MANIFEST.BDT_MANIFEST_ID, DT_MANIFEST.LOG_ID);
//
//        bdtRecord.setBdtId(bdtManifestRecord.getDtId());
//        bdtRecord.setPropertyTerm(serializer.getSnapshotString(snapshot.get("propertyTerm")));
//        bdtRecord.setRepresentationTerm(serializer.getSnapshotString(snapshot.get("representationTerm")));
//        bdtRecord.setDen(bdtRecord.getPropertyTerm() + ". " + bdtRecord.getRepresentationTerm());
//        bdtRecord.setDefinition(serializer.getSnapshotString(snapshot.get("definition")));
//        bdtRecord.setDefinitionSource(serializer.getSnapshotString(snapshot.get("definitionSource")));
//        bdtRecord.setNamespaceId(serializer.getSnapshotId(snapshot.get("namespaceId")));
//        bdtRecord.setIsDeprecated(serializer.getSnapshotByte(snapshot.get("deprecated")));
//        bdtRecord.setIsNillable(serializer.getSnapshotByte(snapshot.get("nillable")));
//        bdtRecord.setDefaultValue(serializer.getSnapshotString(snapshot.get("defaultValue")));
//        bdtRecord.setFixedValue(serializer.getSnapshotString(snapshot.get("fixedValue")));
//        bdtRecord.update();
//
//        cursorLog.setNextLogId(null);
//        cursorLog.update(LOG.NEXT_LOG_ID);
//        dslContext.update(LOG)
//                .setNull(LOG.PREV_LOG_ID)
//                .setNull(LOG.NEXT_LOG_ID)
//                .where(LOG.LOG_ID.in(deleteLogTargets))
//                .execute();
//        dslContext.deleteFrom(LOG).where(LOG.LOG_ID.in(deleteLogTargets)).execute();
//
//        return new CancelRevisionBdtRepositoryResponse(request.getBdtManifestId());
//    }
}