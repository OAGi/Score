import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {PreferencesInfo, TableColumnsProperty} from './preferences';
import {UserToken} from '../../../authentication/domain/auth';
import {loadBooleanProperty, loadProperty, saveBooleanProperty, saveProperty} from '../../../common/utility';

@Injectable()
export class SettingsPreferencesService {

  TABLE_COLUMNS_FOR_CORE_COMPONENT_PAGE_KEY = 'TableColumns-CoreComponentPage';
  TABLE_COLUMNS_FOR_DATA_TYPE_PAGE_KEY = 'TableColumns-DataTypePage';
  TABLE_COLUMNS_FOR_CORE_COMPONENT_WITHOUT_TYPE_COLUMN_PAGE_KEY = 'TableColumns-CoreComponentWithoutTypeColumnPage';
  TABLE_COLUMNS_FOR_DATA_TYPE_WITHOUT_TYPE_COLUMN_PAGE_KEY = 'TableColumns-DataTypeWithoutTypeColumnPage';
  TABLE_COLUMNS_FOR_CORE_COMPONENT_FOR_VERB_BOD_PAGE_KEY = 'TableColumns-CoreComponentForVerbBodPage';
  TABLE_COLUMNS_FOR_CORE_COMPONENT_FOR_NOUN_BOD_PAGE_KEY = 'TableColumns-CoreComponentForNounBodPage';
  FILTER_TYPES_FOR_CORE_COMPONENT_PAGE_KEY = 'FilterTypes-CoreComponentPage';
  FILTER_TYPES_FOR_DATA_TYPE_PAGE_KEY = 'FilterTypes-DataTypePage';
  TABLE_COLUMNS_FOR_CORE_COMPONENT_ACC_REFACTOR_PAGE_KEY = 'TableColumns-CoreComponentAccRefactorPage';
  TABLE_COLUMNS_FOR_CODE_LIST_PAGE_KEY = 'TableColumns-CodeListPage';
  TABLE_COLUMNS_FOR_CODE_LIST_VALUE_PAGE_KEY = 'TableColumns-CodeListValuePage';
  TABLE_COLUMNS_FOR_AGENCY_ID_LIST_PAGE_KEY = 'TableColumns-AgencyIdListPage';
  TABLE_COLUMNS_FOR_AGENCY_ID_LIST_VALUE_PAGE_KEY = 'TableColumns-AgencyIdValueListPage';
  TABLE_COLUMNS_FOR_RELEASE_PAGE_KEY = 'TableColumns-ReleasePage';
  TABLE_COLUMNS_FOR_NAMESPACE_PAGE_KEY = 'TableColumns-NamespacePage';

  TABLE_COLUMNS_FOR_CONTEXT_CATEGORY_PAGE_KEY = 'TableColumns-ContextCategoryPage';
  TABLE_COLUMNS_FOR_CONTEXT_SCHEME_PAGE_KEY = 'TableColumns-ContextSchemePage';
  TABLE_COLUMNS_FOR_CONTEXT_SCHEME_VALUE_PAGE_KEY = 'TableColumns-ContextSchemeValuePage';
  TABLE_COLUMNS_FOR_BUSINESS_CONTEXT_PAGE_KEY = 'TableColumns-BusinessContextPage';
  TABLE_COLUMNS_FOR_BUSINESS_CONTEXT_WITH_TENANT_PAGE_KEY = 'TableColumns-BusinessContextWithTenantPage';
  TABLE_COLUMNS_FOR_BUSINESS_CONTEXT_VALUE_PAGE_KEY = 'TableColumns-BusinessContextValuePage';

  TABLE_COLUMNS_FOR_MODULE_SET_PAGE_KEY = 'TableColumns-ModuleSetPage';
  TABLE_COLUMNS_FOR_MODULE_SET_RELEASE_PAGE_KEY = 'TableColumns-ModuleSetReleasePage';

  TABLE_COLUMNS_FOR_LIBRARY_PAGE_KEY = 'TableColumns-LibraryPage';

  TABLE_COLUMNS_FOR_BIE_PAGE_KEY = 'TableColumns-BiePage';
  TABLE_COLUMNS_FOR_BIE_REUSE_REPORT_PAGE_KEY = 'TableColumns-BieReuseReportPage';
  TABLE_COLUMNS_FOR_BIE_UPLIFT_REPORT_PAGE_KEY = 'TableColumns-BieUpliftReportPage';
  TABLE_COLUMNS_FOR_BIE_PACKAGE_PAGE_KEY = 'TableColumns-BiePackagePage';

