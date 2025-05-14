import {PageRequest} from '../../basis/basis';
import {ParamMap} from '@angular/router';
import {HttpParams} from '@angular/common/http';
import {base64Decode, base64Encode} from '../../common/utility';

export class LogListRequest {
  reference: string;
  page: PageRequest = new PageRequest();

  constructor(paramMap?: ParamMap, defaultPageRequest?: PageRequest) {
    const q = (paramMap) ? paramMap.get('q') : undefined;
    const params = (q) ? new HttpParams({fromString: base64Decode(q)}) : new HttpParams();

    this.page.sortActive = params.get('sortActive') || (defaultPageRequest) ? defaultPageRequest.sortActive : '';
    this.page.sortDirection = params.get('sortDirection') || (defaultPageRequest) ? defaultPageRequest.sortDirection : '';
    this.page.pageIndex = Number(params.get('pageIndex') || (defaultPageRequest) ? defaultPageRequest.pageIndex : 0);
    this.page.pageSize = Number(params.get('pageSize') || (defaultPageRequest) ? defaultPageRequest.pageSize : 10);
  }

  toQuery(): string {
    const params = new HttpParams()
      .set('sortActive', this.page.sortActive)
      .set('sortDirection', this.page.sortDirection)
      .set('pageIndex', '' + this.page.pageIndex)
      .set('pageSize', '' + this.page.pageSize);

    const str = base64Encode(params.toString());
    return (str) ? 'q=' + str : undefined;
  }
}

export class LogSummary {
  logId: number;
  revisionNum: number;
  revisionTrackingNum: number;
}

export class Log {
  logId: number;
  hash: string;
  revisionNum: number;
  revisionTrackingNum: number;
  logAction: string;
  snapshot: string;
  prevLogId: number;
  loginId: string;
  timestamp: number[];
  developer: boolean;
}

function getValueFromObj(obj: any, keyToken: string) {
  const tokens = keyToken.split('->');
  for (const token of tokens) {
    if (!(obj)) {
      return undefined;
    }
    obj = obj[token];
  }
  return obj === undefined ? undefined : obj.toString();
}

export class CcSnapshot {
  guid: string;
  component: string;
  den: string;
  definition: string;
  definitionSource: string;
  state: string;
  ownerUserLoginId: string;
  deprecated: boolean;

  constructor(obj: any) {
    this.guid = obj.guid;
    this.component = obj.component;
    this.den = getValueFromObj(obj, '_metadata->' + this.component + '->den' );
    this.definition = obj.definition;
    this.definitionSource = obj.definitionSource;
    this.state = obj.state;
    this.ownerUserLoginId = getValueFromObj(obj, 'ownerUser->username');
    this.deprecated = obj.deprecated;
  }
}

export class AssociationSnapshot {
  guid: string;
  component: string;
  den: string;
  definition: string;
  definitionSource: string;
  cardinalityMin: number;
  cardinalityMax: number;
  defaultValue: string;
  fixedValue: string;
  entityType: string;
  index: number;
  color: string;
  deprecated: boolean;
  nillable: boolean;

  constructor(obj: any) {
    this.index = 0;
    this.guid = obj.guid;
    this.component = obj.component;
    if (this.component.toUpperCase() === 'ASCC') {
      this.den = getValueFromObj(obj, 'toAsccp->den');
    } else {
      this.den = getValueFromObj(obj, 'toBccp->den');
    }
    this.definition = obj.definition;
    this.definitionSource = obj.definitionSource;
    this.cardinalityMin = obj.cardinalityMin;
    this.cardinalityMax = obj.cardinalityMax;
    this.defaultValue = obj.defaultValue;
    this.fixedValue = obj.fixedValue;
    this.entityType = obj.entityType;
    this.deprecated = obj.deprecated;
    this.nillable = obj.nillable;
  }

  isEqual(other: AssociationSnapshot): boolean {
    return JSON.stringify(this) === JSON.stringify(other);
  }
}

export class AccSnapshot extends CcSnapshot {
  abstract: boolean;
  componentType: string;
  objectClassTerm: string;
  objectClassQualifier: string;
  namespaceUrl: string;
  basedAccObjectClassTerm: string;
  associations: AssociationSnapshot[];

