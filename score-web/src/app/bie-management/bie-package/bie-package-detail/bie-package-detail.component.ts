import {ChangeDetectionStrategy, Component, HostListener, inject, OnInit, signal, viewChild} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {hashCode, saveAsBlobResponse} from '../../../common/utility';
import {SelectionModel} from '@angular/cdk/collections';
import {catchError, map, tap} from 'rxjs/operators';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {AuthService} from '../../../authentication/auth.service';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {WebPageInfoService} from '../../../basis/basis.service';
import {BiePackageDetails} from '../domain/bie-package';
import {BieListEntry} from '../../bie-list/domain/bie-list';
import {BiePackageService} from '../domain/bie-package.service';
import {BiePackageBieDialogComponent} from '../bie-package-add-bie-dialog/bie-package-bie-dialog.component';
import {BIE_TABLE, bieLinkQuery} from '../../bie-list/domain/bie.table';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../../common/score-table';
import {forkJoin, of} from 'rxjs';
import {BieExpressOption} from '../../bie-express/domain/generate-expression';
import {Title} from '@angular/platform-browser';
import {setAppTitleIfPresent} from '../../../common/app-title.strategy';

/** The BIEs of a package: the column settings are those of every list of BIEs; there is no search. */
const BIE_PACKAGE_BIE_TABLE = defineTable({
  ...BIE_TABLE,
  search: undefined,
});

