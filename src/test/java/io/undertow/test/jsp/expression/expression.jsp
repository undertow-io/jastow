<%@ page import="io.undertow.test.jsp.expression.ExpressionJspTestCase"%>
<%@ page contentType="text/plain" %><%!
    private static final String PAGECONTEXT_ATTR = "some.property.with.pageContext";
    private static final String QUAL_METHOD_EXPR = "${ns:lowerCase('STRING')}";
    private static final String UNQUAL_METHOD_EXPR = "${lowerCase('STRING')}";
    private static final String VARIABLE_EXPR = "${requestScope['some.property.with.pageContext']}";
%><%
    request.setAttribute(PAGECONTEXT_ATTR, pageContext);
    ExpressionJspTestCase.evaluate(out, (PageContext) request.getAttribute(PAGECONTEXT_ATTR), QUAL_METHOD_EXPR, UNQUAL_METHOD_EXPR, VARIABLE_EXPR);
%>