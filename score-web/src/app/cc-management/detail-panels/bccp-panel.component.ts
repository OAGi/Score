import {AsyncPipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, EventEmitter, inject, Input, Output} from '@angular/core';
import {FormControl, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatTooltipModule} from '@angular/material/tooltip';
import {TranslatePipe} from '@ngx-translate/core';
import {Observable} from 'rxjs';
import {trim} from '../../common/utility';
import {MaterialModule} from '../../material.module';
import {NamespaceSummary} from '../../namespace-management/domain/namespace';
import {BccpNodeInfo} from '../domain/core-component-node';
import {GithubIntegrationService} from '../domain/github-integration.service';
import {GithubIssueBoxComponent} from '../github-issue-box/github-issue-box.component';
import {selectableNamespaces} from './selectable-namespaces';

/** Which fields of the BCCP panel the screen lets the user change. Every field is read-only unless the screen says so. */
export interface BccpPanelEditable {
  propertyTerm?: boolean;
  nillable?: boolean;
  deprecated?: boolean;
  /** The Value Constraint type and the Fixed Value. */
  valueConstraint?: boolean;
  defaultValue?: boolean;
  /** Pick the namespace from the searchable list (`namespaceFilter`, `filteredNamespaces`). */
  namespace?: boolean;
  /** Definition Source and Definition. */
  definition?: boolean;
}

/** The detail of a BCCP node (core component screens). The screen decides what is editable (`editable`). */
@Component({
  selector: 'score-cc-bccp-panel',
  templateUrl: './bccp-panel.component.html',
  styleUrl: './cc-panel.css',
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [AsyncPipe, FormsModule, ReactiveFormsModule, MaterialModule, MatTooltipModule, TranslatePipe, GithubIssueBoxComponent]
})
export class BccpPanelComponent {

  protected githubService = inject(GithubIntegrationService);
  protected readonly trim = trim;

  @Input({required: true}) detail: BccpNodeInfo;
  /** State and access of the root node of the screen. */
  @Input() state = '';
  @Input() access = '';
  @Input() currentUser = '';
  @Input() editable: BccpPanelEditable = {};
  /** The Fixed Value option cannot be picked (it was not allowed in the previous revision). */
  @Input() fixedOptionDisabled = false;
  @Input() namespaces: NamespaceSummary[] = [];
  @Input() isDeveloper = false;
  @Input() namespaceFilter: FormControl | undefined;
  @Input() filteredNamespaces: Observable<NamespaceSummary[]> | undefined;
  /** Whether the GitHub issues of this component can be edited. */
  @Input() githubEditable = false;
  @Input() canCopyDefinition = true;

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
