import {fakeAsync, ComponentFixture, TestBed} from '@angular/core/testing';

import {ModuleSetReleaseDetailComponent} from './module-set-release-detail.component';

describe('ModuleSetReleaseCreateComponent', () => {
  let component: ModuleSetReleaseDetailComponent;
  let fixture: ComponentFixture<ModuleSetReleaseDetailComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ModuleSetReleaseDetailComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModuleSetReleaseDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
