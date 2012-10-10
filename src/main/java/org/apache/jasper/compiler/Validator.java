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

import static org.apache.jasper.JasperMessages.MESSAGES;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;

import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.tagext.FunctionInfo;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.PageData;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.ValidationMessage;

import org.apache.jasper.JasperException;
import org.apache.jasper.el.ELContextImpl;
import org.xml.sax.Attributes;

/**
 * Performs validation on the page elements. Attributes are checked for
 * mandatory presence, entry value validity, and consistency. As a side effect,
 * some page global value (such as those from page direcitves) are stored, for
 * later use.
 * 
 * @author Kin-man Chung
 * @author Jan Luehe
 * @author Shawn Bayern
 * @author Mark Roth
 */
class Validator {

    /**
     * A visitor to validate and extract page directive info
     */
    static class DirectiveVisitor extends Node.Visitor {

        private PageInfo pageInfo;

        private ErrorDispatcher err;

        private static final JspUtil.ValidAttribute[] pageDirectiveAttrs = {
            new JspUtil.ValidAttribute("language"),
            new JspUtil.ValidAttribute("extends"),
            new JspUtil.ValidAttribute("import"),
            new JspUtil.ValidAttribute("session"),
            new JspUtil.ValidAttribute("buffer"),
            new JspUtil.ValidAttribute("autoFlush"),
            new JspUtil.ValidAttribute("isThreadSafe"),
            new JspUtil.ValidAttribute("info"),
            new JspUtil.ValidAttribute("errorPage"),
            new JspUtil.ValidAttribute("isErrorPage"),
            new JspUtil.ValidAttribute("contentType"),
            new JspUtil.ValidAttribute("pageEncoding"),
            new JspUtil.ValidAttribute("isELIgnored"),
            new JspUtil.ValidAttribute("deferredSyntaxAllowedAsLiteral"),
            new JspUtil.ValidAttribute("trimDirectiveWhitespaces")
        };

        private boolean pageEncodingSeen = false;

        /*
         * Constructor
         */
        DirectiveVisitor(Compiler compiler) throws JasperException {
            this.pageInfo = compiler.getPageInfo();
            this.err = compiler.getErrorDispatcher();
        }

        public void visit(Node.IncludeDirective n) throws JasperException {
            // Since pageDirectiveSeen flag only applies to the Current page
            // save it here and restore it after the file is included.
            boolean pageEncodingSeenSave = pageEncodingSeen;
            pageEncodingSeen = false;
            visitBody(n);
            pageEncodingSeen = pageEncodingSeenSave;
        }

        public void visit(Node.PageDirective n) throws JasperException {

            JspUtil.checkAttributes(TagConstants.PAGE_DIRECTIVE_ACTION, n, pageDirectiveAttrs,
                    err);

            // JSP.2.10.1
            Attributes attrs = n.getAttributes();
            for (int i = 0; attrs != null && i < attrs.getLength(); i++) {
                String attr = attrs.getQName(i);
                String value = attrs.getValue(i);

                if ("language".equals(attr)) {
                    if (pageInfo.getLanguage(false) == null) {
                        pageInfo.setLanguage(value, n, err, true);
                    } else if (!pageInfo.getLanguage(false).equals(value)) {
                        err.jspError(n.getStart(), MESSAGES.invalidConflictingPageDirectiveAttribute
                                (attr, pageInfo.getLanguage(false), value));
                    }
                } else if ("extends".equals(attr)) {
                    if (pageInfo.getExtends(false) == null) {
                        pageInfo.setExtends(value, n);
                    } else if (!pageInfo.getExtends(false).equals(value)) {
                        err.jspError(n.getStart(), MESSAGES.invalidConflictingPageDirectiveAttribute
                                (attr, pageInfo.getExtends(false), value));
                    }
                } else if ("contentType".equals(attr)) {
                    if (pageInfo.getContentType() == null) {
                        pageInfo.setContentType(value);
                    } else if (!pageInfo.getContentType().equals(value)) {
                        err.jspError(n.getStart(), MESSAGES.invalidConflictingPageDirectiveAttribute
                                (attr, pageInfo.getContentType(), value));
                    }
                } else if ("session".equals(attr)) {
                    if (pageInfo.getSession() == null) {
                        pageInfo.setSession(value, n, err);
                    } else if (!pageInfo.getSession().equals(value)) {
                        err.jspError(n.getStart(), MESSAGES.invalidConflictingPageDirectiveAttribute
                                (attr, pageInfo.getSession(), value));
                    }
                } else if ("buffer".equals(attr)) {
                    if (pageInfo.getBufferValue() == null) {
                        pageInfo.setBufferValue(value, n, err);
                    } else if (!pageInfo.getBufferValue().equals(value)) {
                        err.jspError(n.getStart(), MESSAGES.invalidConflictingPageDirectiveAttribute
                                (attr, pageInfo.getBufferValue(), value));
                    }
                } else if ("autoFlush".equals(attr)) {
                    if (pageInfo.getAutoFlush() == null) {
                        pageInfo.setAutoFlush(value, n, err);
                    } else if (!pageInfo.getAutoFlush().equals(value)) {
                        err.jspError(n.getStart(), MESSAGES.invalidConflictingPageDirectiveAttribute
                                (attr, pageInfo.getAutoFlush(), value));
                    }
                } else if ("isThreadSafe".equals(attr)) {
                    if (pageInfo.getIsThreadSafe() == null) {
                        pageInfo.setIsThreadSafe(value, n, err);
                    } else if (!pageInfo.getIsThreadSafe().equals(value)) {
                        err.jspError(n.getStart(), MESSAGES.invalidConflictingPageDirectiveAttribute
                                (attr, pageInfo.getIsThreadSafe(), value));
                    }
                } else if ("isELIgnored".equals(attr)) {
                    if (pageInfo.getIsELIgnored() == null) {
                        pageInfo.setIsELIgnored(value, n, err, true);
                    } else if (!pageInfo.getIsELIgnored().equals(value)) {
                        err.jspError(n.getStart(), MESSAGES.invalidConflictingPageDirectiveAttribute
                                (attr, pageInfo.getIsELIgnored(), value));
                    }
                } else if ("isErrorPage".equals(attr)) {
                    if (pageInfo.getIsErrorPage() == null) {
                        pageInfo.setIsErrorPage(value, n, err);
                    } else if (!pageInfo.getIsErrorPage().equals(value)) {
                        err.jspError(n.getStart(), MESSAGES.invalidConflictingPageDirectiveAttribute
                                (attr, pageInfo.getIsErrorPage(), value));
                    }
                } else if ("errorPage".equals(attr)) {
                    if (pageInfo.getErrorPage() == null) {
                        pageInfo.setErrorPage(value);
                    } else if (!pageInfo.getErrorPage().equals(value)) {
                        err.jspError(n.getStart(), MESSAGES.invalidConflictingPageDirectiveAttribute
                                (attr, pageInfo.getErrorPage(), value));
                    }
                } else if ("info".equals(attr)) {
                    if (pageInfo.getInfo() == null) {
                        pageInfo.setInfo(value);
                    } else if (!pageInfo.getInfo().equals(value)) {
                        err.jspError(n.getStart(), MESSAGES.invalidConflictingPageDirectiveAttribute
                                (attr, pageInfo.getInfo(), value));
                    }
                } else if ("pageEncoding".equals(attr)) {
                    if (pageEncodingSeen)
                        err.jspError(n.getStart(), MESSAGES.invalidDuplicatePageDirectiveAttribute(attr));
                    // 'pageEncoding' can occur at most once per file
                    pageEncodingSeen = true;
                    String actual = comparePageEncodings(value, n);
                    n.getRoot().setPageEncoding(actual);
                } else if ("deferredSyntaxAllowedAsLiteral".equals(attr)) {
                    if (pageInfo.getDeferredSyntaxAllowedAsLiteral() == null) {
                        pageInfo.setDeferredSyntaxAllowedAsLiteral(value, n,
                                err, true);
                    } else if (!pageInfo.getDeferredSyntaxAllowedAsLiteral()
                            .equals(value)) {
                        err.jspError(n.getStart(), MESSAGES.invalidConflictingPageDirectiveAttribute
                                (attr, pageInfo.getDeferredSyntaxAllowedAsLiteral(), value));
                    }
                } else if ("trimDirectiveWhitespaces".equals(attr)) {
                    if (pageInfo.getTrimDirectiveWhitespaces() == null) {
                        pageInfo.setTrimDirectiveWhitespaces(value, n, err,
                                true);
                    } else if (!pageInfo.getTrimDirectiveWhitespaces().equals(
                            value)) {
                        err.jspError(n.getStart(), MESSAGES.invalidConflictingPageDirectiveAttribute
                                (attr, pageInfo.getTrimDirectiveWhitespaces(), value));
                    }
                }
            }

            // Check for bad combinations
            if (pageInfo.getBuffer() == 0 && !pageInfo.isAutoFlush())
                err.jspError(n.getStart(), MESSAGES.invalidConflictingPageDirectiveAutoFlushBuffer());

            // Attributes for imports for this node have been processed by
            // the parsers, just add them to pageInfo.
            pageInfo.addImports(n.getImports());
        }

