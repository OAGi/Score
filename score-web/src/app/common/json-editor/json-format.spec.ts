import {formatJson, minifyJson} from './json-format';

describe('JSON format', () => {
  it('indents each level with two spaces', () => {
    expect(formatJson('{"type":"string","pattern":"^a$"}').text).toBe('{\n  "type": "string",\n  "pattern": "^a$"\n}');
  });

  it('indents nested objects and arrays', () => {
    expect(formatJson('{"a":[1,{"b":2}]}').text).toBe('{\n  "a": [\n    1,\n    {\n      "b": 2\n    }\n  ]\n}');
  });

  it('takes the indentation as an argument', () => {
    expect(formatJson('{"a":1}', 4).text).toBe('{\n    "a": 1\n}');
  });

  it('keeps a JSON that is not an object: a string, a number, an array', () => {
    expect(formatJson('"abc"').text).toBe('"abc"');
    expect(formatJson(' 12 ').text).toBe('12');
    expect(formatJson('[1,2]').text).toBe('[\n  1,\n  2\n]');
  });

  it('leaves a blank text blank, which is not an error', () => {
    expect(formatJson('')).toEqual({text: ''});
    expect(formatJson('  \n ')).toEqual({text: ''});
  });

  it('says why a text is not JSON, and gives no text', () => {
    const result = formatJson('{"a":');
    expect(result.text).toBeUndefined();
    expect(result.error).toBeTruthy();
    expect(formatJson('{a:1}').error).toBeTruthy();
    expect(formatJson("{'a':1}").error).toBeTruthy();
  });

  it('puts a text on one line', () => {
    expect(minifyJson('{\n  "a": [\n    1,\n    2\n  ]\n}').text).toBe('{"a":[1,2]}');
    expect(minifyJson('{"a":').error).toBeTruthy();
  });
});
