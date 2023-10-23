import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OasDocListComponent } from './oas-doc-list.component';

describe('OasDocListComponent', () => {
  let component: OasDocListComponent;
  let fixture: ComponentFixture<OasDocListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OasDocListComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OasDocListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
