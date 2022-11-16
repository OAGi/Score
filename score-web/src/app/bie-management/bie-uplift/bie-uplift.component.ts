import {CdkVirtualScrollViewport} from '@angular/cdk/scrolling';
import {Component, OnInit, ViewChild} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {forkJoin} from 'rxjs';
import {finalize} from 'rxjs/operators';
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
import {
  BieUpliftMap,
  BieUpliftSourceFlatNode,
  BieUpliftTargetFlatNode,
  MatchInfo,
  UpliftNode
} from './domain/bie-uplift';
import {CcNodeService} from '../../cc-management/domain/core-component-node.service';
import {
  AsbiepFlatNode,
  BbiepFlatNode,
  BbieScFlatNode,
  BieFlatNode,
  BieFlatNodeDatabase,
  BieFlatNodeDataSource,
  BieFlatNodeDataSourceSearcher
} from '../domain/bie-flat-tree';
import {CcGraphNode} from '../../cc-management/domain/core-component-node';
import {ReportDialogComponent} from './report-dialog/report-dialog.component';


export class BieUpliftSourceFlatNodeDatabase<T extends BieFlatNode> extends BieFlatNodeDatabase<T> {
  get rootNode(): T {
    const rootNode = super.rootNode;
    return new BieUpliftSourceFlatNode(rootNode) as unknown as T;
  }
  loadChildren(node: T) {
    super.loadChildren(node);
    node.children = node.children.map(e => new BieUpliftSourceFlatNode(e as T));
  }
}

