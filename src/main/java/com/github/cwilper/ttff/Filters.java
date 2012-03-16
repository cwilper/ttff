package com.github.cwilper.ttff;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * Static utility methods for creating common types of {@link Filter}s.
 */
public final class Filters {

    /** Instantiation disallowed. */
    Filters() { throw new AssertionError(); }

    /**
     * Gets a filter that sends objects to the given sink, returning the
     * original item unchanged.
     * <p>
     * When the returned filter is closed, the underlying sink will be closed.
     *
     * @param sink the sink.
     * @param <T> the type.
     * @return the filter.
     */
    public static <T> Filter<T> from(final Sink<T> sink) {
        return new NonMutatingFilter<T>() {
            @Override
            public boolean accepts(T item) throws IOException {
                sink.put(item);
                return true;
            }
            @Override
            public void close() {
                sink.close();
            }
        };
    }

    /**
     * Gets a filter that always either returns the original object unchanged
     * or returns <code>null</code>.
     *
     * @param value whether to accept or reject all items.
     * @param <T> the type.
     * @return the filter.
     */
    public static <T> Filter<T> bool(final boolean value) {
        return new NonMutatingFilter<T>() {
            @Override
            public boolean accepts(T item) {
                return value;
            }
        };
    }

    /**
     * Gets a filter that returns the original object unchanged if it
     * is an <code>instanceof</code> the given class, <code>null</code>
     * otherwise.
     *
     * @param clazz the class.
     * @param <T> the type.
     * @return the filter.
     */
    public static <T> Filter<T> isa(final Class clazz) {
        return new NonMutatingFilter<T>() {
            @Override
            public boolean accepts(T item) {
                return clazz.isInstance(item);
            }
        };
    }

    /**
     * Gets a composite filter that sends the original object to each of the
     * given filters unconditionally, then returns the original object
     * unchanged.
     * <p>
     * When the composite filter is closed, the underlying filters will be
     * closed.
     *
     * @param filters the filters.
     * @param <T> the type.
     * @return the composite filter.
     */
    public static <T> Filter<T> all(Filter<T>... filters) {
        return all(Arrays.asList(filters));
    }

    /**
     * Gets a composite filter that sends the original object to each of the
     * given filters unconditionally, then returns the original object
     * unchanged.
     * <p>
     * When the composite filter is closed, the underlying filters will be
     * closed.
     *
     * @param filters the filters.
     * @param <T> the type.
     * @return the composite filter.
     */
    public static <T> Filter<T> all(Collection<Filter<T>> filters) {
        return new MultiFilter<T>(filters) {
            @Override
            public T accept(T item) throws IOException {
                for (Filter<T> filter: filters) {
                    filter.accept(item);
                }
                return item;
            }
        };
    }

    /**
     * Gets a composite filter that implements short-circuiting AND logic.
     * It sends the object to each of the given filters in order, possibly
     * transforming it at each step, then returns the final filter's response.
     * Along the way, if any filter returns <code>null</code>, the remaining
     * filters will be skipped and it will return <code>null</code>.
     * <p>
     * When the composite filter is closed, the underlying filters will be
     * closed.
     *
     * @param filters the filters.
     * @param <T> the type.
     * @return the composite filter.
     */
    public static <T> Filter<T> and(Filter<T>... filters) {
        return and(Arrays.asList(filters));
    }

    /**
     * Gets a composite filter that implements short-circuiting AND logic.
     * It sends the object to each of the given filters in order, possibly
     * transforming it at each step, then returns the final filter's response.
     * Along the way, if any filter returns <code>null</code>, the remaining
     * filters will be skipped and it will return <code>null</code>.
     * <p>
     * When the composite filter is closed, the underlying filters will be
     * closed.
     *
     * @param filters the filters.
     * @param <T> the type.
     * @return the composite filter.
     */
    public static <T> Filter<T> and(Collection<Filter<T>> filters) {
        return new MultiFilter<T>(filters) {
            @Override
            public T accept(T item) throws IOException {
                T result = item;
                for (Filter<T> filter: filters) {
                    result = filter.accept(item);
                    if (result == null) {
                        return null;
                    }
                }
                return result;
            }
        };
    }

