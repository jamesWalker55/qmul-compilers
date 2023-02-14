/**
 * Define a lexer rules for Cool
 */
lexer grammar CoolLexer;

/* Punctution */

PERIOD              : '.';
COMMA               : ',';
AT                  : '@';
SEMICOLON           : ';';
COLON               : ':';

CURLY_OPEN          : '{' ;
CURLY_CLOSE         : '}' ;
PARENT_OPEN         : '(' ;
PARENT_CLOSE        : ')' ;

/* Operators */

PLUS_OPERATOR       : '+';
MINUS_OPERATOR      : '-';
MULT_OPERATOR       : '*';
DIV_OPERATOR        : '/';

INT_COMPLEMENT_OPERATOR     : '~';

LESS_OPERATOR               : '<';
LESS_EQ_OPERATOR            : '<=';
EQ_OPERATOR                 : '=' ;
ASSIGN_OPERATOR 	        : '<-';
RIGHTARROW                  : '=>';

ERROR : . ;

// Keywords (keywords are case insensitive)
CLASS : [Cc][Ll][Aa][Ss][Ss];
INHERITS : [Ii][Nn][Hh][Ee][Rr][Ii][Tt];

BOOL : TRUE | FALSE; //This passes the actual value of TRUE or FALSE
fragment
TRUE : [t][Rr][Uu][Ee];
fragment
FALSE : [f][Aa][Ll][Ss][Ee];

LET: [Ll][Ee][Tt];
IN: [Ii][Nn];
CASE: [Cc][Aa][Ss][Ee];
OF: [Oo][Ff];

