package com.guiseframework.platform.web;

import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.util.*;

import javax.mail.internet.ContentType;

import static com.garretwilson.javascript.JavaScriptConstants.*;
import static com.garretwilson.text.xml.XMLConstants.*;
import static com.garretwilson.text.xml.stylesheets.css.XMLCSSConstants.*;
import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;

import com.garretwilson.text.xml.xhtml.XHTMLConstants;
import com.garretwilson.util.Debug;
import com.guiseframework.*;
import static com.guiseframework.GuiseApplication.*;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.geometry.Axis;
import com.guiseframework.model.*;

import static com.garretwilson.util.LocaleUtilities.*;
import static com.guiseframework.platform.web.GuiseCSSStyleConstants.*;
import static com.guiseframework.platform.web.WebPlatform.*;

/**Strategy for rendering an application frame as a series of XHTML elements.
This class generates XHTML in the form: &lt;xhtml:html&gt;&lt;xhtml:body&gt;&lt;xhtml:form&gt;[&lt;xhtml:div&gt;]
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public class WebApplicationFrameDepictor<C extends ApplicationFrame> extends AbstractWebFrameDepictor<C>
{

	/**The ID segment for the hidden action input field.*/ 
	protected final static String ACTION_INPUT_ID_SEGMENT="input";

	/**The ID segment for the busy information.*/ 
	protected final static String BUSY_ID_SEGMENT="busy";

	/**The ID segment for the flyover IFrame used with IE6.*/ 
	protected final static String FLYOVER_IFRAME_ID_SEGMENT="flyoverIFrame";

	/**The ID segment for the form.*/ 
	protected final static String FORM_ID_SEGMENT="form";

	/**The ID segment for the layer to shield user input during initialization.*/ 
	protected final static String INIT_IFRAME_ID_SEGMENT="initIFrame";

	/**The ID segment for the model IFrame used with IE6.*/ 
	protected final static String MODAL_IFRAME_ID_SEGMENT="modalIFrame";

	/**The ID for the Guise SWF.*/ 
	protected final static String GUISE_FLASH_ID="guiseFlash";

	/**The available JavaScript versions above 1.0 that could be supported by a browser.*/
	private final static String[] JAVSCRIPT_VERSIONS=new String[]{"1.1", "1.2", "1.3", "1.4", "1.5", "2.0"};
	
	/**Determines the ID to be used for the hidden field associated with the given application frame.
	@param applicationFrame The frame for which an input ID should be returned.
	@return The ID of the hidden field that holds any action value.
	*/
	public static String getActionInputID(final ApplicationFrame applicationFrame)
	{
		return applicationFrame.getDepictID()+'.'+ACTION_INPUT_ID_SEGMENT;	//create an ID for the form ID segment on the frame TODO use common routine
	}

	/**Determines the ID to be used for an element containing busy component information.
	@param applicationFrame The frame for which a busy ID should be returned.
	@return An ID appropriate for creating an element to contain busy component information.
	*/
	public static String getBusyID(final ApplicationFrame applicationFrame)
	{
//TODO fix for no form		return applicationFrame.getID()+'.'+BUSY_ID_SEGMENT;	//create an ID for the busy ID segment on the frame TODO use common routine
		return BUSY_ID_SEGMENT;	//create an ID for the busy ID segment on the frame TODO use common routine
	}

	/**Determines the ID to be used for an IFrame used for a flyover shield in IE6.
	@param applicationFrame The frame for which a flyover IFrame ID should be returned.
	@return An ID appropriate for creating a flyover IFrame for IE6.
	*/
	public static String getFlyoverIFrameID(final ApplicationFrame applicationFrame)
	{
//TODO fix for no form		return applicationFrame.getID()+'.'+FLYOVER_IFRAME_ID_SEGMENT;	//create an ID for the flyover IFrame ID segment on the frame TODO use common routine
		return FLYOVER_IFRAME_ID_SEGMENT;	//create an ID for the flyover IFrame ID segment on the frame TODO use common routine
	}

	/**Determines the ID to be used for a form associated with the given application frame.
	@param applicationFrame The frame for which a form ID should be returned.
	@return An ID appropriate for creating a form in the given frame.
	*/
	public static String getFormID(final ApplicationFrame applicationFrame)
	{
		return applicationFrame.getDepictID()+'.'+FORM_ID_SEGMENT;	//create an ID for the form ID segment on the frame TODO use common routine
	}

	/**Determines the ID to be used for an IFrame used for shielding the UI from user input during initialization.
	@param applicationFrame The frame for which an init IFrame ID should be returned.
	@return An ID appropriate for creating an init IFrame.
	*/
	public static String getInitIFrameID(final ApplicationFrame applicationFrame)
	{
//TODO fix for no form		return applicationFrame.getID()+'.'+INIT_IFRAME_ID_SEGMENT;	//create an ID for the init IFrame ID segment on the frame TODO use common routine
		return INIT_IFRAME_ID_SEGMENT;	//create an ID for the init IFrame ID segment on the frame TODO use common routine
	}

	/**Determines the ID to be used for an IFrame used for a modal shield in IE6.
	@param applicationFrame The frame for which a modal IFrame ID should be returned.
	@return An ID appropriate for creating a modal IFrame for IE6.
	*/
	public static String getModalIFrameID(final ApplicationFrame applicationFrame)
	{
//TODO fix for no form		return applicationFrame.getID()+'.'+MODAL_IFRAME_ID_SEGMENT;	//create an ID for the modal IFrame ID segment on the frame TODO use common routine
		return MODAL_IFRAME_ID_SEGMENT;	//create an ID for the modal IFrame ID segment on the frame TODO use common routine
	}

	/**The lazily-created busy component.*/
	private Component busyComponent=null;

	/**Default constructor using the XHTML <code>body</code> element.*/
	public WebApplicationFrameDepictor()
	{
		super(XHTML_NAMESPACE_URI, ELEMENT_BODY);	//represent <xhtml:body>
	}

	/**Begins the rendering process.
	This version renders an outer html element.
	@exception IOException if there is an error rendering the component.
	*/
	protected void depictBegin() throws IOException
	{
		final WebPlatform platform=getPlatform();	//get the platform
		final WebUserAgentProduct userAgent=getPlatform().getClientProduct();	//get the user agent
		final WebDepictContext depictContext=getDepictContext();	//get the depict context
		final C component=getDepictedObject();	//get the component
		final GuiseSession session=getSession();	//get the session
		final GuiseApplication application=session.getApplication();	//get a reference to the application
		final Locale locale=session.getLocale();	//get the component's locale
			//don't send the XML declaration for MSIE 6, because the XML declaration will switch that user agent into quirks mode
//TODO testing		final boolean writeXMLDeclaration=true;	//always write the XML declaration, even though this will switch IE6 to quirks mode--IE6 in standards mode is incredibly slow
		final boolean isUserAgentIE=userAgent.getBrand()==WebUserAgentProduct.Brand.INTERNET_EXPLORER;	//see if this is IE
		final boolean isUserAgentIE6=isUserAgentIE && userAgent.getVersionNumber()<7;	//see if this is IE6
		final boolean writeXMLDeclaration=!(isUserAgentIE6 && !depictContext.isQuirksMode());	//always write the XML declaration except for IE6 when not in quirks mode (writing the XML declaration puts IE6 into quirks mode)
			//see http://www.quirksmode.org/css/quirksmode.html
			//see http://blogs.msdn.com/ie/archive/2005/09/15/467901.aspx
			//see http://msdn.microsoft.com/library/default.asp?url=/library/en-us/dnie60/html/cssenhancements.asp
			//see http://msdn.microsoft.com/workshop/author/dhtml/reference/properties/compatmode.asp
			//see http://hsivonen.iki.fi/doctype/
			//XML declaration and doctype
		depictContext.writeDocType(writeXMLDeclaration, XHTML_NAMESPACE_URI, ELEMENT_HTML, GUISE_XHTML_DTD_PUBLIC_ID);	//write the doctype and optionally the XML declaration
			//<xhtml:html>
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_HTML);	//<xhtml:html>
		depictContext.writeAttribute(null, ATTRIBUTE_XMLNS, XHTML_NAMESPACE_URI.toString());	//xmlns="http://www.w3.org/1999/xhtml"
		depictContext.writeAttribute(XMLNS_NAMESPACE_URI, GUISE_ML_NAMESPACE_PREFIX, GUISE_ML_NAMESPACE_URI.toString());	//xmlns:guise="http://guiseframework.com/id/ml#"