  TABLE_COLUMNS_FOR_ACCOUNT_PAGE_KEY = 'TableColumns-AccountPage';
  TABLE_COLUMNS_FOR_PENDING_ACCOUNT_PAGE_KEY = 'TableColumns-PendingAccountPage';

  TABLE_COLUMNS_FOR_MESSAGE_PAGE_KEY = 'TableColumns-MessagePage';
  TABLE_COLUMNS_FOR_LOG_PAGE_KEY = 'TableColumns-LogPage';
  TABLE_COLUMNS_FOR_TENANT_PAGE_KEY = 'TableColumns-TenantPage';
  TABLE_COLUMNS_FOR_TENANT_MANAGEMENT_FOR_ACCOUNT_PAGE_KEY = 'TableColumns-TenantManagementForAccountPage';
  TABLE_COLUMNS_FOR_TENANT_MANAGEMENT_FOR_BUSINESS_CONTEXT_PAGE_KEY = 'TableColumns-TenantManagementForBusinessContextPage';

  TABLE_COLUMNS_FOR_OPENAPI_DOCUMENT_PAGE_KEY = 'TableColumns-OpenAPIDocumentPage';
  TABLE_COLUMNS_FOR_BIE_FOR_OAS_DOC_PAGE_KEY = 'TableColumns-BieForOasDocPage';

  TABLE_COLUMNS_FOR_BUSINESS_TERM_PAGE_KEY = 'TableColumns-BusinessTermPage';
  TABLE_COLUMNS_FOR_ASSIGNED_BUSINESS_TERM_PAGE_KEY = 'TableColumns-AssignedBusinessTermPage';

  PAGE_SETTINGS_BROWSER_VIEW_MODE_PROPERTY_KEY = 'PageSettings-BrowserViewMode';
  TREE_SETTINGS_PATH_DELIMITER_PROPERTY_KEY = 'TreeSettings-PathDelimiter';

  constructor(private http: HttpClient) {
  }

  loadColumnsInfo(preferencesInfo: PreferencesInfo, userToken: UserToken,
                  key: string, propertyName: string) {
    let properties: TableColumnsProperty[] = preferencesInfo.tableColumnsInfo[propertyName];
    properties = JSON.parse(loadProperty(userToken, key, JSON.stringify(properties)));

    // If the default column properties hasn't changed
    if (properties.length === preferencesInfo.tableColumnsInfo[propertyName].length) {
      preferencesInfo.tableColumnsInfo[propertyName] = properties;
    }
  }

