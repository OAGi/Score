package org.oagi.score.gateway.http.api.oas_management.service;

import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.context_management.business_context.service.BusinessContextQueryService;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.*;
import org.oagi.score.gateway.http.api.oas_management.model.*;
import org.oagi.score.gateway.http.api.oas_management.repository.BieForOasDocCommandRepository;
import org.oagi.score.gateway.http.api.oas_management.repository.BieForOasDocQueryRepository;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocCommandRepository;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocQueryRepository;
import org.oagi.score.gateway.http.api.oas_management.repository.criteria.*;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.PageResponse;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.OasDoc.OAS_DOC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.OasSecurityScheme.OAS_SECURITY_SCHEME;
import static org.oagi.score.gateway.http.common.util.ScoreGuidUtils.randomGuid;

@Service
@Transactional(readOnly = true)
public class OpenAPIDocService {

    private static final int OPEN_API_VERSION_MAX_LENGTH = OAS_DOC.OPEN_API_VERSION.getDataType().length();
    private static final int TERMS_OF_SERVICE_MAX_LENGTH = OAS_DOC.TERMS_OF_SERVICE.getDataType().length();
    private static final int VERSION_MAX_LENGTH = OAS_DOC.VERSION.getDataType().length();
    private static final int CONTACT_URL_MAX_LENGTH = OAS_DOC.CONTACT_URL.getDataType().length();
    private static final int LICENSE_NAME_MAX_LENGTH = OAS_DOC.LICENSE_NAME.getDataType().length();
    private static final int LICENSE_URL_MAX_LENGTH = OAS_DOC.LICENSE_URL.getDataType().length();
    // Issue #1729
    private static final int SCHEME_NAME_MAX_LENGTH = OAS_SECURITY_SCHEME.SCHEME_NAME.getDataType().length();
    private static final int API_KEY_NAME_MAX_LENGTH = OAS_SECURITY_SCHEME.API_KEY_NAME.getDataType().length();
    private static final int BEARER_FORMAT_MAX_LENGTH = OAS_SECURITY_SCHEME.BEARER_FORMAT.getDataType().length();
    private static final int OPEN_ID_CONNECT_URL_MAX_LENGTH = OAS_SECURITY_SCHEME.OPEN_ID_CONNECT_URL.getDataType().length();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RepositoryFactory repositoryFactory;

    private OasDocCommandRepository command(ScoreUser requester) {
        return repositoryFactory.oasDocCommandRepository(requester);
    };

    private OasDocQueryRepository query(ScoreUser requester) {
        return repositoryFactory.oasDocQueryRepository(requester);
    };

    private BieForOasDocCommandRepository bieForOasDocCommand(ScoreUser requester) {
        return repositoryFactory.bieForOasDocCommandRepository(requester);
    }

    private BieForOasDocQueryRepository bieForOasDocQuery(ScoreUser requester) {
        return repositoryFactory.bieForOasDocQueryRepository(requester);
    }

    @Autowired
    private BusinessContextQueryService businessContextQueryService;

    public GetOasDocResponse getOasDoc(ScoreUser requester, GetOasDocRequest request) {
        GetOasDocResponse response = query(requester).getOasDoc(request);
        return response;
    }

    public GetOasDocListResponse getOasDocList(ScoreUser requester, GetOasDocListRequest request) {
        GetOasDocListResponse response = query(requester).getOasDocList(request);
        return response;
    }

    @Transactional
    public CreateOasDocResponse createOasDoc(ScoreUser requester, CreateOasDocRequest request) {
        validateOasDocRequest(
                request.getOpenAPIVersion(),
                request.getTermsOfService(),
                request.getVersion(),
                request.getContactUrl(),
                request.getLicenseName(),
                request.getLicenseUrl());
        validateSecuritySchemes(request.getSecuritySchemes());
        validateSecurityRequirements(request.getSecuritySchemes(), request.getSecurityRequirements());
        CreateOasDocResponse response = command(requester).createOasDoc(request);
        return response;
    }

