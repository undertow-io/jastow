/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.undertow.test.jsp.expression;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.servlet.ServletException;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.Expression;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.FunctionMapper;
import javax.servlet.jsp.el.VariableResolver;

import io.undertow.jsp.HackInstanceManager;
import io.undertow.jsp.JspServletBuilder;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.test.util.TestClassIntrospector;
import io.undertow.servlet.test.util.TestResourceLoader;
import io.undertow.testutils.DefaultServer;
import io.undertow.testutils.HttpClientUtils;
import io.undertow.testutils.TestHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.jasper.deploy.JspPropertyGroup;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Tomaz Cerar
 */
@RunWith(DefaultServer.class)
public class ExpressionJspTestCase {

    @BeforeClass
    public static void setup() throws ServletException {

        final PathHandler servletPath = new PathHandler();
        final ServletContainer container = ServletContainer.Factory.newInstance();


        DeploymentInfo builder = new DeploymentInfo()
                .setClassLoader(ExpressionJspTestCase.class.getClassLoader())
                .setContextPath("/servletContext")
                .setClassIntrospecter(TestClassIntrospector.INSTANCE)
                .setDeploymentName("servletContext.war")
                .setResourceManager(new TestResourceLoader(ExpressionJspTestCase.class))
                .addServlet(JspServletBuilder.createServlet("Default Jsp Servlet", "*.jsp").addMapping("*.jspx"));


        JspServletBuilder.setupDeployment(builder, new HashMap<String, JspPropertyGroup>(), new HashMap<>(), new HackInstanceManager());

        DeploymentManager manager = container.addDeployment(builder);
        manager.deploy();
        servletPath.addPrefixPath(builder.getContextPath(), manager.start());

        DefaultServer.setRootHandler(servletPath);
    }


    @Test
    public void testLegacyExpressions() throws IOException {
        TestHttpClient client = new TestHttpClient();
        try {
            HttpGet get = new HttpGet(DefaultServer.getDefaultServerURL() + "/servletContext/expression.jsp");
            HttpResponse result = client.execute(get);
            Assert.assertEquals(200, result.getStatusLine().getStatusCode());
            final String response = HttpClientUtils.readResponse(result).trim();
            Assert.assertEquals("test failed, full response:\n" + response, "Test PASSED", response);
        } finally {
            client.getConnectionManager().shutdown();
        }
    }
    @Test
    public void testPostInitEL() throws IOException {
        TestHttpClient client = new TestHttpClient();
        try {
            HttpGet get = new HttpGet(DefaultServer.getDefaultServerURL() + "/servletContext/IIllegal.jsp");
            HttpResponse result = client.execute(get);
            Assert.assertEquals(200, result.getStatusLine().getStatusCode());
            final String response = HttpClientUtils.readResponse(result).trim();
            Assert.assertEquals("test failed, full response:\n" + response, "Test PASSED", response);
        } finally {
            client.getConnectionManager().shutdown();
        }
    }

    @Test
    public void testEncoding() throws IOException {
        TestHttpClient client = new TestHttpClient();
        try {
            HttpGet get = new HttpGet(DefaultServer.getDefaultServerURL() + "/servletContext/I18NPageEncTest.jspx");
            HttpResponse result = client.execute(get);
            Assert.assertEquals(200, result.getStatusLine().getStatusCode());
            final String response = HttpClientUtils.readResponse(result).trim();
            Assert.assertTrue("test failed, full response:\n" + response, response.contains("Test PASSED"));
        } finally {
            client.getConnectionManager().shutdown();
        }
    }

    public static void evaluate(JspWriter out, PageContext pc, String qualifiedMethodExpression, String unqualifiedMethodExpression, String variableExpression) throws Exception {
        assert pc != null;
        TSFunctionMapper mapper = new TSFunctionMapper();
        ExpressionEvaluator eval = pc.getExpressionEvaluator();
        VariableResolver resolver = pc.getVariableResolver();
        assert eval != null : "Unable to obtain ExpressionEvaluator";
        Expression expr = eval.parseExpression(qualifiedMethodExpression, java.lang.String.class, mapper);
        assert expr != null;
        String result = (String) expr.evaluate(resolver);
        if (result != null) {
            if (result.equals("string")) {
                Expression expr2 = eval.parseExpression(variableExpression, javax.servlet.jsp.PageContext.class, null);
                if (expr2 != null) {
                    PageContext pageContext = (PageContext) expr2.evaluate(resolver);
                    if (pageContext != pc) {
                        out.println("Test FAILED.  Resolution didn't return expected value.");
                        out.println("PageContext returned is not the same instance as expected.");
                    }
                    Expression expr3 = eval.parseExpression(unqualifiedMethodExpression, java.lang.String.class, mapper);
                    if (expr3 != null) {
                        result = (String) expr3.evaluate(resolver);
                        if (result != null) {
                            if (result.equals("string")) {
                                out.println("Test PASSED");
                            } else {
                                out.println("Test FAILED. (l3) Expression evaluation returned unexpected value.");
                                out.println("Expected 'string', received '" + result + "'");
                            }
                        } else {
                            out.println("Test FAILED.  (l3) Expression evaluation returned null.");
                        }
                    } else {
                        out.println("Test FAILED. (l3) ExpressionEvaluator.parseExpression" +
                                " returned null.");
                    }
                } else {
                    out.println("Test FAILED. (l2) ExpressionEvaluator returned null.");
                }
            } else {
                out.println("Test FAILED.  (l1) Expression evaluation returned unexpected result.");
                out.println("Expected 'string', Received '" + result + "'");
            }
        } else {
            out.println("Test FAILED. (l1) Expression evaluation returned null.");
        }


    }

    public static void testIIllegalState(PageContext pageContext, JspWriter out) throws Exception {
        assert pageContext != null;
        ELContext elContext = pageContext.getELContext();
        assert elContext != null;
        JspApplicationContext jaContext = JspFactory.getDefaultFactory().getJspApplicationContext(
                pageContext.getServletContext());
        assert jaContext != null;

        try {
            jaContext.addELResolver(new CompositeELResolver());
            out.println("addELResolver call succeeded. Test FAILED.");
        } catch (IllegalStateException ise) {
            out.println("Test PASSED");
        }
    }


    public static class JspFunctions {

        public static String lowerCase(String value) {
            return value.toLowerCase();
        }

        public static String upperCase(String value) {
            return value.toUpperCase();
        }
    }

    /**
     * Simple Function mapper.
     */

    public static class TSFunctionMapper implements FunctionMapper {
        public Method resolveFunction(String prefix, String localName) {
            if (prefix != null || localName != null) {
                try {
                    return JspFunctions.class.getMethod(localName, String.class);
                } catch (Throwable t) {
                    return null;
                }
            }
            return null;
        }
    }
}
