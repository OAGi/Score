package org.oagi.score.repo;

import org.jooq.Record;
import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.bie.model.BieState;
import org.oagi.score.repo.api.openapidoc.model.OasDoc;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.service.common.data.AccessPrivilege;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;
import static org.oagi.score.gateway.http.helper.filter.ContainsFilterBuilder.contains;
import static org.oagi.score.repo.api.bie.model.BieState.*;
import static org.oagi.score.repo.api.impl.jooq.entity.Routines.levenshtein;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

@Repository
public class OasDocRepository {
    @Autowired
    private DSLContext dslContext;

    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public boolean checkOasDocUniqueness(OasDoc oasDoc)
            throws ScoreDataAccessException {

        if (oasDoc.getTitle() != null && oasDoc.getOpenAPIVersion() != null) {
            List<Condition> conditions = new ArrayList<>();

            conditions.add(and(OAS_DOC.TITLE.eq(oasDoc.getTitle()),
                    OAS_DOC.OPEN_API_VERSION.eq(oasDoc.getOpenAPIVersion()),
                    OAS_DOC.VERSION.eq(oasDoc.getVersion()),
                    OAS_DOC.LICENSE_NAME.eq(oasDoc.getLicenseName())));

            if (oasDoc.getOasDocId() != null) {
                conditions.add(OAS_DOC.OAS_DOC_ID.ne(ULong.valueOf(oasDoc.getOasDocId())));
            }
            return dslContext.selectCount()
                    .from(OAS_DOC)
                    .where(conditions)
                    .fetchOneInto(Integer.class) == 0;
        } else
            throw new ScoreDataAccessException("Wrong input data");
    }

    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public boolean checkOasDocTitleUniqueness(OasDoc oasDoc)
            throws ScoreDataAccessException {

        List<Condition> conditions = new ArrayList<>();
        conditions.add(OAS_DOC.TITLE.eq(oasDoc.getTitle()));
        if (oasDoc.getOasDocId() != null) {
            conditions.add(OAS_DOC.OAS_DOC_ID.ne(ULong.valueOf(oasDoc.getOasDocId())));
        }

        if (oasDoc != null) {
            return dslContext.selectCount()
                    .from(OAS_DOC)
                    .where(conditions)
                    .fetchOneInto(Integer.class) == 0;
        } else
            throw new ScoreDataAccessException("Wrong input data");
    }
    public class SelectBieForOasDocListArguments {

        private final List<Field> selectFields = new ArrayList<>();
        private final List<Condition> conditions = new ArrayList<>();
        private List<SortField<?>> sortFields = new ArrayList<>();
        private int offset = -1;
        private int numberOfRows = -1;

        private String den;
        private String type;

        SelectBieForOasDocListArguments() {
            selectFields.addAll(Arrays.asList(
                    TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                    TOP_LEVEL_ASBIEP.VERSION,
                    TOP_LEVEL_ASBIEP.STATUS,
                    ABIE.GUID,
                    ASCCP_MANIFEST.DEN,
                    ASCCP.PROPERTY_TERM,
                    ASBIEP.REMARK,
                    RELEASE.RELEASE_NUM,
                    TOP_LEVEL_ASBIEP.OWNER_USER_ID,
                    APP_USER.LOGIN_ID.as("owner"),
                    TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP,
                    APP_USER.as("updater").LOGIN_ID.as("last_update_user"),
                    TOP_LEVEL_ASBIEP.STATE));
        }

        public List<Field> selectFields() {
            return this.selectFields;
        }

        public SelectBieForOasDocListArguments setOasDocId(BigInteger oasDocId) {
            if (oasDocId != null && oasDocId.longValue() > 0L) {
                conditions.add(OAS_DOC.OAS_DOC_ID.eq(ULong.valueOf(oasDocId)));
            }
            return this;
        }

        public SelectBieForOasDocListArguments setDen(String den) {
            if (StringUtils.hasLength(den)) {
                conditions.addAll(contains(den, ASCCP_MANIFEST.DEN));
                selectFields.add(
                        val(1).minus(levenshtein(lower(ASCCP.PROPERTY_TERM), val(den.toLowerCase()))
                                        .div(greatest(length(ASCCP.PROPERTY_TERM), length(den))))
                                .as("score")
                );
                sortFields.add(field("score").desc());
            }
            return this;
        }

        public SelectBieForOasDocListArguments setPropertyTerm(String propertyTerm) {
            if (StringUtils.hasLength(propertyTerm)) {
                conditions.addAll(contains(propertyTerm, ASCCP.PROPERTY_TERM));
            }
            return this;
        }

        public SelectBieForOasDocListArguments setBusinessContext(String businessContext) {
            if (StringUtils.hasLength(businessContext)) {
                conditions.addAll(contains(businessContext, BIZ_CTX.NAME));
            }
            return this;
        }

        public SelectBieForOasDocListArguments setAsccpManifestId(BigInteger asccpManifestId) {
            if (asccpManifestId != null && asccpManifestId.longValue() > 0L) {
                conditions.add(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ULong.valueOf(asccpManifestId)));
            }
            return this;
        }

        public SelectBieForOasDocListArguments setExcludePropertyTerms(List<String> excludePropertyTerms) {
            if (!excludePropertyTerms.isEmpty()) {
                conditions.add(ASCCP.PROPERTY_TERM.notIn(excludePropertyTerms));
            }
            return this;
        }

