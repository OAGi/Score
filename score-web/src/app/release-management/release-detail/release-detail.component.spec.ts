import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ReleaseDetailComponent} from './release-detail.component';

describe('ReleaseDetailComponent', () => {
  let component: ReleaseDetailComponent;
  let fixture: ComponentFixture<ReleaseDetailComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ReleaseDetailComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReleaseDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
