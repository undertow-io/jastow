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

import java.io.IOException;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

/**
 * Logging IDs 4000-5000
 * @author Remy Maucherat
 */
@MessageBundle(projectCode = "JBWEB")
public interface JasperMessages {

    /**
     * The messages
     */
    JasperMessages MESSAGES = Messages.getBundle(JasperMessages.class);

    @Message(id = 4000, value = "No Java compiler available")
    IllegalStateException noJavaCompiler();

    @Message(id = 4001, value = "Unable to compile class for JSP")
    String failedClassCompilation();

    @Message(id = 4002, value = "Unable to load class for JSP")
    String failedClassLoading();

    @Message(id = 4003, value = "No output folder")
    IllegalStateException noOutputFolder();

    @Message(id = 4004, value = "No output folder")
    IllegalStateException badOutputFolderUrl(@Cause Throwable t);

    @Message(id = 4005, value = "Byte '%s' not 7-bit ASCII")
    IOException invalidByteRead(int b);

    @Message(id = 4006, value = "Mark is not supported by the UTF-8 reader")
    IOException markNotSupportedInUtf8Reader();

    @Message(id = 4007, value = "Expected byte %s of %s-byte UTF-8 sequence")
    String errorUtf8ExpectedByte(int position, int count);

    @Message(id = 4008, value = "Invalid byte %s of %s-byte UTF-8 sequence")
    String errorUtf8InvalidByte(int position, int count);

    @Message(id = 4009, value = "High surrogate bits in UTF-8 sequence must not exceed 0x10 but found 0x%s")
    String errorUtf8InvalidHighSurrogate(String data);

    @Message(id = 4010, value = "Given byte order for encoding \"%s\" is not supported")
    String unsupportedByteOrderForEncoding(String encoding);

    @Message(id = 4011, value = "Invalid encoding name \"%s\"")
    String invalidEncodingDeclared(String encoding);

    @Message(id = 4012, value = "XML version \"%s\" is not supported, only XML 1.0 is supported")
    String unsupportedXmlVersion(String version);

    @Message(id = 4013, value = "The version is required in the XML declaration")
    String noXmlVersion();

    @Message(id = 4014, value = "White space is required before the version pseudo attribute in the text declaration")
    String requiredSpaceBeforeVersionInTextDeclaration();

    @Message(id = 4015, value = "White space is required before the version pseudo attribute in the XML declaration")
    String requiredSpaceBeforeVersionInXmlDeclaration();

    @Message(id = 4016, value = "White space is required before the encoding pseudo attribute in the text declaration")
    String requiredSpaceBeforeEncodingInTextDeclaration();

    @Message(id = 4017, value = "White space is required before the encoding pseudo attribute in the XML declaration")
    String requiredSpaceBeforeEncodingInXmlDeclaration();

    @Message(id = 4018, value = "Encoding is required in the text declaration")
    String requiredEncodingDeclaration();

    @Message(id = 4019, value = "Version is required in the XML declaration")
    String requiredVersionDeclaration();

    @Message(id = 4020, value = "White space is required before the standalone pseudo attribute in the XML declaration")
    String requiredSpaceBeforeStandaloneInXmlDeclaration();

    @Message(id = 4021, value = "The standalone document declaration value must be \"yes\" or \"no\", not \"%s\"")
    String invalidStandaloneDeclaration(String value);

    @Message(id = 4022, value = "No more pseudo attributes is allowed")
    String invalidPseudoAttribute();

    @Message(id = 4023, value = "More pseudo attributes are expected")
    String missingPseudoAttribute();

    @Message(id = 4024, value = "The XML declaration must end with \"?&gt;\"")
    String malformedXmlDeclaration();

    @Message(id = 4025, value = "A pseudo attribute name is expected")
    String missingPseudoAttributeName();

    @Message(id = 4026, value = "The '=' character must follow \"%s\" in the text declaration")
    String missingEqualsInTextDeclaration(String name);

    @Message(id = 4027, value = "The '=' character must follow \"%s\" in the XML declaration")
    String missingEqualsInXmlDeclaration(String name);

    @Message(id = 4028, value = "The value following \"%s\" in the text declaration must be a quoted string")
    String missingQuoteInTextDeclaration(String name);

    @Message(id = 4029, value = "The value following \"%s\" in the XML declaration must be a quoted string")
    String missingQuoteInXmlDeclaration(String name);

    @Message(id = 4030, value = "An invalid XML character (Unicode: 0x%s) was found in the text declaration")
    String invalidCharInTextDeclaration(String name);

