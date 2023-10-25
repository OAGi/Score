import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CreateVerbDialogComponent} from './create-verb-dialog.component';

describe('CreateVerbDialogComponent', () => {
  let component: CreateVerbDialogComponent;
  let fixture: ComponentFixture<CreateVerbDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CreateVerbDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateVerbDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
