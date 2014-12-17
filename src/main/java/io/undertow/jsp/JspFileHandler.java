package io.undertow.jsp;

import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.servlet.handlers.ServletRequestContext;

import javax.servlet.ServletRequest;

import static org.apache.jasper.Constants.JSP_FILE;

public class JspFileHandler implements HttpHandler {

    private final String jspFile;
    private final HttpHandler next;

    public JspFileHandler(final String jspFile, final HttpHandler next) {
        this.jspFile = jspFile;
        this.next = next;
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        ServletRequest request = exchange.getAttachment(ServletRequestContext.ATTACHMENT_KEY).getServletRequest();
        Object old = request.getAttribute(JSP_FILE);
        try {
            if(jspFile == null) {
                request.removeAttribute(JSP_FILE);
            } else {
                request.setAttribute(JSP_FILE, jspFile);
            }
            next.handleRequest(exchange);
        } finally {
            request.setAttribute(JSP_FILE, old);
        }
    }

    public static HandlerWrapper jspFileHandlerWrapper(final String jspFile) {
        return new HandlerWrapper() {
            @Override
            public HttpHandler wrap(HttpHandler handler) {
                return new JspFileHandler(jspFile, handler);
            }
        };
    }
}
