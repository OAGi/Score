import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {BieExpressComponent} from './bie-express.component';

describe('BieExpressComponent', () => {
  let component: BieExpressComponent;
  let fixture: ComponentFixture<BieExpressComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [BieExpressComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BieExpressComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
