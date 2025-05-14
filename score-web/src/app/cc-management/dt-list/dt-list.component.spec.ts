import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {DtListComponent} from './dt-list.component';

describe('CcListComponent', () => {
  let component: DtListComponent;
  let fixture: ComponentFixture<DtListComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [DtListComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DtListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
