import {CountsByUsersAndStates, ccCountOfState, ccCountsByKinds, ccCountsByUsersAndStates, ccDestinations} from './cc-summary-counts';
import {ExtReleaseCount, extDestinations} from './ext-destinations';
import {Destination} from '../../common/destinations/destinations';
import {HttpParams} from '@angular/common/http';
import { AfterViewInit, Component, OnInit, ViewChild, inject, ChangeDetectionStrategy } from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {Router} from '@angular/router';
import {BieListService} from '../../bie-management/bie-list/domain/bie-list.service';
import {BieListEntry} from '../../bie-management/bie-list/domain/bie-list';
import {CcListService} from '../../cc-management/cc-list/domain/cc-list.service';
import {UserExtensionGroup} from '../../cc-management/domain/core-component-node';
import {StateProgressBarItem} from '../../common/state-progress-bar/state-progress-bar';
import {UsersView} from '../../common/state-dashboard/view-toggle.component';
import {RankingState} from '../../common/state-dashboard/user-state-ranking.component';
import {AuthService} from '../../authentication/auth.service';
import {FormControl} from '@angular/forms';
import {forkJoin, of, ReplaySubject} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {base64Encode, initFilter, loadLibrary, saveLibrary} from '../../common/utility';
import {CcListEntry} from '../../cc-management/cc-list/domain/cc-list';
import {ReleaseSummary} from '../../release-management/domain/release';
import {ReleaseService} from '../../release-management/domain/release.service';
import {WebPageInfoService} from '../basis.service';
import {LibrarySummary} from '../../library-management/domain/library';
import {CodeListService} from '../../code-list-management/domain/code-list.service';
import {AgencyIdListService} from '../../agency-id-list-management/domain/agency-id-list.service';
import {NamespaceService} from '../../namespace-management/domain/namespace.service';
import {LibraryService} from '../../library-management/domain/library.service';
import {WorkItemCard} from '../../common/state-dashboard/work-item-card.component';
import {UserToken} from '../../authentication/domain/auth';

/** How many of the user's recent work each of the lists on top of the Home page shows. */
const MY_WORK_SIZE = 5;

export interface UserStatesItem {
  username: string;
  WIP: number;
  Draft: number;
  QA: number;
  Candidate: number;
  Production: number;
  ReleaseDraft: number;
  Published: number;
  total: number;
}

@Component({
  standalone: false,
  selector: 'score-homepage',
  templateUrl: './homepage.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./homepage.component.css']
})
export class HomepageComponent implements OnInit, AfterViewInit {
  private bieService = inject(BieListService);
  private ccService = inject(CcListService);
  private auth = inject(AuthService);
  private releaseService = inject(ReleaseService);
  private namespaceService = inject(NamespaceService);
  private codeListService = inject(CodeListService);
  private agencyIdListService = inject(AgencyIdListService);
  private libraryService = inject(LibraryService);
  private router = inject(Router);
  webPageInfo = inject(WebPageInfoService);


  stateColorList = [{state: 'WIP', color: '#D32F2F'},
    {state: 'Draft', color: '#7B1FA2'},
    {state: 'QA', color: '#7B1FA2'},
    {state: 'Candidate', color: '#303F9F'},
    {state: 'Production', color: '#303F9F'},
    {state: 'ReleaseDraft', color: '#689F38'},
    {state: 'Published', color: '#388E3C'},
    {state: 'Deleted', color: '#616161'}];

  library: LibrarySummary = new LibrarySummary();
  libraries: LibrarySummary[] = [];
  mappedLibraries: { library: LibrarySummary, selected: boolean }[] = [];

  /* CCs */
  numberOfTotalCCByStates: StateProgressBarItem[];
  /** What the Core Components summary counts, by kind: a number opens the list of each kind that it is of. */
  private ccExtReleaseCounts: ExtReleaseCount[] = [];
  private ccKindCounts: {[kind: string]: CountsByUsersAndStates} = {};
  numberOfMyCCByStates: StateProgressBarItem[];

