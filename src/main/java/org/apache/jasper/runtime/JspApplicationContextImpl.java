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
package org.apache.jasper.runtime;

import static org.apache.jasper.JasperMessages.MESSAGES;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELContextEvent;
import javax.el.ELContextListener;
import javax.el.ELManager;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.servlet.ServletContext;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspContext;

import org.apache.jasper.Constants;
import org.apache.jasper.el.ELContextImpl;

/**
 * Implementation of JspApplicationContext
 *
 * @author Jacob Hookom
 */
public class JspApplicationContextImpl implements JspApplicationContext {

    private static final String KEY = JspApplicationContextImpl.class.getName();

    private final ExpressionFactory expressionFactory = ELManager.getExpressionFactory();

    private final List<ELContextListener> contextListeners = new ArrayList<>();

    private final CompositeELResolver resolvers = new CompositeELResolver();

	private boolean instantiated = false;

	public JspApplicationContextImpl() {

	}

    @Override
	public void addELContextListener(ELContextListener listener) {
		if (listener == null) {
			throw MESSAGES.nullElContextListener();
		}
		this.contextListeners.add(listener);
	}

	public static JspApplicationContextImpl getInstance(ServletContext context) {
		if (context == null) {
			throw MESSAGES.nullServletContext();
		}
		JspApplicationContextImpl impl = (JspApplicationContextImpl) context
				.getAttribute(KEY);
		if (impl == null) {
			impl = new JspApplicationContextImpl();
			context.setAttribute(KEY, impl);
		}
		return impl;
	}

	public ELContextImpl createELContext(JspContext context) {
		if (context == null) {
			throw MESSAGES.nullJspContext();
		}

		// create ELContext for JspContext
        ELContextImpl ctx;
        if (Constants.IS_SECURITY_ENABLED) {
            ctx = AccessController.doPrivileged(
                    new PrivilegedAction<ELContextImpl>() {
                        @Override
                        public ELContextImpl run() {
                            return new ELContextImpl(expressionFactory);
                        }
                    });
        } else {
            ctx = new ELContextImpl(expressionFactory);
        }
        ctx.addELResolver(resolvers); //register application resolvers
		ctx.putContext(JspContext.class, context);

		// alert all ELContextListeners
        fireListeners(ctx);
        this.instantiated = true;
        return ctx;
    }

    protected void fireListeners(ELContext elContext) {
        ELContextEvent event = new ELContextEvent(elContext);
		for (int i = 0; i < this.contextListeners.size(); i++) {
			this.contextListeners.get(i).contextCreated(event);
		}
	}

    @Override
	public void addELResolver(ELResolver resolver) throws IllegalStateException {
		if (resolver == null) {
			throw MESSAGES.nullElResolver();
		}
		if (this.instantiated) {
			throw MESSAGES.cannotAddElResolver();
		}
		this.resolvers.add(resolver);
	}

    @Override
	public ExpressionFactory getExpressionFactory() {
		return expressionFactory;
	}

}
