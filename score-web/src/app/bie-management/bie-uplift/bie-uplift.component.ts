import {CdkVirtualScrollViewport} from '@angular/cdk/scrolling';
import {faCircleExclamation, faRecycle} from '@fortawesome/free-solid-svg-icons';
import { Component, OnInit, ViewChild, inject } from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {forkJoin, Observable, of} from 'rxjs';
import {finalize, map, switchMap} from 'rxjs/operators';
import {BusinessContextService} from '../../context-management/business-context/domain/business-context.service';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {MatStepper} from '@angular/material/stepper';
import {BieEditService} from '../bie-edit/domain/bie-edit.service';
import {ReuseBieDialogComponent} from '../bie-edit/reuse-bie-dialog/reuse-bie-dialog.component';
import {BieListService} from '../bie-list/domain/bie-list.service';
import {ReleaseService} from '../../release-management/domain/release.service';
import {AuthService} from '../../authentication/auth.service';
import {BieUpliftService} from './domain/bie-uplift.service';
import {BieUpliftMap, BieUpliftSourceFlatNode, BieUpliftTargetFlatNode, MatchInfo, UpliftNode} from './domain/bie-uplift';
import {CcNodeService} from '../../cc-management/domain/core-component-node.service';
import {
  AsbiepFlatNode,
  BbiepFlatNode,
  BbieScFlatNode,
  BieFlatNode,
  BieFlatNodeDatabase,
  BieFlatNodeDataSource,
  BieFlatNodeDataSourceSearcher,
  isValidBasedTopLevelAsbiepId
} from '../domain/bie-flat-tree';
import {CcGraphNode} from '../../cc-management/domain/core-component-node';
import {ReportDialogComponent} from './report-dialog/report-dialog.component';
import {BieEditAbieNode, RefBie, UsedBie} from '../bie-edit/domain/bie-edit-node';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {saveBooleanProperty} from '../../common/utility';
import {WebPageInfoService} from '../../basis/basis.service';
import {Title} from '@angular/platform-browser';
import {formatAppTitle} from '../../common/app-title.strategy';

function loadAndWrapUpliftChildren<T extends BieFlatNode>(
  node: T,
  loadChildren: (node: T) => void,
  createWrapper: (node: BieFlatNode) => BieFlatNode
): void {
  // BieFlatNode.self is intentionally non-generic because wrapped nodes and
  // raw nodes share the same interface. Narrow it once at this boundary.
  const rawNode = (node.self || node) as T;
  loadChildren(rawNode);
  const children = rawNode.getChildren ? rawNode.getChildren() : rawNode.children;

  const wrap = (item: BieFlatNode): BieFlatNode => {
    const wrapped = createWrapper(item);
    const children = item.getChildren ? item.getChildren() : item.children;
    if (children && children.length > 0) {
      wrapped.children = children.map(wrap);
    }
    return wrapped;
  };

  node.children = children.map(wrap);
}

interface DetachedDescendantMapping {
  source: BieUpliftSourceFlatNode;
  target: BieUpliftTargetFlatNode;
  fixed: boolean;
}


export class BieUpliftSourceFlatNodeDatabase<T extends BieFlatNode> extends BieFlatNodeDatabase<T> {
  private upliftWrappers = new WeakMap<BieFlatNode, BieUpliftSourceFlatNode>();

  get rootNode(): T {
    const rootNode = super.rootNode;
    return new BieUpliftSourceFlatNode(rootNode) as unknown as T;
  }

  override children(node: T): T[] {
    return super.children(node).map(child => this.wrapChild(child)) as unknown as T[];
  }

  loadChildren(node: T) {
    // Resolve BIE usage against the raw node. The uplift wrapper changes the
    // path exposed by its parent, which is intentionally different from the
    // persisted BIE path used by afterBbieScFlatNode().
    loadAndWrapUpliftChildren(
      node,
      rawNode => super.loadChildren(rawNode),
      item => this.wrapChild(item)
    );
  }

  private wrapChild(node: BieFlatNode): BieUpliftSourceFlatNode {
    if (node instanceof BieUpliftSourceFlatNode) {
      return node;
    }
    const rawNode = (node.self || node) as BieFlatNode;
    if (!this.upliftWrappers) {
      this.upliftWrappers = new WeakMap<BieFlatNode, BieUpliftSourceFlatNode>();
    }
    let wrapper = this.upliftWrappers.get(rawNode);
    if (!wrapper) {
      wrapper = new BieUpliftSourceFlatNode(rawNode);
      this.upliftWrappers.set(rawNode, wrapper);
    }
    return wrapper;
  }

  override hasUsedOrInheritedDescendant(node: T): boolean {
    // The uplift visitor decides which source branches to retain while it
    // walks the tree. Avoid recursively probing the same CC graph from every
    // rendered node; that duplicates the traversal and can be quadratic for
    // reused or cyclic structures.
    return false;
  }
}

export class BieUpliftTargetFlatNodeDatabase<T extends BieFlatNode> extends BieFlatNodeDatabase<T> {
  private upliftWrappers = new WeakMap<BieFlatNode, BieUpliftTargetFlatNode>();

  get rootNode(): T {
    const rootNode = super.rootNode;
    return new BieUpliftTargetFlatNode(rootNode) as unknown as T;
  }

  children(node: T): T[] {
    const children = super.children(node).map(child => this.wrapChild(child)) as unknown as T[];
    // A selected BIE supplies persisted usage flags; super.children has already
    // flattened presentation groups. Probing unused CC descendants here would
    // materialize the entire association graph just to display this reference.
    return this.isSelectedReuseSubtree(node)
      ? children.filter(e => e.used || e.inherited)
      : children;
  }

  private isSelectedReuseSubtree(node: T): boolean {
    let current = node as BieFlatNode;
    while (current) {
      if (current.reused && current.topLevelAsbiepId !== undefined && current.topLevelAsbiepId !== null) {
        return true;
      }
      current = current.parent as BieFlatNode;
    }
    return false;
  }

  loadChildren(node: T) {
    loadAndWrapUpliftChildren(
      node,
      rawNode => super.loadChildren(rawNode),
      item => this.wrapChild(item)
    );
  }

  private wrapChild(node: BieFlatNode): BieUpliftTargetFlatNode {
    if (node instanceof BieUpliftTargetFlatNode) {
      return node;
    }
    const rawNode = (node.self || node) as BieFlatNode;
    if (!this.upliftWrappers) {
      this.upliftWrappers = new WeakMap<BieFlatNode, BieUpliftTargetFlatNode>();
    }
    let wrapper = this.upliftWrappers.get(rawNode);
    if (!wrapper) {
      wrapper = new BieUpliftTargetFlatNode(rawNode);
      this.upliftWrappers.set(rawNode, wrapper);
    }
    return wrapper;
  }
}

@Component({
  standalone: false,
  selector: 'score-bie-uplift',
  templateUrl: './bie-uplift.component.html',
  styleUrls: ['./bie-uplift.component.css']
})
export class BieUpliftComponent implements OnInit {
  private bizCtxService = inject(BusinessContextService);
  private bieListService = inject(BieListService);
  private accountService = inject(AccountListService);
  private releaseService = inject(ReleaseService);
  private bieEditService = inject(BieEditService);
  private ccNodeService = inject(CcNodeService);
  private bieUpliftService = inject(BieUpliftService);
  private auth = inject(AuthService);
  private location = inject(Location);
  private router = inject(Router);
  private dialog = inject(MatDialog);
  private route = inject(ActivatedRoute);
  private titleService = inject(Title);
  private confirmDialogService = inject(ConfirmDialogService);
  webPageInfo = inject(WebPageInfoService);


  faRecycle = faRecycle;
  faCircleExclamation = faCircleExclamation;
  subtitle = 'Verify BIE';
  loading = false;

  @ViewChild(MatStepper, {static: true}) stepper: MatStepper;
  @ViewChild('sourceVirtualScroll', {static: true}) public sourceVirtualScroll: CdkVirtualScrollViewport;
  @ViewChild('targetVirtualScroll', {static: true}) public targetVirtualScroll: CdkVirtualScrollViewport;
  virtualScrollItemSize = 33;

  get minBufferPx(): number {
    return 10000 * this.virtualScrollItemSize;
  }

  get maxBufferPx(): number {
    return 1000000 * this.virtualScrollItemSize;
  }

