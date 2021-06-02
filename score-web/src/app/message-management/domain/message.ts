import {ScoreUser} from '../../authentication/domain/auth';
import {PageRequest} from '../../basis/basis';
import {ParamMap} from '@angular/router';
import {HttpParams} from '@angular/common/http';
import {base64Decode, base64Encode} from '../../common/utility';

export class CountOfUnreadMessages {
  countOfUnreadMessages: number;
}

export class MessageListRequest {
  filters: {
  };
  senderUsernameList: string[] = [];
  createdDate: {
    start: Date,
    end: Date,
  };
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
      this.page.pageSize = (defaultPageRequest) ? defaultPageRequest.pageSize : 0;
    }

    this.senderUsernameList = (params.get('senderUsernameList')) ? Array.from(params.get('senderUsernameList').split(',')) : [];
    this.createdDate = {
      start: (params.get('createdDateStart')) ? new Date(params.get('createdDateStart')) : null,
      end: (params.get('createdDateEnd')) ? new Date(params.get('createdDateEnd')) : null
    };
    this.filters = {
    };
  }

  toQuery(): string {
    let params = new HttpParams()
      .set('sortActive', this.page.sortActive)
      .set('sortDirection', this.page.sortDirection)
      .set('pageIndex', '' + this.page.pageIndex)
      .set('pageSize', '' + this.page.pageSize);

    if (this.senderUsernameList && this.senderUsernameList.length > 0) {
      params = params.set('senderUsernameList', this.senderUsernameList.join(','));
    }
    if (this.createdDate.start) {
      params = params.set('createdDateStart', '' + this.createdDate.start.toUTCString());
    }
    if (this.createdDate.end) {
      params = params.set('createdDateEnd', '' + this.createdDate.end.toUTCString());
    }
    const str = base64Encode(params.toString());
    return (str) ? 'q=' + str : undefined;
  }

}

export class MessageList {
  messageId: number;
  sender: ScoreUser;
  subject: string;
  timestamp: Date;
  read: boolean;
}

export class Message {
  messageId: number;
  sender: ScoreUser;
  subject: string;
  body: string;
  bodyContentType: string;
  timestamp: Date;
}
