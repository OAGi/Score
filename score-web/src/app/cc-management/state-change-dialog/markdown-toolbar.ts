/**
 * Selection-editing logic behind the GitHub-style Markdown formatting toolbar of
 * {@link StateChangeDialogComponent}'s Write tab. Every function is pure: it takes the
 * textarea's text plus selection and returns the new text plus the selection to restore,
 * leaving the DOM/Angular wiring to the component. The semantics mirror github.com's
 * comment box: wrap-style actions toggle, line-prefix actions apply to every line of the
 * selection (or the caret's line) and toggle off when all lines already carry the prefix.
 */

/** A textarea's value and selection, before or after an edit. */
export interface TextSelection {
  text: string;
  selectionStart: number;
  selectionEnd: number;
}

/** The toolbar's actions, in GitHub's button order. */
export type MarkdownAction =
  'heading' | 'bold' | 'italic' | 'quote' | 'code' | 'link'
  | 'unordered-list' | 'ordered-list' | 'task-list';

/** Lines matching this already belong to an ordered list ("1. ", "12. ", ...). */
const ORDERED_LINE = /^\d+\. /;

/** Applies one toolbar action; the single entry point the component calls. */
export function applyMarkdownAction(action: MarkdownAction, state: TextSelection): TextSelection {
  switch (action) {
    case 'heading':
      return toggleLinePrefix(state, '### ');
    case 'bold':
      return toggleWrap(state, '**');
    case 'italic':
      return toggleWrap(state, '_');
    case 'quote':
      return toggleLinePrefix(state, '> ');
    case 'code':
      return toggleCode(state);
    case 'link':
      return insertLink(state);
    case 'unordered-list':
      return toggleLinePrefix(state, '- ');
    case 'ordered-list':
      return toggleOrderedList(state);
    case 'task-list':
      return toggleLinePrefix(state, '- [ ] ');
  }
}

/**
 * Toggles a symmetric inline marker (e.g. '**' for bold, '_' for italic, '`' for inline code)
 * around the selection:
 * - selection already wrapped (markers inside it, or immediately around it — the latter also
 *   covers a collapsed caret sitting between freshly inserted markers): markers removed;
 * - otherwise the selection is wrapped and the inner text selected. An empty selection thus
 *   becomes a bare marker pair with the caret in the middle.
 */
export function toggleWrap(state: TextSelection, marker: string): TextSelection {
  const {text, selectionStart: start, selectionEnd: end} = state;
  const selected = text.slice(start, end);

  // Markers inside the selection, e.g. the user selected '**bold**'.
  if (selected.length >= marker.length * 2 && selected.startsWith(marker) && selected.endsWith(marker)) {
    const inner = selected.slice(marker.length, selected.length - marker.length);
    return {
      text: text.slice(0, start) + inner + text.slice(end),
      selectionStart: start,
      selectionEnd: start + inner.length,
    };
  }

  // Markers immediately around the selection, e.g. the user selected the 'bold' of '**bold**'.
  if (start >= marker.length
    && text.slice(start - marker.length, start) === marker
    && text.slice(end, end + marker.length) === marker) {
    return {
      text: text.slice(0, start - marker.length) + selected + text.slice(end + marker.length),
      selectionStart: start - marker.length,
      selectionEnd: start - marker.length + selected.length,
    };
  }

  return {
    text: text.slice(0, start) + marker + selected + marker + text.slice(end),
    selectionStart: start + marker.length,
    selectionEnd: end + marker.length,
  };
}

/**
 * The Code action: inline '`' toggle for a selection without newlines, a fenced '```' block
 * for a multiline selection (toggling the fences off when the selection — or its immediate
 * surroundings — already carries them). The opening/closing fences are kept on their own
 * lines by inserting newlines at unaligned edges, like GitHub does.
 */
