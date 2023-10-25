import {ComponentFixture, TestBed} from '@angular/core/testing';

import {BieExpressOpenapi30Component} from './bie-express.openapi30.component';

describe('BieExpressOpenapi30Component', () => {
  let component: BieExpressOpenapi30Component;
  let fixture: ComponentFixture<BieExpressOpenapi30Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BieExpressOpenapi30Component ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BieExpressOpenapi30Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
