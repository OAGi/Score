import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {BccpDetailComponent} from './bccp-detail.component';

describe('BccpDetailComponent', () => {
  let component: BccpDetailComponent;
  let fixture: ComponentFixture<BccpDetailComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [BccpDetailComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BccpDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
