package com.github.cwilper.ttff;

import java.io.IOException;

public interface Sink<T> extends Closeable {

    void put(T item) throws IOException;

}
