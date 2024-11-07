import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {ModuleSetEditComponent} from './module-set-edit.component';

describe('ModuleSetModuleComponent', () => {
  let component: ModuleSetEditComponent;
  let fixture: ComponentFixture<ModuleSetEditComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ModuleSetEditComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModuleSetEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
