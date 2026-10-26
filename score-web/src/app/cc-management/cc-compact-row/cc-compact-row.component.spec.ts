import {Component} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {WebPageInfoService} from '../../basis/basis.service';
import {CcCompactRowComponent, CcCompactRowElement} from './cc-compact-row.component';

const ELEMENT: CcCompactRowElement = {
  guid: 'abc123',
  state: 'ReleaseDraft',
  deprecated: true,
  tagList: [{name: 'T1', backgroundColor: '#eee', textColor: '#111'}],
  log: {revisionNum: 7},
  owner: {loginId: 'owner1'},
  module: 'Model\\Nouns\\BOM',
  definition: {content: 'What it is.'},
  lastUpdated: {when: new Date(2026, 9, 26, 10, 56), who: {loginId: 'updater1'}}
};

@Component({
  imports: [CcCompactRowComponent],
  template: `
    <score-cc-compact-row [element]="element" label="Some. Thing" link="core_component/acc/1" [external]="external" [type]="type"
      [isNew]="isNew" [showOwner]="showOwner">
      <span class="extra">Value Domain: String</span>
    </score-cc-compact-row>`
})
class HostComponent {
  element = ELEMENT;
  external = true;
  type: string | undefined = 'ACC';
  isNew = false;
  showOwner = true;
}

describe('CcCompactRowComponent', () => {
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

  it('shows type, state (and deprecated), name with its link, tags, GUID, revision, owner and the update', () => {
    const el = render();
    expect(el.querySelector('mat-chip').textContent).toContain('ACC');
    expect(Array.from(el.querySelectorAll('.cc-compact-side span.cc-state')).map(s => s.textContent.trim())).toEqual(['Release Draft', 'Deprecated']);
    const link = el.querySelector('.cc-compact-title a') as HTMLAnchorElement;
    expect(link.textContent).toBe('Some. Thing');
    expect(link.getAttribute('href')).toBe('/core_component/acc/1');
    expect(link.getAttribute('target')).toBe('_blank');
    expect(el.querySelector('.cc-compact-title .tag').textContent).toBe('T1');
    expect(el.querySelector('.description').textContent).toBe('abc123');
    expect(el.querySelector('.cc-rev').textContent).toBe('Rev. 7');
    expect(el.querySelector('.cc-owner').textContent).toBe('Owner owner1');
    expect(el.querySelector('.cc-updated-when').textContent).toBe('Updated 2026-10-26 10:56');
    expect(el.querySelector('.cc-updated-by').textContent).toBe('by updater1');
    expect(el.querySelector('.cc-compact-extra .extra').textContent).toBe('Value Domain: String');
    // The module and the definition follow the owner, on the same line.
    expect(el.querySelector('.cc-compact-meta > .cc-module').textContent).toBe('ModuleModel\\Nouns\\BOM');
    expect(el.querySelector('.cc-compact-meta > .cc-definition').textContent).toBe('DefinitionWhat it is.');
  });

  it('leaves out the module and the definition of a component that has none', () => {
    const el = render(host => host.element = {...ELEMENT, module: undefined, definition: undefined});
    expect(el.querySelector('.cc-module')).toBeNull();
    expect(el.querySelector('.cc-definition')).toBeNull();
  });

  it('leaves out the type chip, the "New" mark, revision and owner when told so, and does not open the link in a new tab', () => {
    const el = render(host => {
      host.type = undefined;
      host.showOwner = false;
      host.external = false;
    });
    expect(el.querySelector('mat-chip')).toBeNull();
    expect(el.querySelector('.new-component')).toBeNull();
    expect(el.querySelector('.cc-rev')).toBeNull();
    expect(el.querySelector('.cc-owner')).toBeNull();
    expect(el.querySelector('.cc-compact-title a').getAttribute('target')).toBeNull();
  });

  it('marks a new component', () => {
    expect(render(host => host.isNew = true).querySelector('.new-component').textContent).toBe('New');
  });
});
