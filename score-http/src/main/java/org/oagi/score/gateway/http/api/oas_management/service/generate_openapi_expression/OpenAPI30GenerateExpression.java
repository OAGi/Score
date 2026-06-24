package org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableMap;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BIE;
import org.oagi.score.gateway.http.api.bie_management.model.Facet;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.GenerationContext;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.Helper;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListValueSummaryRecord;
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
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.oagi.score.gateway.http.api.bie_management.service.generate_expression.Helper.*;
import static org.oagi.score.gateway.http.api.oas_management.model.Operation.*;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;


@Component
@Scope(SCOPE_PROTOTYPE)
public class OpenAPI30GenerateExpression implements BieGenerateOpenApiExpression, InitializingBean {

    private static final String OPEN_API_VERSION = "3.0.3";
    static final String VERSION_PATH_PARAMETER = "{version}";
    private static final Pattern PATH_PARAMETER_PATTERN = Pattern.compile("\\{([^{}]+)}");

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private GenerationContext generationContext;
    private OpenAPIGenerateExpressionOption option;

    private Map<Operation, GeneratorForOperation> generatorForOperationMap = new HashMap<>();
    private ObjectMapper mapper;
    private ObjectMapper expressionMapper;

    private Map<String, Object> root;
    private Map<String, Object> schemas = new LinkedHashMap<>();
    private Map<TopLevelAsbiepId, String> reusedTopLevelAsbiepNameMap;

