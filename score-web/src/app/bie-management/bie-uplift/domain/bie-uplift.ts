import {AsbiepFlatNode, BbiepFlatNode, BbieScFlatNode, BieFlatNode, WrappedBieFlatNode} from '../../domain/bie-flat-tree';
import {getKey} from '../../../common/flat-tree';


export class BieUpliftSourceFlatNode extends WrappedBieFlatNode {
  target: BieUpliftTargetFlatNode;
  /** The target assigned by the initial system mapping, when one exists. */
  systemTarget: BieUpliftTargetFlatNode;
  fixed: boolean;
  context: string;

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

  get asbiePath(): string {
    return (this.type.toUpperCase() === 'ASBIEP') ? (this._node as AsbiepFlatNode).asbiePath : undefined;
  }

  get bbiePath(): string {
    return (this.type.toUpperCase() === 'BBIEP') ? (this._node as BbiepFlatNode).bbiePath : undefined;
  }

  get bbieScPath(): string {
    return (this.type.toUpperCase() === 'BBIE_SC') ? (this._node as BbieScFlatNode).bbieScPath : undefined;
  }

  /**
   * Returns the source path used by the uplift visitor.
   *
   * The tree intentionally hides an ASCCP segment below a reused ASBIEP so
   * that the reused subtree is displayed as one logical reference. The
   * uplift visitor still traverses an unselected reuse inline and therefore
   * addresses its descendants through the full ASCCP-qualified path.
   */
  get upliftPath(): string {
    return this.canonicalPath(this._node);
  }

  private canonicalPath(node: BieFlatNode): string {
    const localPath = this.localPath(node);

    // A reused ASBIEP stores its own subtree with paths rooted at that
    // ASCCP. The uplift visitor traverses the same subtree at its occurrence
    // below the parent ASCC association, so restore that occurrence prefix.
    const reusedAncestor = this.closestReusedAncestor(node);
    if (reusedAncestor) {
      const occurrencePrefix = this.fullAssociationPath(reusedAncestor);
      if (localPath !== occurrencePrefix && !localPath.startsWith(occurrencePrefix + '>')) {
        return this.joinPath(occurrencePrefix, localPath);
      }
    }
    return localPath;
  }

  private closestReusedAncestor(node: BieFlatNode): AsbiepFlatNode | undefined {
    let parent = node.parent as BieFlatNode;
    while (parent) {
      if (parent.bieType.toUpperCase() === 'ASBIEP' && parent.reused) {
        return parent as AsbiepFlatNode;
      }
      parent = parent.parent as BieFlatNode;
    }
    return undefined;
  }

  private fullAssociationPath(node: AsbiepFlatNode): string {
    const localPath = this.associationPath(node);
    const reusedAncestor = this.closestReusedAncestor(node);
    if (!reusedAncestor) {
      return localPath;
    }

    const occurrencePrefix = this.fullAssociationPath(reusedAncestor);
    if (localPath === occurrencePrefix || localPath.startsWith(occurrencePrefix + '>')) {
      return localPath;
    }
    return this.joinPath(occurrencePrefix, localPath);
  }

  private localPath(node: BieFlatNode): string {
    const type = node.bieType.toUpperCase();
    if (type === 'BBIE_SC') {
      const bbiep = node.parent as BbiepFlatNode;
      return this.joinPath(
        this.associationPath(bbiep),
        'BCCP-' + bbiep.bccpNode.manifestId,
        'DT-' + bbiep.bdtNode.manifestId,
        'DT_SC-' + (node as BbieScFlatNode).bdtScNode.manifestId);
    }
    return this.associationPath(node);
  }

  private associationPath(node: BieFlatNode): string {
    const type = node.bieType.toUpperCase();
    if (type === 'ABIE') {
      return node.path;
    }

    // Groups may be hidden in display paths, but the server's occurrence path
    // includes their ASCC/ASCCP/ACC segments when resolving custom mappings.
    const parent = node.parent as BieFlatNode;
    const ownerPath = parent ? this.ownerPath(parent) : '';
    if (type === 'ASBIEP') {
      return this.joinPath(
        ownerPath,
        this.intermediateAccPath(node as AsbiepFlatNode),
        'ASCC-' + (node as AsbiepFlatNode).asccNode.manifestId);
    }
    if (type === 'BBIEP') {
      return this.joinPath(
        ownerPath,
        this.intermediateAccPath(node as BbiepFlatNode),
        'BCC-' + (node as BbiepFlatNode).bccNode.manifestId);
    }
    return node.path;
  }

  private intermediateAccPath(node: AsbiepFlatNode | BbiepFlatNode): string {
    return (node.intermediateAccNodes || []).map(getKey).join('>');
  }

