package org.oagi.score.gateway.http.api.oas_management.data;

import lombok.Data;
import org.oagi.score.repo.api.openapidoc.model.OasDoc;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Data
public class OpenAPIGenerateExpressionOption {
    private OasDoc oasDoc;
    private String filename;
    private BigInteger topLevelAsbiepId;
    private String resourceName;
    private String operationId;
    private String tagName;
    private Map<BigInteger, BigInteger> bizCtxIds = Collections.emptyMap();
    private boolean includeBusinessContextInFilename;
    private boolean includeVersionInFilename;
    private boolean bieDefinition = true;
    private BigInteger metaHeaderTopLevelAsbiepId;
    private BigInteger paginationResponseTopLevelAsbiepId;
    private String verb;
    private String messageBodyType;
    private String openAPIExpressionFormat;
    private boolean openAPICodeGenerationFriendly;
    private HashMap<String, OpenAPITemplateForVerbOption> openAPI30TemplateMap = new HashMap<>();
    public boolean isTwoTemplateOptionDifferent(String verb1, String verb2) {

        String templateKey1 = verb1 +"-" + this.resourceName;
        String templateKey2 = verb2 +"-" + this.resourceName;

        if (openAPI30TemplateMap.containsKey(templateKey1) != openAPI30TemplateMap.containsKey(templateKey2)) {
            return false;
        }
        if (openAPI30TemplateMap.get(templateKey1) != null && openAPI30TemplateMap.get(templateKey1).isIncludePaginationResponse()) {
            return true;
        }
        if (openAPI30TemplateMap.get(templateKey1) != null && openAPI30TemplateMap.get(templateKey2) != null &&
                openAPI30TemplateMap.get(templateKey1).isArrayForJsonExpression() != openAPI30TemplateMap.get(templateKey2).isArrayForJsonExpression()) {
            return true;
        }
        if (openAPI30TemplateMap.get(templateKey1) != null && openAPI30TemplateMap.get(templateKey2) != null &&
                openAPI30TemplateMap.get(templateKey1).isSuppressRootProperty() != openAPI30TemplateMap.get(templateKey2).isSuppressRootProperty()) {
            return true;
        }
        if (openAPI30TemplateMap.get(templateKey1) != null && openAPI30TemplateMap.get(templateKey2) != null &&
                openAPI30TemplateMap.get(templateKey1).isIncludeMetaHeader() != openAPI30TemplateMap.get(templateKey2).isIncludeMetaHeader()) {
            return true;
        }
        return false;
    }
}

