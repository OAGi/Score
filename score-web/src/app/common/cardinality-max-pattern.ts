// The alternatives must be grouped: Validators.pattern wraps a string as ^...$, so without the group
// only the first alternative is start-anchored and only the last is end-anchored.
export const CARDINALITY_MAX_PATTERN = '^([0-9]+|-1|unbounded)$';
