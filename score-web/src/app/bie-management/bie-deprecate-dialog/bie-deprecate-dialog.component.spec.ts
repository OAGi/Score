import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {BieDeprecateDialogComponent} from './bie-deprecate-dialog.component';

describe('BusinessContextValueDialogComponent', () => {
  let component: BieDeprecateDialogComponent;
  let fixture: ComponentFixture<BieDeprecateDialogComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [BieDeprecateDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BieDeprecateDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
