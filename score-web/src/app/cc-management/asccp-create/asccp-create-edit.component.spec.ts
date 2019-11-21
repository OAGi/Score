import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AsccpCreateEditComponent} from './asccp-create-edit.component';

describe('AsccpCreateEditComponent', () => {
  let component: AsccpCreateEditComponent;
  let fixture: ComponentFixture<AsccpCreateEditComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AsccpCreateEditComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AsccpCreateEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
