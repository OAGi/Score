import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {CodeListCreateResponse, CodeListDetails, CodeListListEntry, CodeListListEntryRequest, CodeListSummary} from './code-list';
import {PageResponse} from '../../basis/basis';
import {Comment} from '../../cc-management/domain/core-component-node';
import {map} from 'rxjs/operators';
import {AgencyIdListValueSummary} from '../../agency-id-list-management/domain/agency-id-list';
import {NamespaceSummary} from '../../namespace-management/domain/namespace';

@Injectable()
export class CodeListService {

  constructor(private http: HttpClient) {
  }

  getCodeListSummaries(releaseId: number): Observable<CodeListSummary[]> {
    const params = new HttpParams()
        .set('releaseId', releaseId.toString());

    return this.http.get<CodeListSummary[]>('/api/code-lists/summaries', {params});
  }

  getCodeListList(request: CodeListListEntryRequest): Observable<PageResponse<CodeListListEntry>> {
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
    if (request.extensible && request.extensible.length === 1) {
      params = params.set('extensible', (request.extensible) ? 'true' : 'false');
    }
    if (request.newComponent && request.newComponent.length === 1) {
      params = params.set('newComponent', '' + request.newComponent[0]);
    }
    if (request.ownedByDeveloper !== undefined) {
      params = params.set('ownedByDeveloper', (request.ownedByDeveloper) ? 'true' : 'false');
    }
    if (request.namespaces && request.namespaces.length > 0) {
      params = params.set('namespaces', request.namespaces.map(e => '' + e).join(','));
    }

    return this.http.get<PageResponse<CodeListListEntry>>('/api/code-lists', {params}).pipe(
        map((res: PageResponse<CodeListListEntry>) => ({
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

  getCodeListDetails(manifestId): Observable<CodeListDetails> {
    return this.http.get<CodeListDetails>('/api/code-lists/' + manifestId).pipe(
        map((elm: CodeListDetails) => ({
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

  getPrevCodeListDetails(manifestId): Observable<CodeListDetails> {
    return this.http.get<CodeListDetails>('/api/code-lists/' + manifestId + '/prev');
  }

  create(releaseId: number, basedCodeListManifestId?: number): Observable<CodeListCreateResponse> {
    return this.http.post<CodeListCreateResponse>('/api/code-lists', {
      releaseId,
      basedCodeListManifestId: basedCodeListManifestId ? basedCodeListManifestId : null
    });
  }

  update(codeList: CodeListDetails, state?: string): Observable<any> {
    let body;
    if (state) {
      body = {
        releaseId: codeList.release.releaseId,
        toState: state
      };
      return this.http.patch('/api/code-lists/' + codeList.codeListManifestId + '/state', body);
    } else {
      body = {
        releaseId: codeList.release.releaseId,
        basedCodeListManifestId: (!!codeList.based) ? codeList.based.codeListManifestId : undefined,
        codeListName: codeList.name,
        listId: codeList.listId,
        agencyIdListValueManifestId: (!!codeList.agencyIdListValue) ? codeList.agencyIdListValue.agencyIdListValueManifestId : undefined,
        versionId: codeList.versionId,
        namespaceId: (!!codeList.namespace) ? codeList.namespace.namespaceId : undefined,
        definition: codeList.definition,
        remark: codeList.remark,
        deprecated: codeList.deprecated,
        valueList: codeList.valueList,
      };
    }

    return this.http.put('/api/code-lists/' + codeList.codeListManifestId, body);
  }

  updateState(codeList: CodeListDetails, state: string): Observable<any> {
    return this.update(codeList, state);
  }

  delete(...codeListManifestIds): Observable<any> {
    return this.http.patch<any>('/api/code-lists/mark-as-deleted', {
      codeListManifestIds
    });
  }

  restore(...codeListManifestIds): Observable<any> {
    return this.http.patch<any>('/api/code-lists/restore', {
      codeListManifestIds
    });
  }

  purge(...codeListManifestIds): Observable<any> {
    return this.http.delete<any>('/api/code-lists', {
      body: {
        codeListManifestIds
      }
    });
  }

  checkUniqueness(codeList: CodeListDetails): Observable<boolean> {
    let params = new HttpParams()
      .set('releaseId', '' + codeList.release.releaseId)
      .set('listId', codeList.listId)
      .set('agencyIdListValueManifestId', (!!codeList.agencyIdListValue) ? ('' + codeList.agencyIdListValue.agencyIdListValueManifestId) : '')
      .set('versionId', codeList.versionId);
    if (codeList.codeListManifestId) {
      params = params.set('codeListManifestId', '' + codeList.codeListManifestId);
    }

    return this.http.get<boolean>('/api/code-lists/check-uniqueness', {params});
  }

  checkNameUniqueness(codeList: CodeListDetails): Observable<boolean> {
    let params = new HttpParams()
      .set('releaseId', '' + codeList.release.releaseId)
      .set('codeListName', codeList.name);
    if (codeList.codeListManifestId) {
      params = params.set('codeListManifestId', '' + codeList.codeListManifestId);
    }

    return this.http.get<boolean>('/api/code-lists/check-name-uniqueness', {params});
  }

  makeNewRevision(codeList: CodeListDetails): Observable<any> {
    return this.http.patch('/api/code-lists/' + codeList.codeListManifestId + '/revise', {});
  }

  cancelRevision(manifestId: number): Observable<any> {
    const url = '/api/code-lists/' + manifestId + '/cancel';
    return this.http.patch<any>(url, {});
  }

  transferOwnership(manifestId: number, targetLoginId: string): Observable<any> {
    return this.http.patch<any>('/api/code-lists/' + manifestId + '/transfer', {
      targetLoginId
    });
  }

  uplift(codeList: CodeListListEntry, targetReleaseId: number): Observable<any> {
    const params = new HttpParams()
        .set('targetReleaseId', targetReleaseId.toString());

    return this.http.post<any>('/api/code-lists/' + codeList.codeListManifestId + '/uplift', {}, {params});
  }

  postComment(reference: string, text: string, prevCommentId?: number): Observable<any> {
    return this.http.post('/api/comments/' + reference, {
      reference,
      text,
      prevCommentId: prevCommentId ? prevCommentId : null
    });
  }

  editComment(commentId: number, text: string): Observable<any> {
    return this.http.put('/api/comments/' + commentId, {
      commentId,
      text,
    });
  }

  deleteComment(comment: Comment): Observable<any> {
    return this.http.put('/api/comments/' + comment.commentId, {
      commentId: comment.commentId,
      delete: true,
    });
  }

  getComments(reference): Observable<Comment[]> {
    return this.http.get<Comment[]>('/api/comments/' + reference);
  }
}
