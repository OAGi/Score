import {TestBed} from '@angular/core/testing';
import {SettingsMenuComponent} from './settings-menu.component';
import {AuthService} from '../../authentication/auth.service';

describe('SettingsMenuComponent', () => {
  function create(active: string | null): SettingsMenuComponent {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [{provide: AuthService, useValue: {isAdmin: () => true}}]
    });
    const component = TestBed.runInInjectionContext(() => new SettingsMenuComponent());
    component.active = active;
    return component;
  }

  it('highlights Application settings on each of its pages only', () => {
    expect(create('ApplicationSettings:smtp').applicationSettingsActive).toBe(true);
    expect(create('Preferences').applicationSettingsActive).toBe(false);
    expect(create(null).applicationSettingsActive).toBe(false);
  });

  it('lists the four application-settings pages', () => {
    expect(create(null).applicationSettingsItems.map(i => i.key))
      .toEqual(['features', 'smtp', 'filename-expressions', 'web-page']);
  });
});
