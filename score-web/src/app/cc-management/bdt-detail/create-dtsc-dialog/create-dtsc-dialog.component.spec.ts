import {fakeAsync, ComponentFixture, TestBed} from '@angular/core/testing';

import {CreateDtscDialogComponent} from './create-dtsc-dialog.component';

describe('AppendBccpDialogComponent', () => {
  let component: CreateDtscDialogComponent;
  let fixture: ComponentFixture<CreateDtscDialogComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [CreateDtscDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateDtscDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
