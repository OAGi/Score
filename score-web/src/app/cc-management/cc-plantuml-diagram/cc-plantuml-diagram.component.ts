import {Component, OnInit} from '@angular/core';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import {CcNodeService} from '../domain/core-component-node.service';
import {switchMap} from 'rxjs/operators';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {PlantUmlService} from '../../common/plantuml-diagram/plantuml.service';
import {PreferencesInfo} from '../../settings-management/settings-preferences/domain/preferences';
import {AuthService} from '../../authentication/auth.service';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';
import {forkJoin} from 'rxjs';

@Component({
  standalone: false,
  selector: 'score-cc-plantuml-diagram',
  templateUrl: './cc-plantuml-diagram.component.html',
  styleUrls: ['./cc-plantuml-diagram.component.css']
})
export class CcPlantumlDiagramComponent implements OnInit {

  loading: boolean = false;
  manifestId: number;

  preferencesInfo: PreferencesInfo;

  get browserMode(): boolean {
    if (!this.preferencesInfo) {
      return false;
    }

    return this.preferencesInfo.viewSettingsInfo.pageSettings.browserViewMode;
  }

  public plantUmlTxt: string;
  public sanitizedSvgContent: SafeHtml;

  constructor(private ccService: CcNodeService,
              private plantUmlService: PlantUmlService,
              private auth: AuthService,
              private preferencesService: SettingsPreferencesService,
              private router: Router,
              private route: ActivatedRoute,
              private sanitizer: DomSanitizer) {
    // Sanitize the SVG content to render it correctly
  }

  ngOnInit() {
    this.loading = true;

    forkJoin([
      this.preferencesService.load(this.auth.getUserToken())
    ]).subscribe(([preferencesInfo]) => {
      this.preferencesInfo = preferencesInfo;

      this.route.paramMap.pipe(
          switchMap((params: ParamMap) => {
            this.manifestId = parseInt(params.get('manifestId'), 10);
            return this.ccService.getAsccpNodePlantUml(this.manifestId, {
              asccpLinkTemplate: '/core_component' + (this.browserMode ? '/browser' : '') + '/asccp/{manifestId}/plantuml',
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
    });
  }
}
