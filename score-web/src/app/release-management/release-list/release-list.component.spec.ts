import {fakeAsync, ComponentFixture, TestBed} from '@angular/core/testing';

import {ReleaseListComponent} from './release-list.component';

describe('ReleaseListComponent', () => {
  let component: ReleaseListComponent;
  let fixture: ComponentFixture<ReleaseListComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ReleaseListComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReleaseListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
