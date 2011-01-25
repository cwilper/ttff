package com.github.cwilper.ttff;

import java.io.IOException;
import java.util.NoSuchElementException;

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

    protected abstract T computeNext() throws IOException;

    protected final T endOfData() {
        state = State.DONE;
        return null;
    }

    private boolean tryToComputeNext() throws IOException {
        state = state.FAILED;
        next = computeNext();
        if (state != State.DONE) {
            state = State.READY;
            return true;
        }
        return false;
    }

}
