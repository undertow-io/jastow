package io.undertow.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.descriptor.JspPropertyGroupDescriptor;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import io.undertow.jsp.HackInstanceManager;
import io.undertow.jsp.JspServletBuilder;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.test.util.TestClassIntrospector;
import io.undertow.testutils.AjpIgnore;
import io.undertow.testutils.DefaultServer;
import io.undertow.testutils.HttpClientUtils;
import io.undertow.testutils.HttpOneOnly;
import io.undertow.testutils.HttpsIgnore;
import io.undertow.testutils.TestHttpClient;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.jasper.deploy.FunctionInfo;
import org.apache.jasper.deploy.JspPropertyGroup;
import org.apache.jasper.deploy.TagAttributeInfo;
import org.apache.jasper.deploy.TagFileInfo;
import org.apache.jasper.deploy.TagInfo;
import org.apache.jasper.deploy.TagLibraryInfo;
import org.apache.jasper.deploy.TagLibraryValidatorInfo;
import org.apache.jasper.deploy.TagVariableInfo;
import org.apache.tomcat.util.buf.ByteChunk;
import org.jboss.annotation.javaee.Icon;
import org.jboss.metadata.javaee.spec.DescriptionGroupMetaData;
import org.jboss.metadata.javaee.spec.ParamValueMetaData;
import org.jboss.metadata.parser.jsp.TldMetaDataParser;
import org.jboss.metadata.web.spec.AttributeMetaData;
import org.jboss.metadata.web.spec.FunctionMetaData;
import org.jboss.metadata.web.spec.TagFileMetaData;
import org.jboss.metadata.web.spec.TagMetaData;
import org.jboss.metadata.web.spec.TldMetaData;
import org.jboss.metadata.web.spec.VariableMetaData;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * @author Tomaz Cerar (c) 2017 Red Hat Inc.
 * This class is just a substition class for TomcatBaseTest from tomcat codebase but adapted to work with undertow & jastow.
 */
@RunWith(DefaultServer.class)
@HttpOneOnly
@HttpsIgnore
@AjpIgnore
public abstract class TomcatBaseTest {

    @BeforeClass
    public static void setUp() throws Exception {

        final PathHandler servletPath = new PathHandler();
        final ServletContainer container = ServletContainer.Factory.newInstance();

        Path root = Paths.get(Thread.currentThread().getContextClassLoader().getResource("webapp").toURI());
        DeploymentInfo builder = new DeploymentInfo()
                .setClassLoader(TomcatBaseTest.class.getClassLoader())
                .setContextPath("/test")
                .setClassIntrospecter(TestClassIntrospector.INSTANCE)
                .setDeploymentName("test.war")
                .setResourceManager(new PathResourceManager(root))
                .addServlet(JspServletBuilder.createServlet("Default Jsp Servlet", "*.jsp").addMapping("*.jspx"));
        JspServletBuilder.setupDeployment(builder, getGroupDescriptors(), getTaglibConfig(root), new HackInstanceManager());

        DeploymentManager manager = container.addDeployment(builder);
        manager.deploy();
        servletPath.addPrefixPath(builder.getContextPath(), manager.start());

        DefaultServer.setRootHandler(servletPath);

    }

    protected void getTomcatInstanceTestWebapp(boolean a, boolean b) {
        //no-op just to make as little changes to base tests as possible

    }

    protected int getPort() {
        return DefaultServer.getHostPort();
    }

