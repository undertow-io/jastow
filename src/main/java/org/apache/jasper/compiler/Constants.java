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

    // java.beans
    private static final String PACKAGE_JAVA_BEANS = "java.beans";
    static final String BEANS = PACKAGE_JAVA_BEANS + ".Beans";

    // java.io
    private static final String PACKAGE_JAVA_IO = "java.io";
    static final String IO_EXCEPTION = PACKAGE_JAVA_IO + ".IOException";
    static final String WRITER = PACKAGE_JAVA_IO + ".Writer";
    static final String STRING_READER = PACKAGE_JAVA_IO + ".StringReader";
    static final String STRING_WRITER = PACKAGE_JAVA_IO + ".StringWriter";

    // java.lang
    private static final String PACKAGE_JAVA_LANG = "java.lang";
    static final String BOOLEAN = PACKAGE_JAVA_LANG + ".Boolean";
    static final String BYTE = PACKAGE_JAVA_LANG + ".Byte";
    static final String CHARACTER = PACKAGE_JAVA_LANG + ".Character";
    static final String CLASS = PACKAGE_JAVA_LANG + ".Class";
    static final String CLASS_NOT_FOUND_EXCEPTION = PACKAGE_JAVA_LANG + ".ClassNotFoundException";
    static final String DOUBLE = PACKAGE_JAVA_LANG + ".Double";
    static final String EXCEPTION = PACKAGE_JAVA_LANG + ".Exception";
    static final String FLOAT = PACKAGE_JAVA_LANG + ".Float";
    static final String ILLEGAL_STATE_EXCEPTION = PACKAGE_JAVA_LANG + ".IllegalStateException";
    static final String INSTANTIATION_EXCEPTION = PACKAGE_JAVA_LANG + ".InstantiationException";
    static final String INTEGER = PACKAGE_JAVA_LANG + ".Integer";
    static final String LONG = PACKAGE_JAVA_LANG + ".Long";
    static final String OBJECT = PACKAGE_JAVA_LANG + ".Object";
    static final String SHORT = PACKAGE_JAVA_LANG + ".Short";
    static final String STRING = PACKAGE_JAVA_LANG + ".String";
    static final String THROWABLE = PACKAGE_JAVA_LANG + ".Throwable";
    static final String VOID = PACKAGE_JAVA_LANG + ".Void";

    // java.util
    private static final String PACKAGE_JAVA_UTIL = "java.util";
    static final String ARRAY_LIST = PACKAGE_JAVA_UTIL + ".ArrayList";
    static final String HASH_MAP = PACKAGE_JAVA_UTIL + ".HashMap";
    static final String HASH_SET = PACKAGE_JAVA_UTIL + ".HashSet";
    static final String MAP = PACKAGE_JAVA_UTIL + ".Map";
    static final String SET = PACKAGE_JAVA_UTIL + ".Set";

    // jakarta.el
    private static final String PACKAGE_JAVAX_EL = "jakarta.el";
    static final String EXPRESSION_FACTORY = PACKAGE_JAVAX_EL + ".ExpressionFactory";
    static final String METHOD_EXPRESSION = PACKAGE_JAVAX_EL + ".MethodExpression";
    static final String VALUE_EXPRESSION = PACKAGE_JAVAX_EL + ".ValueExpression";
    static final String VARIABLE_MAPPER = PACKAGE_JAVAX_EL + ".VariableMapper";

    // jakarta.servlet
    private static final String PACKAGE_JAVAX_SERVLET = "jakarta.servlet";
    static final String DISPATCHER_TYPE = PACKAGE_JAVAX_SERVLET + ".DispatcherType";
    static final String SERVLET_CONFIG = PACKAGE_JAVAX_SERVLET + ".ServletConfig";
    static final String SERVLET_CONTEXT = PACKAGE_JAVAX_SERVLET + ".ServletContext";
    static final String SERVLET_EXCEPTION = PACKAGE_JAVAX_SERVLET + ".ServletException";
    // jakarta.servlet.http
    private static final String PACKAGE_JAVAX_SERVLET_HTTP = "jakarta.servlet.http";
    static final String HTTP_SERVLET_REQUEST = PACKAGE_JAVAX_SERVLET_HTTP + ".HttpServletRequest";
    static final String HTTP_SERVLET_RESPONSE = PACKAGE_JAVAX_SERVLET_HTTP + ".HttpServletResponse";
    static final String HTTP_SESSION = PACKAGE_JAVAX_SERVLET_HTTP + ".HttpSession";

    // jakarta.servlet.jsp
    private static final String PACKAGE_JAVAX_SERVLET_JSP = "jakarta.servlet.jsp";
    static final String JSP_CONTEXT = PACKAGE_JAVAX_SERVLET_JSP + ".JspContext";
    static final String JSP_EXCEPTION = PACKAGE_JAVAX_SERVLET_JSP + ".JspException";
    static final String JSP_FACTORY = PACKAGE_JAVAX_SERVLET_JSP + ".JspFactory";
    static final String JSP_WRITER = PACKAGE_JAVAX_SERVLET_JSP + ".JspWriter";
    static final String PAGE_CONTEXT = PACKAGE_JAVAX_SERVLET_JSP + ".PageContext";
    static final String SKIP_PAGE_EXCEPTION = PACKAGE_JAVAX_SERVLET_JSP + ".SkipPageException";

    // jakarta.servlet.jsp.tagext
    private static final String PACKAGE_JAVAX_SERVLET_JSP_TAGEXT = "jakarta.servlet.jsp.tagext";
    static final String BODY_CONTENT = PACKAGE_JAVAX_SERVLET_JSP_TAGEXT + ".BodyContent";
    static final String BODY_TAG = PACKAGE_JAVAX_SERVLET_JSP_TAGEXT + ".BodyTag";
    static final String DYNAMIC_ATTRIBUTES = PACKAGE_JAVAX_SERVLET_JSP_TAGEXT + ".DynamicAttributes";
    static final String JSP_FRAGMENT = PACKAGE_JAVAX_SERVLET_JSP_TAGEXT + ".JspFragment";
    static final String JSP_TAG = PACKAGE_JAVAX_SERVLET_JSP_TAGEXT + ".JspTag";
    static final String SIMPLE_TAG = PACKAGE_JAVAX_SERVLET_JSP_TAGEXT + ".SimpleTag";
    static final String SIMPLE_TAG_SUPPORT = PACKAGE_JAVAX_SERVLET_JSP_TAGEXT + ".SimpleTagSupport";
    static final String TAG = PACKAGE_JAVAX_SERVLET_JSP_TAGEXT + ".Tag";
    static final String TAG_ADAPTER = PACKAGE_JAVAX_SERVLET_JSP_TAGEXT + ".TagAdapter";

    // org.apache.jasper.compiler
    private static final String PACKAGE_ORG_APACHE_JASPER_COMPILER = "org.apache.jasper.compiler";
    static final String GENERATOR = PACKAGE_ORG_APACHE_JASPER_COMPILER + ".Generator";

    // org.apache.jasper.el
    private static final String PACKAGE_ORG_APACHE_JASPER_EL = "org.apache.jasper.el";
    static final String EL_CONTEXT_WRAPPER = PACKAGE_ORG_APACHE_JASPER_EL + ".ELContextWrapper";
    static final String JSP_METHOD_EXPRESSION = PACKAGE_ORG_APACHE_JASPER_EL + ".JspMethodExpression";
    static final String JSP_VALUE_EXPRESSION = PACKAGE_ORG_APACHE_JASPER_EL + ".JspValueExpression";

    // org.apache.jasper.runtime
    private static final String PACKAGE_ORG_APACHE_JASPER_RUNTIME = "org.apache.jasper.runtime";
    static final String INSTANCE_MANAGER_FACTORY = PACKAGE_ORG_APACHE_JASPER_RUNTIME + ".InstanceManagerFactory";
    static final String JSP_CONTEXT_WRAPPER = PACKAGE_ORG_APACHE_JASPER_RUNTIME + ".JspContextWrapper";
    static final String JSP_FRAGMENT_HELPER = PACKAGE_ORG_APACHE_JASPER_RUNTIME + ".JspFragmentHelper";
    static final String JSP_RUNTIME_LIBRARY = PACKAGE_ORG_APACHE_JASPER_RUNTIME + ".JspRuntimeLibrary";
    static final String JSP_SOURCE_DEPENDENT = PACKAGE_ORG_APACHE_JASPER_RUNTIME + ".JspSourceDependent";
    static final String JSP_SOURCE_DIRECTIVES = PACKAGE_ORG_APACHE_JASPER_RUNTIME + ".JspSourceDirectives";
    static final String JSP_SOURCE_IMPORTS = PACKAGE_ORG_APACHE_JASPER_RUNTIME + ".JspSourceImports";
    static final String PAGE_CONTEXT_IMPL = PACKAGE_ORG_APACHE_JASPER_RUNTIME + ".PageContextImpl";
    static final String TAG_HANDLER_POOL = PACKAGE_ORG_APACHE_JASPER_RUNTIME + ".TagHandlerPool";

    // org.apache.tomcat
    private static final String PACKAGE_ORG_APACHE_TOMCAT = "org.apache.tomcat";
    static final String INSTANCE_MANAGER = PACKAGE_ORG_APACHE_TOMCAT + ".InstanceManager";

}
