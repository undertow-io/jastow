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

package io.undertow.test.jsp.basic;

import io.undertow.jsp.HackInstanceManager;
import io.undertow.jsp.JspFileHandler;
import io.undertow.jsp.JspServletBuilder;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.test.util.TestClassIntrospector;
import io.undertow.servlet.test.util.TestResourceLoader;
import io.undertow.testutils.DefaultServer;
import io.undertow.testutils.HttpClientUtils;
import io.undertow.testutils.TestHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.apache.jasper.deploy.JspPropertyGroup;
import org.apache.jasper.deploy.TagLibraryInfo;
import org.apache.jasper.servlet.JspServlet;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author Stuart Douglas
 */
@RunWith(DefaultServer.class)
public class SimpleJspTestCase {

    public static final String KEY = "io.undertow.message";

    @BeforeClass
    public static void setup() throws ServletException {

        final PathHandler servletPath = new PathHandler();
        final ServletContainer container = ServletContainer.Factory.newInstance();


        DeploymentInfo builder = new DeploymentInfo()
                .setClassLoader(SimpleJspTestCase.class.getClassLoader())
                .setContextPath("/servletContext")
                .setClassIntrospecter(TestClassIntrospector.INSTANCE)
                .setDeploymentName("servletContext.war")
                .setResourceManager(new TestResourceLoader(SimpleJspTestCase.class))
                .addServlet(JspServletBuilder.createServlet("Default Jsp Servlet", "*.jsp"))
                .addServlet(new ServletInfo("jsp-file", JspServlet.class)
                                .addHandlerChainWrapper(JspFileHandler.jspFileHandlerWrapper("/a.jsp"))
                               .addMapping("/jspFile"));


        JspServletBuilder.setupDeployment(builder, new HashMap<String, JspPropertyGroup>(), new HashMap<String, TagLibraryInfo>(), new HackInstanceManager());

        DeploymentManager manager = container.addDeployment(builder);
        manager.deploy();
        servletPath.addPrefixPath(builder.getContextPath(), manager.start());

        DefaultServer.setRootHandler(servletPath);
        System.setProperty(KEY, "Hello JSP!");
    }

    @AfterClass
    public static void after() {
        System.getProperties().remove(KEY);
    }

    @Test
    public void testSimpleHttpServlet() throws IOException {
        TestHttpClient client = new TestHttpClient();
        try {
            HttpGet get = new HttpGet(DefaultServer.getDefaultServerURL() + "/servletContext/a.jsp");
            HttpResponse result = client.execute(get);
            Assert.assertEquals(200, result.getStatusLine().getStatusCode());
            final String response = HttpClientUtils.readResponse(result);
            Assert.assertEquals("<HTML><BODY> Message:Hello JSP!</BODY></HTML>", response);
        } finally {
            client.getConnectionManager().shutdown();
        }
    }

    @Test
    public void testServletJSPFile() throws IOException {
        TestHttpClient client = new TestHttpClient();
        try {
            HttpGet get = new HttpGet(DefaultServer.getDefaultServerURL() + "/servletContext/jspFile");
            HttpResponse result = client.execute(get);
            Assert.assertEquals(200, result.getStatusLine().getStatusCode());
            final String response = HttpClientUtils.readResponse(result);
            Assert.assertEquals("<HTML><BODY> Message:Hello JSP!</BODY></HTML>", response);
        } finally {
            client.getConnectionManager().shutdown();
        }
    }

    /**
     * Tests that accessing a JSP via HTTP methods other than GET/POST/HEAD isn't allowed
     *
     * @throws Exception
     */
    @Test
    public void testUnAllowedHTTPMethods() throws Exception {
        final TestHttpClient client = new TestHttpClient();
        try {
            // invoke a DELETE on a JSP whose code sets the HTTP response code to 200
            final HttpDelete httpDelete = new HttpDelete(DefaultServer.getDefaultServerURL() + "/servletContext/200.jsp");
            final HttpResponse deleteResponse = client.execute(httpDelete);
            // we expect the return code to be 405
            Assert.assertEquals("Unexpected response code from " + httpDelete.getMethod() + " request to a JSP", 405, deleteResponse.getStatusLine().getStatusCode());
            EntityUtils.consumeQuietly(deleteResponse.getEntity());

            // invoke a PUT on a JSP whose code sets the HTTP response code to 200
            final HttpPut httpPut = new HttpPut(DefaultServer.getDefaultServerURL() + "/servletContext/200.jsp");
            final HttpResponse putResponse = client.execute(httpPut);
            // we expect the return code to be 405
            Assert.assertEquals("Unexpected response code from " + httpPut.getMethod() + " request to a JSP", 405, putResponse.getStatusLine().getStatusCode());

        } finally {
            client.getConnectionManager().shutdown();
        }
    }

    @Test
    public void testInnerClassesJsp() throws IOException {
        TestHttpClient client = new TestHttpClient();
        try {
            HttpGet get = new HttpGet(DefaultServer.getDefaultServerURL() + "/servletContext/inner-classes.jsp");
            HttpResponse result = client.execute(get);
            Assert.assertEquals(200, result.getStatusLine().getStatusCode());
            final String response = HttpClientUtils.readResponse(result);
            MatcherAssert.assertThat(response, CoreMatchers.containsString("555-2368<br/>"));
        } finally {
            client.getConnectionManager().shutdown();
        }
    }

    @Test
    public void testURLCharacterEncoding() throws IOException {
        TestHttpClient client = new TestHttpClient();
        try {
            HttpGet get = new HttpGet(DefaultServer.getDefaultServerURL() + "/servletContext/include-parent.jsp");
            HttpResponse result = client.execute(get);
            Assert.assertEquals(200, result.getStatusLine().getStatusCode());
            final String response = HttpClientUtils.readResponse(result);
            MatcherAssert.assertThat(response, CoreMatchers.containsString("euro=€"));
            MatcherAssert.assertThat(response, CoreMatchers.containsString("acutes=áéíóú"));
        } finally {
            client.getConnectionManager().shutdown();
        }
    }
}
