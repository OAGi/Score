import {fakeAsync, ComponentFixture, TestBed} from '@angular/core/testing';

import {TransferOwnershipListComponent} from './transfer-ownership-list.component';

describe('AccountListComponent', () => {
  let component: TransferOwnershipListComponent;
  let fixture: ComponentFixture<TransferOwnershipListComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [TransferOwnershipListComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TransferOwnershipListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
