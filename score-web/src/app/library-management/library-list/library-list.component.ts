import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {AuthService} from '../../authentication/auth.service';
import {LibraryService} from '../domain/library.service';
import {createTableColumns, defineTable} from '../../common/score-table';

const LIBRARY_TABLE = defineTable({
  storageKey: 'TableColumns-LibraryPage',
  columns: [
    {id: 'name', label: 'Name', width: 0, sortable: true},
    {id: 'type', label: 'Type', width: 140, sortable: true},
    {id: 'organization', label: 'Organization', width: 400, sortable: true},
    {id: 'domain', label: 'Domain', width: 200, sortable: true},
    {id: 'description', label: 'Description', width: 400, sortable: true},
    {id: 'lastUpdateTimestamp', label: 'Updated On', header: 'Updated on', width: 160, sortable: true},
  ],
  defaultSort: {active: 'lastUpdateTimestamp', direction: 'desc'},
  search: {key: 'name', placeholder: 'Search by Name'},
  filters: [
    {type: 'updater', key: 'updaterLoginIdList', label: 'Updater'},
    {type: 'dateRange', key: 'updatedDate', label: 'Updated', httpParam: 'lastUpdatedOn'},
    {type: 'text', key: 'type', label: 'Type'},
    {type: 'text', key: 'organization', label: 'Organization'},
    {type: 'text', key: 'domain', label: 'Domain'},
    {type: 'text', key: 'description', label: 'Description'},
  ],
});

@Component({
  standalone: false,
  selector: 'score-library-list',
  templateUrl: './library-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./library-list.component.css']
})
export class LibraryListComponent {
  private auth = inject(AuthService);
  private service = inject(LibraryService);

  readonly columns = createTableColumns(LIBRARY_TABLE);
  readonly load = (params: HttpParams) => this.service.getLibraryList(params);

  get isAdmin(): boolean {
    return this.auth.isAdmin();
  }
}
