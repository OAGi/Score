import {Component} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {WebPageInfoService} from '../../basis/basis.service';
import {BieCompactRowComponent, BieCompactRowElement} from './bie-compact-row.component';

const ELEMENT: BieCompactRowElement = {
  guid: 'guid-1',
  den: 'Purchase Order. Detail',
  displayName: 'PO',
  state: 'WIP',
  deprecated: true,
  deprecatedReason: 'Replaced',
  deprecatedRemark: 'Use v2',
  release: {releaseNum: '10.13.2'},
  owner: {loginId: 'owner1'},
  version: '1.2',
  status: 'Working',
  bizTerm: 'Order',
  remark: 'A remark',
  businessContextList: [{businessContextId: 1, name: 'bc1'}, {businessContextId: 2, name: 'bc2'}],
  lastUpdated: {when: new Date(2026, 9, 26, 10, 56), who: {loginId: 'updater1'}}
};

@Component({
  imports: [BieCompactRowComponent],
  template: `<score-bie-compact-row [element]="element" [link]="link" [restriction]="restriction" [external]="external"/>`
})
class HostComponent {
  element = ELEMENT;
  link: string | undefined = '/profile_bie/5';
  restriction: string | undefined;
  external = true;
}

describe('BieCompactRowComponent', () => {
  const render = (change: (host: HostComponent) => void = () => undefined) => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {provide: WebPageInfoService, useValue: {getComponentStateColorSet: () => ({background: 'rgb(1, 2, 3)', font: 'rgb(4, 5, 6)'})}}
      ]
    });
    const fixture = TestBed.createComponent(HostComponent);
    change(fixture.componentInstance);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  };

  it('shows state (and deprecated) and branch on the left; name, display name, GUID, owner and the rest beside them', () => {
    const el = render();
    expect(Array.from(el.querySelectorAll('.bie-compact-side span.bie-state')).map(s => s.textContent.trim())).toEqual(['WIP', 'Deprecated']);
    expect(el.querySelector('.bie-compact-side mat-chip').textContent).toContain('10.13.2');
    const link = el.querySelector('.bie-compact-title a') as HTMLAnchorElement;
    expect(link.textContent).toBe('Purchase Order. Detail');
    expect(link.getAttribute('href')).toBe('/profile_bie/5');
    expect(link.getAttribute('target')).toBe('_blank');
    expect(el.querySelector('.bie-display-name').textContent).toBe('PO');
    expect(el.querySelector('.bie-compact-guid').textContent).toBe('guid-1');
    expect(el.querySelector('.bie-owner').textContent).toBe('Owner owner1');
    expect(el.querySelector('.bie-version').textContent).toBe('Version 1.2');
    expect(el.querySelector('.bie-status').textContent).toBe('Status Working');
    expect(Array.from(el.querySelectorAll('.bie-contexts a')).map(a => a.getAttribute('href'))).toEqual([
      '/context_management/business_context/1', '/context_management/business_context/2']);
    expect(el.querySelector('.bie-biz-term').textContent).toContain('Order');
    expect(el.querySelector('.bie-remark').textContent).toContain('A remark');
    expect(el.querySelector('.bie-compact-updated').textContent).toContain('Updated 2026-10-26 10:56');
    expect(el.querySelector('.bie-compact-updated').textContent).toContain('by updater1');
  });

  it('shows the name without a link for a BIE that cannot be opened', () => {
    const el = render(host => {
      host.link = undefined;
      host.restriction = 'Only the owner can access BIE in this state.';
    });
    expect(el.querySelector('.bie-compact-title a')).toBeNull();
    expect(el.querySelector('.bie-compact-title').textContent).toContain('Purchase Order. Detail');
  });

  it('leaves out what the BIE has no value for', () => {
    const el = render(host => host.element = {
      ...ELEMENT, deprecated: false, version: undefined, status: undefined, bizTerm: undefined, remark: undefined, businessContextList: []
    });
    expect(el.querySelectorAll('.bie-compact-side span.bie-state').length).toBe(1);
    for (const cls of ['.bie-version', '.bie-status', '.bie-biz-term', '.bie-remark', '.bie-contexts']) {
      expect(el.querySelector(cls)).toBeNull();
    }
  });
});
