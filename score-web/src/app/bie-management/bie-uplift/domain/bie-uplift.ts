import {AbieFlatNode, AsbiepFlatNode, BbiepFlatNode, BbieScFlatNode, BieFlatNode, WrappedBieFlatNode} from '../../domain/bie-flat-tree';


export class BieUpliftSourceFlatNode extends WrappedBieFlatNode {
  target: BieUpliftTargetFlatNode;
  fixed: boolean;
  bieId: number;

  constructor(node: BieFlatNode) {
    super(node);
  }

  get type(): string {
    return this._node.bieType;
  }

  get path(): string {
    switch (this.type.toUpperCase()) {
      case 'ASBIEP':
        return (this._node as AsbiepFlatNode).asbiePath;
      case 'BBIEP':
        return (this._node as BbiepFlatNode).bbiePath;
      case 'BBIE_SC':
        return (this._node as BbieScFlatNode).bbieScPath;
      default:
        return this._node.path;
    }
  }

  get isMapped(): boolean {
    if (this.locked) {
      return true;
    }
    if (this.level === 0) {
      return true;
    }
    if (!this.used) {
      return true;
    }
    if (this.target) {
      return !this.derived;
    }
    return false;
  }

  equal(node: BieFlatNode) {
    return this._node === node;
  }
}

export class BieUpliftTargetFlatNode extends WrappedBieFlatNode {
  source: BieUpliftSourceFlatNode;
  reusedTolevelAsbiepId: number;
  emptyRequired: boolean;

  constructor(node: BieFlatNode) {
    super(node);
  }

  get type(): string {
    return this._node.bieType;
  }

  get path(): string {
    switch (this.type.toUpperCase()) {
      case 'ASBIEP':
        return (this._node as AsbiepFlatNode).asbiePath;
      case 'BBIEP':
        return (this._node as BbiepFlatNode).bbiePath;
      case 'BBIE_SC':
        return (this._node as BbieScFlatNode).bbieScPath;
      default:
        return this._node.path;
    }
  }

  equal(node: BieFlatNode) {
    return this._node === node;
  }
}

export class FindTargetAsccpManifestResponse {
  asccpManifestId: number;
}

export class UpliftNode {
  bieType: string;
  bieId: number;
  sourcePath: string;
  sourceManifestId: number;
  targetPath: string;
  targetManifestId: number;
  refTopLevelAsbiepId: number;

  constructor(bieType: string, bieId: number, sourcePath: string, targetPath?: string, refBie?: number) {
    this.bieType = bieType;
    this.bieId = bieId;
    this.sourcePath = sourcePath;
    this.targetPath = targetPath;
    this.refTopLevelAsbiepId = refBie;
  }
}

export class BieUpliftMap {
  sourceAsbiePathMap: Map<number, string>;
  targetAsbiePathMap: Map<number, string>;
  sourceBbiePathMap: Map<number, string>;
  targetBbiePathMap: Map<number, string>;
  sourceBbieScPathMap: Map<number, string>;
  targetBbieScPathMap: Map<number, string>;

  sourceUsedMap: Map<string, number>;
  targetUsedMap: Map<string, number>;

  unMatchedList: UpliftNode[];
  unMatchedMap: Map<string, UpliftNode>;

