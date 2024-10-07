import {fakeAsync, ComponentFixture, TestBed} from '@angular/core/testing';

import {BasedAccDialogComponent} from './based-acc-dialog.component';

describe('BasedAccDialogComponent', () => {
  let component: BasedAccDialogComponent;
  let fixture: ComponentFixture<BasedAccDialogComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [BasedAccDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BasedAccDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
