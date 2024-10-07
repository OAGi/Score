import {ComponentFixture, TestBed} from '@angular/core/testing';

import {OasDocBieListComponent} from './oas-doc-bie-list.component';

describe('OasDocBieListComponent', () => {
  let component: OasDocBieListComponent;
  let fixture: ComponentFixture<OasDocBieListComponent>;

  beforeEach(fakeAsync () => {
    await TestBed.configureTestingModule({
      declarations: [ OasDocBieListComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OasDocBieListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
