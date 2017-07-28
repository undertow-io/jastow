package io.undertow.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.xml.ws.soap.Addressing;

import io.undertow.jsp.HackInstanceManager;
import io.undertow.jsp.JspServletBuilder;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.test.util.TestClassIntrospector;
import io.undertow.testutils.DefaultServer;
import io.undertow.testutils.HttpClientUtils;
import io.undertow.testutils.TestHttpClient;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.jasper.deploy.JspPropertyGroup;
import org.apache.tomcat.util.buf.ByteChunk;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * @author Tomaz Cerar (c) 2017 Red Hat Inc.
 */
@RunWith(DefaultServer.class)
public class TomcatBaseTest {

    @BeforeClass
    public static void setUp() throws ServletException {

        final PathHandler servletPath = new PathHandler();
        final ServletContainer container = ServletContainer.Factory.newInstance();


        DeploymentInfo builder = new DeploymentInfo()
                .setClassLoader(TomcatBaseTest.class.getClassLoader())
                .setContextPath("/test")
                .setClassIntrospecter(TestClassIntrospector.INSTANCE)
                .setDeploymentName("test.war")
                .setResourceManager(new ClassPathResourceManager(Thread.currentThread().getContextClassLoader(), "webapp"))
                .addServlet(JspServletBuilder.createServlet("Default Jsp Servlet", "*.jsp"));
        JspServletBuilder.setupDeployment(builder, getGroupDescriptors(), new HashMap<>(), new HackInstanceManager());

        DeploymentManager manager = container.addDeployment(builder);
        manager.deploy();
        servletPath.addPrefixPath(builder.getContextPath(), manager.start());

        DefaultServer.setRootHandler(servletPath);

    }

    protected void getTomcatInstanceTestWebapp(boolean a, boolean b) {

    }

    protected int getPort() {
        return DefaultServer.getHostPort();
    }

    protected ByteChunk getUrl(String url) throws IOException {
        try (TestHttpClient client = new TestHttpClient()) {
            HttpGet get = new HttpGet(url);
            HttpResponse result = client.execute(get);
            //Assert.assertEquals(200, result.getStatusLine().getStatusCode());
            final String response = HttpClientUtils.readResponse(result);
            return new ByteChunk(response);
        }
    }

    protected int getUrl(String url, ByteChunk buffer, Map<String, List<String>> headers) throws IOException {
        try (TestHttpClient client = new TestHttpClient()) {
            HttpGet get = new HttpGet(url);
            HttpResponse result = client.execute(get);
            buffer.write(HttpClientUtils.readResponse(result));
            if (headers != null) {
                for (Header headerName : result.getAllHeaders()) {
                    headers.put(headerName.getName(), Arrays.stream(result.getHeaders(headerName.getName())).map(Header::getValue).collect(Collectors.toList()));
                }
            }
            return result.getStatusLine().getStatusCode();
        }
    }

    private static Map<String, JspPropertyGroup> getGroupDescriptors() {
        Map<String,JspPropertyGroup> result = new HashMap<>();
        /*
                 <jsp-property-group>
             <url-pattern>/bug5nnnn/bug55262.jsp</url-pattern>
             <include-prelude>/bug5nnnn/bug55262-prelude.jspf</include-prelude>
              <include-coda>/bug5nnnn/bug55262-coda.jspf</include-coda>
             <default-content-type>text/plain</default-content-type>
           </jsp-property-group>
                */
        JspPropertyGroup bug55262 = new JspPropertyGroup();
        bug55262.addIncludePrelude("/bug5nnnn/bug55262-prelude.jspf");
        bug55262.addIncludeCoda("/bug5nnnn/bug55262-coda.jspf");
        bug55262.setDefaultContentType("text/plain");
        addGroup(bug55262, result);

               /*
               <jsp-property-group>
                     <url-pattern>/bug49nnn/bug49726a.jsp</url-pattern>
                     <default-content-type>text/plain</default-content-type>
                   </jsp-property-group>
                */
        JspPropertyGroup bug49726a = new JspPropertyGroup();
        bug49726a.addUrlPattern("/bug49nnn/bug49726a.jsp");
        bug49726a.setDefaultContentType("text/plain");
        addGroup(bug49726a, result);


        return result;
    }

    private static void addGroup(JspPropertyGroup jspPropertyGroup, Map<String, JspPropertyGroup> result) {
        for (String pattern : jspPropertyGroup.getUrlPatterns()) {
            // Split off the groups to individual mappings
            result.put(pattern, jspPropertyGroup);
        }
    }
}
