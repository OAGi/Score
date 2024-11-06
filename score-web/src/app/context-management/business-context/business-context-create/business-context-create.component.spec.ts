import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {BusinessContextCreateComponent} from './business-context-create.component';

describe('BusinessContextCreateComponent', () => {
  let component: BusinessContextCreateComponent;
  let fixture: ComponentFixture<BusinessContextCreateComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [BusinessContextCreateComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BusinessContextCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
