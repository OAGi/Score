"""Tests for MariaDB release repository mapping helpers."""

from datetime import datetime

from app.repositories.vendors.mariadb.release_repository import _to_release_row


def test_release_row_is_latest_when_next_release_is_working():
    """A release whose next release is Working is marked latest."""
    row = _to_release_row(
        _release_tuple(
            prev_release_id=22,
            prev_release_num="10.12.7",
            prev_release_state="Published",
            next_release_id=24,
            next_release_num="Working",
            next_release_state="Initialized",
        )
    )

    assert row.is_latest is True
    assert row.prev_release is not None
    assert row.prev_release.release_id == 22
    assert row.prev_release.release_num == "10.12.7"
    assert row.next_release is not None
    assert row.next_release.release_id == 24
    assert row.next_release.release_num == "Working"


def test_release_row_is_not_latest_when_next_release_is_not_working():
    """A release whose next release is not Working is not marked latest."""
    row = _to_release_row(
        _release_tuple(
            prev_release_id=21,
            prev_release_num="10.12.6",
            prev_release_state="Published",
            next_release_id=23,
            next_release_num="10.12.8",
            next_release_state="Published",
        )
    )

    assert row.is_latest is False
    assert row.next_release is not None
    assert row.next_release.release_num == "10.12.8"


def _release_tuple(
    *,
    prev_release_id,
    prev_release_num,
    prev_release_state,
    next_release_id,
    next_release_num,
    next_release_state,
):
    timestamp = datetime(2026, 5, 7, 15, 48, 24)
    return (
        23,
        3,
        "connectSpec",
        "6fbaba3dbc224d179716d3ff6924dad4",
        "10.12.8",
        None,
        None,
        1,
        "",
        "http://www.openapplications.org/oagis/10",
        "Published",
        timestamp,
        timestamp,
        1,
        1,
        prev_release_id,
        prev_release_num,
        prev_release_state,
        next_release_id,
        next_release_num,
        next_release_state,
    )