    @Transactional
    public UpdateOasDocResponse updateOasDoc(ScoreUser requester, UpdateOasDocRequest request) {
        validateOasDocRequest(
                request.getOpenAPIVersion(),
                request.getTermsOfService(),
                request.getVersion(),
                request.getContactUrl(),
                request.getLicenseName(),
                request.getLicenseUrl());
        validateSecuritySchemes(request.getSecuritySchemes());
        validateSecurityRequirements(request.getSecuritySchemes(), request.getSecurityRequirements());
        UpdateOasDocResponse response = command(requester).updateOasDoc(request);
        return response;
    }

    @Transactional
    public DeleteOasDocResponse deleteOasDoc(ScoreUser requester, DeleteOasDocRequest request) {
        DeleteOasDocResponse response = command(requester).deleteOasDoc(request);
        return response;
    }

    public GetBieForOasDocResponse getBieForOasDoc(ScoreUser requester, GetBieForOasDocRequest request) {
        GetBieForOasDocResponse response = bieForOasDocQuery(requester).getBieForOasDoc(request);
        return response;
    }

    public PageResponse<BieForOasDoc> selectBieForOasDoc(ScoreUser requester, BieForOasDocListRequest request) {
        PageRequest pageRequest = request.getPageRequest();
        PageResponse<BieForOasDoc> result = new SelectBieForOasDocListArguments(
                repositoryFactory.oasDocQueryRepository(requester))
                .setOasDocId(request.getOasDocId())
                .setDen(request.getDen())
                .setPropertyTerm(request.getPropertyTerm())
                .setBusinessContext(request.getBusinessContext())
                .setVersion(request.getVersion())
                .setRemark(request.getRemark())
                .setAsccpManifestId(request.getAsccpManifestId())
                .setExcludePropertyTerms(request.getExcludePropertyTerms())
                .setExcludeTopLevelAsbiepIds(request.getExcludeTopLevelAsbiepIds())
                .setStates(request.getStates())
                .setLibraryId(request.getLibraryId())
                .setReleaseId(request.getReleaseId())
                .setOwnerLoginIdList(request.getOwnerLoginIdList())
                .setUpdaterLoginIdList(request.getUpdaterLoginIdList())
                .setUpdateDate(request.getUpdateStartDate(), request.getUpdateEndDate())
                .setAccess(ULong.valueOf(requester.userId().value()), request.getAccess())
                .setOwnedByDeveloper(request.getOwnedByDeveloper())
                .setSort((pageRequest.sorts().isEmpty()) ? null : pageRequest.sorts().get(0).field(),
                        (pageRequest.sorts().isEmpty()) ? null : pageRequest.sorts().get(0).direction().name())
                .setOffset(pageRequest.pageOffset(), pageRequest.pageSize())
                .fetch();

        List<BieForOasDoc> bieForOasDocList = result.getList();
        bieForOasDocList.forEach(bieForOasDoc -> {
            bieForOasDoc.setBusinessContexts(
                    businessContextQueryService.getBusinessContextSummaryList(
                            requester, bieForOasDoc.getTopLevelAsbiepId(), request.getBusinessContext())
            );
        });

        PageResponse<BieForOasDoc> response = new PageResponse();
        response.setList(bieForOasDocList);
        response.setPage(pageRequest.pageIndex());
        response.setSize(pageRequest.pageSize());
        response.setLength(result.getLength());
        return response;
    }

