import {of, Subject} from 'rxjs';
import {ReleaseListComponent} from './release-list.component';

describe('ReleaseListComponent', () => {
  it('should be defined', () => {
    expect(ReleaseListComponent).toBeTruthy();
  });

  it('replaces the Namespace options and clears the chosen ones when the library changes', () => {
    const component = Object.create(ReleaseListComponent.prototype) as any;
    const setFilter = vi.fn();
    component.libraryChoice = {choose: vi.fn(), library: {libraryId: 2}};
    component.namespaceService = {getNamespaceSummaries: vi.fn(() => of([{namespaceId: 7, uri: 'https://adaptstandard.org'}]))};
    component.view = () => ({setFilter});
    component.filterOptions = {set: vi.fn()};
    component.updateExtraParams = vi.fn();

    component.onLibraryChange({libraryId: 2});

    expect(component.namespaceService.getNamespaceSummaries).toHaveBeenCalledWith(2);
    expect(setFilter).toHaveBeenCalledWith('namespaces', []);
    expect(component.filterOptions.set).toHaveBeenCalledWith({namespaces: [{value: '7', label: 'https://adaptstandard.org'}]});
    expect(component.updateExtraParams).toHaveBeenCalled();
  });
  describe('when the server announces a change of a release', () => {
    function watching(chosenLibraryId: number, ready = true) {
      const messages = new Subject<{ body: string }>();
      const reload = vi.fn();
      const component = Object.create(ReleaseListComponent.prototype) as any;
      component.stompService = {watch: vi.fn(() => messages)};
      component.destroyRef = {onDestroy: vi.fn(() => () => {})};
      component.ready = () => ready;
      component.libraryChoice = {library: {libraryId: chosenLibraryId}};
      component.view = () => ({reload});
      component.watchReleaseChanges();
      const announce = (libraryId: number) => messages.next({body: JSON.stringify({libraryId, releaseId: 5})});
      return {component, announce, reload};
    }

    beforeEach(() => vi.useFakeTimers());
    afterEach(() => vi.useRealTimers());

    it('loads the list again, once for changes in quick succession', () => {
      const {component, announce, reload} = watching(2);
      announce(2);
      announce(2);
      vi.advanceTimersByTime(1000);

      expect(component.stompService.watch).toHaveBeenCalledWith('/topic/release');
      expect(reload).toHaveBeenCalledTimes(1);
    });

    it('ignores a change of another library, and takes one that does not say its library as its own', () => {
      const {announce, reload} = watching(2);
      announce(3);
      vi.advanceTimersByTime(1000);
      expect(reload).not.toHaveBeenCalled();

      announce(0);
      vi.advanceTimersByTime(1000);
      expect(reload).toHaveBeenCalledTimes(1);
    });

    it('tells the user who asked for a release that its work failed on the topic of their own account only', () => {
      const messages = new Subject<{ body: string }>();
      const component = Object.create(ReleaseListComponent.prototype) as any;
      component.stompService = {watch: vi.fn(() => messages)};
      component.destroyRef = {onDestroy: vi.fn(() => () => {})};
      component.snackBar = {open: vi.fn(() => ({onAction: () => of(undefined)}))};
      component.auth = {getUserToken: () => ({username: 'alice'})};
      component.router = {navigateByUrl: vi.fn()};
      component.watchFailuresOfMyReleases();

      expect(component.stompService.watch).toHaveBeenCalledWith('/topic/message/alice');
      messages.next({body: JSON.stringify({messageId: '3'})});
      expect(component.snackBar.open).not.toHaveBeenCalled();
      messages.next({body: JSON.stringify({event: 'releaseFailed', releaseId: 5})});
      expect(component.snackBar.open).toHaveBeenCalledTimes(1);
      expect(component.router.navigateByUrl).toHaveBeenCalledWith('/message');
    });

    it('does not load before the list is ready', () => {
      const {announce, reload} = watching(2, false);
      announce(2);
      vi.advanceTimersByTime(1000);
      expect(reload).not.toHaveBeenCalled();
    });
  });
});
