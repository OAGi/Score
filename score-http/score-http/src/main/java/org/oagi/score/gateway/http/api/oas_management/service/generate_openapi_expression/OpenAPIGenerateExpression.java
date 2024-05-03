package org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableMap;
import org.oagi.score.common.util.OagisComponentType;
import org.oagi.score.data.*;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.GenerationContext;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.Helper;
import org.oagi.score.gateway.http.api.oas_management.data.OpenAPIExpressionFormat;
import org.oagi.score.gateway.http.api.oas_management.data.OpenAPIGenerateExpressionOption;
import org.oagi.score.gateway.http.api.oas_management.data.OpenAPITemplateForVerbOption;
import org.oagi.score.gateway.http.api.oas_management.data.Operation;
import org.oagi.score.gateway.http.helper.ScoreGuid;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.oagi.score.gateway.http.api.bie_management.service.generate_expression.Helper.*;
import static org.oagi.score.gateway.http.api.oas_management.data.Operation.*;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;


@Component
@Scope(SCOPE_PROTOTYPE)
public class OpenAPIGenerateExpression implements BieGenerateOpenApiExpression, InitializingBean {

    private static final String OPEN_API_VERSION = "3.0.3";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private GenerationContext generationContext;
    private OpenAPIGenerateExpressionOption option;

    private Map<Operation, GeneratorForOperation> generatorForOperationMap = new HashMap<>();
    private ObjectMapper mapper;
    private ObjectMapper expressionMapper;

    private Map<String, Object> root;
    private Map<String, Object> schemas = new LinkedHashMap<>();

