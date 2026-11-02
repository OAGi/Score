import {signal} from '@angular/core';
import {of} from 'rxjs';
import {DtListComponent} from './dt-list.component';

/** A bulk Delete, Restore or Purge on the Data Type list clears the busy flag once it succeeds. */
describe('DtListComponent.multipleUpdate', () => {
  const run = (action: string) => {
    const reload = vi.fn();
    const self = {
      busy: signal(false),
      selection: {selected: [{manifestId: 1}], clear: vi.fn()},
      confirmDialogService: {
        newConfig: () => ({data: {} as Record<string, unknown>}),
        open: () => ({afterClosed: () => of(true)}),
      },
      service: {delete: () => of(null), restore: () => of(null), purge: () => of(null)},
      snackBar: {open: vi.fn()},
      view: () => ({reload}),
    };
    DtListComponent.prototype.multipleUpdate.call(self as never, action);
    return {self, reload};
  };

  for (const [action, message] of [['Delete', 'Deleted'], ['Restore', 'Restored'], ['Purge', 'Purged']]) {
    it(`turns the busy flag off after a successful ${action}`, () => {
      const {self, reload} = run(action);
      expect(self.busy()).toBe(false);
      expect(reload).toHaveBeenCalled();
      expect(self.snackBar.open).toHaveBeenCalledWith(message, '', {duration: 3000});
    });
  }
});
