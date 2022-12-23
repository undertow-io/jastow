<%--
    JBoss, Home of Professional Open Source.
    Copyright 2012 Red Hat, Inc., and individual contributors
    as indicated by the @author tags.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
--%>
<%@ taglib uri="/bug.tld" prefix="bug" %>
<html>
    <head>
        <title>Scriptlet expression test</title>
    </head>

    <body>
        <ul>
            <li>Line1|<bug:out value="<%= \"interpreted \" + Boolean.TRUE %>"/>|Line1</li>
            <li>Line2|<bug:out value="not interpreted <%= Boolean.TRUE %>"/>|Line2</li>
            <li>Line3|<bug:out value="not interpreted ${Boolean.TRUE} <%= Boolean.TRUE %>"/>|Line3</li>
            <li>Line4|<bug:out value="\"function(<%= Boolean.TRUE %>, ${Boolean.TRUE}})\""/>|Line4</li>
            <li>Line5|<bug:out value="interpreted ${Boolean.TRUE} ${Boolean.TRUE}"/>|Line5</li>
        </ul>
    </table>
</body>
