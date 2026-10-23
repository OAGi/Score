import {ChangeDetectionStrategy, Component} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {provideRouter} from '@angular/router';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {HttpParams} from '@angular/common/http';
import {SelectionModel} from '@angular/cdk/collections';
import {of} from 'rxjs';
import {By} from '@angular/platform-browser';
import {Location} from '@angular/common';
import {ChangeDetectorRef, signal} from '@angular/core';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {ListPageComponent} from '../list-page/list-page.component';
import {ScoreTableViewComponent} from './score-table-view.component';
import {SCORE_TABLE} from './score-table';
import {TableColumnsState} from './table-columns-state';
import {defineTable} from './table-def';
import {UserToken} from '../../authentication/domain/auth';

const TABLE = defineTable({
  storageKey: 'TableColumns-ViewSpecPage',
  columns: [
    {id: 'name', label: 'Name', width: 0, sortable: true},
    {id: 'size', label: 'Size', width: 80},
  ],
  defaultSort: {active: 'name', direction: 'asc'},
  search: {key: 'name', placeholder: 'Search by Name'},
});

const cells = `
  <ng-container scoreColumnDef="name">
    <td score-cell *scoreCellDef="let row"><b class="n">{{ row.name }}</b></td>
  </ng-container>
  <ng-container [scoreColumnDef]="columns.column('size')">
    <th score-header-cell *scoreHeaderCellDef>Custom size</th>
    <td score-cell *scoreCellDef="let row">{{ row.size }}</td>
  </ng-container>`;

abstract class Base {
  columns = new TableColumnsState(TABLE, {username: 'view-spec'} as UserToken);
  requests: string[] = [];
  load = (params: HttpParams) => {
    this.requests.push(params.toString());
    return of({length: 1, list: [{name: 'a', size: 1}]} as any);
  };
}

/** No page around at all. */
@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [...SCORE_TABLE],
  template: `<div class="plain"><score-table-view [table]="columns" [load]="load">${cells}</score-table-view></div>`
})
class PlainHostComponent extends Base {}

@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [ListPageComponent, ...SCORE_TABLE],
  template: `<score-list-page title="T"><score-table-view [table]="columns" [load]="load">${cells}</score-table-view></score-list-page>`
})
class ListPageHostComponent extends Base {}

const MULTI_TABLE = defineTable({
  storageKey: 'TableColumns-ViewSpecMultiPage',
  columns: [
    {id: 'name', label: 'Name', width: 0, sortable: true},
    {id: 'size', label: 'Size', width: 80, sortable: true},
  ],
  defaultSort: [{active: 'name', direction: 'asc'}],
});

const multiCells = `
  <ng-container scoreColumnDef="name"><td score-cell *scoreCellDef="let row">{{ row.name }}</td></ng-container>
  <ng-container scoreColumnDef="size"><td score-cell *scoreCellDef="let row">{{ row.size }}</td></ng-container>`;

@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [...SCORE_TABLE],
  template: `<score-table-view [table]="columns" [load]="load" multiSort>${multiCells}</score-table-view>`
})
class MultiSortHostComponent extends Base {
  override columns = new TableColumnsState(MULTI_TABLE, {username: 'view-spec'} as UserToken);
}

@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [...SCORE_TABLE],
  template: `<score-table-view [table]="columns" [load]="load" multiSort [multiSortSettings]="false">${multiCells}</score-table-view>`
})
class MultiSortWithoutChipsHostComponent extends Base {
  override columns = new TableColumnsState(MULTI_TABLE, {username: 'view-spec'} as UserToken);
}

const ROWS_TABLE = defineTable({
  storageKey: 'TableColumns-ViewSpecRowsPage',
  columns: [
    {id: 'name', label: 'Name', width: 0},
    {id: 'size', label: 'Size', width: 80},
  ],
  fixed: [
    {id: 'icon', position: 'start', width: 40},
    {id: 'more', position: 'end', header: 'Menu', width: 60},
  ],
  defaultSort: {active: 'name', direction: 'asc'},
});

