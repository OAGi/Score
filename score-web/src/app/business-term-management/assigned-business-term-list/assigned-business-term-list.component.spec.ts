import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AssignedBusinessTermListComponent} from './assigned-business-term-list.component';

describe('ContextSchemeListComponent', () => {
  let component: AssignedBusinessTermListComponent;
  let fixture: ComponentFixture<AssignedBusinessTermListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [AssignedBusinessTermListComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AssignedBusinessTermListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
