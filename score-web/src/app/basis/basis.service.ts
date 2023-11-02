import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BoxColorSet, WebPageInfo} from './about/domain/about';
import {Observable, tap} from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class WebPageInfoService {

  WEB_PAGE_INFO_KEY = 'X-Score-WebPageInfo';

  private fetching = false;

  constructor(private http: HttpClient) {
  }

  get webPageInfo(): WebPageInfo {
    let item = localStorage.getItem(this.WEB_PAGE_INFO_KEY);
    if (!item) {
      this.load();

      this.set(new WebPageInfo());
      item = localStorage.getItem(this.WEB_PAGE_INFO_KEY);
    }
    return new WebPageInfo(JSON.parse(atob(item)));
  }

  set(webPageInfo: WebPageInfo) {
    localStorage.setItem(this.WEB_PAGE_INFO_KEY, btoa(JSON.stringify(webPageInfo)));
  }

  reset() {
    localStorage.removeItem(this.WEB_PAGE_INFO_KEY);
  }

  load(): Observable<WebPageInfo> {
    return this.http.get<WebPageInfo>('api/info/webpage').pipe(tap(val => {
      this.set(val);
    }));
  }

  update(webPageInfo: WebPageInfo): Observable<any> {
    return this.http.post('/api/info/webpage', {
      brand: webPageInfo.brand,
      favicon: webPageInfo.favicon,
      signInStatement: webPageInfo.signInStatement,
      componentStateColorSetMap: webPageInfo.componentStateColorSetMap,
      releaseStateColorSetMap: webPageInfo.releaseStateColorSetMap,
      userRoleColorSetMap: webPageInfo.userRoleColorSetMap
    });
  }

  // delegate methods
  get brand(): string {
    return this.webPageInfo.brand;
  }

  set brand(val: string) {
    this.webPageInfo.brand = val;
  }

  get favicon(): string {
    return this.webPageInfo.favicon;
  }

  set favicon(val: string) {
    this.webPageInfo.favicon = val;
  }

  get signInStatement(): string {
    return this.webPageInfo.signInStatement;
  }

  set signInStatement(val: string) {
    this.webPageInfo.signInStatement = val;
  }

  getComponentStateColorSet(state: string): BoxColorSet | undefined {
    return this.webPageInfo.getComponentStateColorSet(state);
  }

  getReleaseStateColorSet(state: string): BoxColorSet | undefined {
    return this.webPageInfo.getReleaseStateColorSet(state);
  }

  getUserRoleColorSet(state: string): BoxColorSet | undefined {
    return this.webPageInfo.getUserRoleColorSet(state);
  }

}
