import {readFileSync} from 'node:fs';
import {join} from 'node:path';
import {EMPTY} from 'rxjs';
import {MatTableDataSource} from '@angular/material/table';
import {HomepageComponent} from './homepage.component';

describe('HomepageComponent', () => {
  it('should be defined', () => {
    expect(HomepageComponent).toBeTruthy();
  });

  it('links a Data Type to the routed Data Type page, not to core_component/dt', () => {
    const link = (type: string) => HomepageComponent.prototype.ccLink.call({}, {type, manifestId: 5} as any);
    expect(link('DT')).toBe('/data_type/5');
    expect(link('BDT')).toBe('/data_type/5');
    expect(link('ACC')).toBe('/core_component/acc/5');
  });

  it('does not disable the owner link of the Core Components by users table by the BIE branch', () => {
    const html = readFileSync(join(process.cwd(), 'src/app/basis/homepage/homepage.component.html'), 'utf8');
    const start = html.indexOf('<ng-container matColumnDef="username">');
    const end = html.indexOf('</ng-container>', start);
    const column = html.substring(start, end);
    expect(column).toContain('ccDestinationsOf(element.username)');
    expect(column).not.toContain('[class.disabled]');
  });

  it('opens the Core Component list of the users and states table on the Working release', () => {
    const html = readFileSync(join(process.cwd(), 'src/app/basis/homepage/homepage.component.html'), 'utf8');
    const start = html.indexOf('<ng-container matColumnDef="username">');
    const end = html.indexOf('<tr mat-header-row', start);
    const columns = html.substring(start, end);
    expect(columns.match(/ccDestinationsOf\(/g)?.length).toBe(5);
    expect(columns).not.toMatch(/q: q\(/);

    const self = {workingRelease: {releaseId: 7}, q: HomepageComponent.prototype.q};
    const withRelease = HomepageComponent.prototype.ccQ.call(self, [{key: 'states', value: 'WIP'}]);
    expect(atob(withRelease)).toBe('states=WIP&releaseId=7');
    self.workingRelease = undefined;
    expect(atob(HomepageComponent.prototype.ccQ.call(self, [{key: 'states', value: 'WIP'}]))).toBe('states=WIP');
  });

  it('wires the paginator, multi-user filter and sort of the Core Components by users table', () => {
    const paginator: any = {page: EMPTY, initialized: EMPTY};
    const sort: any = {sortChange: EMPTY, initialized: EMPTY};
    const self: any = {
      numberOfBiesByUsersAndStates: new MatTableDataSource<any>(),
      myRecentBIEs: new MatTableDataSource<any>(),
      myRecentCCs: new MatTableDataSource<any>(),
      numberOfCcExtsByUsersAndStates: new MatTableDataSource<any>(),
      numberOfCCsByUsersAndStates: new MatTableDataSource<any>(),
      numberOfCCsByUsersAndStatesPaginator: paginator,
      numberOfCCsByUsersAndStatesSort: sort,
    };
    HomepageComponent.prototype.ngAfterViewInit.call(self);
    expect(self.numberOfCCsByUsersAndStates.paginator).toBe(paginator);
    expect(self.numberOfCCsByUsersAndStates.sort).toBe(sort);
    const predicate = self.numberOfCCsByUsersAndStates.filterPredicate;
    expect(predicate({username: 'a'}, 'a,b')).toBe(true);
    expect(predicate({username: 'b'}, 'a,b')).toBe(true);
    expect(predicate({username: 'c'}, 'a,b')).toBe(false);
  });

  it('lists the Core Components, Code Lists and Agency ID Lists the user worked on together, the latest first, five at most', () => {
    const cc = (n: number) => ({type: 'ACC', den: 'cc' + n, manifestId: n, state: 'WIP', namespaceId: 1, lastUpdated: {when: new Date(2026, 0, n)}});
    const listCard = (title: string, day: number) => ({type: 'Code List', title, link: '/x/' + title, state: 'WIP', stateClass: 'cc-state', meta: [], details: [], updatedWhen: new Date(2026, 0, day)});
    const self: any = {
      myRecentCCEntries: [cc(1), cc(3), cc(5)],
      myRecentListCards: [listCard('codeList', 4), listCard('agencyIdList', 2), listCard('old', 0)],
      namespaceUris: new Map([[1, 'http://ns']]),
      ccLink: HomepageComponent.prototype.ccLink,
    };
    HomepageComponent.prototype['rebuildCcCards'].call(self);
    expect(self.myCcCards.map((c: any) => c.title)).toEqual(['cc5', 'codeList', 'cc3', 'agencyIdList', 'cc1']);
    self.myRecentCCEntries = [{...cc(1), type: 'DT', dtType: 'BDT'}];
    self.myRecentListCards = [];
    HomepageComponent.prototype['rebuildCcCards'].call(self);
    expect(self.myCcCards[0].type).toBe('Data Type');
    self.myRecentCCEntries = [cc(5)];
    HomepageComponent.prototype['rebuildCcCards'].call(self);
    expect(self.myCcCards[0].details).toEqual([
      {label: 'Namespace', value: 'http://ns', placeholder: 'No namespace'},
      {label: 'Definition', value: undefined, placeholder: 'No definition'}]);
  });
});
