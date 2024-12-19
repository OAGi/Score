import {HttpParams} from '@angular/common/http';
import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {Router} from '@angular/router';
import {BieListService} from '../../bie-management/bie-list/domain/bie-list.service';
import {BieList} from '../../bie-management/bie-list/domain/bie-list';
import {CcListService} from '../../cc-management/cc-list/domain/cc-list.service';
import {UserExtensionGroup} from '../../cc-management/domain/core-component-node';
import {StateProgressBarItem} from '../../common/state-progress-bar/state-progress-bar';
import {AuthService} from '../../authentication/auth.service';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {base64Encode, initFilter, loadBranch, loadLibrary, saveBranch, saveLibrary} from '../../common/utility';
import {CcList, SummaryCcExt} from '../../cc-management/cc-list/domain/cc-list';
import {SimpleRelease, WorkingRelease} from '../../release-management/domain/release';
import {ReleaseService} from '../../release-management/domain/release.service';
import {WebPageInfoService} from '../basis.service';
import {Library} from '../../library-management/domain/library';
import {LibraryService} from '../../library-management/domain/library.service';
import {UserToken} from '../../authentication/domain/auth';

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
  selector: 'score-homepage',
  templateUrl: './homepage.component.html',
  styleUrls: ['./homepage.component.css']
})
export class HomepageComponent implements OnInit, AfterViewInit {

  stateColorList = [{state: 'WIP', color: '#D32F2F'},
    {state: 'Draft', color: '#7B1FA2'},
    {state: 'QA', color: '#7B1FA2'},
    {state: 'Candidate', color: '#303F9F'},
    {state: 'Production', color: '#303F9F'},
    {state: 'ReleaseDraft', color: '#689F38'},
    {state: 'Published', color: '#388E3C'},
    {state: 'Deleted', color: '#616161'}];

  library: Library = new Library();
  libraries: Library[] = [];
  mappedLibraries: { library: Library, selected: boolean }[] = [];

  /* CCs */
  numberOfTotalCCByStates: StateProgressBarItem[];
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

  myRecentCCs = new MatTableDataSource<CcList>();
  @ViewChild('myRecentCCsSort', {static: false})
  myRecentCCsSort: MatSort;
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
  releaseListFilterCtrl: FormControl = new FormControl();
  releaseFilteredList: ReplaySubject<SimpleRelease[]> = new ReplaySubject<SimpleRelease[]>(1);
  selectedRelease: SimpleRelease;

  myRecentBIEs = new MatTableDataSource<BieList>();
  @ViewChild('myRecentBIEsSort', {static: false})
  myRecentBIEsSort: MatSort;
  /* End of BIEs */

  /* User Extensions */
  numberOfTotalCcExtByStates: StateProgressBarItem[];
  numberOfMyCcExtByStates: StateProgressBarItem[];

  numberOfCcExtsByUsersAndStates = new MatTableDataSource<UserStatesItem>();
  @ViewChild('numberOfCcExtsByUsersAndStatesPaginator', {static: false})
  numberOfCcExtsByUsersAndStatesPaginator: MatPaginator;
  @ViewChild('numberOfCcExtsByUsersAndStatesSort', {static: false})
  numberOfCcExtsByUsersAndStatesSort: MatSort;

  numberOfCcExtsByUsers_usernameList: string[] = [];
  numberOfCcExtsByUsers_usernameListFilterCtrl: FormControl = new FormControl();
  numberOfCcExtsByUsers_usernameFilteredList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  numberOfCcExtsByUsers_usernameModel: string[] = [];

  myExtensionsUnusedInBIEs = new MatTableDataSource<SummaryCcExt>();
  @ViewChild('myExtensionsUnusedInBIEsPaginator', {static: false})
  myExtensionsUnusedInBIEsPaginator: MatPaginator;
  @ViewChild('myExtensionsUnusedInBIEsSort', {static: false})
  myExtensionsUnusedInBIEsSort: MatSort;
  /* End of User Extensions */

  UEGValue = UserExtensionGroup.value;

  constructor(private bieService: BieListService,
              private ccService: CcListService,
              private auth: AuthService,
              private releaseService: ReleaseService,
              private libraryService: LibraryService,
              private router: Router,
              public webPageInfo: WebPageInfoService) {
  }

