package ru.fizteh.fivt.students.belousova.proxy;

import ru.fizteh.fivt.proxy.LoggingProxyFactory;

import java.io.Writer;
import java.lang.reflect.Proxy;

public class StorableLoggingProxyFactory implements LoggingProxyFactory {
    @Override
    public Object wrap(Writer writer, Object implementation, Class<?> interfaceClass) {
        if (writer == null) {
            throw new IllegalArgumentException("writer is null");
        }

        if (interfaceClass == null) {
            throw new IllegalArgumentException("interface class is null");
        }

        if (!interfaceClass.isInstance(implementation)) {
            throw new IllegalArgumentException("target object does not implementing interface class");
        }

        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException("interface class is not exactly interface");
        }

        return Proxy.newProxyInstance(
                implementation.getClass().getClassLoader(),
                new Class[]{interfaceClass},
                new ProxyInvocationHandler(writer, implementation));
    }
}
