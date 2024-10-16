import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {PreferencesInfo, TableColumnsInfo, TreeSettingsInfo} from './preferences';
import {UserToken} from '../../../authentication/domain/auth';
import {loadProperty, saveProperty} from '../../../common/utility';

@Injectable()
export class SettingsPreferencesService {

  TABLE_COLUMNS_FOR_CORE_COMPONENT_PAGE_KEY = 'TableColumns-CoreComponentPage';
  FILTER_TYPES_FOR_CORE_COMPONENT_PAGE_KEY = 'FilterTypes-CoreComponentPage';
  TABLE_COLUMNS_FOR_CODE_LIST_PAGE_KEY = 'TableColumns-CodeListPage';
  TABLE_COLUMNS_FOR_AGENCY_ID_LIST_PAGE_KEY = 'TableColumns-AgencyIdListPage';
  TABLE_COLUMNS_FOR_RELEASE_PAGE_KEY = 'TableColumns-ReleasePage';
  TABLE_COLUMNS_FOR_NAMESPACE_PAGE_KEY = 'TableColumns-NamespacePage';

  TABLE_COLUMNS_FOR_CONTEXT_CATEGORY_PAGE_KEY = 'TableColumns-ContextCategoryPage';
  TABLE_COLUMNS_FOR_CONTEXT_SCHEME_PAGE_KEY = 'TableColumns-ContextSchemePage';
  TABLE_COLUMNS_FOR_BUSINESS_CONTEXT_PAGE_KEY = 'TableColumns-BusinessContextPage';

  TABLE_COLUMNS_FOR_MODULE_SET_PAGE_KEY = 'TableColumns-ModuleSetPage';
  TABLE_COLUMNS_FOR_MODULE_SET_RELEASE_PAGE_KEY = 'TableColumns-ModuleSetReleasePage';

  TABLE_COLUMNS_FOR_BIE_PAGE_KEY = 'TableColumns-BiePage';
  TABLE_COLUMNS_FOR_BIE_PACKAGE_PAGE_KEY = 'TableColumns-BiePackagePage';

  TABLE_COLUMNS_FOR_ACCOUNT_PAGE_KEY = 'TableColumns-AccountPage';
  TABLE_COLUMNS_FOR_PENDING_ACCOUNT_PAGE_KEY = 'TableColumns-PendingAccountPage';

  TABLE_COLUMNS_FOR_MESSAGE_PAGE_KEY = 'TableColumns-MessagePage';
  TABLE_COLUMNS_FOR_TENANT_PAGE_KEY = 'TableColumns-TenantPage';

  TABLE_COLUMNS_FOR_OPENAPI_DOCUMENT_PAGE_KEY = 'TableColumns-OpenAPIDocumentPage';

  TABLE_COLUMNS_FOR_BUSINESS_TERM_PAGE_KEY = 'TableColumns-BusinessTermPage';
  TABLE_COLUMNS_FOR_ASSIGNED_BUSINESS_TERM_PAGE_KEY = 'TableColumns-AssignedBusinessTermPage';

  PATH_DELIMITER_PROPERTY_KEY = 'Settings-Path-Delimiter';

  constructor(private http: HttpClient) {
  }

