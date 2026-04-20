"""MariaDB repository implementation for Core Components.

This repository follows the same model-first query style used by other
connectCenter repositories. It provides:
- Unified list queries across ACC, ASCCP, and BCCP.
- Type-specific detail queries for ACC/ASCCP/BCCP.
- User, namespace, release, library, and log summary projection in one pass.
- Optional filtering by DEN/tag/date ranges, with safe sort mapping.
"""


from __future__ import annotations

import secrets
from datetime import datetime
from typing import Any, Literal

from sqlalchemy import Select, and_, bindparam, func, literal, select, text, union_all
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.contracts.core_component import CoreComponentRepositoryContract
from app.repositories.contracts.log import LogRepositoryContract
from app.repositories.models import (
    AccInfoRow,
    AsccRelationshipInfoRow,
    AsccpInfoRow,
    BaseAccInfoRow,
    BccRelationshipInfoRow,
    BccpInfoRow,
    DtSummaryRow,
    LibrarySummaryRow,
    LogSummaryRow,
    NamespaceSummaryRow,
    ReleaseSummaryRow,
    ValueConstraintRow,
)
from app.repositories.models.core_component import CoreComponentListRow, GetAccRow, GetAsccpRow, GetBccpRow
from app.repositories.models.tag import TagSummaryRow
from app.repositories.vendors.mariadb.models.core_component import (
    Acc,
    AccManifest,
    Ascc,
    AsccManifest,
    Asccp,
    AsccpManifest,
    Bcc,
    BccManifest,
    Bccp,
    BccpManifest,
    SeqKey,
)
from app.repositories.vendors.mariadb.models.data_type import Dt, DtManifest
from app.repositories.vendors.mariadb.models.library import Library
from app.repositories.vendors.mariadb.models.log import Log
from app.repositories.vendors.mariadb.models.namespace import Namespace
from app.repositories.vendors.mariadb.models.release import Release
from app.repositories.vendors.mariadb.models.tag import (
    AccManifestTag,
    AsccpManifestTag,
    BccpManifestTag,
    Tag,
)
from app.types.identifiers import (
    AccManifestId,
    AppUserId,
    AsccManifestId,
    AsccpManifestId,
    BccManifestId,
    BccpManifestId,
    DataTypeManifestId,
    NamespaceId,
    ReleaseId,
)


