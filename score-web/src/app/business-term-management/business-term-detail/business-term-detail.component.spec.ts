import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {BusinessTermDetailComponent} from './business-term-detail.component';

describe('CodeListCreateComponent', () => {
  let component: BusinessTermDetailComponent;
  let fixture: ComponentFixture<BusinessTermDetailComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [BusinessTermDetailComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BusinessTermDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
