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
import {ExtensionDetailComponent} from '../../extension-detail/extension-detail.component';
import {Base, Semantics} from '../../domain/core-component-node';

const BASED_ACC_TABLE = defineTable({
  ...CC_WITHOUT_TYPE_TABLE,
  filters: [F.states, F.definition, F.module, F.deprecated, F.componentTypes, F.owner, F.updater, F.updatedDate],
});

@Component({
  standalone: false,
  selector: 'score-based-acc-dialog',
  templateUrl: './based-acc-dialog.component.html',
  styleUrls: ['./based-acc-dialog.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager
})
export class BasedAccDialogComponent implements OnInit {
  dialogRef = inject<MatDialogRef<ExtensionDetailComponent>>(MatDialogRef);
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
  readonly columns = createTableColumns(BASED_ACC_TABLE);
  action: string;

  constructor() {
    afterNextRender(() => {
      // The components that can be based on, to begin with.
      this.view().setFilter('componentTypes', [Base.value, Semantics.value]);
      this.ready.set(true);
    });
  }

  ngOnInit() {
    this.action = this.data.action;
    this.extraParams.set({
      libraryId: '' + this.data.libraryId,
      releaseId: '' + (this.data.releaseId || 0),
      types: 'acc',
      excludes: '' + this.data.manifestId
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


  getRouterLink(ccList: CcListEntry) {
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
