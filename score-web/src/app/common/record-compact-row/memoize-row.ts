/**
 * Makes `fn` answer once for a row. The compact row of a table is made from the row in the template (`compactRecord(element)`), which is
 * evaluated again and again: with a new object every time, the row would be drawn again every time.
 */
export function memoizeRow<T extends object, R>(fn: (row: T) => R): (row: T) => R {
  const cache = new WeakMap<T, R>();
  return row => {
    let made = cache.get(row);
    if (made === undefined) {
      made = fn(row);
      cache.set(row, made);
    }
    return made;
  };
}
