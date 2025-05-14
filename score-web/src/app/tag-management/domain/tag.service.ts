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

  appendTag(type: string, manifestId: number, tagId: number): Observable<any> {
    return this.http.post('/api/tags/' + tagId + '/' + type.toLowerCase() + '/' + manifestId, {});
  }

  removeTag(type: string, manifestId: number, tagId: number): Observable<any> {
    return this.http.delete('/api/tags/' + tagId + '/' + type.toLowerCase() + '/' + manifestId, {});
  }

  create(tag: Tag): Observable<any> {
    return this.http.post('/api/tags', {
      name: tag.name,
      textColor: tag.textColor,
      backgroundColor: tag.backgroundColor,
      description: tag.description
    });
  }

  update(tag: Tag): Observable<any> {
    return this.http.put('/api/tags/' + tag.tagId, {
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
