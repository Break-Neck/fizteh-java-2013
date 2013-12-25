package ru.fizteh.fivt.students.demidov.logging;

import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;

import org.json.JSONObject;

public class LoggingInvocationHandler implements InvocationHandler {
    private Writer writer;
    private Object object;

    public LoggingInvocationHandler(Object object, Writer writer) {
        this.writer = writer;
        this.object = object;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = null;
        if (method.getDeclaringClass().equals(Object.class)) {
            try {
                result = method.invoke(object, args);
            } catch (InvocationTargetException catchedException) {
                throw catchedException.getTargetException();
            }
        } else {
            ThreadLocal<JSONObject> jsonObject = new ThreadLocal<JSONObject>() {
                public JSONObject initialValue() {
                    return new JSONObject();
                }
            };
            
            LoggingUtils.writeLog(jsonObject.get(), object, method, args);
            try {
                result = method.invoke(object, args);
                if (method.getReturnType() != void.class) {
                    LoggingUtils.logReturnValue(jsonObject.get(), result);
                }
            } catch (InvocationTargetException catchedException) {
                jsonObject.get().put("thrown", catchedException.getTargetException().toString());
                synchronized (writer) {
                    writer.write(jsonObject.get().toString(2) + '\n');
                }
                throw catchedException.getTargetException();
            }
            synchronized (writer) {
                writer.write(jsonObject.get().toString(2) + '\n');
            }
        }
            
        return result;
    }
}
