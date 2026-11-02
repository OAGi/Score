import {ChangeDetectionStrategy, Component, EventEmitter, Input, Output} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {MatTableDataSource} from '@angular/material/table';
import {MatTooltipModule} from '@angular/material/tooltip';
import {AgencyIdListSummary} from '../../agency-id-list-management/domain/agency-id-list';
import {CodeListSummary} from '../../code-list-management/domain/code-list';
import {MaterialModule} from '../../material.module';
import {CcDtNodeInfo, CcDtScNodeInfo} from '../domain/core-component-node';

/**
 * The value domains of a Data Type or a Data Type Supplementary Component (BDT screen).
 *
 * The screen owns the table data and the selection, and does what Add, Discard and a change of the value domains
 * mean; this shows them and says what the user did.
 */
@Component({
  selector: 'score-cc-value-domain',
  templateUrl: './value-domain.component.html',
  styleUrl: './value-domain.css',
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [FormsModule, MaterialModule, MatTooltipModule]
})
export class ValueDomainComponent {

  protected readonly columns: string[] = ['select', 'type', 'name', 'xbt'];
  protected readonly primitiveList = ['Binary', 'Boolean', 'Decimal', 'Double', 'Float', 'Integer', 'NormalizedString', 'String', 'TimeDuration', 'TimePoint', 'Token'];

  @Input({required: true}) detail: CcDtNodeInfo | CcDtScNodeInfo;
  /** Whether the value domains belong to a Data Type or to a Supplementary Component. */
  @Input({required: true}) kind: 'dt' | 'dtSc';
  @Input({required: true}) dataSource: MatTableDataSource<any>;
  @Input() editable = false;
  /** Whether the Data Type has the token primitive, which Add needs. */
  @Input() hasToken = false;
  @Input() selected: any;
  /** The code lists / agency ID lists the row can still pick: the ones that no other row has. */
  @Input() filteredCodeLists: (element: any) => CodeListSummary[] = () => [];
  @Input() filteredAgencyIdLists: (element: any) => AgencyIdListSummary[] = () => [];

  @Output() add = new EventEmitter<void>();
  @Output() discard = new EventEmitter<void>();
  @Output() toggle = new EventEmitter<any>();
  @Output() valueDomainChange = new EventEmitter<void>();

  protected get awdPri(): 'dtAwdPri' | 'dtScAwdPri' {
    return this.kind === 'dt' ? 'dtAwdPri' : 'dtScAwdPri';
  }

  protected get groups(): any[] {
    return this.kind === 'dt' ? (this.detail as CcDtNodeInfo).dtAwdPriListByGroup : (this.detail as CcDtScNodeInfo).dtScAwdPriListByGroup;
  }

  compareCodeList(a: CodeListSummary, b: CodeListSummary): boolean {
    return !!a && !!b && a.codeListManifestId === b.codeListManifestId;
  }

  compareAgencyIdList(a: AgencyIdListSummary, b: AgencyIdListSummary): boolean {
    return !!a && !!b && a.agencyIdListManifestId === b.agencyIdListManifestId;
  }
}
