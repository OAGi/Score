import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ModuleCreateComponent} from './module-create.component';

describe('ModuleCreateComponent', () => {
  let component: ModuleCreateComponent;
  let fixture: ComponentFixture<ModuleCreateComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ModuleCreateComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModuleCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
