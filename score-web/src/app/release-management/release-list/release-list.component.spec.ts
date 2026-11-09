import {of} from 'rxjs';
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
});