    protected ByteChunk getUrl(String url) throws IOException {
        try (TestHttpClient client = new TestHttpClient()) {
            HttpGet get = new HttpGet(url);
            HttpResponse result = client.execute(get);
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

    private static Map<String, JspPropertyGroupDescriptor> getGroupDescriptors() {
        Map<String, JspPropertyGroupDescriptor> result = new HashMap<>();
        /*
                 <jsp-property-group>
             <url-pattern>/bug5nnnn/bug55262.jsp</url-pattern>
             <include-prelude>/bug5nnnn/bug55262-prelude.jspf</include-prelude>
              <include-coda>/bug5nnnn/bug55262-coda.jspf</include-coda>
             <default-content-type>text/plain</default-content-type>
           </jsp-property-group>
                */
        JspPropertyGroup bug55262 = new JspPropertyGroup();
        bug55262.addUrlPattern("/bug5nnnn/bug55262.jsp");
        bug55262.addIncludePrelude("/bug5nnnn/bug55262-prelude.jspf");
        bug55262.addIncludePrelude("/bug5nnnn/bug55262-prelude.jspf");
        bug55262.addIncludeCoda("/bug5nnnn/bug55262-coda.jspf");
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
        bug49726a.addUrlPattern("/bug49nnn/bug49726b.jsp");
        bug49726a.setDefaultContentType("text/plain");
        addGroup(bug49726a, result);

        /*
         <jsp-property-group>
      <url-pattern>/jsp/encoding/bug60769a.jspx</url-pattern>
      <page-encoding>UTF-8</page-encoding>
      <is-xml>true</is-xml>
    </jsp-property-group>
    <jsp-property-group>
      <url-pattern>/jsp/encoding/bug60769b.jspx</url-pattern>
      <page-encoding>ISO-8859-1</page-encoding>
      <is-xml>true</is-xml>
    </jsp-property-group>
         */
        JspPropertyGroup g = new JspPropertyGroup();
        g.addUrlPattern("/jsp/encoding/bug60769a.jspx");
        g.setPageEncoding("UTF-8");
        g.setIsXml("true");
        addGroup(g, result);

        g = new JspPropertyGroup();
        g.addUrlPattern("/jsp/encoding/bug60769b.jspx");
        g.setPageEncoding("ISO-8859-1");
        g.setIsXml("true");
        addGroup(g, result);


        return result;
    }

    private static Map<String, TagLibraryInfo> getTaglibConfig(Path root) throws Exception {

        Map<String, TagLibraryInfo> tags = new HashMap<>();
        registerTaglibsFromWebInf(tags, root);
        registerTaglibsFromClassPath(tags);

        return tags;
    }


    private static void addGroup(JspPropertyGroup jspPropertyGroup, Map<String, JspPropertyGroupDescriptor> result) {
        for (String pattern : jspPropertyGroup.getUrlPatterns()) {
            // Split off the groups to individual mappings
            result.put(pattern, jspPropertyGroup);
        }
    }

    private static void registerTaglibsFromWebInf(Map<String, TagLibraryInfo> tags, Path root) throws Exception {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root.resolve("WEB-INF"), "*.tld")) {
            for (Path tldFile : stream) {
                String location = "/" + tldFile.subpath(root.getNameCount(), tldFile.getNameCount()).toString().replaceAll("\\\\", "/");
                parseTLD(tags, tldFile, location);
            }
        }
    }

    private static Map<String, TagLibraryInfo> TAGS_CACHE = new HashMap<>();

    private static void registerTaglibsFromClassPath(Map<String, TagLibraryInfo> tags) throws Exception {
        if (!TAGS_CACHE.isEmpty()){
            tags.putAll(TAGS_CACHE);
            return;
        }
        //this is ugly and will break on JDK9
        for (final String cp : System.getProperty("java.class.path").split(File.pathSeparator)) {
            Path jar = Paths.get(cp);
            if (Files.isDirectory(jar)) {
                continue;
            }

            try (FileSystem zip = FileSystems.newFileSystem(jar, null);
                 DirectoryStream<Path> tlds = Files.newDirectoryStream(zip.getPath("META-INF"), "*.tld")) {
                for (Path tldFile : tlds) {
                    parseTLD(tags, tldFile, null);
                }
            }
        }
        TAGS_CACHE.putAll(tags);
    }

