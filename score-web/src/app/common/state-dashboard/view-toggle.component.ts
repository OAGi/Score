import {ChangeDetectionStrategy, Component, EventEmitter, Input, Output} from '@angular/core';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {FaIconComponent} from '@fortawesome/angular-fontawesome';
import {faChartColumn, faTable} from '@fortawesome/free-solid-svg-icons';
import {MatTooltipModule} from '@angular/material/tooltip';

export type UsersView = 'chart' | 'table';

/** Switches a panel between its chart and its table. */
@Component({
  selector: 'score-view-toggle',
  standalone: true,
  imports: [MatButtonToggleModule, FaIconComponent, MatTooltipModule],
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <mat-button-toggle-group hideSingleSelectionIndicator aria-label="View"
                             [value]="value" (change)="valueChange.emit($event.value)">
      <mat-button-toggle value="chart" aria-label="Chart" matTooltip="Chart">
        <fa-icon [icon]="chartIcon"></fa-icon>
      </mat-button-toggle>
      <mat-button-toggle value="table" aria-label="Table" matTooltip="Table">
        <fa-icon [icon]="tableIcon"></fa-icon>
      </mat-button-toggle>
    </mat-button-toggle-group>
  `,
  styles: [`
    mat-button-toggle-group {
      --mat-standard-button-toggle-height: 28px;
      --mat-standard-button-toggle-shape: 14px;
    }

    fa-icon {
      font-size: 14px;
      line-height: 1;
    }

    /* Each toggle is a square as wide as the group is high. */
    mat-button-toggle {
      width: 28px;
      height: 28px;
    }

    mat-button-toggle ::ng-deep .mat-button-toggle-button {
      width: 100%;
      height: 100%;
    }

    mat-button-toggle ::ng-deep .mat-button-toggle-label-content {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 100%;
      padding: 0;
      line-height: 1;
    }
  `]
})
export class ViewToggleComponent {
  readonly chartIcon = faChartColumn;
  readonly tableIcon = faTable;

  @Input() value: UsersView = 'chart';
  @Output() valueChange = new EventEmitter<UsersView>();
}