  numberOfCCsByUsersAndStates = new MatTableDataSource<UserStatesItem>();
  @ViewChild('numberOfCCsByUsersAndStatesPaginator', {static: false})
  numberOfCCsByUsersAndStatesPaginator: MatPaginator;
  @ViewChild('numberOfCCsByUsersAndStatesSort', {static: false})
  numberOfCCsByUsersAndStatesSort: MatSort;

  numberOfCCsByUsers_usernameList: string[] = [];
  numberOfCCsByUsers_usernameListFilterCtrl: FormControl = new FormControl();
  numberOfCCsByUsers_usernameFilteredList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  numberOfCCsByUsers_usernameModel: string[] = [];

  myCcCards: WorkItemCard[] = [];
  private myRecentCCEntries: CcListEntry[] = [];
  /** The Code Lists and Agency ID Lists that the user worked on lately, as cards: they are listed with the Core Components. */
  private myRecentListCards: WorkItemCard[] = [];
  private namespaceUris = new Map<number, string>();
  /* End of CCs */

  /* BIEs */
  numberOfTotalBieByStates: StateProgressBarItem[];
  numberOfMyBieByStates: StateProgressBarItem[];

  numberOfBiesByUsersAndStates = new MatTableDataSource<UserStatesItem>();
  @ViewChild('numberOfBiesByUsersAndStatesPaginator', {static: false})
  numberOfBiesByUsersAndStatesPaginator: MatPaginator;
  @ViewChild('numberOfBiesByUsersAndStatesSort', {static: false})
  numberOfBiesByUsersAndStatesSort: MatSort;

  numberOfBiesByUsers_usernameList: string[] = [];
  numberOfBiesByUsers_usernameListFilterCtrl: FormControl = new FormControl();
  numberOfBiesByUsers_usernameFilteredList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  numberOfBiesByUsers_usernameModel: string[] = [];
  selectedRelease: ReleaseSummary;
  workingRelease: ReleaseSummary;

  myBieCards: WorkItemCard[] = [];
  /* End of BIEs */

  /* User Extensions */
  numberOfTotalCcExtByStates: StateProgressBarItem[];
  /** The User Extensions that the user worked on lately, as cards. */
  myExtCards: WorkItemCard[] = [];

  numberOfCcExtsByUsersAndStates = new MatTableDataSource<UserStatesItem>();
  @ViewChild('numberOfCcExtsByUsersAndStatesPaginator', {static: false})
  numberOfCcExtsByUsersAndStatesPaginator: MatPaginator;
  @ViewChild('numberOfCcExtsByUsersAndStatesSort', {static: false})
  numberOfCcExtsByUsersAndStatesSort: MatSort;

  numberOfCcExtsByUsers_usernameList: string[] = [];
  numberOfCcExtsByUsers_usernameListFilterCtrl: FormControl = new FormControl();
  numberOfCcExtsByUsers_usernameFilteredList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  numberOfCcExtsByUsers_usernameModel: string[] = [];

  /* End of User Extensions */

  UEGValue = UserExtensionGroup.value;

  /** The sections of the cards, in the order of the cards. */
  get kpiSections(): ('cc' | 'bie' | 'ext')[] {
    return [...(this.isDeveloper ? ['cc' as const] : []), 'bie', ...(this.isTenantEnabled || this.isDeveloper ? [] : ['ext' as const])];
  }

  /** The section below the cards follows the card that the carousel shows. */
  onKpiIndex(index: number) {
    const section = this.kpiSections[index];
    if (section) {
      this.selectedSection = section;
    }
  }

  /** Each users panel shows its chart or its table. */
  ccUsersView: UsersView = 'chart';
  bieUsersView: UsersView = 'chart';
  ccExtUsersView: UsersView = 'chart';

