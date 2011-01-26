package com.github.cwilper.ttff;

/**
 * Convenience base class for {@link Sink} implementations.
 * <p>
 * Implements {@link #close()} as a no-op, which subclasses may override.
 *
 * @param <T> the type over which the sink operates.
 */
public abstract class AbstractSink<T>
        extends AbstractCloseable implements Sink<T> {

    /** Constructor for use by subclasses. */
    protected AbstractSink() { }

}
