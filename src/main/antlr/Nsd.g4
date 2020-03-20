grammar Nsd;

programm: (instruction DELIMITER)+ ;

instruction: 'INIT' varName=IDENTIFIER #Init
         | 'INC' varName=IDENTIFIER #Increment
         | 'DEC' varName=IDENTIFIER #Decrement
         | 'RETURN' varName=IDENTIFIER #Return
         | 'LABEL' label=IDENTIFIER #Label
         | 'IFNULL' varName=IDENTIFIER 'GOTO' label=IDENTIFIER #Goto
         ;

DELIMITER: ';';
IDENTIFIER: [a-z][a-zA-Z0-9\-]*;
WHITESPACE: [ \t\n\r] -> skip;
COMMENT: '#' .+? [\n\r]+ -> skip;