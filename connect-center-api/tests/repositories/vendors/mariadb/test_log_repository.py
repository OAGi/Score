"""Tests that revision-log persistence matches what score-http (LogSerializer / JooqLogCommandRepository) writes.

Every component type (ACC, ASCCP, BCCP, DT, code list) must get, for create / update / delete / restore / revise /
cancel, a log row whose action, revision numbering, prev/next chain, manifest head and snapshot match score-http.
"""

from __future__ import annotations

import json
import re
from datetime import datetime
from pathlib import Path

import pytest

pytest.importorskip("aiosqlite")

from sqlalchemy import Boolean, DateTime, Integer, String, Text, event, select  # noqa: E402
from sqlalchemy.dialects import mysql  # noqa: E402
from sqlalchemy.ext.asyncio import AsyncSession, create_async_engine  # noqa: E402
from sqlalchemy.ext.compiler import compiles  # noqa: E402

from app.repositories.vendors.mariadb.log_repository import MariaDbLogRepository  # noqa: E402
from app.repositories.vendors.mariadb.models.agency_id_list import (  # noqa: E402
    AgencyIdListValue,
    AgencyIdListValueManifest,
)
from app.repositories.vendors.mariadb.models.app_user import AppUser  # noqa: E402
from app.repositories.vendors.mariadb.models.base import Base  # noqa: E402
from app.repositories.vendors.mariadb.models.code_list import (  # noqa: E402
    CodeList,
    CodeListManifest,
    CodeListValue,
    CodeListValueManifest,
)
from app.repositories.vendors.mariadb.models.core_component import (  # noqa: E402
    Acc,
    AccManifest,
    Asccp,
    AsccpManifest,
    Bccp,
    BccpManifest,
)
from app.repositories.vendors.mariadb.models.data_type import Dt, DtManifest, DtSc, DtScManifest  # noqa: E402
from app.repositories.vendors.mariadb.models.log import Log  # noqa: E402
from app.repositories.vendors.mariadb.models.namespace import Namespace  # noqa: E402

REPO_ROOT = Path(__file__).resolve().parents[5]
APP_ROOT = REPO_ROOT / "connect-center-api" / "app"
JAVA_LOG_ACTION = (
    REPO_ROOT
    / "score-http/src/main/java/org/oagi/score/gateway/http/api/log_management/model/LogAction.java"
)
NOW = datetime(2026, 1, 1, 12, 0, 0)


@compiles(mysql.TINYINT, "sqlite")
@compiles(mysql.BIGINT, "sqlite")
def _render_integer_on_sqlite(_element, _compiler, **_kw):
    """SQLite only auto-increments a column typed exactly INTEGER PRIMARY KEY."""
    return "INTEGER"

_TABLES = (
    "log", "app_user", "namespace",
    "acc", "acc_manifest", "asccp", "asccp_manifest", "bccp", "bccp_manifest",
    "dt", "dt_manifest", "dt_sc", "dt_sc_manifest",
    "code_list", "code_list_manifest", "code_list_value", "code_list_value_manifest",
    "agency_id_list_value", "agency_id_list_value_manifest",
)


def _filler(column):
    """Return a dummy value for a NOT NULL column without a default."""
    if isinstance(column.type, Boolean):
        return False
    if isinstance(column.type, DateTime):
        return NOW
    if isinstance(column.type, Integer):
        return 1
    if isinstance(column.type, (String, Text)):
        return "x"
    return 1


async def _seed(session: AsyncSession, model, **values):
    """Insert a row, filling every required column that the test does not care about."""
    for column in model.__table__.columns:
        if column.name in values or column.nullable or column.default is not None or column.server_default is not None:
            continue
        if column.primary_key and column.autoincrement:
            continue
        values[column.name] = _filler(column)
    row = model(**values)
    session.add(row)
    await session.flush()
    return row


@pytest.fixture
async def session():
    engine = create_async_engine("sqlite+aiosqlite://")

    def create(conn):
        for name in _TABLES:
            table = Base.metadata.tables[name]
            for column in table.columns:
                column.foreign_keys = set()
                if getattr(column.type, "collation", None):
                    column.type.collation = None
            table.foreign_keys = set()
            table.constraints = {c for c in table.constraints if c.__class__.__name__ != "ForeignKeyConstraint"}
            table.create(conn)

    async with engine.begin() as conn:
        await conn.run_sync(create)
    async with AsyncSession(engine, expire_on_commit=False) as s:
        yield s
    await engine.dispose()


