import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {AgencyIdListDetailComponent} from './agency-id-list-detail.component';

describe('CodeListCreateComponent', () => {
  let component: AgencyIdListDetailComponent;
  let fixture: ComponentFixture<AgencyIdListDetailComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [AgencyIdListDetailComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AgencyIdListDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
