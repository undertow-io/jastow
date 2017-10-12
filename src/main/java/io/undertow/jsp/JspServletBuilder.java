package io.undertow.jsp;

import static org.apache.jasper.Constants.JSP_PROPERTY_GROUPS;
import static org.apache.jasper.Constants.JSP_TAG_LIBRARIES;
import static org.apache.jasper.Constants.SERVLET_VERSION;

import java.util.Collection;
import java.util.Map;

import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.descriptor.JspPropertyGroupDescriptor;
import javax.servlet.descriptor.TaglibDescriptor;

import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.ServletInfo;
import org.apache.jasper.deploy.TagLibraryInfo;
import org.apache.jasper.servlet.JspServlet;
import org.apache.tomcat.InstanceManager;

/**
 * Builder that creates a JSP deployment.
 *
 * @author Stuart Douglas
 */
public class JspServletBuilder {


    public static void setupDeployment(final DeploymentInfo deploymentInfo, final Map<String, ? extends JspPropertyGroupDescriptor> propertyGroups, final Map<String, TagLibraryInfo> tagLibraries, final InstanceManager instanceManager) {
        deploymentInfo.addServletContextAttribute(SERVLET_VERSION, deploymentInfo.getMajorVersion() + "." + deploymentInfo.getMinorVersion());
        deploymentInfo.addServletContextAttribute(JSP_PROPERTY_GROUPS, propertyGroups);
        deploymentInfo.addServletContextAttribute(JSP_TAG_LIBRARIES, tagLibraries);
        deploymentInfo.addServletContextAttribute(InstanceManager.class.getName(), instanceManager);
        deploymentInfo.setJspConfigDescriptor(new JspConfigDescriptor() {
            @Override
            public Collection<TaglibDescriptor> getTaglibs() {
                return null;
            }

            @Override
            public Collection<JspPropertyGroupDescriptor> getJspPropertyGroups() {
                return (Collection<JspPropertyGroupDescriptor>) propertyGroups.values();
            }
        });
    }

    public static ServletInfo createServlet(final String name, final String path) {
        ServletInfo servlet = new ServletInfo(name, JspServlet.class);
        servlet.addMapping(path);
        //if the JSP servlet is mapped to a path that ends in /*
        //we want to perform welcome file matches if the directory is requested
        servlet.setRequireWelcomeFileMapping(true);
        return servlet;
    }


}
