package org.oagi.score.repo.api.impl.jooq.module;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.ModuleRecord;
import org.oagi.score.repo.api.module.ModuleWriteRepository;
import org.oagi.score.repo.api.module.model.*;
import org.oagi.score.repo.api.module.model.Module;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

public class JooqModuleWriteRepository
        extends JooqScoreRepository
        implements ModuleWriteRepository {

    public JooqModuleWriteRepository(DSLContext dslContext) {
        super(dslContext);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public CreateModuleResponse createModule(CreateModuleRequest request) throws ScoreDataAccessException {
        ScoreUser requester = request.getRequester();
        ULong requesterUserId = ULong.valueOf(requester.getUserId());
        LocalDateTime timestamp = LocalDateTime.now();

        ModuleRecord moduleRecord = dslContext().insertInto(MODULE)
                .set(MODULE.MODULE_DIR_ID, ULong.valueOf(request.getModuleDirId()))
                .set(MODULE.NAME, request.getName())
                .set(MODULE.NAMESPACE_ID, ULong.valueOf(request.getNamespaceId()))
                .set(MODULE.VERSION_NUM, request.getVersionNum())
                .set(MODULE.CREATED_BY, requesterUserId)
                .set(MODULE.LAST_UPDATED_BY, requesterUserId)
                .set(MODULE.CREATION_TIMESTAMP, timestamp)
                .set(MODULE.LAST_UPDATE_TIMESTAMP, timestamp)
                .returning()
                .fetchOne();

        Module module = new Module();
        module.setModuleId(moduleRecord.getModuleId().toBigInteger());
        module.setModuleDirId(moduleRecord.getModuleDirId().toBigInteger());
        module.setName(moduleRecord.getName());
        module.setVersionNum(moduleRecord.getVersionNum());
        module.setCreatedBy(requester);
        module.setCreationTimestamp(
                Date.from(moduleRecord.getCreationTimestamp().atZone(ZoneId.systemDefault()).toInstant()));
        module.setLastUpdatedBy(requester);
        module.setLastUpdateTimestamp(
                Date.from(moduleRecord.getLastUpdateTimestamp().atZone(ZoneId.systemDefault()).toInstant()));

        return new CreateModuleResponse(module);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public UpdateModuleResponse updateModule(UpdateModuleRequest request) throws ScoreDataAccessException {
        ScoreUser requester = request.getRequester();
        ULong requesterUserId = ULong.valueOf(requester.getUserId());
        LocalDateTime timestamp = LocalDateTime.now();

        ModuleRecord moduleRecord = dslContext().update(MODULE)
                .set(MODULE.MODULE_DIR_ID, ULong.valueOf(request.getModuleDirId()))
                .set(MODULE.NAME, request.getName())
                .set(MODULE.NAMESPACE_ID, ULong.valueOf(request.getNamespaceId()))
                .set(MODULE.VERSION_NUM, request.getVersionNum())
                .set(MODULE.LAST_UPDATED_BY, requesterUserId)
                .set(MODULE.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(MODULE.MODULE_ID.eq(ULong.valueOf(request.getModuleId())))
                .returning().fetchOne();

        Module module = new Module();
        module.setModuleId(moduleRecord.getModuleId().toBigInteger());
        module.setModuleDirId(moduleRecord.getModuleDirId().toBigInteger());
        module.setName(moduleRecord.getName());
        module.setVersionNum(moduleRecord.getVersionNum());

        module.setCreationTimestamp(
                Date.from(moduleRecord.getCreationTimestamp().atZone(ZoneId.systemDefault()).toInstant()));
        module.setLastUpdatedBy(requester);
        module.setLastUpdateTimestamp(
                Date.from(moduleRecord.getLastUpdateTimestamp().atZone(ZoneId.systemDefault()).toInstant()));

        return new UpdateModuleResponse(module);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public DeleteModuleResponse deleteModule(DeleteModuleRequest request) throws ScoreDataAccessException {
        dslContext().deleteFrom(MODULE)
                .where(MODULE.MODULE_ID.eq(ULong.valueOf(request.getModuleId())));

        return new DeleteModuleResponse();
    }

}
