"""A record refers to the revision of another component it was made with, and the manifest follows the revisions.

So what the repositories read, and what a revision is made with, is what the manifest holds: the latest revision.
"""

import pytest
import pytest_asyncio
from sqlalchemy.ext.asyncio import AsyncSession, create_async_engine
from sqlalchemy.orm import sessionmaker

from app.repositories.vendors.mariadb.core_component_repository import MariaDbCoreComponentRepository
from app.repositories.vendors.mariadb.data_type_repository import MariaDbDataTypeRepository
from app.repositories.vendors.mariadb.models.base import Base
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
)
from app.repositories.vendors.mariadb.models.data_type import Dt, DtManifest, DtSc, DtScManifest

USER = 1
RELEASE = 1

TABLES = [
    "acc",
    "acc_manifest",
    "ascc",
    "ascc_manifest",
    "asccp",
    "asccp_manifest",
    "dt",
    "dt_manifest",
    "dt_sc",
    "dt_sc_manifest",
    "bccp",
    "bccp_manifest",
    "bcc",
    "bcc_manifest",
]


@pytest_asyncio.fixture
async def session():
    engine = create_async_engine("sqlite+aiosqlite:///:memory:")
    async with engine.begin() as connection:
        await connection.run_sync(
            lambda sync: Base.metadata.create_all(sync, tables=[Base.metadata.tables[t] for t in TABLES])
        )
    async with sessionmaker(engine, class_=AsyncSession, expire_on_commit=False)() as s:
        yield s
    await engine.dispose()


def _dt(guid, based_dt_id=None, prev_dt_id=None):
    return Dt(
        guid=guid,
        data_type_term="Code",
        representation_term="Code",
        based_dt_id=based_dt_id,
        state="Published",
        commonly_used=False,
        owner_user_id=USER,
        created_by=USER,
        last_updated_by=USER,
        is_deprecated=False,
        prev_dt_id=prev_dt_id,
    )


def _dt_sc(guid, owner_dt_id, based_dt_sc_id=None):
    return DtSc(
        guid=guid,
        owner_dt_id=owner_dt_id,
        representation_term="Code",
        cardinality_min=0,
        cardinality_max=1,
        based_dt_sc_id=based_dt_sc_id,
        is_deprecated=False,
        created_by=USER,
        owner_user_id=USER,
        last_updated_by=USER,
    )


async def _base_and_derived(session):
    """A base DT with a supplementary component, and a DT derived from it, both of one release."""
    base = _dt("b" * 32)
    session.add(base)
    await session.flush()
    base_sc = _dt_sc("s" * 32, base.dt_id)
    session.add(base_sc)
    base_manifest = DtManifest(release_id=RELEASE, dt_id=base.dt_id, den="Base. Type")
    session.add(base_manifest)
    await session.flush()
    base_sc_manifest = DtScManifest(
        release_id=RELEASE, dt_sc_id=base_sc.dt_sc_id, owner_dt_manifest_id=base_manifest.dt_manifest_id
    )
    session.add(base_sc_manifest)

    derived = _dt("d" * 32, based_dt_id=base.dt_id)
    session.add(derived)
    await session.flush()
    derived_sc = _dt_sc("t" * 32, derived.dt_id, based_dt_sc_id=base_sc.dt_sc_id)
    session.add(derived_sc)
    derived_manifest = DtManifest(
        release_id=RELEASE, dt_id=derived.dt_id, den="Derived. Type", based_dt_manifest_id=base_manifest.dt_manifest_id
    )
    session.add(derived_manifest)
    await session.flush()
    derived_sc_manifest = DtScManifest(
        release_id=RELEASE,
        dt_sc_id=derived_sc.dt_sc_id,
        owner_dt_manifest_id=derived_manifest.dt_manifest_id,
        based_dt_sc_manifest_id=base_sc_manifest.dt_sc_manifest_id,
    )
    session.add(derived_sc_manifest)
    await session.flush()
    return base_manifest, base_sc_manifest, derived_manifest, derived_sc_manifest


async def _revise_base(session, base_manifest, base_sc_manifest):
    """What revising the base does to the records: new records, and the manifests hold them."""
    base = await session.get(Dt, base_manifest.dt_id)
    revised = _dt(base.guid, based_dt_id=base.based_dt_id, prev_dt_id=base.dt_id)
    session.add(revised)
    await session.flush()
    base.next_dt_id = revised.dt_id
    base_manifest.dt_id = revised.dt_id

    base_sc = await session.get(DtSc, base_sc_manifest.dt_sc_id)
    revised_sc = _dt_sc(base_sc.guid, revised.dt_id)
    session.add(revised_sc)
    await session.flush()
    base_sc.next_dt_sc_id = revised_sc.dt_sc_id
    base_sc_manifest.dt_sc_id = revised_sc.dt_sc_id
    await session.flush()
    return revised, revised_sc


