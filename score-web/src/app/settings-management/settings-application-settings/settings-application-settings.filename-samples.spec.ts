import {vi} from 'vitest';
import {of, Subject, throwError} from 'rxjs';
import {SettingsApplicationSettingsComponent} from './settings-application-settings.component';

describe('SettingsApplicationSettingsComponent filename samples', () => {
  const proto = SettingsApplicationSettingsComponent.prototype as any;

  function ctx(overrides: any = {}): any {
    const c: any = {
      applicationSettingsInfo: {
        bieSchemaFilenameExpression: '{BIE ID}',
        bieSchemaFilenameDuplicateHandlerExpression: '-{Incremental}',
        biePackageSchemaFilenameExpression: '{BIE Package Name}',
        biePackageSchemaFilenameDuplicateHandlerExpression: '~{BIE ID}'
      },
      commonSampleKeys: ['BIE ID', 'BIE Property Term', 'BIE Display Name', 'BIE Version', 'BIE DEN', 'BIE Status', 'BIE Remark'],
      packageSampleKeys: ['BIE Package Name', 'BIE Package Version ID', 'BIE Package Version Name'],
      bieSchemaSamples: null,
      biePackageSchemaSamples: null,
      bieSchemaValidationMessage: '',
      biePackageSchemaValidationMessage: '',
      bieSchemaFilenameExpressionError: '',
      biePackageSchemaFilenameExpressionError: '',
      bieSchemaPreview$: {next: vi.fn()},
      biePackageSchemaPreview$: {next: vi.fn()},
      settingsService: {validateFilenameExpression: vi.fn()},
      ...overrides
    };
    for (const name of ['samplesOf', 'sampleKeysOf', 'queuePreview', 'queueBieSchemaFilenamePreview',
      'queueBiePackageSchemaFilenamePreview', 'setSampleValue', 'setSampleBusinessContextName',
      'addSampleBusinessContextName', 'removeSampleBusinessContextName', 'resetSamples', 'trackByIndex',
      'validateExpression', 'setValidationMessage', 'setFilenameExpressionError', 'getFilenameExpressionErrorMessage',
      'getBieSchemaFilenamePreviewRequest', 'getBiePackageSchemaFilenamePreviewRequest']) {
      c[name] = proto[name];
    }
    return c;
  }

  const samples = () => ({values: {'BIE ID': '1', 'BIE Property Term': 'Item Master'}, businessContextNames: ['A', 'B']});

  it('offers the package placeholders only for the package expression', () => {
    const c = ctx();

    expect(c.sampleKeysOf('bie-schema')).not.toContain('BIE Package Name');
    expect(c.sampleKeysOf('bie-package-schema')).toEqual(expect.arrayContaining(['BIE ID', 'BIE Package Name', 'BIE Package Version ID']));
  });

  it('sends the preview request without samples until the backend has answered with its defaults', () => {
    const c = ctx();

    expect(c.getBieSchemaFilenamePreviewRequest()).toEqual({
      expression: '{BIE ID}', duplicateHandlerExpression: '-{Incremental}',
      sampleValues: undefined, sampleBusinessContextNames: undefined
    });
  });

  it('sends the edited samples with the preview request', () => {
    const c = ctx({biePackageSchemaSamples: samples()});

    expect(c.getBiePackageSchemaFilenamePreviewRequest()).toMatchObject({
      expression: '{BIE Package Name}', sampleValues: {'BIE ID': '1', 'BIE Property Term': 'Item Master'},
      sampleBusinessContextNames: ['A', 'B']
    });
  });

  it('previews again when a sample value changes', () => {
    const c = ctx({bieSchemaSamples: samples()});

    c.setSampleValue('bie-schema', 'BIE Property Term', 'Order Line');

    expect(c.bieSchemaSamples.values['BIE Property Term']).toBe('Order Line');
    expect(c.bieSchemaPreview$.next).toHaveBeenCalledWith(expect.objectContaining({
      sampleValues: expect.objectContaining({'BIE Property Term': 'Order Line'})
    }));
    expect(c.biePackageSchemaPreview$.next).not.toHaveBeenCalled();
  });

  it('previews every sample value edit through the real preview stream', () => {
    vi.useFakeTimers();
    try {
      const preview$ = new Subject<any>();
      const previewFilenameExpression = vi.fn().mockReturnValue(of({
        sampleFilename: 'x', sampleDuplicateFilename: 'y', sampleValues: {}, sampleBusinessContextNames: []
      }));
      const c = ctx({
        bieSchemaSamples: samples(), bieSchemaPreview$: preview$, biePackageSchemaPreview$: new Subject<any>(),
        destroy$: new Subject<void>(), setFilenamePreview: vi.fn(),
        settingsService: {previewFilenameExpression}
      });
      c.initializeFilenamePreviewStreams = proto.initializeFilenamePreviewStreams;
      c.initializeFilenamePreviewStreams();

      c.setSampleValue('bie-schema', 'BIE Property Term', 'One');
      vi.advanceTimersByTime(300);
      c.setSampleValue('bie-schema', 'BIE Property Term', 'Two');
      vi.advanceTimersByTime(300);

      expect(previewFilenameExpression).toHaveBeenCalledTimes(2);
      expect(previewFilenameExpression.mock.calls.map(call => call[3]['BIE Property Term'])).toEqual(['One', 'Two']);
    } finally {
      vi.useRealTimers();
    }
  });

  it('edits, adds and removes business context names', () => {
    const c = ctx({biePackageSchemaSamples: samples()});

    c.setSampleBusinessContextName('bie-package-schema', 1, 'Changed');
    c.addSampleBusinessContextName('bie-package-schema');
    expect(c.biePackageSchemaSamples.businessContextNames).toEqual(['A', 'Changed', 'Business Context 3']);

    c.removeSampleBusinessContextName('bie-package-schema', 0);
    expect(c.biePackageSchemaSamples.businessContextNames).toEqual(['Changed', 'Business Context 3']);
    expect(c.biePackageSchemaPreview$.next).toHaveBeenCalledTimes(3);
  });

  it('allows removing every business context name', () => {
    const c = ctx({bieSchemaSamples: samples()});

    c.removeSampleBusinessContextName('bie-schema', 0);
    c.removeSampleBusinessContextName('bie-schema', 0);

    expect(c.bieSchemaSamples.businessContextNames).toEqual([]);
  });

  it('returns to the defaults by forgetting the samples and previewing without them', () => {
    const c = ctx({bieSchemaSamples: samples()});

    c.resetSamples('bie-schema');

    expect(c.bieSchemaSamples).toBeNull();
    expect(c.bieSchemaPreview$.next).toHaveBeenCalledWith(expect.objectContaining({sampleValues: undefined}));
  });

  it('clears the validation message and the error when the expression changes', () => {
    const c = ctx({bieSchemaValidationMessage: 'The expression is valid.', bieSchemaFilenameExpressionError: 'bad'});

    c.queueBieSchemaFilenamePreview();

    expect(c.bieSchemaValidationMessage).toBe('');
    expect(c.bieSchemaFilenameExpressionError).toBe('');
  });

  it('says the expression is valid when the backend accepts it', () => {
    const c = ctx();
    c.settingsService.validateFilenameExpression.mockReturnValue(of(null));

    c.validateExpression('bie-schema');

    expect(c.settingsService.validateFilenameExpression).toHaveBeenCalledWith('bie-schema', '{BIE ID}', '-{Incremental}');
    expect(c.bieSchemaValidationMessage).toBe('The expression is valid.');
    expect(c.bieSchemaFilenameExpressionError).toBe('');
  });

  it('shows the backend message when the expression is not valid', () => {
    const c = ctx({bieSchemaValidationMessage: 'The expression is valid.'});
    c.settingsService.validateFilenameExpression.mockReturnValue(
      throwError(() => ({headers: {get: () => 'Unknown filename placeholder: {Nope}'}})));

    c.validateExpression('bie-schema');

    expect(c.bieSchemaValidationMessage).toBe('');
    expect(c.bieSchemaFilenameExpressionError).toBe('Unknown filename placeholder: {Nope}');
  });

  it('validates the package expression with its own duplicate handler', () => {
    const c = ctx();
    c.settingsService.validateFilenameExpression.mockReturnValue(of(null));

    c.validateExpression('bie-package-schema');

    expect(c.settingsService.validateFilenameExpression)
      .toHaveBeenCalledWith('bie-package-schema', '{BIE Package Name}', '~{BIE ID}');
    expect(c.biePackageSchemaValidationMessage).toBe('The expression is valid.');
    expect(c.bieSchemaValidationMessage).toBe('');
  });
});