    public OpenAPIGenerateExpression(GenerationContext generationContext, OpenAPIGenerateExpressionOption option) {
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
    public void generate(TopLevelAsbiep topLevelAsbiep) {
        generateTopLevelAsbiep(topLevelAsbiep);
    }

    @Override
    public void generate(OpenAPITemplateForVerbOption template) {
        generateTemplate(template);
    }

    private boolean isFriendly() {
        return true;
    }

    private Map<String, Object> getAuthorizationCodeScopes(TopLevelAsbiep topLevelAsbiep) {
        ASBIEP asbiep = generationContext.findASBIEP(topLevelAsbiep.getAsbiepId(), topLevelAsbiep);
        ASCCP basedAsccp = generationContext.findASCCP(asbiep.getBasedAsccpManifestId());
        String bieName = getBieName(topLevelAsbiep);
        Map<String, Object> scopes = new LinkedHashMap<>();
        scopes.put(bieName + "Read", "Allows " + basedAsccp.getPropertyTerm() + " data to be read");
        scopes.put(bieName + "Write", "Allows " + basedAsccp.getPropertyTerm() + " data to be written");
        return scopes;
    }

    private String getBieName(TopLevelAsbiep topLevelAsbiep) {
        return getBieName(topLevelAsbiep, s -> convertIdentifierToId(camelCase(s)));
    }

    private String getBieName(TopLevelAsbiep topLevelAsbiep, Function<String, String> replacer) {
        ASBIEP asbiep = generationContext.findASBIEP(topLevelAsbiep.getAsbiepId(), topLevelAsbiep);
        ASCCP basedAsccp = generationContext.findASCCP(asbiep.getBasedAsccpManifestId());
        return replacer.apply(basedAsccp.getPropertyTerm());
    }

    private List<Map<String, Object>> buildParameters(Operation verb, boolean isArray, boolean hasId) {
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

        if (hasId) {
            parameters.add(ImmutableMap.<String, Object>builder()
                    .put("name", "id")
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

    private void fillPropertiesForTemplate(String schemaName,
                                           ASBIEP asbiep,
                                           boolean isArray, boolean isSuppressRoot) {
        if (schemas.containsKey(schemaName)) {
            return;
        }

        if (isArray) {
            String itemRefSchemaName;
            // Issue #1603
            // If the schema name ends with 'List' without any additional options,
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

        void proceed(TopLevelAsbiep topLevelAsbiep,
                     OpenAPITemplateForVerbOption template,
                     Map<String, Object> path,
                     ASBIEP asbiep);
    }

    private class GetGeneratorForOperation implements GeneratorForOperation {
        @Override
        public Operation getOperation() {
            return GET;
        }

        @Override
        public void proceed(TopLevelAsbiep topLevelAsbiep,
                            OpenAPITemplateForVerbOption template,
                            Map<String, Object> path,
                            ASBIEP asbiep) {
            if (path != null && path.size() > 0) {

            } else {
                boolean isArray = template.isArrayForJsonExpression();
                String schemaName = template.getSchemaName();
                boolean isSuppressRoot = template.isSuppressRootProperty();
                String bieName = getBieName(topLevelAsbiep);
                String pathName = template.getResourceName();
                boolean hasId = pathName.contains("{id}");

                path.put("summary", "");
                path.put("description", "");
                path.put("security", Arrays.asList(ImmutableMap.builder()
                        .put("OAuth2", Arrays.asList(bieName + "Read"))
                        .build()));
                if (template.getTagName() != null) {
                    path.put("tags", Arrays.asList(template.getTagName()));
                }
                path.put("operationId", template.getOperationId());
                path.put("parameters", buildParameters(GET, isArray, hasId));
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
        public void proceed(TopLevelAsbiep topLevelAsbiep,
                            OpenAPITemplateForVerbOption template,
                            Map<String, Object> path,
                            ASBIEP asbiep) {
            if (path != null && path.size() > 0) {
                boolean isArray = template.isArrayForJsonExpression();
                String schemaName = template.getSchemaName();
                boolean isSuppressRoot = template.isSuppressRootProperty();
                if (template.getTagName() != null && !path.containsKey("tags")) {
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
                String pathName = template.getResourceName();

                path.put("summary", "");
                path.put("description", "");
                path.put("security", Arrays.asList(ImmutableMap.builder()
                        .put("OAuth2", Arrays.asList(bieName + "Write"))
                        .build()));
                if (template.getTagName() != null) {
                    path.put("tags", Arrays.asList(template.getTagName()));
                }
                path.put("operationId", template.getOperationId());
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
        public void proceed(TopLevelAsbiep topLevelAsbiep,
                            OpenAPITemplateForVerbOption template,
                            Map<String, Object> path,
                            ASBIEP asbiep) {
            if (path != null && path.size() > 0) {
                boolean isArray = template.isArrayForJsonExpression();
                String schemaName = template.getSchemaName();
                boolean isSuppressRoot = template.isSuppressRootProperty();
                if (template.getTagName() != null && !path.containsKey("tags")) {
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
                String pathName = template.getResourceName();
                boolean hasId = pathName.contains("{id}");

                path.put("summary", "");
                path.put("description", "");
                path.put("security", Arrays.asList(ImmutableMap.builder()
                        .put("OAuth2", Arrays.asList(bieName + "Write"))
                        .build()));
                if (template.getTagName() != null) {
                    path.put("tags", Arrays.asList(template.getTagName()));
                }
                path.put("operationId", template.getOperationId());
                path.put("parameters", buildParameters(getOperation(), isArray, hasId));
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
        public void proceed(TopLevelAsbiep topLevelAsbiep,
                            OpenAPITemplateForVerbOption template,
                            Map<String, Object> path,
                            ASBIEP asbiep) {
            if (path != null && path.size() > 0) {
                boolean isArray = template.isArrayForJsonExpression();
                String schemaName = template.getSchemaName();
                boolean isSuppressRoot = template.isSuppressRootProperty();
                if (template.getTagName() != null && !path.containsKey("tags")) {
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
                String pathName = template.getResourceName();

                path.put("summary", "");
                path.put("description", "");
                path.put("security", Arrays.asList(ImmutableMap.builder()
                        .put("OAuth2", Arrays.asList(bieName + "Write"))
                        .build()));
                if (template.getTagName() != null) {
                    path.put("tags", Arrays.asList(template.getTagName()));
                }
                path.put("operationId", template.getOperationId());
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
        public void proceed(TopLevelAsbiep topLevelAsbiep,
                            OpenAPITemplateForVerbOption template,
                            Map<String, Object> path,
                            ASBIEP asbiep) {
            if (path != null && path.size() > 0) {

            } else {
                boolean isArray = template.isArrayForJsonExpression();
                String schemaName = template.getSchemaName();
                boolean isSuppressRoot = template.isSuppressRootProperty();
                String bieName = getBieName(topLevelAsbiep);
                String pathName = template.getResourceName();

                path.put("summary", "");
                path.put("description", "");
                path.put("security", Arrays.asList(ImmutableMap.builder()
                        .put("OAuth2", Arrays.asList(bieName + "Write"))
                        .build()));
                if (template.getTagName() != null) {
                    path.put("tags", Arrays.asList(template.getTagName()));
                }
                path.put("operationId", template.getOperationId());
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

    private void generateTopLevelAsbiep(TopLevelAsbiep topLevelAsbiep) {
        ASBIEP asbiep = generationContext.findASBIEP(topLevelAsbiep.getAsbiepId(), topLevelAsbiep);
        generationContext.referenceCounter().increase(asbiep);
        try {
            getReference(asbiep);
        } finally {
            generationContext.referenceCounter().decrease(asbiep);
        }
    }

    private void generateTemplate(OpenAPITemplateForVerbOption template) {
        TopLevelAsbiep topLevelAsbiep = template.getTopLevelAsbiep();
        ASBIEP asbiep = generationContext.findASBIEP(topLevelAsbiep.getAsbiepId(), topLevelAsbiep);
        generationContext.referenceCounter().increase(asbiep);
        try {
            ABIE typeAbie = generationContext.queryTargetABIE(asbiep);
            Release release = generationContext.findRelease(topLevelAsbiep.getReleaseId());

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
                        .put("x-oagis-license", StringUtils.hasLength(release.getReleaseLicense()) ? release.getReleaseLicense() : "");
                root.put("info", infoBuilder.build());

                paths = new LinkedHashMap();
                securitySchemes = ImmutableMap.<String, Object>builder()
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

                root.put("paths", paths);
                root.put("components", ImmutableMap.<String, Object>builder()
                        .put("securitySchemes", securitySchemes)
                        .put("schemas", schemas)
                        .build()
                );
            } else {
                paths = (Map<String, Object>) root.get("paths");
                securitySchemes = (Map<String, Object>) ((Map<String, Object>) root.get("components")).get("securitySchemes");

                Map<String, Object> oauth2 = (Map<String, Object>) securitySchemes.get("OAuth2");
                Map<String, Object> flows = (Map<String, Object>) oauth2.get("flows");
                Map<String, Object> authorizationCode = (Map<String, Object>) flows.get("authorizationCode");
                Map<String, Object> scopes = (Map<String, Object>) authorizationCode.get("scopes");
                scopes.putAll(getAuthorizationCodeScopes(topLevelAsbiep));
            }

            Map<String, Object> pathMap = new LinkedHashMap<>();
            Map<String, Object> path = new LinkedHashMap();
            String pathName = template.getResourceName();
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

            GeneratorForOperation generatorForOperation = generatorForOperationMap.get(template.getVerbOption());
            if (generatorForOperation == null) {
                throw new UnsupportedOperationException("Unsupported Operation: " + template.getVerbOption());
            }
            generatorForOperation.proceed(topLevelAsbiep, template, path, asbiep);
        } finally {
            generationContext.referenceCounter().decrease(asbiep);
        }
    }

    private Map<String, Object> makeProperties(ABIE typeAbie, TopLevelAsbiep topLevelAsbiep) {
        ASBIEP asbiep = generationContext.findASBIEP(topLevelAsbiep.getAsbiepId(), topLevelAsbiep);
        ASCCP asccp = generationContext.findASCCP(asbiep.getBasedAsccpManifestId());
        Map<String, Object> properties = new LinkedHashMap();
        // Issue #1148
        properties.put("x-oagis-bie-guid", asbiep.getGuid());
        properties.put("x-oagis-bie-date-time", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(typeAbie.getLastUpdateTimestamp()));
        properties.put("x-oagis-bie-version", StringUtils.hasLength(topLevelAsbiep.getVersion()) ? topLevelAsbiep.getVersion() : "");
        // Issue #1603
        properties.put("x-oagis-bie-den", asccp.getPropertyTerm());
        // Issue #1574
        Release release = generationContext.findRelease(topLevelAsbiep.getReleaseId());
        properties.put("x-oagis-bie-uri", option.getScheme() + "://" + option.getHost() + "/profile_bie/" + topLevelAsbiep.getTopLevelAsbiepId().toString());
        properties.put("x-oagis-release", release.getReleaseNum());
        properties.put("x-oagis-release-date", new SimpleDateFormat("yyyy-MM-dd").format(release.getLastUpdateTimestamp()));
        properties.put("required", new ArrayList());
        properties.put("additionalProperties", false);

        return properties;
    }

    private String getOperationId(String operation, TopLevelAsbiep topLevelAsbiep) {
        String controllerName = getBieName(topLevelAsbiep);
        String action = operation + Character.toUpperCase(controllerName.charAt(0)) + controllerName.substring(1);
        return controllerName + "_" + action;
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
                                ASBIE asbie,
                                GenerationContext generationContext) {

        Map<String, Object> properties = new LinkedHashMap();
        if (!parent.containsKey("properties")) {
            parent.put("properties", new LinkedHashMap<String, Object>());
        }

        ASBIEP asbiep = generationContext.queryAssocToASBIEP(asbie);
        ASCCP asccp = generationContext.queryBasedASCCP(asbiep);
        String name = convertIdentifierToId(camelCase(asccp.getPropertyTerm()));

        int minVal = asbie.getCardinalityMin();
        int maxVal = asbie.getCardinalityMax();
        // Issue #562
        boolean isArray = (maxVal < 0 || maxVal > 1);
        boolean isNillable = asbie.isNillable();

        boolean reused = !asbie.getOwnerTopLevelAsbiepId().equals(asbiep.getOwnerTopLevelAsbiepId());
        if (reused) {
            SchemaReference ref = getReference(asbiep);
            properties.put("$ref", ref.getPath());
        } else {
            ABIE typeAbie = generationContext.queryTargetABIE(asbiep);
            ASCC ascc = generationContext.queryBasedASCC(asbie);

            if (minVal > 0) {
                List<String> parentRequired = (List<String>) parent.get("required");
                if (parentRequired == null) {
                    throw new IllegalStateException();
                }
                parentRequired.add(name);
            }

            if (option.isBieDefinition()) {
                String definition = asbie.getDefinition();
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
                properties = makeGlobalPropertyIfArray(schemas, name, properties);
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
                                ASBIEP asbiep, ABIE abie,
                                GenerationContext generationContext) {

        ASCCP asccp = generationContext.queryBasedASCCP(asbiep);
        String name = convertIdentifierToId(camelCase(asccp.getPropertyTerm()));

        List<String> parentRequired = (List<String>) parent.get("required");
        parentRequired.add(name);

        Map<String, Object> properties = new LinkedHashMap();
        if (!parent.containsKey("properties")) {
            parent.put("properties", new LinkedHashMap<String, Object>());
        }

        if (option.isBieDefinition()) {
            String definition = abie.getDefinition();
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

    private Map<String, Object> toProperties(Xbt xbt) {
        String openapi30Map = xbt.getOpenapi30Map();
        try {
            return mapper.readValue(openapi30Map, LinkedHashMap.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String fillSchemas(Map<String, Object> schemas,
                               Xbt xbt, FacetRestrictionsAware facetRestri) {
        String guid = (facetRestri instanceof BIE) ? ((BIE) facetRestri).getGuid() : ScoreGuid.randomGuid();
        String name = "type_" + guid;

        Map<String, Object> content = toProperties(xbt);
        if (facetRestri.getFacetMinLength() != null) {
            content.put("minLength", facetRestri.getFacetMinLength().longValue());
        }
        if (facetRestri.getFacetMaxLength() != null) {
            content.put("maxLength", facetRestri.getFacetMaxLength().longValue());
        }
        if (StringUtils.hasLength(facetRestri.getFacetPattern())) {
            // Override 'pattern' and 'format' properties
            content.remove("pattern");
            content.remove("format");
            content.put("pattern", facetRestri.getFacetPattern());
        }

        schemas.put(name, content);

        return "#/components/schemas/" + name;
    }

    private String fillSchemas(Map<String, Object> schemas,
                               Xbt xbt) {
        String builtInType = xbt.getBuiltinType();
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
                               BBIE bbie, CodeList codeList) {
        BdtPriRestri bdtPriRestri =
                generationContext.findBdtPriRestriByBbieAndDefaultIsTrue(bbie);

        Map<String, Object> properties;
        if (bdtPriRestri.getCodeListManifestId() != null) {
            properties = new LinkedHashMap();
            properties.put("type", "string");
        } else {
            CdtAwdPriXpsTypeMap cdtAwdPriXpsTypeMap =
                    generationContext.findCdtAwdPriXpsTypeMap(bdtPriRestri.getCdtAwdPriXpsTypeMapId());
            Xbt xbt = generationContext.findXbt(cdtAwdPriXpsTypeMap.getXbtId());
            properties = toProperties(xbt);
        }

        return fillSchemas(properties, schemas, codeList);
    }

    private String fillSchemas(Map<String, Object> schemas,
                               BBIESC bbieSc, CodeList codeList) {
        BdtScPriRestri bdtScPriRestri =
                generationContext.findBdtScPriRestriByBbieScAndDefaultIsTrue(bbieSc);

        Map<String, Object> properties;
        if (bdtScPriRestri.getCodeListManifestId() != null) {
            properties = new LinkedHashMap();
            properties.put("type", "string");
        } else {
            CdtScAwdPriXpsTypeMap cdtScAwdPriXpsTypeMap =
                    generationContext.findCdtScAwdPriXpsTypeMap(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId());
            Xbt xbt = generationContext.findXbt(cdtScAwdPriXpsTypeMap.getXbtId());
            properties = toProperties(xbt);
        }

        return fillSchemas(properties, schemas, codeList);
    }

    private String fillSchemas(Map<String, Object> properties,
                               Map<String, Object> schemas,
                               CodeList codeList) {

        AgencyIdListValue agencyIdListValue = generationContext.findAgencyIdListValue(codeList.getAgencyIdListValueManifestId());
        String codeListName = Helper.getCodeListTypeName(codeList, agencyIdListValue);
        /*
         * Issue #589
         */
        codeListName = Stream.of(codeListName.split("_"))
                .map(e -> convertIdentifierToId(camelCase(e))).collect(Collectors.joining("_"));

        if (!schemas.containsKey(codeListName)) {
            List<CodeListValue> codeListValues = generationContext.getCodeListValues(codeList);
            List<String> enumerations = codeListValues.stream().map(e -> e.getValue()).collect(Collectors.toList());
            if (!enumerations.isEmpty()) {
                properties.put("enum", enumerations);
            }

            schemas.put(codeListName, properties);
        }

        return "#/components/schemas/" + codeListName;
    }

    private String fillSchemas(Map<String, Object> schemas,
                               AgencyIdList agencyIdList) {
        AgencyIdListValue agencyIdListValue =
                generationContext.findAgencyIdListValue(agencyIdList.getAgencyIdListValueManifestId());
        String agencyListTypeName = Helper.getAgencyListTypeName(agencyIdList, agencyIdListValue);
        /*
         * Issue #589
         */
        agencyListTypeName = Stream.of(agencyListTypeName.split("_"))
                .map(e -> convertIdentifierToId(camelCase(e))).collect(Collectors.joining("_"));
        if (!schemas.containsKey(agencyListTypeName)) {
            Map<String, Object> properties = new LinkedHashMap();
            properties.put("type", "string");

            List<AgencyIdListValue> agencyIdListValues =
                    generationContext.findAgencyIdListValueByAgencyIdListManifestId(agencyIdList.getAgencyIdListManifestId());
            List<String> enumerations = agencyIdListValues.stream().map(e -> e.getValue()).collect(Collectors.toList());
            if (!enumerations.isEmpty()) {
                properties.put("enum", enumerations);
            }

            schemas.put(agencyListTypeName, properties);
        }

        return "#/components/schemas/" + agencyListTypeName;
    }

    private void fillProperties(Map<String, Object> parent,
                                Map<String, Object> schemas,
                                ABIE abie,
                                GenerationContext generationContext) {

        List<BIE> children = generationContext.queryChildBIEs(abie);
        ACC acc = generationContext.findACC(abie.getBasedAccManifestId());
        if (OagisComponentType.Choice.getValue() == acc.getOagisComponentType()) {
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
        if (bie instanceof BBIE) {
            BBIE bbie = (BBIE) bie;
            fillProperties(parent, schemas, bbie, generationContext);
        } else {
            ASBIE asbie = (ASBIE) bie;
            if (Helper.isAnyProperty(asbie, generationContext)) {
                parent.put("additionalProperties", true);
            } else {
                ASBIEP asbiep = generationContext.queryAssocToASBIEP(asbie);

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
                                BBIE bbie,
                                GenerationContext generationContext) {
        BCC bcc = generationContext.queryBasedBCC(bbie);
        BCCP bccp = generationContext.queryToBCCP(bcc);
        DT bdt = generationContext.queryBDT(bccp);

        int minVal = bbie.getCardinalityMin();
        int maxVal = bbie.getCardinalityMax();
        // Issue #562
        boolean isArray = (maxVal < 0 || maxVal > 1);
        boolean isNillable = bbie.isNillable();

        String name = convertIdentifierToId(camelCase(bccp.getPropertyTerm()));

        Map<String, Object> properties = new LinkedHashMap();
        if (!parent.containsKey("properties")) {
            parent.put("properties", new LinkedHashMap<String, Object>());
        }

        if (option.isBieDefinition()) {
            String definition = bbie.getDefinition();
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
        if (StringUtils.hasLength(bbie.getFixedValue())) {
            properties.put("enum", Arrays.asList(bbie.getFixedValue()));
        } else if (StringUtils.hasLength(bbie.getDefaultValue())) {
            properties.put("default", bbie.getDefaultValue());
        }

        // Issue #692
        Xbt xbt = getXbt(bbie, bdt);
        String exampleText = bbie.getExample();
        if (StringUtils.hasLength(exampleText)) {
            properties.put("example", exampleText);
        } else { // Issue #1405
            properties.put("example", emptyExample(xbt));
        }

        // Issue #564
        String ref = getReference(schemas, bbie, bdt, generationContext);
        List<BBIESC> bbieScList = generationContext.queryBBIESCs(bbie)
                .stream().filter(e -> e.getCardinalityMax() != 0).collect(Collectors.toList());
        if (bbieScList.isEmpty()) {
            if (ref == null && isFriendly()) {
                Map<String, Object> content = toProperties(xbt);
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
            if (ref == null && isFriendly()) {
                Map<String, Object> content = toProperties(xbt);
                contentProperties.putAll(content);
            } else {
                contentProperties.put("$ref", ref);
            }
            for (String key : Arrays.asList("enum", "default", "example")) {
                if (properties.containsKey(key)) {
                    contentProperties.put(key, properties.remove(key));
                }
            }

            ((List<String>) properties.get("required")).add("content");
            ((Map<String, Object>) properties.get("properties"))
                    .put("content", oneOf(allOf(contentProperties), isNillable));

            for (BBIESC bbieSc : bbieScList) {
                fillProperties(properties, schemas, bbieSc, generationContext);
            }
        }

        if (isArray) {
            String description = (String) properties.remove("description");
            Map<String, Object> items = new LinkedHashMap(properties);
            properties = new LinkedHashMap();
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
        }

        ((Map<String, Object>) parent.get("properties")).put(name, properties);
    }

    private Object emptyExample(Xbt xbt) {
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

    private Xbt getXbt(BBIE bbie, DT bdt) {
        if (bbie.getBdtPriRestriId() == null) {
            BdtPriRestri bdtPriRestri =
                    generationContext.findBdtPriRestriByBbieAndDefaultIsTrue(bbie);
            return Helper.getXbt(generationContext, bdtPriRestri);
        } else {
            BdtPriRestri bdtPriRestri =
                    generationContext.findBdtPriRestri(bbie.getBdtPriRestriId());
            return Helper.getXbt(generationContext, bdtPriRestri);
        }
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

    private SchemaReference getReference(ASBIEP asbiep) {
        return getReference(asbiep, null, true);
    }

    private SchemaReference getReference(ASBIEP asbiep,
                                         String schemaName,
                                         boolean suppressRootProperty) {
        if (schemaName == null) {
            ASCCP asccp = generationContext.queryBasedASCCP(asbiep);
            String propertyName = convertIdentifierToId(camelCase(asccp.getPropertyTerm()));
            schemaName = propertyName;
        }

        TopLevelAsbiep refTopLevelAsbiep = generationContext.findTopLevelAsbiep(asbiep.getOwnerTopLevelAsbiepId());
        Map<String, Object> properties;
        if (!schemas.containsKey(schemaName)) {
            ABIE typeAbie = generationContext.queryTargetABIE(asbiep);
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
                schemaName = schemaName + refTopLevelAsbiep.getTopLevelAsbiepId();
                ABIE typeAbie = generationContext.queryTargetABIE(asbiep);
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

    private String getReference(Map<String, Object> schemas, BBIE bbie, DT bdt,
                                GenerationContext generationContext) {
        CodeList codeList = Helper.getCodeList(generationContext, bbie, bdt);
        String ref;
        if (codeList != null) {
            ref = fillSchemas(schemas, bbie, codeList);
        } else {
            AgencyIdList agencyIdList = generationContext.getAgencyIdList(bbie);
            if (agencyIdList != null) {
                ref = fillSchemas(schemas, agencyIdList);
            } else {
                if (bbie.getFacetMinLength() != null || bbie.getFacetMaxLength() != null || StringUtils.hasLength(bbie.getFacetPattern())) {
                    Xbt xbt = getXbt(bbie, bdt);
                    ref = fillSchemas(schemas, xbt, bbie);
                } else if (!isFriendly()) {
                    Xbt xbt = getXbt(bbie, bdt);
                    ref = fillSchemas(schemas, xbt);
                } else {
                    ref = null;
                }
            }
        }

        return ref;
    }

    private Xbt getXbt(BBIESC bbieSc, DTSC dtSc) {
        if (bbieSc.getDtScPriRestriId() == null) {
            BdtScPriRestri bdtScPriRestri =
                    generationContext.findBdtScPriRestriByBbieScAndDefaultIsTrue(bbieSc);
            CdtScAwdPriXpsTypeMap cdtScAwdPriXpsTypeMap =
                    generationContext.findCdtScAwdPriXpsTypeMap(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId());
            return generationContext.findXbt(cdtScAwdPriXpsTypeMap.getXbtId());
        } else {
            BdtScPriRestri bdtScPriRestri =
                    generationContext.findBdtScPriRestri(bbieSc.getDtScPriRestriId());
            CdtScAwdPriXpsTypeMap cdtScAwdPriXpsTypeMap =
                    generationContext.findCdtScAwdPriXpsTypeMap(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId());
            return generationContext.findXbt(cdtScAwdPriXpsTypeMap.getXbtId());
        }
    }

    private void fillProperties(Map<String, Object> parent,
                                Map<String, Object> schemas,
                                BBIESC bbieSc,
                                GenerationContext generationContext) {
        int minVal = bbieSc.getCardinalityMin();
        int maxVal = bbieSc.getCardinalityMax();
        if (maxVal == 0) {
            return;
        }

        DTSC dtSc = generationContext.findDtSc(bbieSc.getBasedDtScManifestId());
        String name = convertIdentifierToId(toName(dtSc.getPropertyTerm(), dtSc.getRepresentationTerm(), rt -> {
            if ("Text".equals(rt)) {
                return "";
            }
            return rt;
        }, true));
        Map<String, Object> properties = new LinkedHashMap();

        if (option.isBieDefinition()) {
            String definition = bbieSc.getDefinition();
            if (StringUtils.hasLength(definition)) {
                properties.put("description", definition);
            }
        }

        if (minVal > 0) {
            ((List<String>) parent.get("required")).add(name);
        }

        // Issue #596
        if (StringUtils.hasLength(bbieSc.getFixedValue())) {
            properties.put("enum", Arrays.asList(bbieSc.getFixedValue()));
        } else if (StringUtils.hasLength(bbieSc.getDefaultValue())) {
            properties.put("default", bbieSc.getDefaultValue());
        }

        // Issue #692
        Xbt xbt = getXbt(bbieSc, dtSc);
        String exampleText = bbieSc.getExample();
        if (StringUtils.hasLength(exampleText)) {
            properties.put("example", exampleText);
        } else { // Issue #1405
            properties.put("example", emptyExample(xbt));
        }

        CodeList codeList = generationContext.getCodeList(bbieSc);
        String ref;
        if (codeList != null) {
            ref = fillSchemas(schemas, bbieSc, codeList);
        } else {
            AgencyIdList agencyIdList = generationContext.getAgencyIdList(bbieSc);
            if (agencyIdList != null) {
                ref = fillSchemas(schemas, agencyIdList);
            } else {
                if (bbieSc.getFacetMinLength() != null || bbieSc.getFacetMaxLength() != null || StringUtils.hasLength(bbieSc.getFacetPattern())) {
                    ref = fillSchemas(schemas, xbt, bbieSc);
                } else if (!isFriendly()) {
                    ref = fillSchemas(schemas, xbt);
                } else {
                    ref = null;
                }
            }
        }

        if (ref == null && isFriendly()) {
            Map<String, Object> content = toProperties(xbt);
            properties.putAll(content);
        } else {
            properties.put("$ref", ref);
        }
        properties = allOf(properties);

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

        File tempFile = File.createTempFile(ScoreGuid.randomGuid(), null);
        String extension = (expressionMapper.getFactory() instanceof YAMLFactory) ? "yml" : "json";

        tempFile = new File(tempFile.getParentFile(), filename + "." + extension);

        expressionMapper.writeValue(tempFile, root);
        logger.info("Open API " + OPEN_API_VERSION + " Schema is generated: " + tempFile);

        return tempFile;
    }
}
