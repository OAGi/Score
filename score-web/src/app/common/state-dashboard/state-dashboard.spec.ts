import {TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {base64Decode} from '../utility';
import {StateProgressBarItem} from '../state-progress-bar/state-progress-bar';
import {StateSummaryComponent} from './state-summary.component';
import {UserStateRankingComponent} from './user-state-ranking.component';

function item(name: string, value: number, extra: any[] = []): StateProgressBarItem {
  return {
    name, value, disabled: false,
    href: ['/core_component', [{key: 'states', value: name}, ...extra]],
    style: {bg_color: '#123456', text_color: '#fff'}
  };
}

describe('state dashboard components', () => {
  beforeEach(() => TestBed.configureTestingModule({providers: [provideRouter([])]}));

  describe('StateSummaryComponent', () => {
    function render(total: StateProgressBarItem[], mine?: StateProgressBarItem[]) {
      const fixture = TestBed.createComponent(StateSummaryComponent);
      fixture.componentInstance.total = total;
      fixture.componentInstance.mine = mine;
      fixture.detectChanges();
      return fixture;
    }

    it('shows the total in the ring and a chip per non-empty state', () => {
      const el: HTMLElement = render([item('WIP', 3), item('Draft', 0), item('QA', 2)]).nativeElement;
      expect(el.querySelector('.total')?.textContent?.trim()).toBe('5');
      expect(Array.from(el.querySelectorAll('mat-chip')).map(c => c.textContent?.trim())).toEqual(['WIP 3', 'QA 2']);
    });

    it('shows a second donut, with its own chips, for the user\'s own items', () => {
      const el: HTMLElement = render([item('WIP', 3)], [item('WIP', 1)]).nativeElement;
      expect(Array.from(el.querySelectorAll('.figure .total')).map(t => t.textContent?.trim())).toEqual(['3', '1']);
      expect(Array.from(el.querySelectorAll('mat-chip')).map(c => c.textContent?.trim())).toEqual(['WIP 3', 'WIP 1']);
    });

    it('shows one donut without the user\'s own items, and no chip when there is nothing', () => {
      expect(render([item('WIP', 3)]).nativeElement.querySelectorAll('.figure').length).toBe(1);
      expect(render([item('WIP', 0)]).nativeElement.querySelectorAll('mat-chip').length).toBe(0);
    });
  });

  describe('UserStateRankingComponent', () => {
    const states = [{state: 'WIP', background: '#f00', font: '#fff'}, {state: 'Draft', background: '#00f', font: '#fff'}];
    const rows = [
      {username: 'a', WIP: 1, Draft: 0, total: 1},
      {username: 'b', WIP: 4, Draft: 2, total: 6},
      {username: 'c', WIP: 0, Draft: 3, total: 3},
      {username: 'zero', WIP: 0, Draft: 0, total: 0},
    ];

    function render(limit = 5) {
      const fixture = TestBed.createComponent(UserStateRankingComponent);
      fixture.componentInstance.states = states;
      fixture.componentInstance.baseParams = [{key: 'releaseId', value: 7}];
      fixture.componentInstance.limit = limit;
      fixture.componentInstance.rows = rows;
      fixture.detectChanges();
      return fixture;
    }

    const users = (el: HTMLElement) => Array.from(el.querySelectorAll('a.user')).map(e => e.textContent?.trim());

    it('lists the users by total, largest first, leaving out users without items', () => {
      expect(users(render().nativeElement)).toEqual(['b', 'c', 'a']);
    });

    it('shows only the top users until all are asked for', () => {
      const fixture = render(2);
      const el: HTMLElement = fixture.nativeElement;
      expect(users(el)).toEqual(['b', 'c']);
      (el.querySelector('.more') as HTMLButtonElement).click();
      fixture.detectChanges();
      expect(users(el)).toEqual(['b', 'c', 'a']);
    });

    it('says what there is none of, when no user has an item', () => {
      const fixture = TestBed.createComponent(UserStateRankingComponent);
      fixture.componentInstance.emptyText = 'No BIEs yet';
      fixture.componentInstance.rows = [];
      fixture.detectChanges();
      expect(fixture.nativeElement.textContent).toContain('No BIEs yet');
    });

    it('links a segment to the owner, the state and the base filter', () => {
      const el: HTMLElement = render().nativeElement;
      const seg = el.querySelector('a.seg') as HTMLAnchorElement;
      expect(seg.textContent?.trim()).toBe('4');
      const q = new URL(seg.href).searchParams.get('q');
      expect(base64Decode(q!)).toBe('states=WIP&ownerLoginIdList=b&releaseId=7');
    });
  });
});
