import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {ContextCategoryCreateComponent} from './context-category-create.component';

describe('ContextCategoryCreateComponent', () => {
  let component: ContextCategoryCreateComponent;
  let fixture: ComponentFixture<ContextCategoryCreateComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ContextCategoryCreateComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ContextCategoryCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
