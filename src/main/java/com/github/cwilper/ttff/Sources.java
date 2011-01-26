package com.github.cwilper.ttff;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Static utility methods for creating, filtering, and draining
 * {@link Source}s.
 */
public final class Sources {

    /** Instantiation disallowed. */
    Sources() { throw new AssertionError(); }

    /**
     * Gets an empty source.
     *
     * @param <T> the type.
     * @return the source.
     */
    public static <T> Source<T> empty() {
        return new AbstractSource<T>() {
            @Override
            public T computeNext() {
                return endOfData();
            }
        };
    }

    /**
     * Gets a version of the given source whose items are subject to the
     * given filter. Items may be omitted or transformed by the filter.
     * <p>
     * When the returned source is closed, the wrapped source will also be
     * closed, but the filter will not be.
     *
     * @param source the source to filter.
     * @param filter the filter to use.
     * @param <T> the type.
     * @return the filtering source.
     */
    public static <T> Source<T> filter(final Source<T> source,
                                       final Filter<T> filter) {
        return new AbstractSource<T>() {
            @Override
            public T computeNext() throws IOException {
                while (source.hasNext()) {
                    T item = filter.accept(source.next());
                    if (item != null) {
                        return item;
                    }
                }
                return endOfData();
            }

            @Override
            public void close() {
                source.close();
            }
        };
    }

    /**
     * Gets a source from the given items.
     *
     * @param items the items.
     * @param <T> the type.
     * @return the source.
     */
    public static <T> Source<T> from(T... items) {
        return from(Arrays.asList(items));
    }

    /**
     * Gets a source from the items in the given collection.
     *
     * @param collection the collection.
     * @param <T> the type.
     * @return the source.
     */
    public static <T> Source<T> from(Collection<T> collection) {
        return from(collection.iterator());
    }

    /**
     * Gets a source from the items in the given iterator.
     *
     * @param iterator the iterator.
     * @param <T> the type.
     * @return the source.
     */
    public static <T> Source<T> from(final Iterator<T> iterator) {
        return new AbstractSource<T>() {
            @Override
            protected T computeNext() throws IOException {
                if (iterator.hasNext()) {
                    return iterator.next();
                }
                return endOfData();
            }
        };
    }

    /**
     * Gets an iterator that wraps the given source.
     * <p>
     * Callers must ensure that the wrapped source is closed when no longer
     * needed; the returned iterator provides no <code>close()</code> method.
     *
     * @param source the source.
     * @param <T> the type.
     * @return the iterator.
     */
    public static <T> Iterator<T> iterator(final Source<T> source) {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                try {
                    return source.hasNext();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public T next() {
                try {
                    return source.next();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Gets a source that joins the given sources end to end.
     * <p>
     * When closed, the returned source will ensure the wrapped sources are
     * all closed.
     *
     * @param sources the iterator of sources to join.
     * @param <T> the type.
     * @return the joined source.
     */
    public static <T> Source<T> join(Source<T>... sources) {
        return join(Arrays.asList(sources));
    }

    /**
     * Gets a source that joins the given collection of sources end to end.
     * <p>
     * When closed, the returned source will ensure the wrapped sources are
     * all closed.
     *
     * @param collection the collection of sources to join.
     * @param <T> the type.
     * @return the joined source.
     */
    public static <T> Source<T> join(Collection<Source<T>> collection) {
        return join(collection.iterator());
    }

    /**
     * Gets a source that joins the given iterator of sources end to end.
     * <p>
     * When closed, the returned source will ensure the wrapped sources are
     * all closed.
     *
     * @param iterator the sources to join.
     * @param <T> the type.
     * @return the joined source.
     */
    public static <T> Source<T> join(final Iterator<Source<T>> iterator) {
        return new AbstractSource<T>() {
            private Source<T> current = popSource();

            @Override
            public T computeNext() throws IOException {
                while (current != null) {
                    if (current.hasNext()) {
                        return current.next();
                    } else {
                        current.close();
                        current = popSource();
                    }
                }
                return endOfData();
            }

            @Override
            public void close() {
                while (current != null) {
                    current.close();
                    current = popSource();
                }
            }

            private Source<T> popSource() {
                if (iterator.hasNext()) {
                    return iterator.next();
                } else {
                    return null;
                }
            }
        };
    }

    /**
     * Exhausts the given source.
     * <p>
     * The source will be automatically closed regardless of success.
     *
     * @param source the source to exhaust.
     * @param <T> the type.
     * @return the number of items encountered.
     * @throws IOException if an I/O problem occurs.
     */
    public static <T> long drain(Source<T> source) throws IOException {
        return drain(source, new AbstractSink<T>() {
            @Override
            public void put(T item) { }
        });
    }

    /**
     * Exhausts the given source, adding each item to the given collection.
     * <p>
     * The source will be automatically closed regardless of success.
     *
     * @param source the source to exhaust.
     * @param collection the collection add each item to.
     * @param <T> the type.
     * @return the number of items encountered.
     * @throws IOException if an I/O problem occurs.
     */
    public static <T> long drain(Source<T> source,
                                 final Collection<T> collection)
            throws IOException {
        return drain(source, new AbstractSink<T>() {
            @Override
            public void put(T item) {
                collection.add(item);
            }
        });
    }

    /**
     * Exhausts the given source, sending each item to the given sink.
     * <p>
     * The source will be automatically closed regardless of success.
     *
     * @param source the source to exhaust.
     * @param sink the sink to send each item to.
     * @param <T> the type.
     * @return the number of items encountered.
     * @throws IOException if an I/O problem occurs.
     */
    public static <T> long drain(Source<T> source, Sink<T> sink)
            throws IOException {
        long count = 0L;
        try {
            while (source.hasNext()) {
                count++;
                sink.put(source.next());
            }
            return count;
        } finally {
            source.close();
        }
    }
}
