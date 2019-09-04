package org.apache.jasper.runtime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.servlet.jsp.tagext.BodyContent;

public class IncludePrintWriter extends PrintWriter {
    public IncludePrintWriter(Writer out) {
        super(out);
    }

    public IncludePrintWriter(Writer out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public IncludePrintWriter(OutputStream out) {
        super(out);
    }

    public IncludePrintWriter(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public IncludePrintWriter(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    public IncludePrintWriter(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(fileName, csn);
    }

    public IncludePrintWriter(File file) throws FileNotFoundException {
        super(file);
    }

    public IncludePrintWriter(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(file, csn);
    }

    @Override
    public void flush() {
        if(!(out instanceof BodyContent)) {
            super.flush();
        }
    }
}
