package org.oagi.score.gateway.http.api.oas_management.service;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.expression.BieGenerateExpressionResult;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.BieGenerateFailureException;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.GenerationContext;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.oas_management.model.OpenAPIGenerateExpressionOption;
import org.oagi.score.gateway.http.api.oas_management.model.OpenAPITemplateForVerbOption;
import org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression.BieGenerateOpenApiExpression;
import org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression.OpenAPIGenerateExpression;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.api.bie_management.service.generate_expression.Helper.camelCase;
import static org.oagi.score.gateway.http.api.bie_management.service.generate_expression.Helper.convertIdentifierToId;

@Service
@Transactional(readOnly = true)
public class OpenAPIGenerateService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private ApplicationContext applicationContext;

    public BieGenerateExpressionResult generate(
            ScoreUser requester, OpenAPIGenerateExpressionOption openAPIGenerateExpressionOption) throws BieGenerateFailureException {
        File file = generateSchemaForAll(requester, openAPIGenerateExpressionOption);
        return toResult(file);
    }

    public BieGenerateExpressionResult toResult(File file) {
        String filename = file.getName();
        String contentType;
        if (filename.endsWith(".xsd")) {
            contentType = "text/xml";
        } else if (filename.endsWith(".json")) {
            contentType = "application/json";
        } else if (filename.endsWith(".zip")) {
            contentType = "application/zip";
        } else if (filename.endsWith(".yml")) {
            contentType = "text/x-yaml";
        } else {
            contentType = "application/octet-stream";
        }

        return new BieGenerateExpressionResult(filename, contentType, file);
    }

    public File generateSchemaForAll(ScoreUser requester, OpenAPIGenerateExpressionOption option) throws BieGenerateFailureException {
        // leave metaHeader and pagination response untouched at this time for OpenAPI generation
        // need to pass the params
        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        List<TopLevelAsbiepSummaryRecord> topLevelAsbieps = option.getTopLevelAsbiepIdSet().stream()
                .map(e -> topLevelAsbiepQuery.getTopLevelAsbiepSummary(e))
                .collect(Collectors.toList());
        Map<TopLevelAsbiepId, TopLevelAsbiepSummaryRecord> topLevelAsbiepMap =
                topLevelAsbieps.stream().collect(Collectors.toMap(TopLevelAsbiepSummaryRecord::topLevelAsbiepId, Function.identity()));
        for (OpenAPITemplateForVerbOption template : option.getTemplates()) {
            template.setTopLevelAsbiep(topLevelAsbiepMap.get(template.getTopLevelAsbiepId()));
        }

        long millis = System.currentTimeMillis();
        String filename;
        if (StringUtils.hasLength(option.getOasDoc().getVersion())) {
            filename = option.getOasDoc().getTitle() + "-" + option.getOasDoc().getVersion() + "-" + millis;
        } else {
            filename = option.getOasDoc().getTitle() + "-" + millis;
        }

        GenerationContext generationContext = generateContext(requester, topLevelAsbieps);
        BieGenerateOpenApiExpression generateExpression = createBieGenerateOpenAPIExpression(generationContext, option);
        for (TopLevelAsbiepSummaryRecord refTopLevelAsbiep : generationContext.getRefTopLevelAsbiepSet()) {
            generateExpression.generate(refTopLevelAsbiep);
        }

        defineSchemaName(generateExpression, option, generationContext);

        for (OpenAPITemplateForVerbOption template : option.getTemplates()) {
            generateExpression.generate(template);
        }

        File schemaExpressionFile;
        try {
            schemaExpressionFile = generateExpression.asFile(filename);
        } catch (IOException e) {
            throw new BieGenerateFailureException("I/O operation failure.", e);
        }
        return schemaExpressionFile;
    }

    private void defineSchemaName(BieGenerateOpenApiExpression generateExpression,
                                  OpenAPIGenerateExpressionOption option, GenerationContext generationContext) {
        // Issue #1603
        // Pre-define the schema object name for each operation
        //
        // <SchemaObject Name> ::= <UniqueTopLevelBIEName>[List][<MessageBodyType>][<ActionVerb>][Entry]
        //
        // <UniqueTopLevelBIEName> ::= <BIEName> when a DEN only has one distinct BIEId in the list of usages
        // [List] ::= **conditional** name; only used when the outer schema object represents an array or if the BIE has few properties selected in an inner array typically on a GET operation
        // [<MessageBodyType>] ::= **optional** meaning, only used to distinguish distinct BIEs, across different message bodies
        // [<ActionVerb>] :: = 'Update' | 'Delete' | 'Create' | 'Replace' | 'Read'; **optional** name used to further assist in unique DEN when multiple BIE ids exist for the same DEN.
        // [Entry] ::= **optional** name; used for the inner schema object of an array -- seldom used.
        Map<String, List<OpenAPITemplateForVerbOption>> bieNameTemplateMap = new HashMap<>();
        for (OpenAPITemplateForVerbOption template : option.getTemplates()) {
            String bieName = getBieName(template.getTopLevelAsbiep(), generationContext);
            List<OpenAPITemplateForVerbOption> schemaNameTemplates;
            if (bieNameTemplateMap.containsKey(bieName)) {
                schemaNameTemplates = bieNameTemplateMap.get(bieName);
            } else {
                schemaNameTemplates = new ArrayList<>();
            }
            schemaNameTemplates.add(template);
            bieNameTemplateMap.put(bieName, schemaNameTemplates);
        }

        // [List] ::= **conditional**
        Map<String, List<OpenAPITemplateForVerbOption>> bieNameWithArrayTemplateMap = new HashMap<>();
        for (Map.Entry<String, List<OpenAPITemplateForVerbOption>> entry : bieNameTemplateMap.entrySet()) {
            Map<TopLevelAsbiepId, List<OpenAPITemplateForVerbOption>> bieNameTemplateByTopLevelAsbiepIdMap =
                    entry.getValue().stream().collect(Collectors.groupingBy(OpenAPITemplateForVerbOption::getTopLevelAsbiepId));

            for (List<OpenAPITemplateForVerbOption> values : bieNameTemplateByTopLevelAsbiepIdMap.values()) {
                // If any template is checked as array
                if (values.stream().filter(e -> e.isArrayForJsonExpression()).count() > 0) {
                    for (OpenAPITemplateForVerbOption template : values) {
                        String key = entry.getKey() + (template.isArrayForJsonExpression() ? "List" : "ListEntry");
                        List<OpenAPITemplateForVerbOption> schemaNameWithArrayTemplates;
                        if (bieNameWithArrayTemplateMap.containsKey(key)) {
                            schemaNameWithArrayTemplates = bieNameWithArrayTemplateMap.get(key);
                        } else {
                            schemaNameWithArrayTemplates = new ArrayList<>();
                        }
                        schemaNameWithArrayTemplates.add(template);
                        bieNameWithArrayTemplateMap.put(key, schemaNameWithArrayTemplates);
                    }
                } else {
                    String key = entry.getKey();
                    List<OpenAPITemplateForVerbOption> schemaNameWithArrayTemplates;
                    if (bieNameWithArrayTemplateMap.containsKey(key)) {
                        schemaNameWithArrayTemplates = bieNameWithArrayTemplateMap.get(key);
                    } else {
                        schemaNameWithArrayTemplates = new ArrayList<>();
                    }
                    schemaNameWithArrayTemplates.addAll(values);
                    bieNameWithArrayTemplateMap.put(key, schemaNameWithArrayTemplates);
                }
            }
        }

        // [<MessageBodyType>] ::= **optional**
        Map<String, List<OpenAPITemplateForVerbOption>> bieNameWithArrayAndMessageTypeTemplateMap = new HashMap<>();
        for (Map.Entry<String, List<OpenAPITemplateForVerbOption>> entry : bieNameWithArrayTemplateMap.entrySet()) {
            if (!generateExpression.getSchemas().containsKey(entry.getKey()) &&
                    entry.getValue().stream().map(e -> e.getTopLevelAsbiepId()).collect(Collectors.toSet()).size() == 1) {
                for (OpenAPITemplateForVerbOption template : entry.getValue()) {
                    template.setSchemaName(entry.getKey());
                }
            } else {
                for (OpenAPITemplateForVerbOption template : entry.getValue()) {
                    String key = entry.getKey();
                    if (key.endsWith("Entry")) {
                        key = key.substring(0, key.indexOf("Entry")) + template.getMessageBodyType() + "Entry";
                    } else {
                        key = key + template.getMessageBodyType();
                    }
                    List<OpenAPITemplateForVerbOption> schemaNameWithArrayAndMessageTypeTemplates;
                    if (bieNameWithArrayAndMessageTypeTemplateMap.containsKey(key)) {
                        schemaNameWithArrayAndMessageTypeTemplates = bieNameWithArrayAndMessageTypeTemplateMap.get(key);
                    } else {
                        schemaNameWithArrayAndMessageTypeTemplates = new ArrayList<>();
                    }
                    schemaNameWithArrayAndMessageTypeTemplates.add(template);
                    bieNameWithArrayAndMessageTypeTemplateMap.put(key, schemaNameWithArrayAndMessageTypeTemplates);
                }
            }
        }

        // [<ActionVerb>] :: = 'Update' | 'Delete' | 'Create' | 'Replace' | 'Read'; **optional**
        Map<String, List<OpenAPITemplateForVerbOption>> bieNameWithArrayAndMessageTypeAndVerbTemplateMap = new HashMap<>();
        for (Map.Entry<String, List<OpenAPITemplateForVerbOption>> entry : bieNameWithArrayAndMessageTypeTemplateMap.entrySet()) {
            if (entry.getValue().stream().map(e -> e.getTopLevelAsbiepId()).collect(Collectors.toSet()).size() == 1) {
                for (OpenAPITemplateForVerbOption template : entry.getValue()) {
                    template.setSchemaName(entry.getKey());
                }
            } else {
                for (OpenAPITemplateForVerbOption template : entry.getValue()) {
                    String suffix;
                    switch (template.getVerbOption()) {
                        case GET:
                            suffix = "Read";
                            break;
                        case POST:
                            suffix = "Create";
                            break;
                        case PUT:
                            suffix = "Replace";
                            break;
                        case PATCH:
                            suffix = "Update";
                            break;
                        case DELETE:
                            suffix = "Delete";
                            break;
                        default:
                            throw new UnsupportedOperationException();
                    }

                    String key = entry.getKey();
                    if (key.endsWith("Entry")) {
                        key = key.substring(0, key.indexOf("Entry")) + suffix + "Entry";
                    } else {
                        key = key + suffix;
                    }
                    List<OpenAPITemplateForVerbOption> schemaNameWithArrayAndMessageTypeAndVerbTemplates;
                    if (bieNameWithArrayAndMessageTypeAndVerbTemplateMap.containsKey(key)) {
                        schemaNameWithArrayAndMessageTypeAndVerbTemplates = bieNameWithArrayAndMessageTypeAndVerbTemplateMap.get(key);
                    } else {
                        schemaNameWithArrayAndMessageTypeAndVerbTemplates = new ArrayList<>();
                    }
                    schemaNameWithArrayAndMessageTypeAndVerbTemplates.add(template);
                    bieNameWithArrayAndMessageTypeAndVerbTemplateMap.put(key, schemaNameWithArrayAndMessageTypeAndVerbTemplates);
                }
            }
        }

        for (Map.Entry<String, List<OpenAPITemplateForVerbOption>> entry : bieNameWithArrayAndMessageTypeAndVerbTemplateMap.entrySet()) {
            entry.getValue().forEach(e -> e.setSchemaName(entry.getKey()));
        }
    }

    private String getBieName(TopLevelAsbiepSummaryRecord topLevelAsbiep, GenerationContext generationContext) {
        AsbiepSummaryRecord asbiep = generationContext.findASBIEP(topLevelAsbiep.asbiepId(), topLevelAsbiep);
        AsccpSummaryRecord basedAsccp = generationContext.getAsccp(asbiep.basedAsccpManifestId());
        return convertIdentifierToId(camelCase(basedAsccp.propertyTerm()));
    }

    public GenerationContext generateContext(ScoreUser requester, List<TopLevelAsbiepSummaryRecord> topLevelAsbieps) {
        List<TopLevelAsbiepSummaryRecord> mergedTopLevelAsbieps = new ArrayList(topLevelAsbieps);

        if (mergedTopLevelAsbieps.size() == 0) {
            throw new IllegalArgumentException("Cannot found BIEs.");
        }

        return applicationContext.getBean(GenerationContext.class, requester, mergedTopLevelAsbieps);
    }

    private BieGenerateOpenApiExpression createBieGenerateOpenAPIExpression(
            GenerationContext generationContext, OpenAPIGenerateExpressionOption option) {
        return applicationContext.getBean(OpenAPIGenerateExpression.class, generationContext, option);
    }

}