        public SelectBieForOasDocListArguments setExcludeTopLevelAsbiepIds(List<BigInteger> excludeTopLevelAsbiepIds) {
            if (!excludeTopLevelAsbiepIds.isEmpty()) {
                conditions.add(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.notIn(
                        excludeTopLevelAsbiepIds.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList())
                ));
            }
            return this;
        }

        public SelectBieForOasDocListArguments setIncludeTopLevelAsbiepIds(List<BigInteger> includeTopLevelAsbiepIds) {
            if (!includeTopLevelAsbiepIds.isEmpty()) {
                conditions.add(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(
                        includeTopLevelAsbiepIds.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList())
                ));
            }
            return this;
        }

        public SelectBieForOasDocListArguments setStates(List<BieState> states) {
            if (!states.isEmpty()) {
                conditions.add(TOP_LEVEL_ASBIEP.STATE.in(states.stream().map(e -> e.name()).collect(Collectors.toList())));
            }
            return this;
        }
        public SelectBieForOasDocListArguments setType(String type) {
            this.type = type;
            return this;
        }

        public SelectBieForOasDocListArguments setOwnerLoginIds(List<String> ownerLoginIds) {
            if (!ownerLoginIds.isEmpty()) {
                conditions.add(APP_USER.LOGIN_ID.in(ownerLoginIds));
            }
            return this;
        }

        public SelectBieForOasDocListArguments setUpdaterLoginIds(List<String> updaterLoginIds) {
            if (!updaterLoginIds.isEmpty()) {
                conditions.add(APP_USER.as("updater").LOGIN_ID.in(updaterLoginIds));
            }
            return this;
        }

        public SelectBieForOasDocListArguments setUpdateDate(Date from, Date to) {
            return setUpdateDate(
                    (from != null) ? new Timestamp(from.getTime()).toLocalDateTime() : null,
                    (to != null) ? new Timestamp(to.getTime()).toLocalDateTime() : null
            );
        }

        public SelectBieForOasDocListArguments setUpdateDate(LocalDateTime from, LocalDateTime to) {
            if (from != null) {
                conditions.add(TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP.greaterOrEqual(from));
            }
            if (to != null) {
                conditions.add(TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP.lessThan(to));
            }
            return this;
        }

        public SelectBieForOasDocListArguments setAccess(ULong userId, AccessPrivilege access) {
            if (access != null) {
                switch (access) {
                    case CanEdit:
                        conditions.add(
                                and(
                                        TOP_LEVEL_ASBIEP.STATE.notEqual(Initiating.name()),
                                        TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(userId)
                                )
                        );
                        break;

                    case CanView:
                        conditions.add(
                                or(
                                        TOP_LEVEL_ASBIEP.STATE.in(QA.name(), Production.name()),
                                        and(
                                                TOP_LEVEL_ASBIEP.STATE.notEqual(Initiating.name()),
                                                TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(userId)
                                        )
                                )
                        );
                        break;
                }
            }
            return this;
        }

        public SelectBieForOasDocListArguments setSort(String field, String direction) {
            if (StringUtils.hasLength(field)) {
                SortField<?> sortField = null;
                switch (field) {
                    case "state":
                        if ("asc".equals(direction)) {
                            sortField = TOP_LEVEL_ASBIEP.STATE.asc();
                        } else if ("desc".equals(direction)) {
                            sortField = TOP_LEVEL_ASBIEP.STATE.desc();
                        }

                        break;

                    case "topLevelAsccpPropertyTerm":
                        if ("asc".equals(direction)) {
                            sortField = ASCCP.PROPERTY_TERM.asc();
                        } else if ("desc".equals(direction)) {
                            sortField = ASCCP.PROPERTY_TERM.desc();
                        }

                        break;

                    case "den":
                        if ("asc".equals(direction)) {
                            sortField = ASCCP_MANIFEST.DEN.asc();
                        } else if ("desc".equals(direction)) {
                            sortField = ASCCP_MANIFEST.DEN.desc();
                        }
                        break;

                    case "releaseNum":
                        if ("asc".equals(direction)) {
                            sortField = RELEASE.RELEASE_NUM.asc();
                        } else if ("desc".equals(direction)) {
                            sortField = RELEASE.RELEASE_NUM.desc();
                        }

                        break;

                    case "lastUpdateTimestamp":
                        if ("asc".equals(direction)) {
                            sortField = TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP.asc();
                        } else if ("desc".equals(direction)) {
                            sortField = TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP.desc();
                        }

                        break;
                }

                if (sortField != null) {
                    this.sortFields.add(0, sortField);
                }
            }

            return this;
        }

        public SelectBieForOasDocListArguments setOffset(int offset, int numberOfRows) {
            this.offset = offset;
            this.numberOfRows = numberOfRows;
            return this;
        }

        public SelectBieForOasDocListArguments setReleaseId(BigInteger releaseId) {
            if (releaseId != null && releaseId.longValue() > 0) {
                conditions.add(TOP_LEVEL_ASBIEP.RELEASE_ID.eq(ULong.valueOf(releaseId)));
            }
            return this;
        }

        public SelectBieForOasDocListArguments setOwnedByDeveloper(Boolean ownedByDeveloper) {
            if (ownedByDeveloper != null) {
                conditions.add(APP_USER.IS_DEVELOPER.eq(ownedByDeveloper ? (byte) 1 : 0));
            }
            return this;
        }

        public List<Condition> getConditions() {
            return conditions;
        }

        public List<SortField<?>> getSortFields() {
            return this.sortFields;
        }

        public int getOffset() {
            return offset;
        }

        public int getNumberOfRows() {
            return numberOfRows;
        }

        public String getDen() {
            return den;
        }

        public String getType() {
            return type;
        }

        public <E> PaginationResponse<E> fetchInto(Class<? extends E> type) {
            return selectBieForOasDocList(this, type);
        }
    }
    private <E> PaginationResponse<E> selectBieForOasDocList(SelectBieForOasDocListArguments arguments, Class<? extends E> type) {
        SelectOnConditionStep<Record> step = getSelectOnConditionStep(arguments);
        SelectConnectByStep<Record> conditionStep = step.where(arguments.getConditions());

        int pageCount = dslContext.fetchCount(conditionStep);

        List<SortField<?>> sortFields = arguments.getSortFields();
        SelectWithTiesAfterOffsetStep<Record> offsetStep = null;
        if (!sortFields.isEmpty()) {
            if (arguments.getOffset() >= 0 && arguments.getNumberOfRows() >= 0) {
                offsetStep = conditionStep.orderBy(sortFields)
                        .limit(arguments.getOffset(), arguments.getNumberOfRows());
            }
        } else {
            if (arguments.getOffset() >= 0 && arguments.getNumberOfRows() >= 0) {
                offsetStep = conditionStep
                        .limit(arguments.getOffset(), arguments.getNumberOfRows());
            }
        }

        return new PaginationResponse<>(pageCount,
                (offsetStep != null) ?
                        offsetStep.fetchInto(type) : conditionStep.fetchInto(type));
    }

    private SelectOnConditionStep<Record> getSelectOnConditionStep(SelectBieForOasDocListArguments arguments) {
        List<Field> selectFields = arguments.selectFields();
        return dslContext.selectDistinct(selectFields)
                .from(TOP_LEVEL_ASBIEP)
                .join(ASBIEP).on(and(
                        ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID),
                        ASBIEP.ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.ASBIEP_ID))
                )
                .join(ABIE).on(ASBIEP.ROLE_OF_ABIE_ID.eq(ABIE.ABIE_ID))
                .join(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(APP_USER).on(APP_USER.APP_USER_ID.eq(TOP_LEVEL_ASBIEP.OWNER_USER_ID))
                .join(APP_USER.as("updater")).on(APP_USER.as("updater").APP_USER_ID.eq(TOP_LEVEL_ASBIEP.LAST_UPDATED_BY))
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(TOP_LEVEL_ASBIEP.RELEASE_ID))
                .join(BIZ_CTX_ASSIGNMENT).on(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID))
                .join(BIZ_CTX).on(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID.eq(BIZ_CTX.BIZ_CTX_ID));
    }


    public SelectBieForOasDocListArguments selectBieForOasDocLists() {
        return new SelectBieForOasDocListArguments();
    }

    public class GetBieListForOasDocArguments {

        private final List<Field> selectFields = new ArrayList<>();
        private final List<Condition> conditions = new ArrayList<>();
        private List<SortField<?>> sortFields = new ArrayList<>();
        private int offset = -1;
        private int numberOfRows = -1;

        private String den;
        private String type;

        GetBieListForOasDocArguments() {
            selectFields.addAll(Arrays.asList(
                    TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                    TOP_LEVEL_ASBIEP.STATE,
                    TOP_LEVEL_ASBIEP.VERSION,
                    TOP_LEVEL_ASBIEP.RELEASE_ID,
                    ASBIEP.ASBIEP_ID,
                    ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                    ASCCP.ASCCP_ID,
                    ASCCP.GUID,
                    ASCCP_MANIFEST.DEN,
                    APP_USER.as("owner").LOGIN_ID.as("owner"),
                    APP_USER.as("owner").APP_USER_ID.as("owner_user_id"),
                    APP_USER.as("owner").LOGIN_ID.as("owner_login_id"),
                    APP_USER.as("owner").IS_DEVELOPER.as("owner_is_developer"),
                    APP_USER.as("owner").IS_ADMIN.as("owner_is_admin"),
                    OAS_DOC.as("req_oas_doc").OAS_DOC_ID.as("req_oas_doc_id"),
                    OAS_DOC.as("res_oas_doc").OAS_DOC_ID.as("res_oas_doc_id"),
                    OAS_OPERATION.as("req_oas_operation").VERB.as("req_verb"),
                    OAS_OPERATION.as("res_oas_operation").VERB.as("res_verb"),
                    OAS_TAG.as("req_oas_tag").NAME.as("req_tag_name"),
                    OAS_TAG.as("res_oas_tag").NAME.as("res_tag_name"),
                    OAS_REQUEST.MAKE_ARRAY_INDICATOR.as("req_array_indicator"),
                    OAS_RESPONSE.MAKE_ARRAY_INDICATOR.as("res_array_indicator"),
                    OAS_REQUEST.SUPPRESS_ROOT_INDICATOR.as("req_suppress_root_indicator"),
                    OAS_RESPONSE.SUPPRESS_ROOT_INDICATOR.as("res_suppress_root_indicator"),
                    OAS_RESOURCE.as("req_oas_resource").PATH.as("req_resource_name"),
                    OAS_RESOURCE.as("res_oas_resource").PATH.as("res_resource_name"),
                    OAS_OPERATION.as("req_oas_operation").OPERATION_ID.as("req_operation_id"),
                    OAS_OPERATION.as("res_oas_operation").OPERATION_ID.as("res_operation_id"),
                    APP_USER.as("creator").APP_USER_ID.as("creator_user_id"),
                    APP_USER.as("creator").LOGIN_ID.as("creator_login_id"),
                    APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer"),
                    APP_USER.as("updater").APP_USER_ID.as("updater_user_id"),
                    APP_USER.as("updater").LOGIN_ID.as("updater_login_id"),
                    APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer"),
                    OAS_MESSAGE_BODY.CREATION_TIMESTAMP,
                    OAS_MESSAGE_BODY.LAST_UPDATE_TIMESTAMP));
        }

        public List<Field> selectFields() {
            return this.selectFields;
        }

        public GetBieListForOasDocArguments setOasDocId(BigInteger oasDocId) {
            if (oasDocId != null && oasDocId.longValue() > 0L) {
                conditions.add(OAS_DOC.as("res_oas_doc").OAS_DOC_ID.eq(ULong.valueOf(oasDocId)));
            }
            return this;
        }

        public GetBieListForOasDocArguments setDen(String den) {
            if (StringUtils.hasLength(den)) {
                conditions.addAll(contains(den, ASCCP_MANIFEST.DEN));
                selectFields.add(
                        val(1).minus(levenshtein(lower(ASCCP.PROPERTY_TERM), val(den.toLowerCase()))
                                        .div(greatest(length(ASCCP.PROPERTY_TERM), length(den))))
                                .as("score")
                );
                sortFields.add(field("score").desc());
            }
            return this;
        }

        public GetBieListForOasDocArguments setPropertyTerm(String propertyTerm) {
            if (StringUtils.hasLength(propertyTerm)) {
                conditions.addAll(contains(propertyTerm, ASCCP.PROPERTY_TERM));
            }
            return this;
        }

        public GetBieListForOasDocArguments setBusinessContext(String businessContext) {
            if (StringUtils.hasLength(businessContext)) {
                conditions.addAll(contains(businessContext, BIZ_CTX.NAME));
            }
            return this;
        }

        public GetBieListForOasDocArguments setAsccpManifestId(BigInteger asccpManifestId) {
            if (asccpManifestId != null && asccpManifestId.longValue() > 0L) {
                conditions.add(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ULong.valueOf(asccpManifestId)));
            }
            return this;
        }

        public GetBieListForOasDocArguments setExcludePropertyTerms(List<String> excludePropertyTerms) {
            if (!excludePropertyTerms.isEmpty()) {
                conditions.add(ASCCP.PROPERTY_TERM.notIn(excludePropertyTerms));
            }
            return this;
        }

        public GetBieListForOasDocArguments setExcludeTopLevelAsbiepIds(List<BigInteger> excludeTopLevelAsbiepIds) {
            if (!excludeTopLevelAsbiepIds.isEmpty()) {
                conditions.add(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.notIn(
                        excludeTopLevelAsbiepIds.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList())
                ));
            }
            return this;
        }

        public GetBieListForOasDocArguments setIncludeTopLevelAsbiepIds(List<BigInteger> includeTopLevelAsbiepIds) {
            if (!includeTopLevelAsbiepIds.isEmpty()) {
                conditions.add(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(
                        includeTopLevelAsbiepIds.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList())
                ));
            }
            return this;
        }

        public GetBieListForOasDocArguments setStates(List<BieState> states) {
            if (!states.isEmpty()) {
                conditions.add(TOP_LEVEL_ASBIEP.STATE.in(states.stream().map(e -> e.name()).collect(Collectors.toList())));
            }
            return this;
        }
        public GetBieListForOasDocArguments setType(String type) {
            this.type = type;
            return this;
        }

        public GetBieListForOasDocArguments setOwnerLoginIds(List<String> ownerLoginIds) {
            if (!ownerLoginIds.isEmpty()) {
                conditions.add(APP_USER.LOGIN_ID.in(ownerLoginIds));
            }
            return this;
        }

        public GetBieListForOasDocArguments setUpdaterLoginIds(List<String> updaterLoginIds) {
            if (!updaterLoginIds.isEmpty()) {
                conditions.add(APP_USER.as("updater").LOGIN_ID.in(updaterLoginIds));
            }
            return this;
        }

        public GetBieListForOasDocArguments setUpdateDate(Date from, Date to) {
            return setUpdateDate(
                    (from != null) ? new Timestamp(from.getTime()).toLocalDateTime() : null,
                    (to != null) ? new Timestamp(to.getTime()).toLocalDateTime() : null
            );
        }

        public GetBieListForOasDocArguments setUpdateDate(LocalDateTime from, LocalDateTime to) {
            if (from != null) {
                conditions.add(OAS_DOC.LAST_UPDATE_TIMESTAMP.greaterOrEqual(from));
            }
            if (to != null) {
                conditions.add(OAS_DOC.LAST_UPDATE_TIMESTAMP.lessThan(to));
            }
            return this;
        }

        public GetBieListForOasDocArguments setAccess(ULong userId, AccessPrivilege access) {
            if (access != null) {
                switch (access) {
                    case CanEdit:
                        conditions.add(
                                and(
                                        TOP_LEVEL_ASBIEP.STATE.notEqual(Initiating.name()),
                                        TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(userId)
                                )
                        );
                        break;

                    case CanView:
                        conditions.add(
                                or(
                                        TOP_LEVEL_ASBIEP.STATE.in(QA.name(), Production.name()),
                                        and(
                                                TOP_LEVEL_ASBIEP.STATE.notEqual(Initiating.name()),
                                                TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(userId)
                                        )
                                )
                        );
                        break;
                }
            }
            return this;
        }

        public GetBieListForOasDocArguments setSort(String field, String direction) {
            if (StringUtils.hasLength(field)) {
                SortField<?> sortField = null;
                switch (field) {
                    case "state":
                        if ("asc".equals(direction)) {
                            sortField = TOP_LEVEL_ASBIEP.STATE.asc();
                        } else if ("desc".equals(direction)) {
                            sortField = TOP_LEVEL_ASBIEP.STATE.desc();
                        }

                        break;

                    case "topLevelAsccpPropertyTerm":
                        if ("asc".equals(direction)) {
                            sortField = ASCCP.PROPERTY_TERM.asc();
                        } else if ("desc".equals(direction)) {
                            sortField = ASCCP.PROPERTY_TERM.desc();
                        }

                        break;

                    case "den":
                        if ("asc".equals(direction)) {
                            sortField = ASCCP_MANIFEST.DEN.asc();
                        } else if ("desc".equals(direction)) {
                            sortField = ASCCP_MANIFEST.DEN.desc();
                        }
                        break;

                    case "releaseNum":
                        if ("asc".equals(direction)) {
                            sortField = RELEASE.RELEASE_NUM.asc();
                        } else if ("desc".equals(direction)) {
                            sortField = RELEASE.RELEASE_NUM.desc();
                        }

                        break;

                    case "lastUpdateTimestamp":
                        if ("asc".equals(direction)) {
                            sortField = TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP.asc();
                        } else if ("desc".equals(direction)) {
                            sortField = TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP.desc();
                        }

                        break;
                }

                if (sortField != null) {
                    this.sortFields.add(0, sortField);
                }
            }

            return this;
        }

        public GetBieListForOasDocArguments setOffset(int offset, int numberOfRows) {
            this.offset = offset;
            this.numberOfRows = numberOfRows;
            return this;
        }

        public GetBieListForOasDocArguments setReleaseId(BigInteger releaseId) {
            if (releaseId != null && releaseId.longValue() > 0) {
                conditions.add(TOP_LEVEL_ASBIEP.RELEASE_ID.eq(ULong.valueOf(releaseId)));
            }
            return this;
        }

        public GetBieListForOasDocArguments setOwnedByDeveloper(Boolean ownedByDeveloper) {
            if (ownedByDeveloper != null) {
                conditions.add(APP_USER.IS_DEVELOPER.eq(ownedByDeveloper ? (byte) 1 : 0));
            }
            return this;
        }

        public List<Condition> getConditions() {
            return conditions;
        }

        public List<SortField<?>> getSortFields() {
            return this.sortFields;
        }

        public int getOffset() {
            return offset;
        }

        public int getNumberOfRows() {
            return numberOfRows;
        }

        public String getDen() {
            return den;
        }

        public String getType() {
            return type;
        }

        public <E> PaginationResponse<E> fetchInto(Class<? extends E> type) {
            return getBieListForOasDoc(this, type);
        }
    }
    private <E> PaginationResponse<E> getBieListForOasDoc(GetBieListForOasDocArguments arguments, Class<? extends E> type) {
        SelectOnConditionStep<Record> step = getSelectOnConditionStep(arguments);
        SelectConnectByStep<Record> conditionStep = step.where(arguments.getConditions());

        int pageCount = dslContext.fetchCount(conditionStep);

        List<SortField<?>> sortFields = arguments.getSortFields();
        SelectWithTiesAfterOffsetStep<Record> offsetStep = null;
        if (!sortFields.isEmpty()) {
            if (arguments.getOffset() >= 0 && arguments.getNumberOfRows() >= 0) {
                offsetStep = conditionStep.orderBy(sortFields)
                        .limit(arguments.getOffset(), arguments.getNumberOfRows());
            }
        } else {
            if (arguments.getOffset() >= 0 && arguments.getNumberOfRows() >= 0) {
                offsetStep = conditionStep
                        .limit(arguments.getOffset(), arguments.getNumberOfRows());
            }
        }

        return new PaginationResponse<>(pageCount,
                (offsetStep != null) ?
                        offsetStep.fetchInto(type) : conditionStep.fetchInto(type));
    }

    private SelectOnConditionStep<Record> getSelectOnConditionStep(GetBieListForOasDocArguments arguments) {
        List<Field> selectFields = arguments.selectFields();
        return dslContext.selectDistinct(selectFields)
                .from(OAS_MESSAGE_BODY)
                .leftJoin(OAS_REQUEST).on(OAS_REQUEST.OAS_MESSAGE_BODY_ID.eq(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID))
                .leftJoin(OAS_OPERATION.as("req_oas_operation")).on(OAS_REQUEST.OAS_OPERATION_ID.eq(OAS_OPERATION.as("req_oas_operation").OAS_OPERATION_ID))
                .leftJoin(OAS_RESOURCE.as("req_oas_resource")).on(OAS_OPERATION.as("req_oas_operation").OAS_OPERATION_ID.eq(OAS_RESOURCE.as("req_oas_resource").OAS_RESOURCE_ID))
                .leftJoin(OAS_DOC.as("req_oas_doc")).on(OAS_RESOURCE.as("req_oas_resource").OAS_DOC_ID.eq(OAS_DOC.as("req_oas_doc").OAS_DOC_ID))
                .leftJoin(OAS_DOC_TAG.as("req_oas_doc_tag")).on(OAS_DOC.as("req_oas_doc").OAS_DOC_ID.eq(OAS_DOC_TAG.as("req_oas_doc_tag").OAS_DOC_ID))
                .leftJoin(OAS_TAG.as("req_oas_tag")).on(OAS_DOC_TAG.as("req_oas_doc_tag").OAS_TAG_ID.eq(OAS_TAG.as("req_oas_tag").OAS_TAG_ID))
                .leftJoin(OAS_RESPONSE).on(OAS_RESPONSE.OAS_MESSAGE_BODY_ID.eq(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID))
                .leftJoin(OAS_OPERATION.as("res_oas_operation")).on(OAS_RESPONSE.OAS_OPERATION_ID.eq(OAS_OPERATION.as("res_oas_operation").OAS_OPERATION_ID))
                .leftJoin(OAS_RESOURCE.as("res_oas_resource")).on(OAS_OPERATION.as("res_oas_operation").OAS_RESOURCE_ID.eq(OAS_RESOURCE.as("res_oas_resource").OAS_RESOURCE_ID))
                .leftJoin(OAS_DOC.as("res_oas_doc")).on(OAS_RESOURCE.as("res_oas_resource").OAS_DOC_ID.eq(OAS_DOC.as("res_oas_doc").OAS_DOC_ID))
                .leftJoin(OAS_DOC_TAG.as("res_oas_doc_tag")).on(OAS_DOC.as("res_oas_doc").OAS_DOC_ID.eq(OAS_DOC_TAG.as("res_oas_doc_tag").OAS_DOC_ID))
                .leftJoin(OAS_TAG.as("res_oas_tag")).on(OAS_DOC_TAG.as("res_oas_doc_tag").OAS_TAG_ID.eq(OAS_TAG.as("res_oas_tag").OAS_TAG_ID))
                .rightJoin(TOP_LEVEL_ASBIEP).on(OAS_MESSAGE_BODY.TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                .leftJoin(ASBIEP).on(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                .leftJoin(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .leftJoin(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(APP_USER.as("owner")).on(TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(APP_USER.as("owner").APP_USER_ID))
                .join(APP_USER.as("creator")).on(OAS_MESSAGE_BODY.CREATED_BY.eq(APP_USER.as("creator").APP_USER_ID))
                .join(APP_USER.as("updater")).on(OAS_MESSAGE_BODY.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID));
    }

    public class InsertOasMessageBodyArguments {
        private ULong userId;
        private ULong topLevelAsbiepId;
        private LocalDateTime timestamp = new Timestamp(System.currentTimeMillis()).toLocalDateTime();

        public InsertOasMessageBodyArguments setUserId(BigInteger userId) {
            return setUserId(ULong.valueOf(userId));
        }
        public InsertOasMessageBodyArguments setUserId(ULong userId) {
            this.userId = userId;
            return this;
        }
        public InsertOasMessageBodyArguments setTopLevelAsbiepId(BigInteger topLevelAsbiepId) {
            return setTopLevelAsbiepId(ULong.valueOf(topLevelAsbiepId));
        }
        public InsertOasMessageBodyArguments setTopLevelAsbiepId(ULong topLevelAsbiepId) {
            this.topLevelAsbiepId = topLevelAsbiepId;
            return this;
        }
        public InsertOasMessageBodyArguments setTimestamp(long millis) {
            return setTimestamp(new Timestamp(millis).toLocalDateTime());
        }
        public InsertOasMessageBodyArguments setTimestamp(Date date) {
            return setTimestamp(new Timestamp(date.getTime()).toLocalDateTime());
        }
        public InsertOasMessageBodyArguments setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ULong getUserId() {
            return userId;
        }
        public ULong getTopLevelAsbiepId() {
            return topLevelAsbiepId;
        }
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        public ULong execute() {
            return insertOasMessageBody(this);
        }
    }
    public InsertOasMessageBodyArguments insertOasMessageBody() {
        return new InsertOasMessageBodyArguments();
    }
    private ULong insertOasMessageBody(InsertOasMessageBodyArguments arguments) {
        return dslContext.insertInto(OAS_MESSAGE_BODY)
                .set(OAS_MESSAGE_BODY.CREATED_BY, arguments.getUserId())
                .set(OAS_MESSAGE_BODY.LAST_UPDATED_BY, arguments.getUserId())
                .set(OAS_MESSAGE_BODY.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_MESSAGE_BODY.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_MESSAGE_BODY.TOP_LEVEL_ASBIEP_ID, arguments.getTopLevelAsbiepId())
                .returningResult(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID)
                .fetchOne().value1();
    }


    public class InsertOasResourceArguments {
        private ULong userId;
        private ULong oasDocId;
        private String path;
        private String ref;
        private LocalDateTime timestamp = new Timestamp(System.currentTimeMillis()).toLocalDateTime();

        public InsertOasResourceArguments setUserId(BigInteger userId) {
            return setUserId(ULong.valueOf(userId));
        }
        public InsertOasResourceArguments setUserId(ULong userId) {
            this.userId = userId;
            return this;
        }

        public ULong getOasDocId() {
            return oasDocId;
        }

        public InsertOasResourceArguments setOasDocId(ULong oasDocId) {
            this.oasDocId = oasDocId;
            return this;
        }
        public String getPath() {
            return path;
        }
        public InsertOasResourceArguments setOasDocId(BigInteger oasDocId) {
            return setOasDocId(ULong.valueOf(oasDocId));
        }

        public InsertOasResourceArguments setPath(String path) {
            this.path = path;
            return this;
        }

        public String getRef() {
            return ref;
        }

        public InsertOasResourceArguments setRef(String ref) {
            this.ref = ref;
            return this;
        }

        public InsertOasResourceArguments setTimestamp(long millis) {
            return setTimestamp(new Timestamp(millis).toLocalDateTime());
        }
        public InsertOasResourceArguments setTimestamp(Date date) {
            return setTimestamp(new Timestamp(date.getTime()).toLocalDateTime());
        }
        public InsertOasResourceArguments setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        public ULong getUserId() {
            return userId;
        }
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        public ULong execute() {
            return insertOasResource(this);
        }
    }
    public InsertOasResourceArguments insertOasResource() {
        return new InsertOasResourceArguments();
    }
    private ULong insertOasResource(InsertOasResourceArguments arguments) {
        return dslContext.insertInto(OAS_RESOURCE)
                .set(OAS_RESOURCE.CREATED_BY, arguments.getUserId())
                .set(OAS_RESOURCE.LAST_UPDATED_BY, arguments.getUserId())
                .set(OAS_RESOURCE.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_RESOURCE.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_RESOURCE.OAS_DOC_ID, arguments.getOasDocId())
                .set(OAS_RESOURCE.PATH, arguments.getPath())
                .set(OAS_RESOURCE.REF, arguments.getRef())
                .returningResult(OAS_RESOURCE.OAS_RESOURCE_ID)
                .fetchOne().value1();
    }

    public class InsertOasOperationArguments {
        private ULong userId;
        private ULong oasResourceId;
        private String verb;
        private String operationId;
        private String summary;
        private String description;
        private boolean deprecated;
        private LocalDateTime timestamp = new Timestamp(System.currentTimeMillis()).toLocalDateTime();

        public InsertOasOperationArguments setUserId(BigInteger userId) {
            return setUserId(ULong.valueOf(userId));
        }
        public InsertOasOperationArguments setUserId(ULong userId) {
            this.userId = userId;
            return this;
        }

        public ULong getOasResourceId() {
            return oasResourceId;
        }

        public InsertOasOperationArguments setOasResourceId(BigInteger oasResourceId) {
            return setOasResourceId(ULong.valueOf(oasResourceId));
        }

        public InsertOasOperationArguments setOasResourceId(ULong oasResourceId) {
            this.oasResourceId = oasResourceId;
            return this;
        }

        public String getVerb() {
            return verb;
        }

        public InsertOasOperationArguments setVerb(String verb) {
            this.verb = verb;
            return this;
        }

        public String getOperationId() {
            return operationId;
        }

        public InsertOasOperationArguments setOperationId(String operationId) {
            this.operationId = operationId;
            return this;
        }

        public String getSummary() {
            return summary;
        }

        public InsertOasOperationArguments setSummary(String summary) {
            this.summary = summary;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public InsertOasOperationArguments setDescription(String description) {
            this.description = description;
            return this;
        }
        public boolean isDeprecated() {
            return deprecated;
        }

        public InsertOasOperationArguments setDeprecated(boolean deprecated) {
            this.deprecated = deprecated;
            return this;
        }

        public InsertOasOperationArguments setTimestamp(long millis) {
            return setTimestamp(new Timestamp(millis).toLocalDateTime());
        }
        public InsertOasOperationArguments setTimestamp(Date date) {
            return setTimestamp(new Timestamp(date.getTime()).toLocalDateTime());
        }
        public InsertOasOperationArguments setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        public ULong getUserId() {
            return userId;
        }
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        public ULong execute() {
            return insertOasOperation(this);
        }
    }
    public InsertOasOperationArguments insertOasOperation() {
        return new InsertOasOperationArguments();
    }
    private ULong insertOasOperation(InsertOasOperationArguments arguments) {
        return dslContext.insertInto(OAS_OPERATION)
                .set(OAS_OPERATION.CREATED_BY, arguments.getUserId())
                .set(OAS_OPERATION.LAST_UPDATED_BY, arguments.getUserId())
                .set(OAS_OPERATION.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_OPERATION.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_OPERATION.OAS_RESOURCE_ID, arguments.getOasResourceId())
                .set(OAS_OPERATION.VERB, arguments.getVerb())
                .set(OAS_OPERATION.OPERATION_ID, arguments.getOperationId())
                .set(OAS_OPERATION.SUMMARY, arguments.getSummary())
                .set(OAS_OPERATION.DESCRIPTION, arguments.getDescription())
                .set(OAS_OPERATION.DEPRECATED, (byte) (arguments.isDeprecated() ? 1 : 0))
                .returningResult(OAS_OPERATION.OAS_OPERATION_ID)
                .fetchOne().value1();
    }

    public class InsertOasTagArguments {
        private ULong userId;
        private String guid;
        private String name;
        private String description;
        private LocalDateTime timestamp = new Timestamp(System.currentTimeMillis()).toLocalDateTime();
        public InsertOasTagArguments setUserId(BigInteger userId) {
            return setUserId(ULong.valueOf(userId));
        }
        public InsertOasTagArguments setUserId(ULong userId) {
            this.userId = userId;
            return this;
        }
        public InsertOasTagArguments setGuid(String guid) {
            return setGuid(guid);
        }
        public InsertOasTagArguments setName(String name) {
            return setName(name);
        }
        public InsertOasTagArguments setDescription(String description) {
            return setName(description);
        }
        public InsertOasTagArguments setTimestamp(long millis) {
            return setTimestamp(new Timestamp(millis).toLocalDateTime());
        }
        public InsertOasTagArguments setTimestamp(Date date) {
            return setTimestamp(new Timestamp(date.getTime()).toLocalDateTime());
        }
        public InsertOasTagArguments setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ULong getUserId() {
            return userId;
        }
        public String getGuid() {
            return guid;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        public ULong execute() {
            return insertOasTag(this);
        }
    }
    public InsertOasTagArguments insertOasTag() {
        return new InsertOasTagArguments();
    }
    private ULong insertOasTag(InsertOasTagArguments arguments) {
        return dslContext.insertInto(OAS_TAG)
                .set(OAS_TAG.CREATED_BY, arguments.getUserId())
                .set(OAS_TAG.LAST_UPDATED_BY, arguments.getUserId())
                .set(OAS_TAG.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_TAG.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_TAG.NAME, arguments.getName())
                .set(OAS_TAG.DESCRIPTION, arguments.getDescription())
                .set(OAS_TAG.GUID, arguments.getGuid())
                .returningResult(OAS_TAG.OAS_TAG_ID)
                .fetchOne().value1();
    }

    public class InsertOasResourceTagArguments {
        private ULong userId;
        private ULong oasOperationId;
        private ULong oasTagId;
        private LocalDateTime timestamp = new Timestamp(System.currentTimeMillis()).toLocalDateTime();
        public InsertOasResourceTagArguments setUserId(BigInteger userId) {
            return setUserId(ULong.valueOf(userId));
        }
        public InsertOasResourceTagArguments setUserId(ULong userId) {
            this.userId = userId;
            return this;
        }
        public InsertOasResourceTagArguments setOasOperationId(BigInteger oasOperationId) {
            return setOasOperationId(ULong.valueOf(oasOperationId));
        }
        public InsertOasResourceTagArguments setOasOperationId(ULong oasOperationId) {
            this.oasOperationId = oasOperationId;
            return this;
        }

        public InsertOasResourceTagArguments setOasTagId(BigInteger oasTagId) {
            return setOasTagId(ULong.valueOf(oasTagId));
        }
        public InsertOasResourceTagArguments setOasTagId(ULong oasTagId) {
            this.oasTagId = oasTagId;
            return this;
        }
        public InsertOasResourceTagArguments setTimestamp(long millis) {
            return setTimestamp(new Timestamp(millis).toLocalDateTime());
        }
        public InsertOasResourceTagArguments setTimestamp(Date date) {
            return setTimestamp(new Timestamp(date.getTime()).toLocalDateTime());
        }
        public InsertOasResourceTagArguments setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ULong getUserId() {
            return userId;
        }

        public ULong getOasOperationId() {
            return oasOperationId;
        }

        public ULong getOasTagId() {
            return oasTagId;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        public ULong execute() {
            return insertOasResourceTag(this);
        }
    }
    public InsertOasResourceTagArguments insertOasResourceTag() {
        return new InsertOasResourceTagArguments();
    }
    private ULong insertOasResourceTag(InsertOasResourceTagArguments arguments) {
        return dslContext.insertInto(OAS_RESOURCE_TAG)
                .set(OAS_RESOURCE_TAG.CREATED_BY, arguments.getUserId())
                .set(OAS_RESOURCE_TAG.LAST_UPDATED_BY, arguments.getUserId())
                .set(OAS_RESOURCE_TAG.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_RESOURCE_TAG.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_RESOURCE_TAG.OAS_OPERATION_ID, arguments.getOasOperationId())
                .set(OAS_RESOURCE_TAG.OAS_TAG_ID, arguments.getOasTagId())
                .returningResult(OAS_RESOURCE_TAG.OAS_TAG_ID)
                .fetchOne().value1();
    }

    public class InsertOasRequestArguments {
        private ULong userId;
        private ULong oasOperationId;
        private ULong oasMessageBodyId;
        private String description;
        private boolean required;
        private boolean makeArrayIndicator;
        private boolean suppressRootIndicator;
        private boolean includeMetaHeaderIndicator;
        private boolean includePaginationIndicator;
        private LocalDateTime timestamp = new Timestamp(System.currentTimeMillis()).toLocalDateTime();

        public InsertOasRequestArguments setUserId(BigInteger userId) {
            return setUserId(ULong.valueOf(userId));
        }
        public InsertOasRequestArguments setUserId(ULong userId) {
            this.userId = userId;
            return this;
        }

        public ULong getOasOperationId() {
            return oasOperationId;
        }
        public InsertOasRequestArguments setOasOperationId(BigInteger oasOperationId) {
            return setOasOperationId(ULong.valueOf(oasOperationId));
        }

        public InsertOasRequestArguments setOasOperationId(ULong oasOperationId) {
            this.oasOperationId = oasOperationId;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public InsertOasRequestArguments setDescription(String description) {
            this.description = description;
            return this;
        }
        public boolean isRequired() {
            return required;
        }

        public InsertOasRequestArguments setRequired(boolean required) {
            this.required = required;
            return this;
        }

        public ULong getOasMessageBodyId() {
            return oasMessageBodyId;
        }

        public InsertOasRequestArguments setOasMessageBodyId(ULong oasMessageBodyId) {
            this.oasMessageBodyId = oasMessageBodyId;
            return this;
        }

        public boolean isMakeArrayIndicator() {
            return makeArrayIndicator;
        }

        public InsertOasRequestArguments setMakeArrayIndicator(boolean makeArrayIndicator) {
            this.makeArrayIndicator = makeArrayIndicator;
            return this;
        }

        public boolean isSuppressRootIndicator() {
            return suppressRootIndicator;
        }

        public InsertOasRequestArguments setSuppressRootIndicator(boolean suppressRootIndicator) {
            this.suppressRootIndicator = suppressRootIndicator;
            return this;
        }

        public boolean isIncludeMetaHeaderIndicator() {
            return includeMetaHeaderIndicator;
        }

        public InsertOasRequestArguments setIncludeMetaHeaderIndicator(boolean includeMetaHeaderIndicator) {
            this.includeMetaHeaderIndicator = includeMetaHeaderIndicator;
            return this;
        }

        public boolean isIncludePaginationIndicator() {
            return includePaginationIndicator;
        }

        public InsertOasRequestArguments setIncludePaginationIndicator(boolean includePaginationIndicator) {
            this.includePaginationIndicator = includePaginationIndicator;
            return this;
        }

        public InsertOasRequestArguments setTimestamp(long millis) {
            return setTimestamp(new Timestamp(millis).toLocalDateTime());
        }
        public InsertOasRequestArguments setTimestamp(Date date) {
            return setTimestamp(new Timestamp(date.getTime()).toLocalDateTime());
        }
        public InsertOasRequestArguments setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        public ULong getUserId() {
            return userId;
        }
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        public ULong execute() {
            return insertOasRequest(this);
        }
    }
    public InsertOasRequestArguments insertOasRequest() {
        return new InsertOasRequestArguments();
    }
    private ULong insertOasRequest(InsertOasRequestArguments arguments) {
        return dslContext.insertInto(OAS_REQUEST)
                .set(OAS_REQUEST.CREATED_BY, arguments.getUserId())
                .set(OAS_REQUEST.LAST_UPDATED_BY, arguments.getUserId())
                .set(OAS_REQUEST.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_REQUEST.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_REQUEST.OAS_MESSAGE_BODY_ID, arguments.getOasMessageBodyId())
                .set(OAS_REQUEST.OAS_OPERATION_ID, arguments.getOasOperationId())
                .set(OAS_REQUEST.DESCRIPTION, arguments.getDescription())
                .set(OAS_REQUEST.SUPPRESS_ROOT_INDICATOR, (byte) (arguments.isSuppressRootIndicator() ? 1 : 0))
                .set(OAS_REQUEST.MAKE_ARRAY_INDICATOR, (byte) (arguments.makeArrayIndicator ? 1 : 0) )
                .set(OAS_REQUEST.IS_CALLBACK, (byte) 0)
                .set(OAS_REQUEST.REQUIRED, (byte)(arguments.isRequired() ? 1 : 0))
                .returningResult(OAS_REQUEST.OAS_REQUEST_ID)
                .fetchOne().value1();
    }

    public class InsertOasResponseArguments {
        private ULong userId;
        private ULong oasOperationId;
        private ULong oasMessageBodyId;
        private String description;
        private String httpStatusCode;
        private boolean makeArrayIndicator;
        private boolean suppressRootIndicator;
        private boolean includeMetaHeaderIndicator;
        private boolean includePaginationIndicator;
        private boolean includeConfirmIndicator;
        private LocalDateTime timestamp = new Timestamp(System.currentTimeMillis()).toLocalDateTime();

        public InsertOasResponseArguments setUserId(BigInteger userId) {
            return setUserId(ULong.valueOf(userId));
        }
        public InsertOasResponseArguments setUserId(ULong userId) {
            this.userId = userId;
            return this;
        }

        public ULong getOasOperationId() {
            return oasOperationId;
        }
        public InsertOasResponseArguments setOasOperationId(BigInteger oasOperationId) {
            return setOasOperationId(ULong.valueOf(oasOperationId));
        }

        public InsertOasResponseArguments setOasOperationId(ULong oasOperationId) {
            this.oasOperationId = oasOperationId;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public InsertOasResponseArguments setDescription(String description) {
            this.description = description;
            return this;
        }
        public ULong getOasMessageBodyId() {
            return oasMessageBodyId;
        }

        public InsertOasResponseArguments setOasMessageBodyId(ULong oasMessageBodyId) {
            this.oasMessageBodyId = oasMessageBodyId;
            return this;
        }

        public boolean isMakeArrayIndicator() {
            return makeArrayIndicator;
        }

        public InsertOasResponseArguments setMakeArrayIndicator(boolean makeArrayIndicator) {
            this.makeArrayIndicator = makeArrayIndicator;
            return this;
        }

        public boolean isSuppressRootIndicator() {
            return suppressRootIndicator;
        }

        public InsertOasResponseArguments setSuppressRootIndicator(boolean suppressRootIndicator) {
            this.suppressRootIndicator = suppressRootIndicator;
            return this;
        }

        public boolean isIncludeMetaHeaderIndicator() {
            return includeMetaHeaderIndicator;
        }

        public InsertOasResponseArguments setIncludeMetaHeaderIndicator(boolean includeMetaHeaderIndicator) {
            this.includeMetaHeaderIndicator = includeMetaHeaderIndicator;
            return this;
        }

        public boolean isIncludePaginationIndicator() {
            return includePaginationIndicator;
        }

        public InsertOasResponseArguments setIncludePaginationIndicator(boolean includePaginationIndicator) {
            this.includePaginationIndicator = includePaginationIndicator;
            return this;
        }

        public String getHttpStatusCode() {
            return httpStatusCode;
        }

        public InsertOasResponseArguments setHttpStatusCode(String httpStatusCode) {
            this.httpStatusCode = httpStatusCode;
            return this;
        }

        public boolean isIncludeConfirmIndicator() {
            return includeConfirmIndicator;
        }

        public InsertOasResponseArguments setIncludeConfirmIndicator(boolean includeConfirmIndicator) {
            this.includeConfirmIndicator = includeConfirmIndicator;
            return this;
        }

        public InsertOasResponseArguments setTimestamp(long millis) {
            return setTimestamp(new Timestamp(millis).toLocalDateTime());
        }
        public InsertOasResponseArguments setTimestamp(Date date) {
            return setTimestamp(new Timestamp(date.getTime()).toLocalDateTime());
        }
        public InsertOasResponseArguments setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        public ULong getUserId() {
            return userId;
        }
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        public ULong execute() {
            return insertOasResponse(this);
        }
    }
    public InsertOasResponseArguments insertOasResponse() {
        return new InsertOasResponseArguments();
    }
    private ULong insertOasResponse(InsertOasResponseArguments arguments) {
        return dslContext.insertInto(OAS_RESPONSE)
                .set(OAS_RESPONSE.CREATED_BY, arguments.getUserId())
                .set(OAS_RESPONSE.LAST_UPDATED_BY, arguments.getUserId())
                .set(OAS_RESPONSE.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_RESPONSE.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_RESPONSE.OAS_MESSAGE_BODY_ID, arguments.getOasMessageBodyId())
                .set(OAS_RESPONSE.OAS_OPERATION_ID, arguments.getOasOperationId())
                .set(OAS_RESPONSE.DESCRIPTION, arguments.getDescription())
                .set(OAS_RESPONSE.SUPPRESS_ROOT_INDICATOR, (byte) (arguments.isSuppressRootIndicator() ? 1 : 0))
                .set(OAS_RESPONSE.MAKE_ARRAY_INDICATOR, (byte) (arguments.makeArrayIndicator ? 1 : 0) )
                .set(OAS_RESPONSE.INCLUDE_CONFIRM_INDICATOR, (byte) 0)
                .returningResult(OAS_RESPONSE.OAS_RESPONSE_ID)
                .fetchOne().value1();
    }

}
