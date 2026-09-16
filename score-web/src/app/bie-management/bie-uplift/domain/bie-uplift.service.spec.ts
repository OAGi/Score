import {of} from 'rxjs';
import {BieUpliftMap} from './bie-uplift';
import {BieUpliftService} from './bie-uplift.service';
import {vi} from 'vitest';

describe('BieUpliftService', () => {
  it('hydrates the analysis response into a BieUpliftMap instance', () => {
    const response = {
      asbiePathList: [{bieId: 11, source: {path: 'source', context: ''}}]
    };
    const http = {get: vi.fn().mockReturnValue(of(response))};
    const service = Object.create(BieUpliftService.prototype) as BieUpliftService;
    (service as any).http = http;

    service.getUpliftBieMap(220, 74).subscribe(result => {
      expect(result).toBeInstanceOf(BieUpliftMap);
      expect(result.asbiePathList[0].source.path).toBe('source');
    });
  });

  it('hydrates legacy ID-map responses without reintroducing duplicate runtime maps', () => {
    const response = {
      sourceAsbiePathMap: {
        '11': {path: 'legacy-source', context: 'legacy-context'}
      },
      targetAsbiePathMap: {
        '11': {path: 'legacy-target', context: 'legacy-context'}
      }
    };
    const http = {get: vi.fn().mockReturnValue(of(response))};
    const service = Object.create(BieUpliftService.prototype) as BieUpliftService;
    (service as any).http = http;

    service.getUpliftBieMap(220, 74).subscribe(result => {
      expect(result.asbiePathList).toEqual([{
        bieId: 11,
        source: {path: 'legacy-source', context: 'legacy-context'},
        target: {path: 'legacy-target', context: 'legacy-context'}
      }]);
      expect((result as any).sourceAsbiePathMap).toBeUndefined();
    });
  });

  it('hydrates legacy ID-map responses for every mapping kind', () => {
    const response = {
      sourceAsbiePathMap: {'11': {path: 'asbie-source', context: ''}},
      targetAsbiePathMap: {'11': {path: 'asbie-target', context: ''}},
      sourceBbiePathMap: {'22': {path: 'bbie-source', context: ''}},
      targetBbiePathMap: {'22': {path: 'bbie-target', context: ''}},
      sourceBbieScPathMap: {'33': {path: 'bbie-sc-source', context: ''}},
      targetBbieScPathMap: {'33': {path: 'bbie-sc-target', context: ''}}
    };
    const http = {get: vi.fn().mockReturnValue(of(response))};
    const service = Object.create(BieUpliftService.prototype) as BieUpliftService;
    (service as any).http = http;

    service.getUpliftBieMap(220, 74).subscribe(result => {
      expect(result.asbiePathList).toEqual([{
        bieId: 11,
        source: {path: 'asbie-source', context: ''},
        target: {path: 'asbie-target', context: ''}
      }]);
      expect(result.bbiePathList).toEqual([{
        bieId: 22,
        source: {path: 'bbie-source', context: ''},
        target: {path: 'bbie-target', context: ''}
      }]);
      expect(result.bbieScPathList).toEqual([{
        bieId: 33,
        source: {path: 'bbie-sc-source', context: ''},
        target: {path: 'bbie-sc-target', context: ''}
      }]);
      expect((result as any).sourceBbiePathMap).toBeUndefined();
      expect((result as any).sourceBbieScPathMap).toBeUndefined();
    });
  });

  it('prefers occurrence lists when a response contains both modern and legacy fields', () => {
    const response = {
      asbiePathList: [{bieId: 11, source: {path: 'modern-source', context: ''}}],
      sourceAsbiePathMap: {'11': {path: 'legacy-source', context: ''}}
    };
    const http = {get: vi.fn().mockReturnValue(of(response))};
    const service = Object.create(BieUpliftService.prototype) as BieUpliftService;
    (service as any).http = http;

    service.getUpliftBieMap(220, 74).subscribe(result => {
      expect(result.asbiePathList).toEqual(response.asbiePathList);
    });
  });
});
