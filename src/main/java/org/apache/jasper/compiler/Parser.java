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

import java.io.CharArrayWriter;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Pattern;

import jakarta.servlet.jsp.tagext.TagAttributeInfo;
import jakarta.servlet.jsp.tagext.TagFileInfo;
import jakarta.servlet.jsp.tagext.TagInfo;
import jakarta.servlet.jsp.tagext.TagLibraryInfo;

import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.util.UniqueAttributesImpl;
import org.apache.tomcat.util.scan.Jar;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This class implements a parser for a JSP page (non-xml view). JSP page
 * grammar is included here for reference. The token '#' that appears in the
 * production indicates the current input token location in the production.
 *
 * @author Kin-man Chung
 * @author Shawn Bayern
 * @author Mark Roth
 */

class Parser implements TagConstants {

    private final ParserController parserController;

    private final JspCompilationContext ctxt;

    private final JspReader reader;

    private Mark start;

    private final ErrorDispatcher err;

    private int scriptlessCount;

    private final boolean isTagFile;

    private final boolean directivesOnly;

    private final Jar jar;

    private final PageInfo pageInfo;

    // Virtual body content types, to make parsing a little easier.
    // These are not accessible from outside the parser.
    private static final String JAKARTA_BODY_CONTENT_PARAM =
        "JAKARTA_BODY_CONTENT_PARAM";

    private static final String JAKARTA_BODY_CONTENT_PLUGIN =
        "JAKARTA_BODY_CONTENT_PLUGIN";

    private static final String JAKARTA_BODY_CONTENT_TEMPLATE_TEXT =
        "JAKARTA_BODY_CONTENT_TEMPLATE_TEXT";

    /* System property that controls if the strict white space rules are
     * applied.
     */
    private static final boolean STRICT_WHITESPACE = Boolean.valueOf(
            System.getProperty(
                    "org.apache.jasper.compiler.Parser.STRICT_WHITESPACE",
                    "true")).booleanValue();


    /**
     * The constructor
     */
    private Parser(ParserController pc, JspReader reader, boolean isTagFile,
            boolean directivesOnly, Jar jar) {
        this.parserController = pc;
        this.ctxt = pc.getJspCompilationContext();
        this.pageInfo = pc.getCompiler().getPageInfo();
        this.err = pc.getCompiler().getErrorDispatcher();
        this.reader = reader;
        this.scriptlessCount = 0;
        this.isTagFile = isTagFile;
        this.directivesOnly = directivesOnly;
        this.jar = jar;
        start = reader.mark();
    }

    /**
     * The main entry for Parser
     *
     * @param pc
     *            The ParseController, use for getting other objects in compiler
     *            and for parsing included pages
     * @param reader
     *            To read the page
     * @param parent
     *            The parent node to this page, null for top level page
     * @return list of nodes representing the parsed page
     */
    public static Node.Nodes parse(ParserController pc, JspReader reader,
            Node parent, boolean isTagFile, boolean directivesOnly,
            Jar jar, String pageEnc, String jspConfigPageEnc,
            boolean isDefaultPageEncoding, boolean isBomPresent)
            throws JasperException {

        Parser parser = new Parser(pc, reader, isTagFile, directivesOnly, jar);

        Node.Root root = new Node.Root(reader.mark(), parent, false);
        root.setPageEncoding(pageEnc);
        root.setJspConfigPageEncoding(jspConfigPageEnc);
        root.setIsDefaultPageEncoding(isDefaultPageEncoding);
        root.setIsBomPresent(isBomPresent);

        // For the Top level page, add include-prelude and include-coda
        PageInfo pageInfo = pc.getCompiler().getPageInfo();
        if (parent == null && !isTagFile) {
            parser.addInclude(root, pageInfo.getIncludePrelude());
        }
        if (directivesOnly) {
            parser.parseFileDirectives(root);
        } else {
            while (reader.hasMoreInput()) {
                parser.parseElements(root);
            }
        }
        if (parent == null && !isTagFile) {
            parser.addInclude(root, pageInfo.getIncludeCoda());
        }

        Node.Nodes page = new Node.Nodes(root);
        return page;
    }

    /**
     * Attributes ::= (S Attribute)* S?
     */
    Attributes parseAttributes() throws JasperException {
        return parseAttributes(false);
    }
    Attributes parseAttributes(boolean pageDirective) throws JasperException {
        UniqueAttributesImpl attrs = new UniqueAttributesImpl(pageDirective);

        reader.skipSpaces();
        int ws = 1;

        try {
            while (parseAttribute(attrs)) {
                if (ws == 0 && STRICT_WHITESPACE) {
                    err.jspError(reader.mark(),
                            "jsp.error.attribute.nowhitespace");
                }
                ws = reader.skipSpaces();
            }
        } catch (IllegalArgumentException iae) {
            // Duplicate attribute
            err.jspError(reader.mark(), "jsp.error.attribute.duplicate");
        }

        return attrs;
    }

    /**
     * Parse Attributes for a reader, provided for external use
     */
    public static Attributes parseAttributes(ParserController pc,
            JspReader reader) throws JasperException {
        Parser tmpParser = new Parser(pc, reader, false, false, null);
        return tmpParser.parseAttributes(true);
    }

    /**
     * Attribute ::= Name S? Eq S? ( '"<%=' RTAttributeValueDouble | '"'
     * AttributeValueDouble | "'<%=" RTAttributeValueSingle | "'"
     * AttributeValueSingle } Note: JSP and XML spec does not allow while spaces
     * around Eq. It is added to be backward compatible with Tomcat, and with
     * other xml parsers.
     */
    private boolean parseAttribute(AttributesImpl attrs)
            throws JasperException {

        // Get the qualified name
        String qName = parseName();
        if (qName == null)
            return false;

        // Determine prefix and local name components
        String localName = qName;
        String uri = "";
        int index = qName.indexOf(':');
        if (index != -1) {
            String prefix = qName.substring(0, index);
            uri = pageInfo.getURI(prefix);
            if (uri == null) {
                err.jspError(reader.mark(), MESSAGES.invalidAttributePrefix(prefix));
            }
            localName = qName.substring(index + 1);
        }

        reader.skipSpaces();
        if (!reader.matches("="))
            err.jspError(reader.mark(), MESSAGES.missingEqual());

        reader.skipSpaces();
        char quote = (char) reader.nextChar();
        if (quote != '\'' && quote != '"')
            err.jspError(reader.mark(), MESSAGES.missingQuote());

        String watchString = "";
        if (reader.matches("<%="))
            watchString = "%>";
        watchString = watchString + quote;

        String attrValue = parseAttributeValue(watchString);
        attrs.addAttribute(uri, localName, qName, "CDATA", attrValue);
        return true;
    }

    /**
     * Name ::= (Letter | '_' | ':') (Letter | Digit | '.' | '_' | '-' | ':')*
     */
    private String parseName() {
        char ch = (char) reader.peekChar();
        if (Character.isLetter(ch) || ch == '_' || ch == ':') {
            StringBuilder buf = new StringBuilder();
            buf.append(ch);
            reader.nextChar();
            ch = (char) reader.peekChar();
            while (Character.isLetter(ch) || Character.isDigit(ch) || ch == '.'
                    || ch == '_' || ch == '-' || ch == ':') {
                buf.append(ch);
                reader.nextChar();
                ch = (char) reader.peekChar();
            }
            return buf.toString();
        }
        return null;
    }

    /**
     * AttributeValueDouble ::= (QuotedChar - '"')* ('"' | <TRANSLATION_ERROR>)
     * RTAttributeValueDouble ::= ((QuotedChar - '"')* - ((QuotedChar-'"')'%>"')
     * ('%>"' | TRANSLATION_ERROR)
     */
    private String parseAttributeValue(String watch) throws JasperException {
        Mark start = reader.mark();
        Mark stop = reader.skipUntilIgnoreEsc(watch);
        if (stop == null) {
            err.jspError(start, MESSAGES.unterminatedAttribute(watch));
        }

        String ret = null;
        try {
            char quote = watch.charAt(watch.length() - 1);

            // If watch is longer than 1 character this is a scripting
            // expression and EL is always ignored
            boolean isElIgnored =
                pageInfo.isELIgnored() || watch.length() > 1;

            ret = AttributeParser.getUnquoted(reader.getText(start, stop),
                    quote, isElIgnored,
                    pageInfo.isDeferredSyntaxAllowedAsLiteral());
        } catch (IllegalArgumentException iae) {
            err.jspError(start, iae, MESSAGES.errorUnquotingAttributeValue());
        }
        if (watch.length() == 1) // quote
            return ret;

        // Put back delimiter '<%=' and '%>', since they are needed if the
        // attribute does not allow RTexpression.
        return "<%=" + ret + "%>";
    }

