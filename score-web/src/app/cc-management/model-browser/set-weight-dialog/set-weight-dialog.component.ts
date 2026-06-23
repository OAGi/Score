import {Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {A11yModule} from '@angular/cdk/a11y';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';

export interface SetWeightDialogData {
  /** Display name (property term) of the sibling being reordered. */
  name: string;
  /** The current order weight, if this sibling already has one. */
  currentWeight?: number;
}

/**
 * Prompts for the integer ORDER WEIGHT of a sibling (Issue #1638).
 *
 * Result contract (via {@code MatDialogRef.close}):
 * <ul>
 *   <li>a {@code number} — set this weight;</li>
 *   <li>{@code null} — reset this sibling to its default (seq_key) position;</li>
 *   <li>{@code undefined} — cancel (no change).</li>
 * </ul>
 */
@Component({
  standalone: true,
  selector: 'score-set-weight-dialog',
  imports: [CommonModule, FormsModule, A11yModule, MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <div class="ow-dialog">
      <div class="ow-header">
        <span class="ow-title">Set order weight</span>
        <button type="button" class="ow-close" (click)="cancel()" aria-label="Close">
          <mat-icon>close</mat-icon>
        </button>
      </div>

      <div class="ow-body">
        <p class="ow-target">Ordering <span class="ow-target-name">{{ data.name }}</span> among its siblings.</p>
        <p class="ow-desc">
          Higher order weight sorts first, then by name. A sibling without a weight uses a default of
          <strong>0</strong> — so a positive weight moves it above the unset ones and a negative weight
          below them. Weights need not be unique or sequential. Drag-reordering spaces weights by 10,
          so you can set a value in between to slot a sibling between two of them. Affects only model
          browsing and BIE editing — never the generated schema or the CC editor.
        </p>

        <label class="ow-field">
          <span class="ow-label">Order weight</span>
          <input class="ow-input" type="number" step="1" cdkFocusInitial
                 [(ngModel)]="weight" (keyup.enter)="save()"
                 placeholder="e.g. 100" autocomplete="off">
        </label>

        @if (hasCurrent) {
          <p class="ow-hint">Currently {{ data.currentWeight }}. Reset to use the default (0).</p>
        }
      </div>

      <div class="ow-actions">
        @if (hasCurrent) {
          <button type="button" class="ow-text-btn" (click)="reset()">Reset to default</button>
        }
        <span class="ow-spacer"></span>
        <button type="button" mat-button (click)="cancel()">Cancel</button>
        <button type="button" mat-flat-button color="primary" [disabled]="!isValid()" (click)="save()">Save</button>
      </div>
    </div>
  `,
  styles: [`
    .ow-dialog { font-size: 13px; color: rgba(0, 0, 0, 0.87); min-width: 340px; }
    .ow-header {
      display: flex; align-items: center; justify-content: space-between;
      padding: 14px 16px; border-bottom: 1px solid rgba(0, 0, 0, 0.08);
    }
    .ow-title { font-size: 15px; font-weight: 600; }
    .ow-close {
      border: none; background: none; cursor: pointer; padding: 0;
      display: inline-flex; align-items: center; color: rgba(0, 0, 0, 0.5);
    }
    .ow-close:hover { color: rgba(0, 0, 0, 0.87); }
    .ow-close .mat-icon { font-size: 20px; width: 20px; height: 20px; }
    .ow-body { padding: 16px; }
    .ow-target { margin: 0 0 12px 0; color: rgba(0, 0, 0, 0.87); }
    .ow-target-name {
      font-weight: 600; color: rgba(0, 0, 0, 0.87);
      background: rgba(0, 0, 0, 0.07); padding: 2px 8px; border-radius: 4px;
    }
    .ow-desc { margin: 0 0 16px 0; color: rgba(0, 0, 0, 0.6); line-height: 1.5; }
    .ow-field { display: flex; flex-direction: column; gap: 6px; }
    .ow-label { font-weight: 600; color: rgba(0, 0, 0, 0.75); }
    .ow-input {
      height: 34px; width: 100%; box-sizing: border-box; padding: 0 10px;
      font-size: 13px; border: 1px solid rgba(0, 0, 0, 0.24); border-radius: 6px; outline: none;
    }
    .ow-input:focus { border-color: rgba(0, 0, 0, 0.7); box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.18); }
    .ow-hint { margin: 8px 0 0 0; color: rgba(0, 0, 0, 0.5); font-size: 12px; }
    .ow-actions {
      display: flex; align-items: center; gap: 8px;
      padding: 10px 12px 12px 16px; border-top: 1px solid rgba(0, 0, 0, 0.08);
    }
    .ow-spacer { flex: 1 1 auto; }
    .ow-text-btn {
      background: none; border: none; padding: 0; cursor: pointer;
      color: rgba(0, 0, 0, 0.6); font-size: 12px; text-decoration: underline;
    }
    .ow-text-btn:hover { color: rgba(0, 0, 0, 0.87); }
  `],
})
export class SetWeightDialogComponent {
  dialogRef = inject<MatDialogRef<SetWeightDialogComponent>>(MatDialogRef);
  data = inject<SetWeightDialogData>(MAT_DIALOG_DATA);
  weight: number | null = (this.data.currentWeight !== undefined && this.data.currentWeight !== null)
    ? this.data.currentWeight : null;

  get hasCurrent(): boolean {
    return this.data.currentWeight !== undefined && this.data.currentWeight !== null;
  }

  isValid(): boolean {
    return this.weight !== null && this.weight !== undefined && !isNaN(Number(this.weight));
  }

  save(): void {
    if (!this.isValid()) {
      return;
    }
    this.dialogRef.close(Math.trunc(Number(this.weight)));
  }

  reset(): void {
    this.dialogRef.close(null);
  }

  cancel(): void {
    this.dialogRef.close(undefined);
  }
}
