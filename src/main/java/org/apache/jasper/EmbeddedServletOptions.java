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

package org.apache.jasper;

import java.io.File;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.jasper.compiler.JspConfig;
import org.apache.jasper.compiler.TagPluginManager;

/**
 * A class to hold all init parameters specific to the JSP engine. 
 *
 * @author Anil K. Vijendran
 * @author Hans Bergsten
 * @author Pierre Delisle
 */
public final class EmbeddedServletOptions implements Options {
    
    private Properties settings = new Properties();
    
    /**
     * Is Jasper being used in development mode?
     */
    private boolean development = true;
    
    /**
     * Should Ant fork its java compiles of JSP pages.
     */
    public boolean fork = true;
    
    /**
     * Do you want to keep the generated Java files around?
     */
    private boolean keepGenerated = true;
    
    /**
     * Should white spaces between directives or actions be trimmed?
     */
    private boolean trimSpaces = false;
    
    /**
     * Determines whether tag handler pooling is enabled.
     */
    private boolean isPoolingEnabled = true;
    
    /**
     * Do you want support for "mapped" files? This will generate
     * servlet that has a print statement per line of the JSP file.
     * This seems like a really nice feature to have for debugging.
     */
    private boolean mappedFile = true;
    
    /**
     * Do you want stack traces and such displayed in the client's
     * browser? If this is false, such messages go to the standard
     * error or a log file if the standard error is redirected. 
     */
    private boolean sendErrorToClient = false;
    
    /**
     * Do we want to include debugging information in the class file?
     */
    private boolean classDebugInfo = true;
    
    /**
     * Background compile thread check interval in seconds.
     */
    private int checkInterval = 0;
    
    /**
     * Is the generation of SMAP info for JSR45 debuggin suppressed?
     */
    private boolean isSmapSuppressed = false;
    
    /**
     * Should SMAP info for JSR45 debugging be dumped to a file?
     */
    private boolean isSmapDumped = false;
    
    /**
     * Are Text strings to be generated as char arrays?
     */
    private boolean genStringAsCharArray = false;
    
    private boolean errorOnUseBeanInvalidClassAttribute = true;
    
    /**
     * I want to see my generated servlets. Which directory are they
     * in?
     */
    private File scratchDir;
    
    /**
     * Need to have this as is for versions 4 and 5 of IE. Can be set from
     * the initParams so if it changes in the future all that is needed is
     * to have a jsp initParam of type ieClassId="<value>"
     */
    private String ieClassId = "clsid:8AD9C840-044E-11D1-B3E9-00805F499D93";
    
    /**
     * What classpath should I use while compiling generated servlets?
     */
    private String classpath = null;
    
    /**
     * Compiler to use.
     */
    private String compiler = null;
    
    /**
     * Compiler target VM.
     */
    private String compilerTargetVM = "1.5";
    
    /**
     * The compiler source VM.
     */
    private String compilerSourceVM = "1.5";
    
    /**
     * The compiler class name.
     */
    private String compilerClassName = null;
    
    /**
     * Jsp config information
     */
    private JspConfig jspConfig = null;
    
    /**
     * TagPluginManager
     */
    private TagPluginManager tagPluginManager = null;
    
    /**
     * Java platform encoding to generate the JSP
     * page servlet.
     */
    private String javaEncoding = "UTF8";
    
    /**
     * Modification test interval.
     */
    private int modificationTestInterval = 4;
    
    /**
     * Is re-compilation attempted immediately after a failure?
     */
    private boolean recompileOnFail = false;

    /**
     * Is generation of X-Powered-By response header enabled/disabled?
     */
    private boolean xpoweredBy;
    
    /**
     * Should we include a source fragment in exception messages, which could be displayed
     * to the developer ?
     */
    private boolean displaySourceFragment = true;

    
    public String getProperty(String name ) {
        return settings.getProperty( name );
    }
    
    public void setProperty(String name, String value ) {
        if (name != null && value != null){ 
            settings.setProperty( name, value );
        }
    }
    
    /**
     * Are we keeping generated code around?
     */
    public boolean getKeepGenerated() {
        return keepGenerated;
    }
    
    /**
     * Should white spaces between directives or actions be trimmed?
     */
    public boolean getTrimSpaces() {
        return trimSpaces;
    }
    
    public boolean isPoolingEnabled() {
        return isPoolingEnabled;
    }
    
    /**
     * Are we supporting HTML mapped servlets?
     */
    public boolean getMappedFile() {
        return mappedFile;
    }
    
