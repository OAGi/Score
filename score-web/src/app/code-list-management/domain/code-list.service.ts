import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {CodeList, CodeListForList, CodeListForListRequest, SimpleAgencyIdListValue} from './code-list';
import {PageResponse} from '../../basis/basis';
import {CcCreateResponse, Comment} from '../../cc-management/domain/core-component-node';

@Injectable()
export class CodeListService {

  constructor(private http: HttpClient) {
  }

  getCodeListList(request: CodeListForListRequest): Observable<PageResponse<CodeListForList>> {
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

    return this.http.get<PageResponse<CodeListForList>>('/api/code_list', {params});
  }

  getCodeList(manifestId): Observable<CodeList> {
    return this.http.get<CodeList>('/api/code_list/' + manifestId);
  }

  getCodeListRevision(manifestId): Observable<CodeList> {
    return this.http.get<CodeList>('/api/code_list/' + manifestId + '/revision');
  }

  getCodeLists(): Observable<PageResponse<CodeList>> {
    const params = new HttpParams()
      .set('sortActive', '')
      .set('sortDirection', '')
      .set('pageIndex', '-1')
      .set('pageSize', '-1');

    return this.http.get<PageResponse<CodeList>>('/api/code_list', {params});
  }

  getSimpleAgencyIdListValues(): Observable<SimpleAgencyIdListValue[]> {
    return this.http.get<SimpleAgencyIdListValue[]>('/api/simple_agency_id_list_values');
  }

  create(releaseId: number, basedCodeListManifestId?: number): Observable<CcCreateResponse> {
    return this.http.put<CcCreateResponse>('/api/code_list', {
      releaseId,
      basedCodeListManifestId: basedCodeListManifestId ? basedCodeListManifestId : null
    });
  }

  update(codeList: CodeList, state?: string): Observable<any> {
    let body;
    if (state) {
      body = {
        releaseId: codeList.releaseId,
        state
      };
    } else {
      body = {
        releaseId: codeList.releaseId,
        basedCodeListManifestId: codeList.basedCodeListManifestId,
        codeListName: codeList.codeListName,
        listId: codeList.listId,
        agencyId: codeList.agencyId,
        versionId: codeList.versionId,
        namespaceId: codeList.namespaceId,
        definition: codeList.definition,
        definitionSource: codeList.definitionSource,
        remark: codeList.remark,
        extensible: codeList.extensible,
        codeListValues: codeList.codeListValues,
        deprecated: codeList.deprecated
      };
    }

    return this.http.post('/api/code_list/' + codeList.codeListManifestId, body);
  }

  updateState(codeList: CodeList, state: string): Observable<any> {
    return this.update(codeList, state);
  }

  makeNewRevision(codeList: CodeList): Observable<any> {
    return this.http.post('/api/code_list/' + codeList.codeListManifestId + '/revision', {});
  }

  delete(...codeListManifestIds): Observable<any> {
    return this.http.post<any>('/api/code_list/delete', {
      codeListManifestIds
    });
  }

  restore(...codeListManifestIds): Observable<any> {
    return this.http.post<any>('/api/code_list/restore', {
      codeListManifestIds
    });
  }

  checkUniqueness(codeList: CodeList): Observable<boolean> {
    let params = new HttpParams()
      .set('releaseId', '' + codeList.releaseId)
      .set('listId', codeList.listId)
      .set('agencyId', '' + codeList.agencyId)
      .set('versionId', codeList.versionId);
    if (codeList.codeListManifestId) {
      params = params.set('codeListManifestId', '' + codeList.codeListManifestId);
    }

    return this.http.get<boolean>('/api/code_list/check_uniqueness', {params});
  }

  checkNameUniqueness(codeList: CodeList): Observable<boolean> {
    let params = new HttpParams()
      .set('releaseId', '' + codeList.releaseId)
      .set('codeListName', codeList.codeListName);
    if (codeList.codeListManifestId) {
      params = params.set('codeListManifestId', '' + codeList.codeListManifestId);
    }

    return this.http.get<boolean>('/api/code_list/check_name_uniqueness', {params});
  }

  cancelRevision(manifestId: number): Observable<any> {
    const url = '/api/code_list/' + manifestId + '/revision/cancel';
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
    return this.http.post<any>('/api/code_list/' + manifestId + '/transfer_ownership', {
      targetLoginId
    });
  }

  uplift(codeList: CodeListForList, targetReleaseId: number): Observable<any> {
    return this.http.post<any>('/api/code_list/' + codeList.codeListManifestId + '/uplift', {
      targetReleaseId: targetReleaseId
    });
  }
}
