import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {ProductInfo, SignInPageInfo} from './about';

@Injectable()
export class AboutService {

  constructor(private http: HttpClient) {
  }


  getProductInfo(): Observable<ProductInfo[]> {
    return this.http.get<ProductInfo[]>('api/info/products');
  }

  getSignInPageInfo(): Observable<SignInPageInfo> {
    return this.http.get<SignInPageInfo>('api/info/pages/signin');
  }
}
