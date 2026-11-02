import {CstNode, CstParser, createToken, EOF, IRecognitionException, IToken, Lexer, ParserMethod, TokenType} from 'chevrotain';

/** A malformed search expression: what is wrong and where (an offset into the expression). */
export class ParseError extends Error {
  constructor(message: string, readonly position: number) {
    super(message);
  }
}

// ---- tokens ----------------------------------------------------------------------------------------------------------

const WhiteSpace = createToken({name: 'WhiteSpace', pattern: /\s+/, group: Lexer.SKIPPED});
export const StringLit = createToken({name: 'StringLit', pattern: /"[^"]*"|'[^']*'/});
/** A word; one that starts with a digit (a GUID such as `6f3ab1...`) is a word too when it has a letter in it. `∞` is a word. */
export const Ident = createToken({name: 'Ident', pattern: /[A-Za-z_][A-Za-z0-9_-]*|[0-9][A-Za-z0-9_-]*[A-Za-z_][A-Za-z0-9_-]*|∞/});
export const NumberLit = createToken({name: 'NumberLit', pattern: /-?[0-9]+(\.[0-9]+)?/});
export const Op = createToken({name: 'Op', pattern: /!=|<=|>=|=|<|>/});
export const DoubleColon = createToken({name: 'DoubleColon', pattern: /::/});
export const SlashSlash = createToken({name: 'SlashSlash', pattern: /\/\//});
export const DotDot = createToken({name: 'DotDot', pattern: /\.\./});
export const Slash = createToken({name: 'Slash', pattern: /\//});
export const Dot = createToken({name: 'Dot', pattern: /\./});
export const Star = createToken({name: 'Star', pattern: /\*/});
export const LParen = createToken({name: 'LParen', pattern: /\(/});
export const RParen = createToken({name: 'RParen', pattern: /\)/});
export const LBracket = createToken({name: 'LBracket', pattern: /\[/});
export const RBracket = createToken({name: 'RBracket', pattern: /]/});
export const Comma = createToken({name: 'Comma', pattern: /,/});

/** In the order they are tried: the first that matches wins. */
const ALL_TOKENS: TokenType[] = [WhiteSpace, StringLit, Ident, NumberLit, Op, DoubleColon, SlashSlash, DotDot, Slash, Dot, Star, LParen,
  RParen, LBracket, RBracket, Comma];

// ---- grammar ---------------------------------------------------------------------------------------------------------

function isWord(token: IToken, word: string): boolean {
  return token.tokenType === Ident && token.image.toLowerCase() === word;
}

/** Whether the tokens begin a path inside a predicate (`child::x`, `../x`, `.//x`, `*`, `name/x`). */
function startsPath(first: IToken, second: IToken): boolean {
  switch (first.tokenType) {
    case Dot:
    case DotDot:
    case Star:
    case Slash:
    case SlashSlash:
      return true;
    case Ident:
      return second.tokenType === DoubleColon || second.tokenType === Slash || second.tokenType === SlashSlash;
    default:
      return false;
  }
}

/** A word that a comparison is made against as the text it spells: `type = ACC`. */
function isBareWord(first: IToken, second: IToken): boolean {
  return first.tokenType === Ident && !startsPath(first, second) && second.tokenType !== LParen
    && !['true', 'false'].includes(first.image.toLowerCase());
}

/**
 * The grammar of a predicate (what is written between `[` and `]`), as a concrete syntax tree. What the parts mean is
 * decided where the tree is compiled (`search-expression.ts`).
 *
 *   orExpr          = andExpr , { "or" , andExpr } ;
 *   andExpr         = notExpr , { "and" , notExpr } ;
 *   notExpr         = "not" , notExpr | comparison ;                 (* "not" followed by "(" is a function call *)
 *   comparison      = "cardinality" , op , cardinalityValue
 *                   | operand , [ op , comparisonValue ] ;
 *   cardinalityValue = ( ident | string | number ) , [ ".." , [ number | "*" | ident ] ] ;
 *   comparisonValue = bareWord | operand ;
 *   operand         = pathOperand | functionCall | string | number | "(" , orExpr , ")" | ident ;
 *   functionCall    = ident , "(" , [ orExpr , { "," , orExpr } ] , ")" ;
 *   pathOperand     = [ "/" | "//" ] , pathStep , { ( "/" | "//" ) , pathStep } ;
 *   pathStep        = ( ident , "::" , ( ident | string | "*" ) | ident | string | "*" | "." | ".." ) , { predicate } ;
 *   predicate       = "[" , orExpr , "]" ;
 */
class SearchParser extends CstParser {

  readonly orExpr: ParserMethod<[], CstNode>;
  readonly andExpr: ParserMethod<[], CstNode>;
  readonly notExpr: ParserMethod<[], CstNode>;
  readonly comparison: ParserMethod<[], CstNode>;
  readonly cardinalityValue: ParserMethod<[], CstNode>;
  readonly comparisonValue: ParserMethod<[], CstNode>;
  readonly operand: ParserMethod<[], CstNode>;
  readonly functionCall: ParserMethod<[], CstNode>;
  readonly pathOperand: ParserMethod<[], CstNode>;
  readonly pathStep: ParserMethod<[], CstNode>;
  readonly predicate: ParserMethod<[], CstNode>;

  constructor() {
    super(ALL_TOKENS);
    const $ = this;

    this.orExpr = $.RULE('orExpr', () => {
      $.SUBRULE($.andExpr);
      $.MANY({
        GATE: () => isWord($.LA(1), 'or'),
        DEF: () => {
          $.CONSUME(Ident, {LABEL: 'op'});
          $.SUBRULE2($.andExpr);
        }
      });
    });

    this.andExpr = $.RULE('andExpr', () => {
      $.SUBRULE($.notExpr);
      $.MANY({
        GATE: () => isWord($.LA(1), 'and'),
        DEF: () => {
          $.CONSUME(Ident, {LABEL: 'op'});
          $.SUBRULE2($.notExpr);
        }
      });
    });

    this.notExpr = $.RULE('notExpr', () => {
      $.OR([
        {
          GATE: () => isWord($.LA(1), 'not') && $.LA(2).tokenType !== LParen,
          ALT: () => {
            $.CONSUME(Ident, {LABEL: 'not'});
            $.SUBRULE($.notExpr);
          }
        },
        {ALT: () => $.SUBRULE($.comparison)}
      ]);
    });

    this.comparison = $.RULE('comparison', () => {
      $.OR([
        {
          GATE: () => $.LA(1).tokenType === Ident && RANGE_ATTRIBUTES.includes($.LA(1).image.toLowerCase()) && $.LA(2).tokenType === Op,
          ALT: () => {
            $.CONSUME(Ident, {LABEL: 'cardinality'});
            $.CONSUME(Op, {LABEL: 'cardinalityOp'});
            $.SUBRULE($.cardinalityValue);
          }
        },
        {
          ALT: () => {
            $.SUBRULE($.operand, {LABEL: 'left'});
            $.OPTION(() => {
              $.CONSUME2(Op, {LABEL: 'op'});
              $.SUBRULE($.comparisonValue, {LABEL: 'right'});
            });
          }
        }
      ]);
    });

    this.cardinalityValue = $.RULE('cardinalityValue', () => {
      $.OR([
        {ALT: () => $.CONSUME(Ident, {LABEL: 'value'})},
        {ALT: () => $.CONSUME(StringLit, {LABEL: 'value'})},
        {ALT: () => $.CONSUME(NumberLit, {LABEL: 'value'})}
      ]);
      $.OPTION(() => {
        $.CONSUME(DotDot);
        $.OPTION2(() => {
          $.OR2([
            {ALT: () => $.CONSUME2(NumberLit, {LABEL: 'upper'})},
            {ALT: () => $.CONSUME(Star, {LABEL: 'upper'})},
            {ALT: () => $.CONSUME2(Ident, {LABEL: 'upper'})}
          ]);
        });
      });
    });

    this.comparisonValue = $.RULE('comparisonValue', () => {
      $.OR([
        {
          GATE: () => isBareWord($.LA(1), $.LA(2)),
          ALT: () => $.CONSUME(Ident, {LABEL: 'bare'})
        },
        {ALT: () => $.SUBRULE($.operand)}
      ]);
    });

    this.operand = $.RULE('operand', () => {
      $.OR([
        {
          GATE: () => startsPath($.LA(1), $.LA(2)),
          ALT: () => $.SUBRULE($.pathOperand)
        },
        {
          GATE: () => $.LA(1).tokenType === Ident && $.LA(2).tokenType === LParen,
          ALT: () => $.SUBRULE($.functionCall)
        },
        {ALT: () => $.CONSUME(StringLit)},
        {ALT: () => $.CONSUME(NumberLit)},
        {
          ALT: () => {
            $.CONSUME(LParen);
            $.SUBRULE($.orExpr);
            $.CONSUME(RParen);
          }
        },
        {ALT: () => $.CONSUME(Ident)}
      ]);
    });

    this.functionCall = $.RULE('functionCall', () => {
      $.CONSUME(Ident, {LABEL: 'name'});
      $.CONSUME(LParen);
      $.OPTION(() => {
        $.SUBRULE($.orExpr, {LABEL: 'arg'});
        $.MANY(() => {
          $.CONSUME(Comma);
          $.SUBRULE2($.orExpr, {LABEL: 'arg'});
        });
      });
      $.CONSUME(RParen);
    });

    this.pathOperand = $.RULE('pathOperand', () => {
      $.OPTION(() => {
        $.OR([
          {ALT: () => $.CONSUME(Slash, {LABEL: 'lead'})},
          {ALT: () => $.CONSUME(SlashSlash, {LABEL: 'lead'})}
        ]);
      });
      $.SUBRULE($.pathStep);
      $.MANY(() => {
        $.OR2([
          {ALT: () => $.CONSUME2(Slash, {LABEL: 'sep'})},
          {ALT: () => $.CONSUME2(SlashSlash, {LABEL: 'sep'})}
        ]);
        $.SUBRULE2($.pathStep);
      });
    });

    this.pathStep = $.RULE('pathStep', () => {
      $.OR([
        {
          GATE: () => $.LA(1).tokenType === Ident && $.LA(2).tokenType === DoubleColon,
          ALT: () => {
            $.CONSUME(Ident, {LABEL: 'axis'});
            $.CONSUME(DoubleColon);
            $.OR2([
              {ALT: () => $.CONSUME2(Ident, {LABEL: 'name'})},
              {ALT: () => $.CONSUME(StringLit, {LABEL: 'name'})},
              {ALT: () => $.CONSUME(Star, {LABEL: 'name'})}
            ]);
          }
        },
        {
          ALT: () => {
            $.OR3([
              {ALT: () => $.CONSUME3(Ident, {LABEL: 'name'})},
              {ALT: () => $.CONSUME2(StringLit, {LABEL: 'name'})},
              {ALT: () => $.CONSUME2(Star, {LABEL: 'name'})},
              {ALT: () => $.CONSUME(Dot, {LABEL: 'name'})},
              {ALT: () => $.CONSUME(DotDot, {LABEL: 'name'})}
            ]);
          }
        }
      ]);
      $.MANY(() => $.SUBRULE($.predicate));
    });

    this.predicate = $.RULE('predicate', () => {
      $.CONSUME(LBracket);
      $.SUBRULE($.orExpr);
      $.CONSUME(RBracket);
    });

    $.performSelfAnalysis();
  }
}

const lexer = new Lexer(ALL_TOKENS, {positionTracking: 'onlyOffset'});
const parser = new SearchParser();

/**
 * Parses what is written between the brackets of a predicate. `offset` is where the text begins in the whole expression
 * and `end` where the closing bracket is, which is where an expression that stops too soon is reported.
 */
export function parsePredicate(text: string, offset: number, end: number): CstNode {
  const lexed = lexer.tokenize(text);
  if (lexed.errors.length > 0) {
    const at = lexed.errors[0].offset;
    const character = text[at];
    throw new ParseError(character === '"' || character === '\'' ? 'Unterminated string' : 'Unexpected ' + character, offset + at);
  }
  if (lexed.tokens.length === 0) {
    throw new ParseError('Empty predicate', end);
  }
  parser.input = lexed.tokens;
  const cst = parser.orExpr();
  if (parser.errors.length > 0) {
    throw syntaxError(parser.errors[0], offset, end);
  }
  return cst;
}

function syntaxError(error: IRecognitionException, offset: number, end: number): ParseError {
  const token = error.token;
  if (!token || token.tokenType === EOF || isNaN(token.startOffset)) {
    return new ParseError('Unexpected end of predicate', end);
  }
  return new ParseError('Unexpected ' + token.image, offset + token.startOffset);
}

// ---- what may come next ----------------------------------------------------------------------------------------------

/** The attributes whose value is a `min..max` range: they are compared with `=` and `!=` only. */
export const RANGE_ATTRIBUTES = ['cardinality'];

/** What can follow the text of a predicate, as a kind of token; each kind is tried with one example. */
export type Continuation = 'or' | 'and' | 'not' | 'name' | 'call' | 'axis' | 'op' | 'slash' | 'descendant' | 'predicate' | 'parent'
  | 'self' | 'any' | 'closeParen' | 'comma' | 'closeBracket';

const EXAMPLES: Record<Continuation, string> = {
  or: 'or', and: 'and', not: 'not', name: 'x', call: 'x(', axis: 'child::', op: '=', slash: '/', descendant: '//', predicate: '[',
  parent: '..', self: '.', any: '*', closeParen: ')', comma: ',', closeBracket: ']'
};

/** Whether the tokens are the beginning of a predicate: the parser has used them all and is waiting for more. */
function canContinue(tokens: IToken[]): boolean {
  parser.input = tokens;
  parser.orExpr();
  const error = parser.errors[0];
  return !error || error.token.tokenType === EOF || isNaN(error.token.startOffset);
}

export interface PredicateTail {
  /** The last tokens of the text (the kind of each and what it spells). */
  tokens: {type: string, text: string}[];
  /** The kinds of token the parser would accept after the text. */
  next: Set<Continuation>;
  /** Whether the text is a whole condition already. */
  complete: boolean;
}

/**
 * What the grammar allows after the text of a predicate written so far: each kind of token is appended to the text and
 * the parser says whether it could still be the beginning of a predicate. Nothing is returned for text that cannot be read
 * (a lexical error), nor for one that is already wrong.
 */
export function predicateTail(text: string): PredicateTail | undefined {
  const lexed = lexer.tokenize(text);
  if (lexed.errors.length > 0 || !canContinue(lexed.tokens)) {
    return undefined;
  }
  const complete = lexed.tokens.length > 0 && parser.errors.length === 0;
  const next = new Set<Continuation>();
  for (const [kind, example] of Object.entries(EXAMPLES)) {
    const tried = lexer.tokenize(text + ' ' + example);
    if (tried.errors.length === 0 && canContinue(tried.tokens)) {
      next.add(kind as Continuation);
    }
  }
  return {tokens: lexed.tokens.map(t => ({type: t.tokenType.name, text: t.image})), next, complete};
}