@pytest.fixture
async def seeded(session):
    """Seed a developer, a namespace and one WIP component of every type."""
    user = await _seed(session, AppUser, login_id="dev", is_developer=True)
    ns = await _seed(session, Namespace, uri="urn:x", is_std_nmsp=True, owner_user_id=user.app_user_id)
    common = {"owner_user_id": user.app_user_id, "namespace_id": ns.namespace_id, "state": "WIP"}

    acc = await _seed(session, Acc, guid="acc-guid", object_class_term="Party", oagis_component_type=1,
                      is_deprecated=False, is_abstract=False, **common)
    acc_m = await _seed(session, AccManifest, acc_id=acc.acc_id, den="Party. Details")
    asccp = await _seed(session, Asccp, guid="asccp-guid", property_term="Party", reusable_indicator=True,
                        is_deprecated=False, is_nillable=False, **common)
    asccp_m = await _seed(session, AsccpManifest, asccp_id=asccp.asccp_id, role_of_acc_manifest_id=acc_m.acc_manifest_id,
                          den="Party. Party")
    dt = await _seed(session, Dt, guid="dt-guid", data_type_term="Code", representation_term="Code",
                     is_deprecated=False, **common)
    dt_m = await _seed(session, DtManifest, dt_id=dt.dt_id, den="Code. Type")
    sc = await _seed(session, DtSc, guid="sc-guid", property_term="List", representation_term="Identifier",
                     cardinality_min=0, cardinality_max=1, is_deprecated=False, definition="sc def",
                     owner_user_id=user.app_user_id)
    await _seed(session, DtScManifest, dt_sc_id=sc.dt_sc_id, owner_dt_manifest_id=dt_m.dt_manifest_id)
    bccp = await _seed(session, Bccp, guid="bccp-guid", property_term="Amount", representation_term="Amount",
                       is_deprecated=False, is_nillable=False, default_value="0", **common)
    bccp_m = await _seed(session, BccpManifest, bccp_id=bccp.bccp_id, bdt_manifest_id=dt_m.dt_manifest_id,
                         den="Amount. Amount")
    agency = await _seed(session, AgencyIdListValue, guid="agency-guid", value="6", name="UN/CEFACT")
    agency_m = await _seed(session, AgencyIdListValueManifest, agency_id_list_value_id=agency.agency_id_list_value_id)
    cl = await _seed(session, CodeList, guid="cl-guid", name="Currency", list_id="L", version_id="1",
                     extensible_indicator=True, is_deprecated=False, definition="cl def",
                     **common)
    cl_m = await _seed(session, CodeListManifest, code_list_id=cl.code_list_id,
                       agency_id_list_value_manifest_id=agency_m.agency_id_list_value_manifest_id)
    clv = await _seed(session, CodeListValue, guid="clv-guid", code_list_id=cl.code_list_id, value="USD",
                      meaning="Dollar", is_deprecated=False, owner_user_id=user.app_user_id)
    await _seed(session, CodeListValueManifest, code_list_manifest_id=cl_m.code_list_manifest_id,
                code_list_value_id=clv.code_list_value_id)
    return {
        "user": user,
        "acc": acc_m, "asccp": asccp_m, "bccp": bccp_m, "dt": dt_m, "code_list": cl_m,
    }


class _Case:
    """How to append a log for one component type and which snapshot keys score-http emits for it."""

    def __init__(self, key, component, guid, top_keys, metadata_keys):
        self.key = key
        self.component = component
        self.guid = guid
        self.top_keys = top_keys
        self.metadata_keys = metadata_keys

    async def append(self, repo, manifest, user, action):
        common = {"requester_user_id": user.app_user_id, "action": action, "timestamp": NOW}
        if self.key == "acc":
            return await repo.append_acc_log(acc_manifest_id=manifest.acc_manifest_id, relationships=[], **common)
        if self.key == "asccp":
            return await repo.append_asccp_log(asccp_manifest_id=manifest.asccp_manifest_id, **common)
        if self.key == "bccp":
            return await repo.append_bccp_log(bccp_manifest_id=manifest.bccp_manifest_id, **common)
        if self.key == "dt":
            return await repo.append_dt_log(dt_manifest_id=manifest.dt_manifest_id, **common)
        return await repo.append_code_list_log(code_list_manifest_id=manifest.code_list_manifest_id, **common)


