/** A release package that is on the server and is not loaded yet. */
export interface PackageUpload {
  uploadId: string;
  fileName: string;
  /** RECEIVING while the bytes come, READY when it can be looked at and loaded, FAILED when it did not come. */
  state: 'RECEIVING' | 'READY' | 'FAILED';
  bytes: number;
  /** -1 when it is not known how many there are. */
  totalBytes: number;
  message: string | null;
  releaseNum: string | null;
  version: string | null;
}

/** What is in the way of loading a package, which is found without anything being changed. */
export interface PackagePreflight {
  releaseNum: string;
  version: string;
  format: string | null;
  ok: boolean;
  problems: string[];
  /** The latest release of each library, in the package and in this database; null where there is none. */
  libraries: PackageLibraryRelease[];
}

export interface PackageLibraryRelease {
  name: string;
  packageRelease: string | null;
  currentRelease: string | null;
  /** Whether the package is behind this database in the library, which refuses it. */
  behind: boolean;
}

export type PackageLoadState =
  'PENDING' | 'PREFLIGHT' | 'LOADING' | 'MIGRATING' | 'COPYING' | 'VERIFYING' | 'SWAPPING' | 'ROLLING_BACK'
  | 'SUCCEEDED' | 'FAILED' | 'ROLLED_BACK';

/** A load of a package. */
export interface PackageLoadJob {
  id: number;
  state: PackageLoadState;
  /** Whether the load goes on. */
  active: boolean;
  message: string | null;
  packageFileName: string;
  releaseNum: string | null;
  backupSchema: string | null;
  ownerUserId: number;
  creationTimestamp: string;
  lastUpdateTimestamp: string;
}

/** One message that the server logged for a load. */
export interface PackageLoadLogLine {
  /** Counts the messages of the load from 1. */
  number: number;
  time: string;
  level: string;
  message: string;
}

/** The messages logged after the number that was asked after, and the number to ask after next. */
export interface PackageLoadLogPage {
  lines: PackageLoadLogLine[];
  next: number;
}

export interface PackageLoadSettings {
  maxPackageSize: number;
  /** False when the database user of the application may not make the schemas a load goes through. */
  available: boolean;
  /** Why loads are not available. */
  problem: string | null;
}

/** The steps of a load that goes on, in the order it goes through them. */
export const LOAD_STEPS: { state: PackageLoadState; label: string }[] = [
  {state: 'PREFLIGHT', label: 'Checking the package'},
  {state: 'LOADING', label: 'Loading the contents of the package'},
  {state: 'MIGRATING', label: 'Updating the data to this version of the application'},
  {state: 'COPYING', label: 'Copying the work of the users (saving is paused briefly)'},
  {state: 'VERIFYING', label: 'Checking that the loaded data is complete'},
  {state: 'SWAPPING', label: 'Putting the loaded data in place'},
];

/** How far a load is, as a share of its steps (0 to 100); a load that has ended is all of it. */
export function loadProgress(state: PackageLoadState): number {
  if (state === 'SUCCEEDED') {
    return 100;
  }
  const index = LOAD_STEPS.findIndex(step => step.state === state);
  return index < 0 ? 0 : Math.round(index * 100 / LOAD_STEPS.length);
}

export function formatBytes(bytes: number): string {
  if (bytes < 0) {
    return '';
  }
  if (bytes < 1024 * 1024) {
    return (bytes / 1024).toFixed(0) + ' KB';
  }
  if (bytes < 1024 * 1024 * 1024) {
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }
  return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
}
