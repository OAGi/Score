import {ccCountOfState, ccCountsByUsersAndStates} from './cc-summary-counts';
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
import {CompactRecord} from '../../common/record-compact-row/record-compact-row.component';
import {UsersView} from '../../common/state-dashboard/view-toggle.component';
import {faChevronLeft, faChevronRight} from '@fortawesome/free-solid-svg-icons';
import {RankingState} from '../../common/state-dashboard/user-state-ranking.component';
import {AuthService} from '../../authentication/auth.service';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {base64Encode, initFilter, loadBranch, loadLibrary, saveBranch, saveLibrary} from '../../common/utility';
import {CcListEntry, SummaryCcExt} from '../../cc-management/cc-list/domain/cc-list';
import {ReleaseSummary} from '../../release-management/domain/release';
import {ReleaseService} from '../../release-management/domain/release.service';
import {WebPageInfoService} from '../basis.service';
import {LibrarySummary} from '../../library-management/domain/library';
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

  myRecentCCs = new MatTableDataSource<CcListEntry>();
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
  releaseFilteredList: ReplaySubject<ReleaseSummary[]> = new ReplaySubject<ReleaseSummary[]>(1);
  selectedRelease: ReleaseSummary;
  workingRelease: ReleaseSummary;

  myRecentBIEs = new MatTableDataSource<BieListEntry>();
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
  /** The same extensions, as the blocks of the chart view. */
  myExtensionsUnusedInBIEsRecords: CompactRecord[] = [];
  @ViewChild('myExtensionsUnusedInBIEsPaginator', {static: false})
  myExtensionsUnusedInBIEsPaginator: MatPaginator;
  @ViewChild('myExtensionsUnusedInBIEsSort', {static: false})
  myExtensionsUnusedInBIEsSort: MatSort;
  /* End of User Extensions */

  UEGValue = UserExtensionGroup.value;

  /** The sections of the cards, in the order of the cards. */
  get kpiSections(): ('cc' | 'bie' | 'ext')[] {
    return [...(this.isDeveloper ? ['cc' as const] : []), 'bie', ...(this.isTenantEnabled ? [] : ['ext' as const])];
  }

  private kpiScrollTimer: ReturnType<typeof setTimeout> | undefined;
  private kpiNavigatingTimer: ReturnType<typeof setTimeout> | undefined;
  /** The card that a dot or an arrow is taking the track to: the scroll on the way does not choose a card. */
  private kpiNavigatingTo: number | undefined;
  private kpiTargetIndex = 0;
  /** The track slides to a card that a dot or an arrow asked for: it does not snap on the way, as that cuts the slide short. */
  kpiSliding = false;
  private kpiDrag: {startX: number, startLeft: number, from: number, moved: boolean} | undefined;
  /** The track is being dragged with the mouse (a touch screen scrolls it by itself). */
  kpiDragging = false;

  readonly faChevronLeft = faChevronLeft;
  readonly faChevronRight = faChevronRight;

  get kpiIndex(): number {
    return Math.max(this.kpiSections.indexOf(this.selectedSection), 0);
  }

  /** On a narrow screen the cards are a carousel: the card that is in view is the selected one. */
  onKpiScroll(track: HTMLElement) {
    if (track.scrollWidth <= track.clientWidth + 1) {
      return;
    }
    if (this.kpiNavigatingTo !== undefined) {
      if (Math.abs(track.scrollLeft - this.kpiLeftOf(track, this.kpiTargetIndex)) < 2) {
        this.finishKpiSlide();
      }
      return;
    }
    const nearest = this.nearestKpiCard(track);
    const section = this.kpiSections[nearest];
    // The section below is swapped once the swipe has settled: swapping it on the way makes the page flicker.
    clearTimeout(this.kpiScrollTimer);
    this.kpiScrollTimer = setTimeout(() => {
      if (section && section !== this.selectedSection) {
        this.selectedSection = section;
      }
    }, 120);
  }

  /** The card whose middle is the nearest to the middle of the track. */
  private nearestKpiCard(track: HTMLElement): number {
    const middle = track.getBoundingClientRect().left + track.clientWidth / 2;
    let nearest = 0;
    let distance = Number.POSITIVE_INFINITY;
    (Array.from(track.children) as HTMLElement[]).forEach((card, index) => {
      const box = card.getBoundingClientRect();
      const d = Math.abs(box.left + box.width / 2 - middle);
      if (d < distance) {
        distance = d;
        nearest = index;
      }
    });
    return nearest;
  }

  /** Where the track is scrolled to when it shows a card. */
  private kpiLeftOf(track: HTMLElement, index: number): number {
    const card = track.children[index] as HTMLElement;
    return track.scrollLeft + card.getBoundingClientRect().left - track.getBoundingClientRect().left;
  }

  /**
   * The browser has stopped scrolling. A slide that it stopped short (the page below changes with the section, and with it the width of
   * the track) is finished at once, so that the track shows the card that was asked for.
   */
  onKpiScrollEnd(track: HTMLElement) {
    if (this.kpiNavigatingTo !== undefined) {
      const left = this.kpiLeftOf(track, this.kpiTargetIndex);
      if (Math.abs(track.scrollLeft - left) >= 2) {
        track.scrollTo({left, behavior: 'instant'});
      }
    }
    this.finishKpiSlide();
  }

  /** Takes the track to a card, sliding, however far it is. */
  selectKpi(track: HTMLElement, index: number) {
    if (index < 0 || index >= this.kpiSections.length) {
      return;
    }
    this.selectedSection = this.kpiSections[index];
    clearTimeout(this.kpiScrollTimer);
    const card = track.children[index] as HTMLElement | undefined;
    if (!card) {
      return;
    }
    // Only the track is scrolled: `scrollIntoView` also scrolls the page, and the swap of the section below interrupts it.
    const left = this.kpiLeftOf(track, index);
    // What an earlier swipe left to be chosen is not chosen over the card that was asked for.
    clearTimeout(this.kpiScrollTimer);
    if (Math.abs(left - track.scrollLeft) < 1) {
      this.finishKpiSlide();
      return;
    }
    this.kpiNavigatingTo = index;
    this.kpiTargetIndex = index;
    this.kpiSliding = true;
    clearTimeout(this.kpiNavigatingTimer);
    // Only a safety: the slide ends when the card is reached, or when a hand or a wheel takes the track over.
    this.kpiNavigatingTimer = setTimeout(() => this.finishKpiSlide(), 5000);
    track.scrollTo({left, behavior: 'smooth'});
  }

  /** The mouse drags the track as a finger does. */
  /** A hand on the track (a finger, the mouse, a wheel) takes it over from a dot or an arrow that is taking it somewhere. */
  releaseKpiNavigation() {
    this.finishKpiSlide();
  }

  /** The slide to a card is over (it arrived, or it was taken over): the track snaps again, and a swipe chooses a card again. */
  finishKpiSlide() {
    clearTimeout(this.kpiNavigatingTimer);
    this.kpiNavigatingTo = undefined;
    this.kpiSliding = false;
  }

  onKpiPointerDown(track: HTMLElement, event: PointerEvent) {
    this.releaseKpiNavigation();
    if (event.pointerType !== 'mouse' || event.button !== 0) {
      return;
    }
    this.kpiDrag = {startX: event.clientX, startLeft: track.scrollLeft, from: this.nearestKpiCard(track), moved: false};
  }

  onKpiPointerMove(track: HTMLElement, event: PointerEvent) {
    const drag = this.kpiDrag;
    if (!drag) {
      return;
    }
    const dx = event.clientX - drag.startX;
    if (!drag.moved) {
      if (Math.abs(dx) < 5) {
        return;
      }
      drag.moved = true;
      this.kpiDragging = true;
      track.setPointerCapture?.(event.pointerId);
      // The click that ends a drag is not a click on what is under the mouse (a link, a chip).
      const swallow = (e: Event) => {
        e.stopPropagation();
        e.preventDefault();
      };
      track.addEventListener('click', swallow, {capture: true});
      setTimeout(() => track.removeEventListener('click', swallow, {capture: true}), 400);
    }
    track.scrollLeft = drag.startLeft - dx;
  }

  onKpiPointerUp(track: HTMLElement, event: PointerEvent) {
    const drag = this.kpiDrag;
    this.kpiDrag = undefined;
    if (!drag?.moved) {
      return;
    }
    track.releasePointerCapture?.(event.pointerId);
    this.kpiDragging = false;
    const dx = event.clientX - drag.startX;
    // A drag of more than a fifth of the card goes to the next one; a shorter one comes back.
    const threshold = track.clientWidth / 5;
    const index = dx <= -threshold ? drag.from + 1 : (dx >= threshold ? drag.from - 1 : drag.from);
    this.selectKpi(track, Math.min(Math.max(index, 0), this.kpiSections.length - 1));
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

  get bieBaseParams(): {key: string, value: any}[] {
    return [{key: 'releaseIds', value: this.selectedRelease?.releaseId}];
  }

  get ccExtBaseParams(): {key: string, value: any}[] {
    return [
      {key: 'releaseId', value: this.selectedRelease?.releaseId},
      {key: 'types', value: ['ACC']},
      {key: 'componentTypes', value: [UserExtensionGroup.value]}
    ];
  }

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
      this.releaseFilteredList.next([]);
      return;
    }

    const userToken = this.userToken;
    this.workingRelease = undefined;
    forkJoin([
      this.releaseService.getReleaseSummaryList(this.library.libraryId),
      this.releaseService.getWorkingRelease(this.library.libraryId)
    ]).subscribe(([resp, workingRelease]) => {
      this.workingRelease = workingRelease;
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

  onLibraryChange(library: LibrarySummary) {
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
    const styleOf = (state: string) => this.stateStyle(state);
    const releaseParams = this.workingRelease ?
      [{key: 'releaseId', value: this.workingRelease.releaseId}] :
      [];
    this.ccService.getSummaryCcList(this.library.libraryId).subscribe(summaryCcInfo => {
      const ccByUsersAndStates = ccCountsByUsersAndStates(summaryCcInfo.ccByTypesUsersAndStates);

      this.numberOfTotalCCByStates = [];
      this.numberOfMyCCByStates = [];
      for (const item of this.stateColorList) {
        this.numberOfTotalCCByStates.push({
          name: item.state,
          value: ccCountOfState(ccByUsersAndStates, item.state),
          href: ['/core_component', [{key: 'states', value: item.state}, ...releaseParams]],
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

      this.myRecentCCs.data = summaryCcInfo.myRecentCCs;
    });
  }

  initSummaryBIEs(userToken) {
    const styleOf = (state: string) => this.stateStyle(state);
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
          // Read when it is drawn, not when the item is made: the colours are the configuration's, which may arrive or change later.
          get style() {
            return styleOf(item.state);
          }
        });
        this.numberOfMyBieByStates.push({
          name: item.state,
          value: summaryBieInfo.numberOfMyBieByStates[item.state] || 0,
          href: ['/profile_bie', [{key: 'states', value: item.state}, {key: 'ownerLoginIdList', value: userToken.username}, releaseParam]],
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

      this.myRecentBIEs.data = summaryBieInfo.myRecentBIEs;
    });
  }

  initSummaryUserExtensions(userToken) {
    const styleOf = (state: string) => this.stateStyle(state);
    this.ccService.getSummaryCcExtList(this.library.libraryId, this.selectedRelease.releaseId).subscribe(summaryCcExtInfo => {
      const releaseParam = {key: 'releaseId', value: this.selectedRelease.releaseId};
      const typeParam = {key: 'types', value: 'ACC'};
      const componentTypeParam = {key: 'componentTypes', value: UserExtensionGroup.value};
      const ownerLoginIdListParam = {key: 'ownerLoginIdList', value: userToken.username};
      this.numberOfTotalCcExtByStates = [];
      this.numberOfMyCcExtByStates = [];
      for (const item of this.stateColorList) {
        this.numberOfTotalCcExtByStates.push({
          name: item.state,
          value: summaryCcExtInfo.numberOfTotalCcExtByStates[item.state] || 0,
          href: ['/core_component', [{key: 'states', value: item.state} , releaseParam, typeParam, componentTypeParam]],
          disabled: this.selectedRelease.releaseId < 0,
          // Read when it is drawn, not when the item is made: the colours are the configuration's, which may arrive or change later.
          get style() {
            return styleOf(item.state);
          }
        });
        this.numberOfMyCcExtByStates.push({
          name: item.state,
          value: summaryCcExtInfo.numberOfMyCcExtByStates[item.state] || 0,
          href: ['/core_component', [{key: 'states', value: item.state}, releaseParam, typeParam, componentTypeParam, ownerLoginIdListParam]],
          disabled: this.selectedRelease.releaseId < 0,
          // Read when it is drawn, not when the item is made: the colours are the configuration's, which may arrive or change later.
          get style() {
            return styleOf(item.state);
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
      this.myExtensionsUnusedInBIEsRecords = summaryCcExtInfo.myExtensionsUnusedInBIEs.map(e => ({
        badges: [{text: e.state, kind: 'state', cssClass: 'cc-state'}],
        title: e.objectClassTerm,
        link: '/core_component/extension/' + e.accManifestId,
        subtitle: e.guid,
        meta: [
          {label: 'Top-level BIE', value: e.den, link: '/profile_bie/' + e.topLevelAsbiepId},
          {label: 'Association', value: e.associationDen, priority: 2}
        ],
        updated: {when: e.lastUpdateTimestamp, by: e.lastUpdateUser}
      }));
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

    this.myRecentCCs.sort = this.myRecentCCsSort;
    this.myRecentBIEs.sort = this.myRecentBIEsSort;
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
