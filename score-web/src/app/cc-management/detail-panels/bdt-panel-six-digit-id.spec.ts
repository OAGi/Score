import {BdtPanelComponent} from './bdt-panel.component';

/** The Six Hexadecimal Identifier is exactly six hexadecimal characters. */
describe('BdtPanelComponent.randSixDigitId', () => {
  const generate = () => BdtPanelComponent.prototype.randSixDigitId.call({});

  it('always returns six upper-case hexadecimal characters', () => {
    for (let i = 0; i < 500; i++) {
      expect(generate()).toMatch(/^[0-9A-F]{6}$/);
    }
  });

  it('pads small values to six characters', () => {
    vi.spyOn(Math, 'random').mockReturnValue(0);
    expect(generate()).toBe('000000');
    vi.restoreAllMocks();
  });
});