    @Transactional
    public AddBieForOasDocResponse addBieForOasDoc(ScoreUser requester, AddBieForOasDocRequest request) {
        UserId userId = requester.userId();
        if (userId == null) {
            throw new IllegalArgumentException("`userId` parameter must not be null.");
        }
        if (request.getOasDocId() == null) {
            throw new IllegalArgumentException("`oasDocId` parameter must not be null.");
        }
        if (request.getTopLevelAsbiepId() == null) {
            throw new IllegalArgumentException("`TopLevelAsbiepId` parameter must not be null.");
        }

        long millis = System.currentTimeMillis();

        var command = repositoryFactory.oasDocCommandRepository(requester);

        OasMessageBodyId oasMessageBodyId = new InsertOasMessageBodyArguments(command)
                .setUserId(userId)
                .setTopLevelAsbiepId(request.getTopLevelAsbiepId())
                .setTimestamp(millis)
                .execute();

        OasResourceId oasResourceId = new InsertOasResourceArguments(command)
                .setUserId(userId)
                .setOasDocId(request.getOasDocId())
                .setPath(request.getPath())
                .setRef(request.getRef())
                .setTimestamp(millis)
                .execute();

        OasOperationId oasOperationId = new InsertOasOperationArguments(command)
                .setUserId(userId)
                .setOperationId(request.getOperationId())
                .setOasResourceId(oasResourceId)
                .setVerb(request.getVerb())
                .setSummary(request.getSummary())
                .setDescription(request.getDescriptionForOperation())
                .setDeprecated(request.isDeprecatedForOperation())
                .setTimestamp(millis)
                .execute();

        if (request.getTagName() != null) {
            OasTagId oasTagId = new InsertOasTagArguments(command)
                    .setUserId(userId)
                    .setGuid(randomGuid())
                    .setName(request.getTagName())
                    .execute();
            new InsertOasResourceTagArguments(command)
                    .setUserId(userId)
                    .setOasOperationId(oasOperationId)
                    .setOasTagId(oasTagId)
                    .execute();
        }
        OasRequestId oasRequestId = null;
        OasResponseId oasResponseId = null;
        if (request.isOasRequest()) {
            oasRequestId = new InsertOasRequestArguments(command)
                    .setUserId(userId)
                    .setOasOperationId(oasOperationId)
                    .setOasMessageBodyId(oasMessageBodyId)
                    .setDescription(request.getDescription())
                    .setMakeArrayIndicator(request.isMakeArrayIndicator())
                    .setSuppressRootIndicator(request.isSuppressRootIndicator())
                    .setIncludePaginationIndicator(request.isIncludePaginationIndicator())
                    .setIncludeMetaHeaderIndicator(request.isIncludeMetaHeaderIndicator())
                    .setRequired(request.isRequiredForRequestBody())
                    .setTimestamp(millis)
                    .execute();
        } else {
            oasResponseId = new InsertOasResponseArguments(command)
                    .setUserId(userId)
                    .setOasOperationId(oasOperationId)
                    .setOasMessageBodyId(oasMessageBodyId)
                    .setDescription(request.getDescription())
                    .setMakeArrayIndicator(request.isMakeArrayIndicator())
                    .setSuppressRootIndicator(request.isSuppressRootIndicator())
                    .setIncludePaginationIndicator(request.isIncludePaginationIndicator())
                    .setIncludeMetaHeaderIndicator(request.isIncludeMetaHeaderIndicator())
                    .setTimestamp(millis)
                    .execute();
        }
        return new AddBieForOasDocResponse(oasRequestId != null ? oasRequestId : null,
                oasResponseId != null ? oasResponseId : null);
    }

