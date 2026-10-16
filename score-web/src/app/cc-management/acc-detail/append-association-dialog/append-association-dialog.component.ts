import {afterNextRender, ChangeDetectionStrategy, Component, inject, OnInit, signal, viewChild} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {SelectionModel} from '@angular/cdk/collections';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {CcListEntry} from '../../cc-list/domain/cc-list';
import {CcListService} from '../../cc-list/domain/cc-list.service';
import {WebPageInfoService} from '../../../basis/basis.service';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../../common/score-table';
import {CC_DIALOG_FILTERS as F, CC_WITH_TYPE_TABLE} from '../../domain/cc-dialog.table';



const APPEND_ASSOCIATION_TABLE = defineTable({
  ...CC_WITH_TYPE_TABLE,
  filters: [{type: 'select', key: 'types', label: 'Type'}, F.states, F.definition, F.module, F.deprecated, F.owner, F.updater, F.updatedDate],
});

@Component({
  standalone: false,
  selector: 'score-append-association-dialog',
  templateUrl: './append-association-dialog.component.html',
  styleUrls: ['./append-association-dialog.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager
})
export class AppendAssociationDialogComponent implements OnInit {
  dialogRef = inject<MatDialogRef<AppendAssociationDialogComponent>>(MatDialogRef);
  private ccListService = inject(CcListService);
  webPageInfo = inject(WebPageInfoService);
  private confirmDialogService = inject(ConfirmDialogService);
  data = inject(MAT_DIALOG_DATA);

  readonly view = viewChild.required<ScoreTableViewComponent<CcListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<CcListEntry>(false, []);

  readonly ready = signal(false);
  readonly extraParams = signal<Record<string, string>>({});
  readonly filterOptions = signal<Record<string, { value: string, label: string }[]>>({});

  readonly load = (params: HttpParams) => this.ccListService.getCcListPage(
    // Without a type chosen, every type that can be appended is asked for.
    params.has('types') ? params : params.set('types', this.typeList.map(e => e.toLowerCase()).join(',')));
  readonly columns = createTableColumns(APPEND_ASSOCIATION_TABLE);
  typeList: string[] = ['ASCCP', 'BCCP'];
  action: string;

  constructor() {
    this.action = this.data.action;
    if (this.data.isGlobal) {
      this.typeList = ['BCCP'];
    }
    afterNextRender(() => {
      // Every type that can be appended, to begin with.
      this.view().setFilter('types', this.typeList.map(e => e.toLowerCase()));
      this.ready.set(true);
    });
  }

  ngOnInit() {
    const params: Record<string, string> = {
      libraryId: '' + this.data.libraryId,
      releaseId: '' + (this.data.releaseId || 0)
    };
    if (this.data.excludes?.length > 0) {
      params['excludes'] = this.data.excludes.join(',');
    }
    this.extraParams.set(params);
    this.filterOptions.set({types: this.typeList.map(e => ({value: e.toLowerCase(), label: e}))});
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
            this.dialogRef.close(this.selection.selected[0]);
          }
        });
    } else {
      this.dialogRef.close(this.selection.selected[0]);
    }
  }


  getRouterLink(ccList: CcListEntry) {
    switch (ccList.type.toUpperCase()) {
      case 'ASCCP':
        return 'core_component/asccp/' + ccList.manifestId;

      case 'BCCP':
        return 'core_component/bccp/' + ccList.manifestId;

      default:
        return window.location.pathname;
    }
  }
}
