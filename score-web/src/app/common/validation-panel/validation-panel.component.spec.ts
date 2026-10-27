import {Component} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ValidationLocation, ValidationReport, ValidationSession, Violation} from '../../validation/validation';
import {ValidationPanelComponent} from './validation-panel.component';

function location(label: string, address: string, role?: string): ValidationLocation {
  return {
    label, role,
    path: address.split('>').map(part => ({type: part.split('-')[0], manifestId: part.split('-')[1]}))
  };
}

function violation(severity: 'ERROR' | 'WARNING' | 'INFO', message: string, locations: ValidationLocation[] = [],
                   subject?: string): Violation {
  return {
    ruleId: 'RULE-1', severity, message, params: {}, locations,
    subject: subject ? {type: 'ACC', id: '9', label: subject} : null
  };
}

function reportOf(...violations: Violation[]): ValidationReport {
  return {subject: {type: 'ACC', id: '1'}, validatedAt: '', violations, status: 'HAS_ERRORS'};
}

@Component({
  imports: [ValidationPanelComponent],
  template: `
    <score-validation-panel [session]="session" [title]="title" [height]="height" [decision]="decision" [note]="note"
                            (locationSelect)="selected.push($event)" (closed)="closed = closed + 1"
                            (heightChange)="heights.push($event)" (proceed)="proceeded = proceeded + 1"
                            (cancel)="cancelled = cancelled + 1"/>`
})
class HostComponent {
  session = new ValidationSession();
  title = 'Validation Results';
  height = 240;
  decision = false;
  note = '';
  selected: ValidationLocation[] = [];
  heights: number[] = [];
  closed = 0;
  proceeded = 0;
  cancelled = 0;
}

