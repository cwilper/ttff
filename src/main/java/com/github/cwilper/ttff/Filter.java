package com.github.cwilper.ttff;

import java.io.IOException;

/**
 * A function that accepts, rejects, or transforms objects.
 *
 * @param <T> the type over which the filter operates.
 */
public interface Filter<T> extends Closeable {

    /**
     * Accepts, rejects, or transforms the given object.
     *
     * @param item the object.
     * @return The original object, a derivative, or <code>null</code> if
     *         the object is rejected.
     * @throws IOException if an I/O problem occurs.
     */
    T accept(T item) throws IOException;

}
