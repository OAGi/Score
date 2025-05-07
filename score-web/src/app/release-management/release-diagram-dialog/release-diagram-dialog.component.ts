import {Component, Inject, OnInit} from '@angular/core';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import {ActivatedRoute, Router} from '@angular/router';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {PlantUmlService} from '../../cc-management/plantuml-diagram/domain/plantuml.service';
import {ReleaseService} from '../domain/release.service';

@Component({
  selector: 'score-release-diagram-dialog',
  templateUrl: './release-diagram-dialog.component.html',
  styleUrls: ['./release-diagram-dialog.component.css']
})
export class ReleaseDiagramDialogComponent implements OnInit {

  loading: boolean = false;
  releaseId: number;

  public plantUmlTxt: string;
  public sanitizedSvgContent: SafeHtml;

  constructor(public dialogRef: MatDialogRef<ReleaseDiagramDialogComponent>,
              private service: ReleaseService,
              private plantUmlService: PlantUmlService,
              private router: Router,
              private route: ActivatedRoute,
              private sanitizer: DomSanitizer,
              @Inject(MAT_DIALOG_DATA) public data: any) {
    // Sanitize the SVG content to render it correctly

    this.releaseId = data.releaseId;
  }

  ngOnInit() {
    this.loading = true;

    this.service.getPlantUml(this.releaseId, {
      releaseLinkTemplate: '/release/{releaseId}',
      libraryLinkTemplate: '/library/{libraryId}'
    }).subscribe(resp => {

      this.plantUmlTxt = resp.text;
      this.plantUmlService.getDiagram(resp.encodedText, 'svg').subscribe(resp => {

        const reader = new FileReader();
        reader.onload = () => {
          const svgContent = reader.result as string;
          this.sanitizedSvgContent = this.sanitizer.bypassSecurityTrustHtml(svgContent);
        };
        reader.readAsText(resp.body);
      });

      this.loading = false;
    }, err => {
      this.loading = false;
    });
  }
}