    /**
     * Issue #1730: Adds an API operation (endpoint) that does NOT reference a BIE.
     *
     * A BIE-less message body (top_level_asbiep_id = NULL) is created and linked to either an
     * {@code oas_request} (Request-type message body, no schema) or an {@code oas_response}
     * (Response-type, carrying the chosen HTTP status code such as 202/204) without any content
     * schema, so the OpenAPI generator can emit a bodyless operation.
     */
    @Transactional
    public AddBieForOasDocResponse addOperationForOasDoc(ScoreUser requester, AddOperationForOasDocRequest request) {
        UserId userId = requester.userId();
        if (userId == null) {
            throw new IllegalArgumentException("`userId` parameter must not be null.");
        }
        if (request.getOasDocId() == null) {
            throw new IllegalArgumentException("`oasDocId` parameter must not be null.");
        }
        if (request.getVerb() == null) {
            throw new IllegalArgumentException("`verb` parameter must not be null.");
        }

        long millis = System.currentTimeMillis();

        var command = repositoryFactory.oasDocCommandRepository(requester);

        // Issue #1730: BIE-less endpoint -> empty message body (top_level_asbiep_id = NULL).
        OasMessageBodyId oasMessageBodyId = new InsertOasMessageBodyArguments(command)
                .setUserId(userId)
                .setTopLevelAsbiepId(null)
                .setTimestamp(millis)
                .execute();

        OasResourceId oasResourceId = new InsertOasResourceArguments(command)
                .setUserId(userId)
                .setOasDocId(request.getOasDocId())
                .setPath(request.getPath())
                .setRef(request.getRef())
                .setTimestamp(millis)
                .execute();

        OasOperationId oasOperationId = new InsertOasOperationArguments(command)
                .setUserId(userId)
                .setOperationId(request.getOperationId())
                .setOasResourceId(oasResourceId)
                .setVerb(request.getVerb())
                .setSummary(request.getSummary())
                .setDescription(request.getDescription())
                .setDeprecated(false)
                .setTimestamp(millis)
                .execute();

        if (request.getTagName() != null) {
            OasTagId oasTagId = new InsertOasTagArguments(command)
                    .setUserId(userId)
                    .setGuid(randomGuid())
                    .setName(request.getTagName())
                    .execute();
            new InsertOasResourceTagArguments(command)
                    .setUserId(userId)
                    .setOasOperationId(oasOperationId)
                    .setOasTagId(oasTagId)
                    .execute();
        }

        OasRequestId oasRequestId = null;
        OasResponseId oasResponseId = null;
        if (request.isOasRequest()) {
            // Request-type message body: no BIE schema and no status code; the generator omits
            // (empties) the requestBody.
            oasRequestId = new InsertOasRequestArguments(command)
                    .setUserId(userId)
                    .setOasOperationId(oasOperationId)
                    .setOasMessageBodyId(oasMessageBodyId)
                    .setDescription(request.getDescription())
                    .setMakeArrayIndicator(false)
                    // Issue #1730: Suppress Root defaults to checked for added (BIE-less) operations.
                    .setSuppressRootIndicator(true)
                    .setIncludePaginationIndicator(false)
                    .setIncludeMetaHeaderIndicator(false)
                    .setRequired(false)
                    .setTimestamp(millis)
                    .execute();
        } else {
            // Response-type message body: carries the HTTP status code (e.g. 202/204), no content schema.
            String httpStatusCode = (request.getHttpStatusCode() != null)
                    ? String.valueOf(request.getHttpStatusCode()) : null;
            oasResponseId = new InsertOasResponseArguments(command)
                    .setUserId(userId)
                    .setOasOperationId(oasOperationId)
                    .setOasMessageBodyId(oasMessageBodyId)
                    .setDescription(request.getDescription())
                    .setHttpStatusCode(httpStatusCode)
                    .setMakeArrayIndicator(false)
                    // Issue #1730: Suppress Root defaults to checked for added (BIE-less) operations.
                    .setSuppressRootIndicator(true)
                    .setIncludePaginationIndicator(false)
                    .setIncludeMetaHeaderIndicator(false)
                    .setTimestamp(millis)
                    .execute();
        }

        return new AddBieForOasDocResponse(oasRequestId, oasResponseId);
    }

    private void validateOasDocRequest(String openApiVersion,
                                       String termsOfService,
                                       String version,
                                       String contactUrl,
                                       String licenseName,
                                       String licenseUrl) {
        validateMaxLength("openAPIVersion", openApiVersion, OPEN_API_VERSION_MAX_LENGTH);
        validateMaxLength("termsOfService", termsOfService, TERMS_OF_SERVICE_MAX_LENGTH);
        validateMaxLength("version", version, VERSION_MAX_LENGTH);
        validateMaxLength("contactUrl", contactUrl, CONTACT_URL_MAX_LENGTH);
        validateMaxLength("licenseName", licenseName, LICENSE_NAME_MAX_LENGTH);
        validateMaxLength("licenseUrl", licenseUrl, LICENSE_URL_MAX_LENGTH);
    }

    /**
     * Issue #1729: validate the list of OpenAPI 3.0.3 Security Schemes. An empty/null list keeps the
     * legacy default OAuth2 scheme. Each scheme needs a unique, non-blank Scheme Name (the
     * components.securitySchemes map key) and valid type-specific fields.
     */
    private void validateSecuritySchemes(List<OasSecurityScheme> schemes) {
        if (schemes == null || schemes.isEmpty()) {
            return;
        }
        Set<String> explicitNames = new HashSet<>();
        for (OasSecurityScheme scheme : schemes) {
            if (scheme == null) {
                continue;
            }
            String name = scheme.getSchemeName();
            // Scheme Name is optional; a blank falls back to a unique type-derived default at persist time.
            if (!isBlank(name)) {
                validateMaxLength("schemeName", name, SCHEME_NAME_MAX_LENGTH);
                if (!explicitNames.add(name.trim())) {
                    throw new IllegalArgumentException("Duplicate security scheme name: " + name.trim());
                }
            }
            validateSecurityScheme(scheme);
        }
    }

