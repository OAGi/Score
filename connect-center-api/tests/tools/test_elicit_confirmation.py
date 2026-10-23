"""Confirmation prompts work on both MCP protocol eras (``ctx.elicit`` and the input-required round trip)."""

import pytest
from fastmcp import Client, Context, FastMCP
from mcp.types import ElicitResult, InputRequiredResult
from pydantic import BaseModel

from app.tools import elicit_confirmation, elicit_form


class Definition(BaseModel):
    definition: str


def _server() -> FastMCP:
    server = FastMCP("elicit-test")

    @server.tool
    async def confirm(ctx: Context) -> str | InputRequiredResult:
        answer = await elicit_confirmation(ctx, "Are you sure?")
        if isinstance(answer, InputRequiredResult):
            return answer
        return answer.action

    @server.tool
    async def describe(ctx: Context) -> str | InputRequiredResult:
        answer = await elicit_form(ctx, "Define it", Definition)
        if isinstance(answer, InputRequiredResult):
            return answer
        return answer.data.definition if answer.action == "accept" else answer.action

    return server


def _handler(result: ElicitResult):
    async def handler(message, response_type, params, context):
        assert params.requested_schema["type"] == "object"
        return result

    return handler


ANSWERS = [
    (ElicitResult(action="accept", content={}), "accept"),
    (ElicitResult(action="accept"), "accept"),
    (ElicitResult(action="decline"), "decline"),
    (ElicitResult(action="cancel"), "cancel"),
]
IDS = ["accept-empty-form", "accept-without-content", "decline", "cancel"]
MODES = ["legacy", "2026-07-28"]


@pytest.mark.parametrize("mode", MODES)
@pytest.mark.parametrize(("answer", "expected"), ANSWERS, ids=IDS)
async def test_confirmation_maps_every_client_answer(mode, answer, expected):
    async with Client(_server(), elicitation_handler=_handler(answer), mode=mode) as client:
        result = await client.call_tool("confirm", {})

    assert result.data == expected


@pytest.mark.parametrize("mode", MODES)
async def test_form_returns_the_typed_answer(mode):
    answer = ElicitResult(action="accept", content={"definition": "A thing"})
    async with Client(_server(), elicitation_handler=_handler(answer), mode=mode) as client:
        result = await client.call_tool("describe", {})

    assert result.data == "A thing"
