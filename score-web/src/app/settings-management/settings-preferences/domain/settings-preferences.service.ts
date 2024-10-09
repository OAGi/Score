import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {PreferencesInfo, TableColumnsInfo, TreeSettingsInfo} from './preferences';
import {UserToken} from '../../../authentication/domain/auth';
import {loadProperty, saveProperty} from '../../../common/utility';
import {parseJson} from '@angular/cli/src/utilities/json-file';

@Injectable()
export class SettingsPreferencesService {

  TABLE_COLUMNS_FOR_CORE_COMPONENT_PAGE_KEY = 'TableColumns-CoreComponentPage';
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
      preferencesInfo.treeSettingsInfo.delimiter = loadProperty(userToken, this.PATH_DELIMITER_PROPERTY_KEY, '.');

      subscriber.next(preferencesInfo);
      subscriber.complete();
    });
  }

  update(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return new Observable(subscriber => {
      saveProperty(userToken, this.TABLE_COLUMNS_FOR_CORE_COMPONENT_PAGE_KEY,
        JSON.stringify(preferencesInfo.tableColumnsInfo.columnsOfCoreComponentPage));
      saveProperty(userToken, this.PATH_DELIMITER_PROPERTY_KEY, preferencesInfo.treeSettingsInfo.delimiter);

      subscriber.next();
      subscriber.complete();
    });
  }
}
