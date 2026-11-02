import {of} from 'rxjs';
import {AccDetailComponent} from './acc-detail.component';

/** Cancelling the Refactor dialog must neither reload the tree nor discard the unsaved edits. */
describe('AccDetailComponent.refactor', () => {
  const run = (dialogResult: unknown) => {
    const self = {
      dialog: {open: vi.fn(() => ({afterClosed: () => of(dialogResult)}))},
      reload: vi.fn(),
      validation: {askAboutChange: vi.fn()},
    };
    const node = {type: 'ASCCP', asccManifestId: 5, name: 'Prop', parent: {accManifestId: 1}};
    AccDetailComponent.prototype.refactor.call(self, node as never);
    return self;
  };

  it('does not reload when the dialog is cancelled', () => {
    const self = run(undefined);
    expect(self.dialog.open).toHaveBeenCalled();
    expect(self.reload).not.toHaveBeenCalled();
  });

  it('reloads after the move has been made', () => {
    const self = run({});
    expect(self.reload).toHaveBeenCalledWith('Refactored');
  });
});
