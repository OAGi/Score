import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AssignedBusinessTermDetailComponent} from './assigned-business-term-detail.component';

describe('AssignedBusinessTermDetailComponent', () => {
  let component: AssignedBusinessTermDetailComponent;
  let fixture: ComponentFixture<AssignedBusinessTermDetailComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [AssignedBusinessTermDetailComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AssignedBusinessTermDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
