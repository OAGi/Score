import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {BieEditComponent} from './bie-edit.component';

describe('BieEditComponent', () => {
  let component: BieEditComponent;
  let fixture: ComponentFixture<BieEditComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [BieEditComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BieEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
