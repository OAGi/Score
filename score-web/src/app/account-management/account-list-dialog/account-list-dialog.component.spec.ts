import {fakeAsync, ComponentFixture, TestBed} from '@angular/core/testing';

import {AccountListDialogComponent} from './account-list-dialog.component';

describe('AccountListDialogComponent', () => {
  let component: AccountListDialogComponent;
  let fixture: ComponentFixture<AccountListDialogComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [AccountListDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccountListDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
