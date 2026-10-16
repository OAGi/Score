import {afterNextRender, ChangeDetectionStrategy, Component, inject, OnInit, signal, viewChild} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {SelectionModel} from '@angular/cdk/collections';
import {tap} from 'rxjs/operators';
import {BieListEntry} from '../bie-list/domain/bie-list';
import {BieExpressService} from './domain/bie-express.service';
import {BieListService} from '../bie-list/domain/bie-list.service';
import {bieLinkQuery, BIE_FILTERS as F, BIE_TABLE} from '../bie-list/domain/bie.table';
import {BieExpressOption} from './domain/generate-expression';
import {MetaHeaderDialogComponent} from './meta-header-dialog/meta-header-dialog.component';
import {PaginationResponseDialogComponent} from './pagination-response-dialog/pagination-response-dialog.component';
import {saveAsBlobResponse} from '../../common/utility';
import {ReleaseSummary} from '../../release-management/domain/release';
import {ReleaseChoice} from '../../release-management/domain/release-choice';
import {ReleaseService} from '../../release-management/domain/release.service';
import {AuthService} from '../../authentication/auth.service';
import {WebPageInfoService} from '../../basis/basis.service';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../common/score-table';
import {LibrarySummary} from '../../library-management/domain/library';
import {LibraryChoice} from '../../library-management/domain/library-choice';
import {LibraryService} from '../../library-management/domain/library.service';

const BIE_EXPRESS_TABLE = defineTable({
  ...BIE_TABLE,
  filters: [F.states, F.businessContext, F.version, F.remark, F.deprecated, F.owner, F.updater, F.updatedDate,
    // The branch is chosen beside the search box; it is part of the query.
    {type: 'text', key: 'releaseId', label: 'Branch', hidden: true}],
});

@Component({
  standalone: false,
  selector: 'score-bie-express',
  templateUrl: './bie-express.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./bie-express.component.css']
})
export class BieExpressComponent implements OnInit {
  private service = inject(BieExpressService);
  private bieListService = inject(BieListService);
  private releaseService = inject(ReleaseService);
  private libraryService = inject(LibraryService);
  private auth = inject(AuthService);
  private dialog = inject(MatDialog);
  webPageInfo = inject(WebPageInfoService);

