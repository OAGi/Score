import {Component, signal} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {WebPageInfoService} from '../../basis/basis.service';
import {CompactRecord, RecordCompactRowComponent} from './record-compact-row.component';
import {memoizeRow} from './memoize-row';

const RECORD: CompactRecord = {
  badges: [
    {text: 'Release Draft', kind: 'state', cssClass: 'cl-state', state: 'ReleaseDraft'},
    {text: 'v1.2'}
  ],
  isNew: true,
  title: 'A Code List',
  link: '/code_list/5',
  linkQueryParams: {type: 'ASBIE'},
  titleTag: 'CL',
  subtitle: 'guid-1',
  meta: [
    {label: 'Owner', value: 'owner1', strong: true},
    {label: 'Agency', value: 'Acme', priority: 4},
    {label: 'Based on', value: 'Base', link: '/code_list/1', priority: 2},
    {label: 'Reference', value: 'Ref', href: 'https://example.org/x', priority: 1},
    {label: 'Module', value: 'Model\\Nouns', priority: 4},
    {value: 'plain'}
  ],
  updated: {when: new Date(2026, 9, 26, 10, 56), by: 'updater1'}
};

@Component({
  imports: [RecordCompactRowComponent],
  template: `<score-record-compact-row [record]="record" [external]="external"><span class="slot">controls</span><button actions class="act">Add</button></score-record-compact-row>`
})
class HostComponent {
  record = RECORD;
  external = true;
}

/** The button of a row is chosen by a condition (as the Add and the Remove of a tenant's list are). */
@Component({
  imports: [RecordCompactRowComponent],
  template: `
    <score-record-compact-row [record]="record">
      @if (adding()) {
        <button actions class="add">Add</button>
      } @else {
        <button actions class="remove">Remove</button>
      }
    </score-record-compact-row>`
})
class ConditionalActionsHostComponent {
  record: CompactRecord = {title: 'T'};
  adding = signal(true);
}