    private String parseScriptText(String tx) {
        CharArrayWriter cw = new CharArrayWriter();
        int size = tx.length();
        int i = 0;
        while (i < size) {
            char ch = tx.charAt(i);
            if (i + 2 < size && ch == '%' && tx.charAt(i + 1) == '\\'
                    && tx.charAt(i + 2) == '>') {
                cw.write('%');
                cw.write('>');
                i += 3;
            } else {
                cw.write(ch);
                ++i;
            }
        }
        cw.close();
        return cw.toString();
    }

    /*
     * Invokes parserController to parse the included page
     */
    private void processIncludeDirective(String file, Node parent)
            throws JasperException {
        if (file == null) {
            return;
        }

        try {
            parserController.parse(file, parent, jar);
        } catch (FileNotFoundException ex) {
            err.jspError(start, MESSAGES.fileNotFound(file));
        } catch (Exception ex) {
            err.jspError(start, ex, MESSAGES.errorIncluding(file));
        }
    }

    /*
     * Parses a page directive with the following syntax: PageDirective ::= ( S
     * Attribute)*
     */
    private void parsePageDirective(Node parent) throws JasperException {
        Attributes attrs = parseAttributes(true);
        Node.PageDirective n = new Node.PageDirective(attrs, start, parent);

        /*
         * A page directive may contain multiple 'import' attributes, each of
         * which consists of a comma-separated list of package names. Store each
         * list with the node, where it is parsed.
         */
        for (int i = 0; i < attrs.getLength(); i++) {
            if ("import".equals(attrs.getQName(i))) {
                n.addImport(attrs.getValue(i));
            }
        }
    }

    /*
     * Parses an include directive with the following syntax: IncludeDirective
     * ::= ( S Attribute)*
     */
    private void parseIncludeDirective(Node parent) throws JasperException {
        Attributes attrs = parseAttributes();

        // Included file expanded here
        Node includeNode = new Node.IncludeDirective(attrs, start, parent);
        processIncludeDirective(attrs.getValue("file"), includeNode);
    }

    /**
     * Add a list of files. This is used for implementing include-prelude and
     * include-coda of jsp-config element in web.xml
     */
    private void addInclude(Node parent, Collection<String> files) throws JasperException {
        if (files != null) {
            Iterator<String> iter = files.iterator();
            while (iter.hasNext()) {
                String file = iter.next();
                AttributesImpl attrs = new AttributesImpl();
                attrs.addAttribute("", "file", "file", "CDATA", file);

                // Create a dummy Include directive node
                Node includeNode = new Node.IncludeDirective(attrs, reader
                        .mark(), parent);
                processIncludeDirective(file, includeNode);
            }
        }
    }

    /*
     * Parses a taglib directive with the following syntax: Directive ::= ( S
     * Attribute)*
     */
    private void parseTaglibDirective(Node parent) throws JasperException {

        Attributes attrs = parseAttributes();
        String uri = attrs.getValue("uri");
        String prefix = attrs.getValue("prefix");
        if (prefix != null) {
            Mark prevMark = pageInfo.getNonCustomTagPrefix(prefix);
            if (prevMark != null) {
                err.jspError(reader.mark(), MESSAGES.prefixAlreadyInUse
                        (prefix, prevMark.getFile(), prevMark.getLineNumber()));
            }
            if (uri != null) {
                String uriPrev = pageInfo.getURI(prefix);
                if (uriPrev != null && !uriPrev.equals(uri)) {
                    err.jspError(reader.mark(), MESSAGES.prefixRedefinition(prefix, uri, uriPrev));
                }
                if (pageInfo.getTaglib(uri) == null) {
                    TagLibraryInfoImpl impl = null;
                    if (ctxt.getOptions().isCaching()) {
                        impl = (TagLibraryInfoImpl) ctxt.getOptions()
                                .getCache().get(uri);
                    }
                    if (impl == null) {
                        String[] location = ctxt.getTldLocation(uri);
                        impl = new TagLibraryInfoImpl(ctxt, parserController, pageInfo,
                                prefix, uri, location, err);
                        if (ctxt.getOptions().isCaching()) {
                            ctxt.getOptions().getCache().put(uri, impl);
                        }
                    } else {
                        // Current compilation context needs location of cached
                        // tag files
                        for (TagFileInfo info : impl.getTagFiles()) {
                            ctxt.setTagFileJarUrl(info.getPath(), ctxt.getTagFileJarUrl(info.getPath()));
                        }
                    }
                    pageInfo.addTaglib(uri, impl);
                }
                pageInfo.addPrefixMapping(prefix, uri);
            } else {
                String tagdir = attrs.getValue("tagdir");
                if (tagdir != null) {
                    String urnTagdir = URN_JSPTAGDIR + tagdir;
                    if (pageInfo.getTaglib(urnTagdir) == null) {
                        pageInfo.addTaglib(urnTagdir,
                                new ImplicitTagLibraryInfo(ctxt,
                                        parserController, pageInfo, prefix,
                                        tagdir, err));
                    }
                    pageInfo.addPrefixMapping(prefix, urnTagdir);
                }
            }
        }

        @SuppressWarnings("unused")
        Node unused = new Node.TaglibDirective(attrs, start, parent);
    }

    /*
     * Parses a directive with the following syntax: Directive ::= S? ( 'page'
     * PageDirective | 'include' IncludeDirective | 'taglib' TagLibDirective) S?
     * '%>'
     *
     * TagDirective ::= S? ('tag' PageDirective | 'include' IncludeDirective |
     * 'taglib' TagLibDirective) | 'attribute AttributeDirective | 'variable
     * VariableDirective S? '%>'
     */
    private void parseDirective(Node parent) throws JasperException {
        reader.skipSpaces();

        String directive = null;
        if (reader.matches("page")) {
            directive = "&lt;%@ page";
            if (isTagFile) {
                err.jspError(reader.mark(), MESSAGES.invalidDirectiveInTagFile(directive));
            }
            parsePageDirective(parent);
        } else if (reader.matches("include")) {
            directive = "&lt;%@ include";
            parseIncludeDirective(parent);
        } else if (reader.matches("taglib")) {
            if (directivesOnly) {
                // No need to get the tagLibInfo objects. This alos suppresses
                // parsing of any tag files used in this tag file.
                return;
            }
            directive = "&lt;%@ taglib";
            parseTaglibDirective(parent);
        } else if (reader.matches("tag")) {
            directive = "&lt;%@ tag";
            if (!isTagFile) {
                err.jspError(reader.mark(), MESSAGES.invalidDirectiveInPage(directive));
            }
            parseTagDirective(parent);
        } else if (reader.matches("attribute")) {
            directive = "&lt;%@ attribute";
            if (!isTagFile) {
                err.jspError(reader.mark(), MESSAGES.invalidDirectiveInPage(directive));
            }
            parseAttributeDirective(parent);
        } else if (reader.matches("variable")) {
            directive = "&lt;%@ variable";
            if (!isTagFile) {
                err.jspError(reader.mark(), MESSAGES.invalidDirectiveInPage(directive));
            }
            parseVariableDirective(parent);
        } else {
            err.jspError(reader.mark(), MESSAGES.invalidDirective());
        }

        reader.skipSpaces();
        if (!reader.matches("%>")) {
            err.jspError(start, MESSAGES.unterminatedTag(directive));
        }
    }