        public void visit(Node.TagDirective n) throws JasperException {
            // Note: Most of the validation is done in TagFileProcessor
            // when it created a TagInfo object from the
            // tag file in which the directive appeared.

            // This method does additional processing to collect page info

            Attributes attrs = n.getAttributes();
            for (int i = 0; attrs != null && i < attrs.getLength(); i++) {
                String attr = attrs.getQName(i);
                String value = attrs.getValue(i);

                if ("language".equals(attr)) {
                    if (pageInfo.getLanguage(false) == null) {
                        pageInfo.setLanguage(value, n, err, false);
                    } else if (!pageInfo.getLanguage(false).equals(value)) {
                        err.jspError(n.getStart(), MESSAGES.invalidConflictingTagDirectiveAttributeValues
                                (attr, pageInfo.getLanguage(false), value));
                    }
                } else if ("isELIgnored".equals(attr)) {
                    if (pageInfo.getIsELIgnored() == null) {
                        pageInfo.setIsELIgnored(value, n, err, false);
                    } else if (!pageInfo.getIsELIgnored().equals(value)) {
                        err.jspError(n.getStart(), MESSAGES.invalidConflictingTagDirectiveAttributeValues
                                (attr, pageInfo.getIsELIgnored(), value));
                    }
                } else if ("pageEncoding".equals(attr)) {
                    if (pageEncodingSeen)
                        err.jspError(n.getStart(), MESSAGES.invalidDuplicateTagDirectiveAttribute(attr));
                    pageEncodingSeen = true;
                    compareTagEncodings(value, n);
                    n.getRoot().setPageEncoding(value);
                } else if ("deferredSyntaxAllowedAsLiteral".equals(attr)) {
                    if (pageInfo.getDeferredSyntaxAllowedAsLiteral() == null) {
                        pageInfo.setDeferredSyntaxAllowedAsLiteral(value, n,
                                err, false);
                    } else if (!pageInfo.getDeferredSyntaxAllowedAsLiteral()
                            .equals(value)) {
                        err.jspError(n.getStart(), MESSAGES.invalidConflictingTagDirectiveAttributeValues
                                (attr, pageInfo.getDeferredSyntaxAllowedAsLiteral(), value));
                    }
                } else if ("trimDirectiveWhitespaces".equals(attr)) {
                    if (pageInfo.getTrimDirectiveWhitespaces() == null) {
                        pageInfo.setTrimDirectiveWhitespaces(value, n, err,
                                false);
                    } else if (!pageInfo.getTrimDirectiveWhitespaces().equals(
                            value)) {
                        err.jspError(n.getStart(), MESSAGES.invalidConflictingTagDirectiveAttributeValues
                                (attr, pageInfo.getTrimDirectiveWhitespaces(), value));
                    }
                }
            }

            // Attributes for imports for this node have been processed by
            // the parsers, just add them to pageInfo.
            pageInfo.addImports(n.getImports());
        }

        public void visit(Node.AttributeDirective n) throws JasperException {
            // Do nothing, since this attribute directive has already been
            // validated by TagFileProcessor when it created a TagInfo object
            // from the tag file in which the directive appeared
        }

        public void visit(Node.VariableDirective n) throws JasperException {
            // Do nothing, since this variable directive has already been
            // validated by TagFileProcessor when it created a TagInfo object
            // from the tag file in which the directive appeared
        }

        /*
         * Compares page encodings specified in various places, and throws
         * exception in case of page encoding mismatch.
         * 
         * @param pageDirEnc The value of the pageEncoding attribute of the page
         * directive @param pageDir The page directive node
         * 
         * @throws JasperException in case of page encoding mismatch
         */
        private String comparePageEncodings(String thePageDirEnc,
                Node.PageDirective pageDir) throws JasperException {

            Node.Root root = pageDir.getRoot();
            String configEnc = root.getJspConfigPageEncoding();
            String pageDirEnc = thePageDirEnc.toUpperCase(Locale.ENGLISH);

            /*
             * Compare the 'pageEncoding' attribute of the page directive with
             * the encoding specified in the JSP config element whose URL
             * pattern matches this page. Treat "UTF-16", "UTF-16BE", and
             * "UTF-16LE" as identical.
             */
            if (configEnc != null) {
                configEnc = configEnc.toUpperCase(Locale.ENGLISH);
                if (!pageDirEnc.equals(configEnc)
                        && (!pageDirEnc.startsWith("UTF-16") || !configEnc
                                .startsWith("UTF-16"))) {
                    err.jspError(pageDir.getStart(), 
                            MESSAGES.pageEncodingConflictJspPropertyGroup(configEnc, pageDirEnc));
                } else {
                    return configEnc;
                }
            }

            /*
             * Compare the 'pageEncoding' attribute of the page directive with
             * the encoding specified in the XML prolog (only for XML syntax,
             * and only if JSP document contains XML prolog with encoding
             * declaration). Treat "UTF-16", "UTF-16BE", and "UTF-16LE" as
             * identical.
             */
            if ((root.isXmlSyntax() && root.isEncodingSpecifiedInProlog()) || root.isBomPresent()) {
                String pageEnc = root.getPageEncoding().toUpperCase(Locale.ENGLISH);
                if (!pageDirEnc.equals(pageEnc)
                        && (!pageDirEnc.startsWith("UTF-16") || !pageEnc
                                .startsWith("UTF-16"))) {
                    err.jspError(pageDir.getStart(),
                            MESSAGES.pageEncodingConflictProlog(pageEnc, pageDirEnc));
                } else {
                    return pageEnc;
                }
            }
            
            return pageDirEnc;
        }
        
        /*
         * Compares page encodings specified in various places, and throws
         * exception in case of page encoding mismatch.
         * 
         * @param pageDirEnc The value of the pageEncoding attribute of the page
         * directive @param pageDir The page directive node
         * 
         * @throws JasperException in case of page encoding mismatch
         */
        private void compareTagEncodings(String pageDirEnc,
                Node.TagDirective pageDir) throws JasperException {

            Node.Root root = pageDir.getRoot();

            /*
             * Compare the 'pageEncoding' attribute of the page directive with
             * the encoding specified in the XML prolog (only for XML syntax,
             * and only if JSP document contains XML prolog with encoding
             * declaration). Treat "UTF-16", "UTF-16BE", and "UTF-16LE" as
             * identical.
             */
            if ((root.isXmlSyntax() && root.isEncodingSpecifiedInProlog()) || root.isBomPresent()) {
                String pageEnc = root.getPageEncoding();
                if (!pageDirEnc.equals(pageEnc)
                        && (!pageDirEnc.startsWith("UTF-16") || !pageEnc
                                .startsWith("UTF-16"))) {
                    err.jspError(pageDir.getStart(),
                            MESSAGES.pageEncodingConflictProlog(pageEnc, pageDirEnc));
                }
            }
        }

    }

    /**
     * A visitor for validating nodes other than page directives
     */
    static class ValidateVisitor extends Node.Visitor {

        private PageInfo pageInfo;

        private ErrorDispatcher err;

        private ClassLoader loader;

        private final StringBuilder buf = new StringBuilder(32);

        private static final JspUtil.ValidAttribute[] jspRootAttrs = {
                new JspUtil.ValidAttribute("xsi:schemaLocation"),
                new JspUtil.ValidAttribute("version", true) };

        private static final JspUtil.ValidAttribute[] includeDirectiveAttrs = { new JspUtil.ValidAttribute(
                "file", true) };

        private static final JspUtil.ValidAttribute[] taglibDirectiveAttrs = {
                new JspUtil.ValidAttribute("uri"),
                new JspUtil.ValidAttribute("tagdir"),
                new JspUtil.ValidAttribute("prefix", true) };

        private static final JspUtil.ValidAttribute[] includeActionAttrs = {
                new JspUtil.ValidAttribute("page", true, true),
                new JspUtil.ValidAttribute("flush") };

        private static final JspUtil.ValidAttribute[] paramActionAttrs = {
                new JspUtil.ValidAttribute("name", true),
                new JspUtil.ValidAttribute("value", true, true) };