  ngOnInit() {
    this.libraryService.getLibraries().subscribe(libraries => {
      this.initLibraries(libraries);

      this.loadData();
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

  initLibraries(libraries: Library[]) {
    this.libraries = libraries;
    if (this.libraries.length > 0) {
      const savedLibraryId = loadLibrary(this.auth.getUserToken());
      if (savedLibraryId) {
        this.library = this.libraries.filter(e => e.libraryId === savedLibraryId)[0];
      }
      if (!this.library || !this.library.libraryId) {
        this.library = this.libraries[0];
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
    const userToken = this.userToken;
    forkJoin([
      this.releaseService.getSimpleReleases(this.library.libraryId)
    ]).subscribe(([resp]) => {
      resp = [{state: '', releaseId: -1, releaseNum : 'All', workingRelease: false}].concat(resp.filter(e => !e.workingRelease));
      initFilter(this.releaseListFilterCtrl, this.releaseFilteredList, resp, (e) => e.releaseNum);

      const branch = loadBranch(userToken, 'CC');
      if (branch) {
        this.selectedRelease = resp[resp.findIndex(e => e.releaseId === branch)];
      }
      if (this.selectedRelease === undefined) {
        this.selectedRelease = resp[resp.findIndex(e => e.releaseNum === 'All')];
      }
      this.initSummaryCCs(userToken);
      this.initSummaryBIEs(userToken);
      this.initSummaryUserExtensions(userToken);
    });
  }

  onLibraryChange(library: Library) {
    this.library = library;
    saveLibrary(this.auth.getUserToken(), this.library.libraryId);
    this.loadData();
  }

  onChangeRelease() {
    const userToken = this.userToken;
    this.initSummaryBIEs(userToken);
    this.initSummaryUserExtensions(userToken);
    saveBranch(userToken, 'CC', this.selectedRelease.releaseId);
    saveBranch(userToken, 'BIE', this.selectedRelease.releaseId);
  }

  initSummaryCCs(userToken) {
    const releaseParam = {key: 'releaseId', value: WorkingRelease.releaseId};
    this.ccService.getSummaryCcList(this.library.libraryId).subscribe(summaryCcInfo => {
      this.numberOfTotalCCByStates = [];
      this.numberOfMyCCByStates = [];
      for (const item of this.stateColorList) {
        this.numberOfTotalCCByStates.push({
          name: item.state,
          value: summaryCcInfo.numberOfTotalCcByStates[item.state] || 0,
          href: ['/core_component', [{key: 'states', value: item.state}, releaseParam]],
          disabled: false,
          style: {
            bg_color: this.webPageInfo.getComponentStateColorSet(item.state).background || item.color,
            text_color: this.webPageInfo.getComponentStateColorSet(item.state).font || '#ffffff'
          }
        });
        this.numberOfMyCCByStates.push({
          name: item.state,
          value: summaryCcInfo.numberOfMyCcByStates[item.state] || 0,
          href: ['/core_component', [{key: 'states', value: item.state}, releaseParam, {key: 'ownerLoginIds', value: userToken.username}]],
          disabled: false,
          style: {
            bg_color: this.webPageInfo.getComponentStateColorSet(item.state).background || item.color,
            text_color: this.webPageInfo.getComponentStateColorSet(item.state).font || '#ffffff'
          }
        });
      }

      const numberOfCCsByUsersAndStatesData = [];
      this.numberOfCCsByUsers_usernameList = [];
      let data = {};
      let total = 0;
      for (const username of this.keys(summaryCcInfo.ccByUsersAndStates)) {
        total = 0;
        data = {username};
        for (const item of this.stateColorList) {
          data[item.state] = summaryCcInfo.ccByUsersAndStates[username][item.state] || 0;
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

      this.myRecentCCs.data = summaryCcInfo.myRecentCCs;
    });
  }

  initSummaryBIEs(userToken) {
    const releaseParam = {key: 'releaseId', value: this.selectedRelease.releaseId};
    this.bieService.getSummaryBieList(this.library.libraryId, this.selectedRelease.releaseId).subscribe(summaryBieInfo => {
      this.numberOfTotalBieByStates = [];
      this.numberOfMyBieByStates = [];
      for (const item of this.stateColorList) {
        this.numberOfTotalBieByStates.push({
          name: item.state,
          value: summaryBieInfo.numberOfTotalBieByStates[item.state] || 0,
          href: ['/profile_bie', [{key: 'states', value: item.state}, releaseParam]],
          disabled: false,
          style: {
            bg_color: this.webPageInfo.getComponentStateColorSet(item.state).background || item.color,
            text_color: this.webPageInfo.getComponentStateColorSet(item.state).font || '#ffffff'
          }
        });
        this.numberOfMyBieByStates.push({
          name: item.state,
          value: summaryBieInfo.numberOfMyBieByStates[item.state] || 0,
          href: ['/profile_bie', [{key: 'states', value: item.state}, {key: 'ownerLoginIds', value: userToken.username}, releaseParam]],
          disabled: false,
          style: {
            bg_color: this.webPageInfo.getComponentStateColorSet(item.state).background || item.color,
            text_color: this.webPageInfo.getComponentStateColorSet(item.state).font || '#ffffff'
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

      this.myRecentBIEs.data = summaryBieInfo.myRecentBIEs;
    });
  }

  initSummaryUserExtensions(userToken) {
    this.ccService.getSummaryCcExtList(this.library.libraryId, this.selectedRelease.releaseId).subscribe(summaryCcExtInfo => {
      const releaseParam = {key: 'releaseId', value: this.selectedRelease.releaseId};
      const typeParam = {key: 'types', value: 'ACC'};
      const componentTypeParam = {key: 'componentTypes', value: UserExtensionGroup.value};
      const ownerLoginIdsParam = {key: 'ownerLoginIds', value: userToken.username};
      this.numberOfTotalCcExtByStates = [];
      this.numberOfMyCcExtByStates = [];
      for (const item of this.stateColorList) {
        this.numberOfTotalCcExtByStates.push({
          name: item.state,
          value: summaryCcExtInfo.numberOfTotalCcExtByStates[item.state] || 0,
          href: ['/core_component', [{key: 'states', value: item.state} , releaseParam, typeParam, componentTypeParam]],
          disabled: this.selectedRelease.releaseId < 0,
          style: {
            bg_color: this.webPageInfo.getComponentStateColorSet(item.state).background || item.color,
            text_color: this.webPageInfo.getComponentStateColorSet(item.state).font || '#ffffff'
          }
        });
        this.numberOfMyCcExtByStates.push({
          name: item.state,
          value: summaryCcExtInfo.numberOfMyCcExtByStates[item.state] || 0,
          href: ['/core_component', [{key: 'states', value: item.state}, releaseParam, typeParam, componentTypeParam, ownerLoginIdsParam]],
          disabled: this.selectedRelease.releaseId < 0,
          style: {
            bg_color: this.webPageInfo.getComponentStateColorSet(item.state).background || item.color,
            text_color: this.webPageInfo.getComponentStateColorSet(item.state).font || '#ffffff'
          }
        });
      }

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

      this.myExtensionsUnusedInBIEs.data = summaryCcExtInfo.myExtensionsUnusedInBIEs;
    });
  }

  ngAfterViewInit() {
    this.numberOfBiesByUsersAndStates.paginator = this.numberOfBiesByUsersAndStatesPaginator;
    this.numberOfBiesByUsersAndStates.filterPredicate = ((data: UserStatesItem, filter: string) => {
      return Array.from(filter.split(',')).includes(data.username);
    });
    this.numberOfBiesByUsersAndStates.sort = this.numberOfBiesByUsersAndStatesSort;

    this.myRecentBIEs.sort = this.myRecentBIEsSort;

    this.numberOfCcExtsByUsersAndStates.paginator = this.numberOfCcExtsByUsersAndStatesPaginator;
    this.numberOfCcExtsByUsersAndStates.filterPredicate = ((data: UserStatesItem, filter: string) => {
      return Array.from(filter.split(',')).includes(data.username);
    });
    this.numberOfCcExtsByUsersAndStates.sort = this.numberOfCcExtsByUsersAndStatesSort;

    this.myExtensionsUnusedInBIEs.paginator = this.myExtensionsUnusedInBIEsPaginator;
    this.myExtensionsUnusedInBIEs.sort = this.myExtensionsUnusedInBIEsSort;
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

  keys(obj) {
    return (obj) ? Object.keys(obj) : [];
  }
}
