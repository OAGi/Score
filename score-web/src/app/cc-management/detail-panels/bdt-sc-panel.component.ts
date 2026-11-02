import {ChangeDetectionStrategy, Component, EventEmitter, Input, Output} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {MatTooltipModule} from '@angular/material/tooltip';
import {TranslatePipe} from '@ngx-translate/core';
import {trim} from '../../common/utility';
import {MaterialModule} from '../../material.module';
import {CcDtScNodeInfo} from '../domain/core-component-node';

/** Which fields of the BDT SC panel the screen lets the user change. Every field is read-only unless the screen says so. */
export interface BdtScPanelEditable {
  /** Property Term and Representation Term. */
  terms?: boolean;
  cardinality?: boolean;
  /** The Value Constraint type. */
  valueConstraint?: boolean;
  /** Fixed Value and Default Value. */
  value?: boolean;
  /** Definition Source and Definition. */
  definition?: boolean;
}

/**
 * The detail of a Data Type Supplementary Component node on the BDT screen, which edits it. The screen decides what
 * is editable (`editable`) and projects the `score-cc-value-domain` it owns the data of.
 */
@Component({
  selector: 'score-cc-bdt-sc-panel',
  templateUrl: './bdt-sc-panel.component.html',
  styleUrl: './cc-panel.css',
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [FormsModule, MaterialModule, MatTooltipModule, TranslatePipe]
})
export class BdtScPanelComponent {

  protected readonly trim = trim;

  @Input({required: true}) detail: CcDtScNodeInfo;
  /** State and access of the root node of the screen. */
  @Input() state = '';
  @Input() access = '';
  @Input() editable: BdtScPanelEditable = {};

  /** The definition to copy onto the root node's definition. */
  @Output() definitionCopy = new EventEmitter<string>();
  /** The Representation Term was picked. */
  @Output() representationTermChange = new EventEmitter<void>();

  copyToDefinition(text: string) {
    this.definitionCopy.emit(text);
  }
}
