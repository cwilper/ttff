package com.github.cwilper.ttff;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public final class Sources {
    Sources() { throw new AssertionError(); }

    public static <T> Source<T> empty() {
        return new AbstractSource<T>() {
            @Override
            public T computeNext() {
                return endOfData();
            }
        };
    }

    public static <T> Source<T> from(T... items) {
        return from(Arrays.asList(items));
    }

    public static <T> Source<T> from(Collection<T> collection) {
        return from(collection.iterator());
    }

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

    public static <T> Source<T> join(Source<T>... sources) {
        return join(Arrays.asList(sources));
    }

    public static <T> Source<T> join(Collection<Source<T>> sources) {
        return join(sources.iterator());
    }

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

    public static <T> long drain(Source<T> source) throws IOException {
        return drain(source, new AbstractSink<T>() {
            @Override
            public void put(T item) { }
        });
    }

    public static <T> long drain(Source<T> source, Sink<T> sink)
            throws IOException {
        long count = 0L;
        while (source.hasNext()) {
            count++;
            sink.put(source.next());
        }
        return count;
    }

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
}
