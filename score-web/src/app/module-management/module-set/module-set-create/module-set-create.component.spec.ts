import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {ModuleSetCreateComponent} from './module-set-create.component';

describe('ModuleSetCreateComponent', () => {
  let component: ModuleSetCreateComponent;
  let fixture: ComponentFixture<ModuleSetCreateComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ModuleSetCreateComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModuleSetCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
