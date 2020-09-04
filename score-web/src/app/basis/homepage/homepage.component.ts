import {HttpParams} from '@angular/common/http';
import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {Router} from '@angular/router';
import {BieListService} from '../../bie-management/bie-list/domain/bie-list.service';
import {BieList} from '../../bie-management/bie-list/domain/bie-list';
import {CcListService} from '../../cc-management/cc-list/domain/cc-list.service';
import {StateProgressBarItem} from '../../common/state-progress-bar/state-progress-bar';
import {AuthService} from '../../authentication/auth.service';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {base64Encode, initFilter} from '../../common/utility';
import {SummaryCcExt} from '../../cc-management/cc-list/domain/cc-list';

export interface UserStatesItem {
  username: string;
  Editing: number;
  Candidate: number;
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

  numberOfBiesByUsersAndStates = new MatTableDataSource<UserStatesItem>();
  @ViewChild('numberOfBiesByUsersAndStatesPaginator', {static: false})
  numberOfBiesByUsersAndStatesPaginator: MatPaginator;
  @ViewChild('numberOfBiesByUsersAndStatesSort', {static: false})
  numberOfBiesByUsersAndStatesSort: MatSort;

  numberOfBiesByUsers_usernameList: string[] = [];
  numberOfBiesByUsers_usernameListFilterCtrl: FormControl = new FormControl();
  numberOfBiesByUsers_usernameFilteredList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  numberOfBiesByUsers_usernameModel: string[] = [];

  isDeveloper: boolean;

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

  constructor(private bieService: BieListService,
              private ccService: CcListService,
              private authService: AuthService,
              private router: Router) {
  }

  ngOnInit() {
    const userToken = this.authService.getUserToken();
    this.isDeveloper = userToken.role === 'developer';

    this.initSummaryBIEs(userToken);
    this.initSummaryUserExtensions(userToken);
  }

