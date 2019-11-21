import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AsccpCreateComponent} from './asccp-create.component';

describe('AsccpCreateComponent', () => {
  let component: AsccpCreateComponent;
  let fixture: ComponentFixture<AsccpCreateComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [AsccpCreateComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AsccpCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
