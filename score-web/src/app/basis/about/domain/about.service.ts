import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {ProductInfo} from './about';

@Injectable()
export class AboutService {

  constructor(private http: HttpClient) {
  }


  getProductInfo(): Observable<ProductInfo[]> {
    return this.http.get<ProductInfo[]>('api/info/products');
  }
}
