import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {MetaHeaderDialogComponent} from './meta-header-dialog.component';

describe('MetaHeaderDialogComponent', () => {
  let component: MetaHeaderDialogComponent;
  let fixture: ComponentFixture<MetaHeaderDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [MetaHeaderDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MetaHeaderDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
