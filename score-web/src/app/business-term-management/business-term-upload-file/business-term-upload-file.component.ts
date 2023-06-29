import {Component, OnInit} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {BusinessTermService} from '../domain/business-term.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Subscription} from 'rxjs';
import {HttpEventType} from '@angular/common/http';
import {finalize} from 'rxjs/operators';
import {saveAs} from 'file-saver';

@Component({
  selector: 'score-business-term-create',
  templateUrl: './business-term-upload-file.component.html',
  styleUrls: ['./business-term-upload-file.component.css']
})
export class BusinessTermUploadFileComponent implements OnInit {

  title = 'Upload Business Terms';

  requiredFileType = 'text/csv';

  fileName = '';
  uploadProgress: number;
  uploadSub: Subscription;

  constructor(private service: BusinessTermService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar) {
  }

  ngOnInit() {
  }

  onFileSelected(event) {
    const file: File = event.target.files[0];

    if (file) {
      this.fileName = file.name;
      const formData = new FormData();
      formData.append('file', file);

      const upload$ = this.service.uploadFromFile(formData)
        .pipe(
          finalize(() => this.reset())
        );

      this.uploadSub = upload$.subscribe(event => {
        if (event.type === HttpEventType.UploadProgress) {
          this.uploadProgress = Math.round(100 * (event.loaded / event.total));
        }
        if (event.type === HttpEventType.Response) {
          if (event.status === 204) {
            this.snackBar.open('Uploaded', '', {
              duration: 3000,
            });
            this.router.navigateByUrl('/business_term_management/business_term');
          }
          if (event.status === 200) {
            this.snackBar.open('Uploaded. Some of the business terms already existed.', '', {
              duration: 3000,
            });
            this.router.navigateByUrl('/business_term_management/business_term');
          }
        }
      }, error => {
        const errorMessage = error.headers.get('X-Error-Message') || 'Error occurred.';
        this.snackBar.open(errorMessage, '', {
          duration: 3000,
        });
      });
    }
  }

  cancelUpload() {
    this.uploadSub.unsubscribe();
    this.reset();
  }

  reset() {
    this.uploadProgress = null;
    this.uploadSub = null;
  }

  downloadFile() {
    this.service.downloadCSV().subscribe((buffer) => {
      const data: Blob = new Blob([buffer], {
        type: 'text/csv;charset=utf-8'
      });
      saveAs(data, 'businessTermTemplateWithExample.csv');
    });
  }

}
