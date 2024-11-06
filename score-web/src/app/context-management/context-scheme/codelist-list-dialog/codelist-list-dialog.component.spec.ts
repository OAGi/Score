import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {CodelistListDialogComponent} from './codelist-list-dialog.component';

describe('CodelistListDialogComponent', () => {
  let component: CodelistListDialogComponent;
  let fixture: ComponentFixture<CodelistListDialogComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [CodelistListDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CodelistListDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
