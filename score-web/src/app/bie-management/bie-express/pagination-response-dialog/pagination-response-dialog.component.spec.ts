import {fakeAsync, ComponentFixture, TestBed} from '@angular/core/testing';

import {PaginationResponseDialogComponent} from './pagination-response-dialog.component';

describe('PaginationResponseDialogComponent', () => {
  let component: PaginationResponseDialogComponent;
  let fixture: ComponentFixture<PaginationResponseDialogComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [PaginationResponseDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PaginationResponseDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
