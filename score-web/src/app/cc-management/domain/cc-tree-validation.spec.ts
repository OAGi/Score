import {CcTreeValidation, CcTreeValidationHost} from './cc-tree-validation';
import {StructureChangeReport, Violation} from '../../validation/validation';

function violation(accId: string): Violation {
  return {
    ruleId: 'ACC-BASE-01', severity: 'ERROR', message: 'Duplicate', params: {propertyTerm: 'Amount'},
    locations: [{label: 'Amount', path: [{type: 'ACC', manifestId: accId}, {type: 'BCC', manifestId: '7'}]}],
    subject: {type: 'ACC', id: accId, label: 'ACC ' + accId}
  };
}

function change(...violations: Violation[]): StructureChangeReport {
  return {
    checkedCount: 305,
    report: {
      subject: {type: 'ACC', id: '1'}, validatedAt: '', violations,
      status: violations.length ? 'HAS_ERRORS' : 'VALID'
    }
  };
}

describe('CcTreeValidation', () => {
  let host: CcTreeValidationHost;
  let validation: CcTreeValidation;
  let snackBar: { open: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    host = {
      type: 'ACC', manifestId: 1, dataSource: {data: []} as any,
      onClick: vi.fn(), scrollToNode: vi.fn(), element: document.createElement('div')
    };
    snackBar = {open: vi.fn()};
    validation = new CcTreeValidation(host, {} as any, snackBar as any);
  });

  describe('a change that has not been made', () => {
    it('is not asked about when it would break nothing', () => {
      const proceed = vi.fn();

      expect(validation.askAboutChange(change(), proceed)).toBe(false);

      expect(validation.hasReport).toBe(false);
      expect(validation.decision).toBeNull();
      expect(proceed).not.toHaveBeenCalled();
    });

    it('shows what it would break, says how many ACCs were checked, and waits for the answer', () => {
      const proceed = vi.fn();

      expect(validation.askAboutChange(change(violation('2'), violation('3')), proceed)).toBe(true);

      expect(validation.hasReport).toBe(true);
      expect(validation.panelOpen).toBe(true);
      expect(validation.decision?.note).toBe('Checked 305 ACCs');
      expect(proceed).not.toHaveBeenCalled();
    });

    it('is made when the user says so, and the question goes away', () => {
      const proceed = vi.fn();
      validation.askAboutChange(change(violation('2')), proceed);

      validation.proceedWithChange();

      expect(proceed).toHaveBeenCalledTimes(1);
      expect(validation.hasReport).toBe(false);
      expect(validation.decision).toBeNull();
    });

    it('is not made when the user cancels, and the question goes away', () => {
      const proceed = vi.fn();
      validation.askAboutChange(change(violation('2')), proceed);

      validation.cancelChange();

      expect(proceed).not.toHaveBeenCalled();
      expect(validation.hasReport).toBe(false);
      expect(validation.decision).toBeNull();
    });

    it('is not made when the panel is closed: closing the question answers it', () => {
      const proceed = vi.fn();
      validation.askAboutChange(change(violation('2')), proceed);

      validation.close();

      expect(proceed).not.toHaveBeenCalled();
      expect(validation.decision).toBeNull();
    });

    it('is a plain closing of the results when nothing is asked', () => {
      validation.apply(change(violation('2')).report);

      validation.close();

      expect(validation.panelOpen).toBe(false);
      expect(validation.hasReport).toBe(true);
    });
  });

  describe('going to a problem', () => {
    it('opens the page of another ACC, where its problem is', () => {
      const open = vi.spyOn(window, 'open').mockReturnValue(null);

      const went = validation.navigateTo(violation('2').locations[0]);

      expect(went).toBe(true);
      expect(open).toHaveBeenCalledWith('/core_component/acc/2', '_blank');
      expect(host.onClick).not.toHaveBeenCalled();
      open.mockRestore();
    });

    it('does not say that the tree is outdated when a node of the change that is not made yet is not there', () => {
      validation.askAboutChange(change(violation('1')), vi.fn());

      const went = validation.navigateTo(violation('1').locations[0]);

      expect(went).toBe(false);
      expect(snackBar.open).not.toHaveBeenCalled();
    });
  });
});
