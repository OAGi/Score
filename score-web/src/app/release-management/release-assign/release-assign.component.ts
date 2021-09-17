import {CdkDragDrop, moveItemInArray, transferArrayItem} from '@angular/cdk/drag-drop';
import {Component, OnInit} from '@angular/core';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {finalize, switchMap} from 'rxjs/operators';
import {AuthService} from '../../authentication/auth.service';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {ReleaseService} from '../domain/release.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {
  AssignableList,
  AssignableMap,
  AssignableNode,
  ReleaseDetail,
  ReleaseValidationRequest,
  ReleaseValidationResponse, ValidationMessage
} from '../domain/release';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {Namespace} from '../../namespace-management/domain/namespace';
import {filter, hashCode} from '../../common/utility';

@Component({
  selector: 'score-release-list',
  templateUrl: './release-assign.component.html',
  styleUrls: ['./release-assign.component.css']
})
export class ReleaseAssignComponent implements OnInit {

  title = 'Releases Assign';
  $hashCode: string;
  typeList: string[] = ['ACC', 'ASCCP', 'BCCP'];
  loginIdList: string[] = [];
  loginIdListFilterCtrl: FormControl = new FormControl();
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);

  releaseDetail = new ReleaseDetail();
  namespace: Namespace;
  isLoading = false;
  itemsMap: AssignableMap;
  itemsList = new AssignableList();

  filter = {
    den: '',
    type: [],
    owner: []
  };

  isValidated = false;

  constructor(private service: ReleaseService,
              private accountService: AccountListService,
              private snackBar: MatSnackBar,
              private route: ActivatedRoute,
              private router: Router,
              private auth: AuthService,
              private confirmDialogService: ConfirmDialogService) {
  }

  ngOnInit() {
    const userToken = this.auth.getUserToken();
    if (userToken.role !== 'developer') {
      this.router.navigateByUrl('/');
    }
    this.isLoading = true;
    this.route.paramMap.pipe(
      switchMap((params: ParamMap) => {
          const releaseId = params.get('id');
          return forkJoin([
            this.service.getReleaseDetail(releaseId),
            this.service.getReleaseAssignable(releaseId),
            this.accountService.getAccountNames()
          ]);
        }
      )).subscribe(([resp, map, accounts]) => {
      this.releaseDetail = resp;
      this.$hashCode = hashCode(this.releaseDetail);

      this.itemsMap = map;
      this.itemsList = this.mapToList(this.itemsMap);

      this.loginIdList = accounts;
      this.loginIdListFilterCtrl.valueChanges
        .subscribe(() => filter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList));
      this.filteredLoginIdList.next(this.loginIdList);

      this.isLoading = false;
    });
  }

  isDisabled() {
    return this.releaseDetail.state === 'Published';
  }

  get isChanged() {
    return this.$hashCode !== hashCode(this.releaseDetail);
  }

  mapToList(map: AssignableMap): AssignableList {
    const list = new AssignableList();
    for (const key of Array.from(Object.keys(map.assignableAccManifestMap))) {
      const node = map.assignableAccManifestMap[key] as AssignableNode;
      node.visible = true;
      list.assignableList.push(node);
    }

    for (const key of Array.from(Object.keys(map.assignableAsccpManifestMap))) {
      const node = map.assignableAsccpManifestMap[key] as AssignableNode;
      node.visible = true;
      list.assignableList.push(node);
    }

    for (const key of Array.from(Object.keys(map.assignableBccpManifestMap))) {
      const node = map.assignableBccpManifestMap[key] as AssignableNode;
      node.visible = true;
      list.assignableList.push(node);
    }

    for (const key of Array.from(Object.keys(map.assignableCodeListManifestMap))) {
      const node = map.assignableCodeListManifestMap[key] as AssignableNode;
      node.visible = true;
      list.assignableList.push(node);
    }

    for (const key of Array.from(Object.keys(map.assignableAgencyIdListManifestMap))) {
      const node = map.assignableAgencyIdListManifestMap[key] as AssignableNode;
      node.visible = true;
      list.assignableList.push(node);
    }
    list.assignableList.sort(this._sort);
    list.assignedList.sort(this._sort);
    return list;
  }

  _sort(a: AssignableNode, b: AssignableNode): number {
    const sortStateOrder = ['Candidate', 'Draft', 'WIP', 'Deleted'];
    const sortTypeOrder = ['ACC', 'ASCCP', 'BCCP', 'CODE_LIST', 'AGENCY_ID_LIST'];
    if (sortStateOrder.indexOf(a.state) > sortStateOrder.indexOf(b.state)) {
      return 1;
    } else if (sortStateOrder.indexOf(a.state) < sortStateOrder.indexOf(b.state)) {
      return -1;
    } else {
      if (sortTypeOrder.indexOf(a.type) > sortTypeOrder.indexOf(b.type)) {
        return 1;
      } else {
        return -1;
      }
    }
  }

  drop(event: CdkDragDrop<AssignableNode[]>) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      transferArrayItem(event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex);

      this.isValidated = false;
    }
  }

  assignAll() {
    const remains = [];
    for (const node of this.itemsList.assignableList.reverse()) {
      if (node.state === 'Candidate' && node.visible) {
        this.itemsList.assignedList.splice(0, 0, node);
      } else {
        remains.push(node);
      }
    }

    this.itemsList.assignableList = remains;
    this.isValidated = false;
  }

  unassignAll() {
    const remains = [];
    for (const node of this.itemsList.assignedList.reverse()) {
      if (node.state === 'Candidate' && node.visible) {
        this.itemsList.assignableList.splice(0, 0, node);
      } else {
        remains.push(node);
      }
    }

    this.itemsList.assignedList = remains;
    this.isValidated = false;
  }

  sortList(list: AssignableNode[]) {
    list.sort(this._sort);
  }

  filterList() {
    for (const node of this.itemsList.assignableList) {
      node.visible = this.isVisible(node);
    }

    for (const node of this.itemsList.assignedList) {
      node.visible = this.isVisible(node);
    }
  }

  isVisible(node: AssignableNode) {
    if (this.filter.den.length > 2) {
      if (node.den.toLowerCase().indexOf(this.filter.den.toLowerCase()) === -1) {
        return false;
      }
    }
    if (this.filter.type.length > 0) {
      if (this.filter.type.indexOf(node.type) === -1) {
        return false;
      }
    }
    if (this.filter.owner.length > 0) {
      if (this.filter.owner.indexOf(node.ownerUserId) === -1) {
        return false;
      }
    }
    return true;
  }

  onChange() {
    this.filterList();
  }

  count(list: AssignableNode[]) {
    return list.filter(e => e.visible).length;
  }

  validate() {
    const request = new ReleaseValidationRequest();
    this.itemsList.assignedList.forEach(node => {
      switch (node.type) {
        case 'ACC':
          request.assignedAccComponentManifestIds.push(node.manifestId);
          break;
        case 'ASCCP':
          request.assignedAsccpComponentManifestIds.push(node.manifestId);
          break;
        case 'BCCP':
          request.assignedBccpComponentManifestIds.push(node.manifestId);
          break;
        case 'CODE_LIST':
          request.assignedCodeListComponentManifestIds.push(node.manifestId);
          break;
        case 'AGENCY_ID_LIST':
          request.assignedAgencyIdListComponentManifestIds.push(node.manifestId);
          break;
      }
    });

    // reset error messages
    this.itemsList.assignedList.forEach(e => {
      e.errors = [];
    });
    this.itemsList.assignableList.forEach(e => {
      e.errors = [];
    });

    this.isLoading = true;
    this.service.validate(request).pipe(finalize(() => {
      this.isLoading = false;
    })).subscribe(resp => {
      const map: ReleaseValidationResponse = resp;
      if (Object.getOwnPropertyNames(map.statusMapForAcc).length > 0) {
        this.addErrorsToNode(map.statusMapForAcc, 'ACC');
      }
      if (Object.getOwnPropertyNames(map.statusMapForAsccp).length > 0) {
        this.addErrorsToNode(map.statusMapForAsccp, 'ASCCP');
      }
      if (Object.getOwnPropertyNames(map.statusMapForBccp).length > 0) {
        this.addErrorsToNode(map.statusMapForBccp, 'BCCP');
      }
      if (Object.getOwnPropertyNames(map.statusMapForCodeList).length > 0) {
        this.addErrorsToNode(map.statusMapForCodeList, 'CODE_LIST');
      }
      if (Object.getOwnPropertyNames(map.statusMapForAgencyIdList).length > 0) {
        this.addErrorsToNode(map.statusMapForAgencyIdList, 'AGENCY_ID_LIST');
      }
      console.log(map);
      if (map.succeed) {
        this.snackBar.open('All components are valid.', '', {
          duration: 3000,
        });
      }

      this.isValidated = map.succeed;
    });
  }

  addErrorsToNode(map: Map<number, ValidationMessage[]>, type: string) {
    this.itemsList.assignedList.forEach(e => {
      if (e.type === type && map.hasOwnProperty(e.manifestId)) {
        e.errors = map[e.manifestId];
      }
    });
    this.itemsList.assignableList.forEach(e => {
      if (e.type === type && map.hasOwnProperty(e.manifestId)) {
        e.errors = map[e.manifestId];
      }
    });
  }

  makeDraft() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Create Release Draft?';
    dialogConfig.data.content = ['Are you sure you want to make this release to \'Draft\'?'];
    dialogConfig.data.action = 'Create';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          const request = new ReleaseValidationRequest();
          this.itemsList.assignedList.forEach(node => {
            switch (node.type) {
              case 'ACC':
                request.assignedAccComponentManifestIds.push(node.manifestId);
                break;
              case 'ASCCP':
                request.assignedAsccpComponentManifestIds.push(node.manifestId);
                break;
              case 'BCCP':
                request.assignedBccpComponentManifestIds.push(node.manifestId);
                break;
              case 'CODE_LIST':
                request.assignedCodeListComponentManifestIds.push(node.manifestId);
                break;
              case 'AGENCY_ID_LIST':
                request.assignedAgencyIdListComponentManifestIds.push(node.manifestId);
                break;
            }
          });

          // reset error messages
          this.itemsList.assignedList.forEach(e => {
            e.errors = [];
          });
          this.itemsList.assignableList.forEach(e => {
            e.errors = [];
          });

          this.isLoading = true;
          this.service.makeDraft(this.releaseDetail.releaseId, request).pipe(finalize(() => {
            this.isLoading = false;
          })).subscribe(resp => {
            const map: ReleaseValidationResponse = resp;
            if (Object.getOwnPropertyNames(map.statusMapForAcc).length > 0) {
              this.addErrorsToNode(map.statusMapForAcc, 'ACC');
            }
            if (Object.getOwnPropertyNames(map.statusMapForAsccp).length > 0) {
              this.addErrorsToNode(map.statusMapForAsccp, 'ASCCP');
            }
            if (Object.getOwnPropertyNames(map.statusMapForBccp).length > 0) {
              this.addErrorsToNode(map.statusMapForBccp, 'BCCP');
            }
            if (Object.getOwnPropertyNames(map.statusMapForCodeList).length > 0) {
              this.addErrorsToNode(map.statusMapForCodeList, 'CODE_LIST');
            }
            if (Object.getOwnPropertyNames(map.statusMapForAgencyIdList).length > 0) {
              this.addErrorsToNode(map.statusMapForAgencyIdList, 'AGENCY_ID_LIST');
            }
            this.isValidated = map.succeed;

            if (this.isValidated) {
              return this.router.navigateByUrl('/release');
            } else {
              this.snackBar.open('Validation failed.', '', {
                duration: 3000,
              });
            }
          });
        }
      });
  }

  hrefLink(item: AssignableNode): string[] {
    const arr = [];
    if (item.type === 'CODE_LIST' || item.type === 'AGENCY_ID_LIST') {
      arr.push('');
    } else {
      arr.push('/core_component');
    }
    arr.push(item.type.toLowerCase());
    arr.push(item.manifestId);
    return arr;
  }
}
