/* Copyright (c) 2017-2018 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */

@skip whitespace {
    expression ::= hGlue (topToBottomOperator hGlue)*;
    hGlue ::= bottomOverlay ('|' bottomOverlay)*;
    bottomOverlay ::= topOverlay ('_' topOverlay)*;
    topOverlay ::= resize ('^' resize)*;
    resize ::= primitive ('@' number 'x' number)*;
    primitive ::= filename | caption | '(' expression ')';
}
topToBottomOperator ::= '---' '-'*;
filename ::= [A-Za-z0-9./][A-Za-z0-9./_-]*;
number ::= [0-9]+;
whitespace ::= [ \t\r\n]+;
caption ::= ["][ \t\r\nA-Za-z0-9./][ \t\r\nA-Za-z0-9./_-]*["] | ["]["]; 
