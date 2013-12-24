package ru.fizteh.fivt.students.kinanAlsarmini.binder.test.hclasses;

import ru.fizteh.fivt.binder.DoNotBind;

public class SerializableInner {
    @DoNotBind
    public int a;
    private String test;
    protected Long g;

    public SerializableInner() {
    }

    public SerializableInner(int a, String test, Long g) {
        this.a = 0;
        this.test = test;
        this.g = g;
    }
}
