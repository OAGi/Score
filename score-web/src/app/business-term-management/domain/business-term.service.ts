import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {PageRequest, PageResponse} from '../../basis/basis';
import {
  AsbieBbieListEntry,
  AssignedBtListRequest,
  AssignedBusinessTermDetails,
  AssignedBusinessTermListEntry,
  BieToAssign,
  BusinessTermDetails,
  BusinessTermListEntry,
  BusinessTermListRequest
} from './business-term';
import {BieListRequest} from '../../bie-management/bie-list/domain/bie-list';
import {map} from 'rxjs/operators';

@Injectable()
export class BusinessTermService {

  constructor(private http: HttpClient) {
  }

  getBusinessTermList(request: BusinessTermListRequest, byAssignedBies: BieToAssign[]): Observable<PageResponse<BusinessTermListEntry>> {
    let params = new HttpParams();

    if (!!request.page.sortActive && !!request.page.sortDirection) {
      params = params.set('orderBy', ((request.page.sortDirection === 'desc') ? '-' : '+') + request.page.sortActive);
    }
    if (request.page.pageIndex >= 0) {
      params = params.set('pageIndex', request.page.pageIndex);
    }
    if (request.page.pageSize > 0) {
      params = params.set('pageSize', request.page.pageSize);
    }

    if (request.filters.businessTerm) {
      params = params.set('businessTerm', request.filters.businessTerm);
    }
    if (request.filters.externalReferenceUri) {
      params = params.set('externalReferenceUri', request.filters.externalReferenceUri);
    }
    if (request.filters.externalReferenceId) {
      params = params.set('externalReferenceId', request.filters.externalReferenceId);
    }
    if (request.filters.definition) {
      params = params.set('definition', request.filters.definition);
    }
    if (request.filters.bieId) {
      params = params.set('bieId', request.filters.bieId);
    }
    if (request.filters.bieType) {
      params = params.set('bieType', request.filters.bieType);
    }
    if (request.filters.searchByCC && byAssignedBies != null) {
      params = params.set('searchByCC', request.filters.searchByCC);
      params = params.set('byAssignedAsbieIdList', byAssignedBies.filter(e => e.bieType === 'ASBIE').map(e => e.bieId).join(','));
      params = params.set('byAssignedBbieIdList', byAssignedBies.filter(e => e.bieType === 'BBIE').map(e => e.bieId).join(','));
    }
    if (request.filters.typeCode) {
      params = params.set('typeCode', request.filters.typeCode);
    }
    if (request.filters.primaryIndicator) {
      params = params.set('primaryIndicator', request.filters.primaryIndicator);
    }
    if (request.updaterUsernameList.length > 0) {
      params = params.set('updaterUsernameList', request.updaterUsernameList.join(','));
    }
    if (request.updatedDate.start) {
      params = params.set('updateStart', '' + request.updatedDate.start.getTime());
    }
    if (request.updatedDate.end) {
      params = params.set('updateEnd', '' + request.updatedDate.end.getTime());
    }

    return this.http.get<PageResponse<BusinessTermListEntry>>('/api/business-terms', {params}).pipe(
        map((res: PageResponse<BusinessTermListEntry>) => ({
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

  getAsbieBbieListWithRequest(request: BieListRequest): Observable<PageResponse<AsbieBbieListEntry>> {
    let params = new HttpParams()
        .set('libraryId', '' + request.library.libraryId);

    if (!!request.page.sortActive && !!request.page.sortDirection) {
      params = params.set('orderBy', ((request.page.sortDirection === 'desc') ? '-' : '+') + request.page.sortActive);
    }
    if (request.page.pageIndex >= 0) {
      params = params.set('pageIndex', request.page.pageIndex);
    }
    if (request.page.pageSize > 0) {
      params = params.set('pageSize', request.page.pageSize);
    }
    if (request.ownerLoginIdList.length > 0) {
      params = params.set('ownerLoginIdList', request.ownerLoginIdList.join(','));
    }
    if (request.updaterLoginIdList.length > 0) {
      params = params.set('updaterLoginIdList', request.updaterLoginIdList.join(','));
    }
    if (request.updatedDate.start) {
      params = params.set('updateStart', '' + request.updatedDate.start.getTime());
    }
    if (request.updatedDate.end) {
      params = params.set('updateEnd', '' + request.updatedDate.end.getTime());
    }
    if (request.filters.propertyTerm) {
      params = params.set('topLevelAsccpPropertyTerm', request.filters.propertyTerm);
    }
    if (request.filters.den) {
      params = params.set('den', request.filters.den);
    }
    if (request.types) {
      params = params.set('types', request.types.join(','));
    }
    if (request.filters.businessContext) {
      params = params.set('businessContext', request.filters.businessContext);
    }
    if (request.filters.version) {
      params = params.set('version', request.filters.version);
    }
    if (request.filters.remark) {
      params = params.set('remark', request.filters.remark);
    }
    if (request.states.length > 0) {
      params = params.set('states', request.states.join(','));
    }
    if (request.access) {
      params = params.set('access', request.access);
    }
    if (request.releases) {
      params = params.set('releaseIds', request.releases.map(e => e.releaseId.toString()).join(','));
    }
    if (request.ownedByDeveloper !== undefined) {
      params = params.set('ownedByDeveloper', request.ownedByDeveloper.toString());
    }
    return this.http.get<PageResponse<AsbieBbieListEntry>>('/api/business-terms/asbie-bbie', {params}).pipe(
        map((res: PageResponse<AsbieBbieListEntry>) => ({
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

  confirmAsbieBbieListByIdAndType(biesToAssign: BieToAssign[]): Observable<AsbieBbieListEntry[]> {
    return this.http.get<AsbieBbieListEntry[]>('/api/business-terms/asbie-bbie/confirm', {
      params: new HttpParams()
          .set('asbieIdList', biesToAssign.filter(e => e.bieType === 'ASBIE').map(e => e.bieId.toString()).join(','))
          .set('bbieIdList', biesToAssign.filter(e => e.bieType === 'BBIE').map(e => e.bieId.toString()).join(','))
    }).pipe(
        map((entries: AsbieBbieListEntry[]) =>
            entries.map(entry => ({
              ...entry,
              created: {
                ...entry.created,
                when: new Date(entry.created.when),
              },
              lastUpdated: {
                ...entry.lastUpdated,
                when: new Date(entry.lastUpdated.when),
              }
            }))
        )
    );
  }

  getAssignedBusinessTermList(request: AssignedBtListRequest): Observable<PageResponse<AssignedBusinessTermListEntry>> {
    const params = request.toParams();
    return this.http.get<PageResponse<AssignedBusinessTermListEntry>>('/api/business-terms/assign', {params}).pipe(
        map((res: PageResponse<AssignedBusinessTermListEntry>) => ({
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

  getAssignedBusinessTerm(type: string, id: number): Observable<AssignedBusinessTermDetails> {
    return this.http.get<AssignedBusinessTermDetails>('/api/business-terms/assign/' + type.toLowerCase() + '/' + id).pipe(
        (map((elm: AssignedBusinessTermDetails) => ({
          ...elm,
          created: {
            ...elm.created,
            when: new Date(elm.created.when),
          },
          lastUpdated: {
            ...elm.lastUpdated,
            when: new Date(elm.lastUpdated.when),
          }
        })))
    );
  }

  getBusinessTermDetails(id): Observable<BusinessTermDetails> {
    return this.http.get<BusinessTermDetails>('/api/business-terms/' + id);
  }

  create(businessTerm: BusinessTermDetails): Observable<any> {
    if ('' + businessTerm.businessTermId === 'undefined' || !businessTerm.businessTermId) {
      businessTerm.businessTermId = null;
    }
    return this.http.post('/api/business-terms', {
      businessTermId: businessTerm.businessTermId,
      businessTerm: businessTerm.businessTerm,
      comment: businessTerm.comment,
      externalReferenceUri: businessTerm.externalReferenceUri,
      externalReferenceId: businessTerm.externalReferenceId
    });
  }

  uploadFromFile(formData: FormData): Observable<any> {
    return this.http.post('/api/business-terms/csv', formData, {
      reportProgress: true,
      observe: 'events'
    });
  }

  update(businessTerm: BusinessTermDetails): Observable<any> {
    return this.http.put('/api/business-terms/' + businessTerm.businessTermId, {
      businessTermId: businessTerm.businessTermId,
      businessTerm: businessTerm.businessTerm,
      comment: businessTerm.comment,
      externalReferenceUri: businessTerm.externalReferenceUri,
      externalReferenceId: businessTerm.externalReferenceId
    });
  }

  delete(...businessTermIds): Observable<any> {
    if (businessTermIds.length === 1) {
      return this.http.delete('/api/business-terms/' + businessTermIds[0]);
    } else {
      return this.http.delete<any>('/api/business-terms', {
        body: {
          businessTermIdList: businessTermIds
        }
      });
    }
  }

  assignBusinessTermToBie(businessTermId: number, biesToAssign: BieToAssign[], primaryIndicator: boolean, typeCode: string): Observable<any> {
    return this.http.post('/api/business-terms/' + businessTermId + '/assign', {
      biesToAssign,
      primaryIndicator,
      typeCode
    });
  }

  deleteAssignments(assignedBts: AssignedBusinessTermDetails[]): Observable<any> {
    if (assignedBts.length === 1) {
      return this.http.delete('/api/business-terms/assign/' + assignedBts[0].bieType.toLowerCase() + '/' + assignedBts[0].assignedBizTermId);
    } else {
      return this.http.delete<any>('/api/business-terms/assign', {
        body: {
          assignedAsbieBizTermIdList: assignedBts.filter(e => e.bieType === 'ASBIE').map(e => e.assignedBizTermId),
          assignedBbieBizTermIdList: assignedBts.filter(e => e.bieType === 'BBIE').map(e => e.assignedBizTermId)
        }
      });
    }
  }

  makeAsPrimary(...assignedBizTermId): Observable<any> {
    return this.http.put('/api/assigned_business_term/primary', { assignedBizTermId } );
  }

  checkUniqueness(businessTermId: number, businessTerm: string, externalReferenceUri: string): Observable<boolean> {
    let params = new HttpParams()
        .set('businessTerm', businessTerm)
        .set('externalReferenceUri', externalReferenceUri);

    if (!!businessTermId) {
      params = params.set('businessTermId', '' + businessTermId);
    }

    return this.http.get<boolean>('/api/business-terms/check-uniqueness', {
      params
    });
  }

  checkNameUniqueness(businessTermId: number, businessTerm: string): Observable<boolean> {
    let params = new HttpParams()
        .set('businessTerm', businessTerm);

    if (!!businessTermId) {
      params = params.set('businessTermId', '' + businessTermId);
    }

    return this.http.get<boolean>('/api/business-terms/check-name-uniqueness', {
      params
    });
  }

  checkAssignmentUniqueness(bieId: number, bieType: string, businessTermId: number,
                            typeCode: string, primaryIndicator: boolean): Observable<boolean> {
    let params = new HttpParams()
        .set('businessTermId', '' + businessTermId)
        .set('primaryIndicator', (!!primaryIndicator) ? primaryIndicator : false);
    if (!!typeCode) {
      params = params.set('typeCode', typeCode);
    }

    if (bieType === 'ASBIE') {
      params = params.set('asbieId', '' + bieId);
    } else if (bieType === 'BBIE') {
      params = params.set('bbieId', '' + bieId);
    }

    return this.http.get<boolean>('/api/business-terms/assign/check-uniqueness', {
      params
    });
  }

  findIfPrimaryExist(bieId: number, bieType: string, primaryIndicator: boolean, typeCode: string): Observable<PageResponse<AssignedBusinessTermListEntry>> {
    if (primaryIndicator) {
      const req = new AssignedBtListRequest();
      req.page = new PageRequest('lastUpdateTimestamp', 'desc', 0, 10);
      req.filters.typeCode = typeCode;
      req.filters.primaryIndicator = primaryIndicator;
      req.filters.searchByCC = 'true';
      req.filters.bieTypes = [bieType];
      req.filters.bieId = bieId;
      return this.getAssignedBusinessTermList(req);
    }
  }

  updateAssignment(assignedBusinessTerm: AssignedBusinessTermDetails): Observable<any> {
    return this.http.put('/api/business-terms/assign/' + assignedBusinessTerm.bieType + '/' + assignedBusinessTerm.assignedBizTermId, {
      bieType: assignedBusinessTerm.bieType,
      bieId: assignedBusinessTerm.bieId,
      typeCode: assignedBusinessTerm.typeCode,
      primaryIndicator: assignedBusinessTerm.primaryIndicator
    });
  }

  downloadCSV(): Observable<any> {
    return this.http.get('/api/business-terms/csv/template', {
      responseType: 'text'
    });
  }

}
