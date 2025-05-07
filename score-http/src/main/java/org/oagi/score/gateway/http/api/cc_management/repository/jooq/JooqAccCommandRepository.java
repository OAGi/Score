package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.annotation.Nullable;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.cc_management.model.*;
import org.oagi.score.gateway.http.api.cc_management.model.acc.*;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.*;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeyId;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeySummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeyType;
import org.oagi.score.gateway.http.api.cc_management.repository.AccCommandRepository;
import org.oagi.score.gateway.http.api.log_management.model.LogAction;
import org.oagi.score.gateway.http.api.log_management.model.LogId;
import org.oagi.score.gateway.http.api.log_management.service.LogSerializer;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.Tables;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.AsccManifest;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.*;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.compare;
import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType.*;
import static org.oagi.score.gateway.http.api.cc_management.model.bcc.EntityType.Attribute;
import static org.oagi.score.gateway.http.api.cc_management.model.bcc.EntityType.Element;
import static org.oagi.score.gateway.http.api.cc_management.model.seq_key.MoveTo.LAST;
import static org.oagi.score.gateway.http.api.cc_management.model.seq_key.MoveTo.LAST_OF_ATTR;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Acc.ACC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.AccManifest.ACC_MANIFEST;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Ascc.ASCC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.AsccManifest.ASCC_MANIFEST;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Bcc.BCC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.BccManifest.BCC_MANIFEST;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.BccpManifest.BCCP_MANIFEST;
import static org.springframework.util.StringUtils.hasLength;

public class JooqAccCommandRepository extends JooqBaseRepository implements AccCommandRepository {

    private LogSerializer serializer;

    public JooqAccCommandRepository(DSLContext dslContext, ScoreUser requester,
                                    RepositoryFactory repositoryFactory, LogSerializer serializer) {
        super(dslContext, requester, repositoryFactory);

        this.serializer = serializer;
    }

    @Override
    public AccManifestId create(
            ReleaseId releaseId,
            @Nullable AccManifestId basedAccManifestId,
            @Nullable String initialObjectClassTerm,
            @Nullable OagisComponentType initialComponentType,
            @Nullable AccType initialType,
            @Nullable String initialDefinition,
            @Nullable NamespaceId namespaceId) {
        AccManifestRecord basedAccManifest = null;

        if (basedAccManifestId != null) {
            basedAccManifest = dslContext().selectFrom(ACC_MANIFEST)
                    .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(valueOf(basedAccManifestId)))
                    .fetchOne();
        }

        AccRecord acc = new AccRecord();
        acc.setGuid(ScoreGuidUtils.randomGuid());
        acc.setObjectClassTerm(hasLength(initialObjectClassTerm) ? initialObjectClassTerm : "Object Class Term");
        acc.setOagisComponentType((initialComponentType != null ? initialComponentType : Semantics).getValue());
        acc.setType((initialType != null ? initialType : AccType.Default).name());
        if (hasLength(initialDefinition)) {
            acc.setDefinition(initialDefinition);
        }
        acc.setState(CcState.WIP.name());
        acc.setIsAbstract((byte) 0);
        acc.setIsDeprecated((byte) 0);
        if (basedAccManifest != null) {
            acc.setBasedAccId(basedAccManifest.getAccId());
        }
        if (namespaceId != null) {
            acc.setNamespaceId(valueOf(namespaceId));
        }
        UserId requesterId = requester().userId();
        LocalDateTime timestamp = LocalDateTime.now();
        acc.setCreatedBy(valueOf(requesterId));
        acc.setLastUpdatedBy(valueOf(requesterId));
        acc.setOwnerUserId(valueOf(requesterId));
        acc.setCreationTimestamp(timestamp);
        acc.setLastUpdateTimestamp(timestamp);

        acc.setAccId(
                dslContext().insertInto(ACC)
                        .set(acc)
                        .returning(ACC.ACC_ID).fetchOne().getAccId()
        );

        AccManifestRecord accManifest = new AccManifestRecord();
        accManifest.setAccId(acc.getAccId());
        accManifest.setReleaseId(valueOf(releaseId));
        if (basedAccManifest != null) {
            accManifest.setBasedAccManifestId(basedAccManifest.getAccManifestId());
        }
        accManifest.setDen(acc.getObjectClassTerm() + ". Details");

