import {fakeAsync, ComponentFixture, TestBed} from '@angular/core/testing';

import {ContextCategoryDetailComponent} from './context-category-detail.component';

describe('ContextCategoryDetailComponent', () => {
  let component: ContextCategoryDetailComponent;
  let fixture: ComponentFixture<ContextCategoryDetailComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ContextCategoryDetailComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ContextCategoryDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
