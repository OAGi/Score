import {Component} from '@angular/core';
import {LibraryDetails} from '../domain/library';
import {FormControl, Validators} from '@angular/forms';
import {LibraryService} from '../domain/library.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthService} from '../../authentication/auth.service';

@Component({
  selector: 'score-library-create',
  templateUrl: './library-create.component.html',
  styleUrl: './library-create.component.css'
})
export class LibraryCreateComponent {

  title = 'Create Library';
  loading = false;
  library: LibraryDetails;
  uriForm = new FormControl('');

  constructor(private service: LibraryService,
              private snackBar: MatSnackBar,
              private confirmDialogService: ConfirmDialogService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private auth: AuthService) {
  }

  ngOnInit() {
    this.library = new LibraryDetails();

    this.uriForm = new FormControl({value: this.library.link, disabled: !this.isAdmin},
        Validators.pattern('\\w+:(\\/?\\/?)[^\\s]+'));
  }

  get isDisabled() {
    return (!this.library.name) ||
        (!this.uriForm.valid) ||
        (!this.library.organization) ||
        (!this.library.domain);
  }

  back() {
    this.location.back();
  }

  get isAdmin(): boolean {
    return this.auth.isAdmin();
  }

  create() {
    if (this.isDisabled) {
      return;
    }

    this.loading = true;
    this.library.link = this.uriForm.value;
    this.service.create(this.library).subscribe(_ => {
      this.snackBar.open('Created', '', {
        duration: 3000,
      });
      this.loading = false;
      this.router.navigateByUrl('/library');
    }, error => {
      this.loading = false;
      throw error;
    });
  }

}