    @Message(id = 4031, value = "An invalid XML character (Unicode: 0x%s) was found in the XML declaration")
    String invalidCharInXmlDeclaration(String name);

    @Message(id = 4032, value = "Closing quote in the value following \"%s\" in the text declaration is missing")
    String missingClosingQuoteInTextDeclaration(String name);

    @Message(id = 4033, value = "Closing quote in the value following \"%s\" in the XML declaration is missing")
    String missingClosingQuoteInXmlDeclaration(String name);

    @Message(id = 4034, value = "An invalid XML character (Unicode: 0x%s) was found in the processing instruction")
    String invalidCharInProcessingInstruction(String character);

    @Message(id = 4035, value = "An invalid XML character (Unicode: 0x%s) was found in the element content of the document")
    String invalidCharInContent(String character);

    @Message(id = 4036, value = "File \"%s\" not found")
    String fileNotFound(String uri);

    @Message(id = 4037, value = "JSP has been marked unavailable")
    String unavailable();

    @Message(id = 4038, value = "An exception occurred processing JSP page %s at line %s\n\n%s\n\nStacktrace:")
    String jspExceptionWithDetails(String jsp, int line, String extract);

    @Message(id = 4039, value = "Jasper JSP 2.2 Engine")
    String jspInfo();

    @Message(id = 4040, value = "Null attribute name")
    NullPointerException nullAttributeName();

    @Message(id = 4041, value = "Cannot set indexed property")
    String failedSettingBeanIndexedProperty();

    @Message(id = 4042, value = "Cannot find any information on property '%s' in a bean of type '%s'")
    String cannotFindBeanProperty(String property, String beanClass);

    @Message(id = 4043, value = "Can't find a method to write property '%s' of type '%s' in a bean of type '%s'")
    String cannotSetBeanProperty(String property, String propertyClass, String beanClass);

    @Message(id = 4044, value = "Attempted a bean operation on a null object")
    String nullBean();

    @Message(id = 4045, value = "No BeanInfo for the bean of type '%s' could be found, the class likely does not exist")
    String cannotFindBeanInfo(String beanClass);

    @Message(id = 4046, value = "Cannot find a method to read property '%s' in a bean of type '%s'")
    String cannotGetBeanProperty(String property, String beanClass);

    @Message(id = 4047, value = "Unable to convert string \"%s\" to class \"%s\" for attribute \"%s\": %s")
    String errorConvertingBeanProperty(String value, String valueClass, String property, String errorMessage);

    @Message(id = 4048, value = "Property Editor not registered with the PropertyEditorManager")
    IllegalArgumentException noRegisteredPropertyEditor();

    @Message(id = 4049, value = "Illegal to clear() when buffer size == 0")
    IllegalStateException cannotClearWithNoBuffer();

    @Message(id = 4050, value = "Attempt to clear a buffer that's already been flushed")
    IOException cannotClearAfterFlush();

    @Message(id = 4051, value = "JSP Buffer overflow")
    IOException bufferOverflow();

    @Message(id = 4052, value = "Exception occurred when flushing data")
    IllegalStateException errorFlushingData(@Cause Throwable t);

    @Message(id = 4053, value = "Attempt to clear a buffer that's already been flushed")
    IllegalStateException illegalClearAfterFlush(@Cause Throwable t);

    @Message(id = 4054, value = "Cannot access session scope in page that does not participate in any session")
    IllegalStateException cannotUseSessionScope();

    @Message(id = 4055, value = "Invalid scope specified")
    IllegalArgumentException invalidScope();

    @Message(id = 4056, value = "Attribute value %s is quoted with %s which must be escaped when used within the value")
    IllegalArgumentException missingEscaping(String value, String quote);

    @Message(id = 4057, value = "Bad scope specified for useBean")
    String badScopeForUseBean();

    @Message(id = 4058, value = "Malformed library version number")
    String malformedLibraryVersionNumber();

    @Message(id = 4059, value = "Default java encoding %s is invalid on your java platform. An alternate can be specified via the 'javaEncoding' parameter of JspServlet")
    String needAlternateEncoding(String encoding);

    @Message(id = 4060, value = "An error occurred at line: %s in the jsp file: %s")
    String errorInJspFile(int line, String fileName);

    @Message(id = 4061, value = "An error occurred at line: %s in the generated java file")
    String errorInJavaFile(int line);

    @Message(id = 4062, value = "Unable to compile class for JSP: %s")
    String failedClassCompilation(String errorReport);

