import {NO_ERRORS_SCHEMA} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {FormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatCardModule} from '@angular/material/card';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {CodeListValueDialogComponent} from './code-list-value-dialog.component';

describe('CodeListValueDialogComponent', () => {
  const render = (value: string, meaning: string) => {
    TestBed.configureTestingModule({
      declarations: [CodeListValueDialogComponent],
      imports: [FormsModule, MatDialogModule, MatCardModule, MatCheckboxModule, MatFormFieldModule, MatInputModule,
        NoopAnimationsModule],
      providers: [
        {provide: MatDialogRef, useValue: {close: () => undefined}},
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            codeListValue: {value, meaning, definition: {}, deprecated: false},
            lastRevisionValue: undefined,
            isEditable: true
          }
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    });
    const fixture = TestBed.createComponent(CodeListValueDialogComponent);
    fixture.detectChanges();
    return fixture;
  };

  const input = (fixture: ReturnType<typeof render>, label: string): HTMLInputElement => {
    const field = Array.from(fixture.nativeElement.querySelectorAll('mat-form-field') as NodeListOf<HTMLElement>)
      .find(e => e.querySelector('mat-label')?.textContent.trim() === label);
    return field.querySelector('input') as HTMLInputElement;
  };

  it('limits the Code to the 255 characters of its column', () => {
    expect(input(render('KR1', 'meaning'), 'Code').getAttribute('maxlength')).toBe('255');
  });

  it('does not limit the Meaning: its column is text, and the old 100-character limit was not a data limit', () => {
    expect(input(render('KR1', 'meaning'), 'Meaning').hasAttribute('maxlength')).toBe(false);
  });

  it('accepts a Meaning and a Code made of 3-byte characters beyond the old byte limit of 255', () => {
    const fixture = render('KR1', '가'.repeat(100));
    const meaning = input(fixture, 'Meaning');
    meaning.value = '가'.repeat(300);
    meaning.dispatchEvent(new Event('input'));
    const code = input(fixture, 'Code');
    code.value = '나'.repeat(255);
    code.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    expect(fixture.componentInstance.codeListValue.meaning).toBe('가'.repeat(300));
    expect(fixture.componentInstance.codeListValue.value).toBe('나'.repeat(255));
    expect(fixture.componentInstance.isDisabled()).toBe(false);
  });
});
