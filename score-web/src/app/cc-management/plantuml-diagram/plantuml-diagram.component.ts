import {Component, OnInit} from '@angular/core';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import {CcNodeService} from '../domain/core-component-node.service';
import {switchMap} from 'rxjs/operators';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {PlantUmlService} from './domain/plantuml.service';

@Component({
  selector: 'score-plantuml-diagram',
  templateUrl: './plantuml-diagram.component.html',
  styleUrls: ['./plantuml-diagram.component.css']
})
export class PlantUmlDiagramComponent implements OnInit {

  loading: boolean = false;
  manifestId: number;

  public plantUmlTxt: string;
  public sanitizedSvgContent: SafeHtml;

  constructor(private ccService: CcNodeService,
              private plantUmlService: PlantUmlService,
              private router: Router,
              private route: ActivatedRoute,
              private sanitizer: DomSanitizer) {
    // Sanitize the SVG content to render it correctly
  }

  ngOnInit() {
    this.loading = true;

    this.route.paramMap.pipe(
        switchMap((params: ParamMap) => {
          this.manifestId = parseInt(params.get('manifestId'), 10);
          return this.ccService.getAsccpNodePlantUml(this.manifestId, {
            asccpLinkTemplate: '/core_component/browser/asccp/{manifestId}/plantuml',
            bccpLinkTemplate: '/core_component/bccp/{manifestId}'
          });
        }))
        .subscribe(resp => {

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
