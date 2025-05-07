import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../authentication/auth.service';
import {ReleaseService} from '../domain/release.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Router} from '@angular/router';
import {ReleaseDetails} from '../domain/release';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {NamespaceSummary} from '../../namespace-management/domain/namespace';
import {NamespaceService} from '../../namespace-management/domain/namespace.service';
import {LibrarySummary} from '../../library-management/domain/library';
import {LibraryService} from '../../library-management/domain/library.service';
import {loadLibrary, saveLibrary} from '../../common/utility';

@Component({
  selector: 'score-release-list',
  templateUrl: './release-create.component.html',
  styleUrls: ['./release-create.component.css']
})
export class ReleaseCreateComponent implements OnInit {

  title = 'Releases Detail';

  releaseDetail = new ReleaseDetails();
  library: LibrarySummary = new LibrarySummary();
  libraries: LibrarySummary[] = [];
  mappedLibraries: { library: LibrarySummary, selected: boolean }[] = [];
  namespaceList: NamespaceSummary[];
  selectedNamespace: NamespaceSummary;
  namespaceListFilterCtrl: FormControl = new FormControl();
  filteredNamespaceList: ReplaySubject<NamespaceSummary[]> = new ReplaySubject<NamespaceSummary[]>(1);
  loading = false;
  states: string[] = ['Draft', 'Final'];

  constructor(private service: ReleaseService,
              private accountService: AccountListService,
              private namespaceService: NamespaceService,
              private libraryService: LibraryService,
              private snackBar: MatSnackBar,
              private router: Router,
              private auth: AuthService) {
    this.library.libraryId = 0;
  }

  ngOnInit() {
    const userToken = this.auth.getUserToken();
    if (!userToken.roles.includes('developer')) {
      this.router.navigateByUrl('/');
    }

    this.libraryService.getLibrarySummaryList().subscribe(libraries => {
      this.initLibraries(libraries);

      this.loadNamespaces();
    });
  }

  filterNamespaceList() {
    let search = this.namespaceListFilterCtrl.value;
    if (!search) {
      this.filteredNamespaceList.next(this.namespaceList.slice());
      return;
    } else {
      search = search.toLowerCase();
    }
    this.filteredNamespaceList.next(
      this.namespaceList.filter(namespaceList => namespaceList.uri.toLowerCase().indexOf(search) > -1)
    );
  }

  initLibraries(libraries: LibrarySummary[]) {
    this.libraries = libraries;
    if (this.libraries.length > 0) {
      const savedLibraryId = loadLibrary(this.auth.getUserToken());
      if (savedLibraryId) {
        this.library = this.libraries.filter(e => e.libraryId === savedLibraryId)[0];
      }
      if (!this.library || !this.library.libraryId) {
        this.library = this.libraries[0];
      }
      if (this.library) {
        saveLibrary(this.auth.getUserToken(), this.library.libraryId);
      }
      this.mappedLibraries = this.libraries.map(e => {
        return {library: e, selected: (this.library.libraryId === e.libraryId)};
      });
    }
  }

  loadNamespaces() {
    this.namespaceService.getNamespaceSummaries(this.library.libraryId).subscribe(namespaceList => {
      this.namespaceList = namespaceList.filter(e => e.standard);
      this.filteredNamespaceList.next(this.namespaceList.slice());
      this.namespaceListFilterCtrl.valueChanges
        .subscribe(() => {
          this.filterNamespaceList();
        });
    });
  }

  onLibraryChange(library: LibrarySummary) {
    this.library = library;
    saveLibrary(this.auth.getUserToken(), this.library.libraryId);
    this.loadNamespaces();
  }

  create() {
    if (this.library) {
      this.releaseDetail.libraryId = this.library.libraryId;
    }
    if (this.selectedNamespace) {
      this.releaseDetail.namespaceId = this.selectedNamespace.namespaceId;
    }
    this.service.createRelease(this.releaseDetail).subscribe(resp => {
      if (resp.status === 'success') {
        this.snackBar.open('Created', '', {
          duration: 3000,
        });
        return this.router.navigateByUrl('/release');
      } else {
        this.snackBar.open(resp.statusMessage, '', {
          duration: 3000,
        });
      }
    });
  }

  isDisabled() {
    if (this.releaseDetail.releaseNum === undefined || this.releaseDetail.releaseNum === '') {
      return true;
    }
    if (this.selectedNamespace === undefined || this.selectedNamespace === null) {
      return true;
    }
    return false;
  }
}
