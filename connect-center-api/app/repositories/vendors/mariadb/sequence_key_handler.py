"""Helpers for maintaining ACC relationship `seq_key` linked lists."""


from __future__ import annotations

from typing import Any

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.vendors.mariadb.models.core_component import Bcc, BccManifest, SeqKey
from app.types.identifiers import AccManifestId, BccManifestId


class SequenceKeyHandler:
    """Encapsulate `seq_key` chain operations for ACC relationships."""

    def __init__(self, session: AsyncSession):
        """Initialize the handler with the active MariaDB session."""
        self._session = session

    async def move_seq_key(
        self,
        *,
        from_acc_manifest_id: AccManifestId,
        new_seq_key_id: int,
        index: int,
    ) -> None:
        """Insert a `seq_key` node into the ACC relationship chain at the requested index."""
        seq_keys = await self._load_seq_keys(from_acc_manifest_id=from_acc_manifest_id)
        if not seq_keys:
            return
        ordered_all = self.build_inserted_order(seq_keys=seq_keys, new_seq_key_id=new_seq_key_id, index=index)
        self._relink_ordered_seq_keys(ordered_all)

    async def remove_seq_key(
        self,
        *,
        from_acc_manifest_id: AccManifestId,
        current_seq_key_id: int,
    ) -> None:
        """Remove an existing `seq_key` node from the ACC relationship chain."""
        seq_keys = await self._load_seq_keys(from_acc_manifest_id=from_acc_manifest_id)
        if not seq_keys:
            return
        ordered, current = self.build_removed_order(seq_keys=seq_keys, current_seq_key_id=current_seq_key_id)
        if current is None:
            return

        self._relink_ordered_seq_keys(ordered)
        current.prev_seq_key_id = None
        current.next_seq_key_id = None
        await self._session.delete(current)
        await self._session.flush()

    async def place_seq_key_after(
        self,
        *,
        from_acc_manifest_id: AccManifestId,
        current_seq_key_id: int,
        after_seq_key_id: int | None,
    ) -> None:
        """Move an existing `seq_key` node after another node, or to the first position."""
        seq_keys = await self._load_seq_keys(from_acc_manifest_id=from_acc_manifest_id)
        if not seq_keys:
            return
        ordered_all = self.build_reordered_after(
            seq_keys=seq_keys,
            current_seq_key_id=current_seq_key_id,
            after_seq_key_id=after_seq_key_id,
        )
        self._relink_ordered_seq_keys(ordered_all)

    async def move_seq_key_to_last(
        self,
        *,
        from_acc_manifest_id: AccManifestId,
        current_seq_key_id: int,
    ) -> None:
        """Move an existing sequence key to the end of the ACC relationship chain."""
        seq_keys = await self._load_seq_keys(from_acc_manifest_id=from_acc_manifest_id)
        if not seq_keys:
            return
        ordered_all = self.build_order_moved_to_last(seq_keys=seq_keys, current_seq_key_id=current_seq_key_id)
        self._relink_ordered_seq_keys(ordered_all)

    async def move_seq_key_to_last_of_attr(
        self,
        *,
        from_acc_manifest_id: AccManifestId,
        current_seq_key_id: int,
        current_bcc_manifest_id: BccManifestId,
    ) -> None:
        """Move a BCC sequence key after the leading attribute block."""
        seq_keys = await self._load_seq_keys(from_acc_manifest_id=from_acc_manifest_id)
        attribute_manifest_ids = await self._load_attribute_bcc_manifest_ids(seq_keys=seq_keys)
        ordered_all = self.build_order_moved_to_last_of_attr(
            seq_keys=seq_keys,
            current_seq_key_id=current_seq_key_id,
            current_bcc_manifest_id=current_bcc_manifest_id,
            attribute_manifest_ids=attribute_manifest_ids,
        )
        self._relink_ordered_seq_keys(ordered_all)

    async def load_ordered_seq_rows(self, from_acc_manifest_id: AccManifestId) -> list[dict[str, Any]]:
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
                    ).where(SeqKey.from_acc_manifest_id == int(from_acc_manifest_id))
                )
            ).all()
        ]
        if not seq_rows:
            return []

        seq_by_id = {int(row["seq_key_id"]): row for row in seq_rows}
        head_candidates = [
            row
            for row in seq_rows
            if row.get("prev_seq_key_id") is None or int(row["prev_seq_key_id"]) not in seq_by_id
        ]
        current = min(head_candidates or seq_rows, key=lambda row: int(row["seq_key_id"]))

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

        remaining = sorted(
            (row for row in seq_rows if int(row["seq_key_id"]) not in visited_seq_ids),
            key=lambda row: int(row["seq_key_id"]),
        )
        ordered_seq_rows.extend(remaining)
        return ordered_seq_rows

    async def _load_seq_keys(self, *, from_acc_manifest_id: AccManifestId) -> list[SeqKey]:
        """Load all `seq_key` rows for the owning ACC."""
        return (
            await self._session.execute(
                select(SeqKey).where(SeqKey.from_acc_manifest_id == int(from_acc_manifest_id))
            )
        ).scalars().all()

    async def _load_attribute_bcc_manifest_ids(self, *, seq_keys: list[SeqKey]) -> set[int]:
        """Load the set of BCC manifests that are currently typed as attributes."""
        bcc_manifest_ids = [int(seq_key.bcc_manifest_id) for seq_key in seq_keys if seq_key.bcc_manifest_id is not None]
        if not bcc_manifest_ids:
            return set()

        rows = (
            await self._session.execute(
                select(BccManifest.bcc_manifest_id, Bcc.entity_type)
                .join(Bcc, Bcc.bcc_id == BccManifest.bcc_id)
                .where(BccManifest.bcc_manifest_id.in_(bcc_manifest_ids))
            )
        ).all()
        return {
            int(bcc_manifest_id)
            for bcc_manifest_id, raw_entity_type in rows
            if raw_entity_type is not None and int(raw_entity_type) == 0
        }

    @classmethod
    def build_inserted_order(cls, *, seq_keys: list[SeqKey], new_seq_key_id: int, index: int) -> list[SeqKey]:
        """Return the linked-list order after inserting a new node at `index`."""
        seq_key_by_id = {int(seq_key.seq_key_id): seq_key for seq_key in seq_keys}
        existing_seq_keys = [seq_key for seq_key in seq_keys if int(seq_key.seq_key_id) != int(new_seq_key_id)]
        ordered_existing = cls.order_seq_keys(existing_seq_keys)
        if index < -1:
            raise ValueError("`index` must be -1 or a zero-based index.")
        if index > len(ordered_existing):
            raise ValueError(
                f"`index` is out of range for the ACC sequence. Allowed values are 0 to {len(ordered_existing)}, or -1 for the end."
            )
        insert_at = len(ordered_existing) if index < 0 else int(index)
        return ordered_existing[:insert_at] + [seq_key_by_id[int(new_seq_key_id)]] + ordered_existing[insert_at:]

    @classmethod
    def build_removed_order(
        cls,
        *,
        seq_keys: list[SeqKey],
        current_seq_key_id: int,
    ) -> tuple[list[SeqKey], SeqKey | None]:
        """Return the linked-list order without the current node, plus the removed node."""
        seq_key_by_id = {int(seq_key.seq_key_id): seq_key for seq_key in seq_keys}
        current = seq_key_by_id.get(int(current_seq_key_id))
        if current is None:
            return cls.order_seq_keys(seq_keys), None
        ordered = [
            seq_key for seq_key in cls.order_seq_keys(seq_keys)
            if int(seq_key.seq_key_id) != int(current_seq_key_id)
        ]
        return ordered, current

    @classmethod
    def build_reordered_after(
        cls,
        *,
        seq_keys: list[SeqKey],
        current_seq_key_id: int,
        after_seq_key_id: int | None,
    ) -> list[SeqKey]:
        """Return the linked-list order after moving a node after another node."""
        seq_key_by_id = {int(seq_key.seq_key_id): seq_key for seq_key in seq_keys}
        current = seq_key_by_id.get(int(current_seq_key_id))
        if current is None:
            raise LookupError("The association to move is missing its sequence key.")

        ordered = [
            seq_key for seq_key in cls.order_seq_keys(seq_keys)
            if int(seq_key.seq_key_id) != int(current_seq_key_id)
        ]
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
        return ordered[:insert_at] + [current] + ordered[insert_at:]

    @classmethod
    def build_order_moved_to_last(cls, *, seq_keys: list[SeqKey], current_seq_key_id: int) -> list[SeqKey]:
        """Return the linked-list order with the current node moved to the end."""
        ordered = [
            seq_key for seq_key in cls.order_seq_keys(seq_keys)
            if int(seq_key.seq_key_id) != int(current_seq_key_id)
        ]
        after_seq_key_id = int(ordered[-1].seq_key_id) if ordered else None
        return cls.build_reordered_after(
            seq_keys=seq_keys,
            current_seq_key_id=current_seq_key_id,
            after_seq_key_id=after_seq_key_id,
        )

    @classmethod
    def build_order_moved_to_last_of_attr(
        cls,
        *,
        seq_keys: list[SeqKey],
        current_seq_key_id: int,
        current_bcc_manifest_id: BccManifestId,
        attribute_manifest_ids: set[int],
    ) -> list[SeqKey]:
        """Return the linked-list order with the current BCC moved after the leading attribute block."""
        ordered = [
            seq_key for seq_key in cls.order_seq_keys(seq_keys)
            if int(seq_key.seq_key_id) != int(current_seq_key_id)
        ]

        after_seq_key_id: int | None = None
        for seq_key in ordered:
            if seq_key.bcc_manifest_id is None:
                break
            if int(seq_key.bcc_manifest_id) == int(current_bcc_manifest_id):
                continue
            if int(seq_key.bcc_manifest_id) not in attribute_manifest_ids:
                break
            after_seq_key_id = int(seq_key.seq_key_id)

        return cls.build_reordered_after(
            seq_keys=seq_keys,
            current_seq_key_id=current_seq_key_id,
            after_seq_key_id=after_seq_key_id,
        )

    @staticmethod
    def order_seq_keys(seq_keys: list[SeqKey]) -> list[SeqKey]:
        """Return `seq_key` rows in linked-list order, appending orphan nodes last."""
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

    @staticmethod
    def _relink_ordered_seq_keys(ordered_seq_keys: list[SeqKey]) -> None:
        """Rewrite the linked-list pointers for an already ordered set of nodes."""
        for index, seq_key in enumerate(ordered_seq_keys):
            seq_key.prev_seq_key_id = int(ordered_seq_keys[index - 1].seq_key_id) if index > 0 else None
            seq_key.next_seq_key_id = (
                int(ordered_seq_keys[index + 1].seq_key_id)
                if index + 1 < len(ordered_seq_keys)
                else None
            )
