import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {BdtDetailComponent} from './bdt-detail.component';

describe('BccpDetailComponent', () => {
  let component: BdtDetailComponent;
  let fixture: ComponentFixture<BdtDetailComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [BdtDetailComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BdtDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
