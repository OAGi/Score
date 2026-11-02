import {ChangeDetectionStrategy, Component} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {DetailPageComponent} from './detail-page.component';

@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [DetailPageComponent],
  template: `
    <score-detail-page [title]="title" [subtitle]="subtitle" [loading]="loading" [showActions]="showActions"
      [actionsColumn]="actionsColumn" [actionsWrapper]="actionsWrapper" [spacer]="spacer">
      @if (customTitle) {
        <span detailTitle class="custom-title">Custom</span>
      }
      <button detailToolbar class="toolbar-action">Toolbar</button>
      <p class="body">Body</p>
      <button detailActions class="update">Update</button>
      <p detailAfterActions class="after">After</p>
    </score-detail-page>`
})
class HostComponent {
  title: string | null = 'Edit';
  subtitle = '';
  loading = false;
  showActions = true;
  actionsColumn = 'col-12';
  actionsWrapper = true;
  spacer = true;
  customTitle = false;
}

describe('DetailPageComponent', () => {
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

  it('renders the title toolbar, the body, the action group and the content after it in order', () => {
    detect();
    expect(el('mat-toolbar span.title')?.textContent).toBe('Edit');
    expect(el('mat-toolbar button.toolbar-action')).toBeTruthy();
    const section = el('div.context-section')!;
    const order = Array.from(section.children).map(c => c.className.split(' ')[0] || c.tagName.toLowerCase());
    expect(order.indexOf('body')).toBeLessThan(order.indexOf('row'));
    expect(order.indexOf('row')).toBeLessThan(order.indexOf('after'));
  });

  it('puts the action buttons into the .actions wrapper of the column', () => {
    detect();
    expect(el('div.row.action-group > div.col-12.actions-wrap > div.actions > button.update')).toBeTruthy();
  });

  it('uses the given action column', () => {
    host.actionsColumn = 'col-6';
    detect();
    expect(el('div.row.action-group > div.col-6.actions-wrap')).toBeTruthy();
  });

  it('omits the .actions wrapper when actionsWrapper is off', () => {
    host.actionsWrapper = false;
    detect();
    expect(el('div.actions')).toBeNull();
    expect(el('div.actions-wrap > button.update')).toBeTruthy();
  });

  it('omits the action group when showActions is off', () => {
    host.showActions = false;
    detect();
    expect(el('div.action-group')).toBeNull();
    expect(el('button.update')).toBeNull();
  });

  it('replaces the plain title with a projected one', () => {
    host.title = null;
    host.customTitle = true;
    detect();
    expect(el('mat-toolbar span.title')).toBeNull();
    expect(el('mat-toolbar span.custom-title')).toBeTruthy();
  });

  it('shows the loading spinner and supports a subtitle without a spacer', () => {
    host.loading = true;
    host.subtitle = 'Sub';
    host.spacer = false;
    detect();
    expect(el('.loading-container mat-spinner')).toBeTruthy();
    expect(el('mat-card-subtitle')?.textContent).toBe('Sub');
    expect(el('mat-toolbar span.flex-11-auto')).toBeNull();
  });
});
