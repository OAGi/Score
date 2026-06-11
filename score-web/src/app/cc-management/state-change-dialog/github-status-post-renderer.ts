import {
  ComponentChangeSummary,
  ComponentChildChange,
  ComponentChildSummary,
  ComponentFieldChange,
  ComponentSummaryField
} from '../../log-management/domain/log';

/**
 * Renders the GitHub status comments for issue #1533 — the posts suggested when a component
 * reaches Candidate and when it is reverted from Candidate back to WIP. A faithful TypeScript
 * port of the (deleted) backend GitHubStatusPostRenderer: the frontend now pre-fills the
 * state-change dialog's comment box with the rendered post, the user edits it freely, and the
 * backend posts the final text to the linked issues verbatim.
 *
 * A NEW component (revision 1) is announced as an introduction — "New …" with its initial
 * definition — while a REVISED one posts the change list since the prior revision; the revert
 * notification mirrors both.
 *
 * Readability/size guards for the issue thread: child elements and change buckets fold into
 * `<details>` blocks with visible counts, each group is capped at {@link MAX_ITEMS_PER_GROUP}
 * entries (a comment must stay under GitHub's 65,536-character limit), long values such as
 * definitions are cut at {@link MAX_VALUE_LENGTH} characters, and embedded newlines are
 * collapsed so a value cannot break out of its markdown list item.
 */

const MAX_ITEMS_PER_GROUP = 50;

const MAX_VALUE_LENGTH = 100;

/** The comment suggested when a linked component moves to Candidate. */
export function candidatePost(summary: ComponentChangeSummary): string {
  let out = '';
  if (summary.summaryType === 'NEW') {
    out += '### New ' + typeName(summary) + ' "' + summary.name + '"\n';
    out += '\nA new component addressing this issue has moved to **Candidate**.\n';
    out += '\n';
    out += renderFields(summary.fields || []);
    out += renderCollapsedChildren(summary.children || []);
  } else {
    out += '### ' + typeName(summary) + ' "' + summary.name
      + '" (rev. ' + summary.revisionNum + ') moved to Candidate\n';
    out += '\n' + changesIntro(summary) + '\n';
    out += renderChangeBody(summary);
  }
  return out;
}

/**
 * The comment suggested when a linked component leaves Candidate backwards. `backTo` is the
 * state it moved back to — 'WIP' for the full revert, 'Draft' for the one-step demotion.
 */
export function revertPost(summary: ComponentChangeSummary, backTo: string = 'WIP'): string {
  let out = '';
  if (summary.summaryType === 'NEW') {
    out += '### New ' + typeName(summary) + ' "' + summary.name
      + '" was reverted to ' + backTo + '\n';
    out += '\nThe newly introduced component linked to this issue moved back from **Candidate** to '
      + '**' + backTo + '**; its definition may change before it becomes a Candidate again.\n';
  } else {
    out += '### ' + typeName(summary) + ' "' + summary.name
      + '" (rev. ' + summary.revisionNum + ') was reverted to ' + backTo + '\n';
    out += '\nThe following changes, previously a **Candidate** for this issue, moved back to '
      + '**' + backTo + '** and may be reworked:\n';
    out += renderChangeBody(summary);
  }
  return out;
}

// ----- REVISED body -----

function changesIntro(summary: ComponentChangeSummary): string {
  const prev = (summary.prevRevisionNum !== null && summary.prevRevisionNum !== undefined)
    ? summary.prevRevisionNum : summary.revisionNum - 1;
  return (prev === summary.revisionNum)
    ? 'Changes within Rev. ' + prev + ':'
    : 'Changes from Rev. ' + prev + ':';
}

/** The human-readable component type: "Code List" / "Agency ID List" instead of the enum names. */
function typeName(summary: ComponentChangeSummary): string {
  switch (summary.ccType) {
    case 'CODE_LIST':
      return 'Code List';
    case 'AGENCY_ID_LIST':
      return 'Agency ID List';
    default:
      return summary.ccType;
  }
}

/** Mirrors the backend ComponentChangeSummary.isEmpty(): no content change detected at all. */
function isEmpty(summary: ComponentChangeSummary): boolean {
  return (summary.fields || []).length === 0 && (summary.children || []).length === 0
    && (summary.fieldChanges || []).length === 0
    && (summary.childrenAdded || []).length === 0
    && (summary.childrenRemoved || []).length === 0
    && (summary.childrenChanged || []).length === 0;
}

