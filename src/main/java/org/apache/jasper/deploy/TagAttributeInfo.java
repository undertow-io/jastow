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

public class TagAttributeInfo {
    protected String name;
    protected String type;
    protected String reqTime;
    protected String required;
    /*
     * private fields for JSP 2.0
     */
    protected String fragment;
    /*
     * private fields for JSP 2.1
     */
    protected String description;
    protected String deferredValue;
    protected String deferredMethod;
    protected String expectedTypeName;
    protected String methodSignature;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getExpectedTypeName() {
        return expectedTypeName;
    }
    public void setExpectedTypeName(String expectedTypeName) {
        this.expectedTypeName = expectedTypeName;
    }
    public String getMethodSignature() {
        return methodSignature;
    }
    public void setMethodSignature(String methodSignature) {
        this.methodSignature = methodSignature;
    }
    public String getReqTime() {
        return reqTime;
    }
    public void setReqTime(String reqTime) {
        this.reqTime = reqTime;
    }
    public String getRequired() {
        return required;
    }
    public void setRequired(String required) {
        this.required = required;
    }
    public String getFragment() {
        return fragment;
    }
    public void setFragment(String fragment) {
        this.fragment = fragment;
    }
    public String getDeferredValue() {
        return deferredValue;
    }
    public void setDeferredValue(String deferredValue) {
        this.deferredValue = deferredValue;
    }
    public String getDeferredMethod() {
        return deferredMethod;
    }
    public void setDeferredMethod(String deferredMethod) {
        this.deferredMethod = deferredMethod;
    }
    
}
