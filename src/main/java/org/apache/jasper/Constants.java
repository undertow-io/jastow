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

package org.apache.jasper;


/**
 * Some constants and other global data that are used by the compiler and the runtime.
 *
 * @author Anil K. Vijendran
 * @author Harish Prabandham
 * @author Shawn Bayern
 * @author Mark Roth
 */
public class Constants {
    
    /**
     * The servlet context attribute under which we record the Servlet API version
     * support declared for this webapp.
     */
    public static final String SERVLET_VERSION =
        "org.apache.jasper.SERVLET_VERSION";


    /**
     * The servlet context attribute under which we record the set of
     * JSP property groups (as an object of type HashMap<String, JspPropertyGroup>)
     * for this application.
     */
    public static final String JSP_PROPERTY_GROUPS =
        "org.apache.jasper.JSP_PROPERTY_GROUPS";


    /**
     * The servlet context attribute under which we record the set of
     * JSP tag libraries (as an object of type HashMap<String, TagLibraryInfo>) 
     * for this application.
     */
    public static final String JSP_TAG_LIBRARIES =
        "org.apache.jasper.JSP_TAG_LIBRARIES";


    /**
     * The base class of the generated servlets. 
     */
    public static final String JSP_SERVLET_BASE = 
        System.getProperty("org.apache.jasper.Constants.JSP_SERVLET_BASE", "org.apache.jasper.runtime.HttpJspBase");

    /**
     * _jspService is the name of the method that is called by 
     * HttpJspBase.service(). This is where most of the code generated
     * from JSPs go.
     */
    public static final String SERVICE_METHOD_NAME = 
        System.getProperty("org.apache.jasper.Constants.SERVICE_METHOD_NAME", "_jspService");

    /**
     * Default servlet content type.
     */
    public static final String SERVLET_CONTENT_TYPE = "text/html";

    /**
     * These classes/packages are automatically imported by the
     * generated code. 
     */
    public static final String[] STANDARD_IMPORTS = { 
	"javax.servlet.*", 
	"javax.servlet.http.*", 
	"javax.servlet.jsp.*"
    };

    /**
     * ServletContext attribute for the JSP Servlet options.
     */
    public static final String SERVLET_OPTIONS = 
        System.getProperty("org.apache.jasper.Constants.SERVLET_OPTIONS", "org.apache.catalina.jsp_options");

    /**
     * ServletContext attribute for classpath. This is tomcat specific. 
     * Other servlet engines may choose to support this attribute if they 
     * want to have this JSP engine running on them. 
     */
    public static final String SERVLET_CLASSPATH = 
        System.getProperty("org.apache.jasper.Constants.SERVLET_CLASSPATH", "org.apache.catalina.jsp_classpath");

    /**
     * Request attribute for <code>&lt;jsp-file&gt;</code> element of a
     * servlet definition.  If present on a request, this overrides the
     * value returned by <code>request.getServletPath()</code> to select
     * the JSP page to be executed.
     */
    public static final String JSP_FILE = 
        System.getProperty("org.apache.jasper.Constants.JSP_FILE", "org.apache.catalina.jsp_file");


    /**
     * Default size of the JSP buffer.
     */
    public static final int DEFAULT_BUFFER_SIZE = Integer.parseInt(System.getProperty("org.apache.jasper.Constants.DEFAULT_BUFFER_SIZE", "8192"));

    /**
     * Default size for the tag buffers.
     */
    public static final int DEFAULT_TAG_BUFFER_SIZE = Integer.parseInt(System.getProperty("org.apache.jasper.Constants.DEFAULT_TAG_BUFFER_SIZE", "512"));

    /**
     * Default tag handler pool size.
     */
    public static final int MAX_POOL_SIZE = Integer.parseInt(System.getProperty("org.apache.jasper.Constants.MAX_POOL_SIZE", "4"));

    /**
     * The query parameter that causes the JSP engine to just
     * pregenerated the servlet but not invoke it. 
     */
    public static final String PRECOMPILE = 
        System.getProperty("org.apache.jasper.Constants.PRECOMPILE", "jsp_precompile");

    /**
     * The default package name for compiled jsp pages.
     */
    public static final String JSP_PACKAGE_NAME = 
        System.getProperty("org.apache.jasper.Constants.JSP_PACKAGE_NAME", "org.apache.jsp");

    /**
     * The default package name for tag handlers generated from tag files
     */
    public static final String TAG_FILE_PACKAGE_NAME = 
        System.getProperty("org.apache.jasper.Constants.TAG_FILE_PACKAGE_NAME", "org.apache.jsp.tag");

    /**
     * Servlet context and request attributes that the JSP engine
     * uses. 
     */
    public static final String INC_SERVLET_PATH = "javax.servlet.include.servlet_path";

    /**
     * Default URLs to download the pluging for Netscape and IE.
     */
    public static final String NS_PLUGIN_URL = 
        "http://java.sun.com/products/plugin/";

    public static final String IE_PLUGIN_URL = 
        "http://java.sun.com/products/plugin/1.2.2/jinstall-1_2_2-win.cab#Version=1,2,2,0";

    /**
     * Prefix to use for generated temporary variable names
     */
    public static final String TEMP_VARIABLE_NAME_PREFIX =
        System.getProperty("org.apache.jasper.Constants.TEMP_VARIABLE_NAME_PREFIX", "_jspx_temp");

    /**
     * Has security been turned on?
     */
    public static final boolean IS_SECURITY_ENABLED = 
        (System.getSecurityManager() != null);

    public static final boolean USE_INSTANCE_MANAGER_FOR_TAGS =
        Boolean.valueOf(System.getProperty("org.apache.jasper.Constants.USE_INSTANCE_MANAGER_FOR_TAGS", "true")).booleanValue();

    public static final boolean INJECT_TAGS =
        Boolean.valueOf(System.getProperty("org.apache.jasper.Constants.INJECT_TAGS", "false")).booleanValue();

    /**
     * The name of the path parameter used to pass the session identifier
     * back and forth with the client.
     */
    public static final String SESSION_PARAMETER_NAME = System.getProperty("org.apache.catalina.jsessionid", "jsessionid");

}
