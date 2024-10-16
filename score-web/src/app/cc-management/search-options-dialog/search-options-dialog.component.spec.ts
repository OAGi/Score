import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {SearchOptionsDialogComponent} from './search-options-dialog.component';

describe('SearchOptionsDialogComponent', () => {
  let component: SearchOptionsDialogComponent;
  let fixture: ComponentFixture<SearchOptionsDialogComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ SearchOptionsDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchOptionsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
