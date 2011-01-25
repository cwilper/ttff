package com.github.cwilper.ttff;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

public final class Filters {
    Filters() { throw new AssertionError(); }

    public static <T> Filter<T> from(final Sink<T> sink) {
        return new NonMutatingFilter<T>() {
            @Override
            public boolean accepts(T item) throws IOException {
                sink.put(item);
                return true;
            }
        };
    }

    public static <T> Filter<T> bool(final boolean value) {
        return new NonMutatingFilter<T>() {
            @Override
            public boolean accepts(T item) {
                return value;
            }
        };
    }

    public static <T> Filter<T> isa(final Class clazz) {
        return new NonMutatingFilter<T>() {
            @Override
            public boolean accepts(T item) {
                return clazz.isInstance(item);
            }
        };
    }

    public static <T> Filter<T> all(Filter<T>... filters) {
        return all(Arrays.asList(filters));
    }

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

    public static <T> Filter<T> and(Filter<T>... filters) {
        return and(Arrays.asList(filters));
    }

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

    public static <T> Filter<T> or(Filter<T>... filters) {
        return or(Arrays.asList(filters));
    }

    public static <T> Filter<T> or(Collection<Filter<T>> filters) {
        return new MultiFilter<T>(filters) {
            @Override
            public T accept(T item) throws IOException {
                for (Filter<T> filter: filters) {
                    item = filter.accept(item);
                    if (item != null) {
                        return item;
                    }
                }
                return null;
            }
        };
    }

    public static <T> Filter<T> eq(final T object) {
        return new NonMutatingFilter<T>() {
            @Override
            protected boolean accepts(T item) {
                return object.equals(item);
            }
        };
    }

    public static <T> Filter<T> ne(final T object) {
        return not(eq(object));
    }

    public static <T extends Comparable<T>> Filter<T> lt(T comparable) {
        return new ComparableFilter<T>(comparable) {
            @Override
            protected boolean accepts(int result) {
                return result < 0;
            }
        };
    }

    public static <T extends Comparable<T>> Filter<T> le(T comparable) {
        return new ComparableFilter<T>(comparable) {
            @Override
            protected boolean accepts(int result) {
                return result <= 0;
            }
        };
    }

    public static <T extends Comparable<T>> Filter<T> gt(T comparable) {
        return new ComparableFilter<T>(comparable) {
            @Override
            protected boolean accepts(int result) {
                return result > 0;
            }
        };
    }

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
