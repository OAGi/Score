package org.oagi.srt.gateway.http.api.bie_management.data.expression;

import lombok.Data;

@Data
public class GenerateExpressionOption {

    private boolean bieDefinition = true;
    private boolean bieGuid;
    private boolean bieCctsMetaData;
    private boolean businessContext;
    private boolean includeCctsDefinitionTag;
    private boolean includeWhoColumns;
    private boolean bieOagiSrtMetaData;
    private boolean basedCcMetaData;

    private String expressionOption;
    private String packageOption;

    private boolean arrayForJsonExpression;
    private boolean includeMetaHeaderForJson;
    private Long metaHeaderTopLevelAbieId;
    private boolean includePaginationResponseForJson;
    private Long paginationResponseTopLevelAbieId;
}
