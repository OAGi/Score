package org.oagi.score.repo.api.impl.jooq.module;

import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.module.model.*;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.module.ModuleReadRepository;
import org.oagi.score.repo.api.module.model.Module;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.ZoneId;
import java.util.*;

import static org.oagi.score.repo.api.base.SortDirection.ASC;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.impl.jooq.utils.DSLUtils.contains;
import static org.oagi.score.repo.api.impl.jooq.utils.DSLUtils.isNull;
import static org.oagi.score.repo.api.impl.utils.StringUtils.trim;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

public class JooqModuleReadRepository
        extends JooqScoreRepository
        implements ModuleReadRepository {

    public JooqModuleReadRepository(DSLContext dslContext) {
        super(dslContext);
    }

    private SelectOnConditionStep select() {
        return dslContext().select(
                MODULE.MODULE_ID,
                MODULE.MODULE_DIR_ID,
                MODULE_DIR.PATH,
                MODULE_DIR.NAME.as("dir_name"),
                MODULE.NAME,
                MODULE.VERSION_NUM,
                MODULE.NAMESPACE_ID,
                NAMESPACE.URI,
                APP_USER.as("creator").APP_USER_ID.as("creator_user_id"),
                APP_USER.as("creator").LOGIN_ID.as("creator_login_id"),
                APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer"),
                APP_USER.as("updater").APP_USER_ID.as("updater_user_id"),
                APP_USER.as("updater").LOGIN_ID.as("updater_login_id"),
                APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer"),
                MODULE.CREATION_TIMESTAMP,
                MODULE.LAST_UPDATE_TIMESTAMP,
                MODULE_SET_ASSIGNMENT.MODULE_SET_ASSIGNMENT_ID)
                .from(MODULE)
                .join(MODULE_DIR).on(MODULE.MODULE_DIR_ID.eq(MODULE_DIR.MODULE_DIR_ID))
                .join(NAMESPACE).on(NAMESPACE.NAMESPACE_ID.eq(MODULE.NAMESPACE_ID))
                .join(APP_USER.as("creator")).on(MODULE.CREATED_BY.eq(APP_USER.as("creator").APP_USER_ID))
                .join(APP_USER.as("updater")).on(MODULE.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID))
                .leftJoin(MODULE_SET_ASSIGNMENT).on(MODULE.MODULE_ID.eq(MODULE_SET_ASSIGNMENT.MODULE_ID));
    }

    private RecordMapper<Record, Module> mapper() {
        return record -> {
            Module module = new Module();
            module.setModuleId(record.get(MODULE.MODULE_ID).toBigInteger());
            module.setModuleDirId(record.get(MODULE.MODULE_DIR_ID).toBigInteger());
            module.setPath(record.get(MODULE_DIR.PATH).concat("\\").concat(record.get(MODULE.NAME)));
            module.setNamespaceUri(record.get(NAMESPACE.URI));
            module.setName(record.get(MODULE.NAME));
            module.setVersionNum(record.get(MODULE.VERSION_NUM));

            module.setCreatedBy(new ScoreUser(
                    record.get(APP_USER.as("creator").APP_USER_ID.as("creator_user_id")).toBigInteger(),
                    record.get(APP_USER.as("creator").LOGIN_ID.as("creator_login_id")),
                    (byte) 1 == record.get(APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer")) ? DEVELOPER : END_USER
            ));
            module.setLastUpdatedBy(new ScoreUser(
                    record.get(APP_USER.as("updater").APP_USER_ID.as("updater_user_id")).toBigInteger(),
                    record.get(APP_USER.as("updater").LOGIN_ID.as("updater_login_id")),
                    (byte) 1 == record.get(APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer")) ? DEVELOPER : END_USER
            ));
            module.setCreationTimestamp(
                    Date.from(record.get(MODULE.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            module.setLastUpdateTimestamp(
                    Date.from(record.get(MODULE.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            return module;
        };
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetModuleResponse getModule(GetModuleRequest request) throws ScoreDataAccessException {
        Module module = null;

        BigInteger moduleId = request.getModuleId();
        if (!isNull(moduleId)) {
            module = (Module) select()
                    .where(MODULE.MODULE_ID.eq(ULong.valueOf(moduleId)))
                    .fetchOne(mapper());
        }

        return new GetModuleResponse(module);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetModuleListResponse getModuleList(GetModuleListRequest request) throws ScoreDataAccessException {
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

        return new GetModuleListResponse(
                finalStep.fetch(mapper()),
                request.getPageIndex(),
                request.getPageSize(),
                length
        );
    }

    private Collection<Condition> getConditions(GetModuleListRequest request) {
        List<Condition> conditions = new ArrayList();

        if (request.getModuleSetId() != null) {
            conditions.add(MODULE_SET_ASSIGNMENT.MODULE_SET_ID.eq(
                    ULong.valueOf(request.getModuleSetId())
            ));
        }

        if (StringUtils.hasLength(request.getName())) {
            conditions.addAll(contains(request.getName(), BIZ_CTX.NAME));
        }

        return conditions;
    }

    private SortField getSortField(GetModuleListRequest request) {
        if (!StringUtils.hasLength(request.getSortActive())) {
            return null;
        }

        Field field;
        switch (trim(request.getSortActive()).toLowerCase()) {
            case "name":
                field = MODULE.NAME;
                break;

            case "lastupdatetimestamp":
                field = MODULE.LAST_UPDATE_TIMESTAMP;
                break;

            default:
                return null;
        }

        return (request.getSortDirection() == ASC) ? field.asc() : field.desc();
    }
}
