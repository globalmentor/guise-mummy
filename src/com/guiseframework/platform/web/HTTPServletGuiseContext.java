package com.guiseframework.platform.web;

import java.io.*;
import java.net.URI;
import java.util.*;

import static java.util.Collections.*;

import javax.mail.internet.ContentType;
import javax.servlet.http.*;
import javax.xml.parsers.*;

import static com.garretwilson.io.ContentTypeConstants.*;
import static com.garretwilson.io.ContentTypeUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.servlet.http.HttpServletUtilities.*;

import com.garretwilson.servlet.http.HttpServletUtilities;
import com.garretwilson.text.CharacterEncoding;
import static com.garretwilson.text.FormatUtilities.*;
//TODO del if not needed import static com.garretwilson.text.xml.XMLConstants.*;
import com.garretwilson.text.xml.QualifiedName;
import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;
import com.garretwilson.text.xml.xpath.XPath;

import static com.garretwilson.text.CharacterEncodingConstants.*;
import com.garretwilson.util.*;
import static com.garretwilson.util.ArrayUtilities.*;

import com.guiseframework.Destination;
import com.guiseframework.GuiseSession;
import com.guiseframework.context.GuiseContext;
import com.guiseframework.context.text.xml.AbstractXMLGuiseContext;
import com.guiseframework.controller.ControlEvent;
import com.guiseframework.model.FileItemResourceImport;
import com.guiseframework.platform.web.css.CSSStylesheet;
import com.guiseframework.platform.web.css.ClassSelector;
import com.guiseframework.platform.web.css.SimpleSelector;
import com.guiseframework.platform.web.css.TypeSelector;
import com.guiseframework.platform.web.css.GuiseCSSProcessor.IE6FixClass;

import org.apache.commons.fileupload.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**The Guise context of an HTTP servlet.
The output stream defaults to <code>text/plain</code> encoded in <code>UTF-8</code>.
@author Garret Wilson
*/
public class HTTPServletGuiseContext extends AbstractXMLGuiseContext
{

	/**The list of IE6 fix classes.
	This map is populated by {@link #fixIE6Stylesheet(CSSStylesheet)}.
	*/
	private final List<IE6FixClass> ie6FixClasses=new ArrayList<IE6FixClass>();

		/**The list of IE6 fix classes.
		This map is populated by {@link #fixIE6Stylesheet(CSSStylesheet)}.
		*/
		public List<IE6FixClass> getIE6FixClasses() {return ie6FixClasses;}

	/**The HTTP servlet request.*/
	private final HttpServletRequest request;

		/**@return The HTTP servlet request.*/
		protected HttpServletRequest getRequest() {return request;}

	/**The HTTP servlet response.*/
	private final HttpServletResponse response;

		/**@return The HTTP servlet response.*/
		protected HttpServletResponse getResponse() {return response;}

	/**The current absolute navigation URI for this context.*/
	private final URI navigationURI;

		/**@return The current absolute navigation URI for this context.*/
		public URI getNavigationURI() {return navigationURI;}

	/**The content type of the request, or <code>null</code> if no content type was specified.*/
//TODO del when not needed	private final ContentType inputContentType;

		/**@return The content type of the request, or <code>null</code> if no content type was specified.*/
//	TODO del when not needed		protected ContentType getInputContentType() {return inputContentType;}

	/**Whether this context represents an AJAX request.*/
	private final boolean isAJAX;

		/**@return Whether this context represents an AJAX request.*/
		public boolean isAJAX() {return isAJAX;}

	/**The current content type of the output.*/
	private ContentType outputContentType=createContentType(TEXT, PLAIN_SUBTYPE);	//default to text/plain

	/**The string builder that holds the current content being collected.*/
//TODO del when works	private final StringBuilder stringBuilder=new StringBuilder();

		/**@return The string builder that holds the current content being collected.*/
//TODO del when works		public StringBuilder getStringBuilder() {return stringBuilder;}