  constructor(obj) {
    this.sourceAsbiePathMap = new Map<number, string>();
    this.targetAsbiePathMap = new Map<number, string>();
    this.sourceBbiePathMap = new Map<number, string>();
    this.targetBbiePathMap = new Map<number, string>();
    this.sourceBbieScPathMap = new Map<number, string>();
    this.targetBbieScPathMap = new Map<number, string>();
    this.sourceUsedMap = new Map<string, number>();
    this.targetUsedMap = new Map<string, number>();
    this.unMatchedMap = new Map<string, UpliftNode>();
    this.unMatchedList = [];
    for (const k of Object.keys(obj.targetBbieScPathMap)) {
      this.targetUsedMap.set(obj.targetBbieScPathMap[k], Number(k));
      this.targetBbieScPathMap.set(Number(k), obj.targetBbieScPathMap[k]);
    }
    for (const k of Object.keys(obj.targetBbiePathMap)) {
      this.targetUsedMap.set(obj.targetBbiePathMap[k], Number(k));
      this.targetBbiePathMap.set(Number(k), obj.targetBbiePathMap[k]);
    }
    for (const k of Object.keys(obj.targetAsbiePathMap)) {
      this.targetUsedMap.set(obj.targetAsbiePathMap[k], Number(k));
      this.targetAsbiePathMap.set(Number(k), obj.targetAsbiePathMap[k]);
    }

    for (const k of Object.keys(obj.sourceAsbiePathMap)) {
      this.sourceUsedMap.set(obj.sourceAsbiePathMap[k], Number(k));
      this.sourceAsbiePathMap.set(Number(k), obj.sourceAsbiePathMap[k]);
      if (!this.targetAsbiePathMap.get(Number(k))) {
        this.unMatchedList.push(new UpliftNode('ASBIE', Number(k), obj.sourceAsbiePathMap[k], undefined));
      }
    }
    for (const k of Object.keys(obj.sourceBbiePathMap)) {
      this.sourceUsedMap.set(obj.sourceBbiePathMap[k], Number(k));
      this.sourceBbiePathMap.set(Number(k), obj.sourceBbiePathMap[k]);
      if (!this.targetBbiePathMap.get(Number(k))) {
        this.unMatchedList.push(new UpliftNode('BBIE', Number(k), obj.sourceBbiePathMap[k], undefined));
      }
    }
    for (const k of Object.keys(obj.sourceBbieScPathMap)) {
      this.sourceUsedMap.set(obj.sourceBbieScPathMap[k], Number(k));
      this.sourceBbieScPathMap.set(Number(k), obj.sourceBbieScPathMap[k]);
      if (!this.targetBbieScPathMap.get(Number(k))) {
        this.unMatchedList.push(new UpliftNode('BBIE_SC', Number(k), obj.sourceBbieScPathMap[k], undefined));
      }
    }
    this.unMatchedList = this.unMatchedList.sort((a, b) => a.sourcePath.localeCompare(b.sourcePath));
    this.unMatchedList.map(node => this.unMatchedMap.set(node.sourcePath, node));
  }
}

export class MatchInfo {
  name: string;
  bieType: string;
  bieId: number;
  sourcePath: string;
  sourceDisplayPath: string;
  sourceManifestId: number;
  targetPath: string;
  targetDisplayPath: string;
  targetManifestId: number;
  match: string;
  reuse: string;
  message: string;
  valid: boolean;

  constructor(source: BieUpliftSourceFlatNode) {
    const target = source.target;
    this.name = source.name;
    this.bieId = source.bieId;
    this.valid = false;
    this.message = '';
    switch (source.bieType.toUpperCase()) {
      case 'ABIE':
        break;
      case 'ASBIEP':
        this.bieType = 'ASBIE';
        this.sourceManifestId = (source._node as AsbiepFlatNode).asccNode.manifestId;
        this.sourcePath = (source._node as AsbiepFlatNode).asbiePath;
        if (target) {
          this.targetManifestId = (target._node as AsbiepFlatNode).asccNode.manifestId;
          this.targetPath = (target._node as AsbiepFlatNode).asbiePath;
        }
        break;
      case 'BBIEP':
        this.bieType = 'BBIE';
        this.sourceManifestId = (source._node as BbiepFlatNode).bccNode.manifestId;
        this.sourcePath = (source._node as BbiepFlatNode).bbiePath;
        if (target) {
          this.targetManifestId = (target._node as BbiepFlatNode).bccNode.manifestId;
          this.targetPath = (target._node as BbiepFlatNode).bbiePath;
        }
        break;
      case 'BBIE_SC':
        this.bieType = 'BBIE_SC';
        this.sourceManifestId = (source._node as BbieScFlatNode).bdtScNode.manifestId;
        this.sourcePath = (source._node as BbieScFlatNode).bbieScPath;
        if (target) {
          this.targetManifestId = (target._node as BbieScFlatNode).bdtScNode.manifestId;
          this.targetPath = (target._node as BbieScFlatNode).bbieScPath;
        }
        break;
    }
    this.reuse = '';
    this.sourceDisplayPath = '/' + source.parents.map(i => i.name).join('/').replace(/ /g, '');
    if (target) {
      this.targetDisplayPath = '/' + target.parents.map(i => i.name).join('/').replace(/ /g, '');
      if(source.fixed) {
        this.match = 'System';
      } else {
        this.match = 'Manual';
      }
    } else {
      this.targetDisplayPath = '';
      this.match = '';
    }
    if (source.derived) {
      this.reuse = 'Not selected';
      if (target && target.reusedTolevelAsbiepId) {
        this.reuse = 'Selected';
      }
    }
  }
}

export class BieValidationResponse {
  validations: MatchInfo[];
}