        return new AccManifestId(
                dslContext().insertInto(ACC_MANIFEST)
                        .set(accManifest)
                        .returning(ACC_MANIFEST.ACC_MANIFEST_ID)
                        .fetchOne().getAccManifestId().toBigInteger());
    }

    @Override
    public boolean updateState(AccManifestId accManifestId, CcState nextState) {

        var query = repositoryFactory().accQueryRepository(requester());
        AccSummaryRecord acc = query.getAccSummary(accManifestId);

        CcState prevState = acc.state();

        UpdateSetMoreStep moreStep = dslContext().update(ACC)
                .set(ACC.STATE, nextState.name());

        // Change owner of CC when it restored.
        if (prevState == CcState.Deleted && nextState == CcState.WIP) {
            moreStep = moreStep.set(ACC.OWNER_USER_ID, valueOf(requester().userId()));
        }

        if (!prevState.isImplicitMove(nextState)) {
            moreStep = moreStep.set(ACC.LAST_UPDATED_BY, valueOf(requester().userId()))
                    .set(ACC.LAST_UPDATE_TIMESTAMP, LocalDateTime.now());
        }

        int numOfUpdatedRecords = moreStep.where(ACC.ACC_ID.eq(valueOf(acc.accId())))
                .execute();
        boolean updated = numOfUpdatedRecords == 1;

        if (updated) {
            // update associations' state.
            updateAsccState(acc, nextState);
            updateBccState(acc, nextState);

            // Post-processing
            if (nextState == CcState.Published || nextState == CcState.Production) {
                // Issue #1298
                // Update 'deprecated' properties in associated BIEs
                if (acc.deprecated()) {
                    dslContext().update(ASBIE.join(ABIE).on(ASBIE.FROM_ABIE_ID.eq(ABIE.ABIE_ID)))
                            .set(ASBIE.IS_DEPRECATED, (byte) 1)
                            .where(ABIE.BASED_ACC_MANIFEST_ID.eq(valueOf(accManifestId)))
                            .execute();

                    dslContext().update(ASBIE.join(ASBIEP).on(ASBIE.TO_ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                                    .join(ABIE).on(ASBIEP.ROLE_OF_ABIE_ID.eq(ABIE.ABIE_ID)))
                            .set(ASBIE.IS_DEPRECATED, (byte) 1)
                            .where(ABIE.BASED_ACC_MANIFEST_ID.eq(valueOf(accManifestId)))
                            .execute();
                }
            }
        }

        return updated;
    }

    private void updateAsccState(AccSummaryRecord acc, CcState nextState) {
        CcState prevState = acc.state();

        UpdateSetMoreStep moreStep = dslContext().update(ASCC)
                .set(ASCC.STATE, nextState.name());

        // Change owner of CC when it restored.
        if (prevState == CcState.Deleted && nextState == CcState.WIP) {
            moreStep = moreStep.set(ASCC.OWNER_USER_ID, valueOf(requester().userId()));
        }

        if (!prevState.isImplicitMove(nextState)) {
            moreStep = moreStep.set(ASCC.LAST_UPDATED_BY, valueOf(requester().userId()))
                    .set(ASCC.LAST_UPDATE_TIMESTAMP, LocalDateTime.now());
        }

        var query = repositoryFactory().accQueryRepository(requester());
        var asccList = query.getAsccSummaryList(acc.accManifestId());
        moreStep.where(ASCC.ASCC_ID.in(valueOf(
                        asccList.stream()
                                .map(e -> e.asccId()).collect(Collectors.toSet())
                )))
                .execute();

        // Post-processing
        if (nextState == CcState.Published || nextState == CcState.Production) {
            // Issue #1298
            // Update 'deprecated' properties in associated BIEs
            Collection<AsccManifestId> deprecatedAsccManifestIdList =
                    asccList.stream().filter(e -> e.deprecated())
                            .map(e -> e.asccManifestId()).collect(Collectors.toSet());
            if (!deprecatedAsccManifestIdList.isEmpty()) {
                dslContext().update(ASBIE)
                        .set(ASBIE.IS_DEPRECATED, (byte) 1)
                        .where(ASBIE.BASED_ASCC_MANIFEST_ID.in(valueOf(deprecatedAsccManifestIdList)))
                        .execute();
            }
        }
    }

    private void updateBccState(AccSummaryRecord acc, CcState nextState) {
        CcState prevState = acc.state();

        UpdateSetMoreStep moreStep = dslContext().update(BCC)
                .set(BCC.STATE, nextState.name());

        // Change owner of CC when it restored.
        if (prevState == CcState.Deleted && nextState == CcState.WIP) {
            moreStep = moreStep.set(BCC.OWNER_USER_ID, valueOf(requester().userId()));
        }

        if (!prevState.isImplicitMove(nextState)) {
            moreStep = moreStep.set(BCC.LAST_UPDATED_BY, valueOf(requester().userId()))
                    .set(BCC.LAST_UPDATE_TIMESTAMP, LocalDateTime.now());
        }

        var query = repositoryFactory().accQueryRepository(requester());
        var bccList = query.getBccSummaryList(acc.accManifestId());
        moreStep.where(BCC.BCC_ID.in(valueOf(
                        bccList.stream()
                                .map(e -> e.bccId()).collect(Collectors.toSet())
                )))
                .execute();

        // Post-processing
        if (nextState == CcState.Published || nextState == CcState.Production) {
            // Issue #1298
            // Update 'deprecated' properties in associated BIEs
            Collection<BccManifestId> deprecatedBccManifestIdList =
                    bccList.stream().filter(e -> e.deprecated())
                            .map(e -> e.bccManifestId()).collect(Collectors.toSet());
            dslContext().update(BBIE)
                    .set(BBIE.IS_DEPRECATED, (byte) 1)
                    .where(BBIE.BASED_BCC_MANIFEST_ID.in(valueOf(deprecatedBccManifestIdList)))
                    .execute();
        }
    }

    @Override
    public boolean updateBasedAccManifestId(AccManifestId accManifestId, AccManifestId basedAccManifestId) {
        if (accManifestId == null) {
            return false;
        }

        var query = repositoryFactory().accQueryRepository(requester());
        AccSummaryRecord acc = query.getAccSummary(accManifestId);
        if (acc == null) {
            return false;
        }

        int numOfUpdatedRecords = 0;
        if (basedAccManifestId == null) {
            numOfUpdatedRecords += dslContext().update(ACC)
                    .setNull(ACC.BASED_ACC_ID)
                    .set(ACC.LAST_UPDATED_BY, valueOf(requester().userId()))
                    .set(ACC.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                    .where(ACC.ACC_ID.eq(valueOf(acc.accId())))
                    .execute();

            numOfUpdatedRecords += dslContext().update(ACC_MANIFEST)
                    .setNull(ACC_MANIFEST.BASED_ACC_MANIFEST_ID)
                    .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(valueOf(acc.accManifestId())))
                    .execute();
        } else {
            if (basedAccManifestId.equals(acc.basedAccManifestId())) {
                return false;
            }

            AccSummaryRecord based = query.getAccSummary(basedAccManifestId);

            // Issue #1024
            ensureNoConflictsInAssociation(acc, based);

            numOfUpdatedRecords += dslContext().update(ACC)
                    .set(ACC.BASED_ACC_ID, valueOf(based.accId()))
                    .set(ACC.LAST_UPDATED_BY, valueOf(requester().userId()))
                    .set(ACC.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                    .where(ACC.ACC_ID.eq(valueOf(acc.accId())))
                    .execute();

            numOfUpdatedRecords += dslContext().update(ACC_MANIFEST)
                    .set(ACC_MANIFEST.BASED_ACC_MANIFEST_ID, valueOf(based.accManifestId()))
                    .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(valueOf(acc.accManifestId())))
                    .execute();
        }

        return numOfUpdatedRecords == 2;
    }

    private void ensureNoConflictsInAssociation(AccSummaryRecord acc,
                                                AccSummaryRecord based) {

        var query = repositoryFactory().accQueryRepository(requester());

        List<AsccSummaryRecord> asccList = query.getAsccSummaryList(acc.accManifestId());
        List<BccSummaryRecord> bccList = query.getBccSummaryList(acc.accManifestId());

        List<AsccpManifestId> asccpManifestIds = asccList.stream()
                .map(AsccSummaryRecord::toAsccpManifestId).collect(Collectors.toList());
        List<BccpManifestId> bccpManifestIds = bccList.stream()
                .map(BccSummaryRecord::toBccpManifestId).collect(Collectors.toList());

        while (based != null) {
            List<String> conflictAsccpList = dslContext().select(ASCCP_MANIFEST.DEN)
                    .from(ASCCP_MANIFEST)
                    .join(ASCC_MANIFEST).on(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID))
                    .where(and(
                            ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(based.accManifestId())),
                            ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.in(valueOf(asccpManifestIds)))
                    )
                    .fetchInto(String.class);
            if (!conflictAsccpList.isEmpty()) {
                if (conflictAsccpList.size() == 1) {
                    throw new IllegalArgumentException("There is a conflict in ASCCPs between the current ACC and the base ACC [" + conflictAsccpList.get(0) + "]");
                } else {
                    throw new IllegalArgumentException("There are conflicts in ASCCPs between the current ACC and the base ACC [" + String.join(", ", conflictAsccpList) + "]");
                }
            }

            List<String> conflictBccpList = dslContext().select(BCCP_MANIFEST.DEN)
                    .from(BCCP_MANIFEST)
                    .join(BCC_MANIFEST).on(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(BCC_MANIFEST.TO_BCCP_MANIFEST_ID))
                    .where(and(
                            BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(based.accManifestId())),
                            BCC_MANIFEST.TO_BCCP_MANIFEST_ID.in(valueOf(bccpManifestIds)))
                    )
                    .fetchInto(String.class);

            if (!conflictBccpList.isEmpty()) {
                if (conflictBccpList.size() == 1) {
                    throw new IllegalArgumentException("There is a conflict in BCCPs between the current ACC and the base ACC [" + conflictBccpList.get(0) + "]");
                } else {
                    throw new IllegalArgumentException("There are conflicts in BCCPs between the current ACC and the base ACC [" + String.join(", ", conflictBccpList) + "]");
                }
            }

            based = query.getAccSummary(based.basedAccManifestId());
        }
    }

    @Override
    public boolean updateLogId(AccManifestId accManifestId, LogId logId) {
        int numOfUpdatedRecords = dslContext().update(ACC_MANIFEST)
                .set(ACC_MANIFEST.LOG_ID, valueOf(logId))
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(valueOf(accManifestId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public AsccManifestId createAscc(AccManifestId accManifestId,
                                     AsccpManifestId asccpManifestId,
                                     @Nullable Integer pos,
                                     @Nullable Cardinality cardinality,
                                     boolean skipDependencyCheck) {

        var accQuery = repositoryFactory().accQueryRepository(requester());

        AccSummaryRecord fromAcc = accQuery.getAccSummary(accManifestId);
        if (fromAcc == null) {
            return null;
        }

        var asccpQuery = repositoryFactory().asccpQueryRepository(requester());

        AsccpSummaryRecord toAsccp = asccpQuery.getAsccpSummary(asccpManifestId);
        if (toAsccp == null) {
            return null;
        }

        if (!skipDependencyCheck) {
            // Issue #1192
            /*
             * @TODO:
             *   The Service layer validates whether the data is correct,
             *   while the Repository layer is responsible for executing the requested query.
             *   Therefore, this part should be moved to the Service layer.
             */
            List<String> denPathList = new ArrayList<>();
            denPathList.add(fromAcc.den());

            ensureNoConflictInForward(fromAcc, toAsccp, denPathList);
            ensureNoConflictInBackward(fromAcc, toAsccp, denPathList);
        }

        AsccRecord ascc = new AsccRecord();
        ascc.setGuid(ScoreGuidUtils.randomGuid());
        ascc.setCardinalityMin((cardinality != null) ? cardinality.min() : 0);
        ascc.setCardinalityMax((cardinality != null) ? cardinality.max() : -1);
        ascc.setSeqKey(0); // @deprecated
        ascc.setFromAccId(valueOf(fromAcc.accId()));
        ascc.setToAsccpId(valueOf(toAsccp.asccpId()));
        ascc.setState(fromAcc.state().name());
        ascc.setIsDeprecated((byte) 0);
        ascc.setCreatedBy(valueOf(requester().userId()));
        ascc.setLastUpdatedBy(valueOf(requester().userId()));
        ascc.setOwnerUserId(valueOf(requester().userId()));
        LocalDateTime timestamp = LocalDateTime.now();
        ascc.setCreationTimestamp(timestamp);
        ascc.setLastUpdateTimestamp(timestamp);
        ascc.setAsccId(
                dslContext().insertInto(ASCC)
                        .set(ascc)
                        .returning(ASCC.ASCC_ID).fetchOne().getAsccId()
        );

        AsccManifestRecord asccManifestRecord = new AsccManifestRecord();
        asccManifestRecord.setAsccId(ascc.getAsccId());
        asccManifestRecord.setReleaseId(valueOf(fromAcc.release().releaseId()));
        asccManifestRecord.setFromAccManifestId(valueOf(fromAcc.accManifestId()));
        asccManifestRecord.setToAsccpManifestId(valueOf(toAsccp.asccpManifestId()));
        asccManifestRecord.setDen(fromAcc.objectClassTerm() + ". " + toAsccp.den());
        AsccManifestId asccManifestId = new AsccManifestId(
                dslContext().insertInto(ASCC_MANIFEST)
                        .set(asccManifestRecord)
                        .returning(ASCC_MANIFEST.ASCC_MANIFEST_ID)
                        .fetchOne().getAsccManifestId().toBigInteger());

        seqKeyHandler(accQuery.getAsccSummary(asccManifestId)).moveTo((pos != null) ? pos : -1);

        return asccManifestId;
    }

    private void ensureNoConflictInForward(AccSummaryRecord fromAcc,
                                           AsccpSummaryRecord toAsccp,
                                           List<String> denPathList) {
        if (fromAcc == null) {
            return;
        }

        var query = repositoryFactory().accQueryRepository(requester());

        // Issue #1463
        // Find conflicts under 'Group' ACCs.
        {
            List<Record2<ULong, String>> groupAccRecords = dslContext().select(ACC_MANIFEST.ACC_MANIFEST_ID, ACC_MANIFEST.DEN)
                    .from(ASCC_MANIFEST)
                    .join(ASCCP_MANIFEST).on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                    .join(ACC_MANIFEST).on(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                    .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                    .where(and(
                            ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(fromAcc.accManifestId())),
                            ACC.OAGIS_COMPONENT_TYPE.in(Arrays.asList(SemanticGroup.getValue(), UserExtensionGroup.getValue()))
                    ))
                    .fetch();

            for (Record2<ULong, String> groupAccRecord : groupAccRecords) {
                List<ULong> groupAccManifestIdAncestryChart = new ArrayList<>();
                ULong groupAccManifestId = groupAccRecord.get(ACC_MANIFEST.ACC_MANIFEST_ID);
                groupAccManifestIdAncestryChart.add(groupAccManifestId);

                while (groupAccManifestId != null) {
                    groupAccManifestId = dslContext().select(ACC_MANIFEST.BASED_ACC_MANIFEST_ID)
                            .from(ACC_MANIFEST)
                            .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(groupAccManifestId))
                            .fetchOptionalInto(ULong.class).orElse(null);
                    if (groupAccManifestId != null) {
                        groupAccManifestIdAncestryChart.add(groupAccManifestId);
                    }
                }

                int cnt = dslContext().selectCount()
                        .from(ASCC_MANIFEST)
                        .join(ASCCP_MANIFEST).on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                        .where(and(
                                ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.in(groupAccManifestIdAncestryChart),
                                ASCCP_MANIFEST.ASCCP_ID.eq(valueOf(toAsccp.asccpId()))
                        ))
                        .fetchOneInto(Integer.class);
                if (cnt > 0) {
                    denPathList.add(groupAccRecord.get(ACC_MANIFEST.DEN));
                    throw new IllegalArgumentException("ACC [" + String.join(" > ", denPathList) + "] already has ASCCP [" + toAsccp.den() + "]");
                }
            }
        }

        // Check conflicts in forward
        int cnt = dslContext().selectCount()
                .from(ASCC_MANIFEST)
                .join(ASCCP_MANIFEST).on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .where(and(
                        ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(fromAcc.accManifestId())),
                        ASCCP_MANIFEST.ASCCP_ID.eq(valueOf(toAsccp.asccpId()))
                ))
                .fetchOneInto(Integer.class);
        if (cnt > 0) {
            throw new IllegalArgumentException("ACC [" + String.join(" > ", denPathList) + "] already has ASCCP [" + toAsccp.den() + "]");
        }

        AccSummaryRecord basedAcc = query.getAccSummary(fromAcc.basedAccManifestId());
        if (basedAcc != null) {
            List<String> childDenPathList = new ArrayList<>(denPathList);
            childDenPathList.add(basedAcc.den());
            ensureNoConflictInForward(basedAcc, toAsccp, childDenPathList);
        }
    }

    private void ensureNoConflictInBackward(AccSummaryRecord fromAcc,
                                            AsccpSummaryRecord toAsccp,
                                            List<String> denPathList) {

        if (fromAcc == null) {
            return;
        }

        var query = repositoryFactory().accQueryRepository(requester());
        List<AccSummaryRecord> inheritedAccList = query.getInheritedAccSummaryList(fromAcc.accManifestId());
        if (inheritedAccList.isEmpty()) {
            return;
        }

        for (AccSummaryRecord inheritedAcc : inheritedAccList) {
            AccManifestId childAccManifestId = inheritedAcc.accManifestId();
            List<String> childDenPathList = new ArrayList<>(denPathList);
            childDenPathList.add(inheritedAcc.den());

            // Issue #1463
            // Find conflicts under 'Group' ACCs.
            {
                List<Record2<ULong, String>> groupAccRecords = dslContext().select(ACC_MANIFEST.ACC_MANIFEST_ID, ACC_MANIFEST.DEN)
                        .from(ASCC_MANIFEST)
                        .join(ASCCP_MANIFEST).on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                        .join(ACC_MANIFEST).on(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                        .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                        .where(and(
                                ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(childAccManifestId)),
                                ACC.OAGIS_COMPONENT_TYPE.in(Arrays.asList(SemanticGroup.getValue(), UserExtensionGroup.getValue()))
                        ))
                        .fetch();

                for (Record2<ULong, String> groupAccRecord : groupAccRecords) {
                    List<ULong> groupAccManifestIdAncestryChart = new ArrayList<>();
                    ULong groupAccManifestId = groupAccRecord.get(ACC_MANIFEST.ACC_MANIFEST_ID);
                    groupAccManifestIdAncestryChart.add(groupAccManifestId);

                    while (groupAccManifestId != null) {
                        groupAccManifestId = dslContext().select(ACC_MANIFEST.BASED_ACC_MANIFEST_ID)
                                .from(ACC_MANIFEST)
                                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(groupAccManifestId))
                                .fetchOptionalInto(ULong.class).orElse(null);
                        if (groupAccManifestId != null) {
                            groupAccManifestIdAncestryChart.add(groupAccManifestId);
                        }
                    }

                    int cnt = dslContext().selectCount()
                            .from(ASCC_MANIFEST)
                            .join(ASCCP_MANIFEST).on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                            .where(and(
                                    ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.in(groupAccManifestIdAncestryChart),
                                    ASCCP_MANIFEST.ASCCP_ID.eq(valueOf(toAsccp.asccpId()))
                            ))
                            .fetchOneInto(Integer.class);
                    if (cnt > 0) {
                        childDenPathList.add(groupAccRecord.get(ACC_MANIFEST.DEN));
                        throw new IllegalArgumentException("ACC [" + String.join(" < ", childDenPathList) + "] already has ASCCP [" + toAsccp.den() + "]");
                    }
                }
            }

            int cnt = dslContext().selectCount()
                    .from(ASCC_MANIFEST)
                    .join(ASCCP_MANIFEST).on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                    .where(and(
                            ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(childAccManifestId)),
                            ASCCP_MANIFEST.ASCCP_ID.eq(valueOf(toAsccp.asccpId()))
                    ))
                    .fetchOneInto(Integer.class);
            if (cnt > 0) {
                throw new IllegalArgumentException("ACC [" + String.join(" < ", childDenPathList) + "] already has ASCCP [" + toAsccp.den() + "]");
            }

            ensureNoConflictInBackward(inheritedAcc, toAsccp, childDenPathList);
        }
    }

    @Override
    public BccManifestId createBcc(AccManifestId accManifestId,
                                   BccpManifestId bccpManifestId,
                                   @Nullable Integer pos,
                                   @Nullable Cardinality cardinality,
                                   boolean skipDependencyCheck) {

        var accQuery = repositoryFactory().accQueryRepository(requester());

        AccSummaryRecord fromAcc = accQuery.getAccSummary(accManifestId);
        if (fromAcc == null) {
            return null;
        }

        var bccpQuery = repositoryFactory().bccpQueryRepository(requester());

        BccpSummaryRecord toBccp = bccpQuery.getBccpSummary(bccpManifestId);
        if (toBccp == null) {
            return null;
        }

        if (!skipDependencyCheck) {
            // Issue #1192
            /*
             * @TODO:
             *   The Service layer validates whether the data is correct,
             *   while the Repository layer is responsible for executing the requested query.
             *   Therefore, this part should be moved to the Service layer.
             */
            List<String> denPathList = new ArrayList<>();
            denPathList.add(fromAcc.den());

            ensureNoConflictInForward(fromAcc, toBccp, denPathList);
            ensureNoConflictInBackward(fromAcc, toBccp, denPathList);
        }

        BccRecord bcc = new BccRecord();
        bcc.setGuid(ScoreGuidUtils.randomGuid());
        bcc.setCardinalityMin((cardinality != null) ? cardinality.min() : 0);
        bcc.setCardinalityMax((cardinality != null) ? cardinality.max() : -1);
        bcc.setSeqKey(0); // @deprecated
        bcc.setFromAccId(valueOf(fromAcc.accId()));
        bcc.setToBccpId(valueOf(toBccp.bccpId()));
        bcc.setEntityType(Element.getValue());
        bcc.setState(CcState.WIP.name());
        bcc.setIsDeprecated((byte) 0);
        bcc.setIsNillable((byte) 0);
        bcc.setCreatedBy(valueOf(requester().userId()));
        bcc.setLastUpdatedBy(valueOf(requester().userId()));
        bcc.setOwnerUserId(valueOf(requester().userId()));
        LocalDateTime timestamp = LocalDateTime.now();
        bcc.setCreationTimestamp(timestamp);
        bcc.setLastUpdateTimestamp(timestamp);
        bcc.setBccId(
                dslContext().insertInto(BCC)
                        .set(bcc)
                        .returning(BCC.BCC_ID).fetchOne().getBccId()
        );

        BccManifestRecord asccManifestRecord = new BccManifestRecord();
        asccManifestRecord.setBccId(bcc.getBccId());
        asccManifestRecord.setReleaseId(valueOf(fromAcc.release().releaseId()));
        asccManifestRecord.setFromAccManifestId(valueOf(fromAcc.accManifestId()));
        asccManifestRecord.setToBccpManifestId(valueOf(toBccp.bccpManifestId()));
        asccManifestRecord.setDen(fromAcc.objectClassTerm() + ". " + toBccp.den());
        BccManifestId bccManifestId = new BccManifestId(
                dslContext().insertInto(BCC_MANIFEST)
                        .set(asccManifestRecord)
                        .returning(BCC_MANIFEST.BCC_MANIFEST_ID)
                        .fetchOne().getBccManifestId().toBigInteger());

        seqKeyHandler(accQuery.getBccSummary(bccManifestId)).moveTo((pos != null) ? pos : -1);

        return bccManifestId;
    }

    private void ensureNoConflictInForward(AccSummaryRecord fromAcc,
                                           BccpSummaryRecord toBccp,
                                           List<String> denPathList) {
        if (fromAcc == null) {
            return;
        }

        var query = repositoryFactory().accQueryRepository(requester());

        // Issue #1463
        // Find conflicts under 'Group' ACCs.
        {
            List<Record2<ULong, String>> groupAccRecords = dslContext().select(ACC_MANIFEST.ACC_MANIFEST_ID, ACC_MANIFEST.DEN)
                    .from(ASCC_MANIFEST)
                    .join(ASCCP_MANIFEST).on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                    .join(ACC_MANIFEST).on(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                    .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                    .where(and(
                            ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(fromAcc.accManifestId())),
                            ACC.OAGIS_COMPONENT_TYPE.in(Arrays.asList(SemanticGroup.getValue(), UserExtensionGroup.getValue()))
                    ))
                    .fetch();

            for (Record2<ULong, String> groupAccRecord : groupAccRecords) {
                List<ULong> groupAccManifestIdAncestryChart = new ArrayList<>();
                ULong groupAccManifestId = groupAccRecord.get(ACC_MANIFEST.ACC_MANIFEST_ID);
                groupAccManifestIdAncestryChart.add(groupAccManifestId);

                while (groupAccManifestId != null) {
                    groupAccManifestId = dslContext().select(ACC_MANIFEST.BASED_ACC_MANIFEST_ID)
                            .from(ACC_MANIFEST)
                            .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(groupAccManifestId))
                            .fetchOptionalInto(ULong.class).orElse(null);
                    if (groupAccManifestId != null) {
                        groupAccManifestIdAncestryChart.add(groupAccManifestId);
                    }
                }

                int cnt = dslContext().selectCount()
                        .from(BCC_MANIFEST)
                        .join(BCCP_MANIFEST).on(BCC_MANIFEST.TO_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.BCCP_MANIFEST_ID))
                        .where(and(
                                BCC_MANIFEST.FROM_ACC_MANIFEST_ID.in(groupAccManifestIdAncestryChart),
                                BCCP_MANIFEST.BCCP_ID.eq(valueOf(toBccp.bccpId()))
                        ))
                        .fetchOneInto(Integer.class);
                if (cnt > 0) {
                    denPathList.add(groupAccRecord.get(ACC_MANIFEST.DEN));
                    throw new IllegalArgumentException("ACC [" + String.join(" > ", denPathList) + "] already has BCCP [" + toBccp.den() + "]");
                }
            }
        }

        // Check conflicts in forward
        int cnt = dslContext().selectCount()
                .from(BCC_MANIFEST)
                .join(BCCP_MANIFEST).on(BCC_MANIFEST.TO_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.BCCP_MANIFEST_ID))
                .where(and(
                        BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(fromAcc.accManifestId())),
                        BCCP_MANIFEST.BCCP_ID.eq(valueOf(toBccp.bccpId()))
                ))
                .fetchOneInto(Integer.class);
        if (cnt > 0) {
            throw new IllegalArgumentException("ACC [" + String.join(" > ", denPathList) + "] already has BCCP [" + toBccp.den() + "]");
        }

        AccSummaryRecord basedAcc = query.getAccSummary(fromAcc.basedAccManifestId());
        if (basedAcc != null) {
            List<String> childDenPathList = new ArrayList<>(denPathList);
            childDenPathList.add(basedAcc.den());
            ensureNoConflictInForward(basedAcc, toBccp, childDenPathList);
        }
    }

    private void ensureNoConflictInBackward(AccSummaryRecord fromAcc,
                                            BccpSummaryRecord toBccp,
                                            List<String> denPathList) {
        if (fromAcc == null) {
            return;
        }

        var query = repositoryFactory().accQueryRepository(requester());

        List<AccSummaryRecord> inheritedAccList = query.getInheritedAccSummaryList(fromAcc.accManifestId());
        if (inheritedAccList.isEmpty()) {
            return;
        }

        for (AccSummaryRecord inheritedAcc : inheritedAccList) {
            AccManifestId childAccManifestId = inheritedAcc.accManifestId();
            List<String> childDenPathList = new ArrayList<>(denPathList);
            childDenPathList.add(inheritedAcc.den());

            // Issue #1463
            // Find conflicts under 'Group' ACCs.
            {
                List<Record2<ULong, String>> groupAccRecords = dslContext().select(ACC_MANIFEST.ACC_MANIFEST_ID, ACC_MANIFEST.DEN)
                        .from(ASCC_MANIFEST)
                        .join(ASCCP_MANIFEST).on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                        .join(ACC_MANIFEST).on(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                        .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                        .where(and(
                                ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(childAccManifestId)),
                                ACC.OAGIS_COMPONENT_TYPE.in(Arrays.asList(SemanticGroup.getValue(), UserExtensionGroup.getValue()))
                        ))
                        .fetch();

                for (Record2<ULong, String> groupAccRecord : groupAccRecords) {
                    List<ULong> groupAccManifestIdAncestryChart = new ArrayList<>();
                    ULong groupAccManifestId = groupAccRecord.get(ACC_MANIFEST.ACC_MANIFEST_ID);
                    groupAccManifestIdAncestryChart.add(groupAccManifestId);

                    while (groupAccManifestId != null) {
                        groupAccManifestId = dslContext().select(ACC_MANIFEST.BASED_ACC_MANIFEST_ID)
                                .from(ACC_MANIFEST)
                                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(groupAccManifestId))
                                .fetchOptionalInto(ULong.class).orElse(null);
                        if (groupAccManifestId != null) {
                            groupAccManifestIdAncestryChart.add(groupAccManifestId);
                        }
                    }

                    int cnt = dslContext().selectCount()
                            .from(BCC_MANIFEST)
                            .join(BCCP_MANIFEST).on(BCC_MANIFEST.TO_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.BCCP_MANIFEST_ID))
                            .where(and(
                                    BCC_MANIFEST.FROM_ACC_MANIFEST_ID.in(groupAccManifestIdAncestryChart),
                                    BCCP_MANIFEST.BCCP_ID.eq(valueOf(toBccp.bccpId()))
                            ))
                            .fetchOneInto(Integer.class);
                    if (cnt > 0) {
                        childDenPathList.add(groupAccRecord.get(ACC_MANIFEST.DEN));
                        throw new IllegalArgumentException("ACC [" + String.join(" < ", childDenPathList) + "] already has BCCP [" + toBccp.den() + "]");
                    }
                }
            }

            int cnt = dslContext().selectCount()
                    .from(BCC_MANIFEST)
                    .join(BCCP_MANIFEST).on(BCC_MANIFEST.TO_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.BCCP_MANIFEST_ID))
                    .where(and(
                            BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(childAccManifestId)),
                            BCCP_MANIFEST.BCCP_ID.eq(valueOf(toBccp.bccpId()))
                    ))
                    .fetchOneInto(Integer.class);
            if (cnt > 0) {
                throw new IllegalArgumentException("ACC [" + String.join(" < ", childDenPathList) + "] already has BCCP [" + toBccp.den() + "]");
            }

            ensureNoConflictInBackward(inheritedAcc, toBccp, childDenPathList);
        }
    }

    @Override
    public boolean update(AccManifestId accManifestId,
                          @Nullable String objectClassTerm,
                          @Nullable OagisComponentType componentType,
                          @Nullable Boolean isAbstract,
                          @Nullable Boolean deprecated,
                          @Nullable NamespaceId namespaceId,
                          @Nullable Definition definition) {

        if (accManifestId == null) {
            return false;
        }

        AccSummaryRecord acc = repositoryFactory().accQueryRepository(requester()).getAccSummary(accManifestId);
        if (acc == null) {
            return false;
        }

        // update acc record.
        boolean denNeedsToUpdate = false;
        UpdateSetFirstStep<AccRecord> firstStep = dslContext().update(ACC);
        UpdateSetMoreStep<AccRecord> moreStep = null;
        if (compare(acc.objectClassTerm(), objectClassTerm) != 0) {
            if (!hasLength(objectClassTerm)) {
                throw new IllegalArgumentException("Object class term must not be empty.");
            }

            denNeedsToUpdate = true;
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ACC.OBJECT_CLASS_TERM, objectClassTerm);

            String den = objectClassTerm + ". Details";
            dslContext().update(ACC_MANIFEST)
                    .set(ACC_MANIFEST.DEN, den)
                    .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(valueOf(acc.accManifestId())))
                    .execute();
        }
        if (definition != null) {
            if (acc.definition() == null || compare(acc.definition().content(), definition.content()) != 0) {
                if (hasLength(definition.content())) {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .set(ACC.DEFINITION, definition.content());
                } else {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .setNull(ACC.DEFINITION);
                }
            }
            if (acc.definition() == null || compare(acc.definition().source(), definition.source()) != 0) {
                if (hasLength(definition.source())) {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .set(ACC.DEFINITION_SOURCE, definition.source());
                } else {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .setNull(ACC.DEFINITION_SOURCE);
                }
            }
        }
        if (componentType != null && acc.componentType() != componentType) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ACC.OAGIS_COMPONENT_TYPE, componentType.getValue());
        }
        if (isAbstract != null && acc.isAbstract() != isAbstract) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ACC.IS_ABSTRACT, (byte) (isAbstract ? 1 : 0));
        }
        if (deprecated != null && acc.deprecated() != deprecated) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ACC.IS_DEPRECATED, (byte) (deprecated ? 1 : 0));
        }
        if (namespaceId != null) {
            if (!namespaceId.equals(acc.namespaceId())) {
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .set(ACC.NAMESPACE_ID, valueOf(namespaceId));
            }
        } else {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .setNull(ACC.NAMESPACE_ID);
        }

        int numOfUpdatedRecords = 0;
        if (moreStep != null) {
            numOfUpdatedRecords = moreStep.set(ACC.LAST_UPDATED_BY, valueOf(requester().userId()))
                    .set(ACC.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                    .where(ACC.ACC_ID.eq(valueOf(acc.accId())))
                    .execute();
        }

        if (denNeedsToUpdate) {
            for (AsccManifestRecord asccManifestRecord : dslContext().selectFrom(ASCC_MANIFEST)
                    .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(acc.accManifestId())))
                    .fetch()) {

                String asccpDen = dslContext().select(ASCCP_MANIFEST.DEN)
                        .from(ASCCP_MANIFEST)
                        .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(asccManifestRecord.getToAsccpManifestId()))
                        .fetchOneInto(String.class);

                asccManifestRecord.setDen(objectClassTerm + ". " + asccpDen);
                asccManifestRecord.update(ASCC_MANIFEST.DEN);
            }

            for (BccManifestRecord bccManifestRecord : dslContext().selectFrom(BCC_MANIFEST)
                    .where(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(acc.accManifestId())))
                    .fetch()) {

                String bccpDen = dslContext().select(BCCP_MANIFEST.DEN)
                        .from(BCCP_MANIFEST)
                        .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(bccManifestRecord.getToBccpManifestId()))
                        .fetchOneInto(String.class);

                bccManifestRecord.setDen(objectClassTerm + ". " + bccpDen);
                bccManifestRecord.update(BCC_MANIFEST.DEN);
            }

            for (AsccpManifestRecord asccpManifestRecord : dslContext().selectFrom(ASCCP_MANIFEST)
                    .where(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID.eq(valueOf(acc.accManifestId())))
                    .fetch()) {

                AsccpRecord asccpRecord = dslContext().selectFrom(ASCCP)
                        .where(ASCCP.ASCCP_ID.eq(asccpManifestRecord.getAsccpId()))
                        .fetchOne();

                asccpManifestRecord.setDen(asccpRecord.getPropertyTerm() + ". " + objectClassTerm);
                asccpManifestRecord.update(ASCCP_MANIFEST.DEN);

                for (AsccManifestRecord asccManifestRecord : dslContext().selectFrom(ASCC_MANIFEST)
                        .where(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(asccpManifestRecord.getAsccpManifestId()))
                        .fetch()) {

                    String _objectClassTerm = dslContext().select(ACC.OBJECT_CLASS_TERM)
                            .from(ACC)
                            .join(ACC_MANIFEST).on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                            .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(asccManifestRecord.getFromAccManifestId()))
                            .fetchOneInto(String.class);
                    asccManifestRecord.setDen(_objectClassTerm + ". " + asccpManifestRecord.getDen());
                    asccManifestRecord.update(ASCC_MANIFEST.DEN);
                }
            }
        }

        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean update(AsccManifestId asccManifestId,
                          @Nullable Cardinality cardinality,
                          @Nullable Boolean deprecated,
                          @Nullable Definition definition) {

        var query = repositoryFactory().accQueryRepository(requester());
        AsccSummaryRecord ascc = query.getAsccSummary(asccManifestId);
        if (ascc == null) {
            return false;
        }
        AccSummaryRecord acc = query.getAccSummary(ascc.fromAccManifestId());
        if (acc == null) {
            return false;
        }

        // update ascc record.
        UpdateSetFirstStep<AsccRecord> firstStep = dslContext().update(ASCC);
        UpdateSetMoreStep<AsccRecord> moreStep = null;

        String den = acc.objectClassTerm() + ". " + dslContext().select(ASCCP_MANIFEST.DEN)
                .from(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(valueOf(ascc.toAsccpManifestId())))
                .fetchOneInto(String.class);
        boolean denChanged = compare(ascc.den(), den) != 0;

        if (definition != null) {
            if (ascc.definition() == null || compare(ascc.definition().content(), definition.content()) != 0) {
                if (hasLength(definition.content())) {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .set(ASCC.DEFINITION, definition.content());
                } else {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .setNull(ASCC.DEFINITION);
                }
            }
            if (ascc.definition() == null || compare(ascc.definition().source(), definition.source()) != 0) {
                if (hasLength(definition.source())) {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .set(ASCC.DEFINITION_SOURCE, definition.source());
                } else {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .setNull(ASCC.DEFINITION_SOURCE);
                }
            }
        }
        if (deprecated != null && ascc.deprecated() != deprecated) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ASCC.IS_DEPRECATED, (byte) (deprecated ? 1 : 0));
        }

        if (cardinality != null) {
            int currentCardinalityMin = (cardinality.min() != null) ? cardinality.min() : ascc.cardinality().min();
            int currentCardinalityMax = (cardinality.max() != null) ? cardinality.max() : ascc.cardinality().max();

            if (currentCardinalityMin < 0) {
                throw new IllegalArgumentException("cardinalityMin cannot be less than 0");
            }
            if (currentCardinalityMax == -1) {
                currentCardinalityMax = Integer.MAX_VALUE; // Treat -1 as unbounded
            }
            if (currentCardinalityMax < 0) {
                throw new IllegalArgumentException("cardinalityMax cannot be less than 0");
            }
            if (currentCardinalityMin > currentCardinalityMax) {
                throw new IllegalArgumentException("cardinalityMin cannot be greater than cardinalityMax");
            }

            if (cardinality.min() != null && ascc.cardinality().min() != cardinality.min()) {
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .set(ASCC.CARDINALITY_MIN, cardinality.min());
            }
            if (cardinality.max() != null && ascc.cardinality().max() != cardinality.max()) {
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .set(ASCC.CARDINALITY_MAX, cardinality.max());
            }
        }

        int numOfUpdatedRecords = 0;
        if (moreStep != null || denChanged) {
            if (denChanged) {
                numOfUpdatedRecords = dslContext().update(AsccManifest.ASCC_MANIFEST)
                        .set(AsccManifest.ASCC_MANIFEST.DEN, den)
                        .where(AsccManifest.ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(valueOf(ascc.asccManifestId())))
                        .execute();
            }

            if (moreStep != null) {
                numOfUpdatedRecords = moreStep.set(ASCC.LAST_UPDATED_BY, valueOf(requester().userId()))
                        .set(ASCC.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                        .where(ASCC.ASCC_ID.eq(valueOf(ascc.asccId())))
                        .execute();
            }
        }

        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean syncDen(AsccManifestId asccManifestId) {
        if (asccManifestId == null) {
            return false;
        }

        String den = dslContext().select(ACC.OBJECT_CLASS_TERM, ASCCP_MANIFEST.DEN)
                .from(ACC)
                .join(ACC_MANIFEST).on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                .join(ASCC_MANIFEST).on(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID))
                .join(ASCCP_MANIFEST).on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(valueOf(asccManifestId)))
                .fetchOne(record -> record.get(ACC.OBJECT_CLASS_TERM) + ". " + record.get(ASCCP_MANIFEST.DEN));

        int numOfUpdatedRecords = dslContext().update(ASCC_MANIFEST)
                .set(ASCC_MANIFEST.DEN, den)
                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(valueOf(asccManifestId)))
                .execute();

        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean update(BccManifestId bccManifestId,
                          @Nullable EntityType entityType,
                          @Nullable Cardinality cardinality,
                          @Nullable Boolean deprecated,
                          @Nullable Boolean nillable,
                          @Nullable ValueConstraint valueConstraint,
                          @Nullable Definition definition) {

        if (bccManifestId == null) {
            return false;
        }

        var query = repositoryFactory().accQueryRepository(requester());
        BccSummaryRecord bcc = query.getBccSummary(bccManifestId);
        if (bcc == null) {
            return false;
        }
        AccSummaryRecord acc = query.getAccSummary(bcc.fromAccManifestId());
        if (acc == null) {
            return false;
        }

        // update ascc record.
        UpdateSetFirstStep<BccRecord> firstStep = dslContext().update(BCC);
        UpdateSetMoreStep<BccRecord> moreStep = null;

        String den = acc.objectClassTerm() + ". " + dslContext().select(BCCP_MANIFEST.DEN)
                .from(BCCP_MANIFEST)
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(valueOf(bcc.toBccpManifestId())))
                .fetchOneInto(String.class);
        boolean denChanged = compare(bcc.den(), den) != 0;

        if (definition != null) {
            if (bcc.definition() == null || compare(bcc.definition().content(), definition.content()) != 0) {
                if (hasLength(definition.content())) {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .set(BCC.DEFINITION, definition.content());
                } else {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .setNull(BCC.DEFINITION);
                }
            }
            if (bcc.definition() == null || compare(bcc.definition().source(), definition.source()) != 0) {
                if (hasLength(definition.source())) {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .set(BCC.DEFINITION_SOURCE, definition.source());
                } else {
                    moreStep = ((moreStep != null) ? moreStep : firstStep)
                            .setNull(BCC.DEFINITION_SOURCE);
                }
            }
        }
        if (entityType != null && bcc.entityType() != entityType) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(BCC.ENTITY_TYPE, entityType.getValue());

            if (entityType == Element) {
                seqKeyHandler(bcc).moveTo(LAST);
            } else if (entityType == Attribute) {
                seqKeyHandler(bcc).moveTo(LAST_OF_ATTR);

                // Issue #919
                cardinality = new Cardinality(0, 1);
            }
        }
        if (deprecated != null && bcc.deprecated() != deprecated) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(BCC.IS_DEPRECATED, (byte) (deprecated ? 1 : 0));
        }
        if (nillable != null && bcc.nillable() != nillable) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(BCC.IS_NILLABLE, (byte) (nillable ? 1 : 0));
        }

        if (cardinality != null) {
            int currentCardinalityMin = (cardinality.min() != null) ? cardinality.min() : bcc.cardinality().min();
            int currentCardinalityMax = (cardinality.max() != null) ? cardinality.max() : bcc.cardinality().max();

            if (currentCardinalityMin < 0) {
                throw new IllegalArgumentException("cardinalityMin cannot be less than 0");
            }
            if (currentCardinalityMax == -1) {
                currentCardinalityMax = Integer.MAX_VALUE; // Treat -1 as unbounded
            }
            if (currentCardinalityMax < 0) {
                throw new IllegalArgumentException("cardinalityMax cannot be less than 0");
            }
            if (currentCardinalityMin > currentCardinalityMax) {
                throw new IllegalArgumentException("cardinalityMin cannot be greater than cardinalityMax");
            }

            if (cardinality.min() != null && bcc.cardinality().min() != cardinality.min()) {
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .set(BCC.CARDINALITY_MIN, cardinality.min());
            }
            if (cardinality.max() != null && bcc.cardinality().max() != cardinality.max()) {
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .set(BCC.CARDINALITY_MAX, cardinality.max());
            }
        }

        if (valueConstraint != null) {
            if (valueConstraint.hasFixedValue() &&
                    (bcc.valueConstraint() == null || compare(bcc.valueConstraint().fixedValue(), valueConstraint.fixedValue()) != 0)) {
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .set(BCC.FIXED_VALUE, valueConstraint.fixedValue())
                        .setNull(BCC.DEFAULT_VALUE);
            } else if (valueConstraint.hasDefaultValue() &&
                    (bcc.valueConstraint() == null || compare(bcc.valueConstraint().defaultValue(), valueConstraint.defaultValue()) != 0)) {
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .set(BCC.DEFAULT_VALUE, valueConstraint.defaultValue())
                        .setNull(BCC.FIXED_VALUE);
            } else {
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .setNull(BCC.DEFAULT_VALUE)
                        .setNull(BCC.FIXED_VALUE);
            }
        }

        int numOfUpdatedRecords = 0;
        if (moreStep != null || denChanged) {
            if (denChanged) {
                numOfUpdatedRecords = dslContext().update(BCC_MANIFEST)
                        .set(BCC_MANIFEST.DEN, den)
                        .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(valueOf(bcc.bccManifestId())))
                        .execute();
            }

            if (moreStep != null) {
                numOfUpdatedRecords = moreStep.set(BCC.LAST_UPDATED_BY, valueOf(requester().userId()))
                        .set(BCC.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                        .where(BCC.BCC_ID.eq(valueOf(bcc.bccId())))
                        .execute();
            }
        }

        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean syncDen(BccManifestId bccManifestId) {
        if (bccManifestId == null) {
            return false;
        }

        String den = dslContext().select(ACC.OBJECT_CLASS_TERM, BCCP_MANIFEST.DEN)
                .from(ACC)
                .join(ACC_MANIFEST).on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                .join(BCC_MANIFEST).on(ACC_MANIFEST.ACC_MANIFEST_ID.eq(BCC_MANIFEST.FROM_ACC_MANIFEST_ID))
                .join(BCCP_MANIFEST).on(BCC_MANIFEST.TO_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.BCCP_MANIFEST_ID))
                .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(valueOf(bccManifestId)))
                .fetchOne(record -> record.get(ACC.OBJECT_CLASS_TERM) + ". " + record.get(BCCP_MANIFEST.DEN));

        int numOfUpdatedRecords = dslContext().update(BCC_MANIFEST)
                .set(BCC_MANIFEST.DEN, den)
                .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(valueOf(bccManifestId)))
                .execute();

        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean delete(AccManifestId accManifestId) {

        var query = repositoryFactory().accQueryRepository(requester());
        AccSummaryRecord acc = query.getAccSummary(accManifestId);

        dslContext().update(ACC_MANIFEST)
                .setNull(ACC_MANIFEST.LOG_ID)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(valueOf(accManifestId)))
                .execute();

        dslContext().update(LOG)
                .setNull(LOG.PREV_LOG_ID)
                .setNull(LOG.NEXT_LOG_ID)
                .where(LOG.REFERENCE.eq(acc.guid().value()))
                .execute();

        dslContext().deleteFrom(LOG)
                .where(LOG.REFERENCE.eq(acc.guid().value()))
                .execute();

        // discard SEQ_KEYs
        dslContext().update(ASCC_MANIFEST)
                .setNull(ASCC_MANIFEST.SEQ_KEY_ID)
                .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(accManifestId)))
                .execute();

        dslContext().update(BCC_MANIFEST)
                .setNull(BCC_MANIFEST.SEQ_KEY_ID)
                .where(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(accManifestId)))
                .execute();

        dslContext().update(SEQ_KEY)
                .setNull(SEQ_KEY.PREV_SEQ_KEY_ID)
                .setNull(SEQ_KEY.NEXT_SEQ_KEY_ID)
                .where(SEQ_KEY.FROM_ACC_MANIFEST_ID.eq(valueOf(accManifestId)))
                .execute();

        dslContext().deleteFrom(SEQ_KEY)
                .where(SEQ_KEY.FROM_ACC_MANIFEST_ID.eq(valueOf(accManifestId)))
                .execute();

        // discard ASCCs
        List<AsccSummaryRecord> asccList = query.getAsccSummaryList(accManifestId);

        dslContext().deleteFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(accManifestId)))
                .execute();

        if (!asccList.isEmpty()) {
            dslContext().deleteFrom(ASCC)
                    .where(ASCC.ASCC_ID.in(valueOf(
                            asccList.stream().map(e -> e.asccId()).collect(Collectors.toSet()))
                    ))
                    .execute();
        }

        // discard BCCs
        List<BccSummaryRecord> bccList = query.getBccSummaryList(accManifestId);

        dslContext().deleteFrom(BCC_MANIFEST)
                .where(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(accManifestId)))
                .execute();

        if (!bccList.isEmpty()) {
            dslContext().deleteFrom(BCC)
                    .where(BCC.BCC_ID.in(valueOf(
                            bccList.stream().map(e -> e.bccId()).collect(Collectors.toSet()))
                    ))
                    .execute();
        }

        // discard assigned ACC in modules
        dslContext().deleteFrom(MODULE_ACC_MANIFEST)
                .where(MODULE_ACC_MANIFEST.ACC_MANIFEST_ID.eq(valueOf(accManifestId)))
                .execute();

        // discard corresponding tags
        dslContext().deleteFrom(ACC_MANIFEST_TAG)
                .where(ACC_MANIFEST_TAG.ACC_MANIFEST_ID.eq(valueOf(accManifestId)))
                .execute();

        // discard ACC
        dslContext().deleteFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(valueOf(accManifestId)))
                .execute();

        int numOfDeletedRecords = dslContext().deleteFrom(ACC)
                .where(ACC.ACC_ID.eq(valueOf(acc.accId())))
                .execute();
        return numOfDeletedRecords == 1;
    }

    @Override
    public boolean delete(AsccManifestId asccManifestId) {
        if (asccManifestId == null) {
            return false;
        }

        var query = repositoryFactory().accQueryRepository(requester());
        AsccSummaryRecord ascc = query.getAsccSummary(asccManifestId);

        seqKeyHandler(ascc).deleteCurrent();

        dslContext().update(ASCC_MANIFEST)
                .setNull(ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID)
                .where(ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID.eq(valueOf(asccManifestId)))
                .execute();
        dslContext().update(ASCC_MANIFEST)
                .setNull(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID)
                .where(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID.eq(valueOf(asccManifestId)))
                .execute();
        int numOfDeletedRecords = dslContext().deleteFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(valueOf(asccManifestId)))
                .execute();
        if (numOfDeletedRecords == 1) {
            int asccReferences = dslContext().selectCount()
                    .from(ASCC_MANIFEST)
                    .where(ASCC_MANIFEST.ASCC_ID.eq(valueOf(ascc.asccId())))
                    .fetchOptionalInto(Integer.class).orElse(0);
            if (asccReferences == 0) {
                dslContext().update(ASCC)
                        .setNull(ASCC.NEXT_ASCC_ID)
                        .where(ASCC.NEXT_ASCC_ID.eq(valueOf(ascc.asccId())))
                        .execute();
                dslContext().update(ASCC)
                        .setNull(ASCC.PREV_ASCC_ID)
                        .where(ASCC.PREV_ASCC_ID.eq(valueOf(ascc.asccId())))
                        .execute();
                dslContext().deleteFrom(ASCC)
                        .where(ASCC.ASCC_ID.eq(valueOf(ascc.asccId())))
                        .execute();
            }
        }

        return numOfDeletedRecords == 1;
    }

    @Override
    public boolean delete(BccManifestId bccManifestId) {
        if (bccManifestId == null) {
            return false;
        }

        var query = repositoryFactory().accQueryRepository(requester());
        BccSummaryRecord bcc = query.getBccSummary(bccManifestId);

        seqKeyHandler(bcc).deleteCurrent();

        dslContext().update(BCC_MANIFEST)
                .setNull(BCC_MANIFEST.NEXT_BCC_MANIFEST_ID)
                .where(BCC_MANIFEST.NEXT_BCC_MANIFEST_ID.eq(valueOf(bccManifestId)))
                .execute();
        dslContext().update(BCC_MANIFEST)
                .setNull(BCC_MANIFEST.PREV_BCC_MANIFEST_ID)
                .where(BCC_MANIFEST.PREV_BCC_MANIFEST_ID.eq(valueOf(bccManifestId)))
                .execute();
        int numOfDeletedRecords = dslContext().deleteFrom(BCC_MANIFEST)
                .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(valueOf(bccManifestId)))
                .execute();
        if (numOfDeletedRecords == 1) {
            int bccReferences = dslContext().selectCount()
                    .from(BCC_MANIFEST)
                    .where(BCC_MANIFEST.BCC_ID.eq(valueOf(bcc.bccId())))
                    .fetchOptionalInto(Integer.class).orElse(0);
            if (bccReferences == 0) {
                dslContext().update(BCC)
                        .setNull(BCC.NEXT_BCC_ID)
                        .where(BCC.NEXT_BCC_ID.eq(valueOf(bcc.bccId())))
                        .execute();
                dslContext().update(BCC)
                        .setNull(BCC.PREV_BCC_ID)
                        .where(BCC.PREV_BCC_ID.eq(valueOf(bcc.bccId())))
                        .execute();
                dslContext().deleteFrom(BCC)
                        .where(BCC.BCC_ID.eq(valueOf(bcc.bccId())))
                        .execute();
            }
        }

        return numOfDeletedRecords == 1;
    }

    public void revise(AccManifestId accManifestId) {

        var query = repositoryFactory().accQueryRepository(requester());
        AccDetailsRecord prevAcc = query.getAccDetails(accManifestId);

        // creates new acc for revised record.
        AccRecord nextAccRecord = dslContext().selectFrom(ACC)
                .where(ACC.ACC_ID.eq(valueOf(prevAcc.accId())))
                .fetchOne().copy();
        nextAccRecord.setState(CcState.WIP.name());
        nextAccRecord.setCreatedBy(valueOf(requester().userId()));
        nextAccRecord.setLastUpdatedBy(valueOf(requester().userId()));
        nextAccRecord.setOwnerUserId(valueOf(requester().userId()));
        LocalDateTime timestamp = LocalDateTime.now();
        nextAccRecord.setCreationTimestamp(timestamp);
        nextAccRecord.setLastUpdateTimestamp(timestamp);
        nextAccRecord.setPrevAccId(valueOf(prevAcc.accId()));
        AccId nextAccId = new AccId(dslContext().insertInto(ACC)
                .set(nextAccRecord)
                .returning(ACC.ACC_ID)
                .fetchOne().getAccId().toBigInteger());

        dslContext().update(ACC)
                .set(ACC.NEXT_ACC_ID, valueOf(nextAccId))
                .where(ACC.ACC_ID.eq(valueOf(prevAcc.accId())))
                .execute();

        // create new associations for revised record.
        reviseAscc(prevAcc, nextAccId);
        reviseBcc(prevAcc, nextAccId);

        dslContext().update(ACC_MANIFEST)
                .set(ACC_MANIFEST.ACC_ID, valueOf(nextAccId))
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(valueOf(prevAcc.accManifestId())))
                .execute();

        // update `conflict` for asccp_manifests' role_of_acc_manifest_id which indicates given acc manifest.
        dslContext().update(ASCCP_MANIFEST)
                .set(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID, valueOf(prevAcc.accManifestId()))
                .set(ASCCP_MANIFEST.CONFLICT, (byte) 1)
                .where(and(
                        ASCCP_MANIFEST.RELEASE_ID.eq(valueOf(prevAcc.release().releaseId())),
                        ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID.in(valueOf(Arrays.asList(
                                prevAcc.accManifestId(),
                                prevAcc.prevAccManifestId()
                        )))
                ))
                .execute();

        // update `conflict` for acc_manifests' based_acc_manifest_id which indicates given acc manifest.
        dslContext().update(ACC_MANIFEST)
                .set(ACC_MANIFEST.BASED_ACC_MANIFEST_ID, valueOf(prevAcc.accManifestId()))
                .set(ACC_MANIFEST.CONFLICT, (byte) 1)
                .where(and(
                        ACC_MANIFEST.RELEASE_ID.eq(valueOf(prevAcc.release().releaseId())),
                        ACC_MANIFEST.BASED_ACC_MANIFEST_ID.in(valueOf(Arrays.asList(
                                prevAcc.accManifestId(),
                                prevAcc.prevAccManifestId())
                        ))
                ))
                .execute();
    }

    private void reviseAscc(
            AccDetailsRecord prevAcc,
            AccId nextAccId) {

        var query = repositoryFactory().accQueryRepository(requester());
        for (AsccDetailsRecord ascc : query.getAsccDetailsList(prevAcc.accManifestId())) {
            AsccRecord nextAsccRecord = dslContext().selectFrom(ASCC)
                    .where(ASCC.ASCC_ID.eq(valueOf(ascc.asccId())))
                    .fetchOne().copy();
            nextAsccRecord.setFromAccId(valueOf(nextAccId));
            nextAsccRecord.setToAsccpId(valueOf(ascc.toAsccp().asccpId()));
            nextAsccRecord.setState(CcState.WIP.name());
            nextAsccRecord.setCreatedBy(valueOf(requester().userId()));
            nextAsccRecord.setLastUpdatedBy(valueOf(requester().userId()));
            nextAsccRecord.setOwnerUserId(valueOf(requester().userId()));
            LocalDateTime timestamp = LocalDateTime.now();
            nextAsccRecord.setCreationTimestamp(timestamp);
            nextAsccRecord.setLastUpdateTimestamp(timestamp);
            nextAsccRecord.setPrevAsccId(valueOf(ascc.asccId()));
            AsccId nextAsccId = new AsccId(
                    dslContext().insertInto(Tables.ASCC)
                            .set(nextAsccRecord)
                            .returning(Tables.ASCC.ASCC_ID)
                            .fetchOne().getAsccId().toBigInteger());

            dslContext().update(ASCC)
                    .set(ASCC.NEXT_ASCC_ID, valueOf(nextAsccId))
                    .where(ASCC.ASCC_ID.eq(valueOf(ascc.asccId())))
                    .execute();

            dslContext().update(ASCC_MANIFEST)
                    .set(ASCC_MANIFEST.ASCC_ID, valueOf(nextAsccId))
                    .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(valueOf(ascc.asccManifestId())))
                    .execute();
        }
    }

    private void reviseBcc(
            AccDetailsRecord prevAcc,
            AccId nextAccId) {

        var query = repositoryFactory().accQueryRepository(requester());
        for (BccDetailsRecord bcc : query.getBccDetailsList(prevAcc.accManifestId())) {
            BccRecord nextBccRecord = dslContext().selectFrom(BCC)
                    .where(BCC.BCC_ID.eq(valueOf(bcc.bccId())))
                    .fetchOne().copy();
            nextBccRecord.setFromAccId(valueOf(nextAccId));
            nextBccRecord.setToBccpId(valueOf(bcc.toBccp().bccpId()));
            nextBccRecord.setState(CcState.WIP.name());
            nextBccRecord.setCreatedBy(valueOf(requester().userId()));
            nextBccRecord.setLastUpdatedBy(valueOf(requester().userId()));
            nextBccRecord.setOwnerUserId(valueOf(requester().userId()));
            LocalDateTime timestamp = LocalDateTime.now();
            nextBccRecord.setCreationTimestamp(timestamp);
            nextBccRecord.setLastUpdateTimestamp(timestamp);
            nextBccRecord.setPrevBccId(valueOf(bcc.bccId()));
            BccId nextBccId = new BccId(
                    dslContext().insertInto(BCC)
                            .set(nextBccRecord)
                            .returning(BCC.BCC_ID)
                            .fetchOne().getBccId().toBigInteger());

            dslContext().update(BCC)
                    .set(BCC.NEXT_BCC_ID, valueOf(nextBccId))
                    .where(BCC.BCC_ID.eq(valueOf(bcc.bccId())))
                    .execute();

            dslContext().update(BCC_MANIFEST)
                    .set(BCC_MANIFEST.BCC_ID, valueOf(nextBccId))
                    .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(valueOf(bcc.bccManifestId())))
                    .execute();
        }
    }

    public void cancel(AccManifestId accManifestId) {

        var query = repositoryFactory().accQueryRepository(requester());
        AccDetailsRecord acc = query.getAccDetails(accManifestId);
        if (acc == null) {
            throw new IllegalArgumentException("Not found a target ACC");
        }

        AccRecord prevAccRecord = dslContext().selectFrom(ACC)
                .where(ACC.ACC_ID.eq(valueOf(acc.prevAccId())))
                .fetchOptional().orElse(null);

        if (prevAccRecord == null) {
            throw new IllegalArgumentException("Not found previous revision");
        }

        UpdateSetMoreStep moreStep = dslContext().update(ACC_MANIFEST)
                .set(ACC_MANIFEST.ACC_ID, prevAccRecord.getAccId());

        // update ACC MANIFEST's acc_id and revision_id
        if (prevAccRecord.getBasedAccId() != null) {
            AccRecord prevBasedAccRecord = dslContext().selectFrom(ACC)
                    .where(ACC.ACC_ID.eq(prevAccRecord.getBasedAccId()))
                    .fetchOne();

            AccManifestId prevBasedAccManifestId = new AccManifestId(
                    dslContext().select(ACC_MANIFEST.ACC_MANIFEST_ID)
                            .from(ACC_MANIFEST)
                            .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                            .where(and(
                                    ACC_MANIFEST.RELEASE_ID.eq(valueOf(acc.release().releaseId())),
                                    ACC.GUID.eq(prevBasedAccRecord.getGuid())
                            ))
                            .fetchOneInto(BigInteger.class)
            );

            moreStep.set(ACC_MANIFEST.BASED_ACC_MANIFEST_ID, valueOf(prevBasedAccManifestId));
        }

        moreStep.where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(valueOf(acc.accManifestId())))
                .execute();

        discardLogAssociations(acc);

        // unlink prev ACC
        dslContext().update(ACC)
                .setNull(ACC.NEXT_ACC_ID)
                .where(ACC.ACC_ID.eq(prevAccRecord.getAccId()))
                .execute();

        // clean logs up
        dslContext().update(ACC_MANIFEST)
                .setNull(ACC_MANIFEST.LOG_ID)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(valueOf(acc.accManifestId())))
                .execute();

        LogId logId = repositoryFactory().logCommandRepository(requester())
                .revertToStableStateByReference(acc.guid(), CcType.ACC);

        dslContext().update(ACC_MANIFEST)
                .set(ACC_MANIFEST.LOG_ID, valueOf(logId))
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(valueOf(acc.accManifestId())))
                .execute();

        // delete current ACC
        dslContext().deleteFrom(ACC)
                .where(ACC.ACC_ID.eq(valueOf(acc.accId())))
                .execute();
    }

    private void discardLogAssociations(AccDetailsRecord acc) {
        var accQuery = repositoryFactory().accQueryRepository(requester());

        AccDetailsRecord prevAcc = accQuery.getAccDetails(acc.prevAccManifestId());

        List<AsccManifestRecord> asccManifestRecords = dslContext().selectFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(acc.accManifestId()))).fetch();

        List<BccManifestRecord> bccManifestRecords = dslContext().selectFrom(BCC_MANIFEST)
                .where(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(acc.accManifestId()))).fetch();

        List<AsccManifestRecord> nullNextPrevAsccManifestRecords = Collections.emptyList();
        List<BccManifestRecord> nullNextPrevBccManifestRecords = Collections.emptyList();

        if (prevAcc != null) {
            nullNextPrevAsccManifestRecords = dslContext().selectFrom(ASCC_MANIFEST)
                    .where(and(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(prevAcc.accManifestId())),
                            ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID.isNull())).fetch();
            nullNextPrevBccManifestRecords = dslContext().selectFrom(BCC_MANIFEST)
                    .where(and(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(prevAcc.accManifestId())),
                            BCC_MANIFEST.NEXT_BCC_MANIFEST_ID.isNull())).fetch();
        }

        // delete SEQ_KEY for current ACC
        dslContext().update(SEQ_KEY)
                .setNull(SEQ_KEY.PREV_SEQ_KEY_ID)
                .setNull(SEQ_KEY.NEXT_SEQ_KEY_ID)
                .where(SEQ_KEY.FROM_ACC_MANIFEST_ID.eq(valueOf(acc.accManifestId()))).execute();
        dslContext().update(ASCC_MANIFEST)
                .setNull(ASCC_MANIFEST.SEQ_KEY_ID)
                .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(acc.accManifestId()))).execute();
        dslContext().update(BCC_MANIFEST)
                .setNull(BCC_MANIFEST.SEQ_KEY_ID)
                .where(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(acc.accManifestId()))).execute();
        dslContext().deleteFrom(SEQ_KEY).where(SEQ_KEY.FROM_ACC_MANIFEST_ID.eq(valueOf(acc.accManifestId()))).execute();

        for (AsccManifestRecord asccManifestRecord : asccManifestRecords) {
            AsccRecord asccRecord = dslContext().selectFrom(ASCC)
                    .where(ASCC.ASCC_ID.eq(asccManifestRecord.getAsccId())).fetchOne();

            if (asccRecord.getPrevAsccId() == null) {
                // delete ascc and ascc manifest which added this revision
                asccManifestRecord.delete();
                asccRecord.delete();
            } else {
                // delete ascc and update ascc manifest
                AsccRecord prevAsccRecord = dslContext().selectFrom(ASCC)
                        .where(ASCC.ASCC_ID.eq(asccRecord.getPrevAsccId())).fetchOne();
                prevAsccRecord.setNextAsccId(null);
                prevAsccRecord.update(ASCC.NEXT_ASCC_ID);
                asccManifestRecord.setAsccId(prevAsccRecord.getAsccId());
                asccManifestRecord.setSeqKeyId(null);
                asccManifestRecord.update(ASCC_MANIFEST.ASCC_ID, ASCC_MANIFEST.SEQ_KEY_ID);
                asccRecord.delete();
            }
        }

        var asccpQuery = repositoryFactory().asccpQueryRepository(requester());
        for (AsccManifestRecord asccManifestRecord : nullNextPrevAsccManifestRecords) {
            AsccpSummaryRecord toAsccp = asccpQuery.getAsccpSummary(
                    new AsccpManifestId(asccManifestRecord.getToAsccpManifestId().toBigInteger()));

            boolean exists = dslContext().selectCount()
                    .from(ASCC_MANIFEST)
                    .where(and(
                            ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(acc.accManifestId())),
                            ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(valueOf(toAsccp.nextAsccpManifestId()))
                    ))
                    .fetchOptionalInto(Integer.class).orElse(0) > 0;
            if (exists) {
                continue;
            }

            AsccManifestRecord newAsccManifestRecord = new AsccManifestRecord();
            newAsccManifestRecord.setAsccId(asccManifestRecord.getAsccId());
            newAsccManifestRecord.setReleaseId(valueOf(acc.release().releaseId()));
            newAsccManifestRecord.setFromAccManifestId(valueOf(acc.accManifestId()));
            newAsccManifestRecord.setToAsccpManifestId(valueOf(toAsccp.nextAsccpManifestId()));
            String toAsccpDen = toAsccp.den();
            newAsccManifestRecord.setDen(acc.objectClassTerm() + ". " + toAsccpDen);
            newAsccManifestRecord.setPrevAsccManifestId(asccManifestRecord.getAsccManifestId());
            dslContext().insertInto(ASCC_MANIFEST).set(newAsccManifestRecord).execute();
        }

        for (BccManifestRecord bccManifestRecord : bccManifestRecords) {
            BccRecord bccRecord = dslContext().selectFrom(BCC)
                    .where(BCC.BCC_ID.eq(bccManifestRecord.getBccId())).fetchOne();

            if (bccRecord.getPrevBccId() == null) {
                // delete bcc and bcc manifest which added this revision
                bccManifestRecord.delete();
                bccRecord.delete();
            } else {
                // delete bcc and update bcc manifest
                BccRecord prevBccRecord = dslContext().selectFrom(BCC)
                        .where(BCC.BCC_ID.eq(bccRecord.getPrevBccId())).fetchOne();
                prevBccRecord.setNextBccId(null);
                prevBccRecord.update(BCC.NEXT_BCC_ID);
                bccManifestRecord.setBccId(prevBccRecord.getBccId());
                bccManifestRecord.setSeqKeyId(null);
                bccManifestRecord.update(BCC_MANIFEST.BCC_ID, BCC_MANIFEST.SEQ_KEY_ID);
                bccRecord.delete();
            }
        }

        var bccpQuery = repositoryFactory().bccpQueryRepository(requester());
        for (BccManifestRecord bccManifestRecord : nullNextPrevBccManifestRecords) {
            BccpSummaryRecord toBccp = bccpQuery.getBccpSummary(
                    new BccpManifestId(bccManifestRecord.getToBccpManifestId().toBigInteger()));

            boolean exists = dslContext().selectCount()
                    .from(BCC_MANIFEST)
                    .where(and(
                            BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(acc.accManifestId())),
                            BCC_MANIFEST.TO_BCCP_MANIFEST_ID.eq(valueOf(toBccp.nextBccpManifestId()))
                    ))
                    .fetchOptionalInto(Integer.class).orElse(0) > 0;
            if (exists) {
                continue;
            }

            BccManifestRecord newBccManifestRecord = new BccManifestRecord();
            newBccManifestRecord.setBccId(bccManifestRecord.getBccId());
            newBccManifestRecord.setReleaseId(valueOf(acc.release().releaseId()));
            newBccManifestRecord.setFromAccManifestId(valueOf(acc.accManifestId()));
            newBccManifestRecord.setToBccpManifestId(valueOf(toBccp.nextBccpManifestId()));
            String toBccpDen = toBccp.den();
            newBccManifestRecord.setDen(acc.objectClassTerm() + ". " + toBccpDen);
            newBccManifestRecord.setPrevBccManifestId(bccManifestRecord.getBccManifestId());
            dslContext().insertInto(BCC_MANIFEST).set(newBccManifestRecord).execute();
        }

        // update ACCs which using with based current ACC
        dslContext().update(ACC)
                .set(ACC.BASED_ACC_ID, valueOf(acc.prevAccId()))
                .where(ACC.BASED_ACC_ID.eq(valueOf(acc.accId())))
                .execute();

        // update ASCCPs which using with role of current ACC
        dslContext().update(ASCCP)
                .set(ASCCP.ROLE_OF_ACC_ID, valueOf(acc.prevAccId()))
                .where(ASCCP.ROLE_OF_ACC_ID.eq(valueOf(acc.accId())))
                .execute();

        insertSeqKey(acc.accManifestId(), acc.guid());
    }

    private static class Association {
        public final SeqKeyType type;
        public final ULong manifestId;
        private SeqKeyRecord seqKeyRecord;

        public Association(SeqKeyType type, ULong manifestId) {
            this.type = type;
            this.manifestId = manifestId;
        }

        public ULong getManifestId() {
            return manifestId;
        }

        public SeqKeyRecord getSeqKeyRecord() {
            return seqKeyRecord;
        }

        public void setSeqKeyRecord(SeqKeyRecord seqKeyRecord) {
            this.seqKeyRecord = seqKeyRecord;
        }
    }

    public void insertSeqKey(AccManifestId fromAccManifestId, Guid reference) {

        HashMap<String, Association> associationMap = new HashMap<>();
        dslContext().select(ASCC.GUID, ASCC_MANIFEST.ASCC_MANIFEST_ID)
                .from(ASCC_MANIFEST)
                .join(ASCC).on(ASCC_MANIFEST.ASCC_ID.eq(ASCC.ASCC_ID))
                .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(fromAccManifestId))).fetch()
                .forEach(e -> {
                    associationMap.put(e.get(ASCC.GUID),
                            new Association(SeqKeyType.ASCC, e.get(ASCC_MANIFEST.ASCC_MANIFEST_ID)));
                });

        dslContext().select(BCC.GUID, BCC_MANIFEST.BCC_MANIFEST_ID)
                .from(BCC_MANIFEST)
                .join(BCC).on(BCC_MANIFEST.BCC_ID.eq(BCC.BCC_ID))
                .where(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(fromAccManifestId))).fetch()
                .forEach(e -> {
                    associationMap.put(e.get(BCC.GUID),
                            new Association(SeqKeyType.BCC, e.get(BCC_MANIFEST.BCC_MANIFEST_ID)));
                });

        LogRecord log = dslContext().selectFrom(LOG)
                .where(and(
                        LOG.REFERENCE.eq(reference.value()),
                        LOG.LOG_ACTION.eq(LogAction.Revised.name())
                ))
                .orderBy(LOG.LOG_ID.desc())
                .limit(1).fetchOne();

        JsonObject snapshot = serializer.deserialize(log.getSnapshot().toString());
        JsonArray associations = snapshot.get("associations").getAsJsonArray();
        var seqKeyQuery = repositoryFactory().seqKeyQueryRepository(requester());
        var seqKeyCommand = repositoryFactory().seqKeyCommandRepository(requester());
        SeqKeySummaryRecord prev = null;
        for (JsonElement obj : associations) {
            String guid = obj.getAsJsonObject().get("guid").getAsString();
            Association association = associationMap.get(guid);
            if (association == null) {
                return;
            }
            SeqKeyId seqKeyId;
            if (association.type == SeqKeyType.ASCC) {
                seqKeyId = seqKeyCommand.create(fromAccManifestId,
                        new AsccManifestId(association.getManifestId().toBigInteger()));
            } else {
                seqKeyId = seqKeyCommand.create(fromAccManifestId,
                        new BccManifestId(association.getManifestId().toBigInteger()));
            }

            SeqKeySummaryRecord current = seqKeyQuery.getSeqKeySummary(seqKeyId);
            if (prev != null) {
                seqKeyCommand.moveAfter(current, prev);
            }
            prev = current;
        }
    }

    @Override
    public boolean updateOwnership(ScoreUser targetUser, AccManifestId accManifestId) {

        if (targetUser == null) {
            throw new IllegalArgumentException("`targetUser` must not be null.");
        }

        if (accManifestId == null) {
            throw new IllegalArgumentException("`accManifestId` must not be null.");
        }

        var query = repositoryFactory().accQueryRepository(requester());

        AccSummaryRecord acc = query.getAccSummary(accManifestId);
        if (acc == null) {
            throw new IllegalArgumentException("ACC not found.");
        }

        int numOfUpdatedRecords = dslContext().update(ACC)
                .set(ACC.OWNER_USER_ID, valueOf(targetUser.userId()))
                .set(ACC.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(ACC.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                .where(ACC.ACC_ID.eq(valueOf(acc.accId())))
                .execute();
        if (numOfUpdatedRecords < 1) {
            return false;
        }

        for (AsccSummaryRecord ascc : query.getAsccSummaryList(accManifestId)) {
            dslContext().update(ASCC)
                    .set(ASCC.OWNER_USER_ID, valueOf(targetUser.userId()))
                    .set(ASCC.LAST_UPDATED_BY, valueOf(requester().userId()))
                    .set(ASCC.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                    .where(ASCC.ASCC_ID.eq(valueOf(ascc.asccId())))
                    .execute();
        }

        for (BccSummaryRecord bcc : query.getBccSummaryList(accManifestId)) {
            dslContext().update(BCC)
                    .set(BCC.OWNER_USER_ID, valueOf(targetUser.userId()))
                    .set(BCC.LAST_UPDATED_BY, valueOf(requester().userId()))
                    .set(BCC.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                    .where(BCC.BCC_ID.eq(valueOf(bcc.bccId())))
                    .execute();
        }

        return numOfUpdatedRecords == 1;
    }

}