	/**Constructor.
	@param session The Guise user session of which this context is a part.
	@param destination The destination with which this context is associated.
	@param request The HTTP servlet request.
	@param response The HTTP servlet response.
	@exception NullPointerException if the session, destination, request or response is <code>null</code>.
	*/
	public HTTPServletGuiseContext(final GuiseSession session, final Destination destination, final HttpServletRequest request, final HttpServletResponse response)
	{
		super(session, destination);	//construct the parent class
		this.request=checkInstance(request, "Request cannot be null.");
		this.response=checkInstance(response, "Response cannot be null.");
//TODO decide if we want this to include parameters or not		this.navigationURI=URI.create(request.getRequestURL().toString());	//create the absolute navigation URI from the HTTP requested URL
		this.navigationURI=HttpServletUtilities.getRequestURI(request);	//get the absolute navigation URI from the HTTP requested URL

		
		final String contentTypeString=request.getContentType();	//get the request content type
		final ContentType contentType=contentTypeString!=null ? createContentType(contentTypeString) : null;	//create a content type object from the request content type, if there is one
		isAJAX=contentType!=null && GuiseHTTPServlet.GUISE_AJAX_REQUEST_CONTENT_TYPE.match(contentType);	//see if this is a Guise AJAX request
				
/*TODO del
Debug.trace("parameter names:", request.getParameterNames());	//TODO del when finished with dual mulipart+encoded content
Debug.trace("number of parameter names:", request.getParameterNames());
Debug.trace("***********number of distinct parameter keys", parameterListMap.size());
*/
		final ContentType defaultContentType=createContentType(outputContentType.getPrimaryType(), outputContentType.getSubType(), new NameValuePair<String, String>(CHARSET_PARAMETER, UTF_8));	//default to text/plain encoded in UTF-8
		response.setContentType(defaultContentType.toString());	//initialize the default content type and encoding
		setContentLanguage(response, session.getLocale());	//set the response content language
	}

	/**Sets the current view interaction state of this context.
	This implementation exposes the method to the servlet.
	This is a bound property.
	@param newState The new context state.
	@see GuiseContext#STATE_PROPERTY
	*/
	protected void setState(final State newState)
	{
		super.setState(newState);
	}

	/**@return The character encoding currently used for the text output.*/
	public CharacterEncoding getOutputCharacterEncoding()
	{
		return new CharacterEncoding(getResponse().getCharacterEncoding(), NO_BOM);	//return the current output character encoding
	}

	/**@return The current content type of the text output.*/
	public ContentType getOutputContentType() {return outputContentType;}

	/**Sets the content type of the text output.
	This implementation removes all parameters and adds a character set parameter of the current encoding.
	@param contentType The content type of the text output.
	*/
	public void setOutputContentType(final ContentType contentType)
	{
			//default to text/plain encoded in UTF-8 replace the charset parameter with the currently set character set TODO change to really just replace one parameter, instead of removing all others
		this.outputContentType=createContentType(contentType.getPrimaryType(), contentType.getSubType(), new NameValuePair<String, String>(CHARSET_PARAMETER, getOutputCharacterEncoding().getEncoding()));
		getResponse().setContentType(this.outputContentType.toString());	//set the content type of the response, including the current character set
	}

	/**Returns a list of content types accepted by the client.
	@return An array of content types accepted by the client.
	*/
	public ContentType[] getClientAcceptedContentTypes()
	{
		return getAcceptedContentTypes(getRequest());	//return the accepted content types from the request
	}

	/**Determines if the client accepts the given content type.
	Wildcard content types are correctly matched.
	@param contentType The content type to check.
	@return <code>true</code> if the client accept the given content type.
	*/
	public boolean isClientAcceptedContentType(final ContentType contentType)
	{
		return isClientAcceptedContentType(contentType, true);	//check accepted content types, matching wildcards
	}

	/**Determines if the client accepts the given content type.
	@param contentType The content type to check.
	@param matchWildcards <code>true</code> if the content type should be matched against wildcard sequences, as is normal.
	@return <code>true</code> if the client accept the given content type.
	*/
	public boolean isClientAcceptedContentType(final ContentType contentType, final boolean matchWildcards)
	{
		return isAcceptedContentType(getRequest(), contentType, matchWildcards);	//see if the client accepts the content type, matching wildcards if so requested
	}

