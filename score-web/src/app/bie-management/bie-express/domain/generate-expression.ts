export class BieExpressOption {
  filenames = {};
  bizCtxIds = {};

  bieDefinition: boolean;
  businessContext: boolean;

  bieCctsMetaData: boolean;
  includeCctsDefinitionTag: boolean;

  bieOagiScoreMetaData: boolean;
  includeWhoColumns: boolean;

  bieGuid: boolean;
  basedCcMetaData: boolean;

  expressionOption: string;
  packageOption: string;

  includeBusinessContextInFilename: boolean;
  includeVersionInFilename: boolean;

  arrayForJsonExpression: boolean;
  includeMetaHeaderForJson: boolean;
  metaHeaderTopLevelAsbiepId: number;
  includePaginationResponseForJson: boolean;
  paginationResponseTopLevelAsbiepId: number;

  openAPICodeGenerationFriendly = true;
  openAPIExpressionFormat: string;
  openAPI30GetTemplate: boolean;
  arrayForJsonExpressionForOpenAPI30GetTemplate: boolean;
  suppressRootPropertyForOpenAPI30GetTemplate: boolean;
  includeMetaHeaderForJsonForOpenAPI30GetTemplate: boolean;
  metaHeaderTopLevelAsbiepIdForOpenAPI30GetTemplate: number;
  includePaginationResponseForJsonForOpenAPI30GetTemplate: boolean;
  paginationResponseTopLevelAsbiepIdForOpenAPI30GetTemplate: number;

  openAPI30PostTemplate: boolean;
  arrayForJsonExpressionForOpenAPI30PostTemplate: boolean;
  suppressRootPropertyForOpenAPI30PostTemplate: boolean;
  includeMetaHeaderForJsonForOpenAPI30PostTemplate: boolean;
  metaHeaderTopLevelAsbiepIdForOpenAPI30PostTemplate: number;

  odfExpressionFormat: string;
}
