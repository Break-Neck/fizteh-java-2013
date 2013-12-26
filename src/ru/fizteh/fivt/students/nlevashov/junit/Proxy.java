package ru.fizteh.fivt.students.nlevashov.junit;

import org.junit.*;
import static org.junit.Assert.*;
import ru.fizteh.fivt.students.nlevashov.proxy.MyLoggingProxyFactory;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;


public class Proxy {
    MyLoggingProxyFactory factory = new MyLoggingProxyFactory();
    Writer writer = new StringWriter();

    @Test(expected = IllegalArgumentException.class)
    public void nullW() {
        factory.wrap(null, new ArrayList<>(), ArrayList.class.getInterfaces()[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void interfaceNullShouldFail() {
        factory.wrap(writer, new ArrayList<Object>(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void implementNullShouldFail() {
        factory.wrap(writer, null, ArrayList.class.getInterfaces()[0]);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldTargetException() {
        ArrayList<Object> obj = new ArrayList<>();
        List instance = (List) factory.wrap(writer, obj, List.class);
        instance.get(1005000);
    }
}


//внести
//клоз многопот (волатайл)