  private ownerPath(node: BieFlatNode): string {
    const type = node.bieType.toUpperCase();
    if (type === 'ASBIEP') {
      const asbiep = node as AsbiepFlatNode;
      return this.joinPath(
        this.associationPath(asbiep),
        'ASCCP-' + asbiep.asccpNode.manifestId,
        'ACC-' + asbiep.accNode.manifestId);
    }
    return this.associationPath(node);
  }

  private joinPath(...parts: string[]): string {
    const segments: string[] = [];
    parts.filter(part => !!part).forEach(part => {
      part.split('>').filter(segment => !!segment).forEach(segment => {
        // A reused node can expose an intermediate ACC both through its
        // parent path and through intermediateAccNodes. The visitor emits
        // that boundary once, so do the same when composing the request path.
          if (!this.isRepeatedAccountBoundary(segments[segments.length - 1], segment)) {
            segments.push(segment);
          }
      });
    });
    return segments.join('>');
  }

  private isRepeatedAccountBoundary(previousSegment: string, segment: string): boolean {
    return previousSegment === segment && segment.startsWith('ACC-');
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
      return !this.reused || this.target.reusedTopLevelAsbiepId !== undefined &&
        this.target.reusedTopLevelAsbiepId !== null;
    }
    return false;
  }

  /**
   * Keep manually mapped descendants materialized when the generic tree data
   * source collapses a BBIEP. The source node itself is locked under a reused
   * parent, so the underlying BIE node cannot report a normal edit change.
   */
  get isChanged(): boolean {
    return super.isChanged || !!this.target;
  }

  equal(node: BieFlatNode) {
    return this._node === node;
  }
}

export class BieUpliftTargetFlatNode extends WrappedBieFlatNode {
  source: BieUpliftSourceFlatNode;
  reusedTopLevelAsbiepId: number;
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

  get asbiePath(): string {
    return (this.type.toUpperCase() === 'ASBIEP') ? (this._node as AsbiepFlatNode).asbiePath : undefined;
  }

  get bbiePath(): string {
    return (this.type.toUpperCase() === 'BBIEP') ? (this._node as BbiepFlatNode).bbiePath : undefined;
  }

  get bbieScPath(): string {
    return (this.type.toUpperCase() === 'BBIE_SC') ? (this._node as BbieScFlatNode).bbieScPath : undefined;
  }

  equal(node: BieFlatNode) {
    return this._node === node;
  }
}

export class FindTargetAsccpManifestResponse {
  asccpManifestId: number;
  releaseNum: string;
}

export class UpliftNode {
  bieType: string;
  bieId: number;
  sourcePath: string;
  sourceManifestId: number;
  targetPath: string;
  targetManifestId: number;
  refTopLevelAsbiepId: number;
  /** Preserve an explicit unmatched choice instead of re-running server auto-mapping. */
  suppressAutoMapping: boolean;

  constructor(bieType: string, bieId: number, sourcePath: string, targetPath?: string, refBie?: number,
              suppressAutoMapping?: boolean) {
    this.bieType = bieType;
    this.bieId = bieId;
    this.sourcePath = sourcePath;
    this.targetPath = targetPath;
    this.refTopLevelAsbiepId = refBie;
    this.suppressAutoMapping = suppressAutoMapping ?? targetPath === undefined;
  }
}

export interface BiePathContext {
  path: string;
  context: string;
}

export interface BiePathMapping {
  bieId: number;
  source: BiePathContext;
  target?: BiePathContext;
}

export class BieUpliftMap {
  asbiePathList: BiePathMapping[];
  bbiePathList: BiePathMapping[];
  bbieScPathList: BiePathMapping[];

  sourceUsedMap: Map<string, number>;
  targetUsedMap: Map<string, number>;

  unMatchedList: UpliftNode[];
  unMatchedMap: Map<string, UpliftNode>;

  constructor(obj) {
    this.sourceUsedMap = new Map<string, number>();
    this.targetUsedMap = new Map<string, number>();
    this.unMatchedMap = new Map<string, UpliftNode>();
    this.unMatchedList = [];

    this.asbiePathList = this.getPathMappings(obj, 'asbie');
    this.bbiePathList = this.getPathMappings(obj, 'bbie');
    this.bbieScPathList = this.getPathMappings(obj, 'bbieSc');

    this.asbiePathList.forEach(mapping => {
      if (!mapping.target) {
        const node = new UpliftNode('ASBIE', mapping.bieId, mapping.source.path, undefined);
        this.unMatchedList.push(node);
      }
    });
    this.bbiePathList.forEach(mapping => {
      if (!mapping.target) {
        const node = new UpliftNode('BBIE', mapping.bieId, mapping.source.path, undefined);
        this.unMatchedList.push(node);
      }
    });
    this.bbieScPathList.forEach(mapping => {
      if (!mapping.target) {
        const node = new UpliftNode('BBIE_SC', mapping.bieId, mapping.source.path, undefined);
        this.unMatchedList.push(node);
      }
    });

    this.unMatchedList = this.unMatchedList.sort((a, b) => a.sourcePath.localeCompare(b.sourcePath));
    this.unMatchedList.map(node => this.unMatchedMap.set(node.sourcePath, node));
    this.initializePathUsageMaps();
  }

