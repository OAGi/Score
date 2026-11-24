import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideRouter, Router} from '@angular/router';
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
import {base64Decode} from '../../common/utility';

const WORKING_RELEASE = {releaseId: 7, releaseNum: 'Working', workingRelease: true, state: 'Initialized'};

describe('Home BIEs and User Extensions by users: the bars open a list', () => {
  let fixture: ComponentFixture<HomepageComponent>;

  beforeEach(() => {
    const stateColor = {background: '#000', font: '#fff'};
    TestBed.configureTestingModule({
      imports: [BasisModule, NoopAnimationsModule],
      providers: [
        provideRouter(['core_component', 'profile_bie'].map(path => ({path, component: HomepageComponent}))),
        {provide: AuthService, useValue: {getUserToken: () => ({username: 'alice', roles: ['end-user'], tenant: {enabled: false}})}},
        {provide: WebPageInfoService, useValue: {getComponentStateColorSet: () => stateColor, getReleaseStateColorSet: () => stateColor}},
        {provide: LibraryService, useValue: {getLibrarySummaryList: () => of([{libraryId: 1, name: 'connectSpec', isDefault: true}])}},
        {provide: CodeListService, useValue: {getCodeListListPage: () => of({list: []})}},
        {provide: AgencyIdListService, useValue: {getAgencyIdListListPage: () => of({list: []})}},
        {provide: NamespaceService, useValue: {getNamespaceSummaries: () => of([])}},
        {provide: ReleaseService, useValue: {getWorkingRelease: () => of(WORKING_RELEASE)}},
        {provide: CcListService, useValue: {
          getSummaryCcList: () => of({numberOfTotalCcByStates: {}, numberOfMyCcByStates: {}, ccByUsersAndStates: {}, myRecentCCs: [], ccByTypesUsersAndStates: {}}),
          getSummaryCcExtList: () => of({numberOfTotalCcExtByStates: {WIP: 2}, numberOfMyCcExtByStates: {}, ccExtByUsersAndStates: {bob: {WIP: 2}}, myExtensionsUnusedInBIEs: [], ccExtCountsByReleases: [
            {releaseId: 7, releaseNum: '11.5', ownerUsername: 'bob', state: 'WIP', count: 1},
            {releaseId: 5, releaseNum: '10.13.3', ownerUsername: 'bob', state: 'WIP', count: 1}]})}},
        {provide: BieListService, useValue: {getSummaryBieList: () => of({numberOfTotalBieByStates: {WIP: 4}, numberOfMyBieByStates: {}, bieByUsersAndStates: {bob: {WIP: 4}}, myRecentBIEs: []})}},
      ],
    });
  });

  /** The first bar of the users panel of a section, as the page is drawn with that section below its cards. */
  async function bar(section: 'bie' | 'ext'): Promise<HTMLAnchorElement> {
    fixture = TestBed.createComponent(HomepageComponent);
    fixture.componentInstance.selectedSection = section;
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
    return fixture.nativeElement.querySelector('score-user-state-ranking a.seg') as HTMLAnchorElement;
  }

  function target(link: HTMLAnchorElement): {path: string, filter: URLSearchParams} {
    const url = new URL(link.getAttribute('href'), 'http://localhost');
    return {path: url.pathname, filter: new URLSearchParams(base64Decode(url.searchParams.get('q')))};
  }

  it('opens the BIE list of the user and the state, for the BIEs of every release', async () => {
    const link = await bar('bie');
    expect(link.classList.contains('disabled')).toBe(false);
    const {path, filter} = target(link);
    expect(path).toBe('/profile_bie');
    expect(filter.get('states')).toBe('WIP');
    expect(filter.get('ownerLoginIdList')).toBe('bob');
    expect(filter.has('releaseIds')).toBe(false);
  });

  it('asks which release to open for a User Extension number, with how many each lists, and opens the one that is chosen', async () => {
    const link = await bar('ext');
    expect(link.classList.contains('disabled')).toBe(false);
    link.click();
    await fixture.whenStable();
    const items = Array.from(document.querySelectorAll('.mat-mdc-menu-item')).map(e => e.textContent.trim());
    expect(items).toEqual(['11.5 (1)', '10.13.3 (1)']);

    const router = TestBed.inject(Router);
    (Array.from(document.querySelectorAll('.mat-mdc-menu-item')).find(e => e.textContent.trim().startsWith('10.13.3')) as HTMLElement).click();
    await fixture.whenStable();
    const tree = router.parseUrl(router.url);
    const filter = new URLSearchParams(base64Decode(tree.queryParams['q']));
    expect(tree.root.children['primary'].segments.map(s => s.path)).toEqual(['core_component']);
    expect(filter.get('releaseId')).toBe('5');
    expect(filter.get('states')).toBe('WIP');
    expect(filter.get('ownerLoginIdList')).toBe('bob');
  });
});

describe('Home work cards open the item', () => {
  async function openCard(entry: any): Promise<string> {
    const stateColor = {background: '#000', font: '#fff'};
    TestBed.configureTestingModule({
      imports: [BasisModule, NoopAnimationsModule],
      providers: [
        provideRouter(['data_type/:manifestId', 'core_component/:type/:manifestId'].map(path => ({path, component: HomepageComponent}))),
        {provide: AuthService, useValue: {getUserToken: () => ({username: 'alice', roles: ['developer'], tenant: {enabled: false}})}},
        {provide: WebPageInfoService, useValue: {getComponentStateColorSet: () => stateColor, getReleaseStateColorSet: () => stateColor}},
        {provide: LibraryService, useValue: {getLibrarySummaryList: () => of([{libraryId: 1, name: 'connectSpec', isDefault: true}])}},
        {provide: CodeListService, useValue: {getCodeListListPage: () => of({list: []})}},
        {provide: AgencyIdListService, useValue: {getAgencyIdListListPage: () => of({list: []})}},
        {provide: NamespaceService, useValue: {getNamespaceSummaries: () => of([])}},
        {provide: ReleaseService, useValue: {getWorkingRelease: () => of(WORKING_RELEASE)}},
        {provide: CcListService, useValue: {
          getSummaryCcList: () => of({numberOfTotalCcByStates: {}, numberOfMyCcByStates: {}, ccByUsersAndStates: {}, myRecentCCs: [entry], ccByTypesUsersAndStates: {}}),
          getSummaryCcExtList: () => of({numberOfTotalCcExtByStates: {}, numberOfMyCcExtByStates: {}, ccExtByUsersAndStates: {}, myExtensionsUnusedInBIEs: [], ccExtCountsByReleases: []})}},
        {provide: BieListService, useValue: {getSummaryBieList: () => of({numberOfTotalBieByStates: {}, numberOfMyBieByStates: {}, bieByUsersAndStates: {}, myRecentBIEs: []})}},
      ],
    });
    const fixture = TestBed.createComponent(HomepageComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
    const link = fixture.nativeElement.querySelector('score-work-item-card a.card-name') as HTMLAnchorElement;
    link.click();
    await fixture.whenStable();
    return TestBed.inject(Router).url;
  }

  const entry = (type: string, extra: object = {}) => ({type, den: 'x', manifestId: 12, state: 'WIP', namespaceId: 1, lastUpdated: {when: new Date()}, ...extra});

  it('opens a Data Type on the Data Type page', async () => {
    expect(await openCard(entry('DT', {dtType: 'BDT'}))).toBe('/data_type/12');
  });

  it('opens a Core Component on its page', async () => {
    expect(await openCard(entry('BCCP'))).toBe('/core_component/bccp/12');
  });
});
