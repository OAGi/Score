import {afterNextRender, ChangeDetectionStrategy, Component, inject, signal, viewChild} from '@angular/core';
import {SelectionModel} from '@angular/cdk/collections';
import {HttpParams} from '@angular/common/http';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ActivatedRoute, Router} from '@angular/router';
import {AsbieBbieListEntry, AssignedBusinessTermListEntry, BieToAssign} from '../domain/business-term';
import {BusinessTermService} from '../domain/business-term.service';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../common/score-table';

const BIE_TYPES = [{value: 'BBIE', label: 'BBIE'}, {value: 'ASBIE', label: 'ASBIE'}];

const ASSIGNED_BUSINESS_TERM_TABLE = defineTable({
  storageKey: 'TableColumns-AssignedBusinessTermPage',
  columns: [
    {id: 'bieDen', label: 'BIE DEN', width: 0, sortable: true},
    {id: 'bieType', label: 'BIE Type', width: 100, sortable: true},
    {id: 'businessTerm', label: 'Business Term', width: 200, sortable: true},
    {id: 'primaryIndicator', label: 'Preferred Business Term', width: 200, sortable: true},
    {id: 'externalReferenceUri', label: 'External Reference URI', width: 200, sortable: true},
    {id: 'externalReferenceId', label: 'External Reference ID', width: 200, sortable: true},
    {id: 'typeCode', label: 'Type Code', width: 100, sortable: true},
    {id: 'lastUpdateTimestamp', label: 'Updated On', header: 'Updated on', width: 160, sortable: true},
  ],
  defaultSort: {active: 'lastUpdateTimestamp', direction: 'desc'},
  search: {key: 'bieDen', placeholder: 'Search by DEN'},
  filters: [
    {type: 'select', key: 'bieTypes', httpParam: 'bieTypeList', label: 'Type', options: BIE_TYPES},
    {type: 'text', key: 'businessTerm', label: 'Business Term'},
    {type: 'text', key: 'externalReferenceUri', label: 'External Reference URI'},
    {type: 'text', key: 'typeCode', label: 'Type Code'},
    {type: 'updater', key: 'updaterUsernameList', httpParam: 'updaterLoginIdList', label: 'Updater'},
    {
      type: 'dateRange', key: 'updatedDate', label: 'Updated',
      httpStart: 'updateStart', httpEnd: 'updateEnd', urlStart: 'updateStart', urlEnd: 'updateEnd'
    },
    {type: 'boolean', key: 'primaryIndicator', label: 'Preferred Only'},
    // What "search by selected BIE" narrows to.
    {type: 'text', key: 'bieId', label: 'BIE', hidden: true},
  ],
});

@Component({
  standalone: false,
  selector: 'score-assigned-business-term',
  templateUrl: './assigned-business-term-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager
})
export class AssignedBusinessTermListComponent {
  private service = inject(BusinessTermService);
  private confirmDialogService = inject(ConfirmDialogService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);

  readonly columns = createTableColumns(ASSIGNED_BUSINESS_TERM_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<AssignedBusinessTermListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<AssignedBusinessTermListEntry>(true, []);

  /** The list is loaded once the query has been completed with what the URL says about the BIE. */
  readonly ready = signal(false);

  readonly load = (params: HttpParams) => this.service.getAssignedBusinessTermListPage(params);

  constructor() {
    afterNextRender(() => this.start());
  }

  /** Both types are always shown at first, whatever the URL says; a BIE in the URL narrows to it. */
  private start() {
    const view = this.view();
    view.setFilter('bieTypes', ['BBIE', 'ASBIE']);

    const bieId = this.route.snapshot.queryParamMap.get('bieId');
    const bieType = this.route.snapshot.queryParamMap.get('bieType');
    if (bieId !== null && bieId !== '' && bieType !== null && bieType !== '') {
      const bie = new BieToAssign();
      bie.bieId = Number(bieId);
      bie.bieType = bieType;
      view.setFilter('bieId', bieId);
      view.setFilter('bieTypes', [bieType]);
      this.service.confirmAsbieBbieListByIdAndType([bie])
        .subscribe((resp: AsbieBbieListEntry[]) => {
          if (!resp || resp.length !== 1) {
            this.router.navigateByUrl('/business_term_management/assign_business_term/create');
          } else {
            const bieResp = resp[0];
            view.setFilter('bieId', '' + bieResp.bieId);
            view.setFilter('bieTypes', [bieResp.type]);
            view.setFilter('bieDen', bieResp.den);
          }
        });
    }
    this.ready.set(true);
  }

  saveSelectedBIEForSearch() {
    const assignedBizTermIds = this.selection.selected;
    if (assignedBizTermIds.length === 1) {
      const selectedRow = this.view().dataSource.data.filter(row => assignedBizTermIds.includes(row))[0];
      this.view().setFilter('bieId', '' + selectedRow.bieId);
      this.view().setFilter('bieDen', selectedRow.den);
      this.view().reload();
      this.selection.clear();
    } else {
      const dialogConfig = this.confirmDialogService.newConfig();
      dialogConfig.data.header = 'Please select one row';
      dialogConfig.data.content = [
        'To perform search by BIE, please select exactly one row.',
        assignedBizTermIds.length + (assignedBizTermIds.length > 1 ? ' rows are' : ' row is') + ' currently selected.'
      ];
      this.confirmDialogService.open(dialogConfig);
    }
  }

  deselect() {
    this.view().setFilter('bieId', '');
    this.view().setFilter('bieDen', '');
    this.view().setFilter('bieTypes', ['ASBIE', 'BBIE']);
    this.view().reload();
  }

  discard() {
    const assignedBts = this.selection.selected;
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard ' + (assignedBts.length > 1 ? 'business terms assignments' : 'business term assignment') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to discard selected ' +
      (assignedBts.length > 1 ? 'business terms assignments' : 'business term assignment') + '?',
      'The ' + (assignedBts.length > 1 ? 'business terms assignments' : 'business term assignment') + ' will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.deleteAssignments(assignedBts).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.selection.clear();
            this.view().reload();
          });
        }
      });
  }

  openAssignedBt(type, id) {
    this.router.navigate(['/business_term_management/assign_business_term/details/' + id],
      {queryParams: {type, id}});
  }
}
