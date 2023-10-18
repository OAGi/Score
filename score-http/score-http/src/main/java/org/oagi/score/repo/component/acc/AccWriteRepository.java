package org.oagi.score.repo.component.acc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jooq.DSLContext;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.cc_management.data.CcId;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.corecomponent.seqkey.SeqKeyWriteRepository;
import org.oagi.score.repo.api.corecomponent.seqkey.model.*;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.service.common.data.BCCEntityType;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.service.common.data.OagisComponentType;
import org.oagi.score.service.corecomponent.seqkey.MoveTo;
import org.oagi.score.service.corecomponent.seqkey.SeqKeyHandler;
import org.oagi.score.service.log.LogRepository;
import org.oagi.score.service.log.model.LogAction;
import org.oagi.score.service.log.model.LogSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.StringUtils.compare;
import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.Acc.ACC;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.AccManifest.ACC_MANIFEST;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.BccpManifest.BCCP_MANIFEST;

@Repository
public class AccWriteRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private LogSerializer serializer;

    @Autowired
    private ScoreRepositoryFactory scoreRepositoryFactory;

    public CreateAccRepositoryResponse createAcc(CreateAccRepositoryRequest request) {
        ULong userId = ULong.valueOf(sessionService.userId(request.getUser()));
        LocalDateTime timestamp = request.getLocalDateTime();
        AccManifestRecord basedAccManifest = null;

        if (request.getBasedAccManifestId() != null) {
            basedAccManifest = dslContext.selectFrom(ACC_MANIFEST)
                    .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(request.getBasedAccManifestId()))).fetchOne();
        }

        AccRecord acc = new AccRecord();
        acc.setGuid(ScoreGuid.randomGuid());
        acc.setObjectClassTerm(request.getInitialObjectClassTerm());
        acc.setOagisComponentType(request.getInitialComponentType().getValue());
        acc.setType(request.getInitialType().name());
        acc.setDefinition(request.getInitialDefinition());
        acc.setState(CcState.WIP.name());
        acc.setIsAbstract((byte) 0);
        acc.setIsDeprecated((byte) 0);
        if (basedAccManifest != null) {
            acc.setBasedAccId(basedAccManifest.getAccId());
        }
        if (request.getNamespaceId() != null) {
            acc.setNamespaceId(ULong.valueOf(request.getNamespaceId()));
        }
        acc.setCreatedBy(userId);
        acc.setLastUpdatedBy(userId);
        acc.setOwnerUserId(userId);
        acc.setCreationTimestamp(timestamp);
        acc.setLastUpdateTimestamp(timestamp);

        acc.setAccId(
                dslContext.insertInto(ACC)
                        .set(acc)
                        .returning(ACC.ACC_ID).fetchOne().getAccId()
        );

        AccManifestRecord accManifest = new AccManifestRecord();
        accManifest.setAccId(acc.getAccId());
        accManifest.setReleaseId(ULong.valueOf(request.getReleaseId()));
        if (basedAccManifest != null) {
            accManifest.setBasedAccManifestId(basedAccManifest.getAccManifestId());
        }
        accManifest.setDen(acc.getObjectClassTerm() + ". Details");

        LogRecord logRecord =
                logRepository.insertAccLog(
                        accManifest,
                        acc,
                        LogAction.Added,
                        userId, timestamp);
        accManifest.setLogId(logRecord.getLogId());

        accManifest.setAccManifestId(
                dslContext.insertInto(ACC_MANIFEST)
                        .set(accManifest)
                        .returning(ACC_MANIFEST.ACC_MANIFEST_ID).fetchOne().getAccManifestId()
        );

        if (StringUtils.hasLength(request.getTag())) {
            ULong accManifestId = accManifest.getAccManifestId();
            dslContext.selectFrom(TAG)
                    .where(TAG.NAME.eq(request.getTag()))
                    .fetchOptionalInto(TagRecord.class)
                    .ifPresent(tagRecord -> {
                        AccManifestTagRecord accManifestTagRecord = new AccManifestTagRecord();
                        accManifestTagRecord.setAccManifestId(accManifestId);
                        accManifestTagRecord.setTagId(tagRecord.getTagId());
                        accManifestTagRecord.setCreatedBy(userId);
                        accManifestTagRecord.setCreationTimestamp(timestamp);
                        dslContext.insertInto(ACC_MANIFEST_TAG)
                                .set(accManifestTagRecord)
                                .execute();
                    });
        }

        return new CreateAccRepositoryResponse(accManifest.getAccManifestId().toBigInteger());
    }

    public ReviseAccRepositoryResponse reviseAcc(ReviseAccRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        AccManifestRecord accManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(
                        ULong.valueOf(request.getAccManifestId())
                ))
                .fetchOne();

        AccRecord prevAccRecord = dslContext.selectFrom(ACC)
                .where(ACC.ACC_ID.eq(accManifestRecord.getAccId()))
                .fetchOne();

        if (user.isDeveloper()) {
            if (!CcState.Published.equals(CcState.valueOf(prevAccRecord.getState()))) {
                throw new IllegalArgumentException("Only the core component in 'Published' state can be revised.");
            }
        } else {
            if (!CcState.Production.equals(CcState.valueOf(prevAccRecord.getState()))) {
                throw new IllegalArgumentException("Only the core component in 'Production' state can be revised.");
            }
        }

        ULong workingReleaseId = dslContext.select(RELEASE.RELEASE_ID)
                .from(RELEASE)
                .where(RELEASE.RELEASE_NUM.eq("Working"))
                .fetchOneInto(ULong.class);

        ULong targetReleaseId = accManifestRecord.getReleaseId();
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
                .where(APP_USER.APP_USER_ID.eq(prevAccRecord.getOwnerUserId()))
                .fetchOneInto(Boolean.class);

        if (user.isDeveloper() != ownerIsDeveloper) {
            throw new IllegalArgumentException("It only allows to revise the component for users in the same roles.");
        }

        // creates new acc for revised record.
        AccRecord nextAccRecord = prevAccRecord.copy();
        nextAccRecord.setState(CcState.WIP.name());
        nextAccRecord.setCreatedBy(userId);
        nextAccRecord.setLastUpdatedBy(userId);
        nextAccRecord.setOwnerUserId(userId);
        nextAccRecord.setCreationTimestamp(timestamp);
        nextAccRecord.setLastUpdateTimestamp(timestamp);
        nextAccRecord.setPrevAccId(prevAccRecord.getAccId());
        nextAccRecord.setAccId(
                dslContext.insertInto(ACC)
                        .set(nextAccRecord)
                        .returning(ACC.ACC_ID).fetchOne().getAccId()
        );

        prevAccRecord.setNextAccId(nextAccRecord.getAccId());
        prevAccRecord.update(ACC.NEXT_ACC_ID);

        // create new associations for revised record.
        createNewAsccListForRevisedRecord(user, accManifestRecord, nextAccRecord, targetReleaseId, timestamp);
        createNewBccListForRevisedRecord(user, accManifestRecord, nextAccRecord, targetReleaseId, timestamp);

        // creates new revision for revised record.
        LogRecord logRecord =
                logRepository.insertAccLog(accManifestRecord,
                        nextAccRecord, accManifestRecord.getLogId(),
                        LogAction.Revised,
                        userId, timestamp);

        ULong responseAccManifestId;
        accManifestRecord.setAccId(nextAccRecord.getAccId());
        accManifestRecord.setLogId(logRecord.getLogId());
        accManifestRecord.update(ACC_MANIFEST.ACC_ID, ACC_MANIFEST.LOG_ID);

        responseAccManifestId = accManifestRecord.getAccManifestId();

        // update `conflict` for asccp_manifests' role_of_acc_manifest_id which indicates given acc manifest.
        dslContext.update(ASCCP_MANIFEST)
                .set(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID, responseAccManifestId)
                .set(ASCCP_MANIFEST.CONFLICT, (byte) 1)
                .where(and(
                        ASCCP_MANIFEST.RELEASE_ID.eq(targetReleaseId),
                        ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID.in(Arrays.asList(
                                accManifestRecord.getAccManifestId(),
                                accManifestRecord.getPrevAccManifestId()))
                ))
                .execute();

        // update `conflict` for acc_manifests' based_acc_manifest_id which indicates given acc manifest.
        dslContext.update(ACC_MANIFEST)
                .set(ACC_MANIFEST.BASED_ACC_MANIFEST_ID, responseAccManifestId)
                .set(ACC_MANIFEST.CONFLICT, (byte) 1)
                .where(and(
                        ACC_MANIFEST.RELEASE_ID.eq(targetReleaseId),
                        ACC_MANIFEST.BASED_ACC_MANIFEST_ID.in(Arrays.asList(
                                accManifestRecord.getAccManifestId(),
                                accManifestRecord.getPrevAccManifestId()))
                ))
                .execute();

        return new ReviseAccRepositoryResponse(responseAccManifestId.toBigInteger());
    }

    private void createNewAsccListForRevisedRecord(
            AppUser user,
            AccManifestRecord accManifestRecord,
            AccRecord nextAccRecord,
            ULong targetReleaseId,
            LocalDateTime timestamp) {
        ULong fromAccManifestId = accManifestRecord.getAccManifestId();
        for (AsccManifestRecord asccManifestRecord : dslContext.selectFrom(ASCC_MANIFEST)
                .where(and(
                        ASCC_MANIFEST.RELEASE_ID.eq(targetReleaseId),
                        ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(fromAccManifestId)
                ))
                .fetch()) {

            AsccRecord prevAsccRecord = dslContext.selectFrom(ASCC)
                    .where(ASCC.ASCC_ID.eq(asccManifestRecord.getAsccId()))
                    .fetchOne();

            AsccRecord nextAsccRecord = prevAsccRecord.copy();
            nextAsccRecord.setFromAccId(nextAccRecord.getAccId());
            nextAsccRecord.setToAsccpId(
                    dslContext.select(ASCCP_MANIFEST.ASCCP_ID)
                            .from(ASCCP_MANIFEST)
                            .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(asccManifestRecord.getToAsccpManifestId()))
                            .fetchOneInto(ULong.class)
            );
            nextAsccRecord.setState(CcState.WIP.name());
            nextAsccRecord.setCreatedBy(ULong.valueOf(user.getAppUserId()));
            nextAsccRecord.setLastUpdatedBy(ULong.valueOf(user.getAppUserId()));
            nextAsccRecord.setOwnerUserId(ULong.valueOf(user.getAppUserId()));
            nextAsccRecord.setCreationTimestamp(timestamp);
            nextAsccRecord.setLastUpdateTimestamp(timestamp);
            nextAsccRecord.setPrevAsccId(prevAsccRecord.getAsccId());
            nextAsccRecord.setAsccId(
                    dslContext.insertInto(ASCC)
                            .set(nextAsccRecord)
                            .returning(ASCC.ASCC_ID).fetchOne().getAsccId()
            );

            prevAsccRecord.setNextAsccId(nextAsccRecord.getAsccId());
            prevAsccRecord.update(ASCC.NEXT_ASCC_ID);

            asccManifestRecord.setAsccId(nextAsccRecord.getAsccId());
            asccManifestRecord.setFromAccManifestId(accManifestRecord.getAccManifestId());
            asccManifestRecord.update(ASCC_MANIFEST.ASCC_ID, ASCC_MANIFEST.FROM_ACC_MANIFEST_ID);
        }
    }

    private void createNewBccListForRevisedRecord(
            AppUser user,
            AccManifestRecord accManifestRecord,
            AccRecord nextAccRecord,
            ULong targetReleaseId,
            LocalDateTime timestamp) {
        ULong fromAccManifestId = accManifestRecord.getAccManifestId();
        for (BccManifestRecord bccManifestRecord : dslContext.selectFrom(BCC_MANIFEST)
                .where(and(
                        BCC_MANIFEST.RELEASE_ID.eq(targetReleaseId),
                        BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(fromAccManifestId)
                ))
                .fetch()) {

            BccRecord prevBccRecord = dslContext.selectFrom(BCC)
                    .where(BCC.BCC_ID.eq(bccManifestRecord.getBccId()))
                    .fetchOne();

            BccRecord nextBccRecord = prevBccRecord.copy();
            nextBccRecord.setFromAccId(nextAccRecord.getAccId());
            nextBccRecord.setToBccpId(
                    dslContext.select(BCCP_MANIFEST.BCCP_ID)
                            .from(BCCP_MANIFEST)
                            .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(bccManifestRecord.getToBccpManifestId()))
                            .fetchOneInto(ULong.class)
            );
            nextBccRecord.setState(CcState.WIP.name());
            nextBccRecord.setCreatedBy(ULong.valueOf(user.getAppUserId()));
            nextBccRecord.setLastUpdatedBy(ULong.valueOf(user.getAppUserId()));
            nextBccRecord.setOwnerUserId(ULong.valueOf(user.getAppUserId()));
            nextBccRecord.setCreationTimestamp(timestamp);
            nextBccRecord.setLastUpdateTimestamp(timestamp);
            nextBccRecord.setPrevBccId(prevBccRecord.getBccId());
            nextBccRecord.setBccId(
                    dslContext.insertInto(BCC)
                            .set(nextBccRecord)
                            .returning(BCC.BCC_ID).fetchOne().getBccId()
            );

            prevBccRecord.setNextBccId(nextBccRecord.getBccId());
            prevBccRecord.update(BCC.NEXT_BCC_ID);

            bccManifestRecord.setBccId(nextBccRecord.getBccId());
            bccManifestRecord.setFromAccManifestId(accManifestRecord.getAccManifestId());
            bccManifestRecord.update(BCC_MANIFEST.BCC_ID, BCC_MANIFEST.FROM_ACC_MANIFEST_ID);
        }
    }

    private ULong getNewSeqkeyIdByOldSeq(SeqKeyRecord seqKeyRecord, AccManifestRecord accManifestRecord) {
        if (seqKeyRecord.getAsccManifestId() != null) {
            return dslContext.select(ASCC_MANIFEST.as("next").SEQ_KEY_ID)
                    .from(ASCC_MANIFEST.as("prev"))
                    .join(ASCC_MANIFEST.as("next"))
                    .on(ASCC_MANIFEST.as("prev").ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("next").PREV_ASCC_MANIFEST_ID))
                    .where(and(ASCC_MANIFEST.as("prev").SEQ_KEY_ID.eq(seqKeyRecord.getSeqKeyId())),
                            ASCC_MANIFEST.as("prev").FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getPrevAccManifestId()),
                            ASCC_MANIFEST.as("next").FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                    .fetchOneInto(ULong.class);
        } else {
            return dslContext.select(BCC_MANIFEST.as("next").SEQ_KEY_ID)
                    .from(BCC_MANIFEST.as("prev"))
                    .join(BCC_MANIFEST.as("next"))
                    .on(BCC_MANIFEST.as("prev").BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("next").PREV_BCC_MANIFEST_ID))
                    .where(and(BCC_MANIFEST.as("prev").SEQ_KEY_ID.eq(seqKeyRecord.getSeqKeyId())),
                            BCC_MANIFEST.as("prev").FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getPrevAccManifestId()),
                            BCC_MANIFEST.as("next").FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                    .fetchOneInto(ULong.class);
        }
    }

    public UpdateAccPropertiesRepositoryResponse updateAccProperties(UpdateAccPropertiesRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        AccManifestRecord accManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(
                        ULong.valueOf(request.getAccManifestId())
                ))
                .fetchOne();

        AccRecord accRecord = dslContext.selectFrom(ACC)
                .where(ACC.ACC_ID.eq(accManifestRecord.getAccId()))
                .fetchOne();

        if (!CcState.WIP.equals(CcState.valueOf(accRecord.getState()))) {
            throw new IllegalArgumentException("Only the core component in 'WIP' state can be modified.");
        }

        if (!accRecord.getOwnerUserId().equals(userId)) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        // update acc record.
        boolean denNeedsToUpdate = false;
        UpdateSetFirstStep<AccRecord> firstStep = dslContext.update(ACC);
        UpdateSetMoreStep<AccRecord> moreStep = null;
        if (compare(accRecord.getObjectClassTerm(), request.getObjectClassTerm()) != 0) {
            denNeedsToUpdate = true;
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ACC.OBJECT_CLASS_TERM, request.getObjectClassTerm());

            String den = request.getObjectClassTerm() + ". Details";
            accManifestRecord.setDen(den);
            dslContext.update(ACC_MANIFEST)
                    .set(ACC_MANIFEST.DEN, den)
                    .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                    .execute();
        }
        if (compare(accRecord.getDefinition(), request.getDefinition()) != 0) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ACC.DEFINITION, request.getDefinition());
        }
        if (compare(accRecord.getDefinitionSource(), request.getDefinitionSource()) != 0) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ACC.DEFINITION_SOURCE, request.getDefinitionSource());
        }
        if (request.getComponentType() != null) {
            if (accRecord.getOagisComponentType() != request.getComponentType().getValue()) {
                moreStep = ((moreStep != null) ? moreStep : firstStep)
                        .set(ACC.OAGIS_COMPONENT_TYPE, request.getComponentType().getValue());
            }
        }
        if ((accRecord.getIsAbstract() == 1) != request.isAbstract()) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ACC.IS_ABSTRACT, (byte) ((request.isAbstract()) ? 1 : 0));
        }
        if ((accRecord.getIsDeprecated() == 1) != request.isDeprecated()) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ACC.IS_DEPRECATED, (byte) ((request.isDeprecated()) ? 1 : 0));
        }
        if (request.getNamespaceId() == null || request.getNamespaceId().longValue() <= 0L) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .setNull(ACC.NAMESPACE_ID);
        } else {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ACC.NAMESPACE_ID, ULong.valueOf(request.getNamespaceId()));
        }

        if (moreStep != null) {
            moreStep.set(ACC.LAST_UPDATED_BY, userId)
                    .set(ACC.LAST_UPDATE_TIMESTAMP, timestamp)
                    .where(ACC.ACC_ID.eq(accRecord.getAccId()))
                    .execute();

            accRecord = dslContext.selectFrom(ACC)
                    .where(ACC.ACC_ID.eq(accManifestRecord.getAccId()))
                    .fetchOne();
        }

        if (denNeedsToUpdate) {
            for (AsccManifestRecord asccManifestRecord : dslContext.selectFrom(ASCC_MANIFEST)
                    .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                    .fetch()) {

                String asccpDen = dslContext.select(ASCCP_MANIFEST.DEN)
                        .from(ASCCP_MANIFEST)
                        .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(asccManifestRecord.getToAsccpManifestId()))
                        .fetchOneInto(String.class);

                asccManifestRecord.setDen(accRecord.getObjectClassTerm() + ". " + asccpDen);
                asccManifestRecord.update(ASCC_MANIFEST.DEN);
            }

            for (BccManifestRecord bccManifestRecord : dslContext.selectFrom(BCC_MANIFEST)
                    .where(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                    .fetch()) {

                String bccpDen = dslContext.select(BCCP_MANIFEST.DEN)
                        .from(BCCP_MANIFEST)
                        .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(bccManifestRecord.getToBccpManifestId()))
                        .fetchOneInto(String.class);

                bccManifestRecord.setDen(accRecord.getObjectClassTerm() + ". " + bccpDen);
                bccManifestRecord.update(BCC_MANIFEST.DEN);
            }

            for (AsccpManifestRecord asccpManifestRecord : dslContext.selectFrom(ASCCP_MANIFEST)
                    .where(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                    .fetch()) {

                AsccpRecord asccpRecord = dslContext.selectFrom(ASCCP)
                        .where(ASCCP.ASCCP_ID.eq(asccpManifestRecord.getAsccpId()))
                        .fetchOne();

                asccpManifestRecord.setDen(asccpRecord.getPropertyTerm() + ". " + accRecord.getObjectClassTerm());
                asccpManifestRecord.update(ASCCP_MANIFEST.DEN);

                for (AsccManifestRecord asccManifestRecord : dslContext.selectFrom(ASCC_MANIFEST)
                        .where(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(asccpManifestRecord.getAsccpManifestId()))
                        .fetch()) {

                    String objectClassTerm = dslContext.select(ACC.OBJECT_CLASS_TERM)
                            .from(ACC)
                            .join(ACC_MANIFEST).on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                            .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(asccManifestRecord.getFromAccManifestId()))
                            .fetchOneInto(String.class);
                    asccManifestRecord.setDen(objectClassTerm + ". " + asccpManifestRecord.getDen());
                    asccManifestRecord.update(ASCC_MANIFEST.DEN);
                }
            }
        }

        if (moreStep != null) {
            // creates new revision for updated record.
            LogRecord logRecord =
                    logRepository.insertAccLog(accManifestRecord,
                            accRecord, accManifestRecord.getLogId(),
                            LogAction.Modified,
                            userId, timestamp);

            accManifestRecord.setLogId(logRecord.getLogId());
            accManifestRecord.update(ACC_MANIFEST.LOG_ID);
        }

        return new UpdateAccPropertiesRepositoryResponse(accManifestRecord.getAccManifestId().toBigInteger());
    }

    public UpdateAccBasedAccRepositoryResponse updateAccBasedAcc(UpdateAccBasedAccRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        AccManifestRecord accManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(
                        ULong.valueOf(request.getAccManifestId())
                ))
                .fetchOne();

        AccRecord accRecord = dslContext.selectFrom(ACC)
                .where(ACC.ACC_ID.eq(accManifestRecord.getAccId()))
                .fetchOne();

        if (!CcState.WIP.equals(CcState.valueOf(accRecord.getState()))) {
            throw new IllegalArgumentException("Only the core component in 'WIP' state can be modified.");
        }

        if (!accRecord.getOwnerUserId().equals(userId)) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        // update acc record.
        if (request.getBasedAccManifestId() == null) {
            accRecord.setBasedAccId(null);
        } else {
            AccManifestRecord basedAccManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                    .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(request.getBasedAccManifestId())))
                    .fetchOne();

            // Issue #1024
            ensureNoConflictsInAssociation(accManifestRecord, basedAccManifestRecord);
            accRecord.setBasedAccId(basedAccManifestRecord.getAccId());
        }
        accRecord.setLastUpdatedBy(userId);
        accRecord.setLastUpdateTimestamp(timestamp);
        accRecord.update(ACC.BASED_ACC_ID,
                ACC.LAST_UPDATED_BY, ACC.LAST_UPDATE_TIMESTAMP);

        // creates new revision for updated record.
        LogRecord logRecord =
                logRepository.insertAccLog(accManifestRecord,
                        accRecord, accManifestRecord.getLogId(),
                        LogAction.Modified,
                        userId, timestamp);

        if (request.getBasedAccManifestId() == null) {
            accManifestRecord.setBasedAccManifestId(null);
        } else {
            accManifestRecord.setBasedAccManifestId(ULong.valueOf(request.getBasedAccManifestId()));
        }
        accManifestRecord.setLogId(logRecord.getLogId());
        accManifestRecord.update(ACC_MANIFEST.BASED_ACC_MANIFEST_ID, ACC_MANIFEST.LOG_ID);

        return new UpdateAccBasedAccRepositoryResponse(accManifestRecord.getAccManifestId().toBigInteger());
    }

    private void ensureNoConflictsInAssociation(AccManifestRecord accManifestRecord,
                                                AccManifestRecord basedAccManifestRecord) {
        List<AsccManifestRecord> asccManifestRecords = dslContext.selectFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                .fetch();

        List<BccManifestRecord> bccManifestRecords = dslContext.selectFrom(BCC_MANIFEST)
                .where(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                .fetch();

        List<ULong> asccpManifestIds = asccManifestRecords.stream()
                .map(AsccManifestRecord::getToAsccpManifestId).collect(Collectors.toList());

        List<ULong> bccpManifestIds = bccManifestRecords.stream()
                .map(BccManifestRecord::getToBccpManifestId).collect(Collectors.toList());

        while (basedAccManifestRecord != null) {
            List<String> conflictAsccpList = dslContext.select(ASCCP_MANIFEST.DEN)
                    .from(ASCCP_MANIFEST)
                    .join(ASCC_MANIFEST).on(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID))
                    .where(
                            and(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(basedAccManifestRecord.getAccManifestId()),
                                    ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.in(asccpManifestIds))
                    )
                    .fetchInto(String.class);
            if (!conflictAsccpList.isEmpty()) {
                if (conflictAsccpList.size() == 1) {
                    throw new IllegalArgumentException("There is a conflict in ASCCPs between the current ACC and the base ACC [" + conflictAsccpList.get(0) + "]");
                } else {
                    throw new IllegalArgumentException("There are conflicts in ASCCPs between the current ACC and the base ACC [" + String.join(", ", conflictAsccpList) + "]");
                }
            }

            List<String> conflictBccpList = dslContext.select(BCCP_MANIFEST.DEN)
                    .from(BCCP_MANIFEST)
                    .join(BCC_MANIFEST).on(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(BCC_MANIFEST.TO_BCCP_MANIFEST_ID))
                    .where(
                            and(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(basedAccManifestRecord.getAccManifestId()),
                                    BCC_MANIFEST.TO_BCCP_MANIFEST_ID.in(bccpManifestIds))
                    )
                    .fetchInto(String.class);
            if (!conflictBccpList.isEmpty()) {
                if (conflictBccpList.size() == 1) {
                    throw new IllegalArgumentException("There is a conflict in BCCPs between the current ACC and the base ACC [" + conflictBccpList.get(0) + "]");
                } else {
                    throw new IllegalArgumentException("There are conflicts in BCCPs between the current ACC and the base ACC [" + String.join(", ", conflictBccpList) + "]");
                }
            }

            basedAccManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                    .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(basedAccManifestRecord.getBasedAccManifestId())).fetchOne();
        }
    }

    public UpdateAccStateRepositoryResponse updateAccState(UpdateAccStateRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        AccManifestRecord accManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(
                        ULong.valueOf(request.getAccManifestId())
                ))
                .fetchOne();

        AccRecord accRecord = dslContext.selectFrom(ACC)
                .where(ACC.ACC_ID.eq(accManifestRecord.getAccId()))
                .fetchOne();

        CcState prevState = CcState.valueOf(accRecord.getState());
        CcState nextState = request.getToState();

        if (prevState != request.getFromState()) {
            throw new IllegalArgumentException("Target core component is not in '" + request.getFromState() + "' state.");
        }

        if (!prevState.canMove(nextState)) {
            throw new IllegalArgumentException("The core component in '" + prevState + "' state cannot move to '" + nextState + "' state.");
        }

        // Change owner of CC when it restored.
        if (prevState == CcState.Deleted && nextState == CcState.WIP) {
            accRecord.setOwnerUserId(userId);
        } else if (prevState != CcState.Deleted && !accRecord.getOwnerUserId().equals(userId)
                && !prevState.canForceMove(request.getToState())) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        } else if (accRecord.getNamespaceId() == null) {
            throw new IllegalArgumentException("'" + accManifestRecord.getDen() + "' namespace required.");
        }

        // update acc state.
        accRecord.setState(nextState.name());
        if (!prevState.canForceMove(request.getToState())) {
            accRecord.setLastUpdatedBy(userId);
            accRecord.setLastUpdateTimestamp(timestamp);
        }
        accRecord.update(ACC.STATE,
                ACC.LAST_UPDATED_BY, ACC.LAST_UPDATE_TIMESTAMP, ACC.OWNER_USER_ID);

        // update associations' state.
        updateAsccListForStateUpdatedRecord(accManifestRecord, accRecord, nextState, userId, timestamp);
        updateBccListForStateUpdatedRecord(accManifestRecord, accRecord, nextState, userId, timestamp);

        // creates new revision for updated record.
        LogAction logAction = (CcState.Deleted == prevState && CcState.WIP == nextState)
                ? LogAction.Restored : LogAction.Modified;
        LogRecord logRecord =
                logRepository.insertAccLog(accManifestRecord,
                        accRecord, accManifestRecord.getLogId(),
                        logAction,
                        userId, timestamp);

        accManifestRecord.setLogId(logRecord.getLogId());
        accManifestRecord.update(ACC_MANIFEST.LOG_ID);

        return new UpdateAccStateRepositoryResponse(accManifestRecord.getAccManifestId().toBigInteger());
    }

    private void updateAsccListForStateUpdatedRecord(
            AccManifestRecord accManifestRecord,
            AccRecord nextAccRecord,
            CcState nextState,
            ULong userId,
            LocalDateTime timestamp) {
        for (AsccManifestRecord asccManifestRecord : dslContext.selectFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                .fetch()) {

            AsccRecord asccRecord = dslContext.selectFrom(ASCC)
                    .where(ASCC.ASCC_ID.eq(asccManifestRecord.getAsccId()))
                    .fetchOne();

            asccRecord.setFromAccId(nextAccRecord.getAccId());
            asccRecord.setToAsccpId(
                    dslContext.select(ASCCP_MANIFEST.ASCCP_ID)
                            .from(ASCCP_MANIFEST)
                            .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(asccManifestRecord.getToAsccpManifestId()))
                            .fetchOneInto(ULong.class)
            );

            CcState prevState = CcState.valueOf(asccRecord.getState());

            // Change owner of CC when it restored.
            if (prevState == CcState.Deleted && nextState == CcState.WIP) {
                asccRecord.setOwnerUserId(userId);
            }

            asccRecord.setState(nextState.name());
            asccRecord.setLastUpdatedBy(userId);
            asccRecord.setLastUpdateTimestamp(timestamp);
            asccRecord.update(ASCC.FROM_ACC_ID, ASCC.TO_ASCCP_ID, ASCC.STATE,
                    ASCC.LAST_UPDATED_BY, ASCC.LAST_UPDATE_TIMESTAMP, ASCC.OWNER_USER_ID);
        }
    }

    private void updateBccListForStateUpdatedRecord(
            AccManifestRecord accManifestRecord,
            AccRecord nextAccRecord,
            CcState nextState,
            ULong userId,
            LocalDateTime timestamp) {
        for (BccManifestRecord bccManifestRecord : dslContext.selectFrom(BCC_MANIFEST)
                .where(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                .fetch()) {

            BccRecord bccRecord = dslContext.selectFrom(BCC)
                    .where(BCC.BCC_ID.eq(bccManifestRecord.getBccId()))
                    .fetchOne();

            bccRecord.setFromAccId(nextAccRecord.getAccId());
            bccRecord.setToBccpId(
                    dslContext.select(BCCP_MANIFEST.BCCP_ID)
                            .from(BCCP_MANIFEST)
                            .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(bccManifestRecord.getToBccpManifestId()))
                            .fetchOneInto(ULong.class)
            );

            CcState prevState = CcState.valueOf(bccRecord.getState());

            // Change owner of CC when it restored.
            if (prevState == CcState.Deleted && nextState == CcState.WIP) {
                bccRecord.setOwnerUserId(userId);
            }

            bccRecord.setState(nextState.name());
            bccRecord.setLastUpdatedBy(userId);
            bccRecord.setLastUpdateTimestamp(timestamp);
            bccRecord.update(BCC.FROM_ACC_ID, BCC.TO_BCCP_ID, BCC.STATE,
                    BCC.LAST_UPDATED_BY, BCC.LAST_UPDATE_TIMESTAMP, BCC.OWNER_USER_ID);
        }
    }

    public DeleteAccRepositoryResponse deleteAcc(DeleteAccRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        AccManifestRecord accManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(
                        ULong.valueOf(request.getAccManifestId())
                ))
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

        // update acc state.
        accRecord.setState(CcState.Deleted.name());
        accRecord.setLastUpdatedBy(userId);
        accRecord.setLastUpdateTimestamp(timestamp);
        accRecord.update(ACC.STATE,
                ACC.LAST_UPDATED_BY, ACC.LAST_UPDATE_TIMESTAMP);

        // creates new revision for deleted record.
        LogRecord logRecord =
                logRepository.insertAccLog(accManifestRecord,
                        accRecord, accManifestRecord.getLogId(),
                        LogAction.Deleted,
                        userId, timestamp);

        accManifestRecord.setLogId(logRecord.getLogId());
        accManifestRecord.update(ACC_MANIFEST.LOG_ID);

        return new DeleteAccRepositoryResponse(accManifestRecord.getAccManifestId().toBigInteger());
    }

    public PurgeAccRepositoryResponse purgeAcc(PurgeAccRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        AccManifestRecord accManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(
                        ULong.valueOf(request.getAccManifestId())
                ))
                .fetchOne();

        AccRecord accRecord = dslContext.selectFrom(ACC)
                .where(ACC.ACC_ID.eq(accManifestRecord.getAccId()))
                .fetchOne();

        if (!CcState.Deleted.equals(CcState.valueOf(accRecord.getState()))) {
            IllegalArgumentException e = new IllegalArgumentException("Only the core component in 'Deleted' state can be purged.");
            if (request.isIgnoreOnError()) {
                return new PurgeAccRepositoryResponse(accManifestRecord.getAccManifestId().toBigInteger(), e);
            } else {
                throw e;
            }
        }

        if (accRecord.getOagisComponentType() == OagisComponentType.UserExtensionGroup.getValue()) {

        }

        List<AsccpManifestRecord> asccpManifestRecords = dslContext.selectFrom(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                .fetch();
        if (!asccpManifestRecords.isEmpty()) {
            IllegalArgumentException e = new IllegalArgumentException("Please purge related-ASCCPs first before purging the ACC '" + accManifestRecord.getDen() + "'.");
            if (request.isIgnoreOnError()) {
                return new PurgeAccRepositoryResponse(accManifestRecord.getAccManifestId().toBigInteger(), e);
            } else {
                throw e;
            }
        }

        List<AccManifestRecord> basedAccManifestRecords = dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.BASED_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                .fetch();
        if (!basedAccManifestRecords.isEmpty()) {
            IllegalArgumentException e = new IllegalArgumentException("Please purge derivations first before purging the ACC '" + accManifestRecord.getDen() + "'.");
            if (request.isIgnoreOnError()) {
                return new PurgeAccRepositoryResponse(accManifestRecord.getAccManifestId().toBigInteger(), e);
            } else {
                throw e;
            }
        }

        // discard Log
        ULong logId = accManifestRecord.getLogId();
        dslContext.update(ACC_MANIFEST)
                .setNull(ACC_MANIFEST.LOG_ID)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                .execute();

        dslContext.update(LOG)
                .setNull(LOG.PREV_LOG_ID)
                .setNull(LOG.NEXT_LOG_ID)
                .where(LOG.REFERENCE.eq(accRecord.getGuid()))
                .execute();

        dslContext.deleteFrom(LOG)
                .where(LOG.REFERENCE.eq(accRecord.getGuid()))
                .execute();

        // discard SEQ_KEYs
        dslContext.update(ASCC_MANIFEST)
                .setNull(ASCC_MANIFEST.SEQ_KEY_ID)
                .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                .execute();

        dslContext.update(BCC_MANIFEST)
                .setNull(BCC_MANIFEST.SEQ_KEY_ID)
                .where(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                .execute();

        dslContext.update(SEQ_KEY)
                .setNull(SEQ_KEY.PREV_SEQ_KEY_ID)
                .setNull(SEQ_KEY.NEXT_SEQ_KEY_ID)
                .where(SEQ_KEY.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                .execute();

        dslContext.deleteFrom(SEQ_KEY)
                .where(SEQ_KEY.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                .execute();

        // discard ASCCs
        List<AsccManifestRecord> asccManifestRecords = dslContext.selectFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                .fetch();

        dslContext.deleteFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                .execute();

        if (!asccManifestRecords.isEmpty()) {
            dslContext.deleteFrom(ASCC)
                    .where(ASCC.ASCC_ID.in(asccManifestRecords.stream().map(e -> e.getAsccId()).collect(Collectors.toList())))
                    .execute();
        }

        // discard BCCs
        List<BccManifestRecord> bccManifestRecords = dslContext.selectFrom(BCC_MANIFEST)
                .where(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                .fetch();

        dslContext.deleteFrom(BCC_MANIFEST)
                .where(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                .execute();

        if (!bccManifestRecords.isEmpty()) {
            dslContext.deleteFrom(BCC)
                    .where(BCC.BCC_ID.in(bccManifestRecords.stream().map(e -> e.getBccId()).collect(Collectors.toList())))
                    .execute();
        }

        // discard assigned ACC in modules
        dslContext.deleteFrom(MODULE_ACC_MANIFEST)
                .where(MODULE_ACC_MANIFEST.ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                .execute();

        // discard corresponding tags
        dslContext.deleteFrom(ACC_MANIFEST_TAG)
                .where(ACC_MANIFEST_TAG.ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                .execute();

        // discard ACC
        dslContext.deleteFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                .execute();

        dslContext.deleteFrom(ACC)
                .where(ACC.ACC_ID.eq(accRecord.getAccId()))
                .execute();

        return new PurgeAccRepositoryResponse(accManifestRecord.getAccManifestId().toBigInteger());
    }

    public DeleteAccRepositoryResponse removeAcc(DeleteAccRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        AccManifestRecord accManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(
                        ULong.valueOf(request.getAccManifestId())
                ))
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

        if (dslContext.selectCount()
                .from(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                .fetchOneInto(Long.class) == 0) {
            if (dslContext.selectCount()
                    .from(ASCCP_MANIFEST)
                    .where(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                    .fetchOneInto(Long.class) == 0) {
                accManifestRecord.delete();
            }
        }

        if (dslContext.selectCount()
                .from(ASCC)
                .where(ASCC.FROM_ACC_ID.eq(accRecord.getAccId()))
                .fetchOneInto(Long.class) == 0) {
            if (dslContext.selectCount()
                    .from(ASCCP)
                    .where(ASCCP.ROLE_OF_ACC_ID.eq(accRecord.getAccId()))
                    .fetchOneInto(Long.class) == 0) {
                accRecord.delete();
            }
        }

        return new DeleteAccRepositoryResponse(accManifestRecord.getAccManifestId().toBigInteger());
    }

    public UpdateAccOwnerRepositoryResponse updateAccOwner(UpdateAccOwnerRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        AccManifestRecord accManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(
                        ULong.valueOf(request.getAccManifestId())
                ))
                .fetchOne();

        AccRecord accRecord = dslContext.selectFrom(ACC)
                .where(ACC.ACC_ID.eq(accManifestRecord.getAccId()))
                .fetchOne();

        if (!CcState.WIP.equals(CcState.valueOf(accRecord.getState()))) {
            throw new IllegalArgumentException("Only the core component in 'WIP' state can be modified.");
        }

        if (!accRecord.getOwnerUserId().equals(userId)) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        accRecord.setOwnerUserId(ULong.valueOf(request.getOwnerId()));
        accRecord.setLastUpdatedBy(userId);
        accRecord.setLastUpdateTimestamp(timestamp);
        accRecord.update(ACC.OWNER_USER_ID, ACC.LAST_UPDATED_BY, ACC.LAST_UPDATE_TIMESTAMP);

        for (AsccManifestRecord asccManifestRecord : dslContext.selectFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                .fetch()) {

            AsccRecord asccRecord = dslContext.selectFrom(ASCC)
                    .where(ASCC.ASCC_ID.eq(asccManifestRecord.getAsccId()))
                    .fetchOne();

            asccRecord.setOwnerUserId(ULong.valueOf(request.getOwnerId()));
            asccRecord.update(ASCC.OWNER_USER_ID);
        }

        for (BccManifestRecord bccManifestRecord : dslContext.selectFrom(BCC_MANIFEST)
                .where(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                .fetch()) {

            BccRecord bccRecord = dslContext.selectFrom(BCC)
                    .where(BCC.BCC_ID.eq(bccManifestRecord.getBccId()))
                    .fetchOne();

            bccRecord.setOwnerUserId(ULong.valueOf(request.getOwnerId()));
            bccRecord.update(BCC.OWNER_USER_ID);
        }

        LogRecord logRecord =
                logRepository.insertAccLog(accManifestRecord,
                        accRecord, accManifestRecord.getLogId(),
                        LogAction.Modified,
                        userId, timestamp);

        accManifestRecord.setLogId(logRecord.getLogId());
        accManifestRecord.update(ACC_MANIFEST.LOG_ID);

        return new UpdateAccOwnerRepositoryResponse(accManifestRecord.getAccManifestId().toBigInteger());
    }

    public void moveSeq(UpdateSeqKeyRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        AccManifestRecord accManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(
                        ULong.valueOf(request.getAccManifestId())
                ))
                .fetchOne();

        AccRecord accRecord = dslContext.selectFrom(ACC)
                .where(ACC.ACC_ID.eq(accManifestRecord.getAccId()))
                .fetchOne();

        if (!CcState.WIP.equals(CcState.valueOf(accRecord.getState()))) {
            throw new IllegalArgumentException("Only the core component in 'WIP' state can be modified.");
        }

        if (!accRecord.getOwnerUserId().equals(userId)) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        moveSeq(request.getUser(), accRecord, accManifestRecord,
                request.getItem(), request.getAfter());

        LogRecord logRecord =
                logRepository.insertAccLog(accManifestRecord,
                        accRecord, accManifestRecord.getLogId(),
                        LogAction.Modified,
                        userId, timestamp);

        accManifestRecord.setLogId(logRecord.getLogId());
        accManifestRecord.update(ACC_MANIFEST.LOG_ID);
    }

    public void moveSeq(AuthenticatedPrincipal requester, AccRecord accRecord, AccManifestRecord accManifestRecord,
                        CcId item, CcId after) {
        AppUser user = sessionService.getAppUserByUsername(requester);
        ULong userId = ULong.valueOf(user.getAppUserId());
        SeqKeyHandler seqKeyHandler;

        if (!accRecord.getOwnerUserId().equals(userId)) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        switch (item.getType().toLowerCase()) {
            case "asccp":
                AsccManifestRecord asccManifestRecord = getAsccManifestRecordForUpdateSeq(accManifestRecord, item);
                seqKeyHandler = seqKeyHandler(requester, asccManifestRecord);
                break;

            case "bccp":
                BccManifestRecord bccManifestRecord = getBccManifestRecordForUpdateSeq(accManifestRecord, item);
                seqKeyHandler = seqKeyHandler(requester, bccManifestRecord);
                break;

            default:
                throw new IllegalArgumentException();
        }

        if (after == null) {
            seqKeyHandler.moveTo(MoveTo.FIRST);
        } else {
            SeqKey seqKey;
            switch (after.getType().toLowerCase()) {
                case "asccp":
                    AsccManifestRecord asccManifestRecord = getAsccManifestRecordForUpdateSeq(accManifestRecord, after);
                    seqKey = scoreRepositoryFactory.createSeqKeyReadRepository()
                            .getSeqKey(new GetSeqKeyRequest(sessionService.asScoreUser(requester))
                                    .withSeqKeyId(asccManifestRecord.getSeqKeyId().toBigInteger()))
                            .getSeqKey();
                    break;

                case "bccp":
                    BccManifestRecord bccManifestRecord = getBccManifestRecordForUpdateSeq(accManifestRecord, after);
                    seqKey = scoreRepositoryFactory.createSeqKeyReadRepository()
                            .getSeqKey(new GetSeqKeyRequest(sessionService.asScoreUser(requester))
                                    .withSeqKeyId(bccManifestRecord.getSeqKeyId().toBigInteger()))
                            .getSeqKey();
                    break;

                default:
                    throw new IllegalArgumentException();
            }

            seqKeyHandler.moveAfter(seqKey);
        }
    }

    private AsccManifestRecord getAsccManifestRecordForUpdateSeq(AccManifestRecord accManifestRecord, CcId ccId) {
        AsccManifestRecord asccManifestRecord = dslContext
                .selectFrom(ASCC_MANIFEST)
                .where(
                        and(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ULong.valueOf(ccId.getManifestId())),
                                ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                )
                .fetchOneInto(AsccManifestRecord.class);


        if (!asccManifestRecord.getFromAccManifestId().equals(accManifestRecord.getAccManifestId())) {
            throw new IllegalArgumentException("It only allows to modify the core component for the corresponding component.");
        }

        return asccManifestRecord;
    }

    private BccManifestRecord getBccManifestRecordForUpdateSeq(AccManifestRecord accManifestRecord, CcId ccId) {
        BccManifestRecord bccManifestRecord = dslContext
                .selectFrom(BCC_MANIFEST)
                .where(
                        and(BCC_MANIFEST.TO_BCCP_MANIFEST_ID.eq(ULong.valueOf(ccId.getManifestId())),
                                BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId()))
                )
                .fetchOneInto(BccManifestRecord.class);


        if (!bccManifestRecord.getFromAccManifestId().equals(accManifestRecord.getAccManifestId())) {
            throw new IllegalArgumentException("It only allows to modify the core component for the corresponding component.");
        }

        return bccManifestRecord;
    }

    public CancelRevisionAccRepositoryResponse cancelRevisionAcc(CancelRevisionAccRepositoryRequest request) {
        ULong userId = ULong.valueOf(sessionService.userId(request.getUser()));
        LocalDateTime timestamp = request.getLocalDateTime();

        AccManifestRecord accManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(request.getAccManifestId()))).fetchOne();

        if (accManifestRecord == null) {
            throw new IllegalArgumentException("Not found a target ACC");
        }

        AccRecord accRecord = dslContext.selectFrom(ACC)
                .where(ACC.ACC_ID.eq(accManifestRecord.getAccId())).fetchOne();

        AccRecord prevAccRecord = dslContext.selectFrom(ACC)
                .where(ACC.ACC_ID.eq(accRecord.getPrevAccId())).fetchOne();

        if (prevAccRecord == null) {
            throw new IllegalArgumentException("Not found previous revision");
        }

        // update ACC MANIFEST's acc_id and revision_id
        if (prevAccRecord.getBasedAccId() != null) {
            String prevBasedAccGuid = dslContext.select(ACC.GUID)
                    .from(ACC).where(ACC.ACC_ID.eq(prevAccRecord.getBasedAccId())).fetchOneInto(String.class);
            AccManifestRecord basedAccManifest = dslContext.select(ACC_MANIFEST.fields()).from(ACC)
                    .join(ACC_MANIFEST).on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                    .where(and(ACC_MANIFEST.RELEASE_ID.eq(accManifestRecord.getReleaseId()),
                            ACC.GUID.eq(prevBasedAccGuid))).fetchOneInto(AccManifestRecord.class);
            accManifestRecord.setBasedAccManifestId(basedAccManifest.getAccManifestId());
        }
        accManifestRecord.setAccId(accRecord.getPrevAccId());
        accManifestRecord.update(ACC_MANIFEST.ACC_ID, ACC_MANIFEST.BASED_ACC_MANIFEST_ID);

        discardLogAssociations(request.getUser(), accManifestRecord, accRecord);

        // unlink prev ACC
        prevAccRecord.setNextAccId(null);
        prevAccRecord.update(ACC.NEXT_ACC_ID);

        // clean logs up
        logRepository.revertToStableState(accManifestRecord);

        // delete current ACC
        accRecord.delete();

        return new CancelRevisionAccRepositoryResponse(request.getAccManifestId());
    }

    private void discardLogAssociations(AuthenticatedPrincipal user, AccManifestRecord accManifestRecord, AccRecord accRecord) {
        AccManifestRecord prevAccManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(accManifestRecord.getPrevAccManifestId())).fetchOne();

        List<AsccManifestRecord> asccManifestRecords = dslContext.selectFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId())).fetch();

        List<BccManifestRecord> bccManifestRecords = dslContext.selectFrom(BCC_MANIFEST)
                .where(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId())).fetch();

        List<AsccManifestRecord> nullNextPrevAsccManifestRecords = Collections.emptyList();
        List<BccManifestRecord> nullNextPrevBccManifestRecords = Collections.emptyList();

        if (prevAccManifestRecord != null) {
            nullNextPrevAsccManifestRecords = dslContext.selectFrom(ASCC_MANIFEST)
                    .where(and(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(prevAccManifestRecord.getAccManifestId()),
                            ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID.isNull())).fetch();
            nullNextPrevBccManifestRecords = dslContext.selectFrom(BCC_MANIFEST)
                    .where(and(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(prevAccManifestRecord.getAccManifestId()),
                            BCC_MANIFEST.NEXT_BCC_MANIFEST_ID.isNull())).fetch();
        }

        // delete SEQ_KEY for current ACC
        dslContext.update(SEQ_KEY)
                .setNull(SEQ_KEY.PREV_SEQ_KEY_ID)
                .setNull(SEQ_KEY.NEXT_SEQ_KEY_ID)
                .where(SEQ_KEY.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId())).execute();
        dslContext.update(ASCC_MANIFEST)
                .setNull(ASCC_MANIFEST.SEQ_KEY_ID)
                .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId())).execute();
        dslContext.update(BCC_MANIFEST)
                .setNull(BCC_MANIFEST.SEQ_KEY_ID)
                .where(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId())).execute();
        dslContext.deleteFrom(SEQ_KEY).where(SEQ_KEY.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId())).execute();

        for (AsccManifestRecord asccManifestRecord : asccManifestRecords) {
            AsccRecord asccRecord = dslContext.selectFrom(ASCC)
                    .where(ASCC.ASCC_ID.eq(asccManifestRecord.getAsccId())).fetchOne();

            if (asccRecord.getPrevAsccId() == null) {
                // delete ascc and ascc manifest which added this revision
                asccManifestRecord.delete();
                asccRecord.delete();
            } else {
                // delete ascc and update ascc manifest
                AsccRecord prevAsccRecord = dslContext.selectFrom(ASCC)
                        .where(ASCC.ASCC_ID.eq(asccRecord.getPrevAsccId())).fetchOne();
                prevAsccRecord.setNextAsccId(null);
                prevAsccRecord.update(ASCC.NEXT_ASCC_ID);
                asccManifestRecord.setAsccId(prevAsccRecord.getAsccId());
                asccManifestRecord.setSeqKeyId(null);
                asccManifestRecord.update(ASCC_MANIFEST.ASCC_ID, ASCC_MANIFEST.SEQ_KEY_ID);
                asccRecord.delete();
            }
        }

        for (AsccManifestRecord asccManifestRecord : nullNextPrevAsccManifestRecords) {
            AsccManifestRecord newAsccManifestRecord = new AsccManifestRecord();
            newAsccManifestRecord.setAsccId(asccManifestRecord.getAsccId());
            newAsccManifestRecord.setReleaseId(accManifestRecord.getReleaseId());
            newAsccManifestRecord.setFromAccManifestId(accManifestRecord.getAccManifestId());
            ULong toAsccpManifestId = dslContext.select(ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID)
                    .from(ASCCP_MANIFEST)
                    .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(asccManifestRecord.getToAsccpManifestId()))
                    .fetchOneInto(ULong.class);
            newAsccManifestRecord.setToAsccpManifestId(toAsccpManifestId);
            String toAsccpDen = dslContext.select(ASCCP_MANIFEST.DEN)
                    .from(ASCCP_MANIFEST)
                    .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(toAsccpManifestId))
                    .fetchOneInto(String.class);
            newAsccManifestRecord.setDen(accRecord.getObjectClassTerm() + ". " + toAsccpDen);
            newAsccManifestRecord.setPrevAsccManifestId(asccManifestRecord.getAsccManifestId());
            dslContext.insertInto(ASCC_MANIFEST).set(newAsccManifestRecord).execute();
        }

        for (BccManifestRecord bccManifestRecord : bccManifestRecords) {
            BccRecord bccRecord = dslContext.selectFrom(BCC)
                    .where(BCC.BCC_ID.eq(bccManifestRecord.getBccId())).fetchOne();

            if (bccRecord.getPrevBccId() == null) {
                // delete bcc and bcc manifest which added this revision
                bccManifestRecord.delete();
                bccRecord.delete();
            } else {
                // delete bcc and update bcc manifest
                BccRecord prevBccRecord = dslContext.selectFrom(BCC)
                        .where(BCC.BCC_ID.eq(bccRecord.getPrevBccId())).fetchOne();
                prevBccRecord.setNextBccId(null);
                prevBccRecord.update(BCC.NEXT_BCC_ID);
                bccManifestRecord.setBccId(prevBccRecord.getBccId());
                bccManifestRecord.setSeqKeyId(null);
                bccManifestRecord.update(BCC_MANIFEST.BCC_ID, BCC_MANIFEST.SEQ_KEY_ID);
                bccRecord.delete();
            }
        }

        for (BccManifestRecord bccManifestRecord : nullNextPrevBccManifestRecords) {
            BccManifestRecord newBccManifestRecord = new BccManifestRecord();
            newBccManifestRecord.setBccId(bccManifestRecord.getBccId());
            newBccManifestRecord.setReleaseId(accManifestRecord.getReleaseId());
            newBccManifestRecord.setFromAccManifestId(accManifestRecord.getAccManifestId());
            ULong toBccpManifestId = dslContext.select(BCCP_MANIFEST.NEXT_BCCP_MANIFEST_ID)
                    .from(BCCP_MANIFEST)
                    .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(bccManifestRecord.getToBccpManifestId()))
                    .fetchOneInto(ULong.class);
            newBccManifestRecord.setToBccpManifestId(toBccpManifestId);
            String toBccpDen = dslContext.select(BCCP_MANIFEST.DEN)
                    .from(BCCP_MANIFEST)
                    .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(toBccpManifestId))
                    .fetchOneInto(String.class);
            newBccManifestRecord.setDen(accRecord.getObjectClassTerm() + ". " + toBccpDen);
            newBccManifestRecord.setPrevBccManifestId(bccManifestRecord.getBccManifestId());
            dslContext.insertInto(BCC_MANIFEST).set(newBccManifestRecord).execute();
        }

        // update ACCs which using with based current ACC
        dslContext.update(ACC)
                .set(ACC.BASED_ACC_ID, accRecord.getPrevAccId())
                .where(ACC.BASED_ACC_ID.eq(accRecord.getAccId()))
                .execute();

        // update ASCCPs which using with role of current ACC
        dslContext.update(ASCCP)
                .set(ASCCP.ROLE_OF_ACC_ID, accRecord.getPrevAccId())
                .where(ASCCP.ROLE_OF_ACC_ID.eq(accRecord.getAccId()))
                .execute();

        insertSeqKey(user, accManifestRecord.getAccManifestId(), accRecord.getGuid());
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

    public void insertSeqKey(AuthenticatedPrincipal user, ULong fromAccManifestId, String reference) {

        HashMap<String, Association> associationMap = new HashMap<>();
        dslContext.select(ASCC.GUID, ASCC_MANIFEST.ASCC_MANIFEST_ID)
                .from(ASCC_MANIFEST)
                .join(ASCC).on(ASCC_MANIFEST.ASCC_ID.eq(ASCC.ASCC_ID))
                .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(fromAccManifestId)).fetch()
                .forEach(e -> {
                    associationMap.put(e.get(ASCC.GUID),
                            new Association(SeqKeyType.ASCC, e.get(ASCC_MANIFEST.ASCC_MANIFEST_ID)));
                });

        dslContext.select(BCC.GUID, BCC_MANIFEST.BCC_MANIFEST_ID)
                .from(BCC_MANIFEST)
                .join(BCC).on(BCC_MANIFEST.BCC_ID.eq(BCC.BCC_ID))
                .where(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(fromAccManifestId)).fetch()
                .forEach(e -> {
                    associationMap.put(e.get(BCC.GUID),
                            new Association(SeqKeyType.BCC, e.get(BCC_MANIFEST.BCC_MANIFEST_ID)));
                });

        LogRecord log = dslContext.selectFrom(LOG)
                .where(and(LOG.REFERENCE.eq(reference), LOG.LOG_ACTION.eq(LogAction.Revised.name())))
                .orderBy(LOG.LOG_ID.desc())
                .limit(1).fetchOne();

        SeqKeyWriteRepository seqKeyWriteRepository = scoreRepositoryFactory.createSeqKeyWriteRepository();

        JsonObject snapshot = serializer.deserialize(log.getSnapshot().toString());
        JsonArray associations = snapshot.get("associations").getAsJsonArray();
        SeqKey prev = null;
        for (JsonElement obj : associations) {
            String guid = obj.getAsJsonObject().get("guid").getAsString();
            Association association = associationMap.get(guid);
            if (association == null) {
                return;
            }
            CreateSeqKeyRequest request = new CreateSeqKeyRequest(sessionService.asScoreUser(user));
            request.setFromAccManifestId(fromAccManifestId.toBigInteger());
            request.setType(association.type);
            request.setManifestId(association.getManifestId().toBigInteger());
            SeqKey current = seqKeyWriteRepository.createSeqKey(request).getSeqKey();
            if (prev != null) {
                MoveAfterRequest moveAfterRequest = new MoveAfterRequest(sessionService.asScoreUser(user));
                moveAfterRequest.setAfter(prev);
                moveAfterRequest.setItem(current);
                seqKeyWriteRepository.moveAfter(moveAfterRequest);
            }
            prev = current;
        }
    }

    public CancelRevisionAccRepositoryResponse resetRevisionAcc(CancelRevisionAccRepositoryRequest request) {
        AccManifestRecord accManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(request.getAccManifestId()))).fetchOne();

        if (accManifestRecord == null) {
            throw new IllegalArgumentException("Not found a target ACC");
        }

        AccRecord accRecord = dslContext.selectFrom(ACC)
                .where(ACC.ACC_ID.eq(accManifestRecord.getAccId())).fetchOne();

        LogRecord cursorLog = dslContext.selectFrom(LOG)
                .where(LOG.LOG_ID.eq(accManifestRecord.getLogId())).fetchOne();

        UInteger revisionNum = cursorLog.getRevisionNum();

        if (cursorLog.getPrevLogId() == null) {
            throw new IllegalArgumentException("There is no change to be reset.");
        }

        List<ULong> deleteLogTargets = new ArrayList<>();

        while (cursorLog.getPrevLogId() != null) {
            if (!cursorLog.getRevisionNum().equals(revisionNum)) {
                throw new IllegalArgumentException("Cannot find reset point");
            }
            if (cursorLog.getRevisionTrackingNum().equals(UInteger.valueOf(1))) {
                break;
            }
            deleteLogTargets.add(cursorLog.getLogId());
            cursorLog = dslContext.selectFrom(LOG)
                    .where(LOG.LOG_ID.eq(cursorLog.getPrevLogId())).fetchOne();
        }

        JsonObject snapshot = serializer.deserialize(cursorLog.getSnapshot().toString());

        ULong basedAccId = serializer.getSnapshotId(snapshot.get("basedAccId"));

        if (basedAccId != null) {
            AccManifestRecord basedAccManifestRecord = dslContext.selectFrom(ACC_MANIFEST).where(and(
                    ACC_MANIFEST.ACC_ID.eq(basedAccId),
                    ACC_MANIFEST.RELEASE_ID.eq(accManifestRecord.getReleaseId())
            )).fetchOne();

            if (basedAccManifestRecord == null) {
                throw new IllegalArgumentException("Not found based ACC.");
            }

            accManifestRecord.setBasedAccManifestId(basedAccManifestRecord.getAccManifestId());
            accRecord.setBasedAccId(basedAccManifestRecord.getAccId());
        } else {
            accManifestRecord.setBasedAccManifestId(null);
            accRecord.setBasedAccId(null);
        }
        accManifestRecord.setDen(accRecord.getObjectClassTerm() + ". Details");
        accManifestRecord.setLogId(cursorLog.getLogId());
        accManifestRecord.update(ACC_MANIFEST.BASED_ACC_MANIFEST_ID, ACC_MANIFEST.DEN, ACC_MANIFEST.LOG_ID);

        accRecord.setObjectClassTerm(serializer.getSnapshotString(snapshot.get("objectClassTerm")));
        accRecord.setDefinition(serializer.getSnapshotString(snapshot.get("definition")));
        accRecord.setDefinitionSource(serializer.getSnapshotString(snapshot.get("definitionSource")));
        accRecord.setOagisComponentType(OagisComponentType.valueOf(
                serializer.getSnapshotString(snapshot.get("componentType"))).getValue());
        accRecord.setNamespaceId(serializer.getSnapshotId(snapshot.get("namespaceId")));
        accRecord.setIsDeprecated(serializer.getSnapshotByte(snapshot.get("deprecated")));
        accRecord.setIsAbstract(serializer.getSnapshotByte(snapshot.get("abstract")));
        accRecord.update();

        resetAssociations(snapshot.get("associations"), accManifestRecord);

        cursorLog.setNextLogId(null);
        cursorLog.update(LOG.NEXT_LOG_ID);
        dslContext.update(LOG)
                .setNull(LOG.PREV_LOG_ID)
                .setNull(LOG.NEXT_LOG_ID)
                .where(LOG.LOG_ID.in(deleteLogTargets))
                .execute();
        dslContext.deleteFrom(LOG).where(LOG.LOG_ID.in(deleteLogTargets)).execute();

        return new CancelRevisionAccRepositoryResponse(request.getAccManifestId());
    }

    private void resetAssociations(JsonElement associationElement, AccManifestRecord accManifestRecord) {
        JsonArray associations = associationElement.getAsJsonArray();
        int associationCount = associations.size();

        List<AsccManifestRecord> asccManifestRecords = dslContext.selectFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId())).fetch();

        List<BccManifestRecord> bccManifestRecords = dslContext.selectFrom(BCC_MANIFEST)
                .where(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestRecord.getAccManifestId())).fetch();

        List<JsonObject> associationObjects = IntStream.range(0, associationCount)
                .mapToObj(i -> associations.get(i).getAsJsonObject())
                .collect(Collectors.toList());
        for (AsccManifestRecord asccManifestRecord : asccManifestRecords) {
            AsccRecord asccRecord = dslContext.selectFrom(ASCC)
                    .where(ASCC.ASCC_ID.eq(asccManifestRecord.getAsccId())).fetchOne();
            JsonObject asccObject = associationObjects.stream().filter(o ->
                    serializer.getSnapshotString(o.get("component")).equals("ascc")
                            && serializer.getSnapshotString(o.get("guid")).equals(asccRecord.getGuid())
            ).findFirst().orElse(null);

            if (asccObject == null) {
                asccRecord.setState(CcState.Deleted.name());
            } else {
                asccRecord.setCardinalityMin(asccObject.get("cardinalityMin").getAsInt());
                asccRecord.setCardinalityMax(asccObject.get("cardinalityMax").getAsInt());
                asccRecord.setIsDeprecated(serializer.getSnapshotByte(asccObject.get("deprecated")));
                asccRecord.setDefinition(serializer.getSnapshotString(asccObject.get("definition")));
                asccRecord.setDefinitionSource(serializer.getSnapshotString(asccObject.get("definitionSource")));
            }
            asccRecord.update();
        }

        for (BccManifestRecord bccManifestRecord : bccManifestRecords) {
            BccRecord bccRecord = dslContext.selectFrom(BCC)
                    .where(BCC.BCC_ID.eq(bccManifestRecord.getBccId())).fetchOne();
            JsonObject bccObject = associationObjects.stream().filter(o ->
                    serializer.getSnapshotString(o.get("component")).equals("bcc")
                            && serializer.getSnapshotString(o.get("guid")).equals(bccRecord.getGuid())
            ).findFirst().orElse(null);

            if (bccObject == null) {
                bccRecord.setState(CcState.Deleted.name());
            } else {
                bccRecord.setCardinalityMin(bccObject.get("cardinalityMin").getAsInt());
                bccRecord.setCardinalityMax(bccObject.get("cardinalityMax").getAsInt());
                bccRecord.setIsDeprecated(serializer.getSnapshotByte(bccObject.get("deprecated")));
                bccRecord.setDefinition(serializer.getSnapshotString(bccObject.get("definition")));
                bccRecord.setDefinitionSource(serializer.getSnapshotString(bccObject.get("definitionSource")));
                bccRecord.setEntityType(BCCEntityType.valueOf(
                        serializer.getSnapshotString(bccObject.get("entityType"))).getValue());
                bccRecord.setDefaultValue(serializer.getSnapshotString(bccObject.get("defaultValue")));
                bccRecord.setFixedValue(serializer.getSnapshotString(bccObject.get("fixedValue")));
            }
            bccRecord.update();
        }
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

    private SeqKeyHandler seqKeyHandler(AuthenticatedPrincipal user, BccManifestRecord asccManifestRecord) {
        SeqKeyHandler seqKeyHandler = new SeqKeyHandler(scoreRepositoryFactory,
                sessionService.asScoreUser(user));
        seqKeyHandler.initBcc(
                asccManifestRecord.getFromAccManifestId().toBigInteger(),
                (asccManifestRecord.getSeqKeyId() != null) ? asccManifestRecord.getSeqKeyId().toBigInteger() : null,
                asccManifestRecord.getBccManifestId().toBigInteger());
        return seqKeyHandler;
    }

}