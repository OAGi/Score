import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Namespace, NamespaceList, SimpleNamespace} from './namespace';
import {SimpleModule} from '../../module-management/domain/module';

@Injectable()
export class NamespaceService {

  constructor(private http: HttpClient) {
  }

  getNamespaceList(): Observable<NamespaceList[]> {
    return this.http.get<NamespaceList[]>('/api/namespace_list');
  }

  getNamespace(namespaceId): Observable<Namespace> {
    return this.http.get<Namespace>('/api/namespace/' + namespaceId);
  }

  getSimpleNamespaces(): Observable<SimpleNamespace[]> {
    return this.http.get<SimpleNamespace[]>('/api/simple_namespaces');
  }

  getSimpleModules(): Observable<SimpleModule[]> {
    return this.http.get<SimpleModule[]>('/api/simple_modules');
  }

  create(namespace: Namespace): Observable<any> {
    return this.http.put<any>('/api/namespace', {
      uri: namespace.uri,
      prefix: namespace.prefix,
      description: namespace.description
    });
  }

  update(namespace: Namespace): Observable<any> {
    return this.http.post<any>('/api/namespace/' + namespace.namespaceId, {
      uri: namespace.uri,
      prefix: namespace.prefix,
      description: namespace.description
    });
  }

}