    /**
     * Gets a filter that inverts the boolean response of the given filter.
     * Objects that return any value when passed to the wrapped filter will
     * return <code>null</code>. Those that return <code>null</code> will
     * instead return the original value.
     * <p>
     * When the returned filter is closed, the underlying filter will be
     * closed.
     *
     * @param filter the filter to invert.
     * @param <T> the type.
     * @return the inverting filter.
     */
    public static <T> Filter<T> not(final Filter<T> filter) {
        return new NonMutatingFilter<T>() {
            @Override
            public boolean accepts(T item) throws IOException {
                T result = filter.accept(item);
                if (result == null) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void close() {
                filter.close();
            }
        };
    }

    /**
     * Gets a composite filter that implements short-circuiting OR logic.
     * It sends the original object to each of the given filters in order,
     * until one gives a non-<code>null</code> result, then returns that
     * result. If all filters return <code>null</code>, this filter will also
     * return <code>null</code>.
     * <p>
     * When the composite filter is closed, the underlying filters will be
     * closed.
     *
     * @param filters the filters.
     * @param <T> the type.
     * @return the composite filter.
     */
    public static <T> Filter<T> or(Filter<T>... filters) {
        return or(Arrays.asList(filters));
    }

    /**
     * Gets a composite filter that implements short-circuiting OR logic.
     * It sends the original object to each of the given filters in order,
     * until one gives a non-<code>null</code> result, then returns that
     * result. If all filters return <code>null</code>, this filter will also
     * return <code>null</code>.
     * <p>
     * When the composite filter is closed, the underlying filters will be
     * closed.
     *
     * @param filters the filters.
     * @param <T> the type.
     * @return the composite filter.
     */
    public static <T> Filter<T> or(Collection<Filter<T>> filters) {
        return new MultiFilter<T>(filters) {
            @Override
            public T accept(T item) throws IOException {
                for (Filter<T> filter: filters) {
                    T result = filter.accept(item);
                    if (result != null) {
                        return result;
                    }
                }
                return null;
            }
        };
    }

    /**
     * Gets a filter that returns the original value if
     * <code>object.equals(value)</code>, otherwise returns <code>null</code>.
     *
     * @param object the object to compare.
     * @param <T> the type.
     * @return the filter.
     */
    public static <T> Filter<T> eq(final T object) {
        return new NonMutatingFilter<T>() {
            @Override
            protected boolean accepts(T item) {
                return object.equals(item);
            }
        };
    }

    /**
     * Gets a filter that returns <code>null</code> if
     * <code>object.equals(value)</code>, otherwise returns the original
     * value.
     *
     * @param object the object to compare.
     * @param <T> the type.
     * @return the filter.
     */
    public static <T> Filter<T> ne(final T object) {
        return not(eq(object));
    }

    /**
     * Gets a filter that returns the original value if it is less than
     * the given value, otherwise returns <code>null</code>.
     *
     * @param comparable the object to compare.
     * @param <T> the type.
     * @return the filter.
     */
    public static <T extends Comparable<T>> Filter<T> lt(T comparable) {
        return new ComparableFilter<T>(comparable) {
            @Override
            protected boolean accepts(int result) {
                return result < 0;
            }
        };
    }

    /**
     * Gets a filter that returns the original value if it is less than or
     * equal to the given value, otherwise returns <code>null</code>.
     *
     * @param comparable the object to compare.
     * @param <T> the type.
     * @return the filter.
     */
    public static <T extends Comparable<T>> Filter<T> le(T comparable) {
        return new ComparableFilter<T>(comparable) {
            @Override
            protected boolean accepts(int result) {
                return result <= 0;
            }
        };
    }

    /**
     * Gets a filter that returns the original value if it is greater than
     * the given value, otherwise returns <code>null</code>.
     *
     * @param comparable the object to compare.
     * @param <T> the type.
     * @return the filter.
     */
    public static <T extends Comparable<T>> Filter<T> gt(T comparable) {
        return new ComparableFilter<T>(comparable) {
            @Override
            protected boolean accepts(int result) {
                return result > 0;
            }
        };
    }

    /**
     * Gets a filter that returns the original value if it is greater than
     * or equal to the given value, otherwise returns <code>null</code>.
     *
     * @param comparable the object to compare.
     * @param <T> the type.
     * @return the filter.
     */
    public static <T extends Comparable<T>> Filter<T> ge(T comparable) {
        return new ComparableFilter<T>(comparable) {
            @Override
            protected boolean accepts(int result) {
                return result >= 0;
            }
        };
    }

    private static abstract class NonMutatingFilter<T>
            extends AbstractFilter<T> {

        @Override
        public final T accept(T item) throws IOException {
            if (accepts(item)) {
                return item;
            } else {
                return null;
            }
        }

        protected abstract boolean accepts(T item) throws IOException;
    }

    private static abstract class ComparableFilter<T extends Comparable<T>>
            extends NonMutatingFilter<T> {

        private final T comparable;

        ComparableFilter(T comparable) {
            this.comparable = comparable;
        }

        @Override
        public final boolean accepts(T item) {
            return accepts(item.compareTo(comparable));
        }

        protected abstract boolean accepts(int result);
    }

    private static abstract class MultiFilter<T> implements Filter<T> {

        protected final Collection<Filter<T>> filters;

        MultiFilter(Collection<Filter<T>> filters) {
            this.filters = filters;
        }

        @Override
        public void close() {
            for (Filter<T> filter : filters) {
                filter.close();
            }
        }
    }
}
