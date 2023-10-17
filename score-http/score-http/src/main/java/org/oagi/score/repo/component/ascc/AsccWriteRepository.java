package org.oagi.score.repo.component.ascc;

import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.cc_management.data.CcASCCPType;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.service.corecomponent.seqkey.MoveTo;
import org.oagi.score.service.corecomponent.seqkey.SeqKeyHandler;
import org.oagi.score.service.log.LogRepository;
import org.oagi.score.service.log.model.LogAction;
import org.oagi.score.service.log.model.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.compare;
import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.corecomponent.model.OagisComponentType.SemanticGroup;
import static org.oagi.score.repo.api.corecomponent.model.OagisComponentType.UserExtensionGroup;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.Acc.ACC;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.AccManifest.ACC_MANIFEST;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.Ascc.ASCC;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.AsccManifest.ASCC_MANIFEST;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.BccManifest.BCC_MANIFEST;

@Repository
public class AsccWriteRepository {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private ScoreRepositoryFactory scoreRepositoryFactory;

    private void ensureNoConflictInForward(ULong fromAccManifestId,
                                           AsccpManifestRecord asccpManifestRecord, AsccpRecord asccpRecord,
                                           List<String> denPathList) {
        if (fromAccManifestId == null) {
            return;
        }

        // Issue #1463
        // Find conflicts under 'Group' ACCs.
        {
            List<Record2<ULong, String>> groupAccRecords = dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID, ACC_MANIFEST.DEN)
                    .from(ASCC_MANIFEST)
                    .join(ASCCP_MANIFEST).on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                    .join(ACC_MANIFEST).on(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                    .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                    .where(and(
                            ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(fromAccManifestId),
                            ACC.OAGIS_COMPONENT_TYPE.in(Arrays.asList(SemanticGroup.getValue(), UserExtensionGroup.getValue()))
                    ))
                    .fetch();

            for (Record2<ULong, String> groupAccRecord : groupAccRecords) {
                List<ULong> groupAccManifestIdAncestryChart = new ArrayList<>();
                ULong groupAccManifestId = groupAccRecord.get(ACC_MANIFEST.ACC_MANIFEST_ID);
                groupAccManifestIdAncestryChart.add(groupAccManifestId);

                while (groupAccManifestId != null) {
                    groupAccManifestId = dslContext.select(ACC_MANIFEST.BASED_ACC_MANIFEST_ID)
                            .from(ACC_MANIFEST)
                            .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(groupAccManifestId))
                            .fetchOptionalInto(ULong.class).orElse(null);
                    if (groupAccManifestId != null) {
                        groupAccManifestIdAncestryChart.add(groupAccManifestId);
                    }
                }

                int cnt = dslContext.selectCount()
                        .from(ASCC_MANIFEST)
                        .join(ASCCP_MANIFEST).on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                        .where(and(
                                ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.in(groupAccManifestIdAncestryChart),
                                ASCCP_MANIFEST.ASCCP_ID.eq(asccpRecord.getAsccpId())
                        ))
                        .fetchOneInto(Integer.class);
                if (cnt > 0) {
                    denPathList.add(groupAccRecord.get(ACC_MANIFEST.DEN));
                    throw new IllegalArgumentException("ACC [" + String.join(" > ", denPathList) + "] already has ASCCP [" + asccpManifestRecord.getDen() + "]");
                }
            }
        }

        // Check conflicts in forward
        int cnt = dslContext.selectCount()
                .from(ASCC_MANIFEST)
                .join(ASCCP_MANIFEST).on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .where(and(
                        ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(fromAccManifestId),
                        ASCCP_MANIFEST.ASCCP_ID.eq(asccpRecord.getAsccpId())
                ))
                .fetchOneInto(Integer.class);
        if (cnt > 0) {
            throw new IllegalArgumentException("ACC [" + String.join(" > ", denPathList) + "] already has ASCCP [" + asccpManifestRecord.getDen() + "]");
        }

