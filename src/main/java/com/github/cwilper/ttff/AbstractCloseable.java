package com.github.cwilper.ttff;

abstract class AbstractCloseable implements Closeable {

    /**
     * Doesn't do anything by default; subclasses may override.
     */
    @Override
    public void close() {
        // no-op
    }
}
