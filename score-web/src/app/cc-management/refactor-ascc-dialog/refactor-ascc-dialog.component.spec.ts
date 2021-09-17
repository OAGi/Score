import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RefactorAsccDialogComponent} from './refactor-ascc-dialog.component';

describe('BasedAccDialogComponent', () => {
  let component: RefactorAsccDialogComponent;
  let fixture: ComponentFixture<RefactorAsccDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [RefactorAsccDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RefactorAsccDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
