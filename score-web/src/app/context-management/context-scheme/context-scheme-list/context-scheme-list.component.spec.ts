import {fakeAsync, ComponentFixture, TestBed} from '@angular/core/testing';

import {ContextSchemeListComponent} from './context-scheme-list.component';

describe('ContextSchemeListComponent', () => {
  let component: ContextSchemeListComponent;
  let fixture: ComponentFixture<ContextSchemeListComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ContextSchemeListComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ContextSchemeListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
