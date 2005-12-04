package com.javaguise.servlet.http;

import java.util.*;
import static java.util.Collections.*;

import javax.servlet.http.*;

import com.javaguise.GuiseApplication;
import com.javaguise.GuiseSession;

/**Manages Guise sessions for an HTTP web application.
Guise sessions are created and released in conjunction with associated HTTP servlet sessions.
There may be multiple Guise applications within one web application.
@author Garret Wilson
@see GuiseHTTPServlet.HTTPServletGuiseContainer
*/
public class HTTPGuiseSessionManager implements HttpSessionListener
{

	/**The synchronized map of Guise containers keyed to HTTP sessions.*/
	private static final Map<HttpSession, GuiseHTTPServlet.HTTPServletGuiseContainer> guiseContainerMap=synchronizedMap(new HashMap<HttpSession, GuiseHTTPServlet.HTTPServletGuiseContainer>());

	/**Retrieves a session for the given HTTP session.
	This method can only be accessed by classes in the same package.
	@param guiseContainer The Guise container that owns the application. 
	@param guiseApplication The application to install to own the created session..
	@param httpRequest The HTTP request with which the Guise session is to be associated. 
	@return The Guise session associated with the provided HTTP session.
	@exception IllegalArgumentException if the provided HTTP session is not a session from this web application or the HTTP session has been invalidated, and there is therefore no corresponding Guise session.
	*/
	protected static GuiseSession getGuiseSession(final GuiseHTTPServlet.HTTPServletGuiseContainer guiseContainer, final GuiseApplication guiseApplication, final HttpServletRequest httpRequest)
	{
		final HttpSession httpSession=httpRequest.getSession();	//get the current HTTP session from the HTTP request
		guiseContainerMap.put(httpSession, guiseContainer);	//store our Guise container so we'll know with which container this session is associated (this servlet may serve many Guise applications in many Guise containers in the web application)
		return guiseContainer.getGuiseSession(guiseApplication, httpRequest, httpSession);	//ask the Guise application for a Guise session corresponding to the HTTP session
	}
	
	/**Called when an HTTP session is created.
	This implementation does nothing, as there is no way to tell with which Guise container and Guise application application a new Guise application should be associated.
	@param httpSessionEvent Information regarding the created HTTP session.
	@see #getGuiseSession(GuiseHTTPServlet.HTTPServletGuiseContainer, GuiseApplication, HttpServletRequest)
	*/
	public void sessionCreated(final HttpSessionEvent httpSessionEvent)
	{
	}

	/**Called when an HTTP session is invalidated.
	This implementation removes the corresponding Guise session.
	@param httpSessionEvent Information regarding the invalidated HTTP session.
	*/
	public void sessionDestroyed(final HttpSessionEvent httpSessionEvent)
	{
		synchronized(guiseContainerMap)	//don't allow anyone to modify our map of applications while we access it
		{
			final HttpSession httpSession=httpSessionEvent.getSession();	//get the HTTP session just invalidated
			final GuiseHTTPServlet.HTTPServletGuiseContainer guiseContainer=guiseContainerMap.get(httpSession);	//see if we have a Guise container associated with this HTTP request
			if(guiseContainer!=null)	//if we know the Guise container associated with this HTTP request
			{
				guiseContainerMap.remove(httpSession);	//remove the association between this HTTP session and the container
				final GuiseSession guiseSession=guiseContainer.removeGuiseSession(httpSession);	//remove the Guise session associated with the HTTP session in the application
				assert guiseSession!=null : "Guise container associated with HTTP session unexpectedly did not have an associated Guise session.";
			}
		}
	}
}
