package org.oagi.score.repo.component.asccp;

import com.google.gson.JsonObject;
import org.jooq.DSLContext;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.component.ascc.AsccWriteRepository;
import org.oagi.score.repo.component.ascc.UpdateAsccPropertiesRepositoryRequest;
import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.service.log.LogRepository;
import org.oagi.score.service.log.model.LogAction;
import org.oagi.score.service.log.model.LogSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.compare;
import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.Acc.ACC;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.AccManifest.ACC_MANIFEST;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.Asccp.ASCCP;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.AsccpManifest.ASCCP_MANIFEST;

@Repository
public class AsccpWriteRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private AsccWriteRepository asccWriteRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private LogSerializer serializer;

    private String objectClassTerm(ULong accManifestId) {
        return dslContext.select(ACC.OBJECT_CLASS_TERM).from(ACC)
                .join(ACC_MANIFEST).on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(accManifestId))
                .fetchOneInto(String.class);
    }

    public CreateAsccpRepositoryResponse createAsccp(CreateAsccpRepositoryRequest request) {
        ULong userId = ULong.valueOf(sessionService.userId(request.getUser()));
        LocalDateTime timestamp = request.getLocalDateTime();

        AccManifestRecord roleOfAccManifest = dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(request.getRoleOfAccManifestId())))
                .fetchOne();

        AccRecord roleOfAccRecord = dslContext.selectFrom(ACC)
                .where(ACC.ACC_ID.eq(roleOfAccManifest.getAccId()))
                .fetchOne();
        if (roleOfAccRecord.getIsAbstract() == 1) {
            throw new IllegalArgumentException("An abstract ACC cannot be used to create a new ASCCP.");
        }

        AsccpRecord asccp = new AsccpRecord();
        asccp.setGuid(ScoreGuid.randomGuid());
        asccp.setPropertyTerm(request.getInitialPropertyTerm());
        asccp.setRoleOfAccId(roleOfAccManifest.getAccId());
        asccp.setState(request.getInitialState().name());
        asccp.setDefinition(request.getDefinition());
        asccp.setDefinitionSource(request.getDefinitionSource());
        asccp.setReusableIndicator((byte) (request.isReusable() ? 1 : 0));
        asccp.setIsDeprecated((byte) 0);
        asccp.setIsNillable((byte) 0);
        asccp.setType(request.getInitialType().name());
        if (request.getNamespaceId() != null) {
            asccp.setNamespaceId(ULong.valueOf(request.getNamespaceId()));
        }
        asccp.setCreatedBy(userId);
        asccp.setLastUpdatedBy(userId);
        asccp.setOwnerUserId(userId);
        asccp.setCreationTimestamp(timestamp);
        asccp.setLastUpdateTimestamp(timestamp);

        asccp.setAsccpId(
                dslContext.insertInto(ASCCP)
                        .set(asccp)
                        .returning(ASCCP.ASCCP_ID).fetchOne().getAsccpId()
        );

        AsccpManifestRecord asccpManifest = new AsccpManifestRecord();
        asccpManifest.setAsccpId(asccp.getAsccpId());
        asccpManifest.setRoleOfAccManifestId(roleOfAccManifest.getAccManifestId());
        asccpManifest.setReleaseId(ULong.valueOf(request.getReleaseId()));
        asccpManifest.setDen(asccp.getPropertyTerm() + ". " + objectClassTerm(roleOfAccManifest.getAccManifestId()));
        asccpManifest = dslContext.insertInto(ASCCP_MANIFEST)
                .set(asccpManifest)
                .returning(ASCCP_MANIFEST.ASCCP_MANIFEST_ID).fetchOne();

        LogRecord logRecord =
                logRepository.insertAsccpLog(
                        asccpManifest,
                        asccp,
                        LogAction.Added,
                        userId, timestamp);
        asccpManifest.setLogId(logRecord.getLogId());
        asccpManifest.update(ASCCP_MANIFEST.LOG_ID);

        if (StringUtils.hasLength(request.getTag())) {
            ULong asccpManifestId = asccpManifest.getAsccpManifestId();
            dslContext.selectFrom(TAG)
                    .where(TAG.NAME.eq(request.getTag()))
                    .fetchOptionalInto(TagRecord.class)
                    .ifPresent(tagRecord -> {
                        AsccpManifestTagRecord asccpManifestTagRecord = new AsccpManifestTagRecord();
                        asccpManifestTagRecord.setAsccpManifestId(asccpManifestId);
                        asccpManifestTagRecord.setTagId(tagRecord.getTagId());
                        asccpManifestTagRecord.setCreatedBy(userId);
                        asccpManifestTagRecord.setCreationTimestamp(timestamp);
                        dslContext.insertInto(ASCCP_MANIFEST_TAG)
                                .set(asccpManifestTagRecord)
                                .execute();
                    });
        }

        return new CreateAsccpRepositoryResponse(asccpManifest.getAsccpManifestId().toBigInteger());
    }

    public ReviseAsccpRepositoryResponse reviseAsccp(ReviseAsccpRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        AsccpManifestRecord asccpManifestRecord = dslContext.selectFrom(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(
                        ULong.valueOf(request.getAsccpManifestId())
                ))
                .fetchOne();

        AsccpRecord prevAsccpRecord = dslContext.selectFrom(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(asccpManifestRecord.getAsccpId()))
                .fetchOne();

        if (user.isDeveloper()) {
            if (!CcState.Published.equals(CcState.valueOf(prevAsccpRecord.getState()))) {
                throw new IllegalArgumentException("Only the core component in 'Published' state can be revised.");
            }
        } else {
            if (!CcState.Production.equals(CcState.valueOf(prevAsccpRecord.getState()))) {
                throw new IllegalArgumentException("Only the core component in 'Production' state can be revised.");
            }
        }

        ULong workingReleaseId = dslContext.select(RELEASE.RELEASE_ID)
                .from(RELEASE)
                .where(RELEASE.RELEASE_NUM.eq("Working"))
                .fetchOneInto(ULong.class);

        ULong targetReleaseId = asccpManifestRecord.getReleaseId();
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
                .where(APP_USER.APP_USER_ID.eq(prevAsccpRecord.getOwnerUserId()))
                .fetchOneInto(Boolean.class);

        if (user.isDeveloper() != ownerIsDeveloper) {
            throw new IllegalArgumentException("It only allows to revise the component for users in the same roles.");
        }

        // creates new asccp for revised record.
        AsccpRecord nextAsccpRecord = prevAsccpRecord.copy();
        nextAsccpRecord.setState(CcState.WIP.name());
        nextAsccpRecord.setCreatedBy(userId);
        nextAsccpRecord.setLastUpdatedBy(userId);
        nextAsccpRecord.setOwnerUserId(userId);
        nextAsccpRecord.setCreationTimestamp(timestamp);
        nextAsccpRecord.setLastUpdateTimestamp(timestamp);
        nextAsccpRecord.setPrevAsccpId(prevAsccpRecord.getAsccpId());
        nextAsccpRecord.setAsccpId(
                dslContext.insertInto(ASCCP)
                        .set(nextAsccpRecord)
                        .returning(ASCCP.ASCCP_ID).fetchOne().getAsccpId()
        );

        prevAsccpRecord.setNextAsccpId(nextAsccpRecord.getAsccpId());
        prevAsccpRecord.update(ASCCP.NEXT_ASCCP_ID);

        // creates new log for revised record.
        LogRecord logRecord =
                logRepository.insertAsccpLog(
                        asccpManifestRecord,
                        nextAsccpRecord, asccpManifestRecord.getLogId(),
                        LogAction.Revised,
                        userId, timestamp);

        ULong responseAsccpManifestId;

        asccpManifestRecord.setAsccpId(nextAsccpRecord.getAsccpId());
        asccpManifestRecord.setLogId(logRecord.getLogId());
        asccpManifestRecord.update(ASCCP_MANIFEST.ASCCP_ID, ASCCP_MANIFEST.LOG_ID);

        responseAsccpManifestId = asccpManifestRecord.getAsccpManifestId();

        // update `conflict` for ascc_manifests' to_asccp_manifest_id which indicates given asccp manifest.
        dslContext.update(ASCC_MANIFEST)
                .set(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID, responseAsccpManifestId)
                .set(ASCC_MANIFEST.CONFLICT, (byte) 1)
                .where(and(
                        ASCC_MANIFEST.RELEASE_ID.eq(targetReleaseId),
                        ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.in(Arrays.asList(
                                asccpManifestRecord.getAsccpManifestId(),
                                asccpManifestRecord.getPrevAsccpManifestId()))
                ))
                .execute();

        return new ReviseAsccpRepositoryResponse(responseAsccpManifestId.toBigInteger());
    }

    public UpdateAsccpPropertiesRepositoryResponse updateAsccpProperties(UpdateAsccpPropertiesRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        AsccpManifestRecord asccpManifestRecord = dslContext.selectFrom(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(
                        ULong.valueOf(request.getAsccpManifestId())
                ))
                .fetchOne();

        AsccpRecord asccpRecord = dslContext.selectFrom(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(asccpManifestRecord.getAsccpId()))
                .fetchOne();

        if (!CcState.WIP.equals(CcState.valueOf(asccpRecord.getState()))) {
            throw new IllegalArgumentException("Only the core component in 'WIP' state can be modified.");
        }

        if (!asccpRecord.getOwnerUserId().equals(userId)) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        // update asccp record.
        UpdateSetFirstStep<AsccpRecord> firstStep = dslContext.update(ASCCP);
        UpdateSetMoreStep<AsccpRecord> moreStep = null;
        boolean propertyTermChanged = false;
        if (compare(asccpRecord.getPropertyTerm(), request.getPropertyTerm()) != 0) {
            propertyTermChanged = true;
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ASCCP.PROPERTY_TERM, request.getPropertyTerm());

            String den = request.getPropertyTerm() + ". " + objectClassTerm(asccpManifestRecord.getRoleOfAccManifestId());
            asccpManifestRecord.setDen(den);
            dslContext.update(ASCCP_MANIFEST)
                    .set(ASCCP_MANIFEST.DEN, den)
                    .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(asccpManifestRecord.getAsccpManifestId()))
                    .execute();
        }
        if (compare(asccpRecord.getDefinition(), request.getDefinition()) != 0) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ASCCP.DEFINITION, request.getDefinition());
        }
        if (compare(asccpRecord.getDefinitionSource(), request.getDefinitionSource()) != 0) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ASCCP.DEFINITION_SOURCE, request.getDefinitionSource());
        }
        if ((asccpRecord.getReusableIndicator() == 1) != request.isReusable()) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ASCCP.REUSABLE_INDICATOR, (byte) ((request.isReusable()) ? 1 : 0));
        }
        if ((asccpRecord.getIsDeprecated() == 1) != request.isDeprecated()) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ASCCP.IS_DEPRECATED, (byte) ((request.isDeprecated()) ? 1 : 0));
        }
        if ((asccpRecord.getIsNillable() == 1) != request.isNillable()) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ASCCP.IS_NILLABLE, (byte) ((request.isNillable()) ? 1 : 0));
        }
        if (request.getNamespaceId() == null || request.getNamespaceId().longValue() <= 0L) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .setNull(ASCCP.NAMESPACE_ID);
        } else {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ASCCP.NAMESPACE_ID, ULong.valueOf(request.getNamespaceId()));
        }

        if (moreStep != null) {
            moreStep.set(ASCCP.LAST_UPDATED_BY, userId)
                    .set(ASCCP.LAST_UPDATE_TIMESTAMP, timestamp)
                    .where(ASCCP.ASCCP_ID.eq(asccpRecord.getAsccpId()))
                    .execute();

            asccpRecord = dslContext.selectFrom(ASCCP)
                    .where(ASCCP.ASCCP_ID.eq(asccpManifestRecord.getAsccpId()))
                    .fetchOne();

            // creates new log for updated record.
            LogRecord logRecord =
                    logRepository.insertAsccpLog(
                            asccpManifestRecord,
                            asccpRecord, asccpManifestRecord.getLogId(),
                            LogAction.Modified,
                            userId, timestamp);

            asccpManifestRecord.setLogId(logRecord.getLogId());
            asccpManifestRecord.update(ASCCP_MANIFEST.LOG_ID);
        }

        if (propertyTermChanged) {
            for (ULong asccManifestId : dslContext.select(ASCC_MANIFEST.ASCC_MANIFEST_ID)
                    .from(ASCC_MANIFEST)
                    .where(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(asccpManifestRecord.getAsccpManifestId()))
                    .fetchInto(ULong.class)) {

                UpdateAsccPropertiesRepositoryRequest updateAsccPropertiesRepositoryRequest =
                        new UpdateAsccPropertiesRepositoryRequest(request.getUser(), request.getLocalDateTime(),
                                asccManifestId.toBigInteger());
                updateAsccPropertiesRepositoryRequest.setPropagation(true);
                asccWriteRepository.updateAsccProperties(updateAsccPropertiesRepositoryRequest);
            }
        }

        return new UpdateAsccpPropertiesRepositoryResponse(asccpManifestRecord.getAsccpManifestId().toBigInteger());
    }

    public UpdateAsccpPropertiesRepositoryResponse updateAsccpNamespace(UpdateAsccpPropertiesRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        AsccpManifestRecord asccpManifestRecord = dslContext.selectFrom(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(
                        ULong.valueOf(request.getAsccpManifestId())
                ))
                .fetchOne();

        AsccpRecord asccpRecord = dslContext.selectFrom(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(asccpManifestRecord.getAsccpId()))
                .fetchOne();

        // update asccp record.
        UpdateSetFirstStep<AsccpRecord> firstStep = dslContext.update(ASCCP);
        UpdateSetMoreStep<AsccpRecord> moreStep = null;
        if (request.getNamespaceId() == null || request.getNamespaceId().longValue() <= 0L) {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .setNull(ASCCP.NAMESPACE_ID);
        } else {
            moreStep = ((moreStep != null) ? moreStep : firstStep)
                    .set(ASCCP.NAMESPACE_ID, ULong.valueOf(request.getNamespaceId()));
        }

        if (moreStep != null) {
            moreStep.set(ASCCP.LAST_UPDATED_BY, userId)
                    .set(ASCCP.LAST_UPDATE_TIMESTAMP, timestamp)
                    .where(ASCCP.ASCCP_ID.eq(asccpRecord.getAsccpId()))
                    .execute();

            asccpRecord = dslContext.selectFrom(ASCCP)
                    .where(ASCCP.ASCCP_ID.eq(asccpManifestRecord.getAsccpId()))
                    .fetchOne();
        }

        // creates new log for updated record.
        LogRecord logRecord =
                logRepository.insertAsccpLog(
                        asccpManifestRecord,
                        asccpRecord, asccpManifestRecord.getLogId(),
                        LogAction.Modified,
                        userId, timestamp);

        asccpManifestRecord.setLogId(logRecord.getLogId());
        asccpManifestRecord.update(ASCCP_MANIFEST.LOG_ID);

        return new UpdateAsccpPropertiesRepositoryResponse(asccpManifestRecord.getAsccpManifestId().toBigInteger());
    }

    public UpdateAsccpRoleOfAccRepositoryResponse updateAsccpBdt(UpdateAsccpRoleOfAccRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        AsccpManifestRecord asccpManifestRecord = dslContext.selectFrom(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(
                        ULong.valueOf(request.getAsccpManifestId())
                ))
                .fetchOne();

        AsccpRecord asccpRecord = dslContext.selectFrom(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(asccpManifestRecord.getAsccpId()))
                .fetchOne();

        if (!CcState.WIP.equals(CcState.valueOf(asccpRecord.getState()))) {
            throw new IllegalArgumentException("Only the core component in 'WIP' state can be modified.");
        }

        if (!asccpRecord.getOwnerUserId().equals(userId)) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        // update asccp record.
        ULong roleOfAccManifestId = ULong.valueOf(request.getRoleOfAccManifestId());
        ULong roleOfAccId = dslContext.select(ACC_MANIFEST.ACC_ID)
                .from(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(roleOfAccManifestId))
                .fetchOneInto(ULong.class);

        asccpRecord.setRoleOfAccId(roleOfAccId);
        asccpRecord.setLastUpdatedBy(userId);
        asccpRecord.setLastUpdateTimestamp(timestamp);
        asccpRecord.update(ASCCP.ROLE_OF_ACC_ID,
                ASCCP.LAST_UPDATED_BY, ASCCP.LAST_UPDATE_TIMESTAMP);

        asccpManifestRecord.setDen(asccpRecord.getPropertyTerm() + ". " + objectClassTerm(asccpManifestRecord.getRoleOfAccManifestId()));
        asccpManifestRecord.update(ASCCP_MANIFEST.DEN);

        // creates new log for updated record.
        LogRecord logRecord =
                logRepository.insertAsccpLog(
                        asccpManifestRecord,
                        asccpRecord, asccpManifestRecord.getLogId(),
                        LogAction.Modified,
                        userId, timestamp);

        asccpManifestRecord.setRoleOfAccManifestId(roleOfAccManifestId);
        asccpManifestRecord.setLogId(logRecord.getLogId());
        asccpManifestRecord.update(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID, ASCCP_MANIFEST.LOG_ID);

        return new UpdateAsccpRoleOfAccRepositoryResponse(asccpManifestRecord.getAsccpManifestId().toBigInteger(),
                asccpManifestRecord.getDen());
    }

    public UpdateAsccpStateRepositoryResponse updateAsccpState(UpdateAsccpStateRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        AsccpManifestRecord asccpManifestRecord = dslContext.selectFrom(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(
                        ULong.valueOf(request.getAsccpManifestId())
                ))
                .fetchOne();

        AsccpRecord asccpRecord = dslContext.selectFrom(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(asccpManifestRecord.getAsccpId()))
                .fetchOne();

        CcState prevState = CcState.valueOf(asccpRecord.getState());
        CcState nextState = request.getToState();

        if (prevState != request.getFromState()) {
            throw new IllegalArgumentException("Target core component is not in '" + request.getFromState() + "' state.");
        }

        if (!prevState.canMove(nextState)) {
            throw new IllegalArgumentException("The core component in '" + prevState + "' state cannot move to '" + nextState + "' state.");
        }

        // Change owner of CC when it restored.
        if (prevState == CcState.Deleted && nextState == CcState.WIP) {
            asccpRecord.setOwnerUserId(userId);
        } else if (prevState != CcState.Deleted && !asccpRecord.getOwnerUserId().equals(userId)
                && !prevState.canForceMove(request.getToState())) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        } else if (asccpRecord.getNamespaceId() == null) {
            throw new IllegalArgumentException("'" + asccpManifestRecord.getDen() + "' dose not have NamespaceId.");
        }

        // update asccp state.
        asccpRecord.setState(nextState.name());
        if (!prevState.canForceMove(request.getToState())) {
            asccpRecord.setLastUpdatedBy(userId);
            asccpRecord.setLastUpdateTimestamp(timestamp);
        }
        asccpRecord.update(ASCCP.STATE,
                ASCCP.LAST_UPDATED_BY, ASCCP.LAST_UPDATE_TIMESTAMP, ASCCP.OWNER_USER_ID);

        // creates new log for updated record.
        LogAction logAction = (CcState.Deleted == prevState && CcState.WIP == nextState)
                ? LogAction.Restored : LogAction.Modified;
        LogRecord logRecord =
                logRepository.insertAsccpLog(
                        asccpManifestRecord,
                        asccpRecord, asccpManifestRecord.getLogId(),
                        logAction,
                        userId, timestamp);

        asccpManifestRecord.setLogId(logRecord.getLogId());
        asccpManifestRecord.update(ASCCP_MANIFEST.LOG_ID);

        return new UpdateAsccpStateRepositoryResponse(asccpManifestRecord.getAsccpManifestId().toBigInteger());
    }

    public DeleteAsccpRepositoryResponse deleteAsccp(DeleteAsccpRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        AsccpManifestRecord asccpManifestRecord = dslContext.selectFrom(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(
                        ULong.valueOf(request.getAsccpManifestId())
                ))
                .fetchOne();

        AsccpRecord asccpRecord = dslContext.selectFrom(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(asccpManifestRecord.getAsccpId()))
                .fetchOne();

        if (!CcState.WIP.equals(CcState.valueOf(asccpRecord.getState()))) {
            throw new IllegalArgumentException("Only the core component in 'WIP' state can be deleted.");
        }

        if (!asccpRecord.getOwnerUserId().equals(userId)) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        // update asccp state.
        asccpRecord.setState(CcState.Deleted.name());
        asccpRecord.setLastUpdatedBy(userId);
        asccpRecord.setLastUpdateTimestamp(timestamp);
        asccpRecord.update(ASCCP.STATE,
                ASCCP.LAST_UPDATED_BY, ASCCP.LAST_UPDATE_TIMESTAMP);

        // creates new log for deleted record.
        LogRecord logRecord =
                logRepository.insertAsccpLog(
                        asccpManifestRecord,
                        asccpRecord, asccpManifestRecord.getLogId(),
                        LogAction.Deleted,
                        userId, timestamp);

        asccpManifestRecord.setLogId(logRecord.getLogId());
        asccpManifestRecord.update(ASCCP_MANIFEST.LOG_ID);

        return new DeleteAsccpRepositoryResponse(asccpManifestRecord.getAsccpManifestId().toBigInteger());
    }

    public PurgeAsccpRepositoryResponse purgeAsccp(PurgeAsccpRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        AsccpManifestRecord asccpManifestRecord = dslContext.selectFrom(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(
                        ULong.valueOf(request.getAsccpManifestId())
                ))
                .fetchOne();

        AsccpRecord asccpRecord = dslContext.selectFrom(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(asccpManifestRecord.getAsccpId()))
                .fetchOne();

        if (!request.isIgnoreState()) {
            if (!CcState.Deleted.equals(CcState.valueOf(asccpRecord.getState()))) {
                IllegalArgumentException e = new IllegalArgumentException("Only the core component in 'Deleted' state can be purged.");
                if (request.isIgnoreOnError()) {
                    return new PurgeAsccpRepositoryResponse(asccpManifestRecord.getAsccpManifestId().toBigInteger(), e);
                } else {
                    throw e;
                }
            }
        }

        List<AsccManifestRecord> asccManifestRecords = dslContext.selectFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(asccpManifestRecord.getAsccpManifestId()))
                .fetch();
        if (!asccManifestRecords.isEmpty()) {
            IllegalArgumentException e = new IllegalArgumentException("Please purge related-ASCCs first before purging the ASCCP '" + asccpManifestRecord.getDen() + "'.");
            if (request.isIgnoreOnError()) {
                return new PurgeAsccpRepositoryResponse(asccpManifestRecord.getAsccpManifestId().toBigInteger(), e);
            } else {
                throw e;
            }
        }

        // discard Log
        ULong logId = asccpManifestRecord.getLogId();
        dslContext.update(ASCCP_MANIFEST)
                .setNull(ASCCP_MANIFEST.LOG_ID)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(asccpManifestRecord.getAsccpManifestId()))
                .execute();

        dslContext.update(LOG)
                .setNull(LOG.PREV_LOG_ID)
                .setNull(LOG.NEXT_LOG_ID)
                .where(LOG.REFERENCE.eq(asccpRecord.getGuid()))
                .execute();

        dslContext.deleteFrom(LOG)
                .where(LOG.REFERENCE.eq(asccpRecord.getGuid()))
                .execute();

        // discard assigned ASCCP in modules
        dslContext.deleteFrom(MODULE_ASCCP_MANIFEST)
                .where(MODULE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(asccpManifestRecord.getAsccpManifestId()))
                .execute();

        // discard corresponding tags
        dslContext.deleteFrom(ASCCP_MANIFEST_TAG)
                .where(ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID.eq(asccpManifestRecord.getAsccpManifestId()))
                .execute();

        // discard ASCCP
        dslContext.deleteFrom(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(asccpManifestRecord.getAsccpManifestId()))
                .execute();

        dslContext.deleteFrom(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(asccpRecord.getAsccpId()))
                .execute();

        return new PurgeAsccpRepositoryResponse(asccpManifestRecord.getAsccpManifestId().toBigInteger());
    }

    public DeleteAsccpRepositoryResponse removeAsccp(DeleteAsccpRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        AsccpManifestRecord asccpManifestRecord = dslContext.selectFrom(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(
                        ULong.valueOf(request.getAsccpManifestId())
                ))
                .fetchOne();

        AsccpRecord asccpRecord = dslContext.selectFrom(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(asccpManifestRecord.getAsccpId()))
                .fetchOne();

        if (!CcState.WIP.equals(CcState.valueOf(asccpRecord.getState()))) {
            throw new IllegalArgumentException("Only the core component in 'WIP' state can be deleted.");
        }

        if (!asccpRecord.getOwnerUserId().equals(userId)) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        if (dslContext.selectCount()
                .from(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(asccpManifestRecord.getAsccpManifestId()))
                .fetchOneInto(Long.class) == 0) {
            asccpManifestRecord.delete();
        }

        if (dslContext.selectCount()
                .from(ASCC)
                .where(ASCC.TO_ASCCP_ID.eq(asccpRecord.getAsccpId()))
                .fetchOneInto(Long.class) == 0) {
            asccpRecord.delete();
        }

        return new DeleteAsccpRepositoryResponse(asccpManifestRecord.getAsccpManifestId().toBigInteger());
    }

    public UpdateAsccpOwnerRepositoryResponse updateAsccpOwner(UpdateAsccpOwnerRepositoryRequest request) {
        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong userId = ULong.valueOf(user.getAppUserId());
        LocalDateTime timestamp = request.getLocalDateTime();

        AsccpManifestRecord asccpManifestRecord = dslContext.selectFrom(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(
                        ULong.valueOf(request.getAsccpManifestId())
                ))
                .fetchOne();

        AsccpRecord asccpRecord = dslContext.selectFrom(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(asccpManifestRecord.getAsccpId()))
                .fetchOne();

        if (!CcState.WIP.equals(CcState.valueOf(asccpRecord.getState()))) {
            throw new IllegalArgumentException("Only the core component in 'WIP' state can be modified.");
        }

        if (!asccpRecord.getOwnerUserId().equals(userId)) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        asccpRecord.setOwnerUserId(ULong.valueOf(request.getOwnerId()));
        asccpRecord.setLastUpdatedBy(userId);
        asccpRecord.setLastUpdateTimestamp(timestamp);
        asccpRecord.update(ASCCP.OWNER_USER_ID, ASCCP.LAST_UPDATED_BY, ASCCP.LAST_UPDATE_TIMESTAMP);

        LogRecord logRecord =
                logRepository.insertAsccpLog(
                        asccpManifestRecord,
                        asccpRecord, asccpManifestRecord.getLogId(),
                        LogAction.Modified,
                        userId, timestamp);

        asccpManifestRecord.setLogId(logRecord.getLogId());
        asccpManifestRecord.update(ASCCP_MANIFEST.LOG_ID);

        return new UpdateAsccpOwnerRepositoryResponse(asccpManifestRecord.getAsccpManifestId().toBigInteger());
    }

    public CancelRevisionAsccpRepositoryResponse cancelRevisionAsccp(CancelRevisionAsccpRepositoryRequest request) {
        ULong userId = ULong.valueOf(sessionService.userId(request.getUser()));
        LocalDateTime timestamp = request.getLocalDateTime();

        AsccpManifestRecord asccpManifestRecord = dslContext.selectFrom(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(request.getAsccpManifestId()))).fetchOne();

        if (asccpManifestRecord == null) {
            throw new IllegalArgumentException("Not found a target ASCCP");
        }

        AsccpRecord asccpRecord = dslContext.selectFrom(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(asccpManifestRecord.getAsccpId())).fetchOne();

        if (asccpRecord.getPrevAsccpId() == null) {
            throw new IllegalArgumentException("Not found previous log");
        }

        AsccpRecord prevAsccpRecord = dslContext.selectFrom(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(asccpRecord.getPrevAsccpId())).fetchOne();

        // update ASCCP MANIFEST's asccp_id
        if (prevAsccpRecord.getRoleOfAccId() != null) {
            String prevRoleOfAccGuid = dslContext.select(ACC.GUID)
                    .from(ACC).where(ACC.ACC_ID.eq(prevAsccpRecord.getRoleOfAccId())).fetchOneInto(String.class);
            AccManifestRecord roleOfAccManifest = dslContext.select(ACC_MANIFEST.fields()).from(ACC)
                    .join(ACC_MANIFEST).on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                    .where(and(ACC_MANIFEST.RELEASE_ID.eq(asccpManifestRecord.getReleaseId()),
                            ACC.GUID.eq(prevRoleOfAccGuid))).fetchOneInto(AccManifestRecord.class);
            asccpManifestRecord.setRoleOfAccManifestId(roleOfAccManifest.getAccManifestId());
        }
        asccpManifestRecord.setAsccpId(asccpRecord.getPrevAsccpId());
        asccpManifestRecord.update(ASCCP_MANIFEST.ASCCP_ID,
                ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID);

        // update ASCCs which using current ASCCP
        dslContext.update(ASCC)
                .set(ASCC.TO_ASCCP_ID, asccpRecord.getPrevAsccpId())
                .where(ASCC.TO_ASCCP_ID.eq(asccpRecord.getAsccpId()))
                .execute();

        // unlink prev ASCCP
        prevAsccpRecord.setNextAsccpId(null);
        prevAsccpRecord.update(ASCCP.NEXT_ASCCP_ID);

        // clean logs up
        logRepository.revertToStableState(asccpManifestRecord);

        // delete current ASCCP
        asccpRecord.delete();

        return new CancelRevisionAsccpRepositoryResponse(request.getAsccpManifestId());
    }

    public CancelRevisionAsccpRepositoryResponse resetLogAsccp(CancelRevisionAsccpRepositoryRequest request) {
        AsccpManifestRecord asccpManifestRecord = dslContext.selectFrom(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(request.getAsccpManifestId()))).fetchOne();

        if (asccpManifestRecord == null) {
            throw new IllegalArgumentException("Not found a target ASCCP");
        }

        AsccpRecord asccpRecord = dslContext.selectFrom(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(asccpManifestRecord.getAsccpId())).fetchOne();

        LogRecord cursorLog = dslContext.selectFrom(LOG)
                .where(LOG.LOG_ID.eq(asccpManifestRecord.getLogId())).fetchOne();

        UInteger logNum = cursorLog.getRevisionNum();

        if (cursorLog.getPrevLogId() == null) {
            throw new IllegalArgumentException("There is no change to be reset.");
        }

        List<ULong> deleteLogTargets = new ArrayList<>();

        while (cursorLog.getPrevLogId() != null) {
            if (!cursorLog.getRevisionNum().equals(logNum)) {
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

        ULong roleOfAccId = serializer.getSnapshotId(snapshot.get("roleOfAccId"));
        AccManifestRecord accManifestRecord = dslContext.selectFrom(ACC_MANIFEST).where(and(
                ACC_MANIFEST.ACC_ID.eq(roleOfAccId),
                ACC_MANIFEST.RELEASE_ID.eq(asccpManifestRecord.getReleaseId())
        )).fetchOne();

        if (accManifestRecord == null) {
            throw new IllegalArgumentException("Not found role of ACC.");
        }

        AccRecord accRecord = dslContext.selectFrom(ACC).where(ACC.ACC_ID.eq(accManifestRecord.getAccId())).fetchOne();

        asccpManifestRecord.setDen(asccpRecord.getPropertyTerm() + ". " + accRecord.getObjectClassTerm());
        asccpManifestRecord.setRoleOfAccManifestId(accManifestRecord.getAccManifestId());
        asccpManifestRecord.setLogId(cursorLog.getLogId());
        asccpManifestRecord.update(ASCCP_MANIFEST.DEN, ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID, ASCCP_MANIFEST.LOG_ID);

        asccpRecord.setRoleOfAccId(accManifestRecord.getAccId());
        asccpRecord.setPropertyTerm(serializer.getSnapshotString(snapshot.get("propertyTerm")));
        asccpRecord.setDefinition(serializer.getSnapshotString(snapshot.get("definition")));
        asccpRecord.setDefinitionSource(serializer.getSnapshotString(snapshot.get("definitionSource")));
        asccpRecord.setNamespaceId(serializer.getSnapshotId(snapshot.get("namespaceId")));
        asccpRecord.setIsDeprecated(serializer.getSnapshotByte(snapshot.get("deprecated")));
        asccpRecord.setIsNillable(serializer.getSnapshotByte(snapshot.get("nillable")));
        asccpRecord.setReusableIndicator(serializer.getSnapshotByte(snapshot.get("reusable")));
        asccpRecord.update();

        cursorLog.setNextLogId(null);
        cursorLog.update(LOG.NEXT_LOG_ID);
        dslContext.update(LOG)
                .setNull(LOG.PREV_LOG_ID)
                .setNull(LOG.NEXT_LOG_ID)
                .where(LOG.LOG_ID.in(deleteLogTargets))
                .execute();
        dslContext.deleteFrom(LOG).where(LOG.LOG_ID.in(deleteLogTargets)).execute();

        return new CancelRevisionAsccpRepositoryResponse(request.getAsccpManifestId());
    }
}