    /*
     * Parses a directive with the following syntax:
     *
     * XMLJSPDirectiveBody ::= S? ( ( 'page' PageDirectiveAttrList S? ( '/>' | (
     * '>' S? ETag ) ) | ( 'include' IncludeDirectiveAttrList S? ( '/>' | ( '>'
     * S? ETag ) ) | <TRANSLATION_ERROR>
     *
     * XMLTagDefDirectiveBody ::= ( ( 'tag' TagDirectiveAttrList S? ( '/>' | (
     * '>' S? ETag ) ) | ( 'include' IncludeDirectiveAttrList S? ( '/>' | ( '>'
     * S? ETag ) ) | ( 'attribute' AttributeDirectiveAttrList S? ( '/>' | ( '>'
     * S? ETag ) ) | ( 'variable' VariableDirectiveAttrList S? ( '/>' | ( '>' S?
     * ETag ) ) ) | <TRANSLATION_ERROR>
     */
    private void parseXMLDirective(Node parent) throws JasperException {
        reader.skipSpaces();

        String eTag = null;
        if (reader.matches("page")) {
            eTag = "jsp:directive.page";
            if (isTagFile) {
                err.jspError(reader.mark(), MESSAGES.invalidDirectiveInTagFile("&lt;" + eTag));
            }
            parsePageDirective(parent);
        } else if (reader.matches("include")) {
            eTag = "jsp:directive.include";
            parseIncludeDirective(parent);
        } else if (reader.matches("tag")) {
            eTag = "jsp:directive.tag";
            if (!isTagFile) {
                err.jspError(reader.mark(), MESSAGES.invalidDirectiveInPage("&lt;" + eTag));
            }
            parseTagDirective(parent);
        } else if (reader.matches("attribute")) {
            eTag = "jsp:directive.attribute";
            if (!isTagFile) {
                err.jspError(reader.mark(), MESSAGES.invalidDirectiveInPage("&lt;" + eTag));
            }
            parseAttributeDirective(parent);
        } else if (reader.matches("variable")) {
            eTag = "jsp:directive.variable";
            if (!isTagFile) {
                err.jspError(reader.mark(), MESSAGES.invalidDirectiveInPage("&lt;" + eTag));
            }
            parseVariableDirective(parent);
        } else {
            err.jspError(reader.mark(), MESSAGES.invalidDirective());
        }

        reader.skipSpaces();
        if (reader.matches(">")) {
            reader.skipSpaces();
            if (!reader.matchesETag(eTag)) {
                err.jspError(start, MESSAGES.unterminatedTag("&lt;" + eTag));
            }
        } else if (!reader.matches("/>")) {
            err.jspError(start, MESSAGES.unterminatedTag("&lt;" + eTag));
        }
    }

    /*
     * Parses a tag directive with the following syntax: PageDirective ::= ( S
     * Attribute)*
     */
    private void parseTagDirective(Node parent) throws JasperException {
        Attributes attrs = parseAttributes(true);
        Node.TagDirective n = new Node.TagDirective(attrs, start, parent);

        /*
         * A page directive may contain multiple 'import' attributes, each of
         * which consists of a comma-separated list of package names. Store each
         * list with the node, where it is parsed.
         */
        for (int i = 0; i < attrs.getLength(); i++) {
            if ("import".equals(attrs.getQName(i))) {
                n.addImport(attrs.getValue(i));
            }
        }
    }

    /*
     * Parses a attribute directive with the following syntax:
     * AttributeDirective ::= ( S Attribute)*
     */
    private void parseAttributeDirective(Node parent) throws JasperException {
        Attributes attrs = parseAttributes();
        @SuppressWarnings("unused")
        Node unused = new Node.AttributeDirective(attrs, start, parent);
    }

    /*
     * Parses a variable directive with the following syntax:
     * PageDirective ::= ( S Attribute)*
     */
    private void parseVariableDirective(Node parent) throws JasperException {
        Attributes attrs = parseAttributes();
        @SuppressWarnings("unused")
        Node unused = new Node.VariableDirective(attrs, start, parent);
    }

    /*
     * JSPCommentBody ::= (Char* - (Char* '--%>')) '--%>'
     */
    private void parseComment(Node parent) throws JasperException {
        start = reader.mark();
        Mark stop = reader.skipUntil("--%>");
        if (stop == null) {
            err.jspError(start, MESSAGES.unterminatedTag("&lt;%--"));
        }

        @SuppressWarnings("unused")
        Node unused =
        new Node.Comment(reader.getText(start, stop), start, parent);
    }

    /*
     * DeclarationBody ::= (Char* - (char* '%>')) '%>'
     */
    private void parseDeclaration(Node parent) throws JasperException {
        start = reader.mark();
        Mark stop = reader.skipUntil("%>");
        if (stop == null) {
            err.jspError(start, MESSAGES.unterminatedTag("&lt;%!"));
        }

        @SuppressWarnings("unused")
        Node unused = new Node.Declaration(
                parseScriptText(reader.getText(start, stop)), start, parent);
    }

    /*
     * XMLDeclarationBody ::= ( S? '/>' ) | ( S? '>' (Char* - (char* '<'))
     * CDSect?)* ETag | <TRANSLATION_ERROR> CDSect ::= CDStart CData CDEnd
     * CDStart ::= '<![CDATA[' CData ::= (Char* - (Char* ']]>' Char*)) CDEnd
     * ::= ']]>'
     */
    private void parseXMLDeclaration(Node parent) throws JasperException {
        reader.skipSpaces();
        if (!reader.matches("/>")) {
            if (!reader.matches(">")) {
                err.jspError(start, MESSAGES.unterminatedTag("&lt;jsp:declaration&gt;"));
            }
            Mark stop;
            String text;
            while (true) {
                start = reader.mark();
                stop = reader.skipUntil("<");
                if (stop == null) {
                    err.jspError(start, MESSAGES.unterminatedTag("&lt;jsp:declaration&gt;"));
                }
                text = parseScriptText(reader.getText(start, stop));
                new Node.Declaration(text, start, parent);
                if (reader.matches("![CDATA[")) {
                    start = reader.mark();
                    stop = reader.skipUntil("]]>");
                    if (stop == null) {
                        err.jspError(start, MESSAGES.unterminatedTag("CDATA"));
                    }
                    text = parseScriptText(reader.getText(start, stop));
                    new Node.Declaration(text, start, parent);
                } else {
                    break;
                }
            }

            if (!reader.matchesETagWithoutLessThan("jsp:declaration")) {
                err.jspError(start, MESSAGES.unterminatedTag("&lt;jsp:declaration&gt;"));
            }
        }
    }

    /*
     * ExpressionBody ::= (Char* - (char* '%>')) '%>'
     */
    private void parseExpression(Node parent) throws JasperException {
        start = reader.mark();
        Mark stop = reader.skipUntil("%>");
        if (stop == null) {
            err.jspError(start, MESSAGES.unterminatedTag("&lt;%="));
        }

        String expression = reader.getText(start, stop);
        // check for string concatenation inside expressions, separating from expression allows for optimizations later on
        if(!this.ctxt.getOptions().isOptimizeJSPScriptlets()){
            new Node.Expression(parseScriptText(expression),
                    start, parent);
        }
        else {
            if (!matchesConcat(expression) || matchesConcatInMethodArgs(expression)) {
                new Node.Expression(parseScriptText(expression),
                        start, parent);
            } else {
                //need to separate expressions being concatenated
                expression = expression.replaceAll("\\+\\s*\"", "\\+ \"").replaceAll("\"\\s*\\+", "\" \\+");
                String[] tokens = expression.split("((?=\\+\\s\")|(?<=\"\\s\\+))");
                if (tokens.length > 1) {
                    for (String token : tokens) {
                        if (matchesStringLiteral(token) && !matchesStringParam(token)) {
                            //maybe evaluate the expression here before storing as text node?
                            new Node.TemplateText(cleanTextToken(token),
                                    start, parent);
                        } else {
                            String parsedScripText = parseScriptText(cleanExprToken(token));
                            if ( ! "".equals( parsedScripText )){
                                new Node.Expression(parsedScripText,
                                        start, parent);
                            }
                        }
                    }
                } else {
                    //only have one token, therefore there is no string concatenation occurring and string literal is being used as part of expression
                    new Node.Expression(parseScriptText(tokens[0]),
                            start, parent);

                }
            }
        }
    }

    private boolean matchesStringLiteral(String token) {
        return Pattern.compile("\"").matcher(token).find() || "".equals(token.trim());
    }

    private boolean matchesStringParam(String token) {
        return Pattern.compile("\"\\s*\\)|\\(\\s*\"").matcher(token).find();
    }

    private boolean matchesConcat(String token) {
        return Pattern.compile("\\+\\s*\"|\"\\s*\\+").matcher(token).find();
    }

    private boolean matchesConcatInMethodArgs(String token) {
        return Pattern.compile("\\(.*?\\+.*?\\)").matcher(token).find();
    }
    private String cleanTextToken(String token) {
        return cleanExprToken(token.trim().replaceAll("(?<!\\\\)\"|\t|\n|\r", "").replaceAll("\\\\\"","\""));
    }

