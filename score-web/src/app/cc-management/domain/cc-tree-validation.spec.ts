import {CcTreeValidation, CcTreeValidationHost} from './cc-tree-validation';
import {StructureChangeReport, ValidationReport, Violation} from '../../validation/validation';

function violation(accId: string): Violation {
  return {
    ruleId: 'DUP-002', severity: 'ERROR', message: 'Duplicate', params: {propertyTerm: 'Amount'},
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

  describe('what the tree shows of it', () => {
    const rootNode: any = {path: 'ACC-1', level: 0};
    const bcc: any = {path: 'ACC-1>BCC-7', level: 1};
    const other: any = {path: 'ACC-1>BCC-8', level: 1};

    function reportOf(...violations: Violation[]): ValidationReport {
      return {subject: {type: 'ACC', id: '1'}, validatedAt: '', violations, status: 'HAS_ERRORS'};
    }

    function onBcc(severity: 'ERROR' | 'WARNING' | 'INFO', message: string): Violation {
      return {
        ruleId: 'R', severity, message, params: {},
        locations: [{label: 'Amount', path: [{type: 'ACC', manifestId: '1'}, {type: 'BCC', manifestId: '7'}]}]
      };
    }

    it('is given to the tree as one decorator', () => {
      expect(validation.decorators).toEqual([validation]);
    });

    it('shows nothing before anything is validated', () => {
      expect(validation.decorationsFor(rootNode)).toEqual([]);
      expect(validation.decorationsFor(bcc)).toEqual([]);
    });

    it('marks the node a problem is about with the icon of its severity, and nothing else', () => {
      validation.apply(reportOf(onBcc('WARNING', 'Twice')));

      const marks = validation.decorationsFor(bcc);

      expect(marks.length).toBe(1);
      expect(marks[0].icon).toBe('warning');
      expect(marks[0].color).toBe('#f57c00');
      expect(marks[0].tooltip).toBe('Twice');
      expect(marks[0].opacity).toBe(1);
      expect(validation.decorationsFor(other)).toEqual([]);
    });

    it('marks a node with the most serious of its problems and tells all their messages', () => {
      validation.apply(reportOf(onBcc('INFO', 'Fyi'), onBcc('ERROR', 'Twice')));

      const mark = validation.decorationsFor(bcc)[0];

      expect(mark.icon).toBe('error');
      expect(mark.color).toBe('#d32f2f');
      expect(mark.tooltip).toBe('Fyi\nTwice');
    });

    it('dims a mark and says that it may be outdated once the page has changed', () => {
      validation.apply(reportOf(onBcc('ERROR', 'Twice')));
      validation.markStale();

      const mark = validation.decorationsFor(bcc)[0];

      expect(mark.opacity).toBe(0.45);
      expect(mark.tooltip).toBe('Twice\n(may be outdated)');
    });

    it('opens the panel at the node when its mark is clicked', () => {
      validation.apply(reportOf(onBcc('ERROR', 'Twice')));
      validation.close();

      validation.decorationsFor(bcc)[0].onClick();

      expect(validation.panelOpen).toBe(true);
      expect(validation.focusAddress).toBe('ACC-1>BCC-7');
    });

    it('gives the root a button for a closed panel while no node of a problem is on screen', () => {
      validation.apply(reportOf(onBcc('ERROR', 'Twice')));
      validation.close();

      const buttons = validation.decorationsFor(rootNode);

      expect(buttons.length).toBe(1);
      expect(buttons[0].id).toBe('cc-validation-show');
      expect(buttons[0].icon).toBe('error');
      expect(buttons[0].tooltip).toBe('Show validation results');
      buttons[0].onClick();
      expect(validation.panelOpen).toBe(true);
      expect(validation.focusAddress).toBeNull();
    });

    it('gives the root a warning button when the problems are warnings', () => {
      validation.apply(reportOf(onBcc('WARNING', 'Twice')));
      validation.close();

      expect(validation.decorationsFor(rootNode)[0].icon).toBe('warning');
    });

    it('gives the root no button while the panel is open', () => {
      validation.apply(reportOf(onBcc('ERROR', 'Twice')));

      expect(validation.decorationsFor(rootNode).filter(d => d.id === 'cc-validation-show')).toEqual([]);
    });

    it('gives the root no button when a node a problem is about is on screen: it has a mark of its own', () => {
      (host.dataSource as any).data = [rootNode, bcc];
      validation.apply(reportOf(onBcc('ERROR', 'Twice')));
      validation.close();

      expect(validation.decorationsFor(rootNode)).toEqual([]);
    });

    it('gives a node that is not the root no button', () => {
      validation.apply(reportOf(onBcc('ERROR', 'Twice')));
      validation.close();

      expect(validation.decorationsFor(other)).toEqual([]);
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

    describe('in the tree of the page', () => {
      const root: any = {path: 'ACC-1', parent: undefined, children: []};
      const base: any = {path: 'ACC-1>ACC-2', parent: root, children: []};
      const property: any = {path: 'ACC-1>ACC-2>BCC-7', parent: base, children: []};
      let dataSource: any;

      function at(address: string) {
        return {
          label: address,
          path: address.split('>').map(part => ({type: part.split('-')[0], manifestId: part.split('-')[1]}))
        };
      }

      beforeEach(() => {
        root.children = [base];
        base.children = [property];
        dataSource = {data: [root], database: {loadChildren: vi.fn()}, expandTo: vi.fn()};
        (host as any).dataSource = dataSource;
      });

      it('opens the way to the node, shows it in the detail pane and scrolls to it', () => {
        const went = validation.navigateTo(at('ACC-1>ACC-2>BCC-7'));

        expect(went).toBe(true);
        expect(dataSource.expandTo).toHaveBeenCalledWith(base);
        expect(host.onClick).toHaveBeenCalledWith(property);
        expect(host.scrollToNode).toHaveBeenCalledWith(property, 100);
        expect(snackBar.open).not.toHaveBeenCalled();
      });

      it('opens no way to the root, which has none to open', () => {
        const went = validation.navigateTo(at('ACC-1'));

        expect(went).toBe(true);
        expect(dataSource.expandTo).not.toHaveBeenCalled();
        expect(host.onClick).toHaveBeenCalledWith(root);
      });

      it('says that the tree is outdated when the node is not there any more, and marks the results so', () => {
        validation.apply(change(violation('1')).report);

        const went = validation.navigateTo(at('ACC-1>ACC-2>BCC-99'));

        expect(went).toBe(false);
        expect(snackBar.open).toHaveBeenCalledWith('That node is no longer in the tree — re-validate', '', {duration: 4000});
        expect(validation.stale).toBe(true);
        expect(host.onClick).not.toHaveBeenCalled();
        expect(host.scrollToNode).not.toHaveBeenCalled();
      });

      it('goes on after a node of the results was not found: the nodes that are there are still reached', () => {
        validation.apply(change(violation('1')).report);
        validation.navigateTo(at('ACC-1>ACC-2>BCC-99'));

        expect(validation.navigateTo(at('ACC-1>ACC-2>BCC-7'))).toBe(true);
        expect(host.onClick).toHaveBeenCalledWith(property);
      });

      it('opens the page of the kind of object that a problem of another object is about', () => {
        const open = vi.spyOn(window, 'open').mockReturnValue(null);

        validation.navigateTo(at('ASCCP-5'));
        validation.navigateTo(at('BCCP-6'));
        validation.navigateTo(at('DT-7'));

        expect(open.mock.calls.map(call => call[0])).toEqual(
          ['/core_component/asccp/5', '/core_component/bccp/6', '/data_type/7']);
        open.mockRestore();
      });
    });

    it('does not say that the tree is outdated when a node of the change that is not made yet is not there', () => {
      validation.askAboutChange(change(violation('1')), vi.fn());

      const went = validation.navigateTo(violation('1').locations[0]);

      expect(went).toBe(false);
      expect(snackBar.open).not.toHaveBeenCalled();
    });
  });
});