  contextMenuItem: BieFlatNode;

  sourceDataSource: BieFlatNodeDataSource<BieUpliftSourceFlatNode>;
  targetDataSource: BieFlatNodeDataSource<BieUpliftTargetFlatNode>;
  sourceSearcher: BieFlatNodeDataSourceSearcher<BieUpliftSourceFlatNode>;
  targetSearcher: BieFlatNodeDataSourceSearcher<BieUpliftTargetFlatNode>;

  sourceSelectedNode: BieUpliftSourceFlatNode;
  targetSelectedNode: BieUpliftTargetFlatNode;
  private mappedTargetBySource = new Map<BieUpliftSourceFlatNode, BieUpliftTargetFlatNode>();
  private detachedDescendantMappings = new Map<
    BieUpliftSourceFlatNode,
    Map<BieUpliftTargetFlatNode, DetachedDescendantMapping[]>
  >();
  private sourceWrapperByRawNode = new Map<BieFlatNode, BieUpliftSourceFlatNode>();
  private targetWrapperByRawNode = new Map<BieFlatNode, BieUpliftTargetFlatNode>();

  paddingPixel = 12;
  innerY: number = window.innerHeight;

  topLevelAsbiepId: number;
  targetAsccpManifestId: number;
  targetReleaseId: number;
  bieGuid: string;
  bieName: string;
  sourceLibraryId: number;
  sourceReleaseNum: string;
  targetReleaseNum: string;

  unmatchedSource: BieUpliftSourceFlatNode[] = [];
  currentUnmatchedSource: BieUpliftSourceFlatNode;

  HIDE_UNUSED_PROPERTY_KEY = 'BIE-Settings-Hide-Unused';

