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


// Keywords (keywords are case insensitive)
KW_CLASS : [Cc][Ll][Aa][Ss][Ss];
//mode CLASS_MODE;
//END_CLASS: ' ' -> mode(DEFAULT_MODE);
//NAME : . -> more ; // THIS NEEDS TO BE CHANGED TO ACCEPT ONLY WHAT CLASSES CAN BE NAMED
//NAME : TYPE_ID -> more ;
//mode DEFAULT_MODE;
//{ setText("Unterminated string constant"); }
//-> type(ERROR), popMode;
CLASSNAME : TYPE_ID;

KW_INHERITS : [Ii][Nn][Hh][Ee][Rr][Ii][Tt][Ss];

KW_LET: [Ll][Ee][Tt];
KW_IN: [Ii][Nn];
KW_CASE: [Cc][Aa][Ss][Ee];
KW_OF: [Oo][Ff];
KW_ESAC: [Ee][Ss][Aa][Cc];
KW_IF: [Ii][Ff];
KW_THEN: [Tt][Hh][Ee][Nn];
KW_ELSE: [Ee][Ll][Ss][Ee];
KW_FI: [Ff][Ii];
KW_WHILE: [Ww][Hh][Ii][Ll][Ee];
KW_LOOP: [Ll][Oo][Oo][Pp];
KW_NEW: [Nn][Ee][Ww];
KW_ISVOID: [Ii][Ss][Vv][Oo][Ii][Dd];
KW_NOT: [Nn][Oo][Tt];
KW_RETURN: [Rr][Ee][Tt][Uu][Rr][Nn];

/*BOOL, INT AND STRING VALUES*/

BOOLEAN : TRUE | FALSE; //This passes the actual value of TRUE or FALSE
fragment
TRUE : [t][Rr][Uu][Ee];
fragment
FALSE : [f][Aa][Ll][Ss][Ee];

//Greedy operator + matches as much input as possible
INTEGER: DIGIT+;
fragment
DIGIT: [0-9];

//TO CREATE STRING, GO INTO A SEPERATE MODE
BEGIN_STRING : '"' -> more, pushMode(STRING_MODE);

mode STRING_MODE;
END_STRING : '"' -> popMode;
STRING_TEXT : ~[\\\r\n"] -> more;
UNTERMINATED_STRING : '\n'
{ setText("Unterminated string constant"); }
-> type(ERROR), popMode;
mode DEFAULT_MODE;

//Object type keywords
TYPE: KW_OBJECT_TYPE | KW_INT_TYPE | KW_STRING_TYPE | KW_BOOL_TYPE | KW_IO_TYPE | KW_SELF_TYPE;
fragment KW_IO_TYPE: [I][O]; //I'm unsure if IO is case sensitive
fragment KW_SELF_TYPE: [S][E][L][F][_][T][Y][P][E];
fragment KW_OBJECT_TYPE: [O][b][j][e][c][t];
fragment KW_INT_TYPE: [I][n][t];
fragment KW_STRING_TYPE: [S][t][r][i][n][g];
fragment KW_BOOL_TYPE: [B][o][o][L];

COMMENT: '(*' ( COMMENT | .)*? '*)' -> skip; //recursive call for nested comments

//Letters, digits and underscore character
TYPE_ID : [A-Z] IDENTIFIER*;
fragment
LETTER: [a-zA-Z];
IDENTIFIER : LETTER | DIGIT | '_';

WS : [ \r\t\n]+ -> skip ; //skip whitespace

ERROR : . ; //this should be at the end if the input doesnt match any token