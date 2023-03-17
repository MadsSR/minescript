grammar MineScriptV4;

// Ignore comments that start with '#'
COMMENT: '#' ~[\r\n]* -> skip;

// Tokens
RELDIR: 'left' | 'right';
ABSDIR: 'north' | 'south' | 'east' | 'west';
NUMBER: [0-9]+;
BOOL: 'true' | 'false';
BLOCK: 'minecraft:'[a-zA-Z0-9_]+;
ID: [a-zA-Z_][a-zA-Z0-9_]*;

NEWLINE: [\r\n]+;
WS: [ \t]+ -> skip;


program
    : statements EOF
    ;

statements
    : statement*
    ;

statement
    : expression NEWLINE                                                                                                                      #Expr
    | <assoc=right>ID '=' expression NEWLINE                                                                                                  #Assign
    | 'if' '(' expression ')' 'do' statements ('else' 'if' '(' expression ')' 'do' statements)* ('else' 'do' statements)? 'endif'  NEWLINE    #If
    | 'while' '(' expression ')' 'do' statements 'endwhile' NEWLINE                                                                           #While
    | 'repeat' '(' expression ')' 'do' statements 'endrepeat' NEWLINE                                                                         #Repeat
    | 'define' ID '(' formal_paramaters? ')' 'do' (statement | 'return' expression NEWLINE )* 'enddefine' NEWLINE                             #FuncDecl
    | NEWLINE                                                                                                                                 #Newline
    ;

expression
    : ID '(' actual_parameters? ')'                                                                                                           #FuncCall
    | '-' expression                                                                                                                          #Neg
    | 'not' expression                                                                                                                        #NotExpr
    | '(' expression ')'                                                                                                                      #ParenExpr
    | <assoc=right> expression '^' expression                                                                                                 #Pow
    | expression ('*' | '/' | '%') expression                                                                                                 #MultDivMod
    | expression ('+' | '-') expression                                                                                                       #AddSub
    | expression ('<' | '>' | '<=' | '>=') expression                                                                                         #Comp
    | expression ('is' | 'is not') expression                                                                                                 #IsIsNot
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