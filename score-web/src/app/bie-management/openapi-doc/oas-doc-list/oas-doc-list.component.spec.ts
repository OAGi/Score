import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {OasDocListComponent} from './oas-doc-list.component';

describe('OasDocListComponent', () => {
  let component: OasDocListComponent;
  let fixture: ComponentFixture<OasDocListComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [OasDocListComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OasDocListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