  readonly libraryChoice = new LibraryChoice(this.auth);
  readonly releaseChoice = new ReleaseChoice(this.auth, 'BIE');
  readonly columns = createTableColumns(BIE_EXPRESS_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<BieListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<number>(true, []);

  /** The list is of a library and a branch: it waits for them, and loads again when another one is chosen. */
  readonly ready = signal(false);
  readonly extraParams = signal<Record<string, string>>({});
  /** While the expressions are being generated. */
  readonly busy = signal(false);

  businessContextSelection = {};

  option: BieExpressOption;
  jsonSchemaVersions: string[] = ['2020-12', 'Draft-04'];
  openApiVersions: string[] = ['3.1', '3.0'];
  openApiFormats: string[] = ['YAML', 'JSON'];
  odfFormats: string[] = ['ODS', 'FODS', 'XLSX'];
  previousPackageOption: string;

  readonly load = (params: HttpParams) => this.bieListService.getBieListPage(params).pipe(
    tap({
      next: resp => resp.list.forEach((elm: BieListEntry) => {
        this.businessContextSelection[elm.topLevelAsbiepId] = elm.businessContextList[0];
      }),
      error: () => this.businessContextSelection = {}
    })
  );
  readonly rowKey = (row: BieListEntry) => row.topLevelAsbiepId;

  constructor() {
    afterNextRender(() => this.start());
  }

  ngOnInit() {
    this.option = new BieExpressOption();
    this.option.bieDefinition = true;
    this.option.expressionOption = 'XML';
    // Default JSON Schema expression version is '2020-12'.
    this.option.expressionVersion = '2020-12';
    this.option.separateFileReferencesForReusedSchemas = false;
    this.option.packageOption = 'ALL';
    // Default OpenAPI expression format is 'YAML'.
    this.option.openAPIExpressionFormat = 'YAML';
    // Default ODF expression format is 'ODS'.
    this.option.odfExpressionFormat = 'ODS';
  }

  /** Once the table view has the query of the URL: the library and the branch that it asks for. */
  private start() {
    const requestedReleaseId = Number(this.view().filterValue('releaseId') || 0);
    this.libraryService.getLibrarySummaryList().subscribe(libraries => {
      this.libraryChoice.init(libraries);
      this.applyLibrary();
      this.releaseService.getReleaseSummaryList(this.libraryChoice.library?.libraryId, ['Published']).subscribe(releases => {
        this.releaseChoice.init(releases.filter(e => !e.workingRelease), requestedReleaseId);
        this.applyRelease();
        this.ready.set(true);
      });
    });
  }

  /** Only what the user can view, but the Meta Header and the Pagination Response, which are included in another BIE. */
  private applyLibrary() {
    this.extraParams.set({
      libraryId: '' + this.libraryChoice.library?.libraryId,
      access: 'CanView',
      excludePropertyTerms: ['Meta Header', 'Pagination Response'].join(',')
    });
  }

  private applyRelease() {
    this.view().setFilter('releaseId', '' + (this.releaseChoice.release?.releaseId || 0));
  }

  onBranchChange(release: ReleaseSummary) {
    this.releaseChoice.choose(release);
    this.applyRelease();
    this.selection.clear();
    this.view().onSearch();
  }

  onLibraryChange(library: LibrarySummary) {
    this.libraryChoice.choose(library);
    this.releaseService.getReleaseSummaryList(library.libraryId, ['Published']).subscribe(releases => {
      this.releaseChoice.init(releases.filter(e => !e.workingRelease));
      this.applyRelease();
      this.selection.clear();
      this.view().onSearch();
      this.applyLibrary();
    });
  }

  /** The search part of a link to the list that shows the BIE. */
  linkQuery(extras: { [key: string]: any }): string {
    return bieLinkQuery(BIE_EXPRESS_TABLE, this.view().query, extras);
  }

  isSelected(row: BieListEntry) {
    return this.selection.isSelected(row.topLevelAsbiepId);
  }

  generate() {
    const selectedTopLevelAsbiepIds = this.selection.selected;

    this.option.filenames = {};
    this.option.bizCtxIds = {};
    for (const selectedTopLevelAsbiepId of selectedTopLevelAsbiepIds) {
      const filename = this.getFilename(selectedTopLevelAsbiepId);
      this.option.filenames[selectedTopLevelAsbiepId] = filename;

      const selectedBusinessContext = this.businessContextSelection[selectedTopLevelAsbiepId];
      this.option.bizCtxIds[selectedTopLevelAsbiepId] = selectedBusinessContext.businessContextId;
    }

    this.busy.set(true);
    this.service.generate(selectedTopLevelAsbiepIds, this.option).subscribe(resp => {
      saveAsBlobResponse(resp);

      this.busy.set(false);
    }, err => {
      this.busy.set(false);
    });
  }

  getFilename(topLevelAsbiepId: number): string {
    const topLevelAsbiep = this.view().dataSource.data.filter(e => e.topLevelAsbiepId === topLevelAsbiepId)[0];
    const separator = '-';

    let filename = topLevelAsbiep.propertyTerm.trim().split(' ').join(separator);
    if (this.option.includeBusinessContextInFilename) {
      const selectedBusinessContext = this.businessContextSelection[topLevelAsbiepId];
      if (!!selectedBusinessContext) {
        filename += '-' + selectedBusinessContext.name.trim().split(' ').join(separator);
      }
    }
    if (this.option.includeVersionInFilename) {
      if (!!topLevelAsbiep.version) {
        const versionSeparator = '_';
        filename += '-' + topLevelAsbiep.version.trim().split(' ').join(versionSeparator)
          .split('.').join(versionSeparator);
      }
    }
    return filename;
  }

  toggleMetaHeaderOption(event, disabled: boolean,
                         includeMetaHeaderForJsonPropertyKey: string,
                         metaHeaderTopLevelAsbiepIdPropertyKey: string) {
    if (disabled) {
      return;
    }

    if (this.option[metaHeaderTopLevelAsbiepIdPropertyKey]) {
      this.option[includeMetaHeaderForJsonPropertyKey] = false;
      this.option[metaHeaderTopLevelAsbiepIdPropertyKey] = undefined;

      this.option.packageOption = this.previousPackageOption;
    } else {
      const dialogConfig = new MatDialogConfig();
      dialogConfig.width = window.innerWidth + 'px';
      dialogConfig.data = {
        library: this.libraryChoice.library,
        release: this.releaseChoice.release
      };
      const dialogRef = this.dialog.open(MetaHeaderDialogComponent, dialogConfig);
      dialogRef.afterClosed().subscribe(selectedTopLevelAsbiepId => {
        if (selectedTopLevelAsbiepId) {
          this.option[includeMetaHeaderForJsonPropertyKey] = true;
          this.option[metaHeaderTopLevelAsbiepIdPropertyKey] = selectedTopLevelAsbiepId;

          if (includeMetaHeaderForJsonPropertyKey.includes('GetTemplate')) {
            this.option.suppressRootPropertyForOpenAPI30GetTemplate = false;
          } else if (includeMetaHeaderForJsonPropertyKey.includes('PostTemplate')) {
            this.option.suppressRootPropertyForOpenAPI30PostTemplate = false;
          }
          this.previousPackageOption = this.option.packageOption;
          this.option.packageOption = 'EACH';
        } else {
          this.option[includeMetaHeaderForJsonPropertyKey] = false;
          this.option[metaHeaderTopLevelAsbiepIdPropertyKey] = undefined;
        }
      });
    }
  }

  togglePaginationResponseOption(event, disabled: boolean,
                                 includePaginationResponseForJsonPropertyKey: string,
                                 paginationResponseTopLevelAsbiepIdPropertyKey: string) {
    if (disabled) {
      return;
    }

    if (this.option[paginationResponseTopLevelAsbiepIdPropertyKey]) {
      this.option[includePaginationResponseForJsonPropertyKey] = false;
      this.option[paginationResponseTopLevelAsbiepIdPropertyKey] = undefined;

      this.option.packageOption = this.previousPackageOption;
    } else {
      const dialogConfig = new MatDialogConfig();
      dialogConfig.width = window.innerWidth + 'px';
      dialogConfig.data = {
        library: this.libraryChoice.library,
        release: this.releaseChoice.release
      };
      const dialogRef = this.dialog.open(PaginationResponseDialogComponent, dialogConfig);
      dialogRef.afterClosed().subscribe(selectedTopLevelAsbiepId => {
        if (selectedTopLevelAsbiepId) {
          this.option[includePaginationResponseForJsonPropertyKey] = true;
          this.option[paginationResponseTopLevelAsbiepIdPropertyKey] = selectedTopLevelAsbiepId;

          if (includePaginationResponseForJsonPropertyKey.includes('GetTemplate')) {
            this.option.suppressRootPropertyForOpenAPI30GetTemplate = false;
          } else if (includePaginationResponseForJsonPropertyKey.includes('PostTemplate')) {
            this.option.suppressRootPropertyForOpenAPI30PostTemplate = false;
          }
          this.previousPackageOption = this.option.packageOption;
          this.option.packageOption = 'EACH';
        } else {
          this.option[includePaginationResponseForJsonPropertyKey] = false;
          this.option[paginationResponseTopLevelAsbiepIdPropertyKey] = undefined;
        }
      });
    }
  }

  expressionOptionChange() {
    if (this.option.expressionOption === 'OPENAPI3') {
      if (!this.openApiVersions.includes(this.option.expressionVersion)) {
        this.option.expressionVersion = '3.1';
      }
    }

    if (this.option.expressionOption === 'ODF' || this.option.expressionOption === 'AVRO') {
      this.option.packageOption = 'EACH';
    }

    if (this.option.expressionOption === 'JSON') {
      if (!this.jsonSchemaVersions.includes(this.option.expressionVersion)) {
        this.option.expressionVersion = '2020-12';
      }
      this.onJsonSchemaVersionChange();
      if (this.option.includeMetaHeaderForJson || this.option.includePaginationResponseForJson) {
        this.option.packageOption = 'EACH';
      }
    }

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

  onJsonSchemaVersionChange() {
    if (this.isSeparateFileReferencesForReusedSchemasDisabled()) {
      this.option.separateFileReferencesForReusedSchemas = false;
    }
  }

  isSeparateFileReferencesForReusedSchemasDisabled(): boolean {
    if (this.option.expressionOption === 'XML') {
      return false;
    }
    return this.option.expressionOption !== 'JSON'
      || this.option.expressionVersion === 'Draft-04';
  }

  isBasedCcMetaDataDisabled(): boolean {
    return this.option.expressionOption !== 'XML' && this.option.expressionOption !== 'JSON';
  }
}
