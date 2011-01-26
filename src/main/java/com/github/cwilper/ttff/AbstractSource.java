package com.github.cwilper.ttff;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * Convenience base class for {@link Source} implementations.
 * <p>
 * Implements {@link #close()} as a no-op, which subclasses may override.
 * <p>
 * Also implements all other required methods, requiring only that subclasses
 * override {@link #computeNext()}.
 *
 * @param <T> the type over which the source operates.
 */
public abstract class AbstractSource<T>
        extends AbstractCloseable implements Source<T> {

    private State state = State.NOT_READY;

    private enum State {
        /** Next element computed and available via peek or next. */
        READY,

        /** Next element not yet computed. */
        NOT_READY,

        /** No more elements. */
        DONE,

        /** An exception occurred while computing next. */
        FAILED
    }

    private T next;

    /** Constructor for use by subclasses. */
    protected AbstractSource() { }

    @Override
    public final boolean hasNext() throws IOException {
        if (state == State.FAILED) {
            throw new IllegalStateException();
        }
        switch (state) {
            case DONE:
                return false;
            case READY:
                return true;
            default:
        }
        return tryToComputeNext();
    }

    @Override
    public final T next() throws IOException {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        state = State.NOT_READY;
        return next;
    }

    @Override
    public final T peek() throws IOException {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return next;
    }

    /**
     * Gets the next value, advancing the sequence by one. If no more values
     * exist, returns {@link #endOfData}.
     *
     * @return the next value, or <code>endOfData()</code> if exhausted.
     * @throws IOException if an I/O problem occurs.
     */
    protected abstract T computeNext() throws IOException;

    /**
     * Internally signals that sequence is exhausted, then returns
     * <code>null</code>.
     *
     * @return <code>null</code>.
     */
    protected final T endOfData() {
        state = State.DONE;
        return null;
    }

    private boolean tryToComputeNext() throws IOException {
        state = State.FAILED;
        next = computeNext();
        if (state != State.DONE) {
            state = State.READY;
            return true;
        }
        return false;
    }

}
