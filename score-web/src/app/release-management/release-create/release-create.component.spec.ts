import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {ReleaseCreateComponent} from './release-create.component';

describe('ReleaseCreateComponent', () => {
  let component: ReleaseCreateComponent;
  let fixture: ComponentFixture<ReleaseCreateComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ReleaseCreateComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReleaseCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