    @Message(id = 4063, value = "The value for the useBean class attribute %s is invalid")
    String invalidUseBeanAttributeClass(String className);

    @Message(id = 4064, value = "Unable to find setter method for attribute: %s")
    String cannotFindSetterMethod(String attributeName);

    @Message(id = 4065, value = "Error introspecting tag handler: %s")
    String errorIntrospectingTagHandler(String tagHandlerClass);

    @Message(id = 4066, value = "Tag file directory %s does not start with \"/WEB-INF/tags\"")
    String invalidTagFileDirectory(String tagFileDirectory);

    @Message(id = 4067, value = "Invalid implicit TLD for tag file at %s")
    String invalidImplicitTld(String tagFile);

    @Message(id = 4068, value = "Invalid JSP version defined in implicit TLD for tag file at %s")
    String invalidImplicitTldVersion(String tagFile);

    @Message(id = 4069, value = "Unable to display JSP extract. Probably due to a JRE bug (see Tomcat bug 48498 for details).")
    String errorDisplayingJspExtract();

    @Message(id = 4070, value = "Error reading file \"%s\"")
    String errorReadingFile(String file);

    @Message(id = 4071, value = "Error parsing file \"%s\"")
    String errorParsingFile(String file);

    @Message(id = 4072, value = "&lt;jsp:text&gt; must not have any subelements")
    String invalidJspTextSubelements();

    @Message(id = 4073, value = "Unterminated %s tag")
    String unterminatedTag(String tag);

    @Message(id = 4074, value = "According to TLD, tag %s must be empty, but is not")
    String invalidEmptyTagSubelements(String tag);

    @Message(id = 4075, value = "Could not add one or more tag libraries: %s")
    String errorAddingTagLibraries(String errorMessage);

    @Message(id = 4076, value = "Nested &lt;jsp:root&gt;")
    String nestedJspRoot();

    @Message(id = 4077, value = "%s directive cannot be used in a tag file")
    String invalidDirectiveInTagFile(String directive);

    @Message(id = 4078, value = "Scripting elements are disallowed here")
    String invalidScriptingElement();

    @Message(id = 4079, value = "%s action cannot be used in a tag file")
    String invalidActionInTagFile(String action);

    @Message(id = 4080, value = "Invalid standard action: %s")
    String invalidStandardAction(String action);

    @Message(id = 4081, value = "No tag \"%s\" defined in tag library associated with uri \"%s\"")
    String unknownTag(String tag, String tagLibUri);

    @Message(id = 4082, value = "Unable to load tag handler class \"%s\" for tag \"%s\"")
    String errorLoadingTagHandler(String tagClass, String tag);

    @Message(id = 4083, value = "Body of %s element must not contain any XML elements")
    String invalidScriptingBody(String element);

    @Message(id = 4084, value = "Unterminated quotes")
    String unterminatedQuotes();

    @Message(id = 4085, value = "Attribute value should be quoted")
    String unquotedAttributeValue();

    @Message(id = 4086, value = "Recursive include of file %s")
    String invalidRecursiveInclude(String file);

    @Message(id = 4087, value = "File %s not seen in include")
    String invalidInclude(String file);

    @Message(id = 4088, value = "Invalid scope %s specified")
    String invalidScope(String scope);

    @Message(id = 4089, value = "The attribute %s specified in the standard or custom action also appears as the value of the name attribute in the enclosed jsp:attribute")
    String duplicateAttribute(String attributeName);

    @Message(id = 4090, value = "%s: Mandatory attribute %s missing")
    String missingMandatoryAttribute(String elementName, String attributeName);

    @Message(id = 4091, value = "%s has invalid attribute: %s")
    String invalidAttribute(String elementName, String attributeName);

    @Message(id = 4092, value = "Missing \".tag\" suffix in tag file path %s")
    String invalidTagFileName(String path);

    @Message(id = 4093, value = "Unsupported encoding: %s")
    String unsupportedEncoding(String encoding);

    @Message(id = 4094, value = "Page directive: invalid language attribute")
    String unsupportedPageDirectiveLanguage();

    @Message(id = 4095, value = "Tag directive: invalid language attribute")
    String unsupportedTagDirectiveLanguage();

    @Message(id = 4096, value = "Page directive: invalid buffer size")
    String invalidPageDirectiveBufferSize();

    @Message(id = 4097, value = "Page directive: invalid value for session")
    String invalidPageDirectiveSession();

