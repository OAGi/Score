import {TestBed} from '@angular/core/testing';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {readFileSync} from 'node:fs';
import {join} from 'node:path';
import {AuthService} from '../../authentication/auth.service';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {BusinessTermWhereUsedDialogComponent} from '../business-term-where-used-dialog/business-term-where-used-dialog.component';
import {BusinessTermService} from '../domain/business-term.service';
import {BusinessTermListComponent} from './business-term-list.component';

describe('BusinessTermListComponent', () => {
  it('should be defined', () => {
    expect(BusinessTermListComponent).toBeTruthy();
  });

  describe('Where Used', () => {
    let open: ReturnType<typeof vi.fn>;
    let list: BusinessTermListComponent;

    beforeEach(() => {
      open = vi.fn();
      TestBed.configureTestingModule({
        providers: [
          {provide: AuthService, useValue: {getUserToken: () => ({})}},
          {provide: BusinessTermService, useValue: {}},
          {provide: MatDialog, useValue: {open}},
          {provide: ConfirmDialogService, useValue: {}},
          {provide: MatSnackBar, useValue: {open: vi.fn()}},
        ]
      });
      list = TestBed.runInInjectionContext(() => new BusinessTermListComponent());
    });

    afterEach(() => TestBed.resetTestingModule());

    it('opens the dialog with the id and the name of the term of the row', () => {
      list.openWhereUsedDialog({businessTermId: 12, businessTerm: 'Order Number'} as any);

      expect(open).toHaveBeenCalledTimes(1);
      const [component, config] = open.mock.calls[0];
      expect(component).toBe(BusinessTermWhereUsedDialogComponent);
      expect(config.data).toEqual({businessTermId: 12, businessTerm: 'Order Number'});
    });

    it('opens it as wide as the tree needs, and no wider than the window, with no focus taken from the title', () => {
      list.openWhereUsedDialog({businessTermId: 1, businessTerm: 't'} as any);

      const config = open.mock.calls[0][1];
      expect(config.width).toBe('720px');
      expect(config.maxWidth).toBe('90vw');
      expect(config.maxHeight).toBe('90vh');
      expect(config.autoFocus).toBe(false);
    });

    it('has the row menu at the end of the row, with Where Used as its item', () => {
      expect(list.columns.column('more')).toBeTruthy();
      const html = readFileSync(join(process.cwd(),
        'src/app/business-term-management/business-term-list/business-term-list.component.html'), 'utf8');
      expect(html).toContain('(click)="openWhereUsedDialog(contextMenuItem)"');
      expect(html).toContain('Where Used');
    });
  });
});
