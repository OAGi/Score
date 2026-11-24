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

describe('Home cards are a carousel', () => {
  const WIDTH = 1000;
  let fixture: ComponentFixture<HomepageComponent>;
  let component: HomepageComponent;
  let track: HTMLElement;
  let scrollLeft: number;
  let scrollTo: ReturnType<typeof vi.fn>;

  /** A track of three cards as wide as itself, scrolled by `scrollLeft`. */
  function layOut() {
    scrollLeft = 0;
    scrollTo = vi.fn();
    Object.defineProperty(track, 'scrollLeft', {configurable: true, get: () => scrollLeft, set: (v: number) => scrollLeft = v});
    Object.defineProperty(track, 'scrollWidth', {configurable: true, value: WIDTH * 3});
    Object.defineProperty(track, 'clientWidth', {configurable: true, value: WIDTH});
    track.scrollTo = scrollTo as any;
    track.getBoundingClientRect = () => ({left: 0, width: WIDTH, right: WIDTH} as DOMRect);
    Array.from(track.children).forEach((card, i) => {
      (card as HTMLElement).getBoundingClientRect = () => ({left: i * WIDTH - scrollLeft, width: WIDTH, right: (i + 1) * WIDTH - scrollLeft} as DOMRect);
    });
  }

  const mouse = (clientX: number, extra: object = {}) => ({pointerType: 'mouse', button: 0, pointerId: 1, clientX, ...extra}) as any;

  beforeEach(async () => {
    localStorage.clear();
    vi.useFakeTimers();
    fixture = await render();
    component = fixture.componentInstance;
    track = fixture.nativeElement.querySelector('.kpi-grid');
    layOut();
  });

  afterEach(() => {
    vi.useRealTimers();
    localStorage.clear();
  });

  it('has a card, and a dot, for each of the sections', () => {
    expect(component.kpiSections).toEqual(['cc', 'bie', 'ext']);
    expect(track.children.length).toBe(3);
    expect(fixture.nativeElement.querySelectorAll('.kpi-dot').length).toBe(3);
  });

  it('slides to a card that is further away than the next one, as it does to the next one', () => {
    component.selectKpi(track, 2);
    expect(component.selectedSection).toBe('ext');
    expect(scrollTo).toHaveBeenCalledWith({left: 2 * WIDTH, behavior: 'smooth'});

    component.selectKpi(track, 1);
    expect(scrollTo).toHaveBeenLastCalledWith({left: WIDTH, behavior: 'smooth'});
  });

  it('does not snap the track on the way to a card, and snaps it again once it is there', () => {
    expect(component.kpiSliding).toBe(false);
    component.selectKpi(track, 2);
    expect(component.kpiSliding).toBe(true);
    scrollLeft = WIDTH;
    component.onKpiScroll(track);
    expect(component.kpiSliding).toBe(true);
    scrollLeft = 2 * WIDTH;
    component.onKpiScroll(track);
    expect(component.kpiSliding).toBe(false);
  });

  it('finishes a slide that the browser stopped short, so that the card that was asked for is shown', () => {
    component.selectKpi(track, 2);
    scrollTo.mockClear();
    scrollLeft = 1.7 * WIDTH;           // the scroll ended on the way
    component.onKpiScrollEnd(track);
    expect(scrollTo).toHaveBeenCalledWith({left: 2 * WIDTH, behavior: 'instant'});
    expect(component.kpiSliding).toBe(false);
  });

  it('leaves the track alone when a scroll that nobody asked for ends', () => {
    scrollLeft = WIDTH;
    component.onKpiScrollEnd(track);
    expect(scrollTo).not.toHaveBeenCalled();
  });

  it('does not let the scroll on the way choose another card', () => {
    component.selectKpi(track, 2);
    scrollLeft = WIDTH;            // on the way, at the second card
    component.onKpiScroll(track);
    vi.advanceTimersByTime(500);
    expect(component.selectedSection).toBe('ext');

    scrollLeft = 2 * WIDTH;        // arrived
    component.onKpiScroll(track);
    vi.advanceTimersByTime(500);
    expect(component.selectedSection).toBe('ext');
  });

  it('does not take the card back to where it was while the scroll starts slowly', () => {
    component.selectKpi(track, 2);
    scrollLeft = 2 * WIDTH;
    component.onKpiScroll(track);                  // arrived at the third card
    vi.advanceTimersByTime(300);
    expect(component.selectedSection).toBe('ext');

    component.selectKpi(track, 0);                 // back to the first: the scroll is slow to start
    expect(component.selectedSection).toBe('cc');
    for (let i = 0; i < 4; i++) {
      vi.advanceTimersByTime(250);                 // a pause longer than the settle time of a swipe, again and again
      component.onKpiScroll(track);                // still at the third card
      vi.advanceTimersByTime(250);
      expect(component.selectedSection).toBe('cc');
    }
    scrollLeft = 0;
    component.onKpiScroll(track);                  // arrived
    vi.advanceTimersByTime(500);
    expect(component.selectedSection).toBe('cc');
  });

  it('does not let what an earlier swipe left to be chosen take the card back after a dot was clicked', () => {
    scrollLeft = 2 * WIDTH;
    component.onKpiScroll(track);                  // a swipe stops at the third card, to be chosen in 120ms
    component.selectKpi(track, 0);                 // a dot is clicked before that
    vi.advanceTimersByTime(500);
    expect(component.selectedSection).toBe('cc');
  });

  it('lets a swipe choose a card after a dot that was already there', () => {
    component.selectKpi(track, 0);                 // the track is at the first card already: nothing to wait for
    scrollLeft = WIDTH;
    component.onKpiScroll(track);
    vi.advanceTimersByTime(200);
    expect(component.selectedSection).toBe('bie');
  });

  it('lets a finger take the track over from a dot that is on its way', () => {
    component.selectKpi(track, 2);
    component.onKpiPointerDown(track, mouse(500, {pointerType: 'touch'}));
    scrollLeft = WIDTH;
    component.onKpiScroll(track);
    vi.advanceTimersByTime(200);
    expect(component.selectedSection).toBe('bie');
  });

  it('lets a wheel take the track over from a dot that is on its way', () => {
    component.selectKpi(track, 2);
    component.releaseKpiNavigation();
    scrollLeft = WIDTH;
    component.onKpiScroll(track);
    vi.advanceTimersByTime(200);
    expect(component.selectedSection).toBe('bie');
  });

  it('chooses the card that a swipe stops at, once the swipe has settled', () => {
    scrollLeft = WIDTH;
    component.onKpiScroll(track);
    expect(component.selectedSection).toBe('cc');
    vi.advanceTimersByTime(200);
    expect(component.selectedSection).toBe('bie');
  });

  it('is dragged with the mouse: a drag of more than a fifth of the card goes to the next one', () => {
    component.onKpiPointerDown(track, mouse(600));
    component.onKpiPointerMove(track, mouse(400));
    expect(component.kpiDragging).toBe(true);
    expect(scrollLeft).toBe(200);
    component.onKpiPointerUp(track, mouse(400));
    expect(component.kpiDragging).toBe(false);
    expect(component.selectedSection).toBe('bie');
    expect(scrollTo).toHaveBeenLastCalledWith({left: WIDTH - scrollLeft + scrollLeft, behavior: 'smooth'});
  });

  it('is dragged with the mouse: a short drag comes back, and a drag the other way goes to the one before', () => {
    component.onKpiPointerDown(track, mouse(600));
    component.onKpiPointerMove(track, mouse(500));
    component.onKpiPointerUp(track, mouse(500));
    expect(component.selectedSection).toBe('cc');

    scrollLeft = WIDTH;
    component.selectKpi(track, 1);
    vi.advanceTimersByTime(2000);
    component.onKpiPointerDown(track, mouse(100));
    component.onKpiPointerMove(track, mouse(500));
    component.onKpiPointerUp(track, mouse(500));
    expect(component.selectedSection).toBe('cc');
  });

  it('is not dragged by a touch or by a button other than the main one (a touch screen scrolls by itself)', () => {
    component.onKpiPointerDown(track, mouse(600, {pointerType: 'touch'}));
    component.onKpiPointerMove(track, mouse(300));
    expect(component.kpiDragging).toBe(false);
    component.onKpiPointerDown(track, mouse(600, {button: 2}));
    component.onKpiPointerMove(track, mouse(300));
    expect(component.kpiDragging).toBe(false);
    expect(scrollLeft).toBe(0);
  });

  it('does not open what is under the mouse when a drag ends', () => {
    const opened = vi.fn();
    const chip = document.createElement('a');
    chip.addEventListener('click', opened);
    track.children[0].appendChild(chip);

    component.onKpiPointerDown(track, mouse(600));
    component.onKpiPointerMove(track, mouse(300));
    component.onKpiPointerUp(track, mouse(300));
    chip.dispatchEvent(new MouseEvent('click', {bubbles: true}));
    expect(opened).not.toHaveBeenCalled();

    vi.advanceTimersByTime(500);
    chip.dispatchEvent(new MouseEvent('click', {bubbles: true}));
    expect(opened).toHaveBeenCalledTimes(1);
  });

  it('leaves a click that is not a drag to what is under the mouse', () => {
    const opened = vi.fn();
    const chip = document.createElement('a');
    chip.addEventListener('click', opened);
    track.children[0].appendChild(chip);
    component.onKpiPointerDown(track, mouse(600));
    component.onKpiPointerMove(track, mouse(598));
    component.onKpiPointerUp(track, mouse(598));
    chip.dispatchEvent(new MouseEvent('click', {bubbles: true}));
    expect(opened).toHaveBeenCalledTimes(1);
  });
});