//	TODO del; not present in XHTML		context.writeAttribute(null, XHTMLConstants.ATTRIBUTE_LANG, getLanguageTag(locale));	//lang="locale"
		depictContext.writeAttribute(null, XHTMLConstants.ATTRIBUTE_LANG, getLanguageTag(locale));	//lang="locale"
		final Orientation componentOrientation=component.getComponentOrientation();	//get the orientation used by the frame
		writeDirectionAttribute(componentOrientation, componentOrientation.getFlow(Axis.X));	//always write the direction for the <xhtml:html> element
		depictContext.write('\n');
			//<xhtml:head>
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_HEAD);	//<xhtml:head>
		depictContext.write("\n\t");
		final StringBuilder headerCommentStringBuilder=new StringBuilder();	//create a new string builder for constructing the header comment (don't put the comment before the <html> element, or it will break IE6)
		headerCommentStringBuilder.append("\n");
		headerCommentStringBuilder.append("\tDocument: ").append(depictContext.getDepictURI()).append('\n');
		headerCommentStringBuilder.append("\tCreated: ").append(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale).format(new Date())).append('\n');
		headerCommentStringBuilder.append("\n");
		headerCommentStringBuilder.append("\tXHTML produced by ");
		if(!Guise.getInstance().isLicensed())	//if this copy of Guise is not licensed
		{
			headerCommentStringBuilder.append("an unlicensed copy of ");
		}
		headerCommentStringBuilder.append(Guise.GUISE_NAME).append(" build ").append(Guise.BUILD_ID).append(".\n");	//Guise
		headerCommentStringBuilder.append("\tFor more information on ").append(Guise.GUISE_NAME).append(", please go to ").append(Guise.GUISE_WEB_URI);
		headerCommentStringBuilder.append(" or contact <info@globalmentor.com>.\n\t");
		depictContext.writeComment(headerCommentStringBuilder.toString());	//write the header comment
		depictContext.write("\n");
		final Component content=component.getContent();	//get the child component
		final String title=content.getLabel();	//get the child component title
		if(title!=null)	//if a title is given
		{
			//<xhtml:title>
			depictContext.write('\t');
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TITLE);	//<xhtml:title>
			depictContext.write(AbstractModel.getPlainText(session.resolveString(title), content.getLabelContentType()));	//write the title
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TITLE);	//</xhtml:title>
			depictContext.write('\n');
		}
			//<xhtml:link> styles in this order: theme styles (from most distant parent to current theme), application style, destination style
		for(final URI styleURI:depictContext.getStyles())	//for each style URI
		{
//TODO del Debug.trace("looking at style URI", styleURI);
			depictContext.write('\t');
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_LINK);	//<xhtml:link>
			depictContext.writeAttribute(null, ELEMENT_LINK_ATTRIBUTE_REL, LINK_REL_STYLESHEET);	//rel="stylesheet"
			depictContext.writeAttribute(null, ELEMENT_LINK_ATTRIBUTE_TYPE, TEXT_CSS_CONTENT_TYPE.toString());	//type="text/css"
			depictContext.writeAttribute(null, ELEMENT_LINK_ATTRIBUTE_HREF, application.resolveURI(styleURI).toString());	//href="styleURI"	(resolved to context)
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_LINK);	//</xhtml:link>		
			depictContext.write('\n');
		}
			//<xhtml:script> (internal) (we must have separate JavaScript elements for Firefox, and we cannot use XML comments in XHTML or Firefox won't recognize the JavaScript)
			//this script sets up the extended navigator properties, as well as the initial JavaScript version indicator 
		depictContext.write("\t");
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_SCRIPT, false);	//<xhtml:script> (explicitly don't create an empty <xhtml:script> element, otherwise IE wouldn't recognize it)
		depictContext.writeAttribute(null, ELEMENT_SCRIPT_ATTRIBUTE_TYPE, JAVASCRIPT_CONTENT_TYPE.toString());	//type="text/javascript"
		depictContext.write('\n');
		depictContext.write("\t\t");
		depictContext.writeLiteral("navigator.userAgentName=\""+userAgent.getName()+"\";");	//navigator.userAgentName=USER_AGENT_NAME TODO use a constant for the variable name
		depictContext.write('\n');
		depictContext.write("\t\t");
		depictContext.writeLiteral("navigator.userAgentVersionNumber="+userAgent.getVersionNumber()+";");	//navigator.userAgentVersionNumber=USER_AGENT_VERSION_NUMBER TODO use a constant for the variable name
		depictContext.write('\n');
			//JavaScript version variable
		depictContext.write("\t\t");
		depictContext.writeLiteral("var javascriptVersion=1.0;");	//initialize the JavaScript version
		depictContext.write('\n');
			//empty HTML document path variable
		depictContext.write("\t\t");