    private static void parseTLD(Map<String, TagLibraryInfo> tags, Path tldFile, String location) throws Exception {
        XMLStreamReader reader = null;
        try (InputStream is = Files.newInputStream(tldFile)) {
            reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
            TldMetaData metaData = TldMetaDataParser.parse(reader);
            createTldInfo(location, metaData, tags);

        } catch (Exception e) {
            throw new Exception("problem parsing " + tldFile, e);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    //copied over from wildfly's undertow subsystem UndertowDeploymentProcessor.java
    private static TagLibraryInfo createTldInfo(final String location, final TldMetaData tldMetaData, final Map<String, TagLibraryInfo> ret) {
        String relativeLocation = location;
        String jarPath = null;
        if (relativeLocation != null && relativeLocation.startsWith("/WEB-INF/lib/")) {
            int pos = relativeLocation.indexOf('/', "/WEB-INF/lib/".length());
            if (pos > 0) {
                jarPath = relativeLocation.substring(pos);
                if (jarPath.startsWith("/")) {
                    jarPath = jarPath.substring(1);
                }
                relativeLocation = relativeLocation.substring(0, pos);
            }
        }

        TagLibraryInfo tagLibraryInfo = new TagLibraryInfo();
        tagLibraryInfo.setTlibversion(tldMetaData.getTlibVersion());
        if (tldMetaData.getJspVersion() == null) {
            tagLibraryInfo.setJspversion(tldMetaData.getVersion());
        } else {
            tagLibraryInfo.setJspversion(tldMetaData.getJspVersion());
        }
        tagLibraryInfo.setShortname(tldMetaData.getShortName());
        tagLibraryInfo.setUri(tldMetaData.getUri());
        if (tldMetaData.getDescriptionGroup() != null) {
            tagLibraryInfo.setInfo(tldMetaData.getDescriptionGroup().getDescription());
        }
        // Validator
        if (tldMetaData.getValidator() != null) {
            TagLibraryValidatorInfo tagLibraryValidatorInfo = new TagLibraryValidatorInfo();
            tagLibraryValidatorInfo.setValidatorClass(tldMetaData.getValidator().getValidatorClass());
            if (tldMetaData.getValidator().getInitParams() != null) {
                for (ParamValueMetaData paramValueMetaData : tldMetaData.getValidator().getInitParams()) {
                    tagLibraryValidatorInfo.addInitParam(paramValueMetaData.getParamName(), paramValueMetaData.getParamValue());
                }
            }
            tagLibraryInfo.setValidator(tagLibraryValidatorInfo);
        }
        // Tag
        if (tldMetaData.getTags() != null) {
            for (TagMetaData tagMetaData : tldMetaData.getTags()) {
                TagInfo tagInfo = new TagInfo();
                tagInfo.setTagName(tagMetaData.getName());
                tagInfo.setTagClassName(tagMetaData.getTagClass());
                tagInfo.setTagExtraInfo(tagMetaData.getTeiClass());
                if (tagMetaData.getBodyContent() != null) {
                    tagInfo.setBodyContent(tagMetaData.getBodyContent().toString());
                }
                tagInfo.setDynamicAttributes(tagMetaData.getDynamicAttributes());
                // Description group
                if (tagMetaData.getDescriptionGroup() != null) {
                    DescriptionGroupMetaData descriptionGroup = tagMetaData.getDescriptionGroup();
                    if (descriptionGroup.getIcons() != null && descriptionGroup.getIcons().value() != null
                            && (descriptionGroup.getIcons().value().length > 0)) {
                        Icon icon = descriptionGroup.getIcons().value()[0];
                        tagInfo.setLargeIcon(icon.largeIcon());
                        tagInfo.setSmallIcon(icon.smallIcon());
                    }
                    tagInfo.setInfoString(descriptionGroup.getDescription());
                    tagInfo.setDisplayName(descriptionGroup.getDisplayName());
                }
                // Variable
                if (tagMetaData.getVariables() != null) {
                    for (VariableMetaData variableMetaData : tagMetaData.getVariables()) {
                        TagVariableInfo tagVariableInfo = new TagVariableInfo();
                        tagVariableInfo.setNameGiven(variableMetaData.getNameGiven());
                        tagVariableInfo.setNameFromAttribute(variableMetaData.getNameFromAttribute());
                        tagVariableInfo.setClassName(variableMetaData.getVariableClass());
                        tagVariableInfo.setDeclare(variableMetaData.getDeclare());
                        if (variableMetaData.getScope() != null) {
                            tagVariableInfo.setScope(variableMetaData.getScope().toString());
                        }
                        tagInfo.addTagVariableInfo(tagVariableInfo);
                    }
                }
                // Attribute
                if (tagMetaData.getAttributes() != null) {
                    for (AttributeMetaData attributeMetaData : tagMetaData.getAttributes()) {
                        TagAttributeInfo tagAttributeInfo = new TagAttributeInfo();
                        tagAttributeInfo.setName(attributeMetaData.getName());
                        tagAttributeInfo.setType(attributeMetaData.getType());
                        tagAttributeInfo.setReqTime(attributeMetaData.getRtexprvalue());
                        tagAttributeInfo.setRequired(attributeMetaData.getRequired());
                        tagAttributeInfo.setFragment(attributeMetaData.getFragment());
                        if (attributeMetaData.getDeferredValue() != null) {
                            tagAttributeInfo.setDeferredValue("true");
                            tagAttributeInfo.setExpectedTypeName(attributeMetaData.getDeferredValue().getType());
                        } else {
                            tagAttributeInfo.setDeferredValue("false");
                        }
                        if (attributeMetaData.getDeferredMethod() != null) {
                            tagAttributeInfo.setDeferredMethod("true");
                            tagAttributeInfo.setMethodSignature(attributeMetaData.getDeferredMethod().getMethodSignature());
                        } else {
                            tagAttributeInfo.setDeferredMethod("false");
                        }
                        tagInfo.addTagAttributeInfo(tagAttributeInfo);
                    }
                }
                tagLibraryInfo.addTagInfo(tagInfo);
            }
        }
        // Tag files
        if (tldMetaData.getTagFiles() != null) {
            for (TagFileMetaData tagFileMetaData : tldMetaData.getTagFiles()) {
                TagFileInfo tagFileInfo = new TagFileInfo();
                tagFileInfo.setName(tagFileMetaData.getName());
                tagFileInfo.setPath(tagFileMetaData.getPath());
                tagLibraryInfo.addTagFileInfo(tagFileInfo);
            }
        }
        // Function
        if (tldMetaData.getFunctions() != null) {
            for (FunctionMetaData functionMetaData : tldMetaData.getFunctions()) {
                FunctionInfo functionInfo = new FunctionInfo();
                functionInfo.setName(functionMetaData.getName());
                functionInfo.setFunctionClass(functionMetaData.getFunctionClass());
                functionInfo.setFunctionSignature(functionMetaData.getFunctionSignature());
                tagLibraryInfo.addFunctionInfo(functionInfo);
            }
        }

        if (jarPath == null && relativeLocation == null) {
            if (!ret.containsKey(tagLibraryInfo.getUri())) {
                ret.put(tagLibraryInfo.getUri(), tagLibraryInfo);
            }
        } else if (jarPath == null) {
            tagLibraryInfo.setLocation("");
            tagLibraryInfo.setPath(relativeLocation);
            if (!ret.containsKey(tagLibraryInfo.getUri())) {
                ret.put(tagLibraryInfo.getUri(), tagLibraryInfo);
            }
            ret.put(relativeLocation, tagLibraryInfo);
        } else {
            tagLibraryInfo.setLocation(relativeLocation);
            tagLibraryInfo.setPath(jarPath);
            if (!ret.containsKey(tagLibraryInfo.getUri())) {
                ret.put(tagLibraryInfo.getUri(), tagLibraryInfo);
            }
            if (jarPath.equals("META-INF/taglib.tld")) {
                ret.put(relativeLocation, tagLibraryInfo);
            }
        }
        return tagLibraryInfo;
    }

}
