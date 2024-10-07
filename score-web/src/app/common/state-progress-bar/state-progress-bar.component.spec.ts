import {fakeAsync, ComponentFixture, TestBed} from '@angular/core/testing';

import {StateProgressBarComponent} from './state-progress-bar.component';

describe('StateProgressBarComponent', () => {
  let component: StateProgressBarComponent;
  let fixture: ComponentFixture<StateProgressBarComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ StateProgressBarComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StateProgressBarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
