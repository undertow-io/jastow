package io.undertow.test.jsp.classref;

import io.undertow.jsp.HackInstanceManager;
import io.undertow.jsp.JspServletBuilder;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.test.util.TestClassIntrospector;
import io.undertow.servlet.test.util.TestResourceLoader;
import io.undertow.testutils.DefaultServer;
import io.undertow.testutils.TestHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.jasper.deploy.JspPropertyGroup;
import org.apache.jasper.deploy.TagLibraryInfo;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import javax.servlet.ServletException;

/**
 * @tpChapter Class access from jsp
 */

@RunWith(DefaultServer.class)
public class ClassRefTestCase {
    private static final String CONTEXT_NAME = "classref";
    private static final String CONTEXT_PATH = "/" + CONTEXT_NAME;

    @BeforeClass
    public static void setup() throws ServletException {

        final PathHandler servletPath = new PathHandler();
        final ServletContainer container = ServletContainer.Factory.newInstance();

        DeploymentInfo builder = new DeploymentInfo()
                .setClassLoader(ClassRefTestCase.class.getClassLoader())
                .setContextPath(CONTEXT_PATH)
                .setClassIntrospecter(TestClassIntrospector.INSTANCE)
                .setDeploymentName(CONTEXT_NAME + ".war")
                .setResourceManager(new TestResourceLoader(ClassRefTestCase.class))
                .addServlet(JspServletBuilder.createServlet("Default Jsp Servlet", "*.jsp"));

        JspServletBuilder.setupDeployment(builder, new HashMap<String, JspPropertyGroup>(),
                new HashMap<String, TagLibraryInfo>(), new HackInstanceManager());

        DeploymentManager manager = container.addDeployment(builder);
        manager.deploy();
        servletPath.addPrefixPath(builder.getContextPath(), manager.start());

        DefaultServer.setRootHandler(servletPath);
    }

    /**
     * @tpTestDetails Having class with the same name as package name with exception of case can be correctly accessed from JSP
     * for cases when the class is called directly, via FQDN and also indirectly via other class
     */
    @Test
    public void testDirectClassCallFromJsp() throws IOException {
        Assert.assertEquals("Direct access of class from jsp page for class with name matching package name "
                        + "with exception of case should succeed",
                HttpURLConnection.HTTP_OK, statusCodeForResource("test_direct.jsp"));
        Assert.assertEquals("Access via FQDN to class from jsp page for class with name matching package name "
                        + "with exception of case should succeed",
                HttpURLConnection.HTTP_OK, statusCodeForResource("test_fqdn.jsp"));
        Assert.assertEquals("Access indirectly via different class to class from jsp page for class with name matching "
                        + "package name with exception of case should succeed",
                HttpURLConnection.HTTP_OK, statusCodeForResource("test_indirect.jsp"));
    }

    private int statusCodeForResource(String resourceName) throws IOException {
        try (TestHttpClient client = new TestHttpClient()) {
            HttpGet get = new HttpGet(DefaultServer.getDefaultServerURL() + CONTEXT_PATH + "/" + resourceName);
            HttpResponse result = client.execute(get);
            return result.getStatusLine().getStatusCode();
        }
    }

}
