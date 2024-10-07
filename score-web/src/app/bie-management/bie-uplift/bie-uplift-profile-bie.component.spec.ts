import {fakeAsync, ComponentFixture, TestBed} from '@angular/core/testing';

import {BieUpliftProfileBieComponent} from './bie-uplift-profile-bie.component';

describe('BieCopyProfileBieComponent', () => {
  let component: BieUpliftProfileBieComponent;
  let fixture: ComponentFixture<BieUpliftProfileBieComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [BieUpliftProfileBieComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BieUpliftProfileBieComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
