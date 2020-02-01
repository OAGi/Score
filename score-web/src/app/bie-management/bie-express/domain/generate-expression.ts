export class BieExpressOption {
  bieDefinition: boolean;
  businessContext: boolean;

  bieCctsMetaData: boolean;
  includeCctsDefinitionTag: boolean;

  bieOagiSrtMetaData: boolean;
  includeWhoColumns: boolean;

  bieGuid: boolean;
  basedCcMetaData: boolean;

  expressionOption: string;
  packageOption: string;

  arrayForJsonExpression: boolean;
  includeMetaHeaderForJson: boolean;
  metaHeaderTopLevelAbieId: number;
  includePaginationResponseForJson: boolean;
  paginationResponseTopLevelAbieId: number;

  openAPIExpressionFormat: string;
  openAPI30GetTemplate: boolean;
  arrayForJsonExpressionForOpenAPI30GetTemplate: boolean;
  includeMetaHeaderForJsonForOpenAPI30GetTemplate: boolean;
  metaHeaderTopLevelAbieIdForOpenAPI30GetTemplate: number;
  includePaginationResponseForJsonForOpenAPI30GetTemplate: boolean;
  paginationResponseTopLevelAbieIdForOpenAPI30GetTemplate: number;

  openAPI30PostTemplate: boolean;
  arrayForJsonExpressionForOpenAPI30PostTemplate: boolean;
  includeMetaHeaderForJsonForOpenAPI30PostTemplate: boolean;
  metaHeaderTopLevelAbieIdForOpenAPI30PostTemplate: number;
}
