import {HttpClient, HttpParams} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {StructureChangeReport, ValidationReport, ValidationTrigger} from './validation';

@Injectable({providedIn: 'root'})
export class ValidationService {
  private http = inject(HttpClient);

  /** Validates one object of the given kind (ACC, ...); violations are data, so a report with problems is a normal answer. */
  validate(subjectType: string, id: number | string, trigger: ValidationTrigger = 'ON_DEMAND'): Observable<ValidationReport> {
    const params = new HttpParams().set('trigger', trigger);
    return this.http.get<ValidationReport>('/api/validations/' + subjectType.toLowerCase() + '/' + id, {params});
  }

  /** What moving an association to a base ACC would break, asked before it is done. */
  refactorCheck(type: 'ASCC' | 'BCC', targetManifestId: number, destinationManifestId: number): Observable<StructureChangeReport> {
    const params = new HttpParams()
      .set('targetManifestId', targetManifestId.toString())
      .set('destinationManifestId', destinationManifestId.toString());
    return this.http.get<StructureChangeReport>('/api/core-components/' + type.toLowerCase() + '/refactor/check', {params});
  }

  /** What appending an association to the ACC would break, asked before it is appended. */
  appendCheck(accManifestId: number, type: string, propertyManifestId: number): Observable<StructureChangeReport> {
    const key = type.toUpperCase() === 'ASCCP' ? 'asccpManifestId' : 'bccpManifestId';
    const params = new HttpParams().set(key, propertyManifestId.toString());
    return this.http.get<StructureChangeReport>('/api/core-components/acc/' + accManifestId + '/append/check', {params});
  }

  /** What making the ACC based on another ACC would break, asked before it is done. */
  baseCheck(accManifestId: number, basedAccManifestId: number): Observable<StructureChangeReport> {
    const params = new HttpParams().set('basedAccManifestId', basedAccManifestId.toString());
    return this.http.get<StructureChangeReport>('/api/core-components/acc/' + accManifestId + '/base/check', {params});
  }
}
