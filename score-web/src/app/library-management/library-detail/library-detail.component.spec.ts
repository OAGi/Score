import {TestBed} from '@angular/core/testing';
import {ActivatedRoute, Router} from '@angular/router';
import {Location} from '@angular/common';
import {MatSnackBar} from '@angular/material/snack-bar';
import {LibraryDetailComponent} from './library-detail.component';
import {LibraryService} from '../domain/library.service';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {AuthService} from '../../authentication/auth.service';
import {Title} from '@angular/platform-browser';

describe('LibraryDetailComponent', () => {
  it('should be defined', () => {
    expect(LibraryDetailComponent).toBeTruthy();
  });

  it('should match release dependencies when separators differ', () => {
    TestBed.configureTestingModule({
      providers: [
        {provide: LibraryService, useValue: {}},
        {provide: MatSnackBar, useValue: {}},
        {provide: ConfirmDialogService, useValue: {}},
        {provide: Location, useValue: {}},
        {provide: ActivatedRoute, useValue: {}},
        {provide: Router, useValue: {}},
        {provide: AuthService, useValue: {isAdmin: () => true}},
        {provide: Title, useValue: {}}
      ]
    });

    const component = TestBed.runInInjectionContext(() => new LibraryDetailComponent());
    component.availableReleaseDependencies = [{
      releaseId: 1,
      libraryId: 100,
      libraryName: 'connectSpec',
      releaseNum: '10.12.7',
      state: 'Published',
      workingRelease: false
    }];

    const matches = (component as any).filterDependencyOptions('connectSpec 10.12.7');

    expect(matches).toHaveLength(1);
    expect(matches[0].releaseId).toBe(1);
  });

  it('should match release dependencies by partial library name and version tokens', () => {
    TestBed.configureTestingModule({
      providers: [
        {provide: LibraryService, useValue: {}},
        {provide: MatSnackBar, useValue: {}},
        {provide: ConfirmDialogService, useValue: {}},
        {provide: Location, useValue: {}},
        {provide: ActivatedRoute, useValue: {}},
        {provide: Router, useValue: {}},
        {provide: AuthService, useValue: {isAdmin: () => true}},
        {provide: Title, useValue: {}}
      ]
    });

    const component = TestBed.runInInjectionContext(() => new LibraryDetailComponent());
    component.availableReleaseDependencies = [{
      releaseId: 1,
      libraryId: 100,
      libraryName: 'connectSpec',
      releaseNum: '10.12.7',
      state: 'Published',
      workingRelease: false
    }];

    const matches = (component as any).filterDependencyOptions('conn 10.12.7');

    expect(matches).toHaveLength(1);
    expect(matches[0].releaseId).toBe(1);
  });
});
