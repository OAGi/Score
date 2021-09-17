import {CdkVirtualScrollViewport} from '@angular/cdk/scrolling';
import {Component, OnInit, ViewChild} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {forkJoin, merge, Observable} from 'rxjs';
import {finalize, map} from 'rxjs/operators';
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
  AbieFlatNode,
  AsbiepFlatNode,
  BbiepFlatNode,
  BbieScFlatNode,
  BieDataSourceSearcher,
  BieFlatNode,
  BieFlatNodeFlattener,
  VSBieFlatTreeControl,
  VSBieFlatTreeDataSource,
} from '../domain/bie-flat-tree';
import {CcGraph, CcGraphNode} from '../../cc-management/domain/core-component-node';
import {RefBie, UsedBie} from '../bie-edit/domain/bie-edit-node';
import {ReportDialogComponent} from './report-dialog/report-dialog.component';
import {ContextMenuComponent, ContextMenuService} from 'ngx-contextmenu';
import {VSFlatTreeDataSource} from '../../common/flat-tree';


class BieUpliftSourceFlatNodeFlattener extends BieFlatNodeFlattener {

  private _usedAsbieMap: {};
  private _usedBbieMap: {};
  private _usedBbieScMap: {};
  private _refBieList?: RefBie[];

  constructor(ccGraph: CcGraph, asccpManifestId: number, topLevelAsbiepId: number,
              usedBieList: UsedBie[], refBieList: RefBie[]) {
    super(ccGraph, asccpManifestId, topLevelAsbiepId);

    this._usedAsbieMap = usedBieList.filter(e => e.type === 'ASBIE').reduce((r, a) => {
      r[a.manifestId] = [...r[a.manifestId] || [], a];
      return r;
    }, {});
    this._usedBbieMap = usedBieList.filter(e => e.type === 'BBIE').reduce((r, a) => {
      r[a.manifestId] = [...r[a.manifestId] || [], a];
      return r;
    }, {});
    this._usedBbieScMap = usedBieList.filter(e => e.type === 'BBIE_SC').reduce((r, a) => {
      r[a.manifestId] = [...r[a.manifestId] || [], a];
      return r;
    }, {});
    this._refBieList = refBieList;
  }

  afterAsbiepFlatNode(node: AsbiepFlatNode) {
    if (!node.used) {
      let used = this._usedAsbieMap[node.asccNode.manifestId];
      if (!used || used.length === 0) {
        node.used = false;
      } else {
        node.used = new Observable(subscriber => {
          if (!(node.parent as BieFlatNode).used) {
            subscriber.next(false);
          } else {
            used = used.filter(u => u.hashPath === node.asbieHashPath);
            subscriber.next(!!used && used.length > 0 ? true : false);
          }
          subscriber.complete();
        });
      }
    }

    let derived = this._refBieList.filter(u => u.basedAsccManifestId === node.asccNode.manifestId);
    if (!!derived && derived.length > 0) {
      derived = derived.filter(u => u.hashPath === node.asbieHashPath);
    }
    node.derived = !!derived && derived.length > 0;
    if (node.derived) {
      node.topLevelAsbiepId = derived[0].refTopLevelAsbiepId;
    }
  }

  afterBbiepFlatNode(node: BbiepFlatNode) {
    if (!node.used) {
      let used = this._usedBbieMap[node.bccNode.manifestId];
      if (!used || used.length === 0) {
        node.used = false;
      } else {
        node.used = new Observable(subscriber => {
          if (!(node.parent as BieFlatNode).used) {
            subscriber.next(false);
          } else {
            used = used.filter(u => u.hashPath === node.bbieHashPath);
            subscriber.next(!!used && used.length > 0 ? true : false);
          }
          subscriber.complete();
        });
      }
    }
  }

  afterBbieScFlatNode(node: BbieScFlatNode) {
    if (!node.used) {
      let used = this._usedBbieScMap[node.bdtScNode.manifestId];
      if (!used || used.length === 0) {
        node.used = false;
      } else {
        node.used = new Observable(subscriber => {
          if (!(node.parent as BieFlatNode).used) {
            subscriber.next(false);
          } else {
            used = used.filter(u => u.hashPath === node.bbieScHashPath);
            subscriber.next(!!used && used.length > 0 ? true : false);
          }
          subscriber.complete();
        });
      }
    }
  }
}

