import {fakeAsync, ComponentFixture, TestBed} from '@angular/core/testing';

import {CcListComponent} from './cc-list.component';

describe('CcListComponent', () => {
  let component: CcListComponent;
  let fixture: ComponentFixture<CcListComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [CcListComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CcListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
