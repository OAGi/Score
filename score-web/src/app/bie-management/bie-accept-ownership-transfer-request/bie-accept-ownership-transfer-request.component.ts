import { Component, OnInit, inject, ChangeDetectionStrategy } from '@angular/core';
import {BieListService} from '../bie-list/domain/bie-list.service';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  standalone: false,
  selector: 'score-bie-accept-ownership-transfer-request',
  templateUrl: 'bie-accept-ownership-transfer-request.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['bie-accept-ownership-transfer-request.component.css']
})
export class BieAcceptOwnershipTransferRequestComponent implements OnInit {
  private service = inject(BieListService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);


  loading: boolean;

  ngOnInit(): void {
    this.loading = true;

    const topLevelAsbiepId = Number(this.route.snapshot.queryParamMap.get('topLevelAsbiepId'));
    const targetLoginId = this.route.snapshot.queryParamMap.get('targetLoginId');

    let href = window.location.href;
    href = href.substring(0, href.indexOf('/profile_bie'));

    this.service.getBieListByTopLevelAsbiepId(topLevelAsbiepId).subscribe(bie => {
      if (!bie) {
        this.onBieUnavailable();
        return;
      }

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
    }, err => {
      this.loading = false;
      this.snackBar.open(err.status === 403 ? 'You do not have access permission.' : 'Something\'s wrong.', '', {duration: 3000});
      this.router.navigateByUrl('/profile_bie');
    });
  }

  private onBieUnavailable() {
    this.loading = false;
    this.snackBar.open('The requested BIE is unavailable.', '', {duration: 3000});
    this.router.navigateByUrl('/profile_bie');
  }

}
