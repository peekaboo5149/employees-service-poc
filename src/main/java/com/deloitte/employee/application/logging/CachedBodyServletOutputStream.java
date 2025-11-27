package com.deloitte.employee.application.logging;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * caching the output stream.
 * @author Tanmay Kumar
 */
class CachedBodyServletOutputStream extends ServletOutputStream {

    private final ByteArrayOutputStream outputStream;

    /**
     * Constructor.
     * @param outputStream output stream.
     */
    public CachedBodyServletOutputStream(ByteArrayOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * Check if the output stream is ready.
     * @return true.
     */
    @Override
    public boolean isReady() {
        return true;
    }

    /**
     * Set the write listener.
     *
     * @param b byte array.
     * @throws IOException io exception.
     * @throws UnsupportedOperationException unsupported operation exception.
     */
    @Override
    public void write(byte[] b) throws IOException {
        outputStream.write(b);
    }

    /**
     * Set the write listener.
     *
     *  @param writeListener write listener.
     *  @throws UnsupportedOperationException unsupported operation exception.
     */
    @Override
    public void setWriteListener(WriteListener writeListener) {
        throw new UnsupportedOperationException();
    }

    /**
     * write the output stream.
     *
     * @param b the data to be written.
     * @param off the start offset in the data.
     * @param len the number of bytes that are written.
     * @throws IOException io exception.
     * @throws UnsupportedOperationException unsupported operation exception.
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        outputStream.write(b, off, len);
    }

    /**
     * write the output stream.
     *
     * @param b   the {@code byte}.
     * @throws UnsupportedOperationException unsupported operation exception.
     * @throws IOException io exception.
     */
    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
    }
}