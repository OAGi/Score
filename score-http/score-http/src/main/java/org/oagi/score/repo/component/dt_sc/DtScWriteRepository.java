package org.oagi.score.repo.component.dt_sc;

import org.jooq.DSLContext;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.cc_management.data.node.CcBdtScPriRestri;
import org.oagi.score.gateway.http.api.cc_management.data.node.PrimitiveRestriType;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.component.dt.UpdateDtStateRepositoryRequest;
import org.oagi.score.repo.component.dt.UpdateDtStateRepositoryResponse;
import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.service.log.LogRepository;
import org.oagi.score.service.log.model.LogAction;
import org.oagi.score.service.log.model.LogSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.compare;
import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Repository
public class DtScWriteRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private LogSerializer serializer;

    private void updateDerivedSc(DtScManifestRecord baseDtScManifestRecord, DtScRecord baseDtScRecord,
                                 boolean isRepresentationTermChanged) {

        dslContext.selectFrom(DT_SC_MANIFEST).where(DT_SC_MANIFEST.BASED_DT_SC_MANIFEST_ID.eq(baseDtScManifestRecord.getDtScManifestId()))
                .fetchStream().forEach(dtScManifestRecord -> {
                    DtScRecord dtScRecord = dslContext.selectFrom(DT_SC)
                            .where(DT_SC.DT_SC_ID.eq(dtScManifestRecord.getDtScId())).fetchOne();

                    // update bdtSc record.
                    UpdateSetFirstStep<DtScRecord> firstStep = dslContext.update(DT_SC);
                    UpdateSetMoreStep<DtScRecord> moreStep = null;
                    if (compare(dtScRecord.getPropertyTerm(), baseDtScRecord.getPropertyTerm()) != 0) {
                        moreStep = ((moreStep != null) ? moreStep : firstStep)
                                .set(DT_SC.PROPERTY_TERM, baseDtScRecord.getPropertyTerm());
                    }
                    if (compare(dtScRecord.getRepresentationTerm(), baseDtScRecord.getRepresentationTerm()) != 0) {
                        moreStep = ((moreStep != null) ? moreStep : firstStep)
                                .set(DT_SC.REPRESENTATION_TERM, baseDtScRecord.getRepresentationTerm());
                    }
                    if (!dtScRecord.getCardinalityMax().equals(baseDtScRecord.getCardinalityMax())) {
                        moreStep = ((moreStep != null) ? moreStep : firstStep)
                                .set(DT_SC.CARDINALITY_MAX, baseDtScRecord.getCardinalityMax());
                    }
                    if (!dtScRecord.getCardinalityMin().equals(baseDtScRecord.getCardinalityMin())) {
                        moreStep = ((moreStep != null) ? moreStep : firstStep)
                                .set(DT_SC.CARDINALITY_MIN, baseDtScRecord.getCardinalityMin());
                    }
                    if (compare(dtScRecord.getDefinition(), baseDtScRecord.getDefinition()) != 0) {
                        moreStep = ((moreStep != null) ? moreStep : firstStep)
                                .set(DT_SC.DEFINITION, baseDtScRecord.getDefinition());
                    }
                    if (compare(dtScRecord.getDefinitionSource(), baseDtScRecord.getDefinitionSource()) != 0) {
                        moreStep = ((moreStep != null) ? moreStep : firstStep)
                                .set(DT_SC.DEFINITION_SOURCE, baseDtScRecord.getDefinitionSource());
                    }
                    if (compare(dtScRecord.getDefaultValue(), baseDtScRecord.getDefaultValue()) != 0) {
                        moreStep = ((moreStep != null) ? moreStep : firstStep)
                                .set(DT_SC.DEFAULT_VALUE, baseDtScRecord.getDefaultValue())
                                .setNull(DT_SC.FIXED_VALUE);
                    } else if (compare(dtScRecord.getFixedValue(), baseDtScRecord.getFixedValue()) != 0) {
                        moreStep = ((moreStep != null) ? moreStep : firstStep)
                                .set(DT_SC.FIXED_VALUE, baseDtScRecord.getFixedValue())
                                .setNull(DT_SC.DEFAULT_VALUE);
                    }

                    if (moreStep != null) {
                        moreStep.set(DT_SC.LAST_UPDATED_BY, baseDtScRecord.getLastUpdatedBy())
                                .set(DT_SC.LAST_UPDATE_TIMESTAMP, baseDtScRecord.getLastUpdateTimestamp())
                                .where(DT_SC.DT_SC_ID.eq(dtScRecord.getDtScId()))
                                .execute();

                        if (isRepresentationTermChanged) {
                            deleteCdtScAwdPriByDtScId(dtScRecord.getDtScId());
                        }

                        updateDerivedSc(dtScManifestRecord, dslContext.selectFrom(DT_SC)
                                .where(DT_SC.DT_SC_ID.eq(dtScRecord.getDtScId())).fetchOne(),
                                isRepresentationTermChanged);
                    }
                });
    }

    private void deleteCdtScAwdPriByDtScId(ULong dtScId) {
        for (ULong derivedDtScId : dslContext.select(DT_SC.DT_SC_ID)
                .from(DT_SC)
                .where(DT_SC.BASED_DT_SC_ID.eq(dtScId))
                .fetchInto(ULong.class)) {
            deleteCdtScAwdPriByDtScId(derivedDtScId);
        }

        List<ULong> cdtScAwdPriIdList = dslContext.select(CDT_SC_AWD_PRI.CDT_SC_AWD_PRI_ID)
                .from(CDT_SC_AWD_PRI)
                .where(CDT_SC_AWD_PRI.CDT_SC_ID.eq(dtScId))
                .fetchInto(ULong.class);

        if (!cdtScAwdPriIdList.isEmpty()) {
            List<ULong> cdtScAwdPriXpsTypeMapIdList =
                    dslContext.select(CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID)
                            .from(CDT_SC_AWD_PRI_XPS_TYPE_MAP)
                            .where(CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_ID.in(cdtScAwdPriIdList))
                            .fetchInto(ULong.class);

            dslContext.deleteFrom(BDT_SC_PRI_RESTRI)
                    .where(BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID.in(cdtScAwdPriXpsTypeMapIdList))
                    .execute();

            dslContext.deleteFrom(CDT_SC_AWD_PRI_XPS_TYPE_MAP)
                    .where(CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_ID.in(cdtScAwdPriIdList))
                    .execute();

            dslContext.deleteFrom(CDT_SC_AWD_PRI)
                    .where(CDT_SC_AWD_PRI.CDT_SC_AWD_PRI_ID.in(cdtScAwdPriIdList))
                    .execute();
        }
    }

    private void ensureUniquenessOfPropertyTerm(DtScRecord dtScRecord, String propertyTerm) {
        ULong dtId = dtScRecord.getOwnerDtId();
        int cnt = dslContext.selectCount()
                .from(DT_SC)
                .where(and(
                        DT_SC.PROPERTY_TERM.eq(propertyTerm),
                        DT_SC.OWNER_DT_ID.eq(dtId),
                        DT_SC.DT_SC_ID.notEqual(dtScRecord.getDtScId())
                ))
                .fetchOptionalInto(Integer.class).orElse(0);
        if (cnt > 0) {
            throw new IllegalArgumentException("There is an another supplementary component whose property term is same with the request: " + propertyTerm);
        }
    }

    private void ensureUniquenessOfDen(DtScRecord dtScRecord,
                                       String objectClassTerm,
                                       String propertyTerm,
                                       String representationTerm) {
        int cnt = dslContext.selectCount()
                .from(DT_SC)
                .where(and(
                        DT_SC.OBJECT_CLASS_TERM.eq(objectClassTerm),
                        DT_SC.PROPERTY_TERM.eq(propertyTerm),
                        DT_SC.REPRESENTATION_TERM.eq(representationTerm),
                        DT_SC.OWNER_DT_ID.eq(dtScRecord.getOwnerDtId()),
                        DT_SC.DT_SC_ID.notEqual(dtScRecord.getDtScId())
                ))
                .fetchOptionalInto(Integer.class).orElse(0);
        if (cnt > 0) {
            String den = getDen(objectClassTerm, propertyTerm, representationTerm);
            throw new IllegalArgumentException("There is an another supplementary component whose DEN is same with the request: " + den);
        }

        if (dtScRecord.getBasedDtScId() != null) {
            dtScRecord = dslContext.selectFrom(DT_SC)
                    .where(DT_SC.DT_SC_ID.eq(dtScRecord.getBasedDtScId()))
                    .fetchOne();
            ensureUniquenessOfDen(dtScRecord, objectClassTerm, propertyTerm, representationTerm);
        }
    }

    private String getDen(
            String objectClassTerm,
            String propertyTerm,
            String representationTerm) {
        String middleTerm = null;
        if (StringUtils.hasLength(propertyTerm)) {
            middleTerm = propertyTerm.replaceAll(representationTerm, "").trim();
        }
        if (middleTerm != null) {
            return objectClassTerm + ". " + middleTerm + ". " + representationTerm;
        }
        return objectClassTerm + ". " + representationTerm;
    }

    public UpdateDtScPropertiesRepositoryResponse updateDtScProperties(UpdateDtScPropertiesRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        DtScManifestRecord dtScManifestRecord = dslContext.selectFrom(DT_SC_MANIFEST)
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(
                        ULong.valueOf(request.getDtScManifestId())
                ))
                .fetchOne();

        DtScRecord dtScRecord = dslContext.selectFrom(DT_SC)
                .where(DT_SC.DT_SC_ID.eq(dtScManifestRecord.getDtScId()))
                .fetchOne();

        // Issue #1240
        ensureUniquenessOfPropertyTerm(dtScRecord, request.getPropertyTerm());

        ensureUniquenessOfDen(dtScRecord,
                dtScRecord.getObjectClassTerm(),
                request.getPropertyTerm(),
                request.getRepresentationTerm());

        DtManifestRecord dtManifestRecord = dslContext.selectFrom(DT_MANIFEST)
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(dtScManifestRecord.getOwnerDtManifestId()))
                .fetchOne();

        DtRecord dtRecord = dslContext.selectFrom(DT)
                .where(DT.DT_ID.eq(dtManifestRecord.getDtId()))
                .fetchOne();

        if (!CcState.WIP.equals(CcState.valueOf(dtRecord.getState()))) {
            throw new IllegalArgumentException("Only the core component in 'WIP' state can be modified.");
        }

        if (!dtRecord.getOwnerUserId().equals(userId)) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        // update bdtSc record.
        boolean isRepresentationTermChanged = false;
        UpdateSetFirstStep<DtScRecord> firstStep = dslContext.update(DT_SC);
        UpdateSetMoreStep<DtScRecord> moreStep = null;
        if (compare(dtScRecord.getPropertyTerm(), request.getPropertyTerm()) != 0) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(DT_SC.PROPERTY_TERM, request.getPropertyTerm());
        }
        if (compare(dtScRecord.getRepresentationTerm(), request.getRepresentationTerm()) != 0) {
            isRepresentationTermChanged = true;
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(DT_SC.REPRESENTATION_TERM, request.getRepresentationTerm());
        }
        if (!dtScRecord.getCardinalityMax().equals(request.getCardinalityMax())) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(DT_SC.CARDINALITY_MAX, request.getCardinalityMax());
        }
        if (!dtScRecord.getCardinalityMin().equals(request.getCardinalityMin())) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(DT_SC.CARDINALITY_MIN, request.getCardinalityMin());
        }
        if (compare(dtScRecord.getDefinition(), request.getDefinition()) != 0) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(DT_SC.DEFINITION, request.getDefinition());
        }
        if (compare(dtScRecord.getDefinitionSource(), request.getDefinitionSource()) != 0) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(DT_SC.DEFINITION_SOURCE, request.getDefinitionSource());
        }
        if (compare(dtScRecord.getDefaultValue(), request.getDefaultValue()) != 0) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(DT_SC.DEFAULT_VALUE, request.getDefaultValue())
                    .setNull(DT_SC.FIXED_VALUE);
        } else if (compare(dtScRecord.getFixedValue(), request.getFixedValue()) != 0) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(DT_SC.FIXED_VALUE, request.getFixedValue())
                    .setNull(DT_SC.DEFAULT_VALUE);
        }

        if (moreStep != null) {
            moreStep.set(DT_SC.LAST_UPDATED_BY, userId)
                    .set(DT_SC.LAST_UPDATE_TIMESTAMP, timestamp)
                    .where(DT_SC.DT_SC_ID.eq(dtScRecord.getDtScId()))
                    .execute();

            dtScRecord = dslContext.selectFrom(DT_SC)
                    .where(DT_SC.DT_SC_ID.eq(dtScManifestRecord.getDtScId()))
                    .fetchOne();
            updateDerivedSc(dtScManifestRecord, dtScRecord, isRepresentationTermChanged);
        }

        if (isRepresentationTermChanged) {
            deleteCdtScAwdPriByDtScId(dtScRecord.getDtScId());
        }
        updateBdtScPriList(dtScManifestRecord.getDtScManifestId(), request.getCcBdtScPriResriList());

        // creates new log for updated record.
        LogRecord logRecord =
                logRepository.insertBdtLog(
                        dtManifestRecord,
                        dtRecord, dtManifestRecord.getLogId(),
                        LogAction.Modified,
                        userId, timestamp);

        return new UpdateDtScPropertiesRepositoryResponse(dtScManifestRecord.getDtScManifestId().toBigInteger());
    }

    private void deleteDerivedValueDomain(ULong dtScManifestId, List<BdtScPriRestriRecord> deleteList) {
        List<DtScManifestRecord> derivedDtScManifestList = dslContext.selectFrom(DT_SC_MANIFEST)
                .where(DT_SC_MANIFEST.BASED_DT_SC_MANIFEST_ID.eq(dtScManifestId)).fetch();

        List<ULong> cdtScAwdPriXpsTypeMapIdList = deleteList.stream().filter(e -> e.getCdtScAwdPriXpsTypeMapId() != null)
                .map(BdtScPriRestriRecord::getCdtScAwdPriXpsTypeMapId).collect(Collectors.toList());

        List<ULong> codeListManifestId = deleteList.stream().filter(e -> e.getCodeListManifestId() != null)
                .map(BdtScPriRestriRecord::getCodeListManifestId).collect(Collectors.toList());

        List<ULong> agencyIdListManifestId = deleteList.stream().filter(e -> e.getAgencyIdListManifestId() != null)
                .map(BdtScPriRestriRecord::getAgencyIdListManifestId).collect(Collectors.toList());

        for (DtScManifestRecord derivedDtScManifest : derivedDtScManifestList) {
            deleteDerivedValueDomain(derivedDtScManifest.getDtScManifestId(), deleteList);

            dslContext.deleteFrom(BDT_SC_PRI_RESTRI).where(
                            and(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID.eq(derivedDtScManifest.getDtScManifestId())),
                            BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID.in(cdtScAwdPriXpsTypeMapIdList))
                    .execute();
            dslContext.deleteFrom(BDT_SC_PRI_RESTRI).where(
                            and(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID.eq(derivedDtScManifest.getDtScManifestId())),
                            BDT_SC_PRI_RESTRI.CODE_LIST_MANIFEST_ID.in(codeListManifestId))
                    .execute();
            dslContext.deleteFrom(BDT_SC_PRI_RESTRI).where(
                            and(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID.eq(derivedDtScManifest.getDtScManifestId())),
                            BDT_SC_PRI_RESTRI.AGENCY_ID_LIST_MANIFEST_ID.in(agencyIdListManifestId))
                    .execute();

            BdtScPriRestriRecord defaultRecord = dslContext.selectFrom(BDT_SC_PRI_RESTRI).where(
                    and(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID.eq(derivedDtScManifest.getDtScManifestId())),
                    BDT_SC_PRI_RESTRI.IS_DEFAULT.eq((byte) 1)).fetchOne();

            BdtScPriRestriRecord baseDefaultRecord = dslContext.selectFrom(BDT_SC_PRI_RESTRI).where(and(
                    BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID.eq(dtScManifestId),
                    BDT_SC_PRI_RESTRI.IS_DEFAULT.eq((byte) 1))).fetchOne();

            if (defaultRecord == null && baseDefaultRecord != null) {
                if (baseDefaultRecord.getCdtScAwdPriXpsTypeMapId() != null) {
                    dslContext.update(BDT_SC_PRI_RESTRI).set(BDT_SC_PRI_RESTRI.IS_DEFAULT, (byte) 1)
                            .where(and(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID.eq(derivedDtScManifest.getDtScManifestId()),
                                    BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID.eq(baseDefaultRecord.getCdtScAwdPriXpsTypeMapId())))
                            .execute();
                } else if (baseDefaultRecord.getCodeListManifestId() != null) {
                    dslContext.update(BDT_SC_PRI_RESTRI).set(BDT_SC_PRI_RESTRI.IS_DEFAULT, (byte) 1)
                            .where(and(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID.eq(derivedDtScManifest.getDtScManifestId()),
                                    BDT_SC_PRI_RESTRI.CODE_LIST_MANIFEST_ID.eq(baseDefaultRecord.getCodeListManifestId())))
                            .execute();
                } else {
                    dslContext.update(BDT_SC_PRI_RESTRI).set(BDT_SC_PRI_RESTRI.IS_DEFAULT, (byte) 1)
                            .where(and(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID.eq(derivedDtScManifest.getDtScManifestId()),
                                    BDT_SC_PRI_RESTRI.AGENCY_ID_LIST_MANIFEST_ID.eq(baseDefaultRecord.getAgencyIdListManifestId())))
                            .execute();
                }
            }
        }
    }

    private void insertDerivedValueDomain(ULong basedDtScManifestId, List<BdtScPriRestriRecord> insertList) {
        List<DtScManifestRecord> derivedDtScManifestList = dslContext.selectFrom(DT_SC_MANIFEST)
                .where(DT_SC_MANIFEST.BASED_DT_SC_MANIFEST_ID.eq(basedDtScManifestId)).fetch();

        for (DtScManifestRecord dtScManifest : derivedDtScManifestList) {
            for (BdtScPriRestriRecord bdtScPriRestriRecord :
                    insertList.stream().filter(e -> e.getCdtScAwdPriXpsTypeMapId() != null).collect(Collectors.toList())) {
                BdtScPriRestriRecord existRecord = dslContext.selectFrom(BDT_SC_PRI_RESTRI).where(
                        and(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID.eq(dtScManifest.getDtScManifestId())),
                        BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID.eq(bdtScPriRestriRecord.getCdtScAwdPriXpsTypeMapId())).fetchOne();
                if (existRecord == null) {
                    dslContext.insertInto(BDT_SC_PRI_RESTRI)
                            .set(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID, dtScManifest.getDtScManifestId())
                            .set(BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, bdtScPriRestriRecord.getCdtScAwdPriXpsTypeMapId())
                            .set(BDT_SC_PRI_RESTRI.IS_DEFAULT, bdtScPriRestriRecord.getIsDefault()).execute();
                }
            }

            for (BdtScPriRestriRecord bdtScPriRestriRecord :
                    insertList.stream().filter(e -> e.getCodeListManifestId() != null).collect(Collectors.toList())) {
                BdtScPriRestriRecord existRecord = dslContext.selectFrom(BDT_SC_PRI_RESTRI).where(
                        and(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID.eq(dtScManifest.getDtScManifestId())),
                        BDT_SC_PRI_RESTRI.CODE_LIST_MANIFEST_ID.eq(bdtScPriRestriRecord.getCodeListManifestId())).fetchOne();
                if (existRecord == null) {
                    dslContext.insertInto(BDT_SC_PRI_RESTRI)
                            .set(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID, dtScManifest.getDtScManifestId())
                            .set(BDT_SC_PRI_RESTRI.CODE_LIST_MANIFEST_ID, bdtScPriRestriRecord.getCodeListManifestId())
                            .set(BDT_SC_PRI_RESTRI.IS_DEFAULT, bdtScPriRestriRecord.getIsDefault()).execute();
                }
            }

            for (BdtScPriRestriRecord bdtScPriRestriRecord :
                    insertList.stream().filter(e -> e.getAgencyIdListManifestId() != null).collect(Collectors.toList())) {
                BdtScPriRestriRecord existRecord = dslContext.selectFrom(BDT_SC_PRI_RESTRI).where(
                        and(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID.eq(dtScManifest.getDtScManifestId())),
                        BDT_SC_PRI_RESTRI.AGENCY_ID_LIST_MANIFEST_ID.eq(bdtScPriRestriRecord.getAgencyIdListManifestId())).fetchOne();
                if (existRecord == null) {
                    dslContext.insertInto(BDT_SC_PRI_RESTRI)
                            .set(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID, dtScManifest.getDtScManifestId())
                            .set(BDT_SC_PRI_RESTRI.AGENCY_ID_LIST_MANIFEST_ID, bdtScPriRestriRecord.getAgencyIdListManifestId())
                            .set(BDT_SC_PRI_RESTRI.IS_DEFAULT, bdtScPriRestriRecord.getIsDefault()).execute();
                }
            }

            insertDerivedValueDomain(dtScManifest.getDtScManifestId(), insertList);
        }
    }

    private CdtScAwdPriXpsTypeMapRecord createCdtScAwdPriXpsTypeMap(CcBdtScPriRestri restri, ULong dtScId) {

        ULong cdtPriId = dslContext.select(CDT_PRI.CDT_PRI_ID)
                .from(CDT_PRI).where(CDT_PRI.NAME.eq(restri.getPrimitiveName()))
                .fetchOneInto(ULong.class);

        CdtScAwdPriRecord cdtScAwdPriRecord = dslContext.selectFrom(CDT_SC_AWD_PRI)
                .where(and(
                        CDT_SC_AWD_PRI.CDT_PRI_ID.eq(cdtPriId),
                        CDT_SC_AWD_PRI.CDT_SC_ID.eq(dtScId)
                ))
                .fetchOptional().orElse(null);

        if (cdtScAwdPriRecord == null) {
            cdtScAwdPriRecord = new CdtScAwdPriRecord();
            cdtScAwdPriRecord.setCdtPriId(cdtPriId);
            cdtScAwdPriRecord.setCdtScId(dtScId);
            cdtScAwdPriRecord.setIsDefault((byte) (restri.isDefault() ? 1 : 0));
            cdtScAwdPriRecord.setCdtScAwdPriId(
                    dslContext.insertInto(CDT_SC_AWD_PRI)
                            .set(cdtScAwdPriRecord)
                            .returning(CDT_SC_AWD_PRI.CDT_SC_AWD_PRI_ID)
                            .fetchOne().getCdtScAwdPriId()
            );
        }

        CdtScAwdPriXpsTypeMapRecord cdtScAwdPriXpsTypeMapRecord = new CdtScAwdPriXpsTypeMapRecord();
        cdtScAwdPriXpsTypeMapRecord.setCdtScAwdPriId(cdtScAwdPriRecord.getCdtScAwdPriId());
        cdtScAwdPriXpsTypeMapRecord.setXbtId(ULong.valueOf(restri.getXbtId()));
        cdtScAwdPriXpsTypeMapRecord.setCdtScAwdPriXpsTypeMapId(
                dslContext.insertInto(CDT_SC_AWD_PRI_XPS_TYPE_MAP)
                        .set(cdtScAwdPriXpsTypeMapRecord)
                        .returning(CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID)
                        .fetchOne().getCdtScAwdPriXpsTypeMapId()
        );

        return cdtScAwdPriXpsTypeMapRecord;
    }

    private void updateBdtScPriList(ULong dtScManifestId, List<CcBdtScPriRestri> list) {
        List<BdtScPriRestriRecord> records = dslContext
                .selectFrom(BDT_SC_PRI_RESTRI)
                .where(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID.eq(dtScManifestId)).fetch();

        List<BdtScPriRestriRecord> deleteList = new ArrayList<>();

        records.forEach(r -> {
            if (!list.stream().map(CcBdtScPriRestri::getBdtScPriRestriId).collect(Collectors.toList())
                    .contains(r.getBdtScPriRestriId().toBigInteger())) {
                deleteList.add(r);
            }
        });

        if (deleteList.size() > 0) {
            deleteDerivedValueDomain(dtScManifestId, deleteList);
        }

        dslContext.deleteFrom(BDT_SC_PRI_RESTRI).where(BDT_SC_PRI_RESTRI.BDT_SC_PRI_RESTRI_ID.in(
                        deleteList.stream().map(BdtScPriRestriRecord::getBdtScPriRestriId).collect(Collectors.toList())))
                .execute();

        List<BdtScPriRestriRecord> insertedList = new ArrayList<>();
        BdtScPriRestriRecord defaultBdtScPriRestriRecord = null;

        for (CcBdtScPriRestri restri : list) {
            BdtScPriRestriRecord bdtScPriRestriRecord = null;
            if (restri.getBdtScPriRestriId() == null) {
                // insert
                bdtScPriRestriRecord = new BdtScPriRestriRecord();
                bdtScPriRestriRecord.setIsDefault((byte) (restri.isDefault() ? 1 : 0));
                bdtScPriRestriRecord.setBdtScManifestId(dtScManifestId);
                if (restri.getType().equals(PrimitiveRestriType.CodeList)) {
                    bdtScPriRestriRecord.setCodeListManifestId(ULong.valueOf(restri.getCodeListManifestId()));
                } else if (restri.getType().equals(PrimitiveRestriType.AgencyIdList)) {
                    bdtScPriRestriRecord.setAgencyIdListManifestId(ULong.valueOf(restri.getAgencyIdListManifestId()));
                } else {
                    if (restri.getCdtScAwdPriXpsTypeMapId() == null) {
                        ULong dtScId = dslContext.select(DT_SC_MANIFEST.DT_SC_ID).from(DT_SC_MANIFEST)
                                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(dtScManifestId))
                                .fetchOneInto(ULong.class);
                        CdtScAwdPriXpsTypeMapRecord cdtScAwdPriXpsTypeMapRecord = createCdtScAwdPriXpsTypeMap(restri, dtScId);
                        restri.setCdtScAwdPriXpsTypeMapId(cdtScAwdPriXpsTypeMapRecord.getCdtScAwdPriXpsTypeMapId().toBigInteger());
                    }

                    bdtScPriRestriRecord.setCdtScAwdPriXpsTypeMapId(
                            ULong.valueOf(restri.getCdtScAwdPriXpsTypeMapId()));
                }
                restri.setBdtScPriRestriId(dslContext.insertInto(BDT_SC_PRI_RESTRI)
                        .set(bdtScPriRestriRecord)
                        .returning(BDT_SC_PRI_RESTRI.BDT_SC_PRI_RESTRI_ID).fetchOne().getBdtScPriRestriId().toBigInteger());

                insertedList.add(bdtScPriRestriRecord);
            } else {
                // update
                bdtScPriRestriRecord = dslContext.selectFrom(BDT_SC_PRI_RESTRI)
                        .where(BDT_SC_PRI_RESTRI.BDT_SC_PRI_RESTRI_ID.eq(ULong.valueOf(restri.getBdtScPriRestriId())))
                        .fetchOne();

                if (restri.getCdtScAwdPriXpsTypeMapId() != null) {
                    bdtScPriRestriRecord.setCdtScAwdPriXpsTypeMapId(ULong.valueOf(restri.getCdtScAwdPriXpsTypeMapId()));
                    bdtScPriRestriRecord.setCodeListManifestId(null);
                    bdtScPriRestriRecord.setAgencyIdListManifestId(null);
                } else if (restri.getCodeListManifestId() != null) {
                    bdtScPriRestriRecord.setCdtScAwdPriXpsTypeMapId(null);
                    bdtScPriRestriRecord.setCodeListManifestId(ULong.valueOf(restri.getCodeListManifestId()));
                    bdtScPriRestriRecord.setAgencyIdListManifestId(null);
                } else if (restri.getAgencyIdListManifestId() != null) {
                    bdtScPriRestriRecord.setCdtScAwdPriXpsTypeMapId(null);
                    bdtScPriRestriRecord.setCodeListManifestId(null);
                    bdtScPriRestriRecord.setAgencyIdListManifestId(ULong.valueOf(restri.getAgencyIdListManifestId()));
                }

                bdtScPriRestriRecord.setIsDefault((byte) (restri.isDefault() ? 1 : 0));
                bdtScPriRestriRecord.update();
            }

            if (restri.isDefault()) {
                defaultBdtScPriRestriRecord = bdtScPriRestriRecord;
            }
        }

        if (defaultBdtScPriRestriRecord == null) {
            throw new IllegalArgumentException("Default Value Domain required.");
        }

        insertDerivedValueDomain(dtScManifestId, insertedList);
        updateValueDomain(dtScManifestId, defaultBdtScPriRestriRecord);
    }

    private void updateValueDomain(ULong dtScManifestId, BdtScPriRestriRecord defaultBdtScPriRestriRecord) {
        for (BdtScPriRestriRecord bdtScPriRestriRecord :
                dslContext.selectFrom(BDT_SC_PRI_RESTRI).where(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID.eq(dtScManifestId))) {
            if (defaultBdtScPriRestriRecord.getCdtScAwdPriXpsTypeMapId() != null) {
                if (defaultBdtScPriRestriRecord.getCdtScAwdPriXpsTypeMapId().equals(bdtScPriRestriRecord.getCdtScAwdPriXpsTypeMapId())) {
                    bdtScPriRestriRecord.setIsDefault((byte) 1);
                } else {
                    bdtScPriRestriRecord.setIsDefault((byte) 0);
                }
            } else if (defaultBdtScPriRestriRecord.getCodeListManifestId() != null) {
                if (defaultBdtScPriRestriRecord.getCodeListManifestId().equals(bdtScPriRestriRecord.getCodeListManifestId())) {
                    bdtScPriRestriRecord.setIsDefault((byte) 1);
                } else {
                    bdtScPriRestriRecord.setIsDefault((byte) 0);
                }
            } else if (defaultBdtScPriRestriRecord.getAgencyIdListManifestId() != null) {
                if (defaultBdtScPriRestriRecord.getAgencyIdListManifestId().equals(bdtScPriRestriRecord.getAgencyIdListManifestId())) {
                    bdtScPriRestriRecord.setIsDefault((byte) 1);
                } else {
                    bdtScPriRestriRecord.setIsDefault((byte) 0);
                }
            }

            bdtScPriRestriRecord.update(BDT_SC_PRI_RESTRI.IS_DEFAULT);
        }

        for (DtScManifestRecord derivedDtScManifestRecord :
                dslContext.selectFrom(DT_SC_MANIFEST).where(DT_SC_MANIFEST.BASED_DT_SC_MANIFEST_ID.eq(dtScManifestId)).fetch()) {
            updateValueDomain(derivedDtScManifestRecord.getDtScManifestId(), defaultBdtScPriRestriRecord);
        }
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

    public DeleteDtScRepositoryResponse deleteDtSc(DeleteDtScRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        DtScManifestRecord dtScManifestRecord = dslContext.selectFrom(DT_SC_MANIFEST)
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(
                        ULong.valueOf(request.getDtScManifestId())
                ))
                .fetchOne();

        // Delete all DT_SCs derived from the target DT_SC in the request
        for (DtScManifestRecord derivedDtScManifestRecord : dslContext.selectFrom(DT_SC_MANIFEST)
                .where(DT_SC_MANIFEST.BASED_DT_SC_MANIFEST_ID.eq(dtScManifestRecord.getDtScManifestId()))
                .fetch()) {
            deleteDtSc(new DeleteDtScRepositoryRequest(request.getUser(), request.getLocalDateTime(),
                    derivedDtScManifestRecord.getDtScManifestId().toBigInteger()));
        }

        DtScRecord dtScRecord = dslContext.selectFrom(DT_SC)
                .where(DT_SC.DT_SC_ID.eq(dtScManifestRecord.getDtScId()))
                .fetchOne();

        DtManifestRecord dtManifestRecord = dslContext.selectFrom(DT_MANIFEST)
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(dtScManifestRecord.getOwnerDtManifestId())).fetchOne();

        DtRecord dtRecord = dslContext.selectFrom(DT)
                .where(DT.DT_ID.eq(dtManifestRecord.getDtId())).fetchOne();

        if (!CcState.WIP.equals(CcState.valueOf(dtRecord.getState()))) {
            throw new IllegalArgumentException("Only the core component in 'WIP' state can be deleted.");
        }

        if (!dtRecord.getOwnerUserId().equals(userId)) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        int usedBieCount = dslContext.selectCount().from(BBIE_SC)
                .where(BBIE_SC.BASED_DT_SC_MANIFEST_ID.eq(dtScManifestRecord.getDtScManifestId())).fetchOne(0, int.class);

        if (usedBieCount > 0) {
            throw new IllegalArgumentException("This association used in " + usedBieCount + " BIE(s). Can not be deleted.");
        }

        // delete from Tables
        dslContext.deleteFrom(BDT_SC_PRI_RESTRI)
                .where(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID.eq(dtScManifestRecord.getDtScManifestId()))
                .execute();

        for (CdtScAwdPriRecord cdtScAwdPriRecord : dslContext.selectFrom(CDT_SC_AWD_PRI)
                .where(CDT_SC_AWD_PRI.CDT_SC_ID.eq(dtScRecord.getDtScId()))
                .fetch()) {
            dslContext.deleteFrom(CDT_SC_AWD_PRI_XPS_TYPE_MAP)
                    .where(CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_ID.eq(cdtScAwdPriRecord.getCdtScAwdPriId()))
                    .execute();
            cdtScAwdPriRecord.delete();
        }

        DtScManifestRecord prevDtScManifestRecord = null;
        DtScManifestRecord nextDtScManifestRecord = null;
        if (dtScManifestRecord.getPrevDtScManifestId() != null) {
            prevDtScManifestRecord = dslContext.selectFrom(DT_SC_MANIFEST)
                    .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(dtScManifestRecord.getPrevDtScManifestId()))
                    .fetchOne();
        }
        if (dtScManifestRecord.getNextDtScManifestId() != null) {
            nextDtScManifestRecord = dslContext.selectFrom(DT_SC_MANIFEST)
                    .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(dtScManifestRecord.getNextDtScManifestId()))
                    .fetchOne();
        }
        if (prevDtScManifestRecord != null) {
            if (nextDtScManifestRecord != null) {
                dslContext.update(DT_SC_MANIFEST)
                        .set(DT_SC_MANIFEST.NEXT_DT_SC_MANIFEST_ID, nextDtScManifestRecord.getDtScManifestId())
                        .execute();
            } else {
                dslContext.update(DT_SC_MANIFEST)
                        .setNull(DT_SC_MANIFEST.NEXT_DT_SC_MANIFEST_ID)
                        .execute();
            }
        }
        if (nextDtScManifestRecord != null) {
            if (prevDtScManifestRecord != null) {
                dslContext.update(DT_SC_MANIFEST)
                        .set(DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID, prevDtScManifestRecord.getDtScManifestId())
                        .execute();
            } else {
                dslContext.update(DT_SC_MANIFEST)
                        .setNull(DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID)
                        .execute();
            }
        }
        dtScManifestRecord.delete();

        if (dslContext.selectCount().from(DT_SC_MANIFEST)
                .where(DT_SC_MANIFEST.DT_SC_ID.eq(dtScManifestRecord.getDtScId()))
                .fetchOneInto(Integer.class) == 0) {

            deleteCdtScAwdPriByDtScId(dtScRecord.getDtScId());

            DtScRecord prevDtScRecord = null;
            DtScRecord nextDtScRecord = null;
            if (dtScRecord.getPrevDtScId() != null) {
                prevDtScRecord = dslContext.selectFrom(DT_SC)
                        .where(DT_SC.DT_SC_ID.eq(dtScRecord.getPrevDtScId()))
                        .fetchOne();
            }
            if (dtScRecord.getNextDtScId() != null) {
                nextDtScRecord = dslContext.selectFrom(DT_SC)
                        .where(DT_SC.DT_SC_ID.eq(dtScRecord.getNextDtScId()))
                        .fetchOne();
            }
            if (prevDtScRecord != null) {
                if (nextDtScRecord != null) {
                    dslContext.update(DT_SC)
                            .set(DT_SC.NEXT_DT_SC_ID, nextDtScRecord.getDtScId())
                            .execute();
                } else {
                    dslContext.update(DT_SC)
                            .setNull(DT_SC.NEXT_DT_SC_ID)
                            .execute();
                }
            }
            if (nextDtScRecord != null) {
                if (prevDtScRecord != null) {
                    dslContext.update(DT_SC)
                            .set(DT_SC.PREV_DT_SC_ID, prevDtScRecord.getDtScId())
                            .execute();
                } else {
                    dslContext.update(DT_SC)
                            .setNull(DT_SC.PREV_DT_SC_ID)
                            .execute();
                }
            }
            dtScRecord.delete();
        }

        LogRecord logRecord =
                logRepository.insertBdtLog(
                        dtManifestRecord,
                        dtRecord,
                        LogAction.Modified,
                        userId, timestamp);
        dtManifestRecord.setLogId(logRecord.getLogId());
        dtManifestRecord.update(DT_MANIFEST.LOG_ID);

        return new DeleteDtScRepositoryResponse(dtScManifestRecord.getDtScManifestId().toBigInteger());
    }
}