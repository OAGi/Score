import {TestBed} from '@angular/core/testing';
import {HttpClient} from '@angular/common/http';
import {of} from 'rxjs';
import {WebPageInfo} from './about/domain/about';
import {WebPageInfoService} from './basis.service';

describe('WebPageInfoService', () => {
  function create(response: any = {}): WebPageInfoService {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({providers: [{provide: HttpClient, useValue: {get: () => of(response)}}]});
    const service = TestBed.runInInjectionContext(() => new WebPageInfoService());
    service.reset();
    return service;
  }

  it('stores and reads back a brand and a sign-in statement with non-Latin-1 characters', () => {
    const service = create();
    const info = new WebPageInfo();
    info.brand = '<b>환영합니다 😀</b>';
    info.signInStatement = 'ありがとう';
    service.set(info);
    expect(service.webPageInfo.brand).toBe('<b>환영합니다 😀</b>');
    expect(service.webPageInfo.signInStatement).toBe('ありがとう');
    service.reset();
  });

  it('reads a value that an earlier version stored with the plain btoa', () => {
    const service = create();
    localStorage.setItem(service.WEB_PAGE_INFO_KEY, btoa(JSON.stringify({brand: 'caf\u00e9'})));
    expect(service.webPageInfo.brand).toBe('caf\u00e9');
    service.reset();
  });

  it('loads the page info with non-Latin-1 text without failing', () => {
    const service = create({brand: '환영합니다'});
    let loaded: WebPageInfo;
    service.load().subscribe(info => loaded = info);
    expect(loaded.brand).toBe('환영합니다');
    service.reset();
  });
});
