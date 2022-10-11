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

public class TagInfo {

    /*
     * private fields for 1.1 info
     */
    protected String             tagName; // the name of the tag
    protected String             tagClassName;
    protected String             bodyContent;
    protected String             infoString;
    protected String             tagExtraInfo;
    protected ArrayList<TagAttributeInfo> tagAttributeInfos = new ArrayList<TagAttributeInfo>();

    /*
     * private fields for 1.2 info
     */
    protected String             displayName;
    protected String             smallIcon;
    protected String             largeIcon;
    protected ArrayList<TagVariableInfo> tagVariableInfos = new ArrayList<TagVariableInfo>();

    /*
     * Additional private fields for 2.0 info
     */
    protected String dynamicAttributes;
    
    public void addTagAttributeInfo(TagAttributeInfo tagAttributeInfo) {
        tagAttributeInfos.add(tagAttributeInfo);
    }
    
    public TagAttributeInfo[] getTagAttributeInfos() {
        return tagAttributeInfos.toArray(new TagAttributeInfo[0]);
    }
    
    public void addTagVariableInfo(TagVariableInfo tagVariableInfo) {
        tagVariableInfos.add(tagVariableInfo);
    }
    
    public TagVariableInfo[] getTagVariableInfos() {
        return tagVariableInfos.toArray(new TagVariableInfo[0]);
    }
    
    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagClassName() {
        return tagClassName;
    }

    public void setTagClassName(String tagClassName) {
        this.tagClassName = tagClassName;
    }

    public String getBodyContent() {
        return bodyContent;
    }

    public void setBodyContent(String bodyContent) {
        this.bodyContent = bodyContent;
    }

    public String getInfoString() {
        return infoString;
    }

    public void setInfoString(String infoString) {
        this.infoString = infoString;
    }

    public String getTagExtraInfo() {
        return tagExtraInfo;
    }

    public void setTagExtraInfo(String tagExtraInfo) {
        this.tagExtraInfo = tagExtraInfo;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getSmallIcon() {
        return smallIcon;
    }

    public void setSmallIcon(String smallIcon) {
        this.smallIcon = smallIcon;
    }

    public String getLargeIcon() {
        return largeIcon;
    }

    public void setLargeIcon(String largeIcon) {
        this.largeIcon = largeIcon;
    }

    public String getDynamicAttributes() {
        return dynamicAttributes;
    }

    public void setDynamicAttributes(String dynamicAttributes) {
        this.dynamicAttributes = dynamicAttributes;
    }

}
