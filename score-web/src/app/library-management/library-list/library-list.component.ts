import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {AuthService} from '../../authentication/auth.service';
import {LibraryService} from '../domain/library.service';
import {createTableColumns, defineTable} from '../../common/score-table';
import {CompactRecord, memoizeRow} from '../../common/record-compact-row';
import {LibraryListEntry} from '../domain/library';

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

  /** What the compact row shows of a library: made once for a row. */
  readonly compactRecord = memoizeRow((library: LibraryListEntry): CompactRecord => ({
    badges: library.type ? [{text: library.type}] : [],
    titleBadges: library.isDefault ? [{text: 'Default', kind: 'state', cssClass: 'default-indicator', background: '#fdd835', font: '#212121'}] : [],
    title: library.name,
    link: '/library/' + library.libraryId,
    meta: [
      ...(library.organization ? [{
        label: 'Organization', value: library.organization, href: library.link || undefined, priority: 1 as const
      }] : []),
      ...(library.domain ? [{label: 'Domain', value: library.domain, priority: 2 as const}] : []),
      ...(library.description ? [{label: 'Description', value: library.description, priority: 3 as const}] : [])
    ],
    updated: library.lastUpdated ? {when: library.lastUpdated.when, by: library.lastUpdated.who?.loginId} : undefined
  }));

  readonly columns = createTableColumns(LIBRARY_TABLE);
  readonly load = (params: HttpParams) => this.service.getLibraryList(params);

  get isAdmin(): boolean {
    return this.auth.isAdmin();
  }
}
