import {AgencyIdListListComponent} from './agency-id-list-list.component';

describe('AgencyIdListListComponent', () => {
  it('should be defined', () => {
    expect(AgencyIdListListComponent).toBeTruthy();
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
