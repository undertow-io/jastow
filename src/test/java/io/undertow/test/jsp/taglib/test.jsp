<%@ taglib uri="/bug.tld" prefix="bug" %>
<jsp:useBean id="props" class="java.util.HashMap" />
<html>
  <head>
  </head>
  <body>
  ${bah =  {'a':'b','d':'5'}; props.putAll(bah)}
  ${bug:dummy()}
  ${{'a':'b'}; bug:dummy()}

  ${incr = x->x+1; incr(10)}
  ${bah =  {'a':'b','d':'5'}; props.putAll(bah)}
  props = ${props}
  </body>
</html>
