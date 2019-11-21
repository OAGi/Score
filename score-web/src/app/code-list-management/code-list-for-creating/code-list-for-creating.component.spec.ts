import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CodeListForCreatingComponent} from './code-list-for-creating.component';

describe('CodeListForCreatingComponent', () => {
  let component: CodeListForCreatingComponent;
  let fixture: ComponentFixture<CodeListForCreatingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [CodeListForCreatingComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CodeListForCreatingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
