package org.oagi.score.gateway.http.api.module_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.RecordMapper;
import org.jooq.SelectOnConditionStep;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.export.model.Namespace;
import org.oagi.score.gateway.http.api.export.model.ScoreModule;
import org.oagi.score.gateway.http.api.module_management.model.*;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleQueryRepository;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

public class JooqModuleQueryRepository extends JooqBaseRepository implements ModuleQueryRepository {

    public JooqModuleQueryRepository(DSLContext dslContext, ScoreUser requester,
                                     RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public ModuleSummaryRecord getRootModule(ModuleSetId moduleSetId) {
        var queryBuilder = new GetModuleSummaryQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        MODULE.PARENT_MODULE_ID.isNull(),
                        MODULE.MODULE_SET_ID.eq(valueOf(moduleSetId))))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<ModuleSummaryRecord> getModuleSummaryList(ModuleSetId moduleSetId) {
        var queryBuilder = new GetModuleSummaryQueryBuilder();
        return queryBuilder.select()
                .where(MODULE.MODULE_SET_ID.eq(valueOf(moduleSetId)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<ModuleSummaryRecord> getTopLevelModules(ModuleSetId moduleSetId) {
        ModuleSummaryRecord rootModule = getRootModule(moduleSetId);

        var queryBuilder = new GetModuleSummaryQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        MODULE.MODULE_SET_ID.eq(valueOf(moduleSetId)),
                        MODULE.PARENT_MODULE_ID.eq(valueOf(rootModule.moduleId()))))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public ModuleSummaryRecord getModule(ModuleId moduleId) {
        var queryBuilder = new GetModuleSummaryQueryBuilder();
        return queryBuilder.select()
                .where(MODULE.MODULE_ID.eq(valueOf(moduleId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<ModuleSummaryRecord> getChildren(ModuleId moduleId) {
        var queryBuilder = new GetModuleSummaryQueryBuilder();
        return queryBuilder.select()
                .where(MODULE.PARENT_MODULE_ID.eq(valueOf(moduleId)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public ModuleSummaryRecord getDuplicateModule(ModuleId moduleId, String name) {
        var queryBuilder = new GetModuleSummaryQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        MODULE.MODULE_ID.eq(valueOf(moduleId)),
                        MODULE.NAME.eq(name)
                ))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetModuleSummaryQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(
                            MODULE.MODULE_ID,
                            MODULE.MODULE_SET_ID,
                            MODULE.PARENT_MODULE_ID,
                            MODULE.TYPE,
                            MODULE.PATH,
                            MODULE.NAME,
                            MODULE.VERSION_NUM,
                            MODULE.NAMESPACE_ID,
                            NAMESPACE.URI)
                    .from(MODULE)
                    .leftJoin(NAMESPACE).on(NAMESPACE.NAMESPACE_ID.eq(MODULE.NAMESPACE_ID));
        }

        RecordMapper<org.jooq.Record, ModuleSummaryRecord> mapper() {
            return record -> new ModuleSummaryRecord(
                    new ModuleId(record.get(MODULE.MODULE_ID).toBigInteger()),
                    new ModuleSetId(record.get(MODULE.MODULE_SET_ID).toBigInteger()),
                    (record.get(MODULE.PARENT_MODULE_ID) != null) ?
                            new ModuleId(record.get(MODULE.PARENT_MODULE_ID).toBigInteger()) : null,
                    ModuleType.valueOf(record.get(MODULE.TYPE)),
                    record.get(MODULE.PATH),
                    (record.get(MODULE.NAMESPACE_ID) != null) ?
                            new NamespaceId(record.get(MODULE.NAMESPACE_ID).toBigInteger()) : null,
                    record.get(NAMESPACE.URI),
                    record.get(MODULE.NAME),
                    record.get(MODULE.VERSION_NUM));
        }
    }

    @Override
    public boolean hasDuplicateName(ModuleId parentModuleId, String name) {
        return dslContext().selectCount()
                .from(MODULE)
                .where(and(
                        MODULE.PARENT_MODULE_ID.eq(valueOf(parentModuleId)),
                        MODULE.NAME.eq(name)
                )).fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    @Override
    public List<ScoreModule> getScoreModules(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().select(
                        MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID,
                        MODULE_SET_RELEASE.MODULE_SET_ID,
                        MODULE_SET_RELEASE.RELEASE_ID,
                        MODULE.MODULE_ID, MODULE.NAME,
                        MODULE.NAMESPACE_ID.as("module_namespace_id"),
                        NAMESPACE.as("module_namespace").URI.as("module_namespace_uri"),
                        NAMESPACE.as("module_namespace").PREFIX.as("module_namespace_prefix"),
                        RELEASE.NAMESPACE_ID.as("release_namespace_id"),
                        NAMESPACE.as("release_namespace").URI.as("release_namespace_uri"),
                        NAMESPACE.as("release_namespace").PREFIX.as("release_namespace_prefix"),
                        MODULE.VERSION_NUM, MODULE.PATH)
                .from(MODULE_SET_RELEASE)
                .join(RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(MODULE).on(MODULE_SET_RELEASE.MODULE_SET_ID.eq(MODULE.MODULE_SET_ID))
                .leftJoin(NAMESPACE.as("module_namespace")).on(MODULE.NAMESPACE_ID.eq(NAMESPACE.as("module_namespace").NAMESPACE_ID))
                .leftJoin(NAMESPACE.as("release_namespace")).on(RELEASE.NAMESPACE_ID.eq(NAMESPACE.as("release_namespace").NAMESPACE_ID))
                .where(and(MODULE.TYPE.eq("FILE"), MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId))))
                .fetch(record -> {
                    ScoreModule scoreModule = new ScoreModule();
                    scoreModule.setModuleSetReleaseId(
                            new ModuleSetReleaseId(record.get(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID).toBigInteger()));
                    scoreModule.setModuleSetId(
                            new ModuleSetId(record.get(MODULE_SET_RELEASE.MODULE_SET_ID).toBigInteger()));
                    scoreModule.setReleaseId(
                            new ReleaseId(record.get(MODULE_SET_RELEASE.RELEASE_ID).toBigInteger()));
                    scoreModule.setModuleId(
                            new ModuleId(record.get(MODULE.MODULE_ID).toBigInteger()));
                    scoreModule.setName(record.get(MODULE.NAME));

                    ULong moduleNamespaceId = record.get(
                            MODULE.NAMESPACE_ID.as("module_namespace_id"));
                    String moduleNamespaceUri = record.get(
                            NAMESPACE.as("module_namespace").URI.as("module_namespace_uri"));
                    String moduleNamespacePrefix = record.get(
                            NAMESPACE.as("module_namespace").PREFIX.as("module_namespace_prefix"));
                    if (moduleNamespaceId != null) {
                        scoreModule.setModuleNamespace(new Namespace(
                                new NamespaceId(moduleNamespaceId.toBigInteger()), moduleNamespaceUri, moduleNamespacePrefix));
                    }

                    ULong releaseNamespaceId = record.get(
                            RELEASE.NAMESPACE_ID.as("release_namespace_id"));
                    String releaseNamespaceUri = record.get(
                            NAMESPACE.as("release_namespace").URI.as("release_namespace_uri"));
                    String releaseNamespacePrefix = record.get(
                            NAMESPACE.as("release_namespace").PREFIX.as("release_namespace_prefix"));
                    if (releaseNamespaceId != null) {
                        scoreModule.setReleaseNamespace(new Namespace(
                                new NamespaceId(releaseNamespaceId.toBigInteger()), releaseNamespaceUri, releaseNamespacePrefix));
                    }

                    scoreModule.setVersionNum(record.get(MODULE.VERSION_NUM));
                    scoreModule.setPath(record.get(MODULE.PATH));
                    return scoreModule;
                });
    }

}
