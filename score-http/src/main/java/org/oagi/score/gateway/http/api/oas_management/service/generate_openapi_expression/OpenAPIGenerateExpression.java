package org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.collect.ImmutableMap;
import org.oagi.score.gateway.http.api.bie_management.model.BIE;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;

import java.math.BigInteger;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.BieOpenApiDialect;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.BieOpenApiSchemaBuilder;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.GenerationContext;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.oas_management.model.OasOAuthFlow;
import org.oagi.score.gateway.http.api.oas_management.model.OasOAuthScope;
import org.oagi.score.gateway.http.api.oas_management.model.OasSecurityRequirement;
import org.oagi.score.gateway.http.api.oas_management.model.OasSecurityRequirementScheme;
import org.oagi.score.gateway.http.api.oas_management.model.OasSecurityScheme;
import org.oagi.score.gateway.http.api.oas_management.model.OpenAPIExpressionFormat;
import org.oagi.score.gateway.http.api.oas_management.model.OpenAPIGenerateExpressionOption;
import org.oagi.score.gateway.http.api.oas_management.model.OpenAPITemplateForVerbOption;
import org.oagi.score.gateway.http.api.oas_management.model.Operation;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseDetailsRecord;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.oagi.score.gateway.http.api.bie_management.service.generate_expression.Helper.*;
import static org.oagi.score.gateway.http.api.oas_management.model.Operation.*;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;


