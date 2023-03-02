import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {BusinessTermCreateComponent} from './business-term-create.component';

describe('BiztermCreateComponent', () => {
  let component: BusinessTermCreateComponent;
  let fixture: ComponentFixture<BusinessTermCreateComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [BusinessTermCreateComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BusinessTermCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
