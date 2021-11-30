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
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.tagplugin.TagPlugin;
import org.apache.jasper.compiler.tagplugin.TagPluginContext;

/**
 * Manages tag plugin optimizations.
 *
 * @author Kin-man Chung
 */

public class TagPluginManager {

    private static final String TAG_PLUGINS_XML = "/WEB-INF/tagPlugins.xml";
    private static final String TAG_PLUGINS_ROOT_ELEM = "tag-plugins";

    private boolean initialized = false;
    private HashMap<String, TagPlugin> tagPlugins = null;
    private ServletContext ctxt;

    public TagPluginManager(ServletContext ctxt) {
	this.ctxt = ctxt;
    }

    public void apply(Node.Nodes page, ErrorDispatcher err, PageInfo pageInfo)
            throws JasperException {

        init(err);
        if (!tagPlugins.isEmpty()) {
            page.visit(new NodeVisitor(this, pageInfo));
        }
    }

    private void init(ErrorDispatcher err) throws JasperException {
        if (initialized)
            return;
        tagPlugins = new HashMap<>();
        InputStream is = ctxt.getResourceAsStream(TAG_PLUGINS_XML);
        if (is == null)
            return;

        XMLStreamReader reader;
        try {
            reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
            reader.require(XMLStreamConstants.START_DOCUMENT, null, null);

            reader.require(XMLStreamConstants.START_DOCUMENT, null, null);
            while (reader.hasNext() && reader.next() != XMLStreamConstants.START_ELEMENT) {
                // Skip until first element
            }

            if (!TAG_PLUGINS_ROOT_ELEM.equals(reader.getLocalName())) {
                err.jspError(MESSAGES.wrongRootElement(TAG_PLUGINS_XML,
                        TAG_PLUGINS_ROOT_ELEM));
            }


            while (reader.hasNext() && reader.nextTag() != XMLStreamConstants.END_ELEMENT) {
                String elementName = reader.getLocalName();
                if ("tag-plugin".equals(elementName)) { // JSP 1.2
                    String tagClassName = null;
                    String pluginClassName = null;
                    while (reader.hasNext() && reader.nextTag() != XMLStreamConstants.END_ELEMENT) {
                        String childClementName = reader.getLocalName();
                        if ("tag-class".equals(childClementName)) {
                            tagClassName = reader.getElementText().trim();
                        } else if ("plugin-class".equals(childClementName)) {
                            pluginClassName = reader.getElementText().trim();
                        } else {
                            err.jspError(MESSAGES.invalidTagPlugin(TAG_PLUGINS_XML));
                        }
                    }
                    if (tagClassName == null || pluginClassName == null) {
                        err.jspError(MESSAGES.invalidTagPlugin(TAG_PLUGINS_XML));
                    }
                    TagPlugin tagPlugin = null;
                    try {
                        Class<?> pluginClass = Thread.currentThread().getContextClassLoader().loadClass(pluginClassName);
                        tagPlugin = (TagPlugin) pluginClass.newInstance();
                    } catch (Exception e) {
                        throw new JasperException(e);
                    }
                    if (tagPlugin == null) {
                        return;
                    }
                    tagPlugins.put(tagClassName, tagPlugin);

                } else {
                    // All other elements are invalid
                    err.jspError(MESSAGES.invalidTagPlugin(TAG_PLUGINS_XML));
                }
            }
        } catch (XMLStreamException e) {
            err.jspError(e, MESSAGES.invalidTagPlugin(TAG_PLUGINS_XML));
        } catch (FactoryConfigurationError e) {
            throw new JasperException(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable t) {
                }
            }
        }
        initialized = true;
    }

    /**
     * Invoke tag plugin for the given custom tag, if a plugin exists for
     * the custom tag's tag handler.
     * <p/>
     * The given custom tag node will be manipulated by the plugin.
     */
    private void invokePlugin(Node.CustomTag n, PageInfo pageInfo) {
        TagPlugin tagPlugin = tagPlugins.get(n.getTagHandlerClass().getName());
	if (tagPlugin == null) {
	    return;
	}

	TagPluginContext tagPluginContext = new TagPluginContextImpl(n, pageInfo);
	n.setTagPluginContext(tagPluginContext);
	tagPlugin.doTag(tagPluginContext);
    }

    private static class NodeVisitor extends Node.Visitor {
        private final TagPluginManager manager;
        private final PageInfo pageInfo;

        public NodeVisitor(TagPluginManager manager, PageInfo pageInfo) {
            this.manager = manager;
            this.pageInfo = pageInfo;
        }

        @Override
        public void visit(Node.CustomTag n) throws JasperException {
            manager.invokePlugin(n, pageInfo);
            visitBody(n);
        }
    }

     private static class TagPluginContextImpl implements TagPluginContext {
        private final Node.CustomTag node;
        private final PageInfo pageInfo;
        private final HashMap<String, Object> pluginAttributes;
	private Node.Nodes curNodes;

	TagPluginContextImpl(Node.CustomTag n, PageInfo pageInfo) {
	    this.node = n;
	    this.pageInfo = pageInfo;
	    curNodes = new Node.Nodes();
	    n.setAtETag(curNodes);
	    curNodes = new Node.Nodes();
	    n.setAtSTag(curNodes);
	    n.setUseTagPlugin(true);
            pluginAttributes = new HashMap<>();
	}

        @Override
	public TagPluginContext getParentContext() {
	    Node parent = node.getParent();
            if (!(parent instanceof Node.CustomTag)) {
		return null;
	    }
	    return ((Node.CustomTag) parent).getTagPluginContext();
	}

        @Override
	public void setPluginAttribute(String key, Object value) {
	    pluginAttributes.put(key, value);
	}

        @Override
	public Object getPluginAttribute(String key) {
	    return pluginAttributes.get(key);
	}

        @Override
	public boolean isScriptless() {
	    return node.getChildInfo().isScriptless();
	}

        @Override
	public boolean isConstantAttribute(String attribute) {
	    Node.JspAttribute attr = getNodeAttribute(attribute);
	    if (attr == null)
		return false;
	    return attr.isLiteral();
	}

        @Override
	public String getConstantAttribute(String attribute) {
	    Node.JspAttribute attr = getNodeAttribute(attribute);
            if (attr == null)
		return null;
	    return attr.getValue();
	}

        @Override
	public boolean isAttributeSpecified(String attribute) {
	    return getNodeAttribute(attribute) != null;
	}

        @Override
	public String getTemporaryVariableName() {
	    return node.getRoot().nextTemporaryVariableName();
	}

        @Override
	public void generateImport(String imp) {
	    pageInfo.addImport(imp);
	}

        @Override
	public void generateDeclaration(String id, String text) {
	    if (pageInfo.isPluginDeclared(id)) {
		return;
	    }
	    curNodes.add(new Node.Declaration(text, node.getStart(), null));
	}

        @Override
	public void generateJavaSource(String sourceCode) {
	    curNodes.add(new Node.Scriptlet(sourceCode, node.getStart(),
					    null));
	}

        @Override
	public void generateAttribute(String attributeName) {
	    curNodes.add(new Node.AttributeGenerator(node.getStart(),
						     attributeName,
						     node));
	}

        @Override
	public void dontUseTagPlugin() {
	    node.setUseTagPlugin(false);
	}

        @Override
	public void generateBody() {
            // Since we'll generate the body anyway, this is really a nop,
	    // except for the fact that it lets us put the Java sources the
	    // plugins produce in the correct order (w.r.t the body).
	    curNodes = node.getAtETag();
	}

    @Override
    public boolean isTagFile() {
        return pageInfo.isTagFile();
    }

	private Node.JspAttribute getNodeAttribute(String attribute) {
	    Node.JspAttribute[] attrs = node.getJspAttributes();
            for (int i = 0; attrs != null && i < attrs.length; i++) {
		if (attrs[i].getName().equals(attribute)) {
		    return attrs[i];
		}
	    }
	    return null;
	}
    }

}

