import { Injectable, inject } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BoxColorSet, WebPageInfo} from './about/domain/about';
import {Observable, tap} from 'rxjs';
import {decodeUnicodeBase64, encodeUnicodeBase64} from '../common/utility';

@Injectable({
  providedIn: 'root',
})
export class WebPageInfoService {
  private http = inject(HttpClient);


  WEB_PAGE_INFO_KEY = 'X-Score-WebPageInfo';

  private fetching = false;

  /** The page info that the stored text was last read as: a screen asks for it for every cell, and reading it (decoding and parsing the text) is not cheap. */
  private cached?: { item: string; info: WebPageInfo };

  get webPageInfo(): WebPageInfo {
    let item = localStorage.getItem(this.WEB_PAGE_INFO_KEY);
    if (!item) {
      if (!this.fetching) {
        this.fetching = true;
        this.load().subscribe({
          next: () => {
            this.fetching = false;
          },
          error: () => {
            this.fetching = false;
          }
        });
      }

      this.set(new WebPageInfo());
      item = localStorage.getItem(this.WEB_PAGE_INFO_KEY);
    }
    if (this.cached?.item !== item) {
      this.cached = {item, info: new WebPageInfo(JSON.parse(decodeUnicodeBase64(item)))};
    }
    return this.cached.info;
  }

  set(webPageInfo: WebPageInfo) {
    localStorage.setItem(this.WEB_PAGE_INFO_KEY, encodeUnicodeBase64(JSON.stringify(webPageInfo)));
  }

  reset() {
    localStorage.removeItem(this.WEB_PAGE_INFO_KEY);
  }

  load(): Observable<WebPageInfo> {
    return this.http.get<WebPageInfo>('api/info/webpages').pipe(tap(val => {
      try {
        this.set(val);
      } catch (ignore) {
        // The stored copy is only a cache: the page info that was loaded is still returned.
      }
      this.fetching = false;
    }));
  }

  update(webPageInfo: WebPageInfo): Observable<any> {
    return this.http.post('/api/info/webpages', {
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