class MariaDbCoreComponentRepository(CoreComponentRepositoryContract):
    """MariaDB-backed repository for core component list/get operations."""

    def __init__(self, session: AsyncSession, log_repository: LogRepositoryContract):
        """Initialize MariaDbCoreComponentRepository.

        Args:
            session: Database session bound to the current request.
            log_repository: Revision-log repository dependency.
        """
        self._session = session
        self._log_repo = log_repository

    async def create_acc(
        self,
        *,
        release_id: ReleaseId,
        based_acc_manifest_id: AccManifestId | None,
        object_class_term: str,
        oagis_component_type: int,
        acc_type: str,
        definition: str | None,
        namespace_id: NamespaceId | None,
        tag_id: list[int] | None,
        requester_user_id: AppUserId,
    ) -> AccManifestId:
        """Create an ACC, its manifest row, initial log row, and optional tags."""
        based_acc_manifest = None
        if based_acc_manifest_id is not None:
            based_acc_manifest = await self._session.get(AccManifest, int(based_acc_manifest_id))
            if based_acc_manifest is None:
                raise LookupError(
                    f"No ACC exists with manifest ID {int(based_acc_manifest_id)}. "
                    "Please verify the identifier and try again."
                )
            if int(based_acc_manifest.release_id) != int(release_id):
                raise ValueError(
                    "The base ACC manifest must belong to the same release as `release_id`. "
                    "Please choose a base ACC from the target release and try again."
                )
        tags_by_id: dict[int, Tag] = {}
        if tag_id is not None:
            for single_tag_id in tag_id:
                tag = await self._session.get(Tag, int(single_tag_id))
                if tag is None:
                    raise LookupError(
                        f"No tag exists with ID {int(single_tag_id)}. Please verify the identifier and try again."
                    )
                tags_by_id[int(single_tag_id)] = tag
        if namespace_id is not None:
            release = await self._session.get(Release, int(release_id))
            namespace = await self._session.get(Namespace, int(namespace_id))
            if namespace is None:
                raise LookupError(
                    f"No namespace exists with ID {int(namespace_id)}. "
                    "Please verify the identifier and try again."
                )
            if release is None:
                raise LookupError(
                    f"No release exists with ID {int(release_id)}. Please verify the identifier and try again."
                )
            if int(namespace.library_id) != int(release.library_id):
                raise ValueError(
                    "The namespace must belong to the same library as the target release. "
                    "Please choose a namespace from the release library and try again."
                )

        now = datetime.utcnow()
        acc = Acc(
            guid=secrets.token_hex(16),
            type=acc_type,
            object_class_term=object_class_term,
            definition=definition,
            based_acc_id=int(based_acc_manifest.acc_id) if based_acc_manifest is not None else None,
            namespace_id=int(namespace_id) if namespace_id is not None else None,
            owner_user_id=int(requester_user_id),
            created_by=int(requester_user_id),
            last_updated_by=int(requester_user_id),
            creation_timestamp=now,
            last_update_timestamp=now,
            state="WIP",
            is_deprecated=False,
            oagis_component_type=int(oagis_component_type),
            is_abstract=False,
        )
        self._session.add(acc)
        await self._session.flush()
        await self._session.refresh(acc)
        acc_id = int(acc.acc_id)
        acc_guid = str(acc.guid)

        den = f"{object_class_term}. Details"
        acc_manifest = AccManifest(
            release_id=int(release_id),
            acc_id=acc_id,
            based_acc_manifest_id=int(based_acc_manifest_id) if based_acc_manifest_id is not None else None,
            den=den,
            conflict=False,
        )
        self._session.add(acc_manifest)
        await self._session.flush()
        await self._session.refresh(acc_manifest)
        acc_manifest_id = int(acc_manifest.acc_manifest_id)

        await self._log_repo.append_acc_log(
            acc_manifest_id=AccManifestId(acc_manifest_id),
            relationships=[],
            requester_user_id=requester_user_id,
            action="Added",
            timestamp=now,
        )

        for single_tag_id in tags_by_id:
            self._session.add(
                AccManifestTag(
                    acc_manifest_id=acc_manifest_id,
                    tag_id=int(single_tag_id),
                    created_by=int(requester_user_id),
                    creation_timestamp=now,
                )
            )

        await self._session.flush()
        return AccManifestId(acc_manifest_id)

    async def create_ascc(
        self,
        *,
        acc_manifest_id: AccManifestId,
        asccp_manifest_id: AsccpManifestId,
        index: int,
        requester_user_id: AppUserId,
    ) -> AsccManifestId:
        """Append an ASCC to an ACC and place it in `seq_key` order."""
        acc_manifest = await self._session.get(AccManifest, int(acc_manifest_id))
        if acc_manifest is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
            )
        acc = await self._session.get(Acc, int(acc_manifest.acc_id))
        if acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
            )
        asccp_manifest = await self._session.get(AsccpManifest, int(asccp_manifest_id))
        if asccp_manifest is None:
            raise LookupError(
                f"No ASCCP exists with manifest ID {int(asccp_manifest_id)}. Please verify the identifier and try again."
            )
        if int(asccp_manifest.release_id) != int(acc_manifest.release_id):
            raise ValueError(
                "The target ASCCP must belong to the same release as the source ACC. "
                "Please choose an ASCCP from the ACC release and try again."
            )
        asccp = await self._session.get(Asccp, int(asccp_manifest.asccp_id))
        if asccp is None:
            raise LookupError(
                f"No ASCCP exists with manifest ID {int(asccp_manifest_id)}. Please verify the identifier and try again."
            )
        if not bool(asccp.reusable_indicator):
            raise ValueError("Target ASCCP is not reusable.")

        existing_extension_count = await self._session.scalar(
            select(func.count())
            .select_from(AsccManifest)
            .join(AsccpManifest, AsccpManifest.asccp_manifest_id == AsccManifest.to_asccp_manifest_id)
            .join(Asccp, Asccp.asccp_id == AsccpManifest.asccp_id)
            .where(
                AsccManifest.from_acc_manifest_id == int(acc_manifest_id),
                Asccp.type == "Extension",
            )
        )
        if asccp.type == "Extension" and int(existing_extension_count or 0) > 0:
            raise ValueError("This ACC already has Extension ASCCP.")

        duplicate_count = await self._session.scalar(
            select(func.count())
            .select_from(AsccManifest)
            .join(AsccpManifest, AsccpManifest.asccp_manifest_id == AsccManifest.to_asccp_manifest_id)
            .where(
                AsccManifest.from_acc_manifest_id == int(acc_manifest_id),
                AsccpManifest.asccp_id == int(asccp_manifest.asccp_id),
            )
        )
        if int(duplicate_count or 0) > 0:
            raise ValueError(f"ACC [{acc_manifest.den}] already has ASCCP [{asccp_manifest.den}]")

        now = datetime.utcnow()
        acc.last_updated_by = int(requester_user_id)
        acc.last_update_timestamp = now

        ascc = Ascc(
            guid=secrets.token_hex(16),
            cardinality_min=0,
            cardinality_max=-1,
            seq_key=0,
            from_acc_id=int(acc.acc_id),
            to_asccp_id=int(asccp.asccp_id),
            definition=None,
            definition_source=None,
            is_deprecated=False,
            replacement_ascc_id=None,
            created_by=int(requester_user_id),
            owner_user_id=int(requester_user_id),
            last_updated_by=int(requester_user_id),
            creation_timestamp=now,
            last_update_timestamp=now,
            state=acc.state,
            prev_ascc_id=None,
            next_ascc_id=None,
        )
        self._session.add(ascc)
        await self._session.flush()

        ascc_manifest = AsccManifest(
            release_id=int(acc_manifest.release_id),
            ascc_id=int(ascc.ascc_id),
            seq_key_id=None,
            from_acc_manifest_id=int(acc_manifest_id),
            to_asccp_manifest_id=int(asccp_manifest_id),
            den=f"{acc.object_class_term}. {asccp_manifest.den}",
            conflict=False,
            replacement_ascc_manifest_id=None,
            prev_ascc_manifest_id=None,
            next_ascc_manifest_id=None,
        )
        self._session.add(ascc_manifest)
        await self._session.flush()

        seq_key = SeqKey(
            from_acc_manifest_id=int(acc_manifest_id),
            ascc_manifest_id=int(ascc_manifest.ascc_manifest_id),
            bcc_manifest_id=None,
            prev_seq_key_id=None,
            next_seq_key_id=None,
        )
        self._session.add(seq_key)
        await self._session.flush()

        ascc_manifest.seq_key_id = int(seq_key.seq_key_id)
        await self._move_seq_key(
            from_acc_manifest_id=acc_manifest_id,
            new_seq_key_id=int(seq_key.seq_key_id),
            index=index,
        )
        await self._log_repo.append_acc_log(
            acc_manifest_id=acc_manifest_id,
            relationships=await self.get_acc_relationships(acc_manifest_id),
            requester_user_id=requester_user_id,
            action="Modified",
            timestamp=now,
        )
        await self._session.flush()
        return AsccManifestId(int(ascc_manifest.ascc_manifest_id))

    async def create_bcc(
        self,
        *,
        acc_manifest_id: AccManifestId,
        bccp_manifest_id: BccpManifestId,
        index: int,
        requester_user_id: AppUserId,
    ) -> BccManifestId:
        """Append a BCC to an ACC and place it in `seq_key` order."""
        acc_manifest = await self._session.get(AccManifest, int(acc_manifest_id))
        if acc_manifest is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
            )
        acc = await self._session.get(Acc, int(acc_manifest.acc_id))
        if acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
            )
        bccp_manifest = await self._session.get(BccpManifest, int(bccp_manifest_id))
        if bccp_manifest is None:
            raise LookupError(
                f"No BCCP exists with manifest ID {int(bccp_manifest_id)}. Please verify the identifier and try again."
            )
        if int(bccp_manifest.release_id) != int(acc_manifest.release_id):
            raise ValueError(
                "The target BCCP must belong to the same release as the source ACC. "
                "Please choose a BCCP from the ACC release and try again."
            )
        bccp = await self._session.get(Bccp, int(bccp_manifest.bccp_id))
        if bccp is None:
            raise LookupError(
                f"No BCCP exists with manifest ID {int(bccp_manifest_id)}. Please verify the identifier and try again."
            )

        duplicate_count = await self._session.scalar(
            select(func.count())
            .select_from(BccManifest)
            .join(BccpManifest, BccpManifest.bccp_manifest_id == BccManifest.to_bccp_manifest_id)
            .where(
                BccManifest.from_acc_manifest_id == int(acc_manifest_id),
                BccpManifest.bccp_id == int(bccp_manifest.bccp_id),
            )
        )
        if int(duplicate_count or 0) > 0:
            raise ValueError(f"ACC [{acc_manifest.den}] already has BCCP [{bccp_manifest.den}]")

        now = datetime.utcnow()
        acc.last_updated_by = int(requester_user_id)
        acc.last_update_timestamp = now

        bcc = Bcc(
            guid=secrets.token_hex(16),
            cardinality_min=0,
            cardinality_max=-1,
            to_bccp_id=int(bccp.bccp_id),
            from_acc_id=int(acc.acc_id),
            seq_key=0,
            entity_type=1,
            definition=None,
            definition_source=None,
            created_by=int(requester_user_id),
            owner_user_id=int(requester_user_id),
            last_updated_by=int(requester_user_id),
            creation_timestamp=now,
            last_update_timestamp=now,
            state="WIP",
            is_deprecated=False,
            replacement_bcc_id=None,
            is_nillable=False,
            default_value=None,
            fixed_value=None,
            prev_bcc_id=None,
            next_bcc_id=None,
        )
        self._session.add(bcc)
        await self._session.flush()

        bcc_manifest = BccManifest(
            release_id=int(acc_manifest.release_id),
            bcc_id=int(bcc.bcc_id),
            seq_key_id=None,
            from_acc_manifest_id=int(acc_manifest_id),
            to_bccp_manifest_id=int(bccp_manifest_id),
            den=f"{acc.object_class_term}. {bccp_manifest.den}",
            conflict=False,
            replacement_bcc_manifest_id=None,
            prev_bcc_manifest_id=None,
            next_bcc_manifest_id=None,
        )
        self._session.add(bcc_manifest)
        await self._session.flush()

        seq_key = SeqKey(
            from_acc_manifest_id=int(acc_manifest_id),
            ascc_manifest_id=None,
            bcc_manifest_id=int(bcc_manifest.bcc_manifest_id),
            prev_seq_key_id=None,
            next_seq_key_id=None,
        )
        self._session.add(seq_key)
        await self._session.flush()

        bcc_manifest.seq_key_id = int(seq_key.seq_key_id)
        await self._move_seq_key(
            from_acc_manifest_id=acc_manifest_id,
            new_seq_key_id=int(seq_key.seq_key_id),
            index=index,
        )
        await self._log_repo.append_acc_log(
            acc_manifest_id=acc_manifest_id,
            relationships=await self.get_acc_relationships(acc_manifest_id),
            requester_user_id=requester_user_id,
            action="Modified",
            timestamp=now,
        )
        await self._session.flush()
        return BccManifestId(int(bcc_manifest.bcc_manifest_id))

    async def create_asccp(
        self,
        *,
        release_id: ReleaseId,
        role_of_acc_manifest_id: AccManifestId,
        property_term: str,
        asccp_type: str,
        reusable_indicator: bool,
        namespace_id: NamespaceId | None,
        definition: str | None,
        definition_source: str | None,
        requester_user_id: AppUserId,
    ) -> AsccpManifestId:
        """Create an ASCCP, its manifest row, initial log row, and no-content defaults."""
        release = await self._session.get(Release, int(release_id))
        if release is None:
            raise LookupError(
                f"No release exists with ID {int(release_id)}. Please verify the identifier and try again."
            )
        role_of_acc_manifest = await self._session.get(AccManifest, int(role_of_acc_manifest_id))
        if role_of_acc_manifest is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(role_of_acc_manifest_id)}. Please verify the identifier and try again."
            )
        if int(role_of_acc_manifest.release_id) != int(release_id):
            raise ValueError(
                "The role ACC must belong to the same release as the target release. "
                "Please choose an ACC from the target release and try again."
            )
        role_of_acc = await self._session.get(Acc, int(role_of_acc_manifest.acc_id))
        if role_of_acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(role_of_acc_manifest_id)}. Please verify the identifier and try again."
            )
        if bool(role_of_acc.is_abstract):
            raise ValueError("An abstract ACC cannot be used to create a new ASCCP.")
        if namespace_id is not None:
            namespace = await self._session.get(Namespace, int(namespace_id))
            if namespace is None:
                raise LookupError(
                    f"No namespace exists with ID {int(namespace_id)}. Please verify the identifier and try again."
                )
            if int(namespace.library_id) != int(release.library_id):
                raise ValueError(
                    "The namespace must belong to the same library as the target release. "
                    "Please choose a namespace from the release library and try again."
                )

        now = datetime.utcnow()
        asccp = Asccp(
            guid=secrets.token_hex(16),
            property_term=property_term,
            type=asccp_type,
            reusable_indicator=bool(reusable_indicator),
            is_nillable=False,
            definition=definition,
            definition_source=definition_source,
            role_of_acc_id=int(role_of_acc.acc_id),
            namespace_id=int(namespace_id) if namespace_id is not None else None,
            owner_user_id=int(requester_user_id),
            created_by=int(requester_user_id),
            last_updated_by=int(requester_user_id),
            creation_timestamp=now,
            last_update_timestamp=now,
            state="WIP",
            is_deprecated=False,
            replacement_asccp_id=None,
            prev_asccp_id=None,
            next_asccp_id=None,
        )
        self._session.add(asccp)
        await self._session.flush()

        den = self._build_asccp_den(property_term=property_term, object_class_term=str(role_of_acc.object_class_term))
        asccp_manifest = AsccpManifest(
            release_id=int(release_id),
            asccp_id=int(asccp.asccp_id),
            role_of_acc_manifest_id=int(role_of_acc_manifest_id),
            den=den,
            conflict=False,
        )
        self._session.add(asccp_manifest)
        await self._session.flush()

        await self._append_asccp_log(
            asccp_manifest_id=AsccpManifestId(int(asccp_manifest.asccp_manifest_id)),
            requester_user_id=requester_user_id,
            action="Added",
            timestamp=now,
        )
        await self._session.flush()
        return AsccpManifestId(int(asccp_manifest.asccp_manifest_id))

    async def create_bccp(
        self,
        *,
        release_id: ReleaseId,
        bdt_manifest_id: DataTypeManifestId,
        property_term: str,
        requester_user_id: AppUserId,
    ) -> BccpManifestId:
        """Create a BCCP, its manifest row, and initial log row."""
        dt_manifest = await self._get_required_bdt_manifest(
            bdt_manifest_id=bdt_manifest_id,
            context_label="target release",
        )
        if int(dt_manifest.release_id) != int(release_id):
            raise ValueError(
                "The target BDT must belong to the same release as the target release. "
                "Please choose a BDT from the target release and try again."
            )
        dt = await self._session.get(Dt, int(dt_manifest.dt_id))
        if dt is None:
            raise LookupError(
                f"No data type exists with manifest ID {int(bdt_manifest_id)}. Please verify the identifier and try again."
            )

        now = datetime.utcnow()
        bccp = Bccp(
            guid=secrets.token_hex(16),
            property_term=property_term,
            representation_term=str(dt.data_type_term),
            bdt_id=int(dt.dt_id),
            definition=None,
            definition_source=None,
            namespace_id=None,
            owner_user_id=int(requester_user_id),
            created_by=int(requester_user_id),
            last_updated_by=int(requester_user_id),
            creation_timestamp=now,
            last_update_timestamp=now,
            state="WIP",
            is_deprecated=False,
            default_value=None,
            fixed_value=None,
            is_nillable=False,
            replacement_bccp_id=None,
            prev_bccp_id=None,
            next_bccp_id=None,
        )
        self._session.add(bccp)
        await self._session.flush()

        den = self._build_bccp_den(
            property_term=property_term,
            qualifier=str(dt.qualifier) if dt.qualifier is not None else None,
            data_type_term=str(dt.data_type_term),
        )
        bccp_manifest = BccpManifest(
            release_id=int(release_id),
            bccp_id=int(bccp.bccp_id),
            bdt_manifest_id=int(bdt_manifest_id),
            den=den,
        )
        self._session.add(bccp_manifest)
        await self._session.flush()

        await self._append_bccp_log(
            bccp_manifest_id=BccpManifestId(int(bccp_manifest.bccp_manifest_id)),
            requester_user_id=requester_user_id,
            action="Added",
            timestamp=now,
        )
        await self._session.flush()
        return BccpManifestId(int(bccp_manifest.bccp_manifest_id))

    async def move_acc_sequence(
        self,
        *,
        acc_manifest_id: AccManifestId,
        item_ascc_manifest_id: AsccManifestId | None,
        item_bcc_manifest_id: BccManifestId | None,
        after_ascc_manifest_id: AsccManifestId | None,
        after_bcc_manifest_id: BccManifestId | None,
        requester_user_id: AppUserId,
    ) -> bool:
        """Move an existing ASCC/BCC child within an ACC's `seq_key` order."""
        if (item_ascc_manifest_id is None) == (item_bcc_manifest_id is None):
            raise ValueError("Exactly one item association must be provided for sequence movement.")
        if after_ascc_manifest_id is not None and after_bcc_manifest_id is not None:
            raise ValueError("Only one `after` association can be provided for sequence movement.")

        acc_manifest = await self._session.get(AccManifest, int(acc_manifest_id))
        if acc_manifest is None:
            return False
        acc = await self._session.get(Acc, int(acc_manifest.acc_id))
        if acc is None:
            return False

        item_seq_key_id = await self._resolve_seq_key_id(
            acc_manifest_id=acc_manifest_id,
            ascc_manifest_id=item_ascc_manifest_id,
            bcc_manifest_id=item_bcc_manifest_id,
        )
        if item_seq_key_id is None:
            raise LookupError("The association to move was not found in the ACC sequence.")

        after_seq_key_id = await self._resolve_seq_key_id(
            acc_manifest_id=acc_manifest_id,
            ascc_manifest_id=after_ascc_manifest_id,
            bcc_manifest_id=after_bcc_manifest_id,
        )
        if (after_ascc_manifest_id is not None or after_bcc_manifest_id is not None) and after_seq_key_id is None:
            raise LookupError("The target `after` association was not found in the ACC sequence.")
        if after_seq_key_id is not None and int(after_seq_key_id) == int(item_seq_key_id):
            raise ValueError("The association cannot be moved after itself.")

        now = datetime.utcnow()
        acc.last_updated_by = int(requester_user_id)
        acc.last_update_timestamp = now
        await self._place_seq_key_after(
            from_acc_manifest_id=acc_manifest_id,
            current_seq_key_id=int(item_seq_key_id),
            after_seq_key_id=None if after_seq_key_id is None else int(after_seq_key_id),
        )
        await self._log_repo.append_acc_log(
            acc_manifest_id=acc_manifest_id,
            relationships=await self.get_acc_relationships(acc_manifest_id),
            requester_user_id=requester_user_id,
            action="Modified",
            timestamp=now,
        )
        await self._session.flush()
        return True

    async def get_owner_acc_manifest_id_by_ascc_manifest(
        self,
        ascc_manifest_id: AsccManifestId,
    ) -> AccManifestId | None:
        """Resolve the owning ACC manifest identifier for an ASCC manifest."""
        return await self._session.scalar(
            select(AsccManifest.from_acc_manifest_id).where(
                AsccManifest.ascc_manifest_id == int(ascc_manifest_id)
            )
        )

    async def get_owner_acc_manifest_id_by_bcc_manifest(
        self,
        bcc_manifest_id: BccManifestId,
    ) -> AccManifestId | None:
        """Resolve the owning ACC manifest identifier for a BCC manifest."""
        return await self._session.scalar(
            select(BccManifest.from_acc_manifest_id).where(
                BccManifest.bcc_manifest_id == int(bcc_manifest_id)
            )
        )

    async def list_owner_acc_manifest_ids_by_asccp_manifest(
        self,
        asccp_manifest_id: AsccpManifestId,
    ) -> list[AccManifestId]:
        """List ACC manifests that currently reference the ASCCP manifest."""
        rows = await self._session.scalars(
            select(AsccManifest.from_acc_manifest_id)
            .where(AsccManifest.to_asccp_manifest_id == int(asccp_manifest_id))
            .distinct()
        )
        return [AccManifestId(int(value)) for value in rows.all()]

    async def list_owner_acc_manifest_ids_by_bccp_manifest(
        self,
        bccp_manifest_id: BccpManifestId,
    ) -> list[AccManifestId]:
        """List ACC manifests that currently reference the BCCP manifest."""
        rows = await self._session.scalars(
            select(BccManifest.from_acc_manifest_id)
            .where(BccManifest.to_bccp_manifest_id == int(bccp_manifest_id))
            .distinct()
        )
        return [AccManifestId(int(value)) for value in rows.all()]

    async def update_acc(
        self,
        *,
        acc_manifest_id: AccManifestId,
        object_class_term: str | None,
        object_class_term_set: bool,
        oagis_component_type: int | None,
        oagis_component_type_set: bool,
        acc_type: str | None,
        acc_type_set: bool,
        definition: str | None,
        definition_set: bool,
        definition_source: str | None,
        definition_source_set: bool,
        is_abstract: bool | None,
        is_abstract_set: bool,
        deprecated: bool | None,
        deprecated_set: bool,
        namespace_id: NamespaceId | None,
        namespace_id_set: bool,
        requester_user_id: AppUserId,
    ) -> bool:
        """Update mutable ACC fields and sync related DEN values."""
        acc_manifest = await self._session.get(AccManifest, int(acc_manifest_id))
        if acc_manifest is None:
            return False
        acc = await self._session.get(Acc, int(acc_manifest.acc_id))
        if acc is None:
            return False

        release = await self._session.get(Release, int(acc_manifest.release_id))
        if release is None:
            raise LookupError(
                f"No release exists with ID {int(acc_manifest.release_id)}. Please verify the identifier and try again."
            )

        if namespace_id_set and namespace_id is not None:
            namespace = await self._session.get(Namespace, int(namespace_id))
            if namespace is None:
                raise LookupError(
                    f"No namespace exists with ID {int(namespace_id)}. "
                    "Please verify the identifier and try again."
                )
            if int(namespace.library_id) != int(release.library_id):
                raise ValueError(
                    "The namespace must belong to the same library as the target release. "
                    "Please choose a namespace from the release library and try again."
                )

        now = datetime.utcnow()
        den_needs_update = object_class_term_set and object_class_term is not None

        if object_class_term_set:
            acc.object_class_term = object_class_term
            if object_class_term is not None:
                acc_manifest.den = f"{object_class_term}. Details"
        if oagis_component_type_set:
            acc.oagis_component_type = oagis_component_type
        if acc_type_set:
            acc.type = acc_type
        if definition_set:
            acc.definition = definition
        if definition_source_set:
            acc.definition_source = definition_source
        if is_abstract_set and is_abstract is not None:
            acc.is_abstract = bool(is_abstract)
        if deprecated_set and deprecated is not None:
            acc.is_deprecated = bool(deprecated)
        if namespace_id_set:
            acc.namespace_id = None if namespace_id is None else int(namespace_id)

        acc.last_updated_by = int(requester_user_id)
        acc.last_update_timestamp = now
        await self._session.flush()

        if den_needs_update and object_class_term is not None:
            await self._sync_acc_related_den(
                acc_manifest_id=acc_manifest_id,
                object_class_term=object_class_term,
            )

        await self._log_repo.append_acc_log(
            acc_manifest_id=acc_manifest_id,
            relationships=await self.get_acc_relationships(acc_manifest_id),
            requester_user_id=requester_user_id,
            action="Modified",
            timestamp=now,
        )
        await self._session.flush()
        return True

    async def update_acc_base(
        self,
        *,
        acc_manifest_id: AccManifestId,
        based_acc_manifest_id: AccManifestId | None,
        requester_user_id: AppUserId,
    ) -> bool:
        """Set or unset the base ACC manifest and append a new ACC log entry."""
        acc_manifest = await self._session.get(AccManifest, int(acc_manifest_id))
        if acc_manifest is None:
            return False
        acc = await self._session.get(Acc, int(acc_manifest.acc_id))
        if acc is None:
            return False

        now = datetime.utcnow()
        if based_acc_manifest_id is None:
            if acc.based_acc_id is None and acc_manifest.based_acc_manifest_id is None:
                return False
            acc.based_acc_id = None
            acc_manifest.based_acc_manifest_id = None
        else:
            based_acc_manifest = await self._session.get(AccManifest, int(based_acc_manifest_id))
            if based_acc_manifest is None:
                raise LookupError(
                    f"No ACC exists with manifest ID {int(based_acc_manifest_id)}. "
                    "Please verify the identifier and try again."
                )
            if int(based_acc_manifest.release_id) != int(acc_manifest.release_id):
                raise ValueError(
                    "The base ACC manifest must belong to the same release as the target ACC. "
                    "Please choose a base ACC from the ACC release and try again."
                )
            based_acc = await self._session.get(Acc, int(based_acc_manifest.acc_id))
            if based_acc is None:
                raise LookupError(
                    f"No ACC exists with manifest ID {int(based_acc_manifest_id)}. "
                    "Please verify the identifier and try again."
                )
            if int(acc_manifest.acc_manifest_id) == int(based_acc_manifest_id):
                raise ValueError("An ACC cannot be based on itself.")
            if (
                acc_manifest.based_acc_manifest_id is not None
                and int(acc_manifest.based_acc_manifest_id) == int(based_acc_manifest_id)
                and acc.based_acc_id is not None
                and int(acc.based_acc_id) == int(based_acc.acc_id)
            ):
                return False
            acc.based_acc_id = int(based_acc.acc_id)
            acc_manifest.based_acc_manifest_id = int(based_acc_manifest_id)

        acc.last_updated_by = int(requester_user_id)
        acc.last_update_timestamp = now
        await self._session.flush()
        await self._log_repo.append_acc_log(
            acc_manifest_id=acc_manifest_id,
            relationships=await self.get_acc_relationships(acc_manifest_id),
            requester_user_id=requester_user_id,
            action="Modified",
            timestamp=now,
        )
        await self._session.flush()
        return True

    async def add_acc_tags(
        self,
        *,
        acc_manifest_id: AccManifestId,
        tag_id: list[int],
        requester_user_id: AppUserId,
    ) -> bool:
        """Attach tags to an ACC manifest and append a new ACC log entry."""
        acc_manifest = await self._session.get(AccManifest, int(acc_manifest_id))
        if acc_manifest is None:
            return False
        acc = await self._session.get(Acc, int(acc_manifest.acc_id))
        if acc is None:
            return False

        tags_by_id: dict[int, Tag] = {}
        for single_tag_id in tag_id:
            tag = await self._session.get(Tag, int(single_tag_id))
            if tag is None:
                raise LookupError(
                    f"No tag exists with ID {int(single_tag_id)}. Please verify the identifier and try again."
                )
            tags_by_id[int(single_tag_id)] = tag

        current_tag_ids = {
            int(value)
            for value in (
                await self._session.scalars(
                    select(AccManifestTag.tag_id).where(AccManifestTag.acc_manifest_id == int(acc_manifest_id))
                )
            ).all()
        }
        tag_ids_to_add = [single_tag_id for single_tag_id in tags_by_id if single_tag_id not in current_tag_ids]
        if not tag_ids_to_add:
            return False

        now = datetime.utcnow()
        for single_tag_id in tag_ids_to_add:
            self._session.add(
                AccManifestTag(
                    acc_manifest_id=int(acc_manifest_id),
                    tag_id=int(single_tag_id),
                    created_by=int(requester_user_id),
                    creation_timestamp=now,
                )
            )

        acc.last_updated_by = int(requester_user_id)
        acc.last_update_timestamp = now
        await self._session.flush()
        await self._log_repo.append_acc_log(
            acc_manifest_id=acc_manifest_id,
            relationships=await self.get_acc_relationships(acc_manifest_id),
            requester_user_id=requester_user_id,
            action="Modified",
            timestamp=now,
        )
        await self._session.flush()
        return True

    async def remove_acc_tags(
        self,
        *,
        acc_manifest_id: AccManifestId,
        tag_id: list[int],
        requester_user_id: AppUserId,
    ) -> bool:
        """Remove tags from an ACC manifest and append a new ACC log entry."""
        acc_manifest = await self._session.get(AccManifest, int(acc_manifest_id))
        if acc_manifest is None:
            return False
        acc = await self._session.get(Acc, int(acc_manifest.acc_id))
        if acc is None:
            return False

        for single_tag_id in tag_id:
            tag = await self._session.get(Tag, int(single_tag_id))
            if tag is None:
                raise LookupError(
                    f"No tag exists with ID {int(single_tag_id)}. Please verify the identifier and try again."
                )

        current_tag_ids = {
            int(value)
            for value in (
                await self._session.scalars(
                    select(AccManifestTag.tag_id).where(AccManifestTag.acc_manifest_id == int(acc_manifest_id))
                )
            ).all()
        }
        tag_ids_to_remove = [single_tag_id for single_tag_id in tag_id if single_tag_id in current_tag_ids]
        if not tag_ids_to_remove:
            return False

        now = datetime.utcnow()
        await self._session.execute(
            text(
                "DELETE FROM acc_manifest_tag "
                "WHERE acc_manifest_id = :acc_manifest_id AND tag_id IN :tag_ids"
            ).bindparams(bindparam("tag_ids", expanding=True)),
            {"acc_manifest_id": int(acc_manifest_id), "tag_ids": tuple(tag_ids_to_remove)},
        )
        acc.last_updated_by = int(requester_user_id)
        acc.last_update_timestamp = now
        await self._session.flush()
        await self._log_repo.append_acc_log(
            acc_manifest_id=acc_manifest_id,
            relationships=await self.get_acc_relationships(acc_manifest_id),
            requester_user_id=requester_user_id,
            action="Modified",
            timestamp=now,
        )
        await self._session.flush()
        return True

    async def change_acc_state(
        self,
        *,
        acc_manifest_id: AccManifestId,
        state: str,
        restore_owner: bool,
        implicit_move: bool,
        requester_user_id: AppUserId,
    ) -> bool:
        """Update an ACC lifecycle state and append a corresponding log entry."""
        acc_manifest = await self._session.get(AccManifest, int(acc_manifest_id))
        if acc_manifest is None:
            return False
        acc = await self._session.get(Acc, int(acc_manifest.acc_id))
        if acc is None:
            return False

        now = datetime.utcnow()
        previous_state = str(acc.state or "")
        acc.state = state
        if restore_owner:
            acc.owner_user_id = int(requester_user_id)
        if not implicit_move:
            acc.last_updated_by = int(requester_user_id)
            acc.last_update_timestamp = now

        await self._session.flush()

        action = "Modified"
        if state == "Deleted":
            action = "Deleted"
        elif restore_owner:
            action = "Restored"

        await self._log_repo.append_acc_log(
            acc_manifest_id=acc_manifest_id,
            relationships=await self.get_acc_relationships(acc_manifest_id),
            requester_user_id=requester_user_id,
            action=action,
            timestamp=now,
        )
        await self._session.flush()
        return True

    async def revise_acc(
        self,
        *,
        acc_manifest_id: AccManifestId,
        requester_user_id: AppUserId,
    ) -> bool:
        """Create a revised ACC working copy and move the manifest head to it."""
        acc_manifest = await self._session.get(AccManifest, int(acc_manifest_id))
        if acc_manifest is None:
            return False
        acc = await self._session.get(Acc, int(acc_manifest.acc_id))
        if acc is None:
            return False

        now = datetime.utcnow()
        prev_acc_id = int(acc.acc_id)
        next_acc = Acc(
            guid=str(acc.guid),
            type=acc.type,
            object_class_term=acc.object_class_term,
            object_class_qualifier=acc.object_class_qualifier,
            definition=acc.definition,
            definition_source=acc.definition_source,
            based_acc_id=acc.based_acc_id,
            namespace_id=acc.namespace_id,
            owner_user_id=int(requester_user_id),
            created_by=int(requester_user_id),
            last_updated_by=int(requester_user_id),
            creation_timestamp=now,
            last_update_timestamp=now,
            state="WIP",
            is_deprecated=bool(acc.is_deprecated),
            replacement_acc_id=acc.replacement_acc_id,
            oagis_component_type=acc.oagis_component_type,
            is_abstract=bool(acc.is_abstract),
            prev_acc_id=prev_acc_id,
            next_acc_id=None,
        )
        self._session.add(next_acc)
        await self._session.flush()

        acc.next_acc_id = int(next_acc.acc_id)
        await self._session.flush()

        await self._revise_asccs(
            from_acc_manifest_id=acc_manifest_id,
            next_acc_id=int(next_acc.acc_id),
            requester_user_id=requester_user_id,
            timestamp=now,
        )
        await self._revise_bccs(
            from_acc_manifest_id=acc_manifest_id,
            next_acc_id=int(next_acc.acc_id),
            requester_user_id=requester_user_id,
            timestamp=now,
        )

        acc_manifest.acc_id = int(next_acc.acc_id)

        await self._session.execute(
            text(
                "UPDATE asccp_manifest "
                "SET role_of_acc_manifest_id = :acc_manifest_id, conflict = 1 "
                "WHERE release_id = :release_id AND role_of_acc_manifest_id = :acc_manifest_id"
            ),
            {
                "acc_manifest_id": int(acc_manifest_id),
                "release_id": int(acc_manifest.release_id),
            },
        )
        await self._session.execute(
            text(
                "UPDATE acc_manifest "
                "SET based_acc_manifest_id = :acc_manifest_id, conflict = 1 "
                "WHERE release_id = :release_id AND based_acc_manifest_id = :acc_manifest_id"
            ),
            {
                "acc_manifest_id": int(acc_manifest_id),
                "release_id": int(acc_manifest.release_id),
            },
        )
        await self._session.flush()
        await self._log_repo.append_acc_log(
            acc_manifest_id=acc_manifest_id,
            relationships=await self.get_acc_relationships(acc_manifest_id),
            requester_user_id=requester_user_id,
            action="Revised",
            timestamp=now,
        )
        await self._session.flush()
        return True

    async def cancel_acc(
        self,
        *,
        acc_manifest_id: AccManifestId,
    ) -> bool:
        """Cancel the active ACC revision and restore the previous stable revision."""
        acc_manifest = await self._session.get(AccManifest, int(acc_manifest_id))
        if acc_manifest is None:
            return False
        acc = await self._session.get(Acc, int(acc_manifest.acc_id))
        if acc is None:
            return False
        if acc.prev_acc_id is None:
            raise ValueError("Not found previous revision")

        prev_acc = await self._session.get(Acc, int(acc.prev_acc_id))
        if prev_acc is None:
            raise ValueError("Not found previous revision")

        stable_log_id, stable_guid_order = await self._log_repo.revert_acc_log_to_stable_state(
            reference=str(acc.guid),
            current_log_id=int(acc_manifest.log_id) if acc_manifest.log_id is not None else None,
        )
        stable_log = await self._session.get(Log, int(stable_log_id))

        acc_manifest.acc_id = int(prev_acc.acc_id)
        acc_manifest.log_id = int(stable_log_id)
        if prev_acc.based_acc_id is not None:
            prev_based_acc = await self._session.get(Acc, int(prev_acc.based_acc_id))
            prev_based_manifest_id = None
            if prev_based_acc is not None:
                prev_based_manifest_id = await self._session.scalar(
                    select(AccManifest.acc_manifest_id)
                    .join(Acc, AccManifest.acc_id == Acc.acc_id)
                    .where(
                        AccManifest.release_id == int(acc_manifest.release_id),
                        Acc.guid == str(prev_based_acc.guid),
                    )
                )
            acc_manifest.based_acc_manifest_id = (
                int(prev_based_manifest_id) if prev_based_manifest_id is not None else None
            )
        else:
            acc_manifest.based_acc_manifest_id = None

        await self._discard_revision_associations(
            acc_manifest_id=acc_manifest_id,
            current_acc_id=int(acc.acc_id),
        )

        prev_acc.next_acc_id = None

        await self._session.execute(
            text(
                "UPDATE acc SET based_acc_id = :prev_acc_id WHERE based_acc_id = :current_acc_id"
            ),
            {"prev_acc_id": int(prev_acc.acc_id), "current_acc_id": int(acc.acc_id)},
        )
        await self._session.execute(
            text(
                "UPDATE asccp SET role_of_acc_id = :prev_acc_id WHERE role_of_acc_id = :current_acc_id"
            ),
            {"prev_acc_id": int(prev_acc.acc_id), "current_acc_id": int(acc.acc_id)},
        )
        if stable_log is not None:
            await self._session.execute(
                text(
                    "DELETE FROM acc_manifest_tag "
                    "WHERE acc_manifest_id = :acc_manifest_id AND creation_timestamp > :stable_created_at"
                ),
                {
                    "acc_manifest_id": int(acc_manifest_id),
                    "stable_created_at": stable_log.creation_timestamp,
                },
            )

        await self._rebuild_seq_keys_from_guid_order(
            acc_manifest_id=acc_manifest_id,
            ordered_guids=stable_guid_order,
        )

        await self._session.delete(acc)
        await self._session.flush()
        return True

    async def has_related_asccps_for_acc(
        self,
        acc_manifest_id: AccManifestId,
    ) -> bool:
        """Return whether any ASCCP still references the ACC as its role-of ACC."""
        count = await self._session.scalar(
            select(func.count())
            .select_from(AsccpManifest)
            .where(AsccpManifest.role_of_acc_manifest_id == int(acc_manifest_id))
        )
        return int(count or 0) > 0

    async def has_deriving_accs(
        self,
        acc_manifest_id: AccManifestId,
    ) -> bool:
        """Return whether any ACC manifest still derives from the target ACC manifest."""
        count = await self._session.scalar(
            select(func.count())
            .select_from(AccManifest)
            .where(AccManifest.based_acc_manifest_id == int(acc_manifest_id))
        )
        return int(count or 0) > 0

    async def discard_acc(
        self,
        *,
        acc_manifest_id: AccManifestId,
    ) -> bool:
        """Permanently delete an ACC and its direct child rows."""
        acc_manifest = await self._session.get(AccManifest, int(acc_manifest_id))
        if acc_manifest is None:
            return False
        acc = await self._session.get(Acc, int(acc_manifest.acc_id))
        if acc is None:
            return False

        ascc_rows = (
            await self._session.execute(
                select(AsccManifest.ascc_manifest_id, AsccManifest.ascc_id).where(
                    AsccManifest.from_acc_manifest_id == int(acc_manifest_id)
                )
            )
        ).all()
        bcc_rows = (
            await self._session.execute(
                select(BccManifest.bcc_manifest_id, BccManifest.bcc_id).where(
                    BccManifest.from_acc_manifest_id == int(acc_manifest_id)
                )
            )
        ).all()
        ascc_ids = sorted({int(row.ascc_id) for row in ascc_rows})
        bcc_ids = sorted({int(row.bcc_id) for row in bcc_rows})

        await self._session.execute(
            text(
                "UPDATE ascc_manifest SET seq_key_id = NULL "
                "WHERE from_acc_manifest_id = :acc_manifest_id"
            ),
            {"acc_manifest_id": int(acc_manifest_id)},
        )
        await self._session.execute(
            text(
                "UPDATE bcc_manifest SET seq_key_id = NULL "
                "WHERE from_acc_manifest_id = :acc_manifest_id"
            ),
            {"acc_manifest_id": int(acc_manifest_id)},
        )
        await self._session.execute(
            text(
                "UPDATE seq_key SET prev_seq_key_id = NULL, next_seq_key_id = NULL "
                "WHERE from_acc_manifest_id = :acc_manifest_id"
            ),
            {"acc_manifest_id": int(acc_manifest_id)},
        )
        await self._session.execute(
            text("DELETE FROM seq_key WHERE from_acc_manifest_id = :acc_manifest_id"),
            {"acc_manifest_id": int(acc_manifest_id)},
        )

        await self._session.execute(
            text("DELETE FROM ascc_manifest WHERE from_acc_manifest_id = :acc_manifest_id"),
            {"acc_manifest_id": int(acc_manifest_id)},
        )
        if ascc_ids:
            await self._session.execute(
                text("DELETE FROM ascc WHERE ascc_id IN :ascc_ids").bindparams(
                    bindparam("ascc_ids", expanding=True)
                ),
                {"ascc_ids": tuple(ascc_ids)},
            )

        await self._session.execute(
            text("DELETE FROM bcc_manifest WHERE from_acc_manifest_id = :acc_manifest_id"),
            {"acc_manifest_id": int(acc_manifest_id)},
        )
        if bcc_ids:
            await self._session.execute(
                text("DELETE FROM bcc WHERE bcc_id IN :bcc_ids").bindparams(
                    bindparam("bcc_ids", expanding=True)
                ),
                {"bcc_ids": tuple(bcc_ids)},
            )

        await self._session.execute(
            text("DELETE FROM module_acc_manifest WHERE acc_manifest_id = :acc_manifest_id"),
            {"acc_manifest_id": int(acc_manifest_id)},
        )
        await self._session.execute(
            text("DELETE FROM acc_manifest_tag WHERE acc_manifest_id = :acc_manifest_id"),
            {"acc_manifest_id": int(acc_manifest_id)},
        )
        await self._session.execute(
            text("DELETE FROM acc_manifest WHERE acc_manifest_id = :acc_manifest_id"),
            {"acc_manifest_id": int(acc_manifest_id)},
        )
        await self._session.execute(
            text("DELETE FROM acc WHERE acc_id = :acc_id"),
            {"acc_id": int(acc.acc_id)},
        )
        await self._session.flush()
        return True

    async def update_asccp(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
        property_term: str | None,
        property_term_set: bool,
        reusable_indicator: bool | None,
        reusable_indicator_set: bool,
        deprecated: bool | None,
        deprecated_set: bool,
        is_nillable: bool | None,
        is_nillable_set: bool,
        namespace_id: NamespaceId | None,
        namespace_id_set: bool,
        definition: str | None,
        definition_set: bool,
        definition_source: str | None,
        definition_source_set: bool,
        requester_user_id: AppUserId,
    ) -> bool:
        """Update mutable ASCCP fields and sync dependent ASCC DEN values."""
        asccp_manifest = await self._session.get(AsccpManifest, int(asccp_manifest_id))
        if asccp_manifest is None:
            return False
        asccp = await self._session.get(Asccp, int(asccp_manifest.asccp_id))
        if asccp is None:
            return False
        release = await self._session.get(Release, int(asccp_manifest.release_id))
        if release is None:
            raise LookupError(
                f"No release exists with ID {int(asccp_manifest.release_id)}. Please verify the identifier and try again."
            )

        if namespace_id_set and namespace_id is not None:
            namespace = await self._session.get(Namespace, int(namespace_id))
            if namespace is None:
                raise LookupError(
                    f"No namespace exists with ID {int(namespace_id)}. Please verify the identifier and try again."
                )
            if int(namespace.library_id) != int(release.library_id):
                raise ValueError(
                    "The namespace must belong to the same library as the target release. "
                    "Please choose a namespace from the release library and try again."
                )

        now = datetime.utcnow()
        den_needs_update = property_term_set and property_term is not None and property_term != asccp.property_term
        if property_term_set:
            asccp.property_term = property_term
        if reusable_indicator_set and reusable_indicator is not None:
            asccp.reusable_indicator = bool(reusable_indicator)
        if deprecated_set and deprecated is not None:
            asccp.is_deprecated = bool(deprecated)
        if is_nillable_set and is_nillable is not None:
            asccp.is_nillable = bool(is_nillable)
        if namespace_id_set:
            asccp.namespace_id = None if namespace_id is None else int(namespace_id)
        if definition_set:
            asccp.definition = definition
        if definition_source_set:
            asccp.definition_source = definition_source
        asccp.last_updated_by = int(requester_user_id)
        asccp.last_update_timestamp = now
        await self._session.flush()

        if den_needs_update and property_term is not None:
            await self._sync_asccp_related_den(
                asccp_manifest_id=asccp_manifest_id,
                property_term=property_term,
            )

        await self._append_asccp_log(
            asccp_manifest_id=asccp_manifest_id,
            requester_user_id=requester_user_id,
            action="Modified",
            timestamp=now,
        )
        await self._session.flush()
        return True

    async def update_bccp(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
        property_term: str | None,
        property_term_set: bool,
        deprecated: bool | None,
        deprecated_set: bool,
        is_nillable: bool | None,
        is_nillable_set: bool,
        namespace_id: NamespaceId | None,
        namespace_id_set: bool,
        default_value: str | None,
        default_value_set: bool,
        fixed_value: str | None,
        fixed_value_set: bool,
        definition: str | None,
        definition_set: bool,
        definition_source: str | None,
        definition_source_set: bool,
        requester_user_id: AppUserId,
    ) -> bool:
        """Update mutable BCCP fields and sync dependent BCC DEN values."""
        bccp_manifest = await self._session.get(BccpManifest, int(bccp_manifest_id))
        if bccp_manifest is None:
            return False
        bccp = await self._session.get(Bccp, int(bccp_manifest.bccp_id))
        if bccp is None:
            return False
        release = await self._session.get(Release, int(bccp_manifest.release_id))
        if release is None:
            raise LookupError(
                f"No release exists with ID {int(bccp_manifest.release_id)}. Please verify the identifier and try again."
            )
        if namespace_id_set and namespace_id is not None:
            namespace = await self._session.get(Namespace, int(namespace_id))
            if namespace is None:
                raise LookupError(
                    f"No namespace exists with ID {int(namespace_id)}. Please verify the identifier and try again."
                )
            if int(namespace.library_id) != int(release.library_id):
                raise ValueError(
                    "The namespace must belong to the same library as the target release. "
                    "Please choose a namespace from the release library and try again."
                )

        now = datetime.utcnow()
        den_needs_update = property_term_set and property_term is not None and property_term != bccp.property_term
        if property_term_set:
            bccp.property_term = property_term
        if deprecated_set and deprecated is not None:
            bccp.is_deprecated = bool(deprecated)
        if is_nillable_set and is_nillable is not None:
            bccp.is_nillable = bool(is_nillable)
        if namespace_id_set:
            bccp.namespace_id = None if namespace_id is None else int(namespace_id)
        if default_value_set or fixed_value_set:
            if fixed_value_set and fixed_value:
                bccp.fixed_value = fixed_value
                bccp.default_value = None
            elif default_value_set and default_value:
                bccp.default_value = default_value
                bccp.fixed_value = None
            else:
                if default_value_set:
                    bccp.default_value = None
                if fixed_value_set:
                    bccp.fixed_value = None
        if definition_set:
            bccp.definition = definition
        if definition_source_set:
            bccp.definition_source = definition_source
        bccp.last_updated_by = int(requester_user_id)
        bccp.last_update_timestamp = now
        await self._session.flush()

        if den_needs_update and property_term is not None:
            await self._sync_bccp_related_den(
                bccp_manifest_id=bccp_manifest_id,
                property_term=property_term,
            )

        await self._append_bccp_log(
            bccp_manifest_id=bccp_manifest_id,
            requester_user_id=requester_user_id,
            action="Modified",
            timestamp=now,
        )
        await self._session.flush()
        return True

    async def change_asccp_role_of_acc(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
        role_of_acc_manifest_id: AccManifestId,
        requester_user_id: AppUserId,
    ) -> bool:
        """Change the role ACC of an ASCCP and sync dependent ASCC DEN values."""
        asccp_manifest = await self._session.get(AsccpManifest, int(asccp_manifest_id))
        if asccp_manifest is None:
            return False
        asccp = await self._session.get(Asccp, int(asccp_manifest.asccp_id))
        if asccp is None:
            return False
        role_of_acc_manifest = await self._session.get(AccManifest, int(role_of_acc_manifest_id))
        if role_of_acc_manifest is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(role_of_acc_manifest_id)}. Please verify the identifier and try again."
            )
        if int(role_of_acc_manifest.release_id) != int(asccp_manifest.release_id):
            raise ValueError(
                "The role ACC must belong to the same release as the ASCCP. "
                "Please choose an ACC from the ASCCP release and try again."
            )
        role_of_acc = await self._session.get(Acc, int(role_of_acc_manifest.acc_id))
        if role_of_acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(role_of_acc_manifest_id)}. Please verify the identifier and try again."
            )
        if bool(role_of_acc.is_abstract):
            raise ValueError("An abstract ACC cannot be used as the role ACC of an ASCCP.")

        now = datetime.utcnow()
        asccp.role_of_acc_id = int(role_of_acc.acc_id)
        asccp.last_updated_by = int(requester_user_id)
        asccp.last_update_timestamp = now
        asccp_manifest.role_of_acc_manifest_id = int(role_of_acc_manifest_id)
        asccp_manifest.den = self._build_asccp_den(
            property_term=str(asccp.property_term or "Property Term"),
            object_class_term=str(role_of_acc.object_class_term),
        )
        await self._session.flush()
        await self._sync_asccp_related_den(
            asccp_manifest_id=asccp_manifest_id,
            property_term=str(asccp.property_term or "Property Term"),
        )
        await self._append_asccp_log(
            asccp_manifest_id=asccp_manifest_id,
            requester_user_id=requester_user_id,
            action="Modified",
            timestamp=now,
        )
        await self._session.flush()
        return True

    async def change_bccp_bdt(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
        bdt_manifest_id: DataTypeManifestId,
        requester_user_id: AppUserId,
    ) -> bool:
        """Change the BDT of a BCCP and sync dependent BCC DEN values."""
        bccp_manifest = await self._session.get(BccpManifest, int(bccp_manifest_id))
        if bccp_manifest is None:
            return False
        bccp = await self._session.get(Bccp, int(bccp_manifest.bccp_id))
        if bccp is None:
            return False
        dt_manifest = await self._get_required_bdt_manifest(
            bdt_manifest_id=bdt_manifest_id,
            context_label="BCCP release",
        )
        if int(dt_manifest.release_id) != int(bccp_manifest.release_id):
            raise ValueError(
                "The target BDT must belong to the same release as the BCCP. "
                "Please choose a BDT from the BCCP release and try again."
            )
        dt = await self._session.get(Dt, int(dt_manifest.dt_id))
        if dt is None:
            raise LookupError(
                f"No data type exists with manifest ID {int(bdt_manifest_id)}. Please verify the identifier and try again."
            )

        now = datetime.utcnow()
        bccp.bdt_id = int(dt.dt_id)
        bccp.representation_term = str(dt.data_type_term)
        bccp.last_updated_by = int(requester_user_id)
        bccp.last_update_timestamp = now
        bccp_manifest.bdt_manifest_id = int(bdt_manifest_id)
        bccp_manifest.den = self._build_bccp_den(
            property_term=str(bccp.property_term),
            qualifier=str(dt.qualifier) if dt.qualifier is not None else None,
            data_type_term=str(dt.data_type_term),
        )
        await self._session.flush()
        await self._sync_bccp_related_den(
            bccp_manifest_id=bccp_manifest_id,
            property_term=str(bccp.property_term),
        )
        await self._append_bccp_log(
            bccp_manifest_id=bccp_manifest_id,
            requester_user_id=requester_user_id,
            action="Modified",
            timestamp=now,
        )
        await self._session.flush()
        return True

    async def _get_required_bdt_manifest(
        self,
        *,
        bdt_manifest_id: DataTypeManifestId,
        context_label: str,
    ) -> DtManifest:
        """Resolve a DT manifest and require it to represent a BDT."""
        dt_manifest = await self._session.get(DtManifest, int(bdt_manifest_id))
        if dt_manifest is None:
            raise LookupError(
                f"No data type exists with manifest ID {int(bdt_manifest_id)}. Please verify the identifier and try again."
            )
        if dt_manifest.based_dt_manifest_id is None:
            raise ValueError(
                "The target data type must be a BDT before it can be used here. "
                f"Please choose a BDT for the {context_label} and try again."
            )
        return dt_manifest

    async def add_asccp_tags(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
        tag_id: list[int],
        requester_user_id: AppUserId,
    ) -> bool:
        """Attach tags to an ASCCP manifest."""
        return await self._add_manifest_tags(
            manifest_id=int(asccp_manifest_id),
            manifest_model=AsccpManifest,
            component_model=Asccp,
            component_id_attr="asccp_id",
            tag_link_model=AsccpManifestTag,
            tag_link_manifest_col=AsccpManifestTag.asccp_manifest_id,
            requester_user_id=requester_user_id,
            tag_id=tag_id,
        )

    async def remove_asccp_tags(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
        tag_id: list[int],
        requester_user_id: AppUserId,
    ) -> bool:
        """Remove tags from an ASCCP manifest."""
        return await self._remove_manifest_tags(
            manifest_id=int(asccp_manifest_id),
            manifest_model=AsccpManifest,
            component_model=Asccp,
            component_id_attr="asccp_id",
            table_name="asccp_manifest_tag",
            tag_link_model=AsccpManifestTag,
            tag_link_manifest_col=AsccpManifestTag.asccp_manifest_id,
            requester_user_id=requester_user_id,
            tag_id=tag_id,
        )

    async def add_bccp_tags(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
        tag_id: list[int],
        requester_user_id: AppUserId,
    ) -> bool:
        """Attach tags to a BCCP manifest."""
        return await self._add_manifest_tags(
            manifest_id=int(bccp_manifest_id),
            manifest_model=BccpManifest,
            component_model=Bccp,
            component_id_attr="bccp_id",
            tag_link_model=BccpManifestTag,
            tag_link_manifest_col=BccpManifestTag.bccp_manifest_id,
            requester_user_id=requester_user_id,
            tag_id=tag_id,
        )

    async def remove_bccp_tags(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
        tag_id: list[int],
        requester_user_id: AppUserId,
    ) -> bool:
        """Remove tags from a BCCP manifest."""
        return await self._remove_manifest_tags(
            manifest_id=int(bccp_manifest_id),
            manifest_model=BccpManifest,
            component_model=Bccp,
            component_id_attr="bccp_id",
            table_name="bccp_manifest_tag",
            tag_link_model=BccpManifestTag,
            tag_link_manifest_col=BccpManifestTag.bccp_manifest_id,
            requester_user_id=requester_user_id,
            tag_id=tag_id,
        )

    async def change_asccp_state(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
        state: str,
        restore_owner: bool,
        implicit_move: bool,
        requester_user_id: AppUserId,
    ) -> bool:
        """Update an ASCCP lifecycle state and append a corresponding log entry."""
        asccp_manifest = await self._session.get(AsccpManifest, int(asccp_manifest_id))
        if asccp_manifest is None:
            return False
        asccp = await self._session.get(Asccp, int(asccp_manifest.asccp_id))
        if asccp is None:
            return False

        now = datetime.utcnow()
        asccp.state = state
        if restore_owner:
            asccp.owner_user_id = int(requester_user_id)
        if not implicit_move:
            asccp.last_updated_by = int(requester_user_id)
            asccp.last_update_timestamp = now
        await self._session.flush()
        await self._append_asccp_log(
            asccp_manifest_id=asccp_manifest_id,
            requester_user_id=requester_user_id,
            action="Deleted" if state == "Deleted" else ("Restored" if restore_owner else "Modified"),
            timestamp=now,
        )
        await self._session.flush()
        return True

    async def change_bccp_state(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
        state: str,
        restore_owner: bool,
        implicit_move: bool,
        requester_user_id: AppUserId,
    ) -> bool:
        """Update a BCCP lifecycle state and append a corresponding log entry."""
        bccp_manifest = await self._session.get(BccpManifest, int(bccp_manifest_id))
        if bccp_manifest is None:
            return False
        bccp = await self._session.get(Bccp, int(bccp_manifest.bccp_id))
        if bccp is None:
            return False

        now = datetime.utcnow()
        bccp.state = state
        if restore_owner:
            bccp.owner_user_id = int(requester_user_id)
        if not implicit_move:
            bccp.last_updated_by = int(requester_user_id)
            bccp.last_update_timestamp = now
        await self._session.flush()
        await self._append_bccp_log(
            bccp_manifest_id=bccp_manifest_id,
            requester_user_id=requester_user_id,
            action="Deleted" if state == "Deleted" else ("Restored" if restore_owner else "Modified"),
            timestamp=now,
        )
        await self._session.flush()
        return True

    async def revise_asccp(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
        requester_user_id: AppUserId,
    ) -> bool:
        """Create a revised ASCCP working copy and move the manifest head to it."""
        asccp_manifest = await self._session.get(AsccpManifest, int(asccp_manifest_id))
        if asccp_manifest is None:
            return False
        asccp = await self._session.get(Asccp, int(asccp_manifest.asccp_id))
        if asccp is None:
            return False

        now = datetime.utcnow()
        prev_asccp_id = int(asccp.asccp_id)
        next_asccp = Asccp(
            guid=str(asccp.guid),
            property_term=asccp.property_term,
            type=asccp.type,
            reusable_indicator=bool(asccp.reusable_indicator),
            is_nillable=bool(asccp.is_nillable),
            definition=asccp.definition,
            definition_source=asccp.definition_source,
            role_of_acc_id=asccp.role_of_acc_id,
            namespace_id=asccp.namespace_id,
            owner_user_id=int(requester_user_id),
            created_by=int(requester_user_id),
            last_updated_by=int(requester_user_id),
            creation_timestamp=now,
            last_update_timestamp=now,
            state="WIP",
            is_deprecated=bool(asccp.is_deprecated),
            replacement_asccp_id=asccp.replacement_asccp_id,
            prev_asccp_id=prev_asccp_id,
            next_asccp_id=None,
        )
        self._session.add(next_asccp)
        await self._session.flush()

        asccp.next_asccp_id = int(next_asccp.asccp_id)
        asccp_manifest.asccp_id = int(next_asccp.asccp_id)
        await self._session.execute(
            text(
                "UPDATE ascc_manifest "
                "SET to_asccp_manifest_id = :asccp_manifest_id, conflict = 1 "
                "WHERE release_id = :release_id AND to_asccp_manifest_id = :asccp_manifest_id"
            ),
            {"asccp_manifest_id": int(asccp_manifest_id), "release_id": int(asccp_manifest.release_id)},
        )
        await self._session.flush()
        await self._append_asccp_log(
            asccp_manifest_id=asccp_manifest_id,
            requester_user_id=requester_user_id,
            action="Revised",
            timestamp=now,
        )
        await self._session.flush()
        return True

    async def cancel_asccp(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
    ) -> bool:
        """Cancel the active ASCCP revision and restore the previous stable revision."""
        asccp_manifest = await self._session.get(AsccpManifest, int(asccp_manifest_id))
        if asccp_manifest is None:
            return False
        asccp = await self._session.get(Asccp, int(asccp_manifest.asccp_id))
        if asccp is None:
            return False
        if asccp.prev_asccp_id is None:
            raise ValueError("Not found previous revision")
        prev_asccp = await self._session.get(Asccp, int(asccp.prev_asccp_id))
        if prev_asccp is None:
            raise ValueError("Not found previous revision")

        current_log_id = int(asccp_manifest.log_id) if asccp_manifest.log_id is not None else None
        asccp_manifest.log_id = None
        await self._session.flush()
        stable_log_id = await self._log_repo.revert_component_log_to_stable_state(
            reference=str(asccp.guid),
            current_log_id=current_log_id,
        )
        asccp_manifest.asccp_id = int(prev_asccp.asccp_id)
        asccp_manifest.log_id = int(stable_log_id)
        if prev_asccp.role_of_acc_id is not None:
            prev_role_acc = await self._session.get(Acc, int(prev_asccp.role_of_acc_id))
            prev_role_manifest_id = await self._session.scalar(
                select(AccManifest.acc_manifest_id)
                .join(Acc, AccManifest.acc_id == Acc.acc_id)
                .where(
                    AccManifest.release_id == int(asccp_manifest.release_id),
                    Acc.guid == str(prev_role_acc.guid) if prev_role_acc is not None else literal(None),
                )
            )
            asccp_manifest.role_of_acc_manifest_id = int(prev_role_manifest_id) if prev_role_manifest_id is not None else None
        else:
            asccp_manifest.role_of_acc_manifest_id = None

        await self._session.execute(
            text(
                "UPDATE ascc SET to_asccp_id = :prev_asccp_id WHERE to_asccp_id = :current_asccp_id"
            ),
            {"prev_asccp_id": int(prev_asccp.asccp_id), "current_asccp_id": int(asccp.asccp_id)},
        )
        await self._session.execute(
            text(
                "UPDATE ascc_manifest SET conflict = 0 "
                "WHERE release_id = :release_id AND to_asccp_manifest_id = :asccp_manifest_id"
            ),
            {"release_id": int(asccp_manifest.release_id), "asccp_manifest_id": int(asccp_manifest_id)},
        )
        prev_asccp.next_asccp_id = None
        await self._session.delete(asccp)
        await self._session.flush()
        return True

    async def revise_bccp(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
        requester_user_id: AppUserId,
    ) -> bool:
        """Create a revised BCCP working copy and move the manifest head to it."""
        bccp_manifest = await self._session.get(BccpManifest, int(bccp_manifest_id))
        if bccp_manifest is None:
            return False
        bccp = await self._session.get(Bccp, int(bccp_manifest.bccp_id))
        if bccp is None:
            return False

        now = datetime.utcnow()
        prev_bccp_id = int(bccp.bccp_id)
        next_bccp = Bccp(
            guid=str(bccp.guid),
            property_term=bccp.property_term,
            representation_term=bccp.representation_term,
            bdt_id=bccp.bdt_id,
            definition=bccp.definition,
            definition_source=bccp.definition_source,
            namespace_id=bccp.namespace_id,
            owner_user_id=int(requester_user_id),
            created_by=int(requester_user_id),
            last_updated_by=int(requester_user_id),
            creation_timestamp=now,
            last_update_timestamp=now,
            state="WIP",
            is_deprecated=bool(bccp.is_deprecated),
            default_value=bccp.default_value,
            fixed_value=bccp.fixed_value,
            is_nillable=bool(bccp.is_nillable),
            replacement_bccp_id=bccp.replacement_bccp_id,
            prev_bccp_id=prev_bccp_id,
            next_bccp_id=None,
        )
        self._session.add(next_bccp)
        await self._session.flush()

        bccp.next_bccp_id = int(next_bccp.bccp_id)
        bccp_manifest.bccp_id = int(next_bccp.bccp_id)
        await self._session.execute(
            text(
                "UPDATE bcc_manifest "
                "SET to_bccp_manifest_id = :bccp_manifest_id, conflict = 1 "
                "WHERE release_id = :release_id AND to_bccp_manifest_id = :bccp_manifest_id"
            ),
            {"bccp_manifest_id": int(bccp_manifest_id), "release_id": int(bccp_manifest.release_id)},
        )
        await self._session.flush()
        await self._append_bccp_log(
            bccp_manifest_id=bccp_manifest_id,
            requester_user_id=requester_user_id,
            action="Revised",
            timestamp=now,
        )
        await self._session.flush()
        return True

    async def cancel_bccp(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
    ) -> bool:
        """Cancel the active BCCP revision and restore the previous stable revision."""
        bccp_manifest = await self._session.get(BccpManifest, int(bccp_manifest_id))
        if bccp_manifest is None:
            return False
        bccp = await self._session.get(Bccp, int(bccp_manifest.bccp_id))
        if bccp is None:
            return False
        if bccp.prev_bccp_id is None:
            raise ValueError("Not found previous revision")
        prev_bccp = await self._session.get(Bccp, int(bccp.prev_bccp_id))
        if prev_bccp is None:
            raise ValueError("Not found previous revision")

        current_log_id = int(bccp_manifest.log_id) if bccp_manifest.log_id is not None else None
        bccp_manifest.log_id = None
        await self._session.flush()
        stable_log_id = await self._log_repo.revert_component_log_to_stable_state(
            reference=str(bccp.guid),
            current_log_id=current_log_id,
        )
        bccp_manifest.bccp_id = int(prev_bccp.bccp_id)
        bccp_manifest.log_id = int(stable_log_id)
        if prev_bccp.bdt_id is not None:
            prev_bdt = await self._session.get(Dt, int(prev_bccp.bdt_id))
            prev_bdt_manifest_id = await self._session.scalar(
                select(DtManifest.dt_manifest_id)
                .join(Dt, DtManifest.dt_id == Dt.dt_id)
                .where(
                    DtManifest.release_id == int(bccp_manifest.release_id),
                    Dt.guid == str(prev_bdt.guid) if prev_bdt is not None else literal(None),
                )
            )
            bccp_manifest.bdt_manifest_id = int(prev_bdt_manifest_id) if prev_bdt_manifest_id is not None else None

        await self._session.execute(
            text("UPDATE bcc SET to_bccp_id = :prev_bccp_id WHERE to_bccp_id = :current_bccp_id"),
            {"prev_bccp_id": int(prev_bccp.bccp_id), "current_bccp_id": int(bccp.bccp_id)},
        )
        await self._session.execute(
            text(
                "UPDATE bcc_manifest SET conflict = 0 "
                "WHERE release_id = :release_id AND to_bccp_manifest_id = :bccp_manifest_id"
            ),
            {"release_id": int(bccp_manifest.release_id), "bccp_manifest_id": int(bccp_manifest_id)},
        )
        prev_bccp.next_bccp_id = None
        await self._session.delete(bccp)
        await self._session.flush()
        return True

    async def has_related_asccs_for_asccp(
        self,
        asccp_manifest_id: AsccpManifestId,
    ) -> bool:
        """Return whether any ASCC still references the ASCCP."""
        count = await self._session.scalar(
            select(func.count()).select_from(AsccManifest).where(
                AsccManifest.to_asccp_manifest_id == int(asccp_manifest_id)
            )
        )
        return int(count or 0) > 0

    async def has_related_bccs_for_bccp(
        self,
        bccp_manifest_id: BccpManifestId,
    ) -> bool:
        """Return whether any BCC still references the BCCP."""
        count = await self._session.scalar(
            select(func.count()).select_from(BccManifest).where(
                BccManifest.to_bccp_manifest_id == int(bccp_manifest_id)
            )
        )
        return int(count or 0) > 0

    async def discard_asccp(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
    ) -> bool:
        """Permanently delete an ASCCP and its direct related rows."""
        asccp_manifest = await self._session.get(AsccpManifest, int(asccp_manifest_id))
        if asccp_manifest is None:
            return False
        asccp = await self._session.get(Asccp, int(asccp_manifest.asccp_id))
        if asccp is None:
            return False

        asccp_manifest.log_id = None
        await self._session.flush()
        await self._session.execute(
            text("UPDATE log SET prev_log_id = NULL, next_log_id = NULL WHERE reference = :reference"),
            {"reference": str(asccp.guid)},
        )
        await self._session.execute(text("DELETE FROM log WHERE reference = :reference"), {"reference": str(asccp.guid)})
        await self._session.execute(
            text("DELETE FROM module_asccp_manifest WHERE asccp_manifest_id = :asccp_manifest_id"),
            {"asccp_manifest_id": int(asccp_manifest_id)},
        )
        await self._session.execute(
            text("DELETE FROM asccp_manifest_tag WHERE asccp_manifest_id = :asccp_manifest_id"),
            {"asccp_manifest_id": int(asccp_manifest_id)},
        )
        await self._session.execute(
            text("DELETE FROM asccp_manifest WHERE asccp_manifest_id = :asccp_manifest_id"),
            {"asccp_manifest_id": int(asccp_manifest_id)},
        )
        await self._session.execute(text("DELETE FROM asccp WHERE asccp_id = :asccp_id"), {"asccp_id": int(asccp.asccp_id)})
        await self._session.flush()
        return True

    async def discard_bccp(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
    ) -> bool:
        """Permanently delete a BCCP and its direct related rows."""
        bccp_manifest = await self._session.get(BccpManifest, int(bccp_manifest_id))
        if bccp_manifest is None:
            return False
        bccp = await self._session.get(Bccp, int(bccp_manifest.bccp_id))
        if bccp is None:
            return False

        bccp_manifest.log_id = None
        await self._session.flush()
        await self._session.execute(
            text("UPDATE log SET prev_log_id = NULL, next_log_id = NULL WHERE reference = :reference"),
            {"reference": str(bccp.guid)},
        )
        await self._session.execute(text("DELETE FROM log WHERE reference = :reference"), {"reference": str(bccp.guid)})
        await self._session.execute(
            text("DELETE FROM module_bccp_manifest WHERE bccp_manifest_id = :bccp_manifest_id"),
            {"bccp_manifest_id": int(bccp_manifest_id)},
        )
        await self._session.execute(
            text("DELETE FROM bccp_manifest_tag WHERE bccp_manifest_id = :bccp_manifest_id"),
            {"bccp_manifest_id": int(bccp_manifest_id)},
        )
        await self._session.execute(
            text("DELETE FROM bccp_manifest WHERE bccp_manifest_id = :bccp_manifest_id"),
            {"bccp_manifest_id": int(bccp_manifest_id)},
        )
        await self._session.execute(text("DELETE FROM bccp WHERE bccp_id = :bccp_id"), {"bccp_id": int(bccp.bccp_id)})
        await self._session.flush()
        return True

    async def _append_asccp_log(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
        requester_user_id: AppUserId,
        action: str,
        timestamp: datetime,
    ) -> int:
        """Append an ASCCP log row and move the manifest head."""
        asccp_manifest = await self._session.get(AsccpManifest, int(asccp_manifest_id))
        if asccp_manifest is None:
            raise LookupError(
                f"No ASCCP exists with manifest ID {int(asccp_manifest_id)}. Please verify the identifier and try again."
            )
        asccp = await self._session.get(Asccp, int(asccp_manifest.asccp_id))
        if asccp is None:
            raise LookupError(
                f"No ASCCP exists with manifest ID {int(asccp_manifest_id)}. Please verify the identifier and try again."
            )
        log_id = await self._log_repo.append_asccp_log(
            asccp_manifest_id=asccp_manifest_id,
            requester_user_id=requester_user_id,
            action=action,
            timestamp=timestamp,
        )
        return int(log_id)

    async def _append_bccp_log(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
        requester_user_id: AppUserId,
        action: str,
        timestamp: datetime,
    ) -> int:
        """Append a BCCP log row and move the manifest head."""
        bccp_manifest = await self._session.get(BccpManifest, int(bccp_manifest_id))
        if bccp_manifest is None:
            raise LookupError(
                f"No BCCP exists with manifest ID {int(bccp_manifest_id)}. Please verify the identifier and try again."
            )
        bccp = await self._session.get(Bccp, int(bccp_manifest.bccp_id))
        if bccp is None:
            raise LookupError(
                f"No BCCP exists with manifest ID {int(bccp_manifest_id)}. Please verify the identifier and try again."
            )
        log_id = await self._log_repo.append_bccp_log(
            bccp_manifest_id=bccp_manifest_id,
            requester_user_id=requester_user_id,
            action=action,
            timestamp=timestamp,
        )
        return int(log_id)

    @staticmethod
    def _build_asccp_den(*, property_term: str, object_class_term: str) -> str:
        """Build an ASCCP DEN."""
        return f"{property_term}. {object_class_term}"

    @staticmethod
    def _build_bccp_den(*, property_term: str, qualifier: str | None, data_type_term: str) -> str:
        """Build a BCCP DEN."""
        suffix = f"{qualifier}_ {data_type_term}" if qualifier else data_type_term
        return f"{property_term}. {suffix}"

    async def _sync_asccp_related_den(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
        property_term: str,
    ) -> None:
        """Sync the ASCCP DEN and all dependent ASCC DENs."""
        asccp_manifest = await self._session.get(AsccpManifest, int(asccp_manifest_id))
        if asccp_manifest is None:
            return
        role_of_acc_manifest = await self._session.get(AccManifest, int(asccp_manifest.role_of_acc_manifest_id))
        if role_of_acc_manifest is None:
            return
        role_of_acc = await self._session.get(Acc, int(role_of_acc_manifest.acc_id))
        if role_of_acc is None:
            return
        asccp_manifest.den = self._build_asccp_den(
            property_term=property_term,
            object_class_term=str(role_of_acc.object_class_term),
        )
        await self._session.flush()
        ascc_manifests = (
            await self._session.execute(
                select(AsccManifest.ascc_manifest_id, AsccManifest.from_acc_manifest_id)
                .where(AsccManifest.to_asccp_manifest_id == int(asccp_manifest_id))
            )
        ).all()
        for row in ascc_manifests:
            owner_manifest = await self._session.get(AccManifest, int(row.from_acc_manifest_id))
            owner_acc = await self._session.get(Acc, int(owner_manifest.acc_id)) if owner_manifest is not None else None
            ascc_manifest = await self._session.get(AsccManifest, int(row.ascc_manifest_id))
            if owner_acc is None or ascc_manifest is None:
                continue
            ascc_manifest.den = f"{owner_acc.object_class_term}. {asccp_manifest.den}"
        await self._session.flush()

    async def _sync_bccp_related_den(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
        property_term: str,
    ) -> None:
        """Sync the BCCP DEN and all dependent BCC DENs."""
        bccp_manifest = await self._session.get(BccpManifest, int(bccp_manifest_id))
        if bccp_manifest is None:
            return
        dt_manifest = await self._session.get(DtManifest, int(bccp_manifest.bdt_manifest_id))
        dt = await self._session.get(Dt, int(dt_manifest.dt_id)) if dt_manifest is not None else None
        if dt is None:
            return
        bccp_manifest.den = self._build_bccp_den(
            property_term=property_term,
            qualifier=str(dt.qualifier) if dt.qualifier is not None else None,
            data_type_term=str(dt.data_type_term),
        )
        await self._session.flush()
        bcc_manifests = (
            await self._session.execute(
                select(BccManifest.bcc_manifest_id, BccManifest.from_acc_manifest_id)
                .where(BccManifest.to_bccp_manifest_id == int(bccp_manifest_id))
            )
        ).all()
        for row in bcc_manifests:
            owner_manifest = await self._session.get(AccManifest, int(row.from_acc_manifest_id))
            owner_acc = await self._session.get(Acc, int(owner_manifest.acc_id)) if owner_manifest is not None else None
            bcc_manifest = await self._session.get(BccManifest, int(row.bcc_manifest_id))
            if owner_acc is None or bcc_manifest is None:
                continue
            bcc_manifest.den = f"{owner_acc.object_class_term}. {bccp_manifest.den}"
        await self._session.flush()

    async def _add_manifest_tags(
        self,
        *,
        manifest_id: int,
        manifest_model: type[AccManifest] | type[AsccpManifest] | type[BccpManifest],
        component_model: type[Acc] | type[Asccp] | type[Bccp],
        component_id_attr: str,
        tag_link_model: type[AccManifestTag] | type[AsccpManifestTag] | type[BccpManifestTag],
        tag_link_manifest_col: Any,
        requester_user_id: AppUserId,
        tag_id: list[int],
    ) -> bool:
        """Attach tags to a manifest-scoped core component."""
        manifest = await self._session.get(manifest_model, manifest_id)
        if manifest is None:
            return False
        component = await self._session.get(component_model, int(getattr(manifest, component_id_attr)))
        if component is None:
            return False

        tags_by_id: dict[int, Tag] = {}
        for single_tag_id in tag_id:
            tag = await self._session.get(Tag, int(single_tag_id))
            if tag is None:
                raise LookupError(
                    f"No tag exists with ID {int(single_tag_id)}. Please verify the identifier and try again."
                )
            tags_by_id[int(single_tag_id)] = tag
        current_tag_ids = {
            int(value)
            for value in (
                await self._session.scalars(
                    select(tag_link_model.tag_id).where(tag_link_manifest_col == manifest_id)
                )
            ).all()
        }
        tag_ids_to_add = [single_tag_id for single_tag_id in tags_by_id if single_tag_id not in current_tag_ids]
        if not tag_ids_to_add:
            return False

        now = datetime.utcnow()
        for single_tag_id in tag_ids_to_add:
            self._session.add(
                tag_link_model(
                    **{
                        str(tag_link_manifest_col.key): manifest_id,
                        "tag_id": int(single_tag_id),
                        "created_by": int(requester_user_id),
                        "creation_timestamp": now,
                    }
                )
            )
        component.last_updated_by = int(requester_user_id)
        component.last_update_timestamp = now
        await self._session.flush()
        if isinstance(manifest, AsccpManifest):
            await self._append_asccp_log(
                asccp_manifest_id=AsccpManifestId(manifest_id),
                requester_user_id=requester_user_id,
                action="Modified",
                timestamp=now,
            )
        elif isinstance(manifest, BccpManifest):
            await self._append_bccp_log(
                bccp_manifest_id=BccpManifestId(manifest_id),
                requester_user_id=requester_user_id,
                action="Modified",
                timestamp=now,
            )
        await self._session.flush()
        return True

    async def _remove_manifest_tags(
        self,
        *,
        manifest_id: int,
        manifest_model: type[AccManifest] | type[AsccpManifest] | type[BccpManifest],
        component_model: type[Acc] | type[Asccp] | type[Bccp],
        component_id_attr: str,
        table_name: str,
        tag_link_model: type[AccManifestTag] | type[AsccpManifestTag] | type[BccpManifestTag],
        tag_link_manifest_col: Any,
        requester_user_id: AppUserId,
        tag_id: list[int],
    ) -> bool:
        """Remove tags from a manifest-scoped core component."""
        manifest = await self._session.get(manifest_model, manifest_id)
        if manifest is None:
            return False
        component = await self._session.get(component_model, int(getattr(manifest, component_id_attr)))
        if component is None:
            return False
        for single_tag_id in tag_id:
            tag = await self._session.get(Tag, int(single_tag_id))
            if tag is None:
                raise LookupError(
                    f"No tag exists with ID {int(single_tag_id)}. Please verify the identifier and try again."
                )
        current_tag_ids = {
            int(value)
            for value in (
                await self._session.scalars(
                    select(tag_link_model.tag_id).where(tag_link_manifest_col == manifest_id)
                )
            ).all()
        }
        tag_ids_to_remove = [single_tag_id for single_tag_id in tag_id if single_tag_id in current_tag_ids]
        if not tag_ids_to_remove:
            return False

        now = datetime.utcnow()
        await self._session.execute(
            text(
                f"DELETE FROM {table_name} WHERE {tag_link_manifest_col.key} = :manifest_id AND tag_id IN :tag_ids"
            ).bindparams(bindparam("tag_ids", expanding=True)),
            {"manifest_id": manifest_id, "tag_ids": tuple(tag_ids_to_remove)},
        )
        component.last_updated_by = int(requester_user_id)
        component.last_update_timestamp = now
        await self._session.flush()
        if isinstance(manifest, AsccpManifest):
            await self._append_asccp_log(
                asccp_manifest_id=AsccpManifestId(manifest_id),
                requester_user_id=requester_user_id,
                action="Modified",
                timestamp=now,
            )
        elif isinstance(manifest, BccpManifest):
            await self._append_bccp_log(
                bccp_manifest_id=BccpManifestId(manifest_id),
                requester_user_id=requester_user_id,
                action="Modified",
                timestamp=now,
            )
        await self._session.flush()
        return True

    async def list(
        self,
        release_id: ReleaseId,
        dependent_release_ids: list[ReleaseId],
        types: list[str],
        limit: int,
        offset: int,
        sorts: list[tuple[str, Literal["ASC", "DESC"]]],
        den: str | None = None,
        tag: str | None = None,
        creation_timestamp_before: datetime | None = None,
        creation_timestamp_after: datetime | None = None,
        last_update_timestamp_before: datetime | None = None,
        last_update_timestamp_after: datetime | None = None,
    ) -> tuple[int, list[CoreComponentListRow]]:
        """Return a unified paginated list for requested core component types.

        Args:
            release_id: Release identifier used to scope the query.
            dependent_release_ids: Value for `dependent_release_ids`.
            types: Optional component type filter list.
            limit: Maximum number of records to return.
            offset: Number of records to skip before collecting results.
            den: Optional Dictionary Entry Name (DEN) filter.
            tag: Optional tag filter.
            creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
            last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.

        Returns:
            Result of the operation.
        """
        release_ids = [release_id, *[x for x in dependent_release_ids]]
        union_queries: list[Select[Any]] = []

        if "ACC" in types:
            union_queries.append(
                self._build_component_select(
                    component_type="ACC",
                    manifest_model=AccManifest,
                    component_model=Acc,
                    manifest_id_col=AccManifest.acc_manifest_id,
                    manifest_release_col=AccManifest.release_id,
                    manifest_log_col=AccManifest.log_id,
                    manifest_component_id_col=AccManifest.acc_id,
                    component_id_col=Acc.acc_id,
                    component_namespace_col=Acc.namespace_id,
                    component_owner_col=Acc.owner_user_id,
                    component_created_by_col=Acc.created_by,
                    component_updated_by_col=Acc.last_updated_by,
                    guid_col=Acc.guid,
                    den_col=AccManifest.den,
                    name_col=Acc.object_class_term,
                    definition_col=Acc.definition,
                    definition_source_col=Acc.definition_source,
                    state_col=Acc.state,
                    is_deprecated_col=Acc.is_deprecated,
                    creation_ts_col=Acc.creation_timestamp,
                    update_ts_col=Acc.last_update_timestamp,
                    tag_link_model=AccManifestTag,
                    tag_link_manifest_col=AccManifestTag.acc_manifest_id,
                    release_ids=release_ids,
                    den=den,
                    tag=tag,
                    creation_timestamp_before=creation_timestamp_before,
            creation_timestamp_after=creation_timestamp_after,
                    last_update_timestamp_before=last_update_timestamp_before,
            last_update_timestamp_after=last_update_timestamp_after,
                )
            )

        if "ASCCP" in types:
            union_queries.append(
                self._build_component_select(
                    component_type="ASCCP",
                    manifest_model=AsccpManifest,
                    component_model=Asccp,
                    manifest_id_col=AsccpManifest.asccp_manifest_id,
                    manifest_release_col=AsccpManifest.release_id,
                    manifest_log_col=AsccpManifest.log_id,
                    manifest_component_id_col=AsccpManifest.asccp_id,
                    component_id_col=Asccp.asccp_id,
                    component_namespace_col=Asccp.namespace_id,
                    component_owner_col=Asccp.owner_user_id,
                    component_created_by_col=Asccp.created_by,
                    component_updated_by_col=Asccp.last_updated_by,
                    guid_col=Asccp.guid,
                    den_col=AsccpManifest.den,
                    name_col=Asccp.property_term,
                    definition_col=Asccp.definition,
                    definition_source_col=Asccp.definition_source,
                    state_col=Asccp.state,
                    is_deprecated_col=Asccp.is_deprecated,
                    creation_ts_col=Asccp.creation_timestamp,
                    update_ts_col=Asccp.last_update_timestamp,
                    tag_link_model=AsccpManifestTag,
                    tag_link_manifest_col=AsccpManifestTag.asccp_manifest_id,
                    release_ids=release_ids,
                    den=den,
                    tag=tag,
                    creation_timestamp_before=creation_timestamp_before,
            creation_timestamp_after=creation_timestamp_after,
                    last_update_timestamp_before=last_update_timestamp_before,
            last_update_timestamp_after=last_update_timestamp_after,
                )
            )

        if "BCCP" in types:
            union_queries.append(
                self._build_component_select(
                    component_type="BCCP",
                    manifest_model=BccpManifest,
                    component_model=Bccp,
                    manifest_id_col=BccpManifest.bccp_manifest_id,
                    manifest_release_col=BccpManifest.release_id,
                    manifest_log_col=BccpManifest.log_id,
                    manifest_component_id_col=BccpManifest.bccp_id,
                    component_id_col=Bccp.bccp_id,
                    component_namespace_col=Bccp.namespace_id,
                    component_owner_col=Bccp.owner_user_id,
                    component_created_by_col=Bccp.created_by,
                    component_updated_by_col=Bccp.last_updated_by,
                    guid_col=Bccp.guid,
                    den_col=BccpManifest.den,
                    name_col=Bccp.property_term,
                    definition_col=Bccp.definition,
                    definition_source_col=Bccp.definition_source,
                    state_col=Bccp.state,
                    is_deprecated_col=Bccp.is_deprecated,
                    creation_ts_col=Bccp.creation_timestamp,
                    update_ts_col=Bccp.last_update_timestamp,
                    tag_link_model=BccpManifestTag,
                    tag_link_manifest_col=BccpManifestTag.bccp_manifest_id,
                    release_ids=release_ids,
                    den=den,
                    tag=tag,
                    creation_timestamp_before=creation_timestamp_before,
            creation_timestamp_after=creation_timestamp_after,
                    last_update_timestamp_before=last_update_timestamp_before,
            last_update_timestamp_after=last_update_timestamp_after,
                )
            )

        if not union_queries:
            return 0, []

        query = union_queries[0] if len(union_queries) == 1 else union_all(*union_queries)
        sub = query.subquery("q")
        total_stmt = select(func.count()).select_from(sub)
        total = int((await self._session.execute(total_stmt)).scalar_one())

        sort_map = {
            "den": sub.c.den,
            "name": sub.c.name,
            "definition": sub.c.definition,
            "creation_timestamp": sub.c.creation_timestamp,
            "last_update_timestamp": sub.c.last_update_timestamp,
        }
        order_by = []
        for sort in sorts:
            col = sort_map.get(sort[0])
            if col is not None:
                order_by.append(col.desc() if sort[1] == "DESC" else col.asc())
        if not order_by:
            order_by = [sub.c.creation_timestamp.desc()]

        rows = [
            dict(row._mapping)
            for row in (
                await self._session.execute(
                    select(sub).order_by(*order_by).limit(limit).offset(offset)
                )
            ).all()
        ]
        items = [self._to_core_component_raw(row) for row in rows]
        return total, items

    async def _sync_acc_related_den(
        self,
        *,
        acc_manifest_id: AccManifestId,
        object_class_term: str,
    ) -> None:
        """Propagate ACC object-class-term changes to related DEN values."""
        ascc_manifest_rows = (
            await self._session.execute(
                select(AsccManifest).where(AsccManifest.from_acc_manifest_id == int(acc_manifest_id))
            )
        ).scalars().all()
        for ascc_manifest in ascc_manifest_rows:
            asccp_den = await self._session.scalar(
                select(AsccpManifest.den).where(
                    AsccpManifest.asccp_manifest_id == int(ascc_manifest.to_asccp_manifest_id)
                )
            )
            if asccp_den is not None:
                ascc_manifest.den = f"{object_class_term}. {asccp_den}"

        bcc_manifest_rows = (
            await self._session.execute(
                select(BccManifest).where(BccManifest.from_acc_manifest_id == int(acc_manifest_id))
            )
        ).scalars().all()
        for bcc_manifest in bcc_manifest_rows:
            bccp_den = await self._session.scalar(
                select(BccpManifest.den).where(
                    BccpManifest.bccp_manifest_id == int(bcc_manifest.to_bccp_manifest_id)
                )
            )
            if bccp_den is not None:
                bcc_manifest.den = f"{object_class_term}. {bccp_den}"

        role_of_asccp_rows = (
            await self._session.execute(
                select(AsccpManifest).where(AsccpManifest.role_of_acc_manifest_id == int(acc_manifest_id))
            )
        ).scalars().all()
        for asccp_manifest in role_of_asccp_rows:
            asccp = await self._session.get(Asccp, int(asccp_manifest.asccp_id))
            if asccp is None:
                continue
            asccp_manifest.den = f"{asccp.property_term}. {object_class_term}"

            inbound_ascc_rows = (
                await self._session.execute(
                    select(AsccManifest).where(
                        AsccManifest.to_asccp_manifest_id == int(asccp_manifest.asccp_manifest_id)
                    )
                )
            ).scalars().all()
            for inbound_ascc in inbound_ascc_rows:
                source_object_class_term = await self._session.scalar(
                    select(Acc.object_class_term)
                    .join(AccManifest, Acc.acc_id == AccManifest.acc_id)
                    .where(AccManifest.acc_manifest_id == int(inbound_ascc.from_acc_manifest_id))
                )
                if source_object_class_term is not None:
                    inbound_ascc.den = f"{source_object_class_term}. {asccp_manifest.den}"

    async def _move_seq_key(
        self,
        *,
        from_acc_manifest_id: AccManifestId,
        new_seq_key_id: int,
        index: int,
    ) -> None:
        """Insert a `seq_key` node into the ACC relationship chain at the requested index."""
        seq_keys = (
            await self._session.execute(
                select(SeqKey).where(SeqKey.from_acc_manifest_id == int(from_acc_manifest_id))
            )
        ).scalars().all()
        if not seq_keys:
            return

        seq_key_by_id = {int(seq_key.seq_key_id): seq_key for seq_key in seq_keys}
        existing_seq_keys = [seq_key for seq_key in seq_keys if int(seq_key.seq_key_id) != int(new_seq_key_id)]
        ordered_existing = self._order_seq_keys(existing_seq_keys)
        if index < -1:
            raise ValueError("`index` must be -1 or a zero-based index.")
        if index > len(ordered_existing):
            raise ValueError(
                f"`index` is out of range for the ACC sequence. Allowed values are 0 to {len(ordered_existing)}, or -1 for the end."
            )
        insert_at = len(ordered_existing) if index < 0 else int(index)
        ordered_all = ordered_existing[:insert_at] + [seq_key_by_id[int(new_seq_key_id)]] + ordered_existing[insert_at:]

        for index, seq_key in enumerate(ordered_all):
            seq_key.prev_seq_key_id = int(ordered_all[index - 1].seq_key_id) if index > 0 else None
            seq_key.next_seq_key_id = int(ordered_all[index + 1].seq_key_id) if index + 1 < len(ordered_all) else None

    async def _place_seq_key_after(
        self,
        *,
        from_acc_manifest_id: AccManifestId,
        current_seq_key_id: int,
        after_seq_key_id: int | None,
    ) -> None:
        """Move an existing `seq_key` node after another node, or to the first position."""
        seq_keys = (
            await self._session.execute(
                select(SeqKey).where(SeqKey.from_acc_manifest_id == int(from_acc_manifest_id))
            )
        ).scalars().all()
        if not seq_keys:
            return

        seq_key_by_id = {int(seq_key.seq_key_id): seq_key for seq_key in seq_keys}
        current = seq_key_by_id.get(int(current_seq_key_id))
        if current is None:
            raise LookupError("The association to move is missing its sequence key.")

        ordered = [seq_key for seq_key in self._order_seq_keys(seq_keys) if int(seq_key.seq_key_id) != int(current_seq_key_id)]
        if after_seq_key_id is None:
            insert_at = 0
        else:
            after_index = next(
                (
                    index
                    for index, seq_key in enumerate(ordered)
                    if int(seq_key.seq_key_id) == int(after_seq_key_id)
                ),
                None,
            )
            if after_index is None:
                raise LookupError("The target `after` association is missing its sequence key.")
            insert_at = after_index + 1

        ordered_all = ordered[:insert_at] + [current] + ordered[insert_at:]
        for index, seq_key in enumerate(ordered_all):
            seq_key.prev_seq_key_id = int(ordered_all[index - 1].seq_key_id) if index > 0 else None
            seq_key.next_seq_key_id = int(ordered_all[index + 1].seq_key_id) if index + 1 < len(ordered_all) else None

    async def _resolve_seq_key_id(
        self,
        *,
        acc_manifest_id: AccManifestId,
        ascc_manifest_id: AsccManifestId | None = None,
        bcc_manifest_id: BccManifestId | None = None,
    ) -> int | None:
        """Resolve a sequence-key ID for an ASCC/BCC manifest scoped to an ACC."""
        if ascc_manifest_id is not None:
            return await self._session.scalar(
                select(AsccManifest.seq_key_id).where(
                    AsccManifest.ascc_manifest_id == int(ascc_manifest_id),
                    AsccManifest.from_acc_manifest_id == int(acc_manifest_id),
                )
            )
        if bcc_manifest_id is not None:
            return await self._session.scalar(
                select(BccManifest.seq_key_id).where(
                    BccManifest.bcc_manifest_id == int(bcc_manifest_id),
                    BccManifest.from_acc_manifest_id == int(acc_manifest_id),
                )
            )
        return None

    async def _revise_asccs(
        self,
        *,
        from_acc_manifest_id: AccManifestId,
        next_acc_id: int,
        requester_user_id: AppUserId,
        timestamp: datetime,
    ) -> None:
        """Clone ASCC rows for an ACC revision and retarget their manifests."""
        ascc_manifests = (
            await self._session.execute(
                select(AsccManifest).where(AsccManifest.from_acc_manifest_id == int(from_acc_manifest_id))
            )
        ).scalars().all()
        for ascc_manifest in ascc_manifests:
            ascc = await self._session.get(Ascc, int(ascc_manifest.ascc_id))
            if ascc is None:
                continue
            next_ascc = Ascc(
                guid=str(ascc.guid),
                cardinality_min=ascc.cardinality_min,
                cardinality_max=ascc.cardinality_max,
                seq_key=ascc.seq_key,
                from_acc_id=int(next_acc_id),
                to_asccp_id=int(ascc.to_asccp_id),
                definition=ascc.definition,
                definition_source=ascc.definition_source,
                is_deprecated=bool(ascc.is_deprecated),
                replacement_ascc_id=ascc.replacement_ascc_id,
                created_by=int(requester_user_id),
                owner_user_id=int(requester_user_id),
                last_updated_by=int(requester_user_id),
                creation_timestamp=timestamp,
                last_update_timestamp=timestamp,
                state="WIP",
                prev_ascc_id=int(ascc.ascc_id),
                next_ascc_id=None,
            )
            self._session.add(next_ascc)
            await self._session.flush()
            ascc.next_ascc_id = int(next_ascc.ascc_id)
            ascc_manifest.ascc_id = int(next_ascc.ascc_id)

    async def _revise_bccs(
        self,
        *,
        from_acc_manifest_id: AccManifestId,
        next_acc_id: int,
        requester_user_id: AppUserId,
        timestamp: datetime,
    ) -> None:
        """Clone BCC rows for an ACC revision and retarget their manifests."""
        bcc_manifests = (
            await self._session.execute(
                select(BccManifest).where(BccManifest.from_acc_manifest_id == int(from_acc_manifest_id))
            )
        ).scalars().all()
        for bcc_manifest in bcc_manifests:
            bcc = await self._session.get(Bcc, int(bcc_manifest.bcc_id))
            if bcc is None:
                continue
            next_bcc = Bcc(
                guid=str(bcc.guid),
                cardinality_min=bcc.cardinality_min,
                cardinality_max=bcc.cardinality_max,
                to_bccp_id=int(bcc.to_bccp_id),
                from_acc_id=int(next_acc_id),
                seq_key=bcc.seq_key,
                entity_type=bcc.entity_type,
                definition=bcc.definition,
                definition_source=bcc.definition_source,
                created_by=int(requester_user_id),
                owner_user_id=int(requester_user_id),
                last_updated_by=int(requester_user_id),
                creation_timestamp=timestamp,
                last_update_timestamp=timestamp,
                state="WIP",
                is_deprecated=bool(bcc.is_deprecated),
                replacement_bcc_id=bcc.replacement_bcc_id,
                is_nillable=bool(bcc.is_nillable),
                default_value=bcc.default_value,
                fixed_value=bcc.fixed_value,
                prev_bcc_id=int(bcc.bcc_id),
                next_bcc_id=None,
            )
            self._session.add(next_bcc)
            await self._session.flush()
            bcc.next_bcc_id = int(next_bcc.bcc_id)
            bcc_manifest.bcc_id = int(next_bcc.bcc_id)

    async def _discard_revision_associations(
        self,
        *,
        acc_manifest_id: AccManifestId,
        current_acc_id: int,
    ) -> None:
        """Restore manifests to their previous association rows and delete revision-only rows."""
        await self._session.execute(
            text(
                "UPDATE seq_key SET prev_seq_key_id = NULL, next_seq_key_id = NULL "
                "WHERE from_acc_manifest_id = :acc_manifest_id"
            ),
            {"acc_manifest_id": int(acc_manifest_id)},
        )
        await self._session.execute(
            text("UPDATE ascc_manifest SET seq_key_id = NULL WHERE from_acc_manifest_id = :acc_manifest_id"),
            {"acc_manifest_id": int(acc_manifest_id)},
        )
        await self._session.execute(
            text("UPDATE bcc_manifest SET seq_key_id = NULL WHERE from_acc_manifest_id = :acc_manifest_id"),
            {"acc_manifest_id": int(acc_manifest_id)},
        )
        await self._session.execute(
            text("DELETE FROM seq_key WHERE from_acc_manifest_id = :acc_manifest_id"),
            {"acc_manifest_id": int(acc_manifest_id)},
        )

        ascc_manifests = (
            await self._session.execute(
                select(AsccManifest).where(AsccManifest.from_acc_manifest_id == int(acc_manifest_id))
            )
        ).scalars().all()
        ascc_manifests_to_delete: list[AsccManifest] = []
        ascc_rows_to_delete: list[Ascc] = []
        for ascc_manifest in ascc_manifests:
            ascc = await self._session.get(Ascc, int(ascc_manifest.ascc_id))
            if ascc is None:
                continue
            if ascc.prev_ascc_id is None:
                ascc_manifests_to_delete.append(ascc_manifest)
                ascc_rows_to_delete.append(ascc)
                continue
            prev_ascc = await self._session.get(Ascc, int(ascc.prev_ascc_id))
            if prev_ascc is not None:
                prev_ascc.next_ascc_id = None
                ascc_manifest.ascc_id = int(prev_ascc.ascc_id)
                ascc_manifest.seq_key_id = None
            ascc_rows_to_delete.append(ascc)

        await self._session.flush()
        for ascc_manifest in ascc_manifests_to_delete:
            await self._session.delete(ascc_manifest)
        await self._session.flush()
        for ascc in ascc_rows_to_delete:
            await self._session.delete(ascc)
        await self._session.flush()

        bcc_manifests = (
            await self._session.execute(
                select(BccManifest).where(BccManifest.from_acc_manifest_id == int(acc_manifest_id))
            )
        ).scalars().all()
        bcc_manifests_to_delete: list[BccManifest] = []
        bcc_rows_to_delete: list[Bcc] = []
        for bcc_manifest in bcc_manifests:
            bcc = await self._session.get(Bcc, int(bcc_manifest.bcc_id))
            if bcc is None:
                continue
            if bcc.prev_bcc_id is None:
                bcc_manifests_to_delete.append(bcc_manifest)
                bcc_rows_to_delete.append(bcc)
                continue
            prev_bcc = await self._session.get(Bcc, int(bcc.prev_bcc_id))
            if prev_bcc is not None:
                prev_bcc.next_bcc_id = None
                bcc_manifest.bcc_id = int(prev_bcc.bcc_id)
                bcc_manifest.seq_key_id = None
            bcc_rows_to_delete.append(bcc)

        await self._session.flush()
        for bcc_manifest in bcc_manifests_to_delete:
            await self._session.delete(bcc_manifest)
        await self._session.flush()
        for bcc in bcc_rows_to_delete:
            await self._session.delete(bcc)
        await self._session.flush()

    async def _rebuild_seq_keys_from_guid_order(
        self,
        *,
        acc_manifest_id: AccManifestId,
        ordered_guids: list[str],
    ) -> None:
        """Recreate ACC seq_key rows using the stable log association order."""
        guid_to_entry: dict[str, tuple[str, int]] = {}

        ascc_entries = (
            await self._session.execute(
                select(AsccManifest.ascc_manifest_id, Ascc.guid)
                .join(Ascc, Ascc.ascc_id == AsccManifest.ascc_id)
                .where(AsccManifest.from_acc_manifest_id == int(acc_manifest_id))
            )
        ).all()
        for row in ascc_entries:
            guid_to_entry[str(row.guid)] = ("ASCC", int(row.ascc_manifest_id))

        bcc_entries = (
            await self._session.execute(
                select(BccManifest.bcc_manifest_id, Bcc.guid)
                .join(Bcc, Bcc.bcc_id == BccManifest.bcc_id)
                .where(BccManifest.from_acc_manifest_id == int(acc_manifest_id))
            )
        ).all()
        for row in bcc_entries:
            guid_to_entry[str(row.guid)] = ("BCC", int(row.bcc_manifest_id))

        ordered_entries: list[tuple[str, int]] = []
        seen: set[str] = set()
        for guid in ordered_guids:
            entry = guid_to_entry.get(str(guid))
            if entry is None:
                continue
            ordered_entries.append(entry)
            seen.add(str(guid))
        for guid, entry in sorted(guid_to_entry.items(), key=lambda item: item[1][1]):
            if guid not in seen:
                ordered_entries.append(entry)

        prev_seq_key_id: int | None = None
        for component_type, manifest_id in ordered_entries:
            seq_key = SeqKey(
                from_acc_manifest_id=int(acc_manifest_id),
                ascc_manifest_id=manifest_id if component_type == "ASCC" else None,
                bcc_manifest_id=manifest_id if component_type == "BCC" else None,
                prev_seq_key_id=prev_seq_key_id,
                next_seq_key_id=None,
            )
            self._session.add(seq_key)
            await self._session.flush()

            if component_type == "ASCC":
                ascc_manifest = await self._session.get(AsccManifest, int(manifest_id))
                if ascc_manifest is not None:
                    ascc_manifest.seq_key_id = int(seq_key.seq_key_id)
            else:
                bcc_manifest = await self._session.get(BccManifest, int(manifest_id))
                if bcc_manifest is not None:
                    bcc_manifest.seq_key_id = int(seq_key.seq_key_id)

            if prev_seq_key_id is not None:
                prev_seq_key = await self._session.get(SeqKey, int(prev_seq_key_id))
                if prev_seq_key is not None:
                    prev_seq_key.next_seq_key_id = int(seq_key.seq_key_id)
            prev_seq_key_id = int(seq_key.seq_key_id)

    @staticmethod
    def _order_seq_keys(seq_keys: list[SeqKey]) -> list[SeqKey]:
        """Return `seq_key` rows in linked-list order, appending any orphan nodes last."""
        if not seq_keys:
            return []

        seq_key_by_id = {int(seq_key.seq_key_id): seq_key for seq_key in seq_keys}
        head_candidates = [
            seq_key
            for seq_key in seq_keys
            if seq_key.prev_seq_key_id is None or int(seq_key.prev_seq_key_id) not in seq_key_by_id
        ]
        current = min(head_candidates or seq_keys, key=lambda seq_key: int(seq_key.seq_key_id))

        ordered: list[SeqKey] = []
        visited: set[int] = set()
        while current is not None:
            seq_key_id = int(current.seq_key_id)
            if seq_key_id in visited:
                break
            visited.add(seq_key_id)
            ordered.append(current)
            next_seq_key_id = current.next_seq_key_id
            if next_seq_key_id is None:
                break
            current = seq_key_by_id.get(int(next_seq_key_id))

        remaining = sorted(
            (seq_key for seq_key in seq_keys if int(seq_key.seq_key_id) not in visited),
            key=lambda seq_key: int(seq_key.seq_key_id),
        )
        ordered.extend(remaining)
        return ordered

    async def get_acc(self, acc_manifest_id: AccManifestId) -> GetAccRow | None:
        """Get ACC details using score-mcp-server compatible response shape.

        Args:
            acc_manifest_id: ACC manifest identifier.

        Returns:
            Result of the operation.
        """
        core = await self._fetch_one_core("ACC", acc_manifest_id)
        if core is None:
            return None

        extra = (
            await self._session.execute(
                select(
                    AccManifest.based_acc_manifest_id,
                    Acc.object_class_term,
                    Acc.object_class_qualifier,
                    Acc.oagis_component_type,
                    Acc.is_abstract,
                    Acc.type,
                )
                .select_from(AccManifest)
                .join(Acc, Acc.acc_id == AccManifest.acc_id)
                .where(AccManifest.acc_manifest_id == acc_manifest_id)
            )
        ).first()
        if extra is None:
            return None

        base_acc: BaseAccInfoRow | None = None
        based_acc_manifest_id = int(extra[0]) if extra[0] is not None else None
        if based_acc_manifest_id is not None:
            base_row = (
                await self._session.execute(
                    select(
                        AccManifest.acc_manifest_id,
                        Acc.acc_id,
                        Acc.guid,
                        AccManifest.den,
                        Acc.object_class_term,
                        Acc.type,
                        Acc.definition,
                        Acc.definition_source,
                        Namespace.namespace_id,
                        Namespace.prefix.label("namespace_prefix"),
                        Namespace.uri.label("namespace_uri"),
                        Library.library_id,
                        Library.name.label("library_name"),
                        Release.release_id,
                        Release.release_num,
                        Release.state.label("release_state"),
                    )
                    .select_from(AccManifest)
                    .join(Acc, Acc.acc_id == AccManifest.acc_id)
                    .join(Release, Release.release_id == AccManifest.release_id)
                    .join(Library, Library.library_id == Release.library_id)
                    .outerjoin(Namespace, Namespace.namespace_id == Acc.namespace_id)
                    .where(AccManifest.acc_manifest_id == based_acc_manifest_id)
                )
            ).first()
            if base_row is not None:
                br = dict(base_row._mapping)
                base_acc = BaseAccInfoRow(
                    acc_manifest_id=int(br["acc_manifest_id"]),
                    acc_id=int(br["acc_id"]),
                    guid=str(br["guid"]),
                    den=str(br["den"]),
                    object_class_term=str(br["object_class_term"]),
                    type=str(br["type"]) if br.get("type") is not None else None,
                    definition=str(br["definition"]) if br.get("definition") is not None else None,
                    definition_source=str(br["definition_source"]) if br.get("definition_source") is not None else None,
                    namespace=_namespace_summary(br),
                    library=_library_summary(br),
                    release=_release_summary(br),
                )

        relationships = await self.get_acc_relationships(acc_manifest_id)
        tags = await self._get_manifest_tags(
            tag_link_model=AccManifestTag,
            tag_link_manifest_col=AccManifestTag.acc_manifest_id,
            manifest_id=int(acc_manifest_id),
        )

        return GetAccRow(
            acc_manifest_id=acc_manifest_id,
            acc_id=core.component_id,
            base_acc=base_acc,
            relationships=relationships,
            guid=core.guid,
            den=core.den,
            object_class_term=str(extra[1]) if extra[1] is not None else (core.name or ""),
            definition=core.definition,
            definition_source=core.definition_source,
            object_class_qualifier=str(extra[2]) if extra[2] is not None else None,
            component_type=int(extra[3]) if extra[3] is not None else None,
            is_abstract=bool(extra[4]),
            is_deprecated=core.is_deprecated,
            state=core.state,
            namespace=core.namespace,
            library=core.library,
            release=core.release,
            tags=tags,
            log=core.log,
            owner_user_id=core.owner_user_id,
            created_by=core.created_by,
            creation_timestamp=core.creation_timestamp,
            last_updated_by=core.last_updated_by,
            last_update_timestamp=core.last_update_timestamp,
        )

    async def get_acc_relationships(
        self,
        acc_manifest_id: AccManifestId,
    ) -> list[AsccRelationshipInfoRow | BccRelationshipInfoRow]:
        """Get ACC relationships in canonical `seq_key` order."""
        core = await self._fetch_one_core("ACC", acc_manifest_id)
        if core is None:
            return []

        from_acc = AccInfoRow(
            acc_manifest_id=acc_manifest_id,
            acc_id=core.component_id,
            guid=core.guid,
            den=core.den,
            object_class_term=core.name or "",
            definition=core.definition,
            definition_source=core.definition_source,
            is_deprecated=core.is_deprecated,
        )

        # `seq_key` defines canonical ACC relationship order as a linked list.
        ordered_seq_rows = await self._load_ordered_seq_rows(acc_manifest_id)
        ascc_rows_by_manifest_id, bcc_rows_by_manifest_id = await self._load_acc_relationship_rows(ordered_seq_rows)
        return self._build_acc_relationships(
            ordered_seq_rows=ordered_seq_rows,
            ascc_rows_by_manifest_id=ascc_rows_by_manifest_id,
            bcc_rows_by_manifest_id=bcc_rows_by_manifest_id,
            from_acc=from_acc,
        )

    async def _load_ordered_seq_rows(self, from_acc_manifest_id: AccManifestId) -> list[dict[str, Any]]:
        """Load `seq_key` rows for an ACC and return them in linked-list order."""
        seq_rows = [
            dict(row._mapping)
            for row in (
                await self._session.execute(
                    select(
                        SeqKey.seq_key_id,
                        SeqKey.ascc_manifest_id,
                        SeqKey.bcc_manifest_id,
                        SeqKey.prev_seq_key_id,
                        SeqKey.next_seq_key_id,
                    ).where(SeqKey.from_acc_manifest_id == from_acc_manifest_id)
                )
            ).all()
        ]
        if not seq_rows:
            return []

        # Traverse the explicit prev/next chain and guard against cyclic data.
        seq_by_id = {int(row["seq_key_id"]): row for row in seq_rows}
        head_candidates = [row for row in seq_rows if row.get("prev_seq_key_id") is None]
        current = (
            min(head_candidates, key=lambda row: int(row["seq_key_id"]))
            if head_candidates
            else min(seq_rows, key=lambda row: int(row["seq_key_id"]))
        )

        ordered_seq_rows: list[dict[str, Any]] = []
        visited_seq_ids: set[int] = set()
        while current is not None:
            seq_key_id = int(current["seq_key_id"])
            if seq_key_id in visited_seq_ids:
                break
            visited_seq_ids.add(seq_key_id)
            ordered_seq_rows.append(current)
            next_seq_key_id = current.get("next_seq_key_id")
            if next_seq_key_id is None:
                break
            current = seq_by_id.get(int(next_seq_key_id))
        return ordered_seq_rows

    async def _load_acc_relationship_rows(
        self,
        ordered_seq_rows: list[dict[str, Any]],
    ) -> tuple[dict[int, dict[str, Any]], dict[int, dict[str, Any]]]:
        """Load ASCC/BCC detail rows needed to materialize ACC relationships."""
        ascc_manifest_ids = [
            int(row["ascc_manifest_id"])
            for row in ordered_seq_rows
            if row.get("ascc_manifest_id") is not None
        ]
        bcc_manifest_ids = [
            int(row["bcc_manifest_id"])
            for row in ordered_seq_rows
            if row.get("bcc_manifest_id") is not None
        ]
        ascc_rows = await self._load_ascc_rows_by_manifest_id(ascc_manifest_ids)
        bcc_rows = await self._load_bcc_rows_by_manifest_id(bcc_manifest_ids)
        return ascc_rows, bcc_rows

    async def _load_ascc_rows_by_manifest_id(self, ascc_manifest_ids: list[int]) -> dict[int, dict[str, Any]]:
        """Load ASCC relationship rows keyed by ASCC manifest ID."""
        if not ascc_manifest_ids:
            return {}
        rows = [
            dict(result_row._mapping)
            for result_row in (
                await self._session.execute(
                    select(
                        AsccManifest.ascc_manifest_id,
                        Ascc.ascc_id,
                        Ascc.guid,
                        AsccManifest.den,
                        Ascc.cardinality_min,
                        Ascc.cardinality_max,
                        Ascc.is_deprecated,
                        Ascc.definition,
                        Ascc.definition_source,
                        AsccpManifest.asccp_manifest_id,
                        AsccpManifest.role_of_acc_manifest_id,
                        Asccp.asccp_id,
                        Asccp.guid.label("asccp_guid"),
                        AsccpManifest.den.label("asccp_den"),
                        Asccp.property_term,
                        Asccp.definition.label("asccp_definition"),
                        Asccp.definition_source.label("asccp_definition_source"),
                        Asccp.is_deprecated.label("asccp_is_deprecated"),
                    )
                    .select_from(AsccManifest)
                    .join(Ascc, Ascc.ascc_id == AsccManifest.ascc_id)
                    .join(AsccpManifest, AsccpManifest.asccp_manifest_id == AsccManifest.to_asccp_manifest_id)
                    .join(Asccp, Asccp.asccp_id == AsccpManifest.asccp_id)
                    .where(AsccManifest.ascc_manifest_id.in_(ascc_manifest_ids))
                )
            ).all()
        ]
        return {int(row["ascc_manifest_id"]): row for row in rows}

    async def _load_bcc_rows_by_manifest_id(self, bcc_manifest_ids: list[int]) -> dict[int, dict[str, Any]]:
        """Load BCC relationship rows keyed by BCC manifest ID."""
        if not bcc_manifest_ids:
            return {}
        rows = [
            dict(result_row._mapping)
            for result_row in (
                await self._session.execute(
                    select(
                        BccManifest.bcc_manifest_id,
                        Bcc.bcc_id,
                        Bcc.guid,
                        BccManifest.den,
                        Bcc.cardinality_min,
                        Bcc.cardinality_max,
                        Bcc.entity_type,
                        Bcc.is_nillable,
                        Bcc.default_value,
                        Bcc.fixed_value,
                        Bcc.is_deprecated,
                        Bcc.definition,
                        Bcc.definition_source,
                        BccpManifest.bccp_manifest_id,
                        Bccp.bccp_id,
                        Bccp.guid.label("bccp_guid"),
                        BccpManifest.den.label("bccp_den"),
                        Bccp.property_term,
                        Bccp.representation_term,
                        Bccp.definition.label("bccp_definition"),
                        Bccp.definition_source.label("bccp_definition_source"),
                        Bccp.is_deprecated.label("bccp_is_deprecated"),
                        DtManifest.dt_manifest_id,
                        DtManifest.dt_id,
                        DtManifest.based_dt_manifest_id,
                        DtManifest.den.label("dt_den"),
                        Dt.guid.label("dt_guid"),
                        Dt.data_type_term,
                        Dt.qualifier,
                        Dt.representation_term.label("dt_representation_term"),
                        Dt.six_digit_id,
                        Dt.definition.label("dt_definition"),
                        Dt.definition_source.label("dt_definition_source"),
                        Dt.content_component_definition,
                        Dt.is_deprecated.label("dt_is_deprecated"),
                        Namespace.namespace_id.label("dt_namespace_id"),
                        Namespace.prefix.label("dt_namespace_prefix"),
                        Namespace.uri.label("dt_namespace_uri"),
                        Library.library_id.label("dt_library_id"),
                        Library.name.label("dt_library_name"),
                        Release.release_id.label("dt_release_id"),
                        Release.release_num.label("dt_release_num"),
                        Release.state.label("dt_release_state"),
                    )
                    .select_from(BccManifest)
                    .join(Bcc, Bcc.bcc_id == BccManifest.bcc_id)
                    .join(BccpManifest, BccpManifest.bccp_manifest_id == BccManifest.to_bccp_manifest_id)
                    .join(Bccp, Bccp.bccp_id == BccpManifest.bccp_id)
                    .join(DtManifest, DtManifest.dt_manifest_id == BccpManifest.bdt_manifest_id)
                    .join(Dt, Dt.dt_id == DtManifest.dt_id)
                    .join(Release, Release.release_id == DtManifest.release_id)
                    .join(Library, Library.library_id == Release.library_id)
                    .outerjoin(Namespace, Namespace.namespace_id == Dt.namespace_id)
                    .where(BccManifest.bcc_manifest_id.in_(bcc_manifest_ids))
                )
            ).all()
        ]
        return {int(row["bcc_manifest_id"]): row for row in rows}

    def _build_acc_relationships(
        self,
        ordered_seq_rows: list[dict[str, Any]],
        ascc_rows_by_manifest_id: dict[int, dict[str, Any]],
        bcc_rows_by_manifest_id: dict[int, dict[str, Any]],
        from_acc: AccInfoRow,
    ) -> list[AsccRelationshipInfoRow | BccRelationshipInfoRow]:
        """Build ACC relationship DTOs in `seq_key` order."""
        relationships: list[AsccRelationshipInfoRow | BccRelationshipInfoRow] = []
        for seq_row in ordered_seq_rows:
            ascc_manifest_id = seq_row.get("ascc_manifest_id")
            if ascc_manifest_id is not None:
                row = ascc_rows_by_manifest_id.get(int(ascc_manifest_id))
                if row is not None:
                    relationships.append(self._to_ascc_relationship(row, from_acc))
                continue
            bcc_manifest_id = seq_row.get("bcc_manifest_id")
            if bcc_manifest_id is None:
                continue
            row = bcc_rows_by_manifest_id.get(int(bcc_manifest_id))
            if row is not None:
                relationships.append(self._to_bcc_relationship(row, from_acc))
        return relationships

    def _to_ascc_relationship(self, row: dict[str, Any], from_acc: AccInfoRow) -> AsccRelationshipInfoRow:
        """Convert one ASCC row into relationship DTO."""
        return AsccRelationshipInfoRow(
            ascc_manifest_id=row["ascc_manifest_id"],
            ascc_id=row["ascc_id"],
            guid=str(row["guid"]),
            den=str(row["den"]),
            cardinality_min=row["cardinality_min"],
            cardinality_max=row["cardinality_max"],
            is_deprecated=bool(row["is_deprecated"]),
            definition=str(row["definition"]) if row.get("definition") is not None else None,
            definition_source=str(row["definition_source"]) if row.get("definition_source") is not None else None,
            from_acc=from_acc,
            to_asccp=AsccpInfoRow(
                asccp_manifest_id=row["asccp_manifest_id"],
                asccp_id=row["asccp_id"],
                role_of_acc_manifest_id=row["role_of_acc_manifest_id"],
                guid=str(row["asccp_guid"]),
                den=str(row["asccp_den"]),
                property_term=str(row["property_term"]),
                definition=str(row["asccp_definition"]) if row.get("asccp_definition") is not None else None,
                definition_source=(
                    str(row["asccp_definition_source"]) if row.get("asccp_definition_source") is not None else None
                ),
                is_deprecated=bool(row["asccp_is_deprecated"]),
            ),
        )

    def _to_bcc_relationship(self, row: dict[str, Any], from_acc: AccInfoRow) -> BccRelationshipInfoRow:
        """Convert one BCC row into relationship DTO."""
        return BccRelationshipInfoRow(
            bcc_manifest_id=row["bcc_manifest_id"],
            bcc_id=row["bcc_id"],
            guid=str(row["guid"]),
            den=str(row["den"]),
            cardinality_min=row["cardinality_min"],
            cardinality_max=row["cardinality_max"] if row.get("cardinality_max") is not None else -1,
            entity_type="Attribute" if row["entity_type"] == 0 else "Element" if row["entity_type"] == 1 else None,
            is_nillable=bool(row["is_nillable"]),
            value_constraint=self._build_value_constraint(row),
            is_deprecated=bool(row["is_deprecated"]),
            definition=str(row["definition"]) if row.get("definition") is not None else None,
            definition_source=str(row["definition_source"]) if row.get("definition_source") is not None else None,
            from_acc=from_acc,
            to_bccp=BccpInfoRow(
                bccp_manifest_id=row["bccp_manifest_id"],
                bccp_id=row["bccp_id"],
                guid=str(row["bccp_guid"]),
                den=str(row["bccp_den"]),
                property_term=str(row["property_term"]),
                representation_term=str(row["representation_term"]),
                definition=str(row["bccp_definition"]) if row.get("bccp_definition") is not None else None,
                definition_source=(
                    str(row["bccp_definition_source"]) if row.get("bccp_definition_source") is not None else None
                ),
                is_deprecated=bool(row["bccp_is_deprecated"]),
                bdt_manifest=self._to_bdt_summary_from_bcc_row(row),
            ),
        )

    def _build_value_constraint(self, row: dict[str, Any]) -> ValueConstraintRow | None:
        """Create a value-constraint DTO when BCC default/fixed values are present."""
        if row.get("default_value") is None and row.get("fixed_value") is None:
            return None
        return ValueConstraintRow(
            default_value=str(row["default_value"]) if row.get("default_value") is not None else None,
            fixed_value=str(row["fixed_value"]) if row.get("fixed_value") is not None else None,
        )

    def _to_bdt_summary_from_bcc_row(self, row: dict[str, Any]) -> DtSummaryRow:
        """Convert BCC query columns into BDT summary DTO."""
        return DtSummaryRow(
            dt_manifest_id=row["dt_manifest_id"],
            dt_id=row["dt_id"],
            based_dt_manifest_id=row["based_dt_manifest_id"] if row.get("based_dt_manifest_id") is not None else None,
            guid=str(row["dt_guid"]),
            den=str(row["dt_den"]),
            data_type_term=str(row["data_type_term"]) if row.get("data_type_term") is not None else None,
            qualifier=str(row["qualifier"]) if row.get("qualifier") is not None else None,
            representation_term=str(row["dt_representation_term"]) if row.get("dt_representation_term") is not None else None,
            six_digit_id=str(row["six_digit_id"]) if row.get("six_digit_id") is not None else None,
            definition=str(row["dt_definition"]) if row.get("dt_definition") is not None else None,
            definition_source=str(row["dt_definition_source"]) if row.get("dt_definition_source") is not None else None,
            content_component_definition=(
                str(row["content_component_definition"]) if row.get("content_component_definition") is not None else None
            ),
            is_deprecated=bool(row["dt_is_deprecated"]),
            namespace=_namespace_summary(
                {
                    "namespace_id": row.get("dt_namespace_id"),
                    "namespace_prefix": row.get("dt_namespace_prefix"),
                    "namespace_uri": row.get("dt_namespace_uri"),
                }
            ),
            library=_library_summary({"library_id": row["dt_library_id"], "library_name": row["dt_library_name"]}),
            release=_release_summary(
                {
                    "release_id": row["dt_release_id"],
                    "release_num": row.get("dt_release_num"),
                    "release_state": row["dt_release_state"],
                }
            ),
        )

    async def get_asccp(self, asccp_manifest_id: AsccpManifestId) -> GetAsccpRow | None:
        """Get ASCCP details using score-mcp-server compatible response shape.

        Args:
            asccp_manifest_id: ASCCP manifest identifier.

        Returns:
            Result of the operation.
        """
        core = await self._fetch_one_core("ASCCP", asccp_manifest_id)
        if core is None:
            return None

        row = (
            await self._session.execute(
                select(
                    AsccpManifest.role_of_acc_manifest_id,
                    Asccp.property_term,
                    Asccp.reusable_indicator,
                    Asccp.is_nillable,
                )
                .select_from(AsccpManifest)
                .join(Asccp, Asccp.asccp_id == AsccpManifest.asccp_id)
                .where(AsccpManifest.asccp_manifest_id == asccp_manifest_id)
            )
        ).first()
        if row is None:
            return None

        role_of_acc = None
        if row[0] is not None:
            role_row = (
                await self._session.execute(
                    select(
                        AccManifest.acc_manifest_id,
                        Acc.acc_id,
                        Acc.guid,
                        AccManifest.den,
                        Acc.object_class_term,
                        Acc.type,
                        Acc.definition,
                        Acc.definition_source,
                        Namespace.namespace_id,
                        Namespace.prefix.label("namespace_prefix"),
                        Namespace.uri.label("namespace_uri"),
                        Library.library_id,
                        Library.name.label("library_name"),
                        Release.release_id,
                        Release.release_num,
                        Release.state.label("release_state"),
                    )
                    .select_from(AccManifest)
                    .join(Acc, Acc.acc_id == AccManifest.acc_id)
                    .join(Release, Release.release_id == AccManifest.release_id)
                    .join(Library, Library.library_id == Release.library_id)
                    .outerjoin(Namespace, Namespace.namespace_id == Acc.namespace_id)
                    .where(AccManifest.acc_manifest_id == row[0])
                )
            ).first()
            if role_row is not None:
                rr = dict(role_row._mapping)
                role_of_acc = BaseAccInfoRow(
                    acc_manifest_id=int(rr["acc_manifest_id"]),
                    acc_id=int(rr["acc_id"]),
                    guid=str(rr["guid"]),
                    den=str(rr["den"]),
                    object_class_term=str(rr["object_class_term"]),
                    type=str(rr["type"]) if rr.get("type") is not None else None,
                    definition=str(rr["definition"]) if rr.get("definition") is not None else None,
                    definition_source=str(rr["definition_source"]) if rr.get("definition_source") is not None else None,
                    namespace=_namespace_summary(rr),
                    library=_library_summary(rr),
                    release=_release_summary(rr),
                )

        tags = await self._get_manifest_tags(
            tag_link_model=AsccpManifestTag,
            tag_link_manifest_col=AsccpManifestTag.asccp_manifest_id,
            manifest_id=int(asccp_manifest_id),
        )

        return GetAsccpRow(
            asccp_manifest_id=asccp_manifest_id,
            asccp_id=core.component_id,
            role_of_acc=role_of_acc,
            guid=core.guid,
            den=core.den,
            property_term=str(row[1]) if row[1] is not None else None,
            definition=core.definition,
            definition_source=core.definition_source,
            reusable_indicator=bool(row[2]),
            is_nillable=bool(row[3]) if row[3] is not None else None,
            is_deprecated=core.is_deprecated,
            state=core.state,
            namespace=core.namespace,
            library=core.library,
            release=core.release,
            tags=tags,
            log=core.log,
            owner_user_id=core.owner_user_id,
            created_by=core.created_by,
            creation_timestamp=core.creation_timestamp,
            last_updated_by=core.last_updated_by,
            last_update_timestamp=core.last_update_timestamp,
        )

    async def get_bccp(self, bccp_manifest_id: BccpManifestId) -> GetBccpRow | None:
        """Get BCCP details using score-mcp-server compatible response shape.

        Args:
            bccp_manifest_id: BCCP manifest identifier.

        Returns:
            Result of the operation.
        """
        core = await self._fetch_one_core("BCCP", bccp_manifest_id)
        if core is None:
            return None

        row = (
            await self._session.execute(
                select(
                    BccpManifest.bdt_manifest_id,
                    Bccp.property_term,
                    Bccp.representation_term,
                    Bccp.is_nillable,
                    Bccp.default_value,
                    Bccp.fixed_value,
                    DtManifest.dt_manifest_id,
                    DtManifest.dt_id,
                    DtManifest.based_dt_manifest_id,
                    DtManifest.den.label("dt_den"),
                    Dt.guid.label("dt_guid"),
                    Dt.data_type_term,
                    Dt.qualifier,
                    Dt.representation_term.label("dt_representation_term"),
                    Dt.six_digit_id,
                    Dt.definition.label("dt_definition"),
                    Dt.definition_source.label("dt_definition_source"),
                    Dt.content_component_definition,
                    Dt.is_deprecated.label("dt_is_deprecated"),
                    Namespace.namespace_id.label("dt_namespace_id"),
                    Namespace.prefix.label("dt_namespace_prefix"),
                    Namespace.uri.label("dt_namespace_uri"),
                    Library.library_id.label("dt_library_id"),
                    Library.name.label("dt_library_name"),
                    Release.release_id.label("dt_release_id"),
                    Release.release_num.label("dt_release_num"),
                    Release.state.label("dt_release_state"),
                )
                .select_from(BccpManifest)
                .join(Bccp, Bccp.bccp_id == BccpManifest.bccp_id)
                .join(DtManifest, DtManifest.dt_manifest_id == BccpManifest.bdt_manifest_id)
                .join(Dt, Dt.dt_id == DtManifest.dt_id)
                .join(Release, Release.release_id == DtManifest.release_id)
                .join(Library, Library.library_id == Release.library_id)
                .outerjoin(Namespace, Namespace.namespace_id == Dt.namespace_id)
                .where(BccpManifest.bccp_manifest_id == bccp_manifest_id)
            )
        ).first()
        if row is None:
            return None

        value_constraint = None
        if row[4] is not None or row[5] is not None:
            value_constraint = ValueConstraintRow(
                default_value=str(row[4]) if row[4] is not None else None,
                fixed_value=str(row[5]) if row[5] is not None else None,
            )

        bdt = DtSummaryRow(
            dt_manifest_id=row[6],
            dt_id=row[7],
            based_dt_manifest_id=row[8] if row[8] is not None else None,
            guid=str(row[10]),
            den=str(row[9]),
            data_type_term=str(row[11]) if row[11] is not None else None,
            qualifier=str(row[12]) if row[12] is not None else None,
            representation_term=str(row[13]) if row[13] is not None else None,
            six_digit_id=str(row[14]) if row[14] is not None else None,
            definition=str(row[15]) if row[15] is not None else None,
            definition_source=str(row[16]) if row[16] is not None else None,
            content_component_definition=str(row[17]) if row[17] is not None else None,
            is_deprecated=bool(row[18]),
            namespace=_namespace_summary(
                {"namespace_id": row[19], "namespace_prefix": row[20], "namespace_uri": row[21]}
            ),
            library=_library_summary({"library_id": row[22], "library_name": row[23]}),
            release=_release_summary({"release_id": row[24], "release_num": row[25], "release_state": row[26]}),
        )

        tags = await self._get_manifest_tags(
            tag_link_model=BccpManifestTag,
            tag_link_manifest_col=BccpManifestTag.bccp_manifest_id,
            manifest_id=int(bccp_manifest_id),
        )

        return GetBccpRow(
            bccp_manifest_id=bccp_manifest_id,
            bccp_id=core.component_id,
            bdt=bdt,
            guid=core.guid,
            den=core.den,
            property_term=str(row[1]),
            representation_term=str(row[2]),
            definition=core.definition,
            definition_source=core.definition_source,
            is_nillable=bool(row[3]),
            value_constraint=value_constraint,
            is_deprecated=core.is_deprecated,
            state=core.state,
            namespace=core.namespace,
            library=core.library,
            release=core.release,
            tags=tags,
            log=core.log,
            owner_user_id=core.owner_user_id,
            created_by=core.created_by,
            creation_timestamp=core.creation_timestamp,
            last_updated_by=core.last_updated_by,
            last_update_timestamp=core.last_update_timestamp,
        )

    async def _fetch_one_core(self, component_type: str, manifest_id: int) -> CoreComponentListRow | None:
        """Resolve a single component using a direct manifest-id query.

        Args:
            component_type: Value for `component_type`.
            manifest_id: Value for `manifest_id`.

        Returns:
            Result of the operation.
        """
        if component_type == "ACC":
            base_query = self._build_component_select(
                component_type="ACC",
                manifest_model=AccManifest,
                component_model=Acc,
                manifest_id_col=AccManifest.acc_manifest_id,
                manifest_release_col=AccManifest.release_id,
                manifest_log_col=AccManifest.log_id,
                manifest_component_id_col=AccManifest.acc_id,
                component_id_col=Acc.acc_id,
                component_namespace_col=Acc.namespace_id,
                component_owner_col=Acc.owner_user_id,
                component_created_by_col=Acc.created_by,
                component_updated_by_col=Acc.last_updated_by,
                guid_col=Acc.guid,
                den_col=AccManifest.den,
                name_col=Acc.object_class_term,
                definition_col=Acc.definition,
                definition_source_col=Acc.definition_source,
                state_col=Acc.state,
                is_deprecated_col=Acc.is_deprecated,
                creation_ts_col=Acc.creation_timestamp,
                update_ts_col=Acc.last_update_timestamp,
                tag_link_model=AccManifestTag,
                tag_link_manifest_col=AccManifestTag.acc_manifest_id,
                release_ids=None,
                den=None,
                tag=None,
                creation_timestamp_before=None,
                creation_timestamp_after=None,
                last_update_timestamp_before=None,
                last_update_timestamp_after=None,
            )
        elif component_type == "ASCCP":
            base_query = self._build_component_select(
                component_type="ASCCP",
                manifest_model=AsccpManifest,
                component_model=Asccp,
                manifest_id_col=AsccpManifest.asccp_manifest_id,
                manifest_release_col=AsccpManifest.release_id,
                manifest_log_col=AsccpManifest.log_id,
                manifest_component_id_col=AsccpManifest.asccp_id,
                component_id_col=Asccp.asccp_id,
                component_namespace_col=Asccp.namespace_id,
                component_owner_col=Asccp.owner_user_id,
                component_created_by_col=Asccp.created_by,
                component_updated_by_col=Asccp.last_updated_by,
                guid_col=Asccp.guid,
                den_col=AsccpManifest.den,
                name_col=Asccp.property_term,
                definition_col=Asccp.definition,
                definition_source_col=Asccp.definition_source,
                state_col=Asccp.state,
                is_deprecated_col=Asccp.is_deprecated,
                creation_ts_col=Asccp.creation_timestamp,
                update_ts_col=Asccp.last_update_timestamp,
                tag_link_model=AsccpManifestTag,
                tag_link_manifest_col=AsccpManifestTag.asccp_manifest_id,
                release_ids=None,
                den=None,
                tag=None,
                creation_timestamp_before=None,
                creation_timestamp_after=None,
                last_update_timestamp_before=None,
                last_update_timestamp_after=None,
            )
        else:
            base_query = self._build_component_select(
                component_type="BCCP",
                manifest_model=BccpManifest,
                component_model=Bccp,
                manifest_id_col=BccpManifest.bccp_manifest_id,
                manifest_release_col=BccpManifest.release_id,
                manifest_log_col=BccpManifest.log_id,
                manifest_component_id_col=BccpManifest.bccp_id,
                component_id_col=Bccp.bccp_id,
                component_namespace_col=Bccp.namespace_id,
                component_owner_col=Bccp.owner_user_id,
                component_created_by_col=Bccp.created_by,
                component_updated_by_col=Bccp.last_updated_by,
                guid_col=Bccp.guid,
                den_col=BccpManifest.den,
                name_col=Bccp.property_term,
                definition_col=Bccp.definition,
                definition_source_col=Bccp.definition_source,
                state_col=Bccp.state,
                is_deprecated_col=Bccp.is_deprecated,
                creation_ts_col=Bccp.creation_timestamp,
                update_ts_col=Bccp.last_update_timestamp,
                tag_link_model=BccpManifestTag,
                tag_link_manifest_col=BccpManifestTag.bccp_manifest_id,
                release_ids=None,
                den=None,
                tag=None,
                creation_timestamp_before=None,
                creation_timestamp_after=None,
                last_update_timestamp_before=None,
                last_update_timestamp_after=None,
            )

        sub = base_query.subquery("one_core")
        row = (
            await self._session.execute(
                select(sub).where(sub.c.manifest_id == manifest_id)
            )
        ).first()
        if row is None:
            return None
        return self._to_core_component_raw(dict(row._mapping))

    def _build_component_select(
        self,
        component_type: str,
        manifest_model: Any,
        component_model: Any,
        manifest_id_col: Any,
        manifest_release_col: Any,
        manifest_log_col: Any,
        manifest_component_id_col: Any,
        component_id_col: Any,
        component_namespace_col: Any,
        component_owner_col: Any,
        component_created_by_col: Any,
        component_updated_by_col: Any,
        guid_col: Any,
        den_col: Any,
        name_col: Any,
        definition_col: Any,
        definition_source_col: Any,
        state_col: Any,
        is_deprecated_col: Any,
        creation_ts_col: Any,
        update_ts_col: Any,
        tag_link_model: Any,
        tag_link_manifest_col: Any,
        release_ids: list[int] | None,
        den: str | None,
        tag: str | None,
        creation_timestamp_before: datetime | None,
    creation_timestamp_after: datetime | None,
        last_update_timestamp_before: datetime | None,
    last_update_timestamp_after: datetime | None,
    ) -> Select[Any]:
        # Intentionally exclude AppUser joins here.
        # User resolution is handled in the service layer via batched `gets(...)`
        # so AppUser data can be cached and reused across list/detail calls.
        """Build a typed component select for list/detail core fields without user joins.

        Args:
            component_type: Value for `component_type`.
            manifest_model: Value for `manifest_model`.
            component_model: Value for `component_model`.
            manifest_id_col: Value for `manifest_id_col`.
            manifest_release_col: Value for `manifest_release_col`.
            manifest_log_col: Value for `manifest_log_col`.
            manifest_component_id_col: Value for `manifest_component_id_col`.
            component_id_col: Value for `component_id_col`.
            component_namespace_col: Value for `component_namespace_col`.
            component_owner_col: Value for `component_owner_col`.
            component_created_by_col: Value for `component_created_by_col`.
            component_updated_by_col: Value for `component_updated_by_col`.
            guid_col: Value for `guid_col`.
            den_col: Value for `den_col`.
            name_col: Value for `name_col`.
            definition_col: Value for `definition_col`.
            definition_source_col: Value for `definition_source_col`.
            state_col: Value for `state_col`.
            is_deprecated_col: Value for `is_deprecated_col`.
            creation_ts_col: Value for `creation_ts_col`.
            update_ts_col: Value for `update_ts_col`.
            tag_link_model: Value for `tag_link_model`.
            tag_link_manifest_col: Value for `tag_link_manifest_col`.
            release_ids: Release identifiers used to scope the query.
            den: Optional Dictionary Entry Name (DEN) filter.
            tag: Optional tag filter.
            creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
            last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.

        Returns:
            Result of the operation.
        """
        where_clauses = []
        if release_ids is not None:
            where_clauses.append(manifest_release_col.in_(release_ids))
        if den:
            for word in [w.strip() for w in den.split() if w.strip()]:
                where_clauses.append(func.lower(den_col).like(func.lower(f"%{word}%")))
        if tag:
            where_clauses.append(func.lower(Tag.name).like(func.lower(f"%{tag}%")))
        if creation_timestamp_after is not None:
            where_clauses.append(creation_ts_col >= creation_timestamp_after)
        if creation_timestamp_before is not None:
            where_clauses.append(creation_ts_col <= creation_timestamp_before)
        if last_update_timestamp_after is not None:
            where_clauses.append(update_ts_col >= last_update_timestamp_after)
        if last_update_timestamp_before is not None:
            where_clauses.append(update_ts_col <= last_update_timestamp_before)

        return (
            select(
                literal(component_type).label("component_type"),
                manifest_id_col.label("manifest_id"),
                component_id_col.label("component_id"),
                guid_col.label("guid"),
                den_col.label("den"),
                name_col.label("name"),
                definition_col.label("definition"),
                definition_source_col.label("definition_source"),
                is_deprecated_col.label("is_deprecated"),
                state_col.label("state"),
                creation_ts_col.label("creation_timestamp"),
                update_ts_col.label("last_update_timestamp"),
                Namespace.namespace_id.label("namespace_id"),
                Namespace.prefix.label("namespace_prefix"),
                Namespace.uri.label("namespace_uri"),
                Library.library_id.label("library_id"),
                Library.name.label("library_name"),
                Release.release_id.label("release_id"),
                Release.release_num.label("release_num"),
                Release.state.label("release_state"),
                Log.log_id.label("log_id"),
                Log.revision_num.label("revision_num"),
                Log.revision_tracking_num.label("revision_tracking_num"),
                component_owner_col.label("owner_user_id"),
                component_created_by_col.label("created_by"),
                component_updated_by_col.label("last_updated_by"),
                func.min(Tag.name).label("tag"),
            )
            .select_from(manifest_model)
            .join(component_model, component_id_col == manifest_component_id_col)
            .join(Release, Release.release_id == manifest_release_col)
            .join(Library, Library.library_id == Release.library_id)
            .outerjoin(Namespace, Namespace.namespace_id == component_namespace_col)
            .outerjoin(Log, Log.log_id == manifest_log_col)
            .outerjoin(tag_link_model, tag_link_manifest_col == manifest_id_col)
            .outerjoin(Tag, Tag.tag_id == tag_link_model.tag_id)
            .where(and_(*where_clauses))
            .group_by(
                manifest_id_col,
                component_id_col,
                guid_col,
                den_col,
                name_col,
                definition_col,
                definition_source_col,
                is_deprecated_col,
                state_col,
                creation_ts_col,
                update_ts_col,
                Namespace.namespace_id,
                Namespace.prefix,
                Namespace.uri,
                Library.library_id,
                Library.name,
                Release.release_id,
                Release.release_num,
                Release.state,
                Log.log_id,
                Log.revision_num,
                Log.revision_tracking_num,
                component_owner_col,
                component_created_by_col,
                component_updated_by_col,
            )
        )

    def _to_core_component_raw(self, row: dict[str, Any]) -> CoreComponentListRow:
        """Internal helper for to core component raw.

        Args:
            row: Repository row model to convert into a DTO.

        Returns:
            Result of the operation.
        """
        return CoreComponentListRow(
            component_type=str(row["component_type"]),
            manifest_id=row["manifest_id"],
            component_id=row["component_id"],
            guid=str(row["guid"]),
            den=str(row["den"]),
            name=str(row["name"]) if row.get("name") is not None else None,
            definition=str(row["definition"]) if row.get("definition") is not None else None,
            definition_source=str(row["definition_source"]) if row.get("definition_source") is not None else None,
            is_deprecated=bool(row["is_deprecated"]),
            state=str(row["state"]) if row.get("state") is not None else None,
            namespace=_namespace_summary(row),
            library=_library_summary(row),
            release=_release_summary(row),
            log=_log_summary(row),
            owner_user_id=row["owner_user_id"],
            created_by=row["created_by"],
            creation_timestamp=_as_dt(row["creation_timestamp"]),
            last_updated_by=row["last_updated_by"],
            last_update_timestamp=_as_dt(row["last_update_timestamp"]),
            tag=str(row["tag"]) if row.get("tag") is not None else None,
        )

    async def _get_manifest_tags(
        self,
        *,
        tag_link_model: type[AccManifestTag] | type[AsccpManifestTag] | type[BccpManifestTag],
        tag_link_manifest_col: Any,
        manifest_id: int,
    ) -> list[TagSummaryRow]:
        """Load compact tags attached to a manifest row."""
        rows = (
            await self._session.execute(
                select(
                    Tag.tag_id,
                    Tag.name,
                )
                .select_from(tag_link_model)
                .join(Tag, Tag.tag_id == tag_link_model.tag_id)
                .where(tag_link_manifest_col == manifest_id)
                .order_by(Tag.name.asc(), Tag.tag_id.asc())
            )
        ).all()
        return [TagSummaryRow(tag_id=int(row.tag_id), name=str(row.name)) for row in rows]


