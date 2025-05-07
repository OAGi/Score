import {Component, Inject, OnInit} from '@angular/core';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import {ActivatedRoute, Router} from '@angular/router';
import {BieListService} from '../bie-list/domain/bie-list.service';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {PlantUmlService} from '../../cc-management/plantuml-diagram/domain/plantuml.service';

@Component({
  selector: 'score-bie-diagram-dialog',
  templateUrl: './bie-diagram-dialog.component.html',
  styleUrls: ['./bie-diagram-dialog.component.css']
})
export class BieDiagramDialogComponent implements OnInit {

  loading: boolean = false;
  topLevelAsbiepId: number;

  public plantUmlTxt: string;
  public sanitizedSvgContent: SafeHtml;

  constructor(public dialogRef: MatDialogRef<BieDiagramDialogComponent>,
              private service: BieListService,
              private plantUmlService: PlantUmlService,
              private router: Router,
              private route: ActivatedRoute,
              private sanitizer: DomSanitizer,
              @Inject(MAT_DIALOG_DATA) public data: any) {
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
