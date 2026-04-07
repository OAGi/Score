import { Component, HostListener, OnInit, QueryList, ViewChild, ViewChildren, inject } from '@angular/core';
import {switchMap} from 'rxjs/operators';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {Location} from '@angular/common';
import {forkJoin} from 'rxjs';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatDialog} from '@angular/material/dialog';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {AuthService} from '../../authentication/auth.service';
import {Clipboard} from '@angular/cdk/clipboard';
import {RxStompService} from '../../common/score-rx-stomp';
import {WebPageInfoService} from '../../basis/basis.service';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';
import {PreferencesInfo} from '../../settings-management/settings-preferences/domain/preferences';
import {
  ModelBrowserAsccpNode,
  ModelBrowserAccNodeDetail,
  ModelBrowserAsccpNodeDetail,
  ModelBrowserBccpNodeDetail,
  ModelBrowserDtScNodeDetail,
  ModelBrowserNode,
  ModelBrowserNodeDatabase,
  ModelBrowserNodeDataSource,
  ModelBrowserNodeDataSourceSearcher
} from './domain/model-browser-node';
import {CdkVirtualScrollViewport} from '@angular/cdk/scrolling';
import {MatMenuTrigger} from '@angular/material/menu';
import {loadBooleanProperty, saveBooleanProperty} from '../../common/utility';
import {ChangeListener} from '../../bie-management/domain/bie-flat-tree';
import {CcNodeService} from '../domain/core-component-node.service';
import {CcFlatNode} from '../domain/cc-flat-tree';
import {FindUsagesDialogComponent} from '../find-usages-dialog/find-usages-dialog.component';


@Component({
  standalone: false,
  selector: 'score-model-browser',
  templateUrl: './model-browser.component.html',
  styleUrls: ['./model-browser.component.css']
})
export class ModelBrowserComponent implements OnInit, ChangeListener<ModelBrowserNode> {
  private ccNodeService = inject(CcNodeService);
  private snackBar = inject(MatSnackBar);
  private location = inject(Location);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private dialog = inject(MatDialog);
  private confirmDialogService = inject(ConfirmDialogService);
  private preferencesService = inject(SettingsPreferencesService);
  private auth = inject(AuthService);
  private stompService = inject(RxStompService);
  private clipboard = inject(Clipboard);
  webPageInfo = inject(WebPageInfoService);


  loading = false;
  paddingPixel = 12;

  type: string;
  manifestId: number;

  innerY: number = window.innerHeight;
  dataSource: ModelBrowserNodeDataSource<ModelBrowserNode>;
  searcher: ModelBrowserNodeDataSourceSearcher<ModelBrowserNode>;
  cursorNode: ModelBrowserNode;
  selectedNode: ModelBrowserNode;

  @ViewChild('virtualScroll', {static: true}) public virtualScroll: CdkVirtualScrollViewport;
  virtualScrollItemSize = 33;

  get minBufferPx(): number {
    return 10000 * this.virtualScrollItemSize;
  }

  get maxBufferPx(): number {
    return 1000000 * this.virtualScrollItemSize;
  }

  @ViewChildren(MatMenuTrigger) menuTriggerList: QueryList<MatMenuTrigger>;
  contextMenuItem: ModelBrowserNode;

  preferencesInfo: PreferencesInfo;
  HIDE_CARDINALITY_PROPERTY_KEY = 'ModelBrowser-Settings-Hide-Cardinality';

  get hideCardinality(): boolean {
    return this.dataSource.hideCardinality;
  }

  set hideCardinality(hideCardinality: boolean) {
    this.dataSource.hideCardinality = hideCardinality;
    saveBooleanProperty(this.auth.getUserToken(), this.HIDE_CARDINALITY_PROPERTY_KEY, hideCardinality);
  }

