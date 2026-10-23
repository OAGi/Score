import {afterNextRender, ChangeDetectionStrategy, Component, inject, OnInit, signal, viewChild} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {SelectionModel} from '@angular/cdk/collections';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {CcListEntry} from '../../cc-list/domain/cc-list';
import {CcListService} from '../../cc-list/domain/cc-list.service';
import {WebPageInfoService} from '../../../basis/basis.service';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../../common/score-table';
import {CC_DIALOG_FILTERS as F, DT_WITHOUT_TYPE_TABLE} from '../../domain/cc-dialog.table';
import {CcListComponent} from '../cc-list.component';
import {TagService} from '../../../tag-management/domain/tag.service';

const CREATE_BCCP_TABLE = defineTable({
  ...DT_WITHOUT_TYPE_TABLE,
  filters: [F.states, F.definition, F.module, F.deprecated, F.commonlyUsed, F.tags, F.owner, F.updater, F.updatedDate],
});

@Component({
  standalone: false,
  selector: 'score-create-bccp-dialog',
  templateUrl: './create-bccp-dialog.component.html',
  styleUrls: ['./create-bccp-dialog.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager
})
export class CreateBccpDialogComponent implements OnInit {
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
  private tagService = inject(TagService);
  readonly columns = createTableColumns(CREATE_BCCP_TABLE);
  action: string;

  ngOnInit() {
    this.action = this.data.action;
    const params: Record<string, string> = {
      libraryId: '' + this.data.libraryId,
      releaseId: '' + (this.data.releaseId || 0),
      types: 'dt',
      dtTypes: 'BDT'
    };
    if (this.data.excludes?.length > 0) {
      params['excludes'] = this.data.excludes.join(',');
    }
    this.extraParams.set(params);

    this.tagService.getTags().subscribe(tags => {
      this.filterOptions.set({tags: tags.map(e => ({value: e.name, label: e.name}))});
      // Only the commonly used ones, to begin with.
      this.view().setFilter('commonlyUsed', ['true']);
      this.ready.set(true);
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
}
