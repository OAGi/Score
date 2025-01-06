import {Component, HostListener} from '@angular/core';
import {FormControl, Validators} from '@angular/forms';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {Location} from '@angular/common';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {AuthService} from '../../authentication/auth.service';
import {finalize, switchMap} from 'rxjs/operators';
import {Library} from '../domain/library';
import {LibraryService} from '../domain/library.service';
import {hashCode} from 'src/app/common/utility';

@Component({
  selector: 'score-library-detail',
  templateUrl: './library-detail.component.html',
  styleUrl: './library-detail.component.css'
})
export class LibraryDetailComponent {

  title = 'Library Detail';
  loading = false;
  library: Library;
  uriForm = new FormControl('');
  hashCode;

  constructor(private service: LibraryService,
              private snackBar: MatSnackBar,
              private confirmDialogService: ConfirmDialogService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private auth: AuthService) {
  }

  ngOnInit() {
    this.library = new Library();

    // load library
    this.route.paramMap.pipe(
        switchMap((params: ParamMap) =>
            this.service.getLibraryById(Number(params.get('id'))))
    ).subscribe(resp => {
      this.library = resp;
      this.uriForm = new FormControl({value: this.library.link, disabled: !this.isAdmin},
          Validators.pattern('\\w+:(\\/?\\/?)[^\\s]+'));
      this.hashCode = hashCode(resp);
    });
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
