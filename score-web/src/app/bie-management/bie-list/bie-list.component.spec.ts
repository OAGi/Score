import {BieListComponent} from './bie-list.component';

describe('BieListComponent', () => {
  it('should be defined', () => {
    expect(BieListComponent).toBeTruthy();
  });

  describe('bieLink', () => {
    const link = (id: number) => BieListComponent.prototype.bieLink.call({}, {topLevelAsbiepId: id} as any);

    afterEach(() => window.history.replaceState(null, '', '/'));

    it('drops the query string the list puts in its URL', () => {
      window.history.replaceState(null, '', '/profile_bie?q=c29ydA&adv_ser=false');
      expect(link(7)).toBe(window.location.origin + '/profile_bie/7');
    });

    it('is the same without a query string or with a trailing slash', () => {
      window.history.replaceState(null, '', '/profile_bie/');
      expect(link(7)).toBe(window.location.origin + '/profile_bie/7');
    });
  });
});
