import {ComponentFixture, TestBed} from '@angular/core/testing';

import {BieUpliftComponent} from './bie-uplift.component';

describe('BieUpliftComponent', () => {
  let component: BieUpliftComponent;
  let fixture: ComponentFixture<BieUpliftComponent>;

  beforeEach(fakeAsync () => {
    await TestBed.configureTestingModule({
      declarations: [ BieUpliftComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BieUpliftComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