    /**
     * Validate a single Security Scheme's type-specific fields per OpenAPI 3.0.3:
     * <ul>
     *   <li>apiKey -&gt; {@code in} (query|header|cookie) and {@code name} are required</li>
     *   <li>http -&gt; {@code scheme} (bearer|basic) is required; bearer also requires
     *       {@code bearerFormat}; basic suppresses {@code bearerFormat}</li>
     *   <li>oauth2 -&gt; no extra required fields (uses the example.com authorizationCode flow)</li>
     *   <li>openIdConnect -&gt; {@code openIdConnectUrl} is required</li>
     * </ul>
     */
    private void validateSecurityScheme(OasSecurityScheme scheme) {
        String type = scheme.getType();
        if (isBlank(type)) {
            throw new IllegalArgumentException("Security scheme `type` is required.");
        }
        switch (type) {
            case "oauth2":
                validateOAuthFlows(scheme.getFlows());
                break;
            case "apiKey":
                if (isBlank(scheme.getApiKeyIn())) {
                    throw new IllegalArgumentException("`in` is required for an apiKey security scheme.");
                }
                if (!List.of("query", "header", "cookie").contains(scheme.getApiKeyIn())) {
                    throw new IllegalArgumentException("`in` must be one of query, header, cookie.");
                }
                if (isBlank(scheme.getApiKeyName())) {
                    throw new IllegalArgumentException("`name` is required for an apiKey security scheme.");
                }
                validateMaxLength("name", scheme.getApiKeyName(), API_KEY_NAME_MAX_LENGTH);
                break;
            case "http":
                if (isBlank(scheme.getHttpScheme())) {
                    throw new IllegalArgumentException("`scheme` is required for an http security scheme.");
                }
                if ("bearer".equalsIgnoreCase(scheme.getHttpScheme())) {
                    // bearerFormat is an optional hint for the bearer scheme.
                    if (!isBlank(scheme.getBearerFormat())) {
                        validateMaxLength("bearerFormat", scheme.getBearerFormat(), BEARER_FORMAT_MAX_LENGTH);
                    }
                } else if ("basic".equalsIgnoreCase(scheme.getHttpScheme())) {
                    // bearerFormat is not applicable to the basic scheme.
                    scheme.setBearerFormat(null);
                } else {
                    throw new IllegalArgumentException("`scheme` must be one of bearer, basic.");
                }
                break;
            case "openIdConnect":
                if (isBlank(scheme.getOpenIdConnectUrl())) {
                    throw new IllegalArgumentException("`openIdConnectUrl` is required for an openIdConnect security scheme.");
                }
                validateMaxLength("openIdConnectUrl", scheme.getOpenIdConnectUrl(), OPEN_ID_CONNECT_URL_MAX_LENGTH);
                break;
            default:
                throw new IllegalArgumentException("Unsupported security scheme type: " + type);
        }
    }

    private static final List<String> OAUTH_FLOW_TYPES =
            List.of("implicit", "password", "clientCredentials", "authorizationCode", "deviceAuthorization");