  /** The section shown below the cards: the one of the card that was clicked. */
  selectedSection: 'cc' | 'bie' | 'ext' = this.userToken.roles.includes('developer') ? 'cc' : 'bie';

  /** The colours of a state, as the configuration has them now (a state that it does not name has the default of the home page). */
  stateStyle(state: string): {bg_color: string, text_color: string} {
    const colors = this.webPageInfo.getComponentStateColorSet(state);
    const fallback = this.stateColorList.find(e => e.state === state)?.color;
    return {bg_color: colors?.background || fallback, text_color: colors?.font || '#ffffff'};
  }

  /** The states that the users rankings split their bars by, with their colors. */
  get rankingStates(): RankingState[] {
    return this.stateColorList.map(item => {
      const style = this.stateStyle(item.state);
      return {state: item.state, background: style.bg_color, font: style.text_color};
    });
  }

  /** What every link of the Core Components ranking carries: the Working release the Home page counts on. */
  get ccBaseParams(): {key: string, value: any}[] {
    return this.workingRelease ? [{key: 'releaseId', value: this.workingRelease.releaseId}] : [];
  }

  /** What every link of the BIEs carries: the release, unless it is all of them. */
  get bieBaseParams(): {key: string, value: any}[] {
    return this.selectedRelease?.releaseId > 0 ? [{key: 'releaseIds', value: this.selectedRelease.releaseId}] : [];
  }

  /** `q` for the BIE list. */
  bieQ(set: any): string {
    return this.q([...set, ...this.bieBaseParams]);
  }

  /** The pages that a number of the Core Components summary opens (of a user, of a state, or of both). */
  ccDestinationsOf = (username?: string, state?: string): Destination[] =>
    ccDestinations(this.ccKindCounts, this.ccBaseParams, state, username);

  /** The pages that a number of the User Extensions summary opens: a list of each release that has some. */
  ccExtDestinationsOf = (username?: string, state?: string): Destination[] =>
    extDestinations(this.ccExtReleaseCounts, UserExtensionGroup.value, state, username);

  ngOnInit() {
    this.libraryService.getLibrarySummaryList().subscribe(libraries => {
      this.initLibraries(libraries);

      if (this.library?.libraryId) {
        this.loadData();
      }
    });
  }

  get userToken(): UserToken {
    return this.auth.getUserToken();
  }

  get isDeveloper(): boolean {
    return this.userToken.roles.includes('developer');
  }

  get isTenantEnabled(): boolean {
    return this.userToken.tenant.enabled;
  }

  initLibraries(libraries: LibrarySummary[]) {
    this.libraries = libraries;
    if (this.libraries.length > 0) {
      const savedLibraryId = loadLibrary(this.auth.getUserToken());
      if (savedLibraryId) {
        this.library = this.libraries.filter(e => e.libraryId === savedLibraryId)[0];
      }
      if (!this.library || !this.library.libraryId) {
        this.library = this.libraries.find(e => e.isDefault) || this.libraries[0];
      }
      if (this.library) {
        saveLibrary(this.auth.getUserToken(), this.library.libraryId);
      }
      this.mappedLibraries = this.libraries.map(e => {
        return {library: e, selected: (this.library.libraryId === e.libraryId)};
      });
    }
  }

  loadData() {
    if (!this.library?.libraryId) {
      this.workingRelease = undefined;
      this.selectedRelease = undefined;
      return;
    }

    const userToken = this.userToken;
    this.workingRelease = undefined;
    this.namespaceService.getNamespaceSummaries(this.library.libraryId).subscribe(namespaces => {
      this.namespaceUris = new Map(namespaces.map(e => [e.namespaceId, e.uri]));
      this.rebuildCcCards();
    });
    this.releaseService.getWorkingRelease(this.library.libraryId).subscribe(workingRelease => {
      this.workingRelease = workingRelease;
      // The BIEs are those of every release.
      this.selectedRelease = {state: '', releaseId: -1, releaseNum: 'All', workingRelease: false};
      this.initSummaryCCs(userToken);
      this.loadRecentLists(userToken);
      this.initSummaryBIEs(userToken);
      // A developer does not make User Extensions.
      if (!this.isDeveloper) {
        this.initSummaryUserExtensions(userToken);
      }
    });
  }

