import {fakeAsync, ComponentFixture, TestBed} from '@angular/core/testing';

import {ModuleSetReleaseCreateComponent} from './module-set-release-create.component';

describe('ModuleSetReleaseCreateComponent', () => {
  let component: ModuleSetReleaseCreateComponent;
  let fixture: ComponentFixture<ModuleSetReleaseCreateComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ModuleSetReleaseCreateComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModuleSetReleaseCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
