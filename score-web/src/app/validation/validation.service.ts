import {HttpClient, HttpParams} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {ValidationReport, ValidationTrigger} from './validation';

@Injectable({providedIn: 'root'})
export class ValidationService {
  private http = inject(HttpClient);

  /** Validates one object of the given kind (ACC, ...); violations are data, so a report with problems is a normal answer. */
  validate(subjectType: string, id: number | string, trigger: ValidationTrigger = 'ON_DEMAND'): Observable<ValidationReport> {
    const params = new HttpParams().set('trigger', trigger);
    return this.http.get<ValidationReport>('/api/validations/' + subjectType.toLowerCase() + '/' + id, {params});
  }
}
