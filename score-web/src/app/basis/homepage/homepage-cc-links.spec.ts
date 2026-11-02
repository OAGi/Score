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
  oagis: {WIP: 21, Draft: 0, Candidate: 1, total: 22},
  alice: {WIP: 1, Draft: 2, Candidate: 0, total: 3},
  bob: {WIP: 0, Draft: 0, Candidate: 3, total: 3},
};

describe('Home "Core components by users and states" links', () => {
  let fixture: ComponentFixture<HomepageComponent>;
  let router: Router;

  async function render() {
    const stateColor = {background: '#000', font: '#fff'};
    TestBed.configureTestingModule({
      imports: [BasisModule, NoopAnimationsModule],
      providers: [
        provideRouter([{path: 'core_component', component: CoreComponentListStub}]),
        {provide: AuthService, useValue: {getUserToken: () => ({username: 'oagis', roles: ['developer'], tenant: {enabled: false}})}},
        {provide: WebPageInfoService, useValue: {getComponentStateColorSet: () => stateColor, getReleaseStateColorSet: () => stateColor}},
        {provide: LibraryService, useValue: {getLibrarySummaryList: () => of([{libraryId: 1, name: 'connectSpec', isDefault: true}])}},
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

  /** Clicks a link and returns the Core Component list filter that the target page builds from the URL. */
  async function clickAndReadFilter(link: HTMLAnchorElement): Promise<CcListRequest> {
    link.click();
    await fixture.whenStable();
    const tree = router.parseUrl(router.url);
    expect(tree.root.children['primary'].segments.map(s => s.path)).toEqual(['core_component']);
    return new CcListRequest(convertToParamMap(tree.queryParams));
  }

  beforeEach(render);

  it('lists the users that own an ACC, ASCCP or BCCP, but not one that owns only Data Types', () => {
    expect(Object.keys(table()).sort()).toEqual(['alice', 'bob', 'oagis']);
  });

  it('shows the count of each state, without Data Types', () => {
    const rows = table();
    for (const [username, expected] of Object.entries(EXPECTED_ROWS)) {
      for (const column of ['WIP', 'Draft', 'Candidate', 'total'] as const) {
        expect(rows[username][column].textContent.trim(), `${username} ${column}`).toBe(String(expected[column]));
      }
    }
  });

  for (const username of Object.keys(EXPECTED_ROWS)) {
    for (const state of ['WIP', 'Draft', 'Candidate']) {
      it(`opens the ${state} list of ${username} on the Working release`, async () => {
        const filter = await clickAndReadFilter(table()[username][state]);
        expect(filter.states).toEqual([state]);
        expect(filter.ownerLoginIdList).toEqual([username]);
        expect(filter.release.releaseId).toBe(WORKING_RELEASE.releaseId);
      });
    }

    it(`opens the list of ${username} limited to WIP, Draft and Candidate from the Total count`, async () => {
      const filter = await clickAndReadFilter(table()[username]['total']);
      expect(filter.states).toEqual(['WIP', 'Draft', 'Candidate']);
      expect(filter.ownerLoginIdList).toEqual([username]);
      expect(filter.release.releaseId).toBe(WORKING_RELEASE.releaseId);
    });
  }

  it('limits the Total link to exactly the states that the Total count adds up', async () => {
    const countedStates = ['WIP', 'Draft', 'Candidate'];
    const filter = await clickAndReadFilter(table()['oagis']['total']);
    expect(filter.states).toEqual(countedStates);
    const total = countedStates.reduce((sum, s) => sum + EXPECTED_ROWS['oagis'][s], 0);
    expect(table()['oagis']['total'].textContent.trim()).toBe(String(total));
  });
});
