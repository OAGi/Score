import {ChangeDetectionStrategy, Component, DestroyRef, ElementRef, OnInit, computed, inject, signal, viewChild} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {HttpErrorResponse, HttpEventType} from '@angular/common/http';
import {MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {EMPTY, Observable, Subject, interval, of, timer} from 'rxjs';
import {catchError, expand, filter, map, switchMap} from 'rxjs/operators';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {
  PackageLoadJob,
  PackageLoadLogLine,
  PackageLoadLogPage,
  PackageLibraryRelease,
  PackageLoadSettings,
  PackagePreflight,
  PackageUpload,
  formatBytes,
  loadProgress
} from '../domain/release-package-load';
import {ReleasePackageLoadService} from '../domain/release-package-load.service';

/** Where the dialog is: the package comes, it is looked at, it is loaded. */
const DOTS_MS = 400;

type Step = 'package' | 'check' | 'load';

const POLL_LOAD_MS = 2000;

/** The message the server gave for a refusal, else what the browser knows of it. */
export function errorMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    const header = err.headers?.get('X-Error-Message');
    if (header) {
      return header;
    }
    if (typeof err.error === 'string' && err.error) {
      return err.error;
    }
    return err.message;
  }
  return err instanceof Error ? err.message : String(err);
}

/**
 * Loads a release package, which an administrator does from the list of releases: the package is sent from the computer; it is looked
 * at (nothing is changed by that) and what is in its way is told; then the load goes on in the
 * background and the dialog follows it, state by state, until it is over. The dialog can be closed while the load goes on: opening it
 * again shows where the load is.
 */
