import { Injectable, OnInit, inject } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {TenantInfo, TenantList} from './tenants';
import {PageResponse} from '../../basis/basis';

@Injectable()
export class TenantListService implements OnInit {
  private http = inject(HttpClient);

  ngOnInit() {
  }

  getTenants(params: HttpParams): Observable<PageResponse<TenantList>> {
    return this.http.get<PageResponse<TenantList>>('/api/tenants', {params});
  }

  getTenantInfo(tenantId: number): Observable<TenantInfo> {
    return this.http.get<TenantInfo>('/api/tenants/' + tenantId);
  }

  createTenant(tenantName: string): Observable<any> {
    return this.http.post('/api/tenants', tenantName);
  }

  updateTenant(tenantId: number, tenantName: string): Observable<any> {
    return this.http.put('/api/tenants/' + tenantId, tenantName);
  }

  deleteTenant(tenantId: number): Observable<any> {
    return this.http.delete('/api/tenants/' + tenantId);
  }

  addTenantUser(tenantId: number, appUserId: number): Observable<any> {
    return this.http.post('/api/tenants/' + tenantId + '/users/' + appUserId, {});
  }

  deleteTenantUser(tenantId: number, appUserId: number): Observable<any> {
    return this.http.delete('/api/tenants/' + tenantId + '/users/' + appUserId, {});
  }

  addTenantBusinessCtx(tenantId: number, businessCtxId: number): Observable<any> {
    return this.http.post('/api/tenants/' + tenantId + '/business-contexts/' + businessCtxId, {});
  }

  deleteTenantBusinessCtx(tenantId: number, businessCtxId: number): Observable<any> {
    return this.http.delete('/api/tenants/' + tenantId + '/business-contexts/' + businessCtxId, {});
  }
}
