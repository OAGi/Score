import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AppendBccpDialogComponent} from './append-bccp-dialog.component';

describe('AppendBccpDialogComponent', () => {
  let component: AppendBccpDialogComponent;
  let fixture: ComponentFixture<AppendBccpDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [AppendBccpDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppendBccpDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