  private getBaseUsedBieList(baseIds: Array<number | undefined>): Observable<UsedBie[]> {
    const ids = Array.from(new Set(baseIds.filter(isValidBasedTopLevelAsbiepId)));
    if (ids.length === 0) {
      return of([]);
    }
    const visited = new Set<number>();
    const loadFamily = (id: number): Observable<UsedBie[]> => {
      if (!isValidBasedTopLevelAsbiepId(id) || visited.has(id)) {
        return of([]);
      }
      visited.add(id);
      return forkJoin({
        usedBieList: this.bieEditService.getUsedBieList(id),
        rootNode: this.bieEditService.getRootNode(id)
      }).pipe(
        switchMap(({usedBieList, rootNode}) => loadFamily(rootNode?.basedTopLevelAsbiepId).pipe(
          map(parentUsedBieList => usedBieList.concat(parentUsedBieList))
        ))
      );
    };
    return forkJoin(ids.map(id => loadFamily(id))).pipe(
      map(lists => lists.reduce((all, list) => all.concat(list), []))
    );
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.topLevelAsbiepId = Number(params.get('topLevelAsbiepId'));
    });
    this.route.queryParamMap.subscribe(params => {
      this.targetReleaseId = Number(params.get('targetReleaseId'));
    });

    this.loading = true;
    forkJoin([
      this.bieUpliftService.findTargetAsccpManifest(this.topLevelAsbiepId, this.targetReleaseId),
      this.releaseService.getReleaseDetail(String(this.targetReleaseId))
    ]).subscribe(([resp, targetRelease]) => {
      this.targetAsccpManifestId = resp.asccpManifestId;
      this.targetReleaseNum = resp.releaseNum || targetRelease?.releaseNum;

      forkJoin([
        this.bieEditService.getGraphNode(this.topLevelAsbiepId),
        this.bieEditService.getRootNode(this.topLevelAsbiepId),
        this.bieEditService.getUsedBieList(this.topLevelAsbiepId),
        this.bieEditService.getRefBieList(this.topLevelAsbiepId),
        this.ccNodeService.getGraphNode('ASCCP', this.targetAsccpManifestId)
      ]).pipe(switchMap(([sourceCcGraph, sourceRootNode,
                          sourceUsedBieList, sourceRefBieList,
                          targetCcGraph]) => {
        const sourceBaseUsedBieList$ = this.getBaseUsedBieList([
          sourceRootNode?.basedTopLevelAsbiepId,
          ...sourceRefBieList.map((ref: RefBie) => ref.refBasedTopLevelAsbiepId)
        ]);
        return forkJoin({
          sourceCcGraph: of(sourceCcGraph),
          sourceRootNode: of(sourceRootNode),
          sourceUsedBieList: of(sourceUsedBieList),
          sourceRefBieList: of(sourceRefBieList),
          targetCcGraph: of(targetCcGraph),
          sourceBaseUsedBieList: sourceBaseUsedBieList$
        });
      })).subscribe(({sourceCcGraph, sourceRootNode,
                      sourceUsedBieList, sourceRefBieList,
                      targetCcGraph, sourceBaseUsedBieList}) => {
        this.sourceLibraryId = sourceRootNode.libraryId;
        this.bieGuid = sourceRootNode.guid;
        this.bieName = sourceRootNode.name;
        this.sourceReleaseNum = sourceRootNode.releaseNum;
        this.updatePageTitle();

        const sourceDatabase = new BieUpliftSourceFlatNodeDatabase<BieUpliftSourceFlatNode>(sourceCcGraph,
          sourceRootNode, this.topLevelAsbiepId, sourceUsedBieList, sourceRefBieList);
        sourceDatabase.setBaseUsedBieList(sourceBaseUsedBieList);
        this.sourceDataSource = new BieFlatNodeDataSource<BieUpliftSourceFlatNode>(sourceDatabase, this.bieEditService, this.ccNodeService);
        this.sourceSearcher = new BieFlatNodeDataSourceSearcher<BieUpliftSourceFlatNode>(this.sourceDataSource, sourceDatabase);
        this.sourceDataSource.init();

        this.sourceDataSource.hideUnused = true;

        const targetRootNode = new BieEditAbieNode();
        targetRootNode.asccpManifestId = this.targetAsccpManifestId;
        const targetDatabase = new BieUpliftTargetFlatNodeDatabase<BieUpliftTargetFlatNode>(targetCcGraph,
          targetRootNode, undefined, [], []);
        this.targetDataSource = new BieFlatNodeDataSource<BieUpliftTargetFlatNode>(targetDatabase, this.bieEditService, this.ccNodeService);
        this.targetSearcher = new BieFlatNodeDataSourceSearcher<BieUpliftTargetFlatNode>(this.targetDataSource, targetDatabase);
        this.targetDataSource.init();

        this.bieUpliftService.getUpliftBieMap(this.topLevelAsbiepId, this.targetReleaseId).subscribe(bieUpliftMap => {
          let sourceData = [];
          let sourceStack = [this.sourceDataSource.data[0], ];
          const sourceVisited = new Set<BieFlatNode>();
          while (sourceStack.length > 0) {
            const sourceItem = sourceStack.shift();
            // A path is not a node identity: reused BIE occurrences can share
            // the same persisted path while still requiring separate mapping
            // candidates. Use the materialized occurrence instead.
            const sourceIdentity = sourceItem.self || sourceItem;
            if (sourceVisited.has(sourceIdentity)) {
              continue;
            }
            sourceVisited.add(sourceIdentity);
            sourceData.push(sourceItem);
            if (sourceItem.expandable && sourceItem.children.length === 0 && !sourceItem.isCycle) {
              this.sourceDataSource.database.loadChildren(sourceItem);
            }
            sourceStack = sourceItem.getChildren().filter(e => {
              const sourceChild = e as BieUpliftSourceFlatNode;
              return sourceChild.used || sourceChild.inherited || sourceChild.required;
            })
              .concat(sourceStack) as BieUpliftSourceFlatNode[];
          }

          let targetData = [];
          let targetStack = [this.targetDataSource.data[0], ];
          const targetVisited = new Set<BieFlatNode>();
          while (targetStack.length > 0) {
            const targetItem = targetStack.shift();
            const targetIdentity = targetItem.self || targetItem;
            if (targetVisited.has(targetIdentity)) {
              continue;
            }
            targetVisited.add(targetIdentity);
            for (const sourceItem of sourceData) {
              if (sourceItem.bieType === targetItem.bieType &&
                sourceItem.name === targetItem.name &&
                sourceItem.level === targetItem.level) {
                targetData.push(targetItem);

                if (targetItem.expandable && targetItem.children.length === 0 && !targetItem.isCycle) {
                  this.targetDataSource.database.loadChildren(targetItem);
                }
                targetStack = targetItem.getChildren().concat(targetStack) as BieUpliftTargetFlatNode[];
                break;
              }
            }
          }

          sourceData = sourceData.filter(e => !e.isGroup);
          targetData = targetData.filter(e => !e.isGroup);

          this.initMapping(sourceData, targetData, bieUpliftMap);
          this.sourceDataSource.collapse(this.sourceDataSource.data[0]);
          this.targetDataSource.collapse(this.targetDataSource.data[0]);

          sourceData[0].target = targetData[0];
          this.refreshUnmatchedSources(sourceData);
          if (this.unmatchedSource.length > 0) {
            this.unmatchedSource.forEach(e => this.sourceDataSource.expand(e));
            this.currentUnmatchedSource = this.unmatchedSource[0];
            this.expandSourceNode(this.currentUnmatchedSource);
            this.scrollToSourceNode(this.currentUnmatchedSource);
          } else {
            this.expandSourceNode(sourceData[0]);
            this.scrollToSourceNode(sourceData[0]);
          }

          if (!this.targetSelectedNode) {
            this.targetSelectedNode = targetData[0];
          }

          this.loading = false;
        }, () => {
          this.loading = false;
        });
      }, () => {
        this.loading = false;
      });
    }, () => {
      this.loading = false;
    });
  }

  private updatePageTitle(): void {
    const bieName = this.bieName?.trim();
    const sourceReleaseNum = this.sourceReleaseNum?.trim();
    const targetReleaseNum = this.targetReleaseNum?.trim();

    if (!bieName || !sourceReleaseNum || !targetReleaseNum) {
      return;
    }

    this.titleService.setTitle(
      formatAppTitle(`${bieName} BIE from ${sourceReleaseNum} to ${targetReleaseNum}`)
    );
  }

  _getLastTag(path: string): string {
    if (!path) {
      return undefined;
    }
    const strs = path.split('>');
    if (!strs || strs.length === 0) {
      return undefined;
    }
    return strs[strs.length - 1];
  }

  _getManifestId(tag: string): number {
    if (!tag) {
      return undefined;
    }
    return Number(tag.split('-')[1]);
  }

  getKey(node: CcGraphNode): string {
    return node.type.toUpperCase() + '-' + node.manifestId;
  }

  initMapping(sourceData: BieUpliftSourceFlatNode[], targetData: BieUpliftTargetFlatNode[],
              bieUpliftMap: BieUpliftMap) {
    const sourceAsbiepList = new Map<number, BieUpliftSourceFlatNode[]>();
    const sourceBbiepList = new Map<number, BieUpliftSourceFlatNode[]>();
    const sourceBbieScList = new Map<number, BieUpliftSourceFlatNode[]>();
    const mappedSourceAsbieps = new Set<BieUpliftSourceFlatNode>();
    const mappedSourceBbieps = new Set<BieUpliftSourceFlatNode>();
    const mappedSourceBbieScs = new Set<BieUpliftSourceFlatNode>();

    sourceData.forEach(e => {
      if (e.type.toUpperCase() === 'ASBIEP') {
        const key = (e._node as AsbiepFlatNode).asccNode.manifestId;
        if (!sourceAsbiepList.has(key)) {
          sourceAsbiepList.set(key, [e, ]);
        } else {
          sourceAsbiepList.get(key).push(e);
        }
      } else if (e.type.toUpperCase() === 'BBIEP') {
        const key = (e._node as BbiepFlatNode).bccNode.manifestId;
        if (!sourceBbiepList.has(key)) {
          sourceBbiepList.set(key, [e, ]);
        } else {
          sourceBbiepList.get(key).push(e);
        }
      } else if (e.type.toUpperCase() === 'BBIE_SC') {
        const key = (e._node as BbieScFlatNode).bdtScNode.manifestId;
        if (!sourceBbieScList.has(key)) {
          sourceBbieScList.set(key, [e, ]);
        } else {
          sourceBbieScList.get(key).push(e);
        }
      }
    });

    const targetAsbiepList = new Map<number, BieUpliftTargetFlatNode[]>();
    const targetBbiepList = new Map<number, BieUpliftTargetFlatNode[]>();
    const targetBbieScList = new Map<number, BieUpliftTargetFlatNode[]>();
    const mappedTargetAsbieps = new Set<BieUpliftTargetFlatNode>();
    const mappedTargetBbieps = new Set<BieUpliftTargetFlatNode>();
    const mappedTargetBbieScs = new Set<BieUpliftTargetFlatNode>();

    targetData.forEach(e => {
      if (e.type.toUpperCase() === 'ASBIEP') {
        const key = (e._node as AsbiepFlatNode).asccNode.manifestId;
        if (!targetAsbiepList.has(key)) {
          targetAsbiepList.set(key, [e, ]);
        } else {
          targetAsbiepList.get(key).push(e);
        }
      } else if (e.type.toUpperCase() === 'BBIEP') {
        const key = (e._node as BbiepFlatNode).bccNode.manifestId;
        if (!targetBbiepList.has(key)) {
          targetBbiepList.set(key, [e, ]);
        } else {
          targetBbiepList.get(key).push(e);
        }
      } else if (e.type.toUpperCase() === 'BBIE_SC') {
        const key = (e._node as BbieScFlatNode).bdtScNode.manifestId;
        if (!targetBbieScList.has(key)) {
          targetBbieScList.set(key, [e, ]);
        } else {
          targetBbieScList.get(key).push(e);
        }
      }
    });

    for (const mapping of bieUpliftMap.asbiePathList) {
      const asbieId = mapping.bieId;
      const sourceAsbiePathContext = mapping.source;
      const sourceManifestId = this._getManifestId(this._getLastTag(sourceAsbiePathContext.path));
      const source = (sourceAsbiepList.has(sourceManifestId) ? sourceAsbiepList.get(sourceManifestId) : [])
        .find(e => !mappedSourceAsbieps.has(e) &&
          ((e._node as AsbiepFlatNode).asbiePath === sourceAsbiePathContext.path ||
          e.upliftPath === sourceAsbiePathContext.path));
      if (!!source) {
        mappedSourceAsbieps.add(source);
        source.bieId = asbieId;
        source.context = sourceAsbiePathContext.context;
        const targetAsbiePathContext = mapping.target;
        const targetManifestId = this._getManifestId(this._getLastTag(targetAsbiePathContext?.path));
        const target = (targetAsbiepList.has(targetManifestId) ? targetAsbiepList.get(targetManifestId) : [])
          .find(e => !mappedTargetAsbieps.has(e) &&
            (e._node as AsbiepFlatNode).asbiePath === targetAsbiePathContext?.path);
        if (!!target) {
          mappedTargetAsbieps.add(target);
          source.target = target;
          source.systemTarget = target;
          source.fixed = true;
          target.source = source;
          this.registerMapping(source, target);
        }
      }
    }
    for (const mapping of bieUpliftMap.bbiePathList) {
      const bbieId = mapping.bieId;
      const sourceBbiePathContext = mapping.source;
      const sourceManifestId = this._getManifestId(this._getLastTag(sourceBbiePathContext.path));
      const source = (sourceBbiepList.has(sourceManifestId) ? sourceBbiepList.get(sourceManifestId) : [])
        .find(e => !mappedSourceBbieps.has(e) &&
          ((e._node as BbiepFlatNode).bbiePath === sourceBbiePathContext.path ||
          e.upliftPath === sourceBbiePathContext.path));
      if (!!source) {
        mappedSourceBbieps.add(source);
        source.bieId = bbieId;
        source.context = sourceBbiePathContext.context;
        const targetBbiePathContext = mapping.target;
        const targetManifestId = this._getManifestId(this._getLastTag(targetBbiePathContext?.path));
        const target = (targetBbiepList.has(targetManifestId) ? targetBbiepList.get(targetManifestId) : [])
          .find(e => !mappedTargetBbieps.has(e) &&
            (e._node as BbiepFlatNode).bbiePath === targetBbiePathContext?.path);
        if (!!target) {
          mappedTargetBbieps.add(target);
          source.target = target;
          source.systemTarget = target;
          source.fixed = true;
          target.source = source;
          this.registerMapping(source, target);
        }
      }
    }
    for (const mapping of bieUpliftMap.bbieScPathList) {
      const bbieScId = mapping.bieId;
      const sourceBbieScPathContext = mapping.source;
      const sourceManifestId = this._getManifestId(this._getLastTag(sourceBbieScPathContext.path));
      const source = (sourceBbieScList.has(sourceManifestId) ? sourceBbieScList.get(sourceManifestId) : [])
        .find(e => !mappedSourceBbieScs.has(e) &&
          ((e._node as BbieScFlatNode).bbieScPath === sourceBbieScPathContext.path ||
          e.upliftPath === sourceBbieScPathContext.path));
      if (!!source) {
        mappedSourceBbieScs.add(source);
        source.bieId = bbieScId;
        source.context = sourceBbieScPathContext.context;
        const targetBbieScPathContext = mapping.target;
        const targetManifestId = this._getManifestId(this._getLastTag(targetBbieScPathContext?.path));
        const target = (targetBbieScList.has(targetManifestId) ? targetBbieScList.get(targetManifestId) : [])
          .find(e => !mappedTargetBbieScs.has(e) &&
            (e._node as BbieScFlatNode).bbieScPath === targetBbieScPathContext?.path);
        if (!!target) {
          mappedTargetBbieScs.add(target);
          source.target = target;
          source.systemTarget = target;
          source.fixed = true;
          target.source = source;
          this.registerMapping(source, target);
        }
      }
    }
  }

  onResize(event) {
    this.innerY = window.innerHeight;
  }

  get innerHeight(): number {
    return this.innerY - 400;
  }

  search(type: string, backward?: boolean, force?: boolean) {
    if (type === 'source') {
      this.sourceSearcher.search(this.sourceSearcher.inputKeyword, this.sourceSelectedNode, backward, force)
        .subscribe(index => {
          this.sourceVirtualScroll.scrollToOffset(index * this.virtualScrollItemSize, 'smooth');
        });
    } else if (type === 'target') {
      this.targetSearcher.search(this.targetSearcher.inputKeyword, this.targetSelectedNode, backward, force)
        .subscribe(index => {
          this.targetVirtualScroll.scrollToOffset(index * this.virtualScrollItemSize, 'smooth');
        });
    }
  }

  move(type: string, val: number) {
    if (type === 'source') {
      this.sourceSearcher.go(val).subscribe(index => {
        this.onSourceClick(this.sourceDataSource.data[index]);
        this.sourceVirtualScroll.scrollToOffset(index * this.virtualScrollItemSize, 'smooth');
      });
    } else if (type === 'target') {
      this.targetSearcher.go(val).subscribe(index => {
        this.onTargetClick(this.targetDataSource.data[index]);
        this.targetVirtualScroll.scrollToOffset(index * this.virtualScrollItemSize, 'smooth');
      });
    }
  }

  onSourceClick(node: BieUpliftSourceFlatNode) {
    this.sourceSelectedNode = node;

    if (node.target) {
      this.targetSelectedNode = node.target;
      this.expandTargetNode(this.targetSelectedNode);
      this.scrollToTargetNode(this.targetSelectedNode);
    }
  }

  onTargetClick(node: BieUpliftTargetFlatNode) {
    this.targetSelectedNode = node;
    if (node.source) {
      this.scrollToSourceNode(node.source);
    }
  }

  scrollBreadcrumb(elementId: string) {
    const breadcrumbs = document.getElementById(elementId);
    if (breadcrumbs.scrollWidth > breadcrumbs.clientWidth) {
      breadcrumbs.scrollLeft = breadcrumbs.scrollWidth - breadcrumbs.clientWidth;
      breadcrumbs.classList.add('inner-box');
    } else {
      breadcrumbs.scrollLeft = 0;
      breadcrumbs.classList.remove('inner-box');
    }
    return '';
  }

  scrollToSourceNode(node: BieUpliftSourceFlatNode) {
    let index = -1;
    let currentNode = node;
    while (currentNode) {
      index = this.sourceDataSource.getNodeIndex(node);
      if (index !== -1) {
        break;
      }
      this.sourceDataSource.toggleNode(currentNode.parent as BieUpliftSourceFlatNode, true);
      currentNode = currentNode.parent as BieUpliftSourceFlatNode;
    }
    this.scrollTree(this.sourceVirtualScroll, index, 500);
  }

  scrollToTargetNode(node: BieUpliftTargetFlatNode) {
    let index = -1;
    let currentNode = node;
    while (currentNode) {
      index = this.targetDataSource.getNodeIndex(node);
      if (index !== -1) {
        break;
      }
      this.targetDataSource.toggleNode(currentNode.parent as BieUpliftTargetFlatNode, true);
      currentNode = currentNode.parent as BieUpliftTargetFlatNode;
    }
    this.scrollTree(this.targetVirtualScroll, index, 500);
  }

  onSourceBreadCrumbClick(node: BieFlatNode) {
    const sourceNode = this.sourceDataSource.data.find(v => v.equal(node));
    this.onSourceClick(sourceNode);
    this.scrollToSourceNode(sourceNode);
  }

  onTargetBreadCrumbClick(node: BieFlatNode) {
    const targetNode = this.targetDataSource.data.find(v => v.equal(node));
    this.onTargetClick(targetNode);
    this.scrollToTargetNode(targetNode);
  }

  scrollTree(virtualScroll: CdkVirtualScrollViewport, index: number, delay?: number) {
    if (index < 0) {
      return;
    }

    if (delay) {
      setTimeout(() => {
        virtualScroll.scrollToOffset(index * this.virtualScrollItemSize, 'smooth');
      }, delay);
    } else {
      virtualScroll.scrollToOffset(index * this.virtualScrollItemSize, 'smooth');
    }
  }

  isSource(node: BieFlatNode): boolean {
    if (!node) {
      return false;
    }
    return node instanceof BieUpliftSourceFlatNode;
  }

  isTarget(node: BieFlatNode): boolean {
    if (!node) {
      return false;
    }
    return node instanceof BieUpliftTargetFlatNode;
  }

  canMatch(node: BieUpliftTargetFlatNode): boolean {
    return this.getMatchDisabledReason(node) === '';
  }

  isSourceNodeCheckable(node: BieUpliftSourceFlatNode): boolean {
    // The source checkbox is a read-only mapping indicator. System-mapped
    // nodes remain eligible for manual override, so hiding their indicator
    // while fixed would make the source tree disagree with the target tree.
    return node.level > 0 && !this.isSourceNodeCoveredByReuse(node);
  }

  isTargetNodeCheckable(node: BieUpliftTargetFlatNode): boolean {
    return node.level > 0 && !this.isTargetNodeCoveredByReuse(node);
  }

  private isReuseReference(node: BieUpliftTargetFlatNode): boolean {
    return !!node && node.reusedTopLevelAsbiepId !== undefined && node.reusedTopLevelAsbiepId !== null;
  }

  private isTargetNodeCoveredByReuse(node: BieUpliftTargetFlatNode): boolean {
    let parent = node.parent as BieFlatNode;
    while (parent) {
      const parentIdentity = parent.self || parent;
      const targetParent = this.targetWrapperByRawNode?.get(parentIdentity) ||
        (this.targetDataSource?.data || []).find(candidate => candidate === parent || candidate.self === parentIdentity) as
          BieUpliftTargetFlatNode;
      if (this.isReuseReference(targetParent)) {
        return true;
      }
      parent = parent.parent as BieFlatNode;
    }
    return false;
  }

  private isSourceNodeCoveredByReuse(node: BieUpliftSourceFlatNode): boolean {
    for (const [mappedSource, mappedTarget] of this.mappedTargetBySource || []) {
      if (this.isReuseReference(mappedTarget) && mappedSource !== node && this.isLogicalDescendant(node, mappedSource)) {
        return true;
      }
    }
    return false;
  }

  getMatchDisabledReason(node: BieUpliftTargetFlatNode): string {
    if (node.level > 0 && this.isTargetNodeCoveredByReuse(node)) {
      return 'Already mapped by the selected reuse BIE';
    }
    if (!this.sourceSelectedNode) {
      return 'Select a source node first';
    }
    if (!this.hasMappedParentPair(this.sourceSelectedNode, node)) {
      return 'Map the parent node first';
    }
    if (this.sourceSelectedNode.type.toUpperCase() !== node.type.toUpperCase()) {
      return 'Source and target node types must match';
    }
    if (node.type.toUpperCase() === 'BBIEP' &&
        (this.sourceSelectedNode._node as BbiepFlatNode)?.bccNode?.entityType !==
        (node._node as BbiepFlatNode)?.bccNode?.entityType) {
      return 'BBIE attributes and elements cannot be mapped to each other';
    }
    return '';
  }

  checkMatch(event, node: BieUpliftTargetFlatNode) {
    if (!this.canMatch(node)) {
      return;
    }

    const selectedSource = this.sourceSelectedNode;

    if (node.source === selectedSource) {
      this.detachTargetMappings(node);
      this.refreshUnmatchedSources();
      return;
    }

    const compatibilityReason = this.getMappingCompatibilityReason(selectedSource, node);
    if (compatibilityReason) {
      const dialogConfig = this.confirmDialogService.newConfig();
      dialogConfig.data.header = 'Mapping confirmation';
      dialogConfig.data.content = ['The source and target are different. Do you want to continue?'];
      dialogConfig.data.list = [
        'Source: ' + (selectedSource.name || selectedSource.path || ''),
        'Target: ' + (node.name || node.path || '')
      ];
      dialogConfig.data.action = 'Continue';
      this.confirmDialogService.open(dialogConfig).afterClosed().subscribe(result => {
        if (result) {
          this.attachMapping(selectedSource, node);
          this.refreshUnmatchedSources();
        } else {
          // MatCheckbox updates its visual state before the asynchronous
          // confirmation dialog is opened. The mapping model is unchanged on
          // Cancel, so restore the control from that model explicitly.
          event?.source && (event.source.checked = node.source !== undefined);
        }
      });
      return;
    }

    this.attachMapping(selectedSource, node);
    this.refreshUnmatchedSources();
  }

  /**
   * Compare the component referenced by the BIE node, not the BIE property
   * itself. Stable component ids and GUIDs survive release revisions;
   * a manifest id is release-specific and is never used as a component id.
   */
  private getMappingCompatibilityReason(source: BieUpliftSourceFlatNode,
                                        target: BieUpliftTargetFlatNode): string {
    const sourceType = source?.bieType?.toUpperCase();
    const targetType = target?.bieType?.toUpperCase();
    if (!source || !target || sourceType !== targetType) {
      return '';
    }

    let sourceComponent: CcGraphNode;
    let targetComponent: CcGraphNode;
    let componentName: string;
    const sourceNode = source._node;
    const targetNode = target._node;
    if (!sourceNode || !targetNode) {
      return '';
    }
    if (sourceType === 'ASBIEP') {
      sourceComponent = (sourceNode as AsbiepFlatNode).accNode;
      targetComponent = (targetNode as AsbiepFlatNode).accNode;
      componentName = 'role ACC';
    } else if (sourceType === 'BBIEP') {
      sourceComponent = (sourceNode as BbiepFlatNode).bdtNode;
      targetComponent = (targetNode as BbiepFlatNode).bdtNode;
      componentName = 'DT';
    } else if (sourceType === 'BBIE_SC') {
      sourceComponent = (sourceNode as BbieScFlatNode).bdtScNode;
      targetComponent = (targetNode as BbieScFlatNode).bdtScNode;
      componentName = 'DT_SC';
    } else {
      return '';
    }

    if (!sourceComponent || !targetComponent) {
      return '';
    }

    // Manifest ids identify a release-specific representation, so an older
    // payload without stable identity fields must be confirmed by the user.
    const sourceComponentId = sourceComponent.componentId;
    const targetComponentId = targetComponent.componentId;
    const sameComponentId = sourceComponentId !== undefined && sourceComponentId !== null &&
      targetComponentId !== undefined && targetComponentId !== null &&
      sourceComponentId === targetComponentId;
    const sameGuid = !!sourceComponent.guid && !!targetComponent.guid &&
      sourceComponent.guid === targetComponent.guid;
    if (sameComponentId || sameGuid) {
      return '';
    }

    return `The source and target ${componentName} must refer to the same component id or have the same GUID.`;
  }

  private refreshUnmatchedSources(sourceNodes?: BieUpliftSourceFlatNode[]) {
    const previous = this.currentUnmatchedSource;
    const candidates = sourceNodes || this.getLoadedNodes(this.sourceDataSource);
    this.unmatchedSource = candidates.filter(e => !e.isGroup && !e.isMapped);
    this.currentUnmatchedSource = this.unmatchedSource.includes(previous) ? previous : this.unmatchedSource[0];
  }

  private getStructuralParent(node: BieFlatNode, candidates: BieFlatNode[] = [],
                              wrapperByRawNode?: Map<BieFlatNode, BieFlatNode>): BieFlatNode | undefined {
    let parent = node?.parent as BieFlatNode;
    while (parent?.isGroup) {
      parent = parent.parent as BieFlatNode;
    }
    if (!parent) {
      return undefined;
    }
    return wrapperByRawNode?.get(parent) || candidates.find(candidate => candidate === parent || candidate.self === parent) || parent;
  }

  private indexSourceWrappers(nodes: BieFlatNode[], visited: Set<BieFlatNode> = new Set()) {
    if (!nodes) {
      return;
    }
    if (!this.sourceWrapperByRawNode) {
      this.sourceWrapperByRawNode = new Map<BieFlatNode, BieUpliftSourceFlatNode>();
    }
    nodes.forEach(node => {
      const identity = node?.self || node;
      if (!identity || visited.has(identity)) {
        return;
      }
      visited.add(identity);
      this.sourceWrapperByRawNode.set(identity, node as BieUpliftSourceFlatNode);
      this.indexSourceWrappers(node.children as BieFlatNode[] || [], visited);
    });
  }

  /**
   * Return every node that has been materialized in a tree, including nodes
   * hidden by a collapsed ancestor. Mapping state belongs to those nodes, not
   * to the virtual-scroll projection in `data`.
   */
  private getLoadedNodes<T extends BieFlatNode>(dataSource: BieFlatNodeDataSource<T>): T[] {
    const roots = dataSource?.data?.length ? dataSource.data :
      (dataSource?.database?.rootNode ? [dataSource.database.rootNode as T] : []);
    const result: T[] = [];
    const visited = new Set<BieFlatNode>();
    const stack = [...roots];

    while (stack.length > 0) {
      const node = stack.pop();
      const identity = node?.self || node;
      if (!node || visited.has(identity)) {
        continue;
      }
      visited.add(identity);
      result.push(node);
      const children = (node.children || []) as T[];
      for (let i = children.length - 1; i >= 0; i--) {
        stack.push(children[i]);
      }
    }
    return result;
  }

  private indexTargetWrappers(nodes: BieFlatNode[], visited: Set<BieFlatNode> = new Set()) {
    if (!this.targetWrapperByRawNode) {
      this.targetWrapperByRawNode = new Map<BieFlatNode, BieUpliftTargetFlatNode>();
    }
    nodes.forEach(node => {
      const identity = node?.self || node;
      if (!identity || visited.has(identity)) {
        return;
      }
      visited.add(identity);
      this.targetWrapperByRawNode.set(identity, node as BieUpliftTargetFlatNode);
      this.indexTargetWrappers(node.children as BieFlatNode[], visited);
    });
  }

  /**
   * A child can only be mapped beneath corresponding mapped source/target ancestors. The tree
   * wrappers expose raw parents, so the indexes restore the wrapper that owns each raw node before
   * checking its mapping fields. Group nodes are presentation containers and are skipped; level
   * zero is the implicitly mapped BIE root.
   */
  private isExtensionNode(node: BieFlatNode): boolean {
    return !!node && node.bieType?.toUpperCase() === 'ASBIEP' && node.name === 'Extension';
  }

  private hasExtensionAncestor(node: BieFlatNode): boolean {
    let parent = node?.parent as BieFlatNode;
    while (parent) {
      if (this.isExtensionNode(parent)) {
        return true;
      }
      parent = parent.parent as BieFlatNode;
    }
    return false;
  }

  private hasMappedParentPair(sourceNode: BieUpliftSourceFlatNode,
                                targetNode: BieUpliftTargetFlatNode,
                                sourceCandidates?: BieFlatNode[],
                                targetCandidates?: BieFlatNode[]): boolean {
    const sourceParentCandidates = sourceCandidates || this.sourceDataSource?.data || [];
    const targetParentCandidates = targetCandidates || this.targetDataSource?.data || [];
    this.indexSourceWrappers(sourceParentCandidates);
    this.indexTargetWrappers(targetParentCandidates);
    let sourceParent = this.getStructuralParent(sourceNode, sourceParentCandidates,
      this.sourceWrapperByRawNode) as BieUpliftSourceFlatNode;
    let targetParent = this.getStructuralParent(targetNode, targetParentCandidates,
      this.targetWrapperByRawNode) as BieUpliftTargetFlatNode;

    if (this.hasExtensionAncestor(sourceNode)) {
      // User Extension children may relocate below a compatible target
      // ancestor, but a mapped target branch from a different source subtree
      // is still invalid. Walk target ancestors until the nearest mapped one
      // and require that it lies on this source occurrence's logical path.
      while (targetParent && targetParent.level > 0) {
        if (targetParent.source) {
          return this.isLogicalDescendant(sourceNode, targetParent.source);
        }
        targetParent = this.getStructuralParent(targetParent, targetParentCandidates,
          this.targetWrapperByRawNode) as BieUpliftTargetFlatNode;
      }
      return true;
    }

    while (sourceParent || targetParent) {
      if (!sourceParent || !targetParent) {
        return false;
      }
      if (sourceParent.level === 0 || targetParent.level === 0) {
        return sourceParent.level === 0 && targetParent.level === 0;
      }
      if (sourceParent.target !== targetParent || targetParent.source !== sourceParent) {
        return false;
      }
      sourceParent = this.getStructuralParent(sourceParent, sourceParentCandidates,
        this.sourceWrapperByRawNode) as BieUpliftSourceFlatNode;
      targetParent = this.getStructuralParent(targetParent, targetParentCandidates,
        this.targetWrapperByRawNode) as BieUpliftTargetFlatNode;
    }
    return true;
  }

  private clearDescendantMappings(node: BieUpliftTargetFlatNode) {
    if (!this.mappedTargetBySource) {
      return;
    }
    for (const [source, target] of this.mappedTargetBySource) {
      if ((node.source && this.isLogicalDescendant(source, node.source)) ||
          this.isLogicalDescendant(target, node)) {
        this.clearMappingPair(source, target);
      }
    }
  }

  private rememberDescendantMappings(source: BieUpliftSourceFlatNode,
                                     target: BieUpliftTargetFlatNode) {
    if (!this.mappedTargetBySource) {
      return;
    }
    const mappings = Array.from(this.mappedTargetBySource.entries())
      .filter(([mappedSource, mappedTarget]) =>
          (target.source && this.isLogicalDescendant(mappedSource, source)) ||
        this.isLogicalDescendant(mappedTarget, target))
      .map(([mappedSource, mappedTarget]) => ({
        source: mappedSource,
        target: mappedTarget,
        fixed: mappedSource.fixed
      }));
    if (mappings.length === 0) {
      return;
    }

    this.ensureDetachedDescendantMappings();
    let mappingsByTarget = this.detachedDescendantMappings.get(source);
    if (!mappingsByTarget) {
      mappingsByTarget = new Map<BieUpliftTargetFlatNode, DetachedDescendantMapping[]>();
      this.detachedDescendantMappings.set(source, mappingsByTarget);
    }
    mappingsByTarget.set(target, mappings);
  }

  private forgetDetachedMappings(source: BieUpliftSourceFlatNode,
                                  target: BieUpliftTargetFlatNode) {
    if (!this.detachedDescendantMappings) {
      return;
    }
    const mappingsByTarget = this.detachedDescendantMappings.get(source);
    mappingsByTarget?.delete(target);
    if (mappingsByTarget?.size === 0) {
      this.detachedDescendantMappings.delete(source);
    }
  }

  private restoreDescendantMappings(source: BieUpliftSourceFlatNode,
                                    target: BieUpliftTargetFlatNode) {
    if (!this.detachedDescendantMappings) {
      return;
    }
    const mappingsByTarget = this.detachedDescendantMappings.get(source);
    const mappings = mappingsByTarget?.get(target);
    if (!mappings) {
      return;
    }

    mappings.forEach(({source: descendantSource, target: descendantTarget, fixed}) => {
      if (!descendantSource.target && !descendantTarget.source) {
        descendantSource.target = descendantTarget;
        descendantSource.fixed = fixed;
        descendantTarget.source = descendantSource;
        this.registerMapping(descendantSource, descendantTarget);
      }
    });
    this.forgetDetachedMappings(source, target);
  }

  private ensureDetachedDescendantMappings() {
    if (!this.detachedDescendantMappings) {
      this.detachedDescendantMappings = new Map<
        BieUpliftSourceFlatNode,
        Map<BieUpliftTargetFlatNode, DetachedDescendantMapping[]>
      >();
    }
  }

  private isLogicalDescendant(node: BieFlatNode, ancestor: BieFlatNode): boolean {
    let parent = node.parent as BieFlatNode;
    while (parent) {
      if (parent === ancestor || parent === ancestor.self) {
        return true;
      }
      parent = parent.parent as BieFlatNode;
    }
    return false;
  }

  private registerMapping(source: BieUpliftSourceFlatNode, target: BieUpliftTargetFlatNode) {
    if (!this.mappedTargetBySource) {
      this.mappedTargetBySource = new Map<BieUpliftSourceFlatNode, BieUpliftTargetFlatNode>();
    }
    this.mappedTargetBySource.set(source, target);
  }

  private attachMapping(source: BieUpliftSourceFlatNode, target: BieUpliftTargetFlatNode) {
    if (source.target && source.target !== target) {
      this.detachTargetMappings(source.target);
    }
    if (target.source && target.source !== source) {
      this.detachTargetMappings(target);
    }
    source.target = target;
    // A user may temporarily clear a system mapping and select the original
    // target again. Preserve its system classification when the mapping is
    // restored; a different target remains a manual override.
    source.fixed = source.systemTarget === target;
    target.source = source;
    this.registerMapping(source, target);
    this.restoreDescendantMappings(source, target);
  }

  private clearMappingPair(source: BieUpliftSourceFlatNode, target: BieUpliftTargetFlatNode) {
    if (source.target === target) {
      source.target = undefined;
    }
    if (target.source === source) {
      target.source = undefined;
    }
    if (this.mappedTargetBySource?.get(source) === target) {
      this.mappedTargetBySource.delete(source);
    }
    // Descendant mappings are cleared through this path when a parent is
    // detached. They are explicit unmatched choices too and must be sent as
    // negative overrides instead of being silently auto-mapped again.
    source.fixed = false;
    if (this.isReuseReference(target) && this.targetDataSource) {
      this.clearTargetReuseNode(target);
    }
    target.reusedTopLevelAsbiepId = undefined;
  }

  private detachTargetMappings(node: BieUpliftTargetFlatNode) {
    const source = node.source;
    if (source) {
      this.forgetDetachedMappings(source, node);
      this.rememberDescendantMappings(source, node);
    }
    this.clearDescendantMappings(node);
    if (this.isReuseReference(node) && this.targetDataSource) {
      this.clearTargetReuseNode(node);
    }
    node.source = undefined;
    node.reusedTopLevelAsbiepId = undefined;
    if (source?.target === node) {
      source.target = undefined;
      // Once detached, the occurrence is an explicit unmatched choice. Keep
      // the original system target separately for restoration, but do not let
      // the fixed flag hide it from request serialization.
      source.fixed = false;
    }
    if (source && this.mappedTargetBySource?.get(source) === node) {
      this.mappedTargetBySource.delete(source);
    }
  }

  private prepareTargetReuseNode(node: BieUpliftTargetFlatNode, selectedTopLevelAsbiepId: number,
                                 rootNode: BieEditAbieNode) {
    this.validateReuseRootNode(selectedTopLevelAsbiepId, rootNode);
    const inherited = isValidBasedTopLevelAsbiepId(rootNode.basedTopLevelAsbiepId);

    if (this.targetDataSource.isExpanded(node)) {
      this.targetDataSource.collapse(node);
    }

    const asbiepNode = node._node as AsbiepFlatNode;
    asbiepNode.inherited = false;
    asbiepNode.reused = true;
    asbiepNode.topLevelAsbiepId = selectedTopLevelAsbiepId;
    // A selected inherited BIE carries its base through the root metadata. Do
    // not synthesize an empty root: that would silently turn an inherited
    // reference into a non-inherited one during subsequent tree loads.
    asbiepNode.basedTopLevelAsbiepId = rootNode.basedTopLevelAsbiepId;
    asbiepNode.inherited = inherited;
    asbiepNode.rootNode = rootNode;
    asbiepNode.rootNode.topLevelAsbiepId = selectedTopLevelAsbiepId;
    asbiepNode.children = [];
    // The wrapper owns a separate child list; invalidate it so expansion reloads this BIE.
    node.children = [];
    asbiepNode.expandable = undefined;
  }

  private clearTargetReuseNode(node: BieUpliftTargetFlatNode) {
    const wasExpanded = this.targetDataSource.isExpanded(node);
    if (wasExpanded) {
      this.targetDataSource.collapse(node);
    }

    const asbiepNode = node._node as AsbiepFlatNode;
    asbiepNode.inherited = false;
    asbiepNode.reused = false;
    asbiepNode.topLevelAsbiepId = undefined;
    asbiepNode.basedTopLevelAsbiepId = undefined;
    asbiepNode.rootNode = undefined;
    asbiepNode.children = [];
    // The wrapper owns a separate child list; invalidate it so expansion reloads this BIE.
    node.children = [];
    asbiepNode.expandable = undefined;

    if (wasExpanded) {
      this.targetDataSource.expand(node);
    }
  }

  private applyReuseSelection(node: BieUpliftTargetFlatNode, selectedTopLevelAsbiepId: number,
                              rootNode: BieEditAbieNode) {
    // Validate before clearing descendant mappings so a malformed response
    // cannot partially mutate an existing mapping.
    this.validateReuseRootNode(selectedTopLevelAsbiepId, rootNode);
    this.clearDescendantMappings(node);
    this.prepareTargetReuseNode(node, selectedTopLevelAsbiepId, rootNode);

    this.sourceDataSource.expand(node.source);
    this.targetDataSource.expand(node);
    // Selecting a reuse maps only the association that owns the reference.
    // Its descendants are supplied by the selected BIE and are not direct
    // source-to-target mappings in this uplift.
    this.indexTargetWrappers(this.getLoadedNodes(this.targetDataSource));

    this.sourceDataSource.dataChange.next(this.sourceDataSource.data);
    this.targetDataSource.dataChange.next(this.targetDataSource.data);
    node.reusedTopLevelAsbiepId = selectedTopLevelAsbiepId;
    this.refreshUnmatchedSources();
  }

  private validateReuseRootNode(selectedTopLevelAsbiepId: number, rootNode: BieEditAbieNode): void {
    if (!rootNode) {
      throw new Error(`Cannot apply reuse BIE ${selectedTopLevelAsbiepId}: root metadata is missing.`);
    }

    const hasBase = rootNode.basedTopLevelAsbiepId !== undefined &&
      rootNode.basedTopLevelAsbiepId !== null;
    if (hasBase && !isValidBasedTopLevelAsbiepId(rootNode.basedTopLevelAsbiepId)) {
      throw new Error(`Cannot apply reuse BIE ${selectedTopLevelAsbiepId}: invalid basedTopLevelAsbiepId.`);
    }
  }

  /**
   * Target tree nodes use display paths that intentionally hide the outer
   * occurrence when a reused BIE is expanded.  The uplift API needs the
   * canonical CC path, so reuse the source wrapper's path normalizer for the
   * same raw tree node.  Lightweight test doubles may not have a parent; in
   * that case their supplied path is already the only available value.
   */
  private targetRequestPath(node: BieUpliftTargetFlatNode): string {
    if (!node || !node._node || !node._node.parent) {
      return node?.path;
    }
    return new BieUpliftSourceFlatNode(node._node).upliftPath;
  }

  createUpliftBIE() {
    this.loading = true;
    const sourceNodes = this.getLoadedNodes(this.sourceDataSource);
    const targetNodes = this.getLoadedNodes(this.targetDataSource);
    this.indexSourceWrappers(sourceNodes);
    // `emptyRequired` is derived request state. Recompute it from the current
    // mappings so a failed request followed by an edit cannot retain stale
    // target-only parent rows.
    targetNodes.forEach(node => node.emptyRequired = false);
    const source = sourceNodes.filter(e => this.shouldSerializeSourceNode(e));

    const matched = [];
    source.forEach(e => {
      let upliftNode;
      if (e.target) {
        upliftNode = new UpliftNode(e.type, e.bieId, e.upliftPath,
          this.targetRequestPath(e.target), e.target.reusedTopLevelAsbiepId);
        if (e.type.toUpperCase() === 'ASBIEP') {
          upliftNode.bieType = 'ASBIE';
          upliftNode.sourceManifestId = (e._node as AsbiepFlatNode).asccNode.manifestId;
          upliftNode.targetManifestId = (e.target._node as AsbiepFlatNode).asccNode.manifestId;
        } else if (e.type.toUpperCase() === 'BBIEP') {
          upliftNode.bieType = 'BBIE';
          upliftNode.sourceManifestId = (e._node as BbiepFlatNode).bccNode.manifestId;
          upliftNode.targetManifestId = (e.target._node as BbiepFlatNode).bccNode.manifestId;
        } else if (e.type.toUpperCase() === 'BBIE_SC') {
          upliftNode.bieType = 'BBIE_SC';
          upliftNode.sourceManifestId = (e._node as BbieScFlatNode).bdtScNode.manifestId;
          upliftNode.targetManifestId = (e.target._node as BbieScFlatNode).bdtScNode.manifestId;
        }
        e.target.parents.forEach(p => {
          const parentTargetNode = targetNodes.find(v => v.equal(p));
          if (parentTargetNode && parentTargetNode.level !== 0 && !parentTargetNode.source) {
            parentTargetNode.emptyRequired = true;
          }
        });
      } else {
        // An unmatched occurrence is an explicit user choice at create time.
        // Mark it so the API does not silently restore a system mapping that
        // the user removed on the verification page.
        upliftNode = new UpliftNode(e.type, e.bieId, e.upliftPath, undefined, undefined, true);
        if (e.type.toUpperCase() === 'ASBIEP') {
          upliftNode.bieType = 'ASBIE';
          upliftNode.sourceManifestId = (e._node as AsbiepFlatNode).asccNode.manifestId;
        } else if (e.type.toUpperCase() === 'BBIEP') {
          upliftNode.bieType = 'BBIE';
          upliftNode.sourceManifestId = (e._node as BbiepFlatNode).bccNode.manifestId;
        } else if (e.type.toUpperCase() === 'BBIE_SC') {
          upliftNode.bieType = 'BBIE_SC';
          upliftNode.sourceManifestId = (e._node as BbieScFlatNode).bdtScNode.manifestId;
        }
      }

      matched.push(upliftNode);
    });

    const targets = targetNodes.filter(e => !e.isGroup && e.emptyRequired);
    targets.forEach(e => {
      const upliftNode = new UpliftNode(e.type, null, null, this.targetRequestPath(e), null);
      if (e.type.toUpperCase() === 'ASBIEP') {
        upliftNode.bieType = 'ASBIE';
        upliftNode.targetManifestId = (e._node as AsbiepFlatNode).asccNode.manifestId;
      } else if (e.type.toUpperCase() === 'BBIEP') {
        upliftNode.bieType = 'BBIE';
        upliftNode.targetManifestId = (e._node as BbiepFlatNode).bccNode.manifestId;
      } else if (e.type.toUpperCase() === 'BBIE_SC') {
        upliftNode.bieType = 'BBIE_SC';
        upliftNode.targetManifestId = (e._node as BbieScFlatNode).bdtScNode.manifestId;
      }
      matched.push(upliftNode);
    });

    this.bieUpliftService.createUpliftBie(this.topLevelAsbiepId, this.targetAsccpManifestId, matched)
      .pipe(finalize(() => {
        this.loading = false;
      }))
      .subscribe(result => {
        // Issue #1366
        // 'Hide Unused' option must be turned off after BIE creation.
        saveBooleanProperty(this.auth.getUserToken(), this.HIDE_UNUSED_PROPERTY_KEY, false);

        this.router.navigateByUrl('/profile_bie/' + result.topLevelAsbiepId);
      });
  }

  /**
   * Source descendants of a reused ASBIEP are locked in the tree because a selected
   * reuse reference owns that subtree. If the reuse is left unselected, however, a
   * user can manually map those descendants and the mappings must still reach the
   * uplift service. Other locked nodes (for example, nodes from an unusable source
   * state) remain excluded. Selected-reuse descendants are also excluded because
   * their ancestor reference causes the backend to skip the inline subtree.
   */
  private shouldSerializeSourceNode(node: BieUpliftSourceFlatNode): boolean {
    if (node.isGroup) {
      return false;
    }
    if (this.hasSelectedReuseAncestor(node)) {
      return false;
    }
    if (node.reused) {
      return true;
    }
    if (node.fixed) {
      return false;
    }
    if (!node.locked) {
      return true;
    }

    if (!this.hasReusedAncestor(node)) {
      return false;
    }

    const target = node.target;
    return !!target && (target.reusedTopLevelAsbiepId === undefined || target.reusedTopLevelAsbiepId === null);
  }

  private hasSelectedReuseAncestor(node: BieUpliftSourceFlatNode): boolean {
    let parent = node.parent as BieFlatNode;
    if (!parent) {
      return false;
    }
    while (parent) {
      const wrappedParent = this.sourceWrapperByRawNode?.get(parent) ||
        this.sourceDataSource?.data?.find(candidate => candidate.self === parent || candidate === parent) as
          BieUpliftSourceFlatNode;
      const target = wrappedParent?.target;
      if (this.isReuseReference(target)) {
        return true;
      }
      parent = parent.parent as BieFlatNode;
    }
    return false;
  }

  private hasReusedAncestor(node: BieFlatNode): boolean {
    let parent = node.parent as BieFlatNode;
    while (parent) {
      if (parent.reused) {
        return true;
      }
      parent = parent.parent as BieFlatNode;
    }
    return false;
  }

  back() {
    this.router.navigateByUrl('/profile_bie/uplift');
  }

  matchReused(node: BieUpliftTargetFlatNode) {
    if (node.source && node.source.reused && this.hasMappedParentPair(node.source, node)) {
      const dialogRef = this.dialog.open(ReuseBieDialogComponent, {
        data: {
          title: 'Select Profile BIE to reuse',
          asccpManifestId: (node._node as unknown as AsbiepFlatNode).asccpNode.manifestId,
          libraryId: this.sourceLibraryId,
          releaseId: this.targetReleaseId,
          topLevelAsbiepId: this.topLevelAsbiepId,
        },
        width: '100%',
        maxWidth: '100%',
        height: '100%',
        maxHeight: '100%',
        autoFocus: false
      });
      dialogRef.afterClosed().subscribe(selectedTopLevelAsbiepId => {
        if (selectedTopLevelAsbiepId === undefined || selectedTopLevelAsbiepId === null) {
          return;
        }
        this.loading = true;
        this.bieEditService.getRootNode(selectedTopLevelAsbiepId).pipe(
          switchMap(rootNode => {
            return forkJoin({
              rootNode: of(rootNode),
              usedBieList: this.bieEditService.getUsedBieList(selectedTopLevelAsbiepId),
              refBieList: this.bieEditService.getRefBieList(selectedTopLevelAsbiepId)
            });
          }),
          switchMap(({rootNode, usedBieList, refBieList}) => this.getBaseUsedBieList([
            rootNode?.basedTopLevelAsbiepId,
            ...refBieList.map((ref: RefBie) => ref.refBasedTopLevelAsbiepId)
          ]).pipe(map(baseUsedBieList => ({rootNode, usedBieList, refBieList, baseUsedBieList}))))
        ).pipe(finalize(() => {
          this.loading = false;
        })).subscribe(({rootNode, usedBieList, refBieList, baseUsedBieList}) => {
          // Validate before changing any target-side lists. A malformed
          // inherited root must leave the current selection untouched.
          this.validateReuseRootNode(selectedTopLevelAsbiepId, rootNode);
          this.targetDataSource.database.appendUsedBieList(usedBieList);
          this.targetDataSource.database.appendRefBieList(refBieList);
          this.targetDataSource.database.appendBaseUsedBieList(baseUsedBieList);
          this.applyReuseSelection(node, selectedTopLevelAsbiepId, rootNode);
        });
      });
    }
  }

  report() {
    const reports = [];
    const sourceNodes = this.getLoadedNodes(this.sourceDataSource);
    this.indexSourceWrappers(sourceNodes);
    sourceNodes.filter(e => !e.isGroup && e.level > 0 && e.used &&
      (!e.locked || this.shouldSerializeSourceNode(e))).forEach(e => {
      reports.push(new MatchInfo(e));
    });

    const dialogRef = this.dialog.open(ReportDialogComponent, {
      data: {
        topLevelAsbiepId: this.topLevelAsbiepId,
        targetAsccpManifestId: this.targetAsccpManifestId,
        releaseId: this.targetReleaseId,
        matches: reports,
        name: this.bieName,
        guid: this.bieGuid,
        sourceReleaseNum: this.sourceReleaseNum,
        targetReleaseNum: this.targetReleaseNum
      },
      width: '100%',
      maxWidth: '100%',
      height: '100%',
      maxHeight: '100%',
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe(uplift => {
      if (uplift) {
        this.confirmUnselectedReuseThenUplift();
      }
    });
  }

  // Mapped source reuse nodes that the user left without a target BIE selected
  // for the target release. An unmatched association cannot be inline-copied by
  // the server, so it remains an ordinary unmatched row under TA 29.1.7 rather
  // than receiving the inline-copy warning.
  private collectUnselectedReuseNodes(): BieUpliftSourceFlatNode[] {
    return this.getLoadedNodes(this.sourceDataSource).filter(e =>
      e.level > 0 && e.used && !e.locked && e.reused &&
      !!e.target && !this.isReuseReference(e.target));
  }

  // Issue #1735: when reuse nodes are left unselected, the uplift inline-copies
  // their fields instead of keeping a reference to the reused BIE. Make that
  // consequence explicit before proceeding so the user can go back and select.
  private confirmUnselectedReuseThenUplift() {
    const unselectedReuseNodes = this.collectUnselectedReuseNodes();
    if (unselectedReuseNodes.length === 0) {
      this.createUpliftBIE();
      return;
    }

    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Proceed without selecting reuse BIEs?';
    dialogConfig.data.content = [
      unselectedReuseNodes.length + ' reuse BIE node(s) below have no reuse BIE selected for the target release.',
      'If you continue, their fields will be copied into the uplifted BIE and the reference to the reused BIE will NOT be kept.',
      'To preserve the reference instead, go back and click the reuse icon on each node to select a target BIE.'
    ];
    dialogConfig.data.list = unselectedReuseNodes.map(e => '/' + e.parents.map(i => i.name).join('/'));
    dialogConfig.data.action = 'Continue';

    this.confirmDialogService.open(dialogConfig).afterClosed().subscribe(result => {
      if (result) {
        this.createUpliftBIE();
      }
    });
  }

  expandSourceNode(node: BieUpliftSourceFlatNode) {
    this.sourceDataSource.expand(node);
    this.onSourceClick(node);
  }

  expandTargetNode(node: BieUpliftTargetFlatNode) {
    this.targetDataSource.expand(node);
  }

  lookUpUnmatched(direction: number) {
    if (this.unmatchedSource.length === 0) {
      return;
    }
    if (!this.currentUnmatchedSource) {
      this.currentUnmatchedSource = this.unmatchedSource[0];
    }
    const index = this.unmatchedSource.indexOf(this.currentUnmatchedSource);
    if (direction > 0) {
      if (this.unmatchedSource.length > index + 1) {
        this.currentUnmatchedSource = this.unmatchedSource[index + 1];
      } else {
        this.currentUnmatchedSource = this.unmatchedSource[0];
      }
    } else {
      if (index > 0) {
        this.currentUnmatchedSource = this.unmatchedSource[index - 1];
      } else {
        this.currentUnmatchedSource = this.unmatchedSource[this.unmatchedSource.length - 1];
      }
    }

    this.expandSourceNode(this.currentUnmatchedSource);
    this.sourceVirtualScroll.scrollToIndex(
      this.sourceDataSource.data.indexOf(this.currentUnmatchedSource) * this.virtualScrollItemSize, 'smooth'
    );
  }
}
