import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {OasDocAssignDialogComponent} from './oas-doc-assign-dialog.component';

describe('OasDocAssignDialogComponent', () => {
  let component: OasDocAssignDialogComponent;
  let fixture: ComponentFixture<OasDocAssignDialogComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [OasDocAssignDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OasDocAssignDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
