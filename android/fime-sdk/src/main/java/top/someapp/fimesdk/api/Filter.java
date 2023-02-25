package top.someapp.fimesdk.api;

import java.util.Collection;

/**
 * @author zwz
 * Created on 2023-02-25
 */
public interface Filter<E> {

    void filter(Collection<E> items, Schema schema);
}
