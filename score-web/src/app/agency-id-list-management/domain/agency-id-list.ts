import {ScoreUser} from '../../authentication/domain/auth';
import {Definition, WhoAndWhen} from '../../basis/basis';
import {ReleaseSummary} from '../../release-management/domain/release';

import {hashCode4Array} from '../../common/utility';
import {LibrarySummary} from '../../library-management/domain/library';
import {LogSummary} from '../../log-management/domain/log';
import {NamespaceSummary} from '../../namespace-management/domain/namespace';

export class AgencyIdListSummary {

  agencyIdListManifestId: number;
  agencyIdListId: number;
  guid: string;

  name: string;
  listId: string;
  versionId: string;

  deprecated: boolean;
  state: string;

  valueList: AgencyIdListValueSummary[];

}

export class AgencyIdListValueSummary {

  agencyIdListValueManifestId: number;
  agencyIdListValueId: number;
  agencyIdListManifestId: number;
  guid: string;

  value: string;
  name: string;

}

export class AgencyIdListListEntry {

  library: LibrarySummary;
  release: ReleaseSummary;

  agencyIdListManifestId: number;
  agencyIdListId: number;
  guid: string;
  enumTypeGuid: string;

  name: string;
  listId: string;
  versionId: string;

  definition: string;
  definitionSource: string;
  remark: string;

  namespaceId: number;

  deprecated: boolean;
  newComponent: boolean;
  state: string;
  access: string;

  log: LogSummary;
  module: string;

  owner: ScoreUser;
  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;

}

export class AgencyIdListDetails {

  library: LibrarySummary = new LibrarySummary();
  release: ReleaseSummary = new ReleaseSummary();

  agencyIdListManifestId: number;
  agencyIdListId: number;
  guid: string;
  enumTypeGuid: string;

  based: AgencyIdListSummary = new AgencyIdListSummary();

  name: string;
  listId: string;
  versionId: string;

  agencyIdListValue: AgencyIdListValueSummary = new AgencyIdListValueSummary();

  definition: Definition = new Definition();
  remark: string;

  namespace: NamespaceSummary = new NamespaceSummary();

  deprecated: boolean;
  newComponent: boolean;
  state: string;
  access: string;

  log: LogSummary = new LogSummary();
  module: string;

  owner: ScoreUser;
  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;

  valueList: AgencyIdListValueDetails[] = [];

}

export class AgencyIdListValueDetails {

  agencyIdListValueManifestId: number;
  agencyIdListValueId: number;
  guid: string;

  value: string;
  name: string;

  definition: Definition = new Definition();

  deprecated: boolean;
  isDeveloperDefault: boolean;
  isUserDefault: boolean;
  used: boolean;

  owner: ScoreUser;
  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;

}

export class AgencyIdList {
  libraryId: number;
  releaseId: number;
  workingRelease: boolean;
  agencyIdListManifestId: number;
  agencyIdListValueManifestId: number;
  name: string;
  basedAgencyIdListManifestId: number;
  basedAgencyIdListName: string;
  agencyId: number;
  versionId: string;
  namespaceId: number;
  namespaceUri: string;

  guid: string;
  listId: string;
  definition: string;
  definitionSource: string;
  remark: string;
  modulePath: string;

  deprecated: boolean;
  newComponent: boolean;
  state: string;
  access: string;

  releaseNum: string;
  releaseState: string;
  revisionNum: string;
  owner: ScoreUser;
  lastUpdatedBy: ScoreUser;

  lastUpdateTimestamp: Date;

  values: AgencyIdListValue[];
  prev: AgencyIdList;

  prevAgencyIdListManifestId: number;

  constructor(obj?: AgencyIdList) {
    if (!!obj) {
      this.libraryId = obj.libraryId;
      this.releaseId = obj.releaseId;
      this.workingRelease = obj.workingRelease;
      this.agencyIdListManifestId = obj.agencyIdListManifestId;
      this.agencyIdListValueManifestId = obj.agencyIdListValueManifestId;
      this.name = obj.name;
      this.basedAgencyIdListManifestId = obj.basedAgencyIdListManifestId;
      this.basedAgencyIdListName = obj.basedAgencyIdListName;
      this.agencyId = obj.agencyId;
      this.versionId = obj.versionId;
      this.namespaceId = obj.namespaceId;
      this.namespaceUri = obj.namespaceUri;

      this.guid = obj.guid;
      this.listId = obj.listId;
      this.definition = obj.definition;
      this.definitionSource = obj.definitionSource;
      this.remark = obj.remark;
      this.modulePath = obj.modulePath;

      this.deprecated = obj.deprecated;
      this.state = obj.state;
      this.access = obj.access;

      this.releaseNum = obj.releaseNum;
      this.releaseState = obj.releaseState;
      this.revisionNum = obj.revisionNum;
      this.owner = obj.owner;
      this.lastUpdatedBy = obj.lastUpdatedBy;

      this.lastUpdateTimestamp = obj.lastUpdateTimestamp;

      this.values = (obj.values) ? obj.values.map(val => new AgencyIdListValue(val)) : [];
      this.prev = obj.prev;

      this.prevAgencyIdListManifestId = obj.prevAgencyIdListManifestId;
    }
  }

  get hashCode(): number {
    return hashCode4Array(this.libraryId, this.releaseId,
      this.agencyIdListManifestId, this.agencyIdListValueManifestId, this.basedAgencyIdListManifestId,
      this.name, this.agencyId, this.versionId, this.namespaceId,
      this.guid, this.listId, this.definition, this.definitionSource, this.remark, this.modulePath,
      this.deprecated, this.state, this.values);
  }
}

export class AgencyIdListValue {
  agencyIdListValueManifestId: number;
  basedAgencyIdListValueManifestId: number;
  guid: string;
  value: string;
  name: string;
  definition: Definition = new Definition();

  deprecated: boolean;
  developerDefault: boolean;
  userDefault: boolean;
  used: boolean;

  get derived(): boolean {
    return !!this.basedAgencyIdListValueManifestId;
  }

  constructor(obj?: AgencyIdListValue) {
    if (!!obj) {
      this.agencyIdListValueManifestId = obj.agencyIdListValueManifestId;
      this.basedAgencyIdListValueManifestId = obj.basedAgencyIdListValueManifestId;
      this.guid = obj.guid;
      this.value = obj.value;
      this.name = obj.name;
      this.definition = obj.definition;

      this.deprecated = obj.deprecated;
      this.developerDefault = obj.developerDefault;
      this.userDefault = obj.userDefault;
      this.used = obj.used;
    }
  }

  get hashCode(): number {
    return hashCode4Array(this.agencyIdListValueManifestId, this.basedAgencyIdListValueManifestId,
        this.guid, this.value, this.name, this.definition,
        this.deprecated, this.developerDefault, this.userDefault, this.used);
  }
}

export class AgencyIdListCreateResponse {
  agencyIdListManifestId: number;
}
