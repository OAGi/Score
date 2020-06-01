import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {AsccpForAppendAsccp, BccpForAppendBccp} from './extension-detail';
import {HttpClient} from '@angular/common/http';
import {CcNode, CcNodeDetail} from '../../domain/core-component-node';

@Injectable()
export class ExtensionDetailService {

  constructor(private http: HttpClient) {

  }

  getAsccpList(releaseId: number, extensionId: number): Observable<AsccpForAppendAsccp[]> {
    return this.http.get<AsccpForAppendAsccp[]>('/api/core_component/extension/' + releaseId + '/' + extensionId + '/asccp_list');
  }

  getBccpList(releaseId: number, extensionId: number): Observable<BccpForAppendBccp[]> {
    return this.http.get<BccpForAppendBccp[]>('/api/core_component/extension/' + releaseId + '/' + extensionId + '/bccp_list');
  }

  appendAsccp(asccpId: number, releaseId: number, extensionId: number): Observable<any> {
    return this.http.post('/api/core_component/extension/' + releaseId + '/' + extensionId, {
      'action': 'append',
      'type': 'asccp',
      'id': asccpId
    });
  }

  appendBccp(bccpId: number, releaseId: number, extensionId: number): Observable<any> {
    return this.http.post('/api/core_component/extension/' + releaseId + '/' + extensionId, {
      'action': 'append',
      'type': 'bccp',
      'id': bccpId
    });
  }

  discardAscc(asccId: number, releaseId: number, extensionId: number): Observable<any> {
    return this.http.post('/api/core_component/extension/' + releaseId + '/' + extensionId, {
      'action': 'discard',
      'type': 'ascc',
      'id': asccId
    });
  }

  discardBcc(bccId: number, releaseId: number, extensionId: number): Observable<any> {
    return this.http.post('/api/core_component/extension/' + releaseId + '/' + extensionId, {
      'action': 'discard',
      'type': 'bcc',
      'id': bccId
    });
  }

  setState(releaseId: number, extensionId: number, state: String): Observable<any> {
    return this.http.post('/api/core_component/extension/' + releaseId + '/' + extensionId + '/state', {
      state: state
    });
  }

  updateDetails(details: CcNodeDetail[], releaseId: number, extensionId: number): Observable<any> {
    const body = {
      asccpDetails: [],
      bccpDetails: [],
    };

    for (const detail of details) {
      switch (detail.type) {
        case 'asccp':
          body.asccpDetails.push(detail);
          break;
        case 'bccp':
          body.bccpDetails.push(detail);
          break;
      }
    }

    return this.http.post('/api/core_component/extension/' + releaseId + '/' + extensionId + '/detail', body);
  }

  getCcLastRevision(releaseId: number, type: String, Ccid: number): Observable<any> {
    return this.http.get<CcNode>('/api/core_component/extension/' + releaseId + '/' + type + '/' + Ccid + '/reivision');
  }
}
