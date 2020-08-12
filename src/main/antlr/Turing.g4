grammar Turing;

programm: state+ ;

state: '(' statements=statement+ ')';

statement: '('
    newChar=characterLiteral
    oldChar=characterLiteral
    direction=directionLiteral
    nextState=stateNumberLiteral
    ')';

characterLiteral: ZERO_OR_ONE | SPECIAL_CHAR;
directionLiteral: DIRECTION;
stateNumberLiteral: ZERO_OR_ONE | STATENUMBER;

ZERO_OR_ONE: '0' | '1';
SPECIAL_CHAR: '*' | 'B' ;
DIRECTION: 'L' | 'R' ;
STATENUMBER: [+-]?[0-9]+ | 'H' ;
WHITESPACE: [ \t\n\r] -> skip;
COMMENT: '#' .+? [\n\r]+ -> skip;