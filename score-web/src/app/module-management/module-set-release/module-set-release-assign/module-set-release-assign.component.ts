import {SelectionModel} from '@angular/cdk/collections';
import {Location} from '@angular/common';
import {Component, OnInit, ViewChild} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {forkJoin} from 'rxjs';
import {finalize, switchMap} from 'rxjs/operators';
import {AuthService} from '../../../authentication/auth.service';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {AssignableMap, AssignableNode} from '../../../release-management/domain/release';
import {ReleaseService} from '../../../release-management/domain/release.service';
import {ModuleElement, ModuleSetRelease, Tile} from '../../domain/module';
import {ModuleService} from '../../domain/module.service';
import {MatPaginator} from '@angular/material/paginator';
import {UserToken} from '../../../authentication/domain/auth';
import {WebPageInfoService} from '../../../basis/basis.service';

@Component({
  selector: 'score-module-set-assign',
  templateUrl: './module-set-release-assign.component.html',
  styleUrls: ['./module-set-release-assign.component.css']
})
export class ModuleSetReleaseAssignComponent implements OnInit {

  title = 'Edit Module Set Release';
  displayedColumns = ['checkbox', 'type', 'state', 'den', 'timestamp'];
  ccTypes = ['ACC', 'ASCCP', 'BCCP', 'DT', 'CODE_LIST', 'AGENCY_ID_LIST', 'XBT'];
  workingStateList = ['WIP', 'Draft', 'Candidate', 'ReleaseDraft', 'Published', 'Deleted'];
  isUpdating: boolean;
  moduleSetRelease: ModuleSetRelease = new ModuleSetRelease();

  tiles: Tile[] = [];
  rootElement: ModuleElement;
  selectedModuleElement: ModuleElement;

  leftDataSource = new MatTableDataSource();
  leftSelection = new SelectionModel<AssignableNode>(true, []);
  leftFilteredValues = {types: [], states: [], den: ''};

  rightDataSource = new MatTableDataSource();
  rightSelection = new SelectionModel<AssignableNode>(true, []);
  rightFilteredValues = {types: [], states: [], den: ''};

  @ViewChild('leftSort', {static: true}) leftSort: MatSort;
  @ViewChild('leftPaginator', {static: true}) leftPaginator: MatPaginator;
  @ViewChild('rightSort', {static: true}) rightSort: MatSort;
  @ViewChild('rightPaginator', {static: true}) rightPaginator: MatPaginator;

  constructor(private moduleService: ModuleService,
              private releaseService: ReleaseService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private auth: AuthService,
              private confirmDialogService: ConfirmDialogService,
              public webPageInfo: WebPageInfoService) {
  }

  ngOnInit(): void {
    this.leftDataSource = new MatTableDataSource<AssignableNode>();
    this.leftDataSource.sort = this.leftSort;
    this.leftDataSource.paginator = this.leftPaginator;
    this.leftDataSource.filterPredicate = this.customFilterPredicate;

    this.rightDataSource = new MatTableDataSource<AssignableNode>();
    this.rightDataSource.sort = this.rightSort;
    this.rightDataSource.paginator = this.rightPaginator;
    this.rightDataSource.filterPredicate = this.customFilterPredicate;

    this.isUpdating = true;

    this.route.paramMap.pipe(
      switchMap((params: ParamMap) => {
        const moduleSetReleaseId = Number(params.get('moduleSetReleaseId'));
        return this.moduleService.getModuleSetRelease(moduleSetReleaseId);
      }))
      .subscribe(moduleSetRelease => {
        this.init(moduleSetRelease);

        forkJoin([
          this.moduleService.getModules(this.moduleSetRelease.moduleSetId),
          this.moduleService.getAssignable(this.moduleSetRelease.moduleSetReleaseId)
        ]).subscribe(([modules, assignable]) => {
          this.rootElement = modules as ModuleElement;
          this.tiles.push({elements: this.rootElement.child, current: undefined});
          if (this.rootElement.child && this.rootElement.child.length > 0) {
            this.onClickElement(this.tiles[0], this.tiles[0].elements[0]);
          }
          this.leftDataSource.data = this.assignableMapToList(assignable);
          this.isUpdating = false;
        });
      });
  }