//TODO del		context.writeLiteral("var GUISE_EMPTY_HTML_DOCUMENT_PATH=\""+application.resolvePath(GUISE_EMPTY_HTML_DOCUMENT_PATH)+"\";");	//write the path to the Guise empty HTML document
		depictContext.writeLiteral("var GUISE_PUBLIC_RESOURCE_BASE_PATH=\""+application.resolvePath(GUISE_PUBLIC_RESOURCE_BASE_PATH)+"\";");	//write the path to the Guise base path for public resources
		depictContext.write('\n');
		depictContext.write('\t');
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_SCRIPT);	//</xhtml:script>
		depictContext.write("\n");
			//write scripts to test for the JavaScript version
		for(final String javascriptVersion:JAVSCRIPT_VERSIONS)	//for each version
		{
			depictContext.write("\t");	//<xhtml:script> (internal) for testing for a JavaScript version
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_SCRIPT, false);	//<xhtml:script> (explicitly don't create an empty <xhtml:script> element, otherwise IE wouldn't recognize it)
			depictContext.writeAttribute(null, ELEMENT_SCRIPT_ATTRIBUTE_LANGUAGE, "JavaScript"+javascriptVersion);	//language="JavaScriptX.X"
			depictContext.writeLiteral("\t\tjavascriptVersion="+javascriptVersion+";\n");	//update the JavaScript version
			depictContext.write('\t');
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_SCRIPT);	//</xhtml:script>
			depictContext.write("\n");
		}
			//<xhtml:script> (external)
		depictContext.write("\t");
		depictContext.writeJavaScriptElement(JAVASCRIPT_JAVASCRIPT_PATH);	//JavaScript: javascript.js
		depictContext.write("\n");
		depictContext.write("\t");
		depictContext.writeJavaScriptElement(DOM_JAVASCRIPT_PATH);	//JavaScript: dom.js
		depictContext.write("\n");
		depictContext.write("\t");
		depictContext.writeJavaScriptElement(AJAX_JAVASCRIPT_PATH);	//JavaScript: ajax.js
		depictContext.write("\n");
		depictContext.write("\t");
		depictContext.writeJavaScriptElement(GUISE_JAVASCRIPT_PATH);	//JavaScript: guise.js
		depictContext.write("\n");
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_HEAD);	//</xhtml:head>		
		depictContext.write("\n");

			//<xhtml:body>
		super.depictBegin();	//do the default beginning rendering
		depictContext.writeAttribute(null, ATTRIBUTE_ID, getPlatform().getDepictIDString(component.getDepictID()));	//write the standard ID; don't write any class information, because this information (especially the frame-related style IDs) can confuse other frame styles
