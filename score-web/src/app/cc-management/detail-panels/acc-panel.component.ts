import {AsyncPipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, EventEmitter, inject, Input, Output} from '@angular/core';
import {FormControl, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatTooltipModule} from '@angular/material/tooltip';
import {TranslatePipe} from '@ngx-translate/core';
import {Observable} from 'rxjs';
import {trim} from '../../common/utility';
import {MaterialModule} from '../../material.module';
import {NamespaceSummary} from '../../namespace-management/domain/namespace';
import {CcAccNodeInfo, OagisComponentType, OagisComponentTypes} from '../domain/core-component-node';
import {GithubIntegrationService} from '../domain/github-integration.service';
import {GithubIssueBoxComponent} from '../github-issue-box/github-issue-box.component';
import {selectableNamespaces} from './selectable-namespaces';

/** Which fields of the ACC panel the screen lets the user change. Every field is read-only unless the screen says so. */
export interface AccPanelEditable {
  objectClassTerm?: boolean;
  componentType?: boolean;
  abstract?: boolean;
  deprecated?: boolean;
  /** Pick the namespace from the searchable list (`namespaceFilter`, `filteredNamespaces`). */
  namespace?: boolean;
  /** Definition Source and Definition. */
  definition?: boolean;
}

/**
 * The detail of an ACC node (core component screens).
 *
 * The screen decides what is editable (`editable`) and brings the generate / undo buttons, which depend on its AI
 * state: put them into `[nameActions]` and `[definitionActions]`.
 */
@Component({
  selector: 'score-cc-acc-panel',
  templateUrl: './acc-panel.component.html',
  styleUrl: './cc-panel.css',
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [AsyncPipe, FormsModule, ReactiveFormsModule, MaterialModule, MatTooltipModule, TranslatePipe, GithubIssueBoxComponent]
})
export class AccPanelComponent {

  protected githubService = inject(GithubIntegrationService);
  protected readonly trim = trim;
  protected readonly componentTypes: OagisComponentType[] = OagisComponentTypes;

  @Input({required: true}) detail: CcAccNodeInfo;
  /** State and access of the root node of the screen. */
  @Input() state = '';
  @Input() access = '';
  @Input() currentUser = '';
  @Input() editable: AccPanelEditable = {};
  /** The Component Type options the screen allows to pick, and to show at all. */
  @Input() isValidAttributeGroup: (componentType: string) => boolean = () => true;
  @Input() componentTypeAble: (componentType: string) => boolean = () => true;
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
