import {ComponentFixture, TestBed} from '@angular/core/testing';

import {FindUsagesDialogComponent} from './find-usages-dialog.component';

describe('FindUsagesDialogComponent', () => {
  let component: FindUsagesDialogComponent;
  let fixture: ComponentFixture<FindUsagesDialogComponent>;

  beforeEach(fakeAsync () => {
    await TestBed.configureTestingModule({
      declarations: [ FindUsagesDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FindUsagesDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
