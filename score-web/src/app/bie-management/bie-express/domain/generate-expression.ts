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
}
