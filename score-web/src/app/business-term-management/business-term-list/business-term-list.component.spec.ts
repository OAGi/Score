import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {BusinessTermListComponent} from './business-term-list.component';

describe('ContextSchemeListComponent', () => {
  let component: BusinessTermListComponent;
  let fixture: ComponentFixture<BusinessTermListComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [BusinessTermListComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BusinessTermListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
