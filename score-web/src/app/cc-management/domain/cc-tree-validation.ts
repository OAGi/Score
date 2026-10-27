import {MatSnackBar} from '@angular/material/snack-bar';
import {ValidationLocation, ValidationSession} from '../../validation/validation';
import {ValidationService} from '../../validation/validation.service';
import {NodeNavigator} from '../../validation/validation-navigator';
import {ValidationMarker} from '../cc-tree-viewport/cc-tree-viewport.component';
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

/**
 * What a page with a tree of core components knows about its last validation, and what it does with it: asks for it,
 * marks the nodes the problems are about and takes the user to them. Nothing of it is saved anywhere.
 */
export class CcTreeValidation extends ValidationSession implements NodeNavigator {

  panelHeight = 240;

  constructor(private host: CcTreeValidationHost,
              private service: ValidationService,
              private snackBar: MatSnackBar) {
    super();
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
    const dataSource = this.host.dataSource;
    const node = findNodeByLocation(dataSource, location);
    if (!node) {
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
