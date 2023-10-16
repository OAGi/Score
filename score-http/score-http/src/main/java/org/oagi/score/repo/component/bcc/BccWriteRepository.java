package org.oagi.score.repo.component.bcc;

import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.service.corecomponent.seqkey.MoveTo;
import org.oagi.score.service.corecomponent.seqkey.SeqKeyHandler;
import org.oagi.score.service.log.LogRepository;
import org.oagi.score.service.log.model.LogAction;
import org.oagi.score.service.log.model.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
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
import static org.oagi.score.repo.api.impl.jooq.entity.tables.AsccManifest.ASCC_MANIFEST;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.Bcc.BCC;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.BccManifest.BCC_MANIFEST;
import static org.oagi.score.service.common.data.BCCEntityType.Attribute;
import static org.oagi.score.service.common.data.BCCEntityType.Element;
import static org.oagi.score.service.corecomponent.seqkey.MoveTo.LAST;
import static org.oagi.score.service.corecomponent.seqkey.MoveTo.LAST_OF_ATTR;

@Repository
public class BccWriteRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private ScoreRepositoryFactory scoreRepositoryFactory;

    private void ensureNoConflictInForward(ULong fromAccManifestId,
                                           BccpManifestRecord bccpManifestRecord, BccpRecord bccpRecord,
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
                        .from(BCC_MANIFEST)
                        .join(BCCP_MANIFEST).on(BCC_MANIFEST.TO_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.BCCP_MANIFEST_ID))
                        .where(and(
                                BCC_MANIFEST.FROM_ACC_MANIFEST_ID.in(groupAccManifestIdAncestryChart),
                                BCCP_MANIFEST.BCCP_ID.eq(bccpRecord.getBccpId())
                        ))
                        .fetchOneInto(Integer.class);
                if (cnt > 0) {
                    denPathList.add(groupAccRecord.get(ACC_MANIFEST.DEN));
                    throw new IllegalArgumentException("ACC [" + String.join(" > ", denPathList) + "] already has BCCP [" + bccpManifestRecord.getDen() + "]");
                }
            }
        }

        // Check conflicts in forward
        int cnt = dslContext.selectCount()
                .from(BCC_MANIFEST)
                .join(BCCP_MANIFEST).on(BCC_MANIFEST.TO_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.BCCP_MANIFEST_ID))
                .where(and(
                        BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(fromAccManifestId),
                        BCCP_MANIFEST.BCCP_ID.eq(bccpRecord.getBccpId())
                ))
                .fetchOneInto(Integer.class);
        if (cnt > 0) {
            throw new IllegalArgumentException("ACC [" + String.join(" > ", denPathList) + "] already has BCCP [" + bccpManifestRecord.getDen() + "]");
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
            ensureNoConflictInForward(basedAccRecord.get(ACC_MANIFEST.BASED_ACC_MANIFEST_ID), bccpManifestRecord, bccpRecord, childDenPathList);
        }
    }

    private void ensureNoConflictInBackward(ULong fromAccManifestId,
                                            BccpManifestRecord bccpManifestRecord, BccpRecord bccpRecord,
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
                        .from(ASCC_MANIFEST)
                        .join(ASCCP_MANIFEST).on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                        .join(ACC_MANIFEST).on(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                        .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                        .where(and(
                                ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(childAccManifestId),
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
                            .from(BCC_MANIFEST)
                            .join(BCCP_MANIFEST).on(BCC_MANIFEST.TO_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.BCCP_MANIFEST_ID))
                            .where(and(
                                    BCC_MANIFEST.FROM_ACC_MANIFEST_ID.in(groupAccManifestIdAncestryChart),
                                    BCCP_MANIFEST.BCCP_ID.eq(bccpRecord.getBccpId())
                            ))
                            .fetchOneInto(Integer.class);
                    if (cnt > 0) {
                        childDenPathList.add(groupAccRecord.get(ACC_MANIFEST.DEN));
                        throw new IllegalArgumentException("ACC [" + String.join(" < ", childDenPathList) + "] already has BCCP [" + bccpManifestRecord.getDen() + "]");
                    }
                }
            }

            int cnt = dslContext.selectCount()
                    .from(BCC_MANIFEST)
                    .join(BCCP_MANIFEST).on(BCC_MANIFEST.TO_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.BCCP_MANIFEST_ID))
                    .where(and(
                            BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(childAccManifestId),
                            BCCP_MANIFEST.BCCP_ID.eq(bccpRecord.getBccpId())
                    ))
                    .fetchOneInto(Integer.class);
            if (cnt > 0) {
                throw new IllegalArgumentException("ACC [" + String.join(" < ", childDenPathList) + "] already has BCCP [" + bccpManifestRecord.getDen() + "]");
            }

            ensureNoConflictInBackward(childAccManifestId, bccpManifestRecord, bccpRecord, childDenPathList);
        }
    }

    public CreateBccRepositoryResponse createBcc(CreateBccRepositoryRequest request) {
        ULong userId = ULong.valueOf(sessionService.userId(request.getUser()));
        LocalDateTime timestamp = request.getLocalDateTime();

        AccManifestRecord accManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(request.getAccManifestId())))
                .fetchOne();

        if (accManifestRecord == null) {
            throw new IllegalArgumentException("Source ACC does not exist.");
        }

        BccpManifestRecord bccpManifestRecord = dslContext.selectFrom(BCCP_MANIFEST)
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(ULong.valueOf(request.getBccpManifestId())))
                .fetchOne();

        BccpRecord bccpRecord = dslContext.selectFrom(BCCP)
                .where(BCCP.BCCP_ID.eq(bccpManifestRecord.getBccpId())).fetchOne();

        if (bccpManifestRecord == null) {
            throw new IllegalArgumentException("Target BCCP does not exist.");
        }

        AccRecord accRecord = dslContext.selectFrom(ACC)
                .where(ACC.ACC_ID.eq(accManifestRecord.getAccId())).fetchOne();

        // Issue #1192
        List<String> denPathList = new ArrayList<>();
        denPathList.add(accManifestRecord.getDen());

        ensureNoConflictInForward(accManifestRecord.getAccManifestId(), bccpManifestRecord, bccpRecord, denPathList);
        ensureNoConflictInBackward(accManifestRecord.getAccManifestId(), bccpManifestRecord, bccpRecord, denPathList);

        CcState accState = CcState.valueOf(accRecord.getState());
        if (accState != request.getInitialState()) {
            throw new IllegalArgumentException("The initial state of BCC must be '" + accState + "'.");
        }

        BccRecord bcc = new BccRecord();
        bcc.setGuid(ScoreGuid.randomGuid());
        bcc.setCardinalityMin(0);
        bcc.setCardinalityMax(-1);
        bcc.setSeqKey(0); // @deprecated
        bcc.setFromAccId(accRecord.getAccId());
        bcc.setToBccpId(bccpRecord.getBccpId());
        bcc.setEntityType(Element.getValue());
        bcc.setState(CcState.WIP.name());
        bcc.setIsDeprecated((byte) 0);
        bcc.setIsNillable((byte) 0);
        bcc.setCreatedBy(userId);
        bcc.setLastUpdatedBy(userId);
        bcc.setOwnerUserId(userId);
        bcc.setCreationTimestamp(timestamp);
        bcc.setLastUpdateTimestamp(timestamp);
        bcc.setBccId(
                dslContext.insertInto(BCC)
                        .set(bcc)
                        .returning(BCC.BCC_ID).fetchOne().getBccId()
        );

        BccManifestRecord bccManifestRecord = new BccManifestRecord();
        bccManifestRecord.setBccId(bcc.getBccId());
        bccManifestRecord.setReleaseId(ULong.valueOf(request.getReleaseId()));
        bccManifestRecord.setFromAccManifestId(accManifestRecord.getAccManifestId());
        bccManifestRecord.setToBccpManifestId(bccpManifestRecord.getBccpManifestId());
        bccManifestRecord.setDen(accRecord.getObjectClassTerm() + ". " + bccpManifestRecord.getDen());
        bccManifestRecord.setBccManifestId(
                dslContext.insertInto(BCC_MANIFEST)
                        .set(bccManifestRecord)
                        .returning(BCC_MANIFEST.BCC_MANIFEST_ID).fetchOne().getBccManifestId()
        );

        seqKeyHandler(request.getUser(), bccManifestRecord).moveTo(request.getPos());

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



        return new CreateBccRepositoryResponse(bccManifestRecord.getBccManifestId().toBigInteger());
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
                                                            ULong userId,
                                                            LocalDateTime timestamp,
                                                            String hash, LogAction action) {

        if (action.equals(LogAction.IGNORE)) {
            return;
        }
        LogRecord logRecord =
                logRepository.insertAccLog(accManifestRecord,
                        accRecord, accManifestRecord.getLogId(),
                        action, userId, timestamp, hash);

        accManifestRecord.setLogId(logRecord.getLogId());
        accManifestRecord.update(ACC_MANIFEST.LOG_ID);
    }

    public UpdateBccPropertiesRepositoryResponse updateBccProperties(UpdateBccPropertiesRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        BccManifestRecord bccManifestRecord = dslContext.selectFrom(BCC_MANIFEST)
                .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(
                        ULong.valueOf(request.getBccManifestId())
                ))
                .fetchOne();

        BccRecord bccRecord = dslContext.selectFrom(BCC)
                .where(BCC.BCC_ID.eq(bccManifestRecord.getBccId()))
                .fetchOne();

        AccManifestRecord accManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(bccManifestRecord.getFromAccManifestId()))
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

        // update bcc record.
        UpdateSetFirstStep<BccRecord> firstStep = dslContext.update(BCC);
        UpdateSetMoreStep<BccRecord> moreStep = null;

        String den = accRecord.getObjectClassTerm() + ". " + dslContext.select(BCCP_MANIFEST.DEN)
                .from(BCCP)
                .join(BCCP_MANIFEST).on(BCCP.BCCP_ID.eq(BCCP_MANIFEST.BCCP_ID))
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(bccManifestRecord.getToBccpManifestId()))
                .fetchOneInto(String.class);
        boolean denChanged = compare(bccManifestRecord.getDen(), den) != 0;

        if (compare(bccRecord.getDefinition(), request.getDefinition()) != 0) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(BCC.DEFINITION, request.getDefinition());
        }
        if (compare(bccRecord.getDefinitionSource(), request.getDefinitionSource()) != 0) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(BCC.DEFINITION_SOURCE, request.getDefinitionSource());
        }
        if (request.getEntityType() != null) {
            if (request.getEntityType().getValue() != bccRecord.getEntityType()) {
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .set(BCC.ENTITY_TYPE, request.getEntityType().getValue());

                if (request.getEntityType() == Element) {
                    seqKeyHandler(request.getUser(), bccManifestRecord).moveTo(LAST);
                } else if (request.getEntityType() == Attribute) {
                    seqKeyHandler(request.getUser(), bccManifestRecord).moveTo(LAST_OF_ATTR);
                    // Issue #919
                    if (request.getCardinalityMin() < 0 || request.getCardinalityMin() > 1) {
                        request.setCardinalityMin(0);
                    }
                    if (request.getCardinalityMax() < 0 || request.getCardinalityMax() > 1) {
                        request.setCardinalityMax(1);
                    }
                }
            }
        }
        if ((bccRecord.getIsDeprecated() == 1) != request.isDeprecated()) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(BCC.IS_DEPRECATED, (byte) ((request.isDeprecated()) ? 1 : 0));
        }
        if ((bccRecord.getIsNillable() == 1) != request.isNillable()) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(BCC.IS_NILLABLE, (byte) ((request.isNillable()) ? 1 : 0));
        }
        if (request.getCardinalityMin() != null && bccRecord.getCardinalityMin() != request.getCardinalityMin()) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(BCC.CARDINALITY_MIN, request.getCardinalityMin());
        }
        if (request.getCardinalityMax() != null && bccRecord.getCardinalityMax() != request.getCardinalityMax()) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(BCC.CARDINALITY_MAX, request.getCardinalityMax());
        }
        if (compare(bccRecord.getDefaultValue(), request.getDefaultValue()) != 0) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(BCC.DEFAULT_VALUE, request.getDefaultValue());
        }
        if (compare(bccRecord.getFixedValue(), request.getFixedValue()) != 0) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(BCC.FIXED_VALUE, request.getFixedValue());
        }

        if (moreStep != null || denChanged) {
            if (denChanged) {
                dslContext.update(BCC_MANIFEST)
                        .set(BCC_MANIFEST.DEN, den)
                        .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(bccManifestRecord.getBccManifestId()))
                        .execute();
            }

            moreStep.set(BCC.LAST_UPDATED_BY, userId)
                    .set(BCC.LAST_UPDATE_TIMESTAMP, timestamp)
                    .where(BCC.BCC_ID.eq(bccRecord.getBccId()))
                    .execute();

            bccRecord = dslContext.selectFrom(BCC)
                    .where(BCC.BCC_ID.eq(bccManifestRecord.getBccId()))
                    .fetchOne();

            upsertLogIntoAccAndAssociations(
                    accRecord, accManifestRecord,
                    accManifestRecord.getReleaseId(),
                    userId, timestamp
            );
        }

        return new UpdateBccPropertiesRepositoryResponse(bccManifestRecord.getBccManifestId().toBigInteger());
    }

    public DeleteBccRepositoryResponse deleteBcc(DeleteBccRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        BccManifestRecord bccManifestRecord = dslContext.selectFrom(BCC_MANIFEST)
                .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(
                        ULong.valueOf(request.getBccManifestId())
                ))
                .fetchOne();

        BccRecord bccRecord = dslContext.selectFrom(BCC)
                .where(BCC.BCC_ID.eq(bccManifestRecord.getBccId()))
                .fetchOne();

        AccManifestRecord accManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(bccManifestRecord.getFromAccManifestId())).fetchOne();

        AccRecord accRecord = dslContext.selectFrom(ACC)
                .where(ACC.ACC_ID.eq(accManifestRecord.getAccId())).fetchOne();

        if (!CcState.WIP.equals(CcState.valueOf(accRecord.getState()))) {
            throw new IllegalArgumentException("Only the core component in 'WIP' state can be deleted.");
        }

        if (!accRecord.getOwnerUserId().equals(userId)) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        int usedBieCount = dslContext.selectCount().from(BBIE)
                .where(BBIE.BASED_BCC_MANIFEST_ID.eq(bccManifestRecord.getBccManifestId())).fetchOne(0, int.class);

        if (usedBieCount > 0) {
            throw new IllegalArgumentException("This association used in " + usedBieCount + " BIE(s). Can not be deleted.");
        }

        // delete from Tables
        seqKeyHandler(request.getUser(), bccManifestRecord).deleteCurrent();
        dslContext.update(BCC_MANIFEST).setNull(BCC_MANIFEST.NEXT_BCC_MANIFEST_ID)
                .where(BCC_MANIFEST.NEXT_BCC_MANIFEST_ID.eq(bccManifestRecord.getBccManifestId())).execute();
        dslContext.update(BCC_MANIFEST).setNull(BCC_MANIFEST.PREV_BCC_MANIFEST_ID)
                .where(BCC_MANIFEST.PREV_BCC_MANIFEST_ID.eq(bccManifestRecord.getBccManifestId())).execute();
        bccManifestRecord.delete();

        if (dslContext.selectCount().from(BCC_MANIFEST)
                .where(BCC_MANIFEST.BCC_ID.eq(bccManifestRecord.getBccId())).fetchOne(0, int.class) == 0) {
            dslContext.update(BCC).setNull(BCC.NEXT_BCC_ID)
                    .where(BCC.NEXT_BCC_ID.eq(bccRecord.getBccId())).execute();
            dslContext.update(BCC).setNull(BCC.PREV_BCC_ID)
                    .where(BCC.PREV_BCC_ID.eq(bccRecord.getBccId())).execute();
            bccRecord.delete();
        }

        upsertLogIntoAccAndAssociations(
                accRecord, accManifestRecord,
                accManifestRecord.getReleaseId(),
                userId, timestamp
        );

        return new DeleteBccRepositoryResponse(bccManifestRecord.getBccManifestId().toBigInteger());
    }

    private SeqKeyHandler seqKeyHandler(AuthenticatedPrincipal user, BccManifestRecord bccManifestRecord) {
        SeqKeyHandler seqKeyHandler = new SeqKeyHandler(scoreRepositoryFactory,
                sessionService.asScoreUser(user));
        seqKeyHandler.initBcc(
                bccManifestRecord.getFromAccManifestId().toBigInteger(),
                (bccManifestRecord.getSeqKeyId() != null) ? bccManifestRecord.getSeqKeyId().toBigInteger() : null,
                bccManifestRecord.getBccManifestId().toBigInteger());
        return seqKeyHandler;
    }

    public RefactorBccRepositoryResponse refactor(RefactorBccRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        BccManifestRecord targetBccManifestRecord = dslContext.selectFrom(BCC_MANIFEST)
                .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(
                        ULong.valueOf(request.getBccManifestId())
                ))
                .fetchOne();

        AccManifestRecord targetAccManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(request.getAccManifestId())))
                .fetchOne();

        BccRecord targetBccRecord = dslContext.selectFrom(BCC).where(BCC.BCC_ID.eq(targetBccManifestRecord.getBccId())).fetchOne();

        String bccpDen = dslContext.select(BCCP_MANIFEST.DEN)
                .from(BCCP_MANIFEST)
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(targetBccManifestRecord.getToBccpManifestId()))
                .fetchOneInto(String.class);

        AccRecord targetAccRecord = dslContext.selectFrom(ACC)
                .where(ACC.ACC_ID.eq(targetAccManifestRecord.getAccId()))
                .fetchOne();

        List<BccManifestRecord> targetBccManifestList = this.getRefactorTargetBccManifestList(targetBccManifestRecord, targetAccManifestRecord.getAccManifestId());

        String hash = LogUtils.generateHash();

        for (BccManifestRecord bccManifestRecord : targetBccManifestList) {

            BccRecord bccRecord = dslContext.selectFrom(BCC)
                    .where(BCC.BCC_ID.eq(bccManifestRecord.getBccId()))
                    .fetchOne();

            AccManifestRecord prevAccManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                    .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(bccManifestRecord.getFromAccManifestId()))
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

            int usedBieCount = dslContext.selectCount().from(BBIE)
                    .where(BBIE.BASED_BCC_MANIFEST_ID.eq(bccManifestRecord.getBccManifestId())).fetchOne(0, int.class);

            if (usedBieCount > 0) {
                throw new IllegalArgumentException("This association used in " + usedBieCount + " BIE(s). Can not be refactored.");
            }

            seqKeyHandler(request.getUser(), bccManifestRecord).deleteCurrent();
            dslContext.update(BCC_MANIFEST)
                    .setNull(BCC_MANIFEST.NEXT_BCC_MANIFEST_ID)
                    .where(BCC_MANIFEST.NEXT_BCC_MANIFEST_ID.eq(bccManifestRecord.getBccManifestId()))
                    .execute();
            dslContext.update(BCC_MANIFEST)
                    .setNull(BCC_MANIFEST.PREV_BCC_MANIFEST_ID)
                    .where(BCC_MANIFEST.PREV_BCC_MANIFEST_ID.eq(bccManifestRecord.getBccManifestId()))
                    .execute();
            bccManifestRecord.delete();

            dslContext.update(BCC)
                    .setNull(BCC.NEXT_BCC_ID)
                    .where(BCC.NEXT_BCC_ID.eq(bccRecord.getBccId()))
                    .execute();
            dslContext.update(BCC)
                    .setNull(BCC.PREV_BCC_ID)
                    .where(BCC.PREV_BCC_ID.eq(bccRecord.getBccId()))
                    .execute();
            bccRecord.delete();

            upsertLogIntoAccAndAssociationsByAction(
                    prevAccRecord, prevAccManifestRecord,
                    accManifestRecord.getReleaseId(),
                    userId, timestamp, hash, LogAction.Refactored);
        }

        targetBccRecord.setBccId(null);
        targetBccRecord.setLastUpdatedBy(userId);
        targetBccRecord.setLastUpdateTimestamp(timestamp);
        targetBccRecord.setFromAccId(targetAccRecord.getAccId());
        targetBccRecord.setPrevBccId(null);
        targetBccRecord.setNextBccId(null);
        ULong bccId = dslContext.insertInto(BCC).set(targetBccRecord).returning().fetchOne().getBccId();

        targetBccManifestRecord.setBccManifestId(null);
        targetBccManifestRecord.setFromAccManifestId(ULong.valueOf(request.getAccManifestId()));
        targetBccManifestRecord.setBccId(bccId);
        targetBccManifestRecord.setDen(targetAccRecord.getObjectClassTerm() + ". " + bccpDen);
        targetBccManifestRecord.setSeqKeyId(null);
        targetBccManifestRecord.setPrevBccManifestId(null);
        targetBccManifestRecord.setNextBccManifestId(null);
        targetBccManifestRecord.setBccManifestId(
                dslContext.insertInto(BCC_MANIFEST).set(targetBccManifestRecord).returning().fetchOne().getBccManifestId());

        seqKeyHandler(request.getUser(), targetBccManifestRecord).moveTo(MoveTo.LAST);

        upsertLogIntoAccAndAssociationsByAction(
                targetAccRecord, targetAccManifestRecord,
                targetAccManifestRecord.getReleaseId(),
                userId, timestamp, hash, LogAction.Refactored);

        return new RefactorBccRepositoryResponse(targetBccManifestRecord.getBccManifestId().toBigInteger());
    }

    private List<BccManifestRecord> getRefactorTargetBccManifestList(BccManifestRecord bccManifestRecord, ULong targetAccManifestId) {
        ULong releaseId = bccManifestRecord.getReleaseId();
        List<AccManifestRecord> accManifestList = dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.RELEASE_ID.eq(releaseId)).fetch();
        Map<ULong, List<AccManifestRecord>> baseAccMap = accManifestList.stream().filter(e -> e.getBasedAccManifestId() != null)
                .collect(Collectors.groupingBy(AccManifestRecord::getBasedAccManifestId));

        List<BccManifestRecord> bccList = dslContext.selectFrom(BCC_MANIFEST)
                .where(BCC_MANIFEST.RELEASE_ID.eq(releaseId)).fetch();
        Map<ULong, List<BccManifestRecord>> fromAccBccMap = bccList.stream()
                .collect(Collectors.groupingBy(BccManifestRecord::getFromAccManifestId));

        List<ULong> accManifestIdList = new ArrayList<>();

        accManifestIdList.add(targetAccManifestId);

        Set<ULong> accCandidates = new HashSet<>();

        for (ULong cur : accManifestIdList) {
            accCandidates.addAll(getBaseAccManifestId(cur, baseAccMap));
        }

        Set<BccManifestRecord> bccResult = new HashSet<>();

        for (ULong acc : accCandidates) {
            bccResult.addAll(
                    fromAccBccMap.getOrDefault(acc, Collections.emptyList())
                            .stream()
                            .filter(bcc -> bcc.getToBccpManifestId().equals(bccManifestRecord.getToBccpManifestId()))
                            .collect(Collectors.toList()));
        }
        return new ArrayList<>(bccResult);
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