  load(userToken: UserToken): Observable<PreferencesInfo> {
    return new Observable(subscriber => {
      const preferencesInfo = new PreferencesInfo();
      preferencesInfo.tableColumnsInfo = new TableColumnsInfo();
      preferencesInfo.treeSettingsInfo = new TreeSettingsInfo();

      preferencesInfo.tableColumnsInfo.columnsOfCoreComponentPage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_CORE_COMPONENT_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfCoreComponentPage)));
      preferencesInfo.tableColumnsInfo.filterTypesOfCoreComponentPage =
        JSON.parse(loadProperty(userToken, this.FILTER_TYPES_FOR_CORE_COMPONENT_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.filterTypesOfCoreComponentPage)));
      preferencesInfo.tableColumnsInfo.columnsOfCodeListPage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_CODE_LIST_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfCodeListPage)));
      preferencesInfo.tableColumnsInfo.columnsOfAgencyIdListPage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_AGENCY_ID_LIST_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfAgencyIdListPage)));
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
      preferencesInfo.tableColumnsInfo.columnsOfBusinessContextPage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_BUSINESS_CONTEXT_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfBusinessContextPage)));

      preferencesInfo.tableColumnsInfo.columnsOfModuleSetPage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_MODULE_SET_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfModuleSetPage)));
      preferencesInfo.tableColumnsInfo.columnsOfModuleSetReleasePage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_MODULE_SET_RELEASE_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfModuleSetReleasePage)));

      preferencesInfo.tableColumnsInfo.columnsOfBiePage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_BIE_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfBiePage)));
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
      preferencesInfo.tableColumnsInfo.columnsOfTenantPage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_TENANT_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfTenantPage)));

      preferencesInfo.tableColumnsInfo.columnsOfOpenApiDocumentPage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_OPENAPI_DOCUMENT_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfOpenApiDocumentPage)));

      preferencesInfo.tableColumnsInfo.columnsOfBusinessTermPage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_BUSINESS_TERM_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfBusinessTermPage)));
      preferencesInfo.tableColumnsInfo.columnsOfAssignedBusinessTermPage =
        JSON.parse(loadProperty(userToken, this.TABLE_COLUMNS_FOR_ASSIGNED_BUSINESS_TERM_PAGE_KEY,
          JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfAssignedBusinessTermPage)));

      preferencesInfo.treeSettingsInfo.delimiter = loadProperty(userToken, this.PATH_DELIMITER_PROPERTY_KEY, '.');

      subscriber.next(preferencesInfo);
      subscriber.complete();
    });
  }

  update(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return new Observable(subscriber => {
      saveProperty(userToken, this.TABLE_COLUMNS_FOR_CORE_COMPONENT_PAGE_KEY,
        JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfCoreComponentPage));
      saveProperty(userToken, this.FILTER_TYPES_FOR_CORE_COMPONENT_PAGE_KEY,
        JSON.stringify(preferencesInfo.tableColumnsInfo.filterTypesOfCoreComponentPage));
      saveProperty(userToken, this.TABLE_COLUMNS_FOR_CODE_LIST_PAGE_KEY,
        JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfCodeListPage));
      saveProperty(userToken, this.TABLE_COLUMNS_FOR_AGENCY_ID_LIST_PAGE_KEY,
        JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfAgencyIdListPage));
      saveProperty(userToken, this.TABLE_COLUMNS_FOR_RELEASE_PAGE_KEY,
        JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfReleasePage));
      saveProperty(userToken, this.TABLE_COLUMNS_FOR_NAMESPACE_PAGE_KEY,
        JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfNamespacePage));

      saveProperty(userToken, this.TABLE_COLUMNS_FOR_CONTEXT_CATEGORY_PAGE_KEY,
        JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfContextCategoryPage));
      saveProperty(userToken, this.TABLE_COLUMNS_FOR_CONTEXT_SCHEME_PAGE_KEY,
        JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfContextSchemePage));
      saveProperty(userToken, this.TABLE_COLUMNS_FOR_BUSINESS_CONTEXT_PAGE_KEY,
        JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfBusinessContextPage));

      saveProperty(userToken, this.TABLE_COLUMNS_FOR_MODULE_SET_PAGE_KEY,
        JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfModuleSetPage));
      saveProperty(userToken, this.TABLE_COLUMNS_FOR_MODULE_SET_RELEASE_PAGE_KEY,
        JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfModuleSetReleasePage));

      saveProperty(userToken, this.TABLE_COLUMNS_FOR_BIE_PAGE_KEY,
        JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfBiePage));
      saveProperty(userToken, this.TABLE_COLUMNS_FOR_BIE_PACKAGE_PAGE_KEY,
        JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfBiePackagePage));

      saveProperty(userToken, this.TABLE_COLUMNS_FOR_ACCOUNT_PAGE_KEY,
        JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfAccountPage));
      saveProperty(userToken, this.TABLE_COLUMNS_FOR_PENDING_ACCOUNT_PAGE_KEY,
        JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfPendingAccountPage));

      saveProperty(userToken, this.TABLE_COLUMNS_FOR_MESSAGE_PAGE_KEY,
        JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfMessagePage));
      saveProperty(userToken, this.TABLE_COLUMNS_FOR_TENANT_PAGE_KEY,
        JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfTenantPage));

      saveProperty(userToken, this.TABLE_COLUMNS_FOR_OPENAPI_DOCUMENT_PAGE_KEY,
        JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfOpenApiDocumentPage));

      saveProperty(userToken, this.TABLE_COLUMNS_FOR_BUSINESS_TERM_PAGE_KEY,
        JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfBusinessTermPage));
      saveProperty(userToken, this.TABLE_COLUMNS_FOR_ASSIGNED_BUSINESS_TERM_PAGE_KEY,
        JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfAssignedBusinessTermPage));

      saveProperty(userToken, this.PATH_DELIMITER_PROPERTY_KEY, preferencesInfo.treeSettingsInfo.delimiter);

      subscriber.next();
      subscriber.complete();
    });
  }
}
