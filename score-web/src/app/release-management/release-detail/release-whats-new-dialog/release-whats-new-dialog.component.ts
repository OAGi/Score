import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Clipboard} from '@angular/cdk/clipboard';
import {MatSnackBar} from '@angular/material/snack-bar';
import {CcListService} from '../../../cc-management/cc-list/domain/cc-list.service';
import {CcChange} from '../../../cc-management/cc-list/domain/cc-list';
import {compare} from '../../../common/utility';

@Component({
  selector: 'score-release-whats-new-dialog',
  templateUrl: './release-whats-new-dialog.component.html',
  styleUrls: ['./release-whats-new-dialog.component.css']
})
export class ReleaseWhatsNewDialogComponent implements OnInit {

  newACCs: CcChange[] = [];
  newASCCPs: CcChange[] = [];
  newBCCPs: CcChange[] = [];
  newASCCs: CcChange[] = [];
  newBCCs: CcChange[] = [];
  newDTs: CcChange[] = [];
  newCodeLists: CcChange[] = [];
  newAgencyIdLists: CcChange[] = [];

  constructor(
    public dialogRef: MatDialogRef<ReleaseWhatsNewDialogComponent>,
    public ccListService: CcListService,
    public clipboard: Clipboard,
    public snackBar: MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: any) {

  }

  ngOnInit(): void {
    this.ccListService.getCcChanges(this.data.releaseId).subscribe(resp => {
      this.newACCs = resp.ccChangeList.filter(e => e.type === 'ACC' && e.changeType === 'NEW_COMPONENT')
        .sort((a, b) => compare(a.den, b.den));
      this.newACCs = this.newACCs.filter(e => !!e.tagList && e.tagList.length > 0).concat(
        this.newACCs.filter(e => !e.tagList || e.tagList.length === 0));

      this.newASCCPs = resp.ccChangeList.filter(e => e.type === 'ASCCP' && e.changeType === 'NEW_COMPONENT')
        .sort((a, b) => compare(a.den, b.den));
      this.newASCCPs = this.newASCCPs.filter(e => !!e.tagList && e.tagList.length > 0).concat(
        this.newASCCPs.filter(e => !e.tagList || e.tagList.length === 0));

      this.newBCCPs = resp.ccChangeList.filter(e => e.type === 'BCCP' && e.changeType === 'NEW_COMPONENT')
        .sort((a, b) => compare(a.den, b.den));
      this.newBCCPs = this.newBCCPs.filter(e => !!e.tagList && e.tagList.length > 0).concat(
        this.newBCCPs.filter(e => !e.tagList || e.tagList.length === 0));

      this.newASCCs = resp.ccChangeList.filter(e => e.type === 'ASCC' && e.changeType === 'NEW_COMPONENT')
        .sort((a, b) => compare(a.den, b.den));

      this.newBCCs = resp.ccChangeList.filter(e => e.type === 'BCC' && e.changeType === 'NEW_COMPONENT')
        .sort((a, b) => compare(a.den, b.den));

      this.newDTs = resp.ccChangeList.filter(e => e.type === 'DT' && e.changeType === 'NEW_COMPONENT')
        .sort((a, b) => compare(a.den, b.den));
      this.newDTs = this.newDTs.filter(e => !!e.tagList && e.tagList.length > 0).concat(
        this.newDTs.filter(e => !e.tagList || e.tagList.length === 0));

      this.newCodeLists = resp.ccChangeList.filter(e => e.type === 'CODE_LIST' && e.changeType === 'NEW_COMPONENT')
        .sort((a, b) => compare(a.den, b.den));

      this.newAgencyIdLists = resp.ccChangeList.filter(e => e.type === 'AGENCY_ID_LIST' && e.changeType === 'NEW_COMPONENT')
        .sort((a, b) => compare(a.den, b.den));
    });
  }

  getTagInfo(changeList: CcChange[]): string {
    if (!changeList || changeList.length === 0) {
      return '';
    }

    const changeListWithTags = changeList.filter(e => !!e.tagList && e.tagList.length > 0);
    if (!changeListWithTags || changeListWithTags.length === 0) {
      return '';
    }

    const tagCounter = new Map<string, number>();
    for (const change of changeListWithTags) {
      for (const tag of change.tagList) {
        if (tagCounter.has(tag.name)) {
          tagCounter.set(tag.name, tagCounter.get(tag.name) + 1);
        } else {
          tagCounter.set(tag.name, 1);
        }
      }
    }

    return '(' + Array.from(tagCounter.entries())
      .map(([tagName, count]) => count + ' ' + tagName).join(', ') + ')';
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

}
