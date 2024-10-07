import {fakeAsync, ComponentFixture, TestBed} from '@angular/core/testing';

import {BusinessContextDetailComponent} from './business-context-detail.component';

describe('BusinessContextDetailComponent', () => {
  let component: BusinessContextDetailComponent;
  let fixture: ComponentFixture<BusinessContextDetailComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [BusinessContextDetailComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BusinessContextDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
