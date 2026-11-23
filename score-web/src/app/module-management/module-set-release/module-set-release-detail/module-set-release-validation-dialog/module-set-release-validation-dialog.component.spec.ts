import {TestBed} from '@angular/core/testing';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Subject, of} from 'rxjs';
import {HttpErrorResponse} from '@angular/common/http';
import {ModuleSetReleaseValidationDialogComponent} from './module-set-release-validation-dialog.component';
import {ModuleService} from '../../../domain/module.service';
import {Clipboard} from '@angular/cdk/clipboard';
import {MatSnackBar} from '@angular/material/snack-bar';

describe('ModuleSetReleaseValidationDialogComponent', () => {
  function create(moduleService: Partial<ModuleService>, afterClosed$: Subject<any> = new Subject<any>()):
      ModuleSetReleaseValidationDialogComponent {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        {provide: ModuleService, useValue: moduleService},
        {provide: Clipboard, useValue: {copy: () => {}}},
        {provide: MatSnackBar, useValue: {open: () => {}}},
        {provide: MatDialogRef, useValue: {close: () => {}, afterClosed: () => afterClosed$}},
        {provide: MAT_DIALOG_DATA, useValue: {moduleSetReleaseId: 1}}
      ]
    });
    return TestBed.runInInjectionContext(() => new ModuleSetReleaseValidationDialogComponent());
  }

  it('separates the invalid list from the "# Valid:" header with a line break', () => {
    const component = create({} as ModuleService);
    component.resp.results = {
      'invalid-one': 'error',
      'valid-one': 'Valid'
    } as any;

    let copied = '';
    component.clipboard = {copy: (text: string) => (copied = text)} as any;

    component.copyToClipboard();

    expect(copied).toContain('error\n# Valid: 1');
    expect(copied).not.toContain('error# Valid:');
  });

  it('stops polling the progress endpoint once the dialog is closed', () => {
    vi.useFakeTimers();
    try {
      const afterClosed$ = new Subject<any>();
      let progressCalls = 0;
      const component = create({
        validate: () => of({requestId: 'req-1'} as any),
        progressValidation: () => {
          progressCalls++;
          return new Subject<any>();
        }
      }, afterClosed$);

      component.ngOnInit();

      vi.advanceTimersByTime(2000);
      expect(progressCalls).toBe(1);

      afterClosed$.next(undefined);
      afterClosed$.complete();

      vi.advanceTimersByTime(10000);
      expect(progressCalls).toBe(1);
    } finally {
      vi.useRealTimers();
    }
  });

  it('does not throw when the first progress poll fails', () => {
    vi.useFakeTimers();
    try {
      const progress$ = new Subject<any>();
      const component = create({
        validate: () => of({requestId: 'req-1'} as any),
        progressValidation: () => progress$
      });

      component.ngOnInit();
      vi.advanceTimersByTime(2000);

      expect(() => progress$.error(new HttpErrorResponse({error: 'boom'}))).not.toThrow();
      expect(component.resp.done).toBe(true);
      expect(component.resp.results['req-1']).toBeDefined();
    } finally {
      vi.useRealTimers();
    }
  });
});
