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
//write a list of the IDs here e.g. CLASS TYPE_ID CURLY_OPEN CURLY_CLOSE
	CLASS
	;
