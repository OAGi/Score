import {fakeAsync, ComponentFixture, TestBed} from '@angular/core/testing';

import {ContextSchemeValueDialogComponent} from './context-scheme-value-dialog.component';

describe('BusinessContextValueDialogComponent', () => {
  let component: ContextSchemeValueDialogComponent;
  let fixture: ComponentFixture<ContextSchemeValueDialogComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ContextSchemeValueDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ContextSchemeValueDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
