import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {ReuseBieDialogComponent} from './reuse-bie-dialog.component';

describe('MetaHeaderDialogComponent', () => {
  let component: ReuseBieDialogComponent;
  let fixture: ComponentFixture<ReuseBieDialogComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ReuseBieDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReuseBieDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
