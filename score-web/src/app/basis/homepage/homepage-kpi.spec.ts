import {ChangeDetectorRef} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {provideRouter} from '@angular/router';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {of} from 'rxjs';
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {BasisModule} from '../basis.module';
import {HomepageComponent} from './homepage.component';
import {AuthService} from '../../authentication/auth.service';
import {WebPageInfoService} from '../basis.service';
import {WebPageInfo} from '../about/domain/about';
import {CcListService} from '../../cc-management/cc-list/domain/cc-list.service';
import {BieListService} from '../../bie-management/bie-list/domain/bie-list.service';
import {CodeListService} from '../../code-list-management/domain/code-list.service';
import {AgencyIdListService} from '../../agency-id-list-management/domain/agency-id-list.service';
import {NamespaceService} from '../../namespace-management/domain/namespace.service';
import {ReleaseService} from '../../release-management/domain/release.service';
import {LibraryService} from '../../library-management/domain/library.service';

const RELEASE = {releaseId: 7, releaseNum: 'Working', workingRelease: true, state: 'Initialized'};
const CC_SUMMARY = {
  numberOfTotalCcByStates: {}, numberOfMyCcByStates: {}, ccByUsersAndStates: {}, myRecentCCs: [],
  ccByTypesUsersAndStates: {ACC: {alice: {WIP: 5, Draft: 2}, bob: {WIP: 1}}},
};
const BIE_SUMMARY = {
  numberOfTotalBieByStates: {WIP: 4}, numberOfMyBieByStates: {}, bieByUsersAndStates: {alice: {WIP: 4}}, myRecentBIEs: [],
};
const EXT_SUMMARY = {numberOfTotalCcExtByStates: {}, numberOfMyCcExtByStates: {}, ccExtByUsersAndStates: {}, myExtensionsUnusedInBIEs: []};

/** The colours of the configuration, as the settings screen stores them. */
function configuration(colors: {[state: string]: {background: string, font: string}}): WebPageInfo {
  return new WebPageInfo({componentStateColorSetMap: colors} as any);
}

/** The colour as the browser spells it back (`#112233` is `rgb(17, 34, 51)`). */
function rgb(hex: string): string {
  const probe = document.createElement('span');
  probe.style.color = hex;
  return probe.style.color;
}

async function render(config?: WebPageInfo, userToken = {username: 'alice', roles: ['developer'], tenant: {enabled: false}}): Promise<ComponentFixture<HomepageComponent>> {
  TestBed.configureTestingModule({
    imports: [BasisModule, NoopAnimationsModule],
    providers: [
      provideHttpClient(), provideHttpClientTesting(), provideRouter([]),
      {provide: AuthService, useValue: {getUserToken: () => userToken}},
      {provide: LibraryService, useValue: {getLibrarySummaryList: () => of([{libraryId: 1, name: 'connectSpec', isDefault: true}])}},
      {provide: CodeListService, useValue: {getCodeListListPage: () => of({list: []})}},
      {provide: AgencyIdListService, useValue: {getAgencyIdListListPage: () => of({list: []})}},
      {provide: NamespaceService, useValue: {getNamespaceSummaries: () => of([])}},
      {provide: ReleaseService, useValue: {getReleaseSummaryList: () => of([RELEASE]), getWorkingRelease: () => of(RELEASE)}},
      {provide: CcListService, useValue: {getSummaryCcList: () => of(CC_SUMMARY), getSummaryCcExtList: () => of(EXT_SUMMARY)}},
      {provide: BieListService, useValue: {getSummaryBieList: () => of(BIE_SUMMARY)}},
    ],
  });
  if (config) {
    TestBed.inject(WebPageInfoService).set(config);
  }
  const fixture = TestBed.createComponent(HomepageComponent);
  fixture.detectChanges();
  await fixture.whenStable();
  fixture.detectChanges();
  return fixture;
}

/** What the application does when the configuration arrives: its views are checked again. */
function applyChanges(fixture: ComponentFixture<HomepageComponent>) {
  fixture.debugElement.injector.get(ChangeDetectorRef).detectChanges();
}

