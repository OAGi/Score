import {ChangeDetectionStrategy, Component, inject, OnInit, viewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {SelectionModel} from '@angular/cdk/collections';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {CcListEntry} from '../cc-list/domain/cc-list';
import {ExtensionDetailComponent} from '../extension-detail/extension-detail.component';
import {RefactorDialogService} from './domain/refactor-dialog.service';
import {ActivatedRoute, Router} from '@angular/router';
import {WebPageInfoService} from '../../basis/basis.service';
import {CC_WITH_TYPE_TABLE} from '../domain/cc-dialog.table';
import {createTableColumns, defineTable, inMemoryLoad, ScoreTableViewComponent} from '../../common/score-table';
import {IssuedCc} from './domain/refactor-dialog';

/** The ACCs to move to: the column settings of the core component list; there is no search and no order of its own. */
const REFACTOR_ACC_TABLE = defineTable({
  ...CC_WITH_TYPE_TABLE,
  defaultSort: undefined,
  search: undefined,
});

/** The result of the analysis: the ACCs that the move touches, and what stops it. */
const REFACTOR_ISSUE_TABLE = defineTable({
  storageKey: 'TableColumns-CoreComponentAccRefactorPage',
  columns: [
    {id: 'type', label: 'Type', width: 80, sortable: true},
    {id: 'state', label: 'State', width: 104, sortable: true},
    {id: 'den', label: 'DEN', width: 0, sortable: true},
    {id: 'reason', label: 'Issue', width: '30%', sortable: true},
    {id: 'revision', label: 'Revision', width: 80, sortable: true},
    {id: 'owner', label: 'Owner', width: 140, sortable: true},
    {id: 'lastUpdateTimestamp', label: 'Updated On', header: 'Updated on', width: 160, sortable: true},
  ],
});

@Component({
  standalone: false,
  selector: 'score-based-acc-dialog',
  templateUrl: './refactor-dialog.component.html',
  styleUrls: ['./refactor-dialog.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager
})
export class RefactorDialogComponent implements OnInit {
  dialogRef = inject<MatDialogRef<ExtensionDetailComponent>>(MatDialogRef);
  private service = inject(RefactorDialogService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  webPageInfo = inject(WebPageInfoService);
  data = inject(MAT_DIALOG_DATA);
  private confirmDialogService = inject(ConfirmDialogService);


  // ACC Table
  readonly columns = createTableColumns(REFACTOR_ACC_TABLE);
  readonly baseView = viewChild.required<ScoreTableViewComponent<CcListEntry>>('baseView');
  /** The ACCs that the component can be moved to. */
  rows: CcListEntry[] = [];
  readonly load = inMemoryLoad<CcListEntry>(() => this.rows, {
    revision: row => row.log?.revisionNum,
    owner: row => row.owner?.loginId,
    lastUpdateTimestamp: row => row.lastUpdated?.when
  });

  // Issue Table
  readonly issueColumns = createTableColumns(REFACTOR_ISSUE_TABLE);
  readonly issueView = viewChild.required<ScoreTableViewComponent<IssuedCc>>('issueView');
  issueRows: IssuedCc[] = [];
  readonly issueLoad = inMemoryLoad<IssuedCc>(() => this.issueRows, {
    reason: row => (row.reasons || []).join(' ')
  });

  selection = new SelectionModel<CcListEntry>(false, []);
  readonly rowKey = (row: CcListEntry) => row;

  loading = false;
  isValid = false;

  action: string;
  title: string;

  ngOnInit() {
    this.loading = true;

    this.title = this.data.title;

    // Choosing another ACC means the analysis of the previous choice no longer applies.
    this.selection.changed.subscribe(() => {
      this.isValid = false;
    });

    this.service.baseAccList(this.data.accManifestId).subscribe(ccList => {
      this.rows = ccList;
      this.loading = false;
      this.baseView().reload();
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
          this.issueRows = resp.issueList;
          valid = resp.issueList.filter(e => e.reasons.length > 0).length === 0;
        } else {
          this.issueRows = [];
        }
        this.issueView().reload();
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

  getRouterLink(ccList: CcListEntry) {
    if (ccList.oagisComponentType === 'UserExtensionGroup') {
      return 'core_component/extension/' + ccList.manifestId;
    } else {
      return 'core_component/acc/' + ccList.manifestId;
    }
  }
}