//TODO del when works		writeIDClassAttributes(context, component, null, null);	//write the ID and class attributes with no prefixes or suffixes
		writeDirectionAttribute();	//write the component direction, if this component specifies a direction
/*TODO fix for new notification framework
		if(component.hasErrors())	//if this component has one or more errors
		{
			final StringBuilder errorStringBuilder=new StringBuilder();	//we'll construct the error message
			for(final Throwable error:component.getErrors())	//for each error
			{
				final String message=error.getMessage();	//get the error message
				if(errorStringBuilder.length()>0)	//if we've already included error messages
				{
					errorStringBuilder.append("\n");	//separate the messages
				}
				errorStringBuilder.append(context.getSession().getStringResource(ERROR_LABEL_RESOURCE));	//"Error"
					//add the associated component's label, if any
				if(error instanceof ComponentException)	//if the error is a component exception
				{
					final ComponentException componentException=(ComponentException)error;	//get the component exception version of the error
					final Component errorComponent=componentException.getComponent();	//get the associated component
					final String label=errorComponent.getSession().determineString(errorComponent.getLabel(), errorComponent.getLabelResourceKey());	//get the associated label
					if(label!=null)	//if there is a label for this component
					{
						errorStringBuilder.append(' ').append('(').append(label).append(')');	//identify the component by its label
					}
				}
				errorStringBuilder.append(':').append(' ');	//separate the introduction from the actual error message
				errorStringBuilder.append(message!=null ? message : error.toString());	//add the error message, using the string version of the error if there is no message
			}
			context.writeAttribute(null, ATTRIBUTE_ONLOAD, createStatement(alertLiteral(errorStringBuilder.toString())));	//onload="alert('errors...');"
			component.clearErrors();	//remove the error from the frame; we've reported it to the user
		}
*/
		depictContext.write("\n");