  onLibraryChange(library: LibrarySummary) {
    this.library = library;
    saveLibrary(this.auth.getUserToken(), this.library.libraryId);
    this.loadData();
  }

  initSummaryCCs(userToken) {
    const styleOf = (state: string) => this.stateStyle(state);
    const releaseParams = this.workingRelease ?
      [{key: 'releaseId', value: this.workingRelease.releaseId}] :
      [];
    this.ccService.getSummaryCcList(this.library.libraryId).subscribe(summaryCcInfo => {
      const ccByUsersAndStates = ccCountsByUsersAndStates(summaryCcInfo.ccByTypesUsersAndStates);
      this.ccKindCounts = ccCountsByKinds(summaryCcInfo.ccByTypesUsersAndStates);

      this.numberOfTotalCCByStates = [];
      this.numberOfMyCCByStates = [];
      for (const item of this.stateColorList) {
        this.numberOfTotalCCByStates.push({
          name: item.state,
          value: ccCountOfState(ccByUsersAndStates, item.state),
          href: ['/core_component', [{key: 'states', value: item.state}, ...releaseParams]],
          destinations: ccDestinations(this.ccKindCounts, releaseParams, item.state),
          disabled: false,
          // Read when it is drawn, not when the item is made: the colours are the configuration's, which may arrive or change later.
          get style() {
            return styleOf(item.state);
          }
        });
        this.numberOfMyCCByStates.push({
          name: item.state,
          value: ccCountOfState(ccByUsersAndStates, item.state, userToken.username),
          href: ['/core_component', [{key: 'states', value: item.state}, ...releaseParams, {key: 'ownerLoginIdList', value: userToken.username}]],
          disabled: false,
          // Read when it is drawn, not when the item is made: the colours are the configuration's, which may arrive or change later.
          get style() {
            return styleOf(item.state);
          }
        });
      }

      const numberOfCCsByUsersAndStatesData = [];
      this.numberOfCCsByUsers_usernameList = [];
      let data = {};
      let total = 0;
      for (const username of this.keys(ccByUsersAndStates)) {
        total = 0;
        data = {username};
        for (const item of this.stateColorList) {
          data[item.state] = ccByUsersAndStates[username][item.state] || 0;
          total += data[item.state];
        }
        data['total'] = total;
        numberOfCCsByUsersAndStatesData.push(data);
        this.numberOfCCsByUsers_usernameList.push(username);
      }
      this.numberOfCCsByUsersAndStates.data = numberOfCCsByUsersAndStatesData;

      initFilter(
        this.numberOfCCsByUsers_usernameListFilterCtrl,
        this.numberOfCCsByUsers_usernameFilteredList,
        this.numberOfCCsByUsers_usernameList
      );

      this.myRecentCCEntries = summaryCcInfo.myRecentCCs;
      this.rebuildCcCards();
    });
  }

  /** The cards of the recent Core Components, Code Lists and Agency ID Lists: made again when the namespaces or the lists arrive. */
  private rebuildCcCards() {
    const uri = (namespaceId: number) => this.namespaceUris.get(namespaceId);
    const ccCards: WorkItemCard[] = this.myRecentCCEntries.map(element => ({
      type: element.type.endsWith('DT') ? 'Data Type' : element.type,
      title: element.den,
      link: this.ccLink(element),
      state: element.state,
      stateClass: 'cc-state' as const,
      meta: [{label: 'Rev.', value: element.log?.revisionNum}],
      details: [
        {label: 'Namespace', value: uri(element.namespaceId), placeholder: 'No namespace'},
        {label: 'Definition', value: element.definition?.content, placeholder: 'No definition'}
      ],
      updatedWhen: element.lastUpdated?.when
    }));
    const updated = (card: WorkItemCard) => card.updatedWhen ? new Date(card.updatedWhen).getTime() : 0;
    // The latest of them all, as many as the Core Components alone were.
    this.myCcCards = [...ccCards, ...this.myRecentListCards].sort((a, b) => updated(b) - updated(a)).slice(0, MY_WORK_SIZE);
  }

