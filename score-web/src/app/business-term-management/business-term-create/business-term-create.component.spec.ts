import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {BusinessTermCreateComponent} from './business-term-create.component';

describe('BiztermCreateComponent', () => {
  let component: BusinessTermCreateComponent;
  let fixture: ComponentFixture<BusinessTermCreateComponent>;

  beforeEach(fakeAsync(() => {
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
