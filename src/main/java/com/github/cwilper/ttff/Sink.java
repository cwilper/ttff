package com.github.cwilper.ttff;

import java.io.IOException;

/**
 * A receiver of objects.
 *
 * @param <T> the type over which the sink operates.
 */
public interface Sink<T> extends Closeable {

    /**
     * Receives the given object.
     *
     * @param item the object.
     * @throws IOException if an I/O problem occurs.
     */
    void put(T item) throws IOException;

}
