import {afterNextRender, ChangeDetectionStrategy, Component, inject, OnInit, signal, viewChild} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {SelectionModel} from '@angular/cdk/collections';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {CcListEntry} from '../../cc-list/domain/cc-list';
import {CcListService} from '../../cc-list/domain/cc-list.service';
import {WebPageInfoService} from '../../../basis/basis.service';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../../common/score-table';
import {CC_DIALOG_FILTERS as F, CC_WITHOUT_TYPE_TABLE} from '../../domain/cc-dialog.table';
import {CcListComponent} from '../cc-list.component';
import {Verb} from '../../domain/core-component-node';

const CREATE_VERB_TABLE = defineTable({
  ...CC_WITHOUT_TYPE_TABLE,
  filters: [F.states, F.definition, F.module, F.deprecated, F.owner, F.updater, F.updatedDate],
});

@Component({
  standalone: false,
  selector: 'score-create-verb-dialog',
  templateUrl: './create-verb-dialog.component.html',
  styleUrls: ['./create-verb-dialog.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager
})
export class CreateVerbDialogComponent implements OnInit {
  dialogRef = inject<MatDialogRef<CcListComponent>>(MatDialogRef);
  private ccListService = inject(CcListService);
  webPageInfo = inject(WebPageInfoService);
  private confirmDialogService = inject(ConfirmDialogService);
  data = inject(MAT_DIALOG_DATA);

  readonly view = viewChild.required<ScoreTableViewComponent<CcListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<CcListEntry>(false, []);

  readonly ready = signal(false);
  readonly extraParams = signal<Record<string, string>>({});
  readonly filterOptions = signal<Record<string, { value: string, label: string }[]>>({});

  readonly load = (params: HttpParams) => this.ccListService.getCcListPage(params);
  readonly columns = createTableColumns(CREATE_VERB_TABLE);
  action: string;

  ngOnInit() {
    this.action = this.data.action;
    const params: Record<string, string> = {
      libraryId: '' + this.data.libraryId,
      releaseId: '' + (this.data.releaseId || 0),
      types: 'acc',
      componentTypes: Verb.value
    };
    if (this.data.excludes?.length > 0) {
      params['excludes'] = this.data.excludes.join(',');
    }
    this.extraParams.set(params);
    this.ready.set(true);
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
            this.dialogRef.close({
              manifestId: this.selection.selected[0].manifestId,
              objectClassTerm: this.selection.selected[0].name
            });
          }
        });
    } else {
      this.dialogRef.close({
        manifestId: this.selection.selected[0].manifestId,
        objectClassTerm: this.selection.selected[0].name
      });
    }
  }
}
