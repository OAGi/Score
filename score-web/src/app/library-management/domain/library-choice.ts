import {AuthService} from '../../authentication/auth.service';
import {loadLibrary, saveLibrary} from '../../common/utility';
import {LibrarySummary} from './library';

/**
 * The library that a list of a library (modules, namespaces, BIEs, ...) shows: the one the user last chose (remembered per user),
 * or else the default one. `mappedLibraries` is what `score-title-with-library-selector` lists.
 */
export class LibraryChoice {
  libraries: LibrarySummary[] = [];
  mappedLibraries: { library: LibrarySummary, selected: boolean }[] = [];
  library: LibrarySummary = new LibrarySummary();

  constructor(private readonly auth: AuthService) {
  }

  init(libraries: LibrarySummary[]) {
    this.libraries = libraries;
    if (this.libraries.length > 0) {
      const savedLibraryId = loadLibrary(this.auth.getUserToken());
      if (savedLibraryId) {
        this.library = this.libraries.filter(e => e.libraryId === savedLibraryId)[0];
      }
      if (!this.library || !this.library.libraryId) {
        this.library = this.libraries.find(e => e.isDefault) || this.libraries[0];
      }
      if (this.library) {
        saveLibrary(this.auth.getUserToken(), this.library.libraryId);
      }
      this.mappedLibraries = this.libraries.map(e => {
        return {library: e, selected: (this.library.libraryId === e.libraryId)};
      });
    }
  }

  choose(library?: LibrarySummary) {
    this.library = library;
    saveLibrary(this.auth.getUserToken(), this.library?.libraryId);
  }
}
