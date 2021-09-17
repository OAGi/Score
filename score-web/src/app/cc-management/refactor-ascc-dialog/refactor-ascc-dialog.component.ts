import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {MatDatepickerInputEvent} from '@angular/material/datepicker';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {PageRequest} from '../../basis/basis';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {initFilter} from '../../common/utility';
import {WorkingRelease} from '../../release-management/domain/release';
import {CcList, CcListRequest} from '../cc-list/domain/cc-list';
import {CcListService} from '../cc-list/domain/cc-list.service';
import {Base, OagisComponentType, Semantics} from '../domain/core-component-node';
import {ExtensionDetailComponent} from '../extension-detail/extension-detail.component';
import {RefactorAsccDialogService} from './domain/refactor-ascc-dialog.service';

@Component({
  selector: 'score-based-acc-dialog',
  templateUrl: './refactor-ascc-dialog.component.html',
  styleUrls: ['./refactor-ascc-dialog.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0', display: 'none'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ]
})
export class RefactorAsccDialogComponent implements OnInit {

  displayedColumns: string[] = [
    'select', 'type', 'state', 'den', 'revision', 'owner', 'module', 'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<CcList>();
  expandedElement: CcList | null;
  selection = new SelectionModel<CcList>(false, []);

  loading = false;

  request: CcListRequest;
  action: string;
  title: string;

  constructor(public dialogRef: MatDialogRef<ExtensionDetailComponent>,
              private service: RefactorAsccDialogService,
              private accountService: AccountListService,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private confirmDialogService: ConfirmDialogService) {
  }

  ngOnInit() {
    this.loading = true;

    this.title = this.data.title;

    this.service.baseAccList(this.data.accManifestId).subscribe(ccList => {
      this.dataSource.data = ccList;
      this.loading = false;
    });

  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  onClick(): void {
    if (this.selection.selected.length === 0) {
      return;
    }
    if (this.selection.selected[0].deprecated) {
      const dialogConfig = this.confirmDialogService.newConfig();
      dialogConfig.data.header = 'Confirmation required';
      dialogConfig.data.content = ['The selected component is deprecated', 'Are you sure you want to proceed with it?'];
      dialogConfig.data.action = 'Proceed anyway';

      this.confirmDialogService.open(dialogConfig).beforeClosed()
        .subscribe(result => {
          if (result) {
            this.dialogRef.close(this.selection.selected[0].manifestId);
          }
        });
    } else {
      this.dialogRef.close(this.selection.selected[0].manifestId);
    }
  }

  select(row: CcList) {
    this.selection.select(row);
  }

  toggle(row: CcList) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  isSelected(row: CcList) {
    return this.selection.isSelected(row);
  }

  getRouterLink(ccList: CcList) {
    switch (ccList.type.toUpperCase()) {
      case 'ACC':
        if (ccList.oagisComponentType === 'UserExtensionGroup') {
          return 'core_component/extension/' + ccList.manifestId;
        } else {
          return 'core_component/acc/' + ccList.manifestId;
        }
      default:
        return window.location.pathname;
    }
  }
}