    private String cleanExprToken(String token) {
        return token.replaceAll("^\\+\\s|^\\+|\\+$|\\s\\+$","").replaceAll( "\\s{2}", " " );
    }

    /*
     * XMLExpressionBody ::= ( S? '/>' ) | ( S? '>' (Char* - (char* '<'))
     * CDSect?)* ETag ) | <TRANSLATION_ERROR>
     */
    private void parseXMLExpression(Node parent) throws JasperException {
        reader.skipSpaces();
        if (!reader.matches("/>")) {
            if (!reader.matches(">")) {
                err.jspError(start, MESSAGES.unterminatedTag("&lt;jsp:expression&gt;"));
            }
            Mark stop;
            String text;
            while (true) {
                start = reader.mark();
                stop = reader.skipUntil("<");
                if (stop == null) {
                    err.jspError(start, MESSAGES.unterminatedTag("&lt;jsp:expression&gt;"));
                }
                text = parseScriptText(reader.getText(start, stop));
                new Node.Expression(text, start, parent);
                if (reader.matches("![CDATA[")) {
                    start = reader.mark();
                    stop = reader.skipUntil("]]>");
                    if (stop == null) {
                        err.jspError(start, MESSAGES.unterminatedTag("CDATA"));
                    }
                    text = parseScriptText(reader.getText(start, stop));
                    new Node.Expression(text, start, parent);
                } else {
                    break;
                }
            }
            if (!reader.matchesETagWithoutLessThan("jsp:expression")) {
                err.jspError(start, MESSAGES.unterminatedTag("&lt;jsp:expression&gt;"));
            }
        }
    }

    /*
     * ELExpressionBody. Starts with "#{" or "${".  Ends with "}".
     * See JspReader.skipELExpression().
     */
    private void parseELExpression(Node parent, char type)
            throws JasperException {
        start = reader.mark();
        Mark last = reader.skipELExpression();
        if (last == null) {
            err.jspError(start, "jsp.error.unterminated", type + "{");
            }

        @SuppressWarnings("unused")
        Node unused = new Node.ELExpression(type, reader.getText(start, last),
                start, parent);
    }

    /*
     * ScriptletBody ::= (Char* - (char* '%>')) '%>'
     */
    private void parseScriptlet(Node parent) throws JasperException {
        start = reader.mark();
        Mark stop = reader.skipUntil("%>");
        if (stop == null) {
            err.jspError(start, MESSAGES.unterminatedTag("&lt;%"));
        }

        @SuppressWarnings("unused")
        Node unused = new Node.Scriptlet(
                parseScriptText(reader.getText(start, stop)), start, parent);
    }

    /*
     * XMLScriptletBody ::= ( S? '/>' ) | ( S? '>' (Char* - (char* '<'))
     * CDSect?)* ETag ) | <TRANSLATION_ERROR>
     */
    private void parseXMLScriptlet(Node parent) throws JasperException {
        reader.skipSpaces();
        if (!reader.matches("/>")) {
            if (!reader.matches(">")) {
                err.jspError(start, MESSAGES.unterminatedTag("&lt;jsp:scriptlet&gt;"));
            }
            Mark stop;
            String text;
            while (true) {
                start = reader.mark();
                stop = reader.skipUntil("<");
                if (stop == null) {
                    err.jspError(start, MESSAGES.unterminatedTag("&lt;jsp:scriptlet&gt;"));
                }
                text = parseScriptText(reader.getText(start, stop));
                new Node.Scriptlet(text, start, parent);
                if (reader.matches("![CDATA[")) {
                    start = reader.mark();
                    stop = reader.skipUntil("]]>");
                    if (stop == null) {
                        err.jspError(start, MESSAGES.unterminatedTag("CDATA"));
                    }
                    text = parseScriptText(reader.getText(start, stop));
                    new Node.Scriptlet(text, start, parent);
                } else {
                    break;
                }
            }

            if (!reader.matchesETagWithoutLessThan("jsp:scriptlet")) {
                err.jspError(start, MESSAGES.unterminatedTag("&lt;jsp:scriptlet&gt;"));
            }
        }
    }

    /**
     * Param ::= '<jsp:param' S Attributes S? EmptyBody S?
     */
    private void parseParam(Node parent) throws JasperException {
        if (!reader.matches("<jsp:param")) {
            err.jspError(reader.mark(), MESSAGES.missingParamAction());
        }
        Attributes attrs = parseAttributes();
        reader.skipSpaces();

        Node paramActionNode = new Node.ParamAction(attrs, start, parent);

        parseEmptyBody(paramActionNode, "jsp:param");

        reader.skipSpaces();
    }

    /*
     * For Include: StdActionContent ::= Attributes ParamBody
     *
     * ParamBody ::= EmptyBody | ( '>' S? ( '<jsp:attribute' NamedAttributes )? '<jsp:body'
     * (JspBodyParam | <TRANSLATION_ERROR> ) S? ETag ) | ( '>' S? Param* ETag )
     *
     * EmptyBody ::= '/>' | ( '>' ETag ) | ( '>' S? '<jsp:attribute'
     * NamedAttributes ETag )
     *
     * JspBodyParam ::= S? '>' Param* '</jsp:body>'
     */
    private void parseInclude(Node parent) throws JasperException {
        Attributes attrs = parseAttributes();
        reader.skipSpaces();

        Node includeNode = new Node.IncludeAction(attrs, start, parent);

        parseOptionalBody(includeNode, "jsp:include", JAKARTA_BODY_CONTENT_PARAM);
    }

    /*
     * For Forward: StdActionContent ::= Attributes ParamBody
     */
    private void parseForward(Node parent) throws JasperException {
        Attributes attrs = parseAttributes();
        reader.skipSpaces();

        Node forwardNode = new Node.ForwardAction(attrs, start, parent);

        parseOptionalBody(forwardNode, "jsp:forward", JAKARTA_BODY_CONTENT_PARAM);
    }

    private void parseInvoke(Node parent) throws JasperException {
        Attributes attrs = parseAttributes();
        reader.skipSpaces();

        Node invokeNode = new Node.InvokeAction(attrs, start, parent);

        parseEmptyBody(invokeNode, "jsp:invoke");
    }

    private void parseDoBody(Node parent) throws JasperException {
        Attributes attrs = parseAttributes();
        reader.skipSpaces();

        Node doBodyNode = new Node.DoBodyAction(attrs, start, parent);

        parseEmptyBody(doBodyNode, "jsp:doBody");
    }

    private void parseElement(Node parent) throws JasperException {
        Attributes attrs = parseAttributes();
        reader.skipSpaces();

        Node elementNode = new Node.JspElement(attrs, start, parent);

        parseOptionalBody(elementNode, "jsp:element", TagInfo.BODY_CONTENT_JSP);
    }

    /*
     * For GetProperty: StdActionContent ::= Attributes EmptyBody
     */
    private void parseGetProperty(Node parent) throws JasperException {
        Attributes attrs = parseAttributes();
        reader.skipSpaces();

        Node getPropertyNode = new Node.GetProperty(attrs, start, parent);

        parseOptionalBody(getPropertyNode, "jsp:getProperty",
                TagInfo.BODY_CONTENT_EMPTY);
    }

    /*
     * For SetProperty: StdActionContent ::= Attributes EmptyBody
     */
    private void parseSetProperty(Node parent) throws JasperException {
        Attributes attrs = parseAttributes();
        reader.skipSpaces();

        Node setPropertyNode = new Node.SetProperty(attrs, start, parent);

        parseOptionalBody(setPropertyNode, "jsp:setProperty",
                TagInfo.BODY_CONTENT_EMPTY);
    }

    /*
     * EmptyBody ::= '/>' | ( '>' ETag ) | ( '>' S? '<jsp:attribute'
     * NamedAttributes ETag )
     */
    private void parseEmptyBody(Node parent, String tag) throws JasperException {
        if (reader.matches("/>")) {
            // Done
        } else if (reader.matches(">")) {
            if (reader.matchesETag(tag)) {
                // Done
            } else if (reader.matchesOptionalSpacesFollowedBy("<jsp:attribute")) {
                // Parse the one or more named attribute nodes
                parseNamedAttributes(parent);
                if (!reader.matchesETag(tag)) {
                    // Body not allowed
                    err.jspError(reader.mark(),
                            MESSAGES.invalidEmptyBodyTag("&lt;" + tag));
                }
            } else {
                err.jspError(reader.mark(), MESSAGES.invalidEmptyBodyTag("&lt;" + tag));
            }
        } else {
            err.jspError(reader.mark(), MESSAGES.unterminatedTag("&lt;" + tag));
        }
    }

