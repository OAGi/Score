import {AgencyIdListDetailComponent} from './agency-id-list-detail.component';
import {AgencyIdListDetails, AgencyIdListValueDetails} from '../domain/agency-id-list';

describe('AgencyIdListDetailComponent', () => {
  it('should be defined', () => {
    expect(AgencyIdListDetailComponent).toBeTruthy();
  });

  it('blocks Agency ID List update and state changes when Namespace is missing', () => {
    const component = Object.create(AgencyIdListDetailComponent.prototype) as AgencyIdListDetailComponent;
    const open = vi.fn();
    (component as any).snackBar = {open};
    component.isUpdating = false;
    component.hashCode = 'dirty';
    component.agencyIdList = new AgencyIdListDetails();
    component.agencyIdList.state = 'WIP';
    component.agencyIdList.access = 'CanEdit';
    component.agencyIdList.name = 'Custom Agency ID List';
    component.agencyIdList.listId = 'custom-list';
    component.agencyIdList.versionId = '1';
    component.agencyIdList.definition.content = 'definition';

    component.update();
    expect(open).toHaveBeenLastCalledWith('Namespace is required', '', {duration: 3000});

    open.mockClear();
    component.agencyIdList.state = 'QA';
    component.updateState('WIP');
    expect(open).toHaveBeenLastCalledWith('Namespace is required', '', {duration: 3000});
  });

  describe('isAvailable', () => {
    const create = (agencyValueManifestId?: number) => {
      const component = Object.create(AgencyIdListDetailComponent.prototype) as AgencyIdListDetailComponent;
      component.agencyIdList = new AgencyIdListDetails();
      component.agencyIdList.state = 'WIP';
      component.agencyIdList.agencyIdListValue.agencyIdListValueManifestId = agencyValueManifestId as any;
      return component;
    };

    it('lets a new value without a manifest id be removed when the list has no Agency ID List Value', () => {
      const value = new AgencyIdListValueDetails();
      expect(create().isAvailable(value)).toBe(true);
    });

    it('keeps the value that is the Agency ID List Value of the list from being removed', () => {
      const value = new AgencyIdListValueDetails();
      value.agencyIdListValueManifestId = 1;
      expect(create(1).isAvailable(value)).toBe(false);
    });
  });
});
