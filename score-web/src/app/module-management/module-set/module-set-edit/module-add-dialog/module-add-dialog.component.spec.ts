import {TestBed} from '@angular/core/testing';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {NamespaceService} from '../../../../namespace-management/domain/namespace.service';
import {ModuleElement} from '../../../domain/module';
import {ModuleService} from '../../../domain/module.service';
import {ModuleAddDialogComponent} from './module-add-dialog.component';

describe('ModuleAddDialogComponent copy into itself (#126)', () => {
  const element = (moduleId: number, name: string, children?: ModuleElement[]): ModuleElement =>
    Object.assign(new ModuleElement(), {moduleId, name, directory: !!children, children});

  // root(1) -> D(2) -> Sub(3), root(1) -> Other(4)
  const sub = element(3, 'Sub', []);
  const d = element(2, 'D', [sub]);
  const other = element(4, 'Other', []);

  function create(parentModuleId: number): ModuleAddDialogComponent {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        {provide: ModuleService, useValue: {copyModule: vi.fn()}},
        {provide: NamespaceService, useValue: {}},
        {provide: MatSnackBar, useValue: {open: () => {}}},
        {provide: MatDialogRef, useValue: {close: () => {}}},
        {provide: MAT_DIALOG_DATA, useValue: {moduleSetId: 10, libraryId: 1, parentModuleId, parentDirName: 'x'}}
      ]
    });
    const component = TestBed.runInInjectionContext(() => new ModuleAddDialogComponent());
    component.selectedModuleSetId = 10;
    return component;
  }

  it('blocks the destination directory itself', () => {
    expect(create(2).isCopyBlocked(d)).toBe(true);
  });

  it('blocks an ancestor of the destination directory', () => {
    expect(create(3).isCopyBlocked(d)).toBe(true);
  });

  it('allows a module outside of the destination or a module inside it', () => {
    expect(create(2).isCopyBlocked(other)).toBe(false);
    expect(create(2).isCopyBlocked(sub)).toBe(false);
  });

  it('allows anything when copying from another module set', () => {
    const component = create(2);
    component.selectedModuleSetId = 11;
    expect(component.isCopyBlocked(d)).toBe(false);
  });

  it('does not send a blocked copy to the server', () => {
    const component = create(2);
    component.selected = d;
    component.onCopy();
    expect(TestBed.inject(ModuleService).copyModule).not.toHaveBeenCalled();
  });
});
