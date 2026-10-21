import {AsyncPipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, EventEmitter, inject, Input, Output} from '@angular/core';
import {FormControl, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatTooltipModule} from '@angular/material/tooltip';
import {TranslatePipe} from '@ngx-translate/core';
import {Observable} from 'rxjs';
import {trim} from '../../common/utility';
import {MaterialModule} from '../../material.module';
import {NamespaceSummary} from '../../namespace-management/domain/namespace';
import {CcDtNodeInfo} from '../domain/core-component-node';
import {GithubIntegrationService} from '../domain/github-integration.service';
import {GithubIssueBoxComponent} from '../github-issue-box/github-issue-box.component';
import {selectableNamespaces} from './selectable-namespaces';

/** Which fields of the BDT panel the screen lets the user change. Every field is read-only unless the screen says so. */
export interface BdtPanelEditable {
  /** Qualifier and the Six Hexadecimal Identifier. */
  qualifier?: boolean;
  /** Pick the namespace from the searchable list (`namespaceFilter`, `filteredNamespaces`). */
  namespace?: boolean;
  /** Definition Source, Definition and Content Component Definition. */
  definition?: boolean;
}

/**
 * The detail of a Data Type node on the BDT screen, which edits it. The screen decides what is editable (`editable`)
 * and projects the `score-cc-value-domain` it owns the data of.
 */
@Component({
  selector: 'score-cc-bdt-panel',
  templateUrl: './bdt-panel.component.html',
  styleUrl: './cc-panel.css',
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [AsyncPipe, FormsModule, ReactiveFormsModule, MaterialModule, MatTooltipModule, TranslatePipe, GithubIssueBoxComponent]
})
export class BdtPanelComponent {

  protected githubService = inject(GithubIntegrationService);
  protected readonly trim = trim;

  @Input({required: true}) detail: CcDtNodeInfo;
  /** State and access of the root node of the screen. */
  @Input() state = '';
  @Input() access = '';
  @Input() currentUser = '';
  @Input() editable: BdtPanelEditable = {};
  /** The Data Type has a previous revision. */
  @Input() hasRevision = false;
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

  isEmpty(str: string): boolean {
    return str === undefined || str === null || str.trim() === '';
  }

  randSixDigitId(): string {
    return (Math.random() + 1).toString(36).substring(2, 8).toUpperCase();
  }

  copyToDefinition(text: string) {
    this.definitionCopy.emit(text);
  }
}