function renderChangeBody(summary: ComponentChangeSummary): string {
  if (isEmpty(summary)) {
    return '\n_No content changes._\n';
  }
  let out = '';
  const fieldChanges = summary.fieldChanges || [];
  if (fieldChanges.length > 0) {
    out += '\n';
    for (const change of fieldChanges) {
      out += '- ' + changeText(change) + '\n';
    }
  }
  out += renderBucket('Added', summary.childrenAdded || [], true);
  out += renderBucket('Removed', summary.childrenRemoved || [], false);
  out += renderChangedBucket(summary.childrenChanged || []);
  return out;
}

/** One child bucket as a `<details>` block; `withFields` lists each entry's fields. */
function renderBucket(label: string, children: ComponentChildSummary[], withFields: boolean): string {
  if (children.length === 0) {
    return '';
  }
  let out = '\n<details><summary>' + label + ' (' + children.length + ')</summary>\n\n';
  let count = 0;
  for (const child of children) {
    if (++count > MAX_ITEMS_PER_GROUP) {
      out += '- … and ' + (children.length - MAX_ITEMS_PER_GROUP) + ' more\n';
      break;
    }
    out += '- ' + child.kind + ' "' + child.name + '"\n';
    if (withFields) {
      for (const field of (child.fields || [])) {
        out += '  - ' + field.label + ': ' + clip(field.value) + '\n';
      }
    }
  }
  out += '\n</details>\n';
  return out;
}

function renderChangedBucket(children: ComponentChildChange[]): string {
  if (children.length === 0) {
    return '';
  }
  let out = '\n<details><summary>Changed (' + children.length + ')</summary>\n\n';
  let count = 0;
  for (const child of children) {
    if (++count > MAX_ITEMS_PER_GROUP) {
      out += '- … and ' + (children.length - MAX_ITEMS_PER_GROUP) + ' more\n';
      break;
    }
    out += '- ' + child.kind + ' "' + child.name + '"\n';
    for (const change of (child.changes || [])) {
      out += '  - ' + changeText(change) + '\n';
    }
  }
  out += '\n</details>\n';
  return out;
}

// ----- NEW body -----

function renderFields(fields: ComponentSummaryField[]): string {
  let out = '';
  for (const field of fields) {
    out += '- ' + field.label + ': ' + clip(field.value) + '\n';
  }
  return out;
}

/**
 * Folds the child elements into one `<details>` block per group ("Associations",
 * "Supplementary Components", "Values", …) with the count in the visible summary line.
 */
function renderCollapsedChildren(children: ComponentChildSummary[]): string {
  if (children.length === 0) {
    return '';
  }
  const groups = new Map<string, ComponentChildSummary[]>();
  for (const child of children) {
    const label = groupLabel(child.kind);
    const group = groups.get(label);
    if (group) {
      group.push(child);
    } else {
      groups.set(label, [child]);
    }
  }
  let out = '';
  groups.forEach((group, label) => {
    out += renderBucket(label, group, true);
  });
  return out;
}

function groupLabel(kind: string): string {
  switch (kind) {
    case 'ASCC':
    case 'BCC':
      return 'Associations';
    case 'Supplementary Component':
      return 'Supplementary Components';
    case 'Value Domain':
      return 'Value Domains';
    case 'Value':
      return 'Values';
    default:
      return kind + 's';
  }
}

// ----- Value rendering -----

function changeText(change: ComponentFieldChange): string {
  return change.label + ': ' + valueOrNone(change.before) + ' → ' + valueOrNone(change.after);
}

function valueOrNone(value: string | null | undefined): string {
  return (value === null || value === undefined) ? '(none)' : '"' + clip(value) + '"';
}

/**
 * Collapses embedded newlines (so a value cannot break out of its markdown list item) and cuts
 * the value at {@link MAX_VALUE_LENGTH} characters — long definitions otherwise drown the comment.
 */
function clip(value: string | null | undefined): string | null {
  if (value === null || value === undefined) {
    return null;
  }
  const inline = value.replace(/\s+/g, ' ').trim();
  return (inline.length <= MAX_VALUE_LENGTH)
    ? inline
    : inline.substring(0, MAX_VALUE_LENGTH - 3).trimEnd() + '...';
}
