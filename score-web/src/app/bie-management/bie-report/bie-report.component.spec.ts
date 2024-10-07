import {fakeAsync, ComponentFixture, TestBed} from '@angular/core/testing';

import {BieReportComponent} from './bie-report.component';

describe('BieListComponent', () => {
  let component: BieReportComponent;
  let fixture: ComponentFixture<BieReportComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [BieReportComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BieReportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
