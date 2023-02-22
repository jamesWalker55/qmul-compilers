/**
 * Define a grammar for Cool
 */
parser grammar CoolParser;

options { tokenVocab = CoolLexer; }

/*  Starting point for parsing a Cool file  */

anyObjectIdentifier: OBJECT_IDENTIFIER | SELF_IDENTIFIER;
anyTypeIdentifier: TYPE_IDENTIFIER | SELF_TYPE_IDENTIFIER;

program
  : (coolClass SEMICOLON)+ EOF
  ;

coolClass
  // COOL CLASS HAS OPTIONAL INSTANCE VARIABLES, THEN FUNCTIONS
  // write a list of the IDs here e.g. CLASS TYPE_ID CURLY_OPEN CURLY_CLOSE
  // THE ORDER OF THESE ID'S MATTER
  : CLASS anyTypeIdentifier (INHERITS anyTypeIdentifier)?
    BRACE_OPEN
      (feature SEMICOLON)*
    BRACE_CLOSE
  ;

feature
  : anyObjectIdentifier
    PAREN_OPEN (formal (COMMA formal)*)? PAREN_CLOSE
    COLON anyTypeIdentifier BRACE_OPEN expr BRACE_CLOSE
  | anyObjectIdentifier
    COLON anyTypeIdentifier (ASSIGN expr)?
  ;

formal: anyObjectIdentifier COLON anyTypeIdentifier;

expr
  : anyObjectIdentifier ASSIGN expr
  | expr (AT anyTypeIdentifier)? DOT anyObjectIdentifier
    PAREN_OPEN
      (expr (COMMA expr)*)?
    PAREN_CLOSE
  | anyObjectIdentifier
    PAREN_OPEN
      (expr (COMMA expr)*)?
    PAREN_CLOSE
  | IF expr THEN expr ELSE expr FI
  | WHILE expr LOOP expr POOL
  | BRACE_OPEN (expr SEMICOLON)+ BRACE_CLOSE
  | LET
    anyObjectIdentifier COLON anyTypeIdentifier (ASSIGN expr)?
    (COMMA anyObjectIdentifier COLON anyTypeIdentifier (ASSIGN expr)?)*
    IN expr
  | CASE expr OF (anyObjectIdentifier COLON anyTypeIdentifier ARROW expr SEMICOLON)+ ESAC
  | NEW anyTypeIdentifier
  | ISVOID expr
  | expr ADD expr
  | expr SUB expr
  | expr MUL expr
  | expr DIV expr
  | TILDE expr
  | expr LT expr
  | expr LE expr
  | expr EQUAL expr
  | NOT expr
  | PAREN_OPEN expr PAREN_CLOSE
  | anyObjectIdentifier
  | INT_LITERAL
  | STRING_LITERAL
  | BOOL_LITERAL
  ;