/*TODO del; we have to do this dynamically now because of the EOLAS patent loss by Mircosoft
		//Guise Flash
		final String resolvedGuiseFlashPath=application.resolvePath(GUISE_FLASH_PATH);	//resolve the path to the Guise Flash file
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_OBJECT);	//<xhtml:object>
		depictContext.writeAttribute(null, ATTRIBUTE_ID, GUISE_FLASH_ID);	//id="guiseFlash"		
		if(getPlatform().getClientProduct().getBrand()==WebUserAgentProduct.Brand.INTERNET_EXPLORER)	//if the user agent is IE, use the special attributes
		{
			depictContext.writeAttribute(null, ELEMENT_OBJECT_ATTRIBUTE_CLASSID, FLASH_CLASS_ID);	//classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"; only write the classid attributes for IE, because it will prevent the Flash from being loaded in Firefox
			depictContext.writeAttribute(null, ELEMENT_OBJECT_ATTRIBUTE_CODEBASE, getSWFlashCabURI("8,0,0,0", HTTPS_SCHEME.equals(depictContext.getDepictURI().getScheme())).toString());	//codebase="http[s]://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=8,0,0,0"
		}
		else	//if the user agent is not IE, specify the object content type
		{
			depictContext.writeAttribute(null, ELEMENT_OBJECT_ATTRIBUTE_TYPE, FLASH_CONTENT_TYPE.toString());	//type="application/x-shockwave-flash"; don't write the type Type attribute in IE, because this will prevent IE from loading the Flash movie 			
			depictContext.writeAttribute(null, ELEMENT_OBJECT_ATTRIBUTE_DATA, resolvedGuiseFlashPath);	//data="flashURI"; don't write the data attribute in IE, because it will prevent the Flash movie from showing its preloader
		}
		final Map<String, Object> guiseFlashStyles=new HashMap<String, Object>();	//create a new map of styles
//		guiseFlashStyles.put(CSS_PROP_DISPLAY, CSS_DISPLAY_NONE);	//don't display the Flash
		guiseFlashStyles.put(CSS_PROP_WIDTH, Extent.ZERO_EXTENT1);	//zero dimensions; a display of "none" will cause the object not to be initialized on Firefox and IE7
		guiseFlashStyles.put(CSS_PROP_HEIGHT, Extent.ZERO_EXTENT1);	//zero height
		writeStyleAttribute(guiseFlashStyles);	//write the styles
			//param movie="flashURI" (necessary for IE)
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_PARAM);	//<xhtml:param>
		depictContext.writeAttribute(null, ELEMENT_PARAM_ATTRIBUTE_NAME, MOVIE_PARAMETER);	//name="movie"
		depictContext.writeAttribute(null, ELEMENT_PARAM_ATTRIBUTE_VALUE, resolvedGuiseFlashPath);	//value="flashURI"
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_PARAM);	//</xhtml:param>
			//param quality="high"
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_PARAM);	//<xhtml:param>
		depictContext.writeAttribute(null, ELEMENT_PARAM_ATTRIBUTE_NAME, QUALITY_PARAMETER);	//name="quality"
		depictContext.writeAttribute(null, ELEMENT_PARAM_ATTRIBUTE_VALUE, QUALITY_PARAMETER_HIGH);	//value="high"
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_PARAM);	//</xhtml:param>
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_OBJECT);	//</xhtml:object>
*/
		//<xhtml:form>
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_FORM);	//<xhtml:form>
		depictContext.writeAttribute(null, ATTRIBUTE_ID, WebApplicationFrameDepictor.getFormID(component.getSession().getApplicationFrame()));	//write the form ID in the HTML id attribute 
		depictContext.writeAttribute(null, ELEMENT_FORM_ATTRIBUTE_METHOD, FORM_METHOD_POST);	//method="post"
		final ContentType encodingType=hasResourceImportControl(component) ? MULTIPART_FORM_DATA_CONTENT_TYPE : APPLICATION_X_WWW_FORM_URLENCODED_CONTENT_TYPE;	//if we have a file upload component, use a multipart form data content type; otherwise, use the default
		depictContext.writeAttribute(null, ELEMENT_FORM_ATTRIBUTE_ENCTYPE, encodingType.toString());	//enctype="application/x-www-form-urlencoded" (default) or "multipart/form-data" (for file uploads)
		depictContext.writeAttribute(null, ELEMENT_FORM_ATTRIBUTE_ACTION, depictContext.getDepictURI().getRawPath());	//action="navigationURIPath" (submit to same path)
		writeStyleAttribute(getBodyStyles());	//write the component's body styles
		depictContext.write("\n");
			//create a dummy input element within the form that will hold whether this button was pressed;
			//this is to get around an IE 6 bug that submits the content of the button rather than its value, and does so for all buttons, not just the one pressed
		final String actionInputID=WebApplicationFrameDepictor.getActionInputID(component.getSession().getApplicationFrame());	//get the action input ID
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//<xhtml:div>
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_INPUT, true);	//<xhtml:input>
		depictContext.writeAttribute(null, ATTRIBUTE_ID, actionInputID);	//id="xxx:input"
		depictContext.writeAttribute(null, ATTRIBUTE_NAME, actionInputID);	//name="xxx:input"
		depictContext.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_TYPE, INPUT_TYPE_HIDDEN);	//type="hidden"
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_INPUT);	//</xhtml:input>
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//</xhtml:div>
		depictContext.write("\n");
		final Menu menu=component.getMenu();	//get the frame's menu, if any
		if(menu!=null)	//if there is a menu
		{
				//TODO check the type of menu; this may be a sidebar menu
//TODO del			context.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//<xhtml:div>
			menu.depict();	//update the menu
//TODO del			context.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//</xhtml:div>
		}
		final Toolbar toolbar=component.getToolbar();	//get the frame's toolbar, if any
		if(toolbar!=null)	//if there is a toolbar
		{
			toolbar.depict();	//update the toolbar
		}
	}

	/**Ends the rendering process.
	This version ends the outer html element and the inner form element.
	@exception IOException if there is an error rendering the component.
	*/
	protected void depictEnd() throws IOException
	{
		final GuiseSession session=getSession();	//get the session
		final WebDepictContext depictContext=getDepictContext();	//get the depict context
		final GuiseApplication application=session.getApplication();	//get the application
		depictContext.write("\n");
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_FORM);	//</xhtml:form>
		depictContext.write("\n");
			//license
		if(!Guise.getInstance().isLicensed())	//if this copy of Guise is not licensed
		{
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_P);	//<xhtml:p>
			depictContext.writeAttribute(null, ATTRIBUTE_CLASS, LICENSE_CLASS);	//class="license"
			depictContext.write("XHTML produced by an unlicensed copy of ");
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_A);	//<xhtml:a>
			depictContext.writeAttribute(null, ELEMENT_A_ATTRIBUTE_HREF, Guise.GUISE_WEB_URI.toString());	//href="http://www.guiseframework.com/"
			depictContext.write(Guise.GUISE_NAME);	//Guise
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_A);	//</xhtml:a>			
			depictContext.write(". Please contact ");
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_A);	//<xhtml:a>
			depictContext.writeAttribute(null, ELEMENT_A_ATTRIBUTE_HREF, "mailto:sales@globalmentor.com");	//href="mailto:sales@globalmentor.com"
			depictContext.write("GlobalMentor, Inc.");
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_A);	//</xhtml:a>			
			depictContext.write(" for licensing information.");
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_P);	//</xhtml:p>
			depictContext.write("\n");
		}
			//busy component
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//<xhtml:div> (busy)
		final String busyID=getBusyID(session.getApplicationFrame());	//get the busy ID
		depictContext.writeAttribute(null, ATTRIBUTE_ID, busyID);	//id="xxx:busy"		