  private getPathMappings(obj: any, type: string): BiePathMapping[] {
    const list = obj[type + 'PathList'];
    if (Array.isArray(list)) {
      return list;
    }

    const typeName = type.charAt(0).toUpperCase() + type.slice(1);
    const sourceMap = obj['source' + typeName + 'PathMap'] || {};
    const targetMap = obj['target' + typeName + 'PathMap'] || {};
    return Object.keys(sourceMap).map(key => ({
      bieId: Number(key),
      source: sourceMap[key],
      target: targetMap[key]
    }));
  }

  private initializePathUsageMaps() {
    this.asbiePathList.forEach(mapping => this.sourceUsedMap.set(mapping.source.path, mapping.bieId));
    this.bbiePathList.forEach(mapping => this.sourceUsedMap.set(mapping.source.path, mapping.bieId));
    this.bbieScPathList.forEach(mapping => this.sourceUsedMap.set(mapping.source.path, mapping.bieId));
    this.asbiePathList.forEach(mapping => mapping.target && this.targetUsedMap.set(mapping.target.path, mapping.bieId));
    this.bbiePathList.forEach(mapping => mapping.target && this.targetUsedMap.set(mapping.target.path, mapping.bieId));
    this.bbieScPathList.forEach(mapping => mapping.target && this.targetUsedMap.set(mapping.target.path, mapping.bieId));
  }
}

export class MatchInfo {
  name: string;
  bieType: string;
  ccType: string;
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
  status: string;
  valid: boolean;
  context: string;

  private canonicalTargetPath(target: BieUpliftTargetFlatNode): string {
    try {
      return new BieUpliftSourceFlatNode(target._node).upliftPath;
    } catch (_error) {
      // Report rows can be built from partially materialized test/legacy
      // nodes. Preserve their supplied path when the structural metadata
      // needed for canonicalization is unavailable.
      return target.path;
    }
  }

  constructor(source: BieUpliftSourceFlatNode) {
    const target = source.target;
    this.name = source.name;
    this.bieId = source.bieId;
    this.context = source.context || '';
    this.valid = false;
    this.message = '';
    switch (source.bieType.toUpperCase()) {
      case 'ABIE':
        break;
      case 'ASBIEP':
        this.bieType = 'ASBIE';
        this.ccType = 'ASCCP';
        this.sourceManifestId = (source._node as AsbiepFlatNode).asccNode.manifestId;
        this.sourcePath = source.upliftPath;
        if (target) {
          this.targetManifestId = (target._node as AsbiepFlatNode).asccNode.manifestId;
          // Reused target descendants hide their outer occurrence in the
          // display path.  Reports are submitted to the same validation
          // endpoint as create, so they must use the canonical occurrence
          // path as well.
          this.targetPath = this.canonicalTargetPath(target);
        }
        break;
      case 'BBIEP':
        this.bieType = 'BBIE';
        this.ccType = 'BCCP';
        this.sourceManifestId = (source._node as BbiepFlatNode).bccNode.manifestId;
        this.sourcePath = source.upliftPath;
        if (target) {
          this.targetManifestId = (target._node as BbiepFlatNode).bccNode.manifestId;
          this.targetPath = this.canonicalTargetPath(target);
        }
        break;
      case 'BBIE_SC':
        this.bieType = 'BBIE_SC';
        this.ccType = 'DT_SC';
        this.sourceManifestId = (source._node as BbieScFlatNode).bdtScNode.manifestId;
        this.sourcePath = source.upliftPath;
        if (target) {
          this.targetManifestId = (target._node as BbieScFlatNode).bdtScNode.manifestId;
          this.targetPath = this.canonicalTargetPath(target);
        }
        break;
    }
    this.reuse = '';
    this.status = '';
    this.sourceDisplayPath = '/' + source.parents.map(i => i.name).join('/');
    if (target) {
      this.targetDisplayPath = '/' + target.parents.map(i => i.name).join('/');
      if (source.fixed) {
        this.match = 'System';
      } else {
        this.match = 'Manual';
      }
    } else {
      this.targetDisplayPath = '';
      this.match = 'Unmatched';
    }
    if (source.reused) {
      this.reuse = 'Not selected';
      if (target && target.reusedTopLevelAsbiepId) {
        this.reuse = 'Selected';
      }
    }
  }
}

export interface BieValidation {
  bieType: string;
  bieId: number;
  valid: boolean;
  message: string;
  status: string;
  sourcePath?: string;
}

export class BieValidationResponse {
  validations: BieValidation[];
}
