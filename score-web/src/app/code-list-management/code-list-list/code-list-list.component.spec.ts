import {SelectionModel} from '@angular/cdk/collections';
import {CodeListListComponent} from './code-list-list.component';

/** A component with only what the toolbar rules read, so the rules are tested without the application around them. */
function componentWith(selected: any[], currentUser = 'oagis'): any {
  const component = Object.create(CodeListListComponent.prototype);
  component.selection = new SelectionModel<any>(true, selected);
  Object.defineProperty(component, 'currentUser', {value: currentUser});
  return component;
}

function entry(state: string, overrides: any = {}): any {
  return {
    codeListManifestId: Math.floor(Math.random() * 1e9),
    state,
    owner: {loginId: 'oagis'},
    release: {workingRelease: true},
    log: {revisionNum: 1},
    namespaceId: 1,
    ...overrides
  };
}

describe('CodeListListComponent', () => {
  it('should be defined', () => {
    expect(CodeListListComponent).toBeTruthy();
  });

  describe('canToolbarAction (the bulk actions follow the rules of the Core Component list)', () => {
    it('allows nothing when no row is selected', () => {
      const component = componentWith([]);
      for (const action of ['WIP', 'Draft', 'QA', 'Candidate', 'Production', 'Transfer', 'Delete', 'Restore', 'Purge']) {
        expect(component.canToolbarAction(action)).toBe(false);
      }
    });

    it('moves WIP to Draft only on the working branch and to QA only on a release', () => {
      const onWorking = componentWith([entry('WIP', {release: {workingRelease: true}})]);
      expect(onWorking.canToolbarAction('Draft')).toBe(true);
      expect(onWorking.canToolbarAction('QA')).toBe(false);

      const onRelease = componentWith([entry('WIP', {release: {workingRelease: false}})]);
      expect(onRelease.canToolbarAction('Draft')).toBe(false);
      expect(onRelease.canToolbarAction('QA')).toBe(true);
    });

    it('moves Draft to Candidate on the working branch and QA to Production on a release', () => {
      expect(componentWith([entry('Draft')]).canToolbarAction('Candidate')).toBe(true);
      expect(componentWith([entry('QA', {release: {workingRelease: false}})]).canToolbarAction('Production')).toBe(true);
      expect(componentWith([entry('QA', {release: {workingRelease: true}})]).canToolbarAction('Production')).toBe(false);
    });

    it('moves a state only for its owner', () => {
      const others = componentWith([entry('WIP', {owner: {loginId: 'someone-else'}})]);
      expect(others.canToolbarAction('Draft')).toBe(false);
      expect(others.canToolbarAction('Transfer')).toBe(false);
      expect(others.canToolbarAction('Delete')).toBe(false);
    });

    it('applies an action only when it applies to every selected row', () => {
      const mixed = componentWith([entry('WIP'), entry('Draft')]);
      expect(mixed.canToolbarAction('Draft')).toBe(false);
      expect(mixed.canToolbarAction('Candidate')).toBe(false);
      expect(componentWith([entry('WIP'), entry('WIP')]).canToolbarAction('Draft')).toBe(true);
    });

    it('deletes only a WIP code list that was never revised', () => {
      expect(componentWith([entry('WIP')]).canToolbarAction('Delete')).toBe(true);
      expect(componentWith([entry('WIP', {log: {revisionNum: 2}})]).canToolbarAction('Delete')).toBe(false);
    });

    it('restores or purges only deleted code lists', () => {
      const deleted = componentWith([entry('Deleted')]);
      expect(deleted.canToolbarAction('Restore')).toBe(true);
      expect(deleted.canToolbarAction('Purge')).toBe(true);
      expect(componentWith([entry('WIP')]).canToolbarAction('Restore')).toBe(false);
    });

    it('refuses an action it does not know', () => {
      expect(componentWith([entry('WIP')]).canToolbarAction('Unknown')).toBe(false);
    });
  });

  describe('multipleUpdateState', () => {
    it('does nothing when no row is selected', () => {
      const component = componentWith([]);
      component.snackBar = {open: vi.fn()};
      component.githubService = {getStatus: vi.fn()};
      component.multipleUpdateState('Draft');
      expect(component.snackBar.open).not.toHaveBeenCalled();
      expect(component.githubService.getStatus).not.toHaveBeenCalled();
    });

    it('stops with a message when a selected code list has no namespace', () => {
      const component = componentWith([entry('WIP', {namespaceId: null})]);
      component.snackBar = {open: vi.fn()};
      component.githubService = {getStatus: vi.fn()};
      component.multipleUpdateState('Draft');
      expect(component.snackBar.open).toHaveBeenCalledTimes(1);
      expect(component.snackBar.open.mock.calls[0][0]).toContain('Namespace is required');
      expect(component.githubService.getStatus).not.toHaveBeenCalled();
    });
  });
});
