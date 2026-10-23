import { Injectable, inject } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {PreferencesInfo} from './preferences';
import {UserToken} from '../../../authentication/domain/auth';
import {loadBooleanProperty, loadProperty, saveBooleanProperty, saveProperty} from '../../../common/utility';

@Injectable()
export class SettingsPreferencesService {
  private http = inject(HttpClient);

  PAGE_SETTINGS_BROWSER_VIEW_MODE_PROPERTY_KEY = 'PageSettings-BrowserViewMode';
  TREE_SETTINGS_PATH_DELIMITER_PROPERTY_KEY = 'TreeSettings-PathDelimiter';

  load(userToken: UserToken): Observable<PreferencesInfo> {
    return new Observable(subscriber => {
      const preferencesInfo = new PreferencesInfo();

      preferencesInfo.viewSettingsInfo.pageSettings.browserViewMode = loadBooleanProperty(
          userToken, this.PAGE_SETTINGS_BROWSER_VIEW_MODE_PROPERTY_KEY, (userToken.roles.includes('developer') ? false : true));
      preferencesInfo.viewSettingsInfo.treeSettings.delimiter = loadProperty(
          userToken, this.TREE_SETTINGS_PATH_DELIMITER_PROPERTY_KEY, '.');

      subscriber.next(preferencesInfo);
      subscriber.complete();
    });
  }

  updateViewSettingsInfo(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return new Observable(subscriber => {
      saveBooleanProperty(userToken, this.PAGE_SETTINGS_BROWSER_VIEW_MODE_PROPERTY_KEY,
        preferencesInfo.viewSettingsInfo.pageSettings.browserViewMode);
      saveProperty(userToken, this.TREE_SETTINGS_PATH_DELIMITER_PROPERTY_KEY,
        preferencesInfo.viewSettingsInfo.treeSettings.delimiter);

      subscriber.next(undefined);
      subscriber.complete();
    });
  }
}
