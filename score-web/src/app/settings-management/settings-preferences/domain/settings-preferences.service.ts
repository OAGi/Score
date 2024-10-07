import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {PreferencesInfo, TreeFeaturesInfo} from './preferences';
import {UserToken} from '../../../authentication/domain/auth';
import {loadProperty, saveProperty} from '../../../common/utility';

@Injectable()
export class SettingsPreferencesService {

  PATH_DELIMITER_PROPERTY_KEY = 'Settings-Path-Delimiter';

  constructor(private http: HttpClient) {
  }

  load(userToken: UserToken): Observable<PreferencesInfo> {
    return new Observable(subscriber => {
      const preferencesInfo = new PreferencesInfo();
      preferencesInfo.treeFeaturesInfo = new TreeFeaturesInfo();

      preferencesInfo.treeFeaturesInfo.delimiter = loadProperty(userToken, this.PATH_DELIMITER_PROPERTY_KEY, '.');

      subscriber.next(preferencesInfo);
      subscriber.complete();
    });
  }

  update(userToken: UserToken, preferencesInfo: PreferencesInfo): Observable<any> {
    return new Observable(subscriber => {
      saveProperty(userToken, this.PATH_DELIMITER_PROPERTY_KEY, preferencesInfo.treeFeaturesInfo.delimiter);

      subscriber.next();
      subscriber.complete();
    });
  }
}