    /*
     * For UseBean: StdActionContent ::= Attributes OptionalBody
     */
    private void parseUseBean(Node parent) throws JasperException {
        Attributes attrs = parseAttributes();
        reader.skipSpaces();

        Node useBeanNode = new Node.UseBean(attrs, start, parent);

        parseOptionalBody(useBeanNode, "jsp:useBean", TagInfo.BODY_CONTENT_JSP);
    }

    /*
     * Parses OptionalBody, but also reused to parse bodies for plugin and param
     * since the syntax is identical (the only thing that differs substantially
     * is how to process the body, and thus we accept the body type as a
     * parameter).
     *
     * OptionalBody ::= EmptyBody | ActionBody
     *
     * ScriptlessOptionalBody ::= EmptyBody | ScriptlessActionBody
     *
     * TagDependentOptionalBody ::= EmptyBody | TagDependentActionBody
     *
     * EmptyBody ::= '/>' | ( '>' ETag ) | ( '>' S? '<jsp:attribute'
     * NamedAttributes ETag )
     *
     * ActionBody ::= JspAttributeAndBody | ( '>' Body ETag )
     *
     * ScriptlessActionBody ::= JspAttributeAndBody | ( '>' ScriptlessBody ETag )
     *
     * TagDependentActionBody ::= JspAttributeAndBody | ( '>' TagDependentBody
     * ETag )
     *
     */
    private void parseOptionalBody(Node parent, String tag, String bodyType)
            throws JasperException {
        if (reader.matches("/>")) {
            // EmptyBody
            return;
        }

        if (!reader.matches(">")) {
            err.jspError(reader.mark(), MESSAGES.unterminatedTag("&lt;" + tag));
        }

        if (reader.matchesETag(tag)) {
            // EmptyBody
            return;
        }

        if (!parseJspAttributeAndBody(parent, tag, bodyType)) {
            // Must be ( '>' # Body ETag )
            parseBody(parent, tag, bodyType);
        }
    }

    /**
     * Attempts to parse 'JspAttributeAndBody' production. Returns true if it
     * matched, or false if not. Assumes EmptyBody is okay as well.
     *
     * JspAttributeAndBody ::= ( '>' # S? ( '<jsp:attribute' NamedAttributes )? '<jsp:body' (
     * JspBodyBody | <TRANSLATION_ERROR> ) S? ETag )
     */
    private boolean parseJspAttributeAndBody(Node parent, String tag,
            String bodyType) throws JasperException {
        boolean result = false;

        if (reader.matchesOptionalSpacesFollowedBy("<jsp:attribute")) {
            // May be an EmptyBody, depending on whether
            // There's a "<jsp:body" before the ETag

            // First, parse <jsp:attribute> elements:
            parseNamedAttributes(parent);

            result = true;
        }

        if (reader.matchesOptionalSpacesFollowedBy("<jsp:body")) {
            // ActionBody
            parseJspBody(parent, bodyType);
            reader.skipSpaces();
            if (!reader.matchesETag(tag)) {
                err.jspError(reader.mark(), MESSAGES.unterminatedTag("&lt;" + tag));
            }

            result = true;
        } else if (result && !reader.matchesETag(tag)) {
            // If we have <jsp:attribute> but something other than
            // <jsp:body> or the end tag, translation error.
            err.jspError(reader.mark(), MESSAGES.invalidTagBody("&lt;" + tag));
        }

        return result;
    }

    /*
     * Params ::= `>' S? ( ( `<jsp:body>' ( ( S? Param+ S? `</jsp:body>' ) |
     * <TRANSLATION_ERROR> ) ) | Param+ ) '</jsp:params>'
     */
    private void parseJspParams(Node parent) throws JasperException {
        Node jspParamsNode = new Node.ParamsAction(start, parent);
        parseOptionalBody(jspParamsNode, "jsp:params", JAKARTA_BODY_CONTENT_PARAM);
    }

    /*
     * Fallback ::= '/>' | ( `>' S? `<jsp:body>' ( ( S? ( Char* - ( Char* `</jsp:body>' ) ) `</jsp:body>'
     * S? ) | <TRANSLATION_ERROR> ) `</jsp:fallback>' ) | ( '>' ( Char* - (
     * Char* '</jsp:fallback>' ) ) '</jsp:fallback>' )
     */
    private void parseFallBack(Node parent) throws JasperException {
        Node fallBackNode = new Node.FallBackAction(start, parent);
        parseOptionalBody(fallBackNode, "jsp:fallback",
                JAKARTA_BODY_CONTENT_TEMPLATE_TEXT);
    }

    /*
     * For Plugin: StdActionContent ::= Attributes PluginBody
     *
     * PluginBody ::= EmptyBody | ( '>' S? ( '<jsp:attribute' NamedAttributes )? '<jsp:body' (
     * JspBodyPluginTags | <TRANSLATION_ERROR> ) S? ETag ) | ( '>' S? PluginTags
     * ETag )
     *
     * EmptyBody ::= '/>' | ( '>' ETag ) | ( '>' S? '<jsp:attribute'
     * NamedAttributes ETag )
     *
     */
    private void parsePlugin(Node parent) throws JasperException {
        Attributes attrs = parseAttributes();
        reader.skipSpaces();

        Node pluginNode = new Node.PlugIn(attrs, start, parent);

        parseOptionalBody(pluginNode, "jsp:plugin", JAKARTA_BODY_CONTENT_PLUGIN);
    }

    /*
     * PluginTags ::= ( '<jsp:params' Params S? )? ( '<jsp:fallback' Fallback?
     * S? )?
     */
    private void parsePluginTags(Node parent) throws JasperException {
        reader.skipSpaces();

        if (reader.matches("<jsp:params")) {
            parseJspParams(parent);
            reader.skipSpaces();
        }

        if (reader.matches("<jsp:fallback")) {
            parseFallBack(parent);
            reader.skipSpaces();
        }
    }

    /*
     * StandardAction ::= 'include' StdActionContent | 'forward'
     * StdActionContent | 'invoke' StdActionContent | 'doBody' StdActionContent |
     * 'getProperty' StdActionContent | 'setProperty' StdActionContent |
     * 'useBean' StdActionContent | 'plugin' StdActionContent | 'element'
     * StdActionContent
     */
    private void parseStandardAction(Node parent) throws JasperException {
        Mark start = reader.mark();

        if (reader.matches(INCLUDE_ACTION)) {
            parseInclude(parent);
        } else if (reader.matches(FORWARD_ACTION)) {
            parseForward(parent);
        } else if (reader.matches(INVOKE_ACTION)) {
            if (!isTagFile) {
                err.jspError(reader.mark(), MESSAGES.invalidDirectiveInPage("&lt;jsp:invoke"));
            }
            parseInvoke(parent);
        } else if (reader.matches(DOBODY_ACTION)) {
            if (!isTagFile) {
                err.jspError(reader.mark(), MESSAGES.invalidDirectiveInPage("&lt;jsp:doBody"));
            }
            parseDoBody(parent);
        } else if (reader.matches(GET_PROPERTY_ACTION)) {
            parseGetProperty(parent);
        } else if (reader.matches(SET_PROPERTY_ACTION)) {
            parseSetProperty(parent);
        } else if (reader.matches(USE_BEAN_ACTION)) {
            parseUseBean(parent);
        } else if (reader.matches(PLUGIN_ACTION)) {
            parsePlugin(parent);
        } else if (reader.matches(ELEMENT_ACTION)) {
            parseElement(parent);
        } else if (reader.matches(ATTRIBUTE_ACTION)) {
            err.jspError(start, MESSAGES.invalidJspAttribute());
        } else if (reader.matches(BODY_ACTION)) {
            err.jspError(start, MESSAGES.invalidJspBody());
        } else if (reader.matches(FALLBACK_ACTION)) {
            err.jspError(start, MESSAGES.invalidJspFallback());
        } else if (reader.matches(PARAMS_ACTION)) {
            err.jspError(start, MESSAGES.invalidJspParams());
        } else if (reader.matches(PARAM_ACTION)) {
            err.jspError(start, MESSAGES.invalidJspParam());
        } else if (reader.matches(OUTPUT_ACTION)) {
            err.jspError(start, MESSAGES.invalidJspOutput());
        } else {
            err.jspError(start, MESSAGES.invalidStandardAction());
        }
    }

