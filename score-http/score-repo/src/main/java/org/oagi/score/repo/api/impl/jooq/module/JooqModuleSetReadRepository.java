package org.oagi.score.repo.api.impl.jooq.module;

import org.jooq.Record;
import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.ModuleRecord;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.module.ModuleSetReadRepository;
import org.oagi.score.repo.api.module.model.Module;
import org.oagi.score.repo.api.module.model.*;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreRole;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.base.SortDirection.ASC;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.impl.jooq.utils.DSLUtils.contains;
import static org.oagi.score.repo.api.impl.jooq.utils.DSLUtils.isNull;
import static org.oagi.score.repo.api.impl.utils.StringUtils.trim;
import static org.oagi.score.repo.api.user.model.ScoreRole.*;

public class JooqModuleSetReadRepository
        extends JooqScoreRepository
        implements ModuleSetReadRepository {

    public JooqModuleSetReadRepository(DSLContext dslContext) {
        super(dslContext);
    }

    private SelectOnConditionStep select() {
        return dslContext().select(
                MODULE_SET.MODULE_SET_ID,
                MODULE_SET.GUID,
                MODULE_SET.NAME,
                MODULE_SET.DESCRIPTION,
                APP_USER.as("creator").APP_USER_ID.as("creator_user_id"),
                APP_USER.as("creator").LOGIN_ID.as("creator_login_id"),
                APP_USER.as("creator").NAME.as("creator_name"),
                APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer"),
                APP_USER.as("creator").IS_ADMIN.as("creator_is_admin"),
                APP_USER.as("updater").APP_USER_ID.as("updater_user_id"),
                APP_USER.as("updater").LOGIN_ID.as("updater_login_id"),
                APP_USER.as("updater").NAME.as("updater_name"),
                APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer"),
                APP_USER.as("updater").IS_ADMIN.as("updater_is_admin"),
                MODULE_SET.CREATION_TIMESTAMP,
                MODULE_SET.LAST_UPDATE_TIMESTAMP)
                .from(MODULE_SET)
                .join(APP_USER.as("creator")).on(MODULE_SET.CREATED_BY.eq(APP_USER.as("creator").APP_USER_ID))
                .join(APP_USER.as("updater")).on(MODULE_SET.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID));
    }

    private RecordMapper<Record, ModuleSet> mapper() {
        return record -> {
            ModuleSet moduleSet = new ModuleSet();
            moduleSet.setModuleSetId(record.get(MODULE_SET.MODULE_SET_ID).toBigInteger());
            moduleSet.setGuid(record.get(MODULE_SET.GUID));
            moduleSet.setName(record.get(MODULE_SET.NAME));
            moduleSet.setDescription(record.get(MODULE_SET.DESCRIPTION));

            ScoreRole creatorRole = (byte) 1 == record.get(APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer")) ? DEVELOPER : END_USER;
            boolean isCreatorAdmin = (byte) 1 == record.get(APP_USER.as("creator").IS_ADMIN.as("creator_is_admin"));
            moduleSet.setCreatedBy(
                    (isCreatorAdmin) ?
                            new ScoreUser(
                                    record.get(APP_USER.as("creator").APP_USER_ID.as("creator_user_id")).toBigInteger(),
                                    record.get(APP_USER.as("creator").LOGIN_ID.as("creator_login_id")),
                                    record.get(APP_USER.as("creator").NAME.as("creator_name")),
                                    Arrays.asList(creatorRole, ADMINISTRATOR)) :
                            new ScoreUser(
                                    record.get(APP_USER.as("creator").APP_USER_ID.as("creator_user_id")).toBigInteger(),
                                    record.get(APP_USER.as("creator").LOGIN_ID.as("creator_login_id")),
                                    record.get(APP_USER.as("creator").NAME.as("creator_name")),
                                    creatorRole));

            ScoreRole updaterRole = (byte) 1 == record.get(APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer")) ? DEVELOPER : END_USER;
            boolean isUpdaterAdmin = (byte) 1 == record.get(APP_USER.as("updater").IS_ADMIN.as("updater_is_admin"));
            moduleSet.setLastUpdatedBy(
                    (isUpdaterAdmin) ?
                            new ScoreUser(
                                    record.get(APP_USER.as("updater").APP_USER_ID.as("updater_user_id")).toBigInteger(),
                                    record.get(APP_USER.as("updater").LOGIN_ID.as("updater_login_id")),
                                    record.get(APP_USER.as("updater").NAME.as("updater_name")),
                                    Arrays.asList(updaterRole, ADMINISTRATOR)) :
                            new ScoreUser(
                                    record.get(APP_USER.as("updater").APP_USER_ID.as("updater_user_id")).toBigInteger(),
                                    record.get(APP_USER.as("updater").LOGIN_ID.as("updater_login_id")),
                                    record.get(APP_USER.as("updater").NAME.as("updater_name")),
                                    updaterRole));

            moduleSet.setCreationTimestamp(
                    Date.from(record.get(MODULE.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            moduleSet.setLastUpdateTimestamp(
                    Date.from(record.get(MODULE.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            return moduleSet;
        };
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetModuleSetResponse getModuleSet(GetModuleSetRequest request) throws ScoreDataAccessException {
        ModuleSet moduleSet = null;

        BigInteger moduleSetId = request.getModuleSetId();
        if (!isNull(moduleSetId)) {
            moduleSet = (ModuleSet) select()
                    .where(MODULE_SET.MODULE_SET_ID.eq(ULong.valueOf(moduleSetId)))
                    .fetchOne(mapper());
        }

        return new GetModuleSetResponse(moduleSet);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetModuleSetMetadataResponse getModuleSetMetadata(GetModuleSetMetadataRequest request) throws ScoreDataAccessException {
        ULong moduleSetId = ULong.valueOf(request.getModuleSetId());
        ModuleSetMetadata moduleSetMetadata = new ModuleSetMetadata();

        int numberOfDirectories = dslContext().selectCount()
                .from(MODULE)
                .where(and(
                        MODULE.MODULE_SET_ID.eq(moduleSetId),
                        MODULE.TYPE.eq("DIRECTORY")
                ))
                .fetchOptionalInto(Integer.class).orElse(0);
        moduleSetMetadata.setNumberOfDirectories(numberOfDirectories);

        int numberOfFiles = dslContext().selectCount()
                .from(MODULE)
                .where(and(
                        MODULE.MODULE_SET_ID.eq(moduleSetId),
                        MODULE.TYPE.eq("FILE")
                ))
                .fetchOptionalInto(Integer.class).orElse(0);
        moduleSetMetadata.setNumberOfFiles(numberOfFiles);

        return new GetModuleSetMetadataResponse(moduleSetMetadata);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetModuleSetListResponse getModuleSetList(GetModuleSetListRequest request) throws ScoreDataAccessException {
        Collection<Condition> conditions = getConditions(request);

        SelectConditionStep conditionStep;

        conditionStep = select().where(conditions);

        SortField sortField = getSortField(request);
        int length = dslContext().fetchCount(conditionStep);
        SelectFinalStep finalStep;
        if (sortField == null) {
            if (request.isPagination()) {
                finalStep = conditionStep.limit(request.getPageOffset(), request.getPageSize());
            } else {
                finalStep = conditionStep;
            }
        } else {
            if (request.isPagination()) {
                finalStep = conditionStep.orderBy(sortField)
                        .limit(request.getPageOffset(), request.getPageSize());
            } else {
                finalStep = conditionStep.orderBy(sortField);
            }
        }

        return new GetModuleSetListResponse(
                finalStep.fetch(mapper()),
                request.getPageIndex(),
                request.getPageSize(),
                length
        );
    }


    private Collection<Condition> getConditions(GetModuleSetListRequest request) {
        List<Condition> conditions = new ArrayList();

        if (StringUtils.hasLength(request.getName())) {
            conditions.addAll(contains(request.getName(), MODULE_SET.NAME));
        }

        if (!request.getUpdaterUsernameList().isEmpty()) {
            conditions.add(APP_USER.as("updater").LOGIN_ID.in(
                    new HashSet<>(request.getUpdaterUsernameList()).stream()
                            .filter(e -> StringUtils.hasLength(e)).map(e -> trim(e)).collect(Collectors.toList())
            ));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(MODULE_SET.LAST_UPDATE_TIMESTAMP.greaterOrEqual(request.getUpdateStartDate()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(MODULE_SET.LAST_UPDATE_TIMESTAMP.lessThan(request.getUpdateEndDate()));
        }

        return conditions;
    }

    private SortField getSortField(GetModuleSetListRequest request) {
        if (!StringUtils.hasLength(request.getSortActive())) {
            return null;
        }

        Field field;
        switch (trim(request.getSortActive()).toLowerCase()) {
            case "name":
                field = MODULE_SET.NAME;
                break;

            case "lastupdatetimestamp":
                field = MODULE_SET.LAST_UPDATE_TIMESTAMP;
                break;

            default:
                return null;
        }

        return (request.getSortDirection() == ASC) ? field.asc() : field.desc();
    }

    @Override
    public List<Module> getToplevelModules(BigInteger moduleSetId) throws ScoreDataAccessException {
        ModuleRecord rootModule = dslContext().selectFrom(MODULE)
                .where(and(MODULE.PARENT_MODULE_ID.isNull(), MODULE.MODULE_SET_ID.eq(ULong.valueOf(moduleSetId)))).fetchOne();
        return dslContext().select(
                MODULE.MODULE_ID,
                MODULE.PARENT_MODULE_ID,
                MODULE.PATH,
                MODULE.TYPE,
                MODULE.NAME,
                MODULE.VERSION_NUM,
                MODULE.NAMESPACE_ID,
                NAMESPACE.URI,
                MODULE.CREATION_TIMESTAMP,
                MODULE.LAST_UPDATE_TIMESTAMP)
                .from(MODULE)
                .leftJoin(NAMESPACE).on(NAMESPACE.NAMESPACE_ID.eq(MODULE.NAMESPACE_ID))
                .where(and(MODULE.MODULE_SET_ID.eq(ULong.valueOf(moduleSetId)), MODULE.PARENT_MODULE_ID.eq(rootModule.getModuleId())))
                .fetchStream().map(record -> {
                    Module module = new Module();
                    module.setModuleId(record.get(MODULE.MODULE_ID).toBigInteger());
                    if (record.get(MODULE.PARENT_MODULE_ID) != null) {
                        module.setParentModuleId(record.get(MODULE.PARENT_MODULE_ID).toBigInteger());
                    }
                    module.setPath(record.get(MODULE.PATH));
                    if (record.get(MODULE.NAMESPACE_ID) != null) {
                        module.setNamespaceId(record.get(MODULE.NAMESPACE_ID).toBigInteger());
                        module.setNamespaceUri(record.get(NAMESPACE.URI));
                    }
                    module.setName(record.get(MODULE.NAME));
                    module.setType(record.get(MODULE.TYPE));
                    module.setVersionNum(record.get(MODULE.VERSION_NUM));

                    module.setCreationTimestamp(
                            Date.from(record.get(MODULE.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
                    module.setLastUpdateTimestamp(
                            Date.from(record.get(MODULE.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
                    return module;
                }).collect(Collectors.toList());
    }

    @Override
    public List<Module> getAllModules(BigInteger moduleSetId) throws ScoreDataAccessException {
        return dslContext().select(
                MODULE.MODULE_ID,
                MODULE.PARENT_MODULE_ID,
                MODULE.PATH,
                MODULE.TYPE,
                MODULE.NAME,
                MODULE.VERSION_NUM,
                MODULE.NAMESPACE_ID,
                NAMESPACE.URI,
                MODULE.CREATION_TIMESTAMP,
                MODULE.LAST_UPDATE_TIMESTAMP)
                .from(MODULE)
                .leftJoin(NAMESPACE).on(NAMESPACE.NAMESPACE_ID.eq(MODULE.NAMESPACE_ID))
                .where(MODULE.MODULE_SET_ID.eq(ULong.valueOf(moduleSetId)))
                .fetchStream().map(record -> {
                    Module module = new Module();
                    module.setModuleId(record.get(MODULE.MODULE_ID).toBigInteger());
                    if (record.get(MODULE.PARENT_MODULE_ID) != null) {
                        module.setParentModuleId(record.get(MODULE.PARENT_MODULE_ID).toBigInteger());
                    }
                    module.setPath(record.get(MODULE.PATH));
                    if (record.get(MODULE.NAMESPACE_ID) != null) {
                        module.setNamespaceId(record.get(MODULE.NAMESPACE_ID).toBigInteger());
                        module.setNamespaceUri(record.get(NAMESPACE.URI));
                    }
                    module.setName(record.get(MODULE.NAME));
                    module.setType(record.get(MODULE.TYPE));
                    module.setVersionNum(record.get(MODULE.VERSION_NUM));

                    module.setCreationTimestamp(
                            Date.from(record.get(MODULE.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
                    module.setLastUpdateTimestamp(
                            Date.from(record.get(MODULE.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
                    return module;
                }).collect(Collectors.toList());
    }
}
