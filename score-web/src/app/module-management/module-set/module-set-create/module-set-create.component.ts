import {Component, OnInit} from '@angular/core';
import {ModuleSet} from '../../domain/module';
import {ModuleService} from '../../domain/module.service';
import {Location} from '@angular/common';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatDialog} from '@angular/material/dialog';
import {AuthService} from '../../../authentication/auth.service';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {finalize, switchMap} from 'rxjs/operators';
import {hashCode} from '../../../common/utility';

@Component({
  selector: 'score-module-set-create',
  templateUrl: './module-set-create.component.html',
  styleUrls: ['./module-set-create.component.css']
})
export class ModuleSetCreateComponent implements OnInit {

  title = 'Create Module Set';
  isUpdating: boolean;
  moduleSet: ModuleSet = new ModuleSet();
  private $hashCode: string;

  constructor(private service: ModuleService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private auth: AuthService,
              private confirmDialogService: ConfirmDialogService) {
  }

  ngOnInit(): void {
    this.init(this.moduleSet);
  }

  init(moduleSet: ModuleSet) {
    this.moduleSet = moduleSet;
    this.$hashCode = hashCode(this.moduleSet);
  }

  get isChanged(): boolean {
    return hashCode(this.moduleSet) !== this.$hashCode;
  }

  createModuleSet() {
    if (!this.isChanged) {
      return;
    }

    this.isUpdating = true;

    this.service.createModuleSet(this.moduleSet)
      .pipe(finalize(() => {
        this.isUpdating = false;
      }))
      .subscribe(moduleSet => {
        this.snackBar.open('Created', '', {
          duration: 3000,
        });
        this.router.navigateByUrl('/module_management/module_set');
      });
  }

}