  constructor(obj: any) {
    super(obj);
    this.abstract = obj.abstract;
    this.componentType = obj.componentType;
    this.objectClassTerm = obj.objectClassTerm;
    this.objectClassQualifier = obj.objectClassQualifier;
    this.namespaceUrl = getValueFromObj(obj, 'namespace->uri');
    this.basedAccObjectClassTerm = getValueFromObj(obj, 'basedAcc->objectClassTerm');
    this.associations = [];
    obj.associations.forEach(o => {
      this.associations.push(new AssociationSnapshot(o));
    });
  }
}

export class AsccpSnapshot extends CcSnapshot {
  propertyTerm: string;
  roleOfAccObjectClassTerm: string;
  reusable: boolean;
  nillable: boolean;
  namespaceUrl: string;
  decoration: string;

  constructor(obj: any) {
    super(obj);
    this.propertyTerm = obj.propertyTerm;
    this.roleOfAccObjectClassTerm = getValueFromObj(obj, 'roleOfAcc->den');
    this.reusable = obj.reusable;
    this.nillable = obj.nillable;
    this.namespaceUrl = getValueFromObj(obj, 'namespace->uri');
    this.decoration = obj.decoration;
  }
}

export class BccpSnapshot extends CcSnapshot {
  abstract: boolean;
  bdtDen: string;
  componentType: string;
  propertyTerm: string;
  nillable: boolean;
  defaultValue: string;
  fixedValue: string;
  namespaceUrl: string;

  constructor(obj: any) {
    super(obj);
    this.componentType = obj.componentType;
    this.propertyTerm = obj.propertyTerm;
    this.bdtDen = getValueFromObj(obj, 'bdt->den');
    this.nillable = obj.nillable;
    this.namespaceUrl = getValueFromObj(obj, 'namespace->uri');
    this.defaultValue = obj.defaultValue;
    this.fixedValue = obj.fixedValue;
  }
}

export class DtSnapshot extends CcSnapshot {
  qualifier: string;
  dataTypeTerm: string;
  representationTerm: string;
  namespaceUrl: string;
  dtScList: DtScSnapshot[];

  constructor(obj: any) {
    super(obj);

    this.qualifier = obj.qualifier;
    this.dataTypeTerm = obj.dataTypeTerm;
    this.representationTerm = obj.representationTerm;
    this.namespaceUrl = getValueFromObj(obj, 'namespace->uri');
    this.dtScList = [];
    obj.supplementaryComponents.forEach(o => {
      this.dtScList.push(new DtScSnapshot(o));
    });
  }
}

export class DtScSnapshot {
  index: number;
  color: string;
  guid: string;
  component: string;
  propertyTerm: string;
  representationTerm: string;
  cardinalityMin: number;
  cardinalityMax: number;
  definition: string;
  definitionSource: string;
  defaultValue: string;
  fixedValue: string;

  constructor(obj: any) {
    this.index = 0;
    this.guid = obj.guid;
    this.component = obj.component;
    this.propertyTerm = obj.propertyTerm;
    this.representationTerm = obj.representationTerm;
    this.cardinalityMin = obj.cardinalityMin;
    this.cardinalityMax = obj.cardinalityMax;
    this.definition = obj.definition;
    this.definitionSource = obj.definitionSource;
    this.defaultValue = obj.defaultValue;
    this.fixedValue = obj.fixedValue;
  }

  isEqual(other: DtScSnapshot): boolean {
    return JSON.stringify(this) === JSON.stringify(other);
  }
}

export class SnapshotPair {
  before: CcSnapshot;
  after: CcSnapshot;

  associations: SnapshotPair[];

