/** What formatting a JSON text gives: the text, indented, or why it is not JSON. */
export interface JsonFormatResult {
  text?: string;
  error?: string;
}

/**
 * The text as JSON with each level indented, or the reason it cannot be: the editor offers it as a button, so that a
 * value pasted in one line is easy to read and edit. A blank text stays blank: it is not an error for the editor to
 * tell whether a value is required.
 */
export function formatJson(text: string, indent = 2): JsonFormatResult {
  if (!text || !text.trim()) {
    return {text: ''};
  }
  try {
    return {text: JSON.stringify(JSON.parse(text), null, indent)};
  } catch (e) {
    return {error: (e as Error).message};
  }
}

/** The text as JSON on one line, or the reason it cannot be. */
export function minifyJson(text: string): JsonFormatResult {
  return formatJson(text, 0);
}