class VSBieUpliftSourceFlatTreeDataSource extends VSBieFlatTreeDataSource<BieUpliftSourceFlatNode> {
  dataFilter(node: BieUpliftSourceFlatNode): boolean {
    if (node.locked) {
      return false;
    }
    return super.dataFilter(node);
  }
}

@Component({
  selector: 'score-bie-uplift',
  templateUrl: './bie-uplift.component.html',
  styleUrls: ['./bie-uplift.component.css']
})
export class BieUpliftComponent implements OnInit {

  subtitle = 'Verify BIE';
  loading: boolean = false;

  @ViewChild(MatStepper, {static: true}) stepper: MatStepper;
  @ViewChild('sourceVirtualScroll', {static: true}) public sourceVirtualScroll: CdkVirtualScrollViewport;
  @ViewChild('targetVirtualScroll', {static: true}) public targetVirtualScroll: CdkVirtualScrollViewport;
  virtualScrollItemSize: number = 33;

  minBufferPx(dataSource: VSFlatTreeDataSource<BieFlatNode>): number {
    return Math.max((dataSource) ? dataSource.data[0].children.length : 0, 20) * this.virtualScrollItemSize;
  }

  maxBufferPx(dataSource: VSFlatTreeDataSource<BieFlatNode>): number {
    return Math.max((dataSource) ? dataSource.data[0].children.length : 0, 20) * 20 * this.virtualScrollItemSize;
  }

  @ViewChild('defaultSourceContextMenu', {static: true}) public defaultSourceContextMenu: ContextMenuComponent;
  @ViewChild('defaultTargetContextMenu', {static: true}) public defaultTargetContextMenu: ContextMenuComponent;

  sourceTreeControl: VSBieFlatTreeControl<BieUpliftSourceFlatNode> =
    new VSBieFlatTreeControl<BieUpliftSourceFlatNode>(true, undefined, undefined, undefined);
  targetTreeControl: VSBieFlatTreeControl<BieUpliftTargetFlatNode> =
    new VSBieFlatTreeControl<BieUpliftTargetFlatNode>(false, undefined, undefined, undefined);

  sourceDataSource: VSBieUpliftSourceFlatTreeDataSource;
  targetDataSource: VSBieFlatTreeDataSource<BieUpliftTargetFlatNode>;
  sourceSearcher: BieDataSourceSearcher;
  targetSearcher: BieDataSourceSearcher;
  sourceSelectedNode: BieUpliftSourceFlatNode;
  targetSelectedNode: BieUpliftTargetFlatNode;

  paddingPixel = 12;
  innerY: number = window.innerHeight;

  topLevelAsbiepId: number;
  targetAsccpManifestId: number;
  targetReleaseId: number;
  bieGuid: string;
  bieName: string;
  sourceReleaseNum: string;
  targetReleaseNum: string;

  unmatchedSource: BieUpliftSourceFlatNode[] = [];
  currentUnmatchedSource: BieUpliftSourceFlatNode;

