import {readFileSync} from 'node:fs';
import {join} from 'node:path';
import {of} from 'rxjs';
import {CodeListForDerivingComponent} from './code-list-for-deriving.component';

describe('CodeListForDerivingComponent', () => {
  it('should be defined', () => {
    expect(CodeListForDerivingComponent).toBeTruthy();
  });

  describe('next', () => {
    const run = (selected: number[]) => {
      const self = {
        selection: {selected},
        releaseChoice: {release: {releaseId: 7}},
        service: {create: vi.fn(() => of({codeListManifestId: 99}))},
        router: {navigate: vi.fn()},
      };
      CodeListForDerivingComponent.prototype.next.call(self as never);
      return self;
    };

    it('derives a code list from the selected one and opens it', () => {
      const self = run([4089]);
      expect(self.service.create).toHaveBeenCalledWith(7, 4089);
      expect(self.router.navigate).toHaveBeenCalledWith(['/code_list/99']);
    });

    it('does nothing without a selection', () => {
      const self = run([]);
      expect(self.service.create).not.toHaveBeenCalled();
      expect(self.router.navigate).not.toHaveBeenCalled();
    });
  });

  describe('next without a branch', () => {
    it('does nothing when no release is chosen', () => {
      const self = {
        selection: {selected: [4089]},
        releaseChoice: {release: undefined},
        service: {create: vi.fn()},
        router: {navigate: vi.fn()},
      };
      CodeListForDerivingComponent.prototype.next.call(self as never);
      expect(self.service.create).not.toHaveBeenCalled();
    });
  });

  describe('selection', () => {
    it('is cleared when the branch changes', () => {
      const self = {
        releaseChoice: {choose: vi.fn()},
        selection: {clear: vi.fn()},
        applyChoices: vi.fn(),
      };
      CodeListForDerivingComponent.prototype.onBranchChange.call(self as never, {releaseId: 3} as never);
      expect(self.selection.clear).toHaveBeenCalled();
    });

    it('is cleared when the library changes', () => {
      const self = {
        libraryChoice: {choose: vi.fn()},
        releaseChoice: {init: vi.fn()},
        selection: {clear: vi.fn()},
        releaseService: {getReleaseSummaryList: vi.fn(() => of([]))},
        applyChoices: vi.fn(),
      };
      CodeListForDerivingComponent.prototype.onLibraryChange.call(self as never, {libraryId: 2} as never);
      expect(self.selection.clear).toHaveBeenCalled();
    });
  });

  describe('template', () => {
    const html = readFileSync(
      join(process.cwd(), 'src/app/code-list-management/code-list-for-deriving/code-list-for-deriving.component.html'), 'utf8');

    it.each(['codeListName', 'basedCodeListName', 'basedCodeListId', 'agencyIdName', 'modulePath',
      'lastUpdateTimestamp', 'lastUpdateUser', 'element.revision', 'element.owner"'])(
      'does not read the field %s that the list endpoint no longer returns', field => {
        const bound = html.split('\n').filter(line => !line.includes('scoreColumnDef'));
        expect(bound.filter(line => line.includes(field))).toEqual([]);
      });
  });
});
