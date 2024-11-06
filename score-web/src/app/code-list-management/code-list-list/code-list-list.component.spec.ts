import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {CodeListListComponent} from './code-list-list.component';

describe('CodeListForCreatingComponent', () => {
  let component: CodeListListComponent;
  let fixture: ComponentFixture<CodeListListComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [CodeListListComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CodeListListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
