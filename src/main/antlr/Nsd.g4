grammar Nsd;

programm: parameterHeader (instruction DELIMITER)+ EOF;

parameterHeader: 'program' name=IDENTIFIER '(' parameters ')' ':';

parameters: (parameter ',')*;

parameter: name=IDENTIFIER;

instruction: 'INIT' varName=IDENTIFIER #Init
         | 'INC' varName=IDENTIFIER #Increment
         | 'DEC' varName=IDENTIFIER #Decrement
         | 'RETURN' varName=IDENTIFIER #Return
         | 'LABEL' label=IDENTIFIER #Label
         | 'IFNULL' varName=IDENTIFIER 'GOTO' label=IDENTIFIER #Goto
         ;

DELIMITER: ';';
IDENTIFIER: ([a-z] | [A-Z]) ([a-z] | [A-Z] | [0-9] | '$' | '-' | '_')*;
WHITESPACE: [ \t\n\r] -> skip;
COMMENT: '#' .+? [\n\r]+ -> skip;