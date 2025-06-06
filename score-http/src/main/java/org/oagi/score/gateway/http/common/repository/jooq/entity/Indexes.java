/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.gateway.http.common.repository.jooq.entity;


import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Abie;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Acc;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Asbie;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Asbiep;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Ascc;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Asccp;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Bbie;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.BbieBizterm;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.BbieSc;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Bbiep;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Bcc;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Bccp;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.BizCtxAssignment;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Comment;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Dt;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.DtSc;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Exception;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Log;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.ModuleBlobContentManifest;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.OasMessageBody;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.SeqKey;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Xbt;


/**
 * A class modelling indexes of tables in oagi.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Indexes {

    // -------------------------------------------------------------------------
    // INDEX definitions
    // -------------------------------------------------------------------------

    public static final Index ABIE_ABIE_HASH_PATH_K = Internal.createIndex(DSL.name("abie_hash_path_k"), Abie.ABIE, new OrderField[] { Abie.ABIE.HASH_PATH }, false);
    public static final Index ABIE_ABIE_PATH_K = Internal.createIndex(DSL.name("abie_path_k"), Abie.ABIE, new OrderField[] { Abie.ABIE.PATH }, false);
    public static final Index ACC_ACC_GUID_IDX = Internal.createIndex(DSL.name("acc_guid_idx"), Acc.ACC, new OrderField[] { Acc.ACC.GUID }, false);
    public static final Index ACC_ACC_LAST_UPDATE_TIMESTAMP_DESC_IDX = Internal.createIndex(DSL.name("acc_last_update_timestamp_desc_idx"), Acc.ACC, new OrderField[] { Acc.ACC.LAST_UPDATE_TIMESTAMP }, false);
    public static final Index BBIE_BIZTERM_ASBIE_BIZTERM_ASBIE_FK = Internal.createIndex(DSL.name("asbie_bizterm_asbie_fk"), BbieBizterm.BBIE_BIZTERM, new OrderField[] { BbieBizterm.BBIE_BIZTERM.BBIE_ID }, false);
    public static final Index ASBIE_ASBIE_HASH_PATH_K = Internal.createIndex(DSL.name("asbie_hash_path_k"), Asbie.ASBIE, new OrderField[] { Asbie.ASBIE.HASH_PATH }, false);
    public static final Index ASBIE_ASBIE_PATH_K = Internal.createIndex(DSL.name("asbie_path_k"), Asbie.ASBIE, new OrderField[] { Asbie.ASBIE.PATH }, false);
    public static final Index ASBIEP_ASBIEP_HASH_PATH_K = Internal.createIndex(DSL.name("asbiep_hash_path_k"), Asbiep.ASBIEP, new OrderField[] { Asbiep.ASBIEP.HASH_PATH }, false);
    public static final Index ASBIEP_ASBIEP_PATH_K = Internal.createIndex(DSL.name("asbiep_path_k"), Asbiep.ASBIEP, new OrderField[] { Asbiep.ASBIEP.PATH }, false);
    public static final Index ASCC_ASCC_GUID_IDX = Internal.createIndex(DSL.name("ascc_guid_idx"), Ascc.ASCC, new OrderField[] { Ascc.ASCC.GUID }, false);
    public static final Index ASCC_ASCC_LAST_UPDATE_TIMESTAMP_DESC_IDX = Internal.createIndex(DSL.name("ascc_last_update_timestamp_desc_idx"), Ascc.ASCC, new OrderField[] { Ascc.ASCC.LAST_UPDATE_TIMESTAMP }, false);
    public static final Index ASCCP_ASCCP_GUID_IDX = Internal.createIndex(DSL.name("asccp_guid_idx"), Asccp.ASCCP, new OrderField[] { Asccp.ASCCP.GUID }, false);
    public static final Index ASCCP_ASCCP_LAST_UPDATE_TIMESTAMP_DESC_IDX = Internal.createIndex(DSL.name("asccp_last_update_timestamp_desc_idx"), Asccp.ASCCP, new OrderField[] { Asccp.ASCCP.LAST_UPDATE_TIMESTAMP }, false);
    public static final Index BBIE_BBIE_HASH_PATH_K = Internal.createIndex(DSL.name("bbie_hash_path_k"), Bbie.BBIE, new OrderField[] { Bbie.BBIE.HASH_PATH }, false);
    public static final Index BBIE_BBIE_PATH_K = Internal.createIndex(DSL.name("bbie_path_k"), Bbie.BBIE, new OrderField[] { Bbie.BBIE.PATH }, false);
    public static final Index BBIE_SC_BBIE_SC_HASH_PATH_K = Internal.createIndex(DSL.name("bbie_sc_hash_path_k"), BbieSc.BBIE_SC, new OrderField[] { BbieSc.BBIE_SC.HASH_PATH }, false);
    public static final Index BBIE_SC_BBIE_SC_PATH_K = Internal.createIndex(DSL.name("bbie_sc_path_k"), BbieSc.BBIE_SC, new OrderField[] { BbieSc.BBIE_SC.PATH }, false);
    public static final Index BBIEP_BBIEP_HASH_PATH_K = Internal.createIndex(DSL.name("bbiep_hash_path_k"), Bbiep.BBIEP, new OrderField[] { Bbiep.BBIEP.HASH_PATH }, false);
    public static final Index BBIEP_BBIEP_PATH_K = Internal.createIndex(DSL.name("bbiep_path_k"), Bbiep.BBIEP, new OrderField[] { Bbiep.BBIEP.PATH }, false);
    public static final Index BCC_BCC_GUID_IDX = Internal.createIndex(DSL.name("bcc_guid_idx"), Bcc.BCC, new OrderField[] { Bcc.BCC.GUID }, false);
    public static final Index BCC_BCC_LAST_UPDATE_TIMESTAMP_DESC_IDX = Internal.createIndex(DSL.name("bcc_last_update_timestamp_desc_idx"), Bcc.BCC, new OrderField[] { Bcc.BCC.LAST_UPDATE_TIMESTAMP }, false);
    public static final Index BCCP_BCCP_GUID_IDX = Internal.createIndex(DSL.name("bccp_guid_idx"), Bccp.BCCP, new OrderField[] { Bccp.BCCP.GUID }, false);
    public static final Index BCCP_BCCP_LAST_UPDATE_TIMESTAMP_DESC_IDX = Internal.createIndex(DSL.name("bccp_last_update_timestamp_desc_idx"), Bccp.BCCP, new OrderField[] { Bccp.BCCP.LAST_UPDATE_TIMESTAMP }, false);
    public static final Index BIZ_CTX_ASSIGNMENT_BIZ_CTX_ID = Internal.createIndex(DSL.name("biz_ctx_id"), BizCtxAssignment.BIZ_CTX_ASSIGNMENT, new OrderField[] { BizCtxAssignment.BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID }, false);
    public static final Index DT_DT_GUID_IDX = Internal.createIndex(DSL.name("dt_guid_idx"), Dt.DT, new OrderField[] { Dt.DT.GUID }, false);
    public static final Index DT_DT_LAST_UPDATE_TIMESTAMP_DESC_IDX = Internal.createIndex(DSL.name("dt_last_update_timestamp_desc_idx"), Dt.DT, new OrderField[] { Dt.DT.LAST_UPDATE_TIMESTAMP }, false);
    public static final Index DT_SC_DT_SC_GUID_IDX = Internal.createIndex(DSL.name("dt_sc_guid_idx"), DtSc.DT_SC, new OrderField[] { DtSc.DT_SC.GUID }, false);
    public static final Index EXCEPTION_EXCEPTION_TAG_IDX = Internal.createIndex(DSL.name("exception_tag_idx"), Exception.EXCEPTION, new OrderField[] { Exception.EXCEPTION.TAG }, false);
    public static final Index MODULE_BLOB_CONTENT_MANIFEST_MMODULE_BLOB_CONTENT_MANIFEST_LAST_UPDATED_BY_FK = Internal.createIndex(DSL.name("mmodule_blob_content_manifest_last_updated_by_fk"), ModuleBlobContentManifest.MODULE_BLOB_CONTENT_MANIFEST, new OrderField[] { ModuleBlobContentManifest.MODULE_BLOB_CONTENT_MANIFEST.LAST_UPDATED_BY }, false);
    public static final Index MODULE_BLOB_CONTENT_MANIFEST_MODULE_BLOB_CONTENT_MANIFEST_BLOB_CONTENT_MANIFEST_ID_FK = Internal.createIndex(DSL.name("module_blob_content_manifest_blob_content_manifest_id_fk"), ModuleBlobContentManifest.MODULE_BLOB_CONTENT_MANIFEST, new OrderField[] { ModuleBlobContentManifest.MODULE_BLOB_CONTENT_MANIFEST.BLOB_CONTENT_MANIFEST_ID }, false);
    public static final Index OAS_MESSAGE_BODY_OAS_MESSAGE_BODY_OAS_ASBIEP_ID_FK = Internal.createIndex(DSL.name("oas_message_body_oas_asbiep_id_fk"), OasMessageBody.OAS_MESSAGE_BODY, new OrderField[] { OasMessageBody.OAS_MESSAGE_BODY.TOP_LEVEL_ASBIEP_ID }, false);
    public static final Index COMMENT_REFERENCE = Internal.createIndex(DSL.name("reference"), Comment.COMMENT, new OrderField[] { Comment.COMMENT.REFERENCE }, false);
    public static final Index LOG_REFERENCE = Internal.createIndex(DSL.name("reference"), Log.LOG, new OrderField[] { Log.LOG.REFERENCE }, false);
    public static final Index SEQ_KEY_SEQ_KEY_ASCC_MANIFEST_ID = Internal.createIndex(DSL.name("seq_key_ascc_manifest_id"), SeqKey.SEQ_KEY, new OrderField[] { SeqKey.SEQ_KEY.ASCC_MANIFEST_ID }, false);
    public static final Index SEQ_KEY_SEQ_KEY_BCC_MANIFEST_ID = Internal.createIndex(DSL.name("seq_key_bcc_manifest_id"), SeqKey.SEQ_KEY, new OrderField[] { SeqKey.SEQ_KEY.BCC_MANIFEST_ID }, false);
    public static final Index SEQ_KEY_SEQ_KEY_FROM_ACC_MANIFEST_ID = Internal.createIndex(DSL.name("seq_key_from_acc_manifest_id"), SeqKey.SEQ_KEY, new OrderField[] { SeqKey.SEQ_KEY.FROM_ACC_MANIFEST_ID }, false);
    public static final Index XBT_XBT_GUID_IDX = Internal.createIndex(DSL.name("xbt_guid_idx"), Xbt.XBT, new OrderField[] { Xbt.XBT.GUID }, false);
    public static final Index XBT_XBT_LAST_UPDATE_TIMESTAMP_DESC_IDX = Internal.createIndex(DSL.name("xbt_last_update_timestamp_desc_idx"), Xbt.XBT, new OrderField[] { Xbt.XBT.LAST_UPDATE_TIMESTAMP }, false);
}
