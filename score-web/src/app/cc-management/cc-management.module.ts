import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ScoreCommonModule} from '../common/score-common.module';

import {MaterialModule} from '../material.module';
import {AccDetailModule} from './acc-detail/acc-detail.module';
import {AsccpDetailModule} from './asccp-detail/asccp-detail.module';
import {BccpDetailModule} from './bccp-detail/bccp-detail.module';
import {ExtensionDetailModule} from './extension-detail/extension-detail.module';
import {TransferOwnershipDialogModule} from '../common/transfer-ownership-dialog/transfer-ownership-dialog.module';

import {TranslateModule} from '@ngx-translate/core';
import {ContextMenuModule} from 'ngx-contextmenu';
import {CcListModule} from './cc-list/cc-list.module';

@NgModule({
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    CcListModule,
    AccDetailModule,
    AsccpDetailModule,
    BccpDetailModule,
    ExtensionDetailModule,
    TransferOwnershipDialogModule,
    TranslateModule,
    CommonModule,
    ContextMenuModule,
    ScoreCommonModule,
  ]
})
export class CcManagementModule {
}
