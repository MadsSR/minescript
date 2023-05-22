grammar MineScript;

// Ignore comments that start with '#'
COMMENT: '#' ~[\r\n]* -> skip;

// Tokens
RELDIR: 'left' | 'right' | 'up' | 'down';
ABSDIR: 'north' | 'south' | 'east' | 'west' | 'top' | 'bottom';
NUMBER: [0-9]+;
BOOL: 'true' | 'false';
BLOCK: 'minecraft:'[a-zA-Z0-9_]+;
ID: [a-zA-Z_][a-zA-Z0-9_]*;

NEWLINE: [\r\n]+;
WS: [ \t]+ -> skip;


program
    : statement* EOF
    ;

statements
    : 'do' NEWLINE statement*
    ;

statement
    : expression NEWLINE                                                                                                                      #Expr
    | <assoc=right> ID '=' expression NEWLINE                                                                                                 #Assign
    | 'if' '(' expression ')' statements ('else' 'if' '(' expression ')' statements)* ('else' statements)? 'endif'  NEWLINE                   #If
    | 'while' '(' expression ')' statements 'endwhile' NEWLINE                                                                                #While
    | 'repeat' '(' expression ')' statements 'endrepeat' NEWLINE                                                                              #Repeat
    | 'define' ID '(' formal_paramaters? ')' statements 'enddefine' NEWLINE                                                                   #FuncDecl
    | 'return' expression NEWLINE                                                                                                             #Return
    | NEWLINE                                                                                                                                 #Newline
    ;

expression
    : ID '(' actual_parameters? ')'                                                                                                           #FuncCall
    | '(' expression ')'                                                                                                                      #ParenExpr
    | 'not' expression                                                                                                                        #NotExpr
    | <assoc=right> expression '^' expression                                                                                                 #Pow
    | '-' expression                                                                                                                          #Neg
    | expression op=('*' | '/' | '%') expression                                                                                              #MultDivMod
    | expression op=('+' | '-') expression                                                                                                    #AddSub
    | expression op=('<' | '>' | '<=' | '>=') expression                                                                                      #Comp
    | expression op=('is' | 'is not') expression                                                                                              #IsIsNot
    | expression 'and' expression                                                                                                             #And
    | expression 'or' expression                                                                                                              #Or
    | ID                                                                                                                                      #Id
    | BOOL                                                                                                                                    #Bool
    | RELDIR                                                                                                                                  #RelDir
    | ABSDIR                                                                                                                                  #AbsDir
    | BLOCK                                                                                                                                   #Block
    | NUMBER                                                                                                                                  #Number
    ;

formal_paramaters
    : ID (',' ID)*
    ;

actual_parameters
    : expression (',' expression)*
    ;