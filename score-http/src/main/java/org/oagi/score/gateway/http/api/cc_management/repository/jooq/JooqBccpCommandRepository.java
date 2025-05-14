package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

import jakarta.annotation.Nullable;
import org.jooq.DSLContext;
import org.jooq.Record3;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.cc_management.model.ValueConstraint;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.repository.BccpCommandRepository;
import org.oagi.score.gateway.http.api.log_management.model.LogId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.BccpManifestRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.BccpRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.DtRecord;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.apache.commons.lang3.StringUtils.compare;
import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Bccp.BCCP;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.BccpManifest.BCCP_MANIFEST;
import static org.springframework.util.StringUtils.hasLength;

public class JooqBccpCommandRepository extends JooqBaseRepository implements BccpCommandRepository {

    public JooqBccpCommandRepository(DSLContext dslContext, ScoreUser requester,
                                     RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public BccpManifestId create(ReleaseId releaseId,
                                 DtManifestId dtManifestId,
                                 @Nullable String initialPropertyTerm) {

        if (dtManifestId == null) {
            return null;
        }

        DtDetailsRecord dt = repositoryFactory().dtQueryRepository(requester())
                .getDtDetails(dtManifestId);
        if (dt == null) {
            return null;
        }

        if (!hasLength(initialPropertyTerm)) {
            initialPropertyTerm = "Property Term";
        }

        BccpRecord bccp = new BccpRecord();
        bccp.setGuid(ScoreGuidUtils.randomGuid());
        bccp.setPropertyTerm(initialPropertyTerm);
        bccp.setRepresentationTerm(dt.dataTypeTerm());
        bccp.setBdtId(valueOf(dt.dtId()));
        bccp.setState(CcState.WIP.name());
        bccp.setIsDeprecated((byte) 0);
        bccp.setIsNillable((byte) 0);
        bccp.setNamespaceId(null);
        UserId requesterId = requester().userId();
        bccp.setCreatedBy(valueOf(requesterId));
        bccp.setLastUpdatedBy(valueOf(requesterId));
        bccp.setOwnerUserId(valueOf(requesterId));
        LocalDateTime timestamp = LocalDateTime.now();
        bccp.setCreationTimestamp(timestamp);
        bccp.setLastUpdateTimestamp(timestamp);

        bccp.setBccpId(
                dslContext().insertInto(BCCP)
                        .set(bccp)
                        .returning(BCCP.BCCP_ID).fetchOne().getBccpId()
        );

        BccpManifestRecord bccpManifest = new BccpManifestRecord();
        bccpManifest.setBccpId(bccp.getBccpId());
        bccpManifest.setBdtManifestId(valueOf(dt.dtManifestId()));
        bccpManifest.setReleaseId(valueOf(releaseId));
        String den = bccp.getPropertyTerm() + ". " + dt.den().replaceAll(". Type", "");
        bccpManifest.setDen(den);

        return new BccpManifestId(
                dslContext().insertInto(BCCP_MANIFEST)
                        .set(bccpManifest)
                        .returning(BCCP_MANIFEST.BCCP_MANIFEST_ID)
                        .fetchOne().getBccpManifestId().toBigInteger());
    }

    @Override
    public boolean update(BccpManifestId bccpManifestId,
                          @Nullable String propertyTerm,
                          @Nullable Boolean nillable,
                          @Nullable Boolean deprecated,
                          @Nullable NamespaceId namespaceId,
                          @Nullable ValueConstraint valueConstraint,
                          @Nullable Definition definition) {

        if (bccpManifestId == null) {
            return false;
        }

        BccpSummaryRecord bccp = repositoryFactory().bccpQueryRepository(requester())
                .getBccpSummary(bccpManifestId);
        if (bccp == null) {
            return false;
        }

        UpdateSetFirstStep<BccpRecord> firstStep = dslContext().update(BCCP);
        UpdateSetMoreStep<BccpRecord> moreStep = null;

        if (compare(bccp.propertyTerm(), propertyTerm) != 0) {
            if (!hasLength(propertyTerm)) {
                throw new IllegalArgumentException("Property term must not be null.");
            }
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(BCCP.PROPERTY_TERM, propertyTerm);

            Record3<ULong, String, String> result = dslContext().select(DT.DT_ID, DT.QUALIFIER, DT.DATA_TYPE_TERM)
                    .from(DT)
                    .join(DT_MANIFEST).on(DT.DT_ID.eq(DT_MANIFEST.DT_ID))
                    .where(DT_MANIFEST.DT_MANIFEST_ID.eq(valueOf(bccp.dtManifestId())))
                    .fetchOne();

            String qualifier = result.get(DT.QUALIFIER);
            String dataTypeTerm = result.get(DT.DATA_TYPE_TERM);
            String den = propertyTerm + ". " + (((qualifier != null) ? (qualifier + "_ ") : "") + dataTypeTerm);
            dslContext().update(BCCP_MANIFEST)
                    .set(BCCP_MANIFEST.DEN, den)
                    .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(valueOf(bccp.bccpManifestId())))
                    .execute();
        }

        if (valueConstraint != null) {
            if (valueConstraint.hasFixedValue() &&
                    (bccp.valueConstraint() == null || compare(bccp.valueConstraint().fixedValue(), valueConstraint.fixedValue()) != 0)) {
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .set(BCCP.FIXED_VALUE, valueConstraint.fixedValue())
                        .setNull(BCCP.DEFAULT_VALUE);
            } else if (valueConstraint.hasDefaultValue() &&
                    (bccp.valueConstraint() == null || compare(bccp.valueConstraint().defaultValue(), valueConstraint.defaultValue()) != 0)) {
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .set(BCCP.DEFAULT_VALUE, valueConstraint.defaultValue())
                        .setNull(BCCP.FIXED_VALUE);
            } else {
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .setNull(BCCP.DEFAULT_VALUE)
                        .setNull(BCCP.FIXED_VALUE);
            }
        }

        if (definition != null) {
            if (bccp.definition() == null || compare(bccp.definition().content(), definition.content()) != 0) {
                if (hasLength(definition.content())) {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .set(BCCP.DEFINITION, definition.content());
                } else {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .setNull(BCCP.DEFINITION);
                }
            }
            if (bccp.definition() == null || compare(bccp.definition().source(), definition.source()) != 0) {
                if (hasLength(definition.source())) {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .set(BCCP.DEFINITION_SOURCE, definition.source());
                } else {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .setNull(BCCP.DEFINITION_SOURCE);
                }
            }
        }
        if (deprecated != null && bccp.deprecated() != deprecated) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(BCCP.IS_DEPRECATED, (byte) (deprecated ? 1 : 0));
        }
        if (nillable != null && bccp.nillable() != nillable) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(BCCP.IS_NILLABLE, (byte) (nillable ? 1 : 0));
        }
        if (namespaceId != null) {
            if (!namespaceId.equals(bccp.namespaceId())) {
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .set(BCCP.NAMESPACE_ID, valueOf(namespaceId));
            }
        } else {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .setNull(BCCP.NAMESPACE_ID);
        }

        int numOfUpdatedRecords = 0;
        if (moreStep != null) {
            numOfUpdatedRecords = moreStep.set(BCCP.LAST_UPDATED_BY, valueOf(requester().userId()))
                    .set(BCCP.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                    .where(BCCP.BCCP_ID.eq(valueOf(bccp.bccpId())))
                    .execute();
        }

        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean updateDt(BccpManifestId bccpManifestId, DtManifestId dtManifestId) {

        if (bccpManifestId == null || dtManifestId == null) {
            return false;
        }

        BccpSummaryRecord bccp = repositoryFactory().bccpQueryRepository(requester()).getBccpSummary(bccpManifestId);
        if (bccp == null) {
            return false;
        }

        DtSummaryRecord dt = repositoryFactory().dtQueryRepository(requester()).getDtSummary(dtManifestId);
        if (dt == null) {
            return false;
        }

        int numOfUpdatedRecords = dslContext().update(BCCP)
                .set(BCCP.BDT_ID, valueOf(dt.dtId()))
                .set(BCCP.REPRESENTATION_TERM, dt.dataTypeTerm())
                .set(BCCP.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(BCCP.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                .where(BCCP.BCCP_ID.eq(valueOf(bccp.bccpId())))
                .execute();
        if (numOfUpdatedRecords == 1) {
            String den = bccp.propertyTerm() + ". " + (((dt.qualifier() != null) ? (dt.qualifier() + "_ ") : "") + dt.dataTypeTerm());
            numOfUpdatedRecords = dslContext().update(BCCP_MANIFEST)
                    .set(BCCP_MANIFEST.BDT_MANIFEST_ID, valueOf(dt.dtManifestId()))
                    .set(BCCP_MANIFEST.DEN, den)
                    .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(valueOf(bccp.bccpManifestId())))
                    .execute();
        }

        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean updateState(BccpManifestId bccpManifestId, CcState nextState) {

        if (bccpManifestId == null || nextState == null) {
            return false;
        }

        var query = repositoryFactory().bccpQueryRepository(requester());
        BccpSummaryRecord bccp = query.getBccpSummary(bccpManifestId);

        CcState prevState = bccp.state();

        UpdateSetMoreStep moreStep = dslContext().update(BCCP)
                .set(BCCP.STATE, nextState.name());

        // Change owner of CC when it restored.
        if (prevState == CcState.Deleted && nextState == CcState.WIP) {
            moreStep = moreStep.set(BCCP.OWNER_USER_ID, valueOf(requester().userId()));
        }

        if (!prevState.isImplicitMove(nextState)) {
            moreStep = moreStep.set(BCCP.LAST_UPDATED_BY, valueOf(requester().userId()))
                    .set(BCCP.LAST_UPDATE_TIMESTAMP, LocalDateTime.now());
        }

        int numOfUpdatedRecords = moreStep.where(BCCP.BCCP_ID.eq(valueOf(bccp.bccpId())))
                .execute();
        boolean updated = numOfUpdatedRecords == 1;

        if (updated) {
            // Post-processing
            if (nextState == CcState.Published || nextState == CcState.Production) {
                // Issue #1298
                // Update 'deprecated' properties in associated BIEs
                if (bccp.deprecated()) {
                    dslContext().update(BBIE.join(BBIEP).on(BBIE.TO_BBIEP_ID.eq(BBIEP.BBIEP_ID)))
                            .set(BBIE.IS_DEPRECATED, (byte) 1)
                            .where(BBIEP.BASED_BCCP_MANIFEST_ID.eq(valueOf(bccpManifestId)))
                            .execute();
                }
            }
        }

        return updated;
    }

    @Override
    public boolean updateLogId(BccpManifestId bccpManifestId, LogId logId) {
        int numOfUpdatedRecords = dslContext().update(BCCP_MANIFEST)
                .set(BCCP_MANIFEST.LOG_ID, valueOf(logId))
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(valueOf(bccpManifestId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean delete(BccpManifestId bccpManifestId) {

        var query = repositoryFactory().bccpQueryRepository(requester());

        BccpSummaryRecord bccp = query.getBccpSummary(bccpManifestId);

        dslContext().update(BCCP_MANIFEST)
                .setNull(BCCP_MANIFEST.LOG_ID)
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(valueOf(bccpManifestId)))
                .execute();

        dslContext().update(LOG)
                .setNull(LOG.PREV_LOG_ID)
                .setNull(LOG.NEXT_LOG_ID)
                .where(LOG.REFERENCE.eq(bccp.guid().value()))
                .execute();

        dslContext().deleteFrom(LOG)
                .where(LOG.REFERENCE.eq(bccp.guid().value()))
                .execute();

        // discard assigned BCCP in modules
        dslContext().deleteFrom(MODULE_BCCP_MANIFEST)
                .where(MODULE_BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(valueOf(bccpManifestId)))
                .execute();

        // discard corresponding tags
        dslContext().deleteFrom(BCCP_MANIFEST_TAG)
                .where(BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID.eq(valueOf(bccpManifestId)))
                .execute();

        // discard BCCP
        dslContext().deleteFrom(BCCP_MANIFEST)
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(valueOf(bccpManifestId)))
                .execute();

        int numOfDeletedRecords = dslContext().deleteFrom(BCCP)
                .where(BCCP.BCCP_ID.eq(valueOf(bccp.bccpId())))
                .execute();
        return numOfDeletedRecords == 1;
    }

    @Override
    public void revise(BccpManifestId bccpManifestId) {

        var query = repositoryFactory().bccpQueryRepository(requester());
        BccpDetailsRecord prevBccp = query.getBccpDetails(bccpManifestId);

        // creates new bccp for revised record.
        BccpRecord nextBccpRecord = dslContext().selectFrom(BCCP)
                .where(BCCP.BCCP_ID.eq(valueOf(prevBccp.bccpId())))
                .fetchOne().copy();
        nextBccpRecord.setState(CcState.WIP.name());
        nextBccpRecord.setCreatedBy(valueOf(requester().userId()));
        nextBccpRecord.setLastUpdatedBy(valueOf(requester().userId()));
        nextBccpRecord.setOwnerUserId(valueOf(requester().userId()));
        LocalDateTime timestamp = LocalDateTime.now();
        nextBccpRecord.setCreationTimestamp(timestamp);
        nextBccpRecord.setLastUpdateTimestamp(timestamp);
        nextBccpRecord.setPrevBccpId(valueOf(prevBccp.bccpId()));
        BccpId nextBccpId = new BccpId(dslContext().insertInto(BCCP)
                .set(nextBccpRecord)
                .returning(BCCP.BCCP_ID)
                .fetchOne().getBccpId().toBigInteger());

        dslContext().update(BCCP)
                .set(BCCP.NEXT_BCCP_ID, valueOf(nextBccpId))
                .where(BCCP.BCCP_ID.eq(valueOf(prevBccp.bccpId())))
                .execute();

        dslContext().update(BCCP_MANIFEST)
                .set(BCCP_MANIFEST.BCCP_ID, valueOf(nextBccpId))
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(valueOf(prevBccp.bccpManifestId())))
                .execute();

        // update `conflict` for bcc_manifests' to_bccp_manifest_id which indicates given bccp manifest.
        dslContext().update(BCC_MANIFEST)
                .set(BCC_MANIFEST.TO_BCCP_MANIFEST_ID, valueOf(prevBccp.bccpManifestId()))
                .set(BCC_MANIFEST.CONFLICT, (byte) 1)
                .where(and(
                        BCC_MANIFEST.RELEASE_ID.eq(valueOf(prevBccp.release().releaseId())),
                        BCC_MANIFEST.TO_BCCP_MANIFEST_ID.in(valueOf(Arrays.asList(
                                prevBccp.bccpManifestId(),
                                prevBccp.prevBccpManifestId()
                        )))
                ))
                .execute();
    }

    @Override
    public void cancel(BccpManifestId bccpManifestId) {

        var query = repositoryFactory().bccpQueryRepository(requester());
        BccpDetailsRecord bccp = query.getBccpDetails(bccpManifestId);

        if (bccp == null) {
            throw new IllegalArgumentException("Not found a target BCCP");
        }

        BccpRecord prevBccpRecord = dslContext().selectFrom(BCCP)
                .where(BCCP.BCCP_ID.eq(valueOf(bccp.prevBccpId())))
                .fetchOptional().orElse(null);

        if (prevBccpRecord == null) {
            throw new IllegalArgumentException("Not found previous revision");
        }

        UpdateSetMoreStep moreStep = dslContext().update(BCCP_MANIFEST)
                .set(BCCP_MANIFEST.BCCP_ID, prevBccpRecord.getBccpId());

        // update BCCP MANIFEST's bccp_id
        if (prevBccpRecord.getBdtId() != null) {
            DtRecord prevDtRecord = dslContext().selectFrom(DT)
                    .where(DT.DT_ID.eq(prevBccpRecord.getBdtId()))
                    .fetchOne();

            DtManifestId prevDtManifestId = new DtManifestId(
                    dslContext().select(DT_MANIFEST.DT_MANIFEST_ID)
                            .from(DT_MANIFEST)
                            .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                            .where(and(
                                    DT_MANIFEST.RELEASE_ID.eq(valueOf(bccp.release().releaseId())),
                                    DT.GUID.eq(prevDtRecord.getGuid())
                            ))
                            .fetchOneInto(BigInteger.class)
            );

            moreStep.set(BCCP_MANIFEST.BDT_MANIFEST_ID, valueOf(prevDtManifestId));
        }

        moreStep.where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(valueOf(bccp.bccpManifestId())))
                .execute();

        // update BCCs which using current BCCP
        dslContext().update(BCC)
                .set(BCC.TO_BCCP_ID, valueOf(bccp.prevBccpId()))
                .where(BCC.TO_BCCP_ID.eq(valueOf(bccp.bccpId())))
                .execute();

        // unlink prev BCCP
        dslContext().update(BCCP)
                .setNull(BCCP.NEXT_BCCP_ID)
                .where(BCCP.BCCP_ID.eq(prevBccpRecord.getBccpId()))
                .execute();

        // clean logs up
        dslContext().update(BCCP_MANIFEST)
                .setNull(BCCP_MANIFEST.LOG_ID)
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(valueOf(bccp.bccpManifestId())))
                .execute();

        LogId logId = repositoryFactory().logCommandRepository(requester())
                .revertToStableStateByReference(bccp.guid(), CcType.BCCP);

        dslContext().update(BCCP_MANIFEST)
                .set(BCCP_MANIFEST.LOG_ID, valueOf(logId))
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(valueOf(bccp.bccpManifestId())))
                .execute();

        // delete current BCCP
        dslContext().deleteFrom(BCCP)
                .where(BCCP.BCCP_ID.eq(valueOf(bccp.bccpId())))
                .execute();
    }

    @Override
    public boolean updateOwnership(ScoreUser targetUser, BccpManifestId bccpManifestId) {

        if (targetUser == null) {
            throw new IllegalArgumentException("`targetUser` must not be null.");
        }

        if (bccpManifestId == null) {
            throw new IllegalArgumentException("`bccpManifestId` must not be null.");
        }

        var query = repositoryFactory().bccpQueryRepository(requester());

        BccpSummaryRecord bccp = query.getBccpSummary(bccpManifestId);
        if (bccp == null) {
            throw new IllegalArgumentException("BCCP not found.");
        }

        int numOfUpdatedRecords = dslContext().update(BCCP)
                .set(BCCP.OWNER_USER_ID, valueOf(targetUser.userId()))
                .set(BCCP.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(BCCP.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                .where(BCCP.BCCP_ID.eq(valueOf(bccp.bccpId())))
                .execute();
        return numOfUpdatedRecords == 1;
    }

}
