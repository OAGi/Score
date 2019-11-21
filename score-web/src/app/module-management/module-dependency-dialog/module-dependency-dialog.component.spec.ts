import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ModuleDependencyDialogComponent} from './module-dependency-dialog.component';

describe('BusinessContextValueDialogComponent', () => {
  let component: ModuleDependencyDialogComponent;
  let fixture: ComponentFixture<ModuleDependencyDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ModuleDependencyDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModuleDependencyDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
