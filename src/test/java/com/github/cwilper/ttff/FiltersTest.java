package com.github.cwilper.ttff;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class FiltersTest {

    private final static Filter<String> alwaysTrue = Filters.bool(true);
    private final static Filter<String> alwaysFalse = Filters.bool(false);

    @Test (expected=AssertionError.class)
    public void instantiate() throws Exception {
        Filters.class.newInstance();
    }

    @Test
    public void fromSink() throws IOException {
        final StringBuffer sb = new StringBuffer();
        Sink<String> sink = new AbstractSink<String>() {
            @Override
            public void put(String item) {
                sb.append(item);
            }
        };
        Filter<String> filter = Filters.from(sink);
        Assert.assertEquals("a", filter.accept("a"));
        Assert.assertEquals("a", sb.toString());
    }

    @Test
    public void bool() throws IOException {
        Assert.assertEquals("a", alwaysTrue.accept("a"));
        Assert.assertNull(alwaysFalse.accept("a"));
    }

    @Test
    public void isa() throws IOException {
        Filter<Object> isaString = Filters.isa(String.class);
        Assert.assertEquals("a", isaString.accept("a"));
        Assert.assertNull(isaString.accept(1));
    }

    @Test
    public void all() throws IOException {
        List<Filter<String>> list = new ArrayList<Filter<String>>();

        checkTrue(Filters.all(list));

        list.add(alwaysTrue);
        checkTrue(Filters.all(list));
        checkTrue(Filters.all(alwaysTrue));

        list.add(alwaysFalse);
        checkTrue(Filters.all(list));
        checkTrue(Filters.all(alwaysTrue, alwaysFalse));

        list.clear();
        list.add(alwaysFalse);
        checkTrue(Filters.all(list));
        checkTrue(Filters.all(alwaysFalse));

        Filters.all(alwaysFalse).close();
    }

    @Test
    public void and() throws IOException {
        List<Filter<String>> list = new ArrayList<Filter<String>>();

        checkTrue(Filters.and(list));

        list.add(alwaysTrue);
        checkTrue(Filters.and(list));
        checkTrue(Filters.and(alwaysTrue));

        list.add(alwaysFalse);
        checkFalse(Filters.and(list));
        checkFalse(Filters.and(alwaysTrue, alwaysFalse));

        list.clear();
        list.add(alwaysFalse);
        checkFalse(Filters.and(list));
        checkFalse(Filters.and(alwaysFalse));

        Filters.and(alwaysFalse).close();
    }

    @Test
    public void not() throws IOException {
        checkTrue(Filters.not(alwaysFalse));
        checkFalse(Filters.not(alwaysTrue));
        Filters.not(alwaysTrue).close();
    }

    @Test
    public void or() throws IOException {
        List<Filter<String>> list = new ArrayList<Filter<String>>();

        checkFalse(Filters.or(list));

        list.add(alwaysTrue);
        checkTrue(Filters.or(list));
        checkTrue(Filters.or(alwaysTrue));

        list.add(alwaysFalse);
        checkTrue(Filters.or(list));
        checkTrue(Filters.or(alwaysTrue, alwaysFalse));

        list.clear();
        list.add(alwaysFalse);
        checkFalse(Filters.or(list));
        checkFalse(Filters.or(alwaysFalse));

        Filters.or(alwaysFalse).close();
    }

    @Test
    public void eq() throws IOException {
        Filter<String> filter = Filters.eq("a");
        Assert.assertEquals("a", filter.accept("a"));
        Assert.assertNull(filter.accept("b"));
    }

    @Test
    public void ne() throws IOException {
        Filter<String> filter = Filters.ne("a");
        Assert.assertEquals("b", filter.accept("b"));
        Assert.assertNull(filter.accept("a"));
    }

    @Test
    public void lt() throws IOException {
        Filter<Integer> filter = Filters.lt(5);
        Assert.assertEquals(new Integer(4), filter.accept(4));
        Assert.assertNull(filter.accept(5));
        Assert.assertNull(filter.accept(6));
    }

    @Test
    public void le() throws IOException {
        Filter<Integer> filter = Filters.le(5);
        Assert.assertEquals(new Integer(4), filter.accept(4));
        Assert.assertEquals(new Integer(5), filter.accept(5));
        Assert.assertNull(filter.accept(6));
    }

    @Test
    public void gt() throws IOException {
        Filter<Integer> filter = Filters.gt(5);
        Assert.assertEquals(new Integer(6), filter.accept(6));
        Assert.assertNull(filter.accept(5));
        Assert.assertNull(filter.accept(4));
    }

    @Test
    public void ge() throws IOException {
        Filter<Integer> filter = Filters.ge(5);
        Assert.assertEquals(new Integer(6), filter.accept(new Integer(6)));
        Assert.assertEquals(new Integer(5), filter.accept(new Integer(5)));
        Assert.assertNull(filter.accept(new Integer(4)));
    }

    private void checkTrue(Filter<String> filter) throws IOException {
        Assert.assertEquals("a", filter.accept("a"));
    }

    private void checkFalse(Filter<String> filter) throws IOException {
        Assert.assertNull(filter.accept("a"));
    }

}
