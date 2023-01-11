<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<html>
    <body>
        <jsp:include page="/include.jsp">
            <jsp:param name="euro" value="€" />
            <jsp:param name="acutes" value="áéíóú" />
        </jsp:include>
    </body>
</html>