        private static final JspUtil.ValidAttribute[] forwardActionAttrs = { new JspUtil.ValidAttribute(
                "page", true, true) };

        private static final JspUtil.ValidAttribute[] getPropertyAttrs = {
                new JspUtil.ValidAttribute("name", true),
                new JspUtil.ValidAttribute("property", true) };

        private static final JspUtil.ValidAttribute[] setPropertyAttrs = {
                new JspUtil.ValidAttribute("name", true),
                new JspUtil.ValidAttribute("property", true),
                new JspUtil.ValidAttribute("value", false, true),
                new JspUtil.ValidAttribute("param") };

        private static final JspUtil.ValidAttribute[] useBeanAttrs = {
                new JspUtil.ValidAttribute("id", true),
                new JspUtil.ValidAttribute("scope"),
                new JspUtil.ValidAttribute("class"),
                new JspUtil.ValidAttribute("type"),
                new JspUtil.ValidAttribute("beanName", false, true) };

        private static final JspUtil.ValidAttribute[] plugInAttrs = {
                new JspUtil.ValidAttribute("type", true),
                new JspUtil.ValidAttribute("code", true),
                new JspUtil.ValidAttribute("codebase"),
                new JspUtil.ValidAttribute("align"),
                new JspUtil.ValidAttribute("archive"),
                new JspUtil.ValidAttribute("height", false, true),
                new JspUtil.ValidAttribute("hspace"),
                new JspUtil.ValidAttribute("jreversion"),
                new JspUtil.ValidAttribute("name"),
                new JspUtil.ValidAttribute("vspace"),
                new JspUtil.ValidAttribute("width", false, true),
                new JspUtil.ValidAttribute("nspluginurl"),
                new JspUtil.ValidAttribute("iepluginurl") };

        private static final JspUtil.ValidAttribute[] attributeAttrs = {
                new JspUtil.ValidAttribute("name", true),
                new JspUtil.ValidAttribute("omit"),
                new JspUtil.ValidAttribute("trim") };

        private static final JspUtil.ValidAttribute[] invokeAttrs = {
                new JspUtil.ValidAttribute("fragment", true),
                new JspUtil.ValidAttribute("var"),
                new JspUtil.ValidAttribute("varReader"),
                new JspUtil.ValidAttribute("scope") };

        private static final JspUtil.ValidAttribute[] doBodyAttrs = {
                new JspUtil.ValidAttribute("var"),
                new JspUtil.ValidAttribute("varReader"),
                new JspUtil.ValidAttribute("scope") };

        private static final JspUtil.ValidAttribute[] jspOutputAttrs = {
                new JspUtil.ValidAttribute("omit-xml-declaration"),
                new JspUtil.ValidAttribute("doctype-root-element"),
                new JspUtil.ValidAttribute("doctype-public"),
                new JspUtil.ValidAttribute("doctype-system") };

        private final ExpressionFactory expressionFactory;

        /*
         * Constructor
         */
        ValidateVisitor(Compiler compiler) {
            this.pageInfo = compiler.getPageInfo();
            this.err = compiler.getErrorDispatcher();
            this.loader = compiler.getCompilationContext().getClassLoader();
            // Get the cached EL expression factory for this context
            expressionFactory =
                    JspFactory.getDefaultFactory().getJspApplicationContext(
                    compiler.getCompilationContext().getServletContext()).
                    getExpressionFactory();
        }

        public void visit(Node.JspRoot n) throws JasperException {
            JspUtil.checkAttributes(TagConstants.ROOT_ACTION, n, jspRootAttrs, err);
            String version = n.getTextAttribute("version");
            if (!version.equals("1.2") && !version.equals("2.0") && !version.equals("2.1") && !version.equals("2.2")) {
                err.jspError(n.getStart(), MESSAGES.invalidJspVersionNumber(version));
            }
            visitBody(n);
        }

        public void visit(Node.IncludeDirective n) throws JasperException {
            JspUtil.checkAttributes(TagConstants.INCLUDE_DIRECTIVE_ACTION, n,
                    includeDirectiveAttrs, err);
            visitBody(n);
        }

        public void visit(Node.TaglibDirective n) throws JasperException {
            JspUtil.checkAttributes(TagConstants.TAGLIB_DIRECTIVE_ACTION, n,
                    taglibDirectiveAttrs, err);
            // Either 'uri' or 'tagdir' attribute must be specified
            String uri = n.getAttributeValue("uri");
            String tagdir = n.getAttributeValue("tagdir");
            if (uri == null && tagdir == null) {
                err.jspError(n.getStart(), MESSAGES.invalidTaglibDirectiveMissingLocation());
            }
            if (uri != null && tagdir != null) {
                err.jspError(n.getStart(), MESSAGES.invalidTaglibDirectiveConflictingLocation());
            }
        }

        public void visit(Node.ParamAction n) throws JasperException {
            JspUtil.checkAttributes(TagConstants.PARAM_ACTION, n, paramActionAttrs, err);
            // make sure the value of the 'name' attribute is not a
            // request-time expression
            throwErrorIfExpression(n, "name", "jsp:param");
            n.setValue(getJspAttribute(null, "value", null, null, n
                    .getAttributeValue("value"), java.lang.String.class, n,
                    false));
            visitBody(n);
        }

        public void visit(Node.ParamsAction n) throws JasperException {
            // Make sure we've got at least one nested jsp:param
            Node.Nodes subElems = n.getBody();
            if (subElems == null) {
                err.jspError(n.getStart(), MESSAGES.invalidEmptyJspParams());
            }
            visitBody(n);
        }

        public void visit(Node.IncludeAction n) throws JasperException {
            JspUtil.checkAttributes(TagConstants.INCLUDE_ACTION, n, includeActionAttrs,
                    err);
            n.setPage(getJspAttribute(null, "page", null, null, n
                    .getAttributeValue("page"), java.lang.String.class, n,
                    false));
            visitBody(n);
        };

        public void visit(Node.ForwardAction n) throws JasperException {
            JspUtil.checkAttributes(TagConstants.FORWARD_ACTION, n, forwardActionAttrs, err);
            n.setPage(getJspAttribute(null, "page", null, null, n
                    .getAttributeValue("page"), java.lang.String.class, n,
                    false));
            visitBody(n);
        }

        public void visit(Node.GetProperty n) throws JasperException {
            JspUtil.checkAttributes(TagConstants.GET_PROPERTY_ACTION, n, getPropertyAttrs, err);
        }

        public void visit(Node.SetProperty n) throws JasperException {
            JspUtil.checkAttributes(TagConstants.SET_PROPERTY_ACTION, n, setPropertyAttrs, err);
            String property = n.getTextAttribute("property");
            String param = n.getTextAttribute("param");
            String value = n.getAttributeValue("value");

            n.setValue(getJspAttribute(null, "value", null, null, value,
                    java.lang.Object.class, n, false));

            boolean valueSpecified = n.getValue() != null;

            if ("*".equals(property)) {
                if (param != null || valueSpecified)
                    err.jspError(n.getStart(), MESSAGES.invalidSetProperty());
            } else if (param != null && valueSpecified) {
                err.jspError(n.getStart(), MESSAGES.invalidSetPropertyEitherParam());
            }

            visitBody(n);
        }

        public void visit(Node.UseBean n) throws JasperException {
            JspUtil.checkAttributes(TagConstants.USE_BEAN_ACTION, n, useBeanAttrs, err);

            String name = n.getTextAttribute("id");
            String scope = n.getTextAttribute("scope");
            JspUtil.checkScope(scope, n, err);
            String className = n.getTextAttribute("class");
            String type = n.getTextAttribute("type");
            BeanRepository beanInfo = pageInfo.getBeanRepository();

            if (className == null && type == null)
                err.jspError(n.getStart(), MESSAGES.missingUseBeanType());

            if (beanInfo.checkVariable(name))
                err.jspError(n.getStart(), MESSAGES.duplicateUseBeanName(name));

            if ("session".equals(scope) && !pageInfo.isSession())
                err.jspError(n.getStart(), MESSAGES.cannotAccessSessionScopeWithUseBean());

            Node.JspAttribute jattr = getJspAttribute(null, "beanName", null,
                    null, n.getAttributeValue("beanName"),
                    java.lang.String.class, n, false);
            n.setBeanName(jattr);
            if (className != null && jattr != null)
                err.jspError(n.getStart(), MESSAGES.cannotUseBothAttributeAndTypeInUseBean());

            if (className == null)
                className = type;

            beanInfo.addBean(n, name, className, scope);

            visitBody(n);
        }

