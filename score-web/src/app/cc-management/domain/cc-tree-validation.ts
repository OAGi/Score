import {MatSnackBar} from '@angular/material/snack-bar';
import {
  StructureChangeReport,
  ValidationLocation,
  ValidationPathSegment,
  ValidationSession,
  ValidationSeverity
} from '../../validation/validation';
import {ValidationService} from '../../validation/validation.service';
import {NodeNavigator} from '../../validation/validation-navigator';
import {TreeNodeDecoration, TreeNodeDecorator} from '../../common/tree-viewport/tree-decorator';
import {CcFlatNode, CcFlatNodeDataSource} from './cc-flat-tree';
import {findNodeByLocation} from './cc-node-navigator';

/** What a validation on a page with a tree of core components needs from the page. */
export interface CcTreeValidationHost {
  /** The kind of object of the page: ACC, ASCCP, BCCP. */
  readonly type: string;
  readonly manifestId: number;
  readonly dataSource: CcFlatNodeDataSource<CcFlatNode>;
  /** Shows the node in the detail pane. */
  onClick(node: CcFlatNode): void;
  scrollToNode(node: CcFlatNode, offset: number): void;
  /** The element of the page, to keep what the user is reading in sight when the panel opens. */
  readonly element: HTMLElement;
}

/** The most serious problem a validation found with a node, and whether the page has changed since. */
export interface ValidationMarker {
  severity: ValidationSeverity;
  stale: boolean;
  messages: string[];
}

const SEVERITY_ICON: { [severity: string]: { icon: string; color: string } } = {
  ERROR: {icon: 'error', color: '#d32f2f'},
  WARNING: {icon: 'warning', color: '#f57c00'},
  INFO: {icon: 'info', color: '#1976d2'}
};

/**
 * What a page with a tree of core components knows about its last validation, and what it does with it: asks for it,
 * marks the nodes the problems are about and takes the user to them. Nothing of it is saved anywhere.
 */
export class CcTreeValidation extends ValidationSession implements NodeNavigator, TreeNodeDecorator<CcFlatNode> {

  panelHeight = 240;

  /** What the tree of the page is given to show of this validation (see {@link decorationsFor}). */
  readonly decorators: TreeNodeDecorator<CcFlatNode>[] = [this];

  /**
   * A change of structure that waits for the user to say whether it is made, with what it would break in the report.
   * It is only there while the panel asks.
   */
  decision: { proceed: () => void; note: string } | null = null;

  constructor(private host: CcTreeValidationHost,
              private service: ValidationService,
              private snackBar: MatSnackBar) {
    super();
  }

  /**
   * Shows what a change that has not been made would break, and asks whether to make it. A change that would break
   * nothing is not asked about: the answer is yes, and nothing is shown. Returns whether the user is asked.
   */
  askAboutChange(change: StructureChangeReport, proceed: () => void): boolean {
    if (change.report.violations.length === 0) {
      return false;
    }
    this.keepingDetailAtEnd(() => this.apply(change.report));
    this.decision = {
      proceed,
      note: 'Checked ' + change.checkedCount + (change.checkedCount === 1 ? ' ACC' : ' ACCs')
    };
    return true;
  }

  proceedWithChange() {
    const decision = this.decision;
    this.clear();
    decision?.proceed();
  }

  cancelChange() {
    this.clear();
  }

  /** What closing the panel does: it takes the question back (the change is not made), or just closes the results. */
  close() {
    if (this.decision) {
      this.cancelChange();
    } else {
      this.panelOpen = false;
    }
  }

  override clear(): void {
    super.clear();
    this.decision = null;
  }

  /** The room the open panel takes at the bottom of the page: the tree and the detail end above it. */
  get openPanelHeight(): number {
    return this.hasReport && this.panelOpen ? this.panelHeight : 0;
  }

  readonly marker = (node: CcFlatNode): ValidationMarker | undefined => {
    const severity = this.severityAt(node.path);
    return severity ? {severity, stale: this.stale, messages: this.messagesAt(node.path)} : undefined;
  };