    public OpenAPI30GenerateExpression(GenerationContext generationContext, OpenAPIGenerateExpressionOption option) {
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
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        OpenAPIExpressionFormat openAPIExpressionFormat = option.getOpenAPIExpressionFormat();
        switch (openAPIExpressionFormat) {
            case YAML:
                expressionMapper = new ObjectMapper(new YAMLFactory());
                break;
            case JSON:
                expressionMapper = new ObjectMapper();
                break;
        }
        expressionMapper.enable(SerializationFeature.INDENT_OUTPUT);

        root = null;
        schemas = new LinkedHashMap<>();
        reusedTopLevelAsbiepNameMap = null;
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
    // Backend targets OpenAPI 3.0.3 (apiKey | http | oauth2 | openIdConnect).
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
    // with no flows yields an empty flows object. Per OpenAPI 3.0.3 each OAuth Flow Object's scopes key
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
        // oauth2/openIdConnect carry the scope names; all other types MUST be an empty array (3.0.3).
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
            getReference(asbiep, schemaName, isSuppressRoot);
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
            // Issue #1610: OpenAPI 3.0.3 forbids a request body on DELETE (the specification states it
            // "SHALL be ignored"). A "Request" message body therefore drops the body (a banner in the editor
            // prompts switching to OpenAPI 3.1.1 to keep it; see OpenAPI31GenerateExpression) and emits a
            // status-only 202 (Accepted) success -- consistent with the 3.1 generator and anticipating #1347
            // (RFC 9457 error responses), whose proposed Verb x Array matrix makes the DELETE success a 202.
            // A "Response" message body still emits a 200 response carrying the BIE (a response body is
            // allowed in 3.0.3), exactly like the other verbs.
            if (path != null && path.size() > 0) {
                ensurePathParameters(path, getOperation(), template.isArrayForJsonExpression(), template);
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
                    // The response references the BIE schema, so generate it.
                    fillPropertiesForTemplate(schemaName, asbiep, isArray, isSuppressRoot);
                } else {
                    // Request: the body is dropped (3.0.3 forbids it); emit a status-only 202. Nothing
                    // references the BIE schema here, so none is generated (avoids an orphan component).
                    path.put("responses", ImmutableMap.<String, Object>builder()
                            .put("202", ImmutableMap.<String, Object>builder()
                                    .put("description", "")
                                    .build())
                            .build());
                }
            }
        }
    }

    private void generateTopLevelAsbiep(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        AsbiepSummaryRecord asbiep = generationContext.findASBIEP(topLevelAsbiep.asbiepId(), topLevelAsbiep);
        generationContext.referenceCounter().increase(asbiep);
        try {
            getReference(asbiep);
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
                root.put("openapi", option.getOasDoc().getOpenAPIVersion());
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

    private Map<String, Object> makeProperties(AbieSummaryRecord typeAbie, TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        AsbiepSummaryRecord asbiep = generationContext.findASBIEP(topLevelAsbiep.asbiepId(), topLevelAsbiep);
        AsccpSummaryRecord asccp = generationContext.getAsccp(asbiep.basedAsccpManifestId());
        Map<String, Object> properties = new LinkedHashMap();
        // Issue #1148
        properties.put("x-oagis-bie-guid", asbiep.getGuid());
        properties.put("x-oagis-bie-date-time", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(typeAbie.lastUpdated().when()));
        properties.put("x-oagis-bie-version", StringUtils.hasLength(topLevelAsbiep.version()) ? topLevelAsbiep.version() : "");
        // Issue #1603
        properties.put("x-oagis-bie-den", asccp.propertyTerm());
        // Issue #1574
        ReleaseDetailsRecord release = generationContext.findRelease(topLevelAsbiep.release().releaseId());
        properties.put("x-oagis-bie-uri", option.getScheme() + "://" + option.getHost() + "/profile_bie/" + topLevelAsbiep.topLevelAsbiepId().toString());
        properties.put("x-oagis-release", release.releaseNum());
        properties.put("x-oagis-release-date", new SimpleDateFormat("yyyy-MM-dd").format(release.lastUpdated().when()));
        properties.put("required", new ArrayList());
        properties.put("additionalProperties", false);

        return properties;
    }

    private void suppressRootProperty(Map<String, Object> parent) {
        Map<String, Object> properties = (Map<String, Object>) parent.get("properties");
        // Get the first element from 'properties' property and move all children of the element to the parent.
        Set<String> keys = properties.keySet();
        if (keys.isEmpty()) {
            return;
        }
        Map<String, Object> rootProperties = (Map<String, Object>) properties.get(keys.iterator().next());
        parent.put("type", "object");
        Arrays.asList("required", "additionalProperties", "properties").stream().forEach(e -> parent.remove(e));

        for (Map.Entry<String, Object> entry : rootProperties.entrySet()) {
            parent.put(entry.getKey(), entry.getValue());
        }
    }

    private void fillProperties(Map<String, Object> parent,
                                Map<String, Object> schemas,
                                AsbieSummaryRecord asbie,
                                GenerationContext generationContext) {

        Map<String, Object> properties = new LinkedHashMap();
        if (!parent.containsKey("properties")) {
            parent.put("properties", new LinkedHashMap<String, Object>());
        }

        AsbiepSummaryRecord asbiep = generationContext.queryAssocToASBIEP(asbie);
        AsccpSummaryRecord asccp = generationContext.queryBasedASCCP(asbiep);
        String name = convertIdentifierToId(camelCase(asccp.propertyTerm()));

        int minVal = asbie.cardinality().min();
        int maxVal = asbie.cardinality().max();
        // Issue #562
        boolean isArray = (maxVal < 0 || maxVal > 1);
        boolean isNillable = (asbie.nillable() != null) ? asbie.nillable() : false;

        boolean reused = !asbie.ownerTopLevelAsbiepId().equals(asbiep.ownerTopLevelAsbiepId());
        if (minVal > 0) {
            List<String> parentRequired = (List<String>) parent.get("required");
            if (parentRequired == null) {
                throw new IllegalStateException();
            }
            parentRequired.add(name);
        }

        if (reused) {
            SchemaReference ref = getReference(asbiep);
            properties.put("$ref", ref.getPath());
        } else {
            AbieSummaryRecord typeAbie = generationContext.queryTargetABIE(asbiep);
            AsccSummaryRecord ascc = generationContext.queryBasedASCC(asbie);

            if (option.isBieDefinition()) {
                String definition = asbie.definition();
                if (StringUtils.hasLength(definition)) {
                    properties.put("description", definition);
                }
            }

            properties.put("type", "object");
            properties.put("required", new ArrayList());
            properties.put("additionalProperties", false);
            properties.put("properties", new LinkedHashMap<String, Object>());

            fillProperties(properties, schemas, typeAbie, generationContext);

            if (properties.containsKey("required") && ((List) properties.get("required")).isEmpty()) {
                properties.remove("required");
            }

            properties = oneOf(allOf(properties), isNillable);
        }

        // Issue #1298
        if (asbie.deprecated() != null && asbie.deprecated()) {
            properties.put("deprecated", true);
        }

        if (isArray) {
            Map<String, Object> items = new LinkedHashMap();
            items.putAll(properties);

            properties = new LinkedHashMap();

            String description = (String) items.remove("description");
            if (StringUtils.hasLength(description)) {
                properties.put("description", description);
            }
            properties.put("type", "array");
            if (minVal > 0) {
                properties.put("minItems", minVal);
            }
            if (maxVal > 0) {
                properties.put("maxItems", maxVal);
            }
            properties.put("items", items);

            // Issue #1483
            // make a global property for an array
            if (reused) {
                properties = makeGlobalPropertyIfArray(schemas, resolveReusedSchemaName(asbiep), properties);
            }
        }

        ((Map<String, Object>) parent.get("properties")).put(name, properties);
    }

    private Map<String, Object> makeGlobalPropertyIfArray(Map<String, Object> schemas, String name,
                                                          Map<String, Object> properties) {
        if (properties == null || !"array".equals(properties.get("type"))) {
            return properties;
        }

        Set<String> keySet = properties.keySet();
        boolean customized = keySet.contains("minItems") || keySet.contains("maxItems");
        if (customized) {
            return properties;
        }

        String nameForList = name + "List";
        if (!schemas.containsKey(nameForList)) {
            schemas.put(nameForList, properties);
        }

        Map<String, Object> refProperties = new HashMap<>();
        refProperties.put("$ref", "#/components/schemas/" + nameForList);
        return refProperties;
    }

    private void fillProperties(Map<String, Object> parent,
                                Map<String, Object> schemas,
                                AsbiepSummaryRecord asbiep, AbieSummaryRecord abie,
                                GenerationContext generationContext) {

        AsccpSummaryRecord asccp = generationContext.queryBasedASCCP(asbiep);
        String name = convertIdentifierToId(camelCase(asccp.propertyTerm()));

        List<String> parentRequired = (List<String>) parent.get("required");
        parentRequired.add(name);

        Map<String, Object> properties = new LinkedHashMap();
        if (!parent.containsKey("properties")) {
            parent.put("properties", new LinkedHashMap<String, Object>());
        }

        if (option.isBieDefinition()) {
            String definition = asbiep.definition();
            if (!StringUtils.hasLength(definition)) {
                definition = abie.definition();
            }
            if (StringUtils.hasLength(definition)) {
                properties.put("description", definition);
            }
        }

        properties.put("type", "object");
        properties.put("required", new ArrayList());
        properties.put("additionalProperties", false);

        fillProperties(properties, schemas, abie, generationContext);

        if (properties.containsKey("required") && ((List) properties.get("required")).isEmpty()) {
            properties.remove("required");
        }

        ((Map<String, Object>) parent.get("properties")).put(name, properties);
    }

    private Map<String, Object> toProperties(XbtSummaryRecord xbt) {
        String openapi30Map = xbt.openApi30Map();
        try {
            return mapper.readValue(openapi30Map, LinkedHashMap.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String fillSchemas(Map<String, Object> schemas,
                               XbtSummaryRecord xbt, Guid guid, Facet facet) {
        if (guid == null) {
            guid = new Guid(ScoreGuidUtils.randomGuid());
        }

        String name = "type_" + guid;

        Map<String, Object> content = toProperties(xbt);
        if (facet.minLength() != null) {
            content.put("minLength", facet.minLength().longValue());
        }
        if (facet.maxLength() != null) {
            content.put("maxLength", facet.maxLength().longValue());
        }
        if (StringUtils.hasLength(facet.pattern())) {
            // Override 'pattern' and 'format' properties
            content.remove("pattern");
            content.remove("format");
            content.put("pattern", facet.pattern());
        }

        schemas.put(name, content);

        return "#/components/schemas/" + name;
    }

    private String fillSchemas(Map<String, Object> schemas,
                               XbtSummaryRecord xbt) {
        String builtInType = xbt.builtInType();
        if (builtInType.startsWith("xsd:")) {
            builtInType = builtInType.substring(4);
        }
        if (!schemas.containsKey(builtInType)) {
            Map<String, Object> content = toProperties(xbt);
            schemas.put(builtInType, content);
        }

        return "#/components/schemas/" + builtInType;
    }

    private String fillSchemas(Map<String, Object> schemas,
                               BbieSummaryRecord bbie, CodeListSummaryRecord codeList) {
        DtAwdPriSummaryRecord dtAwdPri =
                generationContext.findDtAwdPriByBbieAndDefaultIsTrue(bbie);

        Map<String, Object> properties;
        if (dtAwdPri.codeListManifestId() != null) {
            properties = new LinkedHashMap();
            properties.put("type", "string");
        } else {
            XbtSummaryRecord xbt = generationContext.getXbt(dtAwdPri.xbtManifestId());
            properties = toProperties(xbt);
        }

        return fillSchemas(properties, schemas, codeList);
    }

    private String fillSchemas(Map<String, Object> schemas,
                               BbieScSummaryRecord bbieSc, CodeListSummaryRecord codeList) {
        DtScAwdPriSummaryRecord dtScAwdPri =
                generationContext.findDtScAwdPriByBbieScAndDefaultIsTrue(bbieSc);

        Map<String, Object> properties;
        if (dtScAwdPri.codeListManifestId() != null) {
            properties = new LinkedHashMap();
            properties.put("type", "string");
        } else {
            XbtSummaryRecord xbt = generationContext.getXbt(dtScAwdPri.xbtManifestId());
            properties = toProperties(xbt);
        }

        return fillSchemas(properties, schemas, codeList);
    }

    private String fillSchemas(Map<String, Object> properties,
                               Map<String, Object> schemas,
                               CodeListSummaryRecord codeList) {

        AgencyIdListValueSummaryRecord agencyIdListValue = generationContext.findAgencyIdListValue(codeList.agencyIdListValueManifestId());
        String codeListName = Helper.getCodeListTypeName(codeList, agencyIdListValue);
        /*
         * Issue #589
         */
        codeListName = Stream.of(codeListName.split("_"))
                .map(e -> convertIdentifierToId(camelCase(e))).collect(Collectors.joining("_"));

        if (!schemas.containsKey(codeListName)) {
            List<CodeListValueSummaryRecord> codeListValues = generationContext.getCodeListValues(codeList);
            List<String> enumerations = codeListValues.stream().map(e -> e.value()).collect(Collectors.toList());
            if (!enumerations.isEmpty()) {
                properties.put("enum", enumerations);
            }

            schemas.put(codeListName, properties);
        }

        return "#/components/schemas/" + codeListName;
    }

    private String fillSchemas(Map<String, Object> schemas,
                               AgencyIdListSummaryRecord agencyIdList) {
        AgencyIdListValueSummaryRecord agencyIdListValue =
                generationContext.findAgencyIdListValue(agencyIdList.agencyIdListValueManifestId());
        String agencyListTypeName = Helper.getAgencyListTypeName(agencyIdList, agencyIdListValue);
        /*
         * Issue #589
         */
        agencyListTypeName = Stream.of(agencyListTypeName.split("_"))
                .map(e -> convertIdentifierToId(camelCase(e))).collect(Collectors.joining("_"));
        if (!schemas.containsKey(agencyListTypeName)) {
            Map<String, Object> properties = new LinkedHashMap();
            properties.put("type", "string");

            List<AgencyIdListValueSummaryRecord> agencyIdListValues =
                    generationContext.findAgencyIdListValueByAgencyIdListManifestId(agencyIdList.agencyIdListManifestId());
            List<String> enumerations = agencyIdListValues.stream().map(e -> e.value()).collect(Collectors.toList());
            if (!enumerations.isEmpty()) {
                properties.put("enum", enumerations);
            }

            schemas.put(agencyListTypeName, properties);
        }

        return "#/components/schemas/" + agencyListTypeName;
    }

    private void fillProperties(Map<String, Object> parent,
                                Map<String, Object> schemas,
                                AbieSummaryRecord abie,
                                GenerationContext generationContext) {

        List<BIE> children = generationContext.queryChildBIEs(abie);
        AccSummaryRecord acc = generationContext.getAcc(abie.basedAccManifestId());
        if (OagisComponentType.Choice == acc.componentType()) {
            List<Object> oneOf = new ArrayList();

            for (BIE bie : children) {
                Map<String, Object> item = new LinkedHashMap();
                item.put("type", "object");
                item.put("required", new ArrayList());
                item.put("additionalProperties", false);
                item.put("properties", new LinkedHashMap<String, Object>());

                fillProperties(item, schemas, bie, generationContext);

                if (item.containsKey("required") && ((List) item.get("required")).isEmpty()) {
                    item.remove("required");
                }
                oneOf.add(item);
            }

            parent.clear();
            parent.put("oneOf", oneOf);
        } else {
            for (BIE bie : children) {
                fillProperties(parent, schemas, bie, generationContext);
            }
        }
    }

    private void fillProperties(Map<String, Object> parent,
                                Map<String, Object> schemas,
                                BIE bie,
                                GenerationContext generationContext) {
        if (bie instanceof BbieSummaryRecord) {
            BbieSummaryRecord bbie = (BbieSummaryRecord) bie;
            fillProperties(parent, schemas, bbie, generationContext);
        } else {
            AsbieSummaryRecord asbie = (AsbieSummaryRecord) bie;
            if (Helper.isAnyProperty(asbie, generationContext)) {
                parent.put("additionalProperties", true);
            } else {
                AsbiepSummaryRecord asbiep = generationContext.queryAssocToASBIEP(asbie);

                generationContext.referenceCounter().increase(asbiep)
                        .ifNotCircularReference(asbiep,
                                () -> fillProperties(parent, schemas, asbie, generationContext))
                        .decrease(asbiep);
            }
        }
    }

    private Object readJsonValue(String textContent) {
        try {
            return mapper.readValue(textContent, Object.class);
        } catch (Exception e) {
            logger.warn("Can't read JSON value from given text: " + textContent, e);
        }
        return null;
    }

    private void fillProperties(Map<String, Object> parent,
                                Map<String, Object> schemas,
                                BbieSummaryRecord bbie,
                                GenerationContext generationContext) {
        BccSummaryRecord bcc = generationContext.queryBasedBCC(bbie);
        BccpSummaryRecord bccp = generationContext.queryToBCCP(bcc);
        DtSummaryRecord bdt = generationContext.queryBDT(bccp);

        int minVal = bbie.cardinality().min();
        int maxVal = bbie.cardinality().max();
        // Issue #562
        boolean isArray = (maxVal < 0 || maxVal > 1);
        boolean isNillable = (bbie.nillable() != null) ? bbie.nillable() : false;

        String name = convertIdentifierToId(camelCase(bccp.propertyTerm()));

        Map<String, Object> properties = new LinkedHashMap();
        if (!parent.containsKey("properties")) {
            parent.put("properties", new LinkedHashMap<String, Object>());
        }

        if (option.isBieDefinition()) {
            String definition = bbie.definition();
            if (StringUtils.hasLength(definition)) {
                properties.put("description", definition);
            }
        }

        if (minVal > 0) {
            List<String> parentRequired = (List<String>) parent.get("required");
            if (parentRequired == null) {
                throw new IllegalStateException();
            }
            parentRequired.add(name);
        }

        // Issue #700
        if (bbie.valueConstraint() != null) {
            if (StringUtils.hasLength(bbie.valueConstraint().fixedValue())) {
                properties.put("enum", Arrays.asList(bbie.valueConstraint().fixedValue()));
            } else if (StringUtils.hasLength(bbie.valueConstraint().defaultValue())) {
                properties.put("default", bbie.valueConstraint().defaultValue());
            }
        }

        // Issue #692
        XbtSummaryRecord xbt = getXbt(bbie, bdt);
        String exampleText = bbie.example();
        if (StringUtils.hasLength(exampleText)) {
            properties.put("example", exampleText);
        } else { // Issue #1405
            properties.put("example", emptyExample(xbt));
        }

        // Issue #564
        String ref = getReference(schemas, bbie, bdt, generationContext);
        List<BbieScSummaryRecord> bbieScList = generationContext.queryBBIESCs(bbie)
                .stream().filter(e -> e.cardinality().max() != 0).collect(Collectors.toList());
        if (bbieScList.isEmpty()) {
            if (ref == null) {
                Map<String, Object> content = applyFacet(toProperties(xbt), bbie.facet());
                properties.putAll(content);
            } else {
                properties.put("$ref", ref);
            }
            properties = oneOf(allOf(properties), isNillable);
        } else {
            properties.put("type", "object");
            properties.put("required", new ArrayList());
            properties.put("additionalProperties", false);
            properties.put("properties", new LinkedHashMap<String, Object>());

            Map<String, Object> contentProperties = new LinkedHashMap();
            if (ref == null) {
                Map<String, Object> content = applyFacet(toProperties(xbt), bbie.facet());
                contentProperties.putAll(content);
            } else {
                contentProperties.put("$ref", ref);
            }
            for (String key : Arrays.asList("description", "enum", "default", "example")) {
                if (properties.containsKey(key)) {
                    contentProperties.put(key, properties.remove(key));
                }
            }

            ((List<String>) properties.get("required")).add("content");
            ((Map<String, Object>) properties.get("properties"))
                    .put("content", oneOf(allOf(contentProperties), isNillable));

            for (BbieScSummaryRecord bbieSc : bbieScList) {
                fillProperties(properties, schemas, bbieSc, generationContext);
            }
        }

        // Issue #1298
        if (bbie.deprecated() != null && bbie.deprecated()) {
            properties.put("deprecated", true);
        }

        if (isArray) {
            String description = (String) properties.remove("description");
            Map<String, Object> items = new LinkedHashMap(properties);
            properties = new LinkedHashMap();
            if (StringUtils.hasLength(description)) {
                properties.put("description", description);
            }
            properties.put("type", "array");

            Boolean deprecated = (Boolean) items.remove("deprecated");
            if (deprecated != null) {
                properties.put("deprecated", deprecated);
            }

            if (minVal > 0) {
                properties.put("minItems", minVal);
            }
            if (maxVal > 0) {
                properties.put("maxItems", maxVal);
            }
            properties.put("items", items);
        }

        ((Map<String, Object>) parent.get("properties")).put(name, properties);
    }

    private Object emptyExample(XbtSummaryRecord xbt) {
        Map<String, Object> properties = toProperties(xbt);
        Object type = properties.getOrDefault("type", "string");
        if ("boolean".equals(type)) {
            return false; // false for boolean (example: false)
        } else if ("integer".equals(type) || "number".equals(type)) {
            return null; // null value if integer or numeric (example: null)
        } else {
            return ""; // string or date, then empty string (example: "")
        }
    }

    private Map<String, Object> allOf(Map<String, Object> properties) {
        if (properties.containsKey("$ref") && properties.size() > 1) {
            Map<String, Object> prop = new LinkedHashMap();
            Map<String, Object> refMap = ImmutableMap.<String, Object>builder()
                    .put("$ref", properties.remove("$ref"))
                    .build();
            prop.put("allOf", (properties.isEmpty()) ? Arrays.asList(refMap) : Arrays.asList(refMap, properties));

            return prop;
        }

        return properties;
    }

    private Map<String, Object> oneOf(Map<String, Object> properties,
                                      boolean isNillable) {
        if (isNillable) {
            Map<String, Object> prop = new LinkedHashMap();
            prop.put("oneOf", Arrays.asList(
                    ImmutableMap.builder()
                            .put("nullable", true)
                            .build(),
                    properties
            ));

            return prop;
        }

        return properties;
    }

    private XbtSummaryRecord getXbt(BbieSummaryRecord bbie, DtSummaryRecord bdt) {
        if (bbie.primitiveRestriction().xbtManifestId() == null) {
            DtAwdPriSummaryRecord dtAwdPri =
                    generationContext.findDtAwdPriByBbieAndDefaultIsTrue(bbie);
            return Helper.getXbt(generationContext, dtAwdPri);
        } else {
            return generationContext.getXbt(bbie.primitiveRestriction().xbtManifestId());
        }
    }

    private Map<String, Object> applyFacet(Map<String, Object> content, Facet facet) {
        if (facet != null) {
            String type = (String) content.get("type");
            boolean isTypeString = "string".equals(type);

            if (isTypeString && facet.minLength() != null) {
                content.put("minLength", facet.minLength().longValue());
            }
            if (isTypeString && facet.maxLength() != null) {
                content.put("maxLength", facet.maxLength().longValue());
            }
            if (isTypeString && StringUtils.hasLength(facet.pattern())) {
                // Override 'pattern' and 'format' properties
                content.remove("pattern");
                content.remove("format");
                content.put("pattern", facet.pattern());
            }
        }
        return content;
    }

    private class SchemaReference {

        private String schemaName;
        private String path;
        private Map<String, Object> properties;

        public SchemaReference(String schemaName, String path, Map<String, Object> properties) {
            this.schemaName = schemaName;
            this.path = path;
            this.properties = properties;
        }

        public String getSchemaName() {
            return schemaName;
        }

        public void setSchemaName(String schemaName) {
            this.schemaName = schemaName;
        }

        public String getPath() {
            return path;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }
    }

    private SchemaReference getReference(AsbiepSummaryRecord asbiep) {
        return getReference(asbiep, null, true);
    }

    private SchemaReference getReference(AsbiepSummaryRecord asbiep,
                                         String schemaName,
                                         boolean suppressRootProperty) {
        if (schemaName == null) {
            schemaName = resolveReusedSchemaName(asbiep);
        }

        TopLevelAsbiepSummaryRecord refTopLevelAsbiep = generationContext.findTopLevelAsbiep(asbiep.ownerTopLevelAsbiepId());
        Map<String, Object> properties;
        if (!schemas.containsKey(schemaName)) {
            AbieSummaryRecord typeAbie = generationContext.queryTargetABIE(asbiep);
            properties = makeProperties(typeAbie, refTopLevelAsbiep);
            fillProperties(properties, schemas, asbiep, typeAbie, generationContext);
            if (suppressRootProperty) {
                suppressRootProperty(properties);
            }
            schemas.put(schemaName, properties);
        } else {
            properties = (Map<String, Object>) schemas.get(schemaName);
            // If it's duplicated
            if (!asbiep.getGuid().equals(properties.get("x-oagis-bie-guid"))) {
                schemaName = schemaName + refTopLevelAsbiep.topLevelAsbiepId();
                AbieSummaryRecord typeAbie = generationContext.queryTargetABIE(asbiep);
                properties = makeProperties(typeAbie, refTopLevelAsbiep);
                fillProperties(properties, schemas, asbiep, typeAbie, generationContext);
                if (suppressRootProperty) {
                    suppressRootProperty(properties);
                }
                schemas.put(schemaName, properties);
            }
        }

        String path = "#/components/schemas/" + schemaName;
        return new SchemaReference(schemaName, path, properties);
    }

    private String resolveReusedSchemaName(AsbiepSummaryRecord asbiep) {
        if (reusedTopLevelAsbiepNameMap == null) {
            reusedTopLevelAsbiepNameMap = new LinkedHashMap<>();
        }

        TopLevelAsbiepId ownerTopLevelAsbiepId = asbiep.ownerTopLevelAsbiepId();
        String existing = reusedTopLevelAsbiepNameMap.get(ownerTopLevelAsbiepId);
        if (StringUtils.hasLength(existing)) {
            return existing;
        }

        AsccpSummaryRecord asccp = generationContext.queryBasedASCCP(asbiep);
        String baseName = convertIdentifierToId(camelCase(asccp.propertyTerm()));

        String candidate = baseName;
        int suffix = 1;
        while (schemas.containsKey(candidate)) {
            candidate = baseName + suffix++;
        }

        reusedTopLevelAsbiepNameMap.put(ownerTopLevelAsbiepId, candidate);
        return candidate;
    }

    private String getReference(Map<String, Object> schemas, BbieSummaryRecord bbie, DtSummaryRecord bdt,
                                GenerationContext generationContext) {
        CodeListSummaryRecord codeList = Helper.getCodeList(generationContext, bbie, bdt);
        String ref;
        if (codeList != null) {
            ref = fillSchemas(schemas, bbie, codeList);
        } else {
            AgencyIdListSummaryRecord agencyIdList = generationContext.getAgencyIdList(bbie);
            if (agencyIdList != null) {
                ref = fillSchemas(schemas, agencyIdList);
            } else {
                // Issue #1633
                // Primitive types shouldn't be expressed in the 'schema' content.
                return null;
            }
        }

        return ref;
    }

    private XbtSummaryRecord getXbt(BbieScSummaryRecord bbieSc, DtScSummaryRecord dtSc) {
        if (bbieSc.primitiveRestriction().xbtManifestId() == null) {
            DtScAwdPriSummaryRecord dtScAwdPri =
                    generationContext.findDtScAwdPriByBbieScAndDefaultIsTrue(bbieSc);
            return generationContext.getXbt(dtScAwdPri.xbtManifestId());
        } else {
            return generationContext.getXbt(bbieSc.primitiveRestriction().xbtManifestId());
        }
    }

    private void fillProperties(Map<String, Object> parent,
                                Map<String, Object> schemas,
                                BbieScSummaryRecord bbieSc,
                                GenerationContext generationContext) {
        int minVal = bbieSc.cardinality().min();
        int maxVal = bbieSc.cardinality().max();
        if (maxVal == 0) {
            return;
        }

        DtScSummaryRecord dtSc = generationContext.getDtSc(bbieSc.basedDtScManifestId());
        String name = convertIdentifierToId(toName(dtSc.propertyTerm(), dtSc.representationTerm(), rt -> {
            if ("Text".equals(rt)) {
                return "";
            }
            return rt;
        }, true));
        Map<String, Object> properties = new LinkedHashMap();

        if (option.isBieDefinition()) {
            String definition = bbieSc.definition();
            if (StringUtils.hasLength(definition)) {
                properties.put("description", definition);
            }
        }

        if (minVal > 0) {
            ((List<String>) parent.get("required")).add(name);
        }

        // Issue #596
        if (bbieSc.valueConstraint() != null) {
            if (StringUtils.hasLength(bbieSc.valueConstraint().fixedValue())) {
                properties.put("enum", Arrays.asList(bbieSc.valueConstraint().fixedValue()));
            } else if (StringUtils.hasLength(bbieSc.valueConstraint().defaultValue())) {
                properties.put("default", bbieSc.valueConstraint().defaultValue());
            }
        }

        // Issue #692
        XbtSummaryRecord xbt = getXbt(bbieSc, dtSc);
        String exampleText = bbieSc.example();
        if (StringUtils.hasLength(exampleText)) {
            properties.put("example", exampleText);
        } else { // Issue #1405
            properties.put("example", emptyExample(xbt));
        }

        CodeListSummaryRecord codeList = generationContext.getCodeList(bbieSc);
        String ref;
        if (codeList != null) {
            ref = fillSchemas(schemas, bbieSc, codeList);
        } else {
            AgencyIdListSummaryRecord agencyIdList = generationContext.getAgencyIdList(bbieSc);
            if (agencyIdList != null) {
                ref = fillSchemas(schemas, agencyIdList);
            } else {
                // Issue #1633
                // Primitive types shouldn't be expressed in the 'schema' content.
                ref = null;
            }
        }

        if (ref == null) {
            Map<String, Object> content = applyFacet(toProperties(xbt), bbieSc.facet());
            properties.putAll(content);
        } else {
            properties.put("$ref", ref);
        }
        properties = allOf(properties);

        // Issue #1298
        if (bbieSc.deprecated() != null && bbieSc.deprecated()) {
            properties.put("deprecated", true);
        }

        ((Map<String, Object>) parent.get("properties")).put(name, properties);
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
        String extension = (expressionMapper.getFactory() instanceof YAMLFactory) ? "yml" : "json";

        tempFile = new File(tempFile.getParentFile(), filename + "." + extension);

        expressionMapper.writeValue(tempFile, root);
        logger.info("Open API " + OPEN_API_VERSION + " Schema is generated: " + tempFile);

        return tempFile;
    }
}
