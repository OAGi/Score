import {vi} from 'vitest';
import {SettingsApplicationSettingsComponent} from './settings-application-settings.component';

describe('SettingsApplicationSettingsComponent Ctrl+S', () => {
  const proto = SettingsApplicationSettingsComponent.prototype as any;

  function ctx(section: string, overrides: any = {}): any {
    return {
      section,
      applicationSettingsInfo: {smtpSettingsInfo: {}},
      webPageInfo: {},
      updateWebPageInfo: vi.fn(),
      updateApplicationSettingsInfo: vi.fn(),
      updateBieFilenameExpressions: vi.fn(),
      ...overrides
    };
  }

  function press(c: any) {
    const event: any = {key: 's', ctrlKey: true, metaKey: false, preventDefault: vi.fn(), stopPropagation: vi.fn()};
    proto.handleKeyboardEvent.call(c, event);
    return event;
  }

  it('saves the SMTP settings on the smtp section only', () => {
    const c = ctx('smtp');
    const event = press(c);

    expect(c.updateApplicationSettingsInfo).toHaveBeenCalledTimes(1);
    expect(c.updateWebPageInfo).not.toHaveBeenCalled();
    expect(c.updateBieFilenameExpressions).not.toHaveBeenCalled();
    expect(event.preventDefault).toHaveBeenCalled();
  });

  it('saves the filename expressions on the filename-expressions section only', () => {
    const c = ctx('filename-expressions');
    press(c);

    expect(c.updateBieFilenameExpressions).toHaveBeenCalledTimes(1);
    expect(c.updateWebPageInfo).not.toHaveBeenCalled();
    expect(c.updateApplicationSettingsInfo).not.toHaveBeenCalled();
  });

  it('saves the web page settings on the web-page section when they are loaded', () => {
    const c = ctx('web-page');
    press(c);

    expect(c.updateWebPageInfo).toHaveBeenCalledTimes(1);
    expect(c.updateApplicationSettingsInfo).not.toHaveBeenCalled();
  });

  it('does nothing on the features section', () => {
    const c = ctx('features');
    const event = press(c);

    expect(c.updateWebPageInfo).not.toHaveBeenCalled();
    expect(c.updateApplicationSettingsInfo).not.toHaveBeenCalled();
    expect(c.updateBieFilenameExpressions).not.toHaveBeenCalled();
    expect(event.preventDefault).not.toHaveBeenCalled();
  });

  it('does nothing while the model of the section is not loaded', () => {
    const c = ctx('web-page', {webPageInfo: undefined});
    const event = press(c);

    expect(c.updateWebPageInfo).not.toHaveBeenCalled();
    expect(event.preventDefault).not.toHaveBeenCalled();
  });
});
