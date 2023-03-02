import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AssignBusinessTermBtComponent} from './assign-business-term-bt.component';

describe('BieCopyBizCtxComponent', () => {
  let component: AssignBusinessTermBtComponent;
  let fixture: ComponentFixture<AssignBusinessTermBtComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [AssignBusinessTermBtComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AssignBusinessTermBtComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