export class BieUpliftTargetFlatNodeDatabase<T extends BieFlatNode> extends BieFlatNodeDatabase<T> {
  get rootNode(): T {
    const rootNode = super.rootNode;
    return new BieUpliftTargetFlatNode(rootNode) as unknown as T;
  }
  loadChildren(node: T) {
    super.loadChildren(node);
    node.children = node.children.map(e => new BieUpliftTargetFlatNode(e as T));
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

      forkJoin([
        this.bieEditService.getGraphNode(this.topLevelAsbiepId),
        this.bieEditService.getRootNode(this.topLevelAsbiepId),
        this.bieEditService.getUsedBieList(this.topLevelAsbiepId),
        this.bieEditService.getRefBieList(this.topLevelAsbiepId),
        this.ccNodeService.getGraphNode('ASCCP', this.targetAsccpManifestId)
      ]).subscribe(([sourceCcGraph, sourceRootNode,
                      sourceUsedBieList, sourceRefBieList,
                      targetCcGraph]) => {
        this.bieGuid = sourceRootNode.guid;
        this.bieName = sourceRootNode.name;
        this.sourceReleaseNum = sourceRootNode.releaseNum;

        const sourceDatabase = new BieUpliftSourceFlatNodeDatabase<BieUpliftSourceFlatNode>(sourceCcGraph,
          sourceRootNode.asccpManifestId, this.topLevelAsbiepId, sourceUsedBieList, sourceRefBieList);
        this.sourceDataSource = new BieFlatNodeDataSource<BieUpliftSourceFlatNode>(sourceDatabase, this.bieEditService);
        this.sourceSearcher = new BieFlatNodeDataSourceSearcher<BieUpliftSourceFlatNode>(this.sourceDataSource, sourceDatabase);
        this.sourceDataSource.init();

        this.sourceDataSource.hideUnused = true;

        const targetDatabase = new BieUpliftTargetFlatNodeDatabase<BieUpliftTargetFlatNode>(targetCcGraph,
          this.targetAsccpManifestId, undefined, [], []);
        this.targetDataSource = new BieFlatNodeDataSource<BieUpliftTargetFlatNode>(targetDatabase, this.bieEditService);
        this.targetSearcher = new BieFlatNodeDataSourceSearcher<BieUpliftTargetFlatNode>(this.targetDataSource, targetDatabase);
        this.targetDataSource.init();

        this.bieUpliftService.getUpliftBieMap(this.topLevelAsbiepId, this.targetReleaseId).subscribe(bieUpliftMap => {
          const sourceData = [];
          let sourceStack = [this.sourceDataSource.data[0], ];
          while (sourceStack.length > 0) {
            const item = sourceStack.shift();
            sourceData.push(item);
            if (item.expandable && item.children.length === 0) {
              this.sourceDataSource.database.loadChildren(item);
            }
            sourceStack = item.children.filter(e => (e as BieUpliftSourceFlatNode).used)
              .concat(sourceStack) as BieUpliftSourceFlatNode[];
          }

          const targetData = [];
          let targetStack = [this.targetDataSource.data[0], ];
          while (targetStack.length > 0) {
            const targetItem = targetStack.shift();
            for (const sourceItem of sourceData) {
              if (sourceItem.bieType === targetItem.bieType &&
                sourceItem.name === targetItem.name &&
                sourceItem.level === targetItem.level) {
                targetData.push(targetItem);

                if (targetItem.expandable && targetItem.children.length === 0) {
                  this.targetDataSource.database.loadChildren(targetItem);
                }
                targetStack = targetItem.children.concat(targetStack) as BieUpliftTargetFlatNode[];
                break;
              }
            }
          }

          this.initMapping(sourceData, targetData, bieUpliftMap);
          this.sourceDataSource.collapse(this.sourceDataSource.data[0]);
          this.targetDataSource.collapse(this.targetDataSource.data[0]);

          sourceData[0].target = targetData[0];
          this.unmatchedSource = this.sourceDataSource.data.filter(e => !e.isMapped);
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

  initMapping(sourceData: BieUpliftSourceFlatNode[], targetData: BieUpliftTargetFlatNode[],
              bieUpliftMap: BieUpliftMap) {
    const sourceAsbiepList = new Map<number, BieUpliftSourceFlatNode[]>();
    const sourceBbiepList = new Map<number, BieUpliftSourceFlatNode[]>();
    const sourceBbieScList = new Map<number, BieUpliftSourceFlatNode[]>();

    sourceData.forEach(e => {
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

    targetData.forEach(e => {
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
      index = this.sourceDataSource.data.map(e => e.hashPath).indexOf(node.hashPath);
      if (index !== -1) {
        break;
      }
      this.sourceDataSource.toggleNode(currentNode.parent as BieUpliftSourceFlatNode, true);
      currentNode = currentNode.parent as BieUpliftSourceFlatNode;
    }
    this.scrollTree(this.sourceVirtualScroll, index);
  }

  scrollToTargetNode(node: BieUpliftTargetFlatNode) {
    let index = -1;
    let currentNode = node;
    while (currentNode) {
      index = this.targetDataSource.data.map(e => e.hashPath).indexOf(node.hashPath);
      if (index !== -1) {
        break;
      }
      this.targetDataSource.toggleNode(currentNode.parent as BieUpliftTargetFlatNode, true);
      currentNode = currentNode.parent as BieUpliftTargetFlatNode;
    }
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
    if (index < 0) {
      return;
    }
    scroll.scrollToOffset(index * this.virtualScrollItemSize, 'smooth');
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
      node.reusedTopLevelAsbiepId = undefined;
      this.sourceSelectedNode.target = undefined;
    } else {
      if (this.sourceSelectedNode.target) {
        this.sourceSelectedNode.target.reusedTopLevelAsbiepId = undefined;
        this.sourceSelectedNode.target.source = undefined;
      }
      this.sourceSelectedNode.target = undefined;
      node.source = this.sourceSelectedNode;
      this.sourceSelectedNode.target = node;
    }
  }

  createUpliftBIE() {
    this.loading = true;
    const source = this.sourceDataSource.data.filter(e => {
      if (e.derived) {
        return true;
      }
      if (!e.fixed) {
        return !e.locked;
      }
    });

    const matched = [];
    source.forEach(e => {
      let upliftNode;
      if (e.target) {
        upliftNode = new UpliftNode(e.type, e.bieId, e.path, e.target.path, e.target.reusedTopLevelAsbiepId);
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
          const parentTargetNode = this.targetDataSource.data.find(v => v.equal(p));
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

    const targets = this.targetDataSource.data.filter(e => e.emptyRequired);
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
        this.router.navigateByUrl('/profile_bie/' + result.topLevelAsbiepId);
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
          node.reusedTopLevelAsbiepId = undefined;
        } else {
          node.reusedTopLevelAsbiepId = selectedTopLevelAsbiepId;
        }
      });
    }
  }

  report() {
    const reports = [];
    this.sourceDataSource.data.filter(e => e.level > 0 && e.used && !e.locked).forEach(e => {
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
