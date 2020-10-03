@skip whitespaceExcludingNewline {
    file::= ">>" name description "\n" entry*;
    
}
@skip whitespace{
	wordname ::= [a-z\-]+;
	direction ::= "DOWN" | "ACROSS";
	row ::= Int;
	col ::= Int;
	entry ::= "("  wordname ","  clue "," direction "," row "," col ")"*;
}
name::= stringIndent;
description ::= string;
clue ::= string;
string::= ["]([^\"\r\n\\] | '\\' [\\nrt] )*["];
stringIndent ::= ["][^\"\r\n\t\\]*["];
int ::= [0-9]+;
whitespace ::= [ \t\r\n]+ || javaComment;
whitespaceExcludingNewline ::= [ \t\r]+ || javaComment;
javaComment ::= "\/\/"[^\r\n]*;