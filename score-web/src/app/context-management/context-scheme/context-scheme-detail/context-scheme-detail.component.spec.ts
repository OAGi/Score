import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {ContextSchemeDetailComponent} from './context-scheme-detail.component';

describe('CodeListCreateComponent', () => {
  let component: ContextSchemeDetailComponent;
  let fixture: ComponentFixture<ContextSchemeDetailComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ContextSchemeDetailComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ContextSchemeDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
