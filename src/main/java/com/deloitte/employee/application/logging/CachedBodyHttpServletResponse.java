package com.deloitte.employee.application.logging;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

/**
 * Caching the http servlet response.
 * @author Tanmay Kumar
 */
class CachedBodyHttpServletResponse extends HttpServletResponseWrapper {
    private final ByteArrayOutputStream cachedBody;
    private final PrintWriter printWriter;

    /**
     * Constructor.
     * @param response input stream.
     */
    public CachedBodyHttpServletResponse(HttpServletResponse response) {
        super(response);
        cachedBody = new ByteArrayOutputStream();
        printWriter = new PrintWriter(cachedBody);
    }

    /**
     * Get the output stream.
     * @see HttpServletResponse#getOutputStream()
     * @see CachedBodyServletOutputStream
     * @see ByteArrayOutputStream
     * @return ServletOutputStream
     */
    @Override
    public ServletOutputStream getOutputStream() {
        return new CachedBodyServletOutputStream(cachedBody);
    }

    /**
     * Get the writer.
     * @see HttpServletResponse#getWriter()
     * @return PrintWriter
     * @see PrintWriter
     */
    @Override
    public PrintWriter getWriter() {
        return printWriter;
    }

    /**
     * get the body.
     * @see ByteArrayOutputStream#toByteArray()
     * @return byte[]
     */
    public byte[] getBody() {
        return cachedBody.toByteArray();
    }


}
