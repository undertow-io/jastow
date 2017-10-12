/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomcat.util.buf;


public final class ByteChunk {

    private final StringBuilder content;


    public ByteChunk(String content) {
        this.content = new StringBuilder(content);
    }

    public ByteChunk() {
        this.content = new StringBuilder();
    }

    @Override
    public String toString() {
        return content.toString();
    }

    public void write(String content) {
        this.content.append(content);
    }

    public void recycle() {
        content.setLength(0);
    }

    public String toStringInternal() {
        return toString();
    }

}
