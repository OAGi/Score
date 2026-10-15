import {ChangeDetectionStrategy, Component} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {DetailCardComponent} from './detail-card.component';

@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [DetailCardComponent],
  template: `
    <score-detail-card [heading]="heading" [cardClass]="cardClass">
      <input class="field"/>
      <ng-container ngProjectAs="[cardActions]">
        <mat-card-actions class="card-actions">Actions</mat-card-actions>
      </ng-container>
    </score-detail-card>`
})
class HostComponent {
  heading = '';
  cardClass = '';
}

describe('DetailCardComponent', () => {
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

  it('wraps the content in a padded card', () => {
    detect();
    expect(el('div.p-3 > mat-card > mat-card-content.mat-card-container > input.field')).toBeTruthy();
    expect(el('div.p-3 > mat-toolbar')).toBeNull();
  });

  it('renders the heading above the card only when it is set', () => {
    host.heading = 'Values';
    detect();
    expect(el('div.p-3 > mat-toolbar span.title')?.textContent).toBe('Values');
  });

  it('adds the card class to the card', () => {
    host.cardClass = 'mb-2';
    detect();
    expect(el('mat-card')!.classList.contains('mb-2')).toBe(true);
  });

  it('renders the card actions inside the card, after the content', () => {
    detect();
    const card = el('mat-card')!;
    expect(card.querySelector(':scope > mat-card-actions.card-actions')).toBeTruthy();
    expect(card.lastElementChild!.classList.contains('card-actions')).toBe(true);
  });
});
