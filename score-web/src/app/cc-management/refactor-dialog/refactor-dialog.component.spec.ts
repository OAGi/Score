import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {RefactorDialogComponent} from './refactor-dialog.component';

describe('BasedAccDialogComponent', () => {
  let component: RefactorDialogComponent;
  let fixture: ComponentFixture<RefactorDialogComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [RefactorDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RefactorDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
