import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {
  CcAccNodeDetail,
  CcAsccpNodeDetail,
  CcBccpNodeDetail,
  CcBdtScNodeDetail,
  CcDtNodeDetail,
  CcGraph
} from '../../../cc-management/domain/core-component-node';
import {base64Encode} from '../../../common/utility';

@Injectable()
export class ModelBrowserService {

  constructor(private http: HttpClient) {
  }

  getGraphNode(type: string, manifestId: number): Observable<CcGraph> {
    return this.http.get<CcGraph>('/api/graphs/' + type + '/' + manifestId);
  }

  getAccDetail(accManifestId: number): Observable<CcAccNodeDetail> {
    const url = '/api/core_component/acc/detail';
    let params = new HttpParams();
    params = params.set('type', 'ACC');
    params = params.set('manifestId', String(accManifestId));
    const data = base64Encode(params.toString());
    return this.http.get<CcAccNodeDetail>(url, {params: new HttpParams().set('data', data)});
  }

  getAsccpDetail(asccpManifestId: number, asccManifestId?: number): Observable<CcAsccpNodeDetail> {
    const url = '/api/core_component/asccp/detail';
    let params = new HttpParams();
    params = params.set('type', 'ASCCP');
    params = params.set('manifestId', String(asccpManifestId));
    if (asccManifestId) {
      params = params.set('asccManifestId', String(asccManifestId));
    }
    const data = base64Encode(params.toString());
    return this.http.get<CcAsccpNodeDetail>(url, {params: new HttpParams().set('data', data)});
  }

  getBccpDetail(bccpManifestId: number, bdtManifestId: number, bccManifestId?: number): Observable<CcBccpNodeDetail> {
    const url = '/api/core_component/bccp/detail';
    let params = new HttpParams();
    params = params.set('type', 'BCCP');
    params = params.set('manifestId', String(bccpManifestId));
    params = params.set('bdtManifestId', String(bdtManifestId));
    if (bccManifestId) {
      params = params.set('bccManifestId', String(bccManifestId));
    }
    const data = base64Encode(params.toString());
    return this.http.get<CcBccpNodeDetail>(url, {params: new HttpParams().set('data', data)});
  }

  getBdtDetail(bdtManifestId: number): Observable<CcDtNodeDetail> {
    const url = '/api/core_component/dt/detail';
    let params = new HttpParams();
    params = params.set('type', 'DT');
    params = params.set('manifestId', String(bdtManifestId));
    const data = base64Encode(params.toString());
    return this.http.get<CcDtNodeDetail>(url, {params: new HttpParams().set('data', data)});
  }

  getBdtScDetail(bdtScManifestId: number): Observable<CcBdtScNodeDetail> {
    const url = '/api/core_component/dt_sc/detail';
    let params = new HttpParams();
    params = params.set('type', 'DT_SC');
    params = params.set('manifestId', String(bdtScManifestId));
    const data = base64Encode(params.toString());
    return this.http.get<CcBdtScNodeDetail>(url, {params: new HttpParams().set('data', data)});
  }

}
