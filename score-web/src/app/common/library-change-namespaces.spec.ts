import {signal} from '@angular/core';
import {of} from 'rxjs';
import {vi} from 'vitest';
import {AgencyIdListListComponent} from '../agency-id-list-management/agency-id-list-list/agency-id-list-list.component';
import {CcListComponent} from '../cc-management/cc-list/cc-list.component';
import {DtListComponent} from '../cc-management/dt-list/dt-list.component';
import {CodeListListComponent} from '../code-list-management/code-list-list/code-list-list.component';

/**
 * Changing the library on a list page replaces the Namespace filter options with the namespaces of the new library
 * and drops a namespace that was selected for the old library, before the list is searched again.
 */
describe.each([
  ['CodeListListComponent', CodeListListComponent],
  ['AgencyIdListListComponent', AgencyIdListListComponent],
  ['CcListComponent', CcListComponent],
  ['DtListComponent', DtListComponent],
])('%s.onLibraryChange', (_name, componentType) => {
  const newLibrary = {libraryId: 2} as any;

  function setUp() {
    const calls: string[] = [];
    const selected: Record<string, unknown> = {namespaces: ['11']};
    const view = {
      setFilter: vi.fn((key: string, value: unknown) => {
        calls.push('setFilter:' + key);
        selected[key] = value;
      }),
      onSearch: vi.fn(() => calls.push('onSearch')),
    };
    const releaseService = {getReleaseSummaryList: vi.fn(() => of([{releaseId: 5}]))};
    const namespaceService = {
      getNamespaceSummaries: vi.fn(() => of([{namespaceId: 21, uri: 'urn:new:one'}, {namespaceId: 22, uri: 'urn:new:two'}]))
    };
    const component: any = Object.create(componentType.prototype);
    Object.assign(component, {
      view: () => view,
      releaseService,
      namespaceService,
      libraryChoice: {choose: vi.fn()},
      releaseChoice: {init: vi.fn()},
      busy: signal(false),
      filterOptions: signal({states: [], namespaces: [{value: '11', label: 'urn:old'}]}),
      applyChoices: vi.fn(() => {
        calls.push('applyChoices');
        return false;
      }),
    });
    return {component, view, selected, calls, namespaceService};
  }

  it('replaces the namespace options with those of the new library', () => {
    const {component, namespaceService} = setUp();

    component.onLibraryChange(newLibrary);

    expect(namespaceService.getNamespaceSummaries).toHaveBeenCalledWith(2);
    expect(component.filterOptions().namespaces).toEqual([
      {value: '21', label: 'urn:new:one'},
      {value: '22', label: 'urn:new:two'},
    ]);
  });

  it('clears a namespace selected for the old library before searching again', () => {
    const {component, selected, calls} = setUp();

    component.onLibraryChange(newLibrary);

    expect(selected['namespaces']).toEqual([]);
    expect(calls.indexOf('setFilter:namespaces')).toBeGreaterThanOrEqual(0);
    expect(calls.indexOf('setFilter:namespaces')).toBeLessThan(calls.indexOf('onSearch'));
  });
});
