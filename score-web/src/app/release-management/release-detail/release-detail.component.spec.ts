import {of, Subject} from 'rxjs';
import {ReleaseDetailComponent} from './release-detail.component';

describe('ReleaseDetailComponent', () => {
  it('should be defined', () => {
    expect(ReleaseDetailComponent).toBeTruthy();
  });

  function component(state: string, roles = ['developer']) {
    const c = Object.create(ReleaseDetailComponent.prototype) as any;
    c.releaseDetail = {releaseId: 7, state};
    c.auth = {getUserToken: () => ({roles})};
    c.snackBar = {open: vi.fn()};
    c.isLoading = false;
    c.load = vi.fn();
    return c;
  }

  it('is in progress while it is being drafted or published, and says what recovering does', () => {
    expect(component('Initialized').inProgress).toBe(false);
    expect(component('Draft').inProgress).toBe(false);
    const drafting = component('Drafting');
    expect(drafting.inProgress).toBe(true);
    expect(drafting.inProgressMessage).toContain('\'Initialized\'');
    expect(component('Publishing').inProgressMessage).toContain('\'Draft\'');
    const initializing = component('Initializing');
    expect(initializing.inProgress).toBe(true);
    expect(initializing.inProgressMessage).toContain('taken back to \'Initialized\'');
    expect(initializing.inProgressMessage).toContain('goes back to \'Draft\'');
    expect(component('Processing').inProgress).toBe(true);
  });

  describe('whether it can be given back', () => {
    function inProgress(state: string, busy: boolean) {
      const c = component(state);
      c.releaseDetail.busy = busy;
      return c;
    }

    it('is offered when it is in the middle of its work and nothing is doing it', () => {
      for (const state of ['Drafting', 'Publishing', 'Initializing']) {
        expect(inProgress(state, false).recoverable).toBe(true);
      }
    });

    it('is not offered while the work for it is going on, and says to wait', () => {
      for (const state of ['Drafting', 'Publishing', 'Initializing']) {
        const c = inProgress(state, true);
        expect(c.recoverable).toBe(false);
        expect(c.inProgressMessage).toContain('Wait until it is done.');
        expect(c.inProgressMessage).not.toContain('recover it');
      }
    });

    it('is offered with what recovering does when nothing is doing the work', () => {
      const drafting = inProgress('Drafting', false);
      expect(drafting.inProgressMessage).toContain('nothing is doing it');
      expect(drafting.inProgressMessage).toContain('recover it');
    });

    it('is not offered for a release that is not in the middle of its work, or was left in \'Processing\'', () => {
      for (const state of ['Initialized', 'Draft', 'Published', 'Processing']) {
        expect(inProgress(state, false).recoverable).toBe(false);
      }
    });
  });

  it('is not edited while it is in progress', () => {
    expect(component('Drafting').isDisabled()).toBe(true);
    expect(component('Initialized').isDisabled()).toBe(false);
  });

  describe('recover', () => {
    function recovering(confirmed: boolean) {
      const c = component('Drafting');
      const config = {data: {} as any};
      c.confirmDialogService = {newConfig: () => config, open: () => ({afterClosed: () => of(confirmed)})};
      c.service = {recover: vi.fn(() => of(undefined))};
      return {c, config};
    }

    it('gives the release back once the user has confirmed, and loads it again', () => {
      const {c, config} = recovering(true);
      c.recover();

      expect(config.data.action).toBe('Recover');
      expect(c.service.recover).toHaveBeenCalledWith(7);
      expect(c.snackBar.open).toHaveBeenCalledWith('Recovered', '', {duration: 3000});
      expect(c.load).toHaveBeenCalled();
    });

    it('does nothing when the user does not confirm', () => {
      const {c} = recovering(false);
      c.recover();

      expect(c.service.recover).not.toHaveBeenCalled();
    });
  });

  describe('when the server announces a change of a release', () => {
    function watching(state: string) {
      const messages = new Subject<{ body: string }>();
      const c = component(state);
      c.stompService = {watch: vi.fn(() => messages)};
      c.destroyRef = {onDestroy: vi.fn(() => () => {})};
      Object.defineProperty(c, 'isChanged', {value: false, writable: true});
      c.watchChangesOfThisRelease();
      return {c, announce: (releaseId: number) => messages.next({body: JSON.stringify({libraryId: 1, releaseId})})};
    }

    it('loads the release again when it is the one shown', () => {
      const {c, announce} = watching('Drafting');
      announce(7);
      expect(c.load).toHaveBeenCalledTimes(1);
    });

    it('ignores the change of another release', () => {
      const {c, announce} = watching('Drafting');
      announce(8);
      expect(c.load).not.toHaveBeenCalled();
    });

    it('does not replace what the user has edited, unless the release was being processed', () => {
      const edited = watching('Draft');
      edited.c.isChanged = true;
      edited.announce(7);
      expect(edited.c.load).not.toHaveBeenCalled();

      const processing = watching('Publishing');
      processing.c.isChanged = true;
      processing.announce(7);
      expect(processing.c.load).toHaveBeenCalledTimes(1);
    });
  });
});