  load(userToken: UserToken): Observable<PreferencesInfo> {
    return new Observable(subscriber => {
      const preferencesInfo = new PreferencesInfo();

      preferencesInfo.viewSettingsInfo.pageSettings.browserViewMode = loadBooleanProperty(
          userToken, this.PAGE_SETTINGS_BROWSER_VIEW_MODE_PROPERTY_KEY, (userToken.roles.includes('developer') ? false : true));
      preferencesInfo.viewSettingsInfo.treeSettings.delimiter = loadProperty(
          userToken, this.TREE_SETTINGS_PATH_DELIMITER_PROPERTY_KEY, '.');

      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_CORE_COMPONENT_PAGE_KEY, 'columnsOfCoreComponentPage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_DATA_TYPE_PAGE_KEY, 'columnsOfDataTypePage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_CORE_COMPONENT_WITHOUT_TYPE_COLUMN_PAGE_KEY, 'columnsOfCoreComponentWithoutTypeColumnPage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_DATA_TYPE_WITHOUT_TYPE_COLUMN_PAGE_KEY, 'columnsOfDataTypeWithoutTypeColumnPage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_CORE_COMPONENT_FOR_VERB_BOD_PAGE_KEY, 'columnsOfCoreComponentForVerbBODPage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_CORE_COMPONENT_FOR_NOUN_BOD_PAGE_KEY, 'columnsOfCoreComponentForNounBODPage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.FILTER_TYPES_FOR_CORE_COMPONENT_PAGE_KEY, 'filterTypesOfCoreComponentPage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.FILTER_TYPES_FOR_DATA_TYPE_PAGE_KEY, 'filterTypesOfDataTypePage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_CORE_COMPONENT_ACC_REFACTOR_PAGE_KEY, 'columnsOfCoreComponentAccRefactorPage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_CODE_LIST_PAGE_KEY, 'columnsOfCodeListPage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_CODE_LIST_VALUE_PAGE_KEY, 'columnsOfCodeListValuePage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_AGENCY_ID_LIST_PAGE_KEY, 'columnsOfAgencyIdListPage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_AGENCY_ID_LIST_VALUE_PAGE_KEY, 'columnsOfAgencyIdListValuePage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_RELEASE_PAGE_KEY, 'columnsOfReleasePage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_NAMESPACE_PAGE_KEY, 'columnsOfNamespacePage');

      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_CONTEXT_CATEGORY_PAGE_KEY, 'columnsOfContextCategoryPage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_CONTEXT_SCHEME_PAGE_KEY, 'columnsOfContextSchemePage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_CONTEXT_SCHEME_VALUE_PAGE_KEY, 'columnsOfContextSchemeValuePage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_BUSINESS_CONTEXT_PAGE_KEY, 'columnsOfBusinessContextPage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_BUSINESS_CONTEXT_WITH_TENANT_PAGE_KEY, 'columnsOfBusinessContextWithTenantPage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_BUSINESS_CONTEXT_VALUE_PAGE_KEY, 'columnsOfBusinessContextValuePage');

      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_MODULE_SET_PAGE_KEY, 'columnsOfModuleSetPage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_MODULE_SET_RELEASE_PAGE_KEY, 'columnsOfModuleSetReleasePage');

      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_BIE_PAGE_KEY, 'columnsOfBiePage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_BIE_REUSE_REPORT_PAGE_KEY, 'columnsOfBieReuseReportPage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_BIE_UPLIFT_REPORT_PAGE_KEY, 'columnsOfBieUpliftReportPage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_BIE_PACKAGE_PAGE_KEY, 'columnsOfBiePackagePage');

      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_ACCOUNT_PAGE_KEY, 'columnsOfAccountPage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_PENDING_ACCOUNT_PAGE_KEY, 'columnsOfPendingAccountPage');

      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_MESSAGE_PAGE_KEY, 'columnsOfMessagePage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_LOG_PAGE_KEY, 'columnsOfLogPage');

      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_TENANT_PAGE_KEY, 'columnsOfTenantPage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_TENANT_MANAGEMENT_FOR_ACCOUNT_PAGE_KEY, 'columnsOfTenantManagementForAccountPage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_TENANT_MANAGEMENT_FOR_BUSINESS_CONTEXT_PAGE_KEY, 'columnsOfTenantManagementForBusinessContextPage');

      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_OPENAPI_DOCUMENT_PAGE_KEY, 'columnsOfOpenApiDocumentPage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_BIE_FOR_OAS_DOC_PAGE_KEY, 'columnsOfBieForOasDocPage');

      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_BUSINESS_TERM_PAGE_KEY, 'columnsOfBusinessTermPage');
      this.loadColumnsInfo(preferencesInfo, userToken,
          this.TABLE_COLUMNS_FOR_ASSIGNED_BUSINESS_TERM_PAGE_KEY, 'columnsOfAssignedBusinessTermPage');

