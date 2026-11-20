import {HttpClient, HttpEvent, HttpHeaders, HttpParams} from '@angular/common/http';
import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';
import {
  PackageLoadJob,
  PackageLoadLogPage,
  PackageLoadSettings,
  PackagePreflight,
  PackageUpload
} from './release-package-load';

const URL = '/api/release-package-loads';

/** The loads of release packages, which are for administrators. */
@Injectable()
export class ReleasePackageLoadService {
  private http = inject(HttpClient);

  settings(): Observable<PackageLoadSettings> {
    return this.http.get<PackageLoadSettings>(URL + '/settings');
  }

  /** Sends the file as the body of the request (not as a form), so that the server writes it as it comes, whatever its size. */
  upload(file: File): Observable<HttpEvent<PackageUpload>> {
    return this.http.put<PackageUpload>(URL + '/uploads', file, {
      params: new HttpParams().set('fileName', file.name),
      headers: new HttpHeaders({'Content-Type': 'application/zip'}),
      reportProgress: true,
      observe: 'events'
    });
  }

  discardUpload(uploadId: string): Observable<void> {
    return this.http.delete<void>(URL + '/uploads/' + uploadId);
  }

  preflight(uploadId: string): Observable<PackagePreflight> {
    return this.http.get<PackagePreflight>(URL + '/uploads/' + uploadId + '/preflight');
  }

  start(uploadId: string): Observable<PackageLoadJob> {
    return this.http.post<PackageLoadJob>(URL, {uploadId});
  }

  recent(limit = 10): Observable<PackageLoadJob[]> {
    return this.http.get<PackageLoadJob[]>(URL, {params: new HttpParams().set('limit', limit)});
  }

  get(id: number): Observable<PackageLoadJob> {
    return this.http.get<PackageLoadJob>(URL + '/' + id);
  }

  /** What the server logged for the load after the message number given. */
  log(id: number, after: number): Observable<PackageLoadLogPage> {
    return this.http.get<PackageLoadLogPage>(URL + '/' + id + '/log', {params: new HttpParams().set('after', after)});
  }

  rollback(id: number): Observable<PackageLoadJob> {
    return this.http.post<PackageLoadJob>(URL + '/' + id + '/rollback', null);
  }
}