@Component({
  standalone: false,
  selector: 'score-bie-package-detail',
  templateUrl: './bie-package-detail.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./bie-package-detail.component.css']
})
export class BiePackageDetailComponent implements OnInit {
  private biePackageService = inject(BiePackageService);
  private auth = inject(AuthService);
  private location = inject(Location);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);
  private confirmDialogService = inject(ConfirmDialogService);
  private dialog = inject(MatDialog);
  private titleService = inject(Title);
  webPageInfo = inject(WebPageInfoService);


  title = 'Edit BIE Package';
  biePackage: BiePackageDetails = new BiePackageDetails();
  hashCode;
  disabled: boolean;
  option: BieExpressOption;
  // Issue #1733: BIE Package manifest version. '0.2' is the stable default; '0.3' is a draft that also
  // emits the per-BIE backward compatibility indicator and the package-level revision reason.
  manifestVersion = '0.2';

  get manifestVersionLabel(): string {
    return this.manifestVersion === '0.3' ? '0.3 (draft)' : '0.2';
  }

  readonly columns = createTableColumns(BIE_PACKAGE_BIE_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<BieListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<BieListEntry>(true, [], true, (a, b) => a.topLevelAsbiepId === b.topLevelAsbiepId);
  readonly rowKey = (row: BieListEntry) => row;
  businessContextSelection = {};
  readonly rowClass = (row: BieListEntry) => ({'highlight': row.status === 'Duplication'});
  biePackageId: number;
  /** The list waits for the package. */
  readonly ready = signal(false);
  /** The BIEs of the page that the table shows. */
  rows: BieListEntry[] = [];
  /** The search part of a link to the list that shows the BIE (a `q=` of the current query, with what the link says). */
  linkQuery(extras: { [key: string]: any }): string {
    return bieLinkQuery(BIE_PACKAGE_BIE_TABLE, this.view().query, extras);
  }

  readonly load = (params: HttpParams) => this.biePackageService.getBieListInBiePackagePage(this.biePackageId, params).pipe(
    tap({
      next: resp => {
        this.rows = resp.list;
        this.businessContextSelection = {};
        resp.list.forEach(elm => {
          this.businessContextSelection[elm.topLevelAsbiepId] = elm.businessContextList[0];
        });
      },
      error: () => {
        this.rows = [];
        this.businessContextSelection = {};
      }
    })
  );
  loading = false;

  ngOnInit(): void {
    this.option = new BieExpressOption();
    this.option.bieDefinition = true;
    this.option.expressionOption = 'XML';
    this.option.expressionVersion = '';
    this.option.arrayForJsonExpression = false;
    this.option.packageOption = 'EACH';
    this.option.separateFileReferencesForReusedSchemas = true;
    this.option.includeBusinessContextInFilename = true;
    this.option.includeVersionInFilename = true;

    this.biePackageId = Number(this.route.snapshot.params.id);

    this.biePackageService.get(this.biePackageId).subscribe(biePackage => {
      if (!biePackage) {
        this.redirectToBiePackageList();
        return;
      }

      this.init(biePackage);
      this.ready.set(true);
    }, err => {
      this.loading = false;
      if (err.status === 404) {
        this.redirectToBiePackageList();
        return;
      }

      const errorMessage = (err.status === 403) ?
        'You do not have access permission.' : 'Something\'s wrong.';
      this.snackBar.open(errorMessage, '', {
        duration: 3000
      });
      this.router.navigateByUrl('/bie_package');
    });
  }

  private redirectToBiePackageList() {
    this.loading = false;
    this.snackBar.open('The requested BIE package is unavailable.', '', {
      duration: 3000
    });
    this.router.navigateByUrl('/bie_package');
  }

  get isAdmin(): boolean {
    return this.auth.isAdmin();
  }

  init(biePackage: BiePackageDetails) {
    this.hashCode = hashCode(biePackage);
    this.biePackage = biePackage;
    setAppTitleIfPresent(this.titleService, this.biePackage.name, 'BIE Package');
    this.loading = false;
  }

  getPath(commands?: any[]): string {
    const urlTree = this.router.createUrlTree(commands);
    const path = this.location.prepareExternalUrl(urlTree.toString());
    return window.location.origin + path;
  }

  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent($event: KeyboardEvent) {
    const charCode = $event.key?.toLowerCase();

    // Handle 'Ctrl/Command+S'
    const metaOrCtrlKeyPressed = $event.metaKey || $event.ctrlKey;
    if (metaOrCtrlKeyPressed && charCode === 's') {
      $event.preventDefault();
      $event.stopPropagation();

      this.update();
    }
  }

  openAddDialog($event: any) {
    $event.preventDefault();
    $event.stopPropagation();

    const dialogConfig = new MatDialogConfig();

    dialogConfig.data = this.rows;
    dialogConfig.data.action = 'Add';
    dialogConfig.data.biePackage = this.biePackage;
    dialogConfig.data.webPageInfo = this.webPageInfo;
    dialogConfig.width = '100%';
    dialogConfig.maxWidth = '100%';
    dialogConfig.height = '100%';
    dialogConfig.maxHeight = '100%';
    dialogConfig.autoFocus = false;

    this.loading = true;
    const dialogRef = this.dialog.open(BiePackageBieDialogComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(topLevelAsbiepIdList => {
      if (!topLevelAsbiepIdList) {
        this.loading = false;
        return;
      }

      this.biePackageService.addBieToBiePackage(this.biePackage.biePackageId, ...topLevelAsbiepIdList).subscribe(_ => {
        this.snackBar.open('Added', '', {
          duration: 3000,
        });

        this.selection.clear();
        this.view().reload();

        this.loading = false;
      }, err => {
        this.loading = false;
        throw err;
      });
    }, err => {
      this.loading = false;
      throw err;
    });
  }

  openReplaceDialog($event: any, selected: BieListEntry) {
    $event.preventDefault();
    $event.stopPropagation();

    const dialogConfig = new MatDialogConfig();

    dialogConfig.data = this.rows;
    dialogConfig.data.action = 'Replace';
    dialogConfig.data.den = selected.den;
    dialogConfig.data.excludeTopLevelAsbiepIds = [selected.topLevelAsbiepId];
    dialogConfig.data.biePackage = this.biePackage;
    dialogConfig.data.webPageInfo = this.webPageInfo;
    dialogConfig.width = '100%';
    dialogConfig.maxWidth = '100%';
    dialogConfig.height = '100%';
    dialogConfig.maxHeight = '100%';
    dialogConfig.autoFocus = false;

    this.loading = true;
    const dialogRef = this.dialog.open(BiePackageBieDialogComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(topLevelAsbiepIdList => {
      if (!topLevelAsbiepIdList) {
        this.loading = false;
        return;
      }

      this.biePackageService.replaceBieInBiePackage(this.biePackage.biePackageId,
          selected.topLevelAsbiepId, topLevelAsbiepIdList[0]).subscribe(_ => {
        this.snackBar.open('Replaced', '', {
          duration: 3000,
        });

        this.selection.clear();
        this.view().reload();

        this.loading = false;
      }, err => {
        this.loading = false;
        throw err;
      });
    }, err => {
      this.loading = false;
      throw err;
    });
  }

  removeBieInBiePackage() {
    const bieLists = this.selection.selected;

    const removeBieAction = () => {
      this.biePackageService.deleteBieInBiePackage(
          this.biePackage.biePackageId, ...bieLists.map(e => e.topLevelAsbiepId)).subscribe(_ => {
        this.snackBar.open('Removed', '', {
          duration: 3000,
        });

        this.selection.clear();
        this.view().reload();
      });
    };

    if (!!this.biePackage.prev) {

      // Create an array of observables for each BIE
      const observables = bieLists.map(bie =>
          this.biePackageService.exists(this.biePackage.prev.biePackageId, bie.topLevelAsbiepId).pipe(
              map(_ => bie), // if exists, keep bie; else null
              catchError(() => of(null))          // handle errors gracefully
          )
      );

      // Run all observables in parallel
      forkJoin(observables).subscribe(results => {
        // Filter out nulls (non-existing BIEs)
        const exists = results.filter(bie => bie !== null);

        if (exists.length > 0) {
          const dialogConfig = this.confirmDialogService.newConfig();
          dialogConfig.data.header = 'Remove BIE' + (exists.length > 1 ? 's' : '') + ' in the previous version?';
          dialogConfig.data.content = [
            'Are you sure you want to remove BIE' + (exists.length > 1 ? 's' : '') + ' in the previous version?'
          ];
          dialogConfig.data.action = 'Remove anyway';

          this.confirmDialogService.open(dialogConfig).afterClosed()
              .subscribe(result => {
                if (result) {
                  removeBieAction();
                }
              });
        } else {
          removeBieAction();
        }
      });
    } else {
      // No previous version to compare against (e.g. the initial, non-revised package),
      // so there is nothing to warn about — remove the selected BIEs directly.
      removeBieAction();
    }
  }

  generate() {
    const selectedBieLists = this.selection.selected;
    let topLevelAsbiepIdList;
    if (selectedBieLists === undefined || selectedBieLists.length === 0) {
      topLevelAsbiepIdList = [];
    } else {
      topLevelAsbiepIdList = selectedBieLists.map(e => e.topLevelAsbiepId);
    }

    // Package export uses JSON Schema 2020-12 only when JSON is selected.
    if (this.option.expressionOption === 'JSON') {
      this.option.expressionVersion = '2020-12';
    } else {
      this.option.expressionVersion = '';
    }
    // Package export is intentionally fixed to per-file mode with split reused-schema refs.
    this.option.packageOption = 'EACH';
    this.option.arrayForJsonExpression = false;
    this.option.separateFileReferencesForReusedSchemas = true;
    this.option.includeBusinessContextInFilename = true;
    this.option.includeVersionInFilename = true;
    this.option.filenames = {};
    this.option.bizCtxIds = {};

    const targetBieLists = (selectedBieLists && selectedBieLists.length > 0)
      ? selectedBieLists
      : this.rows;
    for (const bieList of targetBieLists) {
      const selectedBusinessContext = this.businessContextSelection[bieList.topLevelAsbiepId];
      if (!!selectedBusinessContext) {
        this.option.bizCtxIds[bieList.topLevelAsbiepId] = selectedBusinessContext.businessContextId;
      }
    }

    this.loading = true;
    this.biePackageService.generateBiePackage(
      this.biePackage.biePackageId, this.option, this.manifestVersion, ...topLevelAsbiepIdList).subscribe(resp => {
      saveAsBlobResponse(resp);

      this.loading = false;
    }, err => {
      this.loading = false;
      throw err;
    });
  }

  getManifest(): any {
    this.loading = true;
    this.biePackageService.getManifest(this.biePackage.biePackageId).subscribe(resp => {
      this.loading = false;
      console.log(resp);
    }, err => {
      this.loading = false;
    });
  }

  isChanged(): boolean {
    if (!this.biePackage) {
      return false;
    }
    return this.hashCode !== hashCode(this.biePackage);
  }

  isDisabled(biePackage: BiePackageDetails) {
    return (this.disabled) ||
      (biePackage.name === undefined || biePackage.name === '') ||
      (biePackage.versionId === undefined || biePackage.versionId === '') ||
      (biePackage.versionName === undefined || biePackage.versionName === '') ||
      (biePackage.description === undefined || biePackage.description === '');
  }

  update() {
    if (this.hashCode !== hashCode(this.biePackage)) {
      this.biePackageService.update(this.biePackage).subscribe(_ => {
        this.init(this.biePackage);
        this.snackBar.open('Updated', '', {
          duration: 3000,
        });
      });
    }
  }

  updateState(state: string) {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Update state to \'' + state + '\'?';
    dialogConfig.data.content = ['Are you sure you want to update the state to \'' + state + '\'?'];
    dialogConfig.data.action = 'Update';

    this.loading = true;
    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (!result) {
          this.loading = false;
          return;
        }
        this.biePackageService.updateState(this.biePackage.biePackageId, state).subscribe(_ => {
          this.snackBar.open('State updated', '', {
            duration: 3000,
          });

          this.biePackageService.get(this.biePackageId).subscribe(biePackage => {
            this.init(biePackage);
          });

          this.loading = false;
        }, err => {
          this.loading = false;
          throw err;
        });
      });
  }

  discard() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard BIE Package?';
    dialogConfig.data.content = [
      'Are you sure you want to discard selected BIE package?',
      'The BIE package will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.biePackageService.delete(this.biePackage.biePackageId).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.selection.clear();
            this.router.navigateByUrl('/bie_package');
          });
        }
      });
  }

  expressionOptionChange() {
    // Keep package export behavior deterministic regardless of expression switch.
    this.option.packageOption = 'EACH';
    this.option.separateFileReferencesForReusedSchemas = true;
    this.option.expressionVersion = (this.option.expressionOption === 'JSON') ? '2020-12' : '';
    this.option.arrayForJsonExpression = false;
    this.option.includeBusinessContextInFilename = true;
    this.option.includeVersionInFilename = true;

    if (this.option.expressionOption !== 'XML') {
      this.option.bieCctsMetaData = false;
      this.option.includeCctsDefinitionTag = false;
      this.option.bieGuid = false;
      this.option.businessContext = false;
      this.option.bieOagiScoreMetaData = false;
      this.option.includeWhoColumns = false;
    }
    if (this.option.expressionOption !== 'XML' && this.option.expressionOption !== 'JSON') {
      this.option.basedCcMetaData = false;
    }
  }

  bieAnnotationChange() {
    if (!this.option.bieCctsMetaData) {
      this.option.includeCctsDefinitionTag = false;
    }

    if (!this.option.bieOagiScoreMetaData) {
      this.option.includeWhoColumns = false;
    }
  }

  isBasedCcMetaDataDisabled(): boolean {
    return this.option.expressionOption !== 'XML' && this.option.expressionOption !== 'JSON';
  }

  makeNewRevision() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Revise this BIE Package?';
    dialogConfig.data.content = ['Are you sure you want to revise this BIE Package?'];
    dialogConfig.data.action = 'Revise';

    this.confirmDialogService.open(dialogConfig).afterClosed()
        .subscribe(result => {
          if (!result) {
            this.loading = false;
            return;
          }

          this.loading = true;
          this.biePackageService.makeNewRevision(this.biePackage.biePackageId).subscribe(resp => {
            this.snackBar.open('Revised', '', {
              duration: 3000,
            });

            this.router.navigateByUrl('/bie_package/' + resp.biePackageId);
            this.loading = false;
          }, err => {
            this.loading = false;
            throw err;
          });
        });
  }

}
