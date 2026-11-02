import {of} from 'rxjs';
import {BdtDetailComponent} from './bdt-detail.component';

vi.mock('../domain/cc-flat-tree', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../domain/cc-flat-tree')>();
  class FakeDataSource {
    data: unknown[] = [{expanded: false, manifestId: 1, access: undefined, state: undefined, reset: () => {}}];
    hideProhibited = true;
    init() {}
    isExpanded() { return false; }
  }
  return {
    ...actual,
    CcFlatNodeDatabase: class {},
    CcFlatNodeDataSource: FakeDataSource,
    CcFlatNodeDataSourceSearcher: class {},
  };
});

/** Reloading the Data Type page keeps the user's current "Hide Prohibited SCs" choice. */
describe('BdtDetailComponent.reload', () => {
  const reload = (hideProhibited: boolean) => {
    const self = {
      manifestId: 1,
      selectedNode: undefined,
      isUpdating: false,
      dataSource: {data: [], hideProhibited},
      rootNode: {},
      service: {
        getDtDetails: () => of({release: {workingRelease: true}, access: 'CanEdit', state: 'WIP'}),
        getGraphNode: () => of({}),
      },
      validation: {markStale: () => {}},
      cardinality: {restore: () => {}},
      auth: {getUserToken: () => ({})},
      seedRootDetail: () => {},
      onClick: () => {},
    };
    BdtDetailComponent.prototype.reload.call(self as never);
    return self;
  };

  it('keeps Hide Prohibited SCs ticked when it was ticked', () => {
    expect(reload(true).dataSource.hideProhibited).toBe(true);
  });

  it('keeps Hide Prohibited SCs unticked when it was unticked', () => {
    expect(reload(false).dataSource.hideProhibited).toBe(false);
  });
});
