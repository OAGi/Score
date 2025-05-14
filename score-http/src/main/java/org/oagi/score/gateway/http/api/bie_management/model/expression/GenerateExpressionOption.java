package org.oagi.score.gateway.http.api.bie_management.model.expression;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.BiePackageSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;

import java.util.Collections;
import java.util.Map;

@Data
public class GenerateExpressionOption {

    private Map<TopLevelAsbiepId, String> filenames = Collections.emptyMap();
    private Map<TopLevelAsbiepId, BusinessContextId> bizCtxIds = Collections.emptyMap();
    private boolean bieDefinition = true;
    private boolean bieGuid;
    private boolean bieCctsMetaData;
    private boolean businessContext;
    private boolean includeCctsDefinitionTag;
    private boolean includeWhoColumns;
    private boolean bieOagiScoreMetaData;
    private boolean basedCcMetaData;

    private String expressionOption;
    private String packageOption;
    // Issue #1615
    private BiePackageSummaryRecord biePackage;

    private boolean includeBusinessContextInFilename;
    private boolean includeVersionInFilename;

    private boolean arrayForJsonExpression;
    private boolean includeMetaHeaderForJson;
    private TopLevelAsbiepId metaHeaderTopLevelAsbiepId;
    private boolean includePaginationResponseForJson;
    private TopLevelAsbiepId paginationResponseTopLevelAsbiepId;

    private String openAPIExpressionFormat;
    private boolean openAPICodeGenerationFriendly;
    private boolean openAPI30GetTemplate;
    private boolean arrayForJsonExpressionForOpenAPI30GetTemplate;
    private boolean suppressRootPropertyForOpenAPI30GetTemplate;
    private boolean includeMetaHeaderForJsonForOpenAPI30GetTemplate;

    private String scheme;
    private String host;

    public boolean isIncludeMetaHeaderForJsonForOpenAPI30GetTemplate() {
        if (isSuppressRootPropertyForOpenAPI30GetTemplate()) {
            return false;
        }
        return includeMetaHeaderForJsonForOpenAPI30GetTemplate;
    }

    private TopLevelAsbiepId metaHeaderTopLevelAsbiepIdForOpenAPI30GetTemplate;
    private boolean includePaginationResponseForJsonForOpenAPI30GetTemplate;

    public boolean isIncludePaginationResponseForJsonForOpenAPI30GetTemplate() {
        if (isSuppressRootPropertyForOpenAPI30GetTemplate()) {
            return false;
        }
        return includePaginationResponseForJsonForOpenAPI30GetTemplate;
    }

    private TopLevelAsbiepId paginationResponseTopLevelAsbiepIdForOpenAPI30GetTemplate;

    private boolean isOnlyBCCPsForOpenDocumentFormat = true;
    private String odfExpressionFormat = "ODS";

    public boolean hasOpenAPI30GetTemplateOptions() {
        if (isArrayForJsonExpressionForOpenAPI30GetTemplate()) {
            return true;
        }
        if (isSuppressRootPropertyForOpenAPI30GetTemplate()) {
            return true;
        }
        if (isIncludeMetaHeaderForJsonForOpenAPI30GetTemplate()) {
            return true;
        }
        if (isIncludePaginationResponseForJsonForOpenAPI30GetTemplate()) {
            return true;
        }
        return false;
    }

    private boolean openAPI30PostTemplate;
    private boolean arrayForJsonExpressionForOpenAPI30PostTemplate;
    private boolean suppressRootPropertyForOpenAPI30PostTemplate;
    private boolean includeMetaHeaderForJsonForOpenAPI30PostTemplate;
    private TopLevelAsbiepId metaHeaderTopLevelAsbiepIdForOpenAPI30PostTemplate;

    public boolean hasOpenAPI30PostTemplateOptions() {
        if (isArrayForJsonExpressionForOpenAPI30PostTemplate()) {
            return true;
        }
        if (isSuppressRootPropertyForOpenAPI30PostTemplate()) {
            return true;
        }
        if (isIncludeMetaHeaderForJsonForOpenAPI30PostTemplate()) {
            return true;
        }
        return false;
    }

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
