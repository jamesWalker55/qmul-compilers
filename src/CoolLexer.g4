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
CLASS : [Cc][Ll][Aa][Ss][Ss]' '+ -> mode(CLASS_MODE); //1 or more spaces after class

mode CLASS_MODE;
END_CLASS: ' ' -> mode(DEFAULT_MODE);
NAME : . -> more ; // THIS NEEDS TO BE CHANGED TO ACCEPT ONLY WHAT CLASSES CAN BE NAMED
mode DEFAULT_MODE;

INHERITS : [Ii][Nn][Hh][Ee][Rr][Ii][Tt][Ss];

LET: [Ll][Ee][Tt];
IN: [Ii][Nn];
CASE: [Cc][Aa][Ss][Ee];
OF: [Oo][Ff];
ESAC: [Ee][Ss][Aa][Cc];
IF: [Ii][Ff];
THEN: [Tt][Hh][Ee][Nn];
ELSE: [Ee][Ll][Ss][Ee];
FI: [Ff][Ii];
WHILE: [Ww][Hh][Ii][Ll][Ee];
LOOP: [Ll][Oo][Oo][Pp];
NEW: [Nn][Ee][Ww];
ISVOID: [Ii][Ss][Vv][Oo][Ii][Dd];
NOT: [Nn][Oo][Tt];

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
BEGIN_STRING: '"' -> more, mode(STRING_MODE);

mode STRING_MODE;
END_STRING: '"' -> mode(DEFAULT_MODE);
TEXT : . -> more ; // collect more text for string
mode DEFAULT_MODE;

//Object type keywords
TYPE: OBJECT | INT | STRING | BOOL | IO | SELF_TYPE;
fragment IO: [I][O]; //I'm unsure if IO is case sensitive
fragment SELF_TYPE: [S][E][L][F][_][T][Y][P][E];
fragment OBJECT: [O][b][j][e][c][t];
fragment INT: [I][n][t];
fragment STRING: [S][t][r][i][n][g];
fragment BOOL: [B][o][o][L];

COMMENT: '(*' ( COMMENT | .)*? '*)' -> skip; //recursive call for nested comments

ID: [a-z];
fragment
LETTER: [a-zA-Z];

WS : [ \r\t\n]+ -> skip ; //skip whitespace

ERROR : . ; //this should be at the end if the input doesnt match any token