import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {
  AgencyIdListCreateResponse,
  AgencyIdListDetails,
  AgencyIdListForListRequest,
  AgencyIdListListEntry,
  AgencyIdListSummary,
  AgencyIdListValueSummary
} from './agency-id-list';
import {PageResponse} from '../../basis/basis';
import {Comment} from '../../cc-management/domain/core-component-node';
import {map} from 'rxjs/operators';
import {NamespaceSummary} from '../../namespace-management/domain/namespace';

@Injectable()
export class AgencyIdListService {

  constructor(private http: HttpClient) {
  }

  getAgencyIdListList(request: AgencyIdListForListRequest): Observable<PageResponse<AgencyIdListListEntry>> {
    let params = new HttpParams()
        .set('libraryId', '' + request.library.libraryId)
        .set('releaseId', '' + request.release.releaseId)
        .set('pageIndex', '' + request.page.pageIndex)
        .set('pageSize', '' + request.page.pageSize);

    if (!!request.page.sortActive && !!request.page.sortDirection) {
      params = params.set('orderBy', ((request.page.sortDirection === 'desc') ? '-' : '+') + request.page.sortActive);
    }
    if (request.ownerLoginIdList.length > 0) {
      params = params.set('ownerLoginIdList', request.ownerLoginIdList.join(','));
    }
    if (request.updaterLoginIdList.length > 0) {
      params = params.set('updaterLoginIdList', request.updaterLoginIdList.join(','));
    }
    if (!!request.updatedDate.start || !!request.updatedDate.end) {
      params = params.set('lastUpdatedOn',
          '[' + (!!request.updatedDate.start ? request.updatedDate.start.getTime() : '') + '~' +
          (!!request.updatedDate.end ? request.updatedDate.end.getTime() : '') + ']');
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
    if (request.deprecated && request.deprecated.length === 1) {
      params = params.set('deprecated', '' + request.deprecated[0]);
    }
    if (request.newComponent && request.newComponent.length === 1) {
      params = params.set('newComponent', '' + request.newComponent[0]);
    }
    if (request.extensible !== undefined) {
      params = params.set('extensible', (request.extensible) ? 'true' : 'false');
    }
    if (request.ownedByDeveloper !== undefined) {
      params = params.set('ownedByDeveloper', (request.ownedByDeveloper) ? 'true' : 'false');
    }
    if (request.namespaces && request.namespaces.length > 0) {
      params = params.set('namespaces', request.namespaces.map(e => '' + e).join(','));
    }

    return this.http.get<PageResponse<AgencyIdListListEntry>>('/api/agency-id-lists', {params}).pipe(
        map((res: PageResponse<AgencyIdListListEntry>) => ({
          ...res,
          list: res.list.map(elm => ({
            ...elm,
            created: {
              ...elm.created,
              when: new Date(elm.created.when),
            },
            lastUpdated: {
              ...elm.lastUpdated,
              when: new Date(elm.lastUpdated.when),
            }
          }))
        }))
    );
  }

  getAgencyIdListDetails(manifestId): Observable<AgencyIdListDetails> {
    return this.http.get<AgencyIdListDetails>('/api/agency-id-lists/' + manifestId).pipe(
        map((elm: AgencyIdListDetails) => ({
          ...elm,
          agencyIdListValue: elm.agencyIdListValue || new AgencyIdListValueSummary(),
          namespace: elm.namespace || new NamespaceSummary(),
          created: {
            ...elm.created,
            when: new Date(elm.created.when),
          },
          lastUpdated: {
            ...elm.lastUpdated,
            when: new Date(elm.lastUpdated.when),
          }
        }))
    );
  }

  getPrevAgencyIdListDetails(manifestId): Observable<AgencyIdListDetails> {
    return this.http.get<AgencyIdListDetails>('/api/agency-id-lists/' + manifestId + '/prev').pipe(
        map((elm: AgencyIdListDetails) => ({
          ...elm,
          agencyIdListValue: elm.agencyIdListValue || new AgencyIdListValueSummary(),
          namespace: elm.namespace || new NamespaceSummary(),
          created: {
            ...elm.created,
            when: new Date(elm.created.when),
          },
          lastUpdated: {
            ...elm.lastUpdated,
            when: new Date(elm.lastUpdated.when),
          }
        }))
    );
  }

  getAgencyIdListSummaries(releaseId: number): Observable<AgencyIdListSummary[]> {
    const params = new HttpParams()
        .set('releaseId', releaseId.toString());

    return this.http.get<AgencyIdListSummary[]>('/api/agency-id-lists/summaries', {params});
  }

  create(releaseId: number, basedAgencyIdListManifestId?: number): Observable<AgencyIdListCreateResponse> {
    return this.http.post<AgencyIdListCreateResponse>('/api/agency-id-lists', {
      releaseId,
      basedAgencyIdListManifestId: basedAgencyIdListManifestId ? basedAgencyIdListManifestId : null
    });
  }

  update(agencyIdList: AgencyIdListDetails, state?: string): Observable<any> {
    let body;
    if (state) {
      body = {
        releaseId: agencyIdList.release.releaseId,
        toState: state
      };
      return this.http.patch('/api/agency-id-lists/' + agencyIdList.agencyIdListManifestId + '/state', body);
    } else {
      body = {
        agencyIdListManifestId: agencyIdList.agencyIdListManifestId,
        releaseId: agencyIdList.release.releaseId,
        basedAgencyIdListManifestId: (!!agencyIdList.based) ? agencyIdList.based.agencyIdListManifestId : undefined,
        agencyIdListValueManifestId: (!!agencyIdList.agencyIdListValue) ? agencyIdList.agencyIdListValue.agencyIdListValueManifestId : undefined,
        name: agencyIdList.name,
        listId: agencyIdList.listId,
        versionId: agencyIdList.versionId,
        namespaceId: (!!agencyIdList.namespace) ? agencyIdList.namespace.namespaceId : undefined,
        definition: agencyIdList.definition,
        remark: agencyIdList.remark,
        deprecated: agencyIdList.deprecated,
        valueList: agencyIdList.valueList
      };
      return this.http.put('/api/agency-id-lists/' + agencyIdList.agencyIdListManifestId, body);
    }
  }

  updateState(agencyIdList: AgencyIdListDetails, state: string): Observable<any> {
    return this.update(agencyIdList, state);
  }

  delete(...agencyIdListManifestIds): Observable<any> {
    return this.http.patch<any>('/api/agency-id-lists/mark-as-deleted', {
      agencyIdListManifestIds
    });
  }

  restore(...agencyIdListManifestIds): Observable<any> {
    return this.http.patch<any>('/api/agency-id-lists/restore', {
      agencyIdListManifestIds
    });
  }

  purge(...agencyIdListManifestIds): Observable<any> {
    return this.http.delete<any>('/api/agency-id-lists', {
      body: {
        agencyIdListManifestIds
      }
    });
  }

  checkUniqueness(agencyIdList: AgencyIdListDetails): Observable<boolean> {
    let params = new HttpParams()
        .set('releaseId', '' + agencyIdList.release.releaseId)
        .set('listId', agencyIdList.listId)
        .set('versionId', agencyIdList.versionId);
    if (!!agencyIdList.agencyIdListValue && !!agencyIdList.agencyIdListValue.agencyIdListValueManifestId) {
      params = params.set('agencyIdListValueManifestId', '' + agencyIdList.agencyIdListValue.agencyIdListValueManifestId);
    }
    if (agencyIdList.agencyIdListManifestId) {
      params = params.set('agencyIdListManifestId', '' + agencyIdList.agencyIdListManifestId);
    }

    return this.http.get<boolean>('/api/agency-id-lists/check-uniqueness', {params});
  }

  checkNameUniqueness(agencyIdList: AgencyIdListDetails): Observable<boolean> {
    let params = new HttpParams()
        .set('releaseId', '' + agencyIdList.release.releaseId)
        .set('agencyIdListName', agencyIdList.name);
    if (agencyIdList.agencyIdListManifestId) {
      params = params.set('agencyIdListManifestId', '' + agencyIdList.agencyIdListManifestId);
    }

    return this.http.get<boolean>('/api/agency-id-lists/check-name-uniqueness', {params});
  }

  makeNewRevision(agencyIdList: AgencyIdListDetails): Observable<any> {
    return this.http.patch('/api/agency-id-lists/' + agencyIdList.agencyIdListManifestId + '/revise', {});
  }

  cancelRevision(manifestId: number): Observable<any> {
    const url = '/api/agency-id-lists/' + manifestId + '/cancel';
    return this.http.patch<any>(url, {});
  }

  postComment(reference: string, text: string, prevCommentId?: number): Observable<any> {
    return this.http.post('/api/comments/' + reference, {
      reference,
      text,
      prevCommentId: prevCommentId ? prevCommentId : null
    });
  }

  editComment(commentId: number, text: string): Observable<any> {
    return this.http.post('/api/comments/' + commentId, {
      commentId,
      text,
    });
  }

  deleteComment(comment: Comment): Observable<any> {
    return this.http.post('/api/comments/' + comment.commentId, {
      commentId: comment.commentId,
      delete: true,
    });
  }

  getComments(reference): Observable<Comment[]> {
    return this.http.get<Comment[]>('/api/comments/' + reference);
  }

  transferOwnership(manifestId: number, targetLoginId: string): Observable<any> {
    return this.http.patch<any>('/api/agency-id-lists/' + manifestId + '/transfer', {
      targetLoginId
    });
  }

  uplift(agencyIdList: AgencyIdListDetails, targetReleaseId: number): Observable<any> {
    return this.http.post<any>('/api/agency-id-lists/' + agencyIdList.agencyIdListManifestId + '/uplift', {
      targetReleaseId
    });
  }
}