        Record2<ULong, String> basedAccRecord = dslContext.select(ACC_MANIFEST.BASED_ACC_MANIFEST_ID, ACC_MANIFEST.DEN)
                .from(ACC_MANIFEST)
                .join(ACC_MANIFEST.as("base")).on(ACC_MANIFEST.BASED_ACC_MANIFEST_ID.eq(ACC_MANIFEST.as("base").ACC_MANIFEST_ID))
                .join(ACC).on(ACC_MANIFEST.as("base").ACC_ID.eq(ACC.ACC_ID))
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(fromAccManifestId))
                .fetchOne();

        if (basedAccRecord != null) {
            List<String> childDenPathList = new ArrayList<>(denPathList);
            childDenPathList.add(basedAccRecord.get(ACC_MANIFEST.DEN));
            ensureNoConflictInForward(basedAccRecord.get(ACC_MANIFEST.BASED_ACC_MANIFEST_ID), asccpManifestRecord, asccpRecord, childDenPathList);
        }
    }

    private void ensureNoConflictInBackward(ULong fromAccManifestId,
                                            AsccpManifestRecord asccpManifestRecord, AsccpRecord asccpRecord,
                                            List<String> denPathList) {
        List<Record2<ULong, String>> childAccRecords = dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID, ACC_MANIFEST.DEN)
                .from(ACC_MANIFEST)
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .where(ACC_MANIFEST.BASED_ACC_MANIFEST_ID.eq(fromAccManifestId))
                .fetch();
        if (childAccRecords.isEmpty()) {
            return;
        }

        for (Record2<ULong, String> childAccRecord : childAccRecords) {
            ULong childAccManifestId = childAccRecord.get(ACC_MANIFEST.ACC_MANIFEST_ID);
            List<String> childDenPathList = new ArrayList<>(denPathList);
            childDenPathList.add(childAccRecord.get(ACC_MANIFEST.DEN));

            // Issue #1463
            // Find conflicts under 'Group' ACCs.
            {
                List<Record2<ULong, String>> groupAccRecords = dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID, ACC_MANIFEST.DEN)
                        .from(Tables.ASCC_MANIFEST)
                        .join(ASCCP_MANIFEST).on(Tables.ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                        .join(ACC_MANIFEST).on(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                        .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                        .where(and(
                                Tables.ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(childAccManifestId),
                                ACC.OAGIS_COMPONENT_TYPE.in(Arrays.asList(SemanticGroup.getValue(), UserExtensionGroup.getValue()))
                        ))
                        .fetch();

                for (Record2<ULong, String> groupAccRecord : groupAccRecords) {
                    List<ULong> groupAccManifestIdAncestryChart = new ArrayList<>();
                    ULong groupAccManifestId = groupAccRecord.get(ACC_MANIFEST.ACC_MANIFEST_ID);
                    groupAccManifestIdAncestryChart.add(groupAccManifestId);

                    while (groupAccManifestId != null) {
                        groupAccManifestId = dslContext.select(ACC_MANIFEST.BASED_ACC_MANIFEST_ID)
                                .from(ACC_MANIFEST)
                                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(groupAccManifestId))
                                .fetchOptionalInto(ULong.class).orElse(null);
                        if (groupAccManifestId != null) {
                            groupAccManifestIdAncestryChart.add(groupAccManifestId);
                        }
                    }

                    int cnt = dslContext.selectCount()
                            .from(ASCC_MANIFEST)
                            .join(ASCCP_MANIFEST).on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                            .where(and(
                                    ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.in(groupAccManifestIdAncestryChart),
                                    ASCCP_MANIFEST.ASCCP_ID.eq(asccpRecord.getAsccpId())
                            ))
                            .fetchOneInto(Integer.class);
                    if (cnt > 0) {
                        childDenPathList.add(groupAccRecord.get(ACC_MANIFEST.DEN));
                        throw new IllegalArgumentException("ACC [" + String.join(" < ", childDenPathList) + "] already has ASCCP [" + asccpManifestRecord.getDen() + "]");
                    }
                }
            }

            int cnt = dslContext.selectCount()
                    .from(ASCC_MANIFEST)
                    .join(ASCCP_MANIFEST).on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                    .where(and(
                            ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(childAccManifestId),
                            ASCCP_MANIFEST.ASCCP_ID.eq(asccpRecord.getAsccpId())
                    ))
                    .fetchOneInto(Integer.class);
            if (cnt > 0) {
                throw new IllegalArgumentException("ACC [" + String.join(" < ", childDenPathList) + "] already has ASCCP [" + asccpManifestRecord.getDen() + "]");
            }

            ensureNoConflictInBackward(childAccManifestId, asccpManifestRecord, asccpRecord, childDenPathList);
        }
    }

    public CreateAsccRepositoryResponse createAscc(CreateAsccRepositoryRequest request) {
        ULong userId = ULong.valueOf(sessionService.userId(request.getUser()));
        LocalDateTime timestamp = request.getLocalDateTime();

        AccManifestRecord accManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                .where(and(
                        ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())),
                        ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(request.getAccManifestId()))
                ))
                .fetchOne();

        if (accManifestRecord == null) {
            throw new IllegalArgumentException("Source ACC does not exist.");
        }

        AsccpManifestRecord asccpManifestRecord = dslContext.selectFrom(ASCCP_MANIFEST)
                .where(and(
                        ASCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())),
                        ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(request.getAsccpManifestId()))
                ))
                .fetchOne();

        AsccpRecord asccpRecord = dslContext.selectFrom(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(asccpManifestRecord.getAsccpId())).fetchOne();

        if (asccpManifestRecord == null) {
            throw new IllegalArgumentException("Target ASCCP does not exist.");
        }

        AccRecord accRecord = dslContext.selectFrom(ACC)
                .where(ACC.ACC_ID.eq(accManifestRecord.getAccId())).fetchOne();

        // Issue #1192
        List<String> denPathList = new ArrayList<>();
        denPathList.add(accManifestRecord.getDen());

        ensureNoConflictInForward(accManifestRecord.getAccManifestId(), asccpManifestRecord, asccpRecord, denPathList);
        ensureNoConflictInBackward(accManifestRecord.getAccManifestId(), asccpManifestRecord, asccpRecord, denPathList);

        if (dslContext.selectCount()
                .from(ASCCP_MANIFEST)
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(ASCC_MANIFEST).on(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID))
                .where(and(ASCCP.REUSABLE_INDICATOR.eq((byte) 0),
                        ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(request.getAsccpManifestId())))
                )
                .fetchOneInto(Integer.class) > 0) {
            throw new IllegalArgumentException("Target ASCCP is not reusable.");
        }

        if (asccpRecord.getType().equals(CcASCCPType.Extension.name())) {
            if (dslContext.selectCount()
                    .from(ASCCP_MANIFEST)
                    .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                    .join(ASCC_MANIFEST).on(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID))
                    .where(and(ASCCP.TYPE.eq(CcASCCPType.Extension.name()),
                            ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(request.getAsccpManifestId())))
                    )
                    .fetchOneInto(Integer.class) > 0) {
                throw new IllegalArgumentException("This ACC already has Extension ASCCP.");
            }
        }

        AsccRecord ascc = new AsccRecord();
        ascc.setGuid(ScoreGuid.randomGuid());
        ascc.setCardinalityMin(request.getCardinalityMin());
        ascc.setCardinalityMax(request.getCardinalityMax());
        ascc.setSeqKey(0); // @deprecated
        ascc.setFromAccId(accRecord.getAccId());
        ascc.setToAsccpId(asccpRecord.getAsccpId());
        ascc.setState(request.getInitialState().name());
        ascc.setIsDeprecated((byte) 0);
        ascc.setCreatedBy(userId);
        ascc.setLastUpdatedBy(userId);
        ascc.setOwnerUserId(userId);
        ascc.setCreationTimestamp(timestamp);
        ascc.setLastUpdateTimestamp(timestamp);
        ascc.setAsccId(
                dslContext.insertInto(ASCC)
                        .set(ascc)
                        .returning(ASCC.ASCC_ID).fetchOne().getAsccId()
        );

        AsccManifestRecord asccManifestRecord = new AsccManifestRecord();
        asccManifestRecord.setAsccId(ascc.getAsccId());
        asccManifestRecord.setReleaseId(ULong.valueOf(request.getReleaseId()));
        asccManifestRecord.setFromAccManifestId(accManifestRecord.getAccManifestId());
        asccManifestRecord.setToAsccpManifestId(asccpManifestRecord.getAsccpManifestId());
        asccManifestRecord.setDen(accRecord.getObjectClassTerm() + ". " + asccpManifestRecord.getDen());
        asccManifestRecord.setAsccManifestId(
                dslContext.insertInto(ASCC_MANIFEST)
                        .set(asccManifestRecord)
                        .returning(ASCC_MANIFEST.ASCC_MANIFEST_ID).fetchOne().getAsccManifestId()
        );

        seqKeyHandler(request.getUser(), asccManifestRecord).moveTo(request.getPos());

        if (request.getLogAction() != null) {
            upsertLogIntoAccAndAssociationsByAction(
                    accRecord, accManifestRecord,
                    ULong.valueOf(request.getReleaseId()),
                    userId, timestamp, request.getLogHash(), request.getLogAction()
            );
        } else {
            upsertLogIntoAccAndAssociations(
                    accRecord, accManifestRecord,
                    ULong.valueOf(request.getReleaseId()),
                    userId, timestamp
            );
        }


        return new CreateAsccRepositoryResponse(asccManifestRecord.getAsccManifestId().toBigInteger());
    }

    private void upsertLogIntoAccAndAssociations(AccRecord accRecord,
                                                 AccManifestRecord accManifestRecord,
                                                 ULong releaseId,
                                                 ULong userId, LocalDateTime timestamp) {
        upsertLogIntoAccAndAssociationsByAction(accRecord, accManifestRecord, releaseId, userId, timestamp, LogUtils.generateHash(), LogAction.Modified);
    }

    private void upsertLogIntoAccAndAssociationsByAction(AccRecord accRecord,
                                                 AccManifestRecord accManifestRecord,
                                                 ULong releaseId,
                                                 ULong userId, LocalDateTime timestamp,
                                                 String hash, LogAction action) {
        LogRecord logRecord =
                logRepository.insertAccLog(accManifestRecord,
                        accRecord, accManifestRecord.getLogId(),
                        action, userId, timestamp, hash);

        accManifestRecord.setLogId(logRecord.getLogId());
        accManifestRecord.update(ACC_MANIFEST.LOG_ID);
    }

    public UpdateAsccPropertiesRepositoryResponse updateAsccProperties(UpdateAsccPropertiesRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        AsccManifestRecord asccManifestRecord = dslContext.selectFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(
                        ULong.valueOf(request.getAsccManifestId())
                ))
                .fetchOne();

        AsccRecord asccRecord = dslContext.selectFrom(ASCC)
                .where(ASCC.ASCC_ID.eq(asccManifestRecord.getAsccId()))
                .fetchOne();

        AccManifestRecord accManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(asccManifestRecord.getFromAccManifestId()))
                .fetchOne();

        AccRecord accRecord = dslContext.selectFrom(ACC)
                .where(ACC.ACC_ID.eq(accManifestRecord.getAccId()))
                .fetchOne();

        if (!request.isPropagation() && !CcState.WIP.equals(CcState.valueOf(accRecord.getState()))) {
            throw new IllegalArgumentException("Only the core component in 'WIP' state can be modified.");
        }

        if (!request.isPropagation() && !accRecord.getOwnerUserId().equals(userId)) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        // update ascc record.
        UpdateSetFirstStep<AsccRecord> firstStep = dslContext.update(ASCC);
        UpdateSetMoreStep<AsccRecord> moreStep = null;

        String den = accRecord.getObjectClassTerm() + ". " + dslContext.select(ASCCP_MANIFEST.DEN)
                .from(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(asccManifestRecord.getToAsccpManifestId()))
                .fetchOneInto(String.class);
        boolean denChanged = compare(asccManifestRecord.getDen(), den) != 0;

        if (compare(asccRecord.getDefinition(), request.getDefinition()) != 0) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ASCC.DEFINITION, request.getDefinition());
        }
        if (compare(asccRecord.getDefinition(), request.getDefinitionSource()) != 0) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ASCC.DEFINITION_SOURCE, request.getDefinitionSource());
        }
        if ((asccRecord.getIsDeprecated() == 1) != request.isDeprecated()) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ASCC.IS_DEPRECATED, (byte) ((request.isDeprecated()) ? 1 : 0));
        }
        if (request.getCardinalityMin() != null && asccRecord.getCardinalityMin() != request.getCardinalityMin()) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ASCC.CARDINALITY_MIN, request.getCardinalityMin());
        }
        if (request.getCardinalityMax() != null && asccRecord.getCardinalityMax() != request.getCardinalityMax()) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ASCC.CARDINALITY_MAX, request.getCardinalityMax());
        }

        if (moreStep != null || denChanged) {
            if (denChanged) {
                dslContext.update(ASCC_MANIFEST)
                        .set(ASCC_MANIFEST.DEN, den)
                        .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(asccManifestRecord.getAsccManifestId()))
                        .execute();
            }

            moreStep.set(ASCC.LAST_UPDATED_BY, userId)
                    .set(ASCC.LAST_UPDATE_TIMESTAMP, timestamp)
                    .where(ASCC.ASCC_ID.eq(asccRecord.getAsccId()))
                    .execute();

            asccRecord = dslContext.selectFrom(ASCC)
                    .where(ASCC.ASCC_ID.eq(asccManifestRecord.getAsccId()))
                    .fetchOne();

            upsertLogIntoAccAndAssociations(
                    accRecord, accManifestRecord,
                    accManifestRecord.getReleaseId(),
                    userId, timestamp
            );
        }

        return new UpdateAsccPropertiesRepositoryResponse(asccManifestRecord.getAsccManifestId().toBigInteger());
    }

    public DeleteAsccRepositoryResponse deleteAscc(DeleteAsccRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        AsccManifestRecord asccManifestRecord = dslContext.selectFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(
                        ULong.valueOf(request.getAsccManifestId())
                ))
                .fetchOne();

        AsccRecord asccRecord = dslContext.selectFrom(ASCC)
                .where(ASCC.ASCC_ID.eq(asccManifestRecord.getAsccId()))
                .fetchOne();

        AccManifestRecord accManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(asccManifestRecord.getFromAccManifestId()))
                .fetchOne();

        AccRecord accRecord = dslContext.selectFrom(ACC)
                .where(ACC.ACC_ID.eq(accManifestRecord.getAccId()))
                .fetchOne();

        if (!request.isIgnoreState()) {
            if (!CcState.WIP.equals(CcState.valueOf(accRecord.getState()))) {
                throw new IllegalArgumentException("Only the core component in 'WIP' state can be deleted.");
            }

            if (!accRecord.getOwnerUserId().equals(userId)) {
                throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
            }
        }

        int usedBieCount = dslContext.selectCount().from(ASBIE)
                .where(ASBIE.BASED_ASCC_MANIFEST_ID.eq(asccManifestRecord.getAsccManifestId())).fetchOne(0, int.class);

        if (usedBieCount > 0) {
            throw new IllegalArgumentException("This association used in " + usedBieCount + " BIE(s). Can not be deleted.");
        }

        // delete from Tables
        seqKeyHandler(request.getUser(), asccManifestRecord).deleteCurrent();
        dslContext.update(ASCC_MANIFEST).setNull(ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID)
                .where(ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID.eq(asccManifestRecord.getAsccManifestId())).execute();
        dslContext.update(ASCC_MANIFEST).setNull(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID)
                .where(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID.eq(asccManifestRecord.getAsccManifestId())).execute();
        asccManifestRecord.delete();
        if (dslContext.selectCount().from(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.ASCC_ID.eq(asccManifestRecord.getAsccId())).fetchOne(0, int.class) == 0) {
            dslContext.update(ASCC).setNull(ASCC.NEXT_ASCC_ID)
                    .where(ASCC.NEXT_ASCC_ID.eq(asccRecord.getAsccId())).execute();
            dslContext.update(ASCC).setNull(ASCC.PREV_ASCC_ID)
                    .where(ASCC.PREV_ASCC_ID.eq(asccRecord.getAsccId())).execute();
            asccRecord.delete();
        }

        if (request.getLogAction() != null) {
            upsertLogIntoAccAndAssociationsByAction(
                    accRecord, accManifestRecord,
                    accManifestRecord.getReleaseId(),
                    userId, timestamp, request.getLogHash(), request.getLogAction()
            );
        } else {
            upsertLogIntoAccAndAssociations(
                    accRecord, accManifestRecord,
                    accManifestRecord.getReleaseId(),
                    userId, timestamp
            );
        }

        return new DeleteAsccRepositoryResponse(asccManifestRecord.getAsccManifestId().toBigInteger());
    }

    private SeqKeyHandler seqKeyHandler(AuthenticatedPrincipal user, AsccManifestRecord asccManifestRecord) {
        SeqKeyHandler seqKeyHandler = new SeqKeyHandler(scoreRepositoryFactory,
                sessionService.asScoreUser(user));
        seqKeyHandler.initAscc(
                asccManifestRecord.getFromAccManifestId().toBigInteger(),
                (asccManifestRecord.getSeqKeyId() != null) ? asccManifestRecord.getSeqKeyId().toBigInteger() : null,
                asccManifestRecord.getAsccManifestId().toBigInteger());
        return seqKeyHandler;
    }

    public RefactorAsccRepositoryResponse refactor(RefactorAsccRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        AsccManifestRecord targetAsccManifestRecord = dslContext.selectFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(
                        ULong.valueOf(request.getAsccManifestId())
                ))
                .fetchOne();

        AccManifestRecord targetAccManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(request.getAccManifestId())))
                .fetchOne();

        AsccRecord targetAsccRecord = dslContext.selectFrom(ASCC).where(ASCC.ASCC_ID.eq(targetAsccManifestRecord.getAsccId())).fetchOne();

        String asccpDen = dslContext.select(ASCCP_MANIFEST.DEN)
                .from(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(targetAsccManifestRecord.getToAsccpManifestId()))
                .fetchOneInto(String.class);

        AccRecord targetAccRecord = dslContext.selectFrom(ACC)
                .where(ACC.ACC_ID.eq(targetAccManifestRecord.getAccId()))
                .fetchOne();

        List<AsccManifestRecord> targetAsccManifestList = this.getRefactorTargetAsccManifestList(targetAsccManifestRecord, targetAccManifestRecord.getAccManifestId());

        String hash = LogUtils.generateHash();

        for (AsccManifestRecord asccManifestRecord: targetAsccManifestList) {

            AsccRecord asccRecord = dslContext.selectFrom(ASCC)
                    .where(ASCC.ASCC_ID.eq(asccManifestRecord.getAsccId()))
                    .fetchOne();

            AccManifestRecord prevAccManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                    .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(asccManifestRecord.getFromAccManifestId()))
                    .fetchOne();

            AccRecord prevAccRecord = dslContext.selectFrom(ACC)
                    .where(ACC.ACC_ID.eq(prevAccManifestRecord.getAccId()))
                    .fetchOne();

            AccManifestRecord accManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                    .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(request.getAccManifestId())))
                    .fetchOne();

            AccRecord accRecord = dslContext.selectFrom(ACC)
                    .where(ACC.ACC_ID.eq(accManifestRecord.getAccId()))
                    .fetchOne();

            if (!CcState.WIP.equals(CcState.valueOf(accRecord.getState()))) {
                throw new IllegalArgumentException("Only the core component in 'WIP' state can be refactored.");
            }

            if (!accRecord.getOwnerUserId().equals(userId)) {
                throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
            }

            int usedBieCount = dslContext.selectCount().from(ASBIE)
                    .where(ASBIE.BASED_ASCC_MANIFEST_ID.eq(asccManifestRecord.getAsccManifestId())).fetchOne(0, int.class);

            if (usedBieCount > 0) {
                throw new IllegalArgumentException("This association used in " + usedBieCount + " BIE(s). Can not be refactored.");
            }

            seqKeyHandler(request.getUser(), asccManifestRecord).deleteCurrent();
            dslContext.update(ASCC_MANIFEST)
                    .setNull(ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID)
                    .where(ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID.eq(asccManifestRecord.getAsccManifestId()))
                    .execute();
            dslContext.update(ASCC_MANIFEST)
                    .setNull(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID)
                    .where(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID.eq(asccManifestRecord.getAsccManifestId()))
                    .execute();
            asccManifestRecord.delete();

            dslContext.update(ASCC)
                    .setNull(ASCC.NEXT_ASCC_ID)
                    .where(ASCC.NEXT_ASCC_ID.eq(asccRecord.getAsccId()))
                    .execute();
            dslContext.update(ASCC)
                    .setNull(ASCC.PREV_ASCC_ID)
                    .where(ASCC.PREV_ASCC_ID.eq(asccRecord.getAsccId()))
                    .execute();
            asccRecord.delete();

            upsertLogIntoAccAndAssociationsByAction(
                    prevAccRecord, prevAccManifestRecord,
                    accManifestRecord.getReleaseId(),
                    userId, timestamp, hash, LogAction.Refactored
            );
        }

        targetAsccRecord.setAsccId(null);
        targetAsccRecord.setLastUpdatedBy(userId);
        targetAsccRecord.setLastUpdateTimestamp(timestamp);
        targetAsccRecord.setFromAccId(targetAccRecord.getAccId());
        targetAsccRecord.setPrevAsccId(null);
        targetAsccRecord.setNextAsccId(null);
        ULong asccId = dslContext.insertInto(ASCC).set(targetAsccRecord).returning().fetchOne().getAsccId();

        targetAsccManifestRecord.setAsccManifestId(null);
        targetAsccManifestRecord.setFromAccManifestId(ULong.valueOf(request.getAccManifestId()));
        targetAsccManifestRecord.setAsccId(asccId);
        targetAsccManifestRecord.setDen(targetAccRecord.getObjectClassTerm() + ". " + asccpDen);
        targetAsccManifestRecord.setSeqKeyId(null);
        targetAsccManifestRecord.setPrevAsccManifestId(null);
        targetAsccManifestRecord.setNextAsccManifestId(null);
        targetAsccManifestRecord.setAsccManifestId(
                dslContext.insertInto(ASCC_MANIFEST).set(targetAsccManifestRecord).returning().fetchOne().getAsccManifestId());

        seqKeyHandler(request.getUser(), targetAsccManifestRecord).moveTo(MoveTo.LAST);

        upsertLogIntoAccAndAssociationsByAction(
                targetAccRecord, targetAccManifestRecord,
                targetAccManifestRecord.getReleaseId(),
                userId, timestamp, hash, LogAction.Ungrouped
        );

        return new RefactorAsccRepositoryResponse(targetAsccManifestRecord.getAsccManifestId().toBigInteger());
    }

    private List<AsccManifestRecord> getRefactorTargetAsccManifestList(AsccManifestRecord asccManifestRecord, ULong targetAccManifestId) {
        ULong releaseId = asccManifestRecord.getReleaseId();
        List<AccManifestRecord> accManifestList = dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.RELEASE_ID.eq(releaseId)).fetch();
        Map<ULong, List<AccManifestRecord>> baseAccMap = accManifestList.stream().filter(e -> e.getBasedAccManifestId() != null)
                .collect(Collectors.groupingBy(AccManifestRecord::getBasedAccManifestId));

        List<AsccManifestRecord> asccList = dslContext.selectFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.RELEASE_ID.eq(releaseId)).fetch();
        Map<ULong, List<AsccManifestRecord>> fromAccAsccMap = asccList.stream()
                .collect(Collectors.groupingBy(AsccManifestRecord::getFromAccManifestId));

        List<ULong> accManifestIdList = new ArrayList<>();

        accManifestIdList.add(targetAccManifestId);

        Set<ULong> accCandidates = new HashSet<>();

        for (ULong cur : accManifestIdList) {
            accCandidates.addAll(getBaseAccManifestId(cur, baseAccMap));
        }

        Set<AsccManifestRecord> asccResult = new HashSet<>();

        for (ULong acc : accCandidates) {
            asccResult.addAll(
                    fromAccAsccMap.getOrDefault(acc, Collections.emptyList())
                            .stream()
                            .filter(ascc -> ascc.getToAsccpManifestId().equals(asccManifestRecord.getToAsccpManifestId()))
                            .collect(Collectors.toList()));
        }
        return new ArrayList<>(asccResult);
    }

    private List<ULong> getBaseAccManifestId(ULong accManifestId, Map<ULong, List<AccManifestRecord>> baseAccMap) {
        List<ULong> result = new ArrayList<>();
        result.add(accManifestId);
        if (baseAccMap.containsKey(accManifestId)) {
            baseAccMap.get(accManifestId).forEach(e -> {
                result.addAll(getBaseAccManifestId(e.getAccManifestId(), baseAccMap));
            });
        }
        return result;
    }
}