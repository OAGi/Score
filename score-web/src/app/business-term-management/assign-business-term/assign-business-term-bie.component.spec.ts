import {fakeAsync, ComponentFixture, TestBed} from '@angular/core/testing';

import {AssignBusinessTermBieComponent} from './assign-business-term-bie.component';

describe('BieCopyProfileBieComponent', () => {
  let component: AssignBusinessTermBieComponent;
  let fixture: ComponentFixture<AssignBusinessTermBieComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [AssignBusinessTermBieComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AssignBusinessTermBieComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