    @Message(id = 4098, value = "Page directive: invalid value for autoFlush")
    String invalidPageDirectiveAutoFlush();

    @Message(id = 4099, value = "Page directive: invalid value for isThreadSafe")
    String invalidPageDirectiveIsThreadSafe();

    @Message(id = 4100, value = "Page directive: invalid value for isErrorPage")
    String invalidPageDirectiveIsErrorPage();

    @Message(id = 4101, value = "Page directive: invalid value for isELIgnored")
    String invalidPageDirectiveIsElIgnored();

    @Message(id = 4102, value = "Tag directive: invalid value for isELIgnored")
    String invalidTagDirectiveIsElIgnored();

    @Message(id = 4103, value = "Page directive: invalid value for deferredSyntaxAllowedAsLiteral")
    String invalidPageDirectiveDeferredSyntaxAllowedAsLiteral();

    @Message(id = 4104, value = "Tag directive: invalid value for deferredSyntaxAllowedAsLiteral")
    String invalidTagDirectiveDeferredSyntaxAllowedAsLiteral();

    @Message(id = 4105, value = "Page directive: invalid value for trimDirectiveWhitespaces")
    String invalidPageDirectiveTrimDirectiveWhitespaces();

    @Message(id = 4106, value = "Tag directive: invalid value for trimDirectiveWhitespaces")
    String invalidTagDirectiveTrimDirectiveWhitespaces();

    @Message(id = 4107, value = "Page-encoding specified in XML prolog (%s) is different from that specified in jsp-property-group (%s)")
    String encodingConflict(String prologEncoding, String propertyGroupEncoding);

    @Message(id = 4108, value = "Unable to determine scripting variable name from attribute %s")
    String cannotFindVariableNameFromAttribute(String attribute);

    @Message(id = 4109, value = "Name of root element in %s different from %s")
    String wrongRootElement(String file, String element);

    @Message(id = 4110, value = "Invalid tag plugin %s")
    String invalidTagPlugin(String file);

    @Message(id = 4111, value = "Duplicate function name %s in tag library %s")
    String duplicateTagLibraryFunctionName(String function, String library);

    @Message(id = 4112, value = "Mandatory TLD element %s missing in %s")
    String missingRequiredTagLibraryElement(String element, String library);

    @Message(id = 4113, value = "The absolute uri: %s cannot be resolved in either web.xml or the jar files deployed with this application")
    String unresolvableAbsoluteUri(String uri);

    @Message(id = 4114, value = "Unable to get JAR resource \"%s\" containing TLD")
    String errorAccessingJar(String jar);

    @Message(id = 4115, value = "Missing JAR resource \"%s\" containing TLD")
    String missingJar(String jar);

    @Message(id = 4116, value = "Failed to load or instantiate TagExtraInfo class: %s")
    String errorLoadingTagExtraInfo(String tei);

    @Message(id = 4117, value = "Failed to load or instantiate TagLibraryValidator class: %s")
    String errorLoadingTagLibraryValidator(String tlv);

    @Message(id = 4118, value = "Invalid body-content (%s) in tag directive")
    String invalidBodyContentInTagDirective(String content);

    @Message(id = 4119, value = "Tag directive: illegal to have multiple occurrences of the attribute \"%s\" with different values (old: %s, new: %s)")
    String invalidConflictingTagDirectiveAttributeValues(String attribute, String oldValue, String newValue);

    @Message(id = 4120, value = "Cannot specify a value type if 'deferredValue' is not 'true'")
    String cannotUseValueTypeWithoutDeferredValue();

    @Message(id = 4121, value = "Cannot specify a method signature if 'deferredMethod' is not 'true'")
    String cannotUseMethodSignatureWithoutDeferredMethod();

    @Message(id = 4122, value = "'deferredValue' and 'deferredMethod' cannot be both 'true'")
    String cannotUseBothDeferredValueAndMethod();

    @Message(id = 4123, value = "Cannot specify both 'fragment' and 'type' attributes.  If 'fragment' is present, 'type' is fixed as 'jakarta.servlet.jsp.tagext.JspFragment'")
    String cannotUseFragmentWithType();

    @Message(id = 4124, value = "Cannot specify both 'fragment' and 'rtexprvalue' attributes.  If 'fragment' is present, 'rtexprvalue' is fixed as 'true'")
    String cannotUseFragmentWithRtexprValue();

    @Message(id = 4125, value = "Invalid JSP version defined for tag file at %s")
    String invalidTagFileJspVersion(String file);

