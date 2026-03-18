import { Component, OnInit, inject } from '@angular/core';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import {ActivatedRoute, Router} from '@angular/router';
import {BieListService} from '../bie-list/domain/bie-list.service';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {PlantUmlService} from '../../common/plantuml-diagram/plantuml.service';

@Component({
  standalone: false,
  selector: 'score-bie-diagram-dialog',
  templateUrl: './bie-diagram-dialog.component.html',
  styleUrls: ['./bie-diagram-dialog.component.css']
})
export class BieDiagramDialogComponent implements OnInit {
  dialogRef = inject<MatDialogRef<BieDiagramDialogComponent>>(MatDialogRef);
  private service = inject(BieListService);
  private plantUmlService = inject(PlantUmlService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private sanitizer = inject(DomSanitizer);
  data = inject(MAT_DIALOG_DATA);


  loading: boolean = false;
  topLevelAsbiepId: number;

  public plantUmlTxt: string;
  public sanitizedSvgContent: SafeHtml;

  constructor() {
    const data = this.data;

    // Sanitize the SVG content to render it correctly

    this.topLevelAsbiepId = data.topLevelAsbiepId;
  }

  ngOnInit() {
    this.loading = true;

    this.service.getPlantUml(this.topLevelAsbiepId, {
      topLevelAsbiepLinkTemplate: '/profile_bie/{topLevelAsbiepId}'
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
