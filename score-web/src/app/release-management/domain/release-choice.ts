import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {AuthService} from '../../authentication/auth.service';
import {initFilter, loadBranch, saveBranch} from '../../common/utility';
import {ReleaseSummary} from './release';

/**
 * The branch (release) that a list of a library shows: the one in the URL, or the one the user last chose (remembered per user),
 * or else the first. `filterCtrl`/`filtered` are for the search box of the branch selector.
 */
export class ReleaseChoice {
  releases: ReleaseSummary[] = [];
  release: ReleaseSummary = new ReleaseSummary();
  readonly filterCtrl = new FormControl();
  readonly filtered = new ReplaySubject<ReleaseSummary[]>(1);

  /** `cookieType` is what the remembered choice is kept under. */
  constructor(private readonly auth: AuthService, private readonly cookieType = 'CC') {
  }

  /**
   * `requestedReleaseId` is the one asked for (the URL); pass it for the first time only (0 for none). Later (another library)
   * the choice so far is looked for among the new releases.
   */
  init(releases: ReleaseSummary[], requestedReleaseId?: number) {
    if (requestedReleaseId !== undefined) {
      this.release = Object.assign(new ReleaseSummary(), {releaseId: requestedReleaseId});
    }
    this.releases = [...releases];
    if (this.releases.length > 0) {
      if (this.release?.releaseId === 0) {
        const savedReleaseId = loadBranch(this.auth.getUserToken(), this.cookieType);
        if (savedReleaseId) {
          this.release = this.releases.filter(e => e.releaseId === savedReleaseId)[0];
          if (!this.release) {
            this.release = this.releases[0];
            saveBranch(this.auth.getUserToken(), this.cookieType, this.release?.releaseId);
          }
        }
      } else {
        this.release = this.releases.filter(e => e.releaseId === this.release?.releaseId)[0];
      }
    }
    if (!this.release || this.release?.releaseId === 0) {
      this.release = this.releases[0];
    }
    initFilter(this.filterCtrl, this.filtered, this.releases, (e) => e.releaseNum);
  }

  /** The user chose another one. */
  choose(release: ReleaseSummary) {
    this.release = release;
    saveBranch(this.auth.getUserToken(), this.cookieType, release.releaseId);
  }
}