def _repository(session, cls):
    repository = object.__new__(cls)
    repository._session = session
    return repository


@pytest.mark.asyncio
async def test_has_deriving_dts_follows_the_manifest_after_the_base_is_revised(session):
    base_manifest, base_sc_manifest, derived_manifest, _ = await _base_and_derived(session)
    await _revise_base(session, base_manifest, base_sc_manifest)

    repository = _repository(session, MariaDbDataTypeRepository)

    assert await repository.has_deriving_dts(base_manifest.dt_manifest_id)
    assert not await repository.has_deriving_dts(derived_manifest.dt_manifest_id)


@pytest.mark.asyncio
async def test_revised_dt_is_based_on_the_latest_base(session, monkeypatch):
    base_manifest, base_sc_manifest, derived_manifest, derived_sc_manifest = await _base_and_derived(session)
    revised_base, revised_base_sc = await _revise_base(session, base_manifest, base_sc_manifest)
    previous_derived = derived_manifest.dt_id

    async def nothing(*_args, **_kwargs):
        return None

    repository = _repository(session, MariaDbDataTypeRepository)
    for name in ("_append_dt_log", "_copy_dt_awd_pri_by_dt_id", "_copy_dt_sc_awd_pri_by_dt_sc_id"):
        monkeypatch.setattr(MariaDbDataTypeRepository, name, nothing)

    assert await repository.revise_dt(dt_manifest_id=derived_manifest.dt_manifest_id, requester_user_id=USER)

    revised = await session.get(Dt, derived_manifest.dt_id)
    assert revised.dt_id != previous_derived
    assert revised.based_dt_id == revised_base.dt_id
    revised_sc = await session.get(DtSc, derived_sc_manifest.dt_sc_id)
    assert revised_sc.based_dt_sc_id == revised_base_sc.dt_sc_id
    # the record that was revised keeps what it was made with
    assert (await session.get(Dt, previous_derived)).based_dt_id != revised_base.dt_id


@pytest.mark.asyncio
async def test_revised_bccp_has_the_latest_data_type(session, monkeypatch):
    dt = _dt("a" * 32)
    session.add(dt)
    await session.flush()
    dt_manifest = DtManifest(release_id=RELEASE, dt_id=dt.dt_id, den="Type. Type")
    session.add(dt_manifest)
    bccp = Bccp(
        guid="c" * 32,
        property_term="Code",
        representation_term="Code",
        bdt_id=dt.dt_id,
        state="Published",
        owner_user_id=USER,
        created_by=USER,
        last_updated_by=USER,
        is_deprecated=False,
        is_nillable=False,
    )
    session.add(bccp)
    await session.flush()
    bccp_manifest = BccpManifest(
        release_id=RELEASE, bccp_id=bccp.bccp_id, bdt_manifest_id=dt_manifest.dt_manifest_id, den="Code. Code"
    )
    session.add(bccp_manifest)
    await session.flush()

    # the Data Type is revised: the manifest holds the new record, the BCCP still refers to the old one
    revised_dt = _dt(dt.guid, prev_dt_id=dt.dt_id)
    session.add(revised_dt)
    await session.flush()
    dt.next_dt_id = revised_dt.dt_id
    dt_manifest.dt_id = revised_dt.dt_id
    await session.flush()

    async def nothing(*_args, **_kwargs):
        return None

    monkeypatch.setattr(MariaDbCoreComponentRepository, "_append_bccp_log", nothing)
    repository = _repository(session, MariaDbCoreComponentRepository)

    assert await repository.revise_bccp(bccp_manifest_id=bccp_manifest.bccp_manifest_id, requester_user_id=USER)

    revised = await session.get(Bccp, bccp_manifest.bccp_id)
    assert revised.prev_bccp_id == bccp.bccp_id
    assert revised.bdt_id == revised_dt.dt_id
    assert (await session.get(Bccp, bccp.bccp_id)).bdt_id == dt.dt_id


