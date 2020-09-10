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

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
final class Constants {

    private Constants() {
        // forbidden instantiation
    }

    // javax.el
    private static final String PACKAGE_JAVAX_EL = "javax.el";
    static final String EXPRESSION_FACTORY = PACKAGE_JAVAX_EL + ".ExpressionFactory";
    static final String METHOD_EXPRESSION = PACKAGE_JAVAX_EL + ".MethodExpression";
    static final String VALUE_EXPRESSION = PACKAGE_JAVAX_EL + ".ValueExpression";
    static final String VARIABLE_MAPPER = PACKAGE_JAVAX_EL + ".VariableMapper";

    // javax.servlet
    private static final String PACKAGE_JAVAX_SERVLET = "javax.servlet";
    static final String DISPATCHER_TYPE = PACKAGE_JAVAX_SERVLET + ".DispatcherType";
    static final String SERVLET_CONFIG = PACKAGE_JAVAX_SERVLET + ".ServletConfig";
    static final String SERVLET_CONTEXT = PACKAGE_JAVAX_SERVLET + ".ServletContext";
    static final String SERVLET_EXCEPTION = PACKAGE_JAVAX_SERVLET + ".ServletException";
    static final String SINGLE_THREAD_MODEL = PACKAGE_JAVAX_SERVLET + ".SingleThreadModel";

    // javax.servlet.http
    private static final String PACKAGE_JAVAX_SERVLET_HTTP = "javax.servlet.http";
    static final String HTTP_SERVLET_REQUEST = PACKAGE_JAVAX_SERVLET_HTTP + ".HttpServletRequest";
    static final String HTTP_SERVLET_RESPONSE = PACKAGE_JAVAX_SERVLET_HTTP + ".HttpServletResponse";
    static final String HTTP_SESSION = PACKAGE_JAVAX_SERVLET_HTTP + ".HttpSession";

    // javax.servlet.jsp
    private static final String PACKAGE_JAVAX_SERVLET_JSP = "javax.servlet.jsp";
    static final String JSP_CONTEXT = PACKAGE_JAVAX_SERVLET_JSP + ".JspContext";
    static final String JSP_EXCEPTION = PACKAGE_JAVAX_SERVLET_JSP + ".JspException";
    static final String JSP_FACTORY = PACKAGE_JAVAX_SERVLET_JSP + ".JspFactory";
    static final String JSP_WRITER = PACKAGE_JAVAX_SERVLET_JSP + ".JspWriter";
    static final String PAGE_CONTEXT = PACKAGE_JAVAX_SERVLET_JSP + ".PageContext";
    static final String SKIP_PAGE_EXCEPTION = PACKAGE_JAVAX_SERVLET_JSP + ".SkipPageException";

    // javax.servlet.jsp.tagext
    private static final String PACKAGE_JAVAX_SERVLET_JSP_TAGEXT = "javax.servlet.jsp.tagext";
    static final String BODY_CONTENT = PACKAGE_JAVAX_SERVLET_JSP_TAGEXT + ".BodyContent";
    static final String BODY_TAG = PACKAGE_JAVAX_SERVLET_JSP_TAGEXT + ".BodyTag";
    static final String DYNAMIC_ATTRIBUTES = PACKAGE_JAVAX_SERVLET_JSP_TAGEXT + ".DynamicAttributes";
    static final String JSP_FRAGMENT = PACKAGE_JAVAX_SERVLET_JSP_TAGEXT + ".JspFragment";
    static final String JSP_TAG = PACKAGE_JAVAX_SERVLET_JSP_TAGEXT + ".JspTag";
    static final String SIMPLE_TAG = PACKAGE_JAVAX_SERVLET_JSP_TAGEXT + ".SimpleTag";
    static final String SIMPLE_TAG_SUPPORT = PACKAGE_JAVAX_SERVLET_JSP_TAGEXT + ".SimpleTagSupport";
    static final String TAG = PACKAGE_JAVAX_SERVLET_JSP_TAGEXT + ".Tag";
    static final String TAG_ADAPTER = PACKAGE_JAVAX_SERVLET_JSP_TAGEXT + ".TagAdapter";

}