    /*
     * # '<' CustomAction CustomActionBody
     *
     * CustomAction ::= TagPrefix ':' CustomActionName
     *
     * TagPrefix ::= Name
     *
     * CustomActionName ::= Name
     *
     * CustomActionBody ::= ( Attributes CustomActionEnd ) | <TRANSLATION_ERROR>
     *
     * Attributes ::= ( S Attribute )* S?
     *
     * CustomActionEnd ::= CustomActionTagDependent | CustomActionJSPContent |
     * CustomActionScriptlessContent
     *
     * CustomActionTagDependent ::= TagDependentOptionalBody
     *
     * CustomActionJSPContent ::= OptionalBody
     *
     * CustomActionScriptlessContent ::= ScriptlessOptionalBody
     */
    @SuppressWarnings("null") // tagFileInfo can't be null after initial test
    private boolean parseCustomTag(Node parent) throws JasperException {

        if (reader.peekChar() != '<') {
            return false;
        }

        // Parse 'CustomAction' production (tag prefix and custom action name)
        reader.nextChar(); // skip '<'
        String tagName = reader.parseToken(false);
        int i = tagName.indexOf(':');
        if (i == -1) {
            reader.reset(start);
            return false;
        }

        String prefix = tagName.substring(0, i);
        String shortTagName = tagName.substring(i + 1);

        // Check if this is a user-defined tag.
        String uri = pageInfo.getURI(prefix);
        if (uri == null) {
            if (pageInfo.isErrorOnUndeclaredNamespace()) {
                err.jspError(start, MESSAGES.unknownTagPrefix(shortTagName, prefix));
            } else {
            reader.reset(start);
            // Remember the prefix for later error checking
            pageInfo.putNonCustomTagPrefix(prefix, reader.mark());
            return false;
            }
        }

        TagLibraryInfo tagLibInfo = pageInfo.getTaglib(uri);
        TagInfo tagInfo = tagLibInfo.getTag(shortTagName);
        TagFileInfo tagFileInfo = tagLibInfo.getTagFile(shortTagName);
        if (tagInfo == null && tagFileInfo == null) {
            err.jspError(start, MESSAGES.unknownTagPrefix(shortTagName, prefix));
        }
        Class<?> tagHandlerClass = null;
        if (tagInfo != null) {
            // Must be a classic tag, load it here.
            // tag files will be loaded later, in TagFileProcessor
            String handlerClassName = tagInfo.getTagClassName();
            try {
                tagHandlerClass = ctxt.getClassLoader().loadClass(
                        handlerClassName);
            } catch (Exception e) {
                err.jspError(start, MESSAGES.errorLoadingTagHandler(handlerClassName, tagName));
            }
        }

        // Parse 'CustomActionBody' production:
        // At this point we are committed - if anything fails, we produce
        // a translation error.

        // Parse 'Attributes' production:
        Attributes attrs = parseAttributes();
        reader.skipSpaces();

        // Parse 'CustomActionEnd' production:
        if (reader.matches("/>")) {
            if (tagInfo != null) {
                @SuppressWarnings("unused")
                Node unused = new Node.CustomTag(tagName, prefix, shortTagName,
                        uri, attrs, start, parent, tagInfo, tagHandlerClass);
            } else {
                @SuppressWarnings("unused")
                Node unused = new Node.CustomTag(tagName, prefix, shortTagName,
                        uri, attrs, start, parent, tagFileInfo);
            }
            return true;
        }

        // Now we parse one of 'CustomActionTagDependent',
        // 'CustomActionJSPContent', or 'CustomActionScriptlessContent'.
        // depending on body-content in TLD.

        // Looking for a body, it still can be empty; but if there is a
        // a tag body, its syntax would be dependent on the type of
        // body content declared in the TLD.
        String bc;
        if (tagInfo != null) {
            bc = tagInfo.getBodyContent();
        } else {
            bc = tagFileInfo.getTagInfo().getBodyContent();
        }

        Node tagNode = null;
        if (tagInfo != null) {
            tagNode = new Node.CustomTag(tagName, prefix, shortTagName, uri,
                    attrs, start, parent, tagInfo, tagHandlerClass);
        } else {
            tagNode = new Node.CustomTag(tagName, prefix, shortTagName, uri,
                    attrs, start, parent, tagFileInfo);
        }

        parseOptionalBody(tagNode, tagName, bc);

        return true;
    }

    /*
     * Parse for a template text string until '<' or "${" or "#{" is encountered,
     * recognizing escape sequences "<\%", "\${", and "\#{".
     */
    private void parseTemplateText(Node parent) {

        if (!reader.hasMoreInput())
            return;

        CharArrayWriter ttext = new CharArrayWriter();

        int ch = reader.nextChar();
        while (ch != -1) {
            if (ch == '<') {
                // Check for "<\%"
                if (reader.peekChar(0) == '\\' && reader.peekChar(1) == '%') {
                    ttext.write(ch);
                    // Swallow the \
                    reader.nextChar();
                    ttext.write(reader.nextChar());
                } else {
                    if (ttext.size() == 0) {
                        ttext.write(ch);
                    } else {
            reader.pushChar();
                        break;
                    }
                }
            } else if (ch == '\\' && !pageInfo.isELIgnored()) {
                int next = reader.peekChar(0);
                if (next == '$' || next == '#') {
                    if (reader.peekChar(1) == '{') {
                        ttext.write(reader.nextChar());
                        ttext.write(reader.nextChar());
        } else {
            ttext.write(ch);
                        ttext.write(reader.nextChar());
        }
                } else {
                    ttext.write(ch);
                }
            } else if ((ch == '$' || ch == '#' && !pageInfo.isDeferredSyntaxAllowedAsLiteral()) &&
                    !pageInfo.isELIgnored()) {
                if (reader.peekChar(0) == '{') {
                    reader.pushChar();
                    break;
                } else {
                    ttext.write(ch);
                }
            } else {
                ttext.write(ch);
                }
                    ch = reader.nextChar();
                }

        @SuppressWarnings("unused")
        Node unused = new Node.TemplateText(ttext.toString(), start, parent);
    }

    /*
     * XMLTemplateText ::= ( S? '/>' ) | ( S? '>' ( ( Char* - ( Char* ( '<' |
     * '${' ) ) ) ( '${' ELExpressionBody )? CDSect? )* ETag ) |
     * <TRANSLATION_ERROR>
     */
    private void parseXMLTemplateText(Node parent) throws JasperException {
        reader.skipSpaces();
        if (!reader.matches("/>")) {
            if (!reader.matches(">")) {
                err.jspError(start, MESSAGES.unterminatedTag("&lt;jsp:text&gt;"));
            }
            CharArrayWriter ttext = new CharArrayWriter();
                int ch = reader.nextChar();
            while (ch != -1) {
                if (ch == '<') {
                    // Check for <![CDATA[
                    if (!reader.matches("![CDATA[")) {
                        break;
                    }
                    start = reader.mark();
                    Mark stop = reader.skipUntil("]]>");
                    if (stop == null) {
                        err.jspError(start, MESSAGES.unterminatedTag("CDATA"));
                    }
                    String text = reader.getText(start, stop);
                    ttext.write(text, 0, text.length());
                } else if (ch == '\\') {
                    int next = reader.peekChar(0);
                    if (next == '$' || next =='#') {
                        if (reader.peekChar(1) == '{') {
                            ttext.write(reader.nextChar());
                            ttext.write(reader.nextChar());
                    }
                    } else {
                        ttext.write('\\');
                    }
                } else if (ch == '$' || ch == '#') {
                    if (reader.peekChar(0) == '{') {
                        // Swallow the '{'
                        reader.nextChar();

                    // Create a template text node
                        @SuppressWarnings("unused")
                        Node unused = new Node.TemplateText(
                                ttext.toString(), start, parent);

                    // Mark and parse the EL expression and create its node:
                    parseELExpression(parent, (char) ch);

                    start = reader.mark();
                        ttext.reset();
                } else {
                    ttext.write(ch);
                }
                } else {
                    ttext.write(ch);
                }
                ch = reader.nextChar();
            }

            @SuppressWarnings("unused")
            Node unused =
            new Node.TemplateText(ttext.toString(), start, parent);

            if (!reader.hasMoreInput()) {
                err.jspError(start, MESSAGES.unterminatedTag("&lt;jsp:text&gt;"));
            } else if (!reader.matchesETagWithoutLessThan("jsp:text")) {
                err.jspError(start, MESSAGES.badContent());
            }
        }
    }

