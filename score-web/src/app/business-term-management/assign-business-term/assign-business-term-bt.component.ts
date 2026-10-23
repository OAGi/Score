import {afterNextRender, ChangeDetectionStrategy, Component, inject, signal, viewChild} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {SelectionModel} from '@angular/cdk/collections';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {forkJoin} from 'rxjs';
import {map} from 'rxjs/operators';
import {BusinessTermService} from '../domain/business-term.service';
import {BieToAssign, BusinessTermListEntry, PostAssignBusinessTerm} from '../domain/business-term';
import {BUSINESS_TERM_TABLE} from '../domain/business-term.table';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {base64Decode} from '../../common/utility';
import {createTableColumns, ScoreTableViewComponent} from '../../common/score-table';

@Component({
  standalone: false,
  selector: 'score-assign-business-term',
  templateUrl: './assign-business-term-bt.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./assign-business-term-bt.component.css']
})
export class AssignBusinessTermBtComponent {
  private businessTermService = inject(BusinessTermService);
  private confirmDialogService = inject(ConfirmDialogService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);

  title = 'Assign Business Term';
  subtitle = 'Select Business Term';

  readonly columns = createTableColumns(BUSINESS_TERM_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<BusinessTermListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<number>(true, []);

  postAssignBtObj = new PostAssignBusinessTerm();

  /** The list waits for the BIEs that the terms are assigned to. */
  readonly ready = signal(false);
  /** While the BIEs are being checked. */
  readonly busy = signal(false);

  readonly load = (params: HttpParams) => this.businessTermService.getBusinessTermListPage(params);
  readonly rowKey = (row: BusinessTermListEntry) => row.businessTermId;

  constructor() {
    afterNextRender(() => this.start());
  }

  private start() {
    const params: ParamMap = this.route.snapshot.queryParamMap;
    const bieIds = this.extractArrayParam(params, 'bieIds');
    const bieTypes = this.extractArrayParam(params, 'bieTypes');
    if (bieIds.length !== bieTypes.length) {
      this.router.navigateByUrl('/business_term_management/assign_business_term/create');
    }
    const bies: BieToAssign[] = bieIds.map((value, index) => {
      const bie = new BieToAssign();
      bie.bieId = Number(value);
      bie.bieType = bieTypes[index];
      return bie;
    });
    this.busy.set(true);
    this.businessTermService.confirmAsbieBbieListByIdAndType(bies).subscribe(resp => {
      this.busy.set(false);
      if (!resp) {
        this.router.navigateByUrl('/business_term_management/assign_business_term/create');
      } else {
        this.postAssignBtObj.biesToAssign = resp.map(e => {
          const bie = new BieToAssign();
          bie.bieId = e.bieId;
          bie.bieType = e.type;
          return bie;
        });
        this.ready.set(true);
      }
    }, err => {
      this.busy.set(false);
      console.error(err);
    });
  }

  private extractArrayParam(params: ParamMap, paramName: string) {
    let str = params.get(paramName);
    if (!str) {
      const q = (this.route.snapshot.queryParamMap) ? this.route.snapshot.queryParamMap.get('q') : undefined;
      const httpParams = (q) ? new HttpParams({fromString: base64Decode(q)}) : new HttpParams();
      str = httpParams.get(paramName);
    }
    // #1752 - M7: guard against a missing param (null) — split() would throw a TypeError otherwise,
    // breaking step 2 of the assign wizard when navigated without bieIds/bieTypes.
    if (!str) {
      return [];
    }
    return str.split(',');
  }

  create() {
    this.checkUniqueness(this.postAssignBtObj, (_) => {
      if (this.postAssignBtObj.primaryIndicator) {
        forkJoin(this.postAssignBtObj.biesToAssign
          .map(bie => (
            this.businessTermService.findIfPrimaryExist(bie.bieId, bie.bieType,
              this.postAssignBtObj.primaryIndicator)
              .pipe(map(resp => (resp && resp.length > 0))
              ))))
          .subscribe((ifPrimaries: boolean[]) => {
            if (ifPrimaries.filter(ifPrimary => ifPrimary === true).length > 0) {
              const dialogConfig = this.confirmDialogService.newConfig();
              dialogConfig.data.header = 'Overwrite previous preferred business terms?';
              dialogConfig.data.content = [
                'The preferred business term already exists for ' +
                ((this.postAssignBtObj.biesToAssign.length === 1) ? 'the selected BIE.' : 'some of the selected BIEs.') +
                ' Are you sure you want to proceed and overwrite the previous preferred business term assignment?'
              ];
              dialogConfig.data.action = 'Create';
              this.confirmDialogService.open(dialogConfig).afterClosed()
                .subscribe(result => {
                  if (result) {
                    this.doCreate();
                  }
                });
            } else {
              this.doCreate();
            }
          });
      } else {
        this.doCreate();
      }
    });
  }

  doCreate() {
    const businessTerm = this.selection.selected[0];

    this.businessTermService.assignBusinessTermToBie(businessTerm, this.postAssignBtObj.biesToAssign,
      this.postAssignBtObj.primaryIndicator, this.postAssignBtObj.typeCode)
      .subscribe(resp => {
        this.snackBar.open('Created', '', {
          duration: 3000,
        });
        // #1753 - M1 (#1374): after Create, scope the assignment list to the BIE just assigned
        // (only meaningful when a single BIE was targeted; the list reads bieId/bieType).
        const bies = this.postAssignBtObj.biesToAssign;
        if (bies && bies.length === 1) {
          this.router.navigate(['/business_term_management/assign_business_term'],
            {queryParams: {bieId: bies[0].bieId, bieType: bies[0].bieType}});
        } else {
          this.router.navigateByUrl('/business_term_management/assign_business_term');
        }
      });
  }

  back() {
    this.router.navigateByUrl('/business_term_management/assign_business_term/create');
  }

  isCreateDisabled() {
    return (this.selection.selected.length === 0);
  }

  checkUniqueness(_postAssignBusinessTerm: PostAssignBusinessTerm, callbackFn?) {
    forkJoin(_postAssignBusinessTerm.biesToAssign
        .map(bie => (
            this.businessTermService.checkAssignmentUniqueness(bie.bieId, bie.bieType,
                this.selection.selected[0], _postAssignBusinessTerm.typeCode, _postAssignBusinessTerm.primaryIndicator)
        )))
        .subscribe((ifUniques: boolean[]) => {
          if (ifUniques.filter(isUnique => !isUnique).length > 0) {
            this.openDialogDisabledAssignment();
            return;
          }
          return callbackFn && callbackFn(_postAssignBusinessTerm);
        });
  }

  openDialogDisabledAssignment() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Invalid parameters';
    dialogConfig.data.content = [
      'Another business term assignment for the same BIE and type code already exists!'
    ];

    this.confirmDialogService.open(dialogConfig).afterClosed().subscribe(_ => {});
  }

}