# Keys come from LogSerializer.serialize(...) in score-http. Optional keys (definition, defaultValue, ...) are omitted.
_CASES = [
    _Case("acc", "acc", "acc-guid",
          {"component", "guid", "objectClassTerm", "objectClassQualifier", "componentType", "state", "deprecated",
           "abstract", "basedAcc", "ownerUser", "namespace", "associations", "_metadata"},
          {"accManifest", "acc", "associations"}),
    _Case("asccp", "asccp", "asccp-guid",
          {"component", "guid", "propertyTerm", "state", "reusable", "deprecated", "nillable", "roleOfAcc",
           "ownerUser", "namespace", "_metadata"},
          {"asccpManifest", "asccp"}),
    _Case("bccp", "bccp", "bccp-guid",
          {"component", "guid", "propertyTerm", "representationTerm", "state", "deprecated", "nillable", "bdt",
           "ownerUser", "namespace", "_metadata", "defaultValue", "fixedValue"},
          {"bccpManifest", "bccp"}),
    _Case("dt", "dt", "dt-guid",
          {"component", "guid", "representationTerm", "dataTypeTerm", "qualifier", "contentComponentDefinition",
           "sixDigitId", "state", "deprecated", "basedDt", "ownerUser", "namespace", "supplementaryComponents",
           "_metadata"},
          {"dtManifest", "dt", "supplementaryComponents"}),
    _Case("code_list", "codeList", "cl-guid",
          {"component", "guid", "name", "listId", "agencyId", "versionId", "remark", "definition",
           "definitionSource", "state", "deprecated", "extensible", "basedCodeList", "ownerUser", "namespace",
           "values", "_metadata"},
          {"codeListManifest", "codeList", "values"}),
]
_IDS = [c.key for c in _CASES]


async def _log(session, log_id) -> Log:
    return await session.get(Log, log_id)


@pytest.mark.parametrize("case", _CASES, ids=_IDS)
async def test_added_log_has_score_http_snapshot(session, seeded, case):
    """Creating a component writes revision 1/1 with a snapshot in score-http's shape (never NULL)."""
    manifest = seeded[case.key]
    repo = MariaDbLogRepository(session)

    log_id = await case.append(repo, manifest, seeded["user"], "Added")

    log = await _log(session, log_id)
    assert (log.log_action, log.revision_num, log.revision_tracking_num) == ("Added", 1, 1)
    assert log.reference == case.guid
    assert log.prev_log_id is None and log.next_log_id is None
    assert log.snapshot is not None
    snapshot = json.loads(log.snapshot)
    assert snapshot["component"] == case.component
    assert snapshot["guid"] == case.guid
    # Gson omits nulls and score-http readers treat missing == null, so only non-null keys must be a subset.
    assert {k for k, v in snapshot.items() if v is not None} <= case.top_keys | {"definition", "definitionSource"}
    assert {"component", "guid", "state", "ownerUser", "namespace", "_metadata"} <= set(snapshot)
    assert case.metadata_keys <= set(snapshot["_metadata"])
    assert snapshot["ownerUser"] == {"username": "dev", "roles": ["developer"]}
    assert snapshot["namespace"] == {"uri": "urn:x", "standard": True}
    assert snapshot["state"] == "WIP"
    assert manifest.log_id == log_id


@pytest.mark.parametrize("case", _CASES, ids=_IDS)
async def test_update_delete_restore_chain_follows_score_http_numbering(session, seeded, case):
    """Modified/Deleted/Restored keep the revision and bump the tracking number, linking prev/next."""
    manifest = seeded[case.key]
    repo = MariaDbLogRepository(session)
    ids = []
    for action in ("Added", "Modified", "Deleted", "Restored"):
        ids.append(await case.append(repo, manifest, seeded["user"], action))

    logs = [await _log(session, i) for i in ids]
    assert [(l.log_action, l.revision_num, l.revision_tracking_num) for l in logs] == [
        ("Added", 1, 1), ("Modified", 1, 2), ("Deleted", 1, 3), ("Restored", 1, 4),
    ]
    assert [l.prev_log_id for l in logs] == [None, ids[0], ids[1], ids[2]]
    assert [l.next_log_id for l in logs] == [ids[1], ids[2], ids[3], None]
    assert manifest.log_id == ids[-1]
    assert all(l.snapshot for l in logs)


