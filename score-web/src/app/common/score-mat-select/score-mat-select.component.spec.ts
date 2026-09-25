import {ChangeDetectionStrategy, Component} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {FormControl, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {NgxMatSelectSearchModule} from 'ngx-mat-select-search';
import {ScoreMatSelectComponent} from './score-mat-select.component';

@Component({
  changeDetection: ChangeDetectionStrategy.Default,
  imports: [FormsModule, ReactiveFormsModule, MatFormFieldModule, MatSelectModule,
    ScoreMatSelectComponent, NgxMatSelectSearchModule],
  template: `
    <mat-form-field>
      <mat-label>Value</mat-label>
      <score-mat-select [(ngModel)]="value" [disabled]="disabled" [multiple]="multiple"
        [required]="true" [clearable]="clearable" (selectionChange)="changes.push($event.value)">
        <mat-option><ngx-mat-select-search [formControl]="search" /></mat-option>
        <mat-option [value]="0">Zero</mat-option>
        <mat-option [value]="1">One</mat-option>
      </score-mat-select>
    </mat-form-field>
    <mat-form-field>
      <mat-label>Reactive value</mat-label>
      <score-mat-select [formControl]="control">
        <mat-option [value]="1">One</mat-option>
        <mat-select-trigger>Custom display</mat-select-trigger>
      </score-mat-select>
    </mat-form-field>`
})
class HostComponent {
  value: number | number[] | null = 0;
  disabled = false;
  multiple = false;
  clearable = true;
  changes: unknown[] = [];
  search = new FormControl('');
  control = new FormControl(1);
}

describe('ScoreMatSelectComponent', () => {
  let fixture: ComponentFixture<HostComponent>;
  let select: ScoreMatSelectComponent;
  let host: HostComponent;

  async function render(multiple = false, disabled = false, clearable = true) {
    await TestBed.configureTestingModule({imports: [HostComponent, NoopAnimationsModule]}).compileComponents();
    fixture = TestBed.createComponent(HostComponent);
    host = fixture.componentInstance;
    host.multiple = multiple;
    host.disabled = disabled;
    host.clearable = clearable;
    host.value = multiple ? [0, 1] : 0;
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
    select = fixture.debugElement.query(By.directive(ScoreMatSelectComponent)).componentInstance;
  }

  it('clears a falsy selected value through ngModel once and preserves form validation', async () => {
    await render();
    const button = fixture.nativeElement.querySelector('score-mat-select').querySelector('button');
    expect(button).not.toBeNull();
    button.click();
    fixture.detectChanges();
    await fixture.whenStable();
    expect(host.value).toBeNull();
    expect(host.changes).toEqual([null]);
    expect(select.ngControl?.touched).toBe(true);
    expect(select.ngControl?.dirty).toBe(true);
    expect(select.ngControl?.hasError('required')).toBe(true);
    expect(select.panelOpen).toBe(false);
    expect(fixture.nativeElement.querySelector('score-mat-select').querySelector('button')).toBeNull();
  });

  it('hides the clear action and blocks clearing when disabled', async () => {
    await render(false, true);
    expect(fixture.nativeElement.querySelector('score-mat-select').querySelector('button')).toBeNull();
    select.clearSelection(new MouseEvent('click'));
    expect(host.value).toBe(0);
    expect(host.changes).toEqual([]);
    expect(select.ngControl?.touched).toBe(false);
    expect(select.ngControl?.dirty).toBe(false);
  });

  it('hides and restores the clear action when a reactive control is disabled and enabled', async () => {
    await render();
    const element = fixture.nativeElement.querySelectorAll('score-mat-select')[1];
    const reactiveSelect = fixture.debugElement.queryAll(By.directive(ScoreMatSelectComponent))[1].componentInstance;
    expect(element.querySelector('button')).not.toBeNull();
    host.control.disable();
    fixture.detectChanges();
    expect(element.querySelector('button')).toBeNull();
    reactiveSelect.clearSelection(new MouseEvent('click'));
    expect(host.control.value).toBe(1);
    expect(host.control.pristine).toBe(true);
    expect(host.control.untouched).toBe(true);
    host.control.enable();
    fixture.detectChanges();
    expect(element.querySelector('button')).not.toBeNull();
    element.querySelector('button').click();
    expect(host.control.value).toBeNull();
  });

  it('allows opting out of the clear action', async () => {
    await render(false, false, false);
    expect(fixture.nativeElement.querySelector('score-mat-select').querySelector('button')).toBeNull();
  });

  it('clears multiple selection to an empty array', async () => {
    await render(true);
    fixture.nativeElement.querySelector('score-mat-select').querySelector('button').click();
    fixture.detectChanges();
    await fixture.whenStable();
    expect(host.value).toEqual([]);
    expect(host.changes).toEqual([[]]);
    expect(select.selected).toEqual([]);
  });

  it('supports reactive forms, custom triggers, and programmatic reset', async () => {
    await render();
    const reactiveSelect = fixture.nativeElement.querySelectorAll('score-mat-select')[1];
    expect(reactiveSelect.textContent).toContain('Custom display');
    reactiveSelect.querySelector('button').click();
    fixture.detectChanges();
    expect(host.control.value).toBeNull();
    expect(host.control.touched).toBe(true);
    host.control.setValue(1);
    fixture.detectChanges();
    expect(reactiveSelect.querySelector('button')).not.toBeNull();
    host.control.reset();
    fixture.detectChanges();
    expect(reactiveSelect.querySelector('button')).toBeNull();
  });
});
