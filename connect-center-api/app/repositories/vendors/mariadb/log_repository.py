"""MariaDB repository implementation for revision-log persistence."""


from __future__ import annotations

import json
import secrets
from datetime import datetime
from typing import Any

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.contracts.log import LogRepositoryContract
from app.repositories.models.core_component import AsccRelationshipInfoRow, BccRelationshipInfoRow
from app.repositories.vendors.mariadb.models.app_user import AppUser
from app.repositories.vendors.mariadb.models.core_component import (
    Acc,
    AccManifest,
    Ascc,
    Asccp,
    AsccpManifest,
    AsccManifest,
    Bcc,
    Bccp,
    BccpManifest,
    BccManifest,
    SeqKey,
)
from app.repositories.vendors.mariadb.models.data_type import Dt, DtManifest
from app.repositories.vendors.mariadb.models.log import Log
from app.repositories.vendors.mariadb.models.namespace import Namespace
from app.types.identifiers import AccManifestId, AppUserId, AsccpManifestId, BccpManifestId
from app.utils.core_component_constants import OAGIS_COMPONENT_TYPE_NAMES


class MariaDbLogRepository(LogRepositoryContract):
    """MariaDB-backed repository for revision-log persistence."""

    def __init__(self, session: AsyncSession):
        """Initialize MariaDbLogRepository.

        Args:
            session: Database session bound to the current request.
        """
        self._session = session

    async def append_acc_log(
        self,
        *,
        acc_manifest_id: AccManifestId,
        relationships: list[AsccRelationshipInfoRow | BccRelationshipInfoRow],
        requester_user_id: AppUserId,
        action: str,
        timestamp: datetime,
    ) -> int:
        """Append a new ACC log row and update the manifest head."""
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

        current_log = None
        if acc_manifest.log_id is not None:
            current_log = await self._session.get(Log, int(acc_manifest.log_id))

        if action == "Revised":
            revision_num = int(current_log.revision_num) + 1 if current_log is not None else 1
            revision_tracking_num = 1
        else:
            revision_num = int(current_log.revision_num) if current_log is not None else 1
            revision_tracking_num = int(current_log.revision_tracking_num) + 1 if current_log is not None else 1

        log = Log(
            hash=secrets.token_hex(20),
            revision_num=revision_num,
            revision_tracking_num=revision_tracking_num,
            log_action=action,
            reference=str(acc.guid),
            snapshot=await self._serialize_acc_log_snapshot(
                acc_manifest=acc_manifest,
                acc=acc,
                relationships=relationships,
            ),
            prev_log_id=int(current_log.log_id) if current_log is not None else None,
            next_log_id=None,
            created_by=int(requester_user_id),
            creation_timestamp=timestamp,
        )
        self._session.add(log)
        await self._session.flush()
        await self._session.refresh(log)

        if current_log is not None:
            current_log.next_log_id = int(log.log_id)
        acc_manifest.log_id = int(log.log_id)
        return int(log.log_id)

    async def revert_acc_log_to_stable_state(
        self,
        *,
        reference: str,
        current_log_id: int | None,
    ) -> tuple[int, list[str]]:
        """Delete the latest revised ACC log chain and return the stable log head."""
        if current_log_id is None:
            raise LookupError("The ACC does not have a revision log to restore.")

        logs = (
            await self._session.execute(
                select(Log)
                .where(Log.reference == str(reference))
                .order_by(Log.log_id.desc())
            )
        ).scalars().all()
        if not logs:
            raise LookupError("The ACC does not have a revision log to restore.")

        stable_log: Log | None = None
        delete_logs: list[Log] = []
        latest_revision_num = int(logs[0].revision_num)
        for log in logs:
            if int(log.revision_num) < latest_revision_num:
                stable_log = log
                break
            delete_logs.append(log)

        if stable_log is None:
            raise ValueError("No stable ACC revision log is available to restore.")

        delete_log_ids = [int(log.log_id) for log in delete_logs]
        if delete_log_ids:
            affected_manifest_ids = (
                await self._session.execute(
                    select(AccManifest.acc_manifest_id)
                    .join(Acc, AccManifest.acc_id == Acc.acc_id)
                    .where(
                        Acc.guid == str(reference),
                        AccManifest.log_id.in_(delete_log_ids),
                    )
                )
            ).scalars().all()
            for acc_manifest_id in affected_manifest_ids:
                acc_manifest = await self._session.get(AccManifest, int(acc_manifest_id))
                if acc_manifest is not None:
                    acc_manifest.log_id = int(stable_log.log_id)

        stable_log.next_log_id = None
        for log in delete_logs:
            log.prev_log_id = None
            log.next_log_id = None
        await self._session.flush()
        for log in delete_logs:
            await self._session.delete(log)
        await self._session.flush()
        return int(stable_log.log_id), self._extract_acc_association_guid_order(stable_log.snapshot)

    async def append_component_log(
        self,
        *,
        reference: str,
        current_log_id: int | None,
        requester_user_id: AppUserId,
        action: str,
        timestamp: datetime,
        snapshot: str | None = None,
    ) -> int:
        """Append a non-ACC component log row and return the new log ID."""
        current_log = await self._session.get(Log, int(current_log_id)) if current_log_id is not None else None

        if action == "Revised":
            revision_num = int(current_log.revision_num) + 1 if current_log is not None else 1
            revision_tracking_num = 1
        else:
            revision_num = int(current_log.revision_num) if current_log is not None else 1
            revision_tracking_num = int(current_log.revision_tracking_num) + 1 if current_log is not None else 1

        log = Log(
            hash=secrets.token_hex(20),
            revision_num=revision_num,
            revision_tracking_num=revision_tracking_num,
            log_action=action,
            reference=str(reference),
            snapshot=snapshot,
            prev_log_id=int(current_log.log_id) if current_log is not None else None,
            next_log_id=None,
            created_by=int(requester_user_id),
            creation_timestamp=timestamp,
        )
        self._session.add(log)
        await self._session.flush()
        await self._session.refresh(log)

        if current_log is not None:
            current_log.next_log_id = int(log.log_id)
        return int(log.log_id)

    async def append_asccp_log(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
        requester_user_id: AppUserId,
        action: str,
        timestamp: datetime,
    ) -> int:
        """Append a new ASCCP log row and update the manifest head."""
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

        current_log = await self._session.get(Log, int(asccp_manifest.log_id)) if asccp_manifest.log_id is not None else None
        log = await self._create_component_log(
            reference=str(asccp.guid),
            current_log=current_log,
            requester_user_id=requester_user_id,
            action=action,
            timestamp=timestamp,
            snapshot=await self._serialize_asccp_log_snapshot(
                asccp_manifest=asccp_manifest,
                asccp=asccp,
            ),
        )
        asccp_manifest.log_id = int(log.log_id)
        return int(log.log_id)

    async def append_bccp_log(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
        requester_user_id: AppUserId,
        action: str,
        timestamp: datetime,
    ) -> int:
        """Append a new BCCP log row and update the manifest head."""
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

        current_log = await self._session.get(Log, int(bccp_manifest.log_id)) if bccp_manifest.log_id is not None else None
        log = await self._create_component_log(
            reference=str(bccp.guid),
            current_log=current_log,
            requester_user_id=requester_user_id,
            action=action,
            timestamp=timestamp,
            snapshot=await self._serialize_bccp_log_snapshot(
                bccp_manifest=bccp_manifest,
                bccp=bccp,
            ),
        )
        bccp_manifest.log_id = int(log.log_id)
        return int(log.log_id)

    async def revert_component_log_to_stable_state(
        self,
        *,
        reference: str,
        current_log_id: int | None,
    ) -> int:
        """Delete the latest revised non-ACC log chain and return the stable log ID."""
        if current_log_id is None:
            raise LookupError("The component does not have a revision log to restore.")

        logs = (
            await self._session.execute(
                select(Log)
                .where(Log.reference == str(reference))
                .order_by(Log.log_id.desc())
            )
        ).scalars().all()
        if not logs:
            raise LookupError("The component does not have a revision log to restore.")

        stable_log: Log | None = None
        delete_logs: list[Log] = []
        latest_revision_num = int(logs[0].revision_num)
        for log in logs:
            if int(log.revision_num) < latest_revision_num:
                stable_log = log
                break
            delete_logs.append(log)

        if stable_log is None:
            raise ValueError("No stable component revision log is available to restore.")

        stable_log.next_log_id = None
        for log in delete_logs:
            log.prev_log_id = None
            log.next_log_id = None
        await self._session.flush()
        for log in delete_logs:
            await self._session.delete(log)
        await self._session.flush()
        return int(stable_log.log_id)

    async def _create_component_log(
        self,
        *,
        reference: str,
        current_log: Log | None,
        requester_user_id: AppUserId,
        action: str,
        timestamp: datetime,
        snapshot: str | None,
    ) -> Log:
        """Create and append a component log row."""
        if action == "Revised":
            revision_num = int(current_log.revision_num) + 1 if current_log is not None else 1
            revision_tracking_num = 1
        else:
            revision_num = int(current_log.revision_num) if current_log is not None else 1
            revision_tracking_num = int(current_log.revision_tracking_num) + 1 if current_log is not None else 1

        log = Log(
            hash=secrets.token_hex(20),
            revision_num=revision_num,
            revision_tracking_num=revision_tracking_num,
            log_action=action,
            reference=str(reference),
            snapshot=snapshot,
            prev_log_id=int(current_log.log_id) if current_log is not None else None,
            next_log_id=None,
            created_by=int(requester_user_id),
            creation_timestamp=timestamp,
        )
        self._session.add(log)
        await self._session.flush()
        await self._session.refresh(log)

        if current_log is not None:
            current_log.next_log_id = int(log.log_id)
        return log

    async def _serialize_acc_log_snapshot(
        self,
        *,
        acc_manifest: AccManifest,
        acc: Acc,
        relationships: list[AsccRelationshipInfoRow | BccRelationshipInfoRow],
    ) -> str:
        """Serialize the current ACC state using the SCORE log snapshot shape."""
        payload: dict[str, Any] = {
            "component": "acc",
            "guid": str(acc.guid),
            "objectClassTerm": acc.object_class_term,
            "objectClassQualifier": acc.object_class_qualifier,
            "componentType": OAGIS_COMPONENT_TYPE_NAMES.get(int(acc.oagis_component_type))
            if acc.oagis_component_type is not None
            else None,
            "state": acc.state,
            "deprecated": bool(acc.is_deprecated),
            "abstract": bool(acc.is_abstract),
            "basedAcc": await self._resolve_acc_log_based_acc(acc_manifest.based_acc_manifest_id),
            "ownerUser": await self._resolve_acc_log_user(acc.owner_user_id),
            "namespace": await self._resolve_acc_log_namespace(acc.namespace_id),
            "associations": [self._serialize_acc_log_association(relationship) for relationship in relationships],
            "_metadata": await self._build_acc_log_metadata(acc_manifest=acc_manifest, acc=acc, relationships=relationships),
        }
        if acc.definition is not None:
            payload["definition"] = acc.definition
            payload["definitionSource"] = acc.definition_source
        return json.dumps(payload, separators=(",", ":"), sort_keys=True)

    async def _serialize_asccp_log_snapshot(
        self,
        *,
        asccp_manifest: AsccpManifest,
        asccp: Asccp,
    ) -> str:
        """Serialize the current ASCCP state using the SCORE log snapshot shape."""
        payload: dict[str, Any] = {
            "component": "asccp",
            "guid": str(asccp.guid),
            "propertyTerm": asccp.property_term,
            "state": asccp.state,
            "reusable": bool(asccp.reusable_indicator),
            "deprecated": bool(asccp.is_deprecated),
            "nillable": bool(asccp.is_nillable),
            "roleOfAcc": await self._resolve_acc_log_based_acc(asccp_manifest.role_of_acc_manifest_id),
            "ownerUser": await self._resolve_acc_log_user(asccp.owner_user_id),
            "namespace": await self._resolve_acc_log_namespace(asccp.namespace_id),
            "_metadata": {
                "asccpManifest": self._to_log_metadata(asccp_manifest),
                "asccp": self._to_log_metadata(asccp),
            },
        }
        if asccp.definition is not None:
            payload["definition"] = asccp.definition
            payload["definitionSource"] = asccp.definition_source
        return json.dumps(payload, separators=(",", ":"), sort_keys=True)

    async def _serialize_bccp_log_snapshot(
        self,
        *,
        bccp_manifest: BccpManifest,
        bccp: Bccp,
    ) -> str:
        """Serialize the current BCCP state using the SCORE log snapshot shape."""
        payload: dict[str, Any] = {
            "component": "bccp",
            "guid": str(bccp.guid),
            "propertyTerm": bccp.property_term,
            "representationTerm": bccp.representation_term,
            "state": bccp.state,
            "deprecated": bool(bccp.is_deprecated),
            "nillable": bool(bccp.is_nillable),
            "bdt": await self._resolve_bccp_log_dt(bccp_manifest.bdt_manifest_id),
            "ownerUser": await self._resolve_acc_log_user(bccp.owner_user_id),
            "namespace": await self._resolve_acc_log_namespace(bccp.namespace_id),
            "_metadata": {
                "bccpManifest": self._to_log_metadata(bccp_manifest),
                "bccp": self._to_log_metadata(bccp),
            },
        }
        if bccp.definition is not None:
            payload["definition"] = bccp.definition
            payload["definitionSource"] = bccp.definition_source
        if bccp.default_value is not None or bccp.fixed_value is not None:
            payload["defaultValue"] = bccp.default_value
            payload["fixedValue"] = bccp.fixed_value
        return json.dumps(payload, separators=(",", ":"), sort_keys=True)

    async def _resolve_acc_log_based_acc(
        self,
        based_acc_manifest_id: int | None,
    ) -> dict[str, Any]:
        """Return SCORE-style base ACC summary for ACC log snapshots."""
        if based_acc_manifest_id is None:
            return {}

        row = (
            await self._session.execute(
                select(AccManifest, Acc)
                .join(Acc, Acc.acc_id == AccManifest.acc_id)
                .where(AccManifest.acc_manifest_id == int(based_acc_manifest_id))
            )
        ).first()
        if row is None:
            return {}
        based_manifest, based_acc = row
        return {
            "guid": str(based_acc.guid),
            "objectClassTerm": based_acc.object_class_term,
            "den": based_manifest.den,
        }

    async def _resolve_acc_log_user(
        self,
        owner_user_id: int | None,
    ) -> dict[str, Any]:
        """Return SCORE-style user summary for ACC log snapshots."""
        if owner_user_id is None:
            return {}
        user = await self._session.get(AppUser, int(owner_user_id))
        if user is None:
            return {}
        return {
            "username": user.login_id,
            "roles": ["developer"] if bool(user.is_developer) else ["end-user"],
        }

    async def _resolve_acc_log_namespace(
        self,
        namespace_id: int | None,
    ) -> dict[str, Any]:
        """Return SCORE-style namespace summary for ACC log snapshots."""
        if namespace_id is None:
            return {}
        namespace = await self._session.get(Namespace, int(namespace_id))
        if namespace is None:
            return {}
        return {
            "uri": namespace.uri,
            "standard": bool(namespace.is_std_nmsp),
        }

    def _serialize_acc_log_association(
        self,
        relationship: AsccRelationshipInfoRow | BccRelationshipInfoRow,
    ) -> dict[str, Any]:
        """Serialize an ACC association using the SCORE log snapshot shape."""
        if relationship.component_type == "ASCC":
            to_asccp = self._as_mapping(relationship.to_asccp)
            payload: dict[str, Any] = {
                "component": "ascc",
                "guid": relationship.guid,
                "cardinalityMin": relationship.cardinality_min,
                "cardinalityMax": relationship.cardinality_max,
                "deprecated": relationship.is_deprecated,
                "toAsccp": {
                    "guid": self._mapping_or_attr(to_asccp, "guid"),
                    "propertyTerm": self._mapping_or_attr(to_asccp, "property_term"),
                    "den": self._mapping_or_attr(to_asccp, "den"),
                },
            }
            if relationship.definition is not None:
                payload["definition"] = relationship.definition
                payload["definitionSource"] = relationship.definition_source
            return payload

        to_bccp = self._as_mapping(relationship.to_bccp)
        payload = {
            "component": "bcc",
            "guid": relationship.guid,
            "cardinalityMin": relationship.cardinality_min,
            "cardinalityMax": relationship.cardinality_max,
            "entityType": relationship.entity_type,
            "deprecated": relationship.is_deprecated,
            "nillable": relationship.is_nillable,
            "toBccp": {
                "guid": self._mapping_or_attr(to_bccp, "guid"),
                "propertyTerm": self._mapping_or_attr(to_bccp, "property_term"),
                "den": self._mapping_or_attr(to_bccp, "den"),
            },
        }
        if relationship.definition is not None:
            payload["definition"] = relationship.definition
            payload["definitionSource"] = relationship.definition_source
        if relationship.value_constraint is not None:
            payload["defaultValue"] = relationship.value_constraint.default_value
            payload["fixedValue"] = relationship.value_constraint.fixed_value
        return payload

    async def _resolve_bccp_log_dt(
        self,
        dt_manifest_id: int | None,
    ) -> dict[str, Any]:
        """Return SCORE-style DT summary for BCCP log snapshots."""
        if dt_manifest_id is None:
            return {}
        row = (
            await self._session.execute(
                select(DtManifest, Dt)
                .join(Dt, Dt.dt_id == DtManifest.dt_id)
                .where(DtManifest.dt_manifest_id == int(dt_manifest_id))
            )
        ).first()
        if row is None:
            return {}
        dt_manifest, dt = row
        return {
            "guid": str(dt.guid),
            "dataTypeTerm": dt.data_type_term,
            "den": dt_manifest.den,
        }

    @staticmethod
    def _extract_acc_association_guid_order(snapshot: str | None) -> list[str]:
        """Extract association GUID order from a serialized ACC log snapshot."""
        if not snapshot:
            return []
        try:
            payload = json.loads(snapshot)
        except json.JSONDecodeError:
            return []
        associations = payload.get("associations")
        if not isinstance(associations, list):
            return []
        ordered_guids: list[str] = []
        for association in associations:
            if not isinstance(association, dict):
                continue
            guid = association.get("guid")
            if isinstance(guid, str) and guid:
                ordered_guids.append(guid)
        return ordered_guids

    async def _build_acc_log_metadata(
        self,
        *,
        acc_manifest: AccManifest,
        acc: Acc,
        relationships: list[AsccRelationshipInfoRow | BccRelationshipInfoRow],
    ) -> dict[str, Any]:
        """Build SCORE-style ACC log metadata."""
        metadata: dict[str, Any] = {
            "accManifest": self._to_log_metadata(acc_manifest),
            "acc": self._to_log_metadata(acc),
            "associations": [],
        }
        for relationship in relationships:
            if relationship.component_type == "ASCC":
                ascc_manifest = await self._session.get(AsccManifest, int(relationship.ascc_manifest_id))
                ascc = await self._session.get(Ascc, int(relationship.ascc_id))
                seq_key = (
                    await self._session.get(SeqKey, int(ascc_manifest.seq_key_id))
                    if ascc_manifest is not None and ascc_manifest.seq_key_id is not None
                    else None
                )
                metadata["associations"].append(
                    {
                        "asccManifest": self._to_log_metadata(ascc_manifest),
                        "ascc": self._to_log_metadata(ascc),
                        "seqKey": self._to_log_metadata(seq_key),
                    }
                )
            else:
                bcc_manifest = await self._session.get(BccManifest, int(relationship.bcc_manifest_id))
                bcc = await self._session.get(Bcc, int(relationship.bcc_id))
                seq_key = (
                    await self._session.get(SeqKey, int(bcc_manifest.seq_key_id))
                    if bcc_manifest is not None and bcc_manifest.seq_key_id is not None
                    else None
                )
                metadata["associations"].append(
                    {
                        "bccManifest": self._to_log_metadata(bcc_manifest),
                        "bcc": self._to_log_metadata(bcc),
                        "seqKey": self._to_log_metadata(seq_key),
                    }
                )
        return metadata

    @staticmethod
    def _to_log_metadata(record: Any) -> dict[str, Any]:
        """Convert an ORM record to SCORE-style log metadata."""
        if record is None:
            return {}
        properties: dict[str, Any] = {}
        for column in record.__table__.columns:
            name = column.name
            if name == "seq_key":
                continue
            value = getattr(record, name)
            if isinstance(value, datetime):
                properties[name] = value.isoformat()
            else:
                properties[name] = value
        return properties

    @staticmethod
    def _as_mapping(value: Any) -> Any:
        """Return the nested projection in its original mapping/object form."""
        return value

    @staticmethod
    def _mapping_or_attr(value: Any, key: str) -> Any:
        """Read a property from either a mapping-backed or attribute-backed projection."""
        if isinstance(value, dict):
            return value.get(key)
        return getattr(value, key, None)
