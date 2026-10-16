import {ChangeDetectionStrategy, Component, inject, viewChild} from '@angular/core';
import {SelectionModel} from '@angular/cdk/collections';
import {HttpParams} from '@angular/common/http';
import {MatSnackBar} from '@angular/material/snack-bar';
import {map} from 'rxjs/operators';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../../common/score-table';
import {OasDoc, toMinorOpenApiVersion} from '../domain/openapi-doc';
import {OpenAPIService} from '../domain/openapi.service';

const OPEN_API_DOCUMENT_TABLE = defineTable({
  storageKey: 'TableColumns-OpenAPIDocumentPage',
  columns: [
    {id: 'title', label: 'Title', width: 0, sortable: true},
    {id: 'openAPIVersion', label: 'OpenAPI Version', width: 140, sortable: true},
    {id: 'version', label: 'Version', header: 'Document Version', width: 140, sortable: true},
    {id: 'licenseName', label: 'License Name', width: 200, sortable: true},
    {id: 'description', label: 'Description', width: 400, sortable: true},
    {id: 'lastUpdateTimestamp', label: 'Updated On', header: 'Updated on', sortLabel: 'Last Update Timestamp', width: 160, sortable: true},
  ],
  // Several sorts, which the endpoint takes as lists.
  sortParam: 'sortActives',
  defaultSort: [{active: 'lastUpdateTimestamp', direction: 'desc'}],
  search: {key: 'title', placeholder: 'Search by Title'},
  filters: [
    {type: 'text', key: 'description', label: 'Description'},
    {type: 'updater', key: 'updaterUsernameList', label: 'Updater'},
    {
      type: 'dateRange', key: 'updatedDate', label: 'Updated',
      httpStart: 'updateStart', httpEnd: 'updateEnd', urlStart: 'updateStart', urlEnd: 'updateEnd'
    },
  ],
});

@Component({
  standalone: false,
  selector: 'score-oas-doc-list',
  templateUrl: './oas-doc-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager
})
export class OasDocListComponent {
  private openAPIService = inject(OpenAPIService);
  private confirmDialogService = inject(ConfirmDialogService);
  private snackBar = inject(MatSnackBar);

  title = 'OpenAPI Document';

  readonly columns = createTableColumns(OPEN_API_DOCUMENT_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<OasDoc>>(ScoreTableViewComponent);
  selection = new SelectionModel<number>(true, []);

  readonly load = (params: HttpParams) => this.openAPIService.getOasDocListPage(params).pipe(
    map(resp => ({
      ...resp,
      list: resp.list.map((elm: OasDoc) => {
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
        return elm;
      })
    })));
  readonly rowKey = (row: OasDoc) => row.oasDocId;

  displayOpenApiVersion(version: string): string {
    return toMinorOpenApiVersion(version);
  }

  discard() {
    const oasDocIds = this.selection.selected;
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard ' + (oasDocIds.length > 1 ? 'oas docs' : 'oas doc') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to discard selected ' + (oasDocIds.length > 1 ? 'oas docs' : 'oas doc') + '?',
      'The ' + (oasDocIds.length > 1 ? 'oas docs' : 'oas doc') + ' will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.openAPIService.delete(...oasDocIds).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.selection.clear();
            this.view().reload();
          }, err => {
            this.snackBar.open('Discard\'s forbidden! The oas doc is used.', '', {
              duration: 5000,
            });
          });
        }
      });
  }
}
