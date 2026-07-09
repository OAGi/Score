---
title: Issue Management
sidebar_position: 4
---

This page summarizes the project's conventions for filing, labeling, triaging, and closing issues, and how those issues connect to test-case authoring. connectCenter (formerly **Score**) tracks all of its work on GitHub.

:::note[Where issues live]
All issues are tracked in the GitHub repository at [github.com/OAGi/Score/issues](https://github.com/OAGi/Score/issues). Search this list before opening anything new.
:::

## Issue labels

Three labels drive triage. Apply the one that matches the nature of the issue:

| Label | When to use it |
| --- | --- |
| **Bug** | There is unexpected behavior, or behavior that is inconsistent with the design. |
| **Enhancement** | The issue is about a new feature. |
| **Design change** | The current behavior is *as designed*, but the design itself needs to change to reach a more desirable behavior. |

The distinction between **Bug** and **Design change** matters: a Bug deviates from the existing design, while a Design change revises the design that the software already correctly follows.

## Avoid duplicate issues

Before posting, search the existing issues for one that is the same or similar. If a matching issue already exists, add your unique comments there rather than opening a new one. You can also escalate that issue's milestone if needed.

:::tip
Searching first keeps the discussion in one place and prevents fragmented context across near-identical issues.
:::

## Creating a new issue

The issue name should communicate the problematic behavior or new functional requirement simply and directly — for example, *"A Context Category cannot be updated."*

For a **bug**, provide:

- Step-by-step instructions to reproduce the problematic behavior.
- A description of the **expected** behavior.
- Screenshots and a stacktrace when available.

A well-formed bug report looks like this:

```text
Name: The issue is that we cannot update a context category

Description:
To reproduce the issue:
1. Login as an end user
2. Create a context category
3. Open the context category
4. Change the description field
At this point the "Update" button is disabled.

The expected behavior is that a context category can be updated if we
change the description field.
This issue applies to both developer and end user accounts.
```

## Triage and the contribution workflow

Once an issue exists, it is discussed by the core team. If accepted:

1. Assign an appropriate label — **Bug**, **Enhancement**, or **Design change**.
2. For an **Enhancement**, consider capturing high-level functional requirements and design notes in a dedicated requirements document. This record also serves to document discussions with other developers and stakeholders.
3. For a **Design change**, update the corresponding functional-requirements document, if one exists, to reflect the new design.
4. Break the original issue into multiple issues when appropriate — especially when what began as a minor enhancement turns out to touch several areas.

If the issue is **not accepted**, close it.

### Assigning to a milestone and project

An issue can be assigned to a milestone and project by the person who works on it, after considering the milestone's target release date. The core team may move an issue to a different milestone or project when the target release date changes, or when it becomes clear the issue cannot be closed by that date.

### Working the issue

When development begins, the issue moves across a set of kanban cards. An issue may pass through these cards in various orders and more than once; with multiple contributors, some activities (for example, writing detailed test assertions and coding) can proceed in parallel. The cards are:

- **Writing Test Assertion** — author the test assertions (see [Test cases](#test-cases-and-acceptance-testing) below).
- **Coding and Unit Testing** — implement the change and its unit tests.
- **Implementing test script** — turn assertions into executable acceptance tests.
- **Debugging** — run the tests and adjust related content.
- **Updating user guide** — keep the documentation in sync.

When the work for the relevant cards is done, open a pull request. A PR may be opened per task or once for the whole issue; in either case, its comments should identify the specific task and the issue it addresses.

## Closing an issue

The issue should be closed by the person who ran the acceptance test on it — typically the person who merges the change. If a regression appears later, the issue can be reopened, or a new issue can be created. When a merge causes a failure in a routine test, file a new issue that optionally cites the original.

:::note
If a pull request is not accepted, the contributor cycles back through the workflow — re-entering, for example, the **Debugging** stage — rather than abandoning the issue.
:::

## Test cases and acceptance testing

Issue work is tied closely to the project's **test case documents**, which record the expected behavior (both normal and exceptional) of each feature, along with its rules and constraints. These documents feed acceptance-test scripts and frequently the user guide as well.

Test case documents use three levels of functional decomposition:

- **Test Suite (TS)** — typically one per functionality (which usually maps to an application menu item, e.g. a BIE Management test suite for the BIE Management menu). Suites are numbered sequentially starting at 1.
- **Test Case (TC)** — a grouping of test assertions that share a common pre-condition (system and data setup) or sub-functionality. Numbered sequentially within a suite.
- **Test Assertion (TA)** — a single condition or behavior being verified. Numbered sequentially within a case.

**Test Steps** are optional. They describe the sequence of actions that lead to verifying each assertion, and it is good practice for a step to link back to the assertion(s) it verifies. Pre-conditions may appear at any level to describe the state the System Under Test (SUT) must be in before the test applies.

The test suites are authored as Markdown files (`TestSuite1.md` … `TestSuite45.md`) under the project's `docs/test_cases` directory — **45** suites at the time of writing — covering authentication, user management, core component management for developers and end users, code lists, agency ID lists, BIEs, namespaces, modules, releases, business terms, OpenAPI documents, BIE uplifting, and more. The corresponding automated Selenium tests live in the `score-e2e` module.

A test suite document follows this skeleton:

```text
# Test Suite #{test_suite_number}
**{test_suite_name}**

> {test_suite_description}

## Test Case #{test_suite_number.test_case_number}
**{test_case_name}**

> {test_case_description}

{pre_conditions_of_test_case if available}

### Test Assertion:

#### Test Assertion #{test_suite_number.test_case_number.test_assertion_number}
{test_assertion_description}

### Test Step Pre-condition:

{pre_conditions_of_test_step if available}

### Test Step:

{test_steps}
```

:::tip
There is no single correct decomposition. Split or combine suites to make authoring the different testing situations simpler and more manageable — for example, separating a Standard Developer BIE Management suite from an End User one when their business rules differ, then merging them later if that makes the test scripts more efficient.
:::

## See also

- [github.com/OAGi/Score/issues](https://github.com/OAGi/Score/issues) — the live issue tracker.
- [Development Setup](./development-setup.md) — getting a local environment ready to work an issue.
- [Architecture Overview](./architecture.md) — to understand which module an issue belongs to.