  /** The Code Lists and the Agency ID Lists that the user owns and is working on, the latest of each. */
  private loadRecentLists(userToken: UserToken) {
    const params = new HttpParams()
      .set('libraryId', this.library.libraryId)
      .set('releaseId', this.workingRelease.releaseId)
      .set('states', 'WIP,Draft,Candidate')
      .set('ownerLoginIdList', userToken.username)
      .set('orderBy', '-lastUpdateTimestamp')
      .set('pageIndex', 0)
      .set('pageSize', MY_WORK_SIZE);
    forkJoin([
      this.codeListService.getCodeListListPage(params).pipe(catchError(() => of({list: []}))),
      this.agencyIdListService.getAgencyIdListListPage(params).pipe(catchError(() => of({list: []})))
    ]).subscribe(([codeLists, agencyIdLists]) => {
      this.myRecentListCards = [
        ...(codeLists.list as any[]).map(element => this.listCard('Code List', '/code_list/' + element.codeListManifestId, element)),
        ...(agencyIdLists.list as any[]).map(element => this.listCard('Agency ID List', '/agency_id_list/' + element.agencyIdListManifestId, element))
      ];
      this.rebuildCcCards();
    });
  }

  private listCard(type: string, link: string, element: any): WorkItemCard {
    return {
      type, link,
      title: element.name,
      state: element.state,
      stateClass: 'cc-state',
      meta: [{label: 'Rev.', value: element.log?.revisionNum}],
      details: [
        {label: 'Namespace', value: this.namespaceUris.get(element.namespaceId), placeholder: 'No namespace'},
        {label: 'Definition', value: element.definition, placeholder: 'No definition'}
      ],
      updatedWhen: element.lastUpdated?.when
    };
  }

  initSummaryBIEs(userToken) {
    const styleOf = (state: string) => this.stateStyle(state);
    const releaseParams = this.bieBaseParams;
    this.bieService.getSummaryBieList(this.library.libraryId, this.selectedRelease.releaseId).subscribe(summaryBieInfo => {
      this.numberOfTotalBieByStates = [];
      this.numberOfMyBieByStates = [];
      for (const item of this.stateColorList) {
        this.numberOfTotalBieByStates.push({
          name: item.state,
          value: summaryBieInfo.numberOfTotalBieByStates[item.state] || 0,
          href: ['/profile_bie', [{key: 'states', value: item.state}, ...releaseParams]],
          disabled: false,
          // Read when it is drawn, not when the item is made: the colours are the configuration's, which may arrive or change later.
          get style() {
            return styleOf(item.state);
          }
        });
        this.numberOfMyBieByStates.push({
          name: item.state,
          value: summaryBieInfo.numberOfMyBieByStates[item.state] || 0,
          href: ['/profile_bie', [{key: 'states', value: item.state}, {key: 'ownerLoginIdList', value: userToken.username}, ...releaseParams]],
          disabled: false,
          // Read when it is drawn, not when the item is made: the colours are the configuration's, which may arrive or change later.
          get style() {
            return styleOf(item.state);
          }
        });
      }

      const numberOfBiesByUsersAndStatesData = [];
      this.numberOfBiesByUsers_usernameList = [];
      let data = {};
      let total = 0;
      for (const username of this.keys(summaryBieInfo.bieByUsersAndStates)) {
        total = 0;
        data = {username};
        for (const item of this.stateColorList) {
          data[item.state] = summaryBieInfo.bieByUsersAndStates[username][item.state] || 0;
          total += data[item.state];
        }
        data['total'] = total;
        numberOfBiesByUsersAndStatesData.push(data);
        this.numberOfBiesByUsers_usernameList.push(username);
      }
      this.numberOfBiesByUsersAndStates.data = numberOfBiesByUsersAndStatesData;

      initFilter(
        this.numberOfBiesByUsers_usernameListFilterCtrl,
        this.numberOfBiesByUsers_usernameFilteredList,
        this.numberOfBiesByUsers_usernameList
      );

      this.myBieCards = summaryBieInfo.myRecentBIEs.map(element => ({
        type: 'BIE',
        title: element.den,
        link: '/profile_bie/' + element.topLevelAsbiepId,
        state: element.state,
        stateClass: 'bie-state' as const,
        meta: [{label: 'Branch', value: element.release?.releaseNum}],
        details: [
          {label: 'Business contexts', value: element.businessContextList?.map(e => e.name).join(', ')},
          {label: 'Definition', value: element.definition, placeholder: 'No definition'},
          {label: 'Version', value: element.version},
          {label: 'Remark', value: element.remark}
        ],
        updatedWhen: element.lastUpdated?.when
      }));
    });
  }

