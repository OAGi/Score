import {ChangeDetectionStrategy, Component, inject, signal, viewChild} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {SelectionModel} from '@angular/cdk/collections';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {forkJoin} from 'rxjs';
import {finalize} from 'rxjs/operators';
import {BusinessTermService} from '../../../business-term-management/domain/business-term.service';
import {BieToAssign, BusinessTermListEntry} from '../../../business-term-management/domain/business-term';
import {BUSINESS_TERM_TABLE} from '../../../business-term-management/domain/business-term.table';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../../common/score-table';

export interface BieBusinessTermAssignDialogData {
  // The BIE node the term is assigned to. bieType is 'ASBIE' or 'BBIE'; bieId is the asbieId/bbieId.
  bieId: number;
  bieType: string;
  den: string;
}

const BIE_BUSINESS_TERM_ASSIGN_TABLE = defineTable({
  ...BUSINESS_TERM_TABLE,
  defaultSort: [{active: 'businessTerm', direction: 'asc'}],
});

/**
 * Issue #1754: in-place Business Term assignment from the BIE editor. This is the "pick a term" step lifted from
 * the standalone AssignBusinessTermBtComponent wizard so the user never leaves the editor. The node context
 * arrives via MAT_DIALOG_DATA (no bieIds/bieTypes query-param plumbing and no confirmAsbieBbieListByIdAndType
 * round-trip — the node id is already known). It performs the assign itself (keeping the same uniqueness and
 * uniqueness check as the wizard and closes with {changed:true} so the caller refreshes only that node's
 * Business Term panel — never reloading the whole tree.
 */
@Component({
  standalone: false,
  selector: 'score-bie-business-term-assign-dialog',
  templateUrl: './bie-business-term-assign-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./bie-business-term-assign-dialog.component.css']
})
export class BieBusinessTermAssignDialogComponent {
  private businessTermService = inject(BusinessTermService);
  private confirmDialogService = inject(ConfirmDialogService);
  private snackBar = inject(MatSnackBar);
  dialogRef = inject(MatDialogRef<BieBusinessTermAssignDialogComponent>);
  data: BieBusinessTermAssignDialogData = inject(MAT_DIALOG_DATA);

  readonly columns = createTableColumns(BIE_BUSINESS_TERM_ASSIGN_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<BusinessTermListEntry>>(ScoreTableViewComponent);
  // Multi-select: assign every selected businessTermId to the current BIE.
  selection = new SelectionModel<number>(true, []);

  /** While the terms are being assigned. */
  readonly busy = signal(false);
  biesToAssign: BieToAssign[] = this.biesToAssignOf();
  typeCode = '';
  private readonly primaryIndicator = false;

  readonly load = (params: HttpParams) => this.businessTermService.getBusinessTermListPage(params);
  readonly rowKey = (row: BusinessTermListEntry) => row.businessTermId;

  private biesToAssignOf(): BieToAssign[] {
    const bie = new BieToAssign();
    bie.bieId = this.data.bieId;
    bie.bieType = this.data.bieType;
    return [bie];
  }

  isCreateDisabled(): boolean {
    return this.busy() || this.view().loading() || this.selection.selected.length === 0;
  }

  create(): void {
    if (this.isCreateDisabled()) {
      return;
    }
    const businessTermIds = [...this.selection.selected];
    this.busy.set(true);
    this.checkUniqueness(
      businessTermIds,
      () => this.doCreate(businessTermIds),
      () => this.busy.set(false)
    );
  }

  private checkUniqueness(businessTermIds: number[], callbackFn: () => void, doneFn: () => void): void {
    forkJoin(businessTermIds.map(businessTermId =>
      this.businessTermService.checkAssignmentUniqueness(this.data.bieId, this.data.bieType,
        businessTermId, this.typeCode, this.primaryIndicator)
    )).subscribe({
      next: isUniques => {
        if (isUniques.some(isUnique => !isUnique)) {
          doneFn();
          const dialogConfig = this.confirmDialogService.newConfig();
          dialogConfig.data.header = 'Invalid parameters';
          dialogConfig.data.content = [
            'One or more selected business term assignments for the same BIE and type code already exist!'
          ];
          this.confirmDialogService.open(dialogConfig);
          return;
        }
        callbackFn();
      },
      error: () => {
        doneFn();
        this.snackBar.open('Failed to validate the selected business terms.', '', {duration: 5000});
      }
    });
  }

  private doCreate(businessTermIds: number[]): void {
    forkJoin(businessTermIds.map(businessTermId =>
      this.businessTermService.assignBusinessTermToBie(businessTermId, this.biesToAssign,
        this.primaryIndicator, this.typeCode)
    )).pipe(
      finalize(() => this.busy.set(false))
    ).subscribe({
      next: () => {
        this.snackBar.open(
          businessTermIds.length === 1 ? 'Business term assigned.' : 'Business terms assigned.',
          '', {duration: 3000});
        this.dialogRef.close({changed: true});
      },
      error: (err) => {
        const message = (err && err.error && err.error.message)
          ? err.error.message : 'Failed to assign the selected business terms.';
        this.snackBar.open(message, '', {duration: 5000});
      }
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
