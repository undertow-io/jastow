<%@ page import="java.io.IOException" %>
<%@ page import="io.undertow.test.jsp.lambda.AnswerToEverythingComputation" %>


<%
    try {
        response.getWriter().write("Answer to everything is: " + ((AnswerToEverythingComputation) () -> 42).compute());
    } catch (IOException ex) {
        throw new RuntimeException(ex);
    }
%>
