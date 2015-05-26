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

package org.apache.jasper.compiler;

import static org.apache.jasper.JasperMessages.MESSAGES;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.tagext.FunctionInfo;
import javax.servlet.jsp.tagext.PageData;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.TagFileInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.TagLibraryValidator;
import javax.servlet.jsp.tagext.TagVariableInfo;
import javax.servlet.jsp.tagext.ValidationMessage;
import javax.servlet.jsp.tagext.VariableInfo;

import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.apache.tomcat.util.scan.Jar;
import org.apache.tomcat.util.scan.JarFactory;

/**
 * Implementation of the TagLibraryInfo class from the JSP spec.
 * 
 * @author Anil K. Vijendran
 * @author Mandar Raje
 * @author Pierre Delisle
 * @author Kin-man Chung
 * @author Jan Luehe
 */
class TagLibraryInfoImpl extends TagLibraryInfo implements TagConstants {

    /**
     * The types of URI one may specify for a tag library
     */
    public static final int ABS_URI = 0;
    public static final int ROOT_REL_URI = 1;
    public static final int NOROOT_REL_URI = 2;

    private JspCompilationContext ctxt;
    
    private PageInfo pi;

    private ErrorDispatcher err;

    private ParserController parserController;

    private final void print(String name, String value, PrintWriter w) {
        if (value != null) {
            w.print(name + " = {\n\t");
            w.print(value);
            w.print("\n}\n");
        }
    }

    public String toString() {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        print("tlibversion", tlibversion, out);
        print("jspversion", jspversion, out);
        print("shortname", shortname, out);
        print("urn", urn, out);
        print("info", info, out);
        print("uri", uri, out);
        print("tagLibraryValidator", "" + tagLibraryValidator, out);

        for (int i = 0; i < tags.length; i++)
            out.println(tags[i].toString());

        for (int i = 0; i < tagFiles.length; i++)
            out.println(tagFiles[i].toString());

        for (int i = 0; i < functions.length; i++)
            out.println(functions[i].toString());

        return sw.toString();
    }

    /**
     * Constructor.
     */
    public TagLibraryInfoImpl(JspCompilationContext ctxt, ParserController pc, PageInfo pi,
            String prefix, String uriIn, String[] location, ErrorDispatcher err)
            throws JasperException {
        super(prefix, uriIn);

        this.ctxt = ctxt;
        this.parserController = pc;
        this.pi = pi;
        this.err = err;
        
        if (location == null) {
            // The URI points to the TLD itself or to a JAR file in which the
            // TLD is stored
            location = generateTLDLocation(uri, ctxt);
            if (location != null) {
                uri = location[0];
            }
        }

        URL jarFileUrl = null;

        if (location == null) {
            err.jspError(MESSAGES.fileNotFound(uriIn));
        }
        if (location[0] != null && location[0].endsWith(".jar")) {
            try {
                URL jarUrl = ctxt.getServletContext().getResource(location[0]);
                if (jarUrl != null) {
                    jarFileUrl = new URL("jar:" + jarUrl + "!/");
                }
            } catch (MalformedURLException ex) {
                err.jspError(MESSAGES.fileNotFound(uriIn));
            }
        }
        Jar jar = null;
        try {
            if (jarFileUrl != null) {
                jar = JarFactory.newInstance(jarFileUrl);
            }
        } catch (IOException e) {
            throw new JasperException(e);
        }

        org.apache.jasper.deploy.TagLibraryInfo tagLibraryInfo = 
            ((HashMap<String, org.apache.jasper.deploy.TagLibraryInfo>) 
            ctxt.getServletContext().getAttribute(Constants.JSP_TAG_LIBRARIES)).get(uri);
        if (tagLibraryInfo == null) {
            err.jspError(MESSAGES.fileNotFound(uriIn));
        }

        ArrayList<TagInfo> tagInfos = new ArrayList<TagInfo>();
        ArrayList<TagFileInfo> tagFileInfos = new ArrayList<TagFileInfo>();
        HashMap<String, FunctionInfo> functionInfos = new HashMap<String, FunctionInfo>();

        this.jspversion = tagLibraryInfo.getJspversion();
        this.tlibversion = tagLibraryInfo.getTlibversion();
        this.shortname = tagLibraryInfo.getShortname();
        this.urn = tagLibraryInfo.getUri();
        this.info = tagLibraryInfo.getInfo();
        if (tagLibraryInfo.getValidator() != null) {
            this.tagLibraryValidator = createValidator(tagLibraryInfo);
        }
        org.apache.jasper.deploy.TagInfo tagInfosArray[] = tagLibraryInfo.getTags();
        for (int i = 0; i < tagInfosArray.length; i++) {
            TagInfo tagInfo = createTagInfo(tagInfosArray[i]);
            tagInfos.add(tagInfo);
        }
        org.apache.jasper.deploy.TagFileInfo tagFileInfosArray[] = tagLibraryInfo.getTagFileInfos();
        for (int i = 0; i < tagFileInfosArray.length; i++) {
            TagFileInfo tagFileInfo = createTagFileInfo(tagFileInfosArray[i], jar);
            tagFileInfos.add(tagFileInfo);
        }
        org.apache.jasper.deploy.FunctionInfo functionInfosArray[] = tagLibraryInfo.getFunctionInfos();
        for (int i = 0; i < functionInfosArray.length; i++) {
            FunctionInfo functionInfo = createFunctionInfo(functionInfosArray[i]);
            if (functionInfos.containsKey(functionInfo.getName())) {
                err.jspError(MESSAGES.duplicateTagLibraryFunctionName(functionInfo.getName(),
                        uri));
            }
            functionInfos.put(functionInfo.getName(), functionInfo);
        }
        
        if (tlibversion == null) {
            err.jspError(MESSAGES.missingRequiredTagLibraryElement("tlib-version", uri));
        }
        if (jspversion == null) {
            err.jspError(MESSAGES.missingRequiredTagLibraryElement("jsp-version", uri));
        }

        this.tags = tagInfos.toArray(new TagInfo[0]);
        this.tagFiles = tagFileInfos.toArray(new TagFileInfo[0]);
        this.functions = functionInfos.values().toArray(new FunctionInfo[0]);
    }

