import {AsyncPipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, EventEmitter, inject, Input, Output} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {MatTooltipModule} from '@angular/material/tooltip';
import {TranslatePipe} from '@ngx-translate/core';
import {MaterialModule} from '../../material.module';
import {NamespaceSummary} from '../../namespace-management/domain/namespace';
import {DtNodeInfo} from '../domain/core-component-node';
import {GithubIntegrationService} from '../domain/github-integration.service';
import {GithubIssueBoxComponent} from '../github-issue-box/github-issue-box.component';
import {selectableNamespaces} from './selectable-namespaces';

/** The read-only detail of the Data Type under a BCCP node (core component screens). */
@Component({
  selector: 'score-cc-dt-panel',
  templateUrl: './dt-panel.component.html',
  styleUrl: './cc-panel.css',
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [AsyncPipe, FormsModule, MaterialModule, MatTooltipModule, TranslatePipe, GithubIssueBoxComponent]
})
export class DtPanelComponent {

  protected githubService = inject(GithubIntegrationService);

  @Input({required: true}) detail: DtNodeInfo;
  /** State and access of the root node of the screen. */
  @Input() state = '';
  @Input() access = '';
  @Input() currentUser = '';
  @Input() namespaces: NamespaceSummary[] = [];
  @Input() isDeveloper = false;

  /** The definition to copy onto the root node's definition. */
  @Output() definitionCopy = new EventEmitter<string>();

  getSelectableNamespaces(namespaceId?: number): NamespaceSummary[] {
    return selectableNamespaces(this.namespaces, this.isDeveloper, namespaceId);
  }

  openInNewTab(url: string) {
    window.open(url, '_blank');
  }

  copyToDefinition(text: string) {
    this.definitionCopy.emit(text);
  }
}
