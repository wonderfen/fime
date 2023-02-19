package top.someapp.fimesdk.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zwz
 * Created on 2023-02-07
 */
public class Classes {

    private static final Map<String, Class<?>> classCache = new HashMap<>();

    private Classes() {
        //no instance
    }

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(String className) throws ReflectiveOperationException,
            IllegalAccessException {
        Class<?> clz = null;
        if (classCache.containsKey(className)) {
            clz = classCache.get(className);
        }
        else {
            clz = Class.forName(className);
        }
        T t = null;
        if (clz != null) {
            t = (T) clz.newInstance();
        }
        return t;
    }
}
