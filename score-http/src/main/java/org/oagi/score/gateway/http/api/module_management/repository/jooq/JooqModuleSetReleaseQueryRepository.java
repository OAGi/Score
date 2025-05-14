package org.oagi.score.gateway.http.api.module_management.repository.jooq;

import org.jooq.*;
import org.jooq.Record;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.blob_content.BlobContentManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.module_management.model.*;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleSetReleaseQueryRepository;
import org.oagi.score.gateway.http.api.module_management.repository.criteria.ModuleSetReleaseListFilterCriteria;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.common.model.*;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.model.SortDirection.DESC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.util.DSLUtils.contains;


public class JooqModuleSetReleaseQueryRepository extends JooqBaseRepository implements ModuleSetReleaseQueryRepository {

    public JooqModuleSetReleaseQueryRepository(DSLContext dslContext, ScoreUser requester,
                                               RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public List<ModuleSetReleaseSummaryRecord> getModuleSetReleaseSummaryList(LibraryId libraryId) {
        var queryBuilder = new GetModuleSetReleaseSummaryListQueryBuilder();
        return queryBuilder.select()
                .where(LIBRARY.LIBRARY_ID.eq(valueOf(libraryId)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<ModuleSetReleaseSummaryRecord> getModuleSetReleaseSummaryList(ReleaseId releaseId) {
        var queryBuilder = new GetModuleSetReleaseSummaryListQueryBuilder();
        return queryBuilder.select()
                .where(RELEASE.RELEASE_ID.eq(valueOf(releaseId)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public ModuleSetReleaseSummaryRecord getDefaultModuleSetReleaseSummary(ReleaseId releaseId) {
        var queryBuilder = new GetModuleSetReleaseSummaryListQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        MODULE_SET_RELEASE.IS_DEFAULT.eq((byte) 1),
                        MODULE_SET_RELEASE.RELEASE_ID.eq(valueOf(releaseId))
                ))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetModuleSetReleaseSummaryListQueryBuilder {

        SelectOnConditionStep<? extends Record> select() {
            return dslContext().select(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID,
                            MODULE_SET_RELEASE.NAME.as("module_set_release_name"),
                            MODULE_SET_RELEASE.DESCRIPTION,
                            MODULE_SET_RELEASE.IS_DEFAULT,

                            MODULE_SET.MODULE_SET_ID,
                            MODULE_SET.GUID,
                            MODULE_SET.NAME.as("module_set_name"),
                            MODULE_SET.DESCRIPTION.as("module_set_description"),

                            LIBRARY.LIBRARY_ID,
                            LIBRARY.NAME.as("library_name"),
                            LIBRARY.STATE.as("library_state"),
                            LIBRARY.IS_READ_ONLY,

                            RELEASE.RELEASE_ID,
                            RELEASE.RELEASE_NUM,
                            RELEASE.STATE.as("release_state"))
                    .from(MODULE_SET_RELEASE)
                    .join(RELEASE).on(RELEASE.RELEASE_ID.eq(MODULE_SET_RELEASE.RELEASE_ID))
                    .join(MODULE_SET).on(MODULE_SET.MODULE_SET_ID.eq(MODULE_SET_RELEASE.MODULE_SET_ID))
                    .join(LIBRARY).on(and(
                            RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID),
                            MODULE_SET.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID)
                    ));
        }

        RecordMapper<Record, ModuleSetReleaseSummaryRecord> mapper() {
            return record -> {
                LibrarySummaryRecord library = new LibrarySummaryRecord(
                        new LibraryId(record.get(LIBRARY.LIBRARY_ID).toBigInteger()),
                        record.get(LIBRARY.NAME.as("library_name")),
                        record.get(LIBRARY.STATE.as("library_state")),
                        (byte) 1 == record.get(LIBRARY.IS_READ_ONLY)
                );
                ReleaseSummaryRecord release = new ReleaseSummaryRecord(
                        new ReleaseId(record.get(RELEASE.RELEASE_ID).toBigInteger()),
                        new LibraryId(record.get(LIBRARY.LIBRARY_ID).toBigInteger()),
                        record.get(RELEASE.RELEASE_NUM),
                        ReleaseState.valueOf(record.get(RELEASE.STATE.as("release_state")))
                );
                ModuleSetSummaryRecord moduleSet = new ModuleSetSummaryRecord(
                        new ModuleSetId(record.get(MODULE_SET.MODULE_SET_ID).toBigInteger()),
                        library.libraryId(),
                        new Guid(record.get(MODULE_SET.GUID)),
                        record.get(MODULE_SET.NAME.as("module_set_name")),
                        record.get(MODULE_SET.DESCRIPTION.as("module_set_description"))
                );

                return new ModuleSetReleaseSummaryRecord(
                        library, release, moduleSet,
                        new ModuleSetReleaseId(record.get(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID).toBigInteger()),
                        record.get(MODULE_SET_RELEASE.NAME.as("module_set_release_name")),
                        record.get(MODULE_SET_RELEASE.DESCRIPTION),
                        record.get(MODULE_SET_RELEASE.IS_DEFAULT) == 1
                );
            };
        }
    }

    @Override
    public ModuleSetReleaseDetailsRecord getModuleSetReleaseDetails(ModuleSetReleaseId moduleSetReleaseId) {
        var queryBuilder = new GetModuleSetReleaseDetailsQueryBuilder();
        return queryBuilder.select()
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetModuleSetReleaseDetailsQueryBuilder {

        SelectOnConditionStep<? extends Record> select() {
            return dslContext().select(concat(fields(
                            MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID,
                            MODULE_SET_RELEASE.NAME.as("module_set_release_name"),
                            MODULE_SET_RELEASE.DESCRIPTION,
                            MODULE_SET_RELEASE.IS_DEFAULT,

                            MODULE_SET.MODULE_SET_ID,
                            MODULE_SET.GUID,
                            MODULE_SET.NAME.as("module_set_name"),
                            MODULE_SET.DESCRIPTION.as("module_set_description"),

                            LIBRARY.LIBRARY_ID,
                            LIBRARY.NAME.as("library_name"),
                            LIBRARY.STATE.as("library_state"),
                            LIBRARY.IS_READ_ONLY,

                            RELEASE.RELEASE_ID,
                            RELEASE.RELEASE_NUM,
                            RELEASE.STATE.as("release_state"),

                            MODULE_SET_RELEASE.CREATION_TIMESTAMP,
                            MODULE_SET_RELEASE.LAST_UPDATE_TIMESTAMP
                    ), creatorFields(), updaterFields()))
                    .from(MODULE_SET_RELEASE)
                    .join(RELEASE).on(RELEASE.RELEASE_ID.eq(MODULE_SET_RELEASE.RELEASE_ID))
                    .join(MODULE_SET).on(MODULE_SET.MODULE_SET_ID.eq(MODULE_SET_RELEASE.MODULE_SET_ID))
                    .join(LIBRARY).on(and(
                            RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID),
                            MODULE_SET.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID)
                    ))
                    .join(creatorTable()).on(MODULE_SET_RELEASE.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(MODULE_SET_RELEASE.LAST_UPDATED_BY.eq(updaterTablePk()));
        }

        RecordMapper<Record, ModuleSetReleaseDetailsRecord> mapper() {
            return record -> {
                LibrarySummaryRecord library = new LibrarySummaryRecord(
                        new LibraryId(record.get(LIBRARY.LIBRARY_ID).toBigInteger()),
                        record.get(LIBRARY.NAME.as("library_name")),
                        record.get(LIBRARY.STATE.as("library_state")),
                        (byte) 1 == record.get(LIBRARY.IS_READ_ONLY)
                );
                ReleaseSummaryRecord release = new ReleaseSummaryRecord(
                        new ReleaseId(record.get(RELEASE.RELEASE_ID).toBigInteger()),
                        new LibraryId(record.get(LIBRARY.LIBRARY_ID).toBigInteger()),
                        record.get(RELEASE.RELEASE_NUM),
                        ReleaseState.valueOf(record.get(RELEASE.STATE.as("release_state")))
                );
                ModuleSetSummaryRecord moduleSet = new ModuleSetSummaryRecord(
                        new ModuleSetId(record.get(MODULE_SET.MODULE_SET_ID).toBigInteger()),
                        library.libraryId(),
                        new Guid(record.get(MODULE_SET.GUID)),
                        record.get(MODULE_SET.NAME.as("module_set_name")),
                        record.get(MODULE_SET.DESCRIPTION.as("module_set_description"))
                );

                return new ModuleSetReleaseDetailsRecord(
                        library, release, moduleSet,
                        new ModuleSetReleaseId(record.get(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID).toBigInteger()),
                        record.get(MODULE_SET_RELEASE.NAME.as("module_set_release_name")),
                        record.get(MODULE_SET_RELEASE.DESCRIPTION),
                        record.get(MODULE_SET_RELEASE.IS_DEFAULT) == 1,
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(MODULE_SET_RELEASE.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(MODULE_SET_RELEASE.LAST_UPDATE_TIMESTAMP))
                        )
                );
            };
        }
    }

    @Override
    public ResultAndCount<ModuleSetReleaseListEntryRecord> getModuleSetReleaseList(ModuleSetReleaseListFilterCriteria filterCriteria, PageRequest pageRequest) {

        var queryBuilder = new GetModuleSetReleaseListQueryBuilder();
        var where = queryBuilder.select().where(
                queryBuilder.conditions(filterCriteria));
        int count = dslContext().fetchCount(where);
        List<ModuleSetReleaseListEntryRecord> result = queryBuilder.fetch(where, pageRequest);
        return new ResultAndCount(result, count);
    }

    private class GetModuleSetReleaseListQueryBuilder {

        SelectOnConditionStep<? extends Record> select() {
            return dslContext().select(concat(fields(
                            MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID,
                            MODULE_SET_RELEASE.MODULE_SET_ID,
                            MODULE_SET_RELEASE.NAME.as("module_set_release_name"),
                            MODULE_SET_RELEASE.DESCRIPTION,
                            MODULE_SET_RELEASE.RELEASE_ID,
                            MODULE_SET.NAME.as("module_set_name"),
                            RELEASE.RELEASE_NUM,
                            LIBRARY.LIBRARY_ID,
                            LIBRARY.NAME.as("library_name"),
                            MODULE_SET_RELEASE.IS_DEFAULT,
                            MODULE_SET_RELEASE.CREATION_TIMESTAMP,
                            MODULE_SET_RELEASE.LAST_UPDATE_TIMESTAMP
                    ), creatorFields(), updaterFields()))
                    .from(MODULE_SET_RELEASE)
                    .join(RELEASE).on(RELEASE.RELEASE_ID.eq(MODULE_SET_RELEASE.RELEASE_ID))
                    .join(MODULE_SET).on(MODULE_SET.MODULE_SET_ID.eq(MODULE_SET_RELEASE.MODULE_SET_ID))
                    .join(LIBRARY).on(and(
                            RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID),
                            MODULE_SET.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID)
                    ))
                    .join(creatorTable()).on(MODULE_SET_RELEASE.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(MODULE_SET_RELEASE.LAST_UPDATED_BY.eq(updaterTablePk()));
        }

        public List<Condition> conditions(ModuleSetReleaseListFilterCriteria filterCriteria) {
            List<Condition> conditions = new ArrayList();

            conditions.add(LIBRARY.LIBRARY_ID.eq(valueOf(filterCriteria.libraryId())));

            if (StringUtils.hasLength(filterCriteria.name())) {
                conditions.addAll(contains(filterCriteria.name(), MODULE_SET_RELEASE.NAME));
            }
            if (filterCriteria.releaseId() != null) {
                conditions.add(MODULE_SET_RELEASE.RELEASE_ID.eq(valueOf(filterCriteria.releaseId())));
            }
            if (filterCriteria.isDefault() != null) {
                conditions.add(MODULE_SET_RELEASE.IS_DEFAULT.eq((byte) (filterCriteria.isDefault() == true ? 1 : 0)));
            }
            if (filterCriteria.updaterLoginIdSet() != null && !filterCriteria.updaterLoginIdSet().isEmpty()) {
                conditions.add(updaterTable().LOGIN_ID.in(filterCriteria.updaterLoginIdSet()));
            }
            if (filterCriteria.lastUpdatedTimestampRange() != null) {
                if (filterCriteria.lastUpdatedTimestampRange().after() != null) {
                    conditions.add(MODULE_SET.LAST_UPDATE_TIMESTAMP.greaterOrEqual(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().after().getTime()).toLocalDateTime()));
                }
                if (filterCriteria.lastUpdatedTimestampRange().before() != null) {
                    conditions.add(MODULE_SET.LAST_UPDATE_TIMESTAMP.lessThan(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().before().getTime()).toLocalDateTime()));
                }
            }
            return conditions;
        }

        public List<SortField<?>> sortFields(PageRequest pageRequest) {
            List<SortField<?>> sortFields = new ArrayList<>();

            for (Sort sort : pageRequest.sorts()) {
                Field field;
                switch (sort.field()) {
                    case "name":
                        field = MODULE_SET_RELEASE.NAME.as("module_set_release_name");
                        break;

                    case "release":
                    case "releaseNum":
                        field = RELEASE.RELEASE_NUM;
                        break;

                    case "default":
                        field = MODULE_SET_RELEASE.IS_DEFAULT;
                        break;

                    case "lastUpdateTimestamp":
                        field = MODULE_SET_RELEASE.LAST_UPDATE_TIMESTAMP;
                        break;

                    default:
                        continue;
                }

                if (sort.direction() == DESC) {
                    sortFields.add(field.desc());
                } else {
                    sortFields.add(field.asc());
                }
            }

            return sortFields;
        }

        public List<ModuleSetReleaseListEntryRecord> fetch(SelectConditionStep<?> conditionStep, PageRequest pageRequest) {
            var sortFields = sortFields(pageRequest);
            SelectFinalStep<? extends Record> finalStep;
            if (sortFields == null || sortFields.isEmpty()) {
                if (pageRequest.isPagination()) {
                    finalStep = conditionStep.limit(pageRequest.pageOffset(), pageRequest.pageSize());
                } else {
                    finalStep = conditionStep;
                }
            } else {
                if (pageRequest.isPagination()) {
                    finalStep = conditionStep.orderBy(sortFields)
                            .limit(pageRequest.pageOffset(), pageRequest.pageSize());
                } else {
                    finalStep = conditionStep.orderBy(sortFields);
                }
            }
            return finalStep.fetch(mapper());
        }

        RecordMapper<Record, ModuleSetReleaseListEntryRecord> mapper() {
            return record -> new ModuleSetReleaseListEntryRecord(
                    new ModuleSetReleaseId(record.get(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID).toBigInteger()),
                    new ModuleSetId(record.get(MODULE_SET_RELEASE.MODULE_SET_ID).toBigInteger()),
                    record.get(MODULE_SET.NAME.as("module_set_name")),
                    new LibraryId(record.get(LIBRARY.LIBRARY_ID).toBigInteger()),
                    record.get(LIBRARY.NAME.as("library_name")),
                    new ReleaseId(record.get(MODULE_SET_RELEASE.RELEASE_ID).toBigInteger()),
                    record.get(RELEASE.RELEASE_NUM),
                    record.get(MODULE_SET_RELEASE.NAME.as("module_set_release_name")),
                    record.get(MODULE_SET_RELEASE.DESCRIPTION),
                    record.get(MODULE_SET_RELEASE.IS_DEFAULT) == 1,
                    new WhoAndWhen(
                            fetchCreatorSummary(record),
                            Date.from(record.get(MODULE_SET_RELEASE.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant())
                    ),
                    new WhoAndWhen(
                            fetchUpdaterSummary(record),
                            Date.from(record.get(MODULE_SET_RELEASE.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant())
                    )
            );
        }

    }

    @Override
    public boolean exists(ReleaseId releaseId) {

        return dslContext().selectCount()
                .from(MODULE_SET_RELEASE)
                .where(MODULE_SET_RELEASE.RELEASE_ID.eq(valueOf(releaseId)))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    @Override
    public boolean exists(ModuleSetId moduleSetId) {

        return dslContext().selectCount()
                .from(MODULE_SET_RELEASE)
                .where(MODULE_SET_RELEASE.MODULE_SET_ID.eq(valueOf(moduleSetId)))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    @Override
    public String getDefaultName(ModuleSetId moduleSetId) {
        String moduleSetReleaseName = dslContext().select(MODULE_SET.NAME)
                .from(MODULE_SET)
                .where(MODULE_SET.MODULE_SET_ID.eq(valueOf(moduleSetId)))
                .fetchOneInto(String.class);
        // Issue #1276
        return concatWithDuplicateElimination(moduleSetReleaseName, "Module Set Release");
    }

    private String concatWithDuplicateElimination(String str1, String str2) {
        String[] str2Tokens = str2.split(" ");
        String partialStr2 = "";
        for (int i = 0, len = str2Tokens.length; i < len; ++i) {
            partialStr2 += ((i == 0) ? "" : " ") + str2Tokens[i];
            if (str1.endsWith(partialStr2)) {
                return str1 + " " + String.join(" ", Arrays.copyOfRange(str2Tokens, i + 1, len));
            }
        }
        return str1 + " " + str2;
    }

    @Override
    public List<ModuleAccRecord> getModuleAccList(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().select(concat(fields(
                        MODULE_ACC_MANIFEST.MODULE_ACC_MANIFEST_ID,
                        MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID,
                        MODULE_ACC_MANIFEST.ACC_MANIFEST_ID,
                        MODULE_ACC_MANIFEST.MODULE_ID,
                        MODULE.PATH,
                        MODULE_ACC_MANIFEST.CREATION_TIMESTAMP,
                        MODULE_ACC_MANIFEST.LAST_UPDATE_TIMESTAMP
                ), creatorFields(), updaterFields()))
                .from(MODULE_ACC_MANIFEST)
                .join(MODULE).on(MODULE_ACC_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .join(creatorTable()).on(creatorTablePk().eq(MODULE_ACC_MANIFEST.CREATED_BY))
                .join(updaterTable()).on(updaterTablePk().eq(MODULE_ACC_MANIFEST.LAST_UPDATED_BY))
                .where(MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)))
                .fetch(record -> new ModuleAccRecord(
                        new ModuleAccManifestId(record.get(MODULE_ACC_MANIFEST.MODULE_ACC_MANIFEST_ID).toBigInteger()),
                        moduleSetReleaseId,
                        new AccManifestId(record.get(MODULE_ACC_MANIFEST.ACC_MANIFEST_ID).toBigInteger()),
                        new ModuleId(record.get(MODULE_ACC_MANIFEST.MODULE_ID).toBigInteger()),
                        record.get(MODULE.PATH),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(MODULE_ACC_MANIFEST.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(MODULE_ACC_MANIFEST.LAST_UPDATE_TIMESTAMP))
                        ))
                );
    }

    @Override
    public List<ModuleAsccpRecord> getModuleAsccpList(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().select(concat(fields(
                        MODULE_ASCCP_MANIFEST.MODULE_ASCCP_MANIFEST_ID,
                        MODULE_ASCCP_MANIFEST.MODULE_SET_RELEASE_ID,
                        MODULE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                        MODULE_ASCCP_MANIFEST.MODULE_ID,
                        MODULE.PATH,
                        MODULE_ASCCP_MANIFEST.CREATION_TIMESTAMP,
                        MODULE_ASCCP_MANIFEST.LAST_UPDATE_TIMESTAMP
                ), creatorFields(), updaterFields()))
                .from(MODULE_ASCCP_MANIFEST)
                .join(MODULE).on(MODULE_ASCCP_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .join(creatorTable()).on(creatorTablePk().eq(MODULE_ASCCP_MANIFEST.CREATED_BY))
                .join(updaterTable()).on(updaterTablePk().eq(MODULE_ASCCP_MANIFEST.LAST_UPDATED_BY))
                .where(MODULE_ASCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)))
                .fetch(record -> new ModuleAsccpRecord(
                        new ModuleAsccpManifestId(record.get(MODULE_ASCCP_MANIFEST.MODULE_ASCCP_MANIFEST_ID).toBigInteger()),
                        moduleSetReleaseId,
                        new AsccpManifestId(record.get(MODULE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID).toBigInteger()),
                        new ModuleId(record.get(MODULE_ASCCP_MANIFEST.MODULE_ID).toBigInteger()),
                        record.get(MODULE.PATH),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(MODULE_ASCCP_MANIFEST.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(MODULE_ASCCP_MANIFEST.LAST_UPDATE_TIMESTAMP))
                        ))
                );
    }

