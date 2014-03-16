package ru.fizteh.fivt.students.kocurba.shell;

public class StateWrap<T> {
    private T state;

    public StateWrap(T dataBase) {
        state = dataBase;
    }

    public void setState(T newState) {
        state = newState;
    }

    public T getState() {
        return state;
    }

}
