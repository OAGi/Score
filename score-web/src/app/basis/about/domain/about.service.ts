import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {ProductInfo, WebPageInfo} from './about';

@Injectable()
export class AboutService {

  constructor(private http: HttpClient) {
  }


  getProductInfo(): Observable<ProductInfo[]> {
    return this.http.get<ProductInfo[]>('api/info/products');
  }

  getWebPageInfo(): Observable<WebPageInfo> {
    return this.http.get<WebPageInfo>('api/info/webpage');
  }

  updateWebPageInfo(webPageInfo: WebPageInfo): Observable<any> {
    return this.http.post('/api/info/webpage', {
      brand: webPageInfo.brand,
      favicon: webPageInfo.favicon,
      signInStatement: webPageInfo.signInStatement,
      componentStateColorSetMap: webPageInfo.componentStateColorSetMap,
      releaseStateColorSetMap: webPageInfo.releaseStateColorSetMap
    });
  }

}