  initSummaryBIEs(userToken) {
    this.bieService.getSummaryBieList().subscribe(summaryBieInfo => {
      this.numberOfTotalBieByStates = [{
        name: 'Editing',
        value: summaryBieInfo.numberOfTotalBieByStates['Editing'] || 0,
        href: ['/profile_bie', [{key: 'states', value: 'Editing'}]],
        style: {
          bg_color: '#fc2929',
          text_color: '#ffffff'
        }
      }, {
        name: 'Candidate',
        value: summaryBieInfo.numberOfTotalBieByStates['Candidate'] || 0,
        href: ['/profile_bie', [{key: 'states', value: 'Candidate'}]],
        style: {
          bg_color: '#cc317c',
          text_color: '#ffffff'
        }
      }, {
        name: 'Published',
        value: summaryBieInfo.numberOfTotalBieByStates['Published'] || 0,
        href: ['/profile_bie', [{key: 'states', value: 'Published'}]],
        style: {
          bg_color: '#84b6eb',
          text_color: '#ffffff'
        }
      }];

      this.numberOfMyBieByStates = [{
        name: 'Editing',
        value: summaryBieInfo.numberOfMyBieByStates['Editing'] || 0,
        href: ['/profile_bie', [{key: 'states', value: 'Editing'}, {key: 'ownerLoginIds', value: userToken.username}]],
        style: {
          bg_color: '#fc2929',
          text_color: '#ffffff'
        }
      }, {
        name: 'Candidate',
        value: summaryBieInfo.numberOfMyBieByStates['Candidate'] || 0,
        href: ['/profile_bie', [{key: 'states', value: 'Candidate'}, {key: 'ownerLoginIds', value: userToken.username}]],
        style: {
          bg_color: '#cc317c',
          text_color: '#ffffff'
        }
      }, {
        name: 'Published',
        value: summaryBieInfo.numberOfMyBieByStates['Published'] || 0,
        href: ['/profile_bie', [{key: 'states', value: 'Published'}, {key: 'ownerLoginIds', value: userToken.username}]],
        style: {
          bg_color: '#84b6eb',
          text_color: '#ffffff'
        }
      }];

      const numberOfBiesByUsersAndStatesData = [];
      this.numberOfBiesByUsers_usernameList = [];
      for (const username of this.keys(summaryBieInfo.bieByUsersAndStates)) {
        numberOfBiesByUsersAndStatesData.push({
          username,
          Editing: summaryBieInfo.bieByUsersAndStates[username]['Editing'] || 0,
          Candidate: summaryBieInfo.bieByUsersAndStates[username]['Candidate'] || 0,
          Published: summaryBieInfo.bieByUsersAndStates[username]['Published'] || 0,
          total: (summaryBieInfo.bieByUsersAndStates[username]['Editing'] || 0) +
            (summaryBieInfo.bieByUsersAndStates[username]['Candidate'] || 0) +
            (summaryBieInfo.bieByUsersAndStates[username]['Published'] || 0)
        });
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
    this.ccService.getSummaryCcExtList().subscribe(summaryCcExtInfo => {
      const releaseParam = {key: 'releaseId', value: 1};
      const typeParam = {key: 'types', value: 'ACC'};
      const componentTypeParam = {key: 'componentType', value: 'User Extension Group'};
      const ownerLoginIdsParam = {key: 'ownerLoginIds', value: userToken.username};

      this.numberOfTotalCcExtByStates = [{
        name: 'Editing',
        value: summaryCcExtInfo.numberOfTotalCcExtByStates['Editing'] || 0,
        href: ['/core_component', [{key: 'states', value: 'Editing'}, releaseParam, typeParam, componentTypeParam]],
        style: {
          bg_color: '#fc2929',
          text_color: '#ffffff'
        }
      }, {
        name: 'Candidate',
        value: summaryCcExtInfo.numberOfTotalCcExtByStates['Candidate'] || 0,
        href: ['/core_component', [{key: 'states', value: 'Candidate'}, releaseParam, typeParam, componentTypeParam]],
        style: {
          bg_color: '#cc317c',
          text_color: '#ffffff'
        }
      }, {
        name: 'Published',
        value: summaryCcExtInfo.numberOfTotalCcExtByStates['Published'] || 0,
        href: ['/core_component', [{key: 'states', value: 'Published'}, releaseParam, typeParam, componentTypeParam]],
        style: {
          bg_color: '#84b6eb',
          text_color: '#ffffff'
        }
      }];

      this.numberOfMyCcExtByStates = [{
        name: 'Editing',
        value: summaryCcExtInfo.numberOfMyCcExtByStates['Editing'] || 0,
        href: ['/core_component', [{key: 'states', value: 'Editing'}, releaseParam, typeParam, componentTypeParam, ownerLoginIdsParam]],
        style: {
          bg_color: '#fc2929',
          text_color: '#ffffff'
        }
      }, {
        name: 'Candidate',
        value: summaryCcExtInfo.numberOfMyCcExtByStates['Candidate'] || 0,
        href: ['/core_component', [{key: 'states', value: 'Candidate'}, releaseParam, typeParam, componentTypeParam, ownerLoginIdsParam]],
        style: {
          bg_color: '#cc317c',
          text_color: '#ffffff'
        }
      }, {
        name: 'Published',
        value: summaryCcExtInfo.numberOfMyCcExtByStates['Published'] || 0,
        href: ['/core_component', [{key: 'states', value: 'Published'}, releaseParam, typeParam, componentTypeParam, ownerLoginIdsParam]],
        style: {
          bg_color: '#84b6eb',
          text_color: '#ffffff'
        }
      }];

      const numberOfCcExtsByUsersAndStatesData = [];
      this.numberOfCcExtsByUsers_usernameList = [];
      for (const username of this.keys(summaryCcExtInfo.ccExtByUsersAndStates)) {
        numberOfCcExtsByUsersAndStatesData.push({
          username,
          Editing: summaryCcExtInfo.ccExtByUsersAndStates[username]['Editing'] || 0,
          Candidate: summaryCcExtInfo.ccExtByUsersAndStates[username]['Candidate'] || 0,
          Published: summaryCcExtInfo.ccExtByUsersAndStates[username]['Published'] || 0,
          total: (summaryCcExtInfo.ccExtByUsersAndStates[username]['Editing'] || 0) +
            (summaryCcExtInfo.ccExtByUsersAndStates[username]['Candidate'] || 0) +
            (summaryCcExtInfo.ccExtByUsersAndStates[username]['Published'] || 0)
        });
        this.numberOfCcExtsByUsers_usernameList.push(username);
      }
      this.numberOfCcExtsByUsersAndStates.data = numberOfCcExtsByUsersAndStatesData;

      initFilter(
        this.numberOfCcExtsByUsers_usernameListFilterCtrl,
        this.numberOfCcExtsByUsers_usernameFilteredList,
        this.numberOfCcExtsByUsers_usernameList
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
