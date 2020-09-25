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
package org.apache.jasper.el;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.BeanNameELResolver;
import javax.el.BeanNameResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELManager;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ResourceBundleELResolver;
import javax.el.StandardELContext;
import javax.el.StaticFieldELResolver;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.servlet.jsp.el.ImplicitObjectELResolver;
import javax.servlet.jsp.el.ScopedAttributeELResolver;

import io.undertow.jsp.ImportedClassELResolver;
import org.apache.jasper.Constants;

/**
 * Implementation of ELContext
 *
 * @author Jacob Hookom
 */
public final class ELContextImpl extends ELContext {

    private static final FunctionMapper NullFunctionMapper = new FunctionMapper() {
        @Override
        public Method resolveFunction(String prefix, String localName) {
            return null;
        }
    };


    private static final class VariableMapperImpl extends VariableMapper {

        private Map<String, ValueExpression> vars;

        @Override
        public ValueExpression resolveVariable(String variable) {
            if (vars == null) {
                return null;
            }
            return vars.get(variable);
        }

        @Override
        public ValueExpression setVariable(String variable,
                                           ValueExpression expression) {
            if (vars == null) {
                vars = new HashMap<>();
            }
            if (expression == null) {
                return vars.remove(variable);
            } else {
                return vars.put(variable, expression);
            }
        }
    }
/*
    private static final CompositeELResolver DefaultResolver;

    static {
        if (Constants.IS_SECURITY_ENABLED) {
            DefaultResolver = null;
        } else {
            DefaultResolver = new CompositeELResolver();
            ((CompositeELResolver) DefaultResolver).add(ELManager.getExpressionFactory().getStreamELResolver());
            ((CompositeELResolver) DefaultResolver).add(new StaticFieldELResolver());
            ((CompositeELResolver) DefaultResolver).add(new MapELResolver());
            ((CompositeELResolver) DefaultResolver).add(new ResourceBundleELResolver());
            ((CompositeELResolver) DefaultResolver).add(new ListELResolver());
            ((CompositeELResolver) DefaultResolver).add(new ArrayELResolver());
            ((CompositeELResolver) DefaultResolver).add(new BeanELResolver());
        }
    }*/

    //private final ELResolver resolver;

    //private FunctionMapper functionMapper = new DefaultFunctionMapper();

    /*private VariableMapper variableMapper;
*/
    /*public ELContextImpl(ExpressionFactory factory) {
        super(factory);



    }

    public ELContextImpl(ELResolver resolver){
        super(ELManager.getExpressionFactory());
        //addELResolver(resolver);
        addELResolver(new ImplicitObjectELResolver());
        addELResolver(new ScopedAttributeELResolver());
        //this.resolver = resolver;
    }*/

    /*
         * The ELResolver for this ELContext.
         */
    private ELResolver elResolver;

    /*
     * The list of the custom ELResolvers added to the ELResolvers.
     * An ELResolver is added to the list when addELResolver is called.
     */
    private CompositeELResolver customResolvers;

    /*
     * The ELResolver implementing the query operators.
     */
    private ELResolver streamELResolver;

    /*
     * The FunctionMapper for this ELContext.
     */
    private FunctionMapper functionMapper;

    /*
     * The pre-confured init function map;
     */
    private Map<String, Method> initFunctionMap;

    /*
     * The VariableMapper for this ELContext.
     */
    private VariableMapper variableMapper;

    /*
     * If non-null, indicates the presence of a delegate ELContext.
     * When a Standard is constructed from another ELContext, there is no
     * easy way to get its private context map, therefore delegation is needed.
     */
    private ELContext delegate = null;

    /**
     * A bean repository local to this context
     */
    private Map<String, Object> beans = new HashMap<String, Object>();

    /**
     * Construct a default ELContext for a stand-alone environment.
     *
     * @param factory The ExpressionFactory
     */
    public ELContextImpl(ExpressionFactory factory) {
        this.streamELResolver = factory.getStreamELResolver();
        initFunctionMap = factory.getInitFunctionMap();
    }

    /**
     * Construct a StandardELContext from another ELContext.
     *
     * @param context The ELContext that acts as a delegate in most cases
     */
    public ELContextImpl(ELContext context) {
        this.delegate = context;
        // Copy all attributes except map and resolved
        CompositeELResolver elr = new CompositeELResolver();
        elr.add(new BeanNameELResolver(new LocalBeanNameResolver()));
        customResolvers = new CompositeELResolver();
        elr.add(customResolvers);
        elr.add(context.getELResolver());
        elResolver = elr;

        functionMapper = context.getFunctionMapper();
        variableMapper = context.getVariableMapper();
        setLocale(context.getLocale());
    }
    @Deprecated
    public ELContextImpl(ELResolver context,ExpressionFactory factory) {
        this(factory);
        getELResolver();
        addELResolver(context);
    }

    @Override
    public void putContext(Class key, Object contextObject) {
        if (delegate != null) {
            delegate.putContext(key, contextObject);
        } else {
            super.putContext(key, contextObject);
        }
    }

