package com.github.cwilper.ttff;

import java.io.IOException;

/**
 * A sequence of objects.
 * <p>
 * This interface is modeled after {@link java.util.Iterator}, with the
 * following differences:
 * <ul>
 *   <li> It is {@link Closeable}.</li>
 *   <li> All methods (except <code>close()</code>) may throw an
 *        {@link IOException}.</li>
 *   <li> An additional method, {@link #peek()} allows access to the next
 *        item without advancing the sequence.</li>
 *   <li> There is no <code>remove()</code> method.</li>
 * </ul>
 *
 * @param <T> the type over which the source operates.
 * @see Sources
 */
public interface Source<T> extends Closeable {

    /**
     * Tells whether the source has any more objects.
     *
     * @return <code>true</code> if there are more objects, <code>false</code>
     *         otherwise.
     * @throws IOException if an I/O problem occurs.
     */
    boolean hasNext() throws IOException;

    /**
     * Gets the next object, advancing the sequence by one.
     *
     * @return the next object.
     * @throws IOException if an I/O problem occurs.
     */
    T next() throws IOException;

    /**
     * Gets the next object without advancing the sequence.
     *
     * @return the next object.
     * @throws IOException if an I/O problem occurs.
     */
    T peek() throws IOException;

}
