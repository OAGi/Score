import {Component, signal} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {AuthService} from '../../authentication/auth.service';
import {CUSTOM_PAGE_SIZE_KEY, MAX_PAGE_SIZE, ScorePaginator} from './score-paginator.component';
import {loadProperty} from '../utility';
import {UserToken} from '../../authentication/domain/auth';

@Component({
  imports: [ScorePaginator],
  template: `<score-paginator [length]="97" [pageSize]="10" [pageSizeOptions]="[10, 25, 50]" showFirstLastButtons
                              (page)="events.push($event)"></score-paginator>`
})
class HostComponent {
  events: PageEvent[] = [];
}

describe('ScorePaginator', () => {
  let fixture: ComponentFixture<HostComponent>;
  let el: HTMLElement;

  const user = {username: 'paginator-spec'} as UserToken;
  const stored = () => loadProperty(user, CUSTOM_PAGE_SIZE_KEY, '');

  const create = () => {
    fixture = TestBed.createComponent(HostComponent);
    fixture.detectChanges();
    el = fixture.nativeElement;
  };

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [NoopAnimationsModule],
      providers: [{provide: AuthService, useValue: {getUserToken: () => user}}]
    });
    create();
  });

  const paginator = () => fixture.debugElement.children[0].componentInstance as ScorePaginator;
  const openList = () => {
    (el.querySelector('.mat-mdc-select-trigger') as HTMLElement).click();
    fixture.detectChanges();
  };
  const field = () => document.querySelector('.cdk-overlay-container .score-paginator-custom-size input') as HTMLInputElement;
  const apply = (value: string) => {
    openList();
    field().value = value;
    field().dispatchEvent(new KeyboardEvent('keydown', {key: 'Enter', bubbles: true}));
    fixture.detectChanges();
  };

  afterEach(() => document.querySelectorAll('.cdk-overlay-container *').forEach(e => e.remove()));

  it('is a MatPaginator and can be found as one', () => {
    expect(paginator() instanceof MatPaginator).toBe(true);
    expect(fixture.debugElement.children[0].injector.get(MatPaginator)).toBe(paginator());
  });

  it('looks as MatPaginator does: label, size select, range and the four buttons', () => {
    expect(el.querySelector('score-paginator.mat-mdc-paginator[role=group]')).toBeTruthy();
    expect(el.querySelector('.mat-mdc-paginator-page-size-label').textContent.trim()).toBe('Items per page:');
    expect(el.querySelector('.mat-mdc-paginator-page-size-select')).toBeTruthy();
    expect(el.querySelector('.mat-mdc-paginator-range-label').textContent.trim()).toBe('1 – 10 of 97');
    expect(el.querySelectorAll('.mat-mdc-paginator-range-actions button').length).toBe(4);
  });

  const listed = () => Array.from(document.querySelectorAll('.cdk-overlay-container mat-option:not(.score-paginator-custom-option)')).map(o => o.textContent.trim());
  /** What the select shows: it takes a selection for a new option a moment after it is rendered. */
  const shown = async () => {
    await fixture.whenStable();
    fixture.detectChanges();
    return el.querySelector('.mat-mdc-select-value').textContent.trim();
  };

  it('offers the sizes of the list, and the empty field for a size of one\'s own as the last row', () => {
    openList();
    expect(listed()).toEqual(['10', '25', '50']);
    expect(field().value).toBe('');
    expect(document.querySelector('.cdk-overlay-container .score-paginator-custom-size > .score-paginator-custom-size-label').textContent.trim()).toBe('Custom');
    expect(field().getAttribute('aria-label')).toBe('Custom items per page');
    expect(document.querySelector('.cdk-overlay-container .score-paginator-custom-size.mdc-list-item--selected')).toBeNull();
  });

  it('takes a size typed in the field: the size changes and the page event is emitted, but the list does not get a new size', async () => {
    apply('30');
    expect(paginator().pageSize).toBe(30);
    expect(fixture.componentInstance.events.at(-1).pageSize).toBe(30);
    expect(el.querySelector('.mat-mdc-paginator-range-label').textContent.trim()).toBe('1 – 30 of 97');
    expect(await shown()).toBe('30');
    openList();
    expect(listed()).toEqual(['10', '25', '50']);
  });

  it('shows the typed size in the field, as the selected row, to be edited in place', async () => {
    apply('30');
    openList();
    expect(field().value).toBe('30');
    expect(document.querySelector('.cdk-overlay-container .score-paginator-custom-size.mdc-list-item--selected')).toBeTruthy();
    field().value = '40';
    field().dispatchEvent(new KeyboardEvent('keydown', {key: 'Enter', bubbles: true}));
    fixture.detectChanges();
    expect(paginator().pageSize).toBe(40);
    expect(await shown()).toBe('40');
  });

  it('keeps the typed size in the field, not selected, when a size of the list is chosen', () => {
    apply('30');
    openList();
    (document.querySelector('.cdk-overlay-container mat-option') as HTMLElement).click();
    fixture.detectChanges();
    expect(paginator().pageSize).toBe(10);
    openList();
    expect(field().value).toBe('30');
    expect(document.querySelector('.cdk-overlay-container .score-paginator-custom-size.mdc-list-item--selected')).toBeNull();
    expect(listed()).toEqual(['10', '25', '50']);
  });

  it('uses the kept size again when Enter is pressed in the field', () => {
    apply('30');
    openList();
    (document.querySelector('.cdk-overlay-container mat-option') as HTMLElement).click();
    fixture.detectChanges();
    openList();
    field().dispatchEvent(new KeyboardEvent('keydown', {key: 'Enter', bubbles: true}));
    fixture.detectChanges();
    expect(paginator().pageSize).toBe(30);
  });

  it('shows the kept size as the text of the row, and takes it when the row is clicked outside of that', () => {
    apply('30');
    openList();
    (document.querySelector('.cdk-overlay-container mat-option') as HTMLElement).click();
    fixture.detectChanges();
    openList();
    expect(document.querySelector('.cdk-overlay-container .score-paginator-custom-size-text').textContent.trim()).toBe('30');
    expect(document.querySelector('.cdk-overlay-container .score-paginator-custom-size-empty')).toBeNull();
    // A click on the number (the area of the field) does not take it: that is for the field.
    (document.querySelector('.cdk-overlay-container .score-paginator-custom-size-value') as HTMLElement).click();
    fixture.detectChanges();
    expect(paginator().pageSize).toBe(10);
    (document.querySelector('.cdk-overlay-container .score-paginator-custom-size') as HTMLElement).click();
    fixture.detectChanges();
    expect(paginator().pageSize).toBe(30);
  });

  it('shows the field at once when there is no size of one\'s own, and a click on the row takes nothing', () => {
    openList();
    expect(document.querySelector('.cdk-overlay-container .score-paginator-custom-size-empty')).toBeTruthy();
    (document.querySelector('.cdk-overlay-container .score-paginator-custom-size') as HTMLElement).click();
    fixture.detectChanges();
    expect(paginator().pageSize).toBe(10);
    expect(fixture.componentInstance.events.length).toBe(0);
  });

  it('does not take the kept size just because the field is left', () => {
    apply('30');
    openList();
    (document.querySelector('.cdk-overlay-container mat-option') as HTMLElement).click();
    fixture.detectChanges();
    openList();
    field().dispatchEvent(new Event('blur'));
    fixture.detectChanges();
    expect(paginator().pageSize).toBe(10);
    expect(field().value).toBe('30');
  });

  it('keeps the typed size for the user: on another page, and after signing in again', () => {
    apply('30');
    expect(stored()).toBe('30');
    fixture.destroy();
    create();
    expect(paginator().pageSize).toBe(10);
    openList();
    expect(field().value).toBe('30');
  });

  it('does not keep what is a size of the list', () => {
    apply('25');
    expect(paginator().pageSize).toBe(25);
    expect(stored()).toBe('');
  });

  it('has a button at the end of the row that removes the kept size', () => {
    openList();
    expect(document.querySelector('.cdk-overlay-container .score-paginator-custom-size-remove')).toBeNull();
    field().value = '30';
    field().dispatchEvent(new KeyboardEvent('keydown', {key: 'Enter', bubbles: true}));
    fixture.detectChanges();
    openList();
    const edit = document.querySelector('.cdk-overlay-container .score-paginator-custom-size-edit');
    expect(edit.lastElementChild.classList.contains('score-paginator-custom-size-remove')).toBe(true);
    expect(edit.contains(field())).toBe(true);
  });

  it('goes back to the first size, and forgets the size, when the kept one in use is removed', () => {
    apply('30');
    openList();
    (document.querySelector('.cdk-overlay-container .score-paginator-custom-size-remove') as HTMLElement).click();
    fixture.detectChanges();
    expect(paginator().pageSize).toBe(10);
    expect(fixture.componentInstance.events.at(-1).pageSize).toBe(10);
    expect(stored()).toBe('');
    expect(field().value).toBe('');
    expect(document.querySelector('.cdk-overlay-container .score-paginator-custom-size-remove')).toBeNull();
  });

  it('removes a kept size that is not in use without changing the size', () => {
    apply('30');
    openList();
    (document.querySelector('.cdk-overlay-container mat-option') as HTMLElement).click();
    fixture.detectChanges();
    const events = fixture.componentInstance.events.length;
    openList();
    (document.querySelector('.cdk-overlay-container .score-paginator-custom-size-remove') as HTMLElement).click();
    fixture.detectChanges();
    expect(fixture.componentInstance.events.length).toBe(events);
    expect(stored()).toBe('');
    expect(field().value).toBe('');
  });

  it('takes the typed size when the field is left, too', () => {
    openList();
    field().value = '7';
    field().dispatchEvent(new Event('blur'));
    fixture.detectChanges();
    expect(paginator().pageSize).toBe(7);
  });

  it('caps a size above the limit at the limit', () => {
    apply('5000');
    expect(paginator().pageSize).toBe(MAX_PAGE_SIZE);
  });

  it('ignores what is not a positive whole number, and shows the size the table has again', () => {
    openList();
    for (const bad of ['0', '-5', '2.5', 'abc', '']) {
      field().value = bad;
      field().dispatchEvent(new KeyboardEvent('keydown', {key: 'Enter', bubbles: true}));
      fixture.detectChanges();
      expect(paginator().pageSize).toBe(10);
      openList();
      expect(field().value).toBe('');
    }
    expect(fixture.componentInstance.events.length).toBe(0);
  });

  it('does not emit again for the size it has', () => {
    apply('10');
    expect(fixture.componentInstance.events.length).toBe(0);
  });
});
