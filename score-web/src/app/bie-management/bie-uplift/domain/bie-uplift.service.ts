import {Injectable, OnInit} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {CcGraph} from '../../../cc-management/domain/core-component-node';
import {BieUpliftMap, FindTargetAsccpManifestResponse, MatchInfo, UpliftNode, BieValidationResponse} from './bie-uplift';

@Injectable()
export class BieUpliftService implements OnInit {

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
  }

  findTargetAsccpManifest(topLevelAsbiepId: number, releaseId: number): Observable<FindTargetAsccpManifestResponse> {
    const params = new HttpParams()
      .set('targetReleaseId', '' + releaseId);
    return this.http.get<FindTargetAsccpManifestResponse>('/api/profile_bie/' + topLevelAsbiepId + '/uplifting/target', {params: params});
  }

  getUpliftBieMap(topLevelAsbiepId: number, targetReleaseId: number): Observable<BieUpliftMap> {
    return this.http.get<BieUpliftMap>('/api/profile_bie/' + topLevelAsbiepId + '/uplifting?targetReleaseId=' + targetReleaseId);
  }

  createUpliftBie(topLevelAsbiepId: number, targetAsccpManifestId: number, matched: UpliftNode[]): Observable<any> {
    return this.http.post<any>('/api/profile_bie/' + topLevelAsbiepId + '/uplifting', {
      topLevelAsbiepId,
      targetAsccpManifestId,
      customMappingTable: matched
    });
  }

  checkValidationMatches(topLevelAsbiepId: number, targetReleaseId: number, matches: MatchInfo[]): Observable<BieValidationResponse> {
    return this.http.post<BieValidationResponse>('/api/profile_bie/' + topLevelAsbiepId + '/uplifting/' + targetReleaseId + '/valid', {mappingList: matches});
  }
}
