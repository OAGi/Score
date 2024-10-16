import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {CreateAsccpDialogComponent} from './create-asccp-dialog.component';

describe('AppendAsccDialogComponent', () => {
  let component: CreateAsccpDialogComponent;
  let fixture: ComponentFixture<CreateAsccpDialogComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [CreateAsccpDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateAsccpDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
