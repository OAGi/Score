import { Injectable, inject } from '@angular/core';
import {HttpClient, HttpContext} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ApplicationSettingsInfo} from './application-settings';
import {SUPPRESS_ERROR_ALERT} from '../../../authentication/auth.service';

@Injectable()
export class SettingsApplicationSettingsService {
  private http = inject(HttpClient);


  load(): Observable<ApplicationSettingsInfo> {
    return this.http.get<ApplicationSettingsInfo>('/api/application/settings');
  }

  update(applicationSettingsInfo: ApplicationSettingsInfo): Observable<any> {
    return this.http.post('/api/application/settings', {
      smtpSettingsInfo: applicationSettingsInfo.smtpSettingsInfo,
      bieSchemaFilenameExpression: applicationSettingsInfo.bieSchemaFilenameExpression,
      biePackageSchemaFilenameExpression: applicationSettingsInfo.biePackageSchemaFilenameExpression,
      bieSchemaFilenameDuplicateHandlerExpression: applicationSettingsInfo.bieSchemaFilenameDuplicateHandlerExpression,
      biePackageSchemaFilenameDuplicateHandlerExpression: applicationSettingsInfo.biePackageSchemaFilenameDuplicateHandlerExpression
    });
  }

  validateEmail(q: string): Observable<any> {
    return this.http.post('/api/account/email_validation', {
      q
    });
  }

  updateTenantConfiguration(value: boolean): Observable<any> {
    return this.updateBooleanConfiguration('tenant', value);
  }

  updateBusinessTermConfiguration(value: boolean): Observable<any> {
    return this.updateBooleanConfiguration('business-term', value);
  }

  updateBIEInverseModeConfiguration(value: boolean): Observable<any> {
    return this.updateBooleanConfiguration('bie-inverse-mode', value);
  }

  updateFunctionsRequiringEmailTransmissionConfiguration(value: boolean): Observable<any> {
    return this.updateBooleanConfiguration('functions-requiring-email-transmission', value);
  }

  updateBrowseStandardModeConfiguration(value: boolean): Observable<any> {
    return this.updateBooleanConfiguration('browse-standard-mode', value);
  }

  updateBooleanConfiguration(type: string, value: boolean): Observable<any> {
    return this.http.post('/api/application/' + type + '/' + (value ? 'enable' : 'disable'), {});
  }

  updateBieFilenameExpressions(bieSchemaFilenameExpression: string,
                               biePackageSchemaFilenameExpression: string,
                               bieSchemaFilenameDuplicateHandlerExpression: string,
                               biePackageSchemaFilenameDuplicateHandlerExpression: string): Observable<any> {
    return this.http.post('/api/application/settings', {
      bieSchemaFilenameExpression,
      biePackageSchemaFilenameExpression,
      bieSchemaFilenameDuplicateHandlerExpression,
      biePackageSchemaFilenameDuplicateHandlerExpression
    });
  }

  validateFilenameExpression(type: 'bie-schema' | 'bie-package-schema',
                             expression: string,
                             duplicateHandlerExpression: string): Observable<any> {
    return this.http.post('/api/application/filename-expression/' + type + '/validate', {
      expression,
      duplicateHandlerExpression
    }, {
      context: this.suppressErrorAlert()
    });
  }

  previewFilenameExpression(type: 'bie-schema' | 'bie-package-schema',
                            expression: string,
                            duplicateHandlerExpression: string): Observable<any> {
    return this.http.post('/api/application/filename-expression/' + type + '/preview', {
      expression,
      duplicateHandlerExpression
    }, {
      context: this.suppressErrorAlert()
    });
  }

  getConfiguration(key: string): Observable<any> {
    return this.http.get<any>('/api/application/' + key);
  }

  private suppressErrorAlert(): HttpContext {
    return new HttpContext().set(SUPPRESS_ERROR_ALERT, true);
  }

}
