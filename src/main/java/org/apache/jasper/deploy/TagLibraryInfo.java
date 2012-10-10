/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
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

import java.util.ArrayList;

public class TagLibraryInfo {

    // Protected fields

    /**
     * The prefix assigned to this taglib from the taglib directive.
     */
    protected String        prefix;
    
    /**
     * The value of the uri attribute from the taglib directive for 
     * this library.
     */
    protected String        uri;
    
    protected ArrayList<String> listeners = new ArrayList<String>();
    
    protected TagLibraryValidatorInfo validator;

    /**
     * An array describing the tags that are defined in this tag library.
     */
    protected ArrayList<TagInfo>     tags = new ArrayList<TagInfo>();

    /**
     * An array describing the tag files that are defined in this tag library.
     *
     * @since 2.0
     */
    protected ArrayList<TagFileInfo> tagFiles = new ArrayList<TagFileInfo>();
    
    /**
     * An array describing the functions that are defined in this tag library.
     *
     * @since 2.0
     */
    protected ArrayList<FunctionInfo> functionsTag = new ArrayList<FunctionInfo>();

    // Tag Library Data
    
    /**
     * The version of the tag library.
     */
    protected String tlibversion; // required
    
    /**
     * The version of the JSP specification this tag library is written to.
     */
    protected String jspversion;  // required
    
    /**
     * The preferred short name (prefix) as indicated in the TLD.
     */
    protected String shortname;   // required
    
    /**
     * The "reliable" URN indicated in the TLD.
     */
    protected String urn;         // required
    
    /**
     * Information (documentation) for this TLD.
     */
    protected String info;        // optional

    /**
     * The location of the taglib, which should be the JAR path, or empty if exploded.
     */
    protected String location;
    
    /**
     * The access path for the taglib, relative to the location.
     */
    protected String path;
    
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getTlibversion() {
        return tlibversion;
    }

    public void setTlibversion(String tlibversion) {
        this.tlibversion = tlibversion;
    }

    public String getJspversion() {
        return jspversion;
    }

    public void setJspversion(String jspversion) {
        this.jspversion = jspversion;
    }

    /**
     * For the version attribute.
     */
    public void setVersion(String jspversion) {
        this.jspversion = jspversion;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String getUrn() {
        return urn;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void addFunctionInfo(FunctionInfo functionInfo) {
        functionsTag.add(functionInfo);
    }
    
    public FunctionInfo[] getFunctionInfos() {
        return functionsTag.toArray(new FunctionInfo[0]);
    }
    
    public void addTagFileInfo(TagFileInfo tagFileInfo) {
        tagFiles.add(tagFileInfo);
    }
    
    public TagFileInfo[] getTagFileInfos() {
        return tagFiles.toArray(new TagFileInfo[0]);
    }
    
    public void addTagInfo(TagInfo tagInfo) {
        tags.add(tagInfo);
    }
    
    public TagInfo[] getTags() {
        return tags.toArray(new TagInfo[0]);
    }
    
    public void addListener(String listener) {
        listeners.add(listener);
    }
    
    public String[] getListeners() {
        return listeners.toArray(new String[0]);
    }

    public TagLibraryValidatorInfo getValidator() {
        return validator;
    }

    public void setValidator(TagLibraryValidatorInfo validator) {
        this.validator = validator;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
