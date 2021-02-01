import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ReleaseAssignComponent} from './release-assign.component';

describe('ReleaseAssignComponent', () => {
  let component: ReleaseAssignComponent;
  let fixture: ComponentFixture<ReleaseAssignComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ReleaseAssignComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReleaseAssignComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