    @Message(id = 4126, value = "Either name-given or name-from-attribute attribute must be specified in a variable directive")
    String mustSpecifyVariableDirectiveEitherName();

    @Message(id = 4127, value = "Cannot specify both name-given or name-from-attribute attributes in a variable directive")
    String mustNotSpecifyVariableDirectiveBothName();

    @Message(id = 4128, value = "Both or none of the name-from-attribute and alias attributes must be specified in a variable directive")
    String mustNotSpecifyVariableDirectiveBothOrNoneName();

    @Message(id = 4129, value = "The value of %s and the value of %s in line %s are the same")
    String invalidDuplicateNames(String name, String name2, int line);

    @Message(id = 4130, value = "Cannot find an attribute directive with a name attribute with a value \"%s\", the value of this name-from-attribute attribute.")
    String cannotFindAttribute(String name);

    @Message(id = 4131, value = "The attribute directive (declared in line %s and whose name attribute is \"%s\", the value of this name-from-attribute attribute) must be of type java.lang.String, is \"required\" and not a \"rtexprvalue\".")
    String invalidAttributeFound(int line, String name);

    @Message(id = 4132, value = "%s directive can only be used in a tag file")
    String invalidDirectiveInPage(String directive);

    @Message(id = 4133, value = "Invalid directive")
    String invalidDirective();

    @Message(id = 4134, value = "The attribute prefix %s does not correspond to any imported tag library")
    String invalidAttributePrefix(String prefix);

    @Message(id = 4135, value = "Equal symbol expected")
    String missingEqual();

    @Message(id = 4136, value = "Quote symbol expected")
    String missingQuote();

    @Message(id = 4137, value = "Attribute for %s is not properly terminated")
    String unterminatedAttribute(String end);

    @Message(id = 4138, value = "Unable to include %s")
    String errorIncluding(String file);

    @Message(id = 4139, value = "The prefix %s specified in this tag directive has been previously used by an action in file %s line %s")
    String prefixAlreadyInUse(String prefix, String file, int line);

    @Message(id = 4140, value = "Attempt to redefine the prefix %s to %s, when it was already defined as %s in the current scope")
    String prefixRedefinition(String prefix, String uri, String previousUri);

    @Message(id = 4141, value = "Expecting \"jsp:param\" standard action with \"name\" and \"value\" attributes")
    String missingParamAction();

    @Message(id = 4142, value = "The %s tag can only have jsp:attribute in its body")
    String invalidEmptyBodyTag(String tag);

    @Message(id = 4143, value = "Must use jsp:body to specify tag body for %s if jsp:attribute is used.")
    String invalidTagBody(String tag);

    @Message(id = 4144, value = "jsp:attribute must be the subelement of a standard or custom action")
    String invalidJspAttribute();

    @Message(id = 4145, value = "jsp:body must be the subelement of a standard or custom action")
    String invalidJspBody();

    @Message(id = 4146, value = "jsp:fallback must be a direct child of jsp:plugin")
    String invalidJspFallback();

    @Message(id = 4147, value = "jsp:params must be a direct child of jsp:plugin")
    String invalidJspParams();

    @Message(id = 4148, value = "The jsp:param action must not be used outside the jsp:include, jsp:forward, or jsp:params elements")
    String invalidJspParam();

    @Message(id = 4149, value = "jsp:output must not be used in standard syntax")
    String invalidJspOutput();

    @Message(id = 4150, value = "Invalid standard action")
    String invalidStandardAction();

    @Message(id = 4151, value = "No tag \"%s\" defined in tag library imported with prefix \"%s\"")
    String unknownTagPrefix(String tag, String prefix);

    @Message(id = 4152, value = "\'&lt;\', when appears in the body of &lt;jsp:text&gt;, must be encapsulated within a CDATA")
    String badContent();

    @Message(id = 4153, value = "%s not allowed in a template text body")
    String invalidTemplateTextBody(String invalid);

    @Message(id = 4154, value = "Custom tag is not allowed in a template text body")
    String invalidTagInTemplateTextBody();

    @Message(id = 4155, value = "The end tag \"&lt;/%s\" is unbalanced")
    String unbalancedEndTag(String action);

    @Message(id = 4156, value = "A jsp:attribute standard action cannot be nested within another jsp:attribute standard action")
    String invalidJspAttributeNesting();

    @Message(id = 4157, value = "A jsp:body standard action cannot be nested within another jsp:body or jsp:attribute standard action")
    String invalidJspBodyNesting();

    @Message(id = 4158, value = "Invalid body content type")
    String invalidBodyContentType();

