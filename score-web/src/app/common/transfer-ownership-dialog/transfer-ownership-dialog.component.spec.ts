import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {TransferOwnershipDialogComponent} from './transfer-ownership-dialog.component';

describe('BusinessContextValueDialogComponent', () => {
  let component: TransferOwnershipDialogComponent;
  let fixture: ComponentFixture<TransferOwnershipDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [TransferOwnershipDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TransferOwnershipDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
