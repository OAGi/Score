import {WhoAndWhen} from '../../basis/basis';

export class LibrarySummary {
  libraryId: number = 0;
  name: string;
  state: string;
  readOnly: boolean;
  isDefault: boolean;
}

export class LibraryDetails {
  libraryId: number = 0;
  name: string;
  type: string;
  organization: string;
  link: string;
  domain: string;
  description: string;
  namespaceUri: string;
  namespacePrefix: string;
  state: string;
  readOnly: boolean;
  isDefault: boolean;

  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;
}

export class LibraryListEntry {
  libraryId: number = 0;
  name: string;
  type: string;
  organization: string;
  link: string;
  domain: string;
  description: string;
  state: string;
  readOnly: boolean;
  isDefault: boolean;

  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;
}

export interface DiscardLibraryCheck {
  discardable: boolean;
  message: string;
}

export interface LibraryReleaseDependency {
  releaseDepId?: number | null;
  releaseId: number;
  libraryId: number;
  libraryName: string;
  releaseNum: string;
  state: string;
  workingRelease: boolean;
}

export interface LibraryReleaseDependenciesResponse {
  currentDependencies: LibraryReleaseDependency[];
  availableDependencies: LibraryReleaseDependency[];
}
