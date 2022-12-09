import {Injectable, OnInit} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {TenantListRequest, TenantList} from './tenants'
import {PageResponse} from '../../basis/basis';

@Injectable()
export class TenantListService implements OnInit {

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
  }

  getTenants(request: TenantListRequest): Observable<PageResponse<TenantList>> {
    let params = new HttpParams()
      .set('sortDirection', request.page.sortDirection)
      .set('pageIndex', '' + request.page.pageIndex)
      .set('pageSize', '' + request.page.pageSize);
      if (request.name) {
        params = params.set('name', request.name);
      }

    return this.http.get<PageResponse<TenantList>>('/api/tenants', {params: params});
  }

  getTenantInfo(tenantId: number) : Observable<TenantList> {
    return this.http.get<TenantList>('/api/tenants/' + tenantId);
  }

  createTenant(tenantName: string): Observable<any>{
    return this.http.post('/api/tenants', tenantName);
  }

  updateTenant(tenantId:number, tenantName: string): Observable<any>{
    return this.http.put('/api/tenants/' + tenantId, tenantName);
  }
  deleteTenantUser(tenantId:number, appUserId: number): Observable<any>{
    return this.http.put('/api/tenants/users/' + tenantId, appUserId);
  }

  addTenantUser(tenantId:number, appUserId: number): Observable<any>{
    return this.http.post('/api/tenants/users/' + tenantId, appUserId);
  }

  addTenantBusinessCtx(tenantId:number, businessCtxId: number): Observable<any>{
    return this.http.post('/api/tenants/bis-ctx/' + tenantId, businessCtxId);
  }

  deleteTenantBusinessCtx(tenantId:number, businessCtxId: number): Observable<any>{
    return this.http.put('/api/tenants/bis-ctx/' + tenantId, businessCtxId);
  }
}
