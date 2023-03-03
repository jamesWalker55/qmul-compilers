lexer grammar CoolLexer;

@lexer::members {
  int stringCharCount;
  int commentDepth;
  private void checkCommentEOFError() {
    if (_input.LA(1) == EOF) {
      setText("EOF in comment");
      setType(ERROR);
    }
  }
}

// Letter fragments

fragment A: [aA];
fragment B: [bB];
fragment C: [cC];
fragment D: [dD];
fragment E: [eE];
fragment F: [fF];
fragment G: [gG];
fragment H: [hH];
fragment I: [iI];
fragment J: [jJ];
fragment K: [kK];
fragment L: [lL];
fragment M: [mM];
fragment N: [nN];
fragment O: [oO];
fragment P: [pP];
fragment Q: [qQ];
fragment R: [rR];
fragment S: [sS];
fragment T: [tT];
fragment U: [uU];
fragment V: [vV];
fragment W: [wW];
fragment X: [xX];
fragment Y: [yY];
fragment Z: [zZ];
fragment UppercaseLetter: [A-Z];
fragment LowercaseLetter: [a-z];
fragment Digit: [0-9];

// Keywords (keywords are case insensitive)

CLASS:      C L A S S;
INHERITS:   I N H E R I T S;

IF:         I F;
THEN:       T H E N;
ELSE:       E L S E;
FI:         F I;

WHILE:      W H I L E;
LOOP:       L O O P;
POOL:       P O O L;

LET:        L E T;
IN:         I N;

CASE:       C A S E;
OF:         O F;
ESAC:       E S A C;

NEW:        N E W;
ISVOID:     I S V O I D;
NOT:        N O T;

// Unmatched pairs
UNMATCHED_COMMENT_END: '*)' { setText("Unmatched *)"); } -> type(ERROR);

// Separators

COMMA:          ',';
SEMICOLON:      ';';
COLON:          ':';

BRACE_OPEN:     '{';
BRACE_CLOSE:    '}';
PAREN_OPEN:     '(';
PAREN_CLOSE:    ')';

// Operators
// Ordered by precedence

DOT:            '.';
AT:             '@';
TILDE:          '~';
// 'ISVOID'
MUL:            '*';
DIV:            '/';
ADD:            '+';
SUB:            '-';
LE:             '<=';
LT:             '<';
EQUAL:          '=';
// 'NOT'
ASSIGN:         '<-';

// Not considered as an operator. This is used only in CASE statements:
ARROW:          '=>';

// Literals

BOOL_LITERAL
  : 't' R U E
  | 'f' A L S E;

// Greedy operator '+' matches as much input as possible
INT_LITERAL: Digit+;

// The '/' character can escape most characters, including newlines '\n'. For example:
//
//     foo <- "this is \
//     a valid string"
//
// However a string cannot contain a null byte '\u0000'
fragment EscapeSequence: '\\' ~'\u0000';
// Normal, unescaped characters that can appear in a string
fragment UnescapedStringChar: ~[\u0000\\\n"];

// `stringCharCount` is defined at the top of the file
STRING_LITERAL
  : { stringCharCount = 0; } '"' ((UnescapedStringChar | EscapeSequence) { stringCharCount += 1; })* '"'
    { if (stringCharCount > 1024) {setText("String constant too long"); setType(ERROR);} }
  ;

INVALID_STRING_LITERAL:
  ( '"' (UnescapedStringChar | EscapeSequence)* '\n'
    { setText("Unterminated string constant"); }
  | '"' (UnescapedStringChar | EscapeSequence)* EOF
    { setText("EOF in string constant"); }
  | '"' (UnescapedStringChar | EscapeSequence | '\\\u0000')* '"'
    { setText("String contains escaped null character."); }
  | '"' (UnescapedStringChar | EscapeSequence | '\u0000')* '"'
    { setText("String contains null character."); }
  ) -> type(ERROR);

// Identifiers

TYPE_IDENTIFIER:   UppercaseLetter (UppercaseLetter | LowercaseLetter | Digit | '_')*;
OBJECT_IDENTIFIER: LowercaseLetter (UppercaseLetter | LowercaseLetter | Digit | '_')*;

// Comments

// "Comments cannot cross file boundaries", so comments MUST end with '*)'
COMMENT_START: '(*' { commentDepth = 1; checkCommentEOFError(); } -> pushMode(COMMENT_MODE);

mode COMMENT_MODE;

  // UNTERMINATED_COMMENT: . EOF { setText("EOF in comment"); setType(ERROR); System.out.println("HOLY SHIT EOF IN COMMENT"); };
  COMMENT_TEXT: . { checkCommentEOFError(); };
  COMMENT_INNERSTART: '(*' { commentDepth += 1; checkCommentEOFError(); };
  COMMENT_END: '*)' { commentDepth -= 1; if (commentDepth == 0) popMode(); else checkCommentEOFError(); };

mode DEFAULT_MODE;

LINE_COMMENT: '--' ~[\n]*? ('\n' | EOF) -> skip;

// Whitespaces from COOL manual:
// - blank (ascii 32)
// - \n (newline, ascii 10)
// - \f (form feed, ascii 12)
// - \r (carriage return, ascii 13)
// - \t (tab, ascii 9)
// - \v (vertical tab, ascii 11)
WS: [ \n\f\r\t\u000b]+ -> skip; //skip whitespace

// this should be at the end if the input doesnt match any token
ERROR: .;