@pytest.mark.asyncio
async def test_revised_acc_associations_point_to_the_latest_bccp(session):
    """The BCC of a revised ACC is made with the BCCP the manifest holds, not the one the old BCC refers to."""
    dt = _dt("a" * 32)
    session.add(dt)
    await session.flush()
    dt_manifest = DtManifest(release_id=RELEASE, dt_id=dt.dt_id, den="Type. Type")
    session.add(dt_manifest)
    await session.flush()

    def bccp_of(guid, prev=None):
        return Bccp(
            guid=guid,
            property_term="Code",
            representation_term="Code",
            bdt_id=dt.dt_id,
            state="Published",
            owner_user_id=USER,
            created_by=USER,
            last_updated_by=USER,
            is_deprecated=False,
            is_nillable=False,
            prev_bccp_id=prev,
        )

    bccp = bccp_of("c" * 32)
    session.add(bccp)
    await session.flush()
    bccp_manifest = BccpManifest(
        release_id=RELEASE, bccp_id=bccp.bccp_id, bdt_manifest_id=dt_manifest.dt_manifest_id, den="Code. Code"
    )
    session.add(bccp_manifest)
    await session.flush()
    bcc = Bcc(
        guid="e" * 32,
        cardinality_min=0,
        cardinality_max=1,
        to_bccp_id=bccp.bccp_id,
        from_acc_id=1,
        seq_key=1,
        entity_type=1,
        state="Published",
        owner_user_id=USER,
        created_by=USER,
        last_updated_by=USER,
        is_deprecated=False,
        is_nillable=False,
    )
    session.add(bcc)
    await session.flush()
    bcc_manifest = BccManifest(
        release_id=RELEASE,
        bcc_id=bcc.bcc_id,
        from_acc_manifest_id=1,
        to_bccp_manifest_id=bccp_manifest.bccp_manifest_id,
        den="Acc. Code",
    )
    session.add(bcc_manifest)
    await session.flush()

    # the BCCP is revised: the manifest holds the new record, the BCC still refers to the old one
    revised_bccp = bccp_of(bccp.guid, prev=bccp.bccp_id)
    session.add(revised_bccp)
    await session.flush()
    bccp.next_bccp_id = revised_bccp.bccp_id
    bccp_manifest.bccp_id = revised_bccp.bccp_id
    await session.flush()

    repository = _repository(session, MariaDbCoreComponentRepository)
    await repository._revise_bccs(
        from_acc_manifest_id=1, next_acc_id=2, requester_user_id=USER, timestamp=bcc.creation_timestamp
    )

    revised_bcc = await session.get(Bcc, bcc_manifest.bcc_id)
    assert revised_bcc.prev_bcc_id == bcc.bcc_id
    assert revised_bcc.to_bccp_id == revised_bccp.bccp_id
    assert (await session.get(Bcc, bcc.bcc_id)).to_bccp_id == bccp.bccp_id


def _acc(guid, based_acc_id=None, prev_acc_id=None):
    return Acc(
        guid=guid,
        object_class_term="Thing",
        type="Default",
        state="Published",
        is_deprecated=False,
        is_abstract=False,
        based_acc_id=based_acc_id,
        prev_acc_id=prev_acc_id,
        owner_user_id=USER,
        created_by=USER,
        last_updated_by=USER,
    )


def _asccp(guid, role_of_acc_id, prev_asccp_id=None):
    return Asccp(
        guid=guid,
        property_term="Thing",
        type="Default",
        state="Published",
        is_deprecated=False,
        is_nillable=False,
        reusable_indicator=True,
        role_of_acc_id=role_of_acc_id,
        prev_asccp_id=prev_asccp_id,
        owner_user_id=USER,
        created_by=USER,
        last_updated_by=USER,
    )


class _NoLog:
    async def append_acc_log(self, **_kwargs):
        return None


async def _acc_with_manifest(session, guid, based_manifest=None):
    acc = _acc(guid, based_acc_id=None if based_manifest is None else based_manifest.acc_id)
    session.add(acc)
    await session.flush()
    manifest = AccManifest(
        release_id=RELEASE,
        acc_id=acc.acc_id,
        den="Thing. Details",
        based_acc_manifest_id=None if based_manifest is None else based_manifest.acc_manifest_id,
    )
    session.add(manifest)
    await session.flush()
    return acc, manifest


async def _revise_acc_record(session, acc, manifest):
    """What revising the base ACC does to the records: a new record, and the manifest holds it."""
    revised = _acc(acc.guid, prev_acc_id=acc.acc_id)
    session.add(revised)
    await session.flush()
    acc.next_acc_id = revised.acc_id
    manifest.acc_id = revised.acc_id
    await session.flush()
    return revised