    @Message(id = 4159, value = "Page directive: illegal to have multiple occurrences of '%s' with different values (old: %s, new: %s)")
    String invalidConflictingPageDirectiveAttribute(String attribute, String oldValue, String newValue);

    @Message(id = 4160, value = "Page directive must not have multiple occurrences of '%s'")
    String invalidDuplicatePageDirectiveAttribute(String attribute);

    @Message(id = 4161, value = "Page directive auto flush cannot be used with a buffer")
    String invalidConflictingPageDirectiveAutoFlushBuffer();

    @Message(id = 4162, value = "Tag directive must not have multiple occurrences of '%s'")
    String invalidDuplicateTagDirectiveAttribute(String attribute);

    @Message(id = 4163, value = "Page-encoding specified in jsp-property-group (%s) is different from that specified in page directive (%s)")
    String pageEncodingConflictJspPropertyGroup(String jspPropertyGroupEncoding, String pageDirectiveEncoding);

    @Message(id = 4164, value = "Page-encoding specified in XML prolog (%s) is different from that specified in page directive (%s)")
    String pageEncodingConflictProlog(String prologEncoding, String pageDirectiveEncoding);

    @Message(id = 4165, value = "Invalid version number: \"%s\", must be \"1.2\", \"2.0\", \"2.1\", or \"2.2\"")
    String invalidJspVersionNumber(String version);

    @Message(id = 4166, value = "Neither \'uri\' nor \'tagdir\' attribute specified")
    String invalidTaglibDirectiveMissingLocation();

    @Message(id = 4167, value = "Both \'uri\' and \'tagdir\' attributes specified")
    String invalidTaglibDirectiveConflictingLocation();

    @Message(id = 4168, value = "jsp:params must contain at least one nested jsp:param")
    String invalidEmptyJspParams();

    @Message(id = 4169, value = "setProperty: can't have non-null value when property=*")
    String invalidSetProperty();

    @Message(id = 4170, value = "setProperty: can't have non-null value whith param")
    String invalidSetPropertyEitherParam();

    @Message(id = 4171, value = "Missing type for useBean")
    String missingUseBeanType();

    @Message(id = 4172, value = "Duplicate bean name: %s")
    String duplicateUseBeanName(String name);

    @Message(id = 4173, value = "Illegal for useBean to use session scope when JSP page declares (via page directive) that it does not participate in sessions")
    String cannotAccessSessionScopeWithUseBean();

    @Message(id = 4174, value = "Cannot use both and attribute and a type in useBean")
    String cannotUseBothAttributeAndTypeInUseBean();

    @Message(id = 4175, value = "Type not declared in plugin")
    String missingPluginType();

    @Message(id = 4176, value = "Illegal value %s for 'type' attribute in plugin: must be 'bean' or 'applet'")
    String badPluginType(String type);

    @Message(id = 4177, value = "Code not declared in plugin")
    String missingPluginCode();

    @Message(id = 4178, value = "#{..} is not allowed in template text")
    String invalidDeferredExpressionInTemplateText();

    @Message(id = 4179, value = "TagInfo object for %s is missing from TLD")
    String missingTagInfo(String tag);

    @Message(id = 4180, value = "The TLD for the class %s specifies an invalid body-content (JSP) for a SimpleTag")
    String invalidSimpleTagBodyContent(String tag);

    @Message(id = 4181, value = "The %s tag declares that it accepts dynamic attributes but does not implement the required interface")
    String unimplementedDynamicAttributes(String tag);

    @Message(id = 4182, value = "Tag %s has one or more variable subelements and a TagExtraInfo class that returns one or more VariableInfo")
    String invalidTeiWithVariableSubelements(String tag);

    @Message(id = 4183, value = "Mandatory attributes missing")
    String missingMandatoryAttributes();

    @Message(id = 4184, value = "Mandatory XML-style \'name\' attribute missing")
    String missingMandatoryNameAttribute();

    @Message(id = 4185, value = "&lt;jsp:output&gt; must not have a body")
    String invalidJspOutputBody();

    @Message(id = 4186, value = "&lt;jsp:output&gt;: illegal to have multiple occurrences of \"%s\" with different values (old: %s, new: %s)")
    String invalidJspOutputConflict(String attribute, String oldValue, String newValue);

    @Message(id = 4187, value = "&lt;jsp:output&gt;: 'doctype-root-element' and 'doctype-system' attributes must appear together")
    String errorJspOutputDoctype();