	/**Returns a list of languages accepted by the client.
	@return An array of languages accepted by the client.
	*/
	public Locale[] getClientAcceptedLanguages()
	{
		return getAcceptedLanguages(getRequest());	//return the accepted languages from the request
	}

	/**Retrieves the value of a given attribute.
	This version modifed any class attributes if there are IE6 fix classes and this is not an AJAX call.
	@param elementQualifedName The qualified name of the element.
	@param attributeQualifiedName The qualified name of the attribute.
	@param attributeValue The default value of the attribute.
	@return The value of the attribute.
	@see #getIE6FixClasses()
	@see #isAJAX()
	*/
	protected String getAttributeValue(final QualifiedName elementQualifiedName, final QualifiedName attributeQualifiedName, String attributeValue)
	{
		if(XHTML_NAMESPACE_URI.toString().equals(elementQualifiedName.getNamespaceURI()) && attributeQualifiedName.getNamespaceURI()==null && ATTRIBUTE_CLASS.equals(attributeQualifiedName.getLocalName()))	//if this is an XHTML class attribute
		{
			if(!isAJAX())	//if this is not an AJAX request (we don't want to update the value for AJAX requests; maybe eventually we should remove this check, as for AJAX there simply won't be any classes to fix)
			{
				final List<IE6FixClass> ie6FixClasses=getIE6FixClasses();	//get the fix classes
				if(!ie6FixClasses.isEmpty())	//if there are IE6 fix classes
				{
//TODO del Debug.trace("ready to fix classes:", attributeValue);
					final Set<String> newClasses=new HashSet<String>();	//we'll collect new classes to add; use a set to keep from getting duplicates of fixed class names (multiple selectors may match the same class that needs fixed)
					final String[] classNames=attributeValue.split("\\s");	//split out the class names
					for(final IE6FixClass ie6FixClass:ie6FixClasses)	//look at each IE6 fix classes
					{
						final List<SimpleSelector> simpleSelectorSequence=ie6FixClass.getSimpleSelectorSequence();	//get this simple selector sequence
//					TODO del Debug.trace("looking at IE6 fix class", ie6FixClass.getFixClass(), "with simple selector count", simpleSelectorSequence.size());
						final TypeSelector typeSelector=simpleSelectorSequence.size()>0 ? asInstance(simpleSelectorSequence.get(0), TypeSelector.class) : null;	//get the first simple selector if it is a type selector
						if(typeSelector==null || typeSelector.getTypeName().equals(elementQualifiedName.getLocalName()))	//make sure the type selector matches the element name
						{
							boolean classesMatch=true;	//see if this element matches all the classes (we don't have to make sure there are multiple classes; if not, the IE6 fix object wouldn't be included in the list
							for(final SimpleSelector simpleSelector:simpleSelectorSequence)	//for each simple selector
							{
								if(simpleSelector!=typeSelector)	//if this isn't the type selector
								{
									if(simpleSelector instanceof ClassSelector)	//if this is a class selector
									{
//									TODO del Debug.trace("looking at class selector:", ((ClassSelector)simpleSelector).getClassName());
										if(!contains(classNames, ((ClassSelector)simpleSelector).getClassName()))	//if this class name is not contained in the list TODO switch to a hash set to make this more efficient
										{
											classesMatch=false;	//all the classes don't match; don't bother fixing things up
											break;	//stop looking at the classes
										}
									}
								}
							}
							if(classesMatch)	//if all the classes selectors match
							{
//							TODO del Debug.trace("adding fix class:", ie6FixClass.getFixClass());
								newClasses.add(ie6FixClass.getFixClass());	//add the fix class to the class names
							}
						}
					}
					if(!newClasses.isEmpty())	//if there are new classes
					{
						addAll(newClasses, classNames);	//add the normal class names to our new classes list
						attributeValue=formatList(new StringBuilder(), ' ', newClasses).toString();	//combine the classes back into one class string
//TODO del Debug.trace("new attribute value:", attributeValue);
					}
				}
			}
		}		
		return attributeValue;	//return the attribute value with no modifications
	}

}
