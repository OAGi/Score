import {locationAddress, ValidationReport, ValidationSession, Violation} from './validation';

function violation(severity: Violation['severity'], ...addresses: string[]): Violation {
  return {
    ruleId: 'ACC-ASSOC-02', severity, message: 'Duplicate', params: {},
    locations: addresses.map(address => ({
      label: address,
      path: address.split('>').map(part => ({type: part.split('-')[0], manifestId: part.split('-')[1]}))
    }))
  };
}

function report(...violations: Violation[]): ValidationReport {
  const status = violations.some(v => v.severity === 'ERROR') ? 'HAS_ERRORS' : violations.length ? 'HAS_WARNINGS' : 'VALID';
  return {subject: {type: 'ACC', id: '1'}, validatedAt: '', status, violations};
}

describe('ValidationSession', () => {
  it('shows nothing before anything is validated', () => {
    const session = new ValidationSession();

    expect(session.hasReport).toBe(false);
    expect(session.panelOpen).toBe(false);
  });

  it('keeps a report that found something, opens the panel and marks both nodes of a duplicate', () => {
    const session = new ValidationSession();

    session.apply(report(violation('ERROR', 'ACC-1>BCC-10>BCCP-100', 'ACC-1>ACC-2>BCC-20>BCCP-200')));

    expect(session.hasReport).toBe(true);
    expect(session.panelOpen).toBe(true);
    expect(session.severityAt('ACC-1>BCC-10>BCCP-100')).toBe('ERROR');
    expect(session.severityAt('ACC-1>ACC-2>BCC-20>BCCP-200')).toBe('ERROR');
    expect(session.severityAt('ACC-1')).toBeUndefined();
  });

  it('marks a node with the most serious of its violations', () => {
    const session = new ValidationSession();

    session.apply(report(violation('WARNING', 'ACC-1>BCC-10>BCCP-100'), violation('ERROR', 'ACC-1>BCC-10>BCCP-100')));

    expect(session.severityAt('ACC-1>BCC-10>BCCP-100')).toBe('ERROR');
  });

  it('dims a report when the page changes and does not recompute it', () => {
    const session = new ValidationSession();
    session.apply(report(violation('ERROR', 'ACC-1>BCC-10>BCCP-100')));

    session.markStale();

    expect(session.stale).toBe(true);
    expect(session.hasReport).toBe(true);
    expect(session.severityAt('ACC-1>BCC-10>BCCP-100')).toBe('ERROR');
  });

  it('has nothing to dim when nothing was validated', () => {
    const session = new ValidationSession();

    session.markStale();

    expect(session.stale).toBe(false);
  });

  it('is cleared by a clean report: no panel, no markers, not stale', () => {
    const session = new ValidationSession();
    session.apply(report(violation('ERROR', 'ACC-1>BCC-10>BCCP-100')));
    session.markStale();

    session.apply(report());

    expect(session.hasReport).toBe(false);
    expect(session.panelOpen).toBe(false);
    expect(session.stale).toBe(false);
    expect(session.severityAt('ACC-1>BCC-10>BCCP-100')).toBeUndefined();
  });

  it('writes an address the way the tree does', () => {
    expect(locationAddress(violation('ERROR', 'ACC-1>ASCC-2>ASCCP-3').locations[0])).toBe('ACC-1>ASCC-2>ASCCP-3');
  });
});