    @Message(id = 4188, value = "&lt;jsp:output&gt;: 'doctype-system' attribute must appear if 'doctype-public' attribute appears")
    String errorJspOutputMissingDoctype();

    @Message(id = 4189, value = "Missing \'var\' or \'varReader\' attribute")
    String missingVarAttribute();

    @Message(id = 4190, value = "Only one of \'var\' or \'varReader\' may be specified")
    String errorBothVarAttributes();

    @Message(id = 4191, value = "Cannot use both ${} and #{} EL expressions in the same attribute value")
    String errorUsingBothElTypes();

    @Message(id = 4192, value = "A literal value was specified for attribute %s that is defined as a deferred method with a return type of void. JSP.2.3.4 does not permit literal values in this case")
    String errorUsingLiteralValueWithDeferredVoidReturnTyep(String attribute);

    @Message(id = 4193, value = "Unknown attribute type (%s) was declared for attribute %s")
    String unknownAttributeType(String attributeType, String attribute);

    @Message(id = 4194, value = "Cannot coerce value (%s) to type (%s) for attribute %s")
    String errorCoercingAttributeValue(String attribute, String attributeType, String value);

    @Message(id = 4195, value = "According to TLD or attribute directive in tag file, attribute %s does not accept any expressions")
    String noExpressionAllowedForAttribute(String attribute);

    @Message(id = 4196, value = "%s contains invalid expression(s)")
    String invalidExpression(String attributeValue);

    @Message(id = 4197, value = "Attribute %s invalid for tag %s according to TLD")
    String invalidAttributeForTag(String attribute, String tag);

    @Message(id = 4198, value = "The %s attribute of the %s standard action does not accept any expressions")
    String noExpressionAllowedForAttributeInAction(String attribute, String action);

    @Message(id = 4199, value = "The function %s must be used with a prefix when a default namespace is not specified")
    String missingFunctionPrefix(String function);

    @Message(id = 4200, value = "The function prefix %s does not correspond to any imported tag library")
    String unknownFunctionPrefix(String prefix);

    @Message(id = 4201, value = "The function %s cannot be located with the specified prefix")
    String unknownFunction(String function);

    @Message(id = 4202, value = "Invalid syntax for function signature in TLD. Tag Library: %s, Function: %s")
    String invalidFunctionSignature(String prefix, String function);

    @Message(id = 4203, value = "Invalid syntax for function signature in TLD. Parenthesis '(' expected. Tag Library: %s, Function: %s")
    String invalidFunctionSignatureMissingParent(String prefix, String function);

    @Message(id = 4204, value = "The class %s specified in TLD for the function %s cannot be found")
    String missingFunctionClass(String className, String function);

    @Message(id = 4205, value = "The class %s specified in the method signature in TLD for the function %s cannot be found")
    String missingSignatureClass(String className, String function);

    @Message(id = 4206, value = "Method \"%s\" for function \"%s\" not found in class \"%s\"")
    String missingMethodInClass(String method, String function, String className);

    @Message(id = 4207, value = "Validation error messages from TagExtraInfo for %s")
    String errorValidatingTag(String tag);

    @Message(id = 4208, value = "Validation error messages from TagLibraryValidator for %s in %s")
    String errorValidatingTaglibrary(String taglib, String jsp);

    @Message(id = 4209, value = "An exception occurred processing JSP page %s at line %s")
    String jspException(String jsp, int line);

    @Message(id = 4210, value = "Security exception for class %s")
    String securityExceptionLoadingClass(String className);

    @Message(id = 4211, value = "Stacktrace:")
    String stacktrace();

    @Message(id = 4212, value = "Background compilation failed")
    String backgroundCompilationFailed();

    @Message(id = 4213, value = "Security initialization failed")
    String errorInitializingSecurity();

    @Message(id = 4214, value = "Error unquoting attribute value")
    String errorUnquotingAttributeValue();

    @Message(id = 4215, value = "Invalid negative parameter: %s")
    IllegalArgumentException invalidNegativeSmapPosition(int position);

    @Message(id = 4216, value = "Undefined position")
    IllegalArgumentException undefinedPosition();

    @Message(id = 4217, value = "Unknown file name: %s")
    IllegalArgumentException unknownFileName(String fileName);

    @Message(id = 4218, value = "the name attribute of the attribute directive")
    String tagFileProcessorAttrName();

    @Message(id = 4219, value = "the name-given attribute of the variable directive")
    String tagFileProcessorVarNameGiven();

