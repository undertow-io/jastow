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

import java.io.InputStream;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.servlet.jsp.tagext.FunctionInfo;
import javax.servlet.jsp.tagext.TagFileInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;

/**
 * Class responsible for generating an implicit tag library containing tag
 * handlers corresponding to the tag files in "/WEB-INF/tags/" or a 
 * subdirectory of it.
 *
 * @author Jan Luehe
 */
class ImplicitTagLibraryInfo extends TagLibraryInfo {

    private static final String WEB_INF_TAGS = "/WEB-INF/tags";
    private static final String TAG_FILE_SUFFIX = ".tag";
    private static final String TAGX_FILE_SUFFIX = ".tagx";
    private static final String TAGS_SHORTNAME = "tags";
    private static final String TLIB_VERSION = "1.0";
    private static final String JSP_VERSION = "2.0";
    private static final String IMPLICIT_TLD = "implicit.tld";

    // Maps tag names to tag file paths
    private final Hashtable<String,String> tagFileMap;

    private final ParserController pc;
    private final PageInfo pi;
    private final Vector<TagFileInfo> vec;

    /**
     * Constructor.
     */
    public ImplicitTagLibraryInfo(JspCompilationContext ctxt,
            ParserController pc,
            PageInfo pi,
            String prefix,
            String tagdir,
            ErrorDispatcher err) throws JasperException {
        super(prefix, null);
        this.pc = pc;
        this.pi = pi;
        this.tagFileMap = new Hashtable<>();
        this.vec = new Vector<>();

        // Implicit tag libraries have no functions:
        this.functions = new FunctionInfo[0];

        tlibversion = TLIB_VERSION;
        jspversion = JSP_VERSION;

        if (!tagdir.startsWith(WEB_INF_TAGS)) {
            err.jspError(MESSAGES.invalidTagFileDirectory(tagdir));
        }

        // Determine the value of the <short-name> subelement of the
        // "imaginary" <taglib> element
        if (tagdir.equals(WEB_INF_TAGS)
                || tagdir.equals( WEB_INF_TAGS + "/")) {
            shortname = TAGS_SHORTNAME;
        } else {
            shortname = tagdir.substring(WEB_INF_TAGS.length());
            shortname = shortname.replace('/', '-');
        }

        // Populate mapping of tag names to tag file paths
        Set<String> dirList = ctxt.getResourcePaths(tagdir);
        if (dirList != null) {
            Iterator it = dirList.iterator();
            while (it.hasNext()) {
                String path = (String) it.next();
                if (path.endsWith(TAG_FILE_SUFFIX)
                        || path.endsWith(TAGX_FILE_SUFFIX)) {
                    /*
                     * Use the filename of the tag file, without the .tag or
                     * .tagx extension, respectively, as the <name> subelement
                     * of the "imaginary" <tag-file> element
                     */
                    String suffix = path.endsWith(TAG_FILE_SUFFIX) ?
                            TAG_FILE_SUFFIX : TAGX_FILE_SUFFIX; 
                    String tagName = path.substring(path.lastIndexOf("/") + 1);
                    tagName = tagName.substring(0,
                            tagName.lastIndexOf(suffix));
                    tagFileMap.put(tagName, path);
                } else if (path.endsWith(IMPLICIT_TLD)) {
                    InputStream is = null;
                    try {
                        is = ctxt.getResourceAsStream(path);
                        if (is != null) {
                            
                            // Add implicit TLD to dependency list
                            if (pi != null) {
                                pi.addDependant(path, ctxt.getLastModified(path));
                            }
                            
                            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
                            
                            reader.require(XMLStreamConstants.START_DOCUMENT, null, null);
                            while (reader.hasNext() && reader.next() != XMLStreamConstants.START_ELEMENT) {
                                // Skip until first element
                            }
                            
                            final int count = reader.getAttributeCount();
                            for (int i = 0; i < count; i ++) {
                                if ("version".equals(reader.getAttributeLocalName(i))) {
                                    this.jspversion = reader.getAttributeValue(i);
                                }
                            }

                            while (reader.hasNext() && reader.nextTag() != XMLStreamConstants.END_ELEMENT) {
                                String elementName = reader.getLocalName();
                                if ("tlibversion".equals(elementName) // JSP 1.1
                                        || "tlib-version".equals(elementName)) { // JSP 1.2
                                    this.tlibversion = reader.getElementText().trim();
                                } else if ("jspversion".equals(elementName)
                                        || "jsp-version".equals(elementName)) {
                                    this.jspversion = reader.getElementText().trim();
                                } else if ("shortname".equals(elementName) || "short-name".equals(elementName)) {
                                    reader.getElementText();
                                } else {
                                    // All other elements are invalid
                                    err.jspError(MESSAGES.invalidImplicitTld(path));
                                }
                            }

                            try {
                                double version = Double.parseDouble(this.jspversion);
                                if (version < 2.0) {
                                    err.jspError(MESSAGES.invalidImplicitTldVersion(path));
                                }
                            } catch (NumberFormatException e) {
                                err.jspError(MESSAGES.invalidImplicitTldVersion(path));
                            }
                        }
                    } catch (XMLStreamException e) {
                        err.jspError(e, MESSAGES.invalidImplicitTld(path));
                    } finally {
                        if (is != null) {
                            try {
                                is.close();
                            } catch (Throwable t) {
                            }
                        }
                    }
                }
            }
        }        
        
    }

    /**
     * Checks to see if the given tag name maps to a tag file path,
     * and if so, parses the corresponding tag file.
     *
     * @return The TagFileInfo corresponding to the given tag name, or null if
     * the given tag name is not implemented as a tag file
     */
    @Override
    public TagFileInfo getTagFile(String shortName) {

        TagFileInfo tagFile = super.getTagFile(shortName);
        if (tagFile == null) {
            String path = tagFileMap.get(shortName);
            if (path == null) {
                return null;
            }

            TagInfo tagInfo = null;
            try {
                tagInfo = TagFileProcessor.parseTagFileDirectives(pc,
                        shortName,
                        path,
                        null,
                        this);
            } catch (JasperException je) {
                throw new RuntimeException(je.toString(), je);
            }

            tagFile = new TagFileInfo(shortName, path, tagInfo);
            vec.addElement(tagFile);

            this.tagFiles = new TagFileInfo[vec.size()];
            vec.copyInto(this.tagFiles);
        }

        return tagFile;
    }

    @Override
    public TagLibraryInfo[] getTagLibraryInfos() {
        Collection<TagLibraryInfo> coll = pi.getTaglibs();
        return coll.toArray(new TagLibraryInfo[0]);
    }

}