@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [...SCORE_TABLE],
  template: `
    <score-table-view [table]="columns" [load]="load" [selection]="selection" [rowKey]="rowKey"
      [rowClass]="rowClass" (rowClick)="clicked.push($event.name)" [selectOnRowClick]="selectOnRowClick">
      <ng-container [scoreColumnDef]="columns.column('icon')">
        <td score-cell *scoreCellDef="let row"><i class="icon">i</i></td>
      </ng-container>
      <ng-container scoreColumnDef="name"><td score-cell *scoreCellDef="let row">{{ row.name }}</td></ng-container>
      <ng-container scoreColumnDef="size"><td score-cell *scoreCellDef="let row">{{ row.size }}</td></ng-container>
      <ng-container [scoreColumnDef]="columns.column('more')">
        <td score-cell *scoreCellDef="let row"><button class="more">...</button></td>
      </ng-container>
      @if (withDetail) {
        <ng-container *scoreDetailDef="let row"><p class="detail">Detail of {{ row.name }}</p></ng-container>
      }
    </score-table-view>`
})
class RowsHostComponent extends Base {
  override columns = new TableColumnsState(ROWS_TABLE, {username: 'view-spec'} as UserToken);
  selection = new SelectionModel<string>(true, []);
  rowKey = (row: { name: string }) => row.name;
  rowClass = (row: { name: string }) => row.name === 'a' ? ['first', 'invalid-row'] : null;
  clicked: string[] = [];
  selectOnRowClick = true;
  withDetail = true;
}

/** A cell with control flow in it: the query for the header cell of its column then runs again with every row. */
@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [...SCORE_TABLE],
  template: `
    <score-table-view [table]="columns" [load]="load">
      <ng-container scoreColumnDef="name">
        <td score-cell *scoreCellDef="let row">
          @if (row.codeListId) {
            <div><a target="_blank" href="/code_list/{{ row.codeListId }}"><span>{{ row.name }}</span></a></div>
          }
          @if (!row.codeListId) {
            <div><span>{{ row.name }}</span></div>
          }
        </td>
      </ng-container>
      <ng-container scoreColumnDef="size"><td score-cell *scoreCellDef="let row">{{ row.size }}</td></ng-container>
    </score-table-view>`
})
class ControlFlowCellHostComponent extends Base {}

const DIALOG_TABLE = defineTable({
  storageKey: 'TableColumns-ViewSpecDialogPage',
  columns: [
    {id: 'name', label: 'Name', width: 0, sortable: true},
    {id: 'size', label: 'Size', width: 80},
  ],
  defaultSort: {active: 'name', direction: 'asc'},
  search: {key: 'name', placeholder: 'Search by Name'},
  filters: [
    {type: 'updater', key: 'ownerLoginIdList', label: 'Owner'},
    {type: 'updater', key: 'updaterLoginIdList', label: 'Updater'},
  ],
});

/** What a dialog needs: no URL, parameters of its own that arrive late, one choice, and its own things around the search box. */
@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [...SCORE_TABLE],
  template: `
    <score-table-view [table]="columns" [load]="load" [selection]="selection" singleSelect
      [syncUrl]="false" [extraParams]="extra()" [ready]="ready()" advancedSearchOffset="11.5rem">
      <div searchPrefix class="prefix">Branch</div>
      <div advancedFilters class="state">State</div>
      <ng-container scoreColumnDef="name"><td score-cell *scoreCellDef="let row">{{ row.name }}</td></ng-container>
      <ng-container scoreColumnDef="size"><td score-cell *scoreCellDef="let row">{{ row.size }}</td></ng-container>
    </score-table-view>`
})
class DialogHostComponent extends Base {
  override columns = new TableColumnsState(DIALOG_TABLE, {username: 'view-spec'} as UserToken);
  selection = new SelectionModel<{ name: string }>(true, []);
  ready = signal(false);
  extra = signal<Record<string, string | null>>({libraryId: '1', releaseId: '7', states: 'Published,Production', skipped: null});
  override load = (params: HttpParams) => {
    this.requests.push(params.toString());
    return of({length: 2, list: [{name: 'a', size: 1}, {name: 'b', size: 2}]} as any);
  };
}