@pytest.mark.asyncio
async def test_revised_acc_is_based_on_the_latest_base_acc(session, monkeypatch):
    base, base_manifest = await _acc_with_manifest(session, "a" * 32)
    derived, derived_manifest = await _acc_with_manifest(session, "b" * 32, base_manifest)
    revised_base = await _revise_acc_record(session, base, base_manifest)

    async def relationships(*_args, **_kwargs):
        return []

    monkeypatch.setattr(MariaDbCoreComponentRepository, "get_acc_relationships", relationships)
    repository = _repository(session, MariaDbCoreComponentRepository)
    repository._log_repo = _NoLog()

    assert await repository.revise_acc(
        acc_manifest_id=derived_manifest.acc_manifest_id, requester_user_id=USER
    )

    revised = await session.get(Acc, derived_manifest.acc_id)
    assert revised.prev_acc_id == derived.acc_id
    assert revised.based_acc_id == revised_base.acc_id
    assert (await session.get(Acc, derived.acc_id)).based_acc_id == base.acc_id


@pytest.mark.asyncio
async def test_revised_asccp_has_the_latest_role_of_acc(session, monkeypatch):
    acc, acc_manifest = await _acc_with_manifest(session, "a" * 32)
    asccp = _asccp("c" * 32, acc.acc_id)
    session.add(asccp)
    await session.flush()
    asccp_manifest = AsccpManifest(
        release_id=RELEASE,
        asccp_id=asccp.asccp_id,
        role_of_acc_manifest_id=acc_manifest.acc_manifest_id,
        den="Thing. Thing",
    )
    session.add(asccp_manifest)
    await session.flush()
    revised_acc = await _revise_acc_record(session, acc, acc_manifest)

    async def nothing(*_args, **_kwargs):
        return None

    monkeypatch.setattr(MariaDbCoreComponentRepository, "_append_asccp_log", nothing)
    repository = _repository(session, MariaDbCoreComponentRepository)

    assert await repository.revise_asccp(
        asccp_manifest_id=asccp_manifest.asccp_manifest_id, requester_user_id=USER
    )

    revised = await session.get(Asccp, asccp_manifest.asccp_id)
    assert revised.prev_asccp_id == asccp.asccp_id
    assert revised.role_of_acc_id == revised_acc.acc_id
    assert (await session.get(Asccp, asccp.asccp_id)).role_of_acc_id == acc.acc_id


@pytest.mark.asyncio
async def test_revised_acc_associations_point_to_the_latest_asccp(session):
    """The ASCC of a revised ACC is made with the ASCCP the manifest holds, not the one the old ASCC refers to."""
    role, role_manifest = await _acc_with_manifest(session, "a" * 32)
    asccp = _asccp("c" * 32, role.acc_id)
    session.add(asccp)
    await session.flush()
    asccp_manifest = AsccpManifest(
        release_id=RELEASE,
        asccp_id=asccp.asccp_id,
        role_of_acc_manifest_id=role_manifest.acc_manifest_id,
        den="Thing. Thing",
    )
    session.add(asccp_manifest)
    ascc = Ascc(
        guid="e" * 32,
        cardinality_min=0,
        cardinality_max=1,
        from_acc_id=1,
        to_asccp_id=asccp.asccp_id,
        seq_key=1,
        state="Published",
        is_deprecated=False,
        owner_user_id=USER,
        created_by=USER,
        last_updated_by=USER,
    )
    session.add(ascc)
    await session.flush()
    ascc_manifest = AsccManifest(
        release_id=RELEASE,
        ascc_id=ascc.ascc_id,
        from_acc_manifest_id=1,
        to_asccp_manifest_id=asccp_manifest.asccp_manifest_id,
        den="Owner. Thing",
    )
    session.add(ascc_manifest)
    await session.flush()

    revised_asccp = _asccp(asccp.guid, role.acc_id, prev_asccp_id=asccp.asccp_id)
    session.add(revised_asccp)
    await session.flush()
    asccp.next_asccp_id = revised_asccp.asccp_id
    asccp_manifest.asccp_id = revised_asccp.asccp_id
    await session.flush()

    repository = _repository(session, MariaDbCoreComponentRepository)
    await repository._revise_asccs(
        from_acc_manifest_id=1, next_acc_id=2, requester_user_id=USER, timestamp=ascc.creation_timestamp
    )

    revised_ascc = await session.get(Ascc, ascc_manifest.ascc_id)
    assert revised_ascc.prev_ascc_id == ascc.ascc_id
    assert revised_ascc.to_asccp_id == revised_asccp.asccp_id
    assert (await session.get(Ascc, ascc.ascc_id)).to_asccp_id == asccp.asccp_id
