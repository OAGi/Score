import {fakeAsync, ComponentFixture, TestBed} from '@angular/core/testing';

import {CodeListForDerivingComponent} from './code-list-for-deriving.component';

describe('CodeListForCreatingComponent', () => {
  let component: CodeListForDerivingComponent;
  let fixture: ComponentFixture<CodeListForDerivingComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [CodeListForDerivingComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CodeListForDerivingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