    /*
     * AllBody ::= ( '<%--' JSPCommentBody ) | ( '<%@' DirectiveBody ) | ( '<jsp:directive.'
     * XMLDirectiveBody ) | ( '<%!' DeclarationBody ) | ( '<jsp:declaration'
     * XMLDeclarationBody ) | ( '<%=' ExpressionBody ) | ( '<jsp:expression'
     * XMLExpressionBody ) | ( '${' ELExpressionBody ) | ( '<%' ScriptletBody ) | ( '<jsp:scriptlet'
     * XMLScriptletBody ) | ( '<jsp:text' XMLTemplateText ) | ( '<jsp:'
     * StandardAction ) | ( '<' CustomAction CustomActionBody ) | TemplateText
     */
    private void parseElements(Node parent) throws JasperException {
        if (scriptlessCount > 0) {
            // vc: ScriptlessBody
            // We must follow the ScriptlessBody production if one of
            // our parents is ScriptlessBody.
            parseElementsScriptless(parent);
            return;
        }

        start = reader.mark();
        if (reader.matches("<%--")) {
            parseComment(parent);
        } else if (reader.matches("<%@")) {
            parseDirective(parent);
        } else if (reader.matches("<jsp:directive.")) {
            parseXMLDirective(parent);
        } else if (reader.matches("<%!")) {
            parseDeclaration(parent);
        } else if (reader.matches("<jsp:declaration")) {
            parseXMLDeclaration(parent);
        } else if (reader.matches("<%=")) {
            parseExpression(parent);
        } else if (reader.matches("<jsp:expression")) {
            parseXMLExpression(parent);
        } else if (reader.matches("<%")) {
            parseScriptlet(parent);
        } else if (reader.matches("<jsp:scriptlet")) {
            parseXMLScriptlet(parent);
        } else if (reader.matches("<jsp:text")) {
            parseXMLTemplateText(parent);
        } else if (!pageInfo.isELIgnored() && reader.matches("${")) {
            parseELExpression(parent, '$');
        } else if (!pageInfo.isELIgnored()
                && !pageInfo.isDeferredSyntaxAllowedAsLiteral()
                && reader.matches("#{")) {
            parseELExpression(parent, '#');
        } else if (reader.matches("<jsp:")) {
            parseStandardAction(parent);
        } else if (!parseCustomTag(parent)) {
            checkUnbalancedEndTag();
            parseTemplateText(parent);
        }
    }

    /*
     * ScriptlessBody ::= ( '<%--' JSPCommentBody ) | ( '<%@' DirectiveBody ) | ( '<jsp:directive.'
     * XMLDirectiveBody ) | ( '<%!' <TRANSLATION_ERROR> ) | ( '<jsp:declaration'
     * <TRANSLATION_ERROR> ) | ( '<%=' <TRANSLATION_ERROR> ) | ( '<jsp:expression'
     * <TRANSLATION_ERROR> ) | ( '<%' <TRANSLATION_ERROR> ) | ( '<jsp:scriptlet'
     * <TRANSLATION_ERROR> ) | ( '<jsp:text' XMLTemplateText ) | ( '${'
     * ELExpressionBody ) | ( '<jsp:' StandardAction ) | ( '<' CustomAction
     * CustomActionBody ) | TemplateText
     */
    private void parseElementsScriptless(Node parent) throws JasperException {
        // Keep track of how many scriptless nodes we've encountered
        // so we know whether our child nodes are forced scriptless
        scriptlessCount++;

        start = reader.mark();
        if (reader.matches("<%--")) {
            parseComment(parent);
        } else if (reader.matches("<%@")) {
            parseDirective(parent);
        } else if (reader.matches("<jsp:directive.")) {
            parseXMLDirective(parent);
        } else if (reader.matches("<%!")) {
            err.jspError(reader.mark(), MESSAGES.invalidScriptingElement());
        } else if (reader.matches("<jsp:declaration")) {
            err.jspError(reader.mark(), MESSAGES.invalidScriptingElement());
        } else if (reader.matches("<%=")) {
            err.jspError(reader.mark(), MESSAGES.invalidScriptingElement());
        } else if (reader.matches("<jsp:expression")) {
            err.jspError(reader.mark(), MESSAGES.invalidScriptingElement());
        } else if (reader.matches("<%")) {
            err.jspError(reader.mark(), MESSAGES.invalidScriptingElement());
        } else if (reader.matches("<jsp:scriptlet")) {
            err.jspError(reader.mark(), MESSAGES.invalidScriptingElement());
        } else if (reader.matches("<jsp:text")) {
            parseXMLTemplateText(parent);
        } else if (!pageInfo.isELIgnored() && reader.matches("${")) {
            parseELExpression(parent, '$');
        } else if (!pageInfo.isELIgnored()
                && !pageInfo.isDeferredSyntaxAllowedAsLiteral()
                && reader.matches("#{")) {
            parseELExpression(parent, '#');
        } else if (reader.matches("<jsp:")) {
            parseStandardAction(parent);
        } else if (!parseCustomTag(parent)) {
            checkUnbalancedEndTag();
            parseTemplateText(parent);
        }

        scriptlessCount--;
    }

    /*
     * TemplateTextBody ::= ( '<%--' JSPCommentBody ) | ( '<%@' DirectiveBody ) | ( '<jsp:directive.'
     * XMLDirectiveBody ) | ( '<%!' <TRANSLATION_ERROR> ) | ( '<jsp:declaration'
     * <TRANSLATION_ERROR> ) | ( '<%=' <TRANSLATION_ERROR> ) | ( '<jsp:expression'
     * <TRANSLATION_ERROR> ) | ( '<%' <TRANSLATION_ERROR> ) | ( '<jsp:scriptlet'
     * <TRANSLATION_ERROR> ) | ( '<jsp:text' <TRANSLATION_ERROR> ) | ( '${'
     * <TRANSLATION_ERROR> ) | ( '<jsp:' <TRANSLATION_ERROR> ) | TemplateText
     */
    private void parseElementsTemplateText(Node parent) throws JasperException {
        start = reader.mark();
        if (reader.matches("<%--")) {
            parseComment(parent);
        } else if (reader.matches("<%@")) {
            parseDirective(parent);
        } else if (reader.matches("<jsp:directive.")) {
            parseXMLDirective(parent);
        } else if (reader.matches("<%!")) {
            err.jspError(reader.mark(), MESSAGES.invalidTemplateTextBody("&lt;%!"));
        } else if (reader.matches("<jsp:declaration")) {
            err.jspError(reader.mark(), MESSAGES.invalidTemplateTextBody("&lt;jsp:declaration"));
        } else if (reader.matches("<%=")) {
            err.jspError(reader.mark(), MESSAGES.invalidTemplateTextBody("&lt;%="));
        } else if (reader.matches("<jsp:expression")) {
            err.jspError(reader.mark(), MESSAGES.invalidTemplateTextBody("&lt;jsp:expression"));
        } else if (reader.matches("<%")) {
            err.jspError(reader.mark(), MESSAGES.invalidTemplateTextBody("&lt;%"));
        } else if (reader.matches("<jsp:scriptlet")) {
            err.jspError(reader.mark(), MESSAGES.invalidTemplateTextBody("&lt;jsp:scriptlet"));
        } else if (reader.matches("<jsp:text")) {
            err.jspError(reader.mark(), MESSAGES.invalidTemplateTextBody("&lt;jsp:text"));
        } else if (!pageInfo.isELIgnored() && reader.matches("${")) {
            err.jspError(reader.mark(), MESSAGES.invalidTemplateTextBody("${"));
        } else if (!pageInfo.isELIgnored()
                && !pageInfo.isDeferredSyntaxAllowedAsLiteral()
                && reader.matches("#{")) {
            err.jspError(reader.mark(), MESSAGES.invalidTemplateTextBody("#{"));
        } else if (reader.matches("<jsp:")) {
            err.jspError(reader.mark(), MESSAGES.invalidTemplateTextBody("&lt;jsp:"));
        } else if (parseCustomTag(parent)) {
            err.jspError(reader.mark(), MESSAGES.invalidTagInTemplateTextBody());
        } else {
            checkUnbalancedEndTag();
            parseTemplateText(parent);
        }
    }

