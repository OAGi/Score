package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.openapi;

import org.oagi.score.gateway.http.common.util.TempFiles;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.collect.ImmutableMap;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.bie_management.repository.BieQueryRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.TopLevelAsbiepQueryRepository;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.BieGenerateExpression;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.GenerationContext;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextSummaryRecord;
import org.oagi.score.gateway.http.api.oas_management.model.OpenAPIExpressionFormat;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseDetailsRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

import static org.oagi.score.gateway.http.api.bie_management.service.generate_expression.Helper.*;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * Generates OpenAPI 3.x expressions for selected top-level BIEs. The 3.0/3.1 differences
 * (version string, nullability, fixed value, example, built-in type map) live in {@link BieOpenApiDialect}.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class BieOpenAPIGenerateExpression implements BieGenerateExpression, InitializingBean {

    private final BieOpenApiDialect dialect;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private ObjectMapper expressionMapper;

    private Map<String, Object> root;
    private Map<TopLevelAsbiepId, String> reusedTopLevelAsbiepNameMap;
    private GenerateExpressionOption option;
    private ScoreUser requester;

    @Autowired
    private RepositoryFactory repositoryFactory;

    private BieQueryRepository query(ScoreUser requester) {
        return repositoryFactory.bieQueryRepository(requester);
    }

    @Autowired
    private ApplicationContext applicationContext;

    private GenerationContext generationContext;

    public BieOpenAPIGenerateExpression(BieOpenApiDialect dialect) {
        this.dialect = dialect;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        root = null;
        reusedTopLevelAsbiepNameMap = null;
    }

    @Override
    public void reset() throws Exception {
        this.afterPropertiesSet();
    }

    /**
     * Builds a generation context for top-level BIEs and optional OpenAPI template wrappers
     * (meta header / pagination response). Optional wrappers must belong to the same release.
     */
    @Override
    public GenerationContext generateContext(
            ScoreUser requester, List<TopLevelAsbiepSummaryRecord> topLevelAsbieps, GenerateExpressionOption option) {
        List<TopLevelAsbiepSummaryRecord> mergedTopLevelAsbieps = new ArrayList<>(topLevelAsbieps);

        if (mergedTopLevelAsbieps.isEmpty()) {
            throw new IllegalArgumentException("Cannot found BIEs.");
        }
        ReleaseId releaseId = mergedTopLevelAsbieps.get(0).release().releaseId();

        TopLevelAsbiepQueryRepository topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        appendOptionalTopLevelAsbiep(
                mergedTopLevelAsbieps,
                topLevelAsbiepQuery,
                option.isIncludeMetaHeaderForJsonForOpenAPI30GetTemplate(),
                option.getMetaHeaderTopLevelAsbiepIdForOpenAPI30GetTemplate(),
                releaseId,
                "Meta Header");
        appendOptionalTopLevelAsbiep(
                mergedTopLevelAsbieps,
                topLevelAsbiepQuery,
                option.isIncludeMetaHeaderForJsonForOpenAPI30PostTemplate(),
                option.getMetaHeaderTopLevelAsbiepIdForOpenAPI30PostTemplate(),
                releaseId,
                "Meta Header");
        appendOptionalTopLevelAsbiep(
                mergedTopLevelAsbieps,
                topLevelAsbiepQuery,
                option.isIncludePaginationResponseForJsonForOpenAPI30GetTemplate(),
                option.getPaginationResponseTopLevelAsbiepIdForOpenAPI30GetTemplate(),
                releaseId,
                "Pagination Response");

        return applicationContext.getBean(GenerationContext.class, requester, mergedTopLevelAsbieps);
    }

    @Override
    public void generate(ScoreUser requester, TopLevelAsbiepSummaryRecord topLevelAsbiep, GenerationContext generationContext, GenerateExpressionOption option) {
        this.generationContext = generationContext;
        this.option = option;
        this.requester = requester;

        OpenAPIExpressionFormat openAPIExpressionFormat;
        if (!StringUtils.hasLength(this.option.getOpenAPIExpressionFormat())) {
            openAPIExpressionFormat = OpenAPIExpressionFormat.YAML;
        } else {
            openAPIExpressionFormat = OpenAPIExpressionFormat.valueOf(this.option.getOpenAPIExpressionFormat());
        }

        switch (openAPIExpressionFormat) {
            case YAML:
                expressionMapper = YAMLMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build();
                break;
            case JSON:
                expressionMapper = JsonMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build();
                break;
        }

        generateTopLevelAsbiep(topLevelAsbiep);
    }

    private boolean isFriendly() {
        return this.option.isOpenAPICodeGenerationFriendly();
    }

    private String getPathName(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        // Issue #1308
        StringBuilder pathName = new StringBuilder();
        pathName.append("/");

        BusinessContextId bizCtxId = option.getBizCtxIds().get(topLevelAsbiep.topLevelAsbiepId());
        BusinessContextSummaryRecord bizCtx = generationContext.findBusinessContexts(topLevelAsbiep).stream()
                .filter(e -> e.businessContextId().equals(bizCtxId))
                .findAny().orElse(null);

        String bizCtxName = bizCtx.name();
        // RESTful Web API Design V2 document
        // [R85] For URI path segments, consisting of more than a single word, a hyphen character "-"
        // SHOULD be used to separate the words.
        String delimiter = "-";
        bizCtxName = bizCtxName.toLowerCase()
                .replaceAll("\\s", delimiter)
                .replaceAll("[^A-Za-z0-9]", delimiter);
        pathName.append(bizCtxName).append("/");

        String version = topLevelAsbiep.version();
        if (StringUtils.hasLength(version)) {
            version = version.toLowerCase()
                    .replaceAll("\\s", "")
                    .replaceAll("[^A-Za-z0-9]", delimiter);
            pathName.append(version).append("/");
        }

        String bieName = getBieName(topLevelAsbiep, s -> convertIdentifierToId(s
                .replaceAll("\\s", delimiter)
                .replaceAll("[^A-Za-z0-9]", delimiter)));
        pathName.append(bieName);

        // RESTful Web API Design V2 document
        // [R87] For the URI path, lower case letters SHOULD be used.
        return pathName.toString().toLowerCase();
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

    private String getBieName(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        return getBieName(topLevelAsbiep, s -> convertIdentifierToId(camelCase(s)));
    }

    private String getBieName(TopLevelAsbiepSummaryRecord topLevelAsbiep, Function<String, String> replacer) {
        AsbiepSummaryRecord asbiep = generationContext.findASBIEP(topLevelAsbiep.asbiepId(), topLevelAsbiep);
        AsccpSummaryRecord basedAsccp = generationContext.getAsccp(asbiep.basedAsccpManifestId());
        return replacer.apply(basedAsccp.propertyTerm());
    }

    private void generateTopLevelAsbiep(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        AsbiepSummaryRecord asbiep = generationContext.findASBIEP(topLevelAsbiep.asbiepId(), topLevelAsbiep);
        generationContext.referenceCounter().increase(asbiep);
        try {
            AbieSummaryRecord typeAbie = generationContext.queryTargetABIE(asbiep);
            ReleaseDetailsRecord release = generationContext.findRelease(topLevelAsbiep.release().releaseId());

            Map<String, Object> paths;
            Map<String, Object> schemas = new LinkedHashMap<>();
            Map<String, Object> securitySchemes = null;
            if (root == null) {
                root = new LinkedHashMap<>();
                root.put("openapi", dialect.openApiVersion());
                root.put("info", buildInfo(release));

                paths = new LinkedHashMap();
                securitySchemes = buildSecuritySchemes(topLevelAsbiep);

                root.put("paths", paths);
                root.put("components", ImmutableMap.<String, Object>builder()
                        .put("securitySchemes", securitySchemes)
                        .put("schemas", schemas)
                        .build()
                );
                reusedTopLevelAsbiepNameMap = new LinkedHashMap<>();
            } else {
                paths = (Map<String, Object>) root.get("paths");
                schemas = (Map<String, Object>) ((Map<String, Object>) root.get("components")).get("schemas");
                securitySchemes = (Map<String, Object>) ((Map<String, Object>) root.get("components")).get("securitySchemes");

                Map<String, Object> oauth2 = (Map<String, Object>) securitySchemes.get("OAuth2");
                Map<String, Object> flows = (Map<String, Object>) oauth2.get("flows");
                Map<String, Object> authorizationCode = (Map<String, Object>) flows.get("authorizationCode");
                Map<String, Object> scopes = (Map<String, Object>) authorizationCode.get("scopes");
                scopes.putAll(getAuthorizationCodeScopes(topLevelAsbiep));
            }

            BieOpenApiSchemaBuilder schemaBuilder = new BieOpenApiSchemaBuilder(generationContext, dialect,
                    BieOpenApiSchemaPolicy.STANDARD, option.isBieDefinition(), option.getScheme(), option.getHost(),
                    schemas, reusedTopLevelAsbiepNameMap);

            AsccpSummaryRecord basedAsccp = generationContext.getAsccp(asbiep.basedAsccpManifestId());
            Map<String, Object> path = new LinkedHashMap<>();
            path.put("summary", "");
            path.put("description", "");
            String bieName = getBieName(topLevelAsbiep);
            String pathName = getPathName(topLevelAsbiep);
            paths.put(pathName, path);

            boolean isDifferent = option.isGetTemplateAndPostTemplateOptionDifferent();

            if (option.isOpenAPI30GetTemplate()) {
                String schemaName;
                String prefix = "";
                // Issue #1302
                if (shouldPrefixGetSchemaName(isDifferent)) {
                    prefix = "get-";
                }
                if (isFriendly()) {
                    schemaName = prefix + bieName;
                } else {
                    schemaName = prefix + bieName + "-" + typeAbie.getGuid();
                }
                if (schemaName.equals(bieName)) {
                    option.setSuppressRootPropertyForOpenAPI30GetTemplate(true);
                }
                boolean isArray = option.isArrayForJsonExpressionForOpenAPI30GetTemplate();
                path.put("get", buildGetOperation(bieName, basedAsccp, topLevelAsbiep, schemaName, isArray));

                if (!isFriendly() && !schemas.containsKey("integer")) {
                    schemas.put("integer", ImmutableMap.<String, Object>builder()
                            .put("type", "integer")
                            .build());
                }

                if (!schemas.containsKey(schemaName)) {
                    Map<String, Object> properties = schemaBuilder.makeProperties(typeAbie, topLevelAsbiep);
                    fillPropertiesForGetTemplate(properties, schemaBuilder, asbiep, typeAbie);
                    schemas.put(schemaName, properties);
                }
                // Issue #1483
                if (isArray && !schemas.containsKey(schemaName + "List")) {
                    schemas.put(schemaName + "List", ImmutableMap.<String, Object>builder()
                            .put("type", "array")
                            .put("items", ImmutableMap.<String, Object>builder()
                                    .put("$ref", "#/components/schemas/" + schemaName)
                                    .build())
                            .build());
                }
            }

            if (option.isOpenAPI30PostTemplate()) {
                String schemaName;
                String prefix = "";
                // Issue #1302
                if (shouldPrefixPostSchemaName(isDifferent)) {
                    prefix = "post-";
                }
                if (isFriendly()) {
                    schemaName = prefix + bieName;
                } else {
                    schemaName = prefix + bieName + "-" + typeAbie.getGuid();
                }
                if (schemaName.equals(bieName)) {
                    option.setSuppressRootPropertyForOpenAPI30PostTemplate(true);
                }
                boolean isArray = option.isArrayForJsonExpressionForOpenAPI30PostTemplate();
                path.put("post", buildPostOperation(bieName, basedAsccp, topLevelAsbiep, schemaName, isArray));

                if (!schemas.containsKey(schemaName)) {
                    Map<String, Object> properties = schemaBuilder.makeProperties(typeAbie, topLevelAsbiep);
                    fillPropertiesForPostTemplate(properties, schemaBuilder, asbiep, typeAbie);
                    schemas.put(schemaName, properties);
                }

                // Issue #1483
                if (isArray && !schemas.containsKey(schemaName + "List")) {
                    schemas.put(schemaName + "List", ImmutableMap.<String, Object>builder()
                            .put("type", "array")
                            .put("items", ImmutableMap.<String, Object>builder()
                                    .put("$ref", "#/components/schemas/" + schemaName)
                                    .build())
                            .build());
                }
            }
        } finally {
            generationContext.referenceCounter().decrease(asbiep);
        }
    }

    private String getOperationId(String operation, TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        String controllerName = getBieName(topLevelAsbiep);
        String action = operation + Character.toUpperCase(controllerName.charAt(0)) + controllerName.substring(1);
        return controllerName + "_" + action;
    }

    private boolean shouldPrefixGetSchemaName(boolean isDifferent) {
        return isDifferent
                || option.isIncludeMetaHeaderForJsonForOpenAPI30GetTemplate()
                || option.isIncludePaginationResponseForJsonForOpenAPI30GetTemplate();
    }

    private boolean shouldPrefixPostSchemaName(boolean isDifferent) {
        return isDifferent || option.isIncludeMetaHeaderForJsonForOpenAPI30PostTemplate();
    }

    private void fillPropertiesForGetTemplate(Map<String, Object> parent,
                                              BieOpenApiSchemaBuilder schemaBuilder,
                                              AsbiepSummaryRecord asbiep, AbieSummaryRecord abie) {
        /*
         * Issue #587
         */
        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);

        if (option.isIncludeMetaHeaderForJsonForOpenAPI30GetTemplate()) {
            TopLevelAsbiepSummaryRecord metaHeaderTopLevelAsbiep =
                    topLevelAsbiepQuery.getTopLevelAsbiepSummary(option.getMetaHeaderTopLevelAsbiepIdForOpenAPI30GetTemplate());
            schemaBuilder.fillProperties(parent, metaHeaderTopLevelAsbiep);
        }
        if (option.isIncludePaginationResponseForJsonForOpenAPI30GetTemplate()) {
            TopLevelAsbiepSummaryRecord paginationResponseTopLevelAsbiep =
                    topLevelAsbiepQuery.getTopLevelAsbiepSummary(option.getPaginationResponseTopLevelAsbiepIdForOpenAPI30GetTemplate());
            schemaBuilder.fillProperties(parent, paginationResponseTopLevelAsbiep);
        }

        schemaBuilder.fillProperties(parent, asbiep, abie);

        // Issue #1317
        if (option.isSuppressRootPropertyForOpenAPI30GetTemplate()) {
            schemaBuilder.suppressRootProperty(parent);
        }
    }

    private void fillPropertiesForPostTemplate(Map<String, Object> parent,
                                               BieOpenApiSchemaBuilder schemaBuilder,
                                               AsbiepSummaryRecord asbiep, AbieSummaryRecord abie) {
        /*
         * Issue #587
         */
        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);

        if (option.isIncludeMetaHeaderForJsonForOpenAPI30PostTemplate()) {
            TopLevelAsbiepSummaryRecord metaHeaderTopLevelAsbiep =
                    topLevelAsbiepQuery.getTopLevelAsbiepSummary(option.getMetaHeaderTopLevelAsbiepIdForOpenAPI30PostTemplate());
            schemaBuilder.fillProperties(parent, metaHeaderTopLevelAsbiep);
        }

        schemaBuilder.fillProperties(parent, asbiep, abie);

        // Issue #1317
        if (option.isSuppressRootPropertyForOpenAPI30PostTemplate()) {
            schemaBuilder.suppressRootProperty(parent);
        }
    }

    private Map<String, Object> buildInfo(ReleaseDetailsRecord release) {
        return ImmutableMap.<String, Object>builder()
                .put("title", "")
                .put("description", "")
                .put("contact", ImmutableMap.<String, Object>builder()
                        .put("name", "")
                        .put("url", "")
                        .put("email", "example@example.org")
                        .build())
                .put("version", "")
                .put("x-oagis-license", StringUtils.hasLength(release.releaseLicense()) ? release.releaseLicense() : "")
                .build();
    }

    private Map<String, Object> buildSecuritySchemes(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        return ImmutableMap.<String, Object>builder()
                .put("OAuth2", ImmutableMap.<String, Object>builder()
                        .put("type", "oauth2")
                        .put("flows", ImmutableMap.<String, Object>builder()
                                .put("authorizationCode", ImmutableMap.<String, Object>builder()
                                        .put("authorizationUrl", "https://example.com/oauth/authorize")
                                        .put("tokenUrl", "https://example.com/oauth/token")
                                        .put("scopes", getAuthorizationCodeScopes(topLevelAsbiep))
                                        .build())
                                .build())
                        .build())
                .build();
    }

    private Map<String, Object> buildGetOperation(String bieName,
                                                    AsccpSummaryRecord basedAsccp,
                                                    TopLevelAsbiepSummaryRecord topLevelAsbiep,
                                                    String schemaName,
                                                    boolean isArray) {
        Map<String, Object> operation = ImmutableMap.<String, Object>builder()
                .put("summary", "")
                .put("description", "")
                .put("security", Arrays.asList(ImmutableMap.builder()
                        .put("OAuth2", Arrays.asList(bieName + "Read"))
                        .build()))
                .put("tags", Arrays.asList(basedAsccp.propertyTerm()))
                .put("operationId", getOperationId("get", topLevelAsbiep))
                .put("parameters", Arrays.asList(
                        ImmutableMap.<String, Object>builder()
                                .put("name", "id")
                                .put("in", "query")
                                .put("description", "")
                                .put("required", false)
                                .put("schema", (isFriendly()) ? ImmutableMap.<String, Object>builder()
                                        .put("type", "integer")
                                        .build() : ImmutableMap.<String, Object>builder()
                                        .put("$ref", "#/components/schemas/integer")
                                        .build())
                                .build()
                ))
                .put("responses", ImmutableMap.<String, Object>builder()
                        .put("200", ImmutableMap.<String, Object>builder()
                                .put("description", "")
                                .put("content", ImmutableMap.<String, Object>builder()
                                        .put("application/json", ImmutableMap.<String, Object>builder()
                                                .put("schema", ImmutableMap.<String, Object>builder()
                                                        .put("$ref", "#/components/schemas/" + ((isArray) ? schemaName + "List" : schemaName))
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();
        return new LinkedHashMap<>(operation);
    }

    private Map<String, Object> buildPostOperation(String bieName,
                                                     AsccpSummaryRecord basedAsccp,
                                                     TopLevelAsbiepSummaryRecord topLevelAsbiep,
                                                     String schemaName,
                                                     boolean isArray) {
        Map<String, Object> operation = ImmutableMap.<String, Object>builder()
                .put("summary", "")
                .put("description", "")
                .put("security", Arrays.asList(ImmutableMap.builder()
                        .put("OAuth2", Arrays.asList(bieName + "Write"))
                        .build()))
                .put("tags", Arrays.asList(basedAsccp.propertyTerm()))
                .put("operationId", getOperationId("create", topLevelAsbiep))
                .put("requestBody", ImmutableMap.<String, Object>builder()
                        .put("description", "")
                        .put("content", ImmutableMap.<String, Object>builder()
                                .put("application/json", ImmutableMap.<String, Object>builder()
                                        .put("schema", ImmutableMap.<String, Object>builder()
                                                .put("$ref", "#/components/schemas/" + ((isArray) ? schemaName + "List" : schemaName))
                                                .build())
                                        .build())
                                .build())
                        .build())
                .put("responses", ImmutableMap.<String, Object>builder()
                        .put("200", ImmutableMap.<String, Object>builder()
                                .put("description", "")
                                .put("content", ImmutableMap.<String, Object>builder()
                                        .put("application/json", ImmutableMap.<String, Object>builder()
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();
        return new LinkedHashMap<>(operation);
    }

    private void appendOptionalTopLevelAsbiep(
            List<TopLevelAsbiepSummaryRecord> mergedTopLevelAsbieps,
            TopLevelAsbiepQueryRepository topLevelAsbiepQuery,
            boolean enabled,
            TopLevelAsbiepId optionalTopLevelAsbiepId,
            ReleaseId expectedReleaseId,
            String label) {
        if (!enabled || optionalTopLevelAsbiepId == null) {
            return;
        }
        TopLevelAsbiepSummaryRecord optionalTopLevelAsbiep =
                topLevelAsbiepQuery.getTopLevelAsbiepSummary(optionalTopLevelAsbiepId);
        if (!expectedReleaseId.equals(optionalTopLevelAsbiep.release().releaseId())) {
            throw new IllegalArgumentException(label + " release does not match.");
        }
        mergedTopLevelAsbieps.add(optionalTopLevelAsbiep);
    }

    private void ensureRoot() {
        if (root == null) {
            throw new IllegalStateException();
        }
    }

    @Override
    public File asFile(String filename) throws IOException {
        ensureRoot();

        String extension = (expressionMapper instanceof YAMLMapper) ? "yml" : "json";

        File tempFile = TempFiles.newFile(filename + "." + extension);

        expressionMapper.writeValue(tempFile, root);
        logger.info("Open API " + dialect.openApiVersion() + " Schema is generated: " + tempFile);

        return tempFile;
    }
}
