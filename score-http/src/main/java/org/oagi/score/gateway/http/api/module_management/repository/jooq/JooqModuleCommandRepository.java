package org.oagi.score.gateway.http.api.module_management.repository.jooq;

import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.module_management.model.ModuleId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleType;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleCommandRepository;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.time.LocalDateTime;

import static org.oagi.score.gateway.http.api.module_management.model.ModuleType.DIRECTORY;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

public class JooqModuleCommandRepository extends JooqBaseRepository implements ModuleCommandRepository {

    public JooqModuleCommandRepository(DSLContext dslContext, ScoreUser requester,
                                       RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public ModuleId createRootModule(ModuleSetId moduleSetId, NamespaceId namespaceId) {

        LocalDateTime timestamp = LocalDateTime.now();

        return new ModuleId(
                dslContext().insertInto(MODULE)
                        .setNull(MODULE.PARENT_MODULE_ID)
                        .set(MODULE.PATH, "")
                        .set(MODULE.TYPE, DIRECTORY.name())
                        .set(MODULE.NAME, "")
                        .set(MODULE.MODULE_SET_ID, valueOf(moduleSetId))
                        .set(MODULE.NAMESPACE_ID, (namespaceId != null) ? valueOf(namespaceId) : null)
                        .setNull(MODULE.VERSION_NUM)
                        .set(MODULE.CREATED_BY, valueOf(requester().userId()))
                        .set(MODULE.OWNER_USER_ID, valueOf(requester().userId()))
                        .set(MODULE.LAST_UPDATED_BY, valueOf(requester().userId()))
                        .set(MODULE.CREATION_TIMESTAMP, timestamp)
                        .set(MODULE.LAST_UPDATE_TIMESTAMP, timestamp)
                        .returning(MODULE.MODULE_ID)
                        .fetchOne().getModuleId().toBigInteger()
        );
    }

    @Override
    public ModuleId create(ModuleSetId moduleSetId,
                           ModuleId parentModuleId, NamespaceId namespaceId,
                           ModuleType moduleType, String path, String name, String versionNum) {

        LocalDateTime timestamp = LocalDateTime.now();

        return new ModuleId(
                dslContext().insertInto(MODULE)
                        .set(MODULE.PARENT_MODULE_ID, valueOf(parentModuleId))
                        .set(MODULE.PATH, path)
                        .set(MODULE.TYPE, moduleType.name())
                        .set(MODULE.NAME, name)
                        .set(MODULE.VERSION_NUM, versionNum)
                        .set(MODULE.MODULE_SET_ID, valueOf(moduleSetId))
                        .set(MODULE.NAMESPACE_ID, (namespaceId != null) ? valueOf(namespaceId) : null)
                        .set(MODULE.OWNER_USER_ID, valueOf(requester().userId()))
                        .set(MODULE.CREATED_BY, valueOf(requester().userId()))
                        .set(MODULE.LAST_UPDATED_BY, valueOf(requester().userId()))
                        .set(MODULE.CREATION_TIMESTAMP, timestamp)
                        .set(MODULE.LAST_UPDATE_TIMESTAMP, timestamp)
                        .returning(MODULE.MODULE_ID)
                        .fetchOne().getModuleId().toBigInteger()
        );
    }

    @Override
    public boolean update(ModuleId moduleId, NamespaceId namespaceId,
                          String path, String name, String versionNum) {

        LocalDateTime timestamp = LocalDateTime.now();

        int numOfUpdatedRecords = dslContext().update(MODULE)
                .set(MODULE.NAMESPACE_ID, valueOf(namespaceId))
                .set(MODULE.PATH, path)
                .set(MODULE.NAME, name)
                .set(MODULE.VERSION_NUM, versionNum)
                .set(MODULE.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(MODULE.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(MODULE.MODULE_ID.eq(valueOf(moduleId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean updateVersionNumAndNamespaceId(
            ModuleId moduleId,
            NamespaceId namespaceId, String versionNum) {

        LocalDateTime timestamp = LocalDateTime.now();

        int numOfUpdatedRecords = dslContext().update(MODULE)
                .set(MODULE.NAMESPACE_ID, valueOf(namespaceId))
                .set(MODULE.VERSION_NUM, versionNum)
                .set(MODULE.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(MODULE.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(MODULE.MODULE_ID.eq(valueOf(moduleId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean updateVersionNumAndNamespaceIdAndType(
            ModuleId moduleId,
            NamespaceId namespaceId, String versionNum, ModuleType moduleType) {

        LocalDateTime timestamp = LocalDateTime.now();

        int numOfUpdatedRecords = dslContext().update(MODULE)
                .set(MODULE.NAMESPACE_ID, valueOf(namespaceId))
                .set(MODULE.TYPE, moduleType.name())
                .set(MODULE.VERSION_NUM, versionNum)
                .set(MODULE.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(MODULE.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(MODULE.MODULE_ID.eq(valueOf(moduleId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean updatePath(ModuleId moduleId, String path) {

        int numOfUpdatedRecords = dslContext().update(MODULE)
                .set(MODULE.PATH, path)
                .where(MODULE.MODULE_ID.eq(valueOf(moduleId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean delete(ModuleId moduleId) {
        dslContext().delete(MODULE_ACC_MANIFEST).where(MODULE_ACC_MANIFEST.MODULE_ID.eq(valueOf(moduleId))).execute();
        dslContext().delete(MODULE_ASCCP_MANIFEST).where(MODULE_ASCCP_MANIFEST.MODULE_ID.eq(valueOf(moduleId))).execute();
        dslContext().delete(MODULE_BCCP_MANIFEST).where(MODULE_BCCP_MANIFEST.MODULE_ID.eq(valueOf(moduleId))).execute();
        dslContext().delete(MODULE_CODE_LIST_MANIFEST).where(MODULE_CODE_LIST_MANIFEST.MODULE_ID.eq(valueOf(moduleId))).execute();
        dslContext().delete(MODULE_AGENCY_ID_LIST_MANIFEST).where(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_ID.eq(valueOf(moduleId))).execute();
        dslContext().delete(MODULE_DT_MANIFEST).where(MODULE_DT_MANIFEST.MODULE_ID.eq(valueOf(moduleId))).execute();
        dslContext().delete(MODULE_BLOB_CONTENT_MANIFEST).where(MODULE_BLOB_CONTENT_MANIFEST.MODULE_ID.eq(valueOf(moduleId))).execute();
        dslContext().delete(MODULE_XBT_MANIFEST).where(MODULE_XBT_MANIFEST.MODULE_ID.eq(valueOf(moduleId))).execute();

        int numOfDeletedRecords = dslContext().delete(MODULE).where(MODULE.MODULE_ID.eq(valueOf(moduleId))).execute();
        return numOfDeletedRecords == 1;
    }

}
