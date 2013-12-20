package ru.fizteh.fivt.students.asaitgalin.calc;

public class Token {

    public static enum TokenType {
        OPERATOR,
        NUMBER
    }

    public TokenType type;

    public Token(TokenType type) {
        this.type = type;
    }
}
