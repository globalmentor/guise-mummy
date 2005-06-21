package com.garretwilson.guise.context.text;

import java.io.*;

import javax.servlet.http.*;

import com.garretwilson.guise.Guise;

import static com.garretwilson.lang.ObjectUtilities.*;

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
	}
	/**@return A writer for rendering text content.
	@exception IOException if there is an error getting the writer.
	*/
	public Writer getWriter() throws IOException
	{
		return getResponse().getWriter();	//get the writer to the response
	}

}