@pytest.mark.parametrize("case", _CASES, ids=_IDS)
async def test_revise_bumps_revision_and_cancel_restores_stable_log(session, seeded, case):
    """Revised starts revision N+1 / tracking 1; cancelling deletes that chain and returns the stable head."""
    manifest = seeded[case.key]
    repo = MariaDbLogRepository(session)
    added = await case.append(repo, manifest, seeded["user"], "Added")
    revised = await case.append(repo, manifest, seeded["user"], "Revised")
    modified = await case.append(repo, manifest, seeded["user"], "Modified")

    log = await _log(session, revised)
    assert (log.revision_num, log.revision_tracking_num) == (2, 1)
    assert (await _log(session, modified)).revision_tracking_num == 2

    stable = await repo.revert_component_log_to_stable_state(reference=case.guid, current_log_id=modified)

    assert stable == added
    assert (await _log(session, added)).next_log_id is None
    assert await session.get(Log, revised) is None
    assert await session.get(Log, modified) is None


async def test_every_snapshot_is_valid_json_with_component_for_json_extract(session, seeded):
    """score-http finds a component's logs with json_extract(snapshot, '$.component'); it must always be set."""
    repo = MariaDbLogRepository(session)
    for case in _CASES:
        await case.append(repo, seeded[case.key], seeded["user"], "Added")
    rows = (await session.execute(select(Log))).scalars().all()
    assert {json.loads(r.snapshot)["component"] for r in rows} == {c.component for c in _CASES}


async def test_dt_snapshot_includes_supplementary_components(session, seeded):
    """DT snapshots carry dtSc children keyed by GUID, as LogSerializer.serialize(DtScDetailsRecord) does."""
    repo = MariaDbLogRepository(session)
    await _CASES[3].append(repo, seeded["dt"], seeded["user"], "Added")
    snapshot = json.loads((await session.execute(select(Log.snapshot))).scalar_one())

    assert snapshot["supplementaryComponents"] == [{
        "component": "dtSc", "guid": "sc-guid", "propertyTerm": "List", "representationTerm": "Identifier",
        "cardinalityMin": 0, "cardinalityMax": 1, "basedDtSc": {}, "definition": "sc def",
        "definitionSource": None,
    }]
    assert set(snapshot["_metadata"]["supplementaryComponents"][0]) == {"dtScManifest", "dtSc"}


async def test_code_list_snapshot_includes_values_and_agency_id(session, seeded):
    """Code list snapshots carry codeListValue children and the agency ID summary, as LogSerializer does."""
    repo = MariaDbLogRepository(session)
    await _CASES[4].append(repo, seeded["code_list"], seeded["user"], "Added")
    snapshot = json.loads((await session.execute(select(Log.snapshot))).scalar_one())

    assert snapshot["agencyId"] == {"guid": "agency-guid", "value": "6", "name": "UN/CEFACT"}
    assert snapshot["values"] == [{
        "component": "codeListValue", "guid": "clv-guid", "value": "USD", "meaning": "Dollar", "deprecated": False,
    }]
    assert set(snapshot["_metadata"]["values"][0]) == {"codeListValueManifest", "codeListValue"}
    assert snapshot["basedCodeList"] == {}


async def test_missing_manifest_raises_lookup_error(session, seeded):
    repo = MariaDbLogRepository(session)
    for case in _CASES:
        class _Missing:
            acc_manifest_id = asccp_manifest_id = bccp_manifest_id = dt_manifest_id = code_list_manifest_id = 9999
        with pytest.raises(LookupError):
            await case.append(repo, _Missing, seeded["user"], "Added")


def test_every_log_action_written_by_connect_center_api_exists_in_score_http():
    """score-http parses log_action with LogAction.valueOf(), so an unknown value breaks its log pages."""
    if not JAVA_LOG_ACTION.exists():
        pytest.skip("score-http sources are not available")
    enum_body = JAVA_LOG_ACTION.read_text().split("{", 1)[1]
    valid = set(re.findall(r"\b([A-Za-z]+)\b", enum_body))

    written: dict[str, set[str]] = {}
    pattern = re.compile(r"""\baction\s*=\s*(?:\(?\s*)?(?:["']([A-Z][A-Za-z]+)["'])""")
    conditional = re.compile(r"""["']([A-Z][A-Za-z]+)["']""")
    for path in [*(APP_ROOT / "services").rglob("*.py"), *(APP_ROOT / "repositories").rglob("*.py")]:
        for line in path.read_text().splitlines():
            if re.search(r"\baction\s*=", line) and not re.search(r"requested_action|action=action", line):
                for literal in conditional.findall(line):
                    written.setdefault(literal, set()).add(path.name)
            elif pattern.search(line):
                written.setdefault(pattern.search(line).group(1), set()).add(path.name)

    unknown = {a: sorted(f) for a, f in written.items() if a not in valid}
    assert not unknown, f"Log actions unknown to score-http LogAction: {unknown}"
