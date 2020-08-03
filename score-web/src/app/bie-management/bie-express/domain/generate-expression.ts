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
  metaHeaderTopLevelAsbiepId: number;
  includePaginationResponseForJson: boolean;
  paginationResponseTopLevelAsbiepId: number;

  openAPIExpressionFormat: string;
  openAPI30GetTemplate: boolean;
  arrayForJsonExpressionForOpenAPI30GetTemplate: boolean;
  includeMetaHeaderForJsonForOpenAPI30GetTemplate: boolean;
  metaHeaderTopLevelAsbiepIdForOpenAPI30GetTemplate: number;
  includePaginationResponseForJsonForOpenAPI30GetTemplate: boolean;
  paginationResponseTopLevelAsbiepIdForOpenAPI30GetTemplate: number;

  openAPI30PostTemplate: boolean;
  arrayForJsonExpressionForOpenAPI30PostTemplate: boolean;
  includeMetaHeaderForJsonForOpenAPI30PostTemplate: boolean;
  metaHeaderTopLevelAsbiepIdForOpenAPI30PostTemplate: number;
}
