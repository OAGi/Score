package org.oagi.score.repo.api.impl.jooq.module;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.ModuleSetRecord;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.module.ModuleSetWriteRepository;
import org.oagi.score.repo.api.module.model.*;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
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

        if (!StringUtils.hasLength(request.getName())) {
            throw new IllegalArgumentException("Module set name cannot be empty.");
        }

        ModuleSetRecord moduleSetRecord = dslContext().insertInto(MODULE_SET)
                .set(MODULE_SET.LIBRARY_ID, ULong.valueOf(request.getLibraryId()))
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
        moduleSet.setLibraryId(moduleSetRecord.getLibraryId().toBigInteger());
        moduleSet.setName(moduleSetRecord.getName());
        moduleSet.setDescription(moduleSetRecord.getDescription());
        moduleSet.setCreatedBy(requester);
        moduleSet.setCreationTimestamp(
                Date.from(moduleSetRecord.getCreationTimestamp().atZone(ZoneId.systemDefault()).toInstant()));
        moduleSet.setLastUpdatedBy(requester);
        moduleSet.setLastUpdateTimestamp(
                Date.from(moduleSetRecord.getLastUpdateTimestamp().atZone(ZoneId.systemDefault()).toInstant()));

        ULong namespaceId = dslContext().select(NAMESPACE.NAMESPACE_ID).from(NAMESPACE)
                .where(NAMESPACE.IS_STD_NMSP.eq((byte) 1)).limit(1).fetchOneInto(ULong.class);

        ULong rootModuleSetId = dslContext().insertInto(MODULE)
                .setNull(MODULE.PARENT_MODULE_ID)
                .set(MODULE.PATH, "")
                .set(MODULE.TYPE, ModuleType.DIRECTORY.name())
                .set(MODULE.NAME, "")
                .set(MODULE.MODULE_SET_ID, moduleSetRecord.getModuleSetId())
                .set(MODULE.NAMESPACE_ID, namespaceId)
                .setNull(MODULE.VERSION_NUM)
                .set(MODULE.CREATED_BY, requesterUserId)
                .set(MODULE.OWNER_USER_ID, requesterUserId)
                .set(MODULE.LAST_UPDATED_BY, requesterUserId)
                .set(MODULE.CREATION_TIMESTAMP, timestamp)
                .set(MODULE.LAST_UPDATE_TIMESTAMP, timestamp)
                .returning().fetchOne().getModuleId();

        CreateModuleSetResponse response = new CreateModuleSetResponse(moduleSet);
        response.setRootModuleId(rootModuleSetId.toBigInteger());
        return response;
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public UpdateModuleSetResponse updateModuleSet(UpdateModuleSetRequest request) throws ScoreDataAccessException {
        ScoreUser requester = request.getRequester();
        ULong requesterUserId = ULong.valueOf(requester.getUserId());
        LocalDateTime timestamp = LocalDateTime.now();

        if (!StringUtils.hasLength(request.getName())) {
            throw new IllegalArgumentException("Module set name cannot be empty.");
        }

        ModuleSetRecord moduleSetRecord = dslContext().selectFrom(MODULE_SET)
                .where(MODULE_SET.MODULE_SET_ID.eq(ULong.valueOf(request.getModuleSetId())))
                .fetchOne();

        if (moduleSetRecord == null) {
            throw new IllegalArgumentException("Cannot find a module set with the ID " + request.getModuleSetId());
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
        if (dslContext().selectFrom(MODULE_SET_RELEASE)
                .where(MODULE_SET_RELEASE.MODULE_SET_ID.eq(ULong.valueOf(request.getModuleSetId())))
                .fetch().size() > 0) {
            throw new IllegalArgumentException("Module set in use cannot be discarded.");
        }

        dslContext().update(MODULE)
                .setNull(MODULE.PARENT_MODULE_ID)
                .where(MODULE.MODULE_SET_ID.eq(ULong.valueOf(request.getModuleSetId()))).execute();
        dslContext().deleteFrom(MODULE)
                .where(MODULE.MODULE_SET_ID.eq(ULong.valueOf(request.getModuleSetId()))).execute();
        dslContext().deleteFrom(MODULE_SET)
                .where(MODULE_SET.MODULE_SET_ID.eq(ULong.valueOf(request.getModuleSetId()))).execute();

        return new DeleteModuleSetResponse();
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public DeleteModuleSetAssignmentResponse unassignModule(DeleteModuleSetAssignmentRequest request) throws ScoreDataAccessException {

        deleteModule(ULong.valueOf(request.getModuleId()));

        return new DeleteModuleSetAssignmentResponse();
    }

    private void deleteModule(ULong moduleId) {
        dslContext().delete(MODULE_ACC_MANIFEST).where(MODULE_ACC_MANIFEST.MODULE_ID.eq(moduleId)).execute();
        dslContext().delete(MODULE_ASCCP_MANIFEST).where(MODULE_ASCCP_MANIFEST.MODULE_ID.eq(moduleId)).execute();
        dslContext().delete(MODULE_BCCP_MANIFEST).where(MODULE_BCCP_MANIFEST.MODULE_ID.eq(moduleId)).execute();
        dslContext().delete(MODULE_CODE_LIST_MANIFEST).where(MODULE_CODE_LIST_MANIFEST.MODULE_ID.eq(moduleId)).execute();
        dslContext().delete(MODULE_AGENCY_ID_LIST_MANIFEST).where(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_ID.eq(moduleId)).execute();
        dslContext().delete(MODULE_DT_MANIFEST).where(MODULE_DT_MANIFEST.MODULE_ID.eq(moduleId)).execute();
        dslContext().delete(MODULE_BLOB_CONTENT_MANIFEST).where(MODULE_BLOB_CONTENT_MANIFEST.MODULE_ID.eq(moduleId)).execute();
        dslContext().delete(MODULE_XBT_MANIFEST).where(MODULE_XBT_MANIFEST.MODULE_ID.eq(moduleId)).execute();

        dslContext().delete(MODULE).where(MODULE.MODULE_ID.eq(moduleId)).execute();
    }
}