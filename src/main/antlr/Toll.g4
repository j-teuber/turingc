grammar Toll;

program: parameterHeader instrList;

parameterHeader: 'param' params=IDENTIFIEER* ':';

instrList: instr=instruction+ ;

instruction: 'var' name=IDENTIFIEER '=' expr=numExpression ';' #VarInit
           | name=IDENTIFIEER '=' expr=numExpression ';' #VarModification
           | 'while''(' condition=boolExpression ')''{' content=instrList '}' #While
           | 'if''(' condition=boolExpression ')''{' ifContent=instrList '}' #If
           | 'if''(' condition=boolExpression ')''{' ifContent=instrList '}''else''{' elseContent=instrList '}' #IfElse
           | 'when''(' value=numExpression ')''{' content=whenContent+ '}' #When
           | 'return' expr=numExpression ';' #Return
           ;

numExpression: '(' content=numExpression')' #NumParanthesis
             | right=numExpression '/' left=numExpression #Div
             | left=numExpression '*' right=numExpression #Mult
             | left=numExpression '-' right=numExpression #Sub
             | left=numExpression '+' right=numExpression #Add
             | number=NUMBER #Number
             | name=IDENTIFIEER #VarUse
             ;

boolExpression: '(' content=boolExpression ')' #BoolParanthesis
              | left=boolExpression '&&' right=boolExpression #And
              | left=boolExpression '||' right=boolExpression #Or
              | bigger=numExpression '>' smaller=numExpression #More
              | smaller=numExpression '<' bigger=numExpression #Less
              | left=numExpression '==' right=numExpression #Equal
              | bool=BOOLEAN #Boolean
              ;

whenContent: value=numExpression '->''{'content=instrList'}';

IDENTIFIEER: ([a-z] | [A-Z]) ([a-z] | [A-Z] | [0-9] | '$' | '-' | '_')* ;
NUMBER: [0-9]+ ;
BOOLEAN: 'true' | 'false' ;
WHITESPACE: [ \t\n\r] -> skip;
COMMENT: '//' .+? [\n\r]+ -> skip;