        public void visit(Node.PlugIn n) throws JasperException {
            JspUtil.checkAttributes(TagConstants.PLUGIN_ACTION, n, plugInAttrs, err);

            throwErrorIfExpression(n, "type", "jsp:plugin");
            throwErrorIfExpression(n, "code", "jsp:plugin");
            throwErrorIfExpression(n, "codebase", "jsp:plugin");
            throwErrorIfExpression(n, "align", "jsp:plugin");
            throwErrorIfExpression(n, "archive", "jsp:plugin");
            throwErrorIfExpression(n, "hspace", "jsp:plugin");
            throwErrorIfExpression(n, "jreversion", "jsp:plugin");
            throwErrorIfExpression(n, "name", "jsp:plugin");
            throwErrorIfExpression(n, "vspace", "jsp:plugin");
            throwErrorIfExpression(n, "nspluginurl", "jsp:plugin");
            throwErrorIfExpression(n, "iepluginurl", "jsp:plugin");

            String type = n.getTextAttribute("type");
            if (type == null)
                err.jspError(n.getStart(), MESSAGES.missingPluginType());
            if (!type.equals("bean") && !type.equals("applet"))
                err.jspError(n.getStart(), MESSAGES.badPluginType(type));
            if (n.getTextAttribute("code") == null)
                err.jspError(n.getStart(), MESSAGES.missingPluginCode());

            Node.JspAttribute width = getJspAttribute(null, "width", null,
                    null, n.getAttributeValue("width"), java.lang.String.class,
                    n, false);
            n.setWidth(width);

            Node.JspAttribute height = getJspAttribute(null, "height", null,
                    null, n.getAttributeValue("height"),
                    java.lang.String.class, n, false);
            n.setHeight(height);

            visitBody(n);
        }

        public void visit(Node.NamedAttribute n) throws JasperException {
            JspUtil.checkAttributes(TagConstants.ATTRIBUTE_ACTION, n, attributeAttrs, err);
            visitBody(n);
            if (n.getOmit() != null) {
                Attributes attrs = n.getAttributes();
                for (int i = 0; i < attrs.getLength(); i++) {
                    if ("omit".equals(attrs.getLocalName(i))) {
                        n.setOmitAttribute(getJspAttribute(null, attrs.getQName(i),
                                attrs.getURI(i), attrs.getLocalName(i), 
                                attrs.getValue(i), java.lang.Boolean.class, 
                                n, false));
                    }
                }
            }
        }

        public void visit(Node.JspBody n) throws JasperException {
            visitBody(n);
        }

        public void visit(Node.Declaration n) throws JasperException {
            if (pageInfo.isScriptingInvalid()) {
                err.jspError(n.getStart(), MESSAGES.invalidScriptingElement());
            }
        }

        public void visit(Node.Expression n) throws JasperException {
            if (pageInfo.isScriptingInvalid()) {
                err.jspError(n.getStart(), MESSAGES.invalidScriptingElement());
            }
        }

        public void visit(Node.Scriptlet n) throws JasperException {
            if (pageInfo.isScriptingInvalid()) {
                err.jspError(n.getStart(), MESSAGES.invalidScriptingElement());
            }
        }

        public void visit(Node.ELExpression n) throws JasperException {
            // exit if we are ignoring EL all together
            if (pageInfo.isELIgnored())
                return;

            // JSP.2.2 - '#{' not allowed in template text
            if (n.getType() == '#') {
                if (!pageInfo.isDeferredSyntaxAllowedAsLiteral()) {
                    err.jspError(n.getStart(), MESSAGES.invalidDeferredExpressionInTemplateText());
                } else {
                    return;
                }
            }

            // build expression
            StringBuilder expr = this.getBuffer();
            expr.append(n.getType()).append('{').append(n.getText())
                    .append('}');
            ELNode.Nodes el = ELParser.parse(expr.toString(), pageInfo
                    .isDeferredSyntaxAllowedAsLiteral());

            // validate/prepare expression
            prepareExpression(el, n, expr.toString());

            // store it
            n.setEL(el);
        }

        public void visit(Node.UninterpretedTag n) throws JasperException {
            if (n.getNamedAttributeNodes().size() != 0) {
                err.jspError(n.getStart(), MESSAGES.invalidJspAttribute());
            }

            Attributes attrs = n.getAttributes();
            if (attrs != null) {
                int attrSize = attrs.getLength();
                Node.JspAttribute[] jspAttrs = new Node.JspAttribute[attrSize];
                for (int i = 0; i < attrSize; i++) {
                    // JSP.2.2 - '#{' not allowed in template text
                    String value = attrs.getValue(i);
                    if (!pageInfo.isDeferredSyntaxAllowedAsLiteral()) {
                        if (containsDeferredSyntax(value)) {
                            err.jspError(n.getStart(), 
                                    MESSAGES.invalidDeferredExpressionInTemplateText());
                        }
                    }
                    jspAttrs[i] = getJspAttribute(null, attrs.getQName(i),
                            attrs.getURI(i), attrs.getLocalName(i), value, 
                            java.lang.Object.class, n, false);
                }
                n.setJspAttributes(jspAttrs);
            }

            visitBody(n);
        }

        /**
         * Look for a #{ sequence that isn't preceded by \.
         */
        private boolean containsDeferredSyntax(String value) {
            if (value == null) {
                return false;
            }
            
            int i = 0;
            int len = value.length();
            boolean prevCharIsEscape = false;
            while (i < value.length()) {
                char c = value.charAt(i);
                if (c == '#' && (i+1) < len && value.charAt(i+1) == '{' && !prevCharIsEscape) {
                    return true;
                } else if (c == '\\') {
                    prevCharIsEscape = true;
                } else {
                    prevCharIsEscape = false;
                }
                i++;
            }
            return false;
        }

        public void visit(Node.CustomTag n) throws JasperException {

            TagInfo tagInfo = n.getTagInfo();
            if (tagInfo == null) {
                err.jspError(n.getStart(), MESSAGES.missingTagInfo(n.getQName()));
            }

            /*
             * The bodyconet of a SimpleTag cannot be JSP.
             */
            if (n.implementsSimpleTag()
                    && tagInfo.getBodyContent().equalsIgnoreCase(
                            TagInfo.BODY_CONTENT_JSP)) {
                err.jspError(n.getStart(), MESSAGES.invalidSimpleTagBodyContent(tagInfo
                        .getTagClassName()));
            }

            /*
             * If the tag handler declares in the TLD that it supports dynamic
             * attributes, it also must implement the DynamicAttributes
             * interface.
             */
            if (tagInfo.hasDynamicAttributes()
                    && !n.implementsDynamicAttributes()) {
                err.jspError(n.getStart(), MESSAGES.unimplementedDynamicAttributes(n.getQName()));
            }

            /*
             * Make sure all required attributes are present, either as
             * attributes or named attributes (<jsp:attribute>). Also make sure
             * that the same attribute is not specified in both attributes or
             * named attributes.
             */
            TagAttributeInfo[] tldAttrs = tagInfo.getAttributes();
            String customActionUri = n.getURI();
            Attributes attrs = n.getAttributes();
            int attrsSize = (attrs == null) ? 0 : attrs.getLength();
            for (int i = 0; i < tldAttrs.length; i++) {
                String attr = null;
                if (attrs != null) {
                    attr = attrs.getValue(tldAttrs[i].getName());
                    if (attr == null) {
                        attr = attrs.getValue(customActionUri, tldAttrs[i]
                                .getName());
                    }
                }
                Node.NamedAttribute na = n.getNamedAttributeNode(tldAttrs[i]
                        .getName());

                if (tldAttrs[i].isRequired() && attr == null && na == null) {
                    err.jspError(n.getStart(), MESSAGES.missingMandatoryAttribute(n.getLocalName(), tldAttrs[i]
                            .getName()));
                }
                if (attr != null && na != null) {
                    err.jspError(n.getStart(), MESSAGES.duplicateAttribute(tldAttrs[i].getName()));
                }
            }

            Node.Nodes naNodes = n.getNamedAttributeNodes();
            int jspAttrsSize = naNodes.size() + attrsSize;
            Node.JspAttribute[] jspAttrs = null;
            if (jspAttrsSize > 0) {
                jspAttrs = new Node.JspAttribute[jspAttrsSize];
            }
            Hashtable<String, Object> tagDataAttrs = new Hashtable<String, Object>(attrsSize);

            checkXmlAttributes(n, jspAttrs, tagDataAttrs);
            checkNamedAttributes(n, jspAttrs, attrsSize, tagDataAttrs);

            TagData tagData = new TagData(tagDataAttrs);

            // JSP.C1: It is a (translation time) error for an action that
            // has one or more variable subelements to have a TagExtraInfo
            // class that returns a non-null object.
            TagExtraInfo tei = tagInfo.getTagExtraInfo();
            if (tei != null && tei.getVariableInfo(tagData) != null
                    && tei.getVariableInfo(tagData).length > 0
                    && tagInfo.getTagVariableInfos().length > 0) {
                err.jspError(n.getStart(), MESSAGES.invalidTeiWithVariableSubelements(n.getQName()));
            }

            n.setTagData(tagData);
            n.setJspAttributes(jspAttrs);

            visitBody(n);
        }