    /**
     * Should errors be sent to client or thrown into stderr?
     */
    public boolean getSendErrorToClient() {
        return sendErrorToClient;
    }
    
    /**
     * Should class files be compiled with debug information?
     */
    public boolean getClassDebugInfo() {
        return classDebugInfo;
    }
    
    /**
     * Background JSP compile thread check intervall
     */
    public int getCheckInterval() {
        return checkInterval;
    }
    
    /**
     * Modification test interval.
     */
    public int getModificationTestInterval() {
        return modificationTestInterval;
    }
    
    /**
     * Re-compile on failure.
     */
    public boolean getRecompileOnFail() {
        return recompileOnFail;
    }

    /**
     * Is Jasper being used in development mode?
     */
    public boolean getDevelopment() {
        return development;
    }
    
    /**
     * Is the generation of SMAP info for JSR45 debuggin suppressed?
     */
    public boolean isSmapSuppressed() {
        return isSmapSuppressed;
    }
    
    /**
     * Should SMAP info for JSR45 debugging be dumped to a file?
     */
    public boolean isSmapDumped() {
        return isSmapDumped;
    }
    
    /**
     * Are Text strings to be generated as char arrays?
     */
    public boolean genStringAsCharArray() {
        return this.genStringAsCharArray;
    }
    
    /**
     * Class ID for use in the plugin tag when the browser is IE. 
     */
    public String getIeClassId() {
        return ieClassId;
    }
    
    /**
     * What is my scratch dir?
     */
    public File getScratchDir() {
        return scratchDir;
    }
    
    /**
     * What classpath should I use while compiling the servlets
     * generated from JSP files?
     */
    public String getClassPath() {
        return classpath;
    }
    
    /**
     * Is generation of X-Powered-By response header enabled/disabled?
     */
    public boolean isXpoweredBy() {
        return xpoweredBy;
    }
    
    /**
     * Compiler to use.
     */
    public String getCompiler() {
        return compiler;
    }
    
    /**
     * @see Options#getCompilerTargetVM
     */
    public String getCompilerTargetVM() {
        return compilerTargetVM;
    }
    
    /**
     * @see Options#getCompilerSourceVM
     */
    public String getCompilerSourceVM() {
        return compilerSourceVM;
    }
    
    /**
     * Java compiler class to use.
     */
    public String getCompilerClassName() {
        return compilerClassName;
    }

    public boolean getErrorOnUseBeanInvalidClassAttribute() {
        return errorOnUseBeanInvalidClassAttribute;
    }
    
    public void setErrorOnUseBeanInvalidClassAttribute(boolean b) {
        errorOnUseBeanInvalidClassAttribute = b;
    }
    
    public String getJavaEncoding() {
        return javaEncoding;
    }
    
    public boolean getFork() {
        return fork;
    }
    
    public JspConfig getJspConfig() {
        return jspConfig;
    }
    
    public TagPluginManager getTagPluginManager() {
        return tagPluginManager;
    }
    
    public boolean isCaching() {
        return false;
    }
    
    public Map getCache() {
        return null;
    }

    /**
     * Should we include a source fragment in exception messages, which could be displayed
     * to the developer ?
     */
    public boolean getDisplaySourceFragment() {
        return displaySourceFragment;
    }

