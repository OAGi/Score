import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {BccpCreateComponent} from './bccp-create.component';

describe('BccpCreateComponent', () => {
  let component: BccpCreateComponent;
  let fixture: ComponentFixture<BccpCreateComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [BccpCreateComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BccpCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