        public void visit(Node.JspElement n) throws JasperException {

            Attributes attrs = n.getAttributes();
            if (attrs == null) {
                err.jspError(n.getStart(), MESSAGES.missingMandatoryAttributes());
            }
            int xmlAttrLen = attrs.getLength();

            Node.Nodes namedAttrs = n.getNamedAttributeNodes();

            // XML-style 'name' attribute, which is mandatory, must not be
            // included in JspAttribute array
            int jspAttrSize = xmlAttrLen - 1 + namedAttrs.size();

            Node.JspAttribute[] jspAttrs = new Node.JspAttribute[jspAttrSize];
            int jspAttrIndex = 0;

            // Process XML-style attributes
            for (int i = 0; i < xmlAttrLen; i++) {
                if ("name".equals(attrs.getLocalName(i))) {
                    n.setNameAttribute(getJspAttribute(null, attrs.getQName(i),
                            attrs.getURI(i), attrs.getLocalName(i), attrs
                                    .getValue(i), java.lang.String.class, n,
                            false));
                } else {
                    if (jspAttrIndex < jspAttrSize) {
                        jspAttrs[jspAttrIndex++] = getJspAttribute(null, attrs
                                .getQName(i), attrs.getURI(i), attrs
                                .getLocalName(i), attrs.getValue(i),
                                java.lang.Object.class, n, false);
                    }
                }
            }
            if (n.getNameAttribute() == null) {
                err.jspError(n.getStart(), MESSAGES.missingMandatoryNameAttribute());
            }

            // Process named attributes
            for (int i = 0; i < namedAttrs.size(); i++) {
                Node.NamedAttribute na = (Node.NamedAttribute) namedAttrs
                        .getNode(i);
                jspAttrs[jspAttrIndex++] = new Node.JspAttribute(na, null,
                        false);
            }

            n.setJspAttributes(jspAttrs);

            visitBody(n);
        }

        public void visit(Node.JspOutput n) throws JasperException {
            JspUtil.checkAttributes(TagConstants.OUTPUT_ACTION, n, jspOutputAttrs, err);

            if (n.getBody() != null) {
                err.jspError(n.getStart(), MESSAGES.invalidJspOutputBody());
            }

            String omitXmlDecl = n.getAttributeValue("omit-xml-declaration");
            String doctypeName = n.getAttributeValue("doctype-root-element");
            String doctypePublic = n.getAttributeValue("doctype-public");
            String doctypeSystem = n.getAttributeValue("doctype-system");

            String omitXmlDeclOld = pageInfo.getOmitXmlDecl();
            String doctypeNameOld = pageInfo.getDoctypeName();
            String doctypePublicOld = pageInfo.getDoctypePublic();
            String doctypeSystemOld = pageInfo.getDoctypeSystem();

            if (omitXmlDecl != null && omitXmlDeclOld != null
                    && !omitXmlDecl.equals(omitXmlDeclOld)) {
                err.jspError(n.getStart(), MESSAGES.invalidJspOutputConflict
                        ("omit-xml-declaration", omitXmlDeclOld, omitXmlDecl));
            }

            if (doctypeName != null && doctypeNameOld != null
                    && !doctypeName.equals(doctypeNameOld)) {
                err.jspError(n.getStart(), MESSAGES.invalidJspOutputConflict
                        ("doctype-root-element", doctypeNameOld, doctypeName));
            }

            if (doctypePublic != null && doctypePublicOld != null
                    && !doctypePublic.equals(doctypePublicOld)) {
                err.jspError(n.getStart(), MESSAGES.invalidJspOutputConflict
                        ("doctype-public", doctypePublicOld, doctypePublic));
            }

            if (doctypeSystem != null && doctypeSystemOld != null
                    && !doctypeSystem.equals(doctypeSystemOld)) {
                err.jspError(n.getStart(), MESSAGES.invalidJspOutputConflict
                        ("doctype-system", doctypeSystemOld, doctypeSystem));
            }

            if (doctypeName == null && doctypeSystem != null
                    || doctypeName != null && doctypeSystem == null) {
                err.jspError(n.getStart(), MESSAGES.errorJspOutputDoctype());
            }

            if (doctypePublic != null && doctypeSystem == null) {
                err.jspError(n.getStart(), MESSAGES.errorJspOutputMissingDoctype());
            }

            if (omitXmlDecl != null) {
                pageInfo.setOmitXmlDecl(omitXmlDecl);
            }
            if (doctypeName != null) {
                pageInfo.setDoctypeName(doctypeName);
            }
            if (doctypeSystem != null) {
                pageInfo.setDoctypeSystem(doctypeSystem);
            }
            if (doctypePublic != null) {
                pageInfo.setDoctypePublic(doctypePublic);
            }
        }

        public void visit(Node.InvokeAction n) throws JasperException {

            JspUtil.checkAttributes(TagConstants.INVOKE_ACTION, n, invokeAttrs, err);

            String scope = n.getTextAttribute("scope");
            JspUtil.checkScope(scope, n, err);

            String var = n.getTextAttribute("var");
            String varReader = n.getTextAttribute("varReader");
            if (scope != null && var == null && varReader == null) {
                err.jspError(n.getStart(), MESSAGES.missingVarAttribute());
            }
            if (var != null && varReader != null) {
                err.jspError(n.getStart(), MESSAGES.errorBothVarAttributes());
            }
        }

        public void visit(Node.DoBodyAction n) throws JasperException {

            JspUtil.checkAttributes(TagConstants.DOBODY_ACTION, n, doBodyAttrs, err);

            String scope = n.getTextAttribute("scope");
            JspUtil.checkScope(scope, n, err);

            String var = n.getTextAttribute("var");
            String varReader = n.getTextAttribute("varReader");
            if (scope != null && var == null && varReader == null) {
                err.jspError(n.getStart(), MESSAGES.missingVarAttribute());
            }
            if (var != null && varReader != null) {
                err.jspError(n.getStart(), MESSAGES.errorBothVarAttributes());
            }
        }

