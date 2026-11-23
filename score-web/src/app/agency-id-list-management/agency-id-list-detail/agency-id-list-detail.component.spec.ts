import {of} from 'rxjs';
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

  describe('openDialog', () => {
    const create = (result: AgencyIdListValueDetails, existing: AgencyIdListValueDetails[]) => {
      const component = Object.create(AgencyIdListDetailComponent.prototype) as AgencyIdListDetailComponent;
      const open = vi.fn();
      (component as any).snackBar = {open};
      (component as any).auth = {getUserToken: () => ({roles: []})};
      (component as any).dialog = {open: () => ({afterClosed: () => of(result)})};
      component.agencyIdList = new AgencyIdListDetails();
      component.values = existing;
      component.isEditable = () => true;
      component._updateDataSource = vi.fn() as any;
      return {component, open};
    };

    const value = (guid: string, text: string, developerDefault: boolean) => {
      const v = new AgencyIdListValueDetails();
      v.guid = guid;
      v.value = text;
      v.developerDefault = developerDefault;
      return v;
    };

    it('keeps the defaults of other values when the added value is a duplicate', () => {
      const existing = [value('a', 'A', true), value('b', 'B', false)];
      const {component, open} = create(value(undefined as any, 'B', true), existing);
      component.openDialog();
      expect(open).toHaveBeenCalled();
      expect(existing[0].developerDefault).toBe(true);
    });

    it('clears the defaults of other values when the added value is accepted', () => {
      const existing = [value('a', 'A', true)];
      const {component} = create(value(undefined as any, 'C', true), existing);
      component.openDialog();
      expect(existing[0].developerDefault).toBe(false);
    });
  });
});
