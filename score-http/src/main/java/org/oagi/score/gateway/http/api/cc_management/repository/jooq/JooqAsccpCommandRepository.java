package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

import jakarta.annotation.Nullable;
import org.jooq.DSLContext;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.*;
import org.oagi.score.gateway.http.api.cc_management.repository.AsccpCommandRepository;
import org.oagi.score.gateway.http.api.log_management.model.LogId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.AsccpManifest;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.AccRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.AsccpManifestRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.AsccpRecord;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.apache.commons.lang3.StringUtils.compare;
import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.api.cc_management.model.CcState.WIP;
import static org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpType.Default;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Acc.ACC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.AccManifest.ACC_MANIFEST;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Ascc.ASCC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Asccp.ASCCP;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.AsccpManifest.ASCCP_MANIFEST;
import static org.springframework.util.StringUtils.hasLength;

public class JooqAsccpCommandRepository extends JooqBaseRepository implements AsccpCommandRepository {

    public JooqAsccpCommandRepository(DSLContext dslContext, ScoreUser requester,
                                      RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public AsccpManifestId create(
            ReleaseId releaseId,
            AccManifestId roleOfAccManifestId,
            @Nullable String initialPropertyTerm,
            @Nullable AsccpType asccpType,
            @Nullable Boolean reusable,
            @Nullable CcState initialState,
            @Nullable NamespaceId namespaceId,
            @Nullable Definition definition) {

        AccSummaryRecord roleOfAcc = repositoryFactory().accQueryRepository(requester())
                .getAccSummary(roleOfAccManifestId);

        AsccpRecord asccp = new AsccpRecord();
        asccp.setGuid(ScoreGuidUtils.randomGuid());
        String propertyTerm = hasLength(initialPropertyTerm) ? initialPropertyTerm : "Property Term";
        String den = makeDen(propertyTerm, roleOfAcc.objectClassTerm());
        asccp.setPropertyTerm(propertyTerm);
        asccp.setRoleOfAccId(valueOf(roleOfAcc.accId()));
        asccp.setState(((initialState != null) ? initialState : WIP).name());
        if (definition != null) {
            asccp.setDefinition(definition.content());
            asccp.setDefinitionSource(definition.source());
        }
        asccp.setReusableIndicator((byte) (((reusable != null) ? reusable : true) ? 1 : 0));
        asccp.setIsDeprecated((byte) 0);
        asccp.setIsNillable((byte) 0);
        asccp.setType(((asccpType != null) ? asccpType : Default).name());
        if (namespaceId != null) {
            asccp.setNamespaceId(valueOf(namespaceId));
        }
        UserId requesterId = requester().userId();
        asccp.setCreatedBy(valueOf(requesterId));
        asccp.setLastUpdatedBy(valueOf(requesterId));
        asccp.setOwnerUserId(valueOf(requesterId));
        LocalDateTime timestamp = LocalDateTime.now();
        asccp.setCreationTimestamp(timestamp);
        asccp.setLastUpdateTimestamp(timestamp);

        asccp.setAsccpId(
                dslContext().insertInto(ASCCP)
                        .set(asccp)
                        .returning(ASCCP.ASCCP_ID).fetchOne().getAsccpId()
        );

        AsccpManifestRecord asccpManifest = new AsccpManifestRecord();
        asccpManifest.setAsccpId(asccp.getAsccpId());
        asccpManifest.setRoleOfAccManifestId(valueOf(roleOfAcc.accManifestId()));
        asccpManifest.setReleaseId(valueOf(releaseId));
        asccpManifest.setDen(den);

        return new AsccpManifestId(
                dslContext().insertInto(AsccpManifest.ASCCP_MANIFEST)
                        .set(asccpManifest)
                        .returning(AsccpManifest.ASCCP_MANIFEST.ASCCP_MANIFEST_ID)
                        .fetchOne().getAsccpManifestId().toBigInteger());
    }

    private String makeDen(String propertyTerm, String objectClassTerm) {

        String den = propertyTerm + ". " + objectClassTerm;
        // Check the length of den and shorten propertyTerm if necessary
        if (den.length() > 200) {
            // Calculate max length for propertyTerm to fit within 200 characters
            int maxPropertyTermLength = 200 - objectClassTerm.length() - 2; // 2 accounts for the ". " separator

            // Truncate propertyTerm if it exceeds the calculated max length
            if (propertyTerm.length() > maxPropertyTermLength) {
                propertyTerm = propertyTerm.substring(0, maxPropertyTermLength);
            }

            // Rebuild den with truncated propertyTerm
            den = propertyTerm + ". " + objectClassTerm;
        }
        return den;
    }

    @Override
    public boolean update(AsccpManifestId asccpManifestId,
                          @Nullable String propertyTerm,
                          @Nullable Boolean reusable,
                          @Nullable Boolean deprecated,
                          @Nullable Boolean nillable,
                          @Nullable NamespaceId namespaceId,
                          @Nullable Definition definition) {

        if (asccpManifestId == null) {
            throw new IllegalArgumentException("`asccpManifestId` must not be null");
        }

        AsccpSummaryRecord asccp = repositoryFactory().asccpQueryRepository(requester())
                .getAsccpSummary(asccpManifestId);
        if (asccp == null) {
            throw new IllegalArgumentException("ASCCP not found");
        }

        // update asccp record.
        UpdateSetFirstStep<AsccpRecord> firstStep = dslContext().update(ASCCP);
        UpdateSetMoreStep<AsccpRecord> moreStep = null;
        if (propertyTerm != null && compare(asccp.propertyTerm(), propertyTerm) != 0) {
            if (!hasLength(propertyTerm)) {
                throw new IllegalArgumentException("Property term must not be empty.");
            }
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ASCCP.PROPERTY_TERM, propertyTerm);

            AccSummaryRecord roleOfAcc = repositoryFactory().accQueryRepository(requester())
                    .getAccSummary(asccp.roleOfAccManifestId());
            String den = propertyTerm + ". " + roleOfAcc.objectClassTerm();
            dslContext().update(AsccpManifest.ASCCP_MANIFEST)
                    .set(AsccpManifest.ASCCP_MANIFEST.DEN, den)
                    .where(AsccpManifest.ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(valueOf(asccp.asccpManifestId())))
                    .execute();
        }
        if (definition != null) {
            if (asccp.definition() == null || compare(asccp.definition().content(), definition.content()) != 0) {
                if (hasLength(definition.content())) {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .set(ASCCP.DEFINITION, definition.content());
                } else {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .setNull(ASCCP.DEFINITION);
                }
            }
            if (asccp.definition() == null || compare(asccp.definition().source(), definition.source()) != 0) {
                if (hasLength(definition.source())) {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .set(ASCCP.DEFINITION_SOURCE, definition.source());
                } else {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .setNull(ASCCP.DEFINITION_SOURCE);
                }
            }
        }
        if (reusable != null && asccp.reusable() != reusable) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ASCCP.REUSABLE_INDICATOR, (byte) (reusable ? 1 : 0));
        }
        if (deprecated != null && asccp.deprecated() != deprecated) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ASCCP.IS_DEPRECATED, (byte) (deprecated ? 1 : 0));
        }
        if (nillable != null && asccp.nillable() != nillable) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ASCCP.IS_NILLABLE, (byte) (nillable ? 1 : 0));
        }
        if (namespaceId != null) {
            if (!namespaceId.equals(asccp.namespaceId())) {
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .set(ASCCP.NAMESPACE_ID, valueOf(namespaceId));
            }
        } else {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .setNull(ASCCP.NAMESPACE_ID);
        }

        int numOfUpdatedRecords = 0;
        if (moreStep != null) {
            numOfUpdatedRecords = moreStep.set(ASCCP.LAST_UPDATED_BY, valueOf(requester().userId()))
                    .set(ASCCP.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                    .where(ASCCP.ASCCP_ID.eq(valueOf(asccp.asccpId())))
                    .execute();
        }

        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean updateNamespace(AsccpManifestId asccpManifestId,
                                   NamespaceId namespaceId) {

        if (namespaceId == null) {
            throw new IllegalArgumentException("`namespaceId` must not be null");
        }

        return update(asccpManifestId, null, null, null, null, namespaceId, null);
    }

    @Override
    public boolean updateRoleOfAcc(AsccpManifestId asccpManifestId,
                                   AccManifestId roleOfAccManifestId) {

        AsccpSummaryRecord asccp = repositoryFactory().asccpQueryRepository(requester()).getAsccpSummary(asccpManifestId);
        if (asccp == null) {
            return false;
        }

        AccSummaryRecord roleOfAcc = repositoryFactory().accQueryRepository(requester()).getAccSummary(roleOfAccManifestId);
        if (roleOfAcc == null) {
            return false;
        }

        int numOfUpdatedRecords = dslContext().update(ASCCP)
                .set(ASCCP.ROLE_OF_ACC_ID, valueOf(roleOfAcc.accId()))
                .set(ASCCP.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(ASCCP.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                .where(ASCCP.ASCCP_ID.eq(valueOf(asccp.asccpId())))
                .execute();
        if (numOfUpdatedRecords == 1) {
            numOfUpdatedRecords = dslContext().update(ASCCP_MANIFEST)
                    .set(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID, valueOf(roleOfAcc.accManifestId()))
                    .set(ASCCP_MANIFEST.DEN, asccp.propertyTerm() + ". " + roleOfAcc.objectClassTerm())
                    .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(valueOf(asccp.asccpManifestId())))
                    .execute();
        }

        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean updateState(AsccpManifestId asccpManifestId, CcState nextState) {

        var query = repositoryFactory().asccpQueryRepository(requester());
        AsccpSummaryRecord asccp = query.getAsccpSummary(asccpManifestId);

        CcState prevState = asccp.state();

        UpdateSetMoreStep moreStep = dslContext().update(ASCCP)
                .set(ASCCP.STATE, nextState.name());

        // Change owner of CC when it restored.
        if (prevState == CcState.Deleted && nextState == CcState.WIP) {
            moreStep = moreStep.set(ASCCP.OWNER_USER_ID, valueOf(requester().userId()));
        }

        if (!prevState.isImplicitMove(nextState)) {
            moreStep = moreStep.set(ASCCP.LAST_UPDATED_BY, valueOf(requester().userId()))
                    .set(ASCCP.LAST_UPDATE_TIMESTAMP, LocalDateTime.now());
        }

        int numOfUpdatedRecords = moreStep.where(ASCCP.ASCCP_ID.eq(valueOf(asccp.asccpId())))
                .execute();
        boolean updated = numOfUpdatedRecords == 1;

        if (updated) {
            // Post-processing
            if (nextState == CcState.Published || nextState == CcState.Production) {
                // Issue #1298
                // Update 'deprecated' properties in associated BIEs
                if (asccp.deprecated()) {
                    dslContext().update(ASBIE.join(ASBIEP).on(ASBIE.TO_ASBIEP_ID.eq(ASBIEP.ASBIEP_ID)))
                            .set(ASBIE.IS_DEPRECATED, (byte) 1)
                            .where(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(valueOf(asccpManifestId)))
                            .execute();

                    dslContext().update(ASBIE.join(ABIE).on(ASBIE.FROM_ABIE_ID.eq(ABIE.ABIE_ID))
                                    .join(ASBIEP).on(ABIE.ABIE_ID.eq(ASBIEP.ROLE_OF_ABIE_ID)))
                            .set(ASBIE.IS_DEPRECATED, (byte) 1)
                            .where(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(valueOf(asccpManifestId)))
                            .execute();
                }
            }
        }

        return updated;
    }

    @Override
    public boolean updateLogId(AsccpManifestId asccpManifestId, LogId logId) {
        int numOfUpdatedRecords = dslContext().update(ASCCP_MANIFEST)
                .set(ASCCP_MANIFEST.LOG_ID, valueOf(logId))
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(valueOf(asccpManifestId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean delete(AsccpManifestId asccpManifestId) {

        var query = repositoryFactory().asccpQueryRepository(requester());

        AsccpSummaryRecord asccp = query.getAsccpSummary(asccpManifestId);

        dslContext().update(ASCCP_MANIFEST)
                .setNull(ASCCP_MANIFEST.LOG_ID)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(valueOf(asccpManifestId)))
                .execute();

        dslContext().update(LOG)
                .setNull(LOG.PREV_LOG_ID)
                .setNull(LOG.NEXT_LOG_ID)
                .where(LOG.REFERENCE.eq(asccp.guid().value()))
                .execute();

        dslContext().deleteFrom(LOG)
                .where(LOG.REFERENCE.eq(asccp.guid().value()))
                .execute();

        // discard assigned ASCCP in modules
        dslContext().deleteFrom(MODULE_ASCCP_MANIFEST)
                .where(MODULE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(valueOf(asccpManifestId)))
                .execute();

        // discard corresponding tags
        dslContext().deleteFrom(ASCCP_MANIFEST_TAG)
                .where(ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID.eq(valueOf(asccpManifestId)))
                .execute();

        // discard ASCCP
        dslContext().deleteFrom(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(valueOf(asccpManifestId)))
                .execute();

        int numOfDeletedRecords = dslContext().deleteFrom(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(valueOf(asccp.asccpId())))
                .execute();
        return numOfDeletedRecords == 1;
    }

    public void revise(AsccpManifestId asccpManifestId) {

        var query = repositoryFactory().asccpQueryRepository(requester());
        AsccpDetailsRecord prevAsccp = query.getAsccpDetails(asccpManifestId);

        // creates new asccp for revised record.
        AsccpRecord nextAsccpRecord = dslContext().selectFrom(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(valueOf(prevAsccp.asccpId())))
                .fetchOne().copy();
        nextAsccpRecord.setState(CcState.WIP.name());
        nextAsccpRecord.setCreatedBy(valueOf(requester().userId()));
        nextAsccpRecord.setLastUpdatedBy(valueOf(requester().userId()));
        nextAsccpRecord.setOwnerUserId(valueOf(requester().userId()));
        LocalDateTime timestamp = LocalDateTime.now();
        nextAsccpRecord.setCreationTimestamp(timestamp);
        nextAsccpRecord.setLastUpdateTimestamp(timestamp);
        nextAsccpRecord.setPrevAsccpId(valueOf(prevAsccp.asccpId()));
        AsccpId nextAsccpId = new AsccpId(dslContext().insertInto(ASCCP)
                .set(nextAsccpRecord)
                .returning(ASCCP.ASCCP_ID)
                .fetchOne().getAsccpId().toBigInteger());

        dslContext().update(ASCCP)
                .set(ASCCP.NEXT_ASCCP_ID, valueOf(nextAsccpId))
                .where(ASCCP.ASCCP_ID.eq(valueOf(prevAsccp.asccpId())))
                .execute();

        dslContext().update(ASCCP_MANIFEST)
                .set(ASCCP_MANIFEST.ASCCP_ID, valueOf(nextAsccpId))
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(valueOf(prevAsccp.asccpManifestId())))
                .execute();

        // update `conflict` for ascc_manifests' to_asccp_manifest_id which indicates given asccp manifest.
        dslContext().update(ASCC_MANIFEST)
                .set(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID, valueOf(prevAsccp.asccpManifestId()))
                .set(ASCC_MANIFEST.CONFLICT, (byte) 1)
                .where(and(
                        ASCC_MANIFEST.RELEASE_ID.eq(valueOf(prevAsccp.release().releaseId())),
                        ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.in(valueOf(Arrays.asList(
                                prevAsccp.asccpManifestId(),
                                prevAsccp.prevAsccpManifestId()
                        )))
                ))
                .execute();
    }

    public void cancel(AsccpManifestId asccpManifestId) {

        var query = repositoryFactory().asccpQueryRepository(requester());
        AsccpDetailsRecord asccp = query.getAsccpDetails(asccpManifestId);

        if (asccp == null) {
            throw new IllegalArgumentException("Not found a target ASCCP");
        }

        AsccpRecord prevAsccpRecord = dslContext().selectFrom(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(valueOf(asccp.prevAsccpId())))
                .fetchOptional().orElse(null);

        if (prevAsccpRecord == null) {
            throw new IllegalArgumentException("Not found previous revision");
        }

        UpdateSetMoreStep moreStep = dslContext().update(ASCCP_MANIFEST)
                .set(ASCCP_MANIFEST.ASCCP_ID, prevAsccpRecord.getAsccpId());

        // update ASCCP MANIFEST's asccp_id
        if (prevAsccpRecord.getRoleOfAccId() != null) {
            AccRecord prevRoleOfAccRecord = dslContext().selectFrom(ACC)
                    .where(ACC.ACC_ID.eq(prevAsccpRecord.getRoleOfAccId()))
                    .fetchOne();

            AccManifestId prevRoleOfAccManifestId = new AccManifestId(
                    dslContext().select(ACC_MANIFEST.ACC_MANIFEST_ID)
                            .from(ACC_MANIFEST)
                            .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                            .where(and(
                                    ACC_MANIFEST.RELEASE_ID.eq(valueOf(asccp.release().releaseId())),
                                    ACC.GUID.eq(prevRoleOfAccRecord.getGuid())
                            ))
                            .fetchOneInto(BigInteger.class)
            );

            moreStep.set(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID, valueOf(prevRoleOfAccManifestId));
        }

        moreStep.where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(valueOf(asccp.asccpManifestId())))
                .execute();

        // update ASCCs which using current ASCCP
        dslContext().update(ASCC)
                .set(ASCC.TO_ASCCP_ID, valueOf(asccp.prevAsccpId()))
                .where(ASCC.TO_ASCCP_ID.eq(valueOf(asccp.asccpId())))
                .execute();

        // unlink prev ASCCP
        dslContext().update(ASCCP)
                .setNull(ASCCP.NEXT_ASCCP_ID)
                .where(ASCCP.ASCCP_ID.eq(prevAsccpRecord.getAsccpId()))
                .execute();

        // clean logs up
        dslContext().update(ASCCP_MANIFEST)
                .setNull(ASCCP_MANIFEST.LOG_ID)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(valueOf(asccp.asccpManifestId())))
                .execute();

        LogId logId = repositoryFactory().logCommandRepository(requester())
                .revertToStableStateByReference(asccp.guid(), CcType.ASCCP);

        dslContext().update(ASCCP_MANIFEST)
                .set(ASCCP_MANIFEST.LOG_ID, valueOf(logId))
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(valueOf(asccp.asccpManifestId())))
                .execute();

        // delete current ASCCP
        dslContext().deleteFrom(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(valueOf(asccp.asccpId())))
                .execute();
    }

    @Override
    public boolean updateOwnership(ScoreUser targetUser, AsccpManifestId asccpManifestId) {

        if (targetUser == null) {
            throw new IllegalArgumentException("`targetUser` must not be null.");
        }

        if (asccpManifestId == null) {
            throw new IllegalArgumentException("`asccpManifestId` must not be null.");
        }

        var query = repositoryFactory().asccpQueryRepository(requester());

        AsccpSummaryRecord asccp = query.getAsccpSummary(asccpManifestId);
        if (asccp == null) {
            throw new IllegalArgumentException("ASCCP not found.");
        }

        int numOfUpdatedRecords = dslContext().update(ASCCP)
                .set(ASCCP.OWNER_USER_ID, valueOf(targetUser.userId()))
                .set(ASCCP.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(ASCCP.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                .where(ASCCP.ASCCP_ID.eq(valueOf(asccp.asccpId())))
                .execute();
        return numOfUpdatedRecords == 1;
    }

}
