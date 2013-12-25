package ru.fizteh.fivt.students.kinanAlsarmini.binder.test.hclasses;

import ru.fizteh.fivt.binder.DoNotBind;
import ru.fizteh.fivt.binder.Name;

import ru.fizteh.fivt.students.kinanAlsarmini.binder.test.hclasses.SerializableInner;
import ru.fizteh.fivt.students.kinanAlsarmini.binder.test.hclasses.Day;

public class SerializableOuter {
    public Day day;
    private long a;
    public String b;
    protected boolean c;
    public String empty;
    SerializableInner nullish;
    @DoNotBind
    int d;
    @Name("New")
    String rename;

    public SerializableOuter() {
        empty = "";
        nullish = null;
        rename = "ABC";
    }

    public SerializableOuter(Day day, long a, String b, boolean c, int d) {
        this();
        this.day = day;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public void print() {
        System.out.println(day + " " + a + " " + b + " " + c + " " + empty + " " + nullish + " " + d + " " + rename);
    }
}
