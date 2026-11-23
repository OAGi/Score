import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import {map, Observable} from 'rxjs';
import {projectVersion} from '../../../environments/version';
import {WebPageInfoService} from '../basis.service';

/**
 * The page frame shared by the sign-in, pending and disabled screens: the navy hero on the left and the white panel
 * (administrator's brand logo, then the projected content) on the right.
 */
@Component({
  standalone: false,
  selector: 'score-auth-shell',
  templateUrl: './auth-shell.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./auth-shell.component.css']
})
export class AuthShellComponent {
  private webPageInfoService = inject(WebPageInfoService);
  private sanitizer = inject(DomSanitizer);

  version = projectVersion;

  /** The administrator's brand logo (SVG markup); trusted like the navbar's, as only administrators can set it. */
  brand: Observable<SafeHtml | undefined> = this.webPageInfoService.load().pipe(
    map(info => info?.brand ? this.sanitizer.bypassSecurityTrustHtml(info.brand) : undefined));
}