    /**
     * @param uri The uri of the TLD @param ctxt The compilation context
     * 
     * @return String array whose first element denotes the path to the TLD. If
     * the path to the TLD points to a jar file, then the second element denotes
     * the name of the TLD entry in the jar file, which is hardcoded to
     * META-INF/taglib.tld.
     */
    private String[] generateTLDLocation(String uri, JspCompilationContext ctxt)
            throws JasperException {

        int uriType = uriType(uri);
        if (uriType == ABS_URI) {
            err.jspError(MESSAGES.unresolvableAbsoluteUri(uri));
        } else if (uriType == NOROOT_REL_URI) {
            uri = ctxt.resolveRelativeUri(uri);
            if (uri != null) {
                uri = normalize(uri);
            }
        }

        String[] location = new String[2];
        location[0] = uri;
        if (location[0].endsWith("jar")) {
            URL url = null;
            try {
                url = ctxt.getResource(location[0]);
            } catch (Exception ex) {
                err.jspError(ex, MESSAGES.errorAccessingJar(location[0]));
            }
            if (url == null) {
                err.jspError(MESSAGES.missingJar(location[0]));
            }
            location[0] = url.toString();
            location[1] = "META-INF/taglib.tld";
        }

        return location;
    }

    /**
     * Normalize a relative URI path that may have relative values ("/./",
     * "/../", and so on ) it it.  <strong>WARNING</strong> - This method is
     * useful only for normalizing application-generated paths.  It does not
     * try to perform security checks for malicious input.
     *
     * @param path Relative path to be normalized
     */
    private String normalize(String path) {
        return normalize(path, true);
    }

    /**
     * Normalize a relative URI path that may have relative values ("/./",
     * "/../", and so on ) it it.  <strong>WARNING</strong> - This method is
     * useful only for normalizing application-generated paths.  It does not
     * try to perform security checks for malicious input.
     *
     * @param path Relative path to be normalized
     * @param replaceBackSlash Should '\\' be replaced with '/'
     */
    private String normalize(String path, boolean replaceBackSlash) {

        if (path == null)
            return null;

        // Create a place for the normalized path
        String normalized = path;

        if (replaceBackSlash && normalized.indexOf('\\') >= 0)
            normalized = normalized.replace('\\', '/');

        if (normalized.equals("/."))
            return "/";

        // Add a leading "/" if necessary
        if (!normalized.startsWith("/"))
            normalized = "/" + normalized;

        // Resolve occurrences of "//" in the normalized path
        while (true) {
            int index = normalized.indexOf("//");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) +
                normalized.substring(index + 1);
        }

