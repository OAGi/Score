package org.oagi.score.repo.api.impl.jooq.module;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.ModuleSetRecord;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.module.ModuleSetWriteRepository;
import org.oagi.score.repo.api.module.model.ModuleSet;
import org.oagi.score.repo.api.module.model.*;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.MODULE;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.MODULE_SET;
import static org.oagi.score.repo.api.impl.jooq.utils.ScoreGuidUtils.randomGuid;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

public class JooqModuleSetWriteRepository
        extends JooqScoreRepository
        implements ModuleSetWriteRepository {

    public JooqModuleSetWriteRepository(DSLContext dslContext) {
        super(dslContext);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public CreateModuleSetResponse createModuleSet(CreateModuleSetRequest request) throws ScoreDataAccessException {
        ScoreUser requester = request.getRequester();
        ULong requesterUserId = ULong.valueOf(requester.getUserId());
        LocalDateTime timestamp = LocalDateTime.now();

        ModuleSetRecord moduleSetRecord = dslContext().insertInto(MODULE_SET)
                .set(MODULE_SET.GUID, randomGuid())
                .set(MODULE_SET.NAME, request.getName())
                .set(MODULE_SET.DESCRIPTION, request.getDescription())
                .set(MODULE_SET.CREATED_BY, requesterUserId)
                .set(MODULE_SET.LAST_UPDATED_BY, requesterUserId)
                .set(MODULE_SET.CREATION_TIMESTAMP, timestamp)
                .set(MODULE_SET.LAST_UPDATE_TIMESTAMP, timestamp)
                .returning()
                .fetchOne();

        ModuleSet moduleSet = new ModuleSet();
        moduleSet.setModuleSetId(moduleSetRecord.getModuleSetId().toBigInteger());
        moduleSet.setName(moduleSetRecord.getName());
        moduleSet.setDescription(moduleSetRecord.getDescription());
        moduleSet.setCreatedBy(requester);
        moduleSet.setCreationTimestamp(
                Date.from(moduleSetRecord.getCreationTimestamp().atZone(ZoneId.systemDefault()).toInstant()));
        moduleSet.setLastUpdatedBy(requester);
        moduleSet.setLastUpdateTimestamp(
                Date.from(moduleSetRecord.getLastUpdateTimestamp().atZone(ZoneId.systemDefault()).toInstant()));

        return new CreateModuleSetResponse(moduleSet);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public UpdateModuleSetResponse updateModuleSet(UpdateModuleSetRequest request) throws ScoreDataAccessException {
        ScoreUser requester = request.getRequester();
        ULong requesterUserId = ULong.valueOf(requester.getUserId());
        LocalDateTime timestamp = LocalDateTime.now();

        ModuleSetRecord moduleSetRecord = dslContext().selectFrom(MODULE_SET)
                .where(MODULE_SET.MODULE_SET_ID.eq(ULong.valueOf(request.getModuleSetId())))
                .fetchOne();

        if (moduleSetRecord == null) {
            throw new IllegalArgumentException("Can not found ModuleSet.");
        }

        moduleSetRecord.setName(request.getName());
        moduleSetRecord.setDescription(request.getDescription());

        if (moduleSetRecord.changed()) {
            moduleSetRecord.setLastUpdatedBy(requesterUserId);
            moduleSetRecord.setLastUpdateTimestamp(timestamp);
            moduleSetRecord.update();
        }

        ModuleSet moduleSet = new ModuleSet();
        moduleSet.setModuleSetId(moduleSetRecord.getModuleSetId().toBigInteger());
        moduleSet.setName(moduleSetRecord.getName());
        moduleSet.setDescription(moduleSetRecord.getDescription());

        moduleSet.setCreationTimestamp(
                Date.from(moduleSetRecord.getCreationTimestamp().atZone(ZoneId.systemDefault()).toInstant()));
        moduleSet.setLastUpdatedBy(requester);
        moduleSet.setLastUpdateTimestamp(
                Date.from(moduleSetRecord.getLastUpdateTimestamp().atZone(ZoneId.systemDefault()).toInstant()));

        return new UpdateModuleSetResponse(moduleSet);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public DeleteModuleSetResponse deleteModuleSet(DeleteModuleSetRequest request) throws ScoreDataAccessException {
        dslContext().deleteFrom(MODULE_SET)
                .where(MODULE_SET.MODULE_SET_ID.eq(ULong.valueOf(request.getModuleSetId()))).execute();

        return new DeleteModuleSetResponse();
    }
}