    @Override
    public Object getContext(Class key) {
        if (delegate != null) {
            return delegate.getContext(key);
        } else {
            return super.getContext(key);
        }
    }

    /**
     * Construct (if needed) and return a default ELResolver.
     * <p>Retrieves the <code>ELResolver</code> associated with this context.
     * This is a <code>CompositeELResover</code> consists of an ordered list of
     * <code>ELResolver</code>s.
     * <ol>
     * <li>A {@link BeanNameELResolver} for beans defined locally</li>
     * <li>Any custom <code>ELResolver</code>s</li>
     * <li>An <code>ELResolver</code> supporting the collection operations</li>
     * <li>A {@link StaticFieldELResolver} for resolving static fields</li>
     * <li>A {@link MapELResolver} for resolving Map properties</li>
     * <li>A {@link ResourceBundleELResolver} for resolving ResourceBundle properties</li>
     * <li>A {@link ListELResolver} for resolving List properties</li>
     * <li>An {@link ArrayELResolver} for resolving array properties</li>
     * <li>A {@link BeanELResolver} for resolving bean properties</li>
     * </ol>
     *
     * @return The ELResolver for this context.
     */
    @Override
    public ELResolver getELResolver() {
        if (elResolver == null) {
            CompositeELResolver resolver = new CompositeELResolver();
            customResolvers = new CompositeELResolver();
            resolver.add(new ImplicitObjectELResolver());
            resolver.add(customResolvers);
            resolver.add(new BeanNameELResolver(new LocalBeanNameResolver()));
            if (streamELResolver != null) {
                resolver.add(streamELResolver);
            }

            resolver.add(new StaticFieldELResolver());
            resolver.add(new ImportedClassELResolver());//to make static fields & methods work
            resolver.add(new MapELResolver());
            resolver.add(new ResourceBundleELResolver());
            resolver.add(new ListELResolver());
            resolver.add(new ArrayELResolver());
            resolver.add(new BeanELResolver());


            resolver.add(new ScopedAttributeELResolver());
            elResolver = resolver;
        }
        return elResolver;
    }

    /**
     * Add a custom ELResolver to the context.  The list of the custom
     * ELResolvers will be accessed in the order they are added.
     * A custom ELResolver added to the context cannot be removed.
     *
     * @param cELResolver The new ELResolver to be added to the context
     */
    public void addELResolver(ELResolver cELResolver) {
        getELResolver();  // make sure elResolver is constructed
        customResolvers.add(cELResolver);
    }

    /**
     * Get the local bean repository
     *
     * @return the bean repository
     */
    Map<String, Object> getBeans() {
        return beans;
    }

    /**
     * Construct (if needed) and return a default FunctionMapper.
     *
     * @return The default FunctionMapper
     */
    @Override
    public FunctionMapper getFunctionMapper() {
        if (functionMapper == null) {
            functionMapper = new DefaultFunctionMapper(initFunctionMap);
        }
        return functionMapper;
    }

    public void setFunctionMapper(FunctionMapper functionMapper) {
        this.functionMapper = functionMapper;
    }

    /**
     * Construct (if needed) and return a default VariableMapper() {
     *
     * @return The default Variable
     */
    @Override
    public VariableMapper getVariableMapper() {
        if (variableMapper == null) {
            variableMapper = new DefaultVariableMapper();
        }
        return variableMapper;
    }

    private static class DefaultFunctionMapper extends FunctionMapper {

        private Map<String, Method> functions = null;

        DefaultFunctionMapper(Map<String, Method> initMap) {
            functions = (initMap == null) ?
                    new HashMap<String, Method>() :
                    new HashMap<String, Method>(initMap);
        }

        @Override
        public Method resolveFunction(String prefix, String localName) {
            return functions.get(prefix + ":" + localName);
        }


        @Override
        public void mapFunction(String prefix, String localName, Method meth) {
            functions.put(prefix + ":" + localName, meth);
        }
    }

    private static class DefaultVariableMapper extends VariableMapper {

        private Map<String, ValueExpression> variables = null;

        @Override
        public ValueExpression resolveVariable(String variable) {
            if (variables == null) {
                return null;
            }
            return variables.get(variable);
        }

        @Override
        public ValueExpression setVariable(String variable,
                                           ValueExpression expression) {
            if (variables == null) {
                variables = new HashMap<String, ValueExpression>();
            }
            ValueExpression prev = null;
            if (expression == null) {
                prev = variables.remove(variable);
            } else {
                prev = variables.put(variable, expression);
            }
            return prev;
        }
    }

    private class LocalBeanNameResolver extends BeanNameResolver {

        @Override
        public boolean isNameResolved(String beanName) {
            return beans.containsKey(beanName);
        }

        @Override
        public Object getBean(String beanName) {
            return beans.get(beanName);
        }

        @Override
        public void setBeanValue(String beanName, Object value) {
            beans.put(beanName, value);
        }

        @Override
        public boolean isReadOnly(String beanName) {
            return false;
        }

        @Override
        public boolean canCreateBean(String beanName) {
            return true;
        }
    }
}
