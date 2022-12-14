import {PageRequest} from '../../basis/basis';
import {ParamMap} from '@angular/router';
import {HttpParams} from '@angular/common/http';
import {base64Decode, base64Encode} from '../../common/utility';

export class TenantListRequest {
    name: string;
    page: PageRequest = new PageRequest();

    constructor(paramMap?: ParamMap, defaultPageRequest?: PageRequest) {
        const q = (paramMap) ? paramMap.get('q') : undefined;
        const params = (q) ? new HttpParams({fromString: base64Decode(q)}) : new HttpParams(); 
        this.page.sortActive = params.get('sortActive');
        if (!this.page.sortActive) {
          this.page.sortActive = (defaultPageRequest) ? defaultPageRequest.sortActive : '';
        }
        this.page.sortDirection = params.get('sortDirection');
        if (!this.page.sortDirection) {
          this.page.sortDirection = (defaultPageRequest) ? defaultPageRequest.sortDirection : '';
        }
        if (params.get('pageIndex')) {
          this.page.pageIndex = Number(params.get('pageIndex'));
        } else {
          this.page.pageIndex = (defaultPageRequest) ? defaultPageRequest.pageIndex : 0;
        }
        if (params.get('pageSize')) {
          this.page.pageSize = Number(params.get('pageSize'));
        } else {
          this.page.pageSize = (defaultPageRequest) 
          ? defaultPageRequest.pageSize : 0;
        }
        this.name = params.get('name') || '';
    }

    toQuery(extras?): string {
        let params = new HttpParams()
          .set('sortActive', this.page.sortActive)
          .set('sortDirection', this.page.sortDirection)
          .set('pageIndex',  this.page.pageIndex)
          .set('pageSize',   this.page.pageSize);
    
     
        if (this.name && this.name.length > 0) {
          params = params.set('name', this.name);
        }
        if (extras) {
          Object.keys(extras).forEach(key => {
            params = params.set(key.toString(), extras[key]);
          });
        }
        const str = base64Encode(params.toString());
        return (str) ? 'q=' + str : undefined;
      }
}

export class TenantList {
    tenantId: number;
    name: string;
}

export class TenantInfo{
  tenantId: number;
  name: string;
  usersCount: number;
  businessCtxCount:number;
}

export class BusinessTenantContext{
  businessCtxId : number
	name : string
	checked : boolean
}

