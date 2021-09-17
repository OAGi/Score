import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AgencyIdListValueDialogComponent} from './agency-id-list-value-dialog.component';

describe('BusinessContextValueDialogComponent', () => {
  let component: AgencyIdListValueDialogComponent;
  let fixture: ComponentFixture<AgencyIdListValueDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [AgencyIdListValueDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AgencyIdListValueDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