  initSummaryUserExtensions(userToken) {
    const styleOf = (state: string) => this.stateStyle(state);
    this.ccService.getSummaryCcExtList(this.library.libraryId, this.selectedRelease.releaseId).subscribe(summaryCcExtInfo => {
      this.ccExtReleaseCounts = summaryCcExtInfo.ccExtCountsByReleases ?? [];
      const releaseParam = {key: 'releaseId', value: this.selectedRelease.releaseId};
      const typeParam = {key: 'types', value: 'ACC'};
      const componentTypeParam = {key: 'componentTypes', value: UserExtensionGroup.value};
      this.numberOfTotalCcExtByStates = [];
      for (const item of this.stateColorList) {
        this.numberOfTotalCcExtByStates.push({
          name: item.state,
          value: summaryCcExtInfo.numberOfTotalCcExtByStates[item.state] || 0,
          href: ['/core_component', [{key: 'states', value: item.state} , releaseParam, typeParam, componentTypeParam]],
          destinations: extDestinations(this.ccExtReleaseCounts, UserExtensionGroup.value, item.state),
          disabled: false,
          // Read when it is drawn, not when the item is made: the colours are the configuration's, which may arrive or change later.
          get style() {
            return styleOf(item.state);
          }
        });
      }

      this.loadMyExtCards(userToken);

      const numberOfCcExtsByUsersAndStatesData = [];
      this.numberOfCcExtsByUsers_usernameList = [];
      let data = {};
      let total = 0;
      for (const username of this.keys(summaryCcExtInfo.ccExtByUsersAndStates)) {
        total = 0;
        data = {username};
        for (const item of this.stateColorList) {
          data[item.state] = summaryCcExtInfo.ccExtByUsersAndStates[username][item.state] || 0;
          total += data[item.state];
        }
        data['total'] = total;
        numberOfCcExtsByUsersAndStatesData.push(data);
        this.numberOfCcExtsByUsers_usernameList.push(username);
      }

      this.numberOfCcExtsByUsersAndStates.data = numberOfCcExtsByUsersAndStatesData;

      initFilter(
        this.numberOfCcExtsByUsers_usernameListFilterCtrl,
        this.numberOfCcExtsByUsers_usernameFilteredList,
        this.numberOfCcExtsByUsers_usernameList,
      );
    });
  }

