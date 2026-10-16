import {afterNextRender, ChangeDetectionStrategy, Component, inject, signal, viewChild} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {SelectionModel} from '@angular/cdk/collections';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {CcListEntry} from '../../cc-list/domain/cc-list';
import {CcListService} from '../../cc-list/domain/cc-list.service';
import {CcListComponent} from '../cc-list.component';
import {WebPageInfoService} from '../../../basis/basis.service';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../../common/score-table';
import {CC_DIALOG_FILTERS as F} from '../../domain/cc-dialog.table';

const BOD_COLUMNS = [
  {id: 'state', label: 'State', width: 104, sortable: true},
  {id: 'den', label: 'DEN', width: 0, sortable: true},
  {id: 'lastUpdateTimestamp', label: 'Updated On', header: 'Updated on', width: 160, sortable: true},
] as const;

const BOD_FILTERS = [F.states, F.definition, {...F.module, label: 'Module'}, F.updater, F.updatedDate];

const VERB_TABLE = defineTable({
  storageKey: 'TableColumns-CoreComponentForVerbBodPage',
  columns: BOD_COLUMNS,
  // The verbs are not sorted, to begin with.
  defaultSort: [],
  search: {key: 'den', placeholder: 'Search by DEN'},
  filters: BOD_FILTERS,
});

const NOUN_TABLE = defineTable({
  storageKey: 'TableColumns-CoreComponentForNounBodPage',
  columns: BOD_COLUMNS,
  defaultSort: {active: 'lastUpdateTimestamp', direction: 'desc'},
  search: {key: 'den', placeholder: 'Search by DEN'},
  filters: BOD_FILTERS,
});

/** What was chosen stays chosen while the list is searched or paged: the rows are the same ones by their manifest id. */
const newSelection = () => new SelectionModel<CcListEntry>(true, [], true, (a, b) => a.manifestId === b.manifestId);

@Component({
  standalone: false,
  selector: 'score-create-asccp-dialog',
  templateUrl: './create-bod-dialog.component.html',
  styleUrls: ['./create-bod-dialog.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager
})
export class CreateBodDialogComponent {
  dialogRef = inject<MatDialogRef<CcListComponent>>(MatDialogRef);
  private ccListService = inject(CcListService);
  webPageInfo = inject(WebPageInfoService);
  private confirmDialogService = inject(ConfirmDialogService);
  data = inject(MAT_DIALOG_DATA);

  readonly verbColumns = createTableColumns(VERB_TABLE);
  readonly nounColumns = createTableColumns(NOUN_TABLE);
  readonly verbView = viewChild.required<ScoreTableViewComponent<CcListEntry>>('verbView');
  readonly nounView = viewChild.required<ScoreTableViewComponent<CcListEntry>>('nounView');
  verbSelection = newSelection();
  nounSelection = newSelection();
  action: string;

  readonly ready = signal(false);

  /** The ASCCPs of the library and the release; the tag says whether they are verbs or nouns. */
  extraParams: Record<string, string> = {};

  readonly verbLoad = (params: HttpParams) => this.ccListService.getCcListPage(params.set('tags', 'Verb'));
  readonly nounLoad = (params: HttpParams) => this.ccListService.getCcListPage(params.set('tags', 'Noun'));
  readonly rowKey = (row: CcListEntry) => row;

  constructor() {
    this.action = this.data.action;
    this.extraParams = {
      libraryId: '' + this.data.libraryId,
      releaseId: '' + (this.data.releaseId || 0),
      types: 'asccp'
    };
    afterNextRender(() => this.ready.set(true));
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  onClick(): void {
    if (this.nounSelection.selected.length === 0 || this.verbSelection.selected.length === 0) {
      return;
    }

    const numOfDeprecatedComponents =
      this.nounSelection.selected.filter(e => e.deprecated).length +
      this.verbSelection.selected.filter(e => e.deprecated).length;

    const numOfDeletedComponents =
      this.nounSelection.selected.filter(e => e.state === 'Deleted').length +
      this.verbSelection.selected.filter(e => e.state === 'Deleted').length;

    if (numOfDeprecatedComponents > 0 || numOfDeletedComponents > 0) {
      const dialogConfig = this.confirmDialogService.newConfig();
      dialogConfig.data.header = 'Confirmation required';
      dialogConfig.data.content = [
        (numOfDeprecatedComponents > 0) ?
          'There ' + ((numOfDeprecatedComponents === 1) ? 'is a deprecate component' : 'are deprecate components') + ' in the selected list.' :
          'There ' + ((numOfDeletedComponents === 1) ? 'is a deleted component' : 'are deleted components') + ' in the selected list.',
        'Are you sure you want to proceed with it?'];
      dialogConfig.data.action = 'Proceed anyway';

      this.confirmDialogService.open(dialogConfig).beforeClosed()
        .subscribe(result => {
          if (result) {
            this.dialogRef.close({
              verbManifestIdList: this.verbSelection.selected.map(e => e.manifestId),
              nounManifestIdList: this.nounSelection.selected.map(e => e.manifestId)
            });
          }
        });
    } else {
      this.dialogRef.close({
        verbManifestIdList: this.verbSelection.selected.map(e => e.manifestId),
        nounManifestIdList: this.nounSelection.selected.map(e => e.manifestId)
      });
    }
  }

  get totalNumberOfBODs(): number {
    const numOfNounSelection = this.nounSelection.selected.length;
    const numOfVerbSelection = this.verbSelection.selected.length;
    return numOfNounSelection * numOfVerbSelection;
  }

  get BODName(): string {
    const numOfNounSelection = this.nounSelection.selected.length;
    const numOfVerbSelection = this.verbSelection.selected.length;
    if (numOfNounSelection === 0 || numOfVerbSelection === 0) {
      return '';
    }
    return this.verbSelection.selected[0].name + ' ' + this.nounSelection.selected[0].name;
  }

}
