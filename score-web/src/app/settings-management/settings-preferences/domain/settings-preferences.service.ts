import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {PreferencesInfo, TableColumnsProperty} from './preferences';
import {UserToken} from '../../../authentication/domain/auth';
import {loadBooleanProperty, loadProperty, saveBooleanProperty, saveProperty} from '../../../common/utility';

@Injectable()
export class SettingsPreferencesService {

  TABLE_COLUMNS_FOR_CORE_COMPONENT_PAGE_KEY = 'TableColumns-CoreComponentPage';
  FILTER_TYPES_FOR_CORE_COMPONENT_PAGE_KEY = 'FilterTypes-CoreComponentPage';
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
  TABLE_COLUMNS_FOR_BUSINESS_CONTEXT_VALUE_PAGE_KEY = 'TableColumns-BusinessContextValuePage';

  TABLE_COLUMNS_FOR_MODULE_SET_PAGE_KEY = 'TableColumns-ModuleSetPage';
  TABLE_COLUMNS_FOR_MODULE_SET_RELEASE_PAGE_KEY = 'TableColumns-ModuleSetReleasePage';

  TABLE_COLUMNS_FOR_BIE_PAGE_KEY = 'TableColumns-BiePage';
  TABLE_COLUMNS_FOR_BIE_REUSE_REPORT_PAGE_KEY = 'TableColumns-BieReuseReportPage';
  TABLE_COLUMNS_FOR_BIE_UPLIFT_REPORT_PAGE_KEY = 'TableColumns-BieUpliftReportPage';
  TABLE_COLUMNS_FOR_BIE_PACKAGE_PAGE_KEY = 'TableColumns-BiePackagePage';

  TABLE_COLUMNS_FOR_ACCOUNT_PAGE_KEY = 'TableColumns-AccountPage';
  TABLE_COLUMNS_FOR_PENDING_ACCOUNT_PAGE_KEY = 'TableColumns-PendingAccountPage';

  TABLE_COLUMNS_FOR_MESSAGE_PAGE_KEY = 'TableColumns-MessagePage';
  TABLE_COLUMNS_FOR_LOG_PAGE_KEY = 'TableColumns-LogPage';
  TABLE_COLUMNS_FOR_TENANT_PAGE_KEY = 'TableColumns-TenantPage';

  TABLE_COLUMNS_FOR_OPENAPI_DOCUMENT_PAGE_KEY = 'TableColumns-OpenAPIDocumentPage';
  TABLE_COLUMNS_FOR_BIE_FOR_OAS_DOC_PAGE_KEY = 'TableColumns-BieForOasDocPage';

  TABLE_COLUMNS_FOR_BUSINESS_TERM_PAGE_KEY = 'TableColumns-BusinessTermPage';
  TABLE_COLUMNS_FOR_ASSIGNED_BUSINESS_TERM_PAGE_KEY = 'TableColumns-AssignedBusinessTermPage';

  PAGE_SETTINGS_BROWSER_VIEW_MODE_PROPERTY_KEY = 'PageSettings-BrowserViewMode';
  TREE_SETTINGS_PATH_DELIMITER_PROPERTY_KEY = 'TreeSettings-PathDelimiter';

  constructor(private http: HttpClient) {
  }

