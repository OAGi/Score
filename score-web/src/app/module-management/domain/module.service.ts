import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Module, ModuleList, SimpleModule} from './module';

@Injectable()
export class ModuleService {

  constructor(private http: HttpClient) {
  }

  getSimpleModules(): Observable<SimpleModule[]> {
    return this.http.get<SimpleModule[]>('/api/simple_modules');
  }

  getModuleList(): Observable<ModuleList[]> {
    return this.http.get<ModuleList[]>('/api/module_list');
  }

  getModule(moduleId): Observable<Module> {
    return this.http.get<Module>('/api/module/' + moduleId);
  }

  create(module: Module): Observable<any> {
    return this.http.put<any>('/api/module/' + module.moduleId, {
      module: module.module,
      namespaceId: module.namespaceId,
      moduleDependencies: module.moduleDependencies
    });
  }

  update(module: Module): Observable<any> {
    return this.http.post<any>('/api/module/' + module.moduleId, {
      module: module.module,
      namespaceId: module.namespaceId,
      moduleDependencies: module.moduleDependencies
    });
  }

}