describe('Home states take their colours from the configuration', () => {
  beforeEach(() => localStorage.clear());
  afterEach(() => localStorage.clear());

  /** What the first card (Core Components) and the users panel below it draw of a state. */
  function drawn(fixture: ComponentFixture<HomepageComponent>, state: string) {
    const root: HTMLElement = fixture.nativeElement;
    const summary = root.querySelector('score-state-summary') as HTMLElement;
    const chip = Array.from(summary.querySelectorAll('mat-chip')).find(c => c.textContent!.trim().startsWith(state)) as HTMLElement;
    const index = ['WIP', 'Draft'].indexOf(state);
    const ring = summary.querySelectorAll('circle.segment')[index];
    const legend = Array.from(root.querySelectorAll('score-user-state-ranking .legend li'))
      .find(li => li.textContent!.trim() === state)!.querySelector('.dot') as HTMLElement;
    const bar = Array.from(root.querySelectorAll('score-user-state-ranking a.seg')).find(a => (a.getAttribute('title') ?? '').startsWith(state)) as HTMLElement;
    return {
      chipBackground: chip.style.getPropertyValue('--mat-chip-elevated-container-color'),
      chipText: chip.style.getPropertyValue('--mat-chip-label-text-color'),
      ring: ring.getAttribute('stroke'),
      legend: legend.style.backgroundColor,
      barBackground: bar.style.backgroundColor,
      barText: bar.style.color,
    };
  }

  it('draws the chip, the ring, the legend and the bar of a state in the colours that the configuration names', async () => {
    const fixture = await render(configuration({
      WIP: {background: '#112233', font: '#ddeeff'},
      Draft: {background: '#445566', font: '#ffeedd'},
    }));
    const wip = drawn(fixture, 'WIP');
    expect(wip.chipBackground).toBe('#112233');
    expect(wip.chipText).toBe('#ddeeff');
    expect(wip.ring).toBe('#112233');
    expect(wip.legend).toBe(rgb('#112233'));
    expect(wip.barBackground).toBe(rgb('#112233'));
    expect(wip.barText).toBe(rgb('#ddeeff'));
    const draft = drawn(fixture, 'Draft');
    expect(draft.chipBackground).toBe('#445566');
    expect(draft.ring).toBe('#445566');
    expect(draft.barBackground).toBe(rgb('#445566'));
  });

  it('applies a configuration that changes after the page is drawn, without a new load', async () => {
    const fixture = await render(configuration({WIP: {background: '#112233', font: '#ffffff'}, Draft: {background: '#445566', font: '#ffffff'}}));
    const service = TestBed.inject(WebPageInfoService);
    expect(drawn(fixture, 'WIP').ring).toBe('#112233');

    service.set(configuration({WIP: {background: '#aa0000', font: '#00ff00'}, Draft: {background: '#00aa00', font: '#ff00ff'}}));
    applyChanges(fixture);

    const wip = drawn(fixture, 'WIP');
    expect(wip.chipBackground).toBe('#aa0000');
    expect(wip.chipText).toBe('#00ff00');
    expect(wip.ring).toBe('#aa0000');
    expect(wip.legend).toBe(rgb('#aa0000'));
    expect(wip.barBackground).toBe(rgb('#aa0000'));
    expect(wip.barText).toBe(rgb('#00ff00'));
    expect(drawn(fixture, 'Draft').ring).toBe('#00aa00');
  });

  it('applies the configuration that arrives after the first draw (the page info is fetched on the first visit)', async () => {
    const fixture = await render();
    const before = drawn(fixture, 'WIP');
    TestBed.inject(WebPageInfoService).set(configuration({WIP: {background: '#123456', font: '#654321'}}));
    applyChanges(fixture);
    const after = drawn(fixture, 'WIP');
    expect(before.ring).not.toBe('#123456');
    expect(after.ring).toBe('#123456');
    expect(after.chipText).toBe('#654321');
  });

  it('draws a state that the configuration does not name in the colours of the home page', async () => {
    const fixture = await render();
    const wip = drawn(fixture, 'WIP');
    expect(wip.ring).toBe('#D32F2F');
    expect(wip.chipText).toBe('#ffffff');
    expect(drawn(fixture, 'Draft').ring).toBe('#7B1FA2');
  });
});
