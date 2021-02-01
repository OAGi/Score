import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../authentication/auth.service';
import {ReleaseService} from '../domain/release.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Router} from '@angular/router';
import {ReleaseDetail} from '../domain/release';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {NamespaceList} from '../../namespace-management/domain/namespace';
import {NamespaceService} from '../../namespace-management/domain/namespace.service';

@Component({
  selector: 'score-release-list',
  templateUrl: './release-create.component.html',
  styleUrls: ['./release-create.component.css']
})
export class ReleaseCreateComponent implements OnInit {

  title = 'Releases Detail';

  releaseDetail = new ReleaseDetail();
  namespaceList: NamespaceList[];
  selectedNamespace: NamespaceList;
  namespaceListFilterCtrl: FormControl = new FormControl();
  filteredNamespaceList: ReplaySubject<NamespaceList[]> = new ReplaySubject<NamespaceList[]>(1);
  loading = false;
  states: string[] = ['Draft', 'Final'];

  constructor(private service: ReleaseService,
              private accountService: AccountListService,
              private namespaceService: NamespaceService,
              private snackBar: MatSnackBar,
              private router: Router,
              private auth: AuthService) {
  }

  ngOnInit() {
    const userToken = this.auth.getUserToken();
    if (userToken.role !== 'developer') {
      this.router.navigateByUrl('/');
    }
    this.namespaceService.getNamespaceList().subscribe(resp => {
      this.namespaceList = resp.list.filter(e => e.std);
      this.filteredNamespaceList.next(this.namespaceList.slice());
    });

    this.namespaceListFilterCtrl.valueChanges
      .subscribe(() => {
        this.filterNamespaceList();
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

  create() {
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