//TODO del when works		depictContext.writeAttribute(null, ATTRIBUTE_STYLE, "position:absolute;width:300px;height:200px;display:none;");	//style="position:absolute;display:none;"	//TODO use constants; improve
		depictContext.writeAttribute(null, ATTRIBUTE_STYLE, "position:absolute;display:none;");	//style="position:absolute;display:none;"	//TODO use constants; improve
		if(busyComponent==null)	//if no busy component has yet been created
		{
			busyComponent=session.createBusyComponent();	//ask the session to create a new busy component
		}
		busyComponent.depict();	//update the busy component		
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//</xhtml:div> (busy)
		final WebUserAgentProduct userAgent=getPlatform().getClientProduct();	//get the user agent
		if(userAgent.getBrand()==WebUserAgentProduct.Brand.INTERNET_EXPLORER && userAgent.getVersionNumber()<7)	//if this is IE6, create IFrames that will be used to prevent certain controls from showing through
		{
				//modal IFrame
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_IFRAME);	//<xhtml:iframe>
			final String modalIFrameID=getModalIFrameID(session.getApplicationFrame());	//get the modal IFrame ID
			depictContext.writeAttribute(null, ATTRIBUTE_ID, modalIFrameID);	//id="xxx:modalIFrame"		
			depictContext.writeAttribute(null, ELEMENT_IFRAME_ATTRIBUTE_SRC, application.resolvePath(GUISE_EMPTY_HTML_DOCUMENT_PATH));	//src="guise/documents/empty.html"	(resolved to context) TODO wouldn't it be better to resolve the path to the session, now, as a convenience?
			depictContext.writeAttribute(null, ELEMENT_IFRAME_ATTRIBUTE_FRAMEBORDER, "0");	//frameborder="0"
			depictContext.writeAttribute(null, ATTRIBUTE_STYLE, "display:none;position:absolute;top:0px;left:0px;filter:progid:DXImageTransform.Microsoft.Alpha(style=0,opacity=0);");	//TODO maybe allow this this to be set using CSS
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_IFRAME);	//</xhtml:iframe>
				//flyover IFrame
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_IFRAME);	//<xhtml:iframe>
			final String flyoverIFrameID=getFlyoverIFrameID(session.getApplicationFrame());	//get the flyover IFrame ID
			depictContext.writeAttribute(null, ATTRIBUTE_ID, flyoverIFrameID);	//id="xxx:flyoverIFrame"		
			depictContext.writeAttribute(null, ELEMENT_IFRAME_ATTRIBUTE_SRC, application.resolvePath(GUISE_EMPTY_HTML_DOCUMENT_PATH));	//src="guise/documents/empty.html"	(resolved to context) TODO wouldn't it be better to resolve the path to the session, now, as a convenience?
			depictContext.writeAttribute(null, ELEMENT_IFRAME_ATTRIBUTE_FRAMEBORDER, "0");	//frameborder="0"
			depictContext.writeAttribute(null, ATTRIBUTE_STYLE, "display:none;position:absolute;top:0px;left:0px;filter:progid:DXImageTransform.Microsoft.Alpha(style=0,opacity=0);");	//TODO maybe allow this this to be set using CSS
