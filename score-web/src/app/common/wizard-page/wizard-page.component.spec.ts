import {ChangeDetectionStrategy, Component} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {WizardPageComponent} from './wizard-page.component';

@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [WizardPageComponent],
  template: `
    <score-wizard-page [title]="title" [subtitle]="subtitle" [toolbarClass]="toolbarClass" [spacer]="spacer">
      @if (custom) {
        <ng-container ngProjectAs="[wizardTitle]"><span class="custom-title">Custom</span></ng-container>
      }
      <div class="table-view">Table view</div>
      <ng-container ngProjectAs="[listExtra]"><span class="extra">Extra</span></ng-container>
      <ng-container ngProjectAs="[wizardActions]">
        <button class="back">Back</button>
        <button class="next">Next</button>
      </ng-container>
    </score-wizard-page>`
})
class HostComponent {
  title: string | null = 'Create BIE';
  subtitle = 'Select Business Context';
  toolbarClass = 'bg-white d-flex flex-column align-items-start';
  spacer = false;
  custom = false;
}

describe('WizardPageComponent', () => {
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

  it('shows the title with the sub-title in a column toolbar without a spacer', () => {
    detect();
    const toolbar = el('mat-toolbar')!;
    expect(toolbar.classList.contains('flex-column')).toBe(true);
    expect(toolbar.querySelector('span.title')?.textContent).toBe('Create BIE');
    expect(toolbar.querySelector('mat-card-subtitle')?.textContent).toBe('Select Business Context');
    expect(toolbar.querySelector('.flex-11-auto')).toBeNull();
  });

  it('forwards the table view and the extra content to the list shell', () => {
    detect();
    const card = el('div.context-section > mat-card')!;
    expect(card.querySelector(':scope > .table-view')).toBeTruthy();
    expect(card.lastElementChild!.classList.contains('extra')).toBe(true);
  });

  it('renders the action group below the card', () => {
    detect();
    const buttons = fixture.nativeElement.querySelectorAll(
      'div.context-section > div.row.action-group > div.col-12.actions-wrap > div.actions > button');
    expect(Array.from(buttons).map((b: any) => b.className)).toEqual(['back', 'next']);
  });

  it('accepts a projected title and no column row', () => {
    host.title = null;
    host.subtitle = '';
    host.custom = true;
    detect();
    expect(el('mat-toolbar span.title')).toBeNull();
    expect(el('mat-toolbar span.custom-title')).toBeTruthy();
  });
});