describe('ScoreTableViewComponent', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [NoopAnimationsModule],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
    });
  });

  it('works without any page around it: search, table with cells, paginator', () => {
    const fixture = TestBed.createComponent(PlainHostComponent);
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;

    expect(el.querySelector('.plain score-search-bar')).toBeTruthy();
    expect(el.querySelector('.plain score-column-selector')).toBeTruthy();
    expect(el.querySelector('.plain mat-paginator')).toBeTruthy();
    expect(Array.from(el.querySelectorAll('th')).map(t => t.textContent.trim())).toEqual(['Name', 'Custom size']);
    expect(el.querySelector('td b.n').textContent).toBe('a');
    expect(fixture.componentInstance.requests).toEqual(['pageIndex=0&pageSize=10&orderBy=%2Bname']);
  });

  it('is shown by score-list-page in place of its own table region', () => {
    const fixture = TestBed.createComponent(ListPageHostComponent);
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;

    expect(el.querySelectorAll('.table-container').length).toBe(1);
    expect(el.querySelector('mat-card > score-table-view')).toBeTruthy();
    expect(el.querySelector('mat-toolbar span.title').textContent).toBe('T');
  });

  it('refuses a column without a column definition', () => {
    @Component({
      changeDetection: ChangeDetectionStrategy.Eager,
      imports: [...SCORE_TABLE],
      template: `<score-table-view [table]="columns" [load]="load">
        <ng-container scoreColumnDef="name">
          <td score-cell *scoreCellDef="let row">{{ row.name }}</td>
        </ng-container></score-table-view>`
    })
    class IncompleteHostComponent extends Base {}

    const fixture = TestBed.createComponent(IncompleteHostComponent);
    expect(() => fixture.detectChanges()).toThrowError(/missing scoreColumnDef for \[size\]/);
  });

  describe('multiSort', () => {
    const headerOf = (el: HTMLElement, text: string) =>
      Array.from(el.querySelectorAll('th')).find(t => t.textContent.trim().startsWith(text)) as HTMLElement;
    const chips = (el: HTMLElement) => Array.from(el.querySelectorAll('mat-chip-row')).map(c => c.textContent.trim().replace(/\s+/g, ' '));

    it('sorts by several columns, in the order of the clicks, with the sort chips', () => {
      const fixture = TestBed.createComponent(MultiSortHostComponent);
      fixture.detectChanges();
      const el: HTMLElement = fixture.nativeElement;
      const requests = fixture.componentInstance.requests;

      expect(requests).toEqual(['pageIndex=0&pageSize=10&orderBy=%2Bname']);
      expect(el.querySelector('table[matMultiSort], table.mat-mdc-table')).toBeTruthy();
      expect(chips(el).length).toBe(1);

      headerOf(el, 'Size').click();
      fixture.detectChanges();
      expect(requests.at(-1)).toBe('pageIndex=0&pageSize=10&orderBy=%2Bname,%2Bsize');
      expect(chips(el).length).toBe(2);

      headerOf(el, 'Size').click();
      fixture.detectChanges();
      expect(requests.at(-1)).toBe('pageIndex=0&pageSize=10&orderBy=%2Bname,-size');

      headerOf(el, 'Size').click();
      fixture.detectChanges();
      expect(requests.at(-1)).toBe('pageIndex=0&pageSize=10&orderBy=%2Bname');
    });

    it('can leave out the sort chips', () => {
      const fixture = TestBed.createComponent(MultiSortWithoutChipsHostComponent);
      fixture.detectChanges();
      expect(fixture.nativeElement.querySelector('mat-multi-sort-table-settings')).toBeNull();
      expect(fixture.nativeElement.querySelector('score-column-selector')).toBeTruthy();
    });

    it('does not show the chips for a table that sorts by one column', () => {
      const fixture = TestBed.createComponent(PlainHostComponent);
      fixture.detectChanges();
      expect(fixture.nativeElement.querySelector('mat-multi-sort-table-settings')).toBeNull();
    });
  });

  describe('fixed columns, detail row and row hooks', () => {
    const render = () => {
      const fixture = TestBed.createComponent(RowsHostComponent);
      fixture.detectChanges();
      return fixture;
    };
    const dataRow = (el: HTMLElement) => el.querySelector('tr.element-row') as HTMLElement;

    it('shows the fixed columns before and after the selectable ones, but not in the column selector', () => {
      const fixture = render();
      const el: HTMLElement = fixture.nativeElement;
      expect(Array.from(el.querySelectorAll('th')).map(t => t.textContent.trim())).toEqual(['', '', 'Name', 'Size', 'Menu']);
      expect(Array.from(dataRow(el).querySelectorAll('td')).map(t => t.classList.contains('mat-column-icon') || t.classList.contains('mat-column-more'))
      ).toEqual([false, true, false, false, true]);
      expect(fixture.componentInstance.columns.list().map(c => c.name)).toEqual(['Name', 'Size']);
      expect(el.querySelector('th.mat-column-more .resize-handle')?.getAttribute('style')).toContain('display: none');
    });

    it('opens the detail row under a clicked row, and closes it on the next click', () => {
      const fixture = render();
      const el: HTMLElement = fixture.nativeElement;
      const detailTd = () => el.querySelector('tr.detail-row td') as HTMLElement;

      expect(el.querySelectorAll('tr.detail-row').length).toBe(1);
      expect(detailTd().getAttribute('colspan')).toBe('5');
      expect(el.querySelector('table.has-detail')).toBeTruthy();
      expect(el.querySelector('.expanded-row')).toBeNull();

      dataRow(el).click();
      fixture.detectChanges();
      expect(el.querySelector('tr.element-row.expanded-row')).toBeTruthy();
      expect(detailTd().textContent).toContain('Detail of a');

      dataRow(el).click();
      fixture.detectChanges();
      expect(el.querySelector('.expanded-row')).toBeNull();
    });

    it('has no detail row, and no room for one, without *scoreDetailDef', () => {
      const fixture = TestBed.createComponent(RowsHostComponent);
      fixture.componentInstance.withDetail = false;
      fixture.detectChanges();
      expect(fixture.nativeElement.querySelector('tr.detail-row')).toBeNull();
      expect(fixture.nativeElement.querySelector('table.has-detail')).toBeNull();
    });

    it('gives a row its class, tells about a click and selects on it', () => {
      const fixture = render();
      const host = fixture.componentInstance;
      const row = dataRow(fixture.nativeElement);
      expect(row.classList.contains('first') && row.classList.contains('invalid-row')).toBe(true);

      row.click();
      fixture.detectChanges();
      expect(host.clicked).toEqual(['a']);
      expect(host.selection.selected).toEqual(['a']);

      row.click();
      expect(host.selection.selected).toEqual([]);
    });

    it('does not select on a click when it is told not to', () => {
      const fixture = TestBed.createComponent(RowsHostComponent);
      fixture.componentInstance.selectOnRowClick = false;
      fixture.detectChanges();
      dataRow(fixture.nativeElement).click();
      expect(fixture.componentInstance.clicked).toEqual(['a']);
      expect(fixture.componentInstance.selection.selected).toEqual([]);
    });
  });

  it('keeps the default header of a column whose cell has control flow, when the columns change', () => {
    const fixture = TestBed.createComponent(ControlFlowCellHostComponent);
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    const headers = () => Array.from(el.querySelectorAll('th')).map(t => t.textContent.trim());
    expect(headers()).toEqual(['Name', 'Size']);

    const columns = fixture.componentInstance.columns;
    columns.update([{name: 'Size', selected: true}, {name: 'Name', selected: true}]);
    fixture.detectChanges();
    expect(headers()).toEqual(['Size', 'Name']);

    columns.update([{name: 'Size', selected: true}, {name: 'Name', selected: false}]);
    fixture.detectChanges();
    expect(headers()).toEqual(['Size']);
  });

  describe('in a dialog', () => {
    beforeEach(() => {
      TestBed.overrideProvider(AccountListService, {useValue: {getAccountNames: () => of(['x', 'y'])}});
      TestBed.configureTestingModule({providers: [{provide: AccountListService, useValue: {getAccountNames: () => of(['x', 'y'])}}]});
    });
    const render = () => {
      const fixture = TestBed.createComponent(DialogHostComponent);
      fixture.detectChanges();
      return fixture;
    };
    const openAdvancedSearch = (fixture: any) => {
      (fixture.nativeElement.querySelector('.main-search button:last-of-type') as HTMLElement).click();
      fixture.detectChanges();
    };
    const viewOf = (fixture: any) => fixture.debugElement.query(By.directive(ScoreTableViewComponent)).componentInstance as ScoreTableViewComponent;

    it('waits until the screen is ready, then loads with the screen\'s parameters and leaves a null one out', () => {
      const fixture = render();
      const host = fixture.componentInstance;
      expect(host.requests).toEqual([]);

      host.ready.set(true);
      fixture.detectChanges();
      expect(host.requests).toEqual(['pageIndex=0&pageSize=10&orderBy=%2Bname&libraryId=1&releaseId=7&states=Published,Production']);
    });

    it('searches again from the first page when the screen\'s parameters change', () => {
      const fixture = render();
      const host = fixture.componentInstance;
      host.ready.set(true);
      fixture.detectChanges();

      host.extra.set({libraryId: '1', releaseId: '9', states: 'Published'});
      fixture.detectChanges();
      expect(host.requests).toHaveLength(2);
      expect(host.requests.at(-1)).toBe('pageIndex=0&pageSize=10&orderBy=%2Bname&libraryId=1&releaseId=9&states=Published');
    });

    it('does not read or write the URL', () => {
      const location = TestBed.inject(Location);
      const replaceState = vi.spyOn(location, 'replaceState');
      const fixture = render();
      fixture.componentInstance.ready.set(true);
      fixture.detectChanges();
      viewOf(fixture).onSearch();
      expect(replaceState).not.toHaveBeenCalled();
    });

    it('selects one row at a time, with no select-all checkbox', () => {
      const fixture = render();
      fixture.componentInstance.ready.set(true);
      fixture.detectChanges();
      const el: HTMLElement = fixture.nativeElement;
      const rows = el.querySelectorAll<HTMLElement>('tr.element-row');
      const host = fixture.componentInstance;

      expect(el.querySelector('th mat-checkbox')).toBeNull();
      rows[0].click();
      fixture.detectChanges();
      expect(host.selection.selected.map(r => r.name)).toEqual(['a']);
      rows[1].click();
      fixture.detectChanges();
      expect(host.selection.selected.map(r => r.name)).toEqual(['b']);
      rows[1].click();
      fixture.detectChanges();
      expect(host.selection.selected).toEqual([]);
    });

    it('shows what the screen puts before the search box and among the advanced filters', () => {
      const fixture = render();
      fixture.componentInstance.ready.set(true);
      fixture.detectChanges();
      const el: HTMLElement = fixture.nativeElement;
      expect(el.querySelector('.main-search .prefix')).toBeTruthy();

      openAdvancedSearch(fixture);
      const advanced = el.querySelector<HTMLElement>('.advanced-search > div');
      expect(advanced.style.paddingLeft).toBe('11.5rem');
      expect(advanced.querySelector('.row > .state')).toBeTruthy();
    });

    it('has a choice list for each updater filter, and sends each one under its own name', () => {
      const fixture = render();
      const host = fixture.componentInstance;
      host.ready.set(true);
      fixture.detectChanges();
      const view = viewOf(fixture);
      openAdvancedSearch(fixture);

      expect(Object.keys(view.updaterFilters)).toEqual(['ownerLoginIdList', 'updaterLoginIdList']);
      expect(fixture.nativeElement.querySelectorAll('.advanced-search mat-select').length).toBe(2);

      view.query.values['ownerLoginIdList'] = ['x'];
      view.query.values['updaterLoginIdList'] = ['y'];
      view.onSearch();
      expect(host.requests.at(-1)).toContain('ownerLoginIdList=x&updaterLoginIdList=y');
    });
  });

  it('marks the screen to be checked on every event in it, even one that is not let through to the view', () => {
    const fixture = TestBed.createComponent(RowsHostComponent);
    fixture.detectChanges();
    const markForCheck = vi.spyOn(Object.getPrototypeOf(fixture.debugElement.injector.get(ChangeDetectorRef)), 'markForCheck');
    const el: HTMLElement = fixture.nativeElement;
    const button = el.querySelector('button.more') as HTMLElement;
    button.addEventListener('click', e => e.stopPropagation());
    markForCheck.mockClear();

    button.click();
    expect(markForCheck).toHaveBeenCalled();
    markForCheck.mockClear();
    el.querySelector('tr.element-row mat-checkbox input').dispatchEvent(new Event('change', {bubbles: true}));
    expect(markForCheck).toHaveBeenCalled();
    markForCheck.mockRestore();
  });
});
