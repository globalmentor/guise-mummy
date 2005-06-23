package com.garretwilson.guise.context.text;

import java.io.*;
import java.util.*;

import static java.util.Arrays.*;

import javax.mail.internet.ContentType;
import javax.servlet.http.*;

import com.garretwilson.guise.Guise;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.servlet.http.HttpServletUtilities.*;
import com.garretwilson.util.*;

/**The default implementation for providing the context of an HTTP servlet.
@author Garret Wilson
*/
public class DefaultHTTPServletGuiseContext extends AbstractTextGuiseContext
{

	/**The HTTP servlet request.*/
	private final HttpServletRequest request;

		/**@return The HTTP servlet request.*/
		protected HttpServletRequest getRequest() {return request;}

	/**The HTTP servlet response.*/
	private final HttpServletResponse response;

		/**@return The HTTP servlet response.*/
		protected HttpServletResponse getResponse() {return response;}

	/**Constructor.
	@param guise The instance of Guise of which this context is a part.
	@param request The HTTP servlet request.
	@param response The HTTP servlet response.
	@exception NullPointerException if either request or response is <code>null</code>.
	*/
	public DefaultHTTPServletGuiseContext(final Guise<? extends AbstractTextGuiseContext> guise, final HttpServletRequest request, final HttpServletResponse response)
	{
		super(guise);	//construct the parent class
		this.request=checkNull(request, "Request cannot be null.");
		this.response=checkNull(response, "Response cannot be null.");		
			//populate our parameter map
		final ListMap<Object, Object> parameterListMap=getParameterListMap();	//get the map of parameter lists
		final Map<String, Object[]> parameterMap=(Map<String, Object[]>)request.getParameterMap();	//get the request parameter map, casting it to a generic type for ease of use
		for(final Map.Entry<String, Object[]> parameterMapEntry:parameterMap.entrySet())	//for each entry in the map of parameters
		{
			parameterListMap.put(parameterMapEntry.getKey(), asList(parameterMapEntry.getValue()));	//store the the array of values as a list, keyed to the value			
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
