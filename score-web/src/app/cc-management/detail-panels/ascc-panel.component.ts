import {ChangeDetectionStrategy, Component, EventEmitter, Input, Output} from '@angular/core';
import {FormControl, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatTooltipModule} from '@angular/material/tooltip';
import {TranslatePipe} from '@ngx-translate/core';
import {ScoreCommonModule} from '../../common/score-common.module';
import {trim} from '../../common/utility';
import {MaterialModule} from '../../material.module';
import {AsccNodeInfo} from '../domain/core-component-node';

/**
 * The detail of an ASCC node (core component screens).
 * The cardinality controls belong to the screen (it validates them and keeps the tree in step); without them the cardinality is shown read-only.
 */
@Component({
  selector: 'score-cc-ascc-panel',
  templateUrl: './ascc-panel.component.html',
  styleUrl: './cc-panel.css',
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [FormsModule, ReactiveFormsModule, MaterialModule, MatTooltipModule, TranslatePipe, ScoreCommonModule]
})
export class AsccPanelComponent {

  protected readonly trim = trim;

  @Input({required: true}) detail: AsccNodeInfo;
  /** State and access of the root node of the screen. */
  @Input() state = '';
  @Input() access = '';
  /** Whether the fields can be edited. */
  @Input() editable = false;
  @Input() deprecatable = false;
  @Input() canCopyDefinition = true;
  @Input() cardinalityMin: FormControl | undefined;
  @Input() cardinalityMax: FormControl | undefined;

  /** The definition to copy onto the root node's definition. */
  @Output() definitionCopy = new EventEmitter<string>();

  copyToDefinition(text: string) {
    this.definitionCopy.emit(text);
  }
}
