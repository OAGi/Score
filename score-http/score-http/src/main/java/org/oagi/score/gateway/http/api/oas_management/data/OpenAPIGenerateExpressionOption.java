package org.oagi.score.gateway.http.api.oas_management.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;

@Data
public class OpenAPIGenerateExpressionOption {
    private String filename;
    private Map<BigInteger, BigInteger> bizCtxIds = Collections.emptyMap();
    private boolean includeBusinessContextInFilename;
    private boolean includeVersionInFilename;
    private boolean bieDefinition = true;
    private BigInteger metaHeaderTopLevelAsbiepId;
    private BigInteger paginationResponseTopLevelAsbiepId;
    private String verb;
    private String openAPIExpressionFormat;
    private boolean openAPICodeGenerationFriendly;
    private boolean openAPI30GetTemplate;
    private boolean arrayForJsonExpressionForOpenAPI30GetTemplate;
    private boolean suppressRootPropertyForOpenAPI30GetTemplate;
    private boolean includeMetaHeaderForJsonForOpenAPI30GetTemplate;
    private BigInteger metaHeaderTopLevelAsbiepIdForOpenAPI30GetTemplate;
    private boolean includePaginationResponseForJsonForOpenAPI30GetTemplate;
    private BigInteger paginationResponseTopLevelAsbiepIdForOpenAPI30GetTemplate;
    private boolean openAPI30PostTemplate;
    private boolean arrayForJsonExpressionForOpenAPI30PostTemplate;
    private boolean suppressRootPropertyForOpenAPI30PostTemplate;
    private boolean includeMetaHeaderForJsonForOpenAPI30PostTemplate;
    private BigInteger metaHeaderTopLevelAsbiepIdForOpenAPI30PostTemplate;
    public boolean isGetTemplateAndPostTemplateOptionDifferent() {
        if (isOpenAPI30GetTemplate() != isOpenAPI30PostTemplate()) {
            return false;
        }
        if (isIncludePaginationResponseForJsonForOpenAPI30GetTemplate()) {
            return true;
        }
        if (isArrayForJsonExpressionForOpenAPI30GetTemplate() !=
                isArrayForJsonExpressionForOpenAPI30PostTemplate()) {
            return true;
        }
        if (isSuppressRootPropertyForOpenAPI30GetTemplate() !=
                isSuppressRootPropertyForOpenAPI30PostTemplate()) {
            return true;
        }
        if (isIncludeMetaHeaderForJsonForOpenAPI30GetTemplate() !=
                isIncludeMetaHeaderForJsonForOpenAPI30PostTemplate()) {
            return true;
        }
        return false;
    }
}
