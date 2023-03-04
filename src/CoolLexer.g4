lexer grammar CoolLexer;

@lexer::members {
  StringBuilder currentString;
  private char lastChar(int offset) {
    String text = getText();
    if (text.length() > 0) return text.charAt(text.length() - 1 + offset);
    else return '$';
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
COMMENT_END: '*)' { setText("Unmatched *)"); } -> type(ERROR);

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
  : { currentString = new StringBuilder(); }
    '"'
      (
        UnescapedStringChar { currentString.append(lastChar(0)); }
      | EscapeSequence
        {
          if (lastChar(0) == 'n') currentString.append('\n');
          else if (lastChar(0) == 'b') currentString.append('\b');
          else if (lastChar(0) == 't') currentString.append('\t');
          else if (lastChar(0) == 'f') currentString.append('\f');
          else currentString.append(lastChar(0));
        }
      )*
    '"'
    {
      if (currentString.length() > 1024) {
        setText("String constant too long"); setType(ERROR);
      } else {
        setText(currentString.toString());
      }
    }
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
COMMENT: '(*' (COMMENT | .)*? ('*)' { skip(); } | EOF { setText("EOF in comment"); setType(ERROR); });
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