  constructor(before: any, after: any) {
    if (before.component !== after.component) {
      throw new Error();
    }

    if (before.component.toUpperCase() === 'ACC') {
      this.before = new AccSnapshot(before);
      this.after = new AccSnapshot(after);
    } else if (before.component.toUpperCase() === 'ASCCP') {
      this.before = new AsccpSnapshot(before);
      this.after = new AsccpSnapshot(after);
    } else if (before.component.toUpperCase() === 'BCCP') {
      this.before = new BccpSnapshot(before);
      this.after = new BccpSnapshot(after);
    } else if (before.component.toUpperCase() === 'DT') {
      this.before = new DtSnapshot(before);
      this.after = new DtSnapshot(after);
    } else if (before.component.toUpperCase() === 'ASCC' ||
               before.component.toUpperCase() === 'BCC' ||
               before.component.toUpperCase() === 'DTSC') {
      this.before = before;
      this.after = after;
    } else {
      this.before = new CcSnapshot(before);
      this.after = new CcSnapshot(after);
    }

    if (this.component.toUpperCase() === 'ACC') {
      // init index property for associations
      const beforeAssociations = (this.before as AccSnapshot).associations;
      const afterAssociations = (this.after as AccSnapshot).associations;

      this.associations = [];

      let beforeIndex = 1;
      for (const beforeAssociation of beforeAssociations) {
        beforeAssociation.index = beforeIndex;

        let afterIndex = beforeIndex;
        let isExist = false;
        for (const afterAssociation of afterAssociations) {
          if (afterAssociation.index > 0) {
            continue;
          }

          if (beforeAssociation.guid === afterAssociation.guid) {
            isExist = true;
            afterAssociation.index = afterIndex++;
            this.associations.push(new SnapshotPair(beforeAssociation, afterAssociation));
            break;
          }

          afterIndex++;
        }

        if (!isExist) {
          const emptyAssociation = new AssociationSnapshot({
            component: beforeAssociation.component
          });
          this.associations.push(new SnapshotPair(beforeAssociation, emptyAssociation));
        }

        beforeIndex++;
      }

      let afterIndex = 0;
      for (const afterAssociation of afterAssociations) {
        if (afterAssociation.index > 0) {
          afterIndex++;
          continue;
        }

        const emptyAssociation = new AssociationSnapshot({
          component: afterAssociation.component
        });

        this.associations.splice(afterIndex++, 0,
          new SnapshotPair(emptyAssociation, afterAssociation));
      }
    }

    else if (this.component.toUpperCase() === 'DT') {
      // init index property for associations
      const beforeAssociations = (this.before as DtSnapshot).dtScList;
      const afterAssociations = (this.after as DtSnapshot).dtScList;

      this.associations = [];

      let beforeIndex = 1;
      for (const beforeAssociation of beforeAssociations) {
        beforeAssociation.index = beforeIndex;

        let afterIndex = beforeIndex;
        let isExist = false;
        for (const afterAssociation of afterAssociations) {
          if (afterAssociation.index > 0) {
            continue;
          }

          if (beforeAssociation.guid === afterAssociation.guid) {
            isExist = true;
            afterAssociation.index = afterIndex++;
            this.associations.push(new SnapshotPair(beforeAssociation, afterAssociation));
            break;
          }

          afterIndex++;
        }

        if (!isExist) {
          const emptyAssociation = new DtScSnapshot({
            component: beforeAssociation.component
          });
          this.associations.push(new SnapshotPair(beforeAssociation, emptyAssociation));
        }

        beforeIndex++;
      }

      let afterIndex = 0;
      for (const afterAssociation of afterAssociations) {
        if (afterAssociation.index > 0) {
          afterIndex++;
          continue;
        }

        const emptyAssociation = new DtScSnapshot({
          component: afterAssociation.component
        });

        this.associations.splice(afterIndex++, 0,
          new SnapshotPair(emptyAssociation, afterAssociation));
      }
    }
  }

  get component(): string {
    return this.before.component;
  }

  get revisionAction(): string {
    if (this.before.state === this.after.state) {
      return '';
    }

    if (this.after.state === undefined) {
      return (!this.before.state) ? 'Added' : 'Deleted';
    } else {
      return (!this.before.state) ? 'Added' : 'Modified';
    }
  }

  get revisionActionAssociation(): string {
    if (this.before === this.after) {
      return '';
    }

    if (this.after.guid === undefined) {
      return (!this.before.guid) ? 'Added' : 'Deleted';
    } else {
      return (!this.before.guid) ? 'Added' : 'Modified';
    }
  }
}
