import {HttpParams} from '@angular/common/http';
import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {Router} from '@angular/router';
import {BieListService} from '../../bie-management/bie-list/domain/bie-list.service';
import {BieList} from '../../bie-management/bie-list/domain/bie-list';
import {CcListService} from '../../cc-management/cc-list/domain/cc-list.service';
import {OagisComponentTypes, UserExtensionGroup} from '../../cc-management/domain/core-component-node';
import {StateProgressBarItem} from '../../common/state-progress-bar/state-progress-bar';
import {AuthService} from '../../authentication/auth.service';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {base64Encode, filter, initFilter, loadBranch, saveBranch} from '../../common/utility';
import {SummaryCcExt} from '../../cc-management/cc-list/domain/cc-list';
import {SimpleRelease} from '../../release-management/domain/release';
import {ReleaseService} from '../../release-management/domain/release.service';

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

  /* BIEs */
  numberOfTotalBieByStates: StateProgressBarItem[];
  numberOfMyBieByStates: StateProgressBarItem[];

  stateColorList = [{state: 'WIP', color: '#D32F2F'},
    {state: 'Draft', color: '#7B1FA2'},
    {state: 'QA', color: '#7B1FA2'},
    {state: 'Candidate', color: '#303F9F'},
    {state: 'Production', color: '#303F9F'},
    {state: 'ReleaseDraft', color: '#689F38'},
    {state: 'Published', color: '#388E3C'},
    {state: 'Deleted', color: '#616161'}];

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

  isDeveloper: boolean;
  isTenantInstance: boolean;

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
              private authService: AuthService,
              private releaseService: ReleaseService,
              private router: Router) {
  }

  ngOnInit() {
    const userToken = this.authService.getUserToken();
    this.isDeveloper = userToken.roles.includes('developer');
    this.isTenantInstance = userToken.isTenantInstance;

    this.releaseService.getSimpleReleases().subscribe(resp => {
      resp = [{state: '', releaseId: -1, releaseNum : 'All'}].concat(resp.filter(e => e.releaseNum !== 'Working'));
      this.releaseListFilterCtrl.valueChanges
        .subscribe(() => {
          let search = this.releaseListFilterCtrl.value.releaseNum;
          if (!search) {
            this.releaseFilteredList.next(resp.slice());
            return;
          } else {
            search = search.toLowerCase();
          }
          this.releaseFilteredList.next(
            resp.filter(e => e.releaseNum.toLowerCase().indexOf(search) > -1)
          );
        });
      this.releaseFilteredList.next(resp.slice());
      const branch = loadBranch(userToken, 'CC');
      if (branch) {
        this.selectedRelease = resp[resp.findIndex(e => e.releaseId === branch)];
      }
      if (this.selectedRelease === undefined) {
        this.selectedRelease = resp[resp.findIndex(e => e.releaseNum === 'All')];
      }
      this.initSummaryBIEs(userToken);
      this.initSummaryUserExtensions(userToken);
    });
  }

  onChangeRelease() {
    const userToken = this.authService.getUserToken();
    this.initSummaryBIEs(userToken);
    this.initSummaryUserExtensions(userToken);
    saveBranch(userToken, 'CC', this.selectedRelease.releaseId);
    saveBranch(userToken, 'BIE', this.selectedRelease.releaseId);
  }

  initSummaryBIEs(userToken) {
    const releaseParam = {key: 'releaseId', value: this.selectedRelease.releaseId};
    this.bieService.getSummaryBieList(this.selectedRelease.releaseId).subscribe(summaryBieInfo => {
      this.numberOfTotalBieByStates = [];
      this.numberOfMyBieByStates = [];
      for (const item of this.stateColorList) {
        this.numberOfTotalBieByStates.push({
          name: item.state,
          value: summaryBieInfo.numberOfTotalBieByStates[item.state] || 0,
          href: ['/profile_bie', [{key: 'states', value: item.state}, releaseParam]],
          disabled: this.selectedRelease.releaseId < 0,
          style: {
            bg_color: item.color,
            text_color: '#ffffff'
          }
        });
        this.numberOfMyBieByStates.push({
          name: item.state,
          value: summaryBieInfo.numberOfMyBieByStates[item.state] || 0,
          href: ['/profile_bie', [{key: 'states', value: item.state}, {key: 'ownerLoginIds', value: userToken.username}, releaseParam]],
          disabled: this.selectedRelease.releaseId < 0,
          style: {
            bg_color: item.color,
            text_color: '#ffffff'
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
    this.ccService.getSummaryCcExtList(this.selectedRelease.releaseId).subscribe(summaryCcExtInfo => {
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
            bg_color: item.color,
            text_color: '#ffffff'
          }
        });
        this.numberOfMyCcExtByStates.push({
          name: item.state,
          value: summaryCcExtInfo.numberOfMyCcExtByStates[item.state] || 0,
          href: ['/core_component', [{key: 'states', value: item.state}, releaseParam, typeParam, componentTypeParam, ownerLoginIdsParam]],
          disabled: this.selectedRelease.releaseId < 0,
          style: {
            bg_color: item.color,
            text_color: '#ffffff'
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
      params = params.set(param['key'], param['value']);
    }
    return base64Encode(params.toString());
  }

  keys(obj) {
    return (obj) ? Object.keys(obj) : [];
  }
}
