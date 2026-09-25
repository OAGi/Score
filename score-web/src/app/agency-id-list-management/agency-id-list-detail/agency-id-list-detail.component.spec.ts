import {AgencyIdListDetailComponent} from './agency-id-list-detail.component';
import {AgencyIdListDetails} from '../domain/agency-id-list';

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
});
