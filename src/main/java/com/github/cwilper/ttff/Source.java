package com.github.cwilper.ttff;

import java.io.IOException;

public interface Source<T> extends Closeable {

    boolean hasNext() throws IOException;

    T next() throws IOException;

    T peek() throws IOException;

}
