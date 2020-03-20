grammar Turing;

programm: state+ ;

state: '(' statements=statement+ ')';

statement: '(' newChar=CHAR oldChar=CHAR direction=DIRECTION nextState=STATENUMBER ')';

CHAR: '*' | '0' | '1' | 'B' ;
DIRECTION: 'L' | 'R' ;
STATENUMBER: [+-][0-9]+ | 'H' ;
WHITESPACE: [ \t\n\r] -> skip;
COMMENT: '#' .+? [\n\r]+ -> skip;