import {fakeAsync, ComponentFixture, TestBed} from '@angular/core/testing';

import {EditTagsDialogComponent} from './edit-tags-dialog.component';

describe('EditTagsDialogComponent', () => {
  let component: EditTagsDialogComponent;
  let fixture: ComponentFixture<EditTagsDialogComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ EditTagsDialogComponent ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditTagsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