@Component({
  standalone: false,
  selector: 'score-release-package-load-dialog',
  templateUrl: './release-package-load-dialog.component.html',
  styleUrls: ['./release-package-load-dialog.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager,
})
export class ReleasePackageLoadDialogComponent implements OnInit {
  private service = inject(ReleasePackageLoadService);
  private dialogRef = inject(MatDialogRef<ReleasePackageLoadDialogComponent>);
  private snackBar = inject(MatSnackBar);
  private confirmDialogService = inject(ConfirmDialogService);
  private destroyRef = inject(DestroyRef);

  readonly loadProgress = loadProgress;
  readonly formatBytes = formatBytes;

  /** Tells, once, that a load or an undo that this dialog followed is over and has changed the releases: the list behind is to be loaded again. */
  readonly changed = new Subject<void>();

  readonly step = signal<Step>('package');
  readonly settings = signal<PackageLoadSettings | undefined>(undefined);
  readonly recent = signal<PackageLoadJob[]>([]);

  readonly dragOver = signal(false);
  /** While the package is sent to the server, which shows the share that has gone. */
  readonly sending = signal(false);
  readonly upload = signal<PackageUpload | undefined>(undefined);
  readonly checking = signal(false);
  readonly starting = signal(false);
  readonly preflight = signal<PackagePreflight | undefined>(undefined);
  /** The libraries of the package, the ones that need a look first: those the package is behind in, then those it changes. */
  readonly libraries = computed(() => {
    const rank = (library: PackageLibraryRelease) => library.behind ? 0 : library.packageRelease !== library.currentRelease ? 1 : 2;
    return [...(this.preflight()?.libraries ?? [])].sort((a, b) => rank(a) - rank(b));
  });
  readonly job = signal<PackageLoadJob | undefined>(undefined);
  /** What the server logged for the load that is followed, which the user may open to read. */
  readonly logLines = signal<PackageLoadLogLine[]>([]);
  readonly lastLogLine = computed(() => this.logLines().at(-1));
  private logNext = 0;
  private readonly logBox = viewChild<ElementRef<HTMLElement>>('logBox');
  readonly error = signal<string | undefined>(undefined);

  /** The dots after the message of a load that goes on, which come and go (none, ., .., ...) to show that it does. */
  readonly dots = signal('');

  ngOnInit() {
    interval(DOTS_MS).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(tick => this.dots.set('.'.repeat((tick + 1) % 4)));
    this.service.settings().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: settings => this.settings.set(settings),
      error: err => this.error.set(errorMessage(err))
    });
    this.refreshRecent(true);
  }

  // -------------------------------------------------------------------------------------------------------------
  // Where the dialog starts, and the loads that were
  // -------------------------------------------------------------------------------------------------------------

  /** The loads that were started last; a load that goes on is followed at once. */
  private refreshRecent(follow: boolean) {
    this.service.recent(5).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: jobs => {
        this.recent.set(jobs);
        const active = jobs.find(job => job.active);
        if (follow && active) {
          this.follow(active);
        }
      },
      error: err => this.error.set(errorMessage(err))
    });
  }

  /** The latest load that succeeded, which is the one whose swap can be undone. */
  get undoable(): PackageLoadJob | undefined {
    const latestSucceeded = this.recent().find(job => job.state === 'SUCCEEDED');
    return latestSucceeded && this.recent().every(job => !job.active) ? latestSucceeded : undefined;
  }

  // -------------------------------------------------------------------------------------------------------------
  // Step 1: the package comes
  // -------------------------------------------------------------------------------------------------------------

  onFileChosen(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.item(0);
    input.value = '';
    if (file) {
      this.send(file);
    }
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    this.dragOver.set(false);
    const file = event.dataTransfer?.files?.item(0);
    if (file) {
      this.send(file);
    }
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    this.dragOver.set(true);
  }

  private send(file: File) {
    this.error.set(undefined);
    const max = this.settings()?.maxPackageSize;
    if (max && file.size > max) {
      this.error.set('The file is larger than the ' + formatBytes(max) + ' that a package may be.');
      return;
    }
    if (!file.name.toLowerCase().endsWith('.zip')) {
      this.error.set('A release package is a .zip file.');
      return;
    }
    this.sending.set(true);
    this.upload.set({uploadId: '', fileName: file.name, state: 'RECEIVING', bytes: 0, totalBytes: file.size,
      message: null, releaseNum: null, version: null});
    this.service.upload(file).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: event => {
        if (event.type === HttpEventType.UploadProgress) {
          this.upload.update(upload => upload && {...upload, bytes: event.loaded, totalBytes: event.total ?? file.size});
        } else if (event.type === HttpEventType.Response && event.body) {
          this.received(event.body);
        }
      },
      error: err => this.failedToReceive(err)
    });
  }

  private received(upload: PackageUpload) {
    this.upload.set(upload);
    this.sending.set(false);
    this.checkPackage();
  }

  private failedToReceive(err: unknown) {
    this.sending.set(false);
    this.upload.set(undefined);
    this.error.set(errorMessage(err));
  }

  // -------------------------------------------------------------------------------------------------------------
  // Step 2: what is in the way
  // -------------------------------------------------------------------------------------------------------------

  checkPackage() {
    const upload = this.upload();
    if (!upload?.uploadId) {
      return;
    }
    this.error.set(undefined);
    this.step.set('check');
    this.preflight.set(undefined);
    this.checking.set(true);
    this.service.preflight(upload.uploadId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: report => {
        this.preflight.set(report);
        this.checking.set(false);
      },
      error: err => {
        this.checking.set(false);
        this.error.set(errorMessage(err));
      }
    });
  }

  /** Back to the choice of a package: the one that came is thrown away. */
  chooseAnother() {
    const upload = this.upload();
    if (upload?.uploadId) {
      this.service.discardUpload(upload.uploadId).subscribe({error: () => undefined});
    }
    this.upload.set(undefined);
    this.preflight.set(undefined);
    this.error.set(undefined);
    this.step.set('package');
  }

  // -------------------------------------------------------------------------------------------------------------
  // Step 3: the load
  // -------------------------------------------------------------------------------------------------------------

  startLoad() {
    const upload = this.upload();
    if (!upload?.uploadId || !this.preflight()?.ok) {
      return;
    }
    this.error.set(undefined);
    this.starting.set(true);
    this.service.start(upload.uploadId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: job => {
        this.starting.set(false);
        this.upload.set(undefined);
        this.follow(job);
      },
      error: err => {
        this.starting.set(false);
        this.error.set(errorMessage(err));
      }
    });
  }

  /** Follows a load, by asking how it goes (and what the server logged) every few seconds until it is over. */
  follow(job: PackageLoadJob) {
    this.step.set('load');
    this.job.set(job);
    this.error.set(undefined);
    this.logLines.set([]);
    this.logNext = 0;
    let wasActive = job.active;
    const poll = (): Observable<{job: PackageLoadJob; page: PackageLoadLogPage | null}> => this.service.get(job.id).pipe(
      switchMap(current => this.service.log(job.id, this.logNext).pipe(
        map(page => ({job: current, page})),
        catchError(() => of({job: current, page: null}))
      ))
    );
    poll().pipe(
      expand(current => current.job.active ? timer(POLL_LOAD_MS).pipe(switchMap(() => poll())) : EMPTY),
      filter(current => !!current),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: ({job: current, page}) => {
        this.job.set(current);
        if (page) {
          this.appendLog(page);
        }
        if (!current.active) {
          this.refreshRecent(false);
          if (wasActive && (current.state === 'SUCCEEDED' || current.state === 'ROLLED_BACK')) {
            this.changed.next();
          }
        }
        wasActive = current.active;
      },
      error: err => this.error.set(errorMessage(err))
    });
  }

  private appendLog(page: PackageLoadLogPage) {
    if (page.lines.length === 0) {
      return;
    }
    const box = this.logBox()?.nativeElement;
    const atBottom = !box || box.scrollHeight - box.scrollTop - box.clientHeight < 24;
    this.logNext = page.next;
    this.logLines.update(lines => [...lines, ...page.lines]);
    if (atBottom) {
      setTimeout(() => {
        const element = this.logBox()?.nativeElement;
        if (element) {
          element.scrollTop = element.scrollHeight;
        }
      });
    }
  }

  isUndo(job: PackageLoadJob): boolean {
    return job.state === 'ROLLING_BACK' || job.state === 'ROLLED_BACK';
  }

  rollback(job: PackageLoadJob) {
    const config = this.confirmDialogService.newConfig();
    config.data.header = 'Undo the update from ' + (job.releaseNum ?? job.packageFileName) + '?';
    config.data.content = ['The data from before the update is put back. Anything that users saved since the update will be lost.'];
    config.data.action = 'Undo';
    this.confirmDialogService.open(config).afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      this.error.set(undefined);
      this.service.rollback(job.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: started => this.follow(started),
        error: err => this.error.set(errorMessage(err))
      });
    });
  }

  reload() {
    window.location.reload();
  }

  backToStart() {
    this.job.set(undefined);
    this.step.set('package');
    this.refreshRecent(false);
  }

  close() {
    this.dialogRef.close();
  }

  get receivingPercent(): number | null {
    const upload = this.upload();
    return upload && upload.totalBytes > 0 ? Math.min(100, Math.round(upload.bytes * 100 / upload.totalBytes)) : null;
  }
}