describe('RecordCompactRowComponent', () => {
  const render = (change: (host: HostComponent) => void = () => undefined) => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: WebPageInfoService, useValue: {
            getComponentStateColorSet: () => ({background: 'rgb(1, 2, 3)', font: 'rgb(4, 5, 6)'}),
            getReleaseStateColorSet: () => ({background: 'rgb(7, 8, 9)', font: 'rgb(10, 11, 12)'})
          }
        }
      ]
    });
    const fixture = TestBed.createComponent(HostComponent);
    change(fixture.componentInstance);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  };

  it('shows the badges, the title with its link and tag, the subtitle, the values, the update and what is put inside', () => {
    const el = render();
    const state = el.querySelector('.rc-side span.cl-state') as HTMLElement;
    expect(state.textContent).toBe('Release Draft');
    expect(state.className).toContain('ReleaseDraft');
    expect(state.style.backgroundColor).toBe('rgb(1, 2, 3)');
    expect(el.querySelector('.rc-side mat-chip').textContent).toContain('v1.2');
    expect(el.querySelector('.rc-new').textContent).toBe('New');
    const link = el.querySelector('.rc-title a') as HTMLAnchorElement;
    expect(link.textContent).toBe('A Code List');
    expect(link.getAttribute('href')).toBe('/code_list/5?type=ASBIE');
    expect(link.getAttribute('target')).toBe('_blank');
    expect(el.querySelector('.bie-display-name').textContent).toBe('CL');
    expect(el.querySelector('.rc-subtitle').textContent.trim()).toBe('guid-1');
    const items = Array.from(el.querySelectorAll('.rc-meta-item'));
    // The label is apart from its value by a margin, not by a blank.
    expect(items.map(i => i.textContent.replace(/\s+/g, ''))).toEqual(['Ownerowner1', 'AgencyAcme', 'BasedonBase', 'ReferenceRef', 'ModuleModel\\Nouns', 'plain']);
    expect(items[0].querySelector('b').textContent).toBe('owner1');
    expect(items[2].querySelector('a').getAttribute('href')).toBe('/code_list/1');
    // An address outside of the application opens in a new tab.
    expect(items[3].querySelector('a').getAttribute('href')).toBe('https://example.org/x');
    expect(items[3].querySelector('a').getAttribute('target')).toBe('_blank');
    // Only the last of the values shown is cut, with an ellipsis, when the line is full.
    expect(items.map(i => i.classList.contains('last'))).toEqual([false, false, false, false, false, true]);
    // The update spans the lines of text, here the title, the muted line and the values, and sits at their foot.
    expect((el.querySelector('.rc-updated') as HTMLElement).style.getPropertyValue('--rc-text-rows')).toBe('3');
    expect(el.querySelector('.rc-updated').textContent).toContain('Updated 2026-10-26 10:56');
    expect(el.querySelector('.rc-updated').textContent).toContain('by updater1');
    expect(el.querySelector('.rc-slot .slot').textContent).toBe('controls');
    // What is marked `actions` goes to the right end of the row, not under the rest.
    expect(el.querySelector('.rc > .rc-actions .act').textContent).toBe('Add');
    expect(el.querySelector('.rc-slot .act')).toBeNull();
  });

  it('spans one line of text less when the record has no muted line, and one less again when it has no values', () => {
    const rows = (record: CompactRecord) => {
      TestBed.resetTestingModule();
      return (render(host => host.record = record).querySelector('.rc-updated') as HTMLElement).style.getPropertyValue('--rc-text-rows');
    };
    const updated = {when: new Date(2026, 0, 1), by: 'u'};
    expect(rows({title: 'T', meta: [{value: 'v'}], updated})).toBe('2');
    expect(rows({title: 'T', subtitle: 's', updated})).toBe('2');
    expect(rows({title: 'T', updated})).toBe('1');
  });

  it('takes the colours of a release state from the release colour set', () => {
    const el = render(host => host.record = {title: 'R', badges: [{text: 'Published', kind: 'state', cssClass: 'release-state', colors: 'release'}]});
    expect((el.querySelector('.rc-side span.release-state') as HTMLElement).style.backgroundColor).toBe('rgb(7, 8, 9)');
  });

  it('puts a button that a condition chooses at the right end of the row too', () => {
    TestBed.configureTestingModule({providers: [provideRouter([]), {provide: WebPageInfoService, useValue: {}}]});
    const fixture = TestBed.createComponent(ConditionalActionsHostComponent);
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.rc > .rc-actions .add').textContent).toBe('Add');
    fixture.componentInstance.adding.set(false);
    fixture.detectChanges();
    expect(el.querySelector('.rc > .rc-actions .remove').textContent).toBe('Remove');
    expect(el.querySelector('.rc-slot button')).toBeNull();
  });

  it('marks a record that is only a title, to be centred in its row', () => {
    expect(render(host => host.record = {title: 'Only'}).querySelector('.rc').classList.contains('rc-title-only')).toBe(true);
    TestBed.resetTestingModule();
    expect(render(host => host.record = {title: 'T', subtitle: 's'}).querySelector('.rc').classList.contains('rc-title-only')).toBe(false);
  });

  it('says what the time is of, and bolds a title when told so', () => {
    const el = render(host => host.record = {title: 'Unread', titleStrong: true, updated: {when: new Date(2026, 0, 1, 9, 30), label: 'Created'}});
    expect(el.querySelector('.rc-updated span').textContent).toBe('Created 2026-01-01 09:30');
    expect(el.querySelector('.rc-title').classList.contains('rc-title-strong')).toBe(true);
    TestBed.resetTestingModule();
    const other = render(host => host.record = {title: 'Read', updated: {when: new Date(2026, 0, 1, 9, 30)}});
    expect(other.querySelector('.rc-updated span').textContent).toBe('Updated 2026-01-01 09:30');
    expect(other.querySelector('.rc-title').classList.contains('rc-title-strong')).toBe(false);
  });

  it('dims a title when told so, and puts the title in the middle of the update when there is nothing else', () => {
    const el = render(host => host.record = {title: 'Read', titleMuted: true, updated: {when: new Date(2026, 0, 1)}});
    expect(el.querySelector('.rc-title').classList.contains('rc-title-muted')).toBe(true);
    expect(el.querySelector('.rc-title').classList.contains('rc-title-middle')).toBe(true);
    TestBed.resetTestingModule();
    const other = render(host => host.record = {title: 'T', subtitle: 's', updated: {when: new Date(2026, 0, 1)}});
    expect(other.querySelector('.rc-title').classList.contains('rc-title-middle')).toBe(false);
    expect(other.querySelector('.rc-title').classList.contains('rc-title-muted')).toBe(false);
  });

  it('shows the creation with the update, of the same look and with the name after `by` in bold', () => {
    const el = render(host => host.record = {
      title: 'R', subtitle: 'guid',
      created: {when: new Date(2026, 0, 1, 9, 30), by: 'maker'}, updated: {when: new Date(2026, 1, 2, 10, 45), by: 'editor'}
    });
    const blocks = Array.from(el.querySelectorAll('.rc-grid > .rc-updated')) as HTMLElement[];
    expect(blocks.map(b => b.classList.contains('rc-created'))).toEqual([true, false]);
    expect(blocks.map(b => b.querySelector('span').textContent)).toEqual(['Created 2026-01-01 09:30', 'Updated 2026-02-02 10:45']);
    expect(blocks.map(b => b.querySelector('b').textContent)).toEqual(['maker', 'editor']);
    // The update spans the lines of text, the creation too, and sits at their foot.
    expect(blocks[1].style.getPropertyValue('--rc-text-rows')).toBe('3');
  });

  it('says what the title and the muted line are, when told', () => {
    const el = render(host => host.record = {title: '1', titleLabel: 'Context Scheme Value', subtitle: 'one', subtitleLabel: 'Meaning'});
    expect(el.querySelector('.rc-title .rc-title-label').textContent).toBe('Context Scheme Value');
    // The label is apart from what it labels by a margin, not by a blank.
    expect(el.querySelector('.rc-title').textContent.replace(/\s+/g, '')).toBe('ContextSchemeValue1');
    expect(el.querySelector('.rc-subtitle .rc-label').textContent).toBe('Meaning');
    expect(el.querySelector('.rc-subtitle').textContent.replace(/\s+/g, '')).toBe('Meaningone');
  });

  it('shows the title badges beside the title', () => {
    const el = render(host => host.record = {
      title: 'L', titleBadges: [{text: 'Default', kind: 'state', cssClass: 'default-indicator', background: 'rgb(253, 216, 53)', font: 'rgb(33, 33, 33)'}]
    });
    expect(el.querySelector('.rc-title .rc-title-badge span.default-indicator').textContent).toBe('Default');
    expect(el.querySelector('.rc-side')).toBeNull();
  });

  describe('the values that the width has room for', () => {
    let notify: () => void;
    let width: number;

    beforeEach(() => {
      width = 1000;
      vi.stubGlobal('ResizeObserver', class {
        constructor(callback: () => void) {
          notify = callback;
        }
        observe() {
        }
        disconnect() {
        }
      });
      vi.spyOn(HTMLElement.prototype, 'clientWidth', 'get').mockImplementation(() => width);
    });

    afterEach(() => {
      vi.unstubAllGlobals();
      vi.restoreAllMocks();
    });

    const shown = (el: HTMLElement) => Array.from(el.querySelectorAll('.rc-meta-item')).map(i => i.textContent.replace(/\s+/g, ''));

    it('leaves out a value as the block narrows, by its priority (4 below 700px, 3 below 600, 2 below 500, 1 below 400), and the first stays', () => {
      TestBed.configureTestingModule({providers: [provideRouter([]), {provide: WebPageInfoService, useValue: {}}]});
      const fixture = TestBed.createComponent(HostComponent);
      fixture.componentInstance.record = {
        title: 'T', subtitle: 's', updated: {when: new Date(2026, 0, 1)},
        meta: [{value: 'A', priority: 3}, {value: 'B'}, {value: 'C', priority: 1}, {value: 'D', priority: 2}, {value: 'E', priority: 3}, {value: 'F', priority: 4}]
      };
      fixture.detectChanges();
      const el: HTMLElement = fixture.nativeElement;
      const resizeTo = (px: number) => {
        width = px;
        notify();
        fixture.detectChanges();
      };
      resizeTo(800);
      expect(shown(el)).toEqual(['A', 'B', 'C', 'D', 'E', 'F']);
      resizeTo(650);
      expect(shown(el)).toEqual(['A', 'B', 'C', 'D', 'E']);
      resizeTo(550);
      expect(shown(el)).toEqual(['A', 'B', 'C', 'D']);
      resizeTo(450);
      expect(shown(el)).toEqual(['A', 'B', 'C']);
      resizeTo(300);
      // The first stays, though it is of a priority that the width does not allow.
      expect(shown(el)).toEqual(['A', 'B']);
      // The last of those shown is the one that is cut.
      expect(Array.from(el.querySelectorAll('.rc-meta-item.last')).map(i => i.textContent.trim())).toEqual(['B']);
    });
  });

  it('gives a badge its own colours when the screen says so', () => {
    const el = render(host => host.record = {title: 'L', badges: [{text: 'Default', kind: 'state', cssClass: 'default-indicator', background: 'rgb(253, 216, 53)', font: 'rgb(33, 33, 33)'}]});
    const badge = el.querySelector('.rc-side span.default-indicator') as HTMLElement;
    expect(badge.style.backgroundColor).toBe('rgb(253, 216, 53)');
    expect(badge.style.color).toBe('rgb(33, 33, 33)');
  });

  it('shows what it is given and no more: no badges, link, subtitle, values or update', () => {
    const el = render(host => {
      host.record = {title: 'Only a title'};
      host.external = false;
    });
    expect(el.querySelector('.rc-side')).toBeNull();
    expect(el.querySelector('.rc-title a')).toBeNull();
    expect(el.querySelector('.rc-title').textContent).toContain('Only a title');
    for (const cls of ['.rc-subtitle', '.rc-meta', '.rc-updated', '.rc-new', '.bie-display-name']) {
      expect(el.querySelector(cls)).toBeNull();
    }
  });
});

describe('memoizeRow', () => {
  it('makes the answer once for a row', () => {
    let calls = 0;
    const answer = memoizeRow((row: { n: number }) => {
      calls++;
      return {double: row.n * 2};
    });
    const row = {n: 2};
    const first = answer(row);
    expect(answer(row)).toBe(first);
    expect(calls).toBe(1);
    expect(answer({n: 2})).not.toBe(first);
    expect(calls).toBe(2);
  });
});
