import {ChangeDetectionStrategy, Component, EventEmitter, Input, Output} from '@angular/core';
import {FormControl, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatTooltipModule} from '@angular/material/tooltip';
import {TranslatePipe} from '@ngx-translate/core';
import {ScoreCommonModule} from '../../common/score-common.module';
import {trim} from '../../common/utility';
import {MaterialModule} from '../../material.module';
import {BccNodeInfo, EntityType, EntityTypes} from '../domain/core-component-node';

/**
 * The detail of a BCC node (core component screens).
 * The cardinality controls belong to the screen (it validates them and keeps the tree in step); without them the cardinality is shown read-only.
 */
@Component({
  selector: 'score-cc-bcc-panel',
  templateUrl: './bcc-panel.component.html',
  styleUrl: './bcc-panel.component.css',
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [FormsModule, ReactiveFormsModule, MaterialModule, MatTooltipModule, TranslatePipe, ScoreCommonModule]
})
export class BccPanelComponent {

  protected readonly trim = trim;
  protected readonly entityTypes: EntityType[] = EntityTypes;

  @Input({required: true}) detail: BccNodeInfo;
  /** State and access of the root node of the screen. */
  @Input() state = '';
  @Input() access = '';
  /** Whether the fields can be edited. */
  @Input() editable = false;
  @Input() deprecatable = false;
  /** The entity type cannot be changed (revision association, or the data type has supplementary components). */
  @Input() entityTypeLocked = false;
  @Input() canCopyDefinition = true;
  @Input() cardinalityMin: FormControl | undefined;
  @Input() cardinalityMax: FormControl | undefined;

  /** The definition to copy onto the root node's definition. */
  @Output() definitionCopy = new EventEmitter<string>();
  @Output() entityTypeChange = new EventEmitter<void>();

  copyToDefinition(text: string) {
    this.definitionCopy.emit(text);
  }
}
