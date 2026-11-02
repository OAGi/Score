import {WhoAndWhen} from '../../basis/basis';

import {ScoreUser} from '../../authentication/domain/auth';

export class NamespaceDetails {
  namespaceId: number;
  libraryId: number;
  uri: string;
  prefix: string;
  description: string;
  standard: boolean;

  owner: ScoreUser;
  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;

  canEdit: boolean;
}

export class NamespaceListEntry {
  namespaceId: number;
  uri: string;
  prefix: string;
  description: string;
  standard: boolean;

  owner: ScoreUser;
  lastUpdated: WhoAndWhen;
}

export class NamespaceSummary {
  namespaceId: number;
  uri: string;
  standard: boolean;
}
