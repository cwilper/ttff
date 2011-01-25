package com.github.cwilper.ttff;

public interface Closeable {

    /**
     * Frees any resources created or owned by this object.
     * <p>
     * This method can be called multiple times without negative effects.
     * <p>
     * A call to this method generally signals the end of the useful life
     * of the object, after which subsequent calls to other methods of the
     * object may fail or behave unpredictably.
     * <p>
     * Implementations that need to free resources <strong>MUST</strong> be
     * annotated with <code>@PreDestroy</code> so that the container can
     * trigger cleanup when the instance is about to be removed, if
     * applicable.
     * <p>
     * If an error occurs while closing, the implementation
     * <strong>MAY</strong> log it, but <strong>MUST NOT</strong> throw an
     * exception (checked or unchecked).
     */
    void close();

}
