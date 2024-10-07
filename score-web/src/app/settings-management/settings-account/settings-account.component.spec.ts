import {fakeAsync, ComponentFixture, TestBed} from '@angular/core/testing';

import {SettingsAccountComponent} from './settings-account.component';

describe('SettingsProfileComponent', () => {
  let component: SettingsAccountComponent;
  let fixture: ComponentFixture<SettingsAccountComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [SettingsAccountComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SettingsAccountComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