export function toggleCode(state: TextSelection): TextSelection {
  const {text, selectionStart: start, selectionEnd: end} = state;
  const selected = text.slice(start, end);
  if (!selected.includes('\n')) {
    return toggleWrap(state, '`');
  }

  // The selection includes the fences themselves.
  if (selected.startsWith('```\n') && selected.endsWith('\n```')) {
    const inner = selected.slice(4, selected.length - 4);
    return {
      text: text.slice(0, start) + inner + text.slice(end),
      selectionStart: start,
      selectionEnd: start + inner.length,
    };
  }

  // The fences sit immediately around the selection.
  if (start >= 4 && text.slice(start - 4, start) === '```\n' && text.slice(end, end + 4) === '\n```') {
    return {
      text: text.slice(0, start - 4) + selected + text.slice(end + 4),
      selectionStart: start - 4,
      selectionEnd: start - 4 + selected.length,
    };
  }

  const fenceOpen = (start > 0 && text[start - 1] !== '\n' ? '\n' : '') + '```\n';
  const fenceClose = '\n```' + (end < text.length && text[end] !== '\n' ? '\n' : '');
  return {
    text: text.slice(0, start) + fenceOpen + selected + fenceClose + text.slice(end),
    selectionStart: start + fenceOpen.length,
    selectionEnd: start + fenceOpen.length + selected.length,
  };
}

/**
 * The Link action: wraps the selection as '[selection](url)' and selects the 'url'
 * placeholder so the user types the address right away (GitHub's behavior). An empty
 * selection yields '[](url)' — the user fills the text after the url.
 */
export function insertLink(state: TextSelection): TextSelection {
  const {text, selectionStart: start, selectionEnd: end} = state;
  const selected = text.slice(start, end);
  const placeholder = 'url';
  const urlStart = start + '['.length + selected.length + ']('.length;
  return {
    text: text.slice(0, start) + '[' + selected + '](' + placeholder + ')' + text.slice(end),
    selectionStart: urlStart,
    selectionEnd: urlStart + placeholder.length,
  };
}

/**
 * Toggles a line prefix ('### ', '> ', '- ', '- [ ] ') on every line the selection touches
 * (the caret's line when collapsed): when ALL those lines already start with the prefix it is
 * removed, otherwise it is added to each. The whole transformed block ends up selected.
 */
export function toggleLinePrefix(state: TextSelection, prefix: string): TextSelection {
  return transformLines(state, (lines) =>
    lines.every(line => line.startsWith(prefix))
      ? lines.map(line => line.slice(prefix.length))
      : lines.map(line => prefix + line));
}

/**
 * The Ordered-list action: numbers every line the selection touches '1. ', '2. ', ... —
 * or strips the numbering when ALL those lines already have one.
 */
export function toggleOrderedList(state: TextSelection): TextSelection {
  return transformLines(state, (lines) =>
    lines.every(line => ORDERED_LINE.test(line))
      ? lines.map(line => line.replace(ORDERED_LINE, ''))
      : lines.map((line, i) => (i + 1) + '. ' + line));
}

/** Expands the selection to whole lines, maps them, and selects the transformed block. */
function transformLines(state: TextSelection, transform: (lines: string[]) => string[]): TextSelection {
  const {text, selectionStart: start, selectionEnd: end} = state;
  const blockStart = start === 0 ? 0 : text.lastIndexOf('\n', start - 1) + 1;
  // A selection ending right after a newline does not pull in the following line.
  const searchFrom = (end > start && text[end - 1] === '\n') ? end - 1 : end;
  let blockEnd = text.indexOf('\n', searchFrom);
  if (blockEnd === -1) {
    blockEnd = text.length;
  }
  const newBlock = transform(text.slice(blockStart, blockEnd).split('\n')).join('\n');
  return {
    text: text.slice(0, blockStart) + newBlock + text.slice(blockEnd),
    selectionStart: blockStart,
    selectionEnd: blockStart + newBlock.length,
  };
}