//TODO del when works				depictContext.writeAttribute(null, ATTRIBUTE_STYLE, "display:none;position:absolute;top:0px;left:0px;");	//TODO maybe allow this this to be set using CSS
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_IFRAME);	//</xhtml:iframe>
		}
			//init IFrame
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_IFRAME);	//<xhtml:iframe>
		final String initIFrameID=getInitIFrameID(session.getApplicationFrame());	//get the init IFrame ID
		depictContext.writeAttribute(null, ATTRIBUTE_ID, initIFrameID);	//id="xxx:initIFrame"		
		depictContext.writeAttribute(null, ELEMENT_IFRAME_ATTRIBUTE_SRC, application.resolvePath(GUISE_EMPTY_HTML_DOCUMENT_PATH));	//src="guise/documents/empty.html"	(resolved to context) TODO wouldn't it be better to resolve the path to the session, now, as a convenience?
		depictContext.writeAttribute(null, ELEMENT_IFRAME_ATTRIBUTE_FRAMEBORDER, "0");	//frameborder="0"
		depictContext.writeAttribute(null, ATTRIBUTE_STYLE, "display:block;position:absolute;top:0px;left:0px;width:100%;height:100%;filter:progid:DXImageTransform.Microsoft.Alpha(style=0,opacity=0);z-index:9999;");	//TODO maybe allow this this to be set using CSS
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_IFRAME);	//</xhtml:iframe>
			//<xhtml:script> (internal)
			//this script sets up the body length property 
		final int bodyLength=depictContext.getDepictStringBuilder().length();	//get the length of the body
		depictContext.write("\t");
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_SCRIPT, false);	//<xhtml:script> (explicitly don't create an empty <xhtml:script> element, otherwise IE wouldn't recognize it)
		depictContext.writeAttribute(null, ELEMENT_SCRIPT_ATTRIBUTE_TYPE, JAVASCRIPT_CONTENT_TYPE.toString());	//type="text/javascript"
		depictContext.write('\n');
		depictContext.write("\t\t");
		depictContext.writeLiteral("document.bodyLength="+bodyLength+";");	//document.bodyLength=bodyLength TODO use a constant for the variable name
		depictContext.write('\n');
		depictContext.write('\t');
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_SCRIPT);	//</xhtml:script>
		depictContext.write("\n");		
		super.depictEnd();	//do the default ending rendering
		depictContext.write("\n");
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_HTML);	//</xhtml:html>
	}

	/**Determines whether the given component or any of its children contains a resource import control (a control with a resource import value model).
	@param component The component to check for a resource input value model.
	@return <code>true</code> if the component or any of its children contain a resource import control.
	@see ValueControl
	@see ValueModel
	@see ResourceImport
	*/
	protected static boolean hasResourceImportControl(final Component component)
	{
		if(component instanceof ValueControl)	//if this component is a value control
		{
			final Class<?> valueClass=((ValueControl<?>)component).getValueClass();	//get the value class of the value model
			if(ResourceImport.class.isAssignableFrom(valueClass))	//if the value model represents some type of resource import
			{
				return true;	//we found a resource import control
			}
		}
		if(component instanceof CompositeComponent)	//if this component is composed of other components
		{
			for(final Component childComponent:((CompositeComponent)component).getChildren())	//look at each child class component
			{
				if(hasResourceImportControl(childComponent))	//if this child has a resource import control
				{
					return true;	//we found a child with a resource import control
				}
			}
		}
		return false;	//neither this component nor any of its children is a resource import control
	}

}
