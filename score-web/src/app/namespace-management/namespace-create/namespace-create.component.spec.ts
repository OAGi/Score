import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {NamespaceCreateComponent} from './namespace-create.component';

describe('NamespaceCreateComponent', () => {
  let component: NamespaceCreateComponent;
  let fixture: ComponentFixture<NamespaceCreateComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [NamespaceCreateComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NamespaceCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
