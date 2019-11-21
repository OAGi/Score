import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {BusinessContextValueDialogComponent} from './business-context-value-dialog.component';

describe('BusinessContextValueDialogComponent', () => {
  let component: BusinessContextValueDialogComponent;
  let fixture: ComponentFixture<BusinessContextValueDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [BusinessContextValueDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BusinessContextValueDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
