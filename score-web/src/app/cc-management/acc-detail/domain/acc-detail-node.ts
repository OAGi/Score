import {hashCode} from '../../../common/utility';
import {BieEditNode} from '../../../bie-management/bie-edit/domain/bie-edit-node';

export class AccDetailNode {
  releaseId: number;
  guid: string;
  name: string;
  used: boolean;
  hasChild: boolean;

  constructor(obj?: AccDetailNode) {
    this.releaseId = obj && obj.releaseId || 0;
    this.guid = obj && obj.guid || '';
    this.name = obj && obj.name || '';
    this.used = obj && obj.used || false;
    this.hasChild = obj && obj.hasChild || false;
  }
}


/** Flat node with expandable and level information */
export class DynamicFlatNode {
  private $hashCode;

  constructor(public item: BieEditNode,
              public level = 0,
              public isLoading = false,
              public isNullObject = false) {
    this.reset();
  }

  get expandable() {
    return this.item.hasChild;
  }

  get guid() {
    return this.item.type + this.item.guid;
  }

  get hashCode() {
    return hashCode(this.item);
  }

  isChanged() {
    return this.$hashCode !== this.hashCode;
  }

  reset() {
    this.$hashCode = hashCode(this.item);
  }
}