  /** The latest User Extensions that the user owns, from each release that has some of them (the User Extensions are in the releases they were made for). */
  private loadMyExtCards(userToken: UserToken) {
    const releases = new Map<number, string>();
    for (const e of this.ccExtReleaseCounts) {
      if (e.ownerUsername === userToken.username) {
        releases.set(e.releaseId, e.releaseNum);
      }
    }
    if (!releases.size) {
      this.myExtCards = [];
      return;
    }
    forkJoin([...releases.keys()].map(releaseId => this.ccService.getCcListPage(new HttpParams()
      .set('libraryId', this.library.libraryId)
      .set('releaseId', releaseId)
      .set('types', 'acc')
      .set('componentTypes', UserExtensionGroup.value)
      .set('ownerLoginIdList', userToken.username)
      .set('orderBy', '-lastUpdateTimestamp')
      .set('pageIndex', 0)
      .set('pageSize', MY_WORK_SIZE)).pipe(catchError(() => of({list: [] as CcListEntry[]}))))
    ).subscribe(pages => {
      const updated = (card: WorkItemCard) => card.updatedWhen ? new Date(card.updatedWhen).getTime() : 0;
      this.myExtCards = pages.flatMap(page => page.list).map(element => ({
        type: 'User Extension',
        title: element.den,
        link: '/core_component/extension/' + element.manifestId,
        state: element.state,
        stateClass: 'cc-state' as const,
        meta: [{label: 'Release', value: element.release?.releaseNum ?? element.releaseNum}],
        details: [
          {label: 'Definition', value: element.definition?.content, placeholder: 'No definition'}
        ],
        updatedWhen: element.lastUpdated?.when
      })).sort((a, b) => updated(b) - updated(a)).slice(0, MY_WORK_SIZE);
    });
  }

  ngAfterViewInit() {
    this.numberOfCCsByUsersAndStates.paginator = this.numberOfCCsByUsersAndStatesPaginator;
    this.numberOfCCsByUsersAndStates.filterPredicate = ((data: UserStatesItem, filter: string) => {
      return Array.from(filter.split(',')).includes(data.username);
    });
    this.numberOfCCsByUsersAndStates.sort = this.numberOfCCsByUsersAndStatesSort;

    this.numberOfBiesByUsersAndStates.paginator = this.numberOfBiesByUsersAndStatesPaginator;
    this.numberOfBiesByUsersAndStates.filterPredicate = ((data: UserStatesItem, filter: string) => {
      return Array.from(filter.split(',')).includes(data.username);
    });
    this.numberOfBiesByUsersAndStates.sort = this.numberOfBiesByUsersAndStatesSort;

    this.numberOfCcExtsByUsersAndStates.paginator = this.numberOfCcExtsByUsersAndStatesPaginator;
    this.numberOfCcExtsByUsersAndStates.filterPredicate = ((data: UserStatesItem, filter: string) => {
      return Array.from(filter.split(',')).includes(data.username);
    });
    this.numberOfCcExtsByUsersAndStates.sort = this.numberOfCcExtsByUsersAndStatesSort;

  }

  applyFilter(dataSource: MatTableDataSource<UserStatesItem>, model: string[]) {
    dataSource.filter = model.join(',');
  }

  q(set: any): string {
    let params = new HttpParams();
    for (const param of set) {
      params = params.set(param.key, param.value);
    }
    return base64Encode(params.toString());
  }

  /** `q` for the Core Component list on the Working release, the release the Home page counts the CCs on. */
  ccQ(set: any): string {
    const releaseParams = this.workingRelease ?
      [{key: 'releaseId', value: this.workingRelease.releaseId}] :
      [];
    return this.q([...set, ...releaseParams]);
  }

  /** Where a recent Core Component (or Data Type) opens. */
  ccLink(element: CcListEntry): string {
    return (element.type.endsWith('DT') ? '/data_type' : '/core_component/' + element.type.toLowerCase()) + '/' + element.manifestId;
  }

  keys(obj) {
    return (obj) ? Object.keys(obj) : [];
  }
}
