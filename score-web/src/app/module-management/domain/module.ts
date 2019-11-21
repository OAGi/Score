export class SimpleModule {
  moduleId: number;
  module: string;
}

export class Module {
  moduleId: number;
  module: string;
  namespaceId: number;

  moduleDependencies: ModuleDependency[] = [];
}

export class ModuleDependency {
  moduleDepId: number;
  dependencyType: string;
  relatedModuleId: number;
  guid: string;
}

export class ModuleList {
  moduleId: number;
  module: string;
  namespace: string;
  owner: string;
  lastUpdatedBy: string;
  lastUpdateTimestamp: Date;
  sinceRelease: string;
  canEdit: boolean;
}