  load(userToken: UserToken): Observable<PreferencesInfo> {
    return new Observable(subscriber => {
      const preferencesInfo = new PreferencesInfo();

      preferencesInfo.tableColumnsInfo.columnsOfCoreComponentPage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_CORE_COMPONENT_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfCoreComponentPage)));
      preferencesInfo.tableColumnsInfo.filterTypesOfCoreComponentPage =
        JSON.parse(loadProperty(userToken, this.FILTER_TYPES_FOR_CORE_COMPONENT_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.filterTypesOfCoreComponentPage)));
      preferencesInfo.tableColumnsInfo.columnsOfCoreComponentAccRefactorPage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_CORE_COMPONENT_ACC_REFACTOR_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfCoreComponentAccRefactorPage)));
      preferencesInfo.tableColumnsInfo.columnsOfCodeListPage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_CODE_LIST_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfCodeListPage)));
      preferencesInfo.tableColumnsInfo.columnsOfCodeListValuePage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_CODE_LIST_VALUE_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfCodeListValuePage)));
      preferencesInfo.tableColumnsInfo.columnsOfAgencyIdListPage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_AGENCY_ID_LIST_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfAgencyIdListPage)));
      preferencesInfo.tableColumnsInfo.columnsOfAgencyIdListValuePage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_AGENCY_ID_LIST_VALUE_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfAgencyIdListValuePage)));
      preferencesInfo.tableColumnsInfo.columnsOfReleasePage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_RELEASE_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfReleasePage)));
      preferencesInfo.tableColumnsInfo.columnsOfNamespacePage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_NAMESPACE_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfNamespacePage)));

      preferencesInfo.tableColumnsInfo.columnsOfContextCategoryPage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_CONTEXT_CATEGORY_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfContextCategoryPage)));
      preferencesInfo.tableColumnsInfo.columnsOfContextSchemePage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_CONTEXT_SCHEME_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfContextSchemePage)));
      preferencesInfo.tableColumnsInfo.columnsOfContextSchemeValuePage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_CONTEXT_SCHEME_VALUE_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfContextSchemeValuePage)));
      preferencesInfo.tableColumnsInfo.columnsOfBusinessContextPage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_BUSINESS_CONTEXT_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfBusinessContextPage)));
      preferencesInfo.tableColumnsInfo.columnsOfBusinessContextValuePage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_BUSINESS_CONTEXT_VALUE_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfBusinessContextValuePage)));

      preferencesInfo.tableColumnsInfo.columnsOfModuleSetPage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_MODULE_SET_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfModuleSetPage)));
      preferencesInfo.tableColumnsInfo.columnsOfModuleSetReleasePage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_MODULE_SET_RELEASE_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfModuleSetReleasePage)));

      preferencesInfo.tableColumnsInfo.columnsOfBiePage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_BIE_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfBiePage)));
      preferencesInfo.tableColumnsInfo.columnsOfBieReuseReportPage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_BIE_REUSE_REPORT_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfBieReuseReportPage)));
      preferencesInfo.tableColumnsInfo.columnsOfBieUpliftReportPage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_BIE_UPLIFT_REPORT_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfBieUpliftReportPage)));
      preferencesInfo.tableColumnsInfo.columnsOfBiePackagePage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_BIE_PACKAGE_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfBiePackagePage)));

      preferencesInfo.tableColumnsInfo.columnsOfAccountPage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_ACCOUNT_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfAccountPage)));
      preferencesInfo.tableColumnsInfo.columnsOfPendingAccountPage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_PENDING_ACCOUNT_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfPendingAccountPage)));

      preferencesInfo.tableColumnsInfo.columnsOfMessagePage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_MESSAGE_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfMessagePage)));
      preferencesInfo.tableColumnsInfo.columnsOfLogPage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_LOG_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfLogPage)));
      preferencesInfo.tableColumnsInfo.columnsOfTenantPage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_TENANT_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfTenantPage)));

      preferencesInfo.tableColumnsInfo.columnsOfOpenApiDocumentPage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_OPENAPI_DOCUMENT_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfOpenApiDocumentPage)));
      preferencesInfo.tableColumnsInfo.columnsOfBieForOasDocPage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_BIE_FOR_OAS_DOC_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfBieForOasDocPage)));

      preferencesInfo.tableColumnsInfo.columnsOfBusinessTermPage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_BUSINESS_TERM_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfBusinessTermPage)));
      preferencesInfo.tableColumnsInfo.columnsOfAssignedBusinessTermPage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_ASSIGNED_BUSINESS_TERM_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfAssignedBusinessTermPage)));

      preferencesInfo.viewSettingsInfo.pageSettings.browserViewMode = loadBooleanProperty(
        userToken, this.PAGE_SETTINGS_BROWSER_VIEW_MODE_PROPERTY_KEY, (userToken.roles.includes('developer') ? false : true));
      preferencesInfo.viewSettingsInfo.treeSettings.delimiter = loadProperty(
        userToken, this.TREE_SETTINGS_PATH_DELIMITER_PROPERTY_KEY, '.');

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

  updateFilterTypeForCoreComponentPage(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return this.updateTableColumnsInfo(userToken, this.FILTER_TYPES_FOR_CORE_COMPONENT_PAGE_KEY,
      preferencesInfo.tableColumnsInfo.filterTypesOfCoreComponentPage);
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
