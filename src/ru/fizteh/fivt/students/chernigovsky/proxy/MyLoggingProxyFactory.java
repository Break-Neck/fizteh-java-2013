package ru.fizteh.fivt.students.chernigovsky.proxy;

import ru.fizteh.fivt.proxy.LoggingProxyFactory;

import java.io.Writer;
import java.lang.reflect.Proxy;

public class MyLoggingProxyFactory implements LoggingProxyFactory {
    /**
     * Создаёт класс-обёртку вокруг объекта <code>implementation</code>, которая при вызове
     * методов интерфейса <code>interfaceClass</code> выполняет логирование аргументов и результата
     * вызова методов.
     *
     * Класс-обёртка не имеет права выбрасывать свои исключения, но обязана выбрасывать те же самые
     * исключения, что выбрасывает оригинальный класс.
     *
     * Класс-обёртка должен быть потокобезопасным.
     *
     * @param writer          Объект, в который ведётся запись лога.
     * @param implementation  Объект, реализующий интерфейс <code>interfaceClass</code>.
     * @param interfaceClass  Класс интерфейса, методы которого должны выполнять запись в лог.
     *
     * @return Объект, реализующий интерфейс <code>interfaceClass</code>, при вызове методов которого
     * выполняется запись в лог.
     *
     * @throws IllegalArgumentException Если любой из переданных аргументов null или имеет некорректное значение.
     */
    public Object wrap(
            Writer writer,
            Object implementation,
            Class<?> interfaceClass
    ) {
        if (writer == null || implementation == null || interfaceClass == null) {
            throw new IllegalArgumentException("null argument");
        }

        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException("not interface");
        }

        if (!interfaceClass.isInstance(implementation)) {
            throw new IllegalArgumentException("interface isn't instance of implementation");
        }

        return Proxy.newProxyInstance(implementation.getClass().getClassLoader(),
                new Class[]{interfaceClass}, new LoggingInvocationHandler(writer, implementation));
    }
}
