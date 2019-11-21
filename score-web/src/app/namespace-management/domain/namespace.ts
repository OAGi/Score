export class Namespace {
  namespaceId: number;
  uri: string;
  prefix: string;
  description: string;
}

export class NamespaceList {
  namespaceId: number;
  uri: string;
  prefix: string;
  owner: string;
  lastUpdateTimestamp: Date;
  description: string;
  canEdit: boolean;
}

export class SimpleNamespace {
  namespaceId: number;
  uri: string;
}
