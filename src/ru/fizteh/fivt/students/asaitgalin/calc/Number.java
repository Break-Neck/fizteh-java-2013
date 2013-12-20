package ru.fizteh.fivt.students.asaitgalin.calc;

public class Number extends Token {
    private final int radix = 19;
    public int value;

    public Number(String s) throws NumberFormatException {
        super(TokenType.NUMBER);
        value = Integer.parseInt(s, radix);
    }
}
