import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {PageRequest, PageResponse} from '../../basis/basis';
import {
  AssignedBtListRequest,
  AssignedBusinessTerm,
  BieToAssign,
  BusinessTerm,
  BusinessTermListRequest
} from './business-term';

@Injectable()
export class BusinessTermService {

  constructor(private http: HttpClient) {
  }

  getBusinessTermList(request: BusinessTermListRequest, byAssignedBies: BieToAssign[]): Observable<PageResponse<BusinessTerm>> {
    let params = new HttpParams()
      .set('sortActive', request.page.sortActive)
      .set('sortDirection', request.page.sortDirection)
      .set('pageIndex', '' + request.page.pageIndex)
      .set('pageSize', '' + request.page.pageSize);
    if (request.filters.businessTerm) {
      params = params.set('businessTerm', request.filters.businessTerm);
    }
    if (request.filters.externalReferenceUri) {
      params = params.set('externalReferenceUri', request.filters.externalReferenceUri);
    }
    if (request.filters.externalReferenceId) {
      params = params.set('externalReferenceId', request.filters.externalReferenceId);
    }
    if (request.filters.bieId) {
      params = params.set('bieId', request.filters.bieId);
    }
    if (request.filters.bieType) {
      params = params.set('bieType', request.filters.bieType);
    }
    if (request.filters.searchByCC && byAssignedBies != null) {
      params = params.set('searchByCC', request.filters.searchByCC);
      params = params.set('byAssignedBieIds', byAssignedBies.map(bie => bie.bieId).join(','));
      params = params.set('byAssignedBieTypes', byAssignedBies.map(bie => bie.bieType).join(','));
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
    return this.http.get<PageResponse<BusinessTerm>>('/api/business_terms', {params});
  }

  getAssignedBusinessTermList(request: AssignedBtListRequest): Observable<PageResponse<AssignedBusinessTerm>> {
    const params = request.toParams();
    return this.http.get<PageResponse<AssignedBusinessTerm>>('/api/business_terms/assign', {params});
  }

  getAssignedBusinessTerm(type, id): Observable<AssignedBusinessTerm> {
    return this.http.get<AssignedBusinessTerm>('/api/business_terms/assign/' + type + '/' + id);
  }

  getBusinessTerm(id): Observable<BusinessTerm> {
    return this.http.get<BusinessTerm>('/api/business_term/' + id);
  }

  create(businessTerm: BusinessTerm): Observable<any> {
    if ('' + businessTerm.businessTermId === 'undefined' || !businessTerm.businessTermId) {
      businessTerm.businessTermId = null;
    }
    return this.http.put('/api/business_term', {
      businessTermId: businessTerm.businessTermId,
      businessTerm: businessTerm.businessTerm,
      comment: businessTerm.comment,
      externalReferenceUri: businessTerm.externalReferenceUri,
      externalReferenceId: businessTerm.externalReferenceId
    });
  }

  uploadFromFile(formData: FormData): Observable<any> {
    return this.http.post('/api/csv/business_terms', formData, {
      reportProgress: true,
      observe: 'events'
    });
  }

  update(businessTerm: BusinessTerm): Observable<any> {
    return this.http.post('/api/business_term/' + businessTerm.businessTermId, {
      businessTermId: businessTerm.businessTermId,
      businessTerm: businessTerm.businessTerm,
      comment: businessTerm.comment,
      externalReferenceUri: businessTerm.externalReferenceUri,
      externalReferenceId: businessTerm.externalReferenceId
    });
  }

  delete(...businessTermIds): Observable<any> {
    if (businessTermIds.length === 1) {
      return this.http.delete('/api/business_term/' + businessTermIds[0]);
    } else {
      return this.http.post<any>('/api/business_term/delete', {
        businessTermIdList: businessTermIds
      });
    }
  }

  assignBusinessTermToBie(businessTermId: number, biesToAssign: BieToAssign[], primaryIndicator: boolean, typeCode: string): Observable<any> {
    return this.http.put('/api/business_terms/assign', {
      biesToAssign,
      businessTermId,
      primaryIndicator,
      typeCode
    });
  }

  deleteAssignments(assignedBts: AssignedBusinessTerm[]): Observable<any> {
    const assignedBtList = assignedBts.map(a => {
      const b = new BieToAssign();
      b.bieId = a.assignedBizTermId;
      b.bieType = a.bieType;
      return b;
    });
    if (assignedBtList.length === 1) {
      return this.http.delete('/api/business_terms/assign/' + assignedBtList[0].bieType + '/' + assignedBtList[0].bieId);
    } else {
      return this.http.post<any>('/api/business_terms/assign/delete', {
        assignedBtList
      });
    }
  }

  makeAsPrimary(...assignedBizTermId): Observable<any> {
    return this.http.put('/api/assigned_business_term/primary', { assignedBizTermId } );
  }

  checkUniqueness(businessTerm: BusinessTerm): Observable<any> {
    return this.http.post('/api/business_terms/check_uniqueness', {
      businessTermId: businessTerm.businessTermId,
      businessTerm: businessTerm.businessTerm,
      externalReferenceUri: businessTerm.externalReferenceUri
    });
  }

  checkNameUniqueness(businessTerm: BusinessTerm): Observable<any> {
    return this.http.post('/api/business_terms/check_name_uniqueness', {
      businessTermId: businessTerm.businessTermId,
      businessTerm: businessTerm.businessTerm
    });
  }

  checkAssignmentUniqueness(bieId: number, bieType: string, businessTermId: number,
                            typeCode: string, primaryIndicator: boolean): Observable<any> {
    return this.http.post('/api/business_terms/assign/check_uniqueness', {
      biesToAssign: [{ bieId, bieType }],
      businessTermId,
      typeCode,
      primaryIndicator
    });
  }

  findIfPrimaryExist(bieId: number, bieType: string, primaryIndicator: boolean, typeCode: string): Observable<PageResponse<AssignedBusinessTerm>> {
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

  updateAssignment(assignedBusinessTerm: AssignedBusinessTerm): Observable<any> {
    return this.http.post('/api/business_terms/assign/' + assignedBusinessTerm.bieType + '/' + assignedBusinessTerm.assignedBizTermId, {
      bieType: assignedBusinessTerm.bieType,
      bieId: assignedBusinessTerm.bieId,
      typeCode: assignedBusinessTerm.typeCode,
      primaryIndicator: assignedBusinessTerm.primaryIndicator
    });
  }

  downloadCSV(): Observable<any> {
    return this.http.get('/api/csv/business_terms/template', {
      responseType: 'text'
    });
  }

}
