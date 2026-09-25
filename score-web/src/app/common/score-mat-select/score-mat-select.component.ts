import {booleanAttribute, ChangeDetectionStrategy, Component, Input, ViewEncapsulation} from '@angular/core';
import {OverlayModule} from '@angular/cdk/overlay';
import {MatSelect, MatSelectChange} from '@angular/material/select';
import {MatFormFieldControl} from '@angular/material/form-field';
import {MAT_OPTION_PARENT_COMPONENT} from '@angular/material/core';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';

/** MatSelect with an inline clear action; accepts the same options and forms bindings. */
@Component({
  selector: 'score-mat-select',
  standalone: true,
  imports: [OverlayModule, MatButtonModule, MatIconModule, MatTooltipModule],
  templateUrl: './score-mat-select.component.html',
  styleUrls: ['./mat-select-base.css', './score-mat-select.component.css'],
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {provide: MatSelect, useExisting: ScoreMatSelectComponent},
    {provide: MatFormFieldControl, useExisting: ScoreMatSelectComponent},
    {provide: MAT_OPTION_PARENT_COMPONENT, useExisting: ScoreMatSelectComponent}
  ]
})
export class ScoreMatSelectComponent extends MatSelect {
  @Input({transform: booleanAttribute}) clearable = true;
  @Input() clearLabel = 'Clear selection';

  clearSelection(event: Event): void {
    event.stopPropagation();
    if (this.disabled || !this.clearable || this.empty) {
      return;
    }

    const clearedValue = this.multiple ? [] : null;
    this.writeValue(clearedValue);
    this.valueChange.emit(clearedValue);
    this._onChange(clearedValue);
    this._onTouched();
    this.selectionChange.emit(new MatSelectChange(this, clearedValue));
    this.stateChanges.next();
    this.focus();
  }
}