        /*
         * Make sure the given custom action does not have any invalid
         * attributes.
         * 
         * A custom action and its declared attributes always belong to the same
         * namespace, which is identified by the prefix name of the custom tag
         * invocation. For example, in this invocation:
         * 
         * <my:test a="1" b="2" c="3"/>, the action
         * 
         * "test" and its attributes "a", "b", and "c" all belong to the
         * namespace identified by the prefix "my". The above invocation would
         * be equivalent to:
         * 
         * <my:test my:a="1" my:b="2" my:c="3"/>
         * 
         * An action attribute may have a prefix different from that of the
         * action invocation only if the underlying tag handler supports dynamic
         * attributes, in which case the attribute with the different prefix is
         * considered a dynamic attribute.
         */
        private void checkXmlAttributes(Node.CustomTag n,
                Node.JspAttribute[] jspAttrs, Hashtable<String, Object> tagDataAttrs)
                throws JasperException {

            TagInfo tagInfo = n.getTagInfo();
            if (tagInfo == null) {
                err.jspError(n.getStart(), MESSAGES.missingTagInfo(n.getQName()));
            }
            TagAttributeInfo[] tldAttrs = tagInfo.getAttributes();
            Attributes attrs = n.getAttributes();

            for (int i = 0; attrs != null && i < attrs.getLength(); i++) {
                boolean found = false;
                
                boolean runtimeExpression = ((n.getRoot().isXmlSyntax() && attrs.getValue(i).startsWith("%="))
                        || (!n.getRoot().isXmlSyntax() && attrs.getValue(i).startsWith("<%=")));
                boolean elExpression = false;
                boolean deferred = false;
                double libraryVersion = Double.parseDouble(
                        tagInfo.getTagLibrary().getRequiredVersion());
                boolean deferredSyntaxAllowedAsLiteral =
                    pageInfo.isDeferredSyntaxAllowedAsLiteral() ||
                    libraryVersion < 2.1;

                ELNode.Nodes el = null;
                if (!runtimeExpression && !pageInfo.isELIgnored()) {
                    el = ELParser.parse(attrs.getValue(i),
                            deferredSyntaxAllowedAsLiteral);
                    Iterator<ELNode> nodes = el.iterator();
                    while (nodes.hasNext()) {
                        ELNode node = nodes.next();
                        if (node instanceof ELNode.Root) {
                            if (((ELNode.Root) node).getType() == '$') {
                                if (elExpression && deferred) {
                                    err.jspError(n.getStart(), MESSAGES.errorUsingBothElTypes());
                                }
                                elExpression = true;
                            } else if (((ELNode.Root) node).getType() == '#') {
                                if (elExpression && !deferred) {
                                    err.jspError(n.getStart(), MESSAGES.errorUsingBothElTypes());
                                }
                                elExpression = true;
                                deferred = true;
                            }
                        }
                    }
                }

                boolean expression = runtimeExpression || elExpression;
                
                for (int j = 0; tldAttrs != null && j < tldAttrs.length; j++) {
                    if (attrs.getLocalName(i).equals(tldAttrs[j].getName())
                            && (attrs.getURI(i) == null
                                    || attrs.getURI(i).length() == 0 || attrs
                                    .getURI(i).equals(n.getURI()))) {
                        
                        TagAttributeInfo tldAttr = tldAttrs[j];
                        if (tldAttr.canBeRequestTime()
                                || tldAttr.isDeferredMethod() || tldAttr.isDeferredValue()) { // JSP 2.1
                            
                            if (!expression) {
                                
                                String expectedType = null;
                                if (tldAttr.isDeferredMethod()) {
                                    // The String litteral must be castable to what is declared as type
                                    // for the attribute
                                    String m = tldAttr.getMethodSignature();
                                    if (m != null) {
                                        m = m.trim();
                                        int rti = m.indexOf(' ');
                                        if (rti > 0) {
                                            expectedType = m.substring(0, rti).trim();
                                        }
                                    } else {
                                        expectedType = "java.lang.Object";
                                    }
                                }
                                if ("void".equals(expectedType)) {
                                    // Can't specify a literal for a
                                    // deferred method with an expected type
                                    // of void - JSP.2.3.4
                                    err.jspError(n.getStart(),
                                            MESSAGES.errorUsingLiteralValueWithDeferredVoidReturnTyep(tldAttr.getName()));
                                }
                                if (tldAttr.isDeferredValue()) {
                                    // The String litteral must be castable to what is declared as type
                                    // for the attribute
                                    expectedType = tldAttr.getExpectedTypeName();
                                }
                                if (expectedType != null) {
                                    Class expectedClass = String.class;
                                    try {
                                        expectedClass = JspUtil.toClass(expectedType, loader);
                                    } catch (ClassNotFoundException e) {
                                        err.jspError
                                            (n.getStart(), MESSAGES.unknownAttributeType(tldAttr.getName(), expectedType));
                                    }
                                    // Check casting - not possible for all types
                                    if (String.class.equals(expectedClass) ||
                                            expectedClass == Long.TYPE ||
                                            expectedClass == Double.TYPE ||
                                            expectedClass == Byte.TYPE ||
                                            expectedClass == Short.TYPE ||
                                            expectedClass == Integer.TYPE ||
                                            expectedClass == Float.TYPE ||
                                            Number.class.isAssignableFrom(expectedClass) ||
                                            Character.class.equals(expectedClass) ||
                                            Character.TYPE == expectedClass ||
                                            Boolean.class.equals(expectedClass) ||
                                            Boolean.TYPE == expectedClass ||
                                            expectedClass.isEnum()) {
                                        try {
                                            expressionFactory.coerceToType(attrs.getValue(i), expectedClass);
                                        } catch (Exception e) {
                                            err.jspError
                                                (n.getStart(), MESSAGES.errorCoercingAttributeValue(
                                                        tldAttr.getName(), expectedType, attrs.getValue(i)));
                                        }
                                    }
                                }

                                jspAttrs[i] = new Node.JspAttribute(tldAttr,
                                        attrs.getQName(i), attrs.getURI(i), attrs
                                                .getLocalName(i),
                                        attrs.getValue(i), false, null, false);
                            } else {
                                
                                if (deferred && !tldAttr.isDeferredMethod() && !tldAttr.isDeferredValue()) {
                                    // No deferred expressions allowed for this attribute
                                    err.jspError(n.getStart(), 
                                            MESSAGES.noExpressionAllowedForAttribute(tldAttr.getName()));
                                }
                                if (!deferred && !tldAttr.canBeRequestTime()) {
                                    // Only deferred expressions are allowed for this attribute
                                    err.jspError(n.getStart(), 
                                            MESSAGES.noExpressionAllowedForAttribute(tldAttr.getName()));
                                }
                                
                                Class expectedType = String.class;
                                try {
                                    String typeStr = tldAttrs[j].getTypeName();
                                    if (tldAttrs[j].isFragment()) {
                                        expectedType = JspFragment.class;
                                    } else if (typeStr != null) {
                                        expectedType = JspUtil.toClass(typeStr,
                                                loader);
                                    }
                                    if (elExpression) {
                                        // El expression
                                        validateFunctions(el, n);
                                        jspAttrs[i] = new Node.JspAttribute(tldAttr,
                                                attrs.getQName(i), attrs.getURI(i), 
                                                attrs.getLocalName(i),
                                                attrs.getValue(i), false, el, false);
                                        ELContextImpl ctx = new ELContextImpl();
                                        ctx.setFunctionMapper(getFunctionMapper(el));
                                        try {
                                            jspAttrs[i].validateEL(this.pageInfo.getExpressionFactory(), ctx);
                                        } catch (ELException e) {
                                            err.jspError(n.getStart(), 
                                                    MESSAGES.invalidExpression(attrs.getValue(i)), e);
                                        }
                                    } else {
                                        // Runtime expression
                                        jspAttrs[i] = getJspAttribute(tldAttr,
                                                attrs.getQName(i), attrs.getURI(i),
                                                attrs.getLocalName(i), attrs
                                                .getValue(i), expectedType, n,
                                                false);
                                    }
                                } catch (ClassNotFoundException e) {
                                    err.jspError
                                        (n.getStart(), MESSAGES.unknownAttributeType
                                                (tldAttrs[j].getName(), tldAttrs[j].getTypeName()));
                                }
                            }
                            
                        } else {
                            // Attribute does not accept any expressions.
                            // Make sure its value does not contain any.
                            if (expression) {
                                err.jspError(n.getStart(), 
                                        MESSAGES.noExpressionAllowedForAttribute(tldAttr.getName()));
                            }
                            jspAttrs[i] = new Node.JspAttribute(tldAttr,
                                    attrs.getQName(i), attrs.getURI(i), attrs
                                            .getLocalName(i),
                                    attrs.getValue(i), false, null, false);
                        }
                        if (expression) {
                            tagDataAttrs.put(attrs.getQName(i),
                                    TagData.REQUEST_TIME_VALUE);
                        } else {
                            tagDataAttrs.put(attrs.getQName(i), attrs
                                    .getValue(i));
                        }
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    if (tagInfo.hasDynamicAttributes()) {
                        jspAttrs[i] = getJspAttribute(null, attrs.getQName(i),
                                attrs.getURI(i), attrs.getLocalName(i), attrs
                                        .getValue(i), java.lang.Object.class,
                                n, true);
                    } else {
                        err.jspError(n.getStart(), 
                                MESSAGES.invalidAttributeForTag(attrs.getQName(i), n.getLocalName()));
                    }
                }
            }
        }

        /*
         * Make sure the given custom action does not have any invalid named
         * attributes
         */
        private void checkNamedAttributes(Node.CustomTag n,
                Node.JspAttribute[] jspAttrs, int start, 
                Hashtable<String, Object> tagDataAttrs)
                throws JasperException {

            TagInfo tagInfo = n.getTagInfo();
            if (tagInfo == null) {
                err.jspError(n.getStart(), MESSAGES.missingTagInfo(n.getQName()));
            }
            TagAttributeInfo[] tldAttrs = tagInfo.getAttributes();
            Node.Nodes naNodes = n.getNamedAttributeNodes();

            for (int i = 0; i < naNodes.size(); i++) {
                Node.NamedAttribute na = (Node.NamedAttribute) naNodes
                        .getNode(i);
                boolean found = false;
                for (int j = 0; j < tldAttrs.length; j++) {
                    /*
                     * See above comment about namespace matches. For named
                     * attributes, we use the prefix instead of URI as the match
                     * criterion, because in the case of a JSP document, we'd
                     * have to keep track of which namespaces are in scope when
                     * parsing a named attribute, in order to determine the URI
                     * that the prefix of the named attribute's name matches to.
                     */
                    String attrPrefix = na.getPrefix();
                    if (na.getLocalName().equals(tldAttrs[j].getName())
                            && (attrPrefix == null || attrPrefix.length() == 0 || attrPrefix
                                    .equals(n.getPrefix()))) {
                        jspAttrs[start + i] = new Node.JspAttribute(na,
                                tldAttrs[j], false);
                        NamedAttributeVisitor nav = null;
                        if (na.getBody() != null) {
                            nav = new NamedAttributeVisitor();
                            na.getBody().visit(nav);
                        }
                        if (nav != null && nav.hasDynamicContent()) {
                            tagDataAttrs.put(na.getName(),
                                    TagData.REQUEST_TIME_VALUE);
                        } else {
                            tagDataAttrs.put(na.getName(), na.getText());
                        }
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    if (tagInfo.hasDynamicAttributes()) {
                        jspAttrs[start + i] = new Node.JspAttribute(na, null,
                                true);
                    } else {
                        err.jspError(n.getStart(), 
                                MESSAGES.invalidAttributeForTag(na.getName(), n.getLocalName()));
                    }
                }
            }
        }

        /**
         * Preprocess attributes that can be expressions. Expression delimiters
         * are stripped.
         * <p>
         * If value is null, checks if there are any NamedAttribute subelements
         * in the tree node, and if so, constructs a JspAttribute out of a child
         * NamedAttribute node.
         */
        private Node.JspAttribute getJspAttribute(TagAttributeInfo tai,
                String qName, String uri, String localName, String value,
                Class expectedType, Node n, boolean dynamic)
                throws JasperException {

            Node.JspAttribute result = null;

            // XXX Is it an error to see "%=foo%" in non-Xml page?
            // (We won't see "<%=foo%> in xml page because '<' is not a
            // valid attribute value in xml).

            if (value != null) {
                if (n.getRoot().isXmlSyntax() && value.startsWith("%=")) {
                    result = new Node.JspAttribute(tai, qName, uri, localName,
                            value.substring(2, value.length() - 1), true, null,
                            dynamic);
                } else if (!n.getRoot().isXmlSyntax()
                        && value.startsWith("<%=")) {
                    result = new Node.JspAttribute(tai, qName, uri, localName,
                            value.substring(3, value.length() - 2), true, null,
                            dynamic);
                } else if (pageInfo.isELIgnored()) {
                    result = new Node.JspAttribute(tai, qName, uri, localName,
                            value, false, null, dynamic);
                } else {
                    // The attribute can contain expressions but is not a
                    // scriptlet expression; thus, we want to run it through
                    // the expression interpreter

                    // validate expression syntax if string contains
                    // expression(s)
                    ELNode.Nodes el = ELParser.parse(value, pageInfo
                            .isDeferredSyntaxAllowedAsLiteral());

                    if (el.containsEL()) {

                        validateFunctions(el, n);

                        result = new Node.JspAttribute(tai, qName, uri,
                                localName, value, false, el, dynamic);

                        ELContextImpl ctx = new ELContextImpl();
                        ctx.setFunctionMapper(getFunctionMapper(el));

                        try {
                            result.validateEL(this.pageInfo
                                    .getExpressionFactory(), ctx);
                        } catch (ELException e) {
                            this.err.jspError(n.getStart(),
                                    MESSAGES.invalidExpression(value), e);
                        }

                    } else {
                        result = new Node.JspAttribute(tai, qName, uri,
                                localName, value, false, null, dynamic);
                    }
                }
            } else {
                // Value is null. Check for any NamedAttribute subnodes
                // that might contain the value for this attribute.
                // Otherwise, the attribute wasn't found so we return null.

                Node.NamedAttribute namedAttributeNode = n
                        .getNamedAttributeNode(qName);
                if (namedAttributeNode != null) {
                    result = new Node.JspAttribute(namedAttributeNode, tai,
                            dynamic);
                }
            }

            return result;
        }

        /*
         * Return an empty StringBuilder [not thread-safe]
         */
        private StringBuilder getBuffer() {
            this.buf.setLength(0);
            return this.buf;
        }

        /*
         * Checks to see if the given attribute value represents a runtime or EL
         * expression.
         */
        private boolean isExpression(Node n, String value, boolean checkDeferred) {
            
            boolean runtimeExpression = ((n.getRoot().isXmlSyntax() && value.startsWith("%="))
                    || (!n.getRoot().isXmlSyntax() && value.startsWith("<%=")));
            boolean elExpression = false;

            if (!runtimeExpression && !pageInfo.isELIgnored()) {
                Iterator<ELNode> nodes = ELParser.parse(value,
                        pageInfo.isDeferredSyntaxAllowedAsLiteral()).iterator();
                while (nodes.hasNext()) {
                    ELNode node = nodes.next();
                    if (node instanceof ELNode.Root) {
                        if (((ELNode.Root) node).getType() == '$') {
                            elExpression = true;
                        } else if (checkDeferred && !pageInfo.isDeferredSyntaxAllowedAsLiteral() 
                                && ((ELNode.Root) node).getType() == '#') {
                            elExpression = true;
                        }
                    }
                }
            }

            return runtimeExpression || elExpression;

        }

        /*
         * Throws exception if the value of the attribute with the given name in
         * the given node is given as an RT or EL expression, but the spec
         * requires a static value.
         */
        private void throwErrorIfExpression(Node n, String attrName,
                String actionName) throws JasperException {
            if (n.getAttributes() != null
                    && n.getAttributes().getValue(attrName) != null
                    && isExpression(n, n.getAttributes().getValue(attrName), true)) {
                err.jspError(n.getStart(),
                        MESSAGES.noExpressionAllowedForAttributeInAction(attrName, actionName));
            }
        }

        private static class NamedAttributeVisitor extends Node.Visitor {
            private boolean hasDynamicContent;

            public void doVisit(Node n) throws JasperException {
                if (!(n instanceof Node.JspText)
                        && !(n instanceof Node.TemplateText)) {
                    hasDynamicContent = true;
                }
                visitBody(n);
            }

            public boolean hasDynamicContent() {
                return hasDynamicContent;
            }
        }

        private String findUri(String prefix, Node n) {

            for (Node p = n; p != null; p = p.getParent()) {
                Attributes attrs = p.getTaglibAttributes();
                if (attrs == null) {
                    continue;
                }
                for (int i = 0; i < attrs.getLength(); i++) {
                    String name = attrs.getQName(i);
                    int k = name.indexOf(':');
                    if (prefix == null && k < 0) {
                        // prefix not specified and a default ns found
                        return attrs.getValue(i);
                    }
                    if (prefix != null && k >= 0
                            && prefix.equals(name.substring(k + 1))) {
                        return attrs.getValue(i);
                    }
                }
            }
            return null;
        }

        /**
         * Validate functions in EL expressions
         */
        private void validateFunctions(ELNode.Nodes el, Node n)
                throws JasperException {

            class FVVisitor extends ELNode.Visitor {

                Node n;

                FVVisitor(Node n) {
                    this.n = n;
                }

                public void visit(ELNode.Function func) throws JasperException {
                    String prefix = func.getPrefix();
                    String function = func.getName();
                    String uri = null;

                    if (n.getRoot().isXmlSyntax()) {
                        uri = findUri(prefix, n);
                    } else if (prefix != null) {
                        uri = pageInfo.getURI(prefix);
                    }

                    if (uri == null) {
                        if (prefix == null) {
                            err.jspError(n.getStart(), MESSAGES.missingFunctionPrefix(function));
                        } else {
                            err.jspError(n.getStart(), MESSAGES.unknownFunctionPrefix(prefix));
                        }
                    }
                    TagLibraryInfo taglib = pageInfo.getTaglib(uri);
                    FunctionInfo funcInfo = null;
                    if (taglib != null) {
                        funcInfo = taglib.getFunction(function);
                    }
                    if (funcInfo == null) {
                        err.jspError(n.getStart(), MESSAGES.unknownFunction(function));
                    }
                    // Skip TLD function uniqueness check. Done by Schema ?
                    func.setUri(uri);
                    func.setFunctionInfo(funcInfo);
                    processSignature(func);
                }
            }

            el.visit(new FVVisitor(n));
        }

        private void prepareExpression(ELNode.Nodes el, Node n, String expr)
                throws JasperException {
            validateFunctions(el, n);

            // test it out
            ELContextImpl ctx = new ELContextImpl();
            ctx.setFunctionMapper(this.getFunctionMapper(el));
            ExpressionFactory ef = this.pageInfo.getExpressionFactory();
            try {
                ef.createValueExpression(ctx, expr, Object.class);
            } catch (ELException e) {

            }
        }

        private void processSignature(ELNode.Function func)
                throws JasperException {
            func.setMethodName(getMethod(func));
            func.setParameters(getParameters(func));
        }

        /**
         * Get the method name from the signature.
         */
        private String getMethod(ELNode.Function func) throws JasperException {
            FunctionInfo funcInfo = func.getFunctionInfo();
            String signature = funcInfo.getFunctionSignature();

            int start = signature.indexOf(' ');
            if (start < 0) {
                err.jspError(MESSAGES.invalidFunctionSignature(func.getPrefix(), func.getName()));
            }
            int end = signature.indexOf('(');
            if (end < 0) {
                err.jspError(MESSAGES.invalidFunctionSignatureMissingParent(func.getPrefix(), func.getName()));
            }
            return signature.substring(start + 1, end).trim();
        }

        /**
         * Get the parameters types from the function signature.
         * 
         * @return An array of parameter class names
         */
        private String[] getParameters(ELNode.Function func)
                throws JasperException {
            FunctionInfo funcInfo = func.getFunctionInfo();
            String signature = funcInfo.getFunctionSignature();
            ArrayList<String> params = new ArrayList<String>();
            // Signature is of the form
            // <return-type> S <method-name S? '('
            // < <arg-type> ( ',' <arg-type> )* )? ')'
            int start = signature.indexOf('(') + 1;
            boolean lastArg = false;
            while (true) {
                int p = signature.indexOf(',', start);
                if (p < 0) {
                    p = signature.indexOf(')', start);
                    if (p < 0) {
                        err.jspError(MESSAGES.invalidFunctionSignature(func.getPrefix(), func.getName()));
                    }
                    lastArg = true;
                }
                String arg = signature.substring(start, p).trim();
                if (!"".equals(arg)) {
                    params.add(arg);
                }
                if (lastArg) {
                    break;
                }
                start = p + 1;
            }
            return (String[]) params.toArray(new String[params.size()]);
        }

        private FunctionMapper getFunctionMapper(ELNode.Nodes el)
                throws JasperException {

            class ValidateFunctionMapper extends FunctionMapper {

                private HashMap<String, Method> fnmap = new HashMap<String, Method>();

                public void mapFunction(String fnQName, Method method) {
                    fnmap.put(fnQName, method);
                }

                public Method resolveFunction(String prefix, String localName) {
                    return this.fnmap.get(prefix + ":" + localName);
                }
            }

            class MapperELVisitor extends ELNode.Visitor {
                ValidateFunctionMapper fmapper;

                MapperELVisitor(ValidateFunctionMapper fmapper) {
                    this.fmapper = fmapper;
                }

                public void visit(ELNode.Function n) throws JasperException {

                    Class c = null;
                    Method method = null;
                    try {
                        c = loader.loadClass(n.getFunctionInfo()
                                .getFunctionClass());
                    } catch (ClassNotFoundException e) {
                        err.jspError(MESSAGES.missingFunctionClass
                                (n.getFunctionInfo().getFunctionClass(), n.getName()), e);
                    }
                    String paramTypes[] = n.getParameters();
                    int size = paramTypes.length;
                    Class params[] = new Class[size];
                    int i = 0;
                    try {
                        for (i = 0; i < size; i++) {
                            params[i] = JspUtil.toClass(paramTypes[i], loader);
                        }
                        method = c.getDeclaredMethod(n.getMethodName(), params);
                    } catch (ClassNotFoundException e) {
                        err.jspError(MESSAGES.missingSignatureClass(paramTypes[i], n.getName()), e);
                    } catch (NoSuchMethodException e) {
                        err.jspError(MESSAGES.missingMethodInClass(n.getMethodName(), n.getName(), c.getName()));
                    }
                    fmapper.mapFunction(n.getPrefix() + ':' + n.getName(),
                            method);
                }
            }

            ValidateFunctionMapper fmapper = new ValidateFunctionMapper();
            el.visit(new MapperELVisitor(fmapper));
            return fmapper;
        }
    } // End of ValidateVisitor

    /**
     * A visitor for validating TagExtraInfo classes of all tags
     */
    static class TagExtraInfoVisitor extends Node.Visitor {

        private ErrorDispatcher err;

        /*
         * Constructor
         */
        TagExtraInfoVisitor(Compiler compiler) {
            this.err = compiler.getErrorDispatcher();
        }

        public void visit(Node.CustomTag n) throws JasperException {
            TagInfo tagInfo = n.getTagInfo();
            if (tagInfo == null) {
                err.jspError(n.getStart(), MESSAGES.missingTagInfo(n.getQName()));
            }

            ValidationMessage[] errors = tagInfo.validate(n.getTagData());
            if (errors != null && errors.length != 0) {
                StringBuilder errMsg = new StringBuilder();
                errMsg.append("<h3>");
                errMsg.append(MESSAGES.errorValidatingTag(n.getQName()));
                errMsg.append("</h3>");
                for (int i = 0; i < errors.length; i++) {
                    errMsg.append("<p>");
                    if (errors[i].getId() != null) {
                        errMsg.append(errors[i].getId());
                        errMsg.append(": ");
                    }
                    errMsg.append(errors[i].getMessage());
                    errMsg.append("</p>");
                }

                err.jspError(n.getStart(), errMsg.toString());
            }

            visitBody(n);
        }
    }

    public static void validateDirectives(Compiler compiler, Node.Nodes page)
    throws JasperException {
        page.visit(new DirectiveVisitor(compiler));
    }

    public static void validateExDirectives(Compiler compiler, Node.Nodes page)
    throws JasperException {
//      Determine the default output content type
        PageInfo pageInfo = compiler.getPageInfo();
        String contentType = pageInfo.getContentType();

        if (contentType == null || contentType.indexOf("charset=") < 0) {
            boolean isXml = page.getRoot().isXmlSyntax();
            String defaultType;
            if (contentType == null) {
                defaultType = isXml ? "text/xml" : "text/html";
            } else {
                defaultType = contentType;
            }

            String charset = null;
            if (isXml) {
                charset = "UTF-8";
            } else {
                if (!page.getRoot().isDefaultPageEncoding()) {
                    charset = page.getRoot().getPageEncoding();
                }
            }

            if (charset != null) {
                pageInfo.setContentType(defaultType + ";charset=" + charset);
            } else {
                pageInfo.setContentType(defaultType);
            }
        }

        /*
         * Validate all other nodes. This validation step includes checking a
         * custom tag's mandatory and optional attributes against information in
         * the TLD (first validation step for custom tags according to
         * JSP.10.5).
         */
        page.visit(new ValidateVisitor(compiler));

        /*
         * Invoke TagLibraryValidator classes of all imported tags (second
         * validation step for custom tags according to JSP.10.5).
         */
        validateXmlView(new PageDataImpl(page, compiler), compiler);

        /*
         * Invoke TagExtraInfo method isValid() for all imported tags (third
         * validation step for custom tags according to JSP.10.5).
         */
        page.visit(new TagExtraInfoVisitor(compiler));

    }

    // *********************************************************************
    // Private (utility) methods

    /**
     * Validate XML view against the TagLibraryValidator classes of all imported
     * tag libraries.
     */
    private static void validateXmlView(PageData xmlView, Compiler compiler)
            throws JasperException {

        StringBuilder errMsg = null;
        ErrorDispatcher errDisp = compiler.getErrorDispatcher();

        for (Iterator iter = compiler.getPageInfo().getTaglibs().iterator(); iter
                .hasNext();) {

            Object o = iter.next();
            if (!(o instanceof TagLibraryInfoImpl))
                continue;
            TagLibraryInfoImpl tli = (TagLibraryInfoImpl) o;

            ValidationMessage[] errors = tli.validate(xmlView);
            if ((errors != null) && (errors.length != 0)) {
                if (errMsg == null) {
                    errMsg = new StringBuilder();
                }
                errMsg.append("<h3>");
                errMsg.append(MESSAGES.errorValidatingTaglibrary(tli.getShortName(),
                        compiler.getPageInfo().getJspFile()));
                errMsg.append("</h3>");
                for (int i = 0; i < errors.length; i++) {
                    if (errors[i] != null) {
                        errMsg.append("<p>");
                        errMsg.append(errors[i].getId());
                        errMsg.append(": ");
                        errMsg.append(errors[i].getMessage());
                        errMsg.append("</p>");
                    }
                }
            }
        }

        if (errMsg != null) {
            errDisp.jspError(errMsg.toString());
        }
    }
}
