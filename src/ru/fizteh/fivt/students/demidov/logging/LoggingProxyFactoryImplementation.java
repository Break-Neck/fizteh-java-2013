package ru.fizteh.fivt.students.demidov.logging;

import ru.fizteh.fivt.proxy.LoggingProxyFactory;
import java.lang.reflect.Proxy;
import java.io.Writer;

public class LoggingProxyFactoryImplementation implements LoggingProxyFactory {
    public Object wrap(Writer writer, Object implementation, Class<?> interfaceClass) {
        if ((interfaceClass == null) || (writer == null) || (implementation == null)) {
            throw new IllegalArgumentException("null argument");
        }
        if (!(interfaceClass.isInstance(implementation))) {
            throw new IllegalArgumentException("wrong implementation");
        }
        if (!(interfaceClass.isInterface())) {
            throw new IllegalArgumentException("wrong interface");
        }

        return Proxy.newProxyInstance(
                implementation.getClass().getClassLoader(),
                new Class[] {
                    interfaceClass
                },
                new LoggingInvocationHandler(implementation, writer)
        );
    }

}