import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {OagisComponentTypes} from '../../cc-management/domain/core-component-node';
import {AccSnapshot, AsccpSnapshot, AssociationSnapshot, BccpSnapshot, CcSnapshot, SnapshotPair} from '../domain/log';
import {LogService} from '../domain/log.service';
import {forkJoin} from 'rxjs';

@Component({
  selector: 'score-log-compare-dialog',
  templateUrl: './log-compare-dialog.component.html',
  styleUrls: ['./log-compare-dialog.component.css'],
})
export class LogCompareDialogComponent implements OnInit {

  componentTypes = OagisComponentTypes;
  pair: SnapshotPair;

  constructor(
    private service: LogService,
    public dialogRef: MatDialogRef<LogCompareDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any) {
  }

  ngOnInit() {
    forkJoin([
      this.service.getSnapshot(this.data.before),
      this.service.getSnapshot(this.data.after)
    ]).subscribe(([before, after]) => {
      this.pair = new SnapshotPair(before, after);
      if (this.pair.component.toUpperCase() === 'ACC') {
        this.setAssociationColor(this.asAcc(this.pair.before), this.asAcc(this.pair.after));
      }
    });
  }

  get component(): string {
    return (this.pair) ? this.pair.component.toUpperCase() : '';
  }

  get before(): CcSnapshot {
    return this.pair ? this.pair.before : undefined;
  }

  get after(): CcSnapshot {
    return this.pair ? this.pair.after : undefined;
  }

  get associations(): SnapshotPair[] {
    return this.pair ? this.pair.associations : [];
  }

  asAcc(snapshot: CcSnapshot): AccSnapshot {
    return snapshot as AccSnapshot;
  }

  asAssociation(snapshot: CcSnapshot): AssociationSnapshot {
    return snapshot as unknown as AssociationSnapshot;
  }

  asAsccp(snapshot: CcSnapshot): AsccpSnapshot {
    return snapshot as AsccpSnapshot;
  }

  asBccp(snapshot: CcSnapshot): BccpSnapshot {
    return snapshot as BccpSnapshot;
  }

  close() {
    this.dialogRef.close();
  }

  isBoolean(obj: any) {
    if (obj === null || obj === undefined) {
      return false;
    }
    return obj.toString() === 'true' || obj.toString() === 'false' || obj.toString() === '0';
  }

  diffClass(before: any, after: any) {
    if (before === after) {
      return '';
    }
    if (before === undefined || before === ' ') {
      return 'Added';
    }
    if (after === undefined || after === ' ') {
      return 'Deleted';
    }
    return 'Modified';
  }

  diffClassForAssociation(beforeAssociation: AssociationSnapshot,
                          afterAssociation: AssociationSnapshot,
                          idx: number) {
    if (beforeAssociation.guid !== afterAssociation.guid) {
      return 'Modified';
    }
    return '';
  }

  setAssociationColor(before: AccSnapshot, after: AccSnapshot) {
    let isExist = false;
    after.associations.filter(a => {
      isExist = false;
      for (const b of before.associations) {
        if (a.guid === b.guid) {
          isExist = true;
          if (JSON.stringify(a) !== JSON.stringify(b)) {
            a.color = 'Modified';
            b.color = 'Modified';
          }
        }
      }
      if (!isExist) {
        a.color = 'Added';
      }
    });

    before.associations.filter(b => {
      isExist = false;
      for (const a of after.associations) {
        if (a.guid === b.guid) {
          isExist = true;
          if (JSON.stringify(a) !== JSON.stringify(b)) {
            a.color = 'Modified';
            b.color = 'Modified';
          }
        }
      }
      if (!isExist) {
        b.color = 'Deleted';
      }
    });
  }
}
