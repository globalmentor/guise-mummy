package com.garretwilson.guise.servlet.http;

import java.io.*;
import java.net.URI;
import java.util.*;
import static java.util.Collections.*;

import javax.mail.internet.ContentType;
import javax.servlet.http.*;

import com.garretwilson.guise.context.GuiseContext;
import com.garretwilson.guise.context.text.xml.AbstractXMLGuiseContext;
import com.garretwilson.guise.model.FileItemResourceImport;
import com.garretwilson.guise.session.GuiseSession;
import static com.garretwilson.io.ContentTypeConstants.*;
import static com.garretwilson.io.ContentTypeUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.servlet.http.HttpServletUtilities.*;
import com.garretwilson.text.CharacterEncoding;
import static com.garretwilson.text.CharacterEncodingConstants.*;
import com.garretwilson.util.*;

import org.apache.commons.fileupload.*;

/**The Guise context of an HTTP servlet.
The output stream defaults to <code>text/plain</code> encoded in <code>UTF-8</code>.
@author Garret Wilson
*/
public class HTTPServletGuiseContext extends AbstractXMLGuiseContext<HTTPServletGuiseContext>
{

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

	/**The current content type of the output.*/
	private ContentType outputContentType=createContentType(TEXT, PLAIN_SUBTYPE);	//default to text/plain

	/**Constructor.
	@param session The Guise user session of which this context is a part.
	@param request The HTTP servlet request.
	@param response The HTTP servlet response.
	@exception NullPointerException if the session, request or response is <code>null</code>.
	*/
	public HTTPServletGuiseContext(final GuiseSession<HTTPServletGuiseContext> session, final HttpServletRequest request, final HttpServletResponse response)
	{
		super(session);	//construct the parent class
		this.request=checkNull(request, "Request cannot be null.");
		this.response=checkNull(response, "Response cannot be null.");
		this.navigationURI=URI.create(request.getRequestURL().toString());	//create the absolute navigation URI from the HTTP requested URL
			//populate our parameter map
		final ListMap<Object, Object> parameterListMap=getParameterListMap();	//get the map of parameter lists
		if(FileUpload.isMultipartContent(request))	//if this is multipart/form-data content
		{
			final DiskFileUpload diskFileUpload=new DiskFileUpload();	//create a file upload handler
			diskFileUpload.setSizeMax(-1);	//don't reject anything
			try	//try to parse the file items submitted in the request
			{
				final List fileItems=diskFileUpload.parseRequest(request);	//parse the request
				for(final Object object:fileItems)	//look at each file item
				{
					final FileItem fileItem=(FileItem)object;	//cast the object to a file item
					final String parameterKey=fileItem.getFieldName();	//the parameter key will always be the field name
					final Object parameterValue=fileItem.isFormField() ? fileItem.getString() : new FileItemResourceImport(fileItem);	//if this is a form field, store it normally; otherwise, create a file item resource import object
					parameterListMap.addItem(parameterKey, parameterValue);	//store the value in the parameters
				}
			}
			catch(final FileUploadException fileUploadException)	//if there was an error parsing the files
			{
				throw new IllegalArgumentException("Couldn't parse multipart/form-data request.");
			}
		}
		else	//if this is normal application/x-www-form-urlencoded data
		{
			final Iterator parameterEntryIterator=request.getParameterMap().entrySet().iterator();	//get an iterator to the parameter entries
			while(parameterEntryIterator.hasNext())	//while there are more parameter entries
			{
				final Map.Entry parameterEntry=(Map.Entry)parameterEntryIterator.next();	//get the next parameter entry
				final String parameterKey=(String)parameterEntry.getKey();	//get the parameter key
				final String[] parameterValues=(String[])parameterEntry.getValue();	//get the parameter values
				final List<Object> parameterValueList=new ArrayList<Object>(parameterValues.length);	//create a list to hold the parameters
				addAll(parameterValueList, parameterValues);	//add all the parameter values to our list
				parameterListMap.put(parameterKey, parameterValueList);	//store the the array of values as a list, keyed to the value
			}
		}
		final ContentType defaultContentType=createContentType(outputContentType.getPrimaryType(), outputContentType.getSubType(), new NameValuePair<String, String>(CHARSET_PARAMETER, UTF_8));	//default to text/plain encoded in UTF-8
		response.setContentType(defaultContentType.toString());	//initialize the default content type and encoding
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

	/**@return A writer for rendering text content.
	@exception IOException if there is an error getting the writer.
	*/
	public Writer getWriter() throws IOException
	{
		return getResponse().getWriter();	//get the writer to the response
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

}
