import {ComponentFixture, TestBed} from '@angular/core/testing';

import {OasDocCreateComponent} from './oas-doc-create.component';

describe('OasDocCreateComponent', () => {
  let component: OasDocCreateComponent;
  let fixture: ComponentFixture<OasDocCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OasDocCreateComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OasDocCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
