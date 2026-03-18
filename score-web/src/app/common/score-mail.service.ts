import { Injectable, OnInit, inject } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable()
export class MailService implements OnInit {
  private http = inject(HttpClient);


  ngOnInit() {
  }

  sendMail(templateName: string, recipient: number | string, body: any): Observable<any> {
    const params = new HttpParams()
      .set('recipient', recipient);
    return this.http.post<any>('/api/mail/' + templateName, body, {params});
  }

}
