package com.github.cwilper.ttff;

/**
 * Convenience base class for {@link Filter} implementations.
 * <p>
 * Implements {@link #close()} as a no-op, which subclasses may override.
 *
 * @param <T> the type over which the filter operates.
 */
public abstract class AbstractFilter<T>
        extends AbstractCloseable implements Filter<T> {

    /** Constructor for use by subclasses. */
    protected AbstractFilter() { }

}
