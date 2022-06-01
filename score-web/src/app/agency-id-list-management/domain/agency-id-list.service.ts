import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {AgencyIdList, AgencyIdListForListRequest} from './agency-id-list';
import {PageResponse, PaginationResponse} from '../../basis/basis';
import {CcCreateResponse, Comment} from '../../cc-management/domain/core-component-node';

@Injectable()
export class AgencyIdListService {

  constructor(private http: HttpClient) {
  }

  getAgencyIdListList(request: AgencyIdListForListRequest): Observable<PaginationResponse<AgencyIdList>> {
    let params = new HttpParams().set('releaseId', '' + request.release.releaseId)
      .set('sortActive', request.page.sortActive)
      .set('sortDirection', request.page.sortDirection)
      .set('pageIndex', '' + request.page.pageIndex)
      .set('pageSize', '' + request.page.pageSize);
    if (request.updaterLoginIds.length > 0) {
      params = params.set('updaterLoginIds', request.updaterLoginIds.join(','));
    }
    if (request.ownerLoginIds.length > 0) {
      params = params.set('ownerLoginIds', request.ownerLoginIds.join(','));
    }
    if (request.updatedDate.start) {
      params = params.set('updateStart', '' + request.updatedDate.start.getTime());
    }
    if (request.updatedDate.end) {
      params = params.set('updateEnd', '' + request.updatedDate.end.getTime());
    }
    if (request.filters.name) {
      params = params.set('name', request.filters.name);
    }
    if (request.filters.definition) {
      params = params.set('definition', request.filters.definition);
    }
    if (request.filters.module) {
      params = params.set('module', request.filters.module);
    }
    if (request.access) {
      params = params.set('access', request.access);
    }
    if (request.states.length > 0) {
      params = params.set('states', request.states.join(','));
    }
    if (request.deprecated.length === 1) {
      params = params.set('deprecated', '' + request.deprecated[0]);
    }
    if (request.extensible !== undefined) {
      params = params.set('extensible', (request.extensible) ? 'true' : 'false');
    }
    if (request.ownedByDeveloper !== undefined) {
      params = params.set('ownedByDeveloper', (request.ownedByDeveloper) ? 'true' : 'false');
    }

    return this.http.get<PaginationResponse<AgencyIdList>>('/api/agency_id_list', {params});
  }

  getAgencyIdList(manifestId): Observable<AgencyIdList> {
    return this.http.get<AgencyIdList>('/api/agency_id_list/' + manifestId);
  }

  getSimpleAgencyIdLists(releaseId: number): Observable<any> {
    const params = new HttpParams()
      .set('releaseId', releaseId.toString())
      .set('sortActive', '')
      .set('sortDirection', 'ASC')
      .set('pageIndex', '-1')
      .set('pageSize', '-1');

    return this.http.get<any>('/api/agency_id_list', {params});
  }

  create(releaseId: number, basedAgencyIdListManifestId?: number): Observable<CcCreateResponse> {
    return this.http.put<CcCreateResponse>('/api/agency_id_list', {
      releaseId,
      basedAgencyIdListManifestId: basedAgencyIdListManifestId ? basedAgencyIdListManifestId : null
    });
  }

  update(agencyIdList: AgencyIdList, state?: string): Observable<any> {
    let body;
    if (state) {
      body = {
        releaseId: agencyIdList.releaseId,
        toState: state
      };
      return this.http.post('/api/agency_id_list/' + agencyIdList.agencyIdListManifestId + '/state', body);
    } else {
      body = {
        agencyIdListManifestId: agencyIdList.agencyIdListManifestId,
        releaseId: agencyIdList.releaseId,
        basedAgencyIdListManifestId: agencyIdList.basedAgencyIdListManifestId,
        agencyIdListValueManifestId: agencyIdList.agencyIdListValueManifestId,
        name: agencyIdList.name,
        listId: agencyIdList.listId,
        agencyId: agencyIdList.agencyId,
        versionId: agencyIdList.versionId,
        namespaceId: agencyIdList.namespaceId,
        definition: agencyIdList.definition,
        definitionSource: agencyIdList.definitionSource,
        remark: agencyIdList.remark,
        values: agencyIdList.values,
        deprecated: agencyIdList.deprecated
      };
      return this.http.post('/api/agency_id_list/' + agencyIdList.agencyIdListManifestId, body);
    }


  }

  updateState(agencyIdList: AgencyIdList, state: string): Observable<any> {
    return this.update(agencyIdList, state);
  }

  makeNewRevision(agencyIdList: AgencyIdList): Observable<any> {
    return this.http.post('/api/agency_id_list/' + agencyIdList.agencyIdListManifestId + '/revision', {});
  }

  delete(...agencyIdListManifestIds): Observable<any> {
    return this.http.post<any>('/api/agency_id_list/delete', {
      agencyIdListManifestIds
    });
  }

  restore(...agencyIdListManifestIds): Observable<any> {
    return this.http.post<any>('/api/agency_id_list/restore', {
      agencyIdListManifestIds
    });
  }

  checkUniqueness(agencyIdList: AgencyIdList): Observable<boolean> {
    let params = new HttpParams()
      .set('releaseId', '' + agencyIdList.releaseId)
      .set('listId', agencyIdList.listId)
      .set('versionId', agencyIdList.versionId);
    if (agencyIdList.agencyIdListValueManifestId) {
      params = params.set('agencyIdListValueManifestId', '' + agencyIdList.agencyIdListValueManifestId);
    }

    if (agencyIdList.agencyIdListManifestId) {
      params = params.set('agencyIdListManifestId', '' + agencyIdList.agencyIdListManifestId);
    }

    return this.http.get<boolean>('/api/agency_id_list/check_uniqueness', {params});
  }

  checkNameUniqueness(agencyIdList: AgencyIdList): Observable<boolean> {
    let params = new HttpParams()
      .set('releaseId', '' + agencyIdList.releaseId)
      .set('agencyIdListName', agencyIdList.name);
    if (agencyIdList.agencyIdListManifestId) {
      params = params.set('agencyIdListManifestId', '' + agencyIdList.agencyIdListManifestId);
    }

    return this.http.get<boolean>('/api/agency_id_list/check_name_uniqueness', {params});
  }

  cancelRevision(manifestId: number): Observable<any> {
    const url = '/api/agency_id_list/' + manifestId + '/cancel';
    return this.http.post<any>(url, {});
  }

  postComment(reference: string, text: string, prevCommentId?: number): Observable<any> {
    return this.http.put('/api/comment/' + reference, {
      reference,
      text,
      prevCommentId: prevCommentId ? prevCommentId : null
    });
  }

  editComment(commentId: number, text: string): Observable<any> {
    return this.http.post('/api/comment/' + commentId, {
      commentId,
      text,
    });
  }

  deleteComment(comment: Comment): Observable<any> {
    return this.http.post('/api/comment/' + comment.commentId, {
      commentId: comment.commentId,
      delete: true,
    });
  }

  getComments(reference): Observable<Comment[]> {
    return this.http.get<Comment[]>('/api/comments/' + reference);
  }

  transferOwnership(manifestId: number, targetLoginId: string): Observable<any> {
    return this.http.post<any>('/api/agency_id_list/' + manifestId + '/transfer_ownership', {
      targetLoginId
    });
  }

  uplift(agencyIdList: AgencyIdList, targetReleaseId: number): Observable<any> {
    return this.http.post<any>('/api/agency_id_list/' + agencyIdList.agencyIdListManifestId + '/uplift', {
      targetReleaseId: targetReleaseId
    });
  }
}