      subscriber.next(preferencesInfo);
      subscriber.complete();
    });
  }

  updateTableColumnsInfo(userToken: UserToken, key: string, columns: any[]): Observable<any> {
    return new Observable(subscriber => {
      saveProperty(userToken, key, JSON.stringify(columns));

      subscriber.next();
      subscriber.complete();
    });
  }

  updateTableColumnsForCoreComponentPage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_CORE_COMPONENT_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfCoreComponentPage);
  }

  updateTableColumnsForDataTypePage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_DATA_TYPE_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfDataTypePage);
  }

  updateTableColumnsForCoreComponentWithoutTypeColumnPage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_CORE_COMPONENT_WITHOUT_TYPE_COLUMN_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfCoreComponentWithoutTypeColumnPage);
  }

  updateTableColumnsForDataTypeWithoutTypeColumnPage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_DATA_TYPE_WITHOUT_TYPE_COLUMN_PAGE_KEY,
        preferencesInfo.tableColumnsInfo.columnsOfDataTypeWithoutTypeColumnPage);
  }

  updateTableColumnsForCoreComponentForVerbBODPage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_CORE_COMPONENT_FOR_VERB_BOD_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfCoreComponentForVerbBODPage);
  }

  updateTableColumnsForCoreComponentForNounBODPage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_CORE_COMPONENT_FOR_NOUN_BOD_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfCoreComponentForNounBODPage);
  }

  updateFilterTypeForCoreComponentPage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.FILTER_TYPES_FOR_CORE_COMPONENT_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.filterTypesOfCoreComponentPage);
  }

  updateFilterTypeForDataTypePage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.FILTER_TYPES_FOR_DATA_TYPE_PAGE_KEY,
        preferencesInfo.tableColumnsInfo.filterTypesOfDataTypePage);
  }

  updateTableColumnsForCoreComponentAccRefactorPage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_CORE_COMPONENT_ACC_REFACTOR_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfCoreComponentAccRefactorPage);
  }

  updateTableColumnsForCodeListPage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_CODE_LIST_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfCodeListPage);
  }

  updateTableColumnsForCodeListValuePage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_CODE_LIST_VALUE_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfCodeListValuePage);
  }

  updateTableColumnsForAgencyIdListPage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_AGENCY_ID_LIST_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfAgencyIdListPage);
  }

  updateTableColumnsForAgencyIdListValuePage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_AGENCY_ID_LIST_VALUE_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfAgencyIdListValuePage);
  }

  updateTableColumnsForReleasePage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_RELEASE_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfReleasePage);
  }

  updateTableColumnsForNamespacePage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_NAMESPACE_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfNamespacePage);
  }

  updateTableColumnsForContextCategoryPage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_CONTEXT_CATEGORY_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfContextCategoryPage);
  }

  updateTableColumnsForContextSchemePage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_CONTEXT_SCHEME_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfContextSchemePage);
  }

  updateTableColumnsForContextSchemeValuePage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_CONTEXT_SCHEME_VALUE_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfContextSchemeValuePage);
  }

  updateTableColumnsForBusinessContextPage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_BUSINESS_CONTEXT_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfBusinessContextPage);
  }

  updateTableColumnsForBusinessContextWithTenantPage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_BUSINESS_CONTEXT_WITH_TENANT_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfBusinessContextWithTenantPage);
  }

  updateTableColumnsForBusinessContextValuePage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_BUSINESS_CONTEXT_VALUE_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfBusinessContextValuePage);
  }

  updateTableColumnsForModuleSetPage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_MODULE_SET_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfModuleSetPage);
  }

  updateTableColumnsForModuleSetReleasePage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_MODULE_SET_RELEASE_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfModuleSetReleasePage);
  }

  updateTableColumnsForLibraryPage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_LIBRARY_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfLibraryPage);
  }

  updateTableColumnsForBiePage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_BIE_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfBiePage);
  }

  updateTableColumnsForBieReuseReportPage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_BIE_REUSE_REPORT_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfBieReuseReportPage);
  }

  updateTableColumnsForBieUpliftReportPage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_BIE_UPLIFT_REPORT_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfBieUpliftReportPage);
  }

  updateTableColumnsForBiePackagePage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_BIE_PACKAGE_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfBiePackagePage);
  }

  updateTableColumnsForAccountPage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_ACCOUNT_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfAccountPage);
  }

  updateTableColumnsForPendingAccountPage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_PENDING_ACCOUNT_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfPendingAccountPage);
  }

  updateTableColumnsForMessagePage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_MESSAGE_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfMessagePage);
  }

  updateTableColumnsForLogPage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_LOG_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfLogPage);
  }

  updateTableColumnsForTenantPage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_TENANT_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfTenantPage);
  }

  updateTableColumnsForTenantManagementForAccountPage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_TENANT_MANAGEMENT_FOR_ACCOUNT_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfTenantManagementForAccountPage);
  }

  updateTableColumnsForTenantManagementForBusinessContextPage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_TENANT_MANAGEMENT_FOR_BUSINESS_CONTEXT_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfTenantManagementForBusinessContextPage);
  }

  updateTableColumnsForOpenApiDocumentPage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_OPENAPI_DOCUMENT_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfOpenApiDocumentPage);
  }

  updateTableColumnsForBieForOasDocPage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_BIE_FOR_OAS_DOC_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfBieForOasDocPage);
  }

  updateTableColumnsForBusinessTermPage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_BUSINESS_TERM_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfBusinessTermPage);
  }

  updateTableColumnsForAssignedBusinessTermPage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.TABLE_COLUMNS_FOR_ASSIGNED_BUSINESS_TERM_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.columnsOfAssignedBusinessTermPage);
  }

  updateViewSettingsInfo(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return new Observable(subscriber => {
      saveBooleanProperty(userToken, this.PAGE_SETTINGS_BROWSER_VIEW_MODE_PROPERTY_KEY,
        preferencesInfo.viewSettingsInfo.pageSettings.browserViewMode);
      saveProperty(userToken, this.TREE_SETTINGS_PATH_DELIMITER_PROPERTY_KEY,
        preferencesInfo.viewSettingsInfo.treeSettings.delimiter);

      subscriber.next();
      subscriber.complete();
    });
  }
}
