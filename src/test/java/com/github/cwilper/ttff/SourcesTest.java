package com.github.cwilper.ttff;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unchecked")
public class SourcesTest {

    @Test (expected=AssertionError.class)
    public void instantiate() throws Exception {
        Sources.class.newInstance();
    }

    @Test
    public void fromArray() throws IOException {
        Source<String> s;

        s = Sources.from("a");
        Assert.assertEquals(1L, Sources.drain(s));

        s = Sources.from("a", "b", "c");
        Assert.assertEquals(3L, Sources.drain(s));
    }

    @Test
    public void fromCollection() throws IOException {
        Source<String> s;
        List<String> c = new ArrayList<String>();

        s = Sources.from(c);
        Assert.assertEquals(0L, Sources.drain(s));

        c.add("a");
        s = Sources.from(c);
        Assert.assertEquals(1L, Sources.drain(s));

        c.add("b");
        c.add("c");
        s = Sources.from(c);
        Assert.assertEquals(3L, Sources.drain(s));
    }

    @Test
    public void fromIterator() throws IOException {
        Source<String> s;
        List<String> c = new ArrayList<String>();

        s = Sources.from(c.iterator());
        Assert.assertEquals(0L, Sources.drain(s));

        c.add("a");
        s = Sources.from(c.iterator());
        Assert.assertEquals(1L, Sources.drain(s));

        c.add("b");
        c.add("c");
        s = Sources.from(c.iterator());
        Assert.assertEquals(3L, Sources.drain(s));
    }

    @Test
    public void empty() throws IOException {
        Source<String> s = Sources.empty();
        Assert.assertFalse(s.hasNext());
    }

    @Test
    public void iterator() {
        Source<String> s;

        s = Sources.empty();
        Assert.assertEquals(0, drain(Sources.iterator(s)));

        s = Sources.from("a");
        Assert.assertEquals(1, drain(Sources.iterator(s)));

        s = Sources.from("a", "b", "c");
        Assert.assertEquals(3, drain(Sources.iterator(s)));
    }

    @Test (expected=UnsupportedOperationException.class)
    public void iteratorRemove() {
        Source<String> s = Sources.empty();
        Sources.iterator(s).remove();
    }

    @Test (expected=RuntimeException.class)
    public void iteratorHasNextIOException() {
        Source<String> s = new AbstractSource<String>() {
            @Override
            public String computeNext() throws IOException {
                throw new IOException();
            }
        };
        Sources.iterator(s).hasNext();
    }

    @Test (expected=RuntimeException.class)
    public void iteratorNextIOException() {
        Source<String> s = new AbstractSource<String>() {
            @Override
            public String computeNext() throws IOException {
                throw new IOException();
            }
        };
        Sources.iterator(s).next();
    }

    @Test
    public void joinArray() throws IOException {
        Source<String> s1 = Sources.empty();
        Source<String> s2 = Sources.from("s2.a");
        Source<String> s3 = Sources.empty();
        Source<String> s4 = Sources.from("s4.a", "s4.b");
        Source<String> s5 = Sources.empty();

        Source<String> s = Sources.join(s1, s2, s3, s4, s5);

        Assert.assertEquals("s2.a", s.next());
        Assert.assertEquals("s4.a", s.next());
        Assert.assertEquals("s4.b", s.next());

        Assert.assertFalse(s.hasNext());
    }

    @Test
    public void joinCollection() throws IOException {
        List<Source<String>> list = new ArrayList<Source<String>>();
        list.add(Sources.from("s2.a"));
        list.add(Sources.from("s4.a", "s4.b"));

        Source<String> s = Sources.join(list);

        Assert.assertEquals("s2.a", s.next());
        Assert.assertEquals("s4.a", s.next());
        Assert.assertEquals("s4.b", s.next());

        Assert.assertFalse(s.hasNext());
    }

    @Test
    public void joinIterator() throws IOException {
        List<Source<String>> list = new ArrayList<Source<String>>();
        list.add(Sources.from("s2.a"));
        list.add(Sources.from("s4.a", "s4.b"));

        Source<String> s = Sources.join(list.iterator());

        Assert.assertEquals("s2.a", s.next());
        Assert.assertEquals("s4.a", s.next());
        Assert.assertEquals("s4.b", s.next());

        Assert.assertFalse(s.hasNext());
    }

    @Test
    public void joinClosing() throws IOException {
        final StringBuffer didClose = new StringBuffer();
        Source<String> s1 = Sources.empty();
        Source<String> s2 = new AbstractSource<String>() {
            @Override
            public String computeNext() {
                return endOfData();
            }

            @Override
            public void close() {
                didClose.append("yes");
            }
        };
        Sources.join(s1, s2).close();
        Assert.assertEquals("yes", didClose.toString());
    }

    @Test
    public void drainNowhere() throws IOException {
        Source<String> s = Sources.from("a", "b", "c");
        Assert.assertEquals(3, Sources.drain(s));
    }

    @Test
    public void drainToSink() throws IOException {
        Source<String> s = Sources.from("a", "b", "c");
        final StringBuffer sb = new StringBuffer();
        Sink<String> sink = new AbstractSink<String>() {
            @Override
            public void put(String item) {
                sb.append(item);
            }
        };
        Assert.assertEquals(3, Sources.drain(s, sink));
        Assert.assertEquals("abc", sb.toString());
    }

    @Test
    public void drainToCollection() throws IOException {
        Source<String> s = Sources.from("a", "b", "c");
        List<String> list = new ArrayList<String>();
        Assert.assertEquals(3, Sources.drain(s, list));
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("a", list.get(0));
        Assert.assertEquals("b", list.get(1));
        Assert.assertEquals("c", list.get(2));
    }

    @Test
    public void filter() throws IOException {
        String[] values = new String[] { "a", "b", "c" };

        Source oSource;
        Source fSource;

        oSource = Sources.from(values);
        fSource = Sources.filter(oSource, Filters.bool(true));
        Assert.assertEquals(3, Sources.drain(fSource));

        oSource = Sources.from(values);
        fSource = Sources.filter(oSource, Filters.bool(false));
        Assert.assertEquals(0, Sources.drain(fSource));

        oSource = Sources.from(values);
        fSource = Sources.filter(oSource, Filters.gt("b"));
        Assert.assertEquals(1, Sources.drain(fSource));
    }

    private static int drain(Iterator<?> iterator) {
        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        return count;
    }
}