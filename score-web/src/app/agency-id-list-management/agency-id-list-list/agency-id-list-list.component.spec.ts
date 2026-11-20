import {AgencyIdListListComponent} from './agency-id-list-list.component';

describe('AgencyIdListListComponent', () => {
  it('should be defined', () => {
    expect(AgencyIdListListComponent).toBeTruthy();
  });

  describe('showCreateAgencyIdListBtn', () => {
    const show = (self: any): boolean =>
      Object.getOwnPropertyDescriptor(AgencyIdListListComponent.prototype, 'showCreateAgencyIdListBtn')!.get!.call(self);
    const developer = (workingRelease: boolean, exists: boolean) => ({
      auth: {getUserToken: () => ({roles: ['developer']})},
      releaseChoice: {release: {workingRelease}},
      branchHasAgencyIdList: () => exists,
      // The rows of a page that a filter has emptied do not tell whether the branch has a list.
      view: () => ({dataSource: {data: []}})
    });

    it('is hidden for a developer when the branch has a list, even if a filter shows no rows', () => {
      expect(show(developer(true, true))).toBe(false);
    });

    it('is shown for a developer when the branch has no list', () => {
      expect(show(developer(true, false))).toBe(true);
    });

    it('is hidden for a developer outside the working branch', () => {
      expect(show(developer(false, false))).toBe(false);
    });
  });

  describe('isEditable', () => {
    const isEditable = (currentUser: string, item: any): boolean =>
      AgencyIdListListComponent.prototype.isEditable.call({currentUser}, item);

    it('compares the owner login ID, not the display name, with the current user', () => {
      const owner = {loginId: 'oagis', username: 'Open Applications Group Developer'};
      expect(isEditable('oagis', {owner, state: 'WIP'})).toBe(true);
      expect(isEditable('someone', {owner, state: 'WIP'})).toBe(false);
    });

    it('is false unless the Agency ID List is WIP', () => {
      const owner = {loginId: 'oagis', username: 'Open Applications Group Developer'};
      expect(isEditable('oagis', {owner, state: 'Published'})).toBe(false);
    });
  });
});
