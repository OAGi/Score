import {ChangeDetectionStrategy, Component, EventEmitter, Input, Output} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {MatTooltipModule} from '@angular/material/tooltip';
import {TranslatePipe} from '@ngx-translate/core';
import {MaterialModule} from '../../material.module';
import {ScoreCommonModule} from '../../common/score-common.module';
import {CcDtScNodeInfo} from '../domain/core-component-node';

/** The read-only detail of a Data Type Supplementary Component node (core component screens). */
@Component({
  selector: 'score-cc-dt-sc-panel',
  templateUrl: './dt-sc-panel.component.html',
  styleUrl: './cc-panel.css',
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [FormsModule, MaterialModule, MatTooltipModule, TranslatePipe, ScoreCommonModule]
})
export class DtScPanelComponent {

  @Input({required: true}) detail: CcDtScNodeInfo;
  /** State and access of the root node of the screen. */
  @Input() state = '';
  @Input() access = '';

  /** The definition to copy onto the root node's definition. */
  @Output() definitionCopy = new EventEmitter<string>();

  copyToDefinition(text: string) {
    this.definitionCopy.emit(text);
  }
}
