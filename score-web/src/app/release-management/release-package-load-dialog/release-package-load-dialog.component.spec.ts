import {TestBed} from '@angular/core/testing';
import {HttpErrorResponse, HttpEventType, HttpHeaders} from '@angular/common/http';
import {MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {of, Subject, throwError} from 'rxjs';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {PackageLoadJob, PackageLoadState, PackagePreflight, PackageUpload} from '../domain/release-package-load';
import {ReleasePackageLoadService} from '../domain/release-package-load.service';
import {errorMessage, ReleasePackageLoadDialogComponent} from './release-package-load-dialog.component';

function job(id: number, state: PackageLoadState, active = ['PENDING', 'PREFLIGHT', 'LOADING', 'MIGRATING', 'COPYING', 'VERIFYING', 'SWAPPING', 'ROLLING_BACK'].includes(state)): PackageLoadJob {
  return {id, state, active, message: null, packageFileName: '10_0.zip', releaseNum: '10.0', backupSchema: null, ownerUserId: 1,
    creationTimestamp: '2026-11-18T10:00:00', lastUpdateTimestamp: '2026-11-18T10:05:00'};
}

function upload(state: PackageUpload['state'], bytes = 0, total = 100): PackageUpload {
  return {uploadId: 'u-1', fileName: '10_0.zip', state, bytes, totalBytes: total, message: null,
    releaseNum: state === 'READY' ? '10.0' : null, version: state === 'READY' ? '1.2' : null};
}

const OK: PackagePreflight = {releaseNum: '10.0', version: '1.2', format: 'V1', ok: true, problems: []};

describe('ReleasePackageLoadDialogComponent', () => {
  let service: Record<string, ReturnType<typeof vi.fn>>;
  let confirmed: boolean;

  function create(recent: PackageLoadJob[] = []): ReleasePackageLoadDialogComponent {
    service = {
      settings: vi.fn(() => of({maxPackageSize: 1000, available: true, problem: null})),
      upload: vi.fn(),
      discardUpload: vi.fn(() => of(undefined)),
      preflight: vi.fn(() => of(OK)),
      start: vi.fn(() => of(job(7, 'PENDING'))),
      recent: vi.fn(() => of(recent)),
      get: vi.fn(),
      rollback: vi.fn(() => of(job(7, 'ROLLING_BACK'))),
    };
    confirmed = true;
    TestBed.configureTestingModule({
      providers: [
        {provide: ReleasePackageLoadService, useValue: service},
        {provide: MatDialogRef, useValue: {close: vi.fn()}},
        {provide: MatSnackBar, useValue: {open: vi.fn()}},
        {
          provide: ConfirmDialogService,
          useValue: {newConfig: () => ({data: {}}), open: () => ({afterClosed: () => of(confirmed)})}
        },
      ]
    });
    return TestBed.runInInjectionContext(() => new ReleasePackageLoadDialogComponent());
  }

  afterEach(() => {
    vi.useRealTimers();
    TestBed.resetTestingModule();
  });

  it('starts at the choice of a package, with what the server tells of loads and of the size a package may be', () => {
    const dialog = create();
    dialog.ngOnInit();

    expect(dialog.step()).toBe('package');
    expect(dialog.settings()?.maxPackageSize).toBe(1000);
  });

  it('is told that loads are not available when the database user may not make the schemas they need', () => {
    const dialog = create();
    service['settings'].mockReturnValue(of({maxPackageSize: 1000, available: false, problem: 'the database user cannot make a schema'}));
    dialog.ngOnInit();

    expect(dialog.settings()?.available).toBe(false);
    expect(dialog.settings()?.problem).toContain('cannot make a schema');
  });

  it('follows a load that goes on when it is opened, until it is over', () => {
    vi.useFakeTimers();
    const dialog = create([job(7, 'COPYING')]);
    service['get'].mockReturnValueOnce(of(job(7, 'COPYING'))).mockReturnValueOnce(of(job(7, 'SWAPPING'))).mockReturnValue(of(job(7, 'SUCCEEDED')));

    dialog.ngOnInit();
    expect(dialog.step()).toBe('load');
    expect(dialog.job()?.state).toBe('COPYING');

    vi.advanceTimersByTime(2000);
    expect(dialog.job()?.state).toBe('SWAPPING');
    vi.advanceTimersByTime(2000);
    expect(dialog.job()?.state).toBe('SUCCEEDED');
    const calls = service['get'].mock.calls.length;
    vi.advanceTimersByTime(10000);
    expect(service['get'].mock.calls.length).toBe(calls);
  });

  it('refuses a file that is not a zip or is too large, before anything is sent', () => {
    const dialog = create();
    dialog.ngOnInit();

    dialog.onFileChosen({target: {files: {item: () => new File(['x'], 'notes.txt')}, value: ''}} as unknown as Event);
    expect(dialog.error()).toContain('.zip');

    dialog.onFileChosen({target: {files: {item: () => new File([new Uint8Array(2000)], 'big.zip')}, value: ''}} as unknown as Event);
    expect(dialog.error()).toContain('larger');
    expect(service['upload']).not.toHaveBeenCalled();
  });

  it('shows how much of a package has gone, and looks at it when all has', () => {
    const dialog = create();
    dialog.ngOnInit();
    const events = new Subject<unknown>();
    service['upload'].mockReturnValue(events);

    dialog.onFileChosen({target: {files: {item: () => new File([new Uint8Array(100)], '10_0.zip')}, value: ''}} as unknown as Event);
    expect(dialog.sending()).toBe(true);
    events.next({type: HttpEventType.UploadProgress, loaded: 40, total: 100});
    expect(dialog.receivingPercent).toBe(40);

    events.next({type: HttpEventType.Response, body: upload('READY', 100)});

    expect(dialog.sending()).toBe(false);
    expect(dialog.step()).toBe('check');
    expect(service['preflight']).toHaveBeenCalledWith('u-1');
    expect(dialog.preflight()?.ok).toBe(true);
  });

  it('does not load a package that has something in its way, and loads one that has nothing', () => {
    const dialog = create();
    dialog.ngOnInit();
    dialog.upload.set(upload('READY', 100));
    service['preflight'].mockReturnValue(of({...OK, ok: false, problems: ['another load goes on']}));
    dialog.checkPackage();

    dialog.startLoad();
    expect(service['start']).not.toHaveBeenCalled();
    expect(dialog.preflight()?.problems).toEqual(['another load goes on']);

    service['preflight'].mockReturnValue(of(OK));
    service['get'].mockReturnValue(of(job(7, 'SUCCEEDED')));
    dialog.checkPackage();
    dialog.startLoad();

    expect(service['start']).toHaveBeenCalledWith('u-1');
    expect(dialog.step()).toBe('load');
    expect(dialog.job()?.state).toBe('SUCCEEDED');
    expect(dialog.upload()).toBeUndefined();
  });

  it('shows the refusal of the server, with the reason it gave', () => {
    const dialog = create();
    dialog.ngOnInit();
    dialog.upload.set(upload('READY', 100));
    dialog.preflight.set(OK);
    service['start'].mockReturnValue(throwError(() => new HttpErrorResponse({
      status: 409, headers: new HttpHeaders({'X-Error-Message': 'Another load goes on.'})
    })));

    dialog.startLoad();

    expect(dialog.error()).toBe('Another load goes on.');
    expect(dialog.starting()).toBe(false);
    expect(dialog.step()).not.toBe('load');
  });

  it('throws away a package that was brought when another is chosen', () => {
    const dialog = create();
    dialog.ngOnInit();
    dialog.upload.set(upload('READY', 100));
    dialog.step.set('check');

    dialog.chooseAnother();

    expect(service['discardUpload']).toHaveBeenCalledWith('u-1');
    expect(dialog.step()).toBe('package');
    expect(dialog.upload()).toBeUndefined();
  });

  it('offers the undo for the latest load that succeeded', () => {
    const dialog = create([job(9, 'FAILED'), job(8, 'SUCCEEDED'), job(7, 'SUCCEEDED')]);
    dialog.ngOnInit();

    expect(dialog.undoable?.id).toBe(8);
  });

  it('does not offer the undo while a load goes on', () => {
    const dialog = create([job(9, 'LOADING'), job(8, 'SUCCEEDED')]);
    service['get'].mockReturnValue(of(job(9, 'LOADING')));
    dialog.ngOnInit();

    expect(dialog.undoable).toBeUndefined();
  });

  it('undoes the swap when it is confirmed, and follows it', () => {
    const dialog = create([job(7, 'SUCCEEDED')]);
    service['get'].mockReturnValue(of(job(7, 'ROLLED_BACK')));
    dialog.ngOnInit();

    dialog.rollback(job(7, 'SUCCEEDED'));

    expect(service['rollback']).toHaveBeenCalledWith(7);
    expect(dialog.job()?.state).toBe('ROLLED_BACK');
    expect(dialog.isUndo(dialog.job()!)).toBe(true);
  });

  it('does not undo what is not confirmed', () => {
    const dialog = create([job(7, 'SUCCEEDED')]);
    dialog.ngOnInit();
    confirmed = false;

    dialog.rollback(job(7, 'SUCCEEDED'));

    expect(service['rollback']).not.toHaveBeenCalled();
  });

  it('places a load at its step, past the last one when it has succeeded', () => {
    const dialog = create();
    expect(dialog.stepIndex(job(1, 'PREFLIGHT'))).toBe(0);
    expect(dialog.stepIndex(job(1, 'COPYING'))).toBe(3);
    expect(dialog.stepIndex(job(1, 'SUCCEEDED'))).toBe(6);
  });

  describe('errorMessage', () => {
    it('prefers the header of the server, then its text, then the error', () => {
      expect(errorMessage(new HttpErrorResponse({status: 409, headers: new HttpHeaders({'X-Error-Message': 'header'}), error: 'text'}))).toBe('header');
      expect(errorMessage(new HttpErrorResponse({status: 409, error: 'text'}))).toBe('text');
      expect(errorMessage(new Error('plain'))).toBe('plain');
    });
  });
});