        // Resolve occurrences of "/./" in the normalized path
        while (true) {
            int index = normalized.indexOf("/./");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) +
                normalized.substring(index + 2);
        }

        // Resolve occurrences of "/../" in the normalized path
        while (true) {
            int index = normalized.indexOf("/../");
            if (index < 0)
                break;
            if (index == 0)
                return (null);  // Trying to go outside our context
            int index2 = normalized.lastIndexOf('/', index - 1);
            normalized = normalized.substring(0, index2) +
                normalized.substring(index + 3);
        }

        // Return the normalized path that we have completed
        return (normalized);

    }


    public TagLibraryInfo[] getTagLibraryInfos() {
        Collection coll = pi.getTaglibs();
        return (TagLibraryInfo[]) coll.toArray(new TagLibraryInfo[0]);
    }
    
    protected TagInfo createTagInfo(org.apache.jasper.deploy.TagInfo tagInfo)
        throws JasperException {

        ArrayList<TagAttributeInfo> attributeInfos = new ArrayList<TagAttributeInfo>();
        ArrayList<TagVariableInfo> variableInfos = new ArrayList<TagVariableInfo>();

        boolean dynamicAttributes = JspUtil.booleanValue(tagInfo.getDynamicAttributes());

        org.apache.jasper.deploy.TagAttributeInfo attributeInfosArray[] = tagInfo.getTagAttributeInfos();
        for (int i = 0; i < attributeInfosArray.length; i++) {
            TagAttributeInfo attributeInfo = createTagAttributeInfo(attributeInfosArray[i]);
            attributeInfos.add(attributeInfo);
        }

        org.apache.jasper.deploy.TagVariableInfo variableInfosArray[] = tagInfo.getTagVariableInfos();
        for (int i = 0; i < variableInfosArray.length; i++) {
            TagVariableInfo variableInfo = createTagVariableInfo(variableInfosArray[i]);
            variableInfos.add(variableInfo);
        }
        
        TagExtraInfo tei = null;
        String teiClassName = tagInfo.getTagExtraInfo();
        if (teiClassName != null && !teiClassName.equals("")) {
            try {
                Class teiClass = ctxt.getClassLoader().loadClass(teiClassName);
                tei = (TagExtraInfo) teiClass.newInstance();
            } catch (Exception e) {
                err.jspError(e, MESSAGES.errorLoadingTagExtraInfo(teiClassName));
            }
        }

        String tagBodyContent = tagInfo.getBodyContent();
        if (tagBodyContent == null) {
            tagBodyContent = TagInfo.BODY_CONTENT_JSP;
        }

        return new TagInfo(tagInfo.getTagName(), tagInfo.getTagClassName(), tagBodyContent, 
                tagInfo.getInfoString(), this, tei, attributeInfos.toArray(new TagAttributeInfo[0]), 
                tagInfo.getDisplayName(), tagInfo.getSmallIcon(), tagInfo.getLargeIcon(),
                variableInfos.toArray(new TagVariableInfo[0]), dynamicAttributes);
    }
    
    protected TagAttributeInfo createTagAttributeInfo(org.apache.jasper.deploy.TagAttributeInfo attributeInfo) {

        String type = attributeInfo.getType();
        String expectedType = attributeInfo.getExpectedTypeName();
        String methodSignature = attributeInfo.getMethodSignature();
        boolean rtexprvalue = JspUtil.booleanValue(attributeInfo.getReqTime());
        boolean fragment = JspUtil.booleanValue(attributeInfo.getFragment());
        boolean deferredValue = JspUtil.booleanValue(attributeInfo.getDeferredValue());
        boolean deferredMethod = JspUtil.booleanValue(attributeInfo.getDeferredMethod());
        boolean required = JspUtil.booleanValue(attributeInfo.getRequired());
        
        if (type != null) {
            if ("1.2".equals(jspversion)
                    && (type.equals("Boolean") || type.equals("Byte")
                            || type.equals("Character")
                            || type.equals("Double")
                            || type.equals("Float")
                            || type.equals("Integer")
                            || type.equals("Long") || type.equals("Object")
                            || type.equals("Short") || type
                            .equals("String"))) {
                type = "java.lang." + type;
            }
        }

        if (deferredValue) {
            type = "javax.el.ValueExpression";
            if (expectedType != null) {
                expectedType = expectedType.trim();
            } else {
                expectedType = "java.lang.Object";
            }
        }
        
        if (deferredMethod) {
            type = "javax.el.MethodExpression";
            if (methodSignature != null) {
                methodSignature = methodSignature.trim();
            } else {
                methodSignature = "java.lang.Object method()";
            }
        }

        if (fragment) {
            /*
             * According to JSP.C-3 ("TLD Schema Element Structure - tag"),
             * 'type' and 'rtexprvalue' must not be specified if 'fragment' has
             * been specified (this will be enforced by validating parser).
             * Also, if 'fragment' is TRUE, 'type' is fixed at
             * javax.servlet.jsp.tagext.JspFragment, and 'rtexprvalue' is fixed
             * at true. See also JSP.8.5.2.
             */
            type = "javax.servlet.jsp.tagext.JspFragment";
            rtexprvalue = true;
        }

        if (!rtexprvalue && type == null) {
            // According to JSP spec, for static values (those determined at
            // translation time) the type is fixed at java.lang.String.
            type = "java.lang.String";
        }
        
        return new TagAttributeInfo(attributeInfo.getName(), required, 
                type, rtexprvalue, fragment, attributeInfo.getDescription(), 
                deferredValue, deferredMethod, expectedType,
                methodSignature);
    }
    
    protected TagVariableInfo createTagVariableInfo(org.apache.jasper.deploy.TagVariableInfo variableInfo) {
        int scope = VariableInfo.NESTED;
        String s = variableInfo.getScope();
        if (s != null) {
            if ("NESTED".equals(s)) {
                scope = VariableInfo.NESTED;
            } else if ("AT_BEGIN".equals(s)) {
                scope = VariableInfo.AT_BEGIN;
            } else if ("AT_END".equals(s)) {
                scope = VariableInfo.AT_END;
            }
        }
        String className = variableInfo.getClassName();
        if (className == null) {
            className = "java.lang.String";
        }
        boolean declare = true;
        if (variableInfo.getDeclare() != null) {
            declare = JspUtil.booleanValue(variableInfo.getDeclare());
        }
        return new TagVariableInfo(variableInfo.getNameGiven(), variableInfo.getNameFromAttribute(), 
                className, declare, scope);
    }

    protected TagFileInfo createTagFileInfo(org.apache.jasper.deploy.TagFileInfo tagFileInfo, Jar jar)
            throws JasperException{
        String name = tagFileInfo.getName();
        String path = tagFileInfo.getPath();
        if (path.startsWith("/META-INF/tags")) {
            // Tag file packaged in JAR
            // See https://issues.apache.org/bugzilla/show_bug.cgi?id=46471
            // This needs to be removed once all the broken code that depends on
            // it has been removed
            ctxt.setTagFileJarUrl(path, jar.getJarFileURL());
        } else if (!path.startsWith("/WEB-INF/tags")) {
            err.jspError(MESSAGES.invalidTagFileDirectory(path));
        }
        TagInfo tagInfo = TagFileProcessor.parseTagFileDirectives(
                parserController, name, path, jar, this);
        return new TagFileInfo(name, path, tagInfo);
    }
    
    protected FunctionInfo createFunctionInfo(org.apache.jasper.deploy.FunctionInfo functionInfo) {
        return new FunctionInfo(functionInfo.getName(), 
                functionInfo.getFunctionClass(), functionInfo.getFunctionSignature());
    }
    
    
    /** 
     * Returns the type of a URI:
     *     ABS_URI
     *     ROOT_REL_URI
     *     NOROOT_REL_URI
     */
    public static int uriType(String uri) {
        if (uri.indexOf(':') != -1) {
            return ABS_URI;
        } else if (uri.startsWith("/")) {
            return ROOT_REL_URI;
        } else {
            return NOROOT_REL_URI;
        }
    }

    private TagLibraryValidator createValidator(org.apache.jasper.deploy.TagLibraryInfo tagLibraryInfo)
            throws JasperException {
        org.apache.jasper.deploy.TagLibraryValidatorInfo tlvInfo = tagLibraryInfo.getValidator();
        String validatorClass = tlvInfo.getValidatorClass();
        Map<String, Object> initParams = tlvInfo.getInitParams();

        TagLibraryValidator tlv = null;
        if (validatorClass != null && !validatorClass.equals("")) {
            try {
                Class tlvClass = ctxt.getClassLoader()
                        .loadClass(validatorClass);
                tlv = (TagLibraryValidator) tlvClass.newInstance();
            } catch (Exception e) {
                err.jspError(e, MESSAGES.errorLoadingTagLibraryValidator(validatorClass));
            }
        }
        if (tlv != null) {
            tlv.setInitParameters(initParams);
        }
        return tlv;
    }

    // *********************************************************************
    // Until javax.servlet.jsp.tagext.TagLibraryInfo is fixed

    /**
     * The instance (if any) for the TagLibraryValidator class.
     * 
     * @return The TagLibraryValidator instance, if any.
     */
    public TagLibraryValidator getTagLibraryValidator() {
        return tagLibraryValidator;
    }

    /**
     * Translation-time validation of the XML document associated with the JSP
     * page. This is a convenience method on the associated TagLibraryValidator
     * class.
     * 
     * @param thePage
     *            The JSP page object
     * @return A string indicating whether the page is valid or not.
     */
    public ValidationMessage[] validate(PageData thePage) {
        TagLibraryValidator tlv = getTagLibraryValidator();
        if (tlv == null)
            return null;

        String uri = getURI();
        if (uri.startsWith("/")) {
            uri = URN_JSPTLD + uri;
        }

        return tlv.validate(getPrefixString(), uri, thePage);
    }

    protected TagLibraryValidator tagLibraryValidator;
}
