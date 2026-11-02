import {ChangeDetectionStrategy, Component} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ListPageComponent} from './list-page.component';

@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [ListPageComponent],
  template: `
    <score-list-page [title]="title" [subtitle]="subtitle" [spacer]="spacer">
      @if (customTitle) {
        <ng-container ngProjectAs="[listTitle]"><span class="custom-title">Library</span></ng-container>
      }
      <ng-container ngProjectAs="[listActions]"><button class="create">New</button></ng-container>
      <div listHeader class="header">Header</div>
      <div class="table-view">Table view</div>
      <ng-container ngProjectAs="[listExtra]"><span class="extra">Extra</span></ng-container>
      <div listFooter class="footer">Footer</div>
      @if (actionBar) {
        <ng-container ngProjectAs="[listActionBar]">
          <button class="back">Back</button>
          <button class="next">Next</button>
        </ng-container>
      }
    </score-list-page>`
})
class HostComponent {
  title: string | null = 'Contexts';
  spacer = true;
  subtitle = '';
  customTitle = false;
  actionBar = false;
}

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

  it('lays out header, table view and extra inside the card', () => {
    detect();
    const card = el('div.context-section > mat-card')!;
    const children = Array.from(card.children).map(c => c.className.split(' ')[0]);
    expect(children).toEqual(['header', 'table-view', 'extra']);
  });

  it('renders the footer below the card, inside the section', () => {
    detect();
    expect(el('div.context-section > .footer')).toBeTruthy();
  });

  it('renders the action bar below the card when buttons are projected', () => {
    host.actionBar = true;
    detect();
    const buttons = fixture.nativeElement.querySelectorAll(
      'div.context-section > div.row.action-group > div.col-12.actions-wrap > div.actions > button');
    expect(Array.from(buttons).map((b: any) => b.className)).toEqual(['back', 'next']);
  });

  it('leaves the action bar empty when no buttons are projected', () => {
    detect();
    expect(el('div.action-group > .actions-wrap > .actions')?.children.length ?? 0).toBe(0);
  });

  it('stacks a sub-title under the title and keeps a plain row without one', () => {
    detect();
    expect(el('mat-toolbar')!.classList.contains('flex-column')).toBe(false);
    host.subtitle = 'Step 1';
    detect();
    expect(el('mat-toolbar')!.classList.contains('flex-column')).toBe(true);
    expect(el('mat-toolbar mat-card-subtitle')?.textContent).toBe('Step 1');
  });

  it('accepts a projected title in place of the plain one', () => {
    host.title = null;
    host.customTitle = true;
    detect();
    expect(el('mat-toolbar span.title')).toBeNull();
    expect(el('mat-toolbar span.custom-title')).toBeTruthy();
  });
});
