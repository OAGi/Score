package org.oagi.score.repo.api.impl.jooq.businesscontext;

import org.jooq.Record;
import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.businesscontext.ContextCategoryReadRepository;
import org.oagi.score.repo.api.businesscontext.model.*;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreRole;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.base.SortDirection.ASC;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.impl.jooq.utils.DSLUtils.contains;
import static org.oagi.score.repo.api.impl.jooq.utils.DSLUtils.isNull;
import static org.oagi.score.repo.api.impl.utils.StringUtils.trim;
import static org.oagi.score.repo.api.user.model.ScoreRole.*;

public class JooqContextCategoryReadRepository
        extends JooqScoreRepository
        implements ContextCategoryReadRepository {

    public JooqContextCategoryReadRepository(DSLContext dslContext) {
        super(dslContext);
    }

    private SelectOnConditionStep select() {
        return dslContext().select(
                CTX_CATEGORY.CTX_CATEGORY_ID,
                CTX_CATEGORY.GUID,
                CTX_CATEGORY.NAME,
                CTX_CATEGORY.DESCRIPTION,
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
                CTX_CATEGORY.CREATION_TIMESTAMP,
                CTX_CATEGORY.LAST_UPDATE_TIMESTAMP)
                .from(CTX_CATEGORY)
                .join(APP_USER.as("creator")).on(CTX_CATEGORY.CREATED_BY.eq(APP_USER.as("creator").APP_USER_ID))
                .join(APP_USER.as("updater")).on(CTX_CATEGORY.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID));
    }

    private RecordMapper<Record, ContextCategory> mapper() {
        return record -> {
            ContextCategory contextCategory = new ContextCategory();
            contextCategory.setContextCategoryId(record.get(CTX_CATEGORY.CTX_CATEGORY_ID).toBigInteger());
            contextCategory.setGuid(record.get(CTX_CATEGORY.GUID));
            contextCategory.setName(record.get(CTX_CATEGORY.NAME));
            contextCategory.setDescription(record.get(CTX_CATEGORY.DESCRIPTION));
            contextCategory.setUsed(dslContext().selectCount().from(CTX_SCHEME)
                    .where(CTX_SCHEME.CTX_CATEGORY_ID.eq(record.get(CTX_CATEGORY.CTX_CATEGORY_ID)))
                    .fetchOneInto(Integer.class) > 0);

            ScoreRole creatorRole = (byte) 1 == record.get(APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer")) ? DEVELOPER : END_USER;
            boolean isCreatorAdmin = (byte) 1 == record.get(APP_USER.as("creator").IS_ADMIN.as("creator_is_admin"));
            contextCategory.setCreatedBy(
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
            contextCategory.setLastUpdatedBy(
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

            contextCategory.setCreationTimestamp(
                    Date.from(record.get(CTX_CATEGORY.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            contextCategory.setLastUpdateTimestamp(
                    Date.from(record.get(CTX_CATEGORY.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            return contextCategory;
        };
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetContextCategoryResponse getContextCategory(
            GetContextCategoryRequest request) throws ScoreDataAccessException {
        ContextCategory contextCategory = null;

        BigInteger contextCategoryId = request.getContextCategoryId();
        if (!isNull(contextCategoryId)) {
            contextCategory = (ContextCategory) select()
                    .where(CTX_CATEGORY.CTX_CATEGORY_ID.eq(ULong.valueOf(contextCategoryId)))
                    .fetchOne(mapper());
        }

        return new GetContextCategoryResponse(contextCategory);
    }

    private Collection<Condition> getConditions(GetContextCategoryListRequest request) {
        List<Condition> conditions = new ArrayList();

        if (!request.getContextCategoryIds().isEmpty()) {
            if (request.getContextCategoryIds().size() == 1) {
                conditions.add(CTX_CATEGORY.CTX_CATEGORY_ID.eq(
                        ULong.valueOf(request.getContextCategoryIds().iterator().next())
                ));
            } else {
                conditions.add(CTX_CATEGORY.CTX_CATEGORY_ID.in(
                        request.getContextCategoryIds().stream()
                                .map(e -> ULong.valueOf(e)).collect(Collectors.toList())
                ));
            }
        }
        if (StringUtils.hasLength(request.getName())) {
            conditions.addAll(contains(request.getName(), CTX_CATEGORY.NAME));
        }
        if (StringUtils.hasLength(request.getDescription())) {
            conditions.addAll(contains(request.getDescription(), CTX_CATEGORY.DESCRIPTION));
        }
        if (!request.getUpdaterUsernameList().isEmpty()) {
            conditions.add(APP_USER.as("updater").LOGIN_ID.in(
                    new HashSet<>(request.getUpdaterUsernameList()).stream()
                            .filter(e -> StringUtils.hasLength(e)).map(e -> trim(e)).collect(Collectors.toList())
            ));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(CTX_CATEGORY.LAST_UPDATE_TIMESTAMP.greaterOrEqual(request.getUpdateStartDate()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(CTX_CATEGORY.LAST_UPDATE_TIMESTAMP.lessThan(request.getUpdateEndDate()));
        }

        return conditions;
    }

    private SortField getSortField(GetContextCategoryListRequest request) {
        if (!StringUtils.hasLength(request.getSortActive())) {
            return null;
        }

        Field field;
        switch (trim(request.getSortActive()).toLowerCase()) {
            case "name":
                field = CTX_CATEGORY.NAME;
                break;

            case "description":
                field = CTX_CATEGORY.DESCRIPTION;
                break;

            case "lastupdatetimestamp":
                field = CTX_CATEGORY.LAST_UPDATE_TIMESTAMP;
                break;

            default:
                return null;
        }

        return (request.getSortDirection() == ASC) ? field.asc() : field.desc();
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetContextCategoryListResponse getContextCategoryList(
            GetContextCategoryListRequest request) throws ScoreDataAccessException {

        Collection<Condition> conditions = getConditions(request);
        SelectConditionStep conditionStep = select().where(conditions);
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

        return new GetContextCategoryListResponse(
                finalStep.fetch(mapper()),
                request.getPageIndex(),
                request.getPageSize(),
                length
        );
    }
}
