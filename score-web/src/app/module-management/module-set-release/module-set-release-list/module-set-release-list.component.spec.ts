import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ModuleSetReleaseListComponent} from './module-set-release-list.component';

describe('ModuleSetReleaseListComponent', () => {
  let component: ModuleSetReleaseListComponent;
  let fixture: ComponentFixture<ModuleSetReleaseListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ModuleSetReleaseListComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModuleSetReleaseListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
