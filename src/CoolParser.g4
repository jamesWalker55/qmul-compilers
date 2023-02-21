/**
 * Define a grammar for Cool
 */
parser grammar CoolParser;

options { tokenVocab = CoolLexer; }


/*  Starting point for parsing a Cool file  */

program 
	: (coolClass SEMICOLON)+ EOF
	;

coolClass :
//COOL CLASS HAS OPTIONAL INSTANCE VARIABLES, THEN FUNCTIONS
//write a list of the IDs here e.g. CLASS TYPE_ID CURLY_OPEN CURLY_CLOSE
//THE ORDER OF THESE ID'S MATTER
	KW_CLASS
	CLASSNAME
	CURLY_OPEN
	// INHERITS
	CURLY_CLOSE
	;

/*
Cool hierarchy:

CLASSTYPE CLASSNAME {
    VARIABLENAME : OBJECTTYPE (<- VALUE);
    FUNCTIONNAME() : OBJECTTYPE{
        EXPRESSION/BLOCK
    }
};
*/