import {Component} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {convertToParamMap, Router} from '@angular/router';
import {RouterTestingHarness} from '@angular/router/testing';
import {provideRouter} from '@angular/router';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {of} from 'rxjs';
import {BasisModule} from '../basis.module';
import {HomepageComponent} from './homepage.component';
import {AuthService} from '../../authentication/auth.service';
import {WebPageInfoService} from '../basis.service';
import {CcListService} from '../../cc-management/cc-list/domain/cc-list.service';
import {BieListService} from '../../bie-management/bie-list/domain/bie-list.service';
import {CodeListService} from '../../code-list-management/domain/code-list.service';
import {AgencyIdListService} from '../../agency-id-list-management/domain/agency-id-list.service';
import {NamespaceService} from '../../namespace-management/domain/namespace.service';
import {ReleaseService} from '../../release-management/domain/release.service';
import {LibraryService} from '../../library-management/domain/library.service';
import {CcListRequest} from '../../cc-management/cc-list/domain/cc-list';
import {base64Decode} from '../../common/utility';

@Component({standalone: true, template: ''})
class CoreComponentListStub {
}

/** The Working release the Home page counts on, and the summary the backend returns for it (type -> user -> state -> count). */
const WORKING_RELEASE = {releaseId: 7, releaseNum: 'Working', workingRelease: true, state: 'Initialized'};
const SUMMARY = {
  numberOfTotalCcByStates: {},
  numberOfMyCcByStates: {},
  ccByUsersAndStates: {},
  myRecentCCs: [],
  ccByTypesUsersAndStates: {
    ACC: {oagis: {WIP: 15}, alice: {WIP: 1, Draft: 2}, bob: {Candidate: 3}},
    ASCCP: {oagis: {WIP: 2}},
    BCCP: {oagis: {WIP: 4, Candidate: 1}},
    BDT: {oagis: {WIP: 4, Draft: 1}, carol: {Draft: 5}},
  },
};
const EXPECTED_ROWS: {[username: string]: {WIP: number, Draft: number, Candidate: number, total: number}} = {
  oagis: {WIP: 25, Draft: 1, Candidate: 1, total: 27},
  alice: {WIP: 1, Draft: 2, Candidate: 0, total: 3},
  bob: {WIP: 0, Draft: 0, Candidate: 3, total: 3},
  carol: {WIP: 0, Draft: 5, Candidate: 0, total: 5},
};

