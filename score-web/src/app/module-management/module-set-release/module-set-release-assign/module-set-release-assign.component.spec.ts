import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ModuleSetReleaseAssignComponent} from './module-set-release-assign.component';

describe('ModuleSetReleaseCreateComponent', () => {
  let component: ModuleSetReleaseAssignComponent;
  let fixture: ComponentFixture<ModuleSetReleaseAssignComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ModuleSetReleaseAssignComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModuleSetReleaseAssignComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
