import {ParseError, scanBrackets} from './search-grammar';

describe('scanBrackets', () => {
  it('finds the slashes and the outermost brackets, and nothing inside a bracket', () => {
    const scan = scanBrackets('a/b[c/d[e]]/f[g]');
    expect(scan.slashes).toEqual([1, 11]);
    expect(scan.brackets).toEqual([{open: 3, close: 10}, {open: 13, close: 15}]);
  });

  it('hides a bracket or a slash that is quoted inside a bracket', () => {
    const scan = scanBrackets('a[b = "]/[" or c = \']\']/d');
    expect(scan.brackets).toEqual([{open: 1, close: 22}]);
    expect(scan.slashes).toEqual([23]);
  });

  it('does not read quotes outside brackets as quotes', () => {
    expect(scanBrackets('a\'/b').slashes).toEqual([2]);
  });

  it('reports what is wrong and where, counting from the offset', () => {
    const error = (text: string) => {
      try {
        scanBrackets(text, 10);
      } catch (e) {
        return e instanceof ParseError ? [e.message, e.position] : e;
      }
      return undefined;
    };
    expect(error('a]')).toEqual(['Unexpected ]', 11]);
    expect(error('a[b')).toEqual(['Missing ]', 13]);
    expect(error('a[b = "x]')).toEqual(['Unterminated string', 19]);
  });
});
