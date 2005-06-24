package com.garretwilson.guise.servlet.http;

import java.io.*;
import java.net.URI;
import java.util.*;
import static java.util.Collections.*;

import javax.mail.internet.ContentType;
import javax.servlet.http.*;

import com.garretwilson.guise.context.text.AbstractTextGuiseContext;
import com.garretwilson.guise.session.GuiseSession;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.servlet.http.HttpServletUtilities.*;
import com.garretwilson.util.*;

/**The Guise context of an HTTP servlet.
@author Garret Wilson
*/
public class HTTPServletGuiseContext extends AbstractTextGuiseContext<HTTPServletGuiseContext>
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

	/**@return A writer for rendering text content.
	@exception IOException if there is an error getting the writer.
	*/
	public Writer getWriter() throws IOException
	{
		return getResponse().getWriter();	//get the writer to the response
	}

	/**Sets the content type of the text output.
	@param contentType The content type of the text output.
	*/
	public void setOutputContentType(final ContentType contentType)	//TODO allow a way to retrieve this value later
	{
		getResponse().setContentType(contentType.toString());	//set the content type of the response
	}
	
	/**Returns a list of content types accepted by the client.
	@return An array of content types accepted by the client.
	*/
	public ContentType[] getClientAcceptedContentTypes()
	{
		return getAcceptedContentTypes(getRequest());	//return the accepted content types in the request
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
}
