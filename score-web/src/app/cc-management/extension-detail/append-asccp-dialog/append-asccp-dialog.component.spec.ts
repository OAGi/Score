import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AppendAsccpDialogComponent} from './append-asccp-dialog.component';

describe('AppendAsccDialogComponent', () => {
  let component: AppendAsccpDialogComponent;
  let fixture: ComponentFixture<AppendAsccpDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [AppendAsccpDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppendAsccpDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
