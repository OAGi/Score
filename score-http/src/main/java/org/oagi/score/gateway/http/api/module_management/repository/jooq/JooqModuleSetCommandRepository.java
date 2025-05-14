package org.oagi.score.gateway.http.api.module_management.repository.jooq;

import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetId;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleSetCommandRepository;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleSetReleaseQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.util.StringUtils;

import java.time.LocalDateTime;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.MODULE;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.MODULE_SET;
import static org.oagi.score.gateway.http.common.util.ScoreGuidUtils.randomGuid;

public class JooqModuleSetCommandRepository extends JooqBaseRepository implements ModuleSetCommandRepository {

    private final ModuleSetReleaseQueryRepository moduleSetReleaseQueryRepository;

    public JooqModuleSetCommandRepository(DSLContext dslContext, ScoreUser requester,
                                          RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);

        this.moduleSetReleaseQueryRepository = repositoryFactory.moduleSetReleaseQueryRepository(requester);
    }

    @Override
    public ModuleSetId create(LibraryId libraryId, String name, String description) {
        if (!StringUtils.hasLength(name)) {
            throw new IllegalArgumentException("Module set name cannot be empty.");
        }

        LocalDateTime timestamp = LocalDateTime.now();
        return new ModuleSetId(
                dslContext().insertInto(MODULE_SET)
                        .set(MODULE_SET.LIBRARY_ID, valueOf(libraryId))
                        .set(MODULE_SET.GUID, randomGuid())
                        .set(MODULE_SET.NAME, name)
                        .set(MODULE_SET.DESCRIPTION, description)
                        .set(MODULE_SET.CREATED_BY, valueOf(requester().userId()))
                        .set(MODULE_SET.LAST_UPDATED_BY, valueOf(requester().userId()))
                        .set(MODULE_SET.CREATION_TIMESTAMP, timestamp)
                        .set(MODULE_SET.LAST_UPDATE_TIMESTAMP, timestamp)
                        .returning(MODULE_SET.MODULE_SET_ID)
                        .fetchOne().getModuleSetId().toBigInteger()
        );
    }

    @Override
    public boolean update(ModuleSetId moduleSetId, String name, String description) {

        if (!StringUtils.hasLength(name)) {
            throw new IllegalArgumentException("Module set name cannot be empty.");
        }

        LocalDateTime timestamp = LocalDateTime.now();

        int numOfUpdatedRecords = dslContext().update(MODULE_SET)
                .set(MODULE_SET.NAME, name)
                .set(MODULE_SET.DESCRIPTION, description)
                .set(MODULE_SET.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(MODULE_SET.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(MODULE_SET.MODULE_SET_ID.eq(valueOf(moduleSetId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean delete(ModuleSetId moduleSetId) {

        if (moduleSetReleaseQueryRepository.exists(moduleSetId)) {
            throw new IllegalArgumentException("Module set in use cannot be discarded.");
        }

        dslContext().update(MODULE)
                .setNull(MODULE.PARENT_MODULE_ID)
                .where(MODULE.MODULE_SET_ID.eq(valueOf(moduleSetId))).execute();

        dslContext().deleteFrom(MODULE)
                .where(MODULE.MODULE_SET_ID.eq(valueOf(moduleSetId))).execute();

        int numOfDeletedRecords = dslContext().deleteFrom(MODULE_SET)
                .where(MODULE_SET.MODULE_SET_ID.eq(valueOf(moduleSetId))).execute();

        return numOfDeletedRecords == 1;
    }
}