describe('ValidationPanelComponent', () => {
  let fixture: ComponentFixture<HostComponent>;
  let host: HostComponent;
  let root: HTMLElement;

  const render = (report?: ValidationReport, change: (host: HostComponent) => void = () => undefined) => {
    TestBed.configureTestingModule({});
    fixture = TestBed.createComponent(HostComponent);
    host = fixture.componentInstance;
    if (report) {
      host.session.apply(report);
    }
    change(host);
    fixture.detectChanges();
    root = fixture.nativeElement as HTMLElement;
  };

  const text = (selector: string) => root.querySelector(selector)?.textContent?.replace(/\s+/g, ' ').trim();
  const rows = () => Array.from(root.querySelectorAll<HTMLElement>('.violation'));
  const panel = () => root.querySelector<HTMLElement>('.validation-panel');
  const key = (name: string) => {
    const event = new KeyboardEvent('keydown', {key: name, cancelable: true, bubbles: true});
    panel().dispatchEvent(event);
    fixture.detectChanges();
    return event;
  };

  describe('what it says', () => {
    it('gives the title and the numbers of the problems by severity', () => {
      render(reportOf(
        violation('ERROR', 'a'), violation('ERROR', 'b'), violation('WARNING', 'c'), violation('INFO', 'd'), violation('INFO', 'e')));

      expect(text('.panel-title')).toBe('Validation Results');
      expect(text('.panel-summary')).toBe('2 errors · 1 warning · 2 info');
    });

    it('does not make a plural of a single problem', () => {
      render(reportOf(violation('ERROR', 'a')));

      expect(text('.panel-summary')).toBe('1 error');
    });

    it('takes the title the page gives it', () => {
      render(reportOf(violation('ERROR', 'a')), h => h.title = 'Problems');

      expect(text('.panel-title')).toBe('Problems');
    });

    it('adds the note of the page to the summary', () => {
      render(reportOf(violation('ERROR', 'a')), h => h.note = 'Checked 305 ACCs');

      expect(root.textContent).toContain('· Checked 305 ACCs');
    });

    it('does not add a note when the page gives none', () => {
      render(reportOf(violation('ERROR', 'a')));

      expect(root.querySelectorAll('.panel-summary').length).toBe(1);
    });

    it('says that the results may be outdated once the page has changed', () => {
      render(reportOf(violation('ERROR', 'a')), h => h.session.markStale());

      expect(text('.panel-stale')).toBe('Results may be outdated — validate again');
      expect(panel().classList).toContain('stale');
    });

    it('does not say so while the page is as it was', () => {
      render(reportOf(violation('ERROR', 'a')));

      expect(root.querySelector('.panel-stale')).toBeNull();
      expect(panel().classList).not.toContain('stale');
    });

    it('says that there is nothing to show when there is no report', () => {
      render();

      expect(rows().length).toBe(0);
      expect(text('.empty')).toBe('Nothing to show.');
    });

    it('takes its height from the page', () => {
      render(reportOf(violation('ERROR', 'a')), h => h.height = 300);

      expect(panel().style.height).toBe('300px');
    });
  });

  describe('a problem', () => {
    it('shows its message, and the ACC it is about when it is not the one validated', () => {
      render(reportOf(violation('ERROR', 'The property term "Amount" occurs twice.', [], 'Order. Details')));

      expect(rows().length).toBe(1);
      expect(text('.violation .message')).toBe('The property term "Amount" occurs twice.');
      expect(text('.violation .subject')).toBe('Order. Details');
    });

    it('shows no subject when the problem is about the ACC validated', () => {
      render(reportOf(violation('ERROR', 'a')));

      expect(root.querySelector('.violation .subject')).toBeNull();
    });

    it('marks it with the icon and the colour of its severity', () => {
      render(reportOf(violation('ERROR', 'a'), violation('WARNING', 'b'), violation('INFO', 'c')));

      const icons = Array.from(root.querySelectorAll<HTMLElement>('.violation mat-icon.severity'));
      expect(icons.map(icon => icon.textContent.trim())).toEqual(['error', 'warning', 'info']);
      expect(icons.map(icon => icon.classList.contains('error') + '/' + icon.classList.contains('warning') + '/' + icon.classList.contains('info')))
        .toEqual(['true/false/false', 'false/true/false', 'false/false/true']);
    });

    it('lists the nodes it is about, with how the second one relates to the first', () => {
      render(reportOf(violation('ERROR', 'a', [
        location('Amount', 'ACC-1>BCC-7', 'first occurrence'), location('Amount', 'ACC-1>ACC-2>BCC-8', 'conflicts with'),
        location('Amount', 'ACC-1>BCC-9')])));

      const links = Array.from(root.querySelectorAll<HTMLElement>('.violation .location-link'));
      expect(links.map(link => link.textContent.trim())).toEqual(['Amount', 'Amount', 'Amount']);
      // the first one has no role; "and" stands for a role that is not given
      expect(Array.from(root.querySelectorAll('.violation .role')).map(role => role.textContent.trim()))
        .toEqual(['conflicts with', 'and']);
    });

    it('lists no nodes for a problem that is about none', () => {
      render(reportOf(violation('WARNING', 'a')));

      expect(root.querySelector('.violation .locations')).toBeNull();
    });
  });

  describe('going to a node', () => {
    const twoNodes = () => reportOf(violation('ERROR', 'a', [location('First', 'ACC-1>BCC-7'), location('Second', 'ACC-1>BCC-8')]));

    it('tells the page which node a click on its name is about', () => {
      render(twoNodes());

      root.querySelectorAll<HTMLElement>('.location-link')[1].click();

      expect(host.selected.map(l => l.label)).toEqual(['Second']);
    });

    it('goes to the first node when the row is clicked', () => {
      render(twoNodes());

      rows()[0].click();

      expect(host.selected.map(l => l.label)).toEqual(['First']);
    });

    it('goes to the node of the name only, not to the first node of the row as well', () => {
      render(twoNodes());

      root.querySelectorAll<HTMLElement>('.location-link')[1].click();

      expect(host.selected.length).toBe(1);
    });

    it('remembers the nodes that were visited', () => {
      render(twoNodes());
      const links = () => Array.from(root.querySelectorAll<HTMLElement>('.location-link'));

      links()[1].click();
      fixture.detectChanges();

      expect(links().map(link => link.classList.contains('visited'))).toEqual([false, true]);
    });

    it('points at the row of the node whose marker was clicked', () => {
      render(twoNodes(), h => h.session.focusAddress = 'ACC-1>BCC-8');

      expect(rows()[0].classList).toContain('active');
    });

    it('does not point at a row that is not about that node', () => {
      render(twoNodes(), h => h.session.focusAddress = 'ACC-1>BCC-99');

      expect(rows()[0].classList).not.toContain('active');
    });

    it('does nothing for a problem that is about no node', () => {
      render(reportOf(violation('WARNING', 'a')));

      rows()[0].click();

      expect(host.selected).toEqual([]);
    });
  });

  describe('the keyboard', () => {
    const three = () => reportOf(
      violation('ERROR', 'a', [location('A', 'ACC-1>BCC-1')]),
      violation('ERROR', 'b', [location('B', 'ACC-1>BCC-2')]),
      violation('ERROR', 'c', [location('C', 'ACC-1>BCC-3')]));

    it('moves down and up the rows, and stops at both ends', () => {
      render(three());

      key('ArrowDown');
      expect(rows().map(r => r.classList.contains('active'))).toEqual([true, false, false]);
      key('ArrowDown');
      key('ArrowDown');
      key('ArrowDown');
      expect(rows().map(r => r.classList.contains('active'))).toEqual([false, false, true]);
      key('ArrowUp');
      key('ArrowUp');
      key('ArrowUp');
      expect(rows().map(r => r.classList.contains('active'))).toEqual([true, false, false]);
    });

    it('goes to the first node of the row it is on when Enter is pressed', () => {
      render(three());
      key('ArrowDown');
      key('ArrowDown');

      key('Enter');

      expect(host.selected.map(l => l.label)).toEqual(['B']);
    });

    it('does nothing on Enter before a row is chosen', () => {
      render(three());

      key('Enter');

      expect(host.selected).toEqual([]);
    });

    it('closes on Escape', () => {
      render(three());

      key('Escape');

      expect(host.closed).toBe(1);
    });

    it('keeps the keys it does not use, and keeps those it uses from scrolling the page', () => {
      render(three());

      expect(key('a').defaultPrevented).toBe(false);
      expect(key('ArrowDown').defaultPrevented).toBe(true);
    });

    it('has no row to move on when there is nothing to show', () => {
      render();

      expect(key('ArrowDown').defaultPrevented).toBe(false);
      key('Escape');
      expect(host.closed).toBe(0);
    });
  });

  describe('closing', () => {
    const closeButton = () => root.querySelector<HTMLButtonElement>('button[aria-label="Close"]');

    it('closes the results', () => {
      render(reportOf(violation('ERROR', 'a')));

      closeButton().click();

      expect(host.closed).toBe(1);
      expect(host.cancelled).toBe(0);
    });

    it('is a Cancel when the panel asks whether a change is made', () => {
      render(reportOf(violation('ERROR', 'a')), h => h.decision = true);

      closeButton().click();

      expect(host.cancelled).toBe(1);
      expect(host.closed).toBe(0);
    });

    it('closes on Escape as a Cancel when the panel asks', () => {
      render(reportOf(violation('ERROR', 'a')), h => h.decision = true);

      key('Escape');

      expect(host.cancelled).toBe(1);
      expect(host.closed).toBe(0);
    });
  });

  describe('the question of a change', () => {
    it('asks nothing when the results are only shown', () => {
      render(reportOf(violation('ERROR', 'a')));

      expect(root.querySelector('.panel-footer')).toBeNull();
      expect(root.querySelector('#validation-proceed')).toBeNull();
      expect(root.querySelector('#validation-cancel')).toBeNull();
    });

    it('asks whether to make the change all the same, with Cancel and Proceed anyway', () => {
      render(reportOf(violation('ERROR', 'a')), h => h.decision = true);

      expect(text('.footer-text')).toBe('Make the change all the same?');
      expect(text('#validation-cancel')).toBe('Cancel');
      expect(text('#validation-proceed')).toBe('Proceed anyway');
    });

    it('tells the page which of the two was chosen', () => {
      render(reportOf(violation('ERROR', 'a')), h => h.decision = true);

      root.querySelector<HTMLButtonElement>('#validation-proceed').click();
      expect([host.proceeded, host.cancelled]).toEqual([1, 0]);

      root.querySelector<HTMLButtonElement>('#validation-cancel').click();
      expect([host.proceeded, host.cancelled]).toEqual([1, 1]);
    });
  });

  describe('its height', () => {
    const drag = (clientY: number) => {
      panel().querySelector<HTMLElement>('.resize-handle').dispatchEvent(new MouseEvent('mousedown', {bubbles: true, cancelable: true}));
      document.dispatchEvent(new MouseEvent('mousemove', {clientY}));
    };

    beforeEach(() => {
      Object.defineProperty(window, 'innerHeight', {configurable: true, value: 800});
    });

    it('follows the mouse while the handle is held', () => {
      render(reportOf(violation('ERROR', 'a')));

      drag(500);

      expect(host.heights).toEqual([300]);
    });

    it('is never lower than the room it needs', () => {
      render(reportOf(violation('ERROR', 'a')));

      drag(790);

      expect(host.heights).toEqual([96]);
    });

    it('leaves the room for the page above it', () => {
      render(reportOf(violation('ERROR', 'a')));

      drag(0);

      expect(host.heights).toEqual([680]);
    });

    it('stops following when the mouse is let go', () => {
      render(reportOf(violation('ERROR', 'a')));
      drag(500);

      document.dispatchEvent(new MouseEvent('mouseup'));
      document.dispatchEvent(new MouseEvent('mousemove', {clientY: 400}));

      expect(host.heights).toEqual([300]);
    });

    it('does not change while the handle is not held', () => {
      render(reportOf(violation('ERROR', 'a')));

      document.dispatchEvent(new MouseEvent('mousemove', {clientY: 400}));

      expect(host.heights).toEqual([]);
    });
  });
});
