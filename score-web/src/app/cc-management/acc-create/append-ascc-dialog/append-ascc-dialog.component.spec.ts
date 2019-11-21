import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AppendAsccDialogComponent} from './append-ascc-dialog.component';

describe('AppendAsccDialogComponent', () => {
  let component: AppendAsccDialogComponent;
  let fixture: ComponentFixture<AppendAsccDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AppendAsccDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppendAsccDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
