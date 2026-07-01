/**
 * Domain model for the Business Term import dialog: parsing, EDP column-mapping, the editable
 * preview rows, and the batch request/response contract with the backend.
 */

// Field limits, mirroring the backend BusinessTermInputValidator and the business_term columns.
export const BUSINESS_TERM_NAME_MAX_LENGTH = 255;
export const EXTERNAL_REFERENCE_URI_MAX_LENGTH = 65535;
export const EXTERNAL_REFERENCE_ID_MAX_LENGTH = 100;

// Client-side guard for the source file. UX-only — the server's row-count cap is authoritative.
export const MAX_UPLOAD_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB

/** connectCenter business-term fields a source column can map to. URI is handled separately. */
export type TargetField = 'businessTerm' | 'externalReferenceId' | 'definition' | 'comment';

export type UriMode = 'COLUMN' | 'SYNTHESIZE';

/** A recognized source format, surfaced to the user as an auto-detect hint. */
export type DetectedFormat = 'NATIVE' | 'COLLIBRA' | 'ALATION' | 'PURVIEW' | 'GENERIC' | 'UNKNOWN';

/** Result of parsing an uploaded CSV/TSV/XLSX file. */
export interface ParsedFile {
  headers: string[];
  rows: Record<string, string>[];
  /** Non-empty only for Excel workbooks (lets the user pick a sheet). */
  sheetNames: string[];
  /** The worksheet actually parsed (Excel only; null for CSV/TSV). */
  selectedSheet?: string | null;
}

/** How the user maps the source columns onto connectCenter fields. */
export interface ColumnMapping {
  /** target field -> source header name, or null when unmapped. */
  fieldToColumn: Record<TargetField, string | null>;
  uriMode: UriMode;
  /** Source header holding the URI, when uriMode === 'COLUMN'. */
  uriColumn: string | null;
  /** Base URL to prefix, when uriMode === 'SYNTHESIZE'. */
  uriBase: string;
  /** Source header holding the id to append, when uriMode === 'SYNTHESIZE'. */
  uriIdColumn: string | null;
}

/** One editable, selectable preview row. */
export interface ImportRow {
  /** Stable, monotonic id used as the table trackBy (avoids NG0955 duplicate-key errors). */
  id: number;
  /** 1-based source row position, echoed back from the server result. */
  rowIndex: number;
  businessTerm: string;
  externalReferenceUri: string;
  externalReferenceId: string;
  definition: string;
  comment: string;
  /** Validation messages (server-parity); empty means the row is importable. */
  errors: string[];
  /** True once the user manually unchecks an otherwise-valid row (so re-validation won't re-check it). */
  userDeselected: boolean;
}

export interface BusinessTermImportRowRequest {
  rowIndex: number;
  businessTerm: string;
  externalReferenceId: string;
  externalReferenceUri: string;
  definition: string;
  comment: string;
}

export interface BatchImportRowResult {
  rowIndex: number;
  businessTerm: string;
  externalReferenceUri: string;
  outcome: 'CREATED' | 'UPDATED' | 'FAILED';
  message?: string;
}

export interface BatchImportResult {
  createdCount: number;
  updatedCount: number;
  failedCount: number;
  results: BatchImportRowResult[];
}
