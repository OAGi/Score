import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {BieListComponent} from './bie-list.component';

describe('BieListComponent', () => {
  let component: BieListComponent;
  let fixture: ComponentFixture<BieListComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [BieListComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BieListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