  ngOnInit(): void {
    this.loading = true;
    this.route.paramMap.pipe(
      switchMap((params: ParamMap) => {
        this.type = params.get('type');
        this.manifestId = parseInt(params.get('manifestId'), 10);

        return forkJoin([
          this.ccNodeService.getGraphNode(this.type, this.manifestId),
          this.preferencesService.load(this.auth.getUserToken())
        ]);
      })).subscribe(([ccGraph, preferencesInfo]) => {

      this.preferencesInfo = preferencesInfo;

      const database = new ModelBrowserNodeDatabase<ModelBrowserNode>(ccGraph, this.type, this.manifestId);
      this.dataSource = new ModelBrowserNodeDataSource<ModelBrowserNode>(database, this.ccNodeService, [this,]);
      this.searcher = new ModelBrowserNodeDataSourceSearcher<ModelBrowserNode>(this.dataSource, database);
      this.dataSource.init();
      this.dataSource.hideCardinality = loadBooleanProperty(this.auth.getUserToken(), this.HIDE_CARDINALITY_PROPERTY_KEY, false);

      this.onClick(this.dataSource.data[0]);

      this.loading = false;
      return;
    }, err => {
      let errorMessage;
      if (err.status === 403) {
        errorMessage = 'You do not have access permission.';
      } else {
        errorMessage = 'Something\'s wrong.';
      }
      this.snackBar.open(errorMessage, '', {
        duration: 3000
      });
      this.router.navigateByUrl('/core_component');
    });
  }

  onResize(event) {
    this.innerY = window.innerHeight;
  }

  get innerHeight(): number {
    return this.innerY - 200;
  }

