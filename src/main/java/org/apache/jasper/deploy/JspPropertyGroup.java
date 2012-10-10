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

package org.apache.jasper.deploy;

import java.io.Serializable;
import java.util.ArrayList;

import javax.servlet.descriptor.JspPropertyGroupDescriptor;

public class JspPropertyGroup implements Serializable, JspPropertyGroupDescriptor {

    protected ArrayList<String> urlPatterns = new ArrayList<String>();
    protected String elIgnored = null;
    protected String pageEncoding = null;
    protected String scriptingInvalid = null;
    protected String isXml = null;
    protected ArrayList<String> includePreludes = new ArrayList<String>();
    protected ArrayList<String> includeCodas = new ArrayList<String>();
    protected String deferredSyntaxAllowedAsLiteral = null;
    protected String trimDirectiveWhitespaces = null;
    protected String defaultContentType = null;
    protected String buffer = null;
    protected String errorOnUndeclaredNamespace = null;

    public void addUrlPattern(String urlPattern) {
        urlPatterns.add(urlPattern);
    }
    public String getPageEncoding() {
        return pageEncoding;
    }
    public void setPageEncoding(String pageEncoding) {
        this.pageEncoding = pageEncoding;
    }
    public void addIncludePrelude(String includePrelude) {
        includePreludes.add(includePrelude);
    }
    public void addIncludeCoda(String includeCoda) {
        includeCodas.add(includeCoda);
    }
    public String getDefaultContentType() {
        return defaultContentType;
    }
    public void setDefaultContentType(String defaultContentType) {
        this.defaultContentType = defaultContentType;
    }
    public String getBuffer() {
        return buffer;
    }
    public void setBuffer(String buffer) {
        this.buffer = buffer;
    }
    public String getElIgnored() {
        return elIgnored;
    }
    public void setElIgnored(String elIgnored) {
        this.elIgnored = elIgnored;
    }
    public String getScriptingInvalid() {
        return scriptingInvalid;
    }
    public void setScriptingInvalid(String scriptingInvalid) {
        this.scriptingInvalid = scriptingInvalid;
    }
    public String getIsXml() {
        return isXml;
    }
    public void setIsXml(String isXml) {
        this.isXml = isXml;
    }
    public String getDeferredSyntaxAllowedAsLiteral() {
        return deferredSyntaxAllowedAsLiteral;
    }
    public void setDeferredSyntaxAllowedAsLiteral(
            String deferredSyntaxAllowedAsLiteral) {
        this.deferredSyntaxAllowedAsLiteral = deferredSyntaxAllowedAsLiteral;
    }
    public String getTrimDirectiveWhitespaces() {
        return trimDirectiveWhitespaces;
    }
    public void setTrimDirectiveWhitespaces(String trimDirectiveWhitespaces) {
        this.trimDirectiveWhitespaces = trimDirectiveWhitespaces;
    }
    public String getErrorOnUndeclaredNamespace() {
        return errorOnUndeclaredNamespace;
    }
    public void setErrorOnUndeclaredNamespace(String errorOnUndeclaredNamespace) {
        this.errorOnUndeclaredNamespace = errorOnUndeclaredNamespace;
    }
    public ArrayList<String> getUrlPatterns() {
        return urlPatterns;
    }
    public ArrayList<String> getIncludePreludes() {
        return includePreludes;
    }
    public ArrayList<String> getIncludeCodas() {
        return includeCodas;
    }

}