  /**
   * What the tree shows of the validation on the row of a node: on the root, a button that opens a closed panel (while
   * there are problems and none of their nodes is on screen); on a node a problem is about, the icon of the most
   * serious one, dimmed when the page has changed since. Clicking either opens the panel.
   */
  decorationsFor(node: CcFlatNode): TreeNodeDecoration[] {
    const decorations: TreeNodeDecoration[] = [];
    if (node.level === 0 && this.badgeCount > 0) {
      const look = SEVERITY_ICON[this.errorCount > 0 ? 'ERROR' : 'WARNING'];
      decorations.push({id: 'cc-validation-show', ...look, tooltip: 'Show validation results', onClick: () => this.show(null)});
    }
    const marker = this.marker(node);
    if (marker) {
      decorations.push({
        cssClass: 'validation-marker', ...SEVERITY_ICON[marker.severity],
        tooltip: marker.messages.join('\n') + (marker.stale ? '\n(may be outdated)' : ''), tooltipClass: 'validation-tooltip',
        opacity: marker.stale ? 0.45 : 1, onClick: () => this.show(node)
      });
    }
    return decorations;
  }

  /**
   * What the root row of the tree says about a closed panel: the number of problems, but only while no node a
   * problem is about is on screen (a node that is on screen has a marker of its own).
   */
  get badgeCount(): number {
    if (!this.hasReport || this.panelOpen) {
      return 0;
    }
    const shown = new Set(this.host.dataSource.data.map(node => node.path));
    return this.addresses.some(address => shown.has(address)) ? 0 : this.report.violations.length;
  }

  /**
   * Runs a change that makes the detail pane shorter or taller (the panel opening, being resized). A pane that was
   * scrolled to its end stays at its end, so that what the user was reading is not pushed under the panel.
   */
  private keepingDetailAtEnd(change: () => void) {
    const pane: HTMLElement | null = this.host.element.querySelector('.right-panel');
    const atEnd = !!pane && pane.scrollTop + pane.clientHeight >= pane.scrollHeight - 2;
    change();
    if (atEnd) {
      setTimeout(() => pane.scrollTop = pane.scrollHeight);
    }
  }

  resizePanel(height: number) {
    this.keepingDetailAtEnd(() => this.panelHeight = height);
  }

  /** Opens the panel, pointing at the problems of the node whose marker was clicked, if any. */
  show(node: CcFlatNode | null) {
    this.keepingDetailAtEnd(() => {
      this.focusAddress = node ? node.path : null;
      this.panelOpen = true;
    });
  }

  /** Validates the object of the page on the user's request and shows what it found; a clean result only says so. */
  validate() {
    if (this.running) {
      return;
    }
    this.running = true;
    this.service.validate(this.host.type, this.host.manifestId, 'ON_DEMAND').subscribe({
      next: report => {
        this.keepingDetailAtEnd(() => this.apply(report));
        if (!this.hasReport) {
          this.snackBar.open('No issues found', '', {duration: 3000});
        }
      },
      error: err => {
        this.running = false;
        throw err;
      }
    });
  }

  navigateTo(location: ValidationLocation): boolean {
    const root = location.path[0];
    if (root && (root.type !== this.host.type || root.manifestId !== String(this.host.manifestId))) {
      // a problem of another object (the ones a change would affect): it is shown where that object is
      window.open(addressOf(root), '_blank');
      return true;
    }
    const dataSource = this.host.dataSource;
    const node = findNodeByLocation(dataSource, location);
    if (!node) {
      if (this.decision) {
        return false;
      }
      this.markStale();
      this.snackBar.open('That node is no longer in the tree — re-validate', '', {duration: 4000});
      return false;
    }
    if (node.parent) {
      dataSource.expandTo(node.parent as CcFlatNode);
    }
    this.host.onClick(node);
    this.host.scrollToNode(node, 100);
    return true;
  }
}

/** Where the page of an object is. */
function addressOf(segment: ValidationPathSegment): string {
  switch (segment.type) {
    case 'ASCCP':
      return '/core_component/asccp/' + segment.manifestId;
    case 'BCCP':
      return '/core_component/bccp/' + segment.manifestId;
    case 'DT':
      return '/data_type/' + segment.manifestId;
    default:
      return '/core_component/acc/' + segment.manifestId;
  }
}
