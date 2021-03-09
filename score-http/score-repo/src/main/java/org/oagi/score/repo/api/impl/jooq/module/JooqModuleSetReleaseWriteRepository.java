package org.oagi.score.repo.api.impl.jooq.module;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.ModuleSetReleaseRecord;
import org.oagi.score.repo.api.module.ModuleSetReleaseWriteRepository;
import org.oagi.score.repo.api.module.model.*;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

public class JooqModuleSetReleaseWriteRepository
        extends JooqScoreRepository
        implements ModuleSetReleaseWriteRepository {

    public JooqModuleSetReleaseWriteRepository(DSLContext dslContext) {
        super(dslContext);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public CreateModuleSetReleaseResponse createModuleSetRelease(CreateModuleSetReleaseRequest request) throws ScoreDataAccessException {
        ScoreUser requester = request.getRequester();
        ULong requesterUserId = ULong.valueOf(requester.getUserId());
        LocalDateTime timestamp = LocalDateTime.now();

        ModuleSetReleaseRecord moduleSetRecord = dslContext().insertInto(MODULE_SET_RELEASE)
                .set(MODULE_SET_RELEASE.RELEASE_ID, ULong.valueOf(request.getReleaseId()))
                .set(MODULE_SET_RELEASE.MODULE_SET_ID, ULong.valueOf(request.getModuleSetId()))
                .set(MODULE_SET_RELEASE.IS_DEFAULT, request.isDefault() ? (byte) 1 : 0)
                .set(MODULE_SET_RELEASE.CREATED_BY, requesterUserId)
                .set(MODULE_SET_RELEASE.LAST_UPDATED_BY, requesterUserId)
                .set(MODULE_SET_RELEASE.CREATION_TIMESTAMP, timestamp)
                .set(MODULE_SET_RELEASE.LAST_UPDATE_TIMESTAMP, timestamp)
                .returning()
                .fetchOne();

        ModuleSetRelease moduleSetRelease = new ModuleSetRelease();
        moduleSetRelease.setModuleSetReleaseId(moduleSetRecord.getModuleSetReleaseId().toBigInteger());
        moduleSetRelease.setReleaseId(moduleSetRecord.getReleaseId().toBigInteger());
        moduleSetRelease.setModuleSetId(moduleSetRecord.getModuleSetId().toBigInteger());
        moduleSetRelease.setDefault(request.isDefault());
        moduleSetRelease.setCreatedBy(requester);
        moduleSetRelease.setCreationTimestamp(
                Date.from(moduleSetRecord.getCreationTimestamp().atZone(ZoneId.systemDefault()).toInstant()));
        moduleSetRelease.setLastUpdatedBy(requester);
        moduleSetRelease.setLastUpdateTimestamp(
                Date.from(moduleSetRecord.getLastUpdateTimestamp().atZone(ZoneId.systemDefault()).toInstant()));

        return new CreateModuleSetReleaseResponse(moduleSetRelease);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public UpdateModuleSetReleaseResponse updateModuleSetRelease(UpdateModuleSetReleaseRequest request) throws ScoreDataAccessException {
        ScoreUser requester = request.getRequester();
        ULong requesterUserId = ULong.valueOf(requester.getUserId());
        LocalDateTime timestamp = LocalDateTime.now();

        ModuleSetReleaseRecord moduleSetReleaseRecord = dslContext().update(MODULE_SET_RELEASE)
                .set(MODULE_SET_RELEASE.RELEASE_ID, ULong.valueOf(request.getReleaseId()))
                .set(MODULE_SET_RELEASE.MODULE_SET_ID, ULong.valueOf(request.getModuleSetId()))
                .set(MODULE_SET_RELEASE.IS_DEFAULT, request.isDefault() ? (byte) 1 : 0)
                .set(MODULE_SET_RELEASE.LAST_UPDATED_BY, requesterUserId)
                .set(MODULE_SET_RELEASE.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId())))
                .returning().fetchOne();

        ModuleSetRelease moduleSetRelease = new ModuleSetRelease();
        moduleSetRelease.setModuleSetReleaseId(moduleSetReleaseRecord.getModuleSetReleaseId().toBigInteger());
        moduleSetRelease.setReleaseId(moduleSetReleaseRecord.getReleaseId().toBigInteger());
        moduleSetRelease.setModuleSetId(moduleSetReleaseRecord.getModuleSetId().toBigInteger());
        moduleSetRelease.setDefault(request.isDefault());

        moduleSetRelease.setCreationTimestamp(
                Date.from(moduleSetReleaseRecord.getCreationTimestamp().atZone(ZoneId.systemDefault()).toInstant()));
        moduleSetRelease.setLastUpdatedBy(requester);
        moduleSetRelease.setLastUpdateTimestamp(
                Date.from(moduleSetReleaseRecord.getLastUpdateTimestamp().atZone(ZoneId.systemDefault()).toInstant()));

        return new UpdateModuleSetReleaseResponse(moduleSetRelease);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public DeleteModuleSetReleaseResponse deleteModuleSetRelease(DeleteModuleSetReleaseRequest request) throws ScoreDataAccessException {
        dslContext().deleteFrom(MODULE_SET_RELEASE)
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId())))
                .execute();

        return new DeleteModuleSetReleaseResponse();
    }
}