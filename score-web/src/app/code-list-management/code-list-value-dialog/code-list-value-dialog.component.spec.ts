import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {CodeListValueDialogComponent} from './code-list-value-dialog.component';

describe('BusinessContextValueDialogComponent', () => {
  let component: CodeListValueDialogComponent;
  let fixture: ComponentFixture<CodeListValueDialogComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [CodeListValueDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CodeListValueDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
