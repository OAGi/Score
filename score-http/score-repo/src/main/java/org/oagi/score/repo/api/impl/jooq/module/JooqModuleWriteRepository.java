package org.oagi.score.repo.api.impl.jooq.module;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.ModuleRecord;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.module.ModuleWriteRepository;
import org.oagi.score.repo.api.module.model.*;
import org.oagi.score.repo.api.module.model.Module;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.jooq.impl.DSL.*;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

public class JooqModuleWriteRepository
        extends JooqScoreRepository
        implements ModuleWriteRepository {

    public JooqModuleWriteRepository(DSLContext dslContext) {
        super(dslContext);
    }

    private final String MODULE_PATH_SEPARATOR = "\\";

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public CreateModuleResponse createModule(CreateModuleRequest request) throws ScoreDataAccessException {
        ScoreUser requester = request.getRequester();
        ULong requesterUserId = ULong.valueOf(requester.getUserId());
        LocalDateTime timestamp = LocalDateTime.now();

        if (hasDuplicateName(request.getParentModuleId(), request.getName())) {
            throw new IllegalArgumentException("Duplicate module name exist.");
        }

        ModuleRecord parent = dslContext().selectFrom(MODULE)
                .where(MODULE.MODULE_ID.eq(ULong.valueOf(request.getParentModuleId()))).fetchOne();

        String path = parent.getPath().length() > 0 ? parent.getPath() + MODULE_PATH_SEPARATOR + request.getName() : request.getName();

        ModuleRecord moduleRecord = dslContext().insertInto(MODULE)
                .set(MODULE.PARENT_MODULE_ID, parent.getModuleId())
                .set(MODULE.PATH, path)
                .set(MODULE.TYPE, request.getModuleType().name())
                .set(MODULE.NAME, request.getName())
                .set(MODULE.MODULE_SET_ID, ULong.valueOf(request.getModuleSetId()))
                .set(MODULE.NAMESPACE_ID, (request.getNamespaceId() != null) ? ULong.valueOf(request.getNamespaceId()) : null)
                .set(MODULE.VERSION_NUM, request.getVersionNum())
                .set(MODULE.CREATED_BY, requesterUserId)
                .set(MODULE.OWNER_USER_ID, requesterUserId)
                .set(MODULE.LAST_UPDATED_BY, requesterUserId)
                .set(MODULE.CREATION_TIMESTAMP, timestamp)
                .set(MODULE.LAST_UPDATE_TIMESTAMP, timestamp)
                .returning()
                .fetchOne();

        Module module = new Module();
        module.setModuleId(moduleRecord.getModuleId().toBigInteger());
        module.setParentModuleId(moduleRecord.getParentModuleId().toBigInteger());
        module.setName(moduleRecord.getName());
        module.setVersionNum(moduleRecord.getVersionNum());
        if (moduleRecord.getNamespaceId() != null) {
            module.setNamespaceId(moduleRecord.getNamespaceId().toBigInteger());
        }
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

        ModuleRecord moduleRecord = dslContext().selectFrom(MODULE)
                .where(MODULE.MODULE_ID.eq(ULong.valueOf(request.getModuleId())))
                .fetchOne();

        if (moduleRecord == null) {
            throw new IllegalArgumentException("Cannot found a module record [moduleId=" + request.getModuleId() + "]");
        }

        if (request.getNamespaceId() != null) {
            moduleRecord.setNamespaceId(ULong.valueOf(request.getNamespaceId()));
        } else {
            moduleRecord.setNamespaceId(null);
        }

        if (StringUtils.hasLength(request.getVersionNum())) {
            moduleRecord.setVersionNum(request.getVersionNum());
        }

        boolean nameChanged = false;
        List<String> tokens = Arrays.asList(moduleRecord.getPath().split(MODULE_PATH_SEPARATOR + MODULE_PATH_SEPARATOR));

        if (StringUtils.hasLength(request.getName()) && !moduleRecord.getName().equals(request.getName())) {
            if (dslContext().selectFrom(MODULE)
                    .where(and(MODULE.PARENT_MODULE_ID.eq(moduleRecord.getParentModuleId()),
                            MODULE.MODULE_ID.notEqual(moduleRecord.getModuleId()),
                            MODULE.NAME.eq(request.getName()))).fetch().size() > 0) {
                throw new IllegalArgumentException("Duplicate module name exist.");
            }
            tokens.set(tokens.size() - 1, request.getName());

            moduleRecord.setName(request.getName());
            moduleRecord.setPath(String.join(MODULE_PATH_SEPARATOR, tokens));
            nameChanged = true;
        }

        if (moduleRecord.changed()) {
            moduleRecord.setLastUpdatedBy(requesterUserId);
            moduleRecord.setLastUpdateTimestamp(timestamp);
            moduleRecord.update();
            if (nameChanged && moduleRecord.getType().equals(ModuleType.DIRECTORY.name())) {
                broadcastModulePath(moduleRecord.getModuleId(), tokens);
            }
        }

        Module module = new Module();
        module.setModuleId(moduleRecord.getModuleId().toBigInteger());
        module.setParentModuleId(moduleRecord.getParentModuleId().toBigInteger());
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
        ULong moduleId = ULong.valueOf(request.getModuleId());
        deleteModule(moduleId, true);
        return new DeleteModuleResponse();
    }

    private void deleteModule(ULong moduleId, boolean isDirectory) {
        if (isDirectory) {
            List<ModuleRecord> moduleRecordList = dslContext().selectFrom(MODULE)
                    .where(MODULE.PARENT_MODULE_ID.eq(moduleId))
                    .fetch();

            moduleRecordList.forEach(e -> {
                deleteModule(e.getModuleId(), ModuleType.valueOf(e.getType()).equals(ModuleType.DIRECTORY));
            });
        }
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

    private void broadcastModulePath(ULong parentModuleId, List<String> tokens) {
        List<ModuleRecord> moduleRecordList = dslContext().selectFrom(MODULE)
                .where(MODULE.PARENT_MODULE_ID.eq(parentModuleId))
                .fetch();

        moduleRecordList.forEach(e -> {
            e.setPath(String.join(MODULE_PATH_SEPARATOR, tokens) + MODULE_PATH_SEPARATOR + e.getName());
            e.update(MODULE.PATH);
            broadcastModulePath(e.getModuleId(), Arrays.asList(e.getPath().split(MODULE_PATH_SEPARATOR + MODULE_PATH_SEPARATOR)));
        });
    }

    @Override
    public void copyModule(CopyModuleRequest request) throws ScoreDataAccessException {
        ScoreUser requester = request.getRequester();
        ULong requesterUserId = ULong.valueOf(requester.getUserId());
        LocalDateTime timestamp = LocalDateTime.now();

        ModuleRecord moduleRecord = dslContext().selectFrom(MODULE)
                .where(MODULE.MODULE_ID.eq(ULong.valueOf(request.getTargetModuleId()))).fetchOne();

        ModuleRecord parent = dslContext().selectFrom(MODULE)
                .where(MODULE.MODULE_ID.eq(ULong.valueOf(request.getParentModuleId()))).fetchOne();

        if (hasDuplicateName(request.getParentModuleId(), moduleRecord.getName())) {
            copyOverWriteModule(moduleRecord, parent, requesterUserId, timestamp, request.isCopySubModules());
        } else {
            copyInsertModule(moduleRecord, parent, requesterUserId, timestamp, request.isCopySubModules());
        }
    }

    private void copyInsertModule(ModuleRecord target, ModuleRecord parent, ULong requesterUserId, LocalDateTime timestamp, boolean copySub) {
        String path;
        if (parent.getPath().length() == 0) {
            path = target.getName();
        } else {
            path = parent.getPath() + MODULE_PATH_SEPARATOR + target.getName();
        }
        ModuleRecord inserted = dslContext().insertInto(MODULE)
                .set(MODULE.PARENT_MODULE_ID, parent.getModuleId())
                .set(MODULE.NAME, target.getName())
                .set(MODULE.TYPE, target.getType())
                .set(MODULE.PATH, path)
                .set(MODULE.MODULE_SET_ID, parent.getModuleSetId())
                .set(MODULE.NAMESPACE_ID, target.getNamespaceId())
                .set(MODULE.VERSION_NUM, target.getVersionNum())
                .set(MODULE.CREATED_BY, requesterUserId)
                .set(MODULE.OWNER_USER_ID, requesterUserId)
                .set(MODULE.LAST_UPDATED_BY, requesterUserId)
                .set(MODULE.CREATION_TIMESTAMP, timestamp)
                .set(MODULE.LAST_UPDATE_TIMESTAMP, timestamp)
                .returning().fetchOne();

        if (copySub) {
            if (target.getType().equals(ModuleType.DIRECTORY.name())) {
                dslContext().selectFrom(MODULE)
                        .where(MODULE.PARENT_MODULE_ID.eq(target.getModuleId()))
                        .fetchStream().forEach(e -> {
                    copyInsertModule(e, inserted, requesterUserId, timestamp, copySub);
                });
            }
        }

    }

    private void copyOverWriteModule(ModuleRecord target, ModuleRecord parent, ULong requesterUserId, LocalDateTime timestamp, boolean copySub) {
        ModuleRecord duplicated = dslContext().selectFrom(MODULE).where(and(
                MODULE.PARENT_MODULE_ID.eq(parent.getModuleId()),
                MODULE.NAME.eq(target.getName())
        )).fetchOne();

        if (duplicated == null) {
            copyInsertModule(target, parent, requesterUserId, timestamp, copySub);
            return;
        }

        if (duplicated.getType().equals(ModuleType.FILE.name())) {
            if (target.getType().equals(ModuleType.FILE.name())) {
                duplicated.setVersionNum(target.getVersionNum());
                duplicated.setNamespaceId(target.getNamespaceId());
                duplicated.setLastUpdatedBy(requesterUserId);
                duplicated.setLastUpdateTimestamp(timestamp);
                duplicated.update();
            } else {
                duplicated.setVersionNum(target.getVersionNum());
                duplicated.setNamespaceId(target.getNamespaceId());
                duplicated.setLastUpdatedBy(requesterUserId);
                duplicated.setLastUpdateTimestamp(timestamp);
                duplicated.setType(ModuleType.DIRECTORY.name());
                duplicated.update();
                if (copySub) {
                    dslContext().selectFrom(MODULE)
                            .where(MODULE.PARENT_MODULE_ID.eq(target.getModuleId()))
                            .fetchStream().forEach(e -> {
                        copyInsertModule(e, duplicated, requesterUserId, timestamp, copySub);
                    });
                }
            }
        } else {
            if (target.getType().equals(ModuleType.FILE.name())) {
                DeleteModuleRequest deleteModuleRequest = new DeleteModuleRequest();
                deleteModuleRequest.setModuleId(duplicated.getModuleId().toBigInteger());
                deleteModule(deleteModuleRequest);
                if (copySub) {
                    copyInsertModule(target, parent, requesterUserId, timestamp, copySub);
                }
            } else {
                duplicated.setVersionNum(target.getVersionNum());
                duplicated.setNamespaceId(target.getNamespaceId());
                duplicated.setLastUpdatedBy(requesterUserId);
                duplicated.setLastUpdateTimestamp(timestamp);
                duplicated.update();

                if (copySub) {
                    dslContext().selectFrom(MODULE)
                            .where(MODULE.PARENT_MODULE_ID.eq(target.getModuleId()))
                            .fetchStream().forEach(e -> {
                        copyOverWriteModule(e, duplicated, requesterUserId, timestamp, copySub);
                    });
                }
            }
        }
    }

    private boolean hasDuplicateName(BigInteger parentModuleId, String name) {
        if (dslContext().selectFrom(MODULE)
                .where(and(MODULE.PARENT_MODULE_ID.eq(ULong.valueOf(parentModuleId)),
                        MODULE.NAME.eq(name))).fetch().size() > 0) {
            return true;
        }
        return false;
    }
}
