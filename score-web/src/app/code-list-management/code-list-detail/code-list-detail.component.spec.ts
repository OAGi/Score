import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {CodeListDetailComponent} from './code-list-detail.component';

describe('CodeListCreateComponent', () => {
  let component: CodeListDetailComponent;
  let fixture: ComponentFixture<CodeListDetailComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [CodeListDetailComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CodeListDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
