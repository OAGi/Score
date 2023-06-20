import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OasDocAssignComponent } from './oas-doc-assign.component';

describe('OasDocAssignComponent', () => {
  let component: OasDocAssignComponent;
  let fixture: ComponentFixture<OasDocAssignComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OasDocAssignComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OasDocAssignComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