    /*
     * Flag as error if an unbalanced end tag appears by itself.
     */
    private void checkUnbalancedEndTag() throws JasperException {

        if (!reader.matches("</")) {
            return;
        }

        // Check for unbalanced standard actions
        if (reader.matches("jsp:")) {
            err.jspError(start, MESSAGES.unbalancedEndTag("jsp:"));
        }

        // Check for unbalanced custom actions
        String tagName = reader.parseToken(false);
        int i = tagName.indexOf(':');
        if (i == -1 || pageInfo.getURI(tagName.substring(0, i)) == null) {
            reader.reset(start);
            return;
        }

        err.jspError(start, MESSAGES.unbalancedEndTag(tagName));
    }

    /**
     * TagDependentBody :=
     */
    private void parseTagDependentBody(Node parent, String tag)
            throws JasperException {
        Mark bodyStart = reader.mark();
        Mark bodyEnd = reader.skipUntilETag(tag);
        if (bodyEnd == null) {
            err.jspError(start, MESSAGES.unterminatedTag("&lt;" + tag));
        }
        @SuppressWarnings("unused")
        Node unused = new Node.TemplateText(reader.getText(bodyStart, bodyEnd),
                bodyStart, parent);
    }

    /*
     * Parses jsp:body action.
     */
    private void parseJspBody(Node parent, String bodyType)
            throws JasperException {
        Mark start = reader.mark();
        Node bodyNode = new Node.JspBody(start, parent);

        reader.skipSpaces();
        if (!reader.matches("/>")) {
            if (!reader.matches(">")) {
                err.jspError(start, MESSAGES.unterminatedTag("&lt;jsp:body"));
            }
            parseBody(bodyNode, "jsp:body", bodyType);
        }
    }

    /*
     * Parse the body as JSP content. @param tag The name of the tag whose end
     * tag would terminate the body @param bodyType One of the TagInfo body
     * types
     */
    private void parseBody(Node parent, String tag, String bodyType)
            throws JasperException {
        if (bodyType.equalsIgnoreCase(TagInfo.BODY_CONTENT_TAG_DEPENDENT)) {
            parseTagDependentBody(parent, tag);
        } else if (bodyType.equalsIgnoreCase(TagInfo.BODY_CONTENT_EMPTY)) {
            if (!reader.matchesETag(tag)) {
                err.jspError(start, MESSAGES.invalidEmptyTagSubelements(tag));
            }
        } else if (bodyType == JAKARTA_BODY_CONTENT_PLUGIN) {
            // (note the == since we won't recognize JAKARTA_*
            // from outside this module).
            parsePluginTags(parent);
            if (!reader.matchesETag(tag)) {
                err.jspError(reader.mark(), MESSAGES.unterminatedTag("&lt;" + tag));
            }
        } else if (bodyType.equalsIgnoreCase(TagInfo.BODY_CONTENT_JSP)
                || bodyType.equalsIgnoreCase(TagInfo.BODY_CONTENT_SCRIPTLESS)
                || (bodyType == JAKARTA_BODY_CONTENT_PARAM)
                || (bodyType == JAKARTA_BODY_CONTENT_TEMPLATE_TEXT)) {
            while (reader.hasMoreInput()) {
                if (reader.matchesETag(tag)) {
                    return;
                }

                // Check for nested jsp:body or jsp:attribute
                if (tag.equals("jsp:body") || tag.equals("jsp:attribute")) {
                    if (reader.matches("<jsp:attribute")) {
                        err.jspError(reader.mark(), MESSAGES.invalidJspAttributeNesting());
                    } else if (reader.matches("<jsp:body")) {
                        err.jspError(reader.mark(), MESSAGES.invalidJspBodyNesting());
                    }
                }

                if (bodyType.equalsIgnoreCase(TagInfo.BODY_CONTENT_JSP)) {
                    parseElements(parent);
                } else if (bodyType
                        .equalsIgnoreCase(TagInfo.BODY_CONTENT_SCRIPTLESS)) {
                    parseElementsScriptless(parent);
                } else if (bodyType == JAKARTA_BODY_CONTENT_PARAM) {
                    // (note the == since we won't recognize JAKARTA_*
                    // from outside this module).
                    reader.skipSpaces();
                    parseParam(parent);
                } else if (bodyType == JAKARTA_BODY_CONTENT_TEMPLATE_TEXT) {
                    parseElementsTemplateText(parent);
                }
            }
            err.jspError(start, MESSAGES.unterminatedTag("&lt;" + tag));
        } else {
            err.jspError(start, MESSAGES.invalidBodyContentType());
        }
    }

    /*
     * Parses named attributes.
     */
    private void parseNamedAttributes(Node parent) throws JasperException {
        do {
            Mark start = reader.mark();
            Attributes attrs = parseAttributes();
            Node.NamedAttribute namedAttributeNode = new Node.NamedAttribute(
                    attrs, start, parent);

            reader.skipSpaces();
            if (!reader.matches("/>")) {
                if (!reader.matches(">")) {
                    err.jspError(start, MESSAGES.unterminatedTag("&lt;jsp:attribute"));
                }
                if (namedAttributeNode.isTrim()) {
                    reader.skipSpaces();
                }
                parseBody(namedAttributeNode, "jsp:attribute",
                        getAttributeBodyType(parent, attrs.getValue("name")));
                if (namedAttributeNode.isTrim()) {
                    Node.Nodes subElems = namedAttributeNode.getBody();
                    if (subElems != null) {
                        Node lastNode = subElems.getNode(subElems.size() - 1);
                        if (lastNode instanceof Node.TemplateText) {
                            ((Node.TemplateText) lastNode).rtrim();
                        }
                    }
                }
            }
            reader.skipSpaces();
        } while (reader.matches("<jsp:attribute"));
    }

    /**
     * Determine the body type of <jsp:attribute> from the enclosing node
     */
    private String getAttributeBodyType(Node n, String name) {

        if (n instanceof Node.CustomTag) {
            TagInfo tagInfo = ((Node.CustomTag) n).getTagInfo();
            TagAttributeInfo[] tldAttrs = tagInfo.getAttributes();
            for (int i = 0; i < tldAttrs.length; i++) {
                if (name.equals(tldAttrs[i].getName())) {
                    if (tldAttrs[i].isFragment()) {
                        return TagInfo.BODY_CONTENT_SCRIPTLESS;
                    }
                    if (tldAttrs[i].canBeRequestTime()) {
                        return TagInfo.BODY_CONTENT_JSP;
                    }
                }
            }
            if (tagInfo.hasDynamicAttributes()) {
                return TagInfo.BODY_CONTENT_JSP;
            }
        } else if (n instanceof Node.IncludeAction) {
            if ("page".equals(name)) {
                return TagInfo.BODY_CONTENT_JSP;
            }
        } else if (n instanceof Node.ForwardAction) {
            if ("page".equals(name)) {
                return TagInfo.BODY_CONTENT_JSP;
            }
        } else if (n instanceof Node.SetProperty) {
            if ("value".equals(name)) {
                return TagInfo.BODY_CONTENT_JSP;
            }
        } else if (n instanceof Node.UseBean) {
            if ("beanName".equals(name)) {
                return TagInfo.BODY_CONTENT_JSP;
            }
        } else if (n instanceof Node.PlugIn) {
            if ("width".equals(name) || "height".equals(name)) {
                return TagInfo.BODY_CONTENT_JSP;
            }
        } else if (n instanceof Node.ParamAction) {
            if ("value".equals(name)) {
                return TagInfo.BODY_CONTENT_JSP;
            }
        } else if (n instanceof Node.JspElement) {
            return TagInfo.BODY_CONTENT_JSP;
        }

        return JAKARTA_BODY_CONTENT_TEMPLATE_TEXT;
    }

    private void parseFileDirectives(Node parent) throws JasperException {
        reader.skipUntil("<");
        while (reader.hasMoreInput()) {
            start = reader.mark();
            if (reader.matches("%--")) {
                // Comment
                reader.skipUntil("--%>");
            } else if (reader.matches("%@")) {
                parseDirective(parent);
            } else if (reader.matches("jsp:directive.")) {
                parseXMLDirective(parent);
            } else if (reader.matches("%!")) {
                // Declaration
                reader.skipUntil("%>");
            } else if (reader.matches("%=")) {
                // Expression
                reader.skipUntil("%>");
            } else if (reader.matches("%")) {
                // Scriptlet
                reader.skipUntil("%>");
            }
            reader.skipUntil("<");
        }
    }
}