    /**
     * Issue #1729: validate an oauth2 scheme's OAuth Flows Object. At least one flow is REQUIRED (the
     * generator no longer fabricates a default, so a flowless oauth2 scheme would emit a useless
     * `flows: {}`). Each flow needs a valid, unique type and the URLs that its type requires per
     * OpenAPI 3.0.3 (deviceAuthorization is 3.2 and is gated out of the generated output).
     */
    private void validateOAuthFlows(List<OasOAuthFlow> flows) {
        if (flows == null || flows.isEmpty()) {
            throw new IllegalArgumentException("An oauth2 security scheme requires at least one OAuth flow.");
        }
        Set<String> flowTypes = new HashSet<>();
        for (OasOAuthFlow flow : flows) {
            if (flow == null) {
                continue;
            }
            String ft = flow.getFlowType();
            if (isBlank(ft) || !OAUTH_FLOW_TYPES.contains(ft)) {
                throw new IllegalArgumentException("Invalid OAuth flow type: " + ft);
            }
            if (!flowTypes.add(ft)) {
                throw new IllegalArgumentException("Duplicate OAuth flow type: " + ft);
            }
            boolean needsAuthUrl = "implicit".equals(ft) || "authorizationCode".equals(ft);
            boolean needsTokenUrl = "password".equals(ft) || "clientCredentials".equals(ft)
                    || "authorizationCode".equals(ft) || "deviceAuthorization".equals(ft);
            if (needsAuthUrl && isBlank(flow.getAuthorizationUrl())) {
                throw new IllegalArgumentException("`authorizationUrl` is required for the " + ft + " flow.");
            }
            if (needsTokenUrl && isBlank(flow.getTokenUrl())) {
                throw new IllegalArgumentException("`tokenUrl` is required for the " + ft + " flow.");
            }
            if ("deviceAuthorization".equals(ft) && isBlank(flow.getDeviceAuthorizationUrl())) {
                throw new IllegalArgumentException("`deviceAuthorizationUrl` is required for the deviceAuthorization flow.");
            }
        }
    }

    private void validateSecurityRequirements(List<OasSecurityScheme> schemes,
                                              List<OasSecurityRequirement> requirements) {
        if (requirements == null || requirements.isEmpty()) {
            return;
        }
        // Key by the SAME resolved name the persistence/generator emit (trimmed schemeName, else a
        // type-derived default) so a requirement that references a derived name is not falsely rejected.
        Map<String, OasSecurityScheme> schemeMap = (schemes == null ? List.<OasSecurityScheme>of() : schemes).stream()
                .filter(scheme -> scheme != null && !isBlank(scheme.getType()))
                .collect(Collectors.toMap(this::resolvedSchemeName, scheme -> scheme, (a, b) -> a));
        for (OasSecurityRequirement requirement : requirements) {
            if (requirement == null || requirement.isAnonymous()) {
                continue;
            }
            if (requirement.getSchemes() == null || requirement.getSchemes().isEmpty()) {
                continue;
            }
            for (OasSecurityRequirementScheme requirementScheme : requirement.getSchemes()) {
                if (requirementScheme == null || isBlank(requirementScheme.getSchemeName())) {
                    continue;
                }
                OasSecurityScheme scheme = schemeMap.get(requirementScheme.getSchemeName().trim());
                if (scheme == null) {
                    throw new IllegalArgumentException("Unknown security scheme: " + requirementScheme.getSchemeName());
                }
                if (!"oauth2".equals(scheme.getType()) && !"openIdConnect".equals(scheme.getType())
                        && requirementScheme.getScopes() != null && !requirementScheme.getScopes().isEmpty()) {
                    throw new IllegalArgumentException("Scopes are only allowed for oauth2 or openIdConnect security schemes.");
                }
                if ("oauth2".equals(scheme.getType())) {
                    Set<String> declaredScopes = collectOAuthScopeNames(scheme);
                    if (requirementScheme.getScopes() != null) {
                        for (String scope : requirementScheme.getScopes()) {
                            if (!isBlank(scope) && !declaredScopes.contains(scope.trim())) {
                                throw new IllegalArgumentException("Unknown OAuth2 scope: " + scope);
                            }
                        }
                    }
                }
            }
        }
    }

    // Mirror resolveSchemeNameFor (generator) / typeDefaultName (persistence): the components.securitySchemes
    // key is the trimmed schemeName when present, otherwise a type-derived default.
    private String resolvedSchemeName(OasSecurityScheme scheme) {
        if (!isBlank(scheme.getSchemeName())) {
            return scheme.getSchemeName().trim();
        }
        String type = scheme.getType();
        if ("apiKey".equalsIgnoreCase(type)) {
            return "ApiKeyAuth";
        }
        if ("http".equalsIgnoreCase(type)) {
            return "basic".equalsIgnoreCase(scheme.getHttpScheme()) ? "BasicAuth" : "BearerAuth";
        }
        if ("openIdConnect".equalsIgnoreCase(type)) {
            return "OpenID";
        }
        return "OAuth2";
    }

