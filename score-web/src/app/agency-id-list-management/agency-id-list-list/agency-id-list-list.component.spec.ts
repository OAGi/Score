import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {AgencyIdListListComponent} from './agency-id-list-list.component';

describe('CodeListForCreatingComponent', () => {
  let component: AgencyIdListListComponent;
  let fixture: ComponentFixture<AgencyIdListListComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [AgencyIdListListComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AgencyIdListListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
