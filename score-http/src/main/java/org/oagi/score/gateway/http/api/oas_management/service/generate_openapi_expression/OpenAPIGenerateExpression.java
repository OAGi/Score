package org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableMap;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BIE;
import org.oagi.score.gateway.http.api.bie_management.model.Facet;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.oagi.score.gateway.http.api.bie_management.service.generate_expression.Helper.*;
import static org.oagi.score.gateway.http.api.oas_management.model.Operation.*;
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

    private String getBieName(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        return getBieName(topLevelAsbiep, s -> convertIdentifierToId(camelCase(s)));
    }

    private String getBieName(TopLevelAsbiepSummaryRecord topLevelAsbiep, Function<String, String> replacer) {
        AsbiepSummaryRecord asbiep = generationContext.findASBIEP(topLevelAsbiep.asbiepId(), topLevelAsbiep);
        AsccpSummaryRecord basedAsccp = generationContext.getAsccp(asbiep.basedAsccpManifestId());
        return replacer.apply(basedAsccp.propertyTerm());
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
        public void proceed(TopLevelAsbiepSummaryRecord topLevelAsbiep,
                            OpenAPITemplateForVerbOption template,
                            Map<String, Object> path,
                            AsbiepSummaryRecord asbiep) {
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
        public void proceed(TopLevelAsbiepSummaryRecord topLevelAsbiep,
                            OpenAPITemplateForVerbOption template,
                            Map<String, Object> path,
                            AsbiepSummaryRecord asbiep) {
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
        public void proceed(TopLevelAsbiepSummaryRecord topLevelAsbiep,
                            OpenAPITemplateForVerbOption template,
                            Map<String, Object> path,
                            AsbiepSummaryRecord asbiep) {
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
        public void proceed(TopLevelAsbiepSummaryRecord topLevelAsbiep,
                            OpenAPITemplateForVerbOption template,
                            Map<String, Object> path,
                            AsbiepSummaryRecord asbiep) {
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
        AsbiepSummaryRecord asbiep = generationContext.findASBIEP(topLevelAsbiep.asbiepId(), topLevelAsbiep);
        generationContext.referenceCounter().increase(asbiep);
        try {
            AbieSummaryRecord typeAbie = generationContext.queryTargetABIE(asbiep);
            ReleaseDetailsRecord release = generationContext.findRelease(topLevelAsbiep.release().releaseId());

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
                        .put("x-oagis-license", StringUtils.hasLength(release.releaseLicense()) ? release.releaseLicense() : "");
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

    private String getOperationId(String operation, TopLevelAsbiepSummaryRecord topLevelAsbiep) {
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
        boolean isNillable = asbie.nillable();

        boolean reused = !asbie.ownerTopLevelAsbiepId().equals(asbiep.ownerTopLevelAsbiepId());
        if (reused) {
            SchemaReference ref = getReference(asbiep);
            properties.put("$ref", ref.getPath());
        } else {
            AbieSummaryRecord typeAbie = generationContext.queryTargetABIE(asbiep);
            AsccSummaryRecord ascc = generationContext.queryBasedASCC(asbie);

            if (minVal > 0) {
                List<String> parentRequired = (List<String>) parent.get("required");
                if (parentRequired == null) {
                    throw new IllegalStateException();
                }
                parentRequired.add(name);
            }

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
            String definition = abie.definition();
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
        boolean isNillable = bbie.nillable();

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

            for (BbieScSummaryRecord bbieSc : bbieScList) {
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
            AsccpSummaryRecord asccp = generationContext.queryBasedASCCP(asbiep);
            String propertyName = convertIdentifierToId(camelCase(asccp.propertyTerm()));
            schemaName = propertyName;
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
                if (bbie.facet() != null) {
                    XbtSummaryRecord xbt = getXbt(bbie, bdt);
                    ref = fillSchemas(schemas, xbt, bbie.guid(), bbie.facet());
                } else if (!isFriendly()) {
                    XbtSummaryRecord xbt = getXbt(bbie, bdt);
                    ref = fillSchemas(schemas, xbt);
                } else {
                    ref = null;
                }
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
                if (bbieSc.facet() != null) {
                    ref = fillSchemas(schemas, xbt, bbieSc.guid(), bbieSc.facet());
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

        File tempFile = File.createTempFile(ScoreGuidUtils.randomGuid(), null);
        String extension = (expressionMapper.getFactory() instanceof YAMLFactory) ? "yml" : "json";

        tempFile = new File(tempFile.getParentFile(), filename + "." + extension);

        expressionMapper.writeValue(tempFile, root);
        logger.info("Open API " + OPEN_API_VERSION + " Schema is generated: " + tempFile);

        return tempFile;
    }
}
