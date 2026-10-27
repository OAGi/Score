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
import {ValidationService} from '../../validation/validation.service';
import {Observable} from 'rxjs';
import {finalize} from 'rxjs/operators';
import {CompactRecord, memoizeRow} from '../../common/record-compact-row';
import {SeparatePipe} from '../../common/utility';

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
  private validationService = inject(ValidationService);


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
  /** What the move would break is being asked. */
  checking = false;
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

  /**
   * Moves the association, after asking what the move would break: when it would break nothing the move goes on as it
   * always did; otherwise the dialog closes and the page shows what it would break and asks whether to go on.
   */
  doRefactor(): void {
    if (!this.isValid || this.checking) {
      return;
    }
    if (this.selection.selected.length === 0) {
      return;
    }
    const destination = this.selection.selected[0];
    const destinationManifestId = destination.manifestId;
    const type = this.data.type;

    this.checking = true;
    this.validationService.refactorCheck(type, this.data.targetManifestId, destinationManifestId)
      .pipe(finalize(() => this.checking = false))
      .subscribe({
        next: change => {
          if (change.report.violations.length === 0) {
            this.refactorAfterConfirmation(destination).subscribe(resp => this.dialogRef.close(resp));
          } else {
            this.dialogRef.close({check: change, refactor: () => this.refactorAfterConfirmation(destination)});
          }
        },
        // not being able to ask is no reason to take the move away
        error: () => this.refactorAfterConfirmation(destination).subscribe(resp => this.dialogRef.close(resp))
      });
  }

  /** A component that is deprecated is moved only on the user's word; nothing is emitted when the word is no. */
  private refactorAfterConfirmation(destination: CcListEntry): Observable<any> {
    const move = () => this.service.refactor(this.data.type, this.data.targetManifestId, destination.manifestId);
    if (!destination.deprecated) {
      return move();
    }
    return new Observable<any>(subscriber => {
      const dialogConfig = this.confirmDialogService.newConfig();
      dialogConfig.data.header = 'Confirmation required';
      dialogConfig.data.content = ['The selected component is deprecated', 'Are you sure you want to proceed with it?'];
      dialogConfig.data.action = 'Proceed anyway';

      this.confirmDialogService.open(dialogConfig).beforeClosed().subscribe(result => {
        if (result) {
          move().subscribe(subscriber);
        } else {
          subscriber.complete();
        }
      });
    });
  }

  /** What the compact row shows of an issue (a component that cannot be refactored yet): made once for a row. The reasons go under it. */
  readonly issueRecord = memoizeRow((issue: IssuedCc): CompactRecord => ({
    badges: [
      {text: 'ACC'},
      {text: new SeparatePipe().transform(issue.state), kind: 'state', cssClass: 'cc-state', state: issue.state},
      ...(issue.deprecated ? [{text: 'Deprecated', kind: 'state' as const, cssClass: 'cc-state', state: 'Deprecated'}] : [])
    ],
    title: issue.den,
    link: '/core_component/' + (issue.oagisComponentType === 'UserExtensionGroup' ? 'extension/' : 'acc/') + issue.manifestId,
    subtitle: issue.guid,
    meta: [
      {label: 'Rev.', value: '' + issue.revision},
      {label: 'Owner', value: issue.owner, strong: true}
    ],
    updated: issue.lastUpdateTimestamp ? {when: issue.lastUpdateTimestamp, by: issue.lastUpdateUser} : undefined
  }));

  getRouterLink(ccList: CcListEntry) {
    if (ccList.oagisComponentType === 'UserExtensionGroup') {
      return 'core_component/extension/' + ccList.manifestId;
    } else {
      return 'core_component/acc/' + ccList.manifestId;
    }
  }
}
