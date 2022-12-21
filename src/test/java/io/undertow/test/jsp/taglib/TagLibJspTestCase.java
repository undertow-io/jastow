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

package io.undertow.test.jsp.taglib;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;

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
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.jasper.deploy.FunctionInfo;
import org.apache.jasper.deploy.JspPropertyGroup;
import org.apache.jasper.deploy.TagAttributeInfo;
import org.apache.jasper.deploy.TagInfo;
import org.apache.jasper.deploy.TagLibraryInfo;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Stuart Douglas
 */
@RunWith(DefaultServer.class)
public class TagLibJspTestCase {

    @BeforeClass
    public static void setup() throws ServletException {

        final PathHandler servletPath = new PathHandler();
        final ServletContainer container = ServletContainer.Factory.newInstance();



        DeploymentInfo builder = new DeploymentInfo()
                .setClassLoader(TagLibJspTestCase.class.getClassLoader())
                .setContextPath("/servletContext")
                .setClassIntrospecter(TestClassIntrospector.INSTANCE)
                .setDeploymentName("servletContext.war")
                .setResourceManager(new TestResourceLoader(TagLibJspTestCase.class))
                .addServlet(JspServletBuilder.createServlet("Default Jsp Servlet", "*.jsp"));

        Map<String, TagLibraryInfo>  tags = new HashMap<>();
        TagLibraryInfo bugTld = new TagLibraryInfo();
        bugTld.setUri("/bug.tld");
        bugTld.setPath("/bug.tld");
        bugTld.setVersion("2.0");
        bugTld.setTlibversion("1.1");
        FunctionInfo functionInfo = new FunctionInfo();
        functionInfo.setName("dummy");
        functionInfo.setFunctionSignature("java.lang.Runtime getRuntime()");
        functionInfo.setFunctionClass("java.lang.Runtime");
        bugTld.addFunctionInfo(functionInfo);
        TagInfo tagInfo = new TagInfo();
        tagInfo.setTagName("out");
        tagInfo.setTagClassName(MyOutTag.class.getName());
        tagInfo.setBodyContent("empty");
        TagAttributeInfo attr = new TagAttributeInfo();
        attr.setName("value");
        attr.setRequired("true");
        attr.setReqTime("true");
        tagInfo.addTagAttributeInfo(attr);
        bugTld.addTagInfo(tagInfo);
        tags.put("/bug.tld", bugTld);

        JspServletBuilder.setupDeployment(builder, new HashMap<String, JspPropertyGroup>(), tags, new HackInstanceManager());

        DeploymentManager manager = container.addDeployment(builder);
        manager.deploy();
        servletPath.addPrefixPath(builder.getContextPath(), manager.start());

        DefaultServer.setRootHandler(servletPath);
    }



    @Test
    public void testSimpleHttpServlet() throws IOException {
        TestHttpClient client = new TestHttpClient();
        try {
            HttpGet get = new HttpGet(DefaultServer.getDefaultServerURL() + "/servletContext/test.jsp");
            get.setConfig(RequestConfig.custom().setConnectTimeout(5* 60 * 1000)
                    .setSocketTimeout(5 * 60 * 1000).build());
            HttpResponse result = client.execute(get);
            Assert.assertEquals(200, result.getStatusLine().getStatusCode());
            final String response = HttpClientUtils.readResponse(result);
            Assert.assertTrue(response.contains("java.lang.Runtime"));
        } finally {
            client.getConnectionManager().shutdown();
        }
    }

    @Test
    public void testScriptletExpression() throws IOException {
        TestHttpClient client = new TestHttpClient();
        try {
            HttpGet get = new HttpGet(DefaultServer.getDefaultServerURL() + "/servletContext/scriptlet-expression.jsp");
            HttpResponse result = client.execute(get);
            Assert.assertEquals(200, result.getStatusLine().getStatusCode());
            final String response = HttpClientUtils.readResponse(result);
            MatcherAssert.assertThat(response, CoreMatchers.containsString("Line1|interpreted true|Line1"));
            MatcherAssert.assertThat(response, CoreMatchers.containsString("Line2|not interpreted <%= Boolean.TRUE %>|Line2"));
            MatcherAssert.assertThat(response, CoreMatchers.containsString("Line3|not interpreted true <%= Boolean.TRUE %>|Line3"));
            MatcherAssert.assertThat(response, CoreMatchers.containsString("Line4|\"function(<%= Boolean.TRUE %>, true})\"|Line4"));
            MatcherAssert.assertThat(response, CoreMatchers.containsString("Line5|interpreted true true|Line5"));
        } finally {
            client.getConnectionManager().shutdown();
        }
    }

}
