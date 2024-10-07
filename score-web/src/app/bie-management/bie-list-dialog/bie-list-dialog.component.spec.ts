import {fakeAsync, ComponentFixture, TestBed} from '@angular/core/testing';

import {BieListDialogComponent} from './bie-list-dialog.component';

describe('BusinessContextValueDialogComponent', () => {
  let component: BieListDialogComponent;
  let fixture: ComponentFixture<BieListDialogComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [BieListDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BieListDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
