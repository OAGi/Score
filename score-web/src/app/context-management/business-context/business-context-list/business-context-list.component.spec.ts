import {fakeAsync, ComponentFixture, TestBed} from '@angular/core/testing';

import {BusinessContextListComponent} from './business-context-list.component';

describe('BusinessContextListComponent', () => {
  let component: BusinessContextListComponent;
  let fixture: ComponentFixture<BusinessContextListComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [BusinessContextListComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BusinessContextListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
