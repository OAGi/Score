import {ChangeDetectionStrategy, Component} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {BasePageComponent} from './base-page.component';

@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [BasePageComponent],
  template: `
    <score-base-page [title]="title" [subtitle]="subtitle" [loading]="loading" [spacer]="spacer"
      [showToolbar]="showToolbar" [toolbarClass]="toolbarClass">
      @if (custom) {
        <span baseTitle class="custom-title">Custom</span>
      }
      <button baseActions class="action">Act</button>
      <p class="body">Body</p>
    </score-base-page>`
})
class HostComponent {
  title: string | null = 'My Title';
  subtitle = '';
  loading = false;
  spacer = true;
  showToolbar = true;
  toolbarClass = 'bg-white';
  custom = false;
}

describe('BasePageComponent', () => {
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

  it('renders the section, the title and the projected body and actions', () => {
    detect();
    expect(el('div.context-section')).toBeTruthy();
    expect(el('mat-toolbar span.title')?.textContent).toBe('My Title');
    expect(el('mat-toolbar button.action')).toBeTruthy();
    expect(el('div.context-section > p.body')).toBeTruthy();
  });

  it('pushes the actions to the right only when the spacer is on', () => {
    detect();
    expect(el('mat-toolbar span.flex-11-auto')).toBeTruthy();
    host.spacer = false;
    detect();
    expect(el('mat-toolbar span.flex-11-auto')).toBeNull();
  });

  it('renders no plain title when the title is null, and keeps an empty one', () => {
    host.title = null;
    host.custom = true;
    detect();
    expect(el('mat-toolbar span.title')).toBeNull();
    expect(el('mat-toolbar span.custom-title')?.textContent).toBe('Custom');

    host.title = '';
    detect();
    expect(el('mat-toolbar span.title')?.textContent).toBe('');
  });

  it('renders the subtitle only when it is set', () => {
    detect();
    expect(el('mat-card-subtitle')).toBeNull();
    host.subtitle = 'Step 1';
    detect();
    expect(el('mat-card-subtitle')?.textContent).toBe('Step 1');
  });

  it('applies the toolbar class', () => {
    host.toolbarClass = 'bg-white d-flex flex-column';
    detect();
    const toolbar = el('mat-toolbar')!;
    expect(toolbar.classList.contains('d-flex')).toBe(true);
    expect(toolbar.classList.contains('flex-column')).toBe(true);
  });

  it('hides the toolbar when showToolbar is off', () => {
    host.showToolbar = false;
    detect();
    expect(el('mat-toolbar')).toBeNull();
    expect(el('div.context-section > p.body')).toBeTruthy();
  });

  it('shows the loading spinner only while loading', () => {
    detect();
    expect(el('.loading-container mat-spinner')).toBeNull();
    host.loading = true;
    detect();
    expect(el('div.context-section > .loading-container mat-spinner')).toBeTruthy();
  });
});
