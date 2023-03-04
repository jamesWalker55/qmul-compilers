/**
 * Define a grammar for Cool
 */
parser grammar CoolParser;

options { tokenVocab = CoolLexer; }

/*  Starting point for parsing a Cool file  */

program
  : (coolClass SEMICOLON)+ EOF
  ;

coolClass
  // COOL CLASS HAS OPTIONAL INSTANCE VARIABLES, THEN FUNCTIONS
  // write a list of the IDs here e.g. CLASS TYPE_ID CURLY_OPEN CURLY_CLOSE
  // THE ORDER OF THESE ID'S MATTER
  : CLASS TYPE_IDENTIFIER (INHERITS TYPE_IDENTIFIER)?
    BRACE_OPEN
      (feature SEMICOLON)*
    BRACE_CLOSE
  ;

feature
  : OBJECT_IDENTIFIER
    PAREN_OPEN (formal (COMMA formal)*)? PAREN_CLOSE
    COLON TYPE_IDENTIFIER BRACE_OPEN expr BRACE_CLOSE
  | OBJECT_IDENTIFIER
    COLON TYPE_IDENTIFIER (ASSIGN expr)?
  ;

formal: OBJECT_IDENTIFIER COLON TYPE_IDENTIFIER;

expr
  : expr (AT TYPE_IDENTIFIER)? DOT OBJECT_IDENTIFIER
    PAREN_OPEN
      (expr (COMMA expr)*)?
    PAREN_CLOSE
  | OBJECT_IDENTIFIER
    PAREN_OPEN
      (expr (COMMA expr)*)?
    PAREN_CLOSE
  | TILDE expr
  | ISVOID expr
  | expr (MUL | DIV) expr
  | expr (ADD | SUB) expr
  | expr (LT | LE | EQUAL) expr
  | NOT expr
  | <assoc=right> OBJECT_IDENTIFIER ASSIGN expr
  | IF expr THEN expr ELSE expr FI
  | WHILE expr LOOP expr POOL
  | BRACE_OPEN (expr SEMICOLON)+ BRACE_CLOSE
  | LET
    OBJECT_IDENTIFIER COLON TYPE_IDENTIFIER (ASSIGN expr)?
    (COMMA OBJECT_IDENTIFIER COLON TYPE_IDENTIFIER (ASSIGN expr)?)*
    IN expr
  | CASE expr OF (OBJECT_IDENTIFIER COLON TYPE_IDENTIFIER ARROW expr SEMICOLON)+ ESAC
  | NEW TYPE_IDENTIFIER
  | PAREN_OPEN expr PAREN_CLOSE
  | OBJECT_IDENTIFIER
  | INT_LITERAL
  | STRING_LITERAL
  | BOOL_LITERAL
  ;
