import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {OasDocDetailComponent} from './oas-doc-detail.component';

describe('OasDocDetailComponent', () => {
  let component: OasDocDetailComponent;
  let fixture: ComponentFixture<OasDocDetailComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [OasDocDetailComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OasDocDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