/**
 * Generates an OpenAPI Document. What differs between the OpenAPI versions (the version, how a schema is expressed,
 * whether a DELETE operation can have a request body) is decided by the {@link BieOpenApiDialect}.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class OpenAPIGenerateExpression implements BieGenerateOpenApiExpression, InitializingBean {

    static final String VERSION_PATH_PARAMETER = "{version}";
    private static final Pattern PATH_PARAMETER_PATTERN = Pattern.compile("\\{([^{}]+)}");

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final BieOpenApiDialect dialect;
    private GenerationContext generationContext;
    private OpenAPIGenerateExpressionOption option;

    private Map<Operation, GeneratorForOperation> generatorForOperationMap = new HashMap<>();
    private BieOpenApiSchemaBuilder schemaBuilder;
    private ObjectMapper expressionMapper;

    private Map<String, Object> root;
    private Map<String, Object> schemas = new LinkedHashMap<>();
    private Map<TopLevelAsbiepId, String> reusedTopLevelAsbiepNameMap;

    public OpenAPIGenerateExpression(GenerationContext generationContext, OpenAPIGenerateExpressionOption option,
                                     BieOpenApiDialect dialect) {
        this.dialect = dialect;
        this.generationContext = generationContext;
        this.option = option;

        generatorForOperationMap.put(GET, new GetGeneratorForOperation());
        generatorForOperationMap.put(POST, new PostGeneratorForOperation());
        generatorForOperationMap.put(PATCH, new PatchGeneratorForOperation());
        generatorForOperationMap.put(PUT, new PutGeneratorForOperation());
        generatorForOperationMap.put(DELETE, new DeleteGeneratorForOperation());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        OpenAPIExpressionFormat openAPIExpressionFormat = option.getOpenAPIExpressionFormat();
        switch (openAPIExpressionFormat) {
            case YAML:
                expressionMapper = YAMLMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build();
                break;
            case JSON:
                expressionMapper = JsonMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build();
                break;
        }

        root = null;
        schemas = new LinkedHashMap<>();
        reusedTopLevelAsbiepNameMap = new LinkedHashMap<>();
        schemaBuilder = new BieOpenApiSchemaBuilder(generationContext, dialect, new OasDocumentSchemaPolicy(),
                option.isBieDefinition(), option.getScheme(), option.getHost(), schemas, reusedTopLevelAsbiepNameMap);
    }

    @Override
    public void reset() throws Exception {
        this.afterPropertiesSet();
    }

    @Override
    public Map<String, Object> getSchemas() {
        return schemas;
    }

    @Override
    public void generate(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        generateTopLevelAsbiep(topLevelAsbiep);
    }

    @Override
    public void generate(OpenAPITemplateForVerbOption template) {
        generateTemplate(template);
    }

    @Override
    public void generateErrorResponses() {
        // Issue #1347: merge the defaulted 4xx/5xx error responses into every generated operation.
        // 415/422 go onto DELETE only if it can carry a request body (3.1+ — #1610).
        List<ErrorResponseSchemas.OperationErrorSpec> specs = ErrorResponseSchemas.buildSpecs(
                option.getTemplates(), this::getResolvedResourceName, this::resolveConfirmSchemaName);
        ErrorResponseSchemas.injectErrorResponses(root, schemas, specs, dialect.allowsDeleteRequestBody());
    }

    // Issue #1347: the components.schemas name of a ConfirmMessage BIE materialized via generate(TLA).
    // resolveReusedSchemaName caches it in reusedTopLevelAsbiepNameMap keyed by the (owner) top-level id.
    private String resolveConfirmSchemaName(BigInteger topLevelAsbiepId) {
        if (topLevelAsbiepId == null || reusedTopLevelAsbiepNameMap == null) {
            return null;
        }
        return reusedTopLevelAsbiepNameMap.get(new TopLevelAsbiepId(topLevelAsbiepId));
    }

    private boolean isFriendly() {
        return true;
    }

    private Map<String, Object> getAuthorizationCodeScopes(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        AsbiepSummaryRecord asbiep = generationContext.findASBIEP(topLevelAsbiep.asbiepId(), topLevelAsbiep);
        AsccpSummaryRecord basedAsccp = generationContext.getAsccp(asbiep.basedAsccpManifestId());
        String bieName = getBieName(topLevelAsbiep);
        Map<String, Object> scopes = new LinkedHashMap<>();
        scopes.put(bieName + "Read", "Allows " + basedAsccp.propertyTerm() + " data to be read");
        scopes.put(bieName + "Write", "Allows " + basedAsccp.propertyTerm() + " data to be written");
        return scopes;
    }

    // Issue #1729: configurable Security Schemes (components.securitySchemes). An empty/absent list keeps
    // the legacy default OAuth2 scheme (per-operation Read/Write scopes). A non-empty list emits the
    // configured schemes and only the selected root/operation Security Requirement Objects.
    // Backend targets OpenAPI 3.0.3 and 3.1.x (apiKey | http | oauth2 | openIdConnect).
    private List<OasSecurityScheme> securitySchemes() {
        if (option == null || option.getOasDoc() == null || option.getOasDoc().getSecuritySchemes() == null) {
            return Collections.emptyList();
        }
        return option.getOasDoc().getSecuritySchemes();
    }

    private boolean isLegacyDefault() {
        return securitySchemes().isEmpty();
    }

    // The deviceAuthorization OAuth flow is an OpenAPI 3.2+ feature; true only when the document's
    // declared openapi version is >= 3.2.
    private boolean supportsDeviceAuthorization() {
        String version = (option == null || option.getOasDoc() == null) ? null : option.getOasDoc().getOpenAPIVersion();
        if (!StringUtils.hasLength(version)) {
            return false;
        }
        String[] parts = version.split("\\.");
        try {
            int major = Integer.parseInt(parts[0].trim());
            int minor = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;
            return major > 3 || (major == 3 && minor >= 2);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // The document-level (global) security requirements — the root `security` array.
    private List<OasSecurityRequirement> globalSecurityRequirements() {
        if (option == null || option.getOasDoc() == null || option.getOasDoc().getSecurityRequirements() == null) {
            return Collections.emptyList();
        }
        return option.getOasDoc().getSecurityRequirements();
    }

    private String resolveSchemeNameFor(OasSecurityScheme scheme) {
        if (StringUtils.hasLength(scheme.getSchemeName())) {
            return scheme.getSchemeName();
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

    // The legacy default OAuth2 component scheme with per-BIE Read/Write scopes (Issue #1730: a bodyless
    // operation contributes no scopes). Used only when no explicit schemes are configured.
    private Map<String, Object> buildLegacyOauth2Scheme(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        return ImmutableMap.<String, Object>builder()
                .put("OAuth2", ImmutableMap.<String, Object>builder()
                        .put("type", "oauth2")
                        .put("flows", ImmutableMap.<String, Object>builder()
                                .put("authorizationCode", ImmutableMap.<String, Object>builder()
                                        .put("authorizationUrl", "https://example.com/oauth/authorize")
                                        .put("tokenUrl", "https://example.com/oauth/token")
                                        .put("scopes", (topLevelAsbiep == null)
                                                ? new LinkedHashMap<String, Object>()
                                                : getAuthorizationCodeScopes(topLevelAsbiep))
                                        .build())
                                .build())
                        .build())
                .build();
    }

    // Builds the components.securitySchemes map for the explicitly configured schemes (insertion order).
    private Map<String, Object> buildSecuritySchemes() {
        Map<String, Object> result = new LinkedHashMap<>();
        for (OasSecurityScheme scheme : securitySchemes()) {
            if (scheme == null || !StringUtils.hasLength(scheme.getType())) {
                continue;
            }
            result.put(resolveSchemeNameFor(scheme), buildSchemeObject(scheme));
        }
        return result;
    }

    private Map<String, Object> buildSchemeObject(OasSecurityScheme scheme) {
        String type = scheme.getType();
        ImmutableMap.Builder<String, Object> b = ImmutableMap.<String, Object>builder();
        if ("apiKey".equalsIgnoreCase(type)) {
            b.put("type", "apiKey");
            if (StringUtils.hasLength(scheme.getApiKeyIn())) {
                b.put("in", scheme.getApiKeyIn());
            }
            if (StringUtils.hasLength(scheme.getApiKeyName())) {
                b.put("name", scheme.getApiKeyName());
            }
        } else if ("http".equalsIgnoreCase(type)) {
            b.put("type", "http");
            if (StringUtils.hasLength(scheme.getHttpScheme())) {
                b.put("scheme", scheme.getHttpScheme());
            }
            // bearerFormat is only meaningful for the bearer scheme.
            if ("bearer".equalsIgnoreCase(scheme.getHttpScheme()) && StringUtils.hasLength(scheme.getBearerFormat())) {
                b.put("bearerFormat", scheme.getBearerFormat());
            }
        } else if ("openIdConnect".equalsIgnoreCase(type)) {
            b.put("type", "openIdConnect");
            if (StringUtils.hasLength(scheme.getOpenIdConnectUrl())) {
                b.put("openIdConnectUrl", scheme.getOpenIdConnectUrl());
            }
        } else {
            // oauth2 -- emit the configured OAuth Flows Object (or a default authorizationCode flow).
            b.put("type", "oauth2");
            b.put("flows", buildOAuthFlows(scheme));
        }
        if (StringUtils.hasLength(scheme.getDescription())) {
            b.put("description", scheme.getDescription());
        }
        return b.build();
    }

    // Builds the OAuth Flows Object for an oauth2 scheme from its CONFIGURED flows (insertion order).
    // It emits exactly what the user configured — there is NO fabricated fallback, so an oauth2 scheme
    // with no flows yields an empty flows object. Per OpenAPI 3.x each OAuth Flow Object's scopes key
    // is REQUIRED (may be empty), so a scopes map is always emitted per flow. The deviceAuthorization
    // flow (and its deviceAuthorizationUrl) is an OpenAPI 3.2+ feature, skipped for older documents.
    private Map<String, Object> buildOAuthFlows(OasSecurityScheme scheme) {
        boolean deviceAuthSupported = supportsDeviceAuthorization();
        Map<String, Object> result = new LinkedHashMap<>();
        List<OasOAuthFlow> flows = scheme.getFlows();
        if (flows == null) {
            return result;
        }
        for (OasOAuthFlow flow : flows) {
            if (flow == null || !StringUtils.hasLength(flow.getFlowType())) {
                continue;
            }
            if ("deviceAuthorization".equalsIgnoreCase(flow.getFlowType()) && !deviceAuthSupported) {
                continue;
            }
            ImmutableMap.Builder<String, Object> fb = ImmutableMap.<String, Object>builder();
            if (StringUtils.hasLength(flow.getAuthorizationUrl())) {
                fb.put("authorizationUrl", flow.getAuthorizationUrl());
            }
            if (StringUtils.hasLength(flow.getTokenUrl())) {
                fb.put("tokenUrl", flow.getTokenUrl());
            }
            if (deviceAuthSupported && StringUtils.hasLength(flow.getDeviceAuthorizationUrl())) {
                fb.put("deviceAuthorizationUrl", flow.getDeviceAuthorizationUrl());
            }
            if (StringUtils.hasLength(flow.getRefreshUrl())) {
                fb.put("refreshUrl", flow.getRefreshUrl());
            }
            Map<String, Object> scopes = new LinkedHashMap<>();
            if (flow.getScopes() != null) {
                for (OasOAuthScope scope : flow.getScopes()) {
                    if (scope != null && StringUtils.hasLength(scope.getScopeName())) {
                        scopes.put(scope.getScopeName(),
                                scope.getDescription() != null ? scope.getDescription() : "");
                    }
                }
            }
            fb.put("scopes", scopes);
            result.put(flow.getFlowType(), fb.build());
        }
        return result;
    }

    private List<Object> buildSecurityRequirements(List<OasSecurityRequirement> source) {
        List<Object> requirements = new ArrayList<>();
        if (source == null) {
            return requirements;
        }
        // Resolve declared schemes by their emitted components.securitySchemes key so a Security
        // Requirement Object only references declared schemes and uses the correct scope-array shape:
        // oauth2/openIdConnect carry the scope names; all other types MUST be an empty array (3.x).
        Map<String, OasSecurityScheme> declaredSchemes = new LinkedHashMap<>();
        for (OasSecurityScheme scheme : securitySchemes()) {
            if (scheme != null && StringUtils.hasLength(scheme.getType())) {
                declaredSchemes.put(resolveSchemeNameFor(scheme), scheme);
            }
        }
        for (OasSecurityRequirement requirement : source) {
            if (requirement == null) {
                continue;
            }
            if (requirement.isAnonymous()) {
                requirements.add(new LinkedHashMap<String, Object>());
                continue;
            }
            Map<String, Object> securityRequirement = new LinkedHashMap<>();
            if (requirement.getSchemes() != null) {
                for (OasSecurityRequirementScheme scheme : requirement.getSchemes()) {
                    if (scheme == null || !StringUtils.hasLength(scheme.getSchemeName())) {
                        continue;
                    }
                    OasSecurityScheme declared = declaredSchemes.get(scheme.getSchemeName());
                    if (declared == null) {
                        // Skip a requirement that references a scheme not present in components
                        // (e.g. a stale operation override after the scheme was renamed/removed).
                        continue;
                    }
                    boolean scoped = "oauth2".equalsIgnoreCase(declared.getType())
                            || "openIdConnect".equalsIgnoreCase(declared.getType());
                    securityRequirement.put(scheme.getSchemeName(),
                            (scoped && scheme.getScopes() != null) ? scheme.getScopes() : Collections.emptyList());
                }
            }
            if (!securityRequirement.isEmpty()) {
                requirements.add(securityRequirement);
            }
        }
        return requirements;
    }

    // Legacy documents keep the previous per-operation OAuth2 Read/Write scope behavior.
    private void putOperationSecurity(Map<String, Object> path, String scope) {
        if (isLegacyDefault()) {
            path.put("security", Arrays.asList(ImmutableMap.builder()
                    .put("OAuth2", Arrays.asList(scope))
                    .build()));
        }
    }

    private void putConfiguredOperationSecurity(OpenAPITemplateForVerbOption template, Map<String, Object> path) {
        if (isLegacyDefault() || template == null || !template.isSecurityOverridden()) {
            return;
        }
        path.put("security", buildSecurityRequirements(template.getSecurityRequirements()));
    }

    private String getBieName(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        return getBieName(topLevelAsbiep, s -> convertIdentifierToId(camelCase(s)));
    }

    private String getBieName(TopLevelAsbiepSummaryRecord topLevelAsbiep, Function<String, String> replacer) {
        AsbiepSummaryRecord asbiep = generationContext.findASBIEP(topLevelAsbiep.asbiepId(), topLevelAsbiep);
        AsccpSummaryRecord basedAsccp = generationContext.getAsccp(asbiep.basedAsccpManifestId());
        return replacer.apply(basedAsccp.propertyTerm());
    }

    private List<Map<String, Object>> buildParameters(Operation verb, boolean isArray, List<String> pathParameterNames) {
        List<Map<String, Object>> parameters = new ArrayList<>();

        if (verb == GET && isArray) {
            parameters.add(ImmutableMap.<String, Object>builder()
                    .put("name", "sinceLastDateTime")
                    .put("in", "query")
                    .put("description", "Returns resources that have been updated since the last time the endpoint has been called")
                    .put("required", false)
                    .put("schema", ImmutableMap.<String, Object>builder()
                            .put("type", "string")
                            .put("format", "date-time")
                            .build())
                    .build());
        }

        // Issue #1710: Generate one required path parameter for every token found in the resource path.
        for (String pathParameterName : pathParameterNames) {
            parameters.add(ImmutableMap.<String, Object>builder()
                    .put("name", pathParameterName)
                    .put("in", "path")
                    .put("description", "")
                    .put("required", true)
                    .put("schema", ImmutableMap.<String, Object>builder()
                            .put("type", "string")
                            .build())
                    .build());
        }

        return parameters;
    }

    private List<String> extractPathParameterNames(String resourceName) {
        if (!StringUtils.hasLength(resourceName)) {
            return Collections.emptyList();
        }

        // Issue #1710: Parse only valid `{...}` placeholders and ignore malformed fragments.
        Set<String> pathParameterNames = new LinkedHashSet<>();
        Matcher matcher = PATH_PARAMETER_PATTERN.matcher(resourceName);
        while (matcher.find()) {
            String candidate = matcher.group(1).trim();
            if (StringUtils.hasLength(candidate)) {
                pathParameterNames.add(candidate);
            }
        }

        return new ArrayList<>(pathParameterNames);
    }

    static String resolveResourceName(String resourceName, String documentVersion) {
        if (!StringUtils.hasLength(resourceName) || !StringUtils.hasLength(documentVersion)) {
            return resourceName;
        }
        return resourceName.replace(VERSION_PATH_PARAMETER, documentVersion);
    }

    private String getResolvedResourceName(OpenAPITemplateForVerbOption template) {
        if (template == null) {
            return null;
        }
        String documentVersion = option != null && option.getOasDoc() != null ? option.getOasDoc().getVersion() : null;
        return resolveResourceName(template.getResourceName(), documentVersion);
    }

    /**
     * Issue #1710:
     * Ensures OpenAPI parameters are aligned with the current resource name by generating all path parameters
     * declared as `{...}` placeholders, while omitting the `parameters` field when none are applicable.
     */
    private void ensurePathParameters(Map<String, Object> path,
                                      Operation operation,
                                      boolean isArray,
                                      OpenAPITemplateForVerbOption template) {
        List<String> pathParameterNames = extractPathParameterNames(getResolvedResourceName(template));
        List<Map<String, Object>> parameters = buildParameters(operation, isArray, pathParameterNames);
        if (parameters.isEmpty()) {
            // Issue #1710: Omit `parameters` when nothing is generated.
            path.remove("parameters");
            return;
        }

        Object existingParameters = path.get("parameters");
        if (existingParameters instanceof List && !((List<?>) existingParameters).isEmpty()) {
            return;
        }
        path.put("parameters", parameters);
    }

    private void fillPropertiesForTemplate(String schemaName,
                                           AsbiepSummaryRecord asbiep,
                                           boolean isArray, boolean isSuppressRoot) {
        if (schemas.containsKey(schemaName)) {
            return;
        }

        if (isArray) {
            String itemRefSchemaName;
            // Issue #1603
            // If the schema meaning ends with 'List' without any additional options,
            // it means that there is only one BIE with the corresponding Property Term.
            // In this case, 'Entry' is not appended at the end.
            if (schemaName.endsWith("List")) {
                itemRefSchemaName = schemaName.substring(0, schemaName.indexOf("List"));
                if (schemas.containsKey(itemRefSchemaName)) {
                    Map<String, Object> properties = (Map<String, Object>) schemas.get(itemRefSchemaName);
                    // If it's duplicated
                    if (!asbiep.getGuid().equals(properties.get("x-oagis-bie-guid"))) {
                        itemRefSchemaName = schemaName + "Entry";
                    }
                }
            } else {
                itemRefSchemaName = schemaName + "Entry";
            }
            schemas.put(schemaName, ImmutableMap.<String, Object>builder()
                    .put("type", "array")
                    .put("items", ImmutableMap.<String, Object>builder()
                            .put("$ref", "#/components/schemas/" + itemRefSchemaName)
                            .build())
                    .build());

            fillPropertiesForTemplate(itemRefSchemaName, asbiep, false, isSuppressRoot);
        } else {
            schemaBuilder.reference(asbiep, schemaName, isSuppressRoot);
        }
    }

    private interface GeneratorForOperation {
        Operation getOperation();

        void proceed(TopLevelAsbiepSummaryRecord topLevelAsbiep,
                     OpenAPITemplateForVerbOption template,
                     Map<String, Object> path,
                     AsbiepSummaryRecord asbiep);
    }

    private class GetGeneratorForOperation implements GeneratorForOperation {
        @Override
        public Operation getOperation() {
            return GET;
        }

        @Override
        public void proceed(TopLevelAsbiepSummaryRecord topLevelAsbiep,
                            OpenAPITemplateForVerbOption template,
                            Map<String, Object> path,
                            AsbiepSummaryRecord asbiep) {
            if (path != null && path.size() > 0) {
                ensurePathParameters(path, GET, template.isArrayForJsonExpression(), template);
            } else {
                boolean isArray = template.isArrayForJsonExpression();
                String schemaName = template.getSchemaName();
                boolean isSuppressRoot = template.isSuppressRootProperty();
                String bieName = getBieName(topLevelAsbiep);

                path.put("summary", "");
                path.put("description", "");
                putOperationSecurity(path, bieName + "Read");
                // Issue #1729: place the configured per-operation `security` right after `description`
                // (same position as the legacy default), instead of appending it after the body/responses.
                putConfiguredOperationSecurity(template, path);
                if (StringUtils.hasLength(template.getTagName())) {
                    path.put("tags", Arrays.asList(template.getTagName()));
                }
                path.put("operationId", template.getOperationId());
                ensurePathParameters(path, GET, isArray, template);
                if (template.getMessageBodyType().equals("Response")) {
                    path.put("responses", ImmutableMap.<String, Object>builder()
                            .put("200", ImmutableMap.<String, Object>builder()
                                    .put("description", "")
                                    .put("content", ImmutableMap.<String, Object>builder()
                                            .put("application/json", ImmutableMap.<String, Object>builder()
                                                    .put("schema", ImmutableMap.<String, Object>builder()
                                                            .put("$ref", "#/components/schemas/" + schemaName)
                                                            .build())
                                                    .build())
                                            .build())
                                    .build())
                            .build());
                }
                if (!isFriendly() && !schemas.containsKey("integer")) {
                    schemas.put("integer", ImmutableMap.<String, Object>builder()
                            .put("type", "integer")
                            .build());
                }

                fillPropertiesForTemplate(schemaName, asbiep, isArray, isSuppressRoot);
            }
        }
    }

    private class PostGeneratorForOperation implements GeneratorForOperation {
        @Override
        public Operation getOperation() {
            return POST;
        }

        @Override
        public void proceed(TopLevelAsbiepSummaryRecord topLevelAsbiep,
                            OpenAPITemplateForVerbOption template,
                            Map<String, Object> path,
                            AsbiepSummaryRecord asbiep) {
            if (path != null && path.size() > 0) {
                boolean isArray = template.isArrayForJsonExpression();
                String schemaName = template.getSchemaName();
                boolean isSuppressRoot = template.isSuppressRootProperty();
                ensurePathParameters(path, getOperation(), isArray, template);
                if (StringUtils.hasLength(template.getTagName()) && !path.containsKey("tags")) {
                    path.put("tags", Arrays.asList(template.getTagName()));
                }
                if (template.getMessageBodyType().equals("Request")) {
                    path.put("requestBody", ImmutableMap.<String, Object>builder()
                            .put("description", "")
                            .put("content", ImmutableMap.<String, Object>builder()
                                    .put("application/json", ImmutableMap.<String, Object>builder()
                                            .put("schema", ImmutableMap.<String, Object>builder()
                                                    .put("$ref", "#/components/schemas/" + schemaName)
                                                    .build())
                                            .build())
                                    .build())
                            .build());
                }
                if (template.getMessageBodyType().equals("Response")) {
                    path.put("responses", ImmutableMap.<String, Object>builder()
                            .put("200", ImmutableMap.<String, Object>builder()
                                    .put("description", "")
                                    .put("content", ImmutableMap.<String, Object>builder()
                                            .put("application/json", ImmutableMap.<String, Object>builder()
                                                    .put("schema", ImmutableMap.<String, Object>builder()
                                                            .put("$ref", "#/components/schemas/" + schemaName)
                                                            .build())
                                                    .build())
                                            .build())
                                    .build())
                            .build());
                }

                fillPropertiesForTemplate(schemaName, asbiep, isArray, isSuppressRoot);
            } else {
                boolean isArray = template.isArrayForJsonExpression();
                String schemaName = template.getSchemaName();
                boolean isSuppressRoot = template.isSuppressRootProperty();
                String bieName = getBieName(topLevelAsbiep);

                path.put("summary", "");
                path.put("description", "");
                putOperationSecurity(path, bieName + "Write");
                putConfiguredOperationSecurity(template, path);
                if (StringUtils.hasLength(template.getTagName())) {
                    path.put("tags", Arrays.asList(template.getTagName()));
                }
                path.put("operationId", template.getOperationId());
                ensurePathParameters(path, getOperation(), isArray, template);
                if (template.getMessageBodyType().equals("Request")) {
                    path.put("requestBody", ImmutableMap.<String, Object>builder()
                            .put("description", "")
                            .put("content", ImmutableMap.<String, Object>builder()
                                    .put("application/json", ImmutableMap.<String, Object>builder()
                                            .put("schema", ImmutableMap.<String, Object>builder()
                                                    .put("$ref", "#/components/schemas/" + schemaName)
                                                    .build())
                                            .build())
                                    .build())
                            .build());

                    if (!path.containsKey("responses")) {
                        path.put("responses", ImmutableMap.<String, Object>builder()
                                .put("200", ImmutableMap.<String, Object>builder()
                                        .put("description", "")
                                        .put("content", ImmutableMap.<String, Object>builder()
                                                .put("application/json", ImmutableMap.<String, Object>builder()
                                                        .put("schema", ImmutableMap.<String, Object>builder()
                                                                .put("$ref", "#/components/schemas/" + schemaName)
                                                                .build())
                                                        .build())
                                                .build())
                                        .build())
                                .build());
                    }
                }
                if (template.getMessageBodyType().equals("Response")) {
                    path.put("responses", ImmutableMap.<String, Object>builder()
                            .put("200", ImmutableMap.<String, Object>builder()
                                    .put("description", "")
                                    .put("content", ImmutableMap.<String, Object>builder()
                                            .put("application/json", ImmutableMap.<String, Object>builder()
                                                    .put("schema", ImmutableMap.<String, Object>builder()
                                                            .put("$ref", "#/components/schemas/" + schemaName)
                                                            .build())
                                                    .build())
                                            .build())
                                    .build())
                            .build());
                }

                fillPropertiesForTemplate(schemaName, asbiep, isArray, isSuppressRoot);
            }
        }
    }

    private class PatchGeneratorForOperation implements GeneratorForOperation {
        @Override
        public Operation getOperation() {
            return PATCH;
        }

        @Override
        public void proceed(TopLevelAsbiepSummaryRecord topLevelAsbiep,
                            OpenAPITemplateForVerbOption template,
                            Map<String, Object> path,
                            AsbiepSummaryRecord asbiep) {
            if (path != null && path.size() > 0) {
                boolean isArray = template.isArrayForJsonExpression();
                String schemaName = template.getSchemaName();
                boolean isSuppressRoot = template.isSuppressRootProperty();
                ensurePathParameters(path, getOperation(), isArray, template);
                if (StringUtils.hasLength(template.getTagName()) && !path.containsKey("tags")) {
                    path.put("tags", Arrays.asList(template.getTagName()));
                }
                if (template.getMessageBodyType().equals("Request")) {
                    path.put("requestBody", ImmutableMap.<String, Object>builder()
                            .put("description", "")
                            .put("content", ImmutableMap.<String, Object>builder()
                                    .put("application/json", ImmutableMap.<String, Object>builder()
                                            .put("schema", ImmutableMap.<String, Object>builder()
                                                    .put("$ref", "#/components/schemas/" + schemaName)
                                                    .build())
                                            .build())
                                    .build())
                            .build());
                }
                if (template.getMessageBodyType().equals("Response")) {
                    path.put("responses", ImmutableMap.<String, Object>builder()
                            .put("200", ImmutableMap.<String, Object>builder()
                                    .put("description", "")
                                    .put("content", ImmutableMap.<String, Object>builder()
                                            .put("application/json", ImmutableMap.<String, Object>builder()
                                                    .put("schema", ImmutableMap.<String, Object>builder()
                                                            .put("$ref", "#/components/schemas/" + schemaName)
                                                            .build())
                                                    .build())
                                            .build())
                                    .build())
                            .build());
                }

                fillPropertiesForTemplate(schemaName, asbiep, isArray, isSuppressRoot);
            } else {
                boolean isArray = template.isArrayForJsonExpression();
                String schemaName = template.getSchemaName();
                boolean isSuppressRoot = template.isSuppressRootProperty();
                String bieName = getBieName(topLevelAsbiep);

                path.put("summary", "");
                path.put("description", "");
                putOperationSecurity(path, bieName + "Write");
                putConfiguredOperationSecurity(template, path);
                if (StringUtils.hasLength(template.getTagName())) {
                    path.put("tags", Arrays.asList(template.getTagName()));
                }
                path.put("operationId", template.getOperationId());
                ensurePathParameters(path, getOperation(), isArray, template);
                if (template.getMessageBodyType().equals("Request")) {
                    path.put("requestBody", ImmutableMap.<String, Object>builder()
                            .put("description", "")
                            .put("content", ImmutableMap.<String, Object>builder()
                                    .put("application/json", ImmutableMap.<String, Object>builder()
                                            .put("schema", ImmutableMap.<String, Object>builder()
                                                    .put("$ref", "#/components/schemas/" + schemaName)
                                                    .build())
                                            .build())
                                    .build())
                            .build());
                    if (!path.containsKey("responses")) {
                        path.put("responses", ImmutableMap.<String, Object>builder()
                                .put("200", ImmutableMap.<String, Object>builder()
                                        .put("description", "")
                                        .put("content", ImmutableMap.<String, Object>builder()
                                                .put("application/json", ImmutableMap.<String, Object>builder()
                                                        .put("schema", ImmutableMap.<String, Object>builder()
                                                                .put("$ref", "#/components/schemas/" + schemaName)
                                                                .build())
                                                        .build())
                                                .build())
                                        .build())
                                .build());
                    }
                }
                if (template.getMessageBodyType().equals("Response")) {
                    path.put("responses", ImmutableMap.<String, Object>builder()
                            .put("200", ImmutableMap.<String, Object>builder()
                                    .put("description", "")
                                    .put("content", ImmutableMap.<String, Object>builder()
                                            .put("application/json", ImmutableMap.<String, Object>builder()
                                                    .put("schema", ImmutableMap.<String, Object>builder()
                                                            .put("$ref", "#/components/schemas/" + schemaName)
                                                            .build())
                                                    .build())
                                            .build())
                                    .build())
                            .build());
                }

                fillPropertiesForTemplate(schemaName, asbiep, isArray, isSuppressRoot);
            }
        }
    }

    private class PutGeneratorForOperation implements GeneratorForOperation {
        @Override
        public Operation getOperation() {
            return PUT;
        }

        @Override
        public void proceed(TopLevelAsbiepSummaryRecord topLevelAsbiep,
                            OpenAPITemplateForVerbOption template,
                            Map<String, Object> path,
                            AsbiepSummaryRecord asbiep) {
            if (path != null && path.size() > 0) {
                boolean isArray = template.isArrayForJsonExpression();
                String schemaName = template.getSchemaName();
                boolean isSuppressRoot = template.isSuppressRootProperty();
                ensurePathParameters(path, getOperation(), isArray, template);
                if (StringUtils.hasLength(template.getTagName()) && !path.containsKey("tags")) {
                    path.put("tags", Arrays.asList(template.getTagName()));
                }
                if (template.getMessageBodyType().equals("Request")) {
                    path.put("requestBody", ImmutableMap.<String, Object>builder()
                            .put("description", "")
                            .put("content", ImmutableMap.<String, Object>builder()
                                    .put("application/json", ImmutableMap.<String, Object>builder()
                                            .put("schema", ImmutableMap.<String, Object>builder()
                                                    .put("$ref", "#/components/schemas/" + schemaName)
                                                    .build())
                                            .build())
                                    .build())
                            .build());
                }
                if (template.getMessageBodyType().equals("Response")) {
                    path.put("responses", ImmutableMap.<String, Object>builder()
                            .put("200", ImmutableMap.<String, Object>builder()
                                    .put("description", "")
                                    .put("content", ImmutableMap.<String, Object>builder()
                                            .put("application/json", ImmutableMap.<String, Object>builder()
                                                    .put("schema", ImmutableMap.<String, Object>builder()
                                                            .put("$ref", "#/components/schemas/" + schemaName)
                                                            .build())
                                                    .build())
                                            .build())
                                    .build())
                            .build());
                }

                fillPropertiesForTemplate(schemaName, asbiep, isArray, isSuppressRoot);
            } else {
                boolean isArray = template.isArrayForJsonExpression();
                String schemaName = template.getSchemaName();
                boolean isSuppressRoot = template.isSuppressRootProperty();
                String bieName = getBieName(topLevelAsbiep);

                path.put("summary", "");
                path.put("description", "");
                putOperationSecurity(path, bieName + "Write");
                putConfiguredOperationSecurity(template, path);
                if (StringUtils.hasLength(template.getTagName())) {
                    path.put("tags", Arrays.asList(template.getTagName()));
                }
                path.put("operationId", template.getOperationId());
                ensurePathParameters(path, getOperation(), isArray, template);
                if (template.getMessageBodyType().equals("Request")) {
                    path.put("requestBody", ImmutableMap.<String, Object>builder()
                            .put("description", "")
                            .put("content", ImmutableMap.<String, Object>builder()
                                    .put("application/json", ImmutableMap.<String, Object>builder()
                                            .put("schema", ImmutableMap.<String, Object>builder()
                                                    .put("$ref", "#/components/schemas/" + schemaName)
                                                    .build())
                                            .build())
                                    .build())
                            .build());
                    if (!path.containsKey("responses")) {
                        path.put("responses", ImmutableMap.<String, Object>builder()
                                .put("200", ImmutableMap.<String, Object>builder()
                                        .put("description", "")
                                        .put("content", ImmutableMap.<String, Object>builder()
                                                .put("application/json", ImmutableMap.<String, Object>builder()
                                                        .put("schema", ImmutableMap.<String, Object>builder()
                                                                .put("$ref", "#/components/schemas/" + schemaName)
                                                                .build())
                                                        .build())
                                                .build())
                                        .build())
                                .build());
                    }
                }
                if (template.getMessageBodyType().equals("Response")) {
                    path.put("responses", ImmutableMap.<String, Object>builder()
                            .put("200", ImmutableMap.<String, Object>builder()
                                    .put("description", "")
                                    .put("content", ImmutableMap.<String, Object>builder()
                                            .put("application/json", ImmutableMap.<String, Object>builder()
                                                    .put("schema", ImmutableMap.<String, Object>builder()
                                                            .put("$ref", "#/components/schemas/" + schemaName)
                                                            .build())
                                                    .build())
                                            .build())
                                    .build())
                            .build());
                }

                fillPropertiesForTemplate(schemaName, asbiep, isArray, isSuppressRoot);
            }
        }
    }

    private class DeleteGeneratorForOperation implements GeneratorForOperation {
        @Override
        public Operation getOperation() {
            return DELETE;
        }

        @Override
        public void proceed(TopLevelAsbiepSummaryRecord topLevelAsbiep,
                            OpenAPITemplateForVerbOption template,
                            Map<String, Object> path,
                            AsbiepSummaryRecord asbiep) {
            // Issue #1610: OpenAPI 3.1 permits a request body on DELETE, 3.0.3 forbids it (the specification states
            // it "SHALL be ignored"). A "Request" message body therefore emits the requestBody in 3.1 and drops it in
            // 3.0 (a banner in the editor prompts switching to OpenAPI 3.1 to keep it); either way its success
            // response is a status-only 202 (Accepted) -- a DELETE does not echo the request back. This anticipates
            // #1347 (RFC 9457 error responses), whose proposed Verb x Array matrix makes the DELETE success a 202.
            // A "Response" message body emits a 200 response carrying the BIE, exactly like the other verbs.
            // Finer control (e.g. a DELETE request body together with a custom success body) is deferred and
            // tracked on #1610.
            boolean requestBodyAllowed = dialect.allowsDeleteRequestBody();
            boolean isArray = template.isArrayForJsonExpression();
            String schemaName = template.getSchemaName();
            boolean isSuppressRoot = template.isSuppressRootProperty();
            boolean scaffolded = path != null && path.size() > 0;

            if (!scaffolded) {
                String bieName = getBieName(topLevelAsbiep);

                path.put("summary", "");
                path.put("description", "");
                putOperationSecurity(path, bieName + "Write");
                putConfiguredOperationSecurity(template, path);
                if (StringUtils.hasLength(template.getTagName())) {
                    path.put("tags", Arrays.asList(template.getTagName()));
                }
                path.put("operationId", template.getOperationId());
            }
            ensurePathParameters(path, getOperation(), isArray, template);
            if (scaffolded && requestBodyAllowed
                    && StringUtils.hasLength(template.getTagName()) && !path.containsKey("tags")) {
                path.put("tags", Arrays.asList(template.getTagName()));
            }

            boolean isResponse = template.getMessageBodyType().equals("Response");
            if (!isResponse) {
                if (requestBodyAllowed) {
                    path.put("requestBody", ImmutableMap.<String, Object>builder()
                            .put("description", "")
                            .put("content", ImmutableMap.<String, Object>builder()
                                    .put("application/json", ImmutableMap.<String, Object>builder()
                                            .put("schema", ImmutableMap.<String, Object>builder()
                                                    .put("$ref", "#/components/schemas/" + schemaName)
                                                    .build())
                                            .build())
                                    .build())
                            .build());
                }
                // A subsequent template for the SAME DELETE operation (the Response sibling of a Request) must
                // not lose its 200, so a status-only 202 is emitted only when no success response is there yet.
                if (!path.containsKey("responses")) {
                    path.put("responses", ImmutableMap.<String, Object>builder()
                            .put("202", ImmutableMap.<String, Object>builder()
                                    .put("description", "")
                                    .build())
                            .build());
                }
            } else {
                path.put("responses", ImmutableMap.<String, Object>builder()
                        .put("200", ImmutableMap.<String, Object>builder()
                                .put("description", "")
                                .put("content", ImmutableMap.<String, Object>builder()
                                        .put("application/json", ImmutableMap.<String, Object>builder()
                                                .put("schema", ImmutableMap.<String, Object>builder()
                                                        .put("$ref", "#/components/schemas/" + schemaName)
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build());
            }

            // The response references the BIE schema; a request body does too, where it is kept.
            if (isResponse || requestBodyAllowed) {
                fillPropertiesForTemplate(schemaName, asbiep, isArray, isSuppressRoot);
            }
        }
    }

    private void generateTopLevelAsbiep(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        AsbiepSummaryRecord asbiep = generationContext.findASBIEP(topLevelAsbiep.asbiepId(), topLevelAsbiep);
        generationContext.referenceCounter().increase(asbiep);
        try {
            schemaBuilder.reference(asbiep);
        } finally {
            generationContext.referenceCounter().decrease(asbiep);
        }
    }

    private void generateTemplate(OpenAPITemplateForVerbOption template) {
        TopLevelAsbiepSummaryRecord topLevelAsbiep = template.getTopLevelAsbiep();
        // Issue #1730: a bodyless operation references no BIE (no ASBIEP / component schema).
        boolean bodyless = (topLevelAsbiep == null);
        AsbiepSummaryRecord asbiep = bodyless ? null : generationContext.findASBIEP(topLevelAsbiep.asbiepId(), topLevelAsbiep);
        if (!bodyless) {
            generationContext.referenceCounter().increase(asbiep);
        }
        try {
            AbieSummaryRecord typeAbie = bodyless ? null : generationContext.queryTargetABIE(asbiep);
            ReleaseDetailsRecord release = bodyless ? null : generationContext.findRelease(topLevelAsbiep.release().releaseId());

            Map<String, Object> paths;
            Map<String, Object> securitySchemes;

            if (root == null) {
                root = new LinkedHashMap<>();
                root.put("openapi", dialect.openApiVersion());
                ImmutableMap.Builder infoBuilder = ImmutableMap.<String, Object>builder()
                        .put("title", option.getOasDoc().getTitle());
                if (StringUtils.hasLength(option.getOasDoc().getDescription())) {
                    infoBuilder = infoBuilder.put("description", option.getOasDoc().getDescription());
                }
                if (StringUtils.hasLength(option.getOasDoc().getTermsOfService())) {
                    infoBuilder = infoBuilder.put("termsOfService", option.getOasDoc().getTermsOfService());
                }
                if (StringUtils.hasLength(option.getOasDoc().getContactName()) ||
                        StringUtils.hasLength(option.getOasDoc().getContactUrl()) ||
                        StringUtils.hasLength(option.getOasDoc().getContactEmail())) {
                    ImmutableMap.Builder contactBuilder = ImmutableMap.<String, Object>builder();
                    if (StringUtils.hasLength(option.getOasDoc().getContactName())) {
                        contactBuilder = contactBuilder.put("name", option.getOasDoc().getContactName());
                    }
                    if (StringUtils.hasLength(option.getOasDoc().getContactUrl())) {
                        contactBuilder = contactBuilder.put("url", option.getOasDoc().getContactUrl());
                    }
                    if (StringUtils.hasLength(option.getOasDoc().getContactEmail())) {
                        contactBuilder = contactBuilder.put("email", option.getOasDoc().getContactEmail());
                    }
                    infoBuilder = infoBuilder.put("contact", contactBuilder.build());
                }
                if (StringUtils.hasLength(option.getOasDoc().getLicenseName()) ||
                        StringUtils.hasLength(option.getOasDoc().getLicenseUrl())) {
                    ImmutableMap.Builder licenseBuilder = ImmutableMap.<String, Object>builder();
                    if (StringUtils.hasLength(option.getOasDoc().getLicenseName())) {
                        licenseBuilder = licenseBuilder.put("name", option.getOasDoc().getLicenseName());
                    }
                    if (StringUtils.hasLength(option.getOasDoc().getLicenseUrl())) {
                        licenseBuilder = licenseBuilder.put("url", option.getOasDoc().getLicenseUrl());
                    }
                    infoBuilder = infoBuilder.put("license", licenseBuilder.build());
                }
                infoBuilder = infoBuilder.put("version", option.getOasDoc().getVersion())
                        .put("x-oagis-license", (release != null && StringUtils.hasLength(release.releaseLicense())) ? release.releaseLicense() : "");
                root.put("info", infoBuilder.build());

                // Issue #1729: explicit schemes emit the document-level (global) Security Requirement
                // selected in the UI, placed right after `info`. The legacy default adds no root entry.
                if (!isLegacyDefault() && !globalSecurityRequirements().isEmpty()) {
                    root.put("security", buildSecurityRequirements(globalSecurityRequirements()));
                }

                paths = new LinkedHashMap();
                // Issue #1729: with no explicit schemes, keep the legacy OAuth2 component (per-operation
                // Read/Write scopes); otherwise emit every configured scheme.
                securitySchemes = isLegacyDefault()
                        ? buildLegacyOauth2Scheme(topLevelAsbiep)
                        : buildSecuritySchemes();

                root.put("paths", paths);
                root.put("components", ImmutableMap.<String, Object>builder()
                        .put("securitySchemes", securitySchemes)
                        .put("schemas", schemas)
                        .build()
                );
            } else {
                paths = (Map<String, Object>) root.get("paths");
                securitySchemes = (Map<String, Object>) ((Map<String, Object>) root.get("components")).get("securitySchemes");

                // Issue #1729: only the legacy default OAuth2 scheme accumulates per-BIE Read/Write scopes
                // across BIEs; explicit schemes are static so there is nothing to merge.
                if (isLegacyDefault()) {
                    Map<String, Object> oauth2 = (Map<String, Object>) securitySchemes.get("OAuth2");
                    Map<String, Object> flows = (Map<String, Object>) oauth2.get("flows");
                    Map<String, Object> authorizationCode = (Map<String, Object>) flows.get("authorizationCode");
                    Map<String, Object> scopes = (Map<String, Object>) authorizationCode.get("scopes");
                    if (!bodyless) {
                        scopes.putAll(getAuthorizationCodeScopes(topLevelAsbiep));
                    }
                }
            }

            Map<String, Object> pathMap = new LinkedHashMap<>();
            Map<String, Object> path = new LinkedHashMap();
            String pathName = getResolvedResourceName(template);
            String verbKey = template.getVerbOption().name().toLowerCase();
            if (paths.isEmpty() || !paths.containsKey(pathName)) {
                paths.put(pathName, pathMap);
                pathMap.put("summary", "");
                pathMap.put("description", "");
                pathMap.put(verbKey, path);
            } else {
                pathMap = (Map<String, Object>) paths.get(pathName);
                if (!pathMap.containsKey(verbKey)) {
                    pathMap.put(verbKey, path);
                } else {
                    path = (Map<String, Object>) pathMap.get(verbKey);
                }
            }

            if (bodyless) {
                // Issue #1730: emit a BIE-less operation (no request/response schema; status-only response).
                generateBodylessOperation(template, path);
            } else {
                GeneratorForOperation generatorForOperation = generatorForOperationMap.get(template.getVerbOption());
                if (generatorForOperation == null) {
                    throw new UnsupportedOperationException("Unsupported Operation: " + template.getVerbOption());
                }
                generatorForOperation.proceed(topLevelAsbiep, template, path, asbiep);
            }
        } finally {
            if (!bodyless) {
                generationContext.referenceCounter().decrease(asbiep);
            }
        }
    }

    /**
     * Issue #1730: Generates an operation that does not reference a BIE. Emits the HTTP method,
     * path parameters and a single status-only response (e.g. 202 Accepted, 204 No Content)
     * without any request/response body or component schema.
     */
    private void generateBodylessOperation(OpenAPITemplateForVerbOption template, Map<String, Object> path) {
        // Populate operation metadata only if this path+verb has not been built yet, so that a
        // collision with an already-generated operation is not clobbered (and an existing,
        // immutable operation map is never mutated).
        if (!path.containsKey("operationId")) {
            path.put("summary", "");
            path.put("description", "");
            // Issue #1729: configured per-operation `security` right after `description` (before tags).
            putConfiguredOperationSecurity(template, path);
            if (StringUtils.hasLength(template.getTagName())) {
                path.put("tags", Arrays.asList(template.getTagName()));
            }
            path.put("operationId", template.getOperationId());
        }
        ensurePathParameters(path, template.getVerbOption(), template.isArrayForJsonExpression(), template);

        // A bodyless operation must still declare at least one (status-only) response.
        // The status code is taken from the message body when present (Response-type), otherwise
        // derived from the verb (Issue #1730: DELETE -> 202 Accepted, PATCH -> 204 No Content).
        if (!path.containsKey("responses")) {
            Integer statusCode = (template.getHttpStatusCode() != null)
                    ? template.getHttpStatusCode()
                    : defaultStatusForVerb(template.getVerbOption());
            String statusKey = String.valueOf(statusCode);
            path.put("responses", ImmutableMap.<String, Object>builder()
                    .put(statusKey, ImmutableMap.<String, Object>builder()
                            .put("description", reasonPhrase(statusCode))
                            .build())
                    .build());
        }
    }

    private Integer defaultStatusForVerb(Operation verb) {
        if (verb == null) {
            return 200;
        }
        switch (verb) {
            case DELETE:
                return 202;
            case PATCH:
                return 204;
            default:
                return 200;
        }
    }

    private String reasonPhrase(Integer statusCode) {
        if (statusCode == null) {
            return "";
        }
        switch (statusCode) {
            case 200:
                return "OK";
            case 201:
                return "Created";
            case 202:
                return "Accepted";
            case 204:
                return "No Content";
            default:
                return "";
        }
    }

    private void ensureRoot() {
        if (root == null) {
            throw new IllegalStateException();
        }
    }

    @Override
    public File asFile(String filename) throws IOException {
        ensureRoot();

        File tempFile = File.createTempFile(ScoreGuidUtils.randomGuid(), null);
        String extension = (expressionMapper instanceof YAMLMapper) ? "yml" : "json";

        tempFile = new File(tempFile.getParentFile(), filename + "." + extension);

        expressionMapper.writeValue(tempFile, root);
        logger.info("Open API " + dialect.openApiVersion() + " Schema is generated: " + tempFile);

        return tempFile;
    }
}
