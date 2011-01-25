package com.github.cwilper.ttff;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.NoSuchElementException;

public class AbstractSourceTest {

    @Test (expected=IllegalStateException.class)
    public void hasNextIllegalState() throws IOException {
        Source<String> s = new MockSource(true);
        try {
            s.next();
            Assert.fail();
        } catch (IOException e) {
            s.hasNext();
        }
    }

    @Test (expected=NoSuchElementException.class)
    public void typicalIteration() throws IOException {
        Source<String> s = new MockSource(false);
        Assert.assertTrue(s.hasNext());
        Assert.assertEquals("a", s.next());
        Assert.assertFalse(s.hasNext());
        Assert.assertFalse(s.hasNext());
        s.next();
    }

    @Test (expected=NoSuchElementException.class)
    public void peekingIteration() throws IOException {
        Source<String> s = new MockSource(false);
        Assert.assertEquals("a", s.peek());
        Assert.assertTrue(s.hasNext());
        Assert.assertEquals("a", s.next());
        Assert.assertFalse(s.hasNext());
        s.peek();
    }

    class MockSource extends AbstractSource<String> {

        final boolean throwIOE;
        boolean gotOne;

        MockSource(boolean throwIOE) {
            this.throwIOE = throwIOE;
        }

        @Override
        public String computeNext() throws IOException {
            if (throwIOE) {
                throw new IOException();
            } else if (gotOne) {
                return endOfData();
            } else {
                gotOne = true;
                return "a";
            }
        }
    }

}
