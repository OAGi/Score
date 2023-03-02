import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Tag} from './tag';

@Injectable()
export class TagService {

  constructor(private http: HttpClient) {
  }

  getTags(): Observable<Tag[]> {
    return this.http.get<Tag[]>('/api/tags');
  }

  toggleTag(type: string, manifestId: number, name: string): Observable<any> {
    return this.http.post('/api/tags/' + type + '/' + manifestId, {
      name
    });
  }

  add(tag: Tag): Observable<any> {
    return this.http.put('/api/tags', {
      name: tag.name,
      textColor: tag.textColor,
      backgroundColor: tag.backgroundColor,
      description: tag.description
    });
  }

  update(tag: Tag): Observable<any> {
    return this.http.post('/api/tags/' + tag.tagId, {
      name: tag.name,
      textColor: tag.textColor,
      backgroundColor: tag.backgroundColor,
      description: tag.description
    });
  }

  discard(tag: Tag): Observable<any> {
    return this.http.delete('/api/tags/' + tag.tagId);
  }

}
