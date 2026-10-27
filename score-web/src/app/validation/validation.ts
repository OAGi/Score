export type ValidationSeverity = 'ERROR' | 'WARNING' | 'INFO';

export type ValidationTrigger = 'ON_DEMAND' | 'STATE_TRANSITION' | 'RELEASE' | 'STRUCTURE_CHANGE';

/** One step of the address of a node: its kind (ACC, ASCC, ASCCP, BCC, BCCP, ...) and manifest id. */
export interface ValidationPathSegment {
  type: string;
  manifestId: string;
}

/** A node a violation is about; the path is the address the tree of the screen uses, root first. */
export interface ValidationLocation {
  label: string;
  path: ValidationPathSegment[];
  role?: string;
}

/** The subject a violation is about when it is not the one the report is about (a report of what a change would affect). */
export interface ViolationSubject {
  type: string;
  id: string;
  label: string;
}

export interface Violation {
  ruleId: string;
  severity: ValidationSeverity;
  message: string;
  params: { [key: string]: string };
  locations: ValidationLocation[];
  subject?: ViolationSubject | null;
}

export interface ValidationReport {
  subject: { type: string; id: string };
  validatedAt: string;
  status: 'VALID' | 'HAS_WARNINGS' | 'HAS_ERRORS';
  violations: Violation[];
}

/** What a change of structure would break, and how many ACCs were checked to find out. */
export interface StructureChangeReport {
  report: ValidationReport;
  checkedCount: number;
}

const SEVERITY_RANK: { [severity: string]: number } = {ERROR: 0, WARNING: 1, INFO: 2};

/** The address of a node the way the tree of the screen writes it: <code>ACC-1>ASCC-2>ASCCP-3</code>. */
export function locationAddress(location: ValidationLocation): string {
  return location.path.map(segment => segment.type + '-' + segment.manifestId).join('>');
}

export function countOf(report: ValidationReport | null, severity: ValidationSeverity): number {
  return report ? report.violations.filter(v => v.severity === severity).length : 0;
}

/**
 * What a page knows about the validation it ran: the last report, and whether the page has changed since. It lives in
 * the page only; nothing of it is saved, so a page that never validated, or whose last validation found nothing, shows
 * nothing.
 */
export class ValidationSession {

  report: ValidationReport | null = null;
  /** The page has changed since the report was made, so it may no longer be true. */
  stale = false;
  running = false;
  panelOpen = false;
  /** The address of the node whose marker was clicked; the panel points at the violations about it. */
  focusAddress: string | null = null;

  private severityByAddress = new Map<string, ValidationSeverity>();
  private messagesByAddress = new Map<string, string[]>();

  /** Keeps a report that found something and opens the panel; a clean report leaves nothing behind. */
  apply(report: ValidationReport): void {
    this.running = false;
    this.stale = false;
    if (report.violations.length === 0) {
      this.clear();
      return;
    }
    this.report = report;
    this.panelOpen = true;
    this.severityByAddress = new Map();
    this.messagesByAddress = new Map();
    for (const violation of report.violations) {
      for (const location of violation.locations) {
        const address = locationAddress(location);
        const messages = this.messagesByAddress.get(address) ?? [];
        if (!messages.includes(violation.message)) {
          messages.push(violation.message);
        }
        this.messagesByAddress.set(address, messages);
        const known = this.severityByAddress.get(address);
        if (!known || SEVERITY_RANK[violation.severity] < SEVERITY_RANK[known]) {
          this.severityByAddress.set(address, violation.severity);
        }
      }
    }
  }

  clear(): void {
    this.report = null;
    this.stale = false;
    this.panelOpen = false;
    this.severityByAddress = new Map();
    this.messagesByAddress = new Map();
    this.focusAddress = null;
  }

  /** The page changed: a report that is there is kept, but marked as possibly outdated. */
  markStale(): void {
    if (this.report) {
      this.stale = true;
    }
  }

  /** The most serious severity of the violations about the node at this address, if any. */
  severityAt(address: string): ValidationSeverity | undefined {
    return this.report ? this.severityByAddress.get(address) : undefined;
  }

  /** The addresses of the nodes any violation is about. */
  get addresses(): string[] {
    return Array.from(this.severityByAddress.keys());
  }

  /** The messages of the violations about the node at this address. */
  messagesAt(address: string): string[] {
    return this.report ? (this.messagesByAddress.get(address) ?? []) : [];
  }

  get hasReport(): boolean {
    return !!this.report;
  }

  get errorCount(): number {
    return countOf(this.report, 'ERROR');
  }
}
