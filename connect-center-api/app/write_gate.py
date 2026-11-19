"""The gate that stops writing while the schema of the application (score-http) is swapped.

score-http, when it loads a release package, takes one row of the table the row ``write_gate`` of the table ``named_lock`` with an exclusive lock for as long as it
copies, checks and swaps. Every writer of the database, in whatever process, has to read that row with a shared lock before it writes:
InnoDB makes the load wait for the writers that are at work, and makes the writers that come after it wait until the swap is over. Reading
never goes through the gate.

The shared lock lasts until the end of the transaction, so the gate is read once, before the first write of a transaction.
"""

from __future__ import annotations

import logging
import re

from sqlalchemy import event
from sqlalchemy.ext.asyncio import AsyncEngine

logger = logging.getLogger(__name__)

GATE_TABLE = "named_lock"
GATE_NAME = "write_gate"

# MariaDB: lock wait timeout (ER_LOCK_WAIT_TIMEOUT), the statement given up by WAIT (ER_LOCK_ABORTED), no such table (ER_NO_SUCH_TABLE).
_LOCK_TIMEOUT_CODES = {1205, 1689}
_NO_SUCH_TABLE = 1146

_WRITE_STATEMENT = re.compile(r"^\s*(insert|replace|update|delete)\b", re.IGNORECASE)
_ENTERED = "write_gate_entered"


class WriteFrozenError(RuntimeError):
    """A write that waited for the gate for longer than it may: writing is stopped while the schema is swapped."""


def _code(error: BaseException) -> int | None:
    original = getattr(error, "orig", error)
    args = getattr(original, "args", ())
    return args[0] if args and isinstance(args[0], int) else None


def install_write_gate(engine: AsyncEngine, wait_seconds: int = 60) -> None:
    """Makes every transaction of the engine go through the gate before its first write.

    A database that has no gate table (a schema older than the one that has it) is not gated, which is said once.
    """
    sync_engine = engine.sync_engine
    state = {"available": True}
    gate_sql = f"SELECT lock_name FROM {GATE_TABLE} WHERE lock_name = '{GATE_NAME}' LOCK IN SHARE MODE WAIT {max(1, int(wait_seconds))}"

    @event.listens_for(sync_engine, "before_cursor_execute")
    def _enter(conn, cursor, statement, parameters, context, executemany):  # noqa: ANN001
        if not state["available"] or not _WRITE_STATEMENT.match(statement) or conn.info.get(_ENTERED):
            return
        try:
            cursor.execute(gate_sql)
        except Exception as error:  # noqa: BLE001 - the driver's own error classes
            code = _code(error)
            if code in _LOCK_TIMEOUT_CODES:
                raise WriteFrozenError("Writing is stopped while the schema is swapped; try again in a moment.") from error
            if code == _NO_SUCH_TABLE:
                logger.warning("The database has no %s table, so writes are not gated.", GATE_TABLE)
                state["available"] = False
                return
            raise
        conn.info[_ENTERED] = True

    @event.listens_for(sync_engine.pool, "checkout")
    def _reset(dbapi_connection, connection_record, connection_proxy):  # noqa: ANN001
        connection_record.info.pop(_ENTERED, None)

    @event.listens_for(sync_engine, "commit")
    @event.listens_for(sync_engine, "rollback")
    def _leave(conn):  # noqa: ANN001
        conn.info.pop(_ENTERED, None)
