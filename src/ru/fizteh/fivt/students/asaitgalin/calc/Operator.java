package ru.fizteh.fivt.students.asaitgalin.calc;

import java.io.IOException;

public class Operator extends Token {

    public static enum OperatorType {
        ADDITION,
        SUBTRACTION,
        MULTIPLICATION,
        DIVISION,
        LBRACKET,
        RBRACKET
    }

    public OperatorType opType;
    public char lexeme;

    public Operator(char c) throws IOException {
        super(TokenType.OPERATOR);
        lexeme = c;
        switch (c) {
            case '+':
                opType = OperatorType.ADDITION;
                break;
            case '-':
                opType = OperatorType.SUBTRACTION;
                break;
            case '*':
                opType = OperatorType.MULTIPLICATION;
                break;
            case '/':
                opType = OperatorType.DIVISION;
                break;
            case '(':
                opType = OperatorType.LBRACKET;
                break;
            case ')':
                opType = OperatorType.RBRACKET;
                break;
            default:
                throw new IOException("Wrong operation lexeme");
        }
    }
}