def _as_dt(value: Any) -> datetime:
    """Internal helper for as dt.

    Args:
        value: Context value string.

    Returns:
        Result of the operation.
    """
    if isinstance(value, datetime):
        return value
    return datetime.fromisoformat(str(value).replace("Z", "+00:00"))


def _library_summary(row: dict[str, Any]) -> LibrarySummaryRow:
    """Internal helper for library summary.

    Args:
        row: Repository row model to convert into a DTO.

    Returns:
        Result of the operation.
    """
    return LibrarySummaryRow(library_id=row["library_id"], name=str(row["library_name"]))


def _release_summary(row: dict[str, Any]) -> ReleaseSummaryRow:
    """Internal helper for release summary.

    Args:
        row: Repository row model to convert into a DTO.

    Returns:
        Result of the operation.
    """
    return ReleaseSummaryRow(
        release_id=row["release_id"],
        release_num=str(row.get("release_num")) if row.get("release_num") is not None else None,
        state=str(row["release_state"]),
    )


def _namespace_summary(row: dict[str, Any]) -> NamespaceSummaryRow | None:
    """Internal helper for namespace summary.

    Args:
        row: Repository row model to convert into a DTO.

    Returns:
        Result of the operation.
    """
    if row.get("namespace_id") is None:
        return None
    return NamespaceSummaryRow(
        namespace_id=row["namespace_id"],
        prefix=str(row.get("namespace_prefix")) if row.get("namespace_prefix") is not None else None,
        uri=str(row["namespace_uri"]),
    )


def _log_summary(row: dict[str, Any]) -> LogSummaryRow | None:
    """Internal helper for log summary.

    Args:
        row: Repository row model to convert into a DTO.

    Returns:
        Result of the operation.
    """
    if row.get("log_id") is None:
        return None
    return LogSummaryRow(
        log_id=row["log_id"],
        revision_num=int(row.get("revision_num") or 0),
        revision_tracking_num=int(row.get("revision_tracking_num") or 0),
    )
