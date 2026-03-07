import {Component, HostListener, OnDestroy, OnInit} from '@angular/core';
import {AuthService} from '../../authentication/auth.service';
import {SettingsApplicationSettingsService} from './domain/settings-application-settings.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {WebPageInfo} from '../../basis/about/domain/about';
import {DomSanitizer, SafeHtml, SafeResourceUrl} from '@angular/platform-browser';
import {WebPageInfoService} from '../../basis/basis.service';
import {EMPTY, forkJoin, of, Subject} from 'rxjs';
import {catchError, debounceTime, distinctUntilChanged, map, switchMap, takeUntil} from 'rxjs/operators';
import {ApplicationSettingsInfo} from './domain/application-settings';
import {MailService} from '../../common/score-mail.service';
import {HttpErrorResponse} from '@angular/common/http';

@Component({
  selector: 'score-settings-application-settings',
  templateUrl: './settings-application-settings.component.html',
  styleUrls: ['./settings-application-settings.component.css']
})
export class SettingsApplicationSettingsComponent implements OnInit, OnDestroy {

  applicationSettingsInfo: ApplicationSettingsInfo;
  webPageInfo: WebPageInfo;

  title = 'Application settings';
  loading = false;
  bieSchemaSampleFilename = '';
  bieSchemaSampleDuplicateFilename = '';
  biePackageSchemaSampleFilename = '';
  biePackageSchemaSampleDuplicateFilename = '';
  bieSchemaFilenameExpressionError = '';
  biePackageSchemaFilenameExpressionError = '';
  private readonly destroy$ = new Subject<void>();
  private readonly bieSchemaPreview$ = new Subject<FilenameExpressionPreviewRequest>();
  private readonly biePackageSchemaPreview$ = new Subject<FilenameExpressionPreviewRequest>();

  constructor(private auth: AuthService,
              private sanitizer: DomSanitizer,
              private settingsService: SettingsApplicationSettingsService,
              private mailService: MailService,
              private confirmDialogService: ConfirmDialogService,
              private webPageInfoService: WebPageInfoService,
              private snackBar: MatSnackBar) {
  }