    @Message(id = 4220, value = "the name-from-attribute attribute of the variable directive")
    String tagFileProcessorVarNameFrom();

    @Message(id = 4221, value = "the alias attribute of the variable directive")
    String tagFileProcessorVarAlias();

    @Message(id = 4222, value = "the dynamic-attributes attribute of the tag directive")
    String tagFileProcessorTagDynamic();

    @Message(id = 4223, value = "Null context")
    NullPointerException elResolverNullContext();

    @Message(id = 4224, value = "Error resolving variable %s due to %s")
    String errorResolvingVariable(String variable, String message);

    @Message(id = 4225, value = "Legacy VariableResolver wrapped, not writable")
    String legacyVariableResolver();

    @Message(id = 4226, value = "Stream closed")
    String streamClosed();

    @Message(id = 4227, value = "Null text argument")
    IllegalArgumentException nullCharBufferTextArgument();

    @Message(id = 4228, value = "Null characters argument")
    IllegalArgumentException nullCharBufferCharactersArgument();

    @Message(id = 4229, value = "Null writer argument")
    IllegalArgumentException nullCharBufferWriterArgument();

    @Message(id = 4230, value = "Invalid start position")
    IllegalArgumentException invalidCharBufferStartPosition();

    @Message(id = 4231, value = "Invalid length")
    IllegalArgumentException invalidCharBufferLength();

    @Message(id = 4232, value = "No org.apache.tomcat.InstanceManager set in ServletContext")
    IllegalStateException noInstanceManager();

    @Message(id = 4233, value = "Null ELContextListener")
    IllegalArgumentException nullElContextListener();

    @Message(id = 4234, value = "Null ServletContext")
    IllegalArgumentException nullServletContext();

    @Message(id = 4235, value = "Null JspContext")
    IllegalArgumentException nullJspContext();

    @Message(id = 4236, value = "Null ELResolver")
    IllegalArgumentException nullElResolver();

    @Message(id = 4237, value = "Cannot add ELResolver after the first request has been made")
    IllegalStateException cannotAddElResolver();

    @Message(id = 4238, value = "Negative buffer size")
    IllegalArgumentException invalidNegativeBufferSize();

    @Message(id = 4239, value = "Page needs a session and none is available")
    IllegalStateException pageNeedsSession();

    @Message(id = 4240, value = "Null throwable")
    NullPointerException nullThrowable();

    @Message(id = 4241, value = "Invalid function mapping - no such method: %s")
    RuntimeException invalidFunctionMapping(String message);

    @Message(id = 4242, value = "Invalid request parameter %s value %s")
    String invalidRequestParameterValue(String name, String value);

    @Message(id = 4243, value = "The processing instruction target matching \"[xX][mM][lL]\" is not allowed.")
    String reservedPiTarget();

    @Message(id = 4244, value = "White space is required between the processing instruction target and data.")
    String requiredWhiteSpaceAfterPiTarget();

    @Message(id = 4245, value = "In URL tags, when the \"context\" attribute is specified, values of both \"context\" and \"url\" must start with '/'.")
    String invalidContextAndUrlValues();

    @Message(id = 4246, value = "Unexpected internal error during &lt;import&gt: Target servlet called getWriter(), then getOutputStream()")
    IllegalStateException usedOutputStreamAfterWriter();

    @Message(id = 4247, value = "Unexpected internal error during &lt;import&gt: Target servlet called getOutputStream(), then getWriter()")
    IllegalStateException usedWriterAfterOutputStream();

    @Message(id = 4248, value = "JSPs only permit GET POST or HEAD")
    String forbiddenHttpMethod();

    @Message(id = 4249, value = "Page directive: invalid value for import")
    IllegalArgumentException invalidImportStatement();

    @Message(id = 4250, value = "The String literal %s is not valid. It must be contained within single or double quotes.")
    IllegalArgumentException invalidStringLiteral(String aString);

    @Message(id = 4251, value = "An error occurred at line: %s column: %s")
    String errorInJspFileLineColumn(int line, int column);

    @Message(id = 4252, value = "The expression %s is not valid. Within a quoted String only [\\], ['] and [\"] may be escaped with [\\].")
    IllegalArgumentException invalidQuoting(String aString);

    @Message(id = 4253, value = "Page directive: invalid value for errorOnELNotFound")
    String invalidPageDirectiveErrorOnELNotFound();

    @Message(id = 4254, value = "Tag directive: invalid value for errorOnELNotFound")
    String invalidTagDirectiveErrorOnELNotFound();
}