    @Override
    public List<ModuleBccpRecord> getModuleBccpList(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().select(concat(fields(
                        MODULE_BCCP_MANIFEST.MODULE_BCCP_MANIFEST_ID,
                        MODULE_BCCP_MANIFEST.MODULE_SET_RELEASE_ID,
                        MODULE_BCCP_MANIFEST.BCCP_MANIFEST_ID,
                        MODULE_BCCP_MANIFEST.MODULE_ID,
                        MODULE.PATH,
                        MODULE_BCCP_MANIFEST.CREATION_TIMESTAMP,
                        MODULE_BCCP_MANIFEST.LAST_UPDATE_TIMESTAMP
                ), creatorFields(), updaterFields()))
                .from(MODULE_BCCP_MANIFEST)
                .join(MODULE).on(MODULE_BCCP_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .join(creatorTable()).on(creatorTablePk().eq(MODULE_BCCP_MANIFEST.CREATED_BY))
                .join(updaterTable()).on(updaterTablePk().eq(MODULE_BCCP_MANIFEST.LAST_UPDATED_BY))
                .where(MODULE_BCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)))
                .fetch(record -> new ModuleBccpRecord(
                        new ModuleBccpManifestId(record.get(MODULE_BCCP_MANIFEST.MODULE_BCCP_MANIFEST_ID).toBigInteger()),
                        moduleSetReleaseId,
                        new BccpManifestId(record.get(MODULE_BCCP_MANIFEST.BCCP_MANIFEST_ID).toBigInteger()),
                        new ModuleId(record.get(MODULE_BCCP_MANIFEST.MODULE_ID).toBigInteger()),
                        record.get(MODULE.PATH),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(MODULE_BCCP_MANIFEST.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(MODULE_BCCP_MANIFEST.LAST_UPDATE_TIMESTAMP))
                        ))
                );
    }

    @Override
    public List<ModuleDtRecord> getModuleDtList(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().select(concat(fields(
                        MODULE_DT_MANIFEST.MODULE_DT_MANIFEST_ID,
                        MODULE_DT_MANIFEST.MODULE_SET_RELEASE_ID,
                        MODULE_DT_MANIFEST.DT_MANIFEST_ID,
                        MODULE_DT_MANIFEST.MODULE_ID,
                        MODULE.PATH,
                        MODULE_DT_MANIFEST.CREATION_TIMESTAMP,
                        MODULE_DT_MANIFEST.LAST_UPDATE_TIMESTAMP
                ), creatorFields(), updaterFields()))
                .from(MODULE_DT_MANIFEST)
                .join(MODULE).on(MODULE_DT_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .join(creatorTable()).on(creatorTablePk().eq(MODULE_DT_MANIFEST.CREATED_BY))
                .join(updaterTable()).on(updaterTablePk().eq(MODULE_DT_MANIFEST.LAST_UPDATED_BY))
                .where(MODULE_DT_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)))
                .fetch(record -> new ModuleDtRecord(
                        new ModuleDtManifestId(record.get(MODULE_DT_MANIFEST.MODULE_DT_MANIFEST_ID).toBigInteger()),
                        moduleSetReleaseId,
                        new DtManifestId(record.get(MODULE_DT_MANIFEST.DT_MANIFEST_ID).toBigInteger()),
                        new ModuleId(record.get(MODULE_DT_MANIFEST.MODULE_ID).toBigInteger()),
                        record.get(MODULE.PATH),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(MODULE_DT_MANIFEST.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(MODULE_DT_MANIFEST.LAST_UPDATE_TIMESTAMP))
                        ))
                );
    }

    @Override
    public List<ModuleCodeListRecord> getModuleCodeListList(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().select(concat(fields(
                        MODULE_CODE_LIST_MANIFEST.MODULE_CODE_LIST_MANIFEST_ID,
                        MODULE_CODE_LIST_MANIFEST.MODULE_SET_RELEASE_ID,
                        MODULE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,
                        MODULE_CODE_LIST_MANIFEST.MODULE_ID,
                        MODULE.PATH,
                        MODULE_CODE_LIST_MANIFEST.CREATION_TIMESTAMP,
                        MODULE_CODE_LIST_MANIFEST.LAST_UPDATE_TIMESTAMP
                ), creatorFields(), updaterFields()))
                .from(MODULE_CODE_LIST_MANIFEST)
                .join(MODULE).on(MODULE_CODE_LIST_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .join(creatorTable()).on(creatorTablePk().eq(MODULE_CODE_LIST_MANIFEST.CREATED_BY))
                .join(updaterTable()).on(updaterTablePk().eq(MODULE_CODE_LIST_MANIFEST.LAST_UPDATED_BY))
                .where(MODULE_CODE_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)))
                .fetch(record -> new ModuleCodeListRecord(
                        new ModuleCodeListManifestId(record.get(MODULE_CODE_LIST_MANIFEST.MODULE_CODE_LIST_MANIFEST_ID).toBigInteger()),
                        moduleSetReleaseId,
                        new CodeListManifestId(record.get(MODULE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID).toBigInteger()),
                        new ModuleId(record.get(MODULE_CODE_LIST_MANIFEST.MODULE_ID).toBigInteger()),
                        record.get(MODULE.PATH),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(MODULE_CODE_LIST_MANIFEST.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(MODULE_CODE_LIST_MANIFEST.LAST_UPDATE_TIMESTAMP))
                        ))
                );
    }

    @Override
    public List<ModuleAgencyIdListRecord> getModuleAgencyIdListList(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().select(concat(fields(
                        MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_AGENCY_ID_LIST_MANIFEST_ID,
                        MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_SET_RELEASE_ID,
                        MODULE_AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                        MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_ID,
                        MODULE.PATH,
                        MODULE_AGENCY_ID_LIST_MANIFEST.CREATION_TIMESTAMP,
                        MODULE_AGENCY_ID_LIST_MANIFEST.LAST_UPDATE_TIMESTAMP
                ), creatorFields(), updaterFields()))
                .from(MODULE_AGENCY_ID_LIST_MANIFEST)
                .join(MODULE).on(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .join(creatorTable()).on(creatorTablePk().eq(MODULE_AGENCY_ID_LIST_MANIFEST.CREATED_BY))
                .join(updaterTable()).on(updaterTablePk().eq(MODULE_AGENCY_ID_LIST_MANIFEST.LAST_UPDATED_BY))
                .where(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)))
                .fetch(record -> new ModuleAgencyIdListRecord(
                        new ModuleAgencyIdListManifestId(record.get(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()),
                        moduleSetReleaseId,
                        new AgencyIdListManifestId(record.get(MODULE_AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()),
                        new ModuleId(record.get(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_ID).toBigInteger()),
                        record.get(MODULE.PATH),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(MODULE_AGENCY_ID_LIST_MANIFEST.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(MODULE_AGENCY_ID_LIST_MANIFEST.LAST_UPDATE_TIMESTAMP))
                        ))
                );
    }

    @Override
    public List<ModuleXbtRecord> getModuleXbtList(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().select(concat(fields(
                        MODULE_XBT_MANIFEST.MODULE_XBT_MANIFEST_ID,
                        MODULE_XBT_MANIFEST.MODULE_SET_RELEASE_ID,
                        MODULE_XBT_MANIFEST.XBT_MANIFEST_ID,
                        MODULE_XBT_MANIFEST.MODULE_ID,
                        MODULE.PATH,
                        MODULE_XBT_MANIFEST.CREATION_TIMESTAMP,
                        MODULE_XBT_MANIFEST.LAST_UPDATE_TIMESTAMP
                ), creatorFields(), updaterFields()))
                .from(MODULE_XBT_MANIFEST)
                .join(MODULE).on(MODULE_XBT_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .join(creatorTable()).on(creatorTablePk().eq(MODULE_XBT_MANIFEST.CREATED_BY))
                .join(updaterTable()).on(updaterTablePk().eq(MODULE_XBT_MANIFEST.LAST_UPDATED_BY))
                .where(MODULE_XBT_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)))
                .fetch(record -> new ModuleXbtRecord(
                        new ModuleXbtManifestId(record.get(MODULE_XBT_MANIFEST.MODULE_XBT_MANIFEST_ID).toBigInteger()),
                        moduleSetReleaseId,
                        new XbtManifestId(record.get(MODULE_XBT_MANIFEST.XBT_MANIFEST_ID).toBigInteger()),
                        new ModuleId(record.get(MODULE_XBT_MANIFEST.MODULE_ID).toBigInteger()),
                        record.get(MODULE.PATH),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(MODULE_XBT_MANIFEST.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(MODULE_XBT_MANIFEST.LAST_UPDATE_TIMESTAMP))
                        ))
                );
    }

    @Override
    public List<ModuleBlobContentRecord> getModuleBlobContentList(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().select(concat(fields(
                        MODULE_BLOB_CONTENT_MANIFEST.MODULE_BLOB_CONTENT_MANIFEST_ID,
                        MODULE_BLOB_CONTENT_MANIFEST.MODULE_SET_RELEASE_ID,
                        MODULE_BLOB_CONTENT_MANIFEST.BLOB_CONTENT_MANIFEST_ID,
                        MODULE_BLOB_CONTENT_MANIFEST.MODULE_ID,
                        MODULE.PATH,
                        MODULE_BLOB_CONTENT_MANIFEST.CREATION_TIMESTAMP,
                        MODULE_BLOB_CONTENT_MANIFEST.LAST_UPDATE_TIMESTAMP
                ), creatorFields(), updaterFields()))
                .from(MODULE_BLOB_CONTENT_MANIFEST)
                .join(MODULE).on(MODULE_BLOB_CONTENT_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .join(creatorTable()).on(creatorTablePk().eq(MODULE_BLOB_CONTENT_MANIFEST.CREATED_BY))
                .join(updaterTable()).on(updaterTablePk().eq(MODULE_BLOB_CONTENT_MANIFEST.LAST_UPDATED_BY))
                .where(MODULE_BLOB_CONTENT_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)))
                .fetch(record -> new ModuleBlobContentRecord(
                        new ModuleBlobContentManifestId(record.get(MODULE_BLOB_CONTENT_MANIFEST.MODULE_BLOB_CONTENT_MANIFEST_ID).toBigInteger()),
                        moduleSetReleaseId,
                        new BlobContentManifestId(record.get(MODULE_BLOB_CONTENT_MANIFEST.BLOB_CONTENT_MANIFEST_ID).toBigInteger()),
                        new ModuleId(record.get(MODULE_BLOB_CONTENT_MANIFEST.MODULE_ID).toBigInteger()),
                        record.get(MODULE.PATH),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(MODULE_BLOB_CONTENT_MANIFEST.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(MODULE_BLOB_CONTENT_MANIFEST.LAST_UPDATE_TIMESTAMP))
                        ))
                );
    }

}