    private Set<String> collectOAuthScopeNames(OasSecurityScheme scheme) {
        Set<String> scopeNames = new HashSet<>();
        if (scheme.getFlows() == null) {
            return scopeNames;
        }
        for (OasOAuthFlow flow : scheme.getFlows()) {
            if (flow == null || flow.getScopes() == null) {
                continue;
            }
            for (OasOAuthScope scope : flow.getScopes()) {
                if (scope != null && !isBlank(scope.getScopeName())) {
                    scopeNames.add(scope.getScopeName().trim());
                }
            }
        }
        return scopeNames;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void validateMaxLength(String fieldName, String value, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(
                    String.format("`%s` must not exceed %d characters.", fieldName, maxLength));
        }
    }

    @Transactional
    public DeleteBieForOasDocResponse deleteBieForOasDoc(ScoreUser requester, DeleteBieForOasDocRequest request) {
        DeleteBieForOasDocResponse response =
                bieForOasDocCommand(requester).deleteBieForOasDoc(request);
        return response;

    }

    public boolean checkOasDocUniqueness(ScoreUser requester, OasDoc oasDoc) {
        var query = repositoryFactory.oasDocQueryRepository(requester);
        return query.checkOasDocUniqueness(oasDoc);
    }

    public boolean checkOasDocTitleUniqueness(ScoreUser requester, OasDoc oasDoc) {
        var query = repositoryFactory.oasDocQueryRepository(requester);
        return query.checkOasDocTitleUniqueness(oasDoc);
    }

    @Transactional
    public UpdateBieForOasDocResponse updateDetails(ScoreUser requester, UpdateBieForOasDocRequest request) {

        // Issue #1729: per-operation security overrides are persisted through this path (not the OasDoc
        // create/update path), so validate them against the document's declared schemes the same way the
        // document-level requirements are validated (scheme must exist; scope rules per scheme type).
        if (request.getBieForOasDocList() != null) {
            boolean hasOverride = request.getBieForOasDocList().stream()
                    .anyMatch(b -> b != null && b.isSecurityOverridden()
                            && b.getSecurityRequirements() != null && !b.getSecurityRequirements().isEmpty());
            if (hasOverride) {
                OasDoc oasDoc = query(requester).getOasDoc(
                        new GetOasDocRequest(requester).withOasDocId(request.getOasDocId())).getOasDoc();
                List<OasSecurityScheme> schemes = (oasDoc == null) ? null : oasDoc.getSecuritySchemes();
                for (BieForOasDoc bieForOasDoc : request.getBieForOasDocList()) {
                    if (bieForOasDoc != null && bieForOasDoc.isSecurityOverridden()) {
                        validateSecurityRequirements(schemes, bieForOasDoc.getSecurityRequirements());
                    }
                }
            }
        }

        UpdateBieForOasDocResponse response =
                bieForOasDocCommand(requester).updateBieForOasDoc(request);
        return response;
    }

    @Transactional
    public GetOasOperationResponse getOasOperation(ScoreUser requester, GetOasOperationRequest request) {
        GetOasOperationResponse response = query(requester).getOasOperation(request);
        return response;
    }

    @Transactional
    public GetOasRequestTableResponse getOasRequestTable(ScoreUser requester, GetOasRequestTableRequest request) {
        GetOasRequestTableResponse response = query(requester).getOasRequestTable(request);
        return response;
    }

    @Transactional
    public GetOasResponseTableResponse getOasResponseTable(ScoreUser requester, GetOasResponseTableRequest request) {
        GetOasResponseTableResponse response = query(requester).getOasResponseTable(request);
        return response;
    }

    @Transactional
    public GetAssignedOasTagResponse getAssignedOasTag(ScoreUser requester, GetAssignedOasTagRequest request) {
        GetAssignedOasTagResponse response = bieForOasDocQuery(requester).getAssignedOasTag(request);
        return response;
    }
}
