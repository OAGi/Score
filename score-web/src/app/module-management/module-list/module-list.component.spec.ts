import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ModuleListComponent} from './module-list.component';

describe('ModuleListComponent', () => {
  let component: ModuleListComponent;
  let fixture: ComponentFixture<ModuleListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ModuleListComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModuleListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
