import {ChangeDetectionStrategy, Component} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ListPageComponent} from './list-page.component';

@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [ListPageComponent],
  template: `
    <score-list-page [title]="title" [loading]="loading" [showColumns]="showColumns"
      [tableWrapperClass]="tableWrapperClass" [spacer]="spacer">
      @if (customTitle) {
        <ng-container ngProjectAs="[listTitle]"><span class="custom-title">Library</span></ng-container>
      }
      <ng-container ngProjectAs="[listActions]"><button class="create">New</button></ng-container>
      <div listHeader class="header">Header</div>
      <div listFilter class="filter">Filter</div>
      <div listColumns class="columns">Columns</div>
      <table listTable class="table"></table>
      <div listTable class="paginator">Paginator</div>
      <ng-container ngProjectAs="[listExtra]"><span class="extra">Extra</span></ng-container>
      <div listFooter class="footer">Footer</div>
    </score-list-page>`
})
class HostComponent {
  title: string | null = 'Contexts';
  loading = false;
  showColumns = true;
  tableWrapperClass = 'mat-elevation-z1 table-wrapper mt-3';
  spacer = true;
  customTitle = false;
}

@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [ListPageComponent],
  template: `
    <score-list-page title="Contexts">
      <div listColumns class="columns">Columns</div>
      <div listToolbar class="toolbar">Toolbar</div>
    </score-list-page>`
})
class ToolbarHostComponent {}

describe('ListPageComponent', () => {
  let fixture: ComponentFixture<HostComponent>;
  let host: HostComponent;
  const el = (selector: string): HTMLElement | null => fixture.nativeElement.querySelector(selector);
  // The fixture host is checked like an OnPush view, so a changed host property needs to be marked.
  const detect = (): void => {
    fixture.changeDetectorRef.markForCheck();
    fixture.detectChanges();
  };

  beforeEach(() => {
    fixture = TestBed.createComponent(HostComponent);
    host = fixture.componentInstance;
  });

  it('is derived from the base page: title and actions in the toolbar', () => {
    detect();
    expect(el('mat-toolbar span.title')?.textContent).toBe('Contexts');
    expect(el('mat-toolbar button.create')).toBeTruthy();
    expect(el('div.context-section > mat-card')).toBeTruthy();
  });

  it('lays out header, filter, columns, table and extra inside the card', () => {
    detect();
    const card = el('div.context-section > mat-card')!;
    const children = Array.from(card.children).map(c => c.className.split(' ')[0] || c.tagName.toLowerCase());
    expect(children.slice(0, 3)).toEqual(['header', 'filter', 'mat-mdc-card-content']);
    expect(card.querySelector('mat-card-content .table-container .score-column-container .columns')).toBeTruthy();
    expect(card.querySelector('.table-container .table-wrapper > table.table')).toBeTruthy();
    expect(card.querySelector('.table-container .table-wrapper > .paginator')).toBeTruthy();
    expect(card.lastElementChild!.classList.contains('extra')).toBe(true);
  });

  it('renders the footer below the card, inside the section', () => {
    detect();
    expect(el('div.context-section > .footer')).toBeTruthy();
  });

  it('shows the spinner inside the card content while loading', () => {
    host.loading = true;
    detect();
    expect(el('mat-card-content > .loading-container mat-spinner')).toBeTruthy();
  });

  it('can leave out the column row and restyle the table wrapper', () => {
    host.showColumns = false;
    host.tableWrapperClass = 'mat-elevation-z1 table-wrapper';
    detect();
    expect(el('.score-column-container')).toBeNull();
    expect(el('.table-container > .table-wrapper')!.classList.contains('mt-3')).toBe(false);
  });

  it('can replace the whole row above the table', () => {
    const toolbarFixture = TestBed.createComponent(ToolbarHostComponent);
    toolbarFixture.detectChanges();
    const root: HTMLElement = toolbarFixture.nativeElement;
    expect(root.querySelector('.table-container > .toolbar')).toBeTruthy();
    expect(root.querySelector('.score-column-container')).toBeNull();
  });

  it('accepts a projected title in place of the plain one', () => {
    host.title = null;
    host.customTitle = true;
    detect();
    expect(el('mat-toolbar span.title')).toBeNull();
    expect(el('mat-toolbar span.custom-title')).toBeTruthy();
  });
});