  constructor(private bizCtxService: BusinessContextService,
              private bieListService: BieListService,
              private accountService: AccountListService,
              private releaseService: ReleaseService,
              private bieEditService: BieEditService,
              private ccNodeService: CcNodeService,
              private bieUpliftService: BieUpliftService,
              private contextMenuService: ContextMenuService,
              private auth: AuthService,
              private location: Location,
              private router: Router,
              private dialog: MatDialog,
              private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.topLevelAsbiepId = Number(params.get('topLevelAsbiepId'));
    });
    this.route.queryParamMap.subscribe(params => {
      this.targetReleaseId = Number(params.get('targetReleaseId'));
    });

    this.loading = true;
    this.bieUpliftService.findTargetAsccpManifest(this.topLevelAsbiepId, this.targetReleaseId).subscribe(resp => {
      this.targetAsccpManifestId = resp.asccpManifestId;
      this.targetReleaseNum = resp.releaseNum;

      merge(
        forkJoin([
          this.bieEditService.getGraphNode(this.topLevelAsbiepId),
          this.bieEditService.getRootNode(this.topLevelAsbiepId),
          this.bieEditService.getUsedBieList(this.topLevelAsbiepId),
          this.bieEditService.getRefBieList(this.topLevelAsbiepId)
        ]).pipe(map(([sourceCcGraph, sourceRootNode,
                       sourceUsedBieList, sourceRefBieList]) => {
          this.bieGuid = sourceRootNode.guid;
          this.bieName = sourceRootNode.name;
          this.sourceReleaseNum = sourceRootNode.releaseNum;
          return new BieUpliftSourceFlatNodeFlattener(
            sourceCcGraph, sourceRootNode.asccpManifestId, this.topLevelAsbiepId, sourceUsedBieList, sourceRefBieList)
            .flatten();
        })),
        this.ccNodeService.getGraphNode('ASCCP', this.targetAsccpManifestId).pipe(map(targetCcGraph => {
          return new BieFlatNodeFlattener(
            targetCcGraph, this.targetAsccpManifestId).flatten();
        }))
      ).subscribe(nodes => {
        if ((nodes[0] as AbieFlatNode).asccpNode.manifestId === this.targetAsccpManifestId) {
          this.targetDataSource = new VSBieFlatTreeDataSource(this.targetTreeControl, nodes.map(e => new BieUpliftTargetFlatNode(e)));
          this.targetSearcher = new BieDataSourceSearcher(this.targetDataSource);
        } else {
          this.sourceDataSource = new VSBieUpliftSourceFlatTreeDataSource(this.sourceTreeControl, nodes.map(e => new BieUpliftSourceFlatNode(e)));
          this.sourceSearcher = new BieDataSourceSearcher(this.sourceDataSource);
        }
      }, () => {

      }, () => {
        this.bieUpliftService.getUpliftBieMap(this.topLevelAsbiepId, this.targetReleaseId).subscribe(bieUpliftMap => {
          const sourceNodes = this.sourceDataSource.cachedData;
          const targetNodes = this.targetDataSource.cachedData;
          this.initMapping(sourceNodes, targetNodes, bieUpliftMap);

          this.loading = false;
          sourceNodes[0].target = targetNodes[0];
          this.unmatchedSource = this.sourceDataSource.cachedData.filter(e => !e.isMapped);
          if (this.unmatchedSource.length > 0) {
            this.unmatchedSource.forEach(e => this.sourceTreeControl.expand(e));
            this.currentUnmatchedSource = this.unmatchedSource[0];
            this.expandSourceNode(this.currentUnmatchedSource);
            this.scrollToSourceNode(this.currentUnmatchedSource);
          } else {
            this.expandSourceNode(sourceNodes[0]);
            this.scrollToSourceNode(sourceNodes[0]);
          }

          if (!this.targetSelectedNode) {
            this.targetSelectedNode = targetNodes[0];
          }
        });
      });
    });
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

  initMapping(sourceNodes: BieUpliftSourceFlatNode[], targetNodes: BieUpliftTargetFlatNode[],
              bieUpliftMap: BieUpliftMap) {
    const sourceAsbiepList = new Map<number, BieUpliftSourceFlatNode[]>();
    const sourceBbiepList = new Map<number, BieUpliftSourceFlatNode[]>();
    const sourceBbieScList = new Map<number, BieUpliftSourceFlatNode[]>();
    sourceNodes.forEach(e => {
      if (e.type.toUpperCase() === 'ASBIEP') {
        const key = (e._node as AsbiepFlatNode).asccNode.manifestId;
        if (!sourceAsbiepList.has(key)) {
          sourceAsbiepList.set(key, [e,]);
        } else {
          sourceAsbiepList.get(key).push(e);
        }
      } else if (e.type.toUpperCase() === 'BBIEP') {
        const key = (e._node as BbiepFlatNode).bccNode.manifestId;
        if (!sourceBbiepList.has(key)) {
          sourceBbiepList.set(key, [e,]);
        } else {
          sourceBbiepList.get(key).push(e);
        }
      } else if (e.type.toUpperCase() === 'BBIE_SC') {
        const key = (e._node as BbieScFlatNode).bdtScNode.manifestId;
        if (!sourceBbieScList.has(key)) {
          sourceBbieScList.set(key, [e,]);
        } else {
          sourceBbieScList.get(key).push(e);
        }
      }
    });

    const targetAsbiepList = new Map<number, BieUpliftTargetFlatNode[]>();
    const targetBbiepList = new Map<number, BieUpliftTargetFlatNode[]>();
    const targetBbieScList = new Map<number, BieUpliftTargetFlatNode[]>();
    targetNodes.forEach(e => {
      if (e.type.toUpperCase() === 'ASBIEP') {
        const key = (e._node as AsbiepFlatNode).asccNode.manifestId;
        if (!targetAsbiepList.has(key)) {
          targetAsbiepList.set(key, [e,]);
        } else {
          targetAsbiepList.get(key).push(e);
        }
      } else if (e.type.toUpperCase() === 'BBIEP') {
        const key = (e._node as BbiepFlatNode).bccNode.manifestId;
        if (!targetBbiepList.has(key)) {
          targetBbiepList.set(key, [e,]);
        } else {
          targetBbiepList.get(key).push(e);
        }
      } else if (e.type.toUpperCase() === 'BBIE_SC') {
        const key = (e._node as BbieScFlatNode).bdtScNode.manifestId;
        if (!targetBbieScList.has(key)) {
          targetBbieScList.set(key, [e,]);
        } else {
          targetBbieScList.get(key).push(e);
        }
      }
    });

    for (const [asbieId, sourceAsbiePathContext] of Object.entries(bieUpliftMap.sourceAsbiePathMap)) {
      const sourceManifestId = this._getManifestId(this._getLastTag(sourceAsbiePathContext.path));
      const source = (sourceAsbiepList.has(sourceManifestId) ? sourceAsbiepList.get(sourceManifestId) : [])
        .find(e => (e._node as AsbiepFlatNode).asbiePath === sourceAsbiePathContext.path);
      if (!!source) {
        source.bieId = Number(asbieId);
        source.context = sourceAsbiePathContext.context;
        const targetAsbiePathContext = bieUpliftMap.targetAsbiePathMap[asbieId];
        const targetManifestId = this._getManifestId(this._getLastTag(targetAsbiePathContext?.path));
        const target = (targetAsbiepList.has(targetManifestId) ? targetAsbiepList.get(targetManifestId) : [])
          .find(e => (e._node as AsbiepFlatNode).asbiePath === targetAsbiePathContext?.path);
        if (!!target) {
          source.target = target;
          source.fixed = true;
          target.source = source;
        }
      }
    }
    for (const [bbieId, sourceBbiePathContext] of Object.entries(bieUpliftMap.sourceBbiePathMap)) {
      const sourceManifestId = this._getManifestId(this._getLastTag(sourceBbiePathContext.path));
      const source = (sourceBbiepList.has(sourceManifestId) ? sourceBbiepList.get(sourceManifestId) : [])
        .find(e => (e._node as BbiepFlatNode).bbiePath === sourceBbiePathContext.path);
      if (!!source) {
        source.bieId = Number(bbieId);
        source.context = sourceBbiePathContext.context;
        const targetBbiePathContext = bieUpliftMap.targetBbiePathMap[bbieId];
        const targetManifestId = this._getManifestId(this._getLastTag(targetBbiePathContext?.path));
        const target = (targetBbiepList.has(targetManifestId) ? targetBbiepList.get(targetManifestId) : [])
          .find(e => (e._node as BbiepFlatNode).bbiePath === targetBbiePathContext?.path);
        if (!!target) {
          source.target = target;
          source.fixed = true;
          target.source = source;
        }
      }
    }
    for (const [bbieScId, sourceBbieScPathContext] of Object.entries(bieUpliftMap.sourceBbieScPathMap)) {
      const sourceManifestId = this._getManifestId(this._getLastTag(sourceBbieScPathContext.path));
      const source = (sourceBbieScList.has(sourceManifestId) ? sourceBbieScList.get(sourceManifestId) : [])
        .find(e => (e._node as BbieScFlatNode).bbieScPath === sourceBbieScPathContext.path);
      if (!!source) {
        source.bieId = Number(bbieScId);
        source.context = sourceBbieScPathContext.context;
        const targetBbieScPathContext = bieUpliftMap.targetBbieScPathMap[bbieScId];
        const targetManifestId = this._getManifestId(this._getLastTag(targetBbieScPathContext?.path));
        const target = (targetBbieScList.has(targetManifestId) ? targetBbieScList.get(targetManifestId) : [])
          .find(e => (e._node as BbieScFlatNode).bbieScPath === targetBbieScPathContext?.path);
        if (!!target) {
          source.target = target;
          source.fixed = true;
          target.source = source;
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

  onContextMenu($event: MouseEvent, item: BieFlatNode): void {
    let contextMenu;
    if (item instanceof BieUpliftSourceFlatNode) {
      contextMenu = this.defaultSourceContextMenu;
    } else if (item instanceof BieUpliftTargetFlatNode) {
      contextMenu = this.defaultTargetContextMenu;
    }

    if (contextMenu) {
      this.contextMenuService.show.next({
        contextMenu,
        event: $event,
        item,
      });
    }

    $event.preventDefault();
    $event.stopPropagation();
  }

  search(type: string, backward?: boolean, force?: boolean) {
    if (type === 'source') {
      this.sourceSearcher.search(this.sourceSearcher.inputKeyword, this.sourceSelectedNode, backward, force)
        .subscribe(index => {
          this.sourceVirtualScroll.scrollToIndex(index);
        });
    } else if (type === 'target') {
      this.targetSearcher.search(this.targetSearcher.inputKeyword, this.targetSelectedNode, backward, force)
        .subscribe(index => {
          this.targetVirtualScroll.scrollToIndex(index);
        });
    }
  }

  move(type: string, val: number) {
    if (type === 'source') {
      this.sourceSearcher.go(val).subscribe(index => {
        this.sourceVirtualScroll.scrollToIndex(index);
      });
    } else if (type === 'target') {
      this.targetSearcher.go(val).subscribe(index => {
        this.targetVirtualScroll.scrollToIndex(index);
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
    const index = this.sourceDataSource.data.indexOf(node);
    this.scrollTree(this.sourceVirtualScroll, index);
  }

  scrollToTargetNode(node: BieUpliftTargetFlatNode) {
    const index = this.targetDataSource.data.indexOf(node);
    this.scrollTree(this.targetVirtualScroll, index);
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

  scrollTree(scroll: CdkVirtualScrollViewport, index: number) {
    const range = scroll.getRenderedRange();
    if (range.start + 10 > index || range.end - 10 < index) {
      scroll.scrollToIndex(index);
    }
  }

  canMatch(node: BieUpliftTargetFlatNode): boolean {
    if (!this.sourceSelectedNode || this.sourceSelectedNode.fixed) {
      return false;
    }
    if (this.sourceSelectedNode.type.toUpperCase() === node.type.toUpperCase()) {
      if (node.source === undefined || node.source === this.sourceSelectedNode) {
        return true;
      }
    }
    return false;
  }

  checkMatch(event, node: BieUpliftTargetFlatNode) {
    if (!this.canMatch(node)) {
      return;
    }
    if (node.source) {
      node.source = undefined;
      node.reusedTolevelAsbiepId = undefined;
      this.sourceSelectedNode.target = undefined;
    } else {
      if (this.sourceSelectedNode.target) {
        this.sourceSelectedNode.target.reusedTolevelAsbiepId = undefined;
        this.sourceSelectedNode.target.source = undefined;
      }
      this.sourceSelectedNode.target = undefined;
      node.source = this.sourceSelectedNode;
      this.sourceSelectedNode.target = node;
    }
  }

  createUpliftBIE() {
    this.loading = true;
    const source = this.sourceDataSource.cachedData.filter(e => {
      if (e.derived) {
        return true
      }
      if (!e.fixed) {
        return !e.locked;
      }
    });
    const matched = [];
    source.forEach(e => {
      let upliftNode;
      if (e.target) {
        upliftNode = new UpliftNode(e.type, e.bieId, e.path, e.target.path, e.target.reusedTolevelAsbiepId);
        if (e.type.toUpperCase() === 'ASBIEP') {
          upliftNode.bieType = 'ASBIE';
          upliftNode.sourceManifestId = (e._node as AsbiepFlatNode).asccNode.manifestId;
          upliftNode.targetManifestId = (e.target._node as AsbiepFlatNode).asccNode.manifestId;
        } else if (e.type.toUpperCase() === 'BBIEP') {
          upliftNode.bieType = 'BBIEP';
          upliftNode.sourceManifestId = (e._node as BbiepFlatNode).bccNode.manifestId;
          upliftNode.targetManifestId = (e.target._node as BbiepFlatNode).bccNode.manifestId;
        } else if (e.type.toUpperCase() === 'BBIE_SC') {
          upliftNode.bieType = 'BBIE_SC';
          upliftNode.sourceManifestId = (e._node as BbieScFlatNode).bdtScNode.manifestId;
          upliftNode.targetManifestId = (e.target._node as BbieScFlatNode).bdtScNode.manifestId;
        }
        e.target.parents.forEach(p => {
          const parentTargetNode = this.targetDataSource.cachedData.find(v => v.equal(p));
          if (parentTargetNode && parentTargetNode.level !== 0 && !parentTargetNode.source) {
            parentTargetNode.emptyRequired = true;
          }
        });
      } else {
        upliftNode = new UpliftNode(e.type, e.bieId, e.path);
        if (e.type.toUpperCase() === 'ASBIEP') {
          upliftNode.bieType = 'ASBIE';
          upliftNode.sourceManifestId = (e._node as AsbiepFlatNode).asccNode.manifestId;
        } else if (e.type.toUpperCase() === 'BBIEP') {
          upliftNode.bieType = 'BBIEP';
          upliftNode.sourceManifestId = (e._node as BbiepFlatNode).bccNode.manifestId;
        } else if (e.type.toUpperCase() === 'BBIE_SC') {
          upliftNode.bieType = 'BBIE_SC';
          upliftNode.sourceManifestId = (e._node as BbieScFlatNode).bdtScNode.manifestId;
        }
      }

      matched.push(upliftNode);
    });

    const targets = this.targetDataSource.cachedData.filter(e => e.emptyRequired);
    targets.forEach(e => {
      const upliftNode = new UpliftNode(e.type, null, null, e.path, null);
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
        this.router.navigateByUrl('/profile_bie/edit/' + result.topLevelAsbiepId);
      });
  }

  back() {
    this.router.navigateByUrl('/profile_bie/uplift');
  }

  matchReused(node: BieUpliftTargetFlatNode) {
    if (node.source && node.source.derived) {
      const dialogRef = this.dialog.open(ReuseBieDialogComponent, {
        data: {
          asccpManifestId: (node._node as unknown as AsbiepFlatNode).asccpNode.manifestId,
          releaseId: this.targetReleaseId,
          topLevelAsbiepId: this.topLevelAsbiepId
        },
        width: '100%',
        maxWidth: '100%',
        height: '100%',
        maxHeight: '100%',
        autoFocus: false
      });
      dialogRef.afterClosed().subscribe(selectedTopLevelAsbiepId => {
        if (!selectedTopLevelAsbiepId) {
          node.reusedTolevelAsbiepId = undefined;
        } else {
          node.reusedTolevelAsbiepId = selectedTopLevelAsbiepId;
        }
      });
    }
  }

  report() {
    const reports = [];
    this.sourceDataSource.cachedData.filter(e => e.level > 0 && e.used && !e.locked).forEach(e => {
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
        this.createUpliftBIE();
      }
    })
  }

  expandSourceNode(node: BieUpliftSourceFlatNode) {
    node.parents.forEach(p => {
      const parentNode = this.sourceDataSource.cachedData.find(v => v.equal(p));
      this.sourceTreeControl.expand(parentNode);
    });

    this.onSourceClick(node);
  }

  expandTargetNode(node: BieUpliftTargetFlatNode) {
    node.parents.forEach(p => {
      const parentNode = this.targetDataSource.cachedData.find(v => v.equal(p));
      if (node !== parentNode) {
        this.targetTreeControl.expand(parentNode);
      }
    });
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
      this.sourceDataSource.data.indexOf(this.currentUnmatchedSource)
    );
  }
}
