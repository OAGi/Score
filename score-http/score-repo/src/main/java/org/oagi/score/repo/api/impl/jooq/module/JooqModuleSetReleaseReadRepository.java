package org.oagi.score.repo.api.impl.jooq.module;

import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.module.ModuleSetReadRepository;
import org.oagi.score.repo.api.module.ModuleSetReleaseReadRepository;
import org.oagi.score.repo.api.module.model.*;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreUser;

import javax.print.DocFlavor;
import java.math.BigInteger;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.base.SortDirection.ASC;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.impl.jooq.utils.DSLUtils.contains;
import static org.oagi.score.repo.api.impl.jooq.utils.DSLUtils.isNull;
import static org.oagi.score.repo.api.impl.utils.StringUtils.trim;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

public class JooqModuleSetReleaseReadRepository
        extends JooqScoreRepository
        implements ModuleSetReleaseReadRepository {

    public JooqModuleSetReleaseReadRepository(DSLContext dslContext) {
        super(dslContext);
    }

    private SelectOnConditionStep select() {
        return dslContext().select(
                MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID,
                MODULE_SET_RELEASE.MODULE_SET_ID,
                MODULE_SET.NAME,
                MODULE_SET_RELEASE.RELEASE_ID,
                RELEASE.RELEASE_NUM,
                MODULE_SET_RELEASE.IS_DEFAULT,
                APP_USER.as("creator").APP_USER_ID.as("creator_user_id"),
                APP_USER.as("creator").LOGIN_ID.as("creator_login_id"),
                APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer"),
                APP_USER.as("updater").APP_USER_ID.as("updater_user_id"),
                APP_USER.as("updater").LOGIN_ID.as("updater_login_id"),
                APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer"),
                MODULE_SET_RELEASE.CREATION_TIMESTAMP,
                MODULE_SET_RELEASE.LAST_UPDATE_TIMESTAMP)
                .from(MODULE_SET_RELEASE)
                .join(APP_USER.as("creator")).on(MODULE_SET_RELEASE.CREATED_BY.eq(APP_USER.as("creator").APP_USER_ID))
                .join(APP_USER.as("updater")).on(MODULE_SET_RELEASE.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID))
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(MODULE_SET_RELEASE.RELEASE_ID))
                .join(MODULE_SET).on(MODULE_SET.MODULE_SET_ID.eq(MODULE_SET_RELEASE.MODULE_SET_ID));
    }

    private RecordMapper<Record, ModuleSetRelease> mapper() {
        return record -> {
            ModuleSetRelease moduleSetRelease = new ModuleSetRelease();
            moduleSetRelease.setModuleSetReleaseId(record.get(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID).toBigInteger());
            moduleSetRelease.setModuleSetId(record.get(MODULE_SET_RELEASE.MODULE_SET_ID).toBigInteger());
            moduleSetRelease.setModuleSetName(record.get(MODULE_SET.NAME));
            moduleSetRelease.setReleaseId(record.get(MODULE_SET_RELEASE.RELEASE_ID).toBigInteger());
            moduleSetRelease.setReleaseNum(record.get(RELEASE.RELEASE_NUM));
            moduleSetRelease.setDefault(record.get(MODULE_SET_RELEASE.IS_DEFAULT) == 1);

            moduleSetRelease.setCreatedBy(new ScoreUser(
                    record.get(APP_USER.as("creator").APP_USER_ID.as("creator_user_id")).toBigInteger(),
                    record.get(APP_USER.as("creator").LOGIN_ID.as("creator_login_id")),
                    (byte) 1 == record.get(APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer")) ? DEVELOPER : END_USER
            ));
            moduleSetRelease.setLastUpdatedBy(new ScoreUser(
                    record.get(APP_USER.as("updater").APP_USER_ID.as("updater_user_id")).toBigInteger(),
                    record.get(APP_USER.as("updater").LOGIN_ID.as("updater_login_id")),
                    (byte) 1 == record.get(APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer")) ? DEVELOPER : END_USER
            ));
            moduleSetRelease.setCreationTimestamp(
                    Date.from(record.get(MODULE.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            moduleSetRelease.setLastUpdateTimestamp(
                    Date.from(record.get(MODULE.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            return moduleSetRelease;
        };
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetModuleSetReleaseResponse getModuleSetRelease(GetModuleSetReleaseRequest request) throws ScoreDataAccessException {
        ModuleSetRelease moduleSetRelease = null;

        BigInteger moduleSetReleaseId = request.getModuleSetReleaseId();
        if (!isNull(moduleSetReleaseId)) {
            moduleSetRelease = (ModuleSetRelease) select()
                    .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(moduleSetReleaseId)))
                    .fetchOne(mapper());
        }

        return new GetModuleSetReleaseResponse(moduleSetRelease);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetModuleSetReleaseListResponse getModuleSetReleaseList(GetModuleSetReleaseListRequest request) throws ScoreDataAccessException {
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

        return new GetModuleSetReleaseListResponse(
                finalStep.fetch(mapper()),
                request.getPageIndex(),
                request.getPageSize(),
                length
        );
    }


    private Collection<Condition> getConditions(GetModuleSetReleaseListRequest request) {
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
            conditions.add(MODULE_SET_RELEASE.LAST_UPDATE_TIMESTAMP.greaterOrEqual(request.getUpdateStartDate()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(MODULE_SET_RELEASE.LAST_UPDATE_TIMESTAMP.lessThan(request.getUpdateEndDate()));
        }

        return conditions;
    }

    private SortField getSortField(GetModuleSetReleaseListRequest request) {
        if (!StringUtils.hasLength(request.getSortActive())) {
            return null;
        }

        Field field;
        switch (trim(request.getSortActive()).toLowerCase()) {
            case "name":
                field = MODULE_SET.NAME;
                break;

            case "releaseNum":
                field = RELEASE.RELEASE_NUM;
                break;

            case "lastupdatetimestamp":
                field = MODULE_SET_RELEASE.LAST_UPDATE_TIMESTAMP;
                break;

            default:
                return null;
        }

        return (request.getSortDirection() == ASC) ? field.asc() : field.desc();
    }
}
