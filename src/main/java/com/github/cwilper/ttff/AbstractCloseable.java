package com.github.cwilper.ttff;

/**
 * Convenience superclass for abstract {@link Closeable}s in this package.
 */
abstract class AbstractCloseable implements Closeable {

    /** Constructor for use by subclasses. */
    protected AbstractCloseable() { }

    /**
     * Doesn't do anything by default; subclasses may override.
     */
    @Override
    public void close() {
        // no-op
    }
}
