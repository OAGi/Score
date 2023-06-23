import {Component, Inject, OnInit} from '@angular/core';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {CcList, CcListRequest} from '../cc-list/domain/cc-list';
import {ExtensionDetailComponent} from '../extension-detail/extension-detail.component';
import {RefactorDialogService} from './domain/refactor-dialog.service';
import {ActivatedRoute, Router} from '@angular/router';

@Component({
  selector: 'score-based-acc-dialog',
  templateUrl: './refactor-dialog.component.html',
  styleUrls: ['./refactor-dialog.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0', display: 'none'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ]
})
export class RefactorDialogComponent implements OnInit {

  displayedColumns: string[] = [
    'select', 'type', 'state', 'den', 'revision', 'owner', 'lastUpdateTimestamp'
  ];

  issueDisplayedColumns: string[] = [
    'type', 'state', 'den', 'reason', 'revision', 'owner', 'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<CcList>();
  expandedElement: CcList | null;
  selection = new SelectionModel<CcList>(false, []);

  loading = false;
  isValid = false;

  issueDataSource = new MatTableDataSource<CcList>();

  request: CcListRequest;
  action: string;
  title: string;

  constructor(public dialogRef: MatDialogRef<ExtensionDetailComponent>,
              private service: RefactorDialogService,
              private accountService: AccountListService,
              private router: Router,
              private route: ActivatedRoute,
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

  checkValidation(): void {
    if (this.selection.selected.length === 0) {
      return;
    }
    const destinationManifestId = this.selection.selected[0].manifestId;

    this.service.validateRefactoring(this.data.type, this.data.targetManifestId, destinationManifestId)
      .subscribe(resp => {
        let valid = true;
        if (resp.issueList && resp.issueList.length > 0) {
          this.issueDataSource.data = resp.issueList;
          valid = resp.issueList.filter(e => e.reasons.length > 0).length === 0;
        } else {
          this.issueDataSource.data = [];
        }
        this.isValid = valid;
      });
  }

  doRefactor(): void {
    if (!this.isValid) {
      return;
    }
    if (this.selection.selected.length === 0) {
      return;
    }
    const destinationManifestId = this.selection.selected[0].manifestId;
    if (this.selection.selected[0].deprecated) {
      const dialogConfig = this.confirmDialogService.newConfig();
      dialogConfig.data.header = 'Confirmation required';
      dialogConfig.data.content = ['The selected component is deprecated', 'Are you sure you want to proceed with it?'];
      dialogConfig.data.action = 'Proceed anyway';

      this.confirmDialogService.open(dialogConfig).beforeClosed()
        .subscribe(result => {
          if (result) {
            this.service.refactor(this.data.type, this.data.targetManifestId, destinationManifestId)
              .subscribe(resp => {
                this.dialogRef.close(resp);
              });
          }
        });
    } else {
      this.service.refactor(this.data.type, this.data.targetManifestId, destinationManifestId)
        .subscribe(resp => {
          this.dialogRef.close(resp);
        });
    }
  }

  select(row: CcList) {
    this.selection.select(row);
    this.isValid = false;
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
    if (ccList.oagisComponentType === 'UserExtensionGroup') {
      return 'core_component/extension/' + ccList.manifestId;
    } else {
      return 'core_component/acc/' + ccList.manifestId;
    }
  }
}
