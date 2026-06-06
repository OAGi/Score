package org.oagi.score.gateway.http.api.oas_management.repository.jooq;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.*;
import org.oagi.score.gateway.http.api.oas_management.model.*;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocQueryRepository;
import org.oagi.score.gateway.http.api.oas_management.repository.criteria.SelectBieForOasDocListArguments;
import org.oagi.score.gateway.http.common.model.AccessControl;
import org.oagi.score.gateway.http.common.model.PageResponse;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.SortDirection;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.OasRequestRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.OasResponseRecord;
import org.oagi.score.gateway.http.common.util.StringUtils;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.model.ScoreRole.DEVELOPER;
import static org.oagi.score.gateway.http.common.model.ScoreRole.END_USER;
import static org.oagi.score.gateway.http.common.model.SortDirection.ASC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.util.DSLUtils.contains;
import static org.oagi.score.gateway.http.common.util.StringUtils.trim;

public class JooqOasDocQueryRepository extends JooqBaseRepository
        implements OasDocQueryRepository {

    public JooqOasDocQueryRepository(DSLContext dslContext,
                                     ScoreUser requester,
                                     RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    private SelectOnConditionStep select() {
        return dslContext().select(concat(fields(
                        OAS_DOC.OAS_DOC_ID,
                        OAS_DOC.GUID,
                        OAS_DOC.OPEN_API_VERSION,
                        OAS_DOC.TITLE,
                        OAS_DOC.DESCRIPTION,
                        OAS_DOC.TERMS_OF_SERVICE,
                        OAS_DOC.VERSION,
                        OAS_DOC.CONTACT_NAME,
                        OAS_DOC.CONTACT_URL,
                        OAS_DOC.CONTACT_EMAIL,
                        OAS_DOC.LICENSE_NAME,
                        OAS_DOC.LICENSE_URL,
                        OAS_DOC.CREATION_TIMESTAMP,
                        OAS_DOC.LAST_UPDATE_TIMESTAMP
                ), ownerFields(), creatorFields(), updaterFields()))
                .from(OAS_DOC)
                .join(ownerTable()).on(ownerTablePk().eq(OAS_DOC.OWNER_USER_ID))
                .join(creatorTable()).on(creatorTablePk().eq(OAS_DOC.CREATED_BY))
                .join(updaterTable()).on(updaterTablePk().eq(OAS_DOC.LAST_UPDATED_BY));
    }

    private RecordMapper<Record, OasDoc> mapper() {
        return record -> {
            OasDoc oasDoc = new OasDoc();
            oasDoc.setOasDocId(record.get(OAS_DOC.OAS_DOC_ID).toBigInteger());
            oasDoc.setGuid(record.get(OAS_DOC.GUID));
            oasDoc.setOpenAPIVersion(record.get(OAS_DOC.OPEN_API_VERSION));
            oasDoc.setTitle(record.get(OAS_DOC.TITLE));
            oasDoc.setDescription(record.get(OAS_DOC.DESCRIPTION));
            oasDoc.setTermsOfService(record.get(OAS_DOC.TERMS_OF_SERVICE));
            oasDoc.setVersion(record.get(OAS_DOC.VERSION));
            oasDoc.setContactName(record.get(OAS_DOC.CONTACT_NAME));
            oasDoc.setContactUrl(record.get(OAS_DOC.CONTACT_URL));
            oasDoc.setContactEmail(record.get(OAS_DOC.CONTACT_EMAIL));
            oasDoc.setLicenseName(record.get(OAS_DOC.LICENSE_NAME));
            oasDoc.setLicenseUrl(record.get(OAS_DOC.LICENSE_URL));
            oasDoc.setOwnerUserId(fetchOwnerUserId(record));
            oasDoc.setCreatedBy(fetchCreatorSummary(record));
            oasDoc.setLastUpdatedBy(fetchUpdaterSummary(record));
            oasDoc.setCreationTimestamp(toDate(record.get(OAS_DOC.CREATION_TIMESTAMP)));
            oasDoc.setLastUpdateTimestamp(toDate(record.get(OAS_DOC.LAST_UPDATE_TIMESTAMP)));
            return oasDoc;
        };
    }

    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetOasDocResponse getOasDoc(
            GetOasDocRequest request) throws ScoreDataAccessException {
        OasDoc oasDoc = null;

        OasDocId oasDocId = request.getOasDocId();
        if (oasDocId != null) {
            oasDoc = (OasDoc) select()
                    .where(OAS_DOC.OAS_DOC_ID.eq(valueOf(oasDocId)))
                    .fetchOne(mapper());
            // Issue #1729: attach the configured Security Schemes (empty list = default OAuth2).
            if (oasDoc != null) {
                oasDoc.setSecuritySchemes(loadSecuritySchemes(oasDocId));
                oasDoc.setSecurityRequirements(loadDocSecurityRequirements(oasDocId));
            }
        }

        return new GetOasDocResponse(oasDoc);
    }

    // Issue #1729: load the document's Security Schemes in insertion order; empty list = OAuth2 default.
    private List<OasSecurityScheme> loadSecuritySchemes(OasDocId oasDocId) {
        List<OasSecurityScheme> schemes = dslContext().selectFrom(OAS_SECURITY_SCHEME)
                .where(OAS_SECURITY_SCHEME.OAS_DOC_ID.eq(valueOf(oasDocId)))
                .orderBy(OAS_SECURITY_SCHEME.OAS_SECURITY_SCHEME_ID.asc())
                .fetch()
                .map(record -> {
                    OasSecurityScheme scheme = new OasSecurityScheme();
                    scheme.setOasSecuritySchemeId(record.getOasSecuritySchemeId().toBigInteger());
                    scheme.setGuid(record.getGuid());
                    scheme.setType(record.getType());
                    scheme.setSchemeName(record.getSchemeName());
                    scheme.setDescription(record.getDescription());
                    scheme.setApiKeyName(record.getApiKeyName());
                    scheme.setApiKeyIn(record.getApiKeyIn());
                    scheme.setHttpScheme(record.getHttpScheme());
                    scheme.setBearerFormat(record.getBearerFormat());
                    scheme.setOpenIdConnectUrl(record.getOpenIdConnectUrl());
                    return scheme;
                });
        // Issue #1729: attach the OAuth Flows Object (flows + scopes) for oauth2 schemes.
        for (OasSecurityScheme scheme : schemes) {
            if ("oauth2".equalsIgnoreCase(scheme.getType())) {
                scheme.setFlows(loadOAuthFlows(ULong.valueOf(scheme.getOasSecuritySchemeId())));
            }
        }
        return schemes;
    }

    private List<OasSecurityRequirement> loadDocSecurityRequirements(OasDocId oasDocId) {
        Map<Integer, OasSecurityRequirement> requirements = new LinkedHashMap<>();
        // Issue #1729: oas_doc_security references the scheme by oas_security_scheme_id; join to resolve
        // the components.securitySchemes key (a NULL id is an anonymous {} requirement).
        dslContext().select(
                        OAS_DOC_SECURITY.OAS_DOC_SECURITY_ID,
                        OAS_DOC_SECURITY.REQUIREMENT_GROUP,
                        OAS_DOC_SECURITY.OAS_SECURITY_SCHEME_ID,
                        OAS_SECURITY_SCHEME.SCHEME_NAME)
                .from(OAS_DOC_SECURITY)
                .leftJoin(OAS_SECURITY_SCHEME)
                .on(OAS_SECURITY_SCHEME.OAS_SECURITY_SCHEME_ID.eq(OAS_DOC_SECURITY.OAS_SECURITY_SCHEME_ID))
                .where(OAS_DOC_SECURITY.OAS_DOC_ID.eq(valueOf(oasDocId)))
                .orderBy(OAS_DOC_SECURITY.REQUIREMENT_GROUP.asc(), OAS_DOC_SECURITY.OAS_DOC_SECURITY_ID.asc())
                .fetch()
                .forEach(record -> {
                    Integer group = record.get(OAS_DOC_SECURITY.REQUIREMENT_GROUP);
                    OasSecurityRequirement requirement = requirements.computeIfAbsent(group, k -> {
                        OasSecurityRequirement r = new OasSecurityRequirement();
                        r.setSchemes(new ArrayList<>());
                        return r;
                    });
                    String schemeName = record.get(OAS_SECURITY_SCHEME.SCHEME_NAME);
                    if (record.get(OAS_DOC_SECURITY.OAS_SECURITY_SCHEME_ID) == null
                            || !StringUtils.hasLength(schemeName)) {
                        requirement.setAnonymous(true);
                    } else if (!requirement.isAnonymous()) {
                        OasSecurityRequirementScheme scheme = new OasSecurityRequirementScheme();
                        scheme.setSchemeName(schemeName);
                        scheme.setScopes(loadDocSecurityScopes(record.get(OAS_DOC_SECURITY.OAS_DOC_SECURITY_ID)));
                        requirement.getSchemes().add(scheme);
                    }
                });
        return new ArrayList<>(requirements.values());
    }

    private List<String> loadDocSecurityScopes(ULong oasDocSecurityId) {
        return dslContext().select(OAS_DOC_SECURITY_SCOPE.SCOPE_NAME)
                .from(OAS_DOC_SECURITY_SCOPE)
                .where(OAS_DOC_SECURITY_SCOPE.OAS_DOC_SECURITY_ID.eq(oasDocSecurityId))
                .orderBy(OAS_DOC_SECURITY_SCOPE.OAS_DOC_SECURITY_SCOPE_ID.asc())
                .fetchInto(String.class);
    }

    private List<OasOAuthFlow> loadOAuthFlows(ULong oasSecuritySchemeId) {
        return dslContext().selectFrom(OAS_OAUTH_FLOW)
                .where(OAS_OAUTH_FLOW.OAS_SECURITY_SCHEME_ID.eq(oasSecuritySchemeId))
                .orderBy(OAS_OAUTH_FLOW.OAS_OAUTH_FLOW_ID.asc())
                .fetch()
                .map(record -> {
                    OasOAuthFlow flow = new OasOAuthFlow();
                    flow.setOasOAuthFlowId(record.getOasOauthFlowId().toBigInteger());
                    flow.setGuid(record.getGuid());
                    flow.setFlowType(record.getFlowType());
                    flow.setAuthorizationUrl(record.getAuthorizationUrl());
                    flow.setTokenUrl(record.getTokenUrl());
                    flow.setRefreshUrl(record.getRefreshUrl());
                    flow.setDeviceAuthorizationUrl(record.getDeviceAuthorizationUrl());
                    flow.setScopes(loadOAuthScopes(record.getOasOauthFlowId()));
                    return flow;
                });
    }

    private List<OasOAuthScope> loadOAuthScopes(ULong oasOAuthFlowId) {
        return dslContext().selectFrom(OAS_OAUTH_SCOPE)
                .where(OAS_OAUTH_SCOPE.OAS_OAUTH_FLOW_ID.eq(oasOAuthFlowId))
                .orderBy(OAS_OAUTH_SCOPE.OAS_OAUTH_SCOPE_ID.asc())
                .fetch()
                .map(record -> {
                    OasOAuthScope scope = new OasOAuthScope();
                    scope.setOasOAuthScopeId(record.getOasOauthScopeId().toBigInteger());
                    scope.setGuid(record.getGuid());
                    scope.setScopeName(record.getScopeName());
                    scope.setDescription(record.getDescription());
                    return scope;
                });
    }

    private Collection<Condition> getConditions(GetOasDocListRequest request) {
        List<Condition> conditions = new ArrayList();

        if (request.getOasDocIdList() != null && !request.getOasDocIdList().isEmpty()) {
            if (request.getOasDocIdList().size() == 1) {
                conditions.add(OAS_DOC.OAS_DOC_ID.eq(
                        ULong.valueOf(request.getOasDocIdList().iterator().next())
                ));
            } else {
                conditions.add(OAS_DOC.OAS_DOC_ID.in(
                        request.getOasDocIdList().stream()
                                .map(e -> ULong.valueOf(e)).collect(Collectors.toList())
                ));
            }
        }
        if (StringUtils.hasLength(request.getTitle())) {
            conditions.addAll(contains(request.getTitle(), OAS_DOC.TITLE));
        }
        if (StringUtils.hasLength(request.getOpenAPIVersion())) {
            conditions.addAll(contains(request.getOpenAPIVersion(), OAS_DOC.OPEN_API_VERSION));
        }
        if (StringUtils.hasLength(request.getVersion())) {
            conditions.addAll(contains(request.getVersion(), OAS_DOC.VERSION));
        }
        if (StringUtils.hasLength(request.getLicenseName())) {
            conditions.addAll(contains(request.getLicenseName(), OAS_DOC.LICENSE_NAME));
        }
        if (StringUtils.hasLength(request.getDescription())) {
            conditions.addAll(contains(request.getDescription(), OAS_DOC.DESCRIPTION));
        }
        if (!request.getUpdaterUsernameList().isEmpty()) {
            conditions.add(APP_USER.as("updater").LOGIN_ID.in(
                    new HashSet<>(request.getUpdaterUsernameList()).stream()
                            .filter(e -> StringUtils.hasLength(e)).map(e -> trim(e)).collect(Collectors.toList())
            ));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(OAS_DOC.LAST_UPDATE_TIMESTAMP.greaterOrEqual(request.getUpdateStartDate()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(OAS_DOC.LAST_UPDATE_TIMESTAMP.lessThan(request.getUpdateEndDate()));
        }

        return conditions;
    }

    private List<SortField<?>> getSortFields(GetOasDocListRequest request) {
        List<SortField<?>> sortFields = new ArrayList<>();

        for (int i = 0, len = request.getSortActives().size(); i < len; ++i) {
            String sortActive = request.getSortActives().get(i);
            SortDirection sortDirection = request.getSortDirections().get(i);

            Field field;
            switch (sortActive.toLowerCase()) {
                case "title":
                    field = OAS_DOC.TITLE;
                    break;
                case "openapiversion":
                    field = OAS_DOC.OPEN_API_VERSION;
                    break;
                case "version":
                    field = OAS_DOC.VERSION;
                    break;
                case "licensename":
                    field = OAS_DOC.LICENSE_NAME;
                    break;
                case "description":
                    field = OAS_DOC.DESCRIPTION;
                    break;
                case "lastupdatetimestamp":
                    field = OAS_DOC.LAST_UPDATE_TIMESTAMP;
                    break;
                default:
                    continue;
            }

            if (sortDirection == ASC) {
                sortFields.add(field.asc());
            } else {
                sortFields.add(field.desc());
            }
        }
        return sortFields;
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetOasDocListResponse getOasDocList(
            GetOasDocListRequest request) throws ScoreDataAccessException {
        Collection<Condition> conditions = getConditions(request);
        SelectConditionStep conditionStep = select().where(conditions);

        List<SortField<?>> sortFields = getSortFields(request);
        int length = dslContext().fetchCount(conditionStep);
        SelectFinalStep finalStep;
        if (sortFields == null || sortFields.isEmpty()) {
            if (request.isPagination()) {
                finalStep = conditionStep.limit(request.getPageOffset(), request.getPageSize());
            } else {
                finalStep = conditionStep;
            }
        } else {
            if (request.isPagination()) {
                finalStep = conditionStep.orderBy(sortFields)
                        .limit(request.getPageOffset(), request.getPageSize());
            } else {
                finalStep = conditionStep.orderBy(sortFields);
            }
        }

        return new GetOasDocListResponse(
                finalStep.fetch(mapper()),
                request.getPageIndex(),
                request.getPageSize(),
                length
        );
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetOasOperationResponse getOasOperation(GetOasOperationRequest request) throws ScoreDataAccessException {
        OasOperation oasOperation = null;
        OasResourceId oasResourceId = request.getOasResourceId();
        if (oasResourceId != null) {
            oasOperation = (OasOperation) selectForOasOperation()
                    .where(OAS_OPERATION.OAS_RESOURCE_ID.eq(valueOf(oasResourceId)))
                    .fetchOne(mapperForOasOperation());
        }
        return new GetOasOperationResponse(oasOperation);
    }

    private RecordMapper<Record, OasOperation> mapperForOasOperation() {
        return record -> {
            OasOperation oasOperation = new OasOperation();
            oasOperation.setOasOperationId(new OasOperationId(record.get(OAS_OPERATION.OAS_OPERATION_ID).toBigInteger()));
            oasOperation.setOasResourceId(new OasResourceId(record.get(OAS_OPERATION.OAS_RESOURCE_ID).toBigInteger()));
            oasOperation.setVerb(record.get(OAS_OPERATION.VERB));
            oasOperation.setDeprecated((byte) 1 == record.get(OAS_OPERATION.DEPRECATED));
            oasOperation.setOperationId(record.get(OAS_OPERATION.OPERATION_ID));
            oasOperation.setSummary(record.get(OAS_OPERATION.SUMMARY));
            oasOperation.setDescription(record.get(OAS_OPERATION.DESCRIPTION));
            oasOperation.setCreatedBy(fetchCreatorSummary(record));
            oasOperation.setLastUpdatedBy(fetchUpdaterSummary(record));
            oasOperation.setCreationTimestamp(toDate(record.get(OAS_OPERATION.CREATION_TIMESTAMP)));
            oasOperation.setLastUpdateTimestamp(toDate(record.get(OAS_OPERATION.LAST_UPDATE_TIMESTAMP)));
            return oasOperation;
        };
    }

    private SelectOnConditionStep selectForOasOperation() {
        return dslContext().select(concat(fields(
                        OAS_OPERATION.OAS_OPERATION_ID,
                        OAS_OPERATION.OAS_RESOURCE_ID,
                        OAS_OPERATION.DESCRIPTION,
                        OAS_OPERATION.OPERATION_ID,
                        OAS_OPERATION.VERB,
                        OAS_OPERATION.SUMMARY,
                        OAS_OPERATION.DEPRECATED,
                        OAS_OPERATION.CREATION_TIMESTAMP,
                        OAS_OPERATION.LAST_UPDATE_TIMESTAMP
                ), creatorFields(), updaterFields()))
                .from(OAS_OPERATION)
                .join(creatorTable()).on(creatorTablePk().eq(OAS_OPERATION.CREATED_BY))
                .join(updaterTable()).on(updaterTablePk().eq(OAS_OPERATION.LAST_UPDATED_BY));
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetOasRequestTableResponse getOasRequestTable(GetOasRequestTableRequest request) throws ScoreDataAccessException {
        OasRequest oasRequest = null;
        OasRequestRecord oasRequestRecord = null;
        OasOperationId oasOperationId = request.getOasOperationId();
        if (oasOperationId != null) {
            oasRequestRecord = dslContext().selectFrom(OAS_REQUEST).where(OAS_REQUEST.OAS_OPERATION_ID
                    .eq(valueOf(oasOperationId))).fetchOptional().orElse(null);
        }
        if (oasRequestRecord != null) {
            oasRequest = (OasRequest) selectForOasRequestTable()
                    .where(OAS_REQUEST.OAS_OPERATION_ID.eq(valueOf(oasOperationId)))
                    .fetchOne(mapperForOasRequestTable());
            return new GetOasRequestTableResponse(oasRequest);
        }
        return null;
    }

    private SelectOnConditionStep selectForOasRequestTable() {
        return dslContext().select(concat(fields(
                        OAS_REQUEST.OAS_REQUEST_ID,
                        OAS_REQUEST.OAS_OPERATION_ID,
                        OAS_REQUEST.DESCRIPTION,
                        OAS_REQUEST.REQUIRED,
                        OAS_REQUEST.OAS_MESSAGE_BODY_ID,
                        OAS_REQUEST.MAKE_ARRAY_INDICATOR,
                        OAS_REQUEST.SUPPRESS_ROOT_INDICATOR,
                        OAS_REQUEST.IS_CALLBACK,
                        OAS_REQUEST.CREATION_TIMESTAMP,
                        OAS_REQUEST.LAST_UPDATE_TIMESTAMP
                ), creatorFields(), updaterFields()))
                .from(OAS_REQUEST)
                .join(creatorTable()).on(creatorTablePk().eq(OAS_REQUEST.CREATED_BY))
                .join(updaterTable()).on(updaterTablePk().eq(OAS_REQUEST.LAST_UPDATED_BY));
    }

    private RecordMapper<Record, OasRequest> mapperForOasRequestTable() {
        return record -> {
            OasRequest oasRequest = new OasRequest();
            oasRequest.setOasRequestId(new OasRequestId(record.get(OAS_REQUEST.OAS_REQUEST_ID).toBigInteger()));
            oasRequest.setOasOperationId(new OasOperationId(record.get(OAS_REQUEST.OAS_OPERATION_ID).toBigInteger()));
            oasRequest.setDescription(record.get(OAS_REQUEST.DESCRIPTION));
            oasRequest.setRequired((byte) 1 == record.get(OAS_REQUEST.REQUIRED));
            oasRequest.setOasMessageBodyId(new OasMessageBodyId(record.get(OAS_REQUEST.OAS_MESSAGE_BODY_ID).toBigInteger()));
            oasRequest.setMakeArrayIndicator((byte) 1 == record.get(OAS_REQUEST.MAKE_ARRAY_INDICATOR));
            oasRequest.setSuppressRootIndicator((byte) 1 == record.get(OAS_REQUEST.SUPPRESS_ROOT_INDICATOR));
            oasRequest.setCallback((byte) 1 == record.get(OAS_REQUEST.IS_CALLBACK));
            oasRequest.setCreatedBy(fetchCreatorSummary(record));
            oasRequest.setLastUpdatedBy(fetchUpdaterSummary(record));
            oasRequest.setCreationTimestamp(toDate(record.get(OAS_REQUEST.CREATION_TIMESTAMP)));
            oasRequest.setLastUpdateTimestamp(toDate(record.get(OAS_REQUEST.LAST_UPDATE_TIMESTAMP)));
            return oasRequest;
        };
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetOasResponseTableResponse getOasResponseTable(GetOasResponseTableRequest request) throws ScoreDataAccessException {
        OasResponse oasResponse = null;
        OasResponseRecord oasResponseRecord = null;
        OasOperationId oasOperationId = request.getOasOperationId();
        if (oasOperationId != null) {
            oasResponseRecord = dslContext().selectFrom(OAS_RESPONSE).
                    where(OAS_RESPONSE.OAS_OPERATION_ID.eq(valueOf(oasOperationId))).fetchOptional().orElse(null);
        }
        if (oasResponseRecord != null) {
            oasResponse = (OasResponse) selectForOasResponseTable()
                    .where(OAS_RESPONSE.OAS_OPERATION_ID.eq(valueOf(oasOperationId)))
                    .fetchOne(mapperForOasResponseTable());
        }
        return new GetOasResponseTableResponse(oasResponse);
    }

    private SelectOnConditionStep selectForOasResponseTable() {
        return dslContext().select(concat(fields(
                        OAS_RESPONSE.OAS_RESPONSE_ID,
                        OAS_RESPONSE.OAS_OPERATION_ID,
                        OAS_RESPONSE.DESCRIPTION,
                        OAS_RESPONSE.OAS_MESSAGE_BODY_ID,
                        OAS_RESPONSE.MAKE_ARRAY_INDICATOR,
                        OAS_RESPONSE.SUPPRESS_ROOT_INDICATOR,
                        OAS_RESPONSE.INCLUDE_CONFIRM_INDICATOR,
                        OAS_RESPONSE.CREATION_TIMESTAMP,
                        OAS_RESPONSE.LAST_UPDATE_TIMESTAMP
                ), creatorFields(), updaterFields()))
                .from(OAS_RESPONSE)
                .join(creatorTable()).on(creatorTablePk().eq(OAS_RESPONSE.CREATED_BY))
                .join(updaterTable()).on(updaterTablePk().eq(OAS_RESPONSE.LAST_UPDATED_BY));
    }

    private RecordMapper<Record, OasResponse> mapperForOasResponseTable() {
        return record -> {
            OasResponse oasResponse = new OasResponse();
            oasResponse.setOasResponseId(new OasResponseId(record.get(OAS_RESPONSE.OAS_RESPONSE_ID).toBigInteger()));
            oasResponse.setOasOperationId(new OasOperationId(record.get(OAS_RESPONSE.OAS_OPERATION_ID).toBigInteger()));
            oasResponse.setDescription(record.get(OAS_RESPONSE.DESCRIPTION));
            oasResponse.setOasMessageBodyId(new OasMessageBodyId(record.get(OAS_RESPONSE.OAS_MESSAGE_BODY_ID).toBigInteger()));
            oasResponse.setMakeArrayIndicator((byte) 1 == record.get(OAS_RESPONSE.MAKE_ARRAY_INDICATOR));
            oasResponse.setSuppressRootIndicator((byte) 1 == record.get(OAS_RESPONSE.SUPPRESS_ROOT_INDICATOR));
            oasResponse.setIncludeConfirmIndicator((byte) 1 == record.get(OAS_RESPONSE.INCLUDE_CONFIRM_INDICATOR));
            oasResponse.setCreatedBy(fetchCreatorSummary(record));
            oasResponse.setLastUpdatedBy(fetchUpdaterSummary(record));
            oasResponse.setCreationTimestamp(toDate(record.get(OAS_RESPONSE.CREATION_TIMESTAMP)));
            oasResponse.setLastUpdateTimestamp(toDate(record.get(OAS_RESPONSE.LAST_UPDATE_TIMESTAMP)));
            return oasResponse;
        };
    }

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
            return dslContext().selectCount()
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
            return dslContext().selectCount()
                    .from(OAS_DOC)
                    .where(conditions)
                    .fetchOneInto(Integer.class) == 0;
        } else
            throw new ScoreDataAccessException("Wrong input data");
    }

    @Override
    public PageResponse<BieForOasDoc> selectBieForOasDocList(
            SelectBieForOasDocListArguments arguments) throws ScoreDataAccessException {

        SelectOnConditionStep<Record> step = getSelectOnConditionStep(arguments);
        SelectConnectByStep<Record> conditionStep = step.where(arguments.getConditions());

        int pageCount = dslContext().fetchCount(conditionStep);

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

        return new PageResponse<>((offsetStep != null) ?
                offsetStep.fetch(mapperForSelectBieForOasDoc()) : conditionStep.fetch(mapperForSelectBieForOasDoc()),
                arguments.getOffset(), arguments.getNumberOfRows(), pageCount);
    }

    private SelectOnConditionStep<Record> getSelectOnConditionStep(SelectBieForOasDocListArguments arguments) {
        List<Field> selectFields = arguments.selectFields();
        return dslContext().selectDistinct(selectFields)
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
                .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                .join(BIZ_CTX_ASSIGNMENT).on(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID))
                .join(BIZ_CTX).on(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID.eq(BIZ_CTX.BIZ_CTX_ID));
    }

    private RecordMapper<Record, BieForOasDoc> mapperForSelectBieForOasDoc() {
        return record -> {
            BieForOasDoc bieForOasDoc = new BieForOasDoc();
            bieForOasDoc.setTopLevelAsbiepId(new TopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger()));
            bieForOasDoc.setVersion(record.get(TOP_LEVEL_ASBIEP.VERSION));
            bieForOasDoc.setStatus(record.get(TOP_LEVEL_ASBIEP.STATUS));
            bieForOasDoc.setGuid(record.get(ASBIEP.GUID));
            bieForOasDoc.setDen(record.get(ASCCP_MANIFEST.DEN));
            bieForOasDoc.setPropertyTerm(record.get(ASCCP.PROPERTY_TERM));
            bieForOasDoc.setDisplayName(record.get(ASBIEP.DISPLAY_NAME));
            bieForOasDoc.setRemark(record.get(ASBIEP.REMARK));
            bieForOasDoc.setReleaseNum(record.get(RELEASE.RELEASE_NUM));
            bieForOasDoc.setOwner(new UserSummaryRecord(
                    new UserId(record.get(TOP_LEVEL_ASBIEP.OWNER_USER_ID).toBigInteger()),
                    record.get(APP_USER.LOGIN_ID.as("owner")),
                    null, Collections.emptyList()));
            bieForOasDoc.setLastUpdatedBy(new UserSummaryRecord(
                    null,
                    record.get(APP_USER.as("updater").LOGIN_ID.as("last_update_user")),
                    null, Collections.emptyList()));
            bieForOasDoc.setLastUpdateTimestamp(
                    Date.from(record.get(TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant())
            );
            bieForOasDoc.setState(BieState.valueOf(record.get(TOP_LEVEL_ASBIEP.STATE)));
            return bieForOasDoc;
        };
    }

}
