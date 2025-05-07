package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

import jakarta.annotation.Nullable;
import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.*;
import org.oagi.score.gateway.http.api.cc_management.model.dt.*;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.*;
import org.oagi.score.gateway.http.api.cc_management.repository.DtCommandRepository;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.log_management.model.LogId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.*;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;
import org.oagi.score.gateway.http.common.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.compare;
import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.inline;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Acc.ACC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Bccp.BCCP;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.BccpManifest.BCCP_MANIFEST;
import static org.springframework.util.StringUtils.hasLength;

public class JooqDtCommandRepository extends JooqBaseRepository implements DtCommandRepository {

    public JooqDtCommandRepository(DSLContext dslContext, ScoreUser requester,
                                   RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public DtManifestId create(
            ReleaseId releaseId,
            DtManifestId basedDtManifestId) {

        if (releaseId == null) {
            throw new IllegalArgumentException("`releaseId` must not be null");
        }

        if (basedDtManifestId == null) {
            throw new IllegalArgumentException("`basedDtManifestId` must not be null");
        }

        var query = repositoryFactory().dtQueryRepository(requester());

        DtDetailsRecord basedDt = query.getDtDetails(basedDtManifestId);
        List<DtScDetailsRecord> basedDtScList = query.getDtScDetailsList(basedDt.dtManifestId());

        DtRecord dt = new DtRecord();
        dt.setGuid(ScoreGuidUtils.randomGuid());
        dt.setDataTypeTerm(basedDt.dataTypeTerm());
        if (basedDt.qualifier() != null) {
            dt.setQualifier_(basedDt.qualifier());
        }

        dt.setRepresentationTerm(basedDt.representationTerm());
        dt.setBasedDtId(valueOf(basedDt.dtId()));
        dt.setContentComponentDefinition(basedDt.contentComponentDefinition());
        if (basedDt.definition() != null) {
            dt.setDefinition(basedDt.definition().content());
            dt.setDefinitionSource(basedDt.definition().source());
        }
        dt.setState(CcState.WIP.name());
        dt.setIsDeprecated((byte) 0);
        dt.setCommonlyUsed((byte) 0);

        if (basedDt.namespace() != null) {
            if (requester().isDeveloper() && basedDt.namespace().standard()) {
                dt.setNamespaceId(valueOf(basedDt.namespace().namespaceId()));
            } else if (!requester().isDeveloper() && !basedDt.namespace().standard()) {
                dt.setNamespaceId(valueOf(basedDt.namespace().namespaceId()));
            }
        }

        UserId requesterId = requester().userId();
        dt.setCreatedBy(valueOf(requesterId));
        dt.setLastUpdatedBy(valueOf(requesterId));
        dt.setOwnerUserId(valueOf(requesterId));
        LocalDateTime timestamp = LocalDateTime.now();
        dt.setCreationTimestamp(timestamp);
        dt.setLastUpdateTimestamp(timestamp);
        DtId dtId = new DtId(dslContext().insertInto(DT)
                .set(dt)
                .returning(DT.DT_ID).fetchOne().getDtId().toBigInteger());

        DtManifestRecord dtManifest = new DtManifestRecord();
        dtManifest.setDtId(valueOf(dtId));
        dtManifest.setBasedDtManifestId(valueOf(basedDt.dtManifestId()));
        dtManifest.setReleaseId(valueOf(releaseId));
        dtManifest.setDen(((dt.getQualifier_() != null) ? (dt.getQualifier_() + "_ ") : "") + dt.getDataTypeTerm() + ". Type");
        DtManifestId dtManifestId = new DtManifestId(
                dslContext().insertInto(DT_MANIFEST)
                        .set(dtManifest)
                        .returning(DT_MANIFEST.DT_MANIFEST_ID)
                        .fetchOne().getDtManifestId().toBigInteger());

        copyDtAwdPriFromBase(releaseId, dtId, basedDt.dtManifestId());

        for (DtScDetailsRecord basedDtSc : query.getDtScDetailsList(basedDtManifestId)) {

            DtScRecord dtScRecord = new DtScRecord();
            DtScManifestRecord dtScManifestRecord = new DtScManifestRecord();

            dtScRecord.setGuid(ScoreGuidUtils.randomGuid());
            dtScRecord.setObjectClassTerm(basedDtSc.objectClassTerm());
            dtScRecord.setPropertyTerm(basedDtSc.propertyTerm());
            dtScRecord.setRepresentationTerm(basedDtSc.representationTerm());
            if (basedDtSc.definition() != null) {
                dtScRecord.setDefinition(basedDtSc.definition().content());
                dtScRecord.setDefinitionSource(basedDtSc.definition().source());
            }
            dtScRecord.setOwnerDtId(dt.getDtId());
            dtScRecord.setCardinalityMin(basedDtSc.cardinality().min());
            dtScRecord.setCardinalityMax(basedDtSc.cardinality().max());
            dtScRecord.setBasedDtScId(valueOf(basedDtSc.dtScId()));
            if (basedDtSc.valueConstraint() != null) {
                dtScRecord.setDefaultValue(basedDtSc.valueConstraint().defaultValue());
                dtScRecord.setFixedValue(basedDtSc.valueConstraint().fixedValue());
            }
            dtScRecord.setCreatedBy(valueOf(requesterId));
            dtScRecord.setLastUpdatedBy(valueOf(requesterId));
            dtScRecord.setOwnerUserId(valueOf(requesterId));
            dtScRecord.setCreationTimestamp(timestamp);
            dtScRecord.setLastUpdateTimestamp(timestamp);
            DtScId dtScId = new DtScId(
                    dslContext().insertInto(DT_SC)
                            .set(dtScRecord)
                            .returning(DT_SC.DT_SC_ID)
                            .fetchOne().getDtScId().toBigInteger()
            );

            dtScManifestRecord.setReleaseId(valueOf(basedDt.release().releaseId()));
            dtScManifestRecord.setDtScId(valueOf(dtScId));
            dtScManifestRecord.setBasedDtScManifestId(valueOf(basedDtSc.dtScManifestId()));
            dtScManifestRecord.setOwnerDtManifestId(valueOf(dtManifestId));

            DtScManifestId dtScManifestId = new DtScManifestId(
                    dslContext().insertInto(DT_SC_MANIFEST)
                            .set(dtScManifestRecord)
                            .returning(DT_SC_MANIFEST.DT_SC_MANIFEST_ID)
                            .fetchOne().getDtScManifestId().toBigInteger());

            copyDtScAwdPriFromBase(releaseId, dtScId, basedDtSc.dtScManifestId());
        }

        return dtManifestId;
    }

    private void copyDtAwdPriFromBase(ReleaseId releaseId, DtId dtId, DtManifestId basedDtManifestId) {
        dslContext().insertInto(DT_AWD_PRI,
                        DT_AWD_PRI.RELEASE_ID,
                        DT_AWD_PRI.DT_ID,
                        DT_AWD_PRI.XBT_MANIFEST_ID,
                        DT_AWD_PRI.CODE_LIST_MANIFEST_ID,
                        DT_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID,
                        DT_AWD_PRI.IS_DEFAULT)
                .select(dslContext().select(
                                inline(valueOf(releaseId)),
                                inline(valueOf(dtId)),
                                DT_AWD_PRI.XBT_MANIFEST_ID,
                                DT_AWD_PRI.CODE_LIST_MANIFEST_ID,
                                DT_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID,
                                DT_AWD_PRI.IS_DEFAULT)
                        .from(DT_AWD_PRI)
                        .join(DT_MANIFEST).on(and(
                                DT_AWD_PRI.RELEASE_ID.eq(DT_MANIFEST.RELEASE_ID),
                                DT_AWD_PRI.DT_ID.eq(DT_MANIFEST.DT_ID)
                        ))
                        .where(DT_MANIFEST.DT_MANIFEST_ID.eq(valueOf(basedDtManifestId))))
                .execute();
    }

    private void copyDtScAwdPriFromBase(ReleaseId releaseId, DtScId dtScId,
                                        DtScManifestId basedDtScManifestId) {
        dslContext().insertInto(DT_SC_AWD_PRI,
                        DT_SC_AWD_PRI.RELEASE_ID,
                        DT_SC_AWD_PRI.DT_SC_ID,
                        DT_SC_AWD_PRI.XBT_MANIFEST_ID,
                        DT_SC_AWD_PRI.CODE_LIST_MANIFEST_ID,
                        DT_SC_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID,
                        DT_SC_AWD_PRI.IS_DEFAULT)
                .select(dslContext().select(
                                inline(valueOf(releaseId)),
                                inline(valueOf(dtScId)),
                                DT_SC_AWD_PRI.XBT_MANIFEST_ID,
                                DT_SC_AWD_PRI.CODE_LIST_MANIFEST_ID,
                                DT_SC_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID,
                                DT_SC_AWD_PRI.IS_DEFAULT)
                        .from(DT_SC_AWD_PRI)
                        .join(DT_SC_MANIFEST).on(and(
                                DT_SC_AWD_PRI.RELEASE_ID.eq(DT_SC_MANIFEST.RELEASE_ID),
                                DT_SC_AWD_PRI.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID)
                        ))
                        .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(valueOf(basedDtScManifestId))))
                .execute();
    }

    @Override
    public DtScManifestId appendDtSc(DtManifestId ownerDtManifestId) {

        if (ownerDtManifestId == null) {
            throw new IllegalArgumentException("`ownerDtManifestId` must not be null.");
        }

        var query = repositoryFactory().dtQueryRepository(requester());
        DtSummaryRecord dt = query.getDtSummary(ownerDtManifestId);
        if (dt == null) {
            throw new IllegalArgumentException("DT record not found.");
        }

        DtScRecord dtScRecord = new DtScRecord();
        dtScRecord.setGuid(ScoreGuidUtils.randomGuid());
        dtScRecord.setObjectClassTerm(dt.dataTypeTerm());
        dtScRecord.setPropertyTerm(getUniquePropertyTerm(dt.release().releaseId()));

        String defaultRepresentationTerm = "Text";
        dtScRecord.setRepresentationTerm(defaultRepresentationTerm);
        dtScRecord.setOwnerDtId(valueOf(dt.dtId()));
        dtScRecord.setCardinalityMin(0);
        dtScRecord.setCardinalityMax(1);
        dtScRecord.setCreatedBy(valueOf(requester().userId()));
        dtScRecord.setLastUpdatedBy(valueOf(requester().userId()));
        dtScRecord.setOwnerUserId(valueOf(requester().userId()));
        LocalDateTime timestamp = LocalDateTime.now();
        dtScRecord.setCreationTimestamp(timestamp);
        dtScRecord.setLastUpdateTimestamp(timestamp);

        DtScId dtScId = new DtScId(
                dslContext().insertInto(DT_SC)
                        .set(dtScRecord)
                        .returning(DT_SC.DT_SC_ID)
                        .fetchOne().getDtScId().toBigInteger());

        DtScManifestRecord dtScManifestRecord = new DtScManifestRecord();
        dtScManifestRecord.setDtScId(valueOf(dtScId));
        dtScManifestRecord.setReleaseId(valueOf(dt.release().releaseId()));
        dtScManifestRecord.setOwnerDtManifestId(valueOf(dt.dtManifestId()));

        DtScManifestId dtScManifestId = new DtScManifestId(
                dslContext().insertInto(DT_SC_MANIFEST)
                        .set(dtScManifestRecord)
                        .returning(DT_SC_MANIFEST.DT_SC_MANIFEST_ID)
                        .fetchOne().getDtScManifestId().toBigInteger());

        insertDtScAwdPriByRepresentationTerm(dtScManifestId, defaultRepresentationTerm);

        return dtScManifestId;
    }

    private String getUniquePropertyTerm(ReleaseId releaseId) {
        List<String> propertyTerms = dslContext().selectDistinct(DT_SC.PROPERTY_TERM)
                .from(DT_SC)
                .join(DT_SC_MANIFEST).on(DT_SC.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID))
                .join(RELEASE).on(DT_SC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        RELEASE.RELEASE_ID.eq(valueOf(releaseId)),
                        DT_SC.PROPERTY_TERM.like("Property Term%")
                ))
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

    private void insertDtScAwdPriByRepresentationTerm(DtScManifestId dtScManifestId, String representationTerm) {

        if (dtScManifestId == null) {
            throw new IllegalArgumentException("`dtScManifestId` must not be null.");
        }

        var query = repositoryFactory().dtQueryRepository(requester());
        List<DtAwdPriDetailsRecord> dtAwdPriList = query.getDefaultPrimitiveValues(representationTerm);

        DtScSummaryRecord dtSc = query.getDtScSummary(dtScManifestId);
        if (dtSc == null) {
            throw new IllegalArgumentException("DT_SC record not found.");
        }

        dslContext().batch(
                dtAwdPriList.stream()
                        .filter(e -> e.xbt() != null)
                        .map(dtAwdPri -> {
                            DtScAwdPriRecord dtScAwdPriRecord = new DtScAwdPriRecord();
                            dtScAwdPriRecord.setReleaseId(valueOf(dtSc.release().releaseId()));
                            dtScAwdPriRecord.setDtScId(valueOf(dtSc.dtScId()));
                            dtScAwdPriRecord.setXbtManifestId(valueOf(dtAwdPri.xbt().xbtManifestId()));
                            dtScAwdPriRecord.setIsDefault((byte) (dtAwdPri.isDefault() ? 1 : 0));

                            return dslContext().insertInto(DT_SC_AWD_PRI).set(dtScAwdPriRecord);
                        }).collect(Collectors.toList())
        ).execute();
    }

    @Override
    public DtScManifestId createDtScFromBase(DtManifestId ownerDtManifestId, DtScManifestId basedDtScManifestId) {

        if (ownerDtManifestId == null) {
            throw new IllegalArgumentException("`ownerDtManifestId` must not be null.");
        }

        if (basedDtScManifestId == null) {
            throw new IllegalArgumentException("`basedDtScManifestId` must not be null.");
        }

        var query = repositoryFactory().dtQueryRepository(requester());
        DtSummaryRecord dt = query.getDtSummary(ownerDtManifestId);
        if (dt == null) {
            throw new IllegalArgumentException("DT record not found.");
        }

        DtScSummaryRecord basedDtSc = query.getDtScSummary(basedDtScManifestId);
        if (basedDtSc == null) {
            throw new IllegalArgumentException("DT_SC record not found.");
        }

        DtScRecord dtScRecord = new DtScRecord();
        dtScRecord.setGuid(basedDtSc.guid().value());
        dtScRecord.setObjectClassTerm(basedDtSc.objectClassTerm());
        dtScRecord.setPropertyTerm(basedDtSc.propertyTerm());
        dtScRecord.setRepresentationTerm(basedDtSc.representationTerm());
        dtScRecord.setOwnerDtId(valueOf(dt.dtId()));
        dtScRecord.setCardinalityMin(basedDtSc.cardinality().min());
        dtScRecord.setCardinalityMax(basedDtSc.cardinality().max());
        dtScRecord.setCreatedBy(valueOf(requester().userId()));
        dtScRecord.setLastUpdatedBy(valueOf(requester().userId()));
        dtScRecord.setOwnerUserId(valueOf(requester().userId()));
        LocalDateTime timestamp = LocalDateTime.now();
        dtScRecord.setCreationTimestamp(timestamp);
        dtScRecord.setLastUpdateTimestamp(timestamp);

        DtScId dtScId = new DtScId(
                dslContext().insertInto(DT_SC)
                        .set(dtScRecord)
                        .returning(DT_SC.DT_SC_ID)
                        .fetchOne().getDtScId().toBigInteger());

        DtScManifestRecord dtScManifestRecord = new DtScManifestRecord();
        dtScManifestRecord.setDtScId(valueOf(dtScId));
        dtScManifestRecord.setReleaseId(valueOf(dt.release().releaseId()));
        dtScManifestRecord.setOwnerDtManifestId(valueOf(dt.dtManifestId()));
        dtScManifestRecord.setBasedDtScManifestId(valueOf(basedDtSc.dtScManifestId()));

        DtScManifestId dtScManifestId = new DtScManifestId(
                dslContext().insertInto(DT_SC_MANIFEST)
                        .set(dtScManifestRecord)
                        .returning(DT_SC_MANIFEST.DT_SC_MANIFEST_ID)
                        .fetchOne().getDtScManifestId().toBigInteger());

        copyDtScAwdPriFromBase(basedDtSc.release().releaseId(), dtScId, basedDtSc.dtScManifestId());

        return dtScManifestId;
    }

    @Override
    public boolean updateLogId(DtManifestId dtManifestId, LogId logId) {
        int numOfUpdatedRecords = dslContext().update(DT_MANIFEST)
                .set(DT_MANIFEST.LOG_ID, valueOf(logId))
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(valueOf(dtManifestId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean update(DtManifestId dtManifestId,
                          @Nullable String qualifier,
                          @Nullable String sixDigitId,
                          @Nullable Boolean deprecated,
                          @Nullable NamespaceId namespaceId,
                          @Nullable String contentComponentDefinition,
                          @Nullable Definition definition) {

        var query = repositoryFactory().dtQueryRepository(requester());
        DtSummaryRecord dt = query.getDtSummary(dtManifestId);

        // update bdt record.
        UpdateSetFirstStep<DtRecord> firstStep = dslContext().update(DT);
        UpdateSetMoreStep<DtRecord> moreStep = null;
        boolean denChanged = false;
        String newDen = null;
        if (qualifier != null && compare(dt.qualifier(), qualifier) != 0) {
            denChanged = true;
            if (StringUtils.hasLength(qualifier)) {
                newDen = qualifier + "_ " + dt.representationTerm() + ". Type";
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .set(DT.QUALIFIER, qualifier);
            } else {
                newDen = dt.representationTerm() + ". Type";
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .setNull(DT.QUALIFIER);
            }

            dslContext().update(DT_MANIFEST)
                    .set(DT_MANIFEST.DEN, newDen)
                    .where(DT_MANIFEST.DT_MANIFEST_ID.eq(valueOf(dtManifestId)))
                    .execute();
        }
        if (sixDigitId != null && compare(dt.sixDigitId(), sixDigitId) != 0) {
            if (hasLength(sixDigitId)) {
                if (query.hasDuplicateSixDigitId(dtManifestId)) {
                    throw new IllegalArgumentException("Six Digit Id '" + sixDigitId + "' already exist.");
                }
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .set(DT.SIX_DIGIT_ID, sixDigitId);
            } else {
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .setNull(DT.SIX_DIGIT_ID);
            }
        }
        if (contentComponentDefinition != null && compare(dt.contentComponentDefinition(), contentComponentDefinition) != 0) {
            if (hasLength(contentComponentDefinition)) {
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .set(DT.CONTENT_COMPONENT_DEFINITION, contentComponentDefinition);
            } else {
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .setNull(DT.CONTENT_COMPONENT_DEFINITION);
            }
        }
        if (definition != null) {
            if (dt.definition() == null || compare(dt.definition().content(), definition.content()) != 0) {
                if (StringUtils.hasLength(definition.content())) {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .set(DT.DEFINITION, definition.content());
                } else {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .setNull(DT.DEFINITION);
                }
            }
            if (dt.definition() == null || compare(dt.definition().source(), definition.source()) != 0) {
                if (StringUtils.hasLength(definition.source())) {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .set(DT.DEFINITION_SOURCE, definition.source());
                } else {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .setNull(DT.DEFINITION_SOURCE);
                }
            }
        }
        if (deprecated != null && dt.deprecated() != deprecated) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(DT.IS_DEPRECATED, (byte) (deprecated ? 1 : 0));
        }
        if (namespaceId != null) {
            if (!namespaceId.equals(dt.namespaceId())) {
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .set(DT.NAMESPACE_ID, valueOf(namespaceId));
            }
        } else {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .setNull(DT.NAMESPACE_ID);
        }

        int numOfUpdatedRecords = 0;
        if (moreStep != null) {
            numOfUpdatedRecords = moreStep.set(DT.LAST_UPDATED_BY, valueOf(requester().userId()))
                    .set(DT.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                    .where(DT.DT_ID.eq(valueOf(dt.dtId())))
                    .execute();
        }

        if (denChanged) {
            for (Record3<ULong, ULong, String> bccp : dslContext().select(BCCP_MANIFEST.BCCP_MANIFEST_ID, BCCP.BCCP_ID, BCCP.PROPERTY_TERM)
                    .from(BCCP_MANIFEST)
                    .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                    .where(BCCP_MANIFEST.BDT_MANIFEST_ID.eq(valueOf(dtManifestId)))
                    .fetch()) {

                String newBccpDen = bccp.get(BCCP.PROPERTY_TERM) + ". " + newDen.replaceAll(". Type", "");
                dslContext().update(BCCP_MANIFEST)
                        .set(BCCP_MANIFEST.DEN, newBccpDen)
                        .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(bccp.get(BCCP_MANIFEST.BCCP_MANIFEST_ID)))
                        .execute();

                for (Record2<ULong, String> bcc : dslContext().select(BCC_MANIFEST.BCC_MANIFEST_ID, ACC.OBJECT_CLASS_TERM)
                        .from(BCC_MANIFEST)
                        .join(ACC_MANIFEST).on(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                        .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                        .where(BCC_MANIFEST.TO_BCCP_MANIFEST_ID.eq(bccp.get(BCCP_MANIFEST.BCCP_MANIFEST_ID)))
                        .fetch()) {

                    String newBccDen = bcc.get(ACC.OBJECT_CLASS_TERM) + ". " + newBccpDen;
                    dslContext().update(BCC_MANIFEST)
                            .set(BCC_MANIFEST.DEN, newBccDen)
                            .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(bcc.get(BCC_MANIFEST.BCC_MANIFEST_ID)))
                            .execute();
                }
            }
        }

        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean updateState(DtManifestId dtManifestId, CcState nextState) {

        var query = repositoryFactory().dtQueryRepository(requester());
        DtSummaryRecord dt = query.getDtSummary(dtManifestId);

        CcState prevState = dt.state();

        UpdateSetMoreStep moreStep = dslContext().update(DT)
                .set(DT.STATE, nextState.name());

        // Change owner of CC when it restored.
        if (prevState == CcState.Deleted && nextState == CcState.WIP) {
            moreStep = moreStep.set(DT.OWNER_USER_ID, valueOf(requester().userId()));
        }

        if (!prevState.isImplicitMove(nextState)) {
            moreStep = moreStep.set(DT.LAST_UPDATED_BY, valueOf(requester().userId()))
                    .set(DT.LAST_UPDATE_TIMESTAMP, LocalDateTime.now());
        }

        int numOfUpdatedRecords = moreStep.where(DT.DT_ID.eq(valueOf(dt.dtId())))
                .execute();
        boolean updated = numOfUpdatedRecords == 1;

        if (updated) {
            // update associations' state.
            updateDtScState(dt, nextState);

            // Post-processing
            if (nextState == CcState.Published || nextState == CcState.Production) {
                // Issue #1298
                // Update 'deprecated' properties in associated BIEs
                if (dt.deprecated()) {
                    dslContext().update(BBIE_SC.join(BBIE).on(BBIE_SC.BBIE_ID.eq(BBIE.BBIE_ID))
                                    .join(BBIEP).on(BBIE.TO_BBIEP_ID.eq(BBIEP.BBIEP_ID))
                                    .join(BCCP_MANIFEST).on(BBIEP.BASED_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.BCCP_MANIFEST_ID)))
                            .set(BBIE.IS_DEPRECATED, (byte) 1)
                            .set(BBIE_SC.IS_DEPRECATED, (byte) 1)
                            .where(BCCP_MANIFEST.BDT_MANIFEST_ID.eq(valueOf(dtManifestId)))
                            .execute();
                }
            }
        }

        return updated;
    }

    private void updateDtScState(DtSummaryRecord dt, CcState nextState) {
        CcState prevState = dt.state();

        UpdateSetFirstStep firstStep = dslContext().update(DT_SC);
        UpdateSetMoreStep moreStep = null;

        // Change owner of CC when it restored.
        if (prevState == CcState.Deleted && nextState == CcState.WIP) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(DT_SC.OWNER_USER_ID, valueOf(requester().userId()));
        }

        if (!prevState.isImplicitMove(nextState)) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(DT_SC.LAST_UPDATED_BY, valueOf(requester().userId()))
                    .set(DT_SC.LAST_UPDATE_TIMESTAMP, LocalDateTime.now());
        }

        var query = repositoryFactory().dtQueryRepository(requester());
        var dtScList = query.getDtScSummaryList(dt.dtManifestId());
        if (moreStep != null) {
            moreStep.where(DT_SC.DT_SC_ID.in(valueOf(
                            dtScList.stream()
                                    .map(e -> e.dtScId()).collect(Collectors.toSet())
                    )))
                    .execute();
        }

        // Post-processing
        if (nextState == CcState.Published || nextState == CcState.Production) {
            // Issue #1298
            // Update 'deprecated' properties in associated BIEs
            Collection<DtScManifestId> deprecatedDtScManifestIdList =
                    dtScList.stream().filter(e -> e.deprecated())
                            .map(e -> e.dtScManifestId()).collect(Collectors.toSet());
            if (!deprecatedDtScManifestIdList.isEmpty()) {
                dslContext().update(BBIE_SC)
                        .set(BBIE_SC.IS_DEPRECATED, (byte) 1)
                        .where(BBIE_SC.BASED_DT_SC_MANIFEST_ID.in(valueOf(deprecatedDtScManifestIdList)))
                        .execute();
            }
        }
    }

    @Override
    public boolean updateDtSc(DtScManifestId dtScManifestId,
                              @Nullable String propertyTerm,
                              @Nullable String representationTerm,
                              @Nullable Cardinality cardinality,
                              @Nullable Boolean deprecated,
                              @Nullable ValueConstraint valueConstraint,
                              @Nullable Definition definition) {

        var query = repositoryFactory().dtQueryRepository(requester());
        DtScSummaryRecord dtSc = query.getDtScSummary(dtScManifestId);

        // update bdtSc record.
        UpdateSetFirstStep<DtScRecord> firstStep = dslContext().update(DT_SC);
        UpdateSetMoreStep<DtScRecord> moreStep = null;
        if (propertyTerm != null && compare(dtSc.propertyTerm(), propertyTerm) != 0) {
            if (!hasLength(propertyTerm)) {
                throw new IllegalArgumentException("'propertyTerm' must not be null.");
            }
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(DT_SC.PROPERTY_TERM, propertyTerm);
        }
        if (representationTerm != null && compare(dtSc.representationTerm(), representationTerm) != 0) {
            if (!hasLength(representationTerm)) {
                throw new IllegalArgumentException("'representationTerm' must not be null.");
            }
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(DT_SC.REPRESENTATION_TERM, representationTerm);
        }
        if (cardinality != null) {
            if (dtSc.cardinality().min() != cardinality.min()) {
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .set(DT_SC.CARDINALITY_MIN, cardinality.min());
            }
            if (dtSc.cardinality().max() != cardinality.max()) {
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .set(DT_SC.CARDINALITY_MAX, cardinality.max());
            }
        }
        if (definition != null) {
            if (dtSc.definition() == null || compare(dtSc.definition().content(), definition.content()) != 0) {
                if (hasLength(definition.content())) {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .set(DT_SC.DEFINITION, definition.content());
                } else {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .setNull(DT_SC.DEFINITION);
                }
            }
            if (dtSc.definition() == null || compare(dtSc.definition().source(), definition.source()) != 0) {
                if (hasLength(definition.source())) {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .set(DT_SC.DEFINITION_SOURCE, definition.source());
                } else {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .setNull(DT_SC.DEFINITION_SOURCE);
                }
            }
        } else {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .setNull(DT_SC.DEFINITION)
                    .setNull(DT_SC.DEFINITION_SOURCE);
        }
        if (valueConstraint != null) {
            if (valueConstraint.hasFixedValue()) {
                if (valueConstraint.hasFixedValue() &&
                        (dtSc.valueConstraint() == null || compare(dtSc.valueConstraint().fixedValue(), valueConstraint.fixedValue()) != 0)) {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .set(DT_SC.FIXED_VALUE, valueConstraint.fixedValue())
                            .setNull(DT_SC.DEFAULT_VALUE);
                } else if (valueConstraint.hasDefaultValue() &&
                        (dtSc.valueConstraint() == null || compare(dtSc.valueConstraint().defaultValue(), valueConstraint.defaultValue()) != 0)) {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .set(DT_SC.DEFAULT_VALUE, valueConstraint.defaultValue())
                            .setNull(DT_SC.FIXED_VALUE);
                } else {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .setNull(DT_SC.DEFAULT_VALUE)
                            .setNull(DT_SC.FIXED_VALUE);
                }
            }
        } else {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .setNull(DT_SC.DEFAULT_VALUE)
                    .setNull(DT_SC.FIXED_VALUE);
        }

        int numOfUpdatedRecords = 0;
        if (moreStep != null) {
            numOfUpdatedRecords = moreStep.set(DT_SC.LAST_UPDATED_BY, valueOf(requester().userId()))
                    .set(DT_SC.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                    .where(DT_SC.DT_SC_ID.eq(valueOf(dtSc.dtScId())))
                    .execute();
        }

        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean delete(DtManifestId dtManifestId) {

        var query = repositoryFactory().dtQueryRepository(requester());

        DtDetailsRecord dt = query.getDtDetails(dtManifestId);

        dslContext().update(DT_MANIFEST)
                .setNull(DT_MANIFEST.LOG_ID)
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(valueOf(dtManifestId)))
                .execute();

        dslContext().update(LOG)
                .setNull(LOG.PREV_LOG_ID)
                .setNull(LOG.NEXT_LOG_ID)
                .where(LOG.REFERENCE.eq(dt.guid().value()))
                .execute();

        dslContext().deleteFrom(LOG)
                .where(LOG.REFERENCE.eq(dt.guid().value()))
                .execute();

        // discard DT_SCs
        List<DtScDetailsRecord> dtScList = query.getDtScDetailsList(dtManifestId);
        if (!dtScList.isEmpty()) {
            // discard DT_SC_AWD_PRIs
            dslContext().deleteFrom(DT_SC_AWD_PRI)
                    .where(DT_SC_AWD_PRI.DT_SC_AWD_PRI_ID.in(valueOf(
                            dtScList.stream().map(e -> e.dtScAwdPriList()).flatMap(Collection::stream)
                                    .map(e -> e.dtScAwdPriId()).collect(Collectors.toSet()))
                    ))
                    .execute();

            dslContext().deleteFrom(DT_SC_MANIFEST)
                    .where(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.eq(valueOf(dtManifestId)))
                    .execute();

            dslContext().deleteFrom(DT_SC)
                    .where(DT_SC.DT_SC_ID.in(valueOf(
                            dtScList.stream().map(e -> e.dtScId()).collect(Collectors.toSet())
                    )))
                    .execute();
        }

        // discard assigned DT in modules
        dslContext().deleteFrom(MODULE_DT_MANIFEST)
                .where(MODULE_DT_MANIFEST.DT_MANIFEST_ID.eq(valueOf(dtManifestId)))
                .execute();

        if (!dt.dtAwdPriList().isEmpty()) {
            dslContext().deleteFrom(DT_AWD_PRI)
                    .where(DT_AWD_PRI.DT_AWD_PRI_ID.in(valueOf(
                            dt.dtAwdPriList().stream().map(e -> e.dtAwdPriId()).collect(Collectors.toSet())
                    )))
                    .execute();
        }

        // discard corresponding tags
        dslContext().deleteFrom(DT_MANIFEST_TAG)
                .where(DT_MANIFEST_TAG.DT_MANIFEST_ID.eq(valueOf(dtManifestId)))
                .execute();

        // discard DT
        dslContext().deleteFrom(DT_MANIFEST)
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(valueOf(dtManifestId)))
                .execute();

        int numOfDeletedRecords = dslContext().deleteFrom(DT)
                .where(DT.DT_ID.eq(valueOf(dt.dtId())))
                .execute();
        return numOfDeletedRecords == 1;
    }

    @Override
    public boolean delete(DtScManifestId dtScManifestId) {

        if (dtScManifestId == null) {
            throw new IllegalArgumentException("`dtScManifestId` must not be null.");
        }

        var query = repositoryFactory().dtQueryRepository(requester());
        DtScDetailsRecord dtSc = query.getDtScDetails(dtScManifestId);
        if (dtSc == null) {
            throw new IllegalArgumentException("DT_SC record not found.");
        }

        DtScDetailsRecord prev = null;
        DtScDetailsRecord next = null;
        if (dtSc.prevDtScManifestId() != null) {
            prev = query.getDtScDetails(dtSc.prevDtScManifestId());
        }
        if (dtSc.nextDtScManifestId() != null) {
            next = query.getDtScDetails(dtSc.nextDtScManifestId());
        }
        if (prev != null) {
            if (next != null) {
                dslContext().update(DT_SC_MANIFEST)
                        .set(DT_SC_MANIFEST.NEXT_DT_SC_MANIFEST_ID, valueOf(next.dtScManifestId()))
                        .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(valueOf(prev.dtScManifestId())))
                        .execute();
            } else {
                dslContext().update(DT_SC_MANIFEST)
                        .setNull(DT_SC_MANIFEST.NEXT_DT_SC_MANIFEST_ID)
                        .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(valueOf(prev.dtScManifestId())))
                        .execute();
            }
        }
        if (next != null) {
            if (prev != null) {
                dslContext().update(DT_SC_MANIFEST)
                        .set(DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID, valueOf(prev.dtScManifestId()))
                        .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(valueOf(next.dtScManifestId())))
                        .execute();
            } else {
                dslContext().update(DT_SC_MANIFEST)
                        .setNull(DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID)
                        .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(valueOf(next.dtScManifestId())))
                        .execute();
            }
        }

        // discard DT_SC_AWD_PRIs
        dslContext().deleteFrom(DT_SC_AWD_PRI)
                .where(DT_SC_AWD_PRI.DT_SC_AWD_PRI_ID.in(valueOf(
                        dtSc.dtScAwdPriList().stream().map(e -> e.dtScAwdPriId()).collect(Collectors.toSet()))
                ))
                .execute();

        int numOfDeletedRecords = dslContext().delete(DT_SC_MANIFEST)
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(valueOf(dtScManifestId)))
                .execute();

        dslContext().deleteFrom(DT_SC)
                .where(DT_SC.DT_SC_ID.eq(valueOf(dtSc.dtScId())))
                .execute();

        return numOfDeletedRecords == 1;
    }

    @Override
    public void revise(DtManifestId dtManifestId) {

        var query = repositoryFactory().dtQueryRepository(requester());
        DtDetailsRecord prevDt = query.getDtDetails(dtManifestId);

        // creates new bdt for revised record.
        DtRecord nextDtRecord = dslContext().selectFrom(DT)
                .where(DT.DT_ID.eq(valueOf(prevDt.dtId())))
                .fetchOne().copy();
        nextDtRecord.setState(CcState.WIP.name());
        nextDtRecord.setCreatedBy(valueOf(requester().userId()));
        nextDtRecord.setLastUpdatedBy(valueOf(requester().userId()));
        nextDtRecord.setOwnerUserId(valueOf(requester().userId()));
        LocalDateTime timestamp = LocalDateTime.now();
        nextDtRecord.setCreationTimestamp(timestamp);
        nextDtRecord.setLastUpdateTimestamp(timestamp);
        nextDtRecord.setPrevDtId(valueOf(prevDt.dtId()));
        DtId nextDtId = new DtId(dslContext().insertInto(DT)
                .set(nextDtRecord)
                .returning(DT.DT_ID)
                .fetchOne().getDtId().toBigInteger());

        dslContext().update(DT)
                .set(DT.NEXT_DT_ID, valueOf(nextDtId))
                .where(DT.DT_ID.eq(valueOf(prevDt.dtId())))
                .execute();

        copyDtAwdPriFromBase(prevDt.release().releaseId(), nextDtId, prevDt.dtManifestId());

        dslContext().update(DT_MANIFEST)
                .set(DT_MANIFEST.DT_ID, valueOf(nextDtId))
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(valueOf(prevDt.dtManifestId())))
                .execute();

        // revise DT_SCs
        for (DtScSummaryRecord dtSc : query.getDtScSummaryList(prevDt.dtManifestId())) {

            DtScRecord prevDtSc = dslContext().selectFrom(DT_SC)
                    .where(DT_SC.DT_SC_ID.eq(valueOf(dtSc.dtScId())))
                    .fetchOne();

            DtScRecord nextDtSc = prevDtSc.copy();
            nextDtSc.setOwnerDtId(valueOf(nextDtId));
            nextDtSc.setPrevDtScId(prevDtSc.getDtScId());
            DtScId nextDtScId = new DtScId(
                    dslContext().insertInto(DT_SC)
                            .set(nextDtSc)
                            .returning(DT_SC.DT_SC_ID)
                            .fetchOne().getDtScId().toBigInteger());

            dslContext().update(DT_SC)
                    .set(DT_SC.NEXT_DT_SC_ID, valueOf(nextDtScId))
                    .where(DT_SC.DT_SC_ID.eq(prevDtSc.getDtScId()))
                    .execute();

            copyDtScAwdPriFromBase(prevDt.release().releaseId(), nextDtScId, dtSc.dtScManifestId());

            dslContext().update(DT_SC_MANIFEST)
                    .set(DT_SC_MANIFEST.DT_SC_ID, valueOf(nextDtScId))
                    .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(valueOf(dtSc.dtScManifestId())))
                    .execute();
        }
    }

    @Override
    public void cancel(DtManifestId dtManifestId) {

        var query = repositoryFactory().dtQueryRepository(requester());
        DtDetailsRecord dt = query.getDtDetails(dtManifestId);

        if (dt == null) {
            throw new IllegalArgumentException("Not found a target DT");
        }

        DtRecord prevDtRecord = dslContext().selectFrom(DT)
                .where(DT.DT_ID.eq(valueOf(dt.prevDtId())))
                .fetchOptional().orElse(null);

        if (prevDtRecord == null) {
            throw new IllegalArgumentException("Not found previous revision");
        }

        UpdateSetMoreStep moreStep = dslContext().update(DT_MANIFEST)
                .set(DT_MANIFEST.DT_ID, prevDtRecord.getDtId());

        moreStep.where(DT_MANIFEST.DT_MANIFEST_ID.eq(valueOf(dt.dtManifestId())))
                .execute();

        // unlink prev DT
        dslContext().update(DT)
                .setNull(DT.NEXT_DT_ID)
                .where(DT.DT_ID.eq(prevDtRecord.getDtId()))
                .execute();

        List<DtScManifestRecord> dtScManifestRecords = dslContext().selectFrom(DT_SC_MANIFEST)
                .where(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.eq(valueOf(dtManifestId))).fetch();

        // remove revised DT_SCs
        for (DtScSummaryRecord dtSc : query.getDtScSummaryList(dtManifestId)) {

            DtScRecord currentDtSc = dslContext().selectFrom(DT_SC)
                    .where(DT_SC.DT_SC_ID.eq(valueOf(dtSc.dtScId())))
                    .fetchOne();

            DtScRecord prevDtSc = dslContext().selectFrom(DT_SC)
                    .where(DT_SC.DT_SC_ID.eq(currentDtSc.getPrevDtScId())).fetchOne();

            prevDtSc.setNextDtScId(null);
            prevDtSc.update(DT_SC.NEXT_DT_SC_ID);

            dslContext().update(DT_SC_MANIFEST)
                    .set(DT_SC_MANIFEST.DT_SC_ID, prevDtSc.getDtScId())
                    .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(valueOf(dtSc.dtScManifestId())))
                    .execute();

            // delete DT_SC_AWD_PRI
            dslContext().deleteFrom(DT_SC_AWD_PRI)
                    .where(and(
                            DT_SC_AWD_PRI.RELEASE_ID.eq(valueOf(dt.release().releaseId())),
                            DT_SC_AWD_PRI.DT_SC_ID.eq(valueOf(dtSc.dtScId()))
                    ))
                    .execute();

            currentDtSc.delete();
        }

        // clean logs up
        dslContext().update(DT_MANIFEST)
                .setNull(DT_MANIFEST.LOG_ID)
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(valueOf(dt.dtManifestId())))
                .execute();

        LogId logId = repositoryFactory().logCommandRepository(requester())
                .revertToStableStateByReference(dt.guid(), CcType.DT);

        dslContext().update(DT_MANIFEST)
                .set(DT_MANIFEST.LOG_ID, valueOf(logId))
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(valueOf(dt.dtManifestId())))
                .execute();

        // delete DT_AWD_PRI
        dslContext().deleteFrom(DT_AWD_PRI)
                .where(and(
                        DT_AWD_PRI.RELEASE_ID.eq(valueOf(dt.release().releaseId())),
                        DT_AWD_PRI.DT_ID.eq(valueOf(dt.dtId()))
                ))
                .execute();

        // delete current DT
        dslContext().deleteFrom(DT)
                .where(DT.DT_ID.eq(valueOf(dt.dtId())))
                .execute();
    }

    @Override
    public DtAwdPriId createDtAwdPri(ReleaseId releaseId, DtId dtId,
                                     XbtManifestId xbtManifestId,
                                     CodeListManifestId codeListManifestId,
                                     AgencyIdListManifestId agencyIdListManifestId,
                                     boolean isDefault) {

        DtAwdPriRecord dtAwdPriRecord = new DtAwdPriRecord();
        dtAwdPriRecord.setReleaseId(valueOf(releaseId));
        dtAwdPriRecord.setDtId(valueOf(dtId));
        if (codeListManifestId != null) {
            dtAwdPriRecord.setCodeListManifestId(valueOf(codeListManifestId));
        } else if (agencyIdListManifestId != null) {
            dtAwdPriRecord.setAgencyIdListManifestId(valueOf(agencyIdListManifestId));
        } else if (xbtManifestId != null) {
            dtAwdPriRecord.setXbtManifestId(valueOf(xbtManifestId));
        } else {
            throw new IllegalArgumentException("One of `xbtManifestId`, `codeListManifestId`, or `agencyIdListManifestId` must be provided.");
        }
        dtAwdPriRecord.setIsDefault((byte) (isDefault ? 1 : 0));

        return new DtAwdPriId(
                dslContext().insertInto(DT_AWD_PRI)
                        .set(dtAwdPriRecord)
                        .returning(DT_AWD_PRI.DT_AWD_PRI_ID)
                        .fetchOne().getDtAwdPriId().toBigInteger());
    }

    @Override
    public boolean updateDtAwdPri(DtAwdPriId dtAwdPriId,
                                  XbtManifestId xbtManifestId,
                                  CodeListManifestId codeListManifestId,
                                  AgencyIdListManifestId agencyIdListManifestId,
                                  boolean isDefault) {

        UpdateSetMoreStep moreStep = dslContext().update(DT_AWD_PRI)
                .set(DT_AWD_PRI.IS_DEFAULT, (byte) (isDefault ? 1 : 0));
        if (codeListManifestId != null) {
            moreStep = moreStep
                    .setNull(DT_AWD_PRI.XBT_MANIFEST_ID)
                    .set(DT_AWD_PRI.CODE_LIST_MANIFEST_ID, valueOf(codeListManifestId))
                    .setNull(DT_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID);
        } else if (agencyIdListManifestId != null) {
            moreStep = moreStep
                    .setNull(DT_AWD_PRI.XBT_MANIFEST_ID)
                    .setNull(DT_AWD_PRI.CODE_LIST_MANIFEST_ID)
                    .set(DT_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID, valueOf(agencyIdListManifestId));
        } else if (xbtManifestId != null) {
            moreStep = moreStep.set(DT_AWD_PRI.XBT_MANIFEST_ID, valueOf(xbtManifestId))
                    .setNull(DT_AWD_PRI.CODE_LIST_MANIFEST_ID)
                    .setNull(DT_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID);
        } else {
            throw new IllegalArgumentException();
        }

        int numOfUpdatedRecords = moreStep.where(DT_AWD_PRI.DT_AWD_PRI_ID.eq(valueOf(dtAwdPriId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean deleteDtAwdPri(DtAwdPriId dtAwdPriId) {

        if (dtAwdPriId == null) {
            throw new IllegalArgumentException("'dtAwdPriId' must not be null.");
        }

        int numOfDeletedRecords = dslContext().deleteFrom(DT_AWD_PRI)
                .where(DT_AWD_PRI.DT_AWD_PRI_ID.eq(valueOf(dtAwdPriId)))
                .execute();
        return numOfDeletedRecords == 1;
    }

    @Override
    public DtScAwdPriId createDtScAwdPri(ReleaseId releaseId, DtScId dtScId,
                                         XbtManifestId xbtManifestId,
                                         CodeListManifestId codeListManifestId,
                                         AgencyIdListManifestId agencyIdListManifestId,
                                         boolean isDefault) {

        DtScAwdPriRecord dtScAwdPriRecord = new DtScAwdPriRecord();
        dtScAwdPriRecord.setReleaseId(valueOf(releaseId));
        dtScAwdPriRecord.setDtScId(valueOf(dtScId));
        if (codeListManifestId != null) {
            dtScAwdPriRecord.setCodeListManifestId(valueOf(codeListManifestId));
        } else if (agencyIdListManifestId != null) {
            dtScAwdPriRecord.setAgencyIdListManifestId(valueOf(agencyIdListManifestId));
        } else if (xbtManifestId != null) {
            dtScAwdPriRecord.setXbtManifestId(valueOf(xbtManifestId));
        } else {
            throw new IllegalArgumentException("One of `xbtManifestId`, `codeListManifestId`, or `agencyIdListManifestId` must be provided.");
        }
        dtScAwdPriRecord.setIsDefault((byte) (isDefault ? 1 : 0));

        return new DtScAwdPriId(
                dslContext().insertInto(DT_SC_AWD_PRI)
                        .set(dtScAwdPriRecord)
                        .returning(DT_SC_AWD_PRI.DT_SC_AWD_PRI_ID)
                        .fetchOne().getDtScAwdPriId().toBigInteger());
    }

    @Override
    public boolean updateDtScAwdPri(DtScAwdPriId dtScAwdPriId,
                                    XbtManifestId xbtManifestId,
                                    CodeListManifestId codeListManifestId,
                                    AgencyIdListManifestId agencyIdListManifestId,
                                    boolean isDefault) {

        UpdateSetMoreStep moreStep = dslContext().update(DT_SC_AWD_PRI)
                .set(DT_SC_AWD_PRI.IS_DEFAULT, (byte) (isDefault ? 1 : 0));
        if (codeListManifestId != null) {
            moreStep = moreStep
                    .setNull(DT_SC_AWD_PRI.XBT_MANIFEST_ID)
                    .set(DT_SC_AWD_PRI.CODE_LIST_MANIFEST_ID, valueOf(codeListManifestId))
                    .setNull(DT_SC_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID);
        } else if (agencyIdListManifestId != null) {
            moreStep = moreStep
                    .setNull(DT_SC_AWD_PRI.XBT_MANIFEST_ID)
                    .setNull(DT_SC_AWD_PRI.CODE_LIST_MANIFEST_ID)
                    .set(DT_SC_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID, valueOf(agencyIdListManifestId));
        } else if (xbtManifestId != null) {
            moreStep = moreStep.set(DT_SC_AWD_PRI.XBT_MANIFEST_ID, valueOf(xbtManifestId))
                    .setNull(DT_SC_AWD_PRI.CODE_LIST_MANIFEST_ID)
                    .setNull(DT_SC_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID);
        } else {
            throw new IllegalArgumentException();
        }

        int numOfUpdatedRecords = moreStep.where(DT_SC_AWD_PRI.DT_SC_AWD_PRI_ID.eq(valueOf(dtScAwdPriId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean deleteDtScAwdPri(DtScAwdPriId dtScAwdPriId) {

        if (dtScAwdPriId == null) {
            throw new IllegalArgumentException("'dtScAwdPriId' must not be null.");
        }

        int numOfDeletedRecords = dslContext().deleteFrom(DT_SC_AWD_PRI)
                .where(DT_SC_AWD_PRI.DT_SC_AWD_PRI_ID.eq(valueOf(dtScAwdPriId)))
                .execute();
        return numOfDeletedRecords == 1;
    }

    @Override
    public boolean updateOwnership(ScoreUser targetUser, DtManifestId dtManifestId) {

        if (targetUser == null) {
            throw new IllegalArgumentException("`targetUser` must not be null.");
        }

        if (dtManifestId == null) {
            throw new IllegalArgumentException("`dtManifestId` must not be null.");
        }

        var query = repositoryFactory().dtQueryRepository(requester());

        DtSummaryRecord dt = query.getDtSummary(dtManifestId);
        if (dt == null) {
            throw new IllegalArgumentException("DT not found.");
        }

        int numOfUpdatedRecords = dslContext().update(DT)
                .set(DT.OWNER_USER_ID, valueOf(targetUser.userId()))
                .set(DT.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(DT.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                .where(DT.DT_ID.eq(valueOf(dt.dtId())))
                .execute();
        if (numOfUpdatedRecords < 1) {
            return false;
        }

        for (DtScSummaryRecord dtSc : query.getDtScSummaryList(dtManifestId)) {
            dslContext().update(DT_SC)
                    .set(DT_SC.OWNER_USER_ID, valueOf(targetUser.userId()))
                    .set(DT_SC.LAST_UPDATED_BY, valueOf(requester().userId()))
                    .set(DT_SC.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                    .where(DT_SC.DT_SC_ID.eq(valueOf(dtSc.dtScId())))
                    .execute();
        }

        return numOfUpdatedRecords == 1;
    }

}
