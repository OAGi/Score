import {fakeAsync, ComponentFixture, TestBed} from '@angular/core/testing';

import {ContextCategoryListComponent} from './context-category-list.component';

describe('ContextCategoryListComponent', () => {
  let component: ContextCategoryListComponent;
  let fixture: ComponentFixture<ContextCategoryListComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ContextCategoryListComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ContextCategoryListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
