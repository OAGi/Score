import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {CodeListUpliftComponent} from './code-list-uplift.component';

describe('CodeListUpliftComponent', () => {
  let component: CodeListUpliftComponent;
  let fixture: ComponentFixture<CodeListUpliftComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [CodeListUpliftComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CodeListUpliftComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
