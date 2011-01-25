package com.github.cwilper.ttff;

import java.io.IOException;

public interface Filter<T> extends Closeable {

    T accept(T item) throws IOException;

}
