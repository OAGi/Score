import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CodeListCreateComponent} from './code-list-create.component';

describe('CodeListCreateComponent', () => {
  let component: CodeListCreateComponent;
  let fixture: ComponentFixture<CodeListCreateComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [CodeListCreateComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CodeListCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