  ngAfterViewInit() {
    this.leftDataSource.sort = this.leftSort;
    this.rightDataSource.sort = this.rightSort;
  }

  init(moduleSetRelease: ModuleSetRelease) {
    this.moduleSetRelease = moduleSetRelease;
  }

  get userToken(): UserToken {
    return this.auth.getUserToken();
  }

  get roles(): string[] {
    const userToken = this.userToken;
    return (userToken) ? userToken.roles : [];
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected(selection: SelectionModel<AssignableNode>, dataSource: MatTableDataSource<AssignableNode>) {
    const numSelected = selection.selected.length;
    const numRows = dataSource.filteredData.length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle(selection: SelectionModel<AssignableNode>, dataSource: MatTableDataSource<AssignableNode>) {
    this.isAllSelected(selection, dataSource) ?
      selection.clear() :
      dataSource.filteredData.forEach(row => this.select(row, selection));
  }

  select(row: AssignableNode, selection: SelectionModel<AssignableNode>) {
    selection.select(row);
  }

  toggle(row: AssignableNode, selection: SelectionModel<AssignableNode>) {
    if (this.isSelected(row, selection)) {
      selection.deselect(row);
    } else {
      this.select(row, selection);
    }
  }

  isSelected(row: AssignableNode, selection: SelectionModel<AssignableNode>) {
    return selection.isSelected(row);
  }

  onClickElement(tile: Tile, element: ModuleElement) {
    tile.current = element;
    if (!element.directory) {
      this.selectedModuleElement = element;
    }
    const tileIndex = this.tiles.indexOf(tile) + 1;
    if (this.tiles.length > tileIndex) {
      this.tiles.splice(tileIndex, this.tiles.length - tileIndex);
    }
    if (element.directory) {
      this.tiles.push({elements: element.child.sort(this._moduleSort), current: undefined});
    } else {
      this.isUpdating = true;
      this.moduleService.getAssigned(this.moduleSetRelease.moduleSetReleaseId, element.moduleId)
        .pipe(finalize(() => {
          this.isUpdating = false;
        })).subscribe(resp => {
        this.rightDataSource.data = this.assignedMapToList(resp);
      });
    }
  }

  _moduleSort(e1: ModuleElement, e2: ModuleElement): number {
    // @ts-ignore
    return e2.directory - e1.directory ? e2.directory - e1.directory : e1.name > e2.name ? 1 : -1;
  }

  assignableMapToList(map: AssignableMap): AssignableNode[] {
    const list = [];
    list.push(...this.mapToList(map.assignableXbtManifestMap));
    list.push(...this.mapToList(map.assignableAgencyIdListManifestMap));
    list.push(...this.mapToList(map.assignableCodeListManifestMap));
    list.push(...this.mapToList(map.assignableDtManifestMap));
    list.push(...this.mapToList(map.assignableBccpManifestMap));
    list.push(...this.mapToList(map.assignableAsccpManifestMap));
    list.push(...this.mapToList(map.assignableAccManifestMap));

    list.sort(this._sort);
    return list;
  }

  assignedMapToList(map: AssignableMap): AssignableNode[] {
    const list = [];
    list.push(...this.mapToList(map.assignedAccManifestMap));
    list.push(...this.mapToList(map.assignedAsccpManifestMap));
    list.push(...this.mapToList(map.assignedBccpManifestMap));
    list.push(...this.mapToList(map.assignedDtManifestMap));
    list.push(...this.mapToList(map.assignedCodeListManifestMap));
    list.push(...this.mapToList(map.assignedAgencyIdListManifestMap));
    list.push(...this.mapToList(map.assignedXbtManifestMap));

    list.sort(this._sort);
    return list;
  }

  mapToList(map: Map<number, AssignableNode>): AssignableNode[] {
    return Array.from(Object.keys(map)).map(key => {
      const node = map[key] as AssignableNode;
      node.visible = true;
      return node;
    });
  }

  _sort(a: AssignableNode, b: AssignableNode): number {
    const sortStateOrder = ['Candidate', 'Draft', 'WIP', 'Deleted'];
    const sortTypeOrder = ['ACC', 'ASCCP', 'BCCP', 'DT', 'CODE_LIST', 'AGENCY_ID_LIST', 'XBT'];
    if (sortStateOrder.indexOf(a.state) > sortStateOrder.indexOf(b.state)) {
      return 1;
    } else if (sortStateOrder.indexOf(a.state) < sortStateOrder.indexOf(b.state)) {
      return -1;
    } else {
      if (sortTypeOrder.indexOf(a.type) > sortTypeOrder.indexOf(b.type)) {
        return 1;
      } else {
        return -1;
      }
    }
  }

  sortList(list: AssignableNode[]) {
    list.sort(this._sort);
  }

  updateLeftFilterTypes(value) {
    this.leftFilteredValues.types = value;
    this.leftDataSource.filter = JSON.stringify(this.leftFilteredValues);
  }

  updateLeftFilterStates(value) {
    this.leftFilteredValues.states = value;
    this.leftDataSource.filter = JSON.stringify(this.leftFilteredValues);
  }

  updateLeftFilterDen(value) {
    this.leftFilteredValues.den = value;
    this.leftDataSource.filter = JSON.stringify(this.leftFilteredValues);
  }

  updateRightFilterTypes(value) {
    this.rightFilteredValues.types = value;
    this.rightDataSource.filter = JSON.stringify(this.rightFilteredValues);
  }

  updateRightFilterStates(value) {
    this.rightFilteredValues.states = value;
    this.rightDataSource.filter = JSON.stringify(this.rightFilteredValues);
  }

  updateRightFilterDen(value) {
    this.rightFilteredValues.den = value;
    this.rightDataSource.filter = JSON.stringify(this.rightFilteredValues);
  }

  public customFilterPredicate = (data: AssignableNode, filter: string): boolean => {
    let denFiltered = true;
    let typeFiltered = true;
    let stateFiltered = true;
    const filterValue = JSON.parse(filter);

    if (filterValue.den.length > 0) {
      denFiltered = data.den.toLowerCase().indexOf(filterValue.den.trim().toLowerCase()) !== -1;
    }

    if (filterValue.types.length > 0) {
      typeFiltered = filterValue.types.indexOf(data.type) !== -1;
    }

    if (filterValue.states.length > 0) {
      stateFiltered = filterValue.states.indexOf(data.state) !== -1;
    }

    return denFiltered && typeFiltered && stateFiltered;
  }

  assign() {
    if (this.leftSelection.selected.length === 0 || this.selectedModuleElement === undefined) {
      return;
    }
    const items = this.leftSelection.selected;
    const leftItems = this.leftDataSource.data;
    const rightItems = this.rightDataSource.data;

    this.isUpdating = true;
    this.moduleService.createAssign(this.moduleSetRelease, this.selectedModuleElement.moduleId, items)
      .pipe(finalize(() => {
        this.isUpdating = false;
      })).subscribe(_ => {
      this.snackBar.open('Assigned', '', {
        duration: 3000,
      });

      rightItems.push(...items);

      items.forEach(e => {
        const index = leftItems.indexOf(e);
        leftItems.splice(index, 1);
      });

      this.rightDataSource.data = [];
      this.rightDataSource.data = rightItems;
      this.leftDataSource.data = [];
      this.leftDataSource.data = leftItems;

      this.leftSelection.clear();
    });
  }

  unassign() {
    if (this.rightSelection.selected.length === 0) {
      return;
    }
    const items = this.rightSelection.selected;
    const leftItems = this.leftDataSource.data;
    const rightItems = this.rightDataSource.data;

    this.isUpdating = true;
    this.moduleService.discardAssign(this.moduleSetRelease, this.selectedModuleElement.moduleId, items)
      .pipe(finalize(() => {
        this.isUpdating = false;
      })).subscribe(_ => {
      this.snackBar.open('Unassigned', '', {
        duration: 3000,
      });

      leftItems.push(...items);

      items.forEach(e => {
        const index = rightItems.indexOf(e);
        rightItems.splice(index, 1);
      });

      this.leftDataSource.data = [];
      this.leftDataSource.data = leftItems;
      this.rightDataSource.data = [];
      this.rightDataSource.data = rightItems;

      this.rightSelection.clear();
    });
  }

}
