import { Component, OnInit, inject } from '@angular/core';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import {ActivatedRoute, Router} from '@angular/router';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {PlantUmlService} from '../../common/plantuml-diagram/plantuml.service';
import {ReleaseService} from '../domain/release.service';

@Component({
  standalone: false,
  selector: 'score-release-diagram-dialog',
  templateUrl: './release-diagram-dialog.component.html',
  styleUrls: ['./release-diagram-dialog.component.css']
})
export class ReleaseDiagramDialogComponent implements OnInit {
  dialogRef = inject<MatDialogRef<ReleaseDiagramDialogComponent>>(MatDialogRef);
  private service = inject(ReleaseService);
  private plantUmlService = inject(PlantUmlService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private sanitizer = inject(DomSanitizer);
  data = inject(MAT_DIALOG_DATA);


  loading: boolean = false;
  releaseId: number;

  public plantUmlTxt: string;
  public sanitizedSvgContent: SafeHtml;

  constructor() {
    const data = this.data;

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