describe('Home "Core components by users and states" links', () => {
  let fixture: ComponentFixture<HomepageComponent>;
  let router: Router;

  async function render() {
    const stateColor = {background: '#000', font: '#fff'};
    TestBed.configureTestingModule({
      imports: [BasisModule, NoopAnimationsModule],
      providers: [
        provideRouter(['core_component', 'data_type', 'code_list', 'agency_id_list'].map(path => ({path, component: CoreComponentListStub}))),
        {provide: AuthService, useValue: {getUserToken: () => ({username: 'oagis', roles: ['developer'], tenant: {enabled: false}})}},
        {provide: WebPageInfoService, useValue: {getComponentStateColorSet: () => stateColor, getReleaseStateColorSet: () => stateColor}},
        {provide: LibraryService, useValue: {getLibrarySummaryList: () => of([{libraryId: 1, name: 'connectSpec', isDefault: true}])}},
        {provide: CodeListService, useValue: {getCodeListListPage: () => of({list: []})}},
        {provide: AgencyIdListService, useValue: {getAgencyIdListListPage: () => of({list: []})}},
        {provide: NamespaceService, useValue: {getNamespaceSummaries: () => of([])}},
        {provide: ReleaseService, useValue: {getReleaseSummaryList: () => of([WORKING_RELEASE]), getWorkingRelease: () => of(WORKING_RELEASE)}},
        {provide: CcListService, useValue: {getSummaryCcList: () => of(SUMMARY), getSummaryCcExtList: () => of({numberOfTotalCcExtByStates: {}, numberOfMyCcExtByStates: {}, ccExtByUsersAndStates: {}, myExtensionsUnusedInBIEs: []})}},
        {provide: BieListService, useValue: {getSummaryBieList: () => of({numberOfTotalBieByStates: {}, numberOfMyBieByStates: {}, bieByUsersAndStates: {}, myRecentBIEs: []})}},
      ],
    });
    router = TestBed.inject(Router);
    fixture = TestBed.createComponent(HomepageComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  /** The cells of the users and states table: username -> column -> link element. */
  function table(): {[username: string]: {[column: string]: HTMLAnchorElement}} {
    const columns = ['username', 'WIP', 'Draft', 'Candidate', 'total'];
    const table = fixture.nativeElement.querySelector('table[mat-table]:has(th.mat-column-Candidate)') as HTMLElement;
    const rows: {[username: string]: {[column: string]: HTMLAnchorElement}} = {};
    table.querySelectorAll('tr[mat-row]').forEach(tr => {
      const links = Array.from(tr.querySelectorAll('a')) as HTMLAnchorElement[];
      rows[links[0].textContent.trim()] = Object.fromEntries(columns.map((c, i) => [c, links[i]]));
    });
    return rows;
  }

  /** Clicks a link (and, when it asks which page, the item of the menu) and returns the page that it opened and the filter it carries. */
  async function clickAndRead(link: HTMLAnchorElement, menuItem?: string): Promise<{path: string, filter: CcListRequest}> {
    link.click();
    await fixture.whenStable();
    if (menuItem) {
      const item = Array.from(document.querySelectorAll('.mat-mdc-menu-item')).find(e => e.textContent.trim().startsWith(menuItem)) as HTMLElement;
      expect(item, menuItem).toBeTruthy();
      item.click();
      await fixture.whenStable();
    }
    const tree = router.parseUrl(router.url);
    const path = tree.root.children['primary'].segments.map(s => s.path).join('/');
    return {path, filter: new CcListRequest(convertToParamMap(tree.queryParams))};
  }

  beforeEach(render);

  it('lists every user that owns a Core Component, a Data Type, a Code List or an Agency ID List', () => {
    expect(Object.keys(table()).sort()).toEqual(['alice', 'bob', 'carol', 'oagis']);
  });

  it('shows the count of each state, with the Data Types', () => {
    const rows = table();
    for (const [username, expected] of Object.entries(EXPECTED_ROWS)) {
      for (const column of ['WIP', 'Draft', 'Candidate', 'total'] as const) {
        expect(rows[username][column].textContent.trim(), `${username} ${column}`).toBe(String(expected[column]));
      }
    }
  });

  it('opens the list of the one kind that a count is of, on the Working release', async () => {
    const {path, filter} = await clickAndRead(table()['alice']['Draft']);
    expect(path).toBe('core_component');
    expect(filter.states).toEqual(['Draft']);
    expect(filter.ownerLoginIdList).toEqual(['alice']);
    expect(filter.release.releaseId).toBe(WORKING_RELEASE.releaseId);
  });

  it('opens the Data Type list for a count that is of Data Types only', async () => {
    const {path, filter} = await clickAndRead(table()['carol']['Draft']);
    expect(path).toBe('data_type');
    expect(filter.ownerLoginIdList).toEqual(['carol']);
  });

  it('asks which list to open for a count that is of several kinds, and opens the one that is chosen', async () => {
    const link = table()['oagis']['WIP'];
    expect(link.getAttribute('href')).toBeNull();
    const dataTypes = await clickAndRead(link, 'Data Types (4)');
    expect(dataTypes.path).toBe('data_type');
    expect(dataTypes.filter.states).toEqual(['WIP']);
    expect(dataTypes.filter.ownerLoginIdList).toEqual(['oagis']);

    const coreComponents = await clickAndRead(table()['oagis']['WIP'], 'Core Components (21)');
    expect(coreComponents.path).toBe('core_component');
    expect(coreComponents.filter.release.releaseId).toBe(WORKING_RELEASE.releaseId);
  });

  it('limits the list that a Total opens to the states that the Total adds up', async () => {
    const {path, filter} = await clickAndRead(table()['oagis']['total'], 'Core Components (22)');
    expect(path).toBe('core_component');
    expect(filter.states).toEqual(['WIP', 'Draft', 'Candidate']);
    expect(filter.ownerLoginIdList).toEqual(['oagis']);
  });
});
