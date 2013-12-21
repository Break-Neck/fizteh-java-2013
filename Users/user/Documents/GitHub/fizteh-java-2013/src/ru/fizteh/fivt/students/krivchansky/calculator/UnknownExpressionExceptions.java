package ru.fizteh.fivt.students.krivchansky.calculator;

class UnknownExpressionExceptions extends Exception {
    public UnknownExpressionExceptions() {
        super();
    }
    public UnknownExpressionExceptions(String errorDescription){
        super(errorDescription);
    }
    public UnknownExpressionExceptions(Throwable except) {
        super(except);
    }

}
