import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {PendingDetailComponent} from './pending-detail.component';

describe('PendingDetailComponent', () => {
  let component: PendingDetailComponent;
  let fixture: ComponentFixture<PendingDetailComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [PendingDetailComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PendingDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
