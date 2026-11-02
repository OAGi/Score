import {signal} from '@angular/core';
import {of} from 'rxjs';
import {CcListComponent} from './cc-list.component';

/** Creating an OAGi BOD component clears the busy flag once the request succeeds. */
describe('CcListComponent.createBOD', () => {
  it('turns the busy flag off after a successful create', () => {
    const reload = vi.fn();
    const self = {
      busy: signal(false),
      releaseChoice: {release: {workingRelease: true, releaseId: 1}},
      libraryChoice: {library: {libraryId: 1}},
      dialog: {open: () => ({afterClosed: () => of({verbManifestIdList: [1], nounManifestIdList: [2]})})},
      nodeService: {createBOD: () => of({manifestIdList: [10]})},
      snackBar: {open: vi.fn()},
      view: () => ({reload}),
    };
    CcListComponent.prototype.createBOD.call(self as never);
    expect(self.busy()).toBe(false);
    expect(reload).toHaveBeenCalled();
    expect(self.snackBar.open).toHaveBeenCalledWith('A new BOD created.', '', {duration: 3000});
  });

  it('turns the busy flag off when the dialog is cancelled', () => {
    const self = {
      busy: signal(false),
      releaseChoice: {release: {workingRelease: true, releaseId: 1}},
      libraryChoice: {library: {libraryId: 1}},
      dialog: {open: () => ({afterClosed: () => of(undefined)})},
    };
    CcListComponent.prototype.createBOD.call(self as never);
    expect(self.busy()).toBe(false);
  });
});
