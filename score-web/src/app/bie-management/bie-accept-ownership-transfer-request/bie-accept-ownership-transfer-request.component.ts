import {Component, OnInit} from '@angular/core';
import {BieListService} from '../bie-list/domain/bie-list.service';
import {switchMap} from 'rxjs/operators';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {BieEditService} from '../bie-edit/domain/bie-edit.service';

@Component({
  selector: 'score-bie-accept-ownership-transfer-request',
  templateUrl: 'bie-accept-ownership-transfer-request.component.html',
  styleUrls: ['bie-accept-ownership-transfer-request.component.css']
})
export class BieAcceptOwnershipTransferRequestComponent implements OnInit {

  loading: boolean;

  constructor(private service: BieListService,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar) {
  }

  ngOnInit(): void {
    this.loading = true;

    const topLevelAsbiepId = Number(this.route.snapshot.queryParamMap.get('topLevelAsbiepId'));
    const targetLoginId = this.route.snapshot.queryParamMap.get('targetLoginId');

    let href = window.location.href;
    href = href.substring(0, href.indexOf('/profile_bie'));

    this.service.getBieListByTopLevelAsbiepId(topLevelAsbiepId).subscribe(bie => {
      this.service.transferOwnership(topLevelAsbiepId, targetLoginId, true, {
        bie_link: href + '/profile_bie/' + topLevelAsbiepId,
        bie_name: bie.den,
      }).subscribe(_ => {
        this.snackBar.open('Transferred', '', {
          duration: 3000,
        });

        this.loading = false;
        this.router.navigateByUrl('/profile_bie');
      }, error => {
        this.loading = false;
        this.router.navigateByUrl('/profile_bie');
      });
    });
  }

}
