package org.oagi.score.repo.component.ascc;

import org.jooq.DSLContext;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.cc_management.data.CcASCCPType;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.compare;
import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.Acc.ACC;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.AccManifest.ACC_MANIFEST;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.Ascc.ASCC;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.AsccManifest.ASCC_MANIFEST;

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

    private boolean accAlreadyContainAssociation(AccManifestRecord fromAccManifestRecord, String propertyTerm) {
        while (fromAccManifestRecord != null) {
            if (dslContext.selectCount()
                    .from(ASCC_MANIFEST)
                    .join(ASCCP_MANIFEST).on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                    .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                    .where(and(
                            ASCC_MANIFEST.RELEASE_ID.eq(fromAccManifestRecord.getReleaseId()),
                            ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(fromAccManifestRecord.getAccManifestId()),
                            ASCCP.PROPERTY_TERM.eq(propertyTerm)
                    ))
                    .fetchOneInto(Integer.class) > 0) {
                return true;
            }
            fromAccManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                    .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(fromAccManifestRecord.getBasedAccManifestId())).fetchOne();
        }
        return false;
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

        if (accAlreadyContainAssociation(accManifestRecord, asccpRecord.getPropertyTerm())) {
            throw new IllegalArgumentException("Target ASCCP has already included.");
        }

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

        AccRecord accRecord = dslContext.selectFrom(ACC)
                .where(ACC.ACC_ID.eq(accManifestRecord.getAccId())).fetchOne();

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
        ascc.setDen(accRecord.getObjectClassTerm() + ". " + asccpRecord.getDen());
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

        AsccManifestRecord asccManifest = new AsccManifestRecord();
        asccManifest.setAsccId(ascc.getAsccId());
        asccManifest.setReleaseId(ULong.valueOf(request.getReleaseId()));
        asccManifest.setFromAccManifestId(accManifestRecord.getAccManifestId());
        asccManifest.setToAsccpManifestId(asccpManifestRecord.getAsccpManifestId());
        asccManifest.setAsccManifestId(
                dslContext.insertInto(ASCC_MANIFEST)
                        .set(asccManifest)
                        .returning(ASCC_MANIFEST.ASCC_MANIFEST_ID).fetchOne().getAsccManifestId()
        );

        seqKeyHandler(request.getUser(), asccManifest).moveTo(request.getPos());

        upsertLogIntoAccAndAssociations(
                accRecord, accManifestRecord,
                ULong.valueOf(request.getReleaseId()),
                userId, timestamp
        );

        return new CreateAsccRepositoryResponse(asccManifest.getAsccManifestId().toBigInteger());
    }

    private void upsertLogIntoAccAndAssociations(AccRecord accRecord,
                                                 AccManifestRecord accManifestRecord,
                                                 ULong releaseId,
                                                 ULong userId, LocalDateTime timestamp) {
        LogRecord logRecord =
                logRepository.insertAccLog(accManifestRecord,
                        accRecord,
                        accManifestRecord.getLogId(),
                        LogAction.Modified,
                        userId, timestamp);

        accManifestRecord.setLogId(logRecord.getLogId());
        accManifestRecord.update(ACC_MANIFEST.LOG_ID);
    }

    public UpdateAsccPropertiesRepositoryResponse updateAsccProperties(UpdateAsccPropertiesRepositoryRequest request) {
        AppUser user = sessionService.getAppUser(request.getUser());
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

        String den = accRecord.getObjectClassTerm() + ". " + dslContext.select(ASCCP.DEN)
                .from(ASCCP)
                .join(ASCCP_MANIFEST).on(ASCCP.ASCCP_ID.eq(ASCCP_MANIFEST.ASCCP_ID))
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(asccManifestRecord.getToAsccpManifestId()))
                .fetchOneInto(String.class);
        if (compare(asccRecord.getDen(), den) != 0) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ASCC.DEN, den);
        }
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

        if (moreStep != null) {
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
        AppUser user = sessionService.getAppUser(request.getUser());
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

        if (!CcState.WIP.equals(CcState.valueOf(accRecord.getState()))) {
            throw new IllegalArgumentException("Only the core component in 'WIP' state can be deleted.");
        }

        if (!accRecord.getOwnerUserId().equals(userId)) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
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

        upsertLogIntoAccAndAssociations(
                accRecord, accManifestRecord,
                accManifestRecord.getReleaseId(),
                userId, timestamp
        );

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
        AppUser user = sessionService.getAppUser(request.getUser());
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

        String asccpDen = dslContext.select(ASCCP.DEN)
                .from(ASCCP_MANIFEST)
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(asccManifestRecord.getToAsccpManifestId()))
                .fetchOneInto(String.class);

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

        this.checkDuplicate(asccManifestRecord, accManifestRecord.getAccManifestId());

        // delete from Tables
        seqKeyHandler(request.getUser(), asccManifestRecord).deleteCurrent();
        asccManifestRecord.setFromAccManifestId(ULong.valueOf(request.getAccManifestId()));
        asccManifestRecord.update(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID);

        seqKeyHandler(request.getUser(), asccManifestRecord).moveTo(MoveTo.LAST);

        asccRecord.setLastUpdatedBy(userId);
        asccRecord.setLastUpdateTimestamp(timestamp);
        asccRecord.setFromAccId(accRecord.getAccId());
        asccRecord.setDen(accRecord.getObjectClassTerm() + ". " + asccpDen);
        asccRecord.update(ASCC.LAST_UPDATED_BY, ASCC.LAST_UPDATE_TIMESTAMP,
                ASCC.FROM_ACC_ID, ASCC.DEN);

        upsertLogIntoAccAndAssociations(
                accRecord, accManifestRecord,
                accManifestRecord.getReleaseId(),
                userId, timestamp
        );

        upsertLogIntoAccAndAssociations(
                prevAccRecord, prevAccManifestRecord,
                accManifestRecord.getReleaseId(),
                userId, timestamp
        );

        return new RefactorAsccRepositoryResponse(asccManifestRecord.getAsccManifestId().toBigInteger());
    }

    private void checkDuplicate(AsccManifestRecord asccManifestRecord, ULong targetAccManifestId) {
        List<AccManifestRecord> accList = dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.RELEASE_ID.eq(asccManifestRecord.getReleaseId())).fetch();

        Map<ULong, AccManifestRecord> accMap = accList.stream().collect(Collectors.toMap(AccManifestRecord::getAccManifestId, Function.identity()));
        Map<ULong, List<AccManifestRecord>> baseAccMap = accList.stream().filter(e -> e.getBasedAccManifestId() != null)
                .collect(Collectors.groupingBy(AccManifestRecord::getBasedAccManifestId));

        List<AsccManifestRecord> asccList = dslContext.selectFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.RELEASE_ID.eq(asccManifestRecord.getReleaseId())).fetch();

        Map<ULong, List<AsccManifestRecord>> fromAccAsccMap = asccList.stream()
                .collect(Collectors.groupingBy(AsccManifestRecord::getFromAccManifestId));

        List<ULong> accManifestList = new ArrayList<>();

        AccManifestRecord targetAccManifestRecord = accMap.get(targetAccManifestId);

        accManifestList.add(targetAccManifestId);

        while (targetAccManifestRecord.getBasedAccManifestId() != null) {
            accManifestList.add(targetAccManifestRecord.getBasedAccManifestId());
            targetAccManifestRecord = accMap.get(targetAccManifestRecord.getBasedAccManifestId());
        }

        Set<ULong> result = new HashSet<>();

        for (ULong cur : accManifestList) {
            result.addAll(getBaseAccManifestId(cur, baseAccMap));
        }

        Set<AsccManifestRecord> asccResult = new HashSet<>();

        for (ULong acc : result) {
            asccResult.addAll(
                    fromAccAsccMap.getOrDefault(acc, Collections.emptyList())
                            .stream()
                            .filter(ascc -> ascc.getToAsccpManifestId().equals(asccManifestRecord.getToAsccpManifestId())
                                    && !ascc.getAsccManifestId().equals(asccManifestRecord.getAsccManifestId()))
                            .collect(Collectors.toList()));
        }

        if (asccResult.size() > 0) {
            throw new IllegalArgumentException("Unable to refactor. " + asccResult.size() + " duplicate association(s) exist in the derived ACC");
        }
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