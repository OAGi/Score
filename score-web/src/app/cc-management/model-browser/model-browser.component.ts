import {Component, HostListener, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {ModelBrowserService} from './domain/model-browser.service';
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
  ModelBrowserAccNodeDetail,
  ModelBrowserAsccpNodeDetail,
  ModelBrowserBccpNodeDetail,
  ModelBrowserBdtScNodeDetail,
  ModelBrowserNode,
  ModelBrowserNodeDatabase,
  ModelBrowserNodeDataSource,
  ModelBrowserNodeDataSourceSearcher
} from './domain/model-browser-node';
import {CdkVirtualScrollViewport} from '@angular/cdk/scrolling';
import {MatMenuTrigger} from '@angular/material/menu';
import {loadBooleanProperty, saveBooleanProperty} from '../../common/utility';
import {BieEditBbieScNodeDetail, ChangeListener} from '../../bie-management/domain/bie-flat-tree';


@Component({
  selector: 'score-model-browser',
  templateUrl: './model-browser.component.html',
  styleUrls: ['./model-browser.component.css']
})
export class ModelBrowserComponent implements OnInit, ChangeListener<ModelBrowserNode> {

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

  constructor(private service: ModelBrowserService,
              private snackBar: MatSnackBar,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private dialog: MatDialog,
              private confirmDialogService: ConfirmDialogService,
              private preferencesService: SettingsPreferencesService,
              private auth: AuthService,
              private stompService: RxStompService,
              private clipboard: Clipboard,
              public webPageInfo: WebPageInfoService) {
  }

  ngOnInit(): void {
    this.route.paramMap.pipe(
      switchMap((params: ParamMap) => {
        this.type = params.get('type');
        this.manifestId = parseInt(params.get('manifestId'), 10);

        return forkJoin([
          this.service.getGraphNode(this.type, this.manifestId),
          this.preferencesService.load(this.auth.getUserToken())
        ]);
      })).subscribe(([ccGraph, preferencesInfo]) => {

      this.preferencesInfo = preferencesInfo;

      const database = new ModelBrowserNodeDatabase<ModelBrowserNode>(ccGraph, this.type, this.manifestId);
      this.dataSource = new ModelBrowserNodeDataSource<ModelBrowserNode>(database, this.service, [this,]);
      this.searcher = new ModelBrowserNodeDataSourceSearcher<ModelBrowserNode>(this.dataSource, database);
      this.dataSource.init();
      this.dataSource.hideCardinality = loadBooleanProperty(this.auth.getUserToken(), this.HIDE_CARDINALITY_PROPERTY_KEY, false);

      this.onClick(this.dataSource.data[0]);

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
      this.router.navigateByUrl('/core_components');
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
        .filter(e => e.menuData.menuId === 'contextMenu').forEach(trigger => {
        this.contextMenuItem = node;
        trigger.openMenu();
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

  isBdtScDetail(node?: ModelBrowserNode): boolean {
    if (!node) {
      node = this.selectedNode;
    }
    return (node !== undefined) && (node.ccType.toUpperCase() === 'BDT_SC');
  }

  asBdtScDetail(node?: ModelBrowserNode): ModelBrowserBdtScNodeDetail {
    if (!node) {
      node = this.selectedNode;
    }
    return node.detail as ModelBrowserBdtScNodeDetail;
  }

}
