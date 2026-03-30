import { Component, HostListener, inject } from '@angular/core';
import {FormControl, Validators} from '@angular/forms';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {Location} from '@angular/common';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {AuthService} from '../../authentication/auth.service';
import {switchMap} from 'rxjs/operators';
import {LibraryDetails} from '../domain/library';
import {LibraryService} from '../domain/library.service';
import {hashCode} from 'src/app/common/utility';
import {Title} from '@angular/platform-browser';
import {setAppTitleIfPresent} from '../../common/app-title.strategy';

@Component({
  standalone: false,
  selector: 'score-library-detail',
  templateUrl: './library-detail.component.html',
  styleUrl: './library-detail.component.css'
})
export class LibraryDetailComponent {
  private service = inject(LibraryService);
  private snackBar = inject(MatSnackBar);
  private confirmDialogService = inject(ConfirmDialogService);
  private location = inject(Location);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private auth = inject(AuthService);
  private titleService = inject(Title);


  title = 'Library Detail';
  loading = false;
  library: LibraryDetails;

  uriForm = new FormControl('');
  hashCode;

  ngOnInit() {
    this.loading = true;
    this.library = new LibraryDetails();

    // load library
    this.route.paramMap.pipe(
        switchMap((params: ParamMap) =>
            this.service.getLibraryById(Number(params.get('id'))))
    ).subscribe(resp => {
      if (!resp) {
        this.redirectToLibraryList();
        return;
      }

      this.library = resp;
      setAppTitleIfPresent(this.titleService, this.library.name, 'Library');
      this.uriForm = new FormControl({value: this.library.link, disabled: !this.isAdmin},
          Validators.pattern('\\w+:(\\/?\\/?)[^\\s]+'));
      this.hashCode = hashCode(resp);
      this.loading = false;
    }, err => {
      this.loading = false;

      if (err.status === 404) {
        this.redirectToLibraryList();
        return;
      }

      this.snackBar.open('Something\'s wrong.', '', {
        duration: 3000,
      });
    });
  }

  private redirectToLibraryList() {
    this.loading = false;
    this.snackBar.open('The requested library is unavailable.', '', {
      duration: 3000,
    });
    this.router.navigateByUrl('/library');
  }

  isChanged() {
    if (this.uriForm) {
      this.library.link = this.uriForm.value;
    }
    return this.hashCode !== hashCode(this.library);
  }

  isDisabled() {
    return (!this.library.name) ||
        (!this.library.link || !this.uriForm.valid) ||
        (!this.library.organization) ||
        (!this.library.domain);
  }

  back() {
    this.location.back();
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

  get updateDisabled(): boolean {
    return !this.isChanged() || this.isDisabled();
  }

  get isAdmin(): boolean {
    return this.auth.isAdmin();
  }

  update() {
    if (this.updateDisabled) {
      return;
    }

    this.loading = true;
    this.library.link = this.uriForm.value;
    this.service.update(this.library).subscribe(_ => {
      this.hashCode = hashCode(this.library);
      setAppTitleIfPresent(this.titleService, this.library.name, 'Library');
      this.snackBar.open('Updated', '', {
        duration: 3000,
      });
      this.loading = false;
    }, error => {
      this.loading = false;
      throw error;
    });
  }

  discard() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard Library?';
    dialogConfig.data.content = [
      'Are you sure you want to discard this library?',
      'The library will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.loading = true;
    this.confirmDialogService.open(dialogConfig).afterClosed()
        .subscribe(result => {
          if (result) {
            this.service.discard(this.library.libraryId).subscribe(_ => {
              this.snackBar.open('Discarded', '', {
                duration: 3000,
              });
              this.router.navigateByUrl('/library');
            });
          }
          this.loading = false;
        }, error => {
          this.loading = false;
          throw error;
        });
  }

}
