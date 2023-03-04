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
    PAREN_CLOSE #DottedDispatch
  | OBJECT_IDENTIFIER
    PAREN_OPEN
      (expr (COMMA expr)*)?
    PAREN_CLOSE #Dispatch
  | TILDE expr #Tilde
  | ISVOID expr #IsVoid
  | expr (MUL | DIV) expr #MulOrDiv
  | expr (ADD | SUB) expr #AddOrSub
  | expr (LT | LE | EQUAL) expr #Comparator
  | NOT expr #Not
  | <assoc=right> OBJECT_IDENTIFIER ASSIGN expr #Assign
  | IF expr THEN expr ELSE expr FI #If
  | WHILE expr LOOP expr POOL #While
  | BRACE_OPEN (expr SEMICOLON)+ BRACE_CLOSE #Block
  | LET
    OBJECT_IDENTIFIER COLON TYPE_IDENTIFIER (ASSIGN expr)?
    (COMMA OBJECT_IDENTIFIER COLON TYPE_IDENTIFIER (ASSIGN expr)?)*
    IN expr #Let
  | CASE expr OF (OBJECT_IDENTIFIER COLON TYPE_IDENTIFIER ARROW expr SEMICOLON)+ ESAC #Case
  | NEW TYPE_IDENTIFIER #New
  | PAREN_OPEN expr PAREN_CLOSE #Paren
  | OBJECT_IDENTIFIER #ObjectIdentifier
  | INT_LITERAL #IntLiteral
  | STRING_LITERAL #StringLiteral
  | BOOL_LITERAL #BoolLiteral
  ;
