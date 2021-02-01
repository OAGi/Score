import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CreateBccpDialogComponent} from './create-bccp-dialog.component';

describe('AppendBccpDialogComponent', () => {
  let component: CreateBccpDialogComponent;
  let fixture: ComponentFixture<CreateBccpDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [CreateBccpDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateBccpDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
