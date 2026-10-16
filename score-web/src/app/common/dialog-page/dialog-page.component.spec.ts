import {ChangeDetectionStrategy, Component} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MatDialogRef} from '@angular/material/dialog';
import {DialogPageComponent} from './dialog-page.component';

@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [DialogPageComponent],
  template: `
    <score-dialog-page [title]="title" [closable]="closable">
      @if (customTitle) {
        <span dialogTitle class="custom-title">Library</span>
      }
      <div class="body">Body</div>
      <button dialogActions class="ok">OK</button>
    </score-dialog-page>`
})
class HostComponent {
  title: string | null = 'Select';
  closable = true;
  customTitle = false;
}

describe('DialogPageComponent', () => {
  let fixture: ComponentFixture<HostComponent>;
  let host: HostComponent;
  const el = (selector: string): HTMLElement | null => fixture.nativeElement.querySelector(selector);
  const detect = (): void => {
    fixture.changeDetectorRef.markForCheck();
    fixture.detectChanges();
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HostComponent],
      providers: [{provide: MatDialogRef, useValue: {close: () => undefined}}]
    });
    fixture = TestBed.createComponent(HostComponent);
    host = fixture.componentInstance;
    detect();
  });

  it('shows the title, the body and the actions', () => {
    expect(el('.dialog-title')?.textContent).toContain('Select');
    expect(el('.dialog-body .body')).toBeTruthy();
    expect(el('.dialog-actions .ok')).toBeTruthy();
  });

  it('shows the close button unless it is not closable', () => {
    expect(el('.dialog-close')).toBeTruthy();
    host.closable = false;
    detect();
    expect(el('.dialog-close')).toBeNull();
  });

  it('projects a custom title', () => {
    host.title = null;
    host.customTitle = true;
    detect();
    expect(el('.dialog-title .custom-title')).toBeTruthy();
  });
});
