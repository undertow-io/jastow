/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jasper.compiler;

import java.net.MalformedURLException;

import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.xml.sax.SAXException;

/**
 * Class responsible for dispatching JSP parse and javac compilation errors
 * to the configured error handler.
 *
 * This class is also responsible for localizing any error codes before they
 * are passed on to the configured error handler.
 * 
 * In the case of a Java compilation error, the compiler error message is
 * parsed into an array of JavacErrorDetail instances, which is passed on to 
 * the configured error handler.
 *
 * @author Jan Luehe
 * @author Kin-man Chung
 */
public class ErrorDispatcher {

    // Custom error handler
    private ErrorHandler errHandler;

    // Indicates whether the compilation was initiated by JspServlet or JspC
    private boolean jspcMode = false;


    /*
     * Constructor.
     *
     * @param jspcMode true if compilation has been initiated by JspC, false
     * otherwise
     */
    public ErrorDispatcher(boolean jspcMode) {
	// XXX check web.xml for custom error handler
	errHandler = new DefaultErrorHandler();
        this.jspcMode = jspcMode;
    }

    /*
     * Dispatches the given javac compilation errors to the configured error
     * handler.
     *
     * @param javacErrors Array of javac compilation errors
     */
    public void javacError(JavacErrorDetail[] javacErrors)
            throws JasperException {

        errHandler.javacError(javacErrors);
    }


    /*
     * Dispatches the given compilation error report and exception to the
     * configured error handler.
     *
     * @param errorReport Compilation error report
     * @param e Compilation exception
     */
    public void javacError(String errorReport, Exception e)
                throws JasperException {

        errHandler.javacError(errorReport, e);
    }


    public void jspError(String errorMessage) throws JasperException {
        jspError(null, errorMessage, null);
    }

    public void jspError(String errorMessage, Exception e) throws JasperException {
        jspError(null, errorMessage, e);
    }

    public void jspError(Mark where, String errorMessage) throws JasperException {
        jspError(where, errorMessage, null);
    }

    /**
     * Dispatches the given JSP parse error to the configured error handler.
     *
     * The given error code is localized. If it is not found in the
     * resource bundle for localized error messages, it is used as the error
     * message.
     *
     * @param where Error location
     * @param errorMessage The error message
     * @param args Arguments for parametric replacement
     * @param e Parsing exception
     */
    public void jspError(Mark where, String errorMessage, Exception e) throws JasperException {
        String file = null;
        int line = -1;
        int column = -1;
        boolean hasLocation = false;

        // Get error location
        if (where != null) {
            if (jspcMode) {
                // Get the full URL of the resource that caused the error
                try {
                    file = where.getURL().toString();
                } catch (MalformedURLException me) {
                    // Fallback to using context-relative path
                    file = where.getFile();
                }
            } else {
                // Get the context-relative resource path, so as to not
                // disclose any local filesystem details
                file = where.getFile();
            }
            line = where.getLineNumber();
            column = where.getColumnNumber();
            hasLocation = true;
        }

        // Get nested exception
        Exception nestedEx = e;
        if ((e instanceof SAXException)
                && (((SAXException) e).getException() != null)) {
            nestedEx = ((SAXException) e).getException();
        }

        if (hasLocation) {
            errHandler.jspError(file, line, column, errorMessage, nestedEx);
        } else {
            errHandler.jspError(errorMessage, nestedEx);
        }
    }

    /**
     * @param fname
     * @param page
     * @param errMsgBuf
     * @param lineNum
     * @return JavacErrorDetail The error details
     * @throws JasperException
     */
    public static JavacErrorDetail createJavacError(String fname,
            Node.Nodes page, StringBuilder errMsgBuf, int lineNum)
                    throws JasperException {
        return createJavacError(fname, page, errMsgBuf, lineNum, null);
    }
    
    
    /**
     * @param fname
     * @param page
     * @param errMsgBuf
     * @param lineNum
     * @param ctxt
     * @return JavacErrorDetail The error details
     * @throws JasperException
     */
    public static JavacErrorDetail createJavacError(String fname,
            Node.Nodes page, StringBuilder errMsgBuf, int lineNum,
            JspCompilationContext ctxt) throws JasperException {
        JavacErrorDetail javacError;
        // Attempt to map javac error line number to line in JSP page
        ErrorVisitor errVisitor = new ErrorVisitor(lineNum);
        page.visit(errVisitor);
        Node errNode = errVisitor.getJspSourceNode();
        if ((errNode != null) && (errNode.getStart() != null)) {
            // If this is a scriplet node then there is a one to one mapping
            // between JSP lines and Java lines
            if (errVisitor.getJspSourceNode() instanceof Node.Scriptlet) {
                javacError = new JavacErrorDetail(
                        fname,
                        lineNum,
                        errNode.getStart().getFile(),
                        errNode.getStart().getLineNumber() + lineNum -
                        errVisitor.getJspSourceNode().getBeginJavaLine(),
                        errMsgBuf,
                        ctxt);
            } else {
                javacError = new JavacErrorDetail(
                        fname,
                        lineNum,
                        errNode.getStart().getFile(),
                        errNode.getStart().getLineNumber(),
                        errMsgBuf,
                        ctxt);
            }
        } else {
            /*
             * javac error line number cannot be mapped to JSP page
             * line number. For example, this is the case if a 
             * scriptlet is missing a closing brace, which causes
             * havoc with the try-catch-finally block that the code
             * generator places around all generated code: As a result
             * of this, the javac error line numbers will be outside
             * the range of begin and end java line numbers that were
             * generated for the scriptlet, and therefore cannot be
             * mapped to the start line number of the scriptlet in the
             * JSP page.
             * Include just the javac error info in the error detail.
             */
            javacError = new JavacErrorDetail(
                    fname,
                    lineNum,
                    errMsgBuf);
        }
        return javacError;
    }


    /*
     * Visitor responsible for mapping a line number in the generated servlet
     * source code to the corresponding JSP node.
     */
    static class ErrorVisitor extends Node.Visitor {

	// Java source line number to be mapped
	private int lineNum;

	/*
	 * JSP node whose Java source code range in the generated servlet
	 * contains the Java source line number to be mapped
	 */
	Node found;

	/*
	 * Constructor.
	 *
	 * @param lineNum Source line number in the generated servlet code
	 */
	public ErrorVisitor(int lineNum) {
	    this.lineNum = lineNum;
	}

	public void doVisit(Node n) throws JasperException {
	    if ((lineNum >= n.getBeginJavaLine())
		    && (lineNum < n.getEndJavaLine())) {
		found = n;
	    }
        }

	/*
	 * Gets the JSP node to which the source line number in the generated
	 * servlet code was mapped.
	 *
	 * @return JSP node to which the source line number in the generated
	 * servlet code was mapped
	 */
	public Node getJspSourceNode() {
	    return found;
	}
    }
}
