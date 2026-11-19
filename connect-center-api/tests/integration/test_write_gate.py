"""The gate of the writers against a real MariaDB: set CCS_TEST_DB_URL (mysql+asyncmy://root:pw@host:port/db) to run it."""

from __future__ import annotations

import asyncio
import os

import pytest
from sqlalchemy import text
from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker, create_async_engine

from app.write_gate import WriteFrozenError, install_write_gate

URL = os.environ.get("CCS_TEST_DB_URL")
pytestmark = pytest.mark.skipif(not URL, reason="CCS_TEST_DB_URL is not set")

WORKERS = 5


@pytest.fixture
async def engine():
    engine = create_async_engine(URL, pool_size=20)
    async with engine.begin() as conn:
        await conn.execute(text("DROP TABLE IF EXISTS named_lock, gate_probe"))
        await conn.execute(text("CREATE TABLE named_lock (lock_name varchar(100) PRIMARY KEY) ENGINE=InnoDB"))
        await conn.execute(text("INSERT INTO named_lock VALUES ('write_gate')"))
        await conn.execute(text("CREATE TABLE gate_probe (name varchar(20) PRIMARY KEY, value varchar(20)) ENGINE=InnoDB"))
        await conn.execute(text("INSERT INTO gate_probe VALUES ('setting', 'mine')"))
    yield engine
    async with engine.begin() as conn:
        await conn.execute(text("DROP TABLE IF EXISTS named_lock, gate_probe"))
    await engine.dispose()


async def _write(sessions, name):
    async with sessions() as session:
        await session.execute(text("INSERT INTO gate_probe VALUES (:n, 'written')"), {"n": name})
        await session.commit()


async def _read(sessions):
    async with sessions() as session:
        return (await session.execute(text("SELECT value FROM gate_probe WHERE name = 'setting'"))).scalar_one()


async def test_writers_wait_for_the_load_and_go_on_after_it_and_readers_do_not_wait(engine):
    install_write_gate(engine, wait_seconds=30)
    sessions = async_sessionmaker(engine, class_=AsyncSession, expire_on_commit=False)

    # The load: takes the gate, and keeps it.
    load = create_async_engine(URL)
    async with load.connect() as freezer:
        await freezer.execute(text("SELECT lock_name FROM named_lock WHERE lock_name = 'write_gate' FOR UPDATE"))

        writers = [asyncio.create_task(_write(sessions, f"w{i}")) for i in range(WORKERS)]
        readers = [asyncio.create_task(_read(sessions)) for _ in range(WORKERS)]
        values = await asyncio.wait_for(asyncio.gather(*readers), timeout=10)
        await asyncio.sleep(2)

        assert values == ["mine"] * WORKERS, "readers are not held, and read one value"
        assert not any(w.done() for w in writers), "no writer gets through while the load has the gate"
        await freezer.rollback()

    await asyncio.wait_for(asyncio.gather(*writers), timeout=20)
    await load.dispose()
    async with engine.connect() as conn:
        names = (await conn.execute(text("SELECT name FROM gate_probe WHERE name LIKE 'w%' ORDER BY name"))).scalars().all()
    assert names == [f"w{i}" for i in range(WORKERS)]


async def test_a_writer_that_waits_for_longer_than_it_may_is_refused(engine):
    install_write_gate(engine, wait_seconds=1)
    sessions = async_sessionmaker(engine, class_=AsyncSession, expire_on_commit=False)
    load = create_async_engine(URL)
    async with load.connect() as freezer:
        await freezer.execute(text("SELECT lock_name FROM named_lock WHERE lock_name = 'write_gate' FOR UPDATE"))
        with pytest.raises(WriteFrozenError):
            await _write(sessions, "late")
        await freezer.rollback()
    await load.dispose()
