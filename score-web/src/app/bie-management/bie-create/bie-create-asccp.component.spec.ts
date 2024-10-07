import {fakeAsync, ComponentFixture, TestBed} from '@angular/core/testing';

import {BieCreateAsccpComponent} from './bie-create-asccp.component';

describe('BieCopyProfileBieComponent', () => {
  let component: BieCreateAsccpComponent;
  let fixture: ComponentFixture<BieCreateAsccpComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [BieCreateAsccpComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BieCreateAsccpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
