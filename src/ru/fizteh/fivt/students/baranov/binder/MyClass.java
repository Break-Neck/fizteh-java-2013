package ru.fizteh.fivt.students.baranov.binder;

import ru.fizteh.fivt.binder.DoNotBind;
import ru.fizteh.fivt.binder.Name;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 12/20/13
 * Time: 6:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class MyClass {
    public int first;
    public String second;
    @Name("third")
    public int fourth;
    @DoNotBind
    public String xyu;
    public MyClass2 ololo;
}
