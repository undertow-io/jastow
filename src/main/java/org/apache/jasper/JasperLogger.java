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

package org.apache.jasper;

import static org.jboss.logging.Logger.Level.DEBUG;
import static org.jboss.logging.Logger.Level.ERROR;
import static org.jboss.logging.Logger.Level.WARN;

import java.io.File;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;


/**
 * Logging IDs 5000-6000
 * @author Remy Maucherat
 */
@MessageLogger(projectCode = "JBWEB")
public interface JasperLogger extends BasicLogger {

    /**
     * A logger with the category of the package name.
     */
    JasperLogger ROOT_LOGGER = Logger.getMessageLogger(JasperLogger.class, "org.apache.jasper");

    /**
     * A logger with the category of the package name.
     */
    JasperLogger COMPILER_LOGGER = Logger.getMessageLogger(JasperLogger.class, "org.apache.jasper.compiler");

    /**
     * A logger with the category of the package name.
     */
    JasperLogger SERVLET_LOGGER = Logger.getMessageLogger(JasperLogger.class, "org.apache.jasper.servlet");

    @LogMessage(level = WARN)
    @Message(id = 5000, value = "Invalid %s value for the initParam keepgenerated. Will use the default value of \"false\"")
    void invalidKeepGeneratedValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5001, value = "Invalid %s value for the initParam trimSpaces. Will use the default value of \"false\"")
    void invalidTrimSpacesValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5002, value = "Invalid %s value for the initParam enablePooling. Will use the default value of \"false\"")
    void invalidEnablePoolingValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5003, value = "Invalid %s value for the initParam mappedfile. Will use the default value of \"true\"")
    void invalidMappedFileValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5004, value = "Invalid %s value for the initParam sendErrToClient. Will use the default value of \"false\"")
    void invalidSendErrToClientValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5005, value = "Invalid %s value for the initParam classdebuginfo. Will use the default value of \"true\"")
    void invalidClassDebugInfoValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5006, value = "Invalid %s value for the initParam checkInterval. Will disable periodic checking")
    void invalidCheckIntervalValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5007, value = "Invalid %s value for the initParam modificationTestInterval. Will use the default value of \"4\" seconds")
    void invalidModificationTestIntervalValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5008, value = "Invalid %s value for the initParam recompileOnFail. Will use the default value of \"false\"")
    void invalidRecompileOnFailValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5009, value = "Invalid %s value for the initParam development. Will use the default value of \"true\"")
    void invalidDevelopmentValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5010, value = "Invalid %s value for the initParam suppressSmap. Will use the default value of \"false\"")
    void invalidSuppressSmapValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5011, value = "Invalid %s value for the initParam dumpSmap. Will use the default value of \"false\"")
    void invalidDumpSmapValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5012, value = "Invalid %s value for the initParam genStrAsCharArray. Will use the default value of \"false\"")
    void invalidGenStrAsCharArrayValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5013, value = "Invalid %s value for the initParam errorOnUseBeanInvalidClassAttribute. Will use the default value of \"true\"")
    void invalidErrorOnUseBeanInvalidClassAttributeValue(String value);

    @LogMessage(level = ERROR)
    @Message(id = 5014, value = "The JSP container needs a work directory")
    void missingWorkDirectory();

    @LogMessage(level = ERROR)
    @Message(id = 5015, value = "The JSP container needs a valid work directory [%s]")
    void missingWorkDirectory(String workDirectory);

    @LogMessage(level = WARN)
    @Message(id = 5016, value = "Invalid %s value for the initParam fork. Will use the default value of \"true\"")
    void invalidForkValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5017, value = "Invalid %s value for the initParam xpoweredBy. Will use the default value of \"true\"")
    void invalidXpoweredByValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5018, value = "Invalid %s value for the initParam displaySourceFragment. Will use the default value of \"true\"")
    void invalidDisplaySourceFragmentValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5019, value = "Failed loading Java compiler %s")
    void failedLoadingJavaCompiler(String className, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 5020, value = "Failed loading custom options class %s")
    void failedLoadingOptions(String className, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 5021, value = "File \"%s\" not found")
    void fileNotFound(String uri);

    @LogMessage(level = ERROR)
    @Message(id = 5022, value = "Error destroying JSP Servlet instance")
    void errorDestroyingServletInstance(@Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 5023, value = "Bad value %s in the url-pattern subelement in the webapp descriptor")
    void invalidJspPropertyGroupsUrlPattern(String value);

    @LogMessage(level = DEBUG)
    @Message(id = 5024, value = "Exception closing reader")
    void errorClosingReader(@Cause Throwable t);

    @LogMessage(level = DEBUG)
    @Message(id = 5025, value = "Parent class loader is: %s")
    void logParentClassLoader(String parentClassLoader);

    @LogMessage(level = DEBUG)
    @Message(id = 5026, value = "Compilation classpath: %s")
    void logCompilationClasspath(String classpath);

    @LogMessage(level = ERROR)
    @Message(id = 5027, value = "Error reading source file %s")
    void errorReadingSourceFile(String sourceFile, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 5028, value = "Error reading class file %s")
    void errorReadingClassFile(String className, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 5029, value = "Unknown source JVM %s ignored")
    void unknownSourceJvm(String version);

    @LogMessage(level = WARN)
    @Message(id = 5030, value = "Unknown target JVM %s ignored")
    void unknownTargetJvm(String version);

    @LogMessage(level = ERROR)
    @Message(id = 5031, value = "Error creating compiler report")
    void errorCreatingCompilerReport(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 5032, value = "Compiler error")
    void errorCompiling(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 5033, value = "Exception initializing page context")
    void errorInitializingPageContext(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 5034, value = "Error loading core class")
    void errorLoadingCoreClass(@Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 5035, value = "Invalid value '%s' for the initParam maxLoadedJsps. Will use the default value of '-1")
    void invalidMaxLoadedJsps(int maxLoadedJsps);

    @LogMessage(level = WARN)
    @Message(id = 5036, value = "Invalid value '%s' for the initParam jspIdleTimeout. Will use the default value of '-1'")
    void invalidJspIdleTimeout(int timeout);

    @LogMessage(level = WARN)
    @Message(id = 5037, value = "Failed to delete generated Java file '%s'")
    void failedToDeleteGeneratedFile(File file);

    @LogMessage(level = WARN)
    @Message(id = 5038, value = "Failed to delete generated class file(s)")
    void failedToDeleteGeneratedFiles(@Cause Exception e);

    @LogMessage(level = ERROR)
    @Message(id = 5039, value = "Invalid optimizeScriptlets value %s, must be true or false")
    void invalidOptimizeScriptletsValue(String optimiseScriptlets); 

    @LogMessage(level = WARN)
    @Message(id = 5040, value = "Invalid strictQuoteEscaping value %s, must be true or false")
    void invalidStrictQuoteEscaping(String strictQuoteEscaping);

    @LogMessage(level = WARN)
    @Message(id = 5041, value = "Invalid quoteAttributeEL value %s, must be true or false")
    void invalidQuoteAttributeEL(String quoteAttributeEL);
}
