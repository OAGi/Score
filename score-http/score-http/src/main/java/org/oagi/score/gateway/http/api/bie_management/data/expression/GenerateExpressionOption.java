package org.oagi.score.gateway.http.api.bie_management.data.expression;

import lombok.Data;

import java.math.BigInteger;

@Data
public class GenerateExpressionOption {

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

    private boolean arrayForJsonExpression;
    private boolean includeMetaHeaderForJson;
    private BigInteger metaHeaderTopLevelAsbiepId;
    private boolean includePaginationResponseForJson;
    private BigInteger paginationResponseTopLevelAsbiepId;

    private String openAPIExpressionFormat;
    private boolean openAPICodeGenerationFriendly;
    private boolean openAPI30GetTemplate;
    private boolean arrayForJsonExpressionForOpenAPI30GetTemplate;
    private boolean includeMetaHeaderForJsonForOpenAPI30GetTemplate;
    private BigInteger metaHeaderTopLevelAsbiepIdForOpenAPI30GetTemplate;
    private boolean includePaginationResponseForJsonForOpenAPI30GetTemplate;
    private BigInteger paginationResponseTopLevelAsbiepIdForOpenAPI30GetTemplate;

    private boolean openAPI30PostTemplate;
    private boolean arrayForJsonExpressionForOpenAPI30PostTemplate;
    private boolean includeMetaHeaderForJsonForOpenAPI30PostTemplate;
    private BigInteger metaHeaderTopLevelAsbiepIdForOpenAPI30PostTemplate;
}
