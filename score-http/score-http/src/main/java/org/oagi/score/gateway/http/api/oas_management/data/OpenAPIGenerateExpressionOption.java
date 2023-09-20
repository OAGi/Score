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
    private Map<BigInteger, BigInteger> bizCtxIds = Collections.emptyMap();
    private boolean includeBusinessContextInFilename;
    private boolean includeVersionInFilename;
    private boolean bieDefinition = true;
    private BigInteger metaHeaderTopLevelAsbiepId;
    private BigInteger paginationResponseTopLevelAsbiepId;
    private String verb;
    private String openAPIExpressionFormat;
    private boolean openAPICodeGenerationFriendly;
    private HashMap<String, OpenAPITemplateForVerbOption> openAPI30TemplateMap = new HashMap<>();
    public boolean isTwoTemplateOptionDifferent(String verb1, String verb2) {

        if (openAPI30TemplateMap.containsKey(verb1) != openAPI30TemplateMap.containsKey(verb2)) {
            return false;
        }
        if (openAPI30TemplateMap.get(verb1) != null && openAPI30TemplateMap.get(verb2).isIncludePaginationResponse()) {
            return true;
        }
        if (openAPI30TemplateMap.get(verb1) != null && openAPI30TemplateMap.get(verb2) != null &&
                openAPI30TemplateMap.get(verb1).isArrayForJsonExpression() != openAPI30TemplateMap.get(verb2).isArrayForJsonExpression()) {
            return true;
        }
        if (openAPI30TemplateMap.get(verb1) != null && openAPI30TemplateMap.get(verb2) != null &&
                openAPI30TemplateMap.get(verb1).isSuppressRootProperty() != openAPI30TemplateMap.get(verb2).isSuppressRootProperty()) {
            return true;
        }
        if (openAPI30TemplateMap.get(verb1) != null && openAPI30TemplateMap.get(verb2) != null &&
                openAPI30TemplateMap.get(verb1).isIncludeMetaHeader() != openAPI30TemplateMap.get(verb2).isIncludeMetaHeader()) {
            return true;
        }
        return false;
    }
}