  ngOnInit() {
    this.initializeFilenamePreviewStreams();
    this.loading = true;
    forkJoin([
      this.settingsService.load(),
      this.webPageInfoService.load()
    ]).subscribe(([applicationSettingsInfoResp, webPageInfoResp]) => {
      this.applicationSettingsInfo = applicationSettingsInfoResp;
      this.webPageInfo = new WebPageInfo(webPageInfoResp);
      this.queueBieSchemaFilenamePreview();
      this.queueBiePackageSchemaFilenamePreview();

      this.loading = false;
    }, error => {
      this.loading = false;
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  safetHtml(str: string): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(str);
  }

  safeResourceUrl(href: string): SafeResourceUrl {
    return this.sanitizer.bypassSecurityTrustResourceUrl(href);
  }

  get isTenantEnabled(): boolean {
    const userToken = this.auth.getUserToken();
    return userToken.tenant.enabled;
  }

  get isBusinessTermEnabled(): boolean {
    const userToken = this.auth.getUserToken();
    return userToken.businessTerm.enabled;
  }

  get isBIEInverseModeEnabled(): boolean {
    const userToken = this.auth.getUserToken();
    return userToken.bie.inverseMode;
  }

  get isFunctionsRequiringEmailTransmissionEnabled(): boolean {
    const userToken = this.auth.getUserToken();
    return userToken.functionsRequiringEmailTransmission.enabled;
  }

  get isBrowseStandardModeEnabled(): boolean {
    const userToken = this.auth.getUserToken();
    return userToken.browseStandardMode.enabled;
  }

  updateTenantConfiguration(value: boolean) {
    const dialogConfig = this.confirmDialogService.newConfig();
    if (value) {
      dialogConfig.data.header = 'Enable multi-tenant mode?';
      dialogConfig.data.content = ['Are you sure you want to switch Score instance to multi-tenant mode?',
        'In the multi-tenant mode, some functionalities such as creating user extensions, BIE reuse, ',
        'BIE uplifting, and business term function will be unavailable.'];
      dialogConfig.data.action = 'Enable';
    } else {
      dialogConfig.data.header = 'Disable multi-tenant mode?';
      dialogConfig.data.content = ['Are you sure you want to switch Score instance to single tenant mode?',
        'You may lose some data in regard to tenants.'];
      dialogConfig.data.action = 'Disable';
    }

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.settingsService.updateTenantConfiguration(value).subscribe(_ => {
            this.auth.reloadUserToken().subscribe(userToken => {
              if (userToken.tenant.enabled === value) {
                this.snackBar.open('Updated', '', {
                  duration: 3000,
                });
              }
            });
          });
        }
      });
  }

  updateBusinessTermConfiguration(value: boolean) {
    const dialogConfig = this.confirmDialogService.newConfig();
    if (value) {
      dialogConfig.data.header = 'Enable business term function?';
      dialogConfig.data.content = ['Are you sure you want to enable the business term function?',
        'You may lose legacy business term data in BIEs.'];
      dialogConfig.data.action = 'Enable';
    } else {
      dialogConfig.data.header = 'Disable business term function?';
      dialogConfig.data.content = ['Are you sure you want to disable the business term function?',
        'You may lose some data in regard to business terms.'];
      dialogConfig.data.action = 'Disable';
    }

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.settingsService.updateBusinessTermConfiguration(value).subscribe(_ => {
            this.auth.reloadUserToken().subscribe(userToken => {
              if (userToken.businessTerm.enabled === value) {
                this.snackBar.open('Updated', '', {
                  duration: 3000,
                });
              }
            });
          });
        }
      });
  }

  updateBIEInverseModeConfiguration(value: boolean) {
    const dialogConfig = this.confirmDialogService.newConfig();
    if (value) {
      dialogConfig.data.header = 'Enable BIE inverse mode?';
      dialogConfig.data.content = ['Are you sure you want to enable the BIE inverse mode?',
        'The BIE expression could be failed if system does not have enough memory to express a large size of BIEs.'];
      dialogConfig.data.action = 'Enable';
    } else {
      dialogConfig.data.header = 'Disable BIE inverse mode?';
      dialogConfig.data.content = ['Are you sure you want to disable the BIE inverse mode?'];
      dialogConfig.data.action = 'Disable';
    }

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.settingsService.updateBIEInverseModeConfiguration(value).subscribe(_ => {
            this.auth.reloadUserToken().subscribe(userToken => {
              if (userToken.bie.inverseMode === value) {
                this.snackBar.open('Updated', '', {
                  duration: 3000,
                });
              }
            });
          });
        }
      });
  }

  updateFunctionsRequiringEmailTransmissionConfiguration(value: boolean) {
    const dialogConfig = this.confirmDialogService.newConfig();
    if (value) {
      dialogConfig.data.header = 'Enable email-based workflows?';
      dialogConfig.data.content = ['Are you sure you want to enable email-based workflows?',
        'If SMTP settings are incorrect, these functions may not operate properly.'];
      dialogConfig.data.action = 'Enable';
    } else {
      dialogConfig.data.header = 'Disable email-based workflows?';
      dialogConfig.data.content = ['Are you sure you want to disable email-based workflows?'];
      dialogConfig.data.action = 'Disable';
    }

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.settingsService.updateFunctionsRequiringEmailTransmissionConfiguration(value).subscribe(_ => {
            this.auth.reloadUserToken().subscribe(userToken => {
              if (userToken.functionsRequiringEmailTransmission.enabled === value) {
                this.snackBar.open('Updated', '', {
                  duration: 3000,
                });
              }
            });
          });
        }
      });
  }

  updateBrowseStandardModeConfiguration(value: boolean) {
    const dialogConfig = this.confirmDialogService.newConfig();
    if (value) {
      dialogConfig.data.header = 'Enable Browse Standard mode?';
      dialogConfig.data.content = ['Are you sure you want to enable Browse Standard mode for end-users?',
        'When enabled, end-users will use Browse Standard view.'];
      dialogConfig.data.action = 'Enable';
    } else {
      dialogConfig.data.header = 'Disable Browse Standard mode?';
      dialogConfig.data.content = ['Are you sure you want to disable Browse Standard mode?',
        'When disabled, end-users will use the regular Core Component menu.'];
      dialogConfig.data.action = 'Disable';
    }

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.settingsService.updateBrowseStandardModeConfiguration(value).subscribe(_ => {
            this.auth.reloadUserToken().subscribe(userToken => {
              if (userToken.browseStandardMode.enabled === value) {
                this.snackBar.open('Updated', '', {
                  duration: 3000,
                });
              }
            });
          });
        }
      });
  }

  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent($event: KeyboardEvent) {
    const charCode = $event.key?.toLowerCase();

    // Handle 'Ctrl/Command+S'
    const metaOrCtrlKeyPressed = $event.metaKey || $event.ctrlKey;
    if (metaOrCtrlKeyPressed && charCode === 's') {
      $event.preventDefault();
      $event.stopPropagation();

      this.updateWebPageInfo();
    }
  }

  updateWebPageInfo() {
    this.webPageInfoService.update(this.webPageInfo).subscribe(_ => {
      this.webPageInfoService.set(this.webPageInfo);

      this.snackBar.open('Updated', '', {
        duration: 3000,
      });
    });
  }

  updateApplicationSettingsInfo() {
    this.settingsService.update({
      smtpSettingsInfo: this.applicationSettingsInfo.smtpSettingsInfo
    } as ApplicationSettingsInfo).subscribe(_ => {
      this.snackBar.open('Updated', '', {
        duration: 3000,
      });
    });
  }

  updateBieFilenameExpressions() {
    const bieSchemaFilenameExpression = this.applicationSettingsInfo?.bieSchemaFilenameExpression || '';
    const biePackageSchemaFilenameExpression = this.applicationSettingsInfo?.biePackageSchemaFilenameExpression || '';
    const bieSchemaFilenameDuplicateHandlerExpression =
      this.applicationSettingsInfo?.bieSchemaFilenameDuplicateHandlerExpression || '';
    const biePackageSchemaFilenameDuplicateHandlerExpression =
      this.applicationSettingsInfo?.biePackageSchemaFilenameDuplicateHandlerExpression || '';
    forkJoin([
      this.settingsService.validateFilenameExpression(
        'bie-schema',
        bieSchemaFilenameExpression,
        bieSchemaFilenameDuplicateHandlerExpression
      ).pipe(
        map(() => {
          this.setFilenameExpressionError('bie-schema', '');
          return true;
        }),
        catchError(error => {
          this.setFilenamePreview('bie-schema', '', '');
          this.setFilenameExpressionError('bie-schema', this.getFilenameExpressionErrorMessage(error));
          return of(false);
        })
      ),
      this.settingsService.validateFilenameExpression(
        'bie-package-schema',
        biePackageSchemaFilenameExpression,
        biePackageSchemaFilenameDuplicateHandlerExpression
      ).pipe(
        map(() => {
          this.setFilenameExpressionError('bie-package-schema', '');
          return true;
        }),
        catchError(error => {
          this.setFilenamePreview('bie-package-schema', '', '');
          this.setFilenameExpressionError('bie-package-schema', this.getFilenameExpressionErrorMessage(error));
          return of(false);
        })
      )
    ]).subscribe(([isBieSchemaValid, isBiePackageSchemaValid]) => {
      if (!isBieSchemaValid || !isBiePackageSchemaValid) {
        return;
      }

      this.settingsService.updateBieFilenameExpressions(
        bieSchemaFilenameExpression,
        biePackageSchemaFilenameExpression,
        bieSchemaFilenameDuplicateHandlerExpression,
        biePackageSchemaFilenameDuplicateHandlerExpression
      ).subscribe(__ => {
        this.snackBar.open('Updated', '', {
          duration: 3000,
        });
      });
    });
  }

  testEmailSettings() {
    this.loading = true;

    this.mailService.sendMail('test', this.auth.getUserToken().username, {}).subscribe(_ => {
      this.snackBar.open('The test message has been sent.', '', {
        duration: 3000,
      });
      this.loading = false;
    }, error => {
      this.loading = false;
    });
  }

  queueBieSchemaFilenamePreview() {
    this.setFilenameExpressionError('bie-schema', '');
    this.bieSchemaPreview$.next(this.getBieSchemaFilenamePreviewRequest());
  }

  queueBiePackageSchemaFilenamePreview() {
    this.setFilenameExpressionError('bie-package-schema', '');
    this.biePackageSchemaPreview$.next(this.getBiePackageSchemaFilenamePreviewRequest());
  }

  private initializeFilenamePreviewStreams() {
    this.bieSchemaPreview$.pipe(
      debounceTime(300),
      distinctUntilChanged(isSameFilenameExpressionPreviewRequest),
      switchMap(request => this.settingsService.previewFilenameExpression(
        'bie-schema',
        request.expression,
        request.duplicateHandlerExpression
      ).pipe(
        catchError(error => {
          this.setFilenamePreview('bie-schema', '', '');
          this.setFilenameExpressionError('bie-schema', this.getFilenameExpressionErrorMessage(error));
          return EMPTY;
        })
      )),
      takeUntil(this.destroy$)
    ).subscribe(preview => {
      this.setFilenamePreview('bie-schema', preview.sampleFilename, preview.sampleDuplicateFilename);
      this.setFilenameExpressionError('bie-schema', '');
    });

    this.biePackageSchemaPreview$.pipe(
      debounceTime(300),
      distinctUntilChanged(isSameFilenameExpressionPreviewRequest),
      switchMap(request => this.settingsService.previewFilenameExpression(
        'bie-package-schema',
        request.expression,
        request.duplicateHandlerExpression
      ).pipe(
        catchError(error => {
          this.setFilenamePreview('bie-package-schema', '', '');
          this.setFilenameExpressionError('bie-package-schema', this.getFilenameExpressionErrorMessage(error));
          return EMPTY;
        })
      )),
      takeUntil(this.destroy$)
    ).subscribe(preview => {
      this.setFilenamePreview('bie-package-schema', preview.sampleFilename, preview.sampleDuplicateFilename);
      this.setFilenameExpressionError('bie-package-schema', '');
    });
  }

  private getBieSchemaFilenamePreviewRequest(): FilenameExpressionPreviewRequest {
    return {
      expression: this.applicationSettingsInfo?.bieSchemaFilenameExpression || '',
      duplicateHandlerExpression: this.applicationSettingsInfo?.bieSchemaFilenameDuplicateHandlerExpression || ''
    };
  }

  private getBiePackageSchemaFilenamePreviewRequest(): FilenameExpressionPreviewRequest {
    return {
      expression: this.applicationSettingsInfo?.biePackageSchemaFilenameExpression || '',
      duplicateHandlerExpression: this.applicationSettingsInfo?.biePackageSchemaFilenameDuplicateHandlerExpression || ''
    };
  }

  private setFilenamePreview(type: 'bie-schema' | 'bie-package-schema',
                             sampleFilename: string,
                             sampleDuplicateFilename: string) {
    if (type === 'bie-schema') {
      this.bieSchemaSampleFilename = sampleFilename;
      this.bieSchemaSampleDuplicateFilename = sampleDuplicateFilename;
      return;
    }

    this.biePackageSchemaSampleFilename = sampleFilename;
    this.biePackageSchemaSampleDuplicateFilename = sampleDuplicateFilename;
  }

  private setFilenameExpressionError(type: 'bie-schema' | 'bie-package-schema', message: string) {
    if (type === 'bie-schema') {
      this.bieSchemaFilenameExpressionError = message;
      return;
    }

    this.biePackageSchemaFilenameExpressionError = message;
  }

  private getFilenameExpressionErrorMessage(error: HttpErrorResponse): string {
    return error?.headers?.get('x-error-message')
      || error?.error?.message
      || error?.message
      || 'Invalid filename expression.';
  }

}

interface FilenameExpressionPreviewRequest {
  expression: string;
  duplicateHandlerExpression: string;
}

function isSameFilenameExpressionPreviewRequest(
  previous: FilenameExpressionPreviewRequest,
  current: FilenameExpressionPreviewRequest
): boolean {
  return previous.expression === current.expression
    && previous.duplicateHandlerExpression === current.duplicateHandlerExpression;
}