  toggle(node: ModelBrowserNode, $event?: MouseEvent) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    this.dataSource.toggle(node);
  }

  onClick(node: ModelBrowserNode, $event?: MouseEvent) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    this.dataSource.loadDetail(node, (detailNode: ModelBrowserNode) => {
      this.selectedNode = node;
      this.cursorNode = node;
    });

  }

  scrollToNode(node: ModelBrowserNode, delay?: number) {
    const index = this.searcher.getNodeIndex(node);
    this.scrollTree(index, delay);
    this.cursorNode = node;
  }

  private visibleChoiceChildrenOf(choiceNode: ModelBrowserNode): ModelBrowserNode[] {
    return (((choiceNode.children || []) as ModelBrowserNode[]))
      .filter(child => !child.isGroup && !child.isChoice);
  }

  isChoiceChild(node: ModelBrowserNode): boolean {
    return !!node?.parent && (node.parent as ModelBrowserNode).isChoice;
  }

  private visibleChoiceChildren(node: ModelBrowserNode): ModelBrowserNode[] {
    if (!node?.parent || !(node.parent as ModelBrowserNode).isChoice) {
      return [];
    }
    return this.visibleChoiceChildrenOf(node.parent as ModelBrowserNode);
  }

  isFirstChoiceChild(node: ModelBrowserNode): boolean {
    const siblings = this.visibleChoiceChildren(node);
    return siblings.length > 0 && siblings[0] === node;
  }

  isLastChoiceChild(node: ModelBrowserNode): boolean {
    const siblings = this.visibleChoiceChildren(node);
    return siblings.length > 0 && siblings[siblings.length - 1] === node;
  }

  isOnlyChoiceChild(node: ModelBrowserNode): boolean {
    return this.visibleChoiceChildren(node).length === 1;
  }

  private choiceBranchRoot(node: ModelBrowserNode): ModelBrowserNode | null {
    let current = node;
    while (!!current?.parent) {
      const parent = current.parent as ModelBrowserNode;
      if (parent.isChoice) {
        return current;
      }
      current = parent;
    }
    return null;
  }

  choiceTooltip(node: ModelBrowserNode): string | null {
    let current = node;
    while (!!current?.parent) {
      const parent = current.parent as ModelBrowserNode;
      if (parent.isChoice) {
        return (parent as ModelBrowserAsccpNode).accNode?.objectClassTerm || parent.name;
      }
      current = parent;
    }
    return null;
  }

  hasChoiceBranchContinuation(node: ModelBrowserNode): boolean {
    const branchRoot = this.choiceBranchRoot(node);
    return !!branchRoot && branchRoot !== node && !this.isLastChoiceChild(branchRoot);
  }

  choiceGuideExtraOffset(node: ModelBrowserNode): number {
    const branchRoot = this.choiceBranchRoot(node);
    if (!branchRoot) {
      return 0;
    }
    return Math.max(0, node.level - branchRoot.level) * this.paddingPixel;
  }

  choiceGuides(node: ModelBrowserNode): {offset: number, continuation: boolean, first: boolean, last: boolean, only: boolean, tooltip: string | null}[] {
    const guides: {offset: number, continuation: boolean, first: boolean, last: boolean, only: boolean, tooltip: string | null}[] = [];
    let current: ModelBrowserNode = node;

    while (!!current?.parent) {
      const parent = current.parent as ModelBrowserNode;
      if (parent.isChoice) {
        const siblings = this.visibleChoiceChildrenOf(parent);
        const branchRoot = current;
        const branchIndex = siblings.indexOf(branchRoot);
        if (branchIndex !== -1) {
          const continuation = branchRoot !== node;
          const last = branchIndex === siblings.length - 1;
          if (!continuation || !last) {
            guides.push({
              offset: Math.max(0, node.level - branchRoot.level) * this.paddingPixel,
              continuation,
              first: branchIndex === 0,
              last,
              only: siblings.length === 1,
              tooltip: (parent as ModelBrowserAsccpNode).accNode?.objectClassTerm || parent.name
            });
          }
        }
      }
      current = parent;
    }

    return guides.sort((a, b) => b.offset - a.offset);
  }

  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent($event: KeyboardEvent) {
    const charCode = $event.key?.toLowerCase();

  }

  keyNavigation(node: ModelBrowserNode, $event: KeyboardEvent) {
    if ($event.key === 'ArrowDown') {
      this.cursorNode = this.searcher.next(this.cursorNode);
    } else if ($event.key === 'ArrowUp') {
      this.cursorNode = this.searcher.prev(this.cursorNode);
    } else if ($event.key === 'ArrowLeft' || $event.key === 'ArrowRight') {
      this.dataSource.toggle(this.cursorNode);
    } else if ($event.key === 'o' || $event.key === 'O') {
      this.menuTriggerList.toArray().filter(e => !!e.menuData)
          .filter(e => e.menuData.menuId === 'contextMenu' && e.menuData.hashPath === node.hashPath)
          .forEach(trigger => {
            this.contextMenuItem = node;
            if (!trigger.menuOpen) {
              trigger.openMenu();
            }
          });
    } else if ($event.key === 'Enter') {
      this.onClick(this.cursorNode);
    } else {
      return;
    }
    $event.preventDefault();
    $event.stopPropagation();
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

  scrollTree(index: number, delay?: number) {
    if (index < 0) {
      return;
    }

    if (delay) {
      setTimeout(() => {
        this.virtualScroll.scrollToOffset(index * this.virtualScrollItemSize, 'smooth');
      }, delay);
    } else {
      this.virtualScroll.scrollToOffset(index * this.virtualScrollItemSize, 'smooth');
    }
  }

  search(inputKeyword, backward?: boolean, force?: boolean) {
    this.searcher.search(inputKeyword, this.selectedNode, backward, force).subscribe(index => {
      this.scrollTree(index, 500);
    });
  }

  move(val: number) {
    this.searcher.go(val).subscribe(index => {
      this.onClick(this.dataSource.data[index]);
      this.scrollTree(index);
    });
  }

  copyPath(node: ModelBrowserNode) {
    if (!node) {
      return;
    }

    const delimiter = this.preferencesInfo.viewSettingsInfo.treeSettings.delimiter;
    let queryPath = node.queryPath;
    queryPath = queryPath.replaceAll('/', delimiter);

    this.clipboard.copy(queryPath);
    this.snackBar.open('Copied to clipboard', '', {
      duration: 3000
    });
  }

  openDiagram(node: ModelBrowserNode) {
    if (!node) {
      return;
    }

    if (this.isAccDetail(node)) {
      window.open('/core_component/browser/asccp/' + this.asAccDetail(node).asccp.manifestId + '/plantuml', '_blank');
    } else if (this.isAsccpDetail(node)) {
      window.open('/core_component/browser/asccp/' + this.asAsccpDetail(node).asccp.manifestId + '/plantuml', '_blank');
    } else {
      return;
    }
  }

  findUsages(node: ModelBrowserNode) {
    if (!node) {
      return;
    }

    let data;
    if (this.isAccDetail(node)) { // if the node is a root
      data = {
        type: 'ASCCP',
        manifestId: this.asAccDetail(node).asccp.manifestId
      };
    } else if (this.isAsccpDetail(node)) {
      data = {
        type: node.ccType,
        manifestId: this.asAsccpDetail(node).asccp.manifestId
      };
    } else if (this.isBccpDetail(node)) {
      data = {
        type: node.ccType,
        manifestId: this.asBccpDetail(node).bccp.manifestId
      };
    } else {
      return;
    }

    const dialogRef = this.dialog.open(FindUsagesDialogComponent, {
      data,
      width: '600px',
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe(_ => {
    });
  }

  copyLink(node: ModelBrowserNode, $event?) {
    if ($event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    if (!node) {
      return;
    }

    const url = window.location.href;
    const manifestId = this.manifestId.toString();
    const idIdx = url.indexOf(manifestId);
    const queryPath = url.substring(0, idIdx + manifestId.length) + '/' + node.queryPath;

    this.clipboard.copy(queryPath);
    this.snackBar.open('Copied to clipboard', '', {
      duration: 3000
    });
  }

  onChange(entity: ModelBrowserNode, propertyName: string, val: any) {
  }

  /* For type casting of detail property */
  isAccDetail(node?: ModelBrowserNode): boolean {
    if (!node) {
      node = this.selectedNode;
    }
    return (node !== undefined) && (node.ccType.toUpperCase() === 'ACC');
  }

  asAccDetail(node?: ModelBrowserNode): ModelBrowserAccNodeDetail {
    if (!node) {
      node = this.selectedNode;
    }
    return node.detail as ModelBrowserAccNodeDetail;
  }

  isAsccpDetail(node?: ModelBrowserNode): boolean {
    if (!node) {
      node = this.selectedNode;
    }
    return (node !== undefined) && (node.ccType.toUpperCase() === 'ASCCP');
  }

  asAsccpDetail(node?: ModelBrowserNode): ModelBrowserAsccpNodeDetail {
    if (!node) {
      node = this.selectedNode;
    }
    return node.detail as ModelBrowserAsccpNodeDetail;
  }

  isBccpDetail(node?: ModelBrowserNode): boolean {
    if (!node) {
      node = this.selectedNode;
    }
    return (node !== undefined) && (node.ccType.toUpperCase() === 'BCCP');
  }

  asBccpDetail(node?: ModelBrowserNode): ModelBrowserBccpNodeDetail {
    if (!node) {
      node = this.selectedNode;
    }
    return node.detail as ModelBrowserBccpNodeDetail;
  }

  isDtScDetail(node?: ModelBrowserNode): boolean {
    if (!node) {
      node = this.selectedNode;
    }
    return (node !== undefined) && (node.ccType.toUpperCase() === 'BDT_SC');
  }

  asDtScDetail(node?: ModelBrowserNode): ModelBrowserDtScNodeDetail {
    if (!node) {
      node = this.selectedNode;
    }
    return node.detail as ModelBrowserDtScNodeDetail;
  }

  visibleFindUsages(node?: ModelBrowserNode): boolean {
    if (!node) {
      return false;
    }
    return node.ccType.toUpperCase() === 'ACC' || node.ccType.toUpperCase() === 'ASCCP' || node.ccType.toUpperCase() === 'BCCP';
  }

}