    /**
     * Create an EmbeddedServletOptions object using data available from
     * ServletConfig and ServletContext. 
     */
    public EmbeddedServletOptions(ServletConfig config,
            ServletContext context) {
        
        // JVM version numbers
        try {
            if (Float.parseFloat(System.getProperty("java.specification.version")) > 1.4) {
                compilerSourceVM = compilerTargetVM = "1.5";
            } else {
                compilerSourceVM = compilerTargetVM = "1.4";
            }
        } catch (NumberFormatException e) {
            // Ignore
        }
        
        Enumeration enumeration=config.getInitParameterNames();
        while( enumeration.hasMoreElements() ) {
            String k=(String)enumeration.nextElement();
            String v=config.getInitParameter( k );
            setProperty( k, v);
        }
        
        String keepgen = config.getInitParameter("keepgenerated");
        if (keepgen != null) {
            if (keepgen.equalsIgnoreCase("true")) {
                this.keepGenerated = true;
            } else if (keepgen.equalsIgnoreCase("false")) {
                this.keepGenerated = false;
            } else {
                JasperLogger.ROOT_LOGGER.invalidKeepGeneratedValue(keepgen);
            }
        }
        
        
        String trimsp = config.getInitParameter("trimSpaces"); 
        if (trimsp != null) {
            if (trimsp.equalsIgnoreCase("true")) {
                trimSpaces = true;
            } else if (trimsp.equalsIgnoreCase("false")) {
                trimSpaces = false;
            } else {
                JasperLogger.ROOT_LOGGER.invalidTrimSpacesValue(trimsp);
            }
        }
        
        this.isPoolingEnabled = true;
        String poolingEnabledParam
        = config.getInitParameter("enablePooling"); 
        if (poolingEnabledParam != null
                && !poolingEnabledParam.equalsIgnoreCase("true")) {
            if (poolingEnabledParam.equalsIgnoreCase("false")) {
                this.isPoolingEnabled = false;
            } else {
                JasperLogger.ROOT_LOGGER.invalidEnablePoolingValue(poolingEnabledParam);
            }
        }
        
        String mapFile = config.getInitParameter("mappedfile"); 
        if (mapFile != null) {
            if (mapFile.equalsIgnoreCase("true")) {
                this.mappedFile = true;
            } else if (mapFile.equalsIgnoreCase("false")) {
                this.mappedFile = false;
            } else {
                JasperLogger.ROOT_LOGGER.invalidMappedFileValue(mapFile);
            }
        }
        
        String senderr = config.getInitParameter("sendErrToClient");
        if (senderr != null) {
            if (senderr.equalsIgnoreCase("true")) {
                this.sendErrorToClient = true;
            } else if (senderr.equalsIgnoreCase("false")) {
                this.sendErrorToClient = false;
            } else {
                JasperLogger.ROOT_LOGGER.invalidSendErrToClientValue(senderr);
            }
        }
        
        String debugInfo = config.getInitParameter("classdebuginfo");
        if (debugInfo != null) {
            if (debugInfo.equalsIgnoreCase("true")) {
                this.classDebugInfo  = true;
            } else if (debugInfo.equalsIgnoreCase("false")) {
                this.classDebugInfo  = false;
            } else {
                JasperLogger.ROOT_LOGGER.invalidClassDebugInfoValue(debugInfo);
            }
        }
        
        String checkInterval = config.getInitParameter("checkInterval");
        if (checkInterval != null) {
            try {
                this.checkInterval = Integer.parseInt(checkInterval);
            } catch(NumberFormatException ex) {
                JasperLogger.ROOT_LOGGER.invalidCheckIntervalValue(checkInterval);
            }
        }
        
        String modificationTestInterval = config.getInitParameter("modificationTestInterval");
        if (modificationTestInterval != null) {
            try {
                this.modificationTestInterval = Integer.parseInt(modificationTestInterval);
            } catch(NumberFormatException ex) {
                JasperLogger.ROOT_LOGGER.invalidModificationTestIntervalValue(modificationTestInterval);
            }
        }
        
        String recompileOnFail = config.getInitParameter("recompileOnFail"); 
        if (recompileOnFail != null) {
            if (recompileOnFail.equalsIgnoreCase("true")) {
                this.recompileOnFail = true;
            } else if (recompileOnFail.equalsIgnoreCase("false")) {
                this.recompileOnFail = false;
            } else {
                JasperLogger.ROOT_LOGGER.invalidRecompileOnFailValue(recompileOnFail);
            }
        }
        String development = config.getInitParameter("development");
        if (development != null) {
            if (development.equalsIgnoreCase("true")) {
                this.development = true;
            } else if (development.equalsIgnoreCase("false")) {
                this.development = false;
            } else {
                JasperLogger.ROOT_LOGGER.invalidDevelopmentValue(development);
            }
        }
        
        String suppressSmap = config.getInitParameter("suppressSmap");
        if (suppressSmap != null) {
            if (suppressSmap.equalsIgnoreCase("true")) {
                isSmapSuppressed = true;
            } else if (suppressSmap.equalsIgnoreCase("false")) {
                isSmapSuppressed = false;
            } else {
                JasperLogger.ROOT_LOGGER.invalidSuppressSmapValue(suppressSmap);
            }
        }
        
        String dumpSmap = config.getInitParameter("dumpSmap");
        if (dumpSmap != null) {
            if (dumpSmap.equalsIgnoreCase("true")) {
                isSmapDumped = true;
            } else if (dumpSmap.equalsIgnoreCase("false")) {
                isSmapDumped = false;
            } else {
                JasperLogger.ROOT_LOGGER.invalidDumpSmapValue(dumpSmap);
            }
        }
        
        String genCharArray = config.getInitParameter("genStrAsCharArray");
        if (genCharArray != null) {
            if (genCharArray.equalsIgnoreCase("true")) {
                genStringAsCharArray = true;
            } else if (genCharArray.equalsIgnoreCase("false")) {
                genStringAsCharArray = false;
            } else {
                JasperLogger.ROOT_LOGGER.invalidGenStrAsCharArrayValue(genCharArray);
            }
        }
        
        String errBeanClass =
            config.getInitParameter("errorOnUseBeanInvalidClassAttribute");
        if (errBeanClass != null) {
            if (errBeanClass.equalsIgnoreCase("true")) {
                errorOnUseBeanInvalidClassAttribute = true;
            } else if (errBeanClass.equalsIgnoreCase("false")) {
                errorOnUseBeanInvalidClassAttribute = false;
            } else {
                JasperLogger.ROOT_LOGGER.invalidErrorOnUseBeanInvalidClassAttributeValue(errBeanClass);
            }
        }
        
        String ieClassId = config.getInitParameter("ieClassId");
        if (ieClassId != null)
            this.ieClassId = ieClassId;
        
        String classpath = config.getInitParameter("classpath");
        if (classpath != null)
            this.classpath = classpath;
        
        /*
         * scratchdir
         */
        String dir = config.getInitParameter("scratchdir"); 
        if (dir != null) {
            scratchDir = new File(dir);
        } else {
            // First try the Servlet 2.2 javax.servlet.context.tempdir property
            scratchDir = (File) context.getAttribute(ServletContext.TEMPDIR);
            if (scratchDir == null) {
                // Not running in a Servlet 2.2 container.
                // Try to get the JDK 1.2 java.io.tmpdir property
                dir = System.getProperty("java.io.tmpdir");
                if (dir != null)
                    scratchDir = new File(dir);
            }
        }      
        if (this.scratchDir == null) {
            JasperLogger.ROOT_LOGGER.missingWorkDirectory();
            return;
        }
        
        if (!(scratchDir.exists() && scratchDir.canRead() &&
                scratchDir.canWrite() && scratchDir.isDirectory()))
            JasperLogger.ROOT_LOGGER.missingWorkDirectory(scratchDir.getAbsolutePath());
        
        this.compiler = config.getInitParameter("compiler");
        
        String compilerTargetVM = config.getInitParameter("compilerTargetVM");
        if(compilerTargetVM != null) {
            this.compilerTargetVM = compilerTargetVM;
        }
        
        String compilerSourceVM = config.getInitParameter("compilerSourceVM");
        if(compilerSourceVM != null) {
            this.compilerSourceVM = compilerSourceVM;
        }
        
        String javaEncoding = config.getInitParameter("javaEncoding");
        if (javaEncoding != null) {
            this.javaEncoding = javaEncoding;
        }
        
        String compilerClassName = config.getInitParameter("compilerClassName");
        if (compilerClassName != null) {
            this.compilerClassName = compilerClassName;
        }
        
        String fork = config.getInitParameter("fork");
        if (fork != null) {
            if (fork.equalsIgnoreCase("true")) {
                this.fork = true;
            } else if (fork.equalsIgnoreCase("false")) {
                this.fork = false;
            } else {
                JasperLogger.ROOT_LOGGER.invalidForkValue(fork);
            }
        }
        
        String xpoweredBy = config.getInitParameter("xpoweredBy"); 
        if (xpoweredBy != null) {
            if (xpoweredBy.equalsIgnoreCase("true")) {
                this.xpoweredBy = true;
            } else if (xpoweredBy.equalsIgnoreCase("false")) {
                this.xpoweredBy = false;
            } else {
                JasperLogger.ROOT_LOGGER.invalidXpoweredByValue(xpoweredBy);
            }
        }
        
        String displaySourceFragment = config.getInitParameter("displaySourceFragment"); 
        if (displaySourceFragment != null) {
            if (displaySourceFragment.equalsIgnoreCase("true")) {
                this.displaySourceFragment = true;
            } else if (displaySourceFragment.equalsIgnoreCase("false")) {
                this.displaySourceFragment = false;
            } else {
                JasperLogger.ROOT_LOGGER.invalidDisplaySourceFragmentValue(displaySourceFragment);
            }
        }
        
        // Setup the jsp config info for this web app.
        jspConfig = new JspConfig(context);
        
        // Create a Tag plugin instance
        tagPluginManager = new TagPluginManager(context);
    }
    
}

