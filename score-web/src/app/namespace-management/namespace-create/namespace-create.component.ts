import {Component, OnInit} from '@angular/core';
import {FormControl, Validators} from '@angular/forms';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthService} from '../../authentication/auth.service';
import {Namespace} from '../domain/namespace';
import {NamespaceService} from '../domain/namespace.service';
import {Library} from '../../library-management/domain/library';
import {LibraryService} from '../../library-management/domain/library.service';
import {loadLibrary, saveLibrary} from '../../common/utility';

@Component({
  selector: 'score-namespace-create',
  templateUrl: './namespace-create.component.html',
  styleUrls: ['./namespace-create.component.css']
})
export class NamespaceCreateComponent implements OnInit {

  title = 'Create Namespace';
  disabled: boolean;
  library: Library = new Library();
  libraries: Library[] = [];
  mappedLibraries: { library: Library, selected: boolean }[] = [];
  namespace: Namespace;
  uriForm: FormControl;
  hashCode;

  constructor(private service: NamespaceService,
              private libraryService: LibraryService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private auth: AuthService,
              private snackBar: MatSnackBar) {
  }

  ngOnInit() {
    this.disabled = false;
    this.namespace = new Namespace();
    this.uriForm = new FormControl(this.namespace.uri, Validators.pattern('\\w+:(\\/?\\/?)[^\\s]+'));

    this.libraryService.getLibraries().subscribe(libraries => {
      this.initLibraries(libraries);
    });
  }

  isDisabled() {
    return !this.uriForm.valid;
  }

  get isDeveloper(): boolean {
    return this.auth.getUserToken().roles.includes('developer');
  }

  back() {
    this.location.back();
  }

  initLibraries(libraries: Library[]) {
    this.libraries = libraries;
    if (this.libraries.length > 0) {
      const savedLibraryId = loadLibrary(this.auth.getUserToken());
      if (savedLibraryId) {
        this.library = this.libraries.filter(e => e.libraryId === savedLibraryId)[0];
        saveLibrary(this.auth.getUserToken(), this.library.libraryId);
      }
      if (!this.library || this.library.libraryId === 0) {
        this.library = this.libraries[0];
      }
      this.mappedLibraries = this.libraries.map(e => {
        return {library: e, selected: (this.library.libraryId === e.libraryId)};
      });
    }
  }

  onLibraryChange(library: Library) {
    this.library = library;
    saveLibrary(this.auth.getUserToken(), this.library.libraryId);
  }

  create() {
    this.namespace.libraryId = this.library.libraryId;
    this.namespace.uri = this.uriForm.value;
    this.service.create(this.namespace).subscribe(_ => {
      this.snackBar.open('Created', '', {
        duration: 3000,
      });
      this.router.navigateByUrl('/namespace');
    });
